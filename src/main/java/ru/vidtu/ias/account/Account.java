/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2024 VidTu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package ru.vidtu.ias.account;

import java.io.DataInput;
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
     * Gets the account type translation tip key.
     *
     * @return Account type translation key
     */
    String typeTipKey();

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
     * Gets whether the UUID and name equal to this account UUID and name.
     *
     * @param uuid Target UUID
     * @param name Target name
     * @return Whether the profile is equal to this account
     */
    default boolean is(UUID uuid, String name) {
        return this.uuid().equals(uuid) && this.name().equals(name);
    }

    /**
     * Whether the player can log in into this account.
     *
     * @return Whether the {@link #login(LoginHandler)} is appropriate
     */
    boolean canLogin();

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
     * Writes the account type and account to the output.
     *
     * @param out Target output
     * @throws IOException              On I/O error
     * @throws IllegalArgumentException On unknown account type
     */
    @SuppressWarnings("ChainOfInstanceofChecks") // <- Sealed.
    static void writeTyped(DataOutput out, Account account) throws IOException {
        // Get the account type.
        String type;
        if (account instanceof OfflineAccount) {
            type = "ias:offline_v1";
        } else if (account instanceof MicrosoftAccount) {
            type = "ias:microsoft_v1";
        } else {
            throw new IllegalArgumentException("Unknown account type: " + account + " (" + (account != null ? account.getClass() : null) + ")");
        }

        // Write the type.
        out.writeUTF(type);

        // Write the data.
        account.write(out);
    }

    /**
     * Reads the account type and account from the input.
     *
     * @param in Target input
     * @return Read account
     * @throws IOException              On I/O error
     * @throws IllegalArgumentException On unknown account type
     */
    static Account readTyped(DataInput in) throws IOException {
        // Read the type.
        String type = in.readUTF();

        // Read and return the account by type.
        return switch (type) {
            case "ias:offline_v1" -> OfflineAccount.read(in);
            case "ias:microsoft_v1" -> MicrosoftAccount.read(in);
            default -> throw new IllegalArgumentException("Unknown account type: " + type);
        };
    }

    /**
     * Data provided for {@link Account} for authentication in-game.
     *
     * @param name   Player name
     * @param uuid   Player UUID
     * @param token  Session access token
     * @param online Whether the account type is online
     * @author VidTu
     */
    record LoginData(String name, UUID uuid, String token, boolean online) {
        @Override
        public String toString() {
            return "LoginData{" +
                    "name='" + this.name + '\'' +
                    ", uuid=" + this.uuid +
                    ", token=[TOKEN]" +
                    ", online=" + this.online +
                    '}';
        }
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
         * @param data    Auth data
         * @param changed Whether the storage has been modified and may require saving
         */
        void success(LoginData data, boolean changed);

        /**
         * Called when an authentication has failed.
         *
         * @param error Failure reason
         */
        void error(Throwable error);
    }
}
