package ru.vidtu.ias.legacy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vidtu.ias.SharedIAS;
import ru.vidtu.ias.account.Auth;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Skin loader for legacy versions which don't load skins for some reason.
 *
 * @author VidTu
 */
public class SkinLoader {
    /**
     * Load player's skin using {@link SharedIAS#EXECUTOR}.
     *
     * @param uuid Player UUID
     * @return Future that will return pair of image mapped to skin type (<code>true</code> - slim, <code>false</code> - classic), <code>null</code> if player has no skin,
     */
    public static @NotNull CompletableFuture<Map.@Nullable Entry<@NotNull BufferedImage, @NotNull Boolean>> loadSkin(@NotNull UUID uuid) {
        CompletableFuture<Map.Entry<BufferedImage, Boolean>> cf = new CompletableFuture<>();
        SharedIAS.EXECUTOR.execute(() -> {
            try {
                HttpsURLConnection conn = (HttpsURLConnection) new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid).openConnection();
                if (Auth.FIXED_CONTEXT != null) conn.setSSLSocketFactory(Auth.FIXED_CONTEXT.getSocketFactory());
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
                    try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        throw new IllegalArgumentException("loadSkin response: " + conn.getResponseCode() + ", data: " + err.lines().collect(Collectors.joining("\n")));
                    } catch (Throwable t) {
                        throw new IllegalArgumentException("loadSkin response: " + conn.getResponseCode(), t);
                    }
                }
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    JsonObject resp = SharedIAS.GSON.fromJson(in.lines().collect(Collectors.joining("\n")), JsonObject.class);
                    JsonObject json = StreamSupport.stream(resp.getAsJsonArray("properties").spliterator(), false)
                            .map(JsonElement::getAsJsonObject).filter(jo -> jo.get("name").getAsString().equalsIgnoreCase("textures"))
                            .findAny().map(jo -> SharedIAS.GSON.fromJson(new String(Base64.getDecoder().decode(jo.get("value")
                                    .getAsString()), StandardCharsets.UTF_8), JsonObject.class)).orElse(null);
                    if (json == null || !json.has("textures") || !json.getAsJsonObject("textures").has("SKIN")) {
                        cf.complete(null);
                        return;
                    }
                    JsonObject skin = json.getAsJsonObject("textures").getAsJsonObject("SKIN");
                    cf.complete(new AbstractMap.SimpleImmutableEntry<>(ImageIO.read(new URL(skin.get("url").getAsString())), skin
                            .has("metadata") && skin.getAsJsonObject("metadata")
                            .has("model") && skin.getAsJsonObject("metadata").get("model")
                            .getAsString().equalsIgnoreCase("slim")));
                }
            } catch (Throwable t) {
                SharedIAS.LOG.warn("Unable to load skin for: " + uuid, t);
            }
        });
        return cf;
    }
}
