/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2026 VidTu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package ru.vidtu.ias.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.config.migrator.Migrator;
import ru.vidtu.ias.utils.GSONUtils;
import ru.vidtu.ias.utils.IUtils;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * IAS config.
 *
 * @author VidTu
 */
public final class IASConfig {
    /**
     * Config GSON.
     */
    @NotNull
    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.FINAL)
            .create();

    /**
     * Logger for this class.
     */
    @NotNull
    public static final Logger LOGGER = LoggerFactory.getLogger("IAS/IASConfig");

    /**
     * Whether the title screen text is enabled, {@code true} by default.
     */
    public static boolean titleText = true;

    /**
     * Custom title screen text X position, {@code null} by default.
     */
    @Nullable
    public static String titleTextX = null;

    /**
     * Custom title screen text Y position, {@code null} by default.
     */
    @Nullable
    public static String titleTextY = null;

    /**
     * Alignment for title screen text, {@link TextAlign#LEFT} by default.
     */
    @Nullable
    public static TextAlign titleTextAlign = TextAlign.LEFT;

    /**
     * Whether the title screen button is enabled, {@code true} by default.
     */
    public static boolean titleButton = true;

    /**
     * Custom title screen button X position, {@code null} by default.
     */
    @Nullable
    public static String titleButtonX = null;

    /**
     * Custom title screen button Y position, {@code null} by default.
     */
    @Nullable
    public static String titleButtonY = null;

    /**
     * Whether the servers screen text is enabled, {@code false} by default.
     */
    public static boolean serversText = true;

    /**
     * Custom servers screen text X position, {@code null} by default.
     */
    @Nullable
    public static String serversTextX = null;

    /**
     * Custom servers screen text Y position, {@code null} by default.
     */
    @Nullable
    public static String serversTextY = null;

    /**
     * Alignment for servers screen text, {@link TextAlign#LEFT} by default.
     */
    @Nullable
    public static TextAlign serversTextAlign = TextAlign.LEFT;

    /**
     * Whether the servers screen button is enabled, {@code false} by default.
     */
    public static boolean serversButton = true;

    /**
     * Custom servers screen button X position, {@code null} by default.
     */
    @Nullable
    public static String serversButtonX = null;

    /**
     * Custom servers screen button Y position, {@code null} by default.
     */
    @Nullable
    public static String serversButtonY = null;

    /**
     * Allow storing accounts without Crypt, {@code false} by default.
     */
    public static boolean allowNoCrypt = false;

    /**
     * Display warning toasts for invalid names, {@code true} by default.
     */
    public static boolean nickWarns = true;

    /**
     * Allow unexpected pigs to show up, {@code true} by default.
     */
    public static boolean unexpectedPigs = true;

    /**
     * Whether to show the nick in the title bar, {@code false} by default.
     */
    public static boolean barNick = false;

    /**
     * Whether to close the account screen after logging in, {@code false} by default.
     */
    public static boolean closeOnLogin = false;

    /**
     * Current HTTP server mode, {@link ServerMode#AVAILABLE} by default.
     */
    @Nullable
    public static ServerMode server = ServerMode.AVAILABLE;

    /**
     * Crypt password echoing, {@code true} by default.
     */
    public static boolean passwordEchoing = true;

    /**
     * Creates a new config for GSON.
     */
    @Contract(pure = true)
    private IASConfig() {
        // Private
    }

    /**
     * Loads the config.
     *
     * @param path Config directory (not file)
     * @throws RuntimeException If unable to load the config
     */
    public static void load(@NotNull Path path) {
        try {
            // Log.
            LOGGER.debug("IAS: Loading config for {}...", path);

            // Get the file.
            Path file = path.resolve("ias.json");

            // Skip if it doesn't exist.
            if (!Files.isRegularFile(file)) {
                LOGGER.debug("IAS: Config not found. Saving...");
                save(path);
                return;
            }

            // Read the file.
            String value = Files.readString(file);

            // Read JSON.
            JsonObject json = GSON.fromJson(value, JsonObject.class);
            int version = json.has("version") ? GSONUtils.getIntOrThrow(json, "version") : 1;
            LOGGER.trace("IAS: Loaded config version is {}.", version);

            // Load migrated, if any.
            Migrator migrator = Migrator.fromVersion(version);
            if (migrator != null) {
                LOGGER.info("IAS: Migrating old config version {} via {}.", version, migrator);
                migrator.load(json);
                LOGGER.info("IAS: Migrated old config.");
                save(path);
                return;
            }

            // Hacky JSON reading.
            GSON.fromJson(json, IASConfig.class);

            // Log it.
            LOGGER.debug("IAS: Config loaded.");
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to load IAS config.", t);
        } finally {
            // NPE protection.
            titleTextAlign = Objects.requireNonNullElse(titleTextAlign, TextAlign.LEFT);
            serversTextAlign = Objects.requireNonNullElse(serversTextAlign, TextAlign.LEFT);
            server = Objects.requireNonNullElse(server, ServerMode.AVAILABLE);
        }
    }

    /**
     * Saves the config.
     *
     * @param path Config directory (not file)
     * @throws RuntimeException If unable to save the config
     */
    public static void save(@NotNull Path path) {
        try {
            // Log.
            LOGGER.debug("IAS: Saving config into {}...", path);

            // NPE protection.
            titleTextAlign = Objects.requireNonNullElse(titleTextAlign, TextAlign.LEFT);
            serversTextAlign = Objects.requireNonNullElse(serversTextAlign, TextAlign.LEFT);
            server = Objects.requireNonNullElse(server, ServerMode.AVAILABLE);

            // Get the file.
            Path file = path.resolve("ias.json");

            // Hacky JSON writing.
            @SuppressWarnings("InstantiationOfUtilityClass") // <- Hack.
            JsonObject json = (JsonObject) GSON.toJsonTree(new IASConfig());

            // Write JSON.
            json.addProperty("version", 3);
            String value = GSON.toJson(json);

            // Create parent directories.
            Files.createDirectories(file.getParent());

            // Write the file.
            Files.writeString(file, value, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE,
                    StandardOpenOption.SYNC, StandardOpenOption.DSYNC);

            // Log.
            LOGGER.debug("IAS: Config saved to {}.", file);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to save IAS config.", t);
        }
    }

    /**
     * Gets whether to use server auth for MS.
     *
     * @return Whether to use server auth for MS
     */
    public static boolean useServerAuth() {
        if (server == null) return IUtils.canUseSunServer();
        return switch (server) {
            case ALWAYS -> true;
            case NEVER -> false;
            case AVAILABLE -> IUtils.canUseSunServer();
        };
    }
}
