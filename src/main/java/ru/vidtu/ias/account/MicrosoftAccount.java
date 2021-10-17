package ru.vidtu.ias.account;

import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import ru.vidtu.ias.mixins.MinecraftClientAccessor;
import ru.vidtu.ias.utils.Auth;

public class MicrosoftAccount implements Account {
	private String username;
	private String accessToken;
	private String refreshToken;
	private UUID uuid;
	private int uses;
	private long lastUse;
	
	public MicrosoftAccount(String name, String token, String refresh, UUID uuid) {
		this.username = name;
		this.accessToken = token;
		this.refreshToken = refresh;
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
				syncRefresh();
			} catch (Throwable t) {
				mc.execute(() -> handler.accept(t));
				return;
			}
			mc.execute(() -> {
				((MinecraftClientAccessor)mc).setSession(new Session(username, UUIDTypeAdapter.fromUUID(uuid), accessToken, "mojang"));
				handler.accept(null);
			});
		}, "IAS MS Reauth Thread").start();
	}
	
	/**
	 * Synchronically validate and refresh account.
	 * @throws Throwable If we're unable to refresh account
	 */
	public void syncRefresh() throws Throwable {
		try {
			Auth.checkGameOwnership(accessToken);
			Pair<UUID, String> profile = Auth.getProfile(accessToken);
			uuid = profile.getLeft();
			username = profile.getRight();
		} catch (Throwable t) {
			try {
				Pair<String, String> authRefreshTokens = Auth.refreshToken(refreshToken);
				String refreshToken = authRefreshTokens.getRight();
				String xblToken = Auth.authXBL(authRefreshTokens.getLeft()); //authToken
				Pair<String, String> xstsTokenUserhash = Auth.authXSTS(xblToken);
				String accessToken = Auth.authMinecraft(xstsTokenUserhash.getRight(), xstsTokenUserhash.getLeft());
				Auth.checkGameOwnership(accessToken);
				Pair<UUID, String> profile = Auth.getProfile(accessToken);
				this.uuid = profile.getLeft();
				this.username = profile.getRight();
				this.accessToken = accessToken;
				this.refreshToken = refreshToken;
			} catch (Throwable th) {
				th.addSuppressed(t);
				throw th;
			}
    	}
	}

	@Override
	public boolean editable() {
		return false;
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
