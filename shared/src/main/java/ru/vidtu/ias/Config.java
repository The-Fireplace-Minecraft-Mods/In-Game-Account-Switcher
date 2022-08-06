package ru.vidtu.ias;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.Auth;
import ru.vidtu.ias.account.MicrosoftAccount;
import ru.vidtu.ias.account.OfflineAccount;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for loading, saving and manipulating config.
 *
 * @author VidTu
 */
public class Config {
    private static final int CONFIG_VERSION = 2;
    public static List<Account> accounts = new ArrayList<>();
    public static boolean titleScreenText = true;
    public static String titleScreenTextX;
    public static String titleScreenTextY;
    public static Alignment titleScreenTextAlignment = Alignment.CENTER;
    public static boolean titleScreenButton = true;
    public static String titleScreenButtonX;
    public static String titleScreenButtonY;
    public static boolean multiplayerScreenButton = false;
    public static String multiplayerScreenButtonX;
    public static String multiplayerScreenButtonY;
    public static boolean experimentalJavaFXBrowser = false;

    /**
     * Load config values from the config.
     *
     * @param gameDir Game directory path
     */
    public static void load(@NotNull Path gameDir) {
        try {
            Path p = gameDir.resolve("config").resolve("ias.json");
            if (!Files.isRegularFile(p)) return;
            JsonObject jo = SharedIAS.GSON.fromJson(new String(Files.readAllBytes(p), StandardCharsets.UTF_8), JsonObject.class);
            if (!jo.has("version")) { // Assuming version 1.
                accounts = jo.has("accounts") ? loadAccounts(jo.getAsJsonArray("accounts"), 1) : new ArrayList<>();
                titleScreenTextX = jo.has("textX") ? jo.get("textX").getAsString() : null;
                titleScreenTextX = jo.has("textY") ? jo.get("textY").getAsString() : null;
                titleScreenButtonX = jo.has("btnX") ? jo.get("btnX").getAsString() : null;
                titleScreenButtonY = jo.has("btnY") ? jo.get("btnY").getAsString() : null;
                multiplayerScreenButton = jo.has("showOnMPScreen") && jo.get("showOnMPScreen").getAsBoolean();
                titleScreenButton = !jo.has("showOnTitleScreen") || jo.get("showOnTitleScreen").getAsBoolean();
                return;
            }
            int version = jo.get("version").getAsInt();
            if (version != CONFIG_VERSION) throw new IllegalStateException("Unknown config version: " + version + ", content: " + jo);
            accounts = jo.has("accounts") ? loadAccounts(jo.getAsJsonArray("accounts"), version) : new ArrayList<>();
            titleScreenText = !jo.has("titleScreenText") || jo.get("titleScreenText").getAsBoolean();
            titleScreenTextX = jo.has("titleScreenTextX") ? jo.get("titleScreenTextX").getAsString() : null;
            titleScreenTextY = jo.has("titleScreenTextY") ? jo.get("titleScreenTextY").getAsString() : null;
            titleScreenTextAlignment = jo.has("titleScreenTextAlignment") ? Alignment.getOr(jo.get("titleScreenTextAlignment").getAsString(), Alignment.CENTER) : Alignment.CENTER;
            titleScreenButton = !jo.has("titleScreenButton") || jo.get("titleScreenButton").getAsBoolean();
            titleScreenButtonX = jo.has("titleScreenButtonX") ? jo.get("titleScreenButtonX").getAsString() : null;
            titleScreenButtonY = jo.has("titleScreenButtonY") ? jo.get("titleScreenButtonY").getAsString() : null;
            multiplayerScreenButton = jo.has("multiplayerScreenButton") && jo.get("multiplayerScreenButton").getAsBoolean();
            multiplayerScreenButtonX = jo.has("multiplayerScreenButtonX") ? jo.get("multiplayerScreenButtonX").getAsString() : null;
            multiplayerScreenButtonY = jo.has("multiplayerScreenButtonY") ? jo.get("multiplayerScreenButtonY").getAsString() : null;
            experimentalJavaFXBrowser = jo.has("experimentalJavaFXBrowser") && jo.get("experimentalJavaFXBrowser").getAsBoolean() && experimentalJavaFXBrowserAvailable();
        } catch (Throwable t) {
            SharedIAS.LOG.error("Unable to load IAS config.", t);
        }
    }

    /**
     * Load all accounts from JSON array.
     *
     * @param accounts JSON array to load
     * @param version  Config version
     * @return Loaded accounts
     */
    @Contract(pure = true)
    private static @NotNull List<@NotNull Account> loadAccounts(@NotNull JsonArray accounts, int version) {
        List<Account> accs = new ArrayList<>();
        for (JsonElement je : accounts) {
            Account account = loadAccount(je.getAsJsonObject().get("type").getAsString(), version == 1 ? je
                    .getAsJsonObject().getAsJsonObject("data") : je.getAsJsonObject(), version);
            if (account != null) accs.add(account);
        }
        return accs;
    }

