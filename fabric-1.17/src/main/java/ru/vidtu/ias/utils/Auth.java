package ru.vidtu.ias.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.lang3.tuple.Pair;
import ru.vidtu.ias.account.AuthException;
import ru.vidtu.ias.account.MojangAccount;
import the_fireplace.ias.IAS;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class for Microsoft authentication system.<br>
 *
 * @author VidTu
 * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme">Reference</a>
 */
public class Auth {
	/**
	 * Process <code>Authorization Code -> Authorization Token</code> step.
	 *
	 * @param code Code from user auth redirect
	 * @return Pair of <code>[access_token,refresh_token]</code> (Auth Token, Refresh Token) from JSON response
	 * @throws IllegalArgumentException If server response is not HTTP Success (200-299)
	 * @throws Exception                If something goes wrong
	 * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Authorization_Code_-.3E_Authorization_Token">Reference</a>
	 */
    public static Pair<String, String> authCode2Token(String code) throws IllegalArgumentException, Exception {
		Request pr = new Request("https://login.live.com/oauth20_token.srf");
		pr.header("Content-Type", "application/x-www-form-urlencoded");
        HashMap<Object, Object> req = new HashMap<>();
        req.put("client_id", "54fd49e4-2103-4044-9603-2b028c814ec3");
        req.put("code", code);
        req.put("grant_type", "authorization_code");
        req.put("redirect_uri", "http://localhost:59125");
        req.put("scope", "XboxLive.signin XboxLive.offline_access");
        pr.post(req);
        if (pr.response() < 200 || pr.response() >= 300) throw new IllegalArgumentException("authCode2Token response: " + pr.response());
        JsonObject resp = IAS.GSON.fromJson(pr.body(), JsonObject.class);
        return Pair.of(resp.get("access_token").getAsString(), resp.get("refresh_token").getAsString());
    }

	/**
	 * Process <code>Refreshing Tokens</code> step.
	 *
	 * @param refreshToken Refresh token from {@link #authCode2Token(String)} or from this method
	 * @return Pair of <code>[access_token,refresh_token]</code> (Auth Token, Refresh Token) from JSON response
	 * @throws IllegalArgumentException If server response is not HTTP Success (200-299)
	 * @throws Exception                If something goes wrong
	 * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Refreshing_Tokens">Reference</a>
	 */
    public static Pair<String, String> refreshToken(String refreshToken) throws IllegalArgumentException, Exception {
    	Request r = new Request("https://login.live.com/oauth20_token.srf");
		r.get();
		Map<Object, Object> req = new HashMap<>();
		req.put("client_id", "54fd49e4-2103-4044-9603-2b028c814ec3");
		req.put("refresh_token", refreshToken);
		req.put("grant_type", "refresh_token");
		req.put("redirect_uri", "http://localhost:59125");
		r.post(req);
		if (r.response() < 200 || r.response() >= 300) throw new IllegalArgumentException("refreshToken response: " + r.response());
		JsonObject resp = IAS.GSON.fromJson(r.body(), JsonObject.class);
		return Pair.of(resp.get("access_token").getAsString(), resp.get("refresh_token").getAsString());
    }

	/**
	 * Process <code>Authenticate with XBL</code> step.
	 *
	 * @param authToken Authorization Token (<code>access_token</code>) from {@link #authCode2Token(String)} or {@link #refreshToken(String)}
	 * @return The <code>Token</code> (XBL token) from JSON response
	 * @throws IllegalArgumentException If server response is not HTTP Success (200-299)
	 * @throws Exception                If something goes wrong
	 * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Authenticate_with_XBL">Reference</a>
	 */
    public static String authXBL(String authToken) throws IllegalArgumentException, Exception {
		Request pr = new Request("https://user.auth.xboxlive.com/user/authenticate");
		pr.header("Content-Type", "application/json");
		pr.header("Accept", "application/json");
		JsonObject req = new JsonObject();
		JsonObject reqProps = new JsonObject();
		reqProps.addProperty("AuthMethod", "RPS");
		reqProps.addProperty("SiteName", "user.auth.xboxlive.com");
		reqProps.addProperty("RpsTicket", "d=" + authToken);
		req.add("Properties", reqProps);
		req.addProperty("RelyingParty", "http://auth.xboxlive.com");
		req.addProperty("TokenType", "JWT");
        pr.post(req.toString()); //Note: Here we're encoding parameters as JSON. ('key': 'value')
        if (pr.response() < 200 || pr.response() >= 300) throw new IllegalArgumentException("authXBL response: " + pr.response());
        return IAS.GSON.fromJson(pr.body(), JsonObject.class).get("Token").getAsString();
    }

