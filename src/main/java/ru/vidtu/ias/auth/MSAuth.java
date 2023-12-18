/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2023 VidTu
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

package ru.vidtu.ias.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ru.vidtu.ias.utils.GSONUtils;

import java.net.URI;
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
 * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme">wiki.vg/Microsoft_Authentication_Scheme</a>
 */
public final class MSAuth {
    /**
     * Gson with support for MS values decoding.
     */
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(MSTokens.class, new MSTokens.Adapter())
            .registerTypeAdapter(XHashedToken.class, new XHashedToken.Adapter())
            .registerTypeAdapter(MCProfile.class, new MCProfile.Adapter())
            .create();

    /**
     * Request user-agent.
     */
    private final String userAgent;

    /**
     * Request timeout.
     */
    private final Duration timeout;

    /**
     * Request executor.
     */
    private final Executor executor;

    /**
     * Request client.
     */
    private final HttpClient client;

    /**
     * Endpoint for {@link #msacToMsaMsr(String)} and {@link #msrToMsaMsr(String)}.
     */
    private final URI msTokensUri;

    /**
     * Endpoint for {@link #msaToXbl(String)}.
     */
    private final URI xboxUri;

    /**
     * Endpoint for {@link #xblToXsts(String, String)}.
     */
    private final URI xstsUri;

    /**
     * Endpoint for {@link #xstsToMca(String, String)}.
     */
    private final URI minecraftUri;

    /**
     * Endpoint for {@link #mcaToMcp(String)}.
     */
    private final URI profileUri;

    /**
     * Endpoint for {@link #nameToMcp(String)}.
     */
    private final String nameProfileUri;

    /**
     * Payload for {@link #msacToMsaMsr(String)}.
     * Replace the "{@code %%ias_code%%}" with your code.
     */
    private final String codeToTokensPayload;

    /**
     * Payload for {@link #msrToMsaMsr(String)}.
     * Replace the {@code %%ias_refresh%%} with your refresh.
     */
    private final String refreshToTokensPayload;