    /**
     * Load account from the type and provided JSON object.
     *
     * @param type    Account type, usually <code>ias:microsoft</code> or <code>ias:offline</code>
     * @param json    Account JSON data
     * @param version Config version
     * @return New account, <code>null</code> if unable to create or unknown type
     * @apiNote This method can be easily injected by Mixins or ASM if you want to support custom accounts via mods.
     */
    @Contract(pure = true)
    private static @Nullable Account loadAccount(@NotNull String type, @NotNull JsonObject json, int version) {
        if (type.equalsIgnoreCase("ias:microsoft") || type.equalsIgnoreCase("ru.vidtu.ias.account.MicrosoftAccount")) {
            return new MicrosoftAccount(version == 1 ? json.get("username").getAsString() : json.get("name")
                    .getAsString(), json.get("accessToken").getAsString(), json.get("refreshToken").getAsString(),
                    UUID.fromString(json.get("uuid").getAsString()));
        }
        if (type.equalsIgnoreCase("ias:offline") || type.equalsIgnoreCase("ru.vidtu.ias.account.OfflineAccount")) {
            String name = version == 1 ? json.get("username").getAsString() : json.get("name").getAsString();
            return new OfflineAccount(name, version == 1 ? Auth.resolveUUID(name) : UUID.fromString(json.get("uuid").getAsString()));
        }
        return null;
    }

    /**
     * Save config values to the config.
     *
     * @param gameDir Game directory path
     */
    public static void save(@NotNull Path gameDir) {
        try {
            Path p = gameDir.resolve("config").resolve("ias.json");
            Files.createDirectories(p.getParent());
            JsonObject jo = new JsonObject();
            jo.addProperty("version", CONFIG_VERSION);
            jo.add("accounts", saveAccounts(accounts));
            jo.addProperty("titleScreenText", titleScreenText);
            if (titleScreenTextX != null) jo.addProperty("titleScreenTextX", titleScreenTextX);
            if (titleScreenTextY != null) jo.addProperty("titleScreenTextY", titleScreenTextY);
            if (titleScreenTextAlignment != null) jo.addProperty("titleScreenTextAlignment", titleScreenTextAlignment.name());
            jo.addProperty("titleScreenButton", titleScreenButton);
            if (titleScreenButtonX != null) jo.addProperty("titleScreenButtonX", titleScreenButtonX);
            if (titleScreenButtonY != null) jo.addProperty("titleScreenButtonY", titleScreenButtonY);
            jo.addProperty("multiplayerScreenButton", multiplayerScreenButton);
            if (multiplayerScreenButtonX != null) jo.addProperty("multiplayerScreenButtonX", multiplayerScreenButtonX);
            if (multiplayerScreenButtonY != null) jo.addProperty("multiplayerScreenButtonY", multiplayerScreenButtonY);
            jo.addProperty("experimentalJavaFXBrowser", experimentalJavaFXBrowser);
            Files.write(p, jo.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Throwable t) {
            SharedIAS.LOG.error("Unable to save IAS config.", t);
        }
    }

    /**
     * Save all accounts to the JSON array.
     *
     * @param accounts Accounts list
     * @return Saved accounts as JSON array
     */
    @Contract(pure = true)
    private static @NotNull JsonArray saveAccounts(@NotNull List<@NotNull Account> accounts) {
        JsonArray ja = new JsonArray();
        for (Account a : accounts) {
            JsonObject jo = saveAccount(a);
            if (jo != null) ja.add(jo);
        }
        return ja;
    }

    /**
     * Save to account to JSON object.
     *
     * @param account Account to save
     * @return Saved JSON account, <code>null</code> if unable to save or unknown type
     * @apiNote This method can be easily injected by Mixins or ASM if you want to support custom accounts via mods.
     */
    @Contract(pure = true)
    private static @Nullable JsonObject saveAccount(@NotNull Account account) {
        if (account instanceof MicrosoftAccount) {
            JsonObject jo = new JsonObject();
            MicrosoftAccount ma = (MicrosoftAccount) account;
            jo.addProperty("type", "ias:microsoft");
            jo.addProperty("name", ma.name());
            jo.addProperty("accessToken", ma.accessToken());
            jo.addProperty("refreshToken", ma.refreshToken());
            jo.addProperty("uuid", ma.uuid().toString());
            return jo;
        }
        if (account instanceof OfflineAccount) {
            JsonObject jo = new JsonObject();
            jo.addProperty("type", "ias:offline");
            jo.addProperty("name", account.name());
            jo.addProperty("uuid", account.uuid().toString());
            return jo;
        }
        return null;
    }

    /**
     * Get if experimental JavaFX browser window is available for your device and Java.
     *
     * @return <code>true</code> if JavaFX browser is available, <code>false</code> otherwise
     */
    public static boolean experimentalJavaFXBrowserAvailable() {
        try {
            Class.forName("javafx.scene.web.WebView");
            return true;
        } catch (Throwable ignored) {}
        return false;
    }

    /**
     * Text alignment.
     *
     * @author VidTu
     */
    public enum Alignment {
        LEFT("ias.configGui.titleScreenText.alignment.left"),
        CENTER("ias.configGui.titleScreenText.alignment.center"),
        RIGHT("ias.configGui.titleScreenText.alignment.right");
        private final String key;
        Alignment(@NotNull String key) {
            this.key = key;
        }

        /**
         * Get translation key.
         *
         * @return Translation key
         */
        @Contract(pure = true)
        public @NotNull String key() {
            return key;
        }

        /**
         * Get text alignment by name.
         *
         * @param name     Alignment name
         * @param fallback Fallback value
         * @return Alignment found by name, <code>fallback</code> if not found
         */
        @Contract(pure = true)
        public static @NotNull Alignment getOr(@NotNull String name, @NotNull Alignment fallback) {
            for (Alignment v : values()) {
                if (v.name().equalsIgnoreCase(name)) return v;
            }
            return fallback;
        }
    }
}
