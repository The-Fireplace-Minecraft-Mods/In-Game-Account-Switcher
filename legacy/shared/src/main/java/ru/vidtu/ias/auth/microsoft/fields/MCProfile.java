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

package ru.vidtu.ias.auth.microsoft.fields;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.auth.microsoft.MSAuth;
import ru.vidtu.ias.utils.GSONUtils;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minecraft player profile.
 *
 * @param uuid Profile UUID
 * @param name Profile name
 * @author VidTu
 * @see MSAuth#mcaToMcp(String)
 * @see MSAuth#nameToMcp(String)
 */
public record MCProfile(@NotNull UUID uuid, @NotNull String name) {
    /**
     * Dashless UUID pattern.
     */
    @NotNull
    private static final Pattern UUID_DASHLESS = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    /**
     * Dashed UUID replacer.
     */
    @NotNull
    private static final String UUID_DASHED = "$1-$2-$3-$4-$5";

    /**
     * Extracts the MS tokens from the JSON.
     *
     * @param json Target JSON
     * @return Extracted MS tokens
     * @throws JsonParseException If unable to extract
     */
    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static MCProfile fromJson(@NotNull JsonObject json) {
        try {
            // Extract the ID and name.
            String id = GSONUtils.getStringOrThrow(json, "id");
            String name = GSONUtils.getStringOrThrow(json, "name");

            // Validate and convert the UUID.
            Matcher matcher = UUID_DASHLESS.matcher(id);
            if (!matcher.matches()) {
                throw new IllegalStateException("Invalid UUID: " + id);
            }
            id = matcher.replaceAll(UUID_DASHED);
            UUID uuid = UUID.fromString(id);

            // Create and return.
            return new MCProfile(uuid, name);
        } catch (Throwable t) {
            // Rethrow.
            throw new JsonParseException("Unable to parse MCProfile: " + json, t);
        }
    }
}
