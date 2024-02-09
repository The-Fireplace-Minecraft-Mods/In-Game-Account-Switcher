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

package ru.vidtu.ias.config.migrator;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.regex.Pattern;

/**
 * Config migration helper.
 *
 * @author VidTu
 */
public sealed interface Migrator permits MigratorV1, MigratorV2 {
    /**
     * Data obfuscation helper.
     */
    Pattern OBFUSCATE_LOGS = Pattern.compile("(\"?(?:accessToken|refreshToken)\"?\\s*:)\"?[^,\":{}\\[\\]]*\"?", Pattern.CASE_INSENSITIVE);

    /**
     * Loads the config.
     *
     * @param json Target JSON
     * @throws JsonParseException If unable to load
     */
    void load(JsonObject json);

    /**
     * Gets the migrator for the version.
     *
     * @param version Target version
     * @return Any config migrator, {@code null} to use direct config loading method
     * @throws IllegalArgumentException If the version is not valid
     */
    static Migrator fromVersion(int version) {
        return switch (version) {
            case 1 -> new MigratorV1();
            case 2 -> new MigratorV2();
            case 3 -> null;
            default -> throw new IllegalArgumentException("Unknown config version: " + version);
        };
    }
}
