/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2024 VidTu
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.FINAL)
            .create();

    /**
     * Logger for this class.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("IAS/IASConfig");

    /**
     * For future config versions.
     */
    public static boolean skipMigration = false;

    /**
     * Whether the title screen text is enabled, {@code true} by default.
     */
    public static boolean titleText = true;

    /**
     * Custom title screen text X position, {@code null} by default.
     */
    public static String titleTextX = null;

    /**
     * Custom title screen text Y position, {@code null} by default.
     */
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
    public static String titleButtonX = null;

    /**
     * Custom title screen button Y position, {@code null} by default.
     */
    public static String titleButtonY = null;

    /**
     * Whether the servers screen text is enabled, {@code false} by default.
     */
    public static boolean serversText = true;

    /**
     * Custom servers screen text X position, {@code null} by default.
     */
    public static String serversTextX = null;

    /**
     * Custom servers screen text Y position, {@code null} by default.
     */
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
    public static String serversButtonX = null;

    /**
     * Custom servers screen button Y position, {@code null} by default.
     */
    public static String serversButtonY = null;

    /**
     * Allow storing accounts without Crypt.
     */
    public static boolean allowNoCrypt = false;

    /**
     * Display warning toasts for invalid names.
     */
    public static boolean nickWarns = true;

    /**
     * Allow unexpected pigs to show up.
     */
    public static boolean unexpectedPigs = true;

    /**
     * Creates a new config for GSON.
     */
    private IASConfig() {
        // Private
    }

    /**
     * Loads the config.
     *
     * @param path Config directory (not file)
     * @throws RuntimeException If unable to load the config
     */
    public static void load(Path path) {
        try {
            // Get the file.
            Path file = path.resolve("ias_v9.json");

            // Skip if it doesn't exist.
            if (!Files.isRegularFile(file)) {
                save(path);
                return;
            }

            // Read the file.
            String value = Files.readString(file);

            // Hacky JSON reading.
            GSON.fromJson(value, IASConfig.class);

            // NPE protection.
            titleTextAlign = Objects.requireNonNullElse(titleTextAlign, TextAlign.LEFT);
            serversTextAlign = Objects.requireNonNullElse(serversTextAlign, TextAlign.LEFT);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to load IAS config.", t);
        }
    }

    /**
     * Saves the config.
     *
     * @param path Config directory (not file)
     * @throws RuntimeException If unable to save the config
     */
    public static void save(Path path) {
        try {
            // Get the file.
            Path file = path.resolve("ias_v9.json");

            // Hacky JSON writing.
            @SuppressWarnings("InstantiationOfUtilityClass") // <- Hack.
            String value = GSON.toJson(new IASConfig());

            // Create parent directories.
            Files.createDirectories(file.getParent());

            // Write the file.
            Files.writeString(file, value, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE,
                    StandardOpenOption.SYNC, StandardOpenOption.DSYNC);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to save IAS config.", t);
        }
    }
}
