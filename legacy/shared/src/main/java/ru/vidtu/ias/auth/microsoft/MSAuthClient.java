/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2025 VidTu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package ru.vidtu.ias.auth.microsoft;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.MicrosoftAccount;
import ru.vidtu.ias.auth.handlers.CreateHandler;
import ru.vidtu.ias.auth.microsoft.fields.DeviceAuth;
import ru.vidtu.ias.auth.microsoft.fields.MSTokens;
import ru.vidtu.ias.crypt.Crypt;
import ru.vidtu.ias.utils.Holder;
import ru.vidtu.ias.utils.IUtils;
import ru.vidtu.ias.utils.exceptions.DevicePendingException;
import ru.vidtu.ias.utils.exceptions.FriendlyException;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client for MS authentication.
 *
 * @author VidTu
 */
public final class MSAuthClient implements Closeable {
    /**
     * Logger for this class.
     */
    @NotNull
    public static final Logger LOGGER = LoggerFactory.getLogger("IAS/MSAuthClient");

    /**
     * Account crypt.
     */
    @NotNull
    private final Crypt crypt;

    /**
     * Login handler.
     */
    @NotNull
    private final CreateHandler handler;

    /**
     * Device auth.
     */
    private DeviceAuth auth;

    /**
     * Expire instant.
     */
    @Nullable
    private Instant expire;

    /**
     * Next poll instant.
     */
    @Nullable
    private Instant poll;

    /**
     * Polling task, if any.
     */
    @Nullable
    private ScheduledFuture<?> task;

    /**
     * Creates an HTTP client for MS auth.
     *
     * @param crypt   Account crypt
     * @param handler Creation handler
     */
    @Contract(pure = true)
    public MSAuthClient(@NotNull Crypt crypt, @NotNull CreateHandler handler) {
        this.crypt = crypt;
        this.handler = handler;
    }

    /**
     * Starts the client device auth.
     *
     * @return Future that will complete with device auth, with null (on cancel) or exceptionally
     */
    @CheckReturnValue
    @NotNull
    public CompletableFuture<DeviceAuth> start() {
        // Stop if cancelled.
        if (this.handler.cancelled()) return CompletableFuture.completedFuture(null);

        // Request DAC.
        return MSAuth.requestDac().thenApplyAsync(auth -> {
            // Stop if cancelled.
            if (this.handler.cancelled()) return null;
            LOGGER.info("IAS: Got device auth code.");

            // Flush the auth.
            Duration interval = auth.interval();
            this.auth = auth;
            this.expire = Instant.now().plus(auth.expire());
            this.poll = Instant.now().plus(interval);

            // Flush the task.
            this.close();
            this.task = IAS.executor().scheduleWithFixedDelay(this::tick, interval.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);
            LOGGER.info("IAS: HTTP polling started with delay of {}.", interval);

            // Return as-is.
            return auth;
        }, IAS.executor());
    }

