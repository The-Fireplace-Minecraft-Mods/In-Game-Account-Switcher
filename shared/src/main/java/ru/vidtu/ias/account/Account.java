package ru.vidtu.ias.account;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Interface implemented by all Minecraft accounts.
 *
 * @author VidTu
 */
public interface Account {
    /**
     * Get the UUID of this account.
     *
     * @return Account UUID
     */
    @NotNull UUID uuid();

    /**
     * Get the player name of this account.
     *
     * @return Account player name
     */
    @NotNull String name();

    /**
     * Future that will return auth data if authentication is successful or will complete exceptionally if authentication failed.
     * Future must be ready to be called from another thread.
     *
     * @param progressHandler Progress handler, must be ready to be called from another thread
     */
    @NotNull CompletableFuture<@NotNull AuthData> login(@NotNull BiConsumer<@NotNull String, @NotNull Object[]> progressHandler);

    /**
     * Immutable version-independent authentication data class.
     *
     * @author VidTu
     */
    class AuthData {
        /**
         * <code>Microsoft Authentication</code> - current system used by Minecraft.
         */
        public static final String MSA = "msa";

        /**
         * <code>Mojang Authentication</code> - deprecated system, no longer officially supported by Minecraft.
         */
        public static final String MOJANG = "mojang";

        /**
         * <code>Legacy Authentication</code> - deprecated system, not officially supported by Minecraft, often used for offline accounts.
         */
        public static final String LEGACY = "legacy";

        private final String name;
        private final UUID uuid;
        private final String accessToken;
        private final String userType;

        /**
         * Create new authentication data.
         *
         * @param name        Player name
         * @param uuid        Player UUID
         * @param accessToken Account access token
         * @param userType    Account type, usually <code>MSA</code>, <code>MOJANG</code> or <code>LEGACY</code>
         */
        public AuthData(@NotNull String name, @NotNull UUID uuid, @NotNull String accessToken, @NotNull String userType) {
            this.name = name;
            this.uuid = uuid;
            this.accessToken = accessToken;
            this.userType = userType;
        }

        /**
         * Get player name.
         *
         * @return Player name
         */
        @Contract(pure = true)
        public @NotNull String name() {
            return name;
        }

        /**
         * Get player unique ID.
         *
         * @return Player UUID
         */
        @Contract(pure = true)
        public @NotNull UUID uuid() {
            return uuid;
        }

        /**
         * Get account access token.
         *
         * @return Access token
         */
        @Contract(pure = true)
        public @NotNull String accessToken() {
            return accessToken;
        }

        /**
         * Get user type, usually <code>MSA</code>, <code>MOJANG</code> or <code>LEGACY</code>
         *
         * @return User type
         */
        @Contract(pure = true)
        public @NotNull String userType() {
            return userType;
        }
    }
}
