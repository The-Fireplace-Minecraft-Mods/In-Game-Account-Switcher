package ru.vidtu.iasfork.msauth;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Util;
import the_fireplace.ias.tools.HttpTools;

public class AuthSys {
	private static final Gson gson = new Gson();
	private static volatile HttpServer srv;
    public static void start(MSAuthHandler h) {
    	String done = "<html><body><h1>" + I18n.translate("ias.msauth.canclosenow") + "</h1></body></html>";
    	new Thread(() -> {
    		try {
    			if (srv != null) {
    				stop();
    				return;
    			}
    			h.setState("ias.msauth.state.waiting");
    			if (!HttpTools.ping("http://minecraft.net")) throw new MicrosoftAuthException("No intenet connection");
        		srv = HttpServer.create(new InetSocketAddress(59125), 0);
            	srv.createContext("/", new HttpHandler() {
    				public void handle(HttpExchange ex) throws IOException {
    					try {
    						h.cancellble(false);
    						h.setState("ias.msauth.state.response");
    						MinecraftClient.getInstance().execute(() -> GLFW.glfwFocusWindow(MinecraftClient.getInstance().getWindow().getHandle()));
    						ex.getResponseHeaders().add("Location", "http://localhost:59125/end");
    						ex.sendResponseHeaders(302, -1);
    						String s = ex.getRequestURI().getQuery();
    						if (s == null) {
    							h.error(new MicrosoftAuthException("query=null"));
    						} else if (s.startsWith("code=")) {
    							accessTokenStep(s.replace("code=", ""), h);
    						} else if (s.equals("error=access_denied&error_description=The user has denied access to the scope requested by the client application.")) {
    							h.error(new MicrosoftAuthException(I18n.translate("ias.msauth.error.revoked")));
    						} else {
    							h.error(new MicrosoftAuthException(s));
    						}
    					} catch (Throwable t) {
    						t.printStackTrace();
    						h.error(t);
    						stop();
    					}
    				}
    			});
            	srv.createContext("/end", new HttpHandler() {
					public void handle(HttpExchange ex) throws IOException {
						try {
							byte[] b = done.getBytes(StandardCharsets.UTF_8);
							ex.getResponseHeaders().put("Content-Type", Arrays.asList("text/html; charset=UTF-8"));
							ex.sendResponseHeaders(200, b.length);
							OutputStream os = ex.getResponseBody();
							os.write(b);
							os.flush();
							os.close();
						} catch (Throwable t) {
							t.printStackTrace();
						}
						stop();
					}
				});
            	srv.start();
            	Util.getOperatingSystem().open("https://login.live.com/oauth20_authorize.srf" +
                        "?client_id=54fd49e4-2103-4044-9603-2b028c814ec3" +
                        "&response_type=code" +
                        "&scope=XboxLive.signin%20XboxLive.offline_access" +
                        "&redirect_uri=http://localhost:59125" +
                        "&prompt=consent");
        	} catch (Throwable t) {
        		h.error(t);
        		stop();
        	}
    	}, "Auth Thread").start();
    }
    
    public static void start(String access, String refresh, MSAuthHandler h) {
    	h.cancellble(false);
    	new Thread(() -> {
    		try {
    			minecraftStoreVerify(access, refresh, h);
    		} catch (Throwable t) {
    			try {
    				h.setState("ias.msauth.state.refreshing");
    				Request r = new Request("https://login.live.com/oauth20_token.srf").get();
        			Map<Object, Object> data = new HashMap<>();
        			data.put("client_id", "54fd49e4-2103-4044-9603-2b028c814ec3");
        			data.put("refresh_token", refresh);
        			data.put("grant_type", "refresh_token");
        			data.put("redirect_uri", "http://localhost:59125");
        			r.post(data);
        			if (r.response() != 200) throw new MicrosoftAuthException("accessToken response: " + r.response());
        			JsonObject jo = gson.fromJson(r.body(), JsonObject.class);
                    xblStep(jo.get("access_token").getAsString(),
                    		jo.get("refresh_token").getAsString(), h);
    			} catch (Throwable th) {
    				h.error(t);
    			}
        	}
    	}, "Auth Thread").start();
    }
    
    public static void stop() {
    	try {
    		if (srv != null) {
    			srv.stop(0);
    			srv = null;
    		}
    	} catch (Throwable t) {}
    }

    public static void accessTokenStep(String code, MSAuthHandler h) throws Exception {
    	h.setState("ias.msauth.state.token");
		Request pr = new Request("https://login.live.com/oauth20_token.srf").header("Content-Type", "application/x-www-form-urlencoded");
        Map<Object, Object> data = new HashMap<>();
        data.put("client_id", "54fd49e4-2103-4044-9603-2b028c814ec3");
        data.put("code", code);
        data.put("grant_type", "authorization_code");
        data.put("redirect_uri", "http://localhost:59125");
        data.put("scope", "XboxLive.signin XboxLive.offline_access");
        pr.post(data);
        if (pr.response() != 200) throw new MicrosoftAuthException("accessToken response: " + pr.response());
        JsonObject jo = gson.fromJson(pr.body(), JsonObject.class);
        xblStep(jo.get("access_token").getAsString(),
        		jo.get("refresh_token").getAsString(), h);
    }

