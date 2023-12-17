package ru.vidtu.ias.auth;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.MicrosoftAccount;
import ru.vidtu.ias.utils.CryptUtils;
import ru.vidtu.ias.utils.Holder;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP server for MS authentication.
 *
 * @author VidTu
 */
public final class MSAuthServer implements Runnable, Closeable {
    /**
     * Redirect URI with one {@code %s} argument to be replaced by port.
     */
    private static final String REDIRECT_URI = "http://localhost:%s/in_game_account_switcher_long_enough_uri_to_prevent_accidental_leaks_on_screensharing_even_if_you_have_like_extremely_big_screen_though_it_might_not_mork_but_we_will_try_it_anyway_to_prevent_funny_things_from_happening_or_something";

    /**
     * End URI with one {@code %s} argument to be replaced by port.
     */
    private static final String END_URI = "http://localhost:%s/end";

    /**
     * Message to display on "done" page.
     */
    private final String doneMessage;

    /**
     * Encryption password.
     */
    private final String password;

    /**
     * Login handler.
     */
    private final CreateHandler handler;

    /**
     * Created HTTP server.
     */
    private final HttpServer server;

    /**
     * Bound port.
     * {@code 0} if not yet bound.
     */
    private int port;

    /**
     * Creates an HTTP server for MS auth.
     *
     * @param doneMessage Message on the "done" screen
     * @param password    Encryption password
     * @param handler     Creation handler
     * @throws RuntimeException If unable to create an HTTP server
     */
    public MSAuthServer(String doneMessage, String password, CreateHandler handler) {
        try {
            // Assign the values.
            this.doneMessage = doneMessage;
            this.password = password;
            this.handler = handler;

            // Create the server.
            this.server = HttpServer.create();
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to create HTTP server for MS auth.", t);
        }
    }

    @Override
    public void run() {
        try {
            // Log it and display progress.
            IAS.LOG.info("IAS: Booting up local HTTP server: {}", this.server);
            this.handler.stage(MicrosoftAccount.SERVER);

            // Create the root handler.
            this.server.createContext("/", ex -> {
                try {
                    // Close and ignore if not localhost.
                    if (!ex.getRemoteAddress().getAddress().isLoopbackAddress()) {
                        ex.close();
                        return;
                    }

                    // Capture the query.
                    URI uri = ex.getRequestURI();

                    // Get the auth page.
                    byte[] data;
                    try (InputStream in = MSAuthServer.class.getResourceAsStream("/auth.html")) {
                        Objects.requireNonNull(in, "Auth page is null.");

                        // Read and replace the page data.
                        String page = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        page = page.replace("%%ias_message%%", this.doneMessage);
                        data = page.getBytes(StandardCharsets.UTF_8);
                    }

                    // Redirect. (to try and increase security)
                    Headers headers = ex.getResponseHeaders();
                    headers.add("Content-Type", "text/html; charset=UTF-8");
                    headers.add("Content-Length", Integer.toString(data.length));
                    headers.add("Server", IAS.userAgent());
                    headers.add("Location", END_URI.formatted(this.port));
                    ex.sendResponseHeaders(302, data.length);

                    // Write the response.
                    try (OutputStream out = ex.getResponseBody()) {
                        out.write(data);
                    }

                    // Close the request.
                    ex.close();

                    // Send the query.
                    auth(uri);

                    // Wait for processing.
                    Thread.sleep(10000L);

                    // Close the server.
                    close();
                } catch (Throwable t) {
                    // Try to close the request.
                    try {
                        ex.close();
                    } catch (Throwable th) {
                        t.addSuppressed(th);
                    }

                    // Try to close the server.
                    try {
                        close();
                    } catch (Throwable th) {
                        t.addSuppressed(th);
                    }

                    // Rethrow.
                    this.handler.error(new RuntimeException("Unexpected exception on '/': " + ex, t));
                }
            });

            // Create the end handler. (safe spot)
            this.server.createContext("/end", ex -> {
                try {
                    // Close and ignore if not localhost.
                    if (!ex.getRemoteAddress().getAddress().isLoopbackAddress()) {
                        ex.close();
                        return;
                    }

                    // Get the auth page.
                    byte[] data;
                    try (InputStream in = MSAuthServer.class.getResourceAsStream("/auth.html")) {
                        Objects.requireNonNull(in, "Auth page is null.");

                        // Read and replace the page data.
                        String page = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        page = page.replace("%%ias_message%%", this.doneMessage);
                        data = page.getBytes(StandardCharsets.UTF_8);
                    }

                    // Send headers.
                    Headers headers = ex.getResponseHeaders();
                    headers.add("Content-Type", "text/html; charset=UTF-8");
                    headers.add("Content-Length", Integer.toString(data.length));
                    headers.add("Server", IAS.userAgent());
                    ex.sendResponseHeaders(200, data.length);

                    // Write the response.
                    try (OutputStream out = ex.getResponseBody()) {
                        out.write(data);
                    }

                    // Close the request.
                    ex.close();

                    // Close the server.
                    close();
                } catch (Throwable t) {
                    // Try to close the request.
                    try {
                        ex.close();
                    } catch (Throwable th) {
                        t.addSuppressed(th);
                    }

                    // Try to close the server.
                    try {
                        close();
                    } catch (Throwable th) {
                        t.addSuppressed(th);
                    }

                    // Rethrow.
                    this.handler.error(new RuntimeException("Unexpected exception on '/end': " + ex, t));
                }
            });

            // Start the server.
            this.bindToSupportedPort();
            this.server.start();

            // Log it.
            IAS.LOG.info("IAS: HTTP server {} started.", this.server);
        } catch (Throwable t) {
            // Try to close the server.
            try {
                close();
            } catch (Throwable th) {
                t.addSuppressed(th);
            }

            // Rethrow.
            throw new RuntimeException("Unable to start the server: " + this.server, t);
        }
    }

