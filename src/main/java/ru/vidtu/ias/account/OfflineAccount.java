package ru.vidtu.ias.account;

import java.util.function.Consumer;

import net.minecraft.client.MinecraftClient;

public class OfflineAccount implements Account {
	private String username;
	public int uses;
	public long lastUse;
	
	public OfflineAccount(String name) {
		this.username = name;
	}

	@Override
	public String alias() {
		return username;
	}

	@Override
	public void login(MinecraftClient mc, Consumer<Throwable> handler) {
		throw new UnsupportedOperationException("Account not online");
	}

	@Override
	public boolean editable() {
		return true;
	}

	@Override
	public boolean online() {
		return false;
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
}
