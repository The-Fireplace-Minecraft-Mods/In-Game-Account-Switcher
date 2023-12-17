package ru.vidtu.ias.account;

import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Parent interface for all accounts.
 *
 * @author VidTu
 */
public sealed interface Account permits OfflineAccount, MicrosoftAccount {

    /**
     * Gets the UUID of this account.
     *
     * @return Account UUID
     */
    UUID uuid();

    /**
     * Gets the username of this account.
     *
     * @return Account player name
     */
    String name();

    /**
     * Starts the authentication process for this account.
     *
     * @param handler Login handler
     */
    void login(LoginHandler handler);

    /**
     * Writes the account to the output.
     *
     * @param out Target output
     * @throws IOException On I/O error
     */
    void write(DataOutput out) throws IOException;

    /**
     * Data provided for {@link Account} for authentication in-game.
     *
     * @param name  Player name
     * @param uuid  Player UUID
     * @param token Session access token
     * @param type  User type
     * @author VidTu
     */
    record LoginData(String name, UUID uuid, String token, String type) {
        /**
         * Microsoft Authentication - current system used by Minecraft.
         */
        public static final String MSA = "msa";

        /**
         * Legacy Authentication - deprecated system, not officially supported by Minecraft, used for offline accounts.
         */
        public static final String LEGACY = "legacy";
    }

    /**
     * Handler for logins.
     *
     * @author VidTu
     * @apiNote All methods in this class can be called from another thread
     */
    interface LoginHandler {
        /**
         * Changes the authentication stage.
         *
         * @param stage New auth stage translation key
         */
        void stage(String stage);

        /**
         * Requests an encryption password.
         *
         * @return Future that will complete with password string on password enter, with {@code null} on cancel, exceptionally on error
         */
        CompletableFuture<String> password();

        /**
         * Called when an authentication has performed successfully.
         *
         * @param data Auth data
         */
        void success(LoginData data);

        /**
         * Called when an authentication has failed.
         *
         * @param error Failure reason
         */
        void error(Throwable error);
    }
}