	/**
	 * Process <code>Authenticate with XSTS</code> step.
	 *
	 * @param xblToken XBL Token from {@link #authXBL(String)}
	 * @return Pair of <code>[Token,Userhash]</code> ([XSTS Token, XUI-UHS Userhash]) from JSON response
	 * @throws AuthException            If the account doesn't have an Xbox account, XboxLife is banned in country or you're under 18. (Expected exception)
	 * @throws IllegalArgumentException If server response is not HTTP Success (200-299)
	 * @throws Exception                If something goes wrong
	 * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Authenticate_with_XSTS">Reference</a>
	 */
    public static Pair<String, String> authXSTS(String xblToken) throws AuthException, IllegalArgumentException, Exception {
		Request pr = new Request("https://xsts.auth.xboxlive.com/xsts/authorize");
		pr.header("Content-Type", "application/json");
		pr.header("Accept", "application/json");
		JsonObject req = new JsonObject();
		JsonObject reqProps = new JsonObject();
		JsonArray userTokens = new JsonArray();
		userTokens.add(xblToken);
		reqProps.add("UserTokens", userTokens); //Singleton JSON Array.
		reqProps.addProperty("SandboxId", "RETAIL");
        req.add("Properties", reqProps);
        req.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        req.addProperty("TokenType", "JWT");
        pr.post(req.toString());
        if (pr.response() == 401) throw new AuthException(new TranslatableComponent("ias.msauth.error.noxbox"));
        if (pr.response() < 200 || pr.response() >= 300) throw new IllegalArgumentException("authXSTS response: " + pr.response());
        JsonObject resp = IAS.GSON.fromJson(pr.body(), JsonObject.class);
        return Pair.of(resp.get("Token").getAsString(), resp.getAsJsonObject("DisplayClaims")
        		.getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString());
    }

	/**
	 * Process <code>Authenticate with Minecraft</code> step.
	 *
	 * @param userHash  XUI-UHS Userhash from {@link #authXSTS(String)}
	 * @param xstsToken XSTS Token from {@link #authXSTS(String)}
	 * @return The <code>access_token</code> (Minecraft access token) from JSON response
	 * @throws IllegalArgumentException If server response is not HTTP Success (200-299)
	 * @throws Exception                If something goes wrong
	 * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Authenticate_with_Minecraft">Reference</a>
	 */
    public static String authMinecraft(String userHash, String xstsToken) throws IllegalArgumentException, Exception {
		Request pr = new Request("https://api.minecraftservices.com/authentication/login_with_xbox");
		pr.header("Content-Type", "application/json");
		pr.header("Accept", "application/json");
		JsonObject req = new JsonObject();
        req.addProperty("identityToken", "XBL3.0 x=" + userHash + ";" + xstsToken);
        pr.post(req.toString());
        if (pr.response() < 200 || pr.response() >= 300) throw new IllegalArgumentException("authMinecraft response: " + pr.response());
        return IAS.GSON.fromJson(pr.body(), JsonObject.class).get("access_token").getAsString();
    }

	/**
	 * Process <code>Checking Game Ownership</code> step.
	 *
	 * @param accessToken Minecraft access token from {@link #authMinecraft(String, String)}
	 * @throws AuthException            If the account doesn't own the game (Expected exception)
	 * @throws IllegalArgumentException If server response is not HTTP Success (200-299)
	 * @throws Exception                If something goes wrong
	 * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Checking_Game_Ownership">Reference</a>
	 */
    public static void checkGameOwnership(String accessToken) throws AuthException, IllegalArgumentException, Exception {
		Request pr = new Request("https://api.minecraftservices.com/entitlements/mcstore");
		pr.header("Authorization", "Bearer " + accessToken);
		pr.get();
		if (pr.response() < 200 || pr.response() >= 300) throw new IllegalArgumentException("checkGameOwnership response: " + pr.response());
        if (IAS.GSON.fromJson(pr.body(), JsonObject.class).getAsJsonArray("items").size() == 0) throw new AuthException(new TranslatableComponent("ias.msauth.error.gamenotowned"));
    }

