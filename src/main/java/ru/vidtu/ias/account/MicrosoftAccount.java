/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2024 VidTu
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

package ru.vidtu.ias.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.auth.MSAuth;
import ru.vidtu.ias.crypt.Crypt;
import ru.vidtu.ias.utils.Holder;
import ru.vidtu.ias.utils.IUtils;
import ru.vidtu.ias.utils.ProbableException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Encrypted Microsoft account instance.
 *
 * @author VidTu
 */
public final class MicrosoftAccount implements Account {
    /**
     * Initializing login.
     */
    public static final String INITIALIZING = "ias.login.initializing";

    /**
     * Starting server.
     */
    public static final String SERVER = "ias.login.server";

    /**
     * Link has been open in browser.
     */
    public static final String BROWSER = "ias.login.link";

    /**
     * Processing response.
     */
    public static final String PROCESSING = "ias.login.processing";

    /**
     * Decrypting tokens.
     */
    public static final String DECRYPTING = "ias.login.decrypting";

    /**
     * Encrypting tokens.
     */
    public static final String ENCRYPTING = "ias.login.encrypting";

    /**
     * Creating services.
     */
    public static final String SERVICES = "ias.login.services";

    /**
     * Converting Microsoft Authentication Code (MSAC) to Microsoft Access (MSA) and Microsoft Refresh (MSR) tokens.
     */
    public static final String MSAC_TO_MSA_MSR = "ias.login.msacToMsaMsr";

    /**
     * Converting Microsoft Refresh (MSR) token to Microsoft Access (MSA) and Microsoft Refresh (MSR) tokens.
     */
    public static final String MSR_TO_MSA_MSR = "ias.login.msrToMsaMsr";

    /**
     * Converting Microsoft Access (MSA) token to Xbox Live (XBL) token.
     */
    public static final String MSA_TO_XBL = "ias.login.msaToXbl";

    /**
     * Converting Xbox Live (XBL) token to Xbox Secure Token Service (XSTS) token.
     */
    public static final String XBL_TO_XSTS = "ias.login.xblToXsts";

    /**
     * Converting Xbox Secure Token Service (XSTS) token to Minecraft Access (MCA) token.
     */
    public static final String XSTS_TO_MCA = "ias.login.xstsToMca";

    /**
     * Converting Minecraft Access (MCA) token to Minecraft Profile. (MCP)
     */
    public static final String MCA_TO_MCP = "ias.login.mcaToMcp";

    /**
     * Finalizing login.
     */
    public static final String FINALIZING = "ias.login.finalizing";

    /**
     * Logger for this class.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("IAS/MicrosoftAccount");

    /**
     * Account UUID.
     */
    private UUID uuid;

    /**
     * Account name.
     */
    private String name;

    /**
     * Encrypted account data.
     */
    private byte[] data;

    /**
     * Creates a new Microsoft account.
     *
     * @param uuid Account UUID
     * @param name Account name
     * @param data Encrypted account data
     */
    public MicrosoftAccount(UUID uuid, String name, byte[] data) {
        this.uuid = uuid;
        this.name = name;
        this.data = data;
    }

