package ru.vidtu.ias.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.auth.account.Account;
import ru.vidtu.ias.auth.ms.MSAuth;
import ru.vidtu.ias.auth.account.MicrosoftAccount;
import ru.vidtu.ias.auth.account.OfflineAccount;
import ru.vidtu.ias.auth.storage.AccountStorage;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for loading, saving and manipulating config.
 *
 * @author VidTu
 */
public class IASConfig {
    // Accounts
    private AccountStorage accounts;

    // Config
    private boolean titleText = true;
    private String titleTextX;
    private String titleTextY;
    private Alignment titleTextAlign = Alignment.CENTER;
    private boolean titleButton = true;
    private String titleButtonX;
    private String titleButtonY;
    private boolean mpButton = false;
    private String mpButtonX;
    private String mpButtonY;

    @Contract(pure = true)
    public AccountStorage accounts() {
        return accounts;
    }

    @Contract(pure = true)
    public boolean titleText() {
        return titleText;
    }

    @Contract(pure = true)
    public String titleTextX() {
        return titleTextX;
    }

    @Contract(pure = true)
    public String titleTextY() {
        return titleTextY;
    }

    @Contract(pure = true)
    public Alignment titleTextAlign() {
        return titleTextAlign;
    }

    @Contract(pure = true)
    public boolean titleButton() {
        return titleButton;
    }

    @Contract(pure = true)
    public String titleButtonX() {
        return titleButtonX;
    }

    @Contract(pure = true)
    public String titleButtonY() {
        return titleButtonY;
    }

    @Contract(pure = true)
    public boolean mpButton() {
        return mpButton;
    }

    @Contract(pure = true)
    public String mpButtonX() {
        return mpButtonX;
    }

    @Contract(pure = true)
    public String mpButtonY() {
        return mpButtonY;
    }

    public void titleText(boolean titleText) {
        this.titleText = titleText;
    }

    public void titleTextX(String titleTextX) {
        this.titleTextX = titleTextX;
    }

    public void titleTextY(String titleTextY) {
        this.titleTextY = titleTextY;
    }

    public void titleTextAlign(Alignment titleTextAlign) {
        this.titleTextAlign = titleTextAlign;
    }

    public void titleButton(boolean titleButton) {
        this.titleButton = titleButton;
    }

    public void titleButtonX(String titleButtonX) {
        this.titleButtonX = titleButtonX;
    }

    public void titleButtonY(String titleButtonY) {
        this.titleButtonY = titleButtonY;
    }

    public void mpButton(boolean mpButton) {
        this.mpButton = mpButton;
    }

    public void mpButtonX(String mpButtonX) {
        this.mpButtonX = mpButtonX;
    }

    public void mpButtonY(String mpButtonY) {
        this.mpButtonY = mpButtonY;
    }

    /**
     * GSON serializer and deserializer for {@link IASConfig}.
     *
     * @author VidTu
     */
    public static final class Adapter implements JsonSerializer<IASConfig>, JsonDeserializer<IASConfig> {
        private static final int VERSION = 3;

        @Override
        public IASConfig deserialize(JsonElement entry, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            return null;
        }

        @Override
        public JsonElement serialize(IASConfig config, Type type, JsonSerializationContext ctx) {
            JsonObject json = new JsonObject();
            json.addProperty("version", VERSION);
            json.addProperty("titleText", config.titleText);
            json.addProperty("titleTextX", config.titleTextX);
            json.addProperty("titleTextY", config.titleTextY);
            json.addProperty("titleTextAlign", config.titleTextAlign.name());
            json.addProperty("titleButton", config.titleButton);
            json.addProperty("titleButtonX", config.titleButtonX);
            json.addProperty("titleButtonY", config.titleButtonY);
            json.addProperty("mpButton", config.mpButton);
            json.addProperty("mpButtonX", config.mpButtonX);
            json.addProperty("mpButtonY", config.mpButtonY);
            return json;
        }
    }