    /**
     * Ticks the task.
     */
    private void tick() {
        try {
            // Stop if cancelled.
            if (this.handler.cancelled()) {
                this.close();
                return;
            }

            // Get state.
            MSTokens ms;
            try {
                ms = MSAuth.dacToMsaMsr(this.auth.device());
            } catch (Throwable t) {
                // Pending exception, ignore.
                if (IUtils.anyInCausalChain(t, DevicePendingException.class::isInstance)) {
                    return;
                }

                // Close and throw for any other.
                this.close();
                this.handler.error(new RuntimeException("HTTP polling error.", t));
                return;
            }

            // Stop task.
            this.close();

            // Stop if cancelled.
            if (this.handler.cancelled()) return;

            // Log it and display progress.
            LOGGER.info("IAS: Processing response...");
            this.handler.stage(MicrosoftAccount.PROCESSING);

            // Value holders.
            Holder<String> access = new Holder<>();
            Holder<byte[]> data = new Holder<>();

            // Extract the MSAC.
            CompletableFuture.completedFuture(null).thenComposeAsync(ignored -> {
                // Stop if cancelled.
                if (this.handler.cancelled()) return CompletableFuture.completedFuture(null);

                // Log it and display progress.
                LOGGER.info("IAS: Converting MSA to XBL...");
                this.handler.stage(MicrosoftAccount.MSA_TO_XBL);

                // Convert MSA to XBL.
                return MSAuth.msaToXbl(ms.access());
            }, IAS.executor()).thenComposeAsync(xbl -> {
                // Stop if cancelled.
                if (xbl == null || this.handler.cancelled()) return CompletableFuture.completedFuture(null);

                // Log it and display progress.
                LOGGER.info("IAS: Converting XBL to XSTS...");
                this.handler.stage(MicrosoftAccount.XBL_TO_XSTS);

                // Convert XBL to XSTS.
                return MSAuth.xblToXsts(xbl.token(), xbl.hash());
            }, IAS.executor()).thenComposeAsync(xsts -> {
                // Stop if cancelled.
                if (xsts == null || this.handler.cancelled()) return CompletableFuture.completedFuture(null);

                // Log it and display progress.
                LOGGER.info("IAS: Converting XSTS to MCA...");
                this.handler.stage(MicrosoftAccount.XSTS_TO_MCA);

                // Convert XSTS to MCA.
                return MSAuth.xstsToMca(xsts.token(), xsts.hash());
            }, IAS.executor()).thenComposeAsync(token -> {
                // Stop if cancelled.
                if (token == null || this.handler.cancelled()) return CompletableFuture.completedFuture(null);

                // Update the access token.
                access.set(token);

                // Log it and display progress.
                LOGGER.info("IAS: Converting MCA to MCP...");
                this.handler.stage(MicrosoftAccount.MCA_TO_MCP);

                // Convert MCA to MCP.
                return MSAuth.mcaToMcp(token);
            }, IAS.executor()).exceptionallyAsync(t -> {
                // Probable case - no internet connection.
                if (IUtils.anyInCausalChain(t, err -> err instanceof UnresolvedAddressException || err instanceof NoRouteToHostException || err instanceof HttpTimeoutException || err instanceof ConnectException)) {
                    throw new FriendlyException("Unable to connect to MS servers.", t,  "ias.error.connect");
                }

                // Handle error.
                throw new RuntimeException("Unable to perform MS auth.", t);
            }, IAS.executor()).thenApplyAsync(profile -> {
                // Stop if cancelled.
                if (profile == null || this.handler.cancelled()) return null;

                // Log it and display progress.
                LOGGER.info("IAS: Encrypting tokens...");
                this.handler.stage(MicrosoftAccount.ENCRYPTING);

                // Write the tokens.
                byte[] unencrypted;
                try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                     DataOutputStream out = new DataOutputStream(byteOut)) {
                    // Write the access token.
                    out.writeUTF(access.get());

                    // Write the refresh token.
                    out.writeUTF(ms.refresh());

                    // Flush it.
                    unencrypted = byteOut.toByteArray();
                } catch (Throwable t) {
                    throw new RuntimeException("Unable to write the tokens.", t);
                }

                // Encrypt the tokens.
                try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                     DataOutputStream out = new DataOutputStream(byteOut)) {
                    // Encrypt.
                    byte[] encrypted = this.crypt.encrypt(unencrypted);

                    // Write data.
                    out.writeUTF(this.crypt.type());
                    out.write(encrypted);

                    // Flush it.
                    data.set(byteOut.toByteArray());
                } catch (Throwable t) {
                    throw new RuntimeException("Unable to encrypt the tokens.", t);
                }

                // Return the profile as-is.
                return profile;
            }, IAS.executor()).thenAcceptAsync(profile -> {
                // Stop if cancelled.
                if (profile == null || this.handler.cancelled()) return;

                // Authentication successful, refresh the profile.
                UUID uuid = profile.uuid();
                String name = profile.name();

                // Log it and display progress.
                LOGGER.info("IAS: Successfully added {}", profile);
                this.handler.stage(MicrosoftAccount.FINALIZING);

                // Create and return the data.
                MicrosoftAccount account = new MicrosoftAccount(this.crypt.insecure(), uuid, name, data.get());
                this.handler.success(account);
            }, IAS.executor()).exceptionallyAsync(t -> {
                // Handle error.
                this.handler.error(new RuntimeException("Unable to create an MS account.", t));

                // Return null.
                return null;
            }, IAS.executor());
        } catch (Throwable t) {
            // Handle.
            this.handler.error(new RuntimeException("Unable to finalize MS auth.", t));
        }
    }

    @Override
    public void close() {
        // Cancel any task.
        if (this.task != null) {
            this.task.cancel(false);
            this.task = null;
            LOGGER.info("IAS: HTTP polling stopped.");
        }
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String toString() {
        return "MSAuthClient{" +
                "crypt=" + this.crypt +
                ", expire=" + this.expire +
                ", poll=" + this.poll +
                '}';
    }
}
