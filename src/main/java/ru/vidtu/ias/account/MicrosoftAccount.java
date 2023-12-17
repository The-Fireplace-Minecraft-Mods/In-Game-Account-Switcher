package ru.vidtu.ias.account;

import ru.vidtu.ias.IAS;
import ru.vidtu.ias.auth.MSAuth;
import ru.vidtu.ias.utils.CryptUtils;
import ru.vidtu.ias.utils.Holder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;
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
    public void login(LoginHandler handler) {
        try {
            // Log it and display progress.
            IAS.LOG.info("IAS: Logging (Microsoft) as {}/{}", this.uuid, this.name);
            handler.stage(INITIALIZING);

            // Create the authenticator.
            MSAuth auth = new MSAuth(IAS.userAgent(), IAS.CLIENT_ID, null, Duration.ofSeconds(15L), IAS.executor());

            // Value holders.
            Holder<String> password = new Holder<>();
            Holder<String> access = new Holder<>();
            Holder<String> refresh = new Holder<>();

            // Ask for password.
            handler.password().thenApplyAsync(pwd -> {
                // Skip if cancelled.
                if (pwd == null) return false;

                // Set the value.
                password.set(pwd);

                // Log it and display progress.
                IAS.LOG.info("IAS: Decrypting tokens...");
                handler.stage(DECRYPTING);

                // Decrypt the tokens.
                byte[] decrypted;
                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(this.data);
                     DataInputStream in = new DataInputStream(byteIn)) {
                    // Read the salt.
                    byte[] salt = new byte[256];
                    in.readFully(salt);

                    // Read the encrypted data.
                    byte[] encrypted = in.readAllBytes();

                    // Decrypt the data.
                    decrypted = CryptUtils.decrypt(encrypted, pwd, salt);
                } catch (Throwable t) {
                    throw new RuntimeException("Unable to decrypt the tokens.", t);
                }

                // Read the decrypted data into tokens.
                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(decrypted);
                     DataInputStream in = new DataInputStream(byteIn)) {
                    // Read the access token.
                    access.set(in.readUTF());

                    // Read the refresh token.
                    refresh.set(in.readUTF());

                    // Verify the buffer.
                    if (in.available() > 0) {
                        throw new IOException("Leftover: " + in.available());
                    }

                    // Return continue.
                    return true;
                } catch (Throwable t) {
                    throw new RuntimeException("Unable to read the tokens.", t);
                }
            }, IAS.executor()).thenComposeAsync(progress -> {
                // Skip if cancelled.
                if (!progress) return CompletableFuture.completedFuture(null);

                // Log it and display progress.
                IAS.LOG.info("IAS: Converting MCA to MCP... (stored)");
                handler.stage(MCA_TO_MCP);

                // Convert MCA to MCP.
                return auth.mcaToMcp(access.get());
            }, IAS.executor()).exceptionallyComposeAsync(original -> {
                // Log it and display progress.
                IAS.LOG.warn("IAS: MCA is (probably) expired. Refreshing...");
                IAS.LOG.info("IAS: Converting MSR to MSA/MSR...");
                handler.stage(MSR_TO_MSA_MSR);

                // Convert MSR to MSA/MSR.
                return auth.msrToMsaMsr(refresh.get()).thenComposeAsync(ms -> {
                    // Update the refresh token.
                    refresh.set(ms.refresh());

                    // Log it and display progress.
                    IAS.LOG.info("IAS: Converting MSA to XBL...");
                    handler.stage(MSA_TO_XBL);

                    // Convert MSA to XBL.
                    return auth.msaToXbl(ms.access());
                }, IAS.executor()).thenComposeAsync(xbl -> {
                    // Log it and display progress.
                    IAS.LOG.info("IAS: Converting XBL to XSTS...");
                    handler.stage(XBL_TO_XSTS);

                    // Convert XBL to XSTS.
                    return auth.xblToXsts(xbl.token(), xbl.hash());
                }, IAS.executor()).thenComposeAsync(xsts -> {
                    // Log it and display progress.
                    IAS.LOG.info("IAS: Converting XSTS to MCA...");
                    handler.stage(XSTS_TO_MCA);

                    // Convert XSTS to MCA.
                    return auth.xstsToMca(xsts.token(), xsts.hash());
                }, IAS.executor()).thenComposeAsync(token -> {
                    // Update the access token.
                    access.set(token);

                    // Log it and display progress.
                    IAS.LOG.info("IAS: Converting MCA TO MCP... (refreshed)");
                    handler.stage(MCA_TO_MCP);

                    // Convert MCA to MCP.
                    return auth.mcaToMcp(token);
                }, IAS.executor()).thenApplyAsync(profile -> {
                    // Log it and display progress.
                    IAS.LOG.info("IAS: Encrypting tokens...");
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
                        // Generate and write the salt.
                        Random random = SecureRandom.getInstanceStrong();
                        byte[] salt = new byte[256];
                        random.nextBytes(salt);
                        out.write(salt);

                        // Write the data.
                        out.write(unencrypted);

                        // Encrypt the data.
                        this.data = CryptUtils.encrypt(unencrypted, password.get(), salt);
                    } catch (Throwable t) {
                        throw new RuntimeException("Unable to encrypt the tokens.", t);
                    }

                    // Return the profile as-is.
                    return profile;
                }, IAS.executor()).exceptionallyAsync(t -> {
                    // Rethrow. (adding original)
                    RuntimeException e = new RuntimeException("Unable to refresh MSR.", t);
                    t.addSuppressed(original);
                    throw e;
                }, IAS.executor());
            }, IAS.executor()).thenAcceptAsync(profile -> {
                // Authentication successful, refresh the profile.
                this.uuid = profile.uuid();
                this.name = profile.name();

                // Log it and display progress.
                IAS.LOG.info("IAS: Successful login as {}", profile);
                handler.stage(FINALIZING);

                // Create and return the data.
                LoginData data = new LoginData(this.name, this.uuid, access.get(), LoginData.MSA);
                handler.success(data);
            }, IAS.executor()).exceptionallyAsync(t -> {
                // Handle error.
                handler.error(new RuntimeException("Unable to create an MS account", t));

                // Return null.
                return null;
            }, IAS.executor());
        } catch (Throwable t) {
            // Handle.
            handler.error(new RuntimeException("Unable to begin MS auth.", t));
        }
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
