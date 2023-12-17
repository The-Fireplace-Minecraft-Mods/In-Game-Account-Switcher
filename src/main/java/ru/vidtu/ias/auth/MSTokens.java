package ru.vidtu.ias.auth;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.utils.GSONUtils;

import java.lang.reflect.Type;

/**
 * A pair of Microsoft access and refresh tokens.
 *
 * @param access
 * @param refresh
 * @author VidTu
 * @apiNote The {@link #access()} provided is not suitable for {@link Account.LoginData#token()}
 * @see MSAuth#msacToMsaMsr(String)
 * @see MSAuth#msrToMsaMsr(String)
 * @see MSAuth#msaToXbl(String)
 */
public record MSTokens(String access, String refresh) {
    /**
     * GSON deserializer for {@link MSTokens}.
     *
     * @author VidTu
     * @apiNote API (decode-only) adapter
     */
    public static final class Adapter implements JsonDeserializer<MSTokens> {
        @Override
        public MSTokens deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            try {
                // Get the JSON.
                JsonObject json = element.getAsJsonObject();

                // Extract the tokens.
                String access = GSONUtils.getStringOrThrow(json, "access_token");
                String refresh = GSONUtils.getStringOrThrow(json, "refresh_token");

                // Create the tokens.
                return new MSTokens(access, refresh);
            } catch (Throwable t) {
                // Rethrow.
                throw new JsonParseException("Unable to parse Microsoft tokens: " + element, t);
            }
        }
    }
}
