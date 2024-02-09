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

package ru.vidtu.ias.auth;

import ru.vidtu.ias.account.Account;

import java.util.UUID;

/**
 * Data provided for {@link Account} for authentication in-game.
 *
 * @param name   Player name
 * @param uuid   Player UUID
 * @param token  Session access token
 * @param online Whether the account type is online
 * @author VidTu
 */
public record LoginData(String name, UUID uuid, String token, boolean online) {
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
