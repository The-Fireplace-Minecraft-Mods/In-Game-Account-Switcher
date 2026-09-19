/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2026 VidTu
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
import org.jetbrains.annotations.Nullable;
import ru.vidtu.ias.auth.handlers.LoginHandler;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/**
 * Offline account instance.
 *
 * @author VidTu
 */
public final class OfflineAccount implements Account {
    /**
     * Account name.
     */
    @NotNull
    private final String name;

    /**
     * Account UUID.
     */
    @NotNull
    private final UUID uuid;

    /**
     * Account skin.
     */
    @Nullable
    private final UUID skin;

    /**
     * Creates a new offline account.
     *
     * @param name Offline account name
     * @param skin Skin to use, {@code null} if none
     */
    @Contract(pure = true)
    public OfflineAccount(@NotNull String name, @Nullable UUID skin) {
        this.name = name;
        this.uuid = uuid(name);
        this.skin = skin;
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public UUID uuid() {
        return this.uuid;
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String name() {
        return this.name;
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String type() {
        return "ias:offline_v2";
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String typeTipKey() {
        return "ias.accounts.tip.type.offline";
    }

    @Contract(value = "-> false", pure = true)
    @Override
    public boolean canLogin() {
        // Offline account can be logged in only via offline.
        return false;
    }

    @Contract(value = "-> false", pure = true)
    @Override
    public boolean insecure() {
        return false;
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public UUID skin() {
        return Objects.requireNonNullElse(this.skin, this.uuid);
    }

    @Override
    public void login(@NotNull LoginHandler handler, Runnable onComplete) {
        // Offline account can be logged in only via offline.
        handler.error(new UnsupportedOperationException("Offline account login: " + this));
    }

    /**
     * Writes the account to the output.
     *
     * @param out Target output
     * @throws IOException On I/O error
     */
    @Override
    public void write(@NotNull DataOutput out) throws IOException {
        // Write the name.
        out.writeUTF(this.name);

        // Write the skin.
        if (this.skin != null) {
            out.writeBoolean(true);
            out.writeLong(this.skin.getMostSignificantBits());
            out.writeLong(this.skin.getLeastSignificantBits());
        } else {
            out.writeBoolean(false);
        }
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OfflineAccount that)) return false;
        return Objects.equals(this.name, that.name);
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String toString() {
        return "OfflineAccount{" +
                "name='" + this.name + '\'' +
                ", uuid=" + this.uuid +
                '}';
    }

    /**
     * Reads the account (version 1) from the input.
     *
     * @param in Target input
     * @return Read account
     * @throws IOException On I/O error
     */
    @CheckReturnValue
    @NotNull
    public static OfflineAccount readV1(DataInput in) throws IOException {
        // Read the name.
        String name = in.readUTF();

        // Create and return.
        return new OfflineAccount(name, null);
    }

    /**
     * Reads the account (version 2) from the input.
     *
     * @param in Target input
     * @return Read account
     * @throws IOException On I/O error
     */
    @CheckReturnValue
    @NotNull
    public static OfflineAccount readV2(DataInput in) throws IOException {
        // Read the name.
        String name = in.readUTF();

        // Read the skin, if any.
        UUID skin = in.readBoolean() ? new UUID(in.readLong(), in.readLong()) : null;

        // Create and return.
        return new OfflineAccount(name, skin);
    }

    /**
     * Creates a conventional offline UUID from name.
     *
     * @param name Target name
     * @return Created conventional offline UUID
     */
    @Contract(pure = true)
    @NotNull
    public static UUID uuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }
}