    @Override
    public UUID uuid() {
        return this.uuid;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public boolean canLogin() {
        // Online accounts should be loggable.
        return true;
    }

    @Override
    public void login(LoginHandler handler) {
        try {
            // Log it and display progress.
            LOGGER.info("IAS: Logging (Microsoft) as {}/{}", this.uuid, this.name);
            handler.stage(INITIALIZING);

            // Create the authenticator.
            MSAuth auth = new MSAuth(IAS.userAgent(), IAS.CLIENT_ID, null, Duration.ofSeconds(15L), IAS.executor());

            // Value holders.
            Holder<Crypt> crypt = new Holder<>();
            Holder<String> access = new Holder<>();
            Holder<String> refresh = new Holder<>();
            Holder<Boolean> changed = new Holder<>(false);

            // Read the crypt.
            CompletableFuture<Crypt> future;
            byte[] crypted;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(this.data);
                 DataInputStream in = new DataInputStream(byteIn)) {
                // Read and process the crypt.
                future = Crypt.decrypt(in, handler::password);

                // Crypted data.
                crypted = in.readAllBytes();
            }

            // Decrypt.
            future.thenApplyAsync(value -> {
                // Crypt was cancelled.
                if (value == null) {
                    // Log it.
                    LOGGER.info("IAS: Crypt was cancelled.");

                    // Return cancel.
                    return null;
                }

                // Log it and display progress.
                LOGGER.info("IAS: Decrypting tokens...");
                handler.stage(DECRYPTING);

                // Set the crypt.
                crypt.set(value);

                // Decrypt.
                return value.decrypt(crypted);
            }, IAS.executor()).thenApplyAsync(value -> {
                // Skip if cancelled.
                if (value == null) return false;

                // Read the decrypted data into tokens.
                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(value);
                     DataInputStream in = new DataInputStream(byteIn)) {
                    // Read the access token.
                    access.set(in.readUTF());

                    // Read the refresh token.
                    refresh.set(in.readUTF());

                    // Verify the buffer.
                    int available = in.available();
                    if (available != 0) {
                        throw new IOException("Leftover: " + in.available());
                    }

                    // Return continue.
                    return true;
                } catch (Throwable t) {
                    throw new RuntimeException("Unable to read the tokens.", t);
                }
            }, IAS.executor()).thenComposeAsync(value -> {
                // Skip if cancelled.
                if (!value) return CompletableFuture.completedFuture(null);

                // Log it and display progress.
                LOGGER.info("IAS: Converting MCA to MCP... (stored)");
                handler.stage(MCA_TO_MCP);

                // Convert MCA to MCP.
                return auth.mcaToMcp(access.get()).exceptionallyComposeAsync(original -> {
                    // Log it and display progress.
                    LOGGER.warn("IAS: MCA is (probably) expired. Refreshing...");
                    LOGGER.info("IAS: Converting MSR to MSA/MSR...");
                    handler.stage(MSR_TO_MSA_MSR);

                    // Convert MSR to MSA/MSR.
                    return auth.msrToMsaMsr(refresh.get()).thenComposeAsync(ms -> {
                        // Update the refresh token.
                        refresh.set(ms.refresh());

                        // Log it and display progress.
                        LOGGER.info("IAS: Converting MSA to XBL...");
                        handler.stage(MSA_TO_XBL);

                        // Convert MSA to XBL.
                        return auth.msaToXbl(ms.access());
                    }, IAS.executor()).thenComposeAsync(xbl -> {
                        // Log it and display progress.
                        LOGGER.info("IAS: Converting XBL to XSTS...");
                        handler.stage(XBL_TO_XSTS);

                        // Convert XBL to XSTS.
                        return auth.xblToXsts(xbl.token(), xbl.hash());
                    }, IAS.executor()).thenComposeAsync(xsts -> {
                        // Log it and display progress.
                        LOGGER.info("IAS: Converting XSTS to MCA...");
                        handler.stage(XSTS_TO_MCA);

                        // Convert XSTS to MCA.
                        return auth.xstsToMca(xsts.token(), xsts.hash());
                    }, IAS.executor()).thenComposeAsync(token -> {
                        // Update the access token.
                        access.set(token);

                        // Log it and display progress.
                        LOGGER.info("IAS: Converting MCA TO MCP... (refreshed)");
                        handler.stage(MCA_TO_MCP);

                        // Convert MCA to MCP.
                        return auth.mcaToMcp(token);
                    }, IAS.executor()).exceptionallyAsync(t -> {
                        t.addSuppressed(original);

                        // Probable case - no internet connection.
                        if (IUtils.anyInCausalChain(t, err -> err instanceof UnresolvedAddressException || err instanceof NoRouteToHostException || err instanceof HttpTimeoutException)) {
                            throw new ProbableException("ias.error.connect", t);
                        }

                        // Handle error.
                        throw new RuntimeException("Unable to perform MSR auth.", t);
                    }, IAS.executor()).thenApplyAsync(profile -> {
                        // Log it and display progress.
                        LOGGER.info("IAS: Encrypting tokens...");
                        handler.stage(ENCRYPTING);

                        // Write the tokens.
                        byte[] unencrypted;
                        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                             DataOutputStream out = new DataOutputStream(byteOut)) {
                            // Write the access token.
                            out.writeUTF(access.get());

                            // Write the refresh token.
                            out.writeUTF(refresh.get());

                            // Flush it.
                            unencrypted = byteOut.toByteArray();
                        } catch (Throwable t) {
                            throw new RuntimeException("Unable to write the tokens.", t);
                        }

                        // Encrypt the tokens.
                        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                             DataOutputStream out = new DataOutputStream(byteOut)) {
                            // Encrypt.
                            byte[] encrypted = crypt.get().encrypt(unencrypted);

                            // Write type.
                            Crypt.encrypt(out, crypt.get());

                            // Write data.
                            out.write(encrypted);

                            // Flush it.
                            this.data = byteOut.toByteArray();
                            changed.set(true);
                        } catch (Throwable t) {
                            throw new RuntimeException("Unable to encrypt the tokens.", t);
                        }

                        // Return the profile as-is.
                        return profile;
                    }, IAS.executor()).exceptionallyAsync(t -> {
                        // Rethrow. (adding original)
                        t.addSuppressed(original);
                        throw new RuntimeException("Unable to refresh MSR.", t);
                    }, IAS.executor());
                }, IAS.executor());
            }, IAS.executor()).thenAcceptAsync(profile -> {
                // Skip if null.
                if (profile == null) return;

                // Authentication successful, refresh the profile.
                UUID uuid = profile.uuid();
                String name = profile.name();
                if (!this.uuid.equals(uuid) || !this.name.equals(name)) {
                    this.uuid = profile.uuid();
                    this.name = profile.name();
                    changed.set(true);
                }

                // Log it and display progress.
                LOGGER.info("IAS: Successful login as {}", profile);
                handler.stage(FINALIZING);

                // Create and return the data.
                LoginData login = new LoginData(this.name, this.uuid, access.get(), true);
                handler.success(login, changed.get());
            }, IAS.executor()).exceptionallyAsync(t -> {
                // Handle error.
                handler.error(new RuntimeException("Unable to login as MS account", t));

                // Return null.
                return null;
            }, IAS.executor());
        } catch (Throwable t) {
            // Handle.
            handler.error(new RuntimeException("Unable to begin MS auth.", t));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MicrosoftAccount that)) return false;
        return Objects.equals(this.uuid, that.uuid) && Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(this.uuid);
        result = 31 * result + Objects.hashCode(this.name);
        return result;
    }

    @Override
    public String toString() {
        return "MicrosoftAccount{" +
                "uuid=" + this.uuid +
                ", name='" + this.name + '\'' +
                ", data='[DATA]'" +
                '}';
    }

    @Override
    public void write(DataOutput out) throws IOException {
        // Write the UUID.
        out.writeLong(this.uuid.getMostSignificantBits());
        out.writeLong(this.uuid.getLeastSignificantBits());

        // Write the name.
        out.writeUTF(this.name);

        // Write the data.
        out.writeShort(this.data.length);
        out.write(this.data);
    }

    /**
     * Reads the account from the input.
     *
     * @param in Target input
     * @return Read account
     * @throws IOException On I/O error
     */
    public static MicrosoftAccount read(DataInput in) throws IOException {
        // Read the UUID.
        UUID uuid = new UUID(in.readLong(), in.readLong());

        // Read the name.
        String name = in.readUTF();

        // Read the data.
        int length = in.readUnsignedShort();
        byte[] data = new byte[length];
        in.readFully(data);

        // Create and return.
        return new MicrosoftAccount(uuid, name, data);
    }
}
