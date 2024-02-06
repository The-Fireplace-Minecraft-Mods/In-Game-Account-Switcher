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
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Offline account instance.
 *
 * @param uuid Account UUID
 * @param name Account name
 * @author VidTu
 */
public record OfflineAccount(UUID uuid, String name) implements Account {
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
        // Write the UUID.
        out.writeLong(this.uuid.getMostSignificantBits());
        out.writeLong(this.uuid.getLeastSignificantBits());

        // Write the name.
        out.writeUTF(this.name);
    }

    /**
     * Reads the account from the input.
     *
     * @param in Target input
     * @return Read account
     * @throws IOException On I/O error
     */
    public static OfflineAccount read(DataInput in) throws IOException {
        // Read the UUID.
        UUID uuid = new UUID(in.readLong(), in.readLong());

        // Read the name.
        String name = in.readUTF();

        // Create and return.
        return new OfflineAccount(uuid, name);
    }

    /**
     * Creates an offline account with UUID matching the offline UUID convention.
     *
     * @param name Offline account name
     * @return Created offline account
     */
    public static OfflineAccount create(String name) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        return new OfflineAccount(uuid, name);
    }
}
