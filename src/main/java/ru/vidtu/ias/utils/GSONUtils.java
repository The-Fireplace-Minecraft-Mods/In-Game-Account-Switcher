/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2023 VidTu
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

package ru.vidtu.ias.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Various GSON utils.
 *
 * @author VidTu
 */
public final class GSONUtils {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private GSONUtils() {
        throw new AssertionError("No instances.");
    }

    /**
     * Gets the string value from the JSON object.
     *
     * @param json Target object
     * @param key  Target key
     * @return Read string
     * @throws JsonParseException If there was no string by that key, was a non-string element by that key, or if the json or the key is {@code null}
     */
    public static String getStringOrThrow(JsonObject json, String key) {
        try {
            return json.get(key).getAsString();
        } catch (Throwable t) {
            throw new JsonParseException("Expected to have string '" + key + "': " + json, t);
        }
    }

    /**
     * Gets the object value from the JSON object.
     *
     * @param json Target object
     * @param key  Target key
     * @return Read object
     * @throws JsonParseException If there was no object by that key, was a non-object element by that key, or if the json or the key is {@code null}
     */
    public static JsonObject getObjectOrThrow(JsonObject json, String key) {
        try {
            return json.get(key).getAsJsonObject();
        } catch (Throwable t) {
            throw new JsonParseException("Expected to have object '" + key + "': " + json, t);
        }
    }

    /**
     * Gets the array value from the JSON object.
     *
     * @param json Target object
     * @param key  Target key
     * @return Read array
     * @throws JsonParseException If there was no array by that key, was a non-array element by that key, or if the json or the key is {@code null}
     */
    public static JsonArray getArrayOrThrow(JsonObject json, String key) {
        try {
            return json.get(key).getAsJsonArray();
        } catch (Throwable t) {
            throw new JsonParseException("Expected to have array '" + key + "': " + json, t);
        }
    }
}
