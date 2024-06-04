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

import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.auth.handlers.LoginHandler;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

/**
 * Parent interface for all accounts.
 *
 * @author VidTu
 */
public sealed interface Account permits OfflineAccount, MicrosoftAccount {
    /**
     * Gets the account type.
     *
     * @return Account storage type
     */
    @Contract(pure = true)
    @NotNull
    String type();

    /**
     * Gets the account type translation tip key.
     *
     * @return Account type translation key
     */
    @Contract(pure = true)
    @NotNull
    String typeTipKey();

    /**
     * Gets the UUID of this account.
     *
     * @return Account UUID
     */
    @Contract(pure = true)
    @NotNull
    UUID uuid();

    /**
     * Gets the username of this account.
     *
     * @return Account player name
     */
    @Contract(pure = true)
    @NotNull
    String name();

    /**
     * Whether the player can log in into this account.
     *
     * @return Whether the {@link #login(LoginHandler)} is appropriate
     */
    @Contract(pure = true)
    boolean canLogin();

    /**
     * Gets the insecure state.
     *
     * @return Whether the account is insecurely stored
     */
    @Contract(pure = true)
    boolean insecure();

    /**
     * Gets the skin UUID.
     *
     * @return Account skin UUID
     */
    @Contract(pure = true)
    @NotNull
    UUID skin();

    /**
     * Starts the authentication process for this account.
     *
     * @param handler Login handler
     */
    void login(@NotNull LoginHandler handler);

    /**
     * Writes the account to the output.
     *
     * @param out Target output
     * @throws IOException On I/O error
     */
    void write(@NotNull DataOutput out) throws IOException;

    /**
     * Writes the account type and account to the output.
     *
     * @param out     Target output
     * @param account Target account
     * @throws IOException              On I/O error
     * @throws IllegalArgumentException On unknown account type
     */
    static void writeTyped(@NotNull DataOutput out, @NotNull Account account) throws IOException {
        // Get the account type.
        String type = account.type();

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
    @CheckReturnValue
    @NotNull
    static Account readTyped(@NotNull DataInput in) throws IOException {
        // Read the type.
        String type = in.readUTF();

        // Read and return the account by type.
        return switch (type) {
            case "ias:offline_v1" -> OfflineAccount.readV1(in);
            case "ias:offline_v2" -> OfflineAccount.readV2(in);
            case "ias:microsoft_v1" -> MicrosoftAccount.read(in);
            default -> throw new IllegalArgumentException("Unknown account type: " + type);
        };
    }

}
