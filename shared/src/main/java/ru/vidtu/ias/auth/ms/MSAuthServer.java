package ru.vidtu.ias.auth.ms;

import com.google.common.io.Resources;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.auth.account.MicrosoftAccount;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class MSAuthServer implements Runnable, Supplier<CompletableFuture<MicrosoftAccount>>, Closeable {
    private final HttpServer server;
    private final CompletableFuture<MicrosoftAccount> future;
    private final Consumer<AuthStage> progress;
    private final String doneMessage;

    private String refresh;
    private String access;

    public MSAuthServer(@NotNull String doneMessage, @NotNull Consumer<AuthStage> progress) throws IOException {
        this.server = HttpServer.create(InetSocketAddress.createUnresolved("127.0.0.1", 59125), 0);
        this.future = new CompletableFuture<>();
        this.progress = progress;
        this.doneMessage = doneMessage;
        // FIXME
//        "https://login.live.com/oauth20_authorize.srf" +
//                "?client_id=54fd49e4-2103-4044-9603-2b028c814ec3" +
//                "&response_type=code" +
//                "&scope=XboxLive.signin%20XboxLive.offline_access" +
//                "&redirect_uri=http://localhost:59125" +
//                "&prompt=select_account";
    }

    @Override
    public void run() {
        progress.accept(AuthStage.INITIALIZING);
        server.createContext("/", ex -> {
            try {
                if (!ex.getRemoteAddress().getAddress().isLoopbackAddress()) {
                    ex.close();
                    return;
                }
                URL pageUrl = MSAuthServer.class.getResource("/auth.html");
                Objects.requireNonNull(pageUrl, "Auth page is null.");
                String pageValue = Resources.toString(pageUrl, StandardCharsets.UTF_8);
                pageValue = pageValue.replace("%%ias_message%%", doneMessage);
                byte[] data = pageValue.getBytes(StandardCharsets.UTF_8);
                ex.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                ex.getResponseHeaders().add("Location", "http://127.0.0.1:59125/end");
                ex.sendResponseHeaders(302, data.length);
                try (OutputStream out = ex.getResponseBody()) {
                    out.write(data);
                }
                ex.close();
                auth(ex.getRequestURI().getQuery());
                Thread.sleep(10000L);
                close();
            } catch (Throwable t) {
                try {
                    ex.close();
                } catch (Throwable th) {
                    t.addSuppressed(th);
                }
                close();
                future.completeExceptionally(new RuntimeException("Unexpected exception on '/'", t));
            }
        });
        server.createContext("/end", ex -> {
            try {
                if (!ex.getRemoteAddress().getAddress().isLoopbackAddress()) {
                    ex.close();
                    return;
                }
                URL pageUrl = MSAuthServer.class.getResource("/auth.html");
                Objects.requireNonNull(pageUrl, "Auth page is null.");
                String pageValue = Resources.toString(pageUrl, StandardCharsets.UTF_8);
                pageValue = pageValue.replace("%%ias_message%%", doneMessage);
                byte[] data = pageValue.getBytes(StandardCharsets.UTF_8);
                ex.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                ex.sendResponseHeaders(200, data.length);
                try (OutputStream out = ex.getResponseBody()) {
                    out.write(data);
                }
                ex.close();
                close();
            } catch (Throwable t) {
                try {
                    ex.close();
                } catch (Throwable th) {
                    t.addSuppressed(th);
                }
                close();
                future.completeExceptionally(new RuntimeException("Unexpected exception on '/end'", t));
            }
        });
        server.start();
        progress.accept(AuthStage.OPEN_BROWSER);
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public CompletableFuture<MicrosoftAccount> get() {
        return future;
    }

    private void auth(@Nullable String query) {
        CompletableFuture.supplyAsync(() -> {
            IAS.log().info("Authenticating...");
            progress.accept(AuthStage.INITIALIZING);
            return new MSAuth(IAS.userAgent(), IAS.clientId(), IAS.redirectUri(), Duration.ofSeconds(15L), IAS.executor());
        }, IAS.executor()).thenComposeAsync(auth -> {
            IAS.log().info("Extracting code...");
            progress.accept(AuthStage.MSR_TO_MS);
            if (query == null) {
                throw new NullPointerException("Query from callback is null. Possibly you've opened the 127.0.0.1 url directly, instead of opening it via the link or you're using old browser (or unsupported plugins/extensions).");
            }
            if (query.equals("error=access_denied&error_description=The user has denied access to the scope requested by the client application.")) {
                return CompletableFuture.completedFuture(null);
            }
            if (!query.startsWith("code=")) {
                throw new IllegalStateException("Invalid query: " + query);
            }
            String code = query.replace("code=", "");
            progress.accept(AuthStage.CODE_TO_MS);
            return auth.codeToTokens(code).thenComposeAsync(ms -> {
                this.refresh = ms.refresh();
                IAS.log().info("Converting MSA to XBL...");
                progress.accept(AuthStage.MSA_TO_XBL);
                return auth.accessToXbox(ms.access());
            }, IAS.executor()).thenComposeAsync(xbl -> {
                IAS.log().info("Converting XBL to XSTS...");
                progress.accept(AuthStage.XBL_TO_XSTS);
                return auth.xboxToSecureToken(xbl.token(), xbl.hash());
            }, IAS.executor()).thenComposeAsync(xsts -> {
                IAS.log().info("Converting XSTS to MA...");
                progress.accept(AuthStage.XSTS_TO_MA);
                return auth.secureTokenToMinecraft(xsts.token(), xsts.hash());
            }, IAS.executor()).thenComposeAsync(access -> {
                this.access = access;
                IAS.log().info("Getting profile via MA...");
                progress.accept(AuthStage.MA_TO_PROFILE);
                return auth.minecraftToProfile(access);
            }, IAS.executor()).thenApplyAsync(profile -> {
                UUID uuid = profile.uuid();
                String name = profile.name();
                return new MicrosoftAccount(uuid, name, access, refresh);
            }, IAS.executor());
        }, IAS.executor()).whenCompleteAsync((account, th) -> {
            if (th != null) {
                future.completeExceptionally(new RuntimeException("Unable to auth", th));
                return;
            }
            future.complete(account);
        }, IAS.executor());
    }

    @Override
    public void close() {
        if (server != null) {
            server.stop(0);
        }
    }
}