	/**
	 * Process <code>Get the profile</code> step.
	 *
	 * @param accessToken Minecraft access token from {@link #authMinecraft(String, String)}
	 * @return Pair of <code>[id,name]</code> (UUID, Playername) from JSON response
	 * @throws IllegalArgumentException If server response is not HTTP Success (200-299)
	 * @throws Exception                If something goes wrong
	 * @see <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Get_the_profile">Reference</a>
	 */
    public static Pair<UUID, String> getProfile(String accessToken) throws IllegalArgumentException, Exception {
		Request pr = new Request("https://api.minecraftservices.com/minecraft/profile");
		pr.header("Authorization", "Bearer " + accessToken);
		pr.get();
        if (pr.response() < 200 || pr.response() >= 300) throw new IllegalArgumentException("getProfile response: " + pr.response());
        JsonObject resp = IAS.GSON.fromJson(pr.body(), JsonObject.class);
        return Pair.of(UUIDTypeAdapter.fromString(resp.get("id").getAsString()), resp.get("name").getAsString());
    }

	/**
	 * Perform authentication using Mojang auth system.
	 *
	 * @param name Player login (usually email)
	 * @param pwd  Player password
	 * @return Authorized Mojang account
	 * @throws AuthException If auth exception occurs (Invalid login/pass, Too fast login, Account migrated to Microsoft, etc.)
	 * @throws IOException   If connection exception occurs
	 */
    public static MojangAccount authMojang(String name, String pwd) throws AuthException, IOException {
    	Request r = new Request("https://authserver.mojang.com/authenticate");
		r.header("Content-Type", "application/json");
		UUID clientToken = UUID.randomUUID();
		JsonObject req = new JsonObject();
		JsonObject agent = new JsonObject();
		agent.addProperty("name", "Minecraft");
		agent.addProperty("version", 1);
		req.add("agent", agent);
		req.addProperty("username", name);
		req.addProperty("password", pwd);
		req.addProperty("clientToken", UUIDTypeAdapter.fromUUID(clientToken));
		r.post(req.toString());
		if (r.response() < 200 || r.response() >= 300) {
			JsonObject jo;
			String reqerr = r.error();
			try {
				jo = new Gson().fromJson(reqerr, JsonObject.class);
			} catch (Exception ex) {
				if (reqerr.toLowerCase().contains("cloudfront")) {
					throw new AuthException(new TranslatableComponent("ias.mojauth.toofast"), reqerr); //CloudFront DoS/DDoS protection.
				}
				throw new AuthException(new TranslatableComponent("ias.auth.unknown", reqerr), reqerr);
			}
			String err = jo.get("error").getAsString();
			if (err.equals("ForbiddenOperationException")) {
				String msg = jo.get("errorMessage").getAsString();
				if (msg.equals("Invalid credentials. Invalid username or password.")) {
					throw new AuthException(new TranslatableComponent("ias.mojauth.invalidcreds"), jo.toString());
				}
				if (msg.equals("Invalid credentials.")) {
					throw new AuthException(new TranslatableComponent("ias.mojauth.toofast"), jo.toString());
				}
			}
			if (err.equals("ResourceException")) {
				throw new AuthException(new TranslatableComponent("ias.mojauth.migrated"), jo.toString());
			}
			throw new AuthException(new TranslatableComponent("ias.auth.unknown", jo.toString()));
		}
		JsonObject resp = new Gson().fromJson(r.body(), JsonObject.class);
		String accessToken = resp.get("accessToken").getAsString();
		UUID respClientToken = UUIDTypeAdapter.fromString(resp.get("clientToken").getAsString());
		if (!respClientToken.equals(clientToken))
			throw new AuthException(new TranslatableComponent("ias.auth.unknown",
					"Response token " + respClientToken + " is not equals to sent token " + clientToken));
		UUID uuid = UUIDTypeAdapter.fromString(resp.getAsJsonObject("selectedProfile").get("id").getAsString());
		String username = resp.getAsJsonObject("selectedProfile").get("name").getAsString();
		return new MojangAccount(username, accessToken, clientToken, uuid);
    }
}
