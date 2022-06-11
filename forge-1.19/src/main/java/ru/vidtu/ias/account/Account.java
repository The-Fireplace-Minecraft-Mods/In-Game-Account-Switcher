package ru.vidtu.ias.account;

import net.minecraft.client.Minecraft;

import java.util.UUID;
import java.util.function.Consumer;

public interface Account {
    String alias();

    void login(Minecraft mc, Consumer<Throwable> handler);

    boolean editable();

    boolean online();

    void use();

    int uses();

    long lastUse();

    UUID uuid();
}
