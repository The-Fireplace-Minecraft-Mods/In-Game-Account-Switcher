package ru.vidtu.ias;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import ru.vidtu.ias.account.Account;
import the_fireplace.ias.IAS;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config {
    public static final transient Gson GSON = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).registerTypeAdapter(Account.class, new IASJsonSerializer()).create();

    public static boolean caseSensitiveSearch;
    public static String textX, textY, btnX, btnY;
    public static boolean showOnMPScreen;
    public static boolean showOnTitleScreen = true;
    public static List<Account> accounts = new ArrayList<>();

    public static void load() {
        try {
            Path p = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("ias.json");
            if (!Files.isRegularFile(p)) return;
            GSON.fromJson(new String(Files.readAllBytes(p), StandardCharsets.UTF_8), Config.class);
        } catch (Throwable t) {
            IAS.LOG.error("Unable to load IAS config.", t);
        }
    }

    public static void save() {
        try {
            Path p = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("ias.json");
            Files.createDirectories(p.getParent());
            Files.write(p, GSON.toJson(new Config()).getBytes(StandardCharsets.UTF_8));
        } catch (Throwable t) {
            IAS.LOG.error("Unable to save IAS config.", t);
        }
    }

    public static class IASJsonSerializer implements JsonSerializer<Account>, JsonDeserializer<Account> {
        @Override
        public Account deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                JsonObject jo = json.getAsJsonObject();
                Class<?> type = Class.forName(jo.get("type").getAsString());
                return context.deserialize(jo.get("data"), type);
            } catch (Throwable t) {
                throw new JsonParseException("Unable to parse account: " + json, t);
            }
        }

        @Override
        public JsonElement serialize(Account src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jo = new JsonObject();
            jo.addProperty("type", src.getClass().getName());
            jo.add("data", context.serialize(src));
            return jo;
        }
    }
}
