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

package ru.vidtu.ias.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Various GSON utils.
 *
 * @author VidTu
 */
public final class GSONUtils {
    /**
     * Shared GSON instance.
     */
    @NotNull
    public static final Gson GSON = new Gson();

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    @Contract(value = "-> fail", pure = true)
    private GSONUtils() {
        throw new AssertionError("No instances.");
    }

    /**
     * Gets the boolean value from the JSON object.
     *
     * @param json Target object
     * @param key  Target key
     * @return Read boolean
     * @throws JsonParseException If there was no boolean by that key, was a non-boolean element by that key, or if the json or the key is {@code null}
     */
    @Contract(pure = true)
    public static boolean getBooleanOrThrow(@NotNull JsonObject json, @NotNull String key) {
        try {
            return json.get(key).getAsBoolean();
        } catch (Throwable t) {
            throw new JsonParseException("Expected to have boolean '" + key + "': " + json, t);
        }
    }

    /**
     * Gets the int value from the JSON object.
     *
     * @param json Target object
     * @param key  Target key
     * @return Read int
     * @throws JsonParseException If there was no int by that key, was a non-int element by that key, or if the json or the key is {@code null}
     */
    @Contract(pure = true)
    public static int getIntOrThrow(@NotNull JsonObject json, @NotNull String key) {
        try {
            return json.get(key).getAsInt();
        } catch (Throwable t) {
            throw new JsonParseException("Expected to have int '" + key + "': " + json, t);
        }
    }

    /**
     * Gets the long value from the JSON object.
     *
     * @param json Target object
     * @param key  Target key
     * @return Read long
     * @throws JsonParseException If there was no long by that key, was a non-long element by that key, or if the json or the key is {@code null}
     */
    @Contract(pure = true)
    public static long getLongOrThrow(@NotNull JsonObject json, @NotNull String key) {
        try {
            return json.get(key).getAsLong();
        } catch (Throwable t) {
            throw new JsonParseException("Expected to have long '" + key + "': " + json, t);
        }
    }

    /**
     * Gets the string value from the JSON object.
     *
     * @param json Target object
     * @param key  Target key
     * @return Read string
     * @throws JsonParseException If there was no string by that key, was a non-string element by that key, or if the json or the key is {@code null}
     */
    @Contract(pure = true)
    @NotNull
    public static String getStringOrThrow(@NotNull JsonObject json, @NotNull String key) {
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
    @Contract(pure = true)
    @NotNull
    public static JsonObject getObjectOrThrow(@NotNull JsonObject json, @NotNull String key) {
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
    @Contract(pure = true)
    @NotNull
    public static JsonArray getArrayOrThrow(@NotNull JsonObject json, @NotNull String key) {
        try {
            return json.get(key).getAsJsonArray();
        } catch (Throwable t) {
            throw new JsonParseException("Expected to have array '" + key + "': " + json, t);
        }
    }
}
