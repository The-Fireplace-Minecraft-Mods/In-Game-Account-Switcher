package ru.vidtu.ias.auth.ms;

import com.google.errorprone.annotations.CheckReturnValue;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
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
 * @see MSAuth#minecraftToProfile(String)
 * @see MSAuth#nameToProfile(String)
 */
public record MCProfile(@NotNull UUID uuid, @NotNull String name) {
    /**
     * GSON deserializer for {@link MCProfile}.
     *
     * @author VidTu
     */
    public static final class Adapter implements JsonDeserializer<MCProfile> {
        private static final Pattern UUID_DASHLESS = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
        private static final String UUID_DASHES = "$1-$2-$3-$4-$5";

        @Contract(value = "!null, _, _ -> new; null, _, _ -> fail")
        @CheckReturnValue
        @Override
        @NotNull
        public MCProfile deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            try {
                JsonObject json = element.getAsJsonObject();
                String id = GSONUtils.getStringOrThrow(json, "id");
                String name = GSONUtils.getStringOrThrow(json, "name");
                Matcher matcher = UUID_DASHLESS.matcher(id);
                if (!matcher.matches()) {
                    throw new IllegalStateException("Invalid UUID: " + id);
                }
                id = matcher.replaceAll(UUID_DASHES);
                UUID uuid = UUID.fromString(id);
                return new MCProfile(uuid, name);
            } catch (Throwable t) {
                throw new JsonParseException("Unable to parse Microsoft tokens: " + element, t);
            }
        }
    }
}
