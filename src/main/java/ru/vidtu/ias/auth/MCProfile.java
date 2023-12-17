package ru.vidtu.ias.auth;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ru.vidtu.ias.utils.GSONUtils;

import java.lang.reflect.Type;
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
public record MCProfile(UUID uuid, String name) {
    /**
     * GSON deserializer for {@link MCProfile}.
     *
     * @author VidTu
     * @apiNote API (decode-only) adapter
     */
    public static final class Adapter implements JsonDeserializer<MCProfile> {
        /**
         * Dashless UUID pattern.
         */
        private static final Pattern UUID_DASHLESS = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

        /**
         * Dashed UUID replacer.
         */
        private static final String UUID_DASHED = "$1-$2-$3-$4-$5";

        @Override
        public MCProfile deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            try {
                // Get the JSON.
                JsonObject json = element.getAsJsonObject();

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
                throw new JsonParseException("Unable to parse Microsoft tokens: " + element, t);
            }
        }
    }
}
