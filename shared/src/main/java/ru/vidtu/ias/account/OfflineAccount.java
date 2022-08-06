package ru.vidtu.ias.account;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Offline account for Minecraft.
 *
 * @author VidTu
 */
public class OfflineAccount implements Account {
    private final String name;
    private final UUID uuid;
    public OfflineAccount(@NotNull String name, @NotNull UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public @NotNull UUID uuid() {
        return uuid;
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public @NotNull CompletableFuture<@NotNull AuthData> login(@NotNull BiConsumer<@NotNull String, @NotNull Object[]> progressHandler) {
        return CompletableFuture.completedFuture(new AuthData(name(), uuid(), "0", AuthData.LEGACY));
    }
}
