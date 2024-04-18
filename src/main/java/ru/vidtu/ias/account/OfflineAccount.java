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
    private final String name;

    /**
     * Account UUID.
     */
    private final UUID uuid;

    /**
     * Creates a new offline account.
     *
     * @param name Offline account name
     */
    public OfflineAccount(String name) {
        this.name = name;
        this.uuid = uuid(name);
    }

    @Override
    public UUID uuid() {
        return this.uuid;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String type() {
        return "ias:offline_v1";
    }

    @Override
    public String typeTipKey() {
        return "ias.accounts.tip.type.offline";
    }

    @Override
    public boolean canLogin() {
        // Offline account can be logged in only via offline.
        return false;
    }

    @Override
    public boolean insecure() {
        return false;
    }

    @Override
    public void login(LoginHandler handler) {
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
    public void write(DataOutput out) throws IOException {
        // Write the name.
        out.writeUTF(this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OfflineAccount that)) return false;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public String toString() {
        return "OfflineAccount{" +
                "name='" + this.name + '\'' +
                ", uuid=" + this.uuid +
                '}';
    }

    /**
     * Reads the account from the input.
     *
     * @param in Target input
     * @return Read account
     * @throws IOException On I/O error
     */
    public static OfflineAccount read(DataInput in) throws IOException {
        // Read the name.
        String name = in.readUTF();

        // Create and return.
        return new OfflineAccount(name);
    }

    /**
     * Creates a conventional offline UUID from name.
     *
     * @param name Target name
     * @return Created conventional offline UUID
     */
    public static UUID uuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }
}
