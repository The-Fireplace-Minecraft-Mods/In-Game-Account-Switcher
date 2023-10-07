package ru.vidtu.ias.auth.ms;

import com.google.errorprone.annotations.CheckReturnValue;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.auth.AuthData;
import ru.vidtu.ias.utils.GSONUtils;

import java.lang.reflect.Type;

/**
 * A pair of Microsoft access and refresh tokens.
 *
 * @param access
 * @param refresh
 * @author VidTu
 * @apiNote The {@link #access()} provided is not suitable for {@link AuthData#token()}
 * @see MSAuth#codeToTokens(String)
 * @see MSAuth#refreshToTokens(String)
 * @see MSAuth#accessToXbox(String)
 */
public record MSTokens(@NotNull String access, @NotNull String refresh) {
    /**
     * GSON deserializer for {@link MSTokens}.
     *
     * @author VidTu
     */
    public static final class Adapter implements JsonDeserializer<MSTokens> {
        @Contract(value = "!null, _, _ -> new; null, _, _ -> fail")
        @CheckReturnValue
        @Override
        @NotNull
        public MSTokens deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            try {
                JsonObject json = element.getAsJsonObject();
                String access = GSONUtils.getStringOrThrow(json, "access_token");
                String refresh = GSONUtils.getStringOrThrow(json, "refresh_token");
                return new MSTokens(access, refresh);
            } catch (Throwable t) {
                throw new JsonParseException("Unable to parse Microsoft tokens: " + element, t);
            }
        }
    }
}
