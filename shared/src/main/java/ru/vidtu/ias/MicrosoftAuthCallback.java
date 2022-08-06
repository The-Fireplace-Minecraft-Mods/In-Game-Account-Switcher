package ru.vidtu.ias;

import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vidtu.ias.account.Auth;
import ru.vidtu.ias.account.MicrosoftAccount;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class MicrosoftAuthCallback implements Closeable {
    public static final String MICROSOFT_AUTH_URL = "https://login.live.com/oauth20_authorize.srf" +
            "?client_id=54fd49e4-2103-4044-9603-2b028c814ec3" +
            "&response_type=code" +
            "&scope=XboxLive.signin%20XboxLive.offline_access" +
            "&redirect_uri=http://localhost:59125" +
            "&prompt=select_account";
    private HttpServer server;

    public @NotNull CompletableFuture<@Nullable MicrosoftAccount> start(@NotNull BiConsumer<@NotNull String, @NotNull Object[]> progressHandler, @NotNull String done) {
        CompletableFuture<MicrosoftAccount> cf = new CompletableFuture<>();
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 59125), 0);
            server.createContext("/", ex -> {
                SharedIAS.LOG.info("Microsoft authentication callback request: " + ex.getRemoteAddress());
                try (BufferedReader in = new BufferedReader(new InputStreamReader(MicrosoftAuthCallback.class
                        .getResourceAsStream("/authPage.html"), StandardCharsets.UTF_8))) {
                    progressHandler.accept("ias.loginGui.microsoft.progress", new Object[] {"preparing"});
                    byte[] b = in.lines().collect(Collectors.joining("\n")).replace("%message%", done).getBytes(StandardCharsets.UTF_8);
                    ex.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    ex.sendResponseHeaders(307, b.length);
                    try (OutputStream os = ex.getResponseBody()) {
                        os.write(b);
                    }
                    close();
                    SharedIAS.EXECUTOR.execute(() -> {
                        try {
                            cf.complete(auth(progressHandler, ex.getRequestURI().getQuery()));
                        } catch (Throwable t) {
                            SharedIAS.LOG.error("Unable to authenticate via Microsoft.", t);
                            cf.completeExceptionally(t);
                        }
                    });
                } catch (Throwable t) {
                    SharedIAS.LOG.error("Unable to process request on Microsoft authentication callback server.", t);
                    close();
                    cf.completeExceptionally(t);
                }
            });
            server.start();
            SharedIAS.LOG.info("Started Microsoft authentication callback server.");
        } catch (Throwable t) {
            SharedIAS.LOG.error("Unable to run the Microsoft authentication callback server.", t);
            close();
            cf.completeExceptionally(t);
        }
        return cf;
    }

    private @Nullable MicrosoftAccount auth(@NotNull BiConsumer<@NotNull String, @NotNull Object[]> progressHandler,
                                            @Nullable String query) throws Exception {
        SharedIAS.LOG.info("Authenticating...");
        if (query == null) throw new NullPointerException("query=null");
        if (query.equals("error=access_denied&error_description=The user has denied access to the scope requested by the client application.")) return null;
        if (!query.startsWith("code=")) throw new IllegalStateException("query=" + query);
        SharedIAS.LOG.info("Step: codeToToken.");
        progressHandler.accept("ias.loginGui.microsoft.progress", new Object[] {"codeToToken"});
        Map.Entry<String, String> authRefreshTokens = Auth.codeToToken(query.replace("code=", ""));
        String refreshToken = authRefreshTokens.getValue();
        SharedIAS.LOG.info("Step: authXBL.");
        progressHandler.accept("ias.loginGui.microsoft.progress", new Object[] {"authXBL"});
        String xblToken = Auth.authXBL(authRefreshTokens.getKey());
        SharedIAS.LOG.info("Step: authXSTS.");
        progressHandler.accept("ias.loginGui.microsoft.progress", new Object[] {"authXSTS"});
        Map.Entry<String, String> xstsTokenUserhash = Auth.authXSTS(xblToken);
        SharedIAS.LOG.info("Step: authMinecraft.");
        progressHandler.accept("ias.loginGui.microsoft.progress", new Object[] {"authMinecraft"});
        String accessToken = Auth.authMinecraft(xstsTokenUserhash.getValue(), xstsTokenUserhash.getKey());
        SharedIAS.LOG.info("Step: getProfile.");
        progressHandler.accept("ias.loginGui.microsoft.progress", new Object[] {"getProfile"});
        Map.Entry<UUID, String> profile = Auth.getProfile(accessToken);
        SharedIAS.LOG.info("Authenticated.");
        return new MicrosoftAccount(profile.getValue(), accessToken, refreshToken, profile.getKey());
    }

    @Override
    public void close() {
        try {
            if (server != null) {
                server.stop(0);
                SharedIAS.LOG.info("Stopped Microsoft authentication callback server.");
            }
        } catch (Throwable t) {
            SharedIAS.LOG.error("Unable to stop the Microsoft authentication callback server.", t);
        }
    }
}