    // FIXME
//    /**
//     * Load config values from the config.
//     *
//     * @param gameDir Game directory path
//     */
//    public static void load(@NotNull Path gameDir) {
//        try {
//            Path p = gameDir.resolve("config").resolve("ias.json");
//            if (!Files.isRegularFile(p)) return;
//            JsonObject jo = new Gson().fromJson(new String(Files.readAllBytes(p), StandardCharsets.UTF_8), JsonObject.class);
//            if (!jo.has("version")) { // Assuming version 1.
//                accounts = jo.has("accounts") ? loadAccounts(jo.getAsJsonArray("accounts"), 1) : new ArrayList<>();
//                titleTextX = jo.has("textX") ? jo.get("textX").getAsString() : null;
//                titleTextX = jo.has("textY") ? jo.get("textY").getAsString() : null;
//                titleButtonX = jo.has("btnX") ? jo.get("btnX").getAsString() : null;
//                titleButtonY = jo.has("btnY") ? jo.get("btnY").getAsString() : null;
//                mpButton = jo.has("showOnMPScreen") && jo.get("showOnMPScreen").getAsBoolean();
//                titleButton = !jo.has("showOnTitleScreen") || jo.get("showOnTitleScreen").getAsBoolean();
//                return;
//            }
//            int version = jo.get("version").getAsInt();
//            if (version != CONFIG_VERSION) throw new IllegalStateException("Unknown config version: " + version + ", content: " + jo);
//            accounts = jo.has("accounts") ? loadAccounts(jo.getAsJsonArray("accounts"), version) : new ArrayList<>();
//            titleText = !jo.has("titleScreenText") || jo.get("titleScreenText").getAsBoolean();
//            titleTextX = jo.has("titleScreenTextX") ? jo.get("titleScreenTextX").getAsString() : null;
//            titleTextY = jo.has("titleScreenTextY") ? jo.get("titleScreenTextY").getAsString() : null;
//            titleTextAlign = jo.has("titleScreenTextAlignment") ? Alignment.getOr(jo.get("titleScreenTextAlignment").getAsString(), Alignment.CENTER) : Alignment.CENTER;
//            titleButton = !jo.has("titleScreenButton") || jo.get("titleScreenButton").getAsBoolean();
//            titleButtonX = jo.has("titleScreenButtonX") ? jo.get("titleScreenButtonX").getAsString() : null;
//            titleButtonY = jo.has("titleScreenButtonY") ? jo.get("titleScreenButtonY").getAsString() : null;
//            mpButton = jo.has("multiplayerScreenButton") && jo.get("multiplayerScreenButton").getAsBoolean();
//            mpButtonX = jo.has("multiplayerScreenButtonX") ? jo.get("multiplayerScreenButtonX").getAsString() : null;
//            mpButtonY = jo.has("multiplayerScreenButtonY") ? jo.get("multiplayerScreenButtonY").getAsString() : null;
//            experimentalJavaFXBrowser = jo.has("experimentalJavaFXBrowser") && jo.get("experimentalJavaFXBrowser").getAsBoolean() && experimentalJavaFXBrowserAvailable();
//        } catch (Throwable t) {
//            IAS.log().error("Unable to load IAS config.", t);
//        }
//    }
//
//    /**
//     * Load all accounts from JSON array.
//     *
//     * @param accounts JSON array to load
//     * @param version  Config version
//     * @return Loaded accounts
//     */
//    @Contract(pure = true)
//    private static @NotNull List<@NotNull Account> loadAccounts(@NotNull JsonArray accounts, int version) {
//        List<Account> accs = new ArrayList<>();
//        for (JsonElement je : accounts) {
//            Account account = loadAccount(je.getAsJsonObject().get("type").getAsString(), version == 1 ? je
//                    .getAsJsonObject().getAsJsonObject("data") : je.getAsJsonObject(), version);
//            if (account != null) accs.add(account);
//        }
//        return accs;
//    }
//
//    /**
//     * Load account from the type and provided JSON object.
//     *
//     * @param type    Account type, usually <code>ias:microsoft</code> or <code>ias:offline</code>
//     * @param json    Account JSON data
//     * @param version Config version
//     * @return New account, <code>null</code> if unable to create or unknown type
//     * @apiNote This method can be easily injected by Mixins or ASM if you want to support custom accounts via mods.
//     */
//    @Contract(pure = true)
//    private static @Nullable Account loadAccount(@NotNull String type, @NotNull JsonObject json, int version) {
//        if (type.equalsIgnoreCase("ias:microsoft") || type.equalsIgnoreCase("ru.vidtu.ias.auth.account.MicrosoftAccount")) {
//            return new MicrosoftAccount(UUID.fromString(json.get("uuid").getAsString()),
//                    version == 1 ? json.get("username").getAsString() : json.get("name").getAsString(),
//                    json.get("accessToken").getAsString(), json.get("refreshToken").getAsString());
//        }
//        if (type.equalsIgnoreCase("ias:offline") || type.equalsIgnoreCase("ru.vidtu.ias.auth.account.OfflineAccount")) {
//            String name = version == 1 ? json.get("username").getAsString() : json.get("name").getAsString();
//            UUID uuid;
//            if (version == 1) {
//                MSAuth auth = new MSAuth(IAS.userAgent(), IAS.clientId(), IAS.redirectUri(), Duration.ofSeconds(5L), IAS.executor());
//                try {
//                    uuid = auth.nameToProfile(name).get().uuid();
//                } catch (Exception ignored) {
//                    uuid = UUID.nameUUIDFromBytes("OfflinePlayer:".concat(name).getBytes(StandardCharsets.UTF_8));
//                }
//            } else {
//                uuid =UUID.fromString(json.get("uuid").getAsString());
//            }
//            return new OfflineAccount(name, uuid);
//        }
//        return null;
//    }
//
//    /**
//     * Save config values to the config.
//     *
//     * @param gameDir Game directory path
//     */
//    public static void save(@NotNull Path gameDir) {
//        try {
//            Path p = gameDir.resolve("config").resolve("ias.json");
//            Files.createDirectories(p.getParent());
//            JsonObject jo = new JsonObject();
//            jo.addProperty("version", CONFIG_VERSION);
//            jo.add("accounts", saveAccounts(accounts));
//            jo.addProperty("titleScreenText", titleText);
//            if (titleTextX != null) jo.addProperty("titleScreenTextX", titleTextX);
//            if (titleTextY != null) jo.addProperty("titleScreenTextY", titleTextY);
//            if (titleTextAlign != null) jo.addProperty("titleScreenTextAlignment", titleTextAlign.name());
//            jo.addProperty("titleScreenButton", titleButton);
//            if (titleButtonX != null) jo.addProperty("titleScreenButtonX", titleButtonX);
//            if (titleButtonY != null) jo.addProperty("titleScreenButtonY", titleButtonY);
//            jo.addProperty("multiplayerScreenButton", mpButton);
//            if (mpButtonX != null) jo.addProperty("multiplayerScreenButtonX", mpButtonX);
//            if (mpButtonY != null) jo.addProperty("multiplayerScreenButtonY", mpButtonY);
//            jo.addProperty("experimentalJavaFXBrowser", experimentalJavaFXBrowser);
//            Files.write(p, jo.toString().getBytes(StandardCharsets.UTF_8));
//        } catch (Throwable t) {
//            IAS.log().error("Unable to save IAS config.", t);
//        }
//    }
//
//    /**
//     * Save all accounts to the JSON array.
//     *
//     * @param accounts Accounts list
//     * @return Saved accounts as JSON array
//     */
//    @Contract(pure = true)
//    private static @NotNull JsonArray saveAccounts(@NotNull List<@NotNull Account> accounts) {
//        JsonArray ja = new JsonArray();
//        for (Account a : accounts) {
//            JsonObject jo = saveAccount(a);
//            if (jo != null) ja.add(jo);
//        }
//        return ja;
//    }
//
//    /**
//     * Save to account to JSON object.
//     *
//     * @param account Account to save
//     * @return Saved JSON account, <code>null</code> if unable to save or unknown type
//     * @apiNote This method can be easily injected by Mixins or ASM if you want to support custom accounts via mods.
//     */
//    @Contract(pure = true)
//    private static @Nullable JsonObject saveAccount(@NotNull Account account) {
//        InstanceCreator
//        if (account instanceof MicrosoftAccount) {
//            JsonObject jo = new JsonObject();
//            MicrosoftAccount ma = (MicrosoftAccount) account;
//            jo.addProperty("type", "ias:microsoft");
//            jo.addProperty("name", ma.name());
//            jo.addProperty("accessToken", ma.access());
//            jo.addProperty("refreshToken", ma.refresh());
//            jo.addProperty("uuid", ma.uuid().toString());
//            return jo;
//        }
//        if (account instanceof OfflineAccount) {
//            JsonObject jo = new JsonObject();
//            jo.addProperty("type", "ias:offline");
//            jo.addProperty("name", account.name());
//            jo.addProperty("uuid", account.uuid().toString());
//            return jo;
//        }
//        return null;
//    }
}
