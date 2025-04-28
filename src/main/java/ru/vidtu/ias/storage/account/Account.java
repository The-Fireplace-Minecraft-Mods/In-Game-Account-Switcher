/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2025 VidTu
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

package ru.vidtu.ias.storage.account;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.ias.auth.handlers.LoginHandler;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.UTFDataFormatException;
import java.util.UUID;

/**
 * Parent class for all accounts.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public sealed abstract class Account permits OfflineAccount, MicrosoftAccount {
    /**
     * Whether the "Login" button should be enabled and call to {@link #login(LoginHandler)} is allowed
     */
    final boolean online;

    /**
     * Creates the account.
     *
     * @param online Whether the "Login" button should be enabled and call to {@link #login(LoginHandler)} is allowed
     */
    @Contract(pure = true)
    Account(boolean online) {
        this.online = online;
    }

    /**
     * Gets the online.
     *
     * @return Whether the "Login" button should be enabled and call to {@link #login(LoginHandler)} is allowed
     * @see #login(LoginHandler)
     */
    @Contract(pure = true)
    public final boolean online() {
        return this.online;
    }

    /**
     * Gets the UUID.
     *
     * @return Account UUID
     * @see #name()
     * @see #skin()
     */
    @Contract(pure = true)
    public abstract UUID uuid();

    /**
     * Gets the name.
     *
     * @return Account name
     * @see #uuid()
     */
    @Contract(pure = true)
    public abstract String name();

    /**
     * Gets the skin.
     *
     * @return Account skin UUID, usually the same as {@link #uuid()}
     * @see #uuid()
     */
    @Contract(pure = true)
    public abstract UUID skin();

    /**
     * Gets the tip.
     *
     * @return An immutable list of account hover tooltip lines
     */
    @Contract(pure = true)
    @Unmodifiable
    public abstract ImmutableList<Component> tip();

    /**
     * Starts the authentication process for this account.
     *
     * @param handler Login handler
     * @see #online()
     */
    public abstract void login(LoginHandler handler);

    /**
     * Encodes the account into the binary output.
     *
     * @param out Binary output
     * @throws IOException If an I/O error occurs
     */
    public abstract void encode(DataOutput out) throws IOException;

    /**
     * Decodes the account from the binary input.
     *
     * @param in Binary input
     * @return A newly created decoded account
     * @throws EOFException           If the {@code in} reaches the end before reading all the data
     * @throws UTFDataFormatException If the decoded {@code type} or {@code name} string byte sequence can't be used to create a valid UTF string
     * @throws InvalidObjectException If the decoded {@code type} is not valid
     * @throws IOException            If an I/O error occurs
     */
    @CheckReturnValue
    public static Account decode(DataInput in) throws IOException {
        // Validate.
        assert in != null : "IAS: Parameter 'in' is null.";

        // Decode the type.
        String type = in.readUTF(); // Implicit NPE for 'in'

        // Decode account by type.
        switch (type) {
            case "ias:microsoft_v2": return MicrosoftAccount.decode(in, /*hasInsecureFlag=*/false);
            case "ias:offline_v2": return OfflineAccount.decode(in, /*hasSkin=*/true);
            case "ias:microsoft_v1": return MicrosoftAccount.decode(in, /*hasInsecureFlag=*/true);
            case "ias:offline_v1": return OfflineAccount.decode(in, /*hasSkin=*/false);
            default: throw new InvalidObjectException("Unknown account type: " + type);
        }
    }
}
