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

package ru.vidtu.ias.config;

import ru.vidtu.ias.utils.IUtils;

/**
 * HTTP server mode.
 *
 * @author VidTu
 */
public enum ServerMode {
    /**
     * Always use HTTP server, never use Microsoft device auth.
     */
    ALWAYS("ias.config.server.always"),

    /**
     * Use HTTP server if {@link IUtils#canUseSunServer()}, otherwise use Microsoft device auth.
     */
    AVAILABLE("ias.config.server.available"),

    /**
     * Never use HTTP server, always use Microsoft device auth.
     */
    NEVER("ias.config.server.never");

    /**
     * Mode translation key.
     */
    private final String key;

    /**
     * Creates a new mode.
     *
     * @param key Mode translation key
     */
    ServerMode(String key) {
        this.key = key;
    }

    /**
     * Gets the translation key.
     *
     * @return Translation key
     */
    @Override
    public String toString() {
        return this.key;
    }
}
