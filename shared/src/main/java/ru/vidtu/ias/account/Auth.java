package ru.vidtu.ias.account;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.SharedIAS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Class for Microsoft authentication system.<br>
 *
 * @author VidTu
 * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme">Reference</a>
 */
public class Auth {
    private static final String CLIENT_ID = "54fd49e4-2103-4044-9603-2b028c814ec3";
    private static final String REDIRECT_URI = "http://localhost:59125";

    /**
     * Get Microsoft Access Token and Microsoft Refresh Token from Microsoft Authentication Code.
     *
     * @param code Code from user auth redirect
     * @return Pair of Microsoft Access Token and Microsoft Refresh Token
     * @throws Exception If something goes wrong
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Authorization_Code_-.3E_Authorization_Token">Reference</a>
     */
    public static Map.@NotNull Entry<@NotNull String, @NotNull String> codeToToken(@NotNull String code) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("https://login.live.com/oauth20_token.srf").openConnection();
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);
        try (OutputStream out = conn.getOutputStream()) {
            out.write(("client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") + "&" +
                    "code=" + URLEncoder.encode(code, "UTF-8") + "&" +
                    "grant_type=authorization_code&" +
                    "redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") + "&" +
                    "scope=XboxLive.signin%20XboxLive.offline_access").getBytes(StandardCharsets.UTF_8));
            if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
                try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    throw new IllegalArgumentException("codeToToken response: " + conn.getResponseCode() + ", data: " + err.lines().collect(Collectors.joining("\n")));
                } catch (Throwable t) {
                    throw new IllegalArgumentException("codeToToken response: " + conn.getResponseCode(), t);
                }
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                JsonObject resp = SharedIAS.GSON.fromJson(in.lines().collect(Collectors.joining("\n")), JsonObject.class);
                return new AbstractMap.SimpleImmutableEntry<>(resp.get("access_token").getAsString(), resp.get("refresh_token").getAsString());
            }
        }
    }

    /**
     * Refresh Old Microsoft Refresh Token and get new Microsoft Access Token.
     *
     * @param refreshToken Microsoft Refresh Token from {@link #codeToToken(String)} or from this method
     * @return Pair of Microsoft Access Token and Microsoft Refresh Token
     * @throws Exception If something goes wrong
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Refreshing_Tokens">Reference</a>
     */
    public static Map.@NotNull Entry<@NotNull String, @NotNull String> refreshToken(@NotNull String refreshToken) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("https://login.live.com/oauth20_token.srf").openConnection();
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);
        try (OutputStream out = conn.getOutputStream()) {
            out.write(("client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") + "&" +
                    "refresh_token=" + URLEncoder.encode(refreshToken, "UTF-8") + "&" +
                    "grant_type=refresh_token&" +
                    "redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") + "&" +
                    "scope=XboxLive.signin%20XboxLive.offline_access").getBytes(StandardCharsets.UTF_8));
            if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
                try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    throw new IllegalArgumentException("refreshToken response: " + conn.getResponseCode() + ", data: " + err.lines().collect(Collectors.joining("\n")));
                } catch (Throwable t) {
                    throw new IllegalArgumentException("refreshToken response: " + conn.getResponseCode(), t);
                }
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                JsonObject resp = SharedIAS.GSON.fromJson(in.lines().collect(Collectors.joining("\n")), JsonObject.class);
                return new AbstractMap.SimpleImmutableEntry<>(resp.get("access_token").getAsString(), resp.get("refresh_token").getAsString());
            }
        }
    }

    /**
     * Get XBL Token from Microsoft Access Token.
     *
     * @param authToken Microsoft Access Token from {@link #codeToToken(String)} or {@link #refreshToken(String)}
     * @return XBL Token
     * @throws Exception If something goes wrong
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Authenticate_with_XBL">Reference</a>
     */
    public static @NotNull String authXBL(@NotNull String authToken) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("https://user.auth.xboxlive.com/user/authenticate").openConnection();
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);
        try (OutputStream out = conn.getOutputStream()) {
            JsonObject req = new JsonObject();
            JsonObject reqProps = new JsonObject();
            reqProps.addProperty("AuthMethod", "RPS");
            reqProps.addProperty("SiteName", "user.auth.xboxlive.com");
            reqProps.addProperty("RpsTicket", "d=" + authToken);
            req.add("Properties", reqProps);
            req.addProperty("RelyingParty", "http://auth.xboxlive.com");
            req.addProperty("TokenType", "JWT");
            out.write(req.toString().getBytes(StandardCharsets.UTF_8));
            if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
                try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    throw new IllegalArgumentException("authXBL response: " + conn.getResponseCode() + ", data: " + err.lines().collect(Collectors.joining("\n")));
                } catch (Throwable t) {
                    throw new IllegalArgumentException("authXBL response: " + conn.getResponseCode(), t);
                }
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                JsonObject resp = SharedIAS.GSON.fromJson(in.lines().collect(Collectors.joining("\n")), JsonObject.class);
                return resp.get("Token").getAsString();
            }
        }
    }

    /**
     * Get XSTS Token and XUI-UHS Userhash from XBL Token.
     *
     * @param xblToken XBL Token from {@link #authXBL(String)}
     * @return Pair of XSTS Token and XUI-UHS Userhash
     * @throws Exception If something goes wrong
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Authenticate_with_XSTS">Reference</a>
     */
    public static Map.@NotNull Entry<@NotNull String, @NotNull String> authXSTS(@NotNull String xblToken) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("https://xsts.auth.xboxlive.com/xsts/authorize").openConnection();
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);
        try (OutputStream out = conn.getOutputStream()) {
            JsonObject req = new JsonObject();
            JsonObject reqProps = new JsonObject();
            JsonArray userTokens = new JsonArray();
            userTokens.add(xblToken);
            reqProps.add("UserTokens", userTokens);
            reqProps.addProperty("SandboxId", "RETAIL");
            req.add("Properties", reqProps);
            req.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
            req.addProperty("TokenType", "JWT");
            out.write(req.toString().getBytes(StandardCharsets.UTF_8));
            if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
                try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    throw new IllegalArgumentException("authXSTS response: " + conn.getResponseCode() + ", data: " + err.lines().collect(Collectors.joining("\n")));
                } catch (Throwable t) {
                    throw new IllegalArgumentException("authXSTS response: " + conn.getResponseCode(), t);
                }
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                JsonObject resp = SharedIAS.GSON.fromJson(in.lines().collect(Collectors.joining("\n")), JsonObject.class);
                return new AbstractMap.SimpleImmutableEntry<>(resp.get("Token").getAsString(), resp.getAsJsonObject("DisplayClaims")
                        .getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString());
            }
        }

    }

    /**
     * Get Minecraft Access Token from XUI-UHS Userhash and XSTS Token.
     *
     * @param userHash  XUI-UHS Userhash from {@link #authXSTS(String)}
     * @param xstsToken XSTS Token from {@link #authXSTS(String)}
     * @return Minecraft Access Token
     * @throws Exception If something goes wrong
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Authenticate_with_Minecraft">Reference</a>
     */
    public static @NotNull String authMinecraft(@NotNull String userHash, @NotNull String xstsToken) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("https://api.minecraftservices.com/authentication/login_with_xbox").openConnection();
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);
        try (OutputStream out = conn.getOutputStream()) {
            JsonObject req = new JsonObject();
            req.addProperty("identityToken", "XBL3.0 x=" + userHash + ";" + xstsToken);
            out.write(req.toString().getBytes(StandardCharsets.UTF_8));
            if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
                try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    throw new IllegalArgumentException("authMinecraft response: " + conn.getResponseCode() + ", data: " + err.lines().collect(Collectors.joining("\n")));
                } catch (Throwable t) {
                    throw new IllegalArgumentException("authMinecraft response: " + conn.getResponseCode(), t);
                }
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                JsonObject resp = SharedIAS.GSON.fromJson(in.lines().collect(Collectors.joining("\n")), JsonObject.class);
                return resp.get("access_token").getAsString();
            }
        }
    }

    /**
     * Get Player UUID and Player Name from Minecraft Access Token.
     *
     * @param accessToken Minecraft Access Token from {@link #authMinecraft(String, String)}
     * @return Pair of Player UUID and Player Name
     * @throws Exception If something goes wrong
     * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Get_the_profile">Reference</a>
     */
    public static Map.@NotNull Entry<@NotNull UUID, @NotNull String> getProfile(@NotNull String accessToken) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("https://api.minecraftservices.com/minecraft/profile").openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
            try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("getProfile response: " + conn.getResponseCode() + ", data: " + err.lines().collect(Collectors.joining("\n")));
            } catch (Throwable t) {
                throw new IllegalArgumentException("getProfile response: " + conn.getResponseCode(), t);
            }
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            JsonObject resp = SharedIAS.GSON.fromJson(in.lines().collect(Collectors.joining("\n")), JsonObject.class);
            return new AbstractMap.SimpleImmutableEntry<>(UUID.fromString(resp.get("id").getAsString().replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")),
                    resp.get("name").getAsString());
        }
    }

    /**
     * Resolve UUID from name using Mojang API.
     *
     * @param name Player name
     * @return Resolved v4 UUID, v3 Offline UUID if it can't be resolved
     */
    public static @NotNull UUID resolveUUID(@NotNull String name) {
        try (InputStreamReader in = new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/"
                + name).openStream(), StandardCharsets.UTF_8)) {
            UUID uuid = UUID.fromString(SharedIAS.GSON.fromJson(in, JsonObject.class).get("id").getAsString().replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
            return uuid;
        } catch (Throwable ignored) {
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
            return uuid;
        }
    }
}
