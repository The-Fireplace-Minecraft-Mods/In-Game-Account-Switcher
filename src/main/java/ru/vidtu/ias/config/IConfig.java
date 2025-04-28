/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2025 VidTu
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

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.config.migrator.Migrator;
import ru.vidtu.ias.platform.IStonecutter;
import ru.vidtu.ias.utils.GSONUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * IAS config storage.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class IConfig {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LogManager.getLogger("IAS/IConfig");

    /**
     * GSON instance for configuration loading/saving.
     */
    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.FINAL)
            .create();

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
     * Current HTTP server mode, {@link ServerMode#AVAILABLE} by default.
     */
    public static ServerMode server = ServerMode.AVAILABLE;

    /**
     * Crypt password echoing, {@code true} by default.
     */
    public static boolean passwordEchoing = true;

    /**
     * Creates a new config via GSON.
     */
    @Contract(pure = true)
    private IConfig() {
        // Private
    }

    /**
     * Loads the config, suppressing and logging any errors.
     *
     * @see #save()
     */
    public static void load() {
        try {
            // Log. (**TRACE**)
            LOGGER.trace(IAS.IAS_MARKER, "IAS: Loading the config... (directory: {})", IStonecutter.CONFIG_DIRECTORY);

            // Resolve the file.
            Path file = IStonecutter.CONFIG_DIRECTORY.resolve("ias.json");

            // Read the JSON.
            JsonObject json;
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                // Load the JSON.
                json = GSON.fromJson(reader, JsonObject.class);
            }

            // Determine the version.
            int version = (json.has("version") ? GSONUtils.getIntOrThrow(json, "version") : 1);
            LOGGER.trace(IAS.IAS_MARKER, "IAS: Loaded config version is {}.", version);

            // Migrate the config.
            Migrator migrator = Migrator.fromVersion(version);
            if (migrator != null) {
                LOGGER.info(IAS.IAS_MARKER, "IAS: Migrating old config version {} via {}.", version, migrator);
                migrator.load(json);
                LOGGER.info(IAS.IAS_MARKER, "IAS: Migrated old config.");
                save();
                return;
            }

            // Load the config.
            GSON.fromJson(json, IConfig.class);

            // Log. (**DEBUG**)
            LOGGER.debug(IAS.IAS_MARKER, "IAS: Config has been loaded. (directory: {}, file: {})", IStonecutter.CONFIG_DIRECTORY, file);
        } catch (NoSuchFileException nsfe) {
            // Log. (**DEBUG**)
            LOGGER.debug(IAS.IAS_MARKER, "IAS: Ignoring missing IAS config.", nsfe);
        } catch (Throwable t) {
            // Log.
            LOGGER.error(IAS.IAS_MARKER, "IAS: Unable to load the IAS config.", t);
        } finally {
            // Clamp.
            titleTextAlign = MoreObjects.firstNonNull(titleTextAlign, TextAlign.LEFT);
            serversTextAlign = MoreObjects.firstNonNull(serversTextAlign, TextAlign.LEFT);
            server = MoreObjects.firstNonNull(server, ServerMode.AVAILABLE);
        }
    }

    /**
     * Saves the storage, suppressing and logging any errors.
     *
     * @see #load()
     */
    public static void save() {
        try {
            // Log. (**TRACE**)
            LOGGER.trace(IAS.IAS_MARKER, "IAS: Saving the storage... (directory: {})", IStonecutter.CONFIG_DIRECTORY);

            // Resolve the file.
            Path file = IStonecutter.CONFIG_DIRECTORY.resolve("ias.json");

            // Save the config.
            JsonObject json = (JsonObject) GSON.toJsonTree(new IConfig());
            json.addProperty("version", 3);

            // Write the config.
            Files.createDirectories(file.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.SYNC)) {
                GSON.toJson(json, writer);
            }

            // Log. (**DEBUG**)
            LOGGER.debug(IAS.IAS_MARKER, "IAS: Config has been saved. (directory: {}, file: {})", IStonecutter.GAME_DIRECTORY, file);
        } catch (Throwable t) {
            // Log.
            LOGGER.error(IAS.IAS_MARKER, "IAS: Unable to save the IAS config.", t);
        }
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "IAS/IConfig{}";
    }
}
