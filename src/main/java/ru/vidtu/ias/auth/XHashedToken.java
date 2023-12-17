package ru.vidtu.ias.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ru.vidtu.ias.utils.GSONUtils;

import java.lang.reflect.Type;

/**
 * XBL or XSTS token paired with user hash.
 *
 * @param token Target token
 * @param hash  User hash
 * @author VidTu
 * @see MSAuth#msaToXbl(String)
 * @see MSAuth#xblToXsts(String, String)
 * @see MSAuth#xstsToMca(String, String)
 */
public record XHashedToken(String token, String hash) {
    /**
     * GSON deserializer for {@link XHashedToken}.
     *
     * @author VidTu
     * @apiNote API (decode-only) adapter
     */
    public static final class Adapter implements JsonDeserializer<XHashedToken> {
        @Override
        public XHashedToken deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            try {
                // Get the JSON.
                JsonObject json = element.getAsJsonObject();

                // Extract the token.
                String token = GSONUtils.getStringOrThrow(json, "Token");
                JsonObject displayClaims = GSONUtils.getObjectOrThrow(json, "DisplayClaims");

                // Extract the XUI (UHS) token.
                JsonArray xui = GSONUtils.getArrayOrThrow(displayClaims, "xui");
                int size = xui.size();
                if (size != 1) {
                    throw new IllegalArgumentException("Unexpected 'xui' size: " + size);
                }
                JsonElement xuiEntry = xui.get(0);
                if (!xuiEntry.isJsonObject()) {
                    throw new IllegalArgumentException("Entry of 'xui' is not a JSON object: " + xuiEntry);
                }
                JsonObject xuiJson = xuiEntry.getAsJsonObject();
                String uhs = GSONUtils.getStringOrThrow(xuiJson, "uhs");

                // Create the token.
                return new XHashedToken(token, uhs);
            } catch (Throwable t) {
                // Rethrow.
                throw new JsonParseException("Unable to parse user hashed token: " + element, t);
            }
        }
    }
}