    /**
     * Bind the server to any supported port.
     *
     * @throws RuntimeException If unable to bind
     */
    private void bindToSupportedPort() {
        // Any thrown exceptions.
        List<RuntimeException> thrown = new LinkedList<>();

        // Note that this port range MUST be declared in Microsoft valid
        // redirect URIs, so using any port won't work. I did register some
        // ports in the UI.
        for (int port = 59125; port <= 59129; port++) {
            try {
                // Try to bind.
                this.server.bind(new InetSocketAddress(port), 0);

                // No exception is thrown, return and do not process throwing.
                return;
            } catch (Throwable t) {
                // Add to thrown.
                thrown.add(new RuntimeException("Unable to bind: " + port, t));
            }
        }

        // Rethrow all errors.
        RuntimeException holder = new RuntimeException("Unable to bind to any port: " + this.server);
        thrown.forEach(holder::addSuppressed);
        throw holder;
    }

    /**
     * Tries to finalize the auth.
     *
     * @param uri Request URI
     */
    private void auth(URI uri) {
        try {
            // Log it and display progress.
            IAS.LOG.info("IAS: Processing response...");
            this.handler.stage(MicrosoftAccount.PROCESSING);

            // Extract the query.
            String query = uri.getQuery();

            // Create the authenticator.
            MSAuth auth = new MSAuth(IAS.userAgent(), IAS.CLIENT_ID, REDIRECT_URI.formatted(this.port), Duration.ofSeconds(15L), IAS.executor());

            // Value holders.
            Holder<String> access = new Holder<>();
            Holder<String> refresh = new Holder<>();
            Holder<byte[]> data = new Holder<>();

            // Extract the MSAC.
            CompletableFuture.supplyAsync(() -> {
                // Log it and display progress.
                IAS.LOG.info("IAS: Extracting MSAC from query...");

                // Null query. What?
                if (query == null) {
                    throw new NullPointerException("Query from callback is null. Possibly you've opened the 127.0.0.1 url directly, instead of opening it via the link or you're using old browser. (or unsupported plugins/extensions)");
                }

                // User aborted the auth.
                if (query.toLowerCase(Locale.ROOT).contains("access_denied")) {
                    return null;
                }

                // Query won't start with code. Weird query.
                if (!query.startsWith("code=")) {
                    // Throw, suppressing possible another code location.
                    throw new IllegalStateException("Invalid query: " + query
                            .replaceAll("code=[A-Za-z0-9_.-]*", "code=[CODE]"));
                }

                // Extract the MSAC.
                return query.replace("code=", "");
            }, IAS.executor()).thenComposeAsync(code -> {
                // Log it and display progress.
                IAS.LOG.info("IAS: Converting MSAC to MSA/MSR...");
                this.handler.stage(MicrosoftAccount.MSAC_TO_MSA_MSR);

                // Convert MSAC to MSA/MSR.
                return auth.msacToMsaMsr(code);
            }, IAS.executor()).thenComposeAsync(ms -> {
                // Update the refresh token.
                refresh.set(ms.refresh());

                // Log it and display progress.
                IAS.LOG.info("IAS: Converting MSA to XBL...");
                this.handler.stage(MicrosoftAccount.MSA_TO_XBL);

                // Convert MSA to XBL.
                return auth.msaToXbl(ms.access());
            }, IAS.executor()).thenComposeAsync(xbl -> {
                // Log it and display progress.
                IAS.LOG.info("IAS: Converting XBL to XSTS...");
                this.handler.stage(MicrosoftAccount.XBL_TO_XSTS);

                // Convert XBL to XSTS.
                return auth.xblToXsts(xbl.token(), xbl.hash());
            }, IAS.executor()).thenComposeAsync(xsts -> {
                // Log it and display progress.
                IAS.LOG.info("IAS: Converting XSTS to MCA...");
                this.handler.stage(MicrosoftAccount.XSTS_TO_MCA);

                // Convert XSTS to MCA.
                return auth.xstsToMca(xsts.token(), xsts.hash());
            }, IAS.executor()).thenComposeAsync(token -> {
                // Update the access token.
                access.set(token);

                // Log it and display progress.
                IAS.LOG.info("IAS: Converting MCA to MCP...");
                this.handler.stage(MicrosoftAccount.MCA_TO_MCP);

                // Convert MCA to MCP.
                return auth.mcaToMcp(token);
            }, IAS.executor()).thenApplyAsync(profile -> {
                // Log it and display progress.
                IAS.LOG.info("IAS: Encrypting tokens...");
                this.handler.stage(MicrosoftAccount.ENCRYPTING);

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
                    data.set(CryptUtils.encrypt(unencrypted, this.password, salt));
                } catch (Throwable t) {
                    throw new RuntimeException("Unable to encrypt the tokens.", t);
                }

                // Return the profile as-is.
                return profile;
            }, IAS.executor()).thenAcceptAsync(profile -> {
                // Authentication successful, refresh the profile.
                UUID uuid = profile.uuid();
                String name = profile.name();

                // Log it and display progress.
                IAS.LOG.info("IAS: Successfully added {}", profile);
                this.handler.stage(MicrosoftAccount.FINALIZING);

                // Create and return the data.
                MicrosoftAccount account = new MicrosoftAccount(uuid, name, data.get());
                this.handler.success(account);
            }, IAS.executor()).exceptionallyAsync(t -> {
                // Handle error.
                this.handler.error(new RuntimeException("Unable to create an MS account", t));

                // Return null.
                return null;
            }, IAS.executor());
        } catch (Throwable t) {
            // Handle.
            this.handler.error(new RuntimeException("Unable to finalize MS auth.", t));
        }
    }

    /**
     * Closes the server.
     */
    @Override
    public void close() {
        // Close the server.
        this.server.stop(0);
    }

    /**
     * Handler for creating accounts.
     *
     * @author VidTu
     * @apiNote All methods in this class can be called from another thread
     */
    public interface CreateHandler {
        /**
         * Changes the authentication stage.
         *
         * @param stage New auth stage translation key
         */
        void stage(String stage);

        /**
         * Called when an authentication has performed successfully.
         *
         * @param account Created account
         */
        void success(MicrosoftAccount account);

        /**
         * Called when an authentication has failed.
         *
         * @param error Failure reason
         */
        void error(Throwable error);
    }
}
