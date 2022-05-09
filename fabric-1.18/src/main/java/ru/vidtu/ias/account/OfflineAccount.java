package ru.vidtu.ias.account;

import net.minecraft.client.Minecraft;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

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
    public void login(Minecraft mc, Consumer<Throwable> handler) {
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

    @Override
    public UUID uuid() {
        return UUID.nameUUIDFromBytes("OfflinePlayer".concat(username).getBytes(StandardCharsets.UTF_8));
    }
}
