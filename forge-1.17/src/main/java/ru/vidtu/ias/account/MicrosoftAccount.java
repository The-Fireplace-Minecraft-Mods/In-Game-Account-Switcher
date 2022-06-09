package ru.vidtu.ias.account;

import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import org.apache.commons.lang3.tuple.Pair;
import ru.vidtu.ias.utils.Auth;
import the_fireplace.ias.IAS;

import java.util.UUID;
import java.util.function.Consumer;

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
	public void login(Minecraft mc, Consumer<Throwable> handler) {
		IAS.EXECUTOR.execute(() -> {
			try {
				syncRefresh();
			} catch (Throwable t) {
				mc.execute(() -> handler.accept(t));
				return;
			}
			mc.execute(() -> {
				mc.user = new User(username, UUIDTypeAdapter.fromUUID(uuid), accessToken, "mojang");
				handler.accept(null);
			});
		});
	}

	/**
	 * Synchronically validate and refresh account.
	 *
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
