package ru.vidtu.ias.auth.ms;

import com.google.errorprone.annotations.CheckReturnValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.utils.GSONUtils;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Class for Microsoft authentication system.
 *
 * @author VidTu
 * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme">Reference</a>
 */
public final class MSAuth {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(MSTokens.class, new MSTokens.Adapter())
            .registerTypeAdapter(XHashedToken.class, new XHashedToken.Adapter())
            .registerTypeAdapter(MCProfile.class, new MCProfile.Adapter())
            .create();

    // Provided
    private final String userAgent;
    private final Duration timeout;
    private final Executor executor;

    // Constructed
    private final HttpClient client;

    private final URI msTokensUri;
    private final URI xboxUri;
    private final URI xstsUri;
    private final URI minecraftUri;
    private final URI profileUri;
    private final String nameProfileUri;

    private final String codeToTokensPayload;
    private final String refreshToTokensPayload;

    public MSAuth(@NotNull String userAgent, @NotNull String clientId, @NotNull String redirectUri,
                  @NotNull Duration timeout, @NotNull Executor executor) {
        try {
            this.userAgent = userAgent;
            this.timeout = timeout;
            this.executor = executor;

            this.client = HttpClient.newBuilder()
                    .executor(executor)
                    .connectTimeout(timeout)
                    .build();

            this.msTokensUri = new URI("https://login.live.com/oauth20_token.srf");
            this.xboxUri = new URI("https://user.auth.xboxlive.com/user/authenticate");
            this.xstsUri = new URI("https://xsts.auth.xboxlive.com/xsts/authorize");
            this.minecraftUri = new URI("https://api.minecraftservices.com/authentication/login_with_xbox");
            this.profileUri = new URI("https://api.minecraftservices.com/minecraft/profile");
            this.nameProfileUri = "https://api.mojang.com/users/profiles/minecraft/%s";

            this.codeToTokensPayload = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                    "&code=%%ias_code%%" +
                    "&grant_type=authorization_code" +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                    "&scope=XboxLive.signin%20XboxLive.offline_access";
            this.refreshToTokensPayload = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) + "&" +
                    "refresh_token=%%ias_refresh%%&" +
                    "grant_type=refresh_token&" +
                    "redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) + "&" +
                    "scope=XboxLive.signin%20XboxLive.offline_access";
        } catch (Throwable t) {
            throw new RuntimeException("Unable to init MSAuth.", t);
        }
    }

    /**
     * Gets the Microsoft (MS) tokens from the Microsoft (MSAC) authentication code.
     *
     * @param code Microsoft authentication code (e.g. from user auth redirect)
     * @return Future that will complete with Microsoft (MS) tokens or exceptionally
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Microsoft_OAuth2_Flow">Reference</a>
     * @see #refreshToTokens(String)
     */
    @CheckReturnValue
    @NotNull
    public CompletableFuture<MSTokens> codeToTokens(@NotNull String code) {
        String payload = codeToTokensPayload.replace("%%ias_code%%", URLEncoder.encode(code, StandardCharsets.UTF_8));
        return client.sendAsync(HttpRequest.newBuilder(msTokensUri)
                .header("User-Agent", userAgent)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build(), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
            try {
                int status = response.statusCode();
                if (status < 200 || status > 299) {
                    throw new IllegalArgumentException("Invalid status code: " + status);
                }
                MSTokens tokens = GSON.fromJson(response.body(), MSTokens.class);
                Objects.requireNonNull(tokens, "Response is null");
                return tokens;
            } catch (Throwable t) {
                throw new RuntimeException("Unable to convert Microsoft authentication (MSAC) code to Microsoft (MS) tokens (" + response + "): " + response.body(), t);
            }
        }, executor);
    }

    /**
     * Gets the Microsoft (MS) tokens using old Microsoft refresh (MSR) token.
     *
     * @param refresh Microsoft refresh (MSR) token (e.g. from {@link MSTokens#refresh()})
     * @return Future that will complete with Microsoft tokens or exceptionally
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Microsoft_OAuth2_Flow">Reference</a>
     */
    @CheckReturnValue
    @NotNull
    public CompletableFuture<MSTokens> refreshToTokens(@NotNull String refresh) {
        String payload = refreshToTokensPayload.replace("%%ias_refresh%%", URLEncoder.encode(refresh, StandardCharsets.UTF_8));
        return client.sendAsync(HttpRequest.newBuilder(msTokensUri)
                .header("User-Agent", userAgent)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build(), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
            try {
                int status = response.statusCode();
                if (status < 200 || status > 299) {
                    throw new IllegalArgumentException("Invalid status code: " + status);
                }
                MSTokens tokens = GSON.fromJson(response.body(), MSTokens.class);
                Objects.requireNonNull(tokens, "Response is null");
                return tokens;
            } catch (Throwable t) {
                throw new RuntimeException("Unable to convert Microsoft refresh (MSR) token to Microsoft (MS) tokens (" + response + "): " + response.body(), t);
            }
        }, executor);
    }

    /**
     * Gets the Xbox Live (XBL) token from the Microsoft access (MSA) token.
     *
     * @param authToken Microsoft access (MSA) token (e.g. from {@link MSTokens#access()})
     * @return Future that will complete with an XBL token and a user hash or exceptionally
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Authenticate_with_Xbox_Live">Reference</a>
     */
    @CheckReturnValue
    @NotNull
    public CompletableFuture<XHashedToken> accessToXbox(@NotNull String authToken) {
        // This is ugly, but I won't create custom classes and serializers just for this.
        JsonObject request = new JsonObject();
        JsonObject requestProperties = new JsonObject();
        requestProperties.addProperty("AuthMethod", "RPS");
        requestProperties.addProperty("SiteName", "user.auth.xboxlive.com");
        requestProperties.addProperty("RpsTicket", "d=" + authToken);
        request.add("Properties", requestProperties);

        // We disable "HTTP -> HTTPS" inspection here, because it's not an actual URL,
        // but a payload parameter (possibly) required by the specification:
        // https://wiki.vg/Microsoft_Authentication_Scheme#Authenticate_with_Xbox_Live
        //noinspection HttpUrlsUsage
        request.addProperty("RelyingParty", "http://auth.xboxlive.com");

        request.addProperty("TokenType", "JWT");
        String payload = GSON.toJson(request);
        return client.sendAsync(HttpRequest.newBuilder(xboxUri)
                .header("User-Agent", userAgent)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build(), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
            try {
                int status = response.statusCode();
                if (status < 200 || status > 299) {
                    throw new IllegalArgumentException("Invalid status code: " + status);
                }
                XHashedToken token = GSON.fromJson(response.body(), XHashedToken.class);
                Objects.requireNonNull(token, "Response is null");
                return token;
            } catch (Throwable t) {
                throw new RuntimeException("Unable to convert Microsoft access (MSA) token to Xbox Live (XBL) token (" + response + "): " + response.body(), t);
            }
        }, executor);
    }

    /**
     * Gets the Xbox Secure Token Service (XSTS) token from the Xbox Live (XBL) access token.
     *
     * @param xbl  Xbox Live (XBL) token (e.g. from {@link #accessToXbox(String)})
     * @param hash User hash to verify, {@code null} to skip verification (e.g. from {@link #accessToXbox(String)})
     * @return Future that will complete with an XSTS token and a user hash or exceptionally
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Obtain_XSTS_token_for_Minecraft">Reference</a>
     */
    @CheckReturnValue
    @NotNull
    public CompletableFuture<XHashedToken> xboxToSecureToken(@NotNull String xbl, @Nullable String hash) {
        // This is ugly, but I won't create custom classes and serializers just for this.
        JsonObject request = new JsonObject();
        JsonObject requestProperties = new JsonObject();
        JsonArray requestUserTokens = new JsonArray();
        requestUserTokens.add(xbl);
        requestProperties.add("UserTokens", requestUserTokens);
        requestProperties.addProperty("SandboxId", "RETAIL");
        request.add("Properties", requestProperties);
        request.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        request.addProperty("TokenType", "JWT");
        String payload = GSON.toJson(request);
        return client.sendAsync(HttpRequest.newBuilder(xstsUri)
                .header("User-Agent", userAgent)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build(), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
            try {
                int status = response.statusCode();
                if (status < 200 || status > 299) {
                    throw new IllegalArgumentException("Invalid status code: " + status);
                }
                XHashedToken token = GSON.fromJson(response.body(), XHashedToken.class);
                Objects.requireNonNull(token, "Response is null");
                if (hash != null && !hash.equals(token.hash())) {
                    throw new IllegalStateException("Mismatching XBL and XSTS user hashes. (" + hash + " != " + token.hash() + ")");
                }
                return token;
            } catch (Throwable t) {
                throw new RuntimeException("Unable to convert Xbox Live (XBL) token to Xbox Secure Token Service (XSTS) token (" + response + "): " + response.body(), t);
            }
        }, executor);
    }

    /**
     * Gets the Minecraft Access (MA) token from the Xbox Secure Token Service (XSTS) token and user hash.
     *
     * @param xsts Xbox Secure Token Service (XSTS) token (e.g. from {@link #xboxToSecureToken(String, String)})
     * @param hash User hash (e.g. from {@link XHashedToken#hash()})
     * @return Future that will complete with an MA token or exceptionally
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Authenticate_with_Minecraft">Reference</a>
     */
    @CheckReturnValue
    @NotNull
    public CompletableFuture<String> secureTokenToMinecraft(@NotNull String xsts, @NotNull String hash) {
        // This is ugly, but I won't create custom classes and serializers just for this.
        JsonObject request = new JsonObject();
        request.addProperty("identityToken", "XBL3.0 x=" + hash + ";" + xsts);
        String payload = GSON.toJson(request);
        return client.sendAsync(HttpRequest.newBuilder(minecraftUri)
                .header("User-Agent", userAgent)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build(), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
            try {
                int status = response.statusCode();
                if (status < 200 || status > 299) {
                    throw new IllegalArgumentException("Invalid status code: " + status);
                }
                JsonObject json = GSON.fromJson(response.body(), JsonObject.class);
                Objects.requireNonNull(json, "Response is null");
                return GSONUtils.getStringOrThrow(json, "access_token");
            } catch (Throwable t) {
                throw new RuntimeException("Unable to convert Xbox Secure Token Service (XSTS) token to Minecraft Access (MA) token (" + response + "): " + response.body(), t);
            }
        }, executor);
    }

    /**
     * Gets the Minecraft profile from the Minecraft Access (MA) token.
     *
     * @param access Minecraft Access (MA) token (e.g. from {@link #secureTokenToMinecraft(String, String)})
     * @return Future that will complete with a Minecraft profile or exceptionally
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Getting_the_profile">Reference</a>
     */
    @CheckReturnValue
    @NotNull
    public CompletableFuture<MCProfile> minecraftToProfile(@NotNull String access) {
        return client.sendAsync(HttpRequest.newBuilder(profileUri)
                .header("User-Agent", userAgent)
                .header("Authorization", "Bearer " + access)
                .timeout(timeout)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
            try {
                int status = response.statusCode();
                if (status < 200 || status > 299) {
                    throw new IllegalArgumentException("Invalid status code: " + status);
                }
                MCProfile profile = GSON.fromJson(response.body(), MCProfile.class);
                Objects.requireNonNull(profile, "Response is null");
                return profile;
            } catch (Throwable t) {
                throw new RuntimeException("Unable to convert Minecraft Access (MA) token to Minecraft profile (" + response + "): " + response.body(), t);
            }
        }, executor);
    }

    /**
     * Resolve profile from name using Mojang API.
     *
     * @param name Player name
     * @return Future with resolved (v4) UUID or with offline (v3) UUID if it can't be resolved
     */
    public CompletableFuture<MCProfile> nameToProfile(@NotNull String name) {
        try {
            return client.sendAsync(HttpRequest.newBuilder(new URI(nameProfileUri.formatted(name)))
                    .header("User-Agent", userAgent)
                    .timeout(timeout)
                    .GET()
                    .build(), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
                try {
                    int status = response.statusCode();
                    if (status < 200 || status > 299) {
                        throw new IllegalArgumentException("Invalid status code: " + status);
                    }
                    MCProfile profile = GSON.fromJson(response.body(), MCProfile.class);
                    Objects.requireNonNull(profile, "Response is null");
                    return profile;
                } catch (Throwable t) {
                    throw new RuntimeException("Unable to resolve Minecraft profile from name (" + response + "): " + response.body(), t);
                }
            }, executor).exceptionallyAsync(ignored -> {
                UUID uuid = UUID.nameUUIDFromBytes("OfflinePlayer:".concat(name).getBytes(StandardCharsets.UTF_8));
                return new MCProfile(uuid, name);
            }, executor);
        } catch (URISyntaxException ignored) {
            UUID uuid = UUID.nameUUIDFromBytes("OfflinePlayer:".concat(name).getBytes(StandardCharsets.UTF_8));
            return CompletableFuture.completedFuture(new MCProfile(uuid, name));
        }
    }
}
