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

package ru.vidtu.ias.auth.microsoft;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.MicrosoftAccount;
import ru.vidtu.ias.auth.handlers.CreateHandler;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.crypt.Crypt;
import ru.vidtu.ias.utils.Holder;
import ru.vidtu.ias.utils.IUtils;
import ru.vidtu.ias.utils.exceptions.FriendlyException;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP server for MS authentication.
 *
 * @author VidTu
 */
public final class MSAuthServer implements Runnable, Closeable {
    /**
     * Auth URI with {@code %%port%%} to be replaced by port and {@code %%state%%} to be replaced by state.
     */
    private static final String MICROSOFT_AUTH_URL = "https://login.live.com/oauth20_authorize.srf" +
            "?client_id=54fd49e4-2103-4044-9603-2b028c814ec3" +
            "&response_type=code" +
            "&scope=XboxLive.signin%20XboxLive.offline_access" +
            "&redirect_uri=http://localhost:%%port%%/in_game_account_switcher_long_enough_uri_to_prevent_accidental_leaks_on_screensharing_even_if_you_have_like_extremely_big_screen_though_it_might_not_mork_but_we_will_try_it_anyway_to_prevent_funny_things_from_happening_or_something" +
            "&prompt=select_account" +
            "&state=%%state%%";

    /**
     * Redirect URI with one {@code %s} argument to be replaced by port.
     */
    private static final String REDIRECT_URI = "http://localhost:%s/in_game_account_switcher_long_enough_uri_to_prevent_accidental_leaks_on_screensharing_even_if_you_have_like_extremely_big_screen_though_it_might_not_mork_but_we_will_try_it_anyway_to_prevent_funny_things_from_happening_or_something";

    /**
     * End URI with one {@code %s} argument to be replaced by port.
     */
    private static final String END_URI = "http://localhost:%s/end";

    /**
     * Random state.
     */
    private static final String STATE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.-_";

    /**
     * Data extraction pattern.
     */
    private static final Pattern DATA_EXTRACT_PATTERN = Pattern.compile("^code=([^&]*)&state=([^&]*)$");

    /**
     * Code obfuscation pattern.
     */
    private static final Pattern CODE_OBFUSCATE_PATTERN = Pattern.compile("code=[^&]*", Pattern.CASE_INSENSITIVE);

    /**
     * Logger for this class.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("IAS/MSAuthServer");

    /**
     * Message to display on "done" page.
     */
    private final String doneMessage;

    /**
     * Account crypt.
     */
    private final Crypt crypt;

    /**
     * Login handler.
     */
    private final CreateHandler handler;

    /**
     * Created HTTP server.
     */
    private final HttpServer server;

    /**
     * Validation state.
     */
    private final String state;

    /**
     * Bound port.
     * {@code 0} if not yet bound.
     */
    private int port;

    /**
     * Whether the request to the main endpoint has been received once.
     */
    private boolean once;

