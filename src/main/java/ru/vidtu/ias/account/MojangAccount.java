package ru.vidtu.ias.account;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import net.minecraft.text.TranslatableText;
import ru.vidtu.ias.mixins.MinecraftClientAccessor;
import ru.vidtu.ias.utils.Request;

public class MojangAccount implements Account {
	private String username;
	private String accessToken;
	private UUID clientToken;
	private UUID uuid;
	public int uses;
	public long lastUse;
	public MojangAccount(String username, String accessToken, UUID clientToken, UUID uuid) {
		this.username = username;
		this.accessToken = accessToken;
		this.clientToken = clientToken;
		this.uuid = uuid;
	}
	
	@Override
	public String alias() {
		return username;
	}

	@Override
	public void login(MinecraftClient mc, Consumer<Throwable> handler) {
		new Thread(() -> {
			try {
				if (!validate()) refresh();
			} catch (Throwable t) {
				mc.execute(() -> handler.accept(t));
				return;
			}
			mc.execute(() -> {
				((MinecraftClientAccessor)mc).setSession(new Session(username, UUIDTypeAdapter.fromUUID(uuid),
						accessToken, Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));
				handler.accept(null);
			});
		}, "IAS Mojang Reauth Thread").start();
	}
	
	public boolean validate() throws Exception {
		Request r = new Request("https://authserver.mojang.com/validate");
		r.header("Content-Type", "application/json");
		JsonObject jo = new JsonObject();
		jo.addProperty("accessToken", accessToken);
		jo.addProperty("clientToken", UUIDTypeAdapter.fromUUID(clientToken));
		r.post(jo.toString());
		return r.response() >= 200 && r.response() < 300;
	}
	
	public void refresh() throws Exception {
		Request r = new Request("https://authserver.mojang.com/refresh");
		r.header("Content-Type", "application/json");
		JsonObject req = new JsonObject();
		req.addProperty("accessToken", accessToken);
		req.addProperty("clientToken", UUIDTypeAdapter.fromUUID(clientToken));
		r.post(req.toString());
		if (r.response() < 200 || r.response() >= 300) throw new AuthException(new TranslatableText("ias.mojauth.unknown", r.error()));
		JsonObject resp = new Gson().fromJson(r.body(), JsonObject.class);
		accessToken = resp.get("accessToken").getAsString();
		clientToken = UUIDTypeAdapter.fromString(resp.get("clientToken").getAsString());
		uuid = UUIDTypeAdapter.fromString(resp.getAsJsonObject("selectedProfile").get("id").getAsString());
		username = resp.getAsJsonObject("selectedProfile").get("name").getAsString();
	}

	@Override
	public boolean editable() {
		return true;
	}

	@Override
	public boolean online() {
		return true;
	}
	
	@Override
	public int uses() {
		return uses;
	}

	@Override
	public long lastUse() {
		return lastUse;
	}
	
	@Override
	public void use() {
		uses++;
		lastUse = System.currentTimeMillis();
	}
	
	@Override
	public UUID uuid() {
		return uuid;
	}
}
