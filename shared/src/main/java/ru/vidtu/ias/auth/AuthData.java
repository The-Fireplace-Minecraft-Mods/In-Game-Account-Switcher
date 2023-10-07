package ru.vidtu.ias.auth;

import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.auth.account.Account;

import java.util.UUID;

/**
 * Data provided by {@link Account} for authentication in-game.
 *
 * @param name  Player name
 * @param uuid  Player UUID
 * @param token Session access token
 * @param type  User type
 * @author VidTu
 */
public record AuthData(@NotNull String name, @NotNull UUID uuid, @NotNull String token, @NotNull String type) {
    /**
     * Microsoft Authentication - current system used by Minecraft.
     */
    public static final String MSA = "msa";

    /**
     * Legacy Authentication - deprecated system, not officially supported by Minecraft, used for offline accounts.
     */
    public static final String LEGACY = "legacy";
}
