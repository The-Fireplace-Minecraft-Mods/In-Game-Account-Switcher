package ru.vidtu.ias.utils;

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
