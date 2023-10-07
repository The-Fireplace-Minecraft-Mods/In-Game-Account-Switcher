package ru.vidtu.ias.auth.account;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.auth.AuthData;
import ru.vidtu.ias.auth.ms.AuthStage;
import ru.vidtu.ias.utils.GSONUtils;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Offline account instance.
 *
 * @author VidTu
 */
public record OfflineAccount(@NotNull UUID uuid, @NotNull String name) implements Account {
    @Contract(value = "_ -> new", pure = true)
    @Override
    @NotNull
    public CompletableFuture<AuthData> login(@NotNull Consumer<AuthStage> progress) {
        IAS.log().info("Logging (offline) as {}/{}", uuid, name);
        return CompletableFuture.completedFuture(new AuthData(name, uuid, "0", AuthData.LEGACY));
    }

    /**
     * GSON serializer and deserializer for {@link OfflineAccount}.
     *
     * @author VidTu
     */
    public static final class Adapter implements JsonSerializer<OfflineAccount>, JsonDeserializer<OfflineAccount> {
        @Contract(value = "_, _, _ -> new", pure = true)
        @Override
        @NotNull
        public JsonElement serialize(OfflineAccount account, Type type, JsonSerializationContext ctx) {
            JsonObject json = new JsonObject();
            json.addProperty("uuid", account.uuid.toString());
            json.addProperty("name", account.name);
            return json;
        }

        @Contract(value = "!null, _, _ -> new; null, _, _ -> fail", pure = true)
        @Override
        @NotNull
        public OfflineAccount deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            try {
                JsonObject json = element.getAsJsonObject();
                String uuidValue = GSONUtils.getStringOrThrow(json, "uuid");
                String name = GSONUtils.getStringOrThrow(json, "name");
                UUID uuid = UUID.fromString(uuidValue);
                return new OfflineAccount(uuid, name);
            } catch (Throwable t) {
                throw new JsonParseException("Unable to parse offline account: " + element, t);
            }
        }
    }
}
