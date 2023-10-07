package ru.vidtu.ias.auth.ms;

import com.google.errorprone.annotations.CheckReturnValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.utils.GSONUtils;

import java.lang.reflect.Type;

/**
 * XBL or XSTS token paired with user hash.
 *
 * @param token Target token
 * @param hash  User hash
 * @author VidTu
 * @see MSAuth#accessToXbox(String)
 * @see MSAuth#xboxToSecureToken(String, String)
 * @see MSAuth#secureTokenToMinecraft(String, String)
 */
public record XHashedToken(@NotNull String token, @NotNull String hash) {
    /**
     * GSON deserializer for {@link XHashedToken}.
     *
     * @author VidTu
     */
    public static final class Adapter implements JsonDeserializer<XHashedToken> {
        @Contract(value = "!null, _, _ -> new; null, _, _ -> fail")
        @CheckReturnValue
        @Override
        @NotNull
        public XHashedToken deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            try {
                JsonObject json = element.getAsJsonObject();
                String token = GSONUtils.getStringOrThrow(json, "Token");
                JsonObject displayClaims = GSONUtils.getObjectOrThrow(json, "DisplayClaims");
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
                return new XHashedToken(token, uhs);
            } catch (Throwable t) {
                throw new JsonParseException("Unable to parse user hashed token: " + element, t);
            }
        }
    }
}