    /**
     * Creates an HTTP server for MS auth.
     *
     * @param doneMessage Message on the "done" screen
     * @param crypt       Account crypt
     * @param handler     Creation handler
     * @throws RuntimeException If unable to create an HTTP server
     */
    public MSAuthServer(String doneMessage, Crypt crypt, CreateHandler handler) {
        try {
            // Assign the values.
            this.doneMessage = doneMessage;
            this.crypt = crypt;
            this.handler = handler;

            // Create the server.
            this.server = HttpServer.create();

            // Generate the state.
            SecureRandom random = SecureRandom.getInstanceStrong();
            int length = random.nextInt(96, 128);
            StringBuilder builder = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                builder.appendCodePoint(STATE_CHARACTERS.codePointAt(random.nextInt(STATE_CHARACTERS.length())));
            }
            this.state = builder.toString();
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to create HTTP server for MS auth.", t);
        }
    }

    @Override
    public void run() {
        try {
            // Stop if cancelled.
            if (this.handler.cancelled()) return;

            // Log it and display progress.
            LOGGER.info("IAS: Booting up local HTTP server...");
            this.handler.stage(MicrosoftAccount.SERVER);

            // Create the root handler.
            this.server.createContext("/", ex -> {
                try {
                    // Close if already requested.
                    if (this.once) {
                        LOGGER.debug("IAS: Closed non-once HTTP request to '/'.");
                        ex.close();
                        return;
                    }

                    // Log it.
                    LOGGER.info("IAS: Requested HTTP to '/'.");

                    // Close and ignore if not localhost.
                    if (!ex.getRemoteAddress().getAddress().isLoopbackAddress()) {
                        LOGGER.warn("IAS: Closed not loopback HTTP request to '/'.");
                        ex.close();
                        return;
                    }

                    // Mark as once.
                    this.once = true;

                    // Capture the query.
                    URI uri = ex.getRequestURI();

                    // Get the auth page.
                    byte[] data;
                    try (InputStream in = MSAuthServer.class.getResourceAsStream("/auth.html")) {
                        Objects.requireNonNull(in, "Auth page is null.");

                        // Read and replace the page data.
                        String page = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        page = page
                                .replace("%%ias_icon%%", IASConfig.unexpectedPigs ? "🐷👍" : "✅")
                                .replace("%%ias_message%%", this.doneMessage);
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
                    this.auth(uri);

                    // Close the server.
                    IAS.executor().schedule(this::close, 10L, TimeUnit.SECONDS);
                } catch (Throwable t) {
                    // Try to close the request.
                    try {
                        ex.close();
                    } catch (Throwable th) {
                        t.addSuppressed(th);
                    }

                    // Try to close the server.
                    try {
                        this.close();
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
                    // Log it.
                    LOGGER.info("IAS: Requested HTTP to '/end'.");

                    // Close and ignore if not localhost.
                    if (!ex.getRemoteAddress().getAddress().isLoopbackAddress()) {
                        LOGGER.warn("IAS: Closed not loopback request to '/end'.");
                        ex.close();
                        return;
                    }

                    // Get the auth page.
                    byte[] data;
                    try (InputStream in = MSAuthServer.class.getResourceAsStream("/auth.html")) {
                        Objects.requireNonNull(in, "Auth page is null.");

                        // Read and replace the page data.
                        String page = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        page = page
                                .replace("%%ias_icon%%", IASConfig.unexpectedPigs ? "🐷👍" : "✅")
                                .replace("%%ias_message%%", this.doneMessage);
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
                    IAS.executor().schedule(this::close, 10L, TimeUnit.SECONDS);
                } catch (Throwable t) {
                    // Try to close the request.
                    try {
                        ex.close();
                    } catch (Throwable th) {
                        t.addSuppressed(th);
                    }

                    // Try to close the server.
                    try {
                        this.close();
                    } catch (Throwable th) {
                        t.addSuppressed(th);
                    }

                    // Rethrow.
                    this.handler.error(new RuntimeException("Unexpected exception on '/end': " + ex, t));
                }
            });

            // Stop if cancelled.
            if (this.handler.cancelled()) return;

            // Start the server.
            this.bindToSupportedPort();
            this.server.start();

            // Log it.
            LOGGER.info("IAS: HTTP server started.");
        } catch (Throwable t) {
            // Try to close the server.
            try {
                this.close();
            } catch (Throwable th) {
                t.addSuppressed(th);
            }

            // Rethrow.
            throw new RuntimeException("Unable to start the server.", t);
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
        for (int port = 59125; port <= 59135; port++) {
            try {
                // Try to bind.
                this.server.bind(new InetSocketAddress(port), 0);

                // No exception is thrown, return and do not process throwing.
                this.port = port;
                LOGGER.info("IAS: Bound HTTP server to {} port.", this.port);
                return;
            } catch (Throwable t) {
                // Add to thrown exceptions.
                thrown.add(new RuntimeException("Unable to bind: " + port, t));
            }
        }

        // Rethrow all errors.
        RuntimeException holder = new RuntimeException("Unable to bind to any port.");
        thrown.forEach(holder::addSuppressed);
        throw holder;
    }

    /**
     * Gets the auth URL.
     *
     * @return Auth URL
     */
    public String authUrl() {
        return MICROSOFT_AUTH_URL
                .replace("%%port%%", Integer.toString(this.port))
                .replace("%%state%%", this.state);
    }

    /**
     * Tries to finalize the auth.
     *
     * @param uri Request URI
     */
    private void auth(URI uri) {
        try {
            // Stop if cancelled.
            if (this.handler.cancelled()) return;

            // Log it and display progress.
            LOGGER.info("IAS: Processing response...");
            this.handler.stage(MicrosoftAccount.PROCESSING);

            // Extract the query.
            String query = uri.getQuery();

            // Value holders.
            Holder<String> access = new Holder<>();
            Holder<String> refresh = new Holder<>();
            Holder<byte[]> data = new Holder<>();

            // Extract the MSAC.
            CompletableFuture.supplyAsync(() -> {
                // Stop if cancelled.
                if (this.handler.cancelled()) return null;

                // Log it and display progress.
                LOGGER.info("IAS: Extracting MSAC from query...");

                // Probable case - direct URL.
                if (query == null) {
                    throw new FriendlyException("Null query.", "ias.error.query");
                }

                // Probable case - User aborted the auth.
                if (query.toLowerCase(Locale.ROOT).contains("access_denied")) {
                    // Throw, suppressing possible another code location.
                    throw new FriendlyException("Invalid query (access denied): " + CODE_OBFUSCATE_PATTERN.matcher(query)
                            .replaceAll("code=[CODE]"), "ias.error.cancel");
                }

                // Query won't start with code. Weird query.
                Matcher matcher = DATA_EXTRACT_PATTERN.matcher(query);
                if (!matcher.matches()) {
                    // Throw, suppressing possible another code location.
                    throw new IllegalStateException("Invalid query: " + CODE_OBFUSCATE_PATTERN.matcher(query)
                            .replaceAll("code=[CODE]"));
                }

                // Extract and validate the state.
                String state = matcher.group(2);
                if (!this.state.equals(state)) {
                    throw new IllegalStateException("Expected state " + state + ", got " + this.state);
                }

                // Extract the MSAC.
                return matcher.group(1);
            }, IAS.executor()).thenComposeAsync(code -> {
                // Stop if cancelled.
                if (code == null || this.handler.cancelled()) return CompletableFuture.completedFuture(null);

                // Log it and display progress.
                LOGGER.info("IAS: Converting MSAC to MSA/MSR...");
                this.handler.stage(MicrosoftAccount.MSAC_TO_MSA_MSR);

                // Convert MSAC to MSA/MSR.
                return MSAuth.msacToMsaMsr(code, REDIRECT_URI.formatted(this.port));
            }, IAS.executor()).thenComposeAsync(ms -> {
                // Stop if cancelled.
                if (ms == null || this.handler.cancelled()) return CompletableFuture.completedFuture(null);

                // Update the refresh token.
                refresh.set(ms.refresh());

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
                if (IUtils.anyInCausalChain(t, err -> err instanceof UnresolvedAddressException || err instanceof NoRouteToHostException || err instanceof HttpTimeoutException)) {
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
                    byte[] encrypted = this.crypt.encrypt(unencrypted);

                    // Write type.
                    Crypt.encrypt(out, this.crypt);

                    // Write data.
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

    /**
     * Closes the server.
     */
    @Override
    public void close() {
        // Close the server.
        this.server.stop(0);

        // Log it.
        LOGGER.info("IAS: HTTP server stopped.");
    }

    @Override
    public String toString() {
        return "MSAuthServer{" +
                "crypt=" + this.crypt +
                ", port=" + this.port +
                '}';
    }
}