    public static void xblStep(String token, String refresh, MSAuthHandler h) throws Exception {
    	h.setState("ias.msauth.state.xbl");
		Request pr = new Request("https://user.auth.xboxlive.com/user/authenticate").header("Content-Type", "application/json").header("Accept", "application/json");
        HashMap<Object, Object> map = new HashMap<>();
        HashMap<Object, Object> sub = new HashMap<>();
        sub.put("AuthMethod", "RPS");
        sub.put("SiteName", "user.auth.xboxlive.com");
        sub.put("RpsTicket", "d=" + token);
        map.put("Properties", sub);
        map.put("RelyingParty", "http://auth.xboxlive.com");
        map.put("TokenType", "JWT");
        pr.post(gson.toJson(map));
        if (pr.response() != 200) throw new MicrosoftAuthException("xbl response: " + pr.response());
        xstsStep(gson.fromJson(pr.body(), JsonObject.class).get("Token").getAsString(), refresh, h);
    }

    public static void xstsStep(String xbl, String refresh, MSAuthHandler h) throws Exception {
    	h.setState("ias.msauth.state.xsts");
		Request pr = new Request("https://xsts.auth.xboxlive.com/xsts/authorize").header("Content-Type", "application/json").header("Accept", "application/json");
        HashMap<Object, Object> map = new HashMap<>();
        HashMap<Object, Object> sub = new HashMap<>();
        sub.put("SandboxId", "RETAIL");
        sub.put("UserTokens", Arrays.asList(xbl));
        map.put("Properties", sub);
        map.put("RelyingParty", "rp://api.minecraftservices.com/");
        map.put("TokenType", "JWT");
        pr.post(gson.toJson(map));
        if (pr.response() == 401) throw new MicrosoftAuthException(I18n.translate("ias.msauth.error.noxbox"));
        if (pr.response() != 200) throw new MicrosoftAuthException("xsts response: " + pr.response());
        JsonObject jo = gson.fromJson(pr.body(), JsonObject.class);
        minecraftTokenStep(jo.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0)
        		.getAsJsonObject().get("uhs").getAsString(), jo.get("Token").getAsString(), refresh, h);
    }

    public static void minecraftTokenStep(String xbl, String xsts, String refresh, MSAuthHandler h) throws Exception {
    	h.setState("ias.msauth.state.mcauth");
		Request pr = new Request("https://api.minecraftservices.com/authentication/login_with_xbox").header("Content-Type", "application/json").header("Accept", "application/json");
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("identityToken", "XBL3.0 x=" + xbl + ";" + xsts);
        pr.post(gson.toJson(map));
        if (pr.response() != 200) throw new MicrosoftAuthException("minecraftToken response: " + pr.response());
        minecraftStoreVerify(gson.fromJson(pr.body(), JsonObject.class).get("access_token").getAsString(), refresh, h);
    }

    public static void minecraftStoreVerify(String token, String refresh, MSAuthHandler h) throws Exception {
    	h.setState("ias.msauth.state.verify");
		Request pr = new Request("https://api.minecraftservices.com/entitlements/mcstore").header("Authorization", "Bearer " + token).get();
        if (pr.response() != 200) throw new MicrosoftAuthException("minecraftStore response: " + pr.response());
        if (gson.fromJson(pr.body(), JsonObject.class).getAsJsonArray("items").size() == 0) throw new MicrosoftAuthException(I18n.translate("ias.msauth.error.gamenotowned"));
        minecraftProfileVerify(token, refresh, h);
    }

    public static void minecraftProfileVerify(String token, String refresh, MSAuthHandler h) throws Exception {
    	h.setState("ias.msauth.state.profile");
		Request pr = new Request("https://api.minecraftservices.com/minecraft/profile").header("Authorization", "Bearer " + token).get();
        if (pr.response() != 200) throw new MicrosoftAuthException("minecraftProfile response: " + pr.response());
        JsonObject jo = gson.fromJson(pr.body(), JsonObject.class);
        String name = (String) jo.get("name").getAsString();
        String uuid = (String) jo.get("id").getAsString();
        h.success(name, uuid, token, refresh);
    }
    
    public static class MicrosoftAuthException extends Exception {
    	private static final long serialVersionUID = 1L;
    	public MicrosoftAuthException() {}
		public MicrosoftAuthException(String s) {
			super(s);
		}
    }
}