    /**
     * Creates the new authenticator.
     *
     * @param userAgent   Request user agent
     * @param clientId    Current Microsoft client ID
     * @param redirectUri Redirect URI, {@code null} if not meant to be used for {@link #msacToMsaMsr(String)}
     * @param timeout     HTTP client timeout
     * @param executor    HTTP request executor
     */
    public MSAuth(String userAgent, String clientId, String redirectUri, Duration timeout, Executor executor) {
        try {
            // Assign the fields.
            this.userAgent = userAgent;
            this.timeout = timeout;
            this.executor = executor;

            // Create the client.
            this.client = HttpClient.newBuilder()
                    .executor(executor)
                    .connectTimeout(timeout)
                    .build();

            // Create the endpoints.
            this.msTokensUri = new URI("https://login.live.com/oauth20_token.srf");
            this.xboxUri = new URI("https://user.auth.xboxlive.com/user/authenticate");
            this.xstsUri = new URI("https://xsts.auth.xboxlive.com/xsts/authorize");
            this.minecraftUri = new URI("https://api.minecraftservices.com/authentication/login_with_xbox");
            this.profileUri = new URI("https://api.minecraftservices.com/minecraft/profile");
            this.nameProfileUri = "https://api.mojang.com/users/profiles/minecraft/%s";

            // Create the payloads.
            this.codeToTokensPayload = redirectUri != null ? "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                    "&code=%%ias_code%%" +
                    "&grant_type=authorization_code" +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                    "&scope=XboxLive.signin%20XboxLive.offline_access" : null;
            this.refreshToTokensPayload = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) + "&" +
                    "refresh_token=%%ias_refresh%%&" +
                    "grant_type=refresh_token&" +
                    "scope=XboxLive.signin%20XboxLive.offline_access";
        } catch (Throwable t) {
            throw new RuntimeException("Unable to init MSAuth.", t);
        }
    }

    /**
     * Gets the Microsoft Access (MSA) and Microsoft Refresh (MSR) tokens from the Microsoft Authentication Code. (MSAC)
     *
     * @param code Microsoft Authentication Code (MSAC; e.g. from user auth redirect)
     * @return Future that will complete with Microsoft Access (MSA) and Microsoft Refresh (MSR) tokens or exceptionally
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Microsoft_OAuth2_Flow">Reference</a>
     * @see #msrToMsaMsr(String)
     */
    public CompletableFuture<MSTokens> msacToMsaMsr(String code) {
        // Has no redirect URI.
        if (this.codeToTokensPayload == null) {
            // Fail.
            return CompletableFuture.failedFuture(new IllegalStateException("This authenticator can't be used for converting code to tokens, because it has no redirect URI."));
        }

        // Create the payload.
        String payload = this.codeToTokensPayload
                .replace("%%ias_code%%", URLEncoder.encode(code, StandardCharsets.UTF_8));

        // Send the request.
        return this.client.sendAsync(HttpRequest.newBuilder(this.msTokensUri)
                .header("User-Agent", this.userAgent)
                .header("Accept", "application/x-www-form-urlencoded")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(this.timeout)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build(), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
            // Process the response.
            try {
                // Check the code.
                int status = response.statusCode();
                if (status < 200 || status > 299) {
                    throw new IllegalArgumentException("Invalid status code: " + status);
                }

                // Decode the tokens and return them.
                MSTokens tokens = GSON.fromJson(response.body(), MSTokens.class);
                Objects.requireNonNull(tokens, "Response is null");
                return tokens;
            } catch (Throwable t) {
                // Rethrow, trying to remove sensitive data.
                String message = "Unable to convert Microsoft Authentication Code (MSAC) to Microsoft Access (MSA) and Microsoft Refresh (MSR) tokens (" + response + " with " + response.headers() + "): " + response.body();
                message = message.replace(code, "[MSAC]");
                throw new RuntimeException(message, t);
            }
        }, this.executor);
    }

    /**
     * Gets the Microsoft Access (MSA) and Microsoft Refresh (MSR) tokens using Microsoft Refresh (MSR) token.
     *
     * @param refresh Microsoft Refresh (MSR) token (e.g. from {@link MSTokens#refresh()})
     * @return Future that will complete with Microsoft Access (MSA) and Microsoft Refresh (MSR) Tokens or exceptionally
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Microsoft_OAuth2_Flow">Reference</a>
     */
    public CompletableFuture<MSTokens> msrToMsaMsr(String refresh) {
        // Create the payload.
        String payload = this.refreshToTokensPayload
                .replace("%%ias_refresh%%", URLEncoder.encode(refresh, StandardCharsets.UTF_8));

        // Send the request.
        return this.client.sendAsync(HttpRequest.newBuilder(this.msTokensUri)
                .header("User-Agent", this.userAgent)
                .header("Accept", "application/x-www-form-urlencoded")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(this.timeout)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build(), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
            // Process the response.
            try {
                // Check the code.
                int status = response.statusCode();
                if (status < 200 || status > 299) {
                    throw new IllegalArgumentException("Invalid status code: " + status);
                }

                // Decode the tokens and return them.
                MSTokens tokens = GSON.fromJson(response.body(), MSTokens.class);
                Objects.requireNonNull(tokens, "Response is null");
                return tokens;
            } catch (Throwable t) {
                // Rethrow, trying to remove sensitive data.
                String message = "Unable to convert Microsoft Refresh (MSR) token to Microsoft Access (MSA) and Microsoft Refresh (MSR) tokens (" + response + " with " + response.headers() + "): " + response.body();
                message = message.replace(refresh, "[MSR]");
                throw new RuntimeException(message, t);
            }
        }, this.executor);
    }

    /**
     * Gets the Xbox Live (XBL) token from the Microsoft Access (MSA) token.
     *
     * @param authToken Microsoft Access (MSA) token (e.g. from {@link MSTokens#access()})
     * @return Future that will complete with an XBL token and a user hash or exceptionally
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Authenticate_with_Xbox_Live">Reference</a>
     */
    public CompletableFuture<XHashedToken> msaToXbl(String authToken) {
        // Create the payload.
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

        // Send the request.
        return this.client.sendAsync(HttpRequest.newBuilder(this.xboxUri)
                .header("User-Agent", this.userAgent)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(this.timeout)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build(), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
            // Process the response.
            try {
                // Check the code.
                int status = response.statusCode();
                if (status < 200 || status > 299) {
                    throw new IllegalArgumentException("Invalid status code: " + status);
                }

                // Decode the XBL and return it.
                XHashedToken token = GSON.fromJson(response.body(), XHashedToken.class);
                Objects.requireNonNull(token, "Response is null");
                return token;
            } catch (Throwable t) {
                // Rethrow, trying to remove sensitive data.
                String message = "Unable to convert Microsoft Access (MSA) token to Xbox Live (XBL) token (" + response + " with " + response.headers() + "): " + response.body();
                message = message.replace(authToken, "[MSA]");
                throw new RuntimeException(message, t);
            }
        }, this.executor);
    }

    /**
     * Gets the Xbox Secure Token Service (XSTS) token from the Xbox Live (XBL) access token.
     *
     * @param xbl  Xbox Live (XBL) token (e.g. from {@link #msaToXbl(String)})
     * @param hash User hash to verify, {@code null} to skip verification (e.g. from {@link #msaToXbl(String)})
     * @return Future that will complete with an XSTS token and a user hash or exceptionally
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Obtain_XSTS_token_for_Minecraft">Reference</a>
     */
    public CompletableFuture<XHashedToken> xblToXsts(String xbl, String hash) {
        // Create the payload.
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

        // Send the request.
        return this.client.sendAsync(HttpRequest.newBuilder(this.xstsUri)
                .header("User-Agent", this.userAgent)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(this.timeout)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build(), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
            // Process the response.
            try {
                // Check the code.
                int status = response.statusCode();
                if (status < 200 || status > 299) {
                    throw new IllegalArgumentException("Invalid status code: " + status);
                }

                // Decode the XSTS.
                XHashedToken token = GSON.fromJson(response.body(), XHashedToken.class);

                // Validate it.
                Objects.requireNonNull(token, "Response is null");
                if (hash != null && !hash.equals(token.hash())) {
                    throw new IllegalStateException("Mismatching XBL and XSTS user hashes.");
                }

                // Return it.
                return token;
            } catch (Throwable t) {
                // Rethrow, trying to remove sensitive data.
                String message = "Unable to convert Xbox Live (XBL) token to Xbox Secure Token Service (XSTS) token (" + response + " with " + response.headers() + "): " + response.body();
                message = message.replace(xbl, "[XBL]");
                message = message.replace(hash, "[HASH]");
                throw new RuntimeException(message, t);
            }
        }, this.executor);
    }

    /**
     * Gets the Minecraft Access (MCA) token from the Xbox Secure Token Service (XSTS) token and user hash.
     *
     * @param xsts Xbox Secure Token Service (XSTS) token (e.g. from {@link #xblToXsts(String, String)})
     * @param hash User hash (e.g. from {@link XHashedToken#hash()})
     * @return Future that will complete with an MCA token or exceptionally
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Authenticate_with_Minecraft">Reference</a>
     */
    public CompletableFuture<String> xstsToMca(String xsts, String hash) {
        // Create the payload.
        // This is ugly, but I won't create custom classes and serializers just for this.
        JsonObject request = new JsonObject();
        request.addProperty("identityToken", "XBL3.0 x=" + hash + ";" + xsts);
        String payload = GSON.toJson(request);

        // Send the request.
        return this.client.sendAsync(HttpRequest.newBuilder(this.minecraftUri)
                .header("User-Agent", this.userAgent)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(this.timeout)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build(), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
            // Process the response.
            try {
                // Check the code.
                int status = response.statusCode();
                if (status < 200 || status > 299) {
                    throw new IllegalArgumentException("Invalid status code: " + status);
                }

                // Decode the token and return it.
                JsonObject json = GSON.fromJson(response.body(), JsonObject.class);
                Objects.requireNonNull(json, "Response is null");
                return GSONUtils.getStringOrThrow(json, "access_token");
            } catch (Throwable t) {
                // Rethrow, trying to remove sensitive data.
                String message = "Unable to convert Xbox Secure Token Service (XSTS) token to Minecraft Access (MCA) token (" + response + " with " + response.headers() + "): " + response.body();
                message = message.replace(xsts, "[XSTS]");
                message = message.replace(hash, "[HASH]");
                throw new RuntimeException(message, t);
            }
        }, this.executor);
    }

    /**
     * Gets the Minecraft Profile (MCP) from the Minecraft Access (MCA) token.
     *
     * @param access Minecraft Access (MCA) token (e.g. from {@link #xstsToMca(String, String)})
     * @return Future that will complete with an MCP or exceptionally
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Getting_the_profile">Reference</a>
     */
    public CompletableFuture<MCProfile> mcaToMcp(String access) {
        // Send the request.
        return this.client.sendAsync(HttpRequest.newBuilder(this.profileUri)
                .header("User-Agent", this.userAgent)
                .header("Authorization", "Bearer " + access)
                .timeout(this.timeout)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
            // Process the response.
            try {
                // Check the code.
                int status = response.statusCode();
                if (status < 200 || status > 299) {
                    throw new IllegalArgumentException("Invalid status code: " + status);
                }

                // Decode the profile and return it.
                MCProfile profile = GSON.fromJson(response.body(), MCProfile.class);
                Objects.requireNonNull(profile, "Response is null");
                return profile;
            } catch (Throwable t) {
                // Rethrow, trying to remove sensitive data.
                String message = "Unable to convert Minecraft Access (MCA) token to Minecraft Profile (MCP) (" + response + "): " + response.body();
                message = message.replace(access, "[MCA]");
                throw new RuntimeException(message, t);
            }
        }, this.executor);
    }

    /**
     * Resolve Minecraft Profile (MCP) from name using Mojang API.
     *
     * @param name Player name
     * @return Future with resolved profile (or offline as fallback)
     */
    public CompletableFuture<MCProfile> nameToMcp(String name) {
        try {
            // Send the request.
            return this.client.sendAsync(HttpRequest.newBuilder(new URI(this.nameProfileUri.formatted(URLEncoder.encode(name, StandardCharsets.UTF_8))))
                    .header("User-Agent", this.userAgent)
                    .timeout(this.timeout)
                    .GET()
                    .build(), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
                // Check the code.
                int status = response.statusCode();
                if (status < 200 || status > 299) {
                    throw new IllegalArgumentException("Invalid status code: " + status);
                }

                // Decode the profile and return it.
                MCProfile profile = GSON.fromJson(response.body(), MCProfile.class);
                Objects.requireNonNull(profile, "Response is null");
                return profile;
            }, this.executor).exceptionallyAsync(ignored -> {
                // Fallback to offline.
                UUID uuid = UUID.nameUUIDFromBytes("OfflinePlayer:".concat(name).getBytes(StandardCharsets.UTF_8));
                return new MCProfile(uuid, name);
            }, this.executor);
        } catch (Throwable ignored) {
            // Fallback to offline.
            UUID uuid = UUID.nameUUIDFromBytes("OfflinePlayer:".concat(name).getBytes(StandardCharsets.UTF_8));
            return CompletableFuture.completedFuture(new MCProfile(uuid, name));
        }
    }
}
