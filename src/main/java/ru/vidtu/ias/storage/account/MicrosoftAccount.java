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

package ru.vidtu.ias.storage.account;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.User;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.auth.handlers.LoginHandler;
import ru.vidtu.ias.auth.microsoft.MSAuth;
import ru.vidtu.ias.platform.IStonecutter;
import ru.vidtu.ias.storage.crypt.Crypt;
import ru.vidtu.ias.utils.Holder;
import ru.vidtu.ias.utils.IUtils;
import ru.vidtu.ias.utils.exceptions.FriendlyException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.UTFDataFormatException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Encrypted Microsoft account instance.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class MicrosoftAccount extends Account {
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
     * Link has been open in browser.
     */
    public static final String CLIENT_BROWSER = "ias.login.linkClient";

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
     * Account type for the hover tooltip.
     */
    private static final Component TIP_TYPE = IStonecutter.translate("ias.accounts.tip.type.microsoft");

    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LogManager.getLogger("IAS/MicrosoftAccount");

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
     * An immutable list of account hover tooltip lines.
     */
    @Unmodifiable
    private ImmutableList<Component> tip;

    /**
     * Creates a new Microsoft account.
     *
     * @param uuid Account UUID
     * @param name Account name
     * @param data Encrypted account data
     */
    @Contract(pure = true)
    public MicrosoftAccount(UUID uuid, String name, byte[] data) {
        // Call super.
        super(/*online=*/true);

        // Validate.
        assert data != null : "IAS: Parameter 'data' is null. (uuid: " + uuid + ", name: " + name + ", account: " + this + ')';
        assert uuid != null : "IAS: Parameter 'uuid' is null. (name: " + name + ", data: " + data.length + " BYTES, account: " + this + ')';
        assert name != null : "IAS: Parameter 'name' is null. (uuid: " + uuid + ", data: " + data.length + " BYTES, account: " + this + ')';
        assert uuid.version() == 4 : "IAS: UUID version is not 4. (name: " + name + ", uuid: " + uuid + ", data: " + data.length + " BYTES, account: " + this + ", uuidVersion" + ')';
        assert !name.isBlank() : "IAS: Name is blank. (name: " + name + ", uuid: " + uuid + ", data: " + data.length + " BYTES, account: " + this + ')';
        assert name.length() <= 16 : "IAS: Name is longer than 16 characters. (name: " + name + ", uuid: " + uuid + ", data: " + data.length + " BYTES, account: " + this + ", nameLength: " + name.length() + ')';
        assert data.length != 0 : "IAS: Data is empty. (name: " + name + ", uuid: " + uuid + ", account: " + this + ')';

        // Assign.
        this.uuid = uuid;
        this.name = name.intern(); // Implicit NPE for 'name'
        this.data = data.clone(); // Implicit NPE for 'data'

        // Create.
        this.tip = ImmutableList.of(
                IStonecutter.translate("options.generic_value", IStonecutter.translate("ias.accounts.tip.nick"), this.name),
                IStonecutter.translate("options.generic_value", IStonecutter.translate("ias.accounts.tip.uuid"), this.uuid.toString().intern()),
                IStonecutter.translate("options.generic_value", IStonecutter.translate("ias.accounts.tip.type"), TIP_TYPE)
        );
    }

    /**
     * Gets the UUID. Microsoft account UUIDs may technically change, but this is unlikely.
     *
     * @return Account UUID
     * @see #name()
     * @see #skin()
     */
    @Contract(pure = true)
    @Override
    public UUID uuid() {
        return this.uuid;
    }

    /**
     * Gets the name. Microsoft account names may be changed via the website and therefore may change.
     *
     * @return Account name
     * @see #uuid()
     */
    @Contract(pure = true)
    @Override
    public String name() {
        return this.name;
    }

    /**
     * Gets the skin. Microsoft accounts always return the same value as {@link #uuid()}.
     *
     * @return Account skin UUID, exactly the same as {@link #uuid()}
     * @see #uuid()
     */
    @Contract(pure = true)
    @Override
    public UUID skin() {
        return this.uuid;
    }

    /**
     * Gets the tip.
     *
     * @return An immutable list of account hover tooltip lines
     */
    @Contract(pure = true)
    @Unmodifiable
    public ImmutableList<Component> tip() {
        return this.tip;
    }

    @Override
    public void login(LoginHandler handler) {
        try {
            // Skip if cancelled.
            if (handler.cancelled()) return;

            // Log it and display progress.
            LOGGER.info("IAS: Logging (Microsoft) as {}/{}", this.uuid, this.name);
            handler.stage(INITIALIZING);

            // Value holders.
            Holder<Crypt> crypt = new Holder<>();
            Holder<String> access = new Holder<>();
            Holder<String> refresh = new Holder<>();
            Holder<Boolean> recrypt = new Holder<>(false);

            // Read the crypt.
            CompletableFuture<Crypt> future;
            byte[] crypted;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(this.data);
                 DataInputStream in = new DataInputStream(byteIn)) {
                // Read and process the crypt.
                future = Crypt.readType(in, handler::password);

                // Crypted data.
                crypted = in.readAllBytes();
            }

            // Decrypt.
            future.thenApplyAsync(value -> {
                // Skip if cancelled.
                if (value == null || handler.cancelled()) return null;

                // Log it and display progress.
                LOGGER.info("IAS: Decrypting tokens...");
                handler.stage(DECRYPTING);

                // Decrypt.
                byte[] data = value.decrypt(crypted);

                // Migrate and set the crypt.
                Crypt migrate = value.migrate();
                if (migrate != null) {
                    crypt.set(migrate);
                    recrypt.set(true);
                } else {
                    crypt.set(value);
                }

                // Continue.
                return data;
            }, IAS.EXECUTOR).thenApplyAsync(value -> {
                // Skip if cancelled.
                if (value == null || handler.cancelled()) return false;

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
            }, IAS.EXECUTOR).thenComposeAsync(value -> {
                // Skip if cancelled.
                if (!value || handler.cancelled()) return CompletableFuture.completedFuture(null);

                // Log it and display progress.
                LOGGER.info("IAS: Converting MCA to MCP... (stored)");
                handler.stage(MCA_TO_MCP);

                // Convert MCA to MCP.
                return MSAuth.mcaToMcp(access.get()).exceptionallyComposeAsync(original -> {
                    // Skip if cancelled.
                    if (handler.cancelled()) return CompletableFuture.completedFuture(null);

                    // Log it and display progress.
                    LOGGER.warn("IAS: MCA is (probably) expired. Refreshing...");
                    LOGGER.info("IAS: Converting MSR to MSA/MSR...");
                    handler.stage(MSR_TO_MSA_MSR);

                    // Require recrypting data.
                    recrypt.set(true);

                    // Convert MSR to MSA/MSR.
                    return MSAuth.msrToMsaMsr(refresh.get()).thenComposeAsync(ms -> {
                        // Skip if cancelled.
                        if (ms == null || handler.cancelled()) return CompletableFuture.completedFuture(null);

                        // Update the refresh token.
                        refresh.set(ms.refresh());

                        // Log it and display progress.
                        LOGGER.info("IAS: Converting MSA to XBL...");
                        handler.stage(MSA_TO_XBL);

                        // Convert MSA to XBL.
                        return MSAuth.msaToXbl(ms.access());
                    }, IAS.EXECUTOR).thenComposeAsync(xbl -> {
                        // Skip if cancelled.
                        if (xbl == null || handler.cancelled()) return CompletableFuture.completedFuture(null);

                        // Log it and display progress.
                        LOGGER.info("IAS: Converting XBL to XSTS...");
                        handler.stage(XBL_TO_XSTS);

                        // Convert XBL to XSTS.
                        return MSAuth.xblToXsts(xbl.token(), xbl.hash());
                    }, IAS.EXECUTOR).thenComposeAsync(xsts -> {
                        // Skip if cancelled.
                        if (xsts == null || handler.cancelled()) return CompletableFuture.completedFuture(null);

                        // Log it and display progress.
                        LOGGER.info("IAS: Converting XSTS to MCA...");
                        handler.stage(XSTS_TO_MCA);

                        // Convert XSTS to MCA.
                        return MSAuth.xstsToMca(xsts.token(), xsts.hash());
                    }, IAS.EXECUTOR).thenComposeAsync(token -> {
                        // Skip if cancelled.
                        if (token == null || handler.cancelled()) return CompletableFuture.completedFuture(null);

                        // Update the access token.
                        access.set(token);

                        // Log it and display progress.
                        LOGGER.info("IAS: Converting MCA TO MCP... (refreshed)");
                        handler.stage(MCA_TO_MCP);

                        // Convert MCA to MCP.
                        return MSAuth.mcaToMcp(token);
                    }, IAS.EXECUTOR).exceptionallyAsync(t -> {
                        t.addSuppressed(original);

                        // Probable case - no internet connection.
                        if (IUtils.anyInCausalChain(t, err -> err instanceof UnresolvedAddressException || err instanceof NoRouteToHostException || err instanceof HttpTimeoutException || err instanceof ConnectException)) {
                            throw new FriendlyException("Unable to connect to MSR servers.", t, "ias.error.connect");
                        }

                        // Handle error.
                        throw new RuntimeException("Unable to perform MSR auth.", t);
                    }, IAS.EXECUTOR).exceptionallyAsync(t -> {
                        // Rethrow. (adding original)
                        t.addSuppressed(original);
                        throw new RuntimeException("Unable to refresh MSR.", t);
                    }, IAS.EXECUTOR);
                }, IAS.EXECUTOR);
            }, IAS.EXECUTOR).thenAcceptAsync(profile -> {
                // Skip if cancelled.
                if (profile == null || handler.cancelled()) return;

                // Re-encrypt if required.
                boolean saveStorage = false;
                if (recrypt.get()) {
                    // Log it and display progress.
                    LOGGER.info("IAS: Encrypting tokens...");
                    handler.stage(ENCRYPTING);

                    // Write the tokens.
                    String accessMem = access.get();
                    String refreshMem = refresh.get();
                    byte[] unencrypted;
                    try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream(accessMem.length() + refreshMem.length() + 4);
                         DataOutputStream out = new DataOutputStream(byteOut)) {
                        // Write the access token.
                        out.writeUTF(accessMem);

                        // Write the refresh token.
                        out.writeUTF(refreshMem);

                        // Flush it.
                        unencrypted = byteOut.toByteArray();
                    } catch (Throwable t) {
                        throw new RuntimeException("Unable to write the tokens.", t);
                    }

                    // Encrypt the tokens.
                    try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream(unencrypted.length + 32);
                         DataOutputStream out = new DataOutputStream(byteOut)) {
                        // Encrypt.
                        Crypt val = crypt.get();
                        byte[] encrypted = val.encrypt(unencrypted);

                        // Write data.
                        out.writeUTF(val.type());
                        out.write(encrypted);

                        // Flush it.
                        this.data = byteOut.toByteArray();
                        saveStorage = true;
                    } catch (Throwable t) {
                        throw new RuntimeException("Unable to encrypt the tokens.", t);
                    }
                }

                // Authentication successful, refresh the profile.
                UUID uuid = profile.uuid();
                String name = profile.name();
                if (!this.uuid.equals(uuid) || !this.name.equals(name)) {
                    this.uuid = profile.uuid();
                    this.name = profile.name();
                    saveStorage = true;
                }

                // Log it and display progress.
                LOGGER.info("IAS: Successful login as {}", profile);
                handler.stage(FINALIZING);

                // Create and return the data.
                User login = new User(this.name, this.uuid, access.get(), Optional.empty(), Optional.empty(), User.Type.MSA);
                handler.success(login, saveStorage);
            }, IAS.EXECUTOR).exceptionallyAsync(t -> {
                // Handle error.
                handler.error(new RuntimeException("Unable to login as MS account", t));

                // Return null.
                return null;
            }, IAS.EXECUTOR);
        } catch (Throwable t) {
            // Handle.
            handler.error(new RuntimeException("Unable to begin MS auth.", t));
        }
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MicrosoftAccount that)) return false;
        return Objects.equals(this.uuid, that.uuid) && Objects.equals(this.name, that.name);
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + Objects.hashCode(this.uuid);
        hash = 31 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        byte[] data = this.data;
        return "IAS/MicrosoftAccount{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", data=" + (data != null ? ('[' + data.length + " BYTES]") : null) +
                '}';
    }

    /**
     * Encodes the account into the binary output.
     *
     * @param out Binary output
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void encode(DataOutput out) throws IOException {
        // Validate.
        assert out != null : "Parameter 'out' is null. (account: " + this + ')';

        // Encode the type.
        out.writeUTF("ias:microsoft_v2"); // Implicit NPE for 'out'

        // Encode the UUID.
        UUID uuid = this.uuid;
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());

        // Encode the name.
        out.writeUTF(this.name);

        // Encode the data.
        byte[] data = this.data;
        out.writeShort(data.length);
        out.write(data);
    }

    /**
     * Decodes the account from the binary input.
     *
     * @param in              Binary input
     * @param hasInsecureFlag Whether the account has an "insecure" flag attached to it
     * @return A newly created decoded account
     * @throws EOFException           If the {@code in} reaches the end before reading all the data
     * @throws UTFDataFormatException If the decoded {@code name} string byte sequence can't be used to create a valid UTF string
     * @throws InvalidObjectException If the decoded {@code name} is blank or too long or the {@code skin} is present ant its version is not {@code 4}
     * @throws IOException            If an I/O error occurs
     */
    @CheckReturnValue
    static MicrosoftAccount decode(DataInput in, boolean hasInsecureFlag) throws IOException {
        // Decode and ignore the insecure.
        if (hasInsecureFlag) {
            boolean ignoredInsecureFlag = in.readBoolean();
        }

        // Decode and validate the UUID.
        long msb = in.readLong();
        long lsb = in.readLong();
        UUID uuid = new UUID(msb, lsb);
        int version = uuid.version();
        if (version != 4) {
            throw new InvalidObjectException("IAS: UUID version is not 4. (in: " + in + ", uuid: " + ", uuidVersion: " + version + ')');
        }

        // Decode and validate the name.
        String name = in.readUTF();
        if (name.isBlank()) {
            throw new InvalidObjectException("IAS: Name is blank. (in: " + in + ", uuid: " + uuid + ", name: " + name + ')');
        }
        int nameLength = name.length();
        if (nameLength > 16) {
            throw new InvalidObjectException("IAS: Name is longer than 16 characters. (in: " + in + ", uuid: " + uuid + ", name: " + name + ", nameLength: " + nameLength + ')');
        }

        // Decode the data.
        int length = in.readUnsignedShort();
        byte[] data = new byte[length];
        in.readFully(data);

        // Create and return.
        return new MicrosoftAccount(uuid, name, data);
    }
}
