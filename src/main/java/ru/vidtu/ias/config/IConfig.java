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
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.platform.IStonecutter;
import ru.vidtu.ias.ui.config.ConfigScreen;
import ru.vidtu.ias.utils.Expression;
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
 * @see ConfigScreen
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
     * Show the 'Current Account' label on the title screen, {@code true} by default.
     */
    private static boolean titleText = true;

    /**
     * Custom title screen text X position, {@code null} by default.
     */
    @Nullable
    private static String titleTextX = null;

    /**
     * Custom title screen text Y position, {@code null} by default.
     */
    @Nullable
    private static String titleTextY = null;

    /**
     * Alignment for title screen text, {@link TextAlign#LEFT} by default.
     */
    private static TextAlign titleTextAlign = TextAlign.LEFT;

    /**
     * Whether the title screen button is enabled, {@code true} by default.
     */
    private static boolean titleButton = true;

    /**
     * Custom title screen button X position, {@code null} by default.
     */
    @Nullable
    private static String titleButtonX = null;

    /**
     * Custom title screen button Y position, {@code null} by default.
     */
    @Nullable
    private static String titleButtonY = null;

    /**
     * Whether the servers screen text is enabled, {@code false} by default.
     */
    private static boolean serversText = true;

    /**
     * Custom servers screen text X position, {@code null} by default.
     */
    @Nullable
    private static String serversTextX = null;

    /**
     * Custom servers screen text Y position, {@code null} by default.
     */
    @Nullable
    private static String serversTextY = null;

    /**
     * Alignment for servers screen text, {@link TextAlign#LEFT} by default.
     */
    private static TextAlign serversTextAlign = TextAlign.LEFT;

    /**
     * Whether the servers screen button is enabled, {@code false} by default.
     */
    private static boolean serversButton = true;

    /**
     * Custom servers screen button X position, {@code null} by default.
     */
    @Nullable
    private static String serversButtonX = null;

    /**
     * Custom servers screen button Y position, {@code null} by default.
     */
    @Nullable
    private static String serversButtonY = null;

    /**
     * Whether to show the nick in the title bar, {@code false} by default.
     */
    private static boolean barNick = false;

    /**
     * Current HTTP server mode, {@link ServerMode#AVAILABLE} by default.
     */
    private static ServerMode server = ServerMode.AVAILABLE;

    /**
     * Crypt password echoing, {@code true} by default.
     */
    private static boolean passwordEchoing = true;

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

    /**
     * Gets the title text state.
     *
     * @return Show the 'Current Account' label on the title screen, {@code true} by default
     * @see #titleText(boolean)
     * @see #titleTextX()
     * @see #titleTextY()
     * @see #titleTextAlign()
     */
    @Contract(pure = true)
    public static boolean titleText() {
        return titleText;
    }

    /**
     * Sets the title text state.
     *
     * @param titleText Show the 'Current Account' label on the title screen, {@code true} by default
     * @see #titleText()
     * @see #titleTextX()
     * @see #titleTextY()
     * @see #titleTextAlign()
     */
    public static void titleText(boolean titleText) {
        IConfig.titleText = titleText;
    }

    /**
     * Gets the title text X.
     *
     * @return Custom title screen text X position, {@code null} by default
     * @see #titleText()
     * @see #titleTextX(String)
     * @see #titleTextY()
     * @see #titleTextAlign()
     */
    @Contract(pure = true)
    @Nullable
    public static String titleTextX() {
        return titleTextX;
    }

    /**
     * Sets the title text X.
     *
     * @param titleTextX Custom title screen text X position, {@code null} by default
     * @see #titleText()
     * @see #titleTextX()
     * @see #titleTextY()
     * @see #titleTextAlign()
     */
    public static void titleTextX(@Nullable String titleTextX) {
        IConfig.titleTextX = (titleTextX == null || titleTextX.isBlank() ? null : Expression.SPACE_PATTERN.matcher(titleTextX.strip()).replaceAll(" "));
    }

    /**
     * Gets the title text Y.
     *
     * @return Custom title screen text Y position, {@code null} by default
     * @see #titleText()
     * @see #titleTextX()
     * @see #titleTextY(String)
     * @see #titleTextAlign()
     */
    @Contract(pure = true)
    @Nullable
    public static String titleTextY() {
        return titleTextY;
    }

    /**
     * Sets the title text Y.
     *
     * @param titleTextY Custom title screen text Y position, {@code null} by default
     * @see #titleText()
     * @see #titleTextX()
     * @see #titleTextY()
     * @see #titleTextAlign()
     */
    public static void titleTextY(@Nullable String titleTextY) {
        IConfig.titleTextY = (titleTextY == null || titleTextY.isBlank() ? null : Expression.SPACE_PATTERN.matcher(titleTextY.strip()).replaceAll(" "));
    }

    /**
     * Gets the title text align.
     *
     * @return Alignment for title screen text, {@link TextAlign#LEFT} by default
     * @see #titleText()
     * @see #titleTextX()
     * @see #titleTextY()
     * @see #titleTextAlign(TextAlign)
     * @see #cycleTitleTextAlign(boolean)
     */
    @Contract(pure = true)
    public static TextAlign titleTextAlign() {
        return titleTextAlign;
    }

    /**
     * Sets the title text align.
     *
     * @param titleTextAlign Alignment for title screen text, {@link TextAlign#LEFT} by default
     * @apiNote Only for config migration, use {@link #cycleTitleTextAlign(boolean)}
     * @see #titleText()
     * @see #titleTextX()
     * @see #titleTextY()
     * @see #titleTextAlign()
     * @see #cycleTitleTextAlign(boolean)
     */
    @ApiStatus.Obsolete
    public static void titleTextAlign(TextAlign titleTextAlign) {
        IConfig.titleTextAlign = MoreObjects.firstNonNull(titleTextAlign, TextAlign.LEFT);
    }

    /**
     * Cycles the title text align.
     *
     * @param back Whether to cycle backwards
     * @return New title text alignment
     * @see #titleText()
     * @see #titleTextX()
     * @see #titleTextY()
     * @see #titleTextAlign()
     * @see #titleTextAlign(TextAlign)
     */
    @CheckReturnValue
    public static TextAlign cycleTitleTextAlign(boolean back) {
        switch (titleTextAlign) {
            case LEFT: return (titleTextAlign = (back ? TextAlign.RIGHT : TextAlign.CENTER));
            case CENTER: return (titleTextAlign = (back ? TextAlign.LEFT : TextAlign.RIGHT));
            case RIGHT: return (titleTextAlign = (back ? TextAlign.CENTER : TextAlign.LEFT));
            default: return (titleTextAlign = TextAlign.LEFT);
        }
    }

    /**
     * Gets the title button.
     *
     * @return Whether the title screen button is enabled, {@code true} by default
     * @see #titleButton(boolean)
     * @see #titleButtonX()
     * @see #titleButtonY()
     */
    @Contract(pure = true)
    public static boolean titleButton() {
        return titleButton;
    }

    /**
     * Sets the title button.
     *
     * @param titleButton Whether the title screen button is enabled, {@code true} by default
     * @see #titleButton()
     * @see #titleButtonX()
     * @see #titleButtonY()
     */
    public static void titleButton(boolean titleButton) {
        IConfig.titleButton = titleButton;
    }

    /**
     * Gets the title button X.
     *
     * @return Custom title screen button X position, {@code null} by default
     * @see #titleButton()
     * @see #titleButtonX(String)
     * @see #titleButtonY()
     */
    @Contract(pure = true)
    @Nullable
    public static String titleButtonX() {
        return titleButtonX;
    }

    /**
     * Sets the title button X.
     *
     * @param titleButtonX Custom title screen button X position, {@code null} by default
     * @see #titleButton()
     * @see #titleButtonX()
     * @see #titleButtonY()
     */
    public static void titleButtonX(@Nullable String titleButtonX) {
        IConfig.titleButtonX = (titleButtonX == null || titleButtonX.isBlank() ? null : Expression.SPACE_PATTERN.matcher(titleButtonX.strip()).replaceAll(" "));
    }

    /**
     * Gets the title button Y.
     *
     * @return Custom title screen button Y position, {@code null} by default
     * @see #titleButton()
     * @see #titleButtonX()
     * @see #titleButtonY(String)
     */
    @Contract(pure = true)
    @Nullable
    public static String titleButtonY() {
        return titleButtonY;
    }

    /**
     * Sets the title button Y.
     *
     * @param titleButtonY Custom title screen button Y position, {@code null} by default
     * @see #titleButton()
     * @see #titleButtonX()
     * @see #titleButtonY()
     */
    public static void titleButtonY(@Nullable String titleButtonY) {
        IConfig.titleButtonY = (titleButtonY == null || titleButtonY.isBlank() ? null : Expression.SPACE_PATTERN.matcher(titleButtonY.strip()).replaceAll(" "));
    }

    /**
     * Gets the servers text state.
     *
     * @return Show the 'Current Account' label on the servers screen, {@code true} by default
     * @see #serversText(boolean)
     * @see #serversTextX()
     * @see #serversTextY()
     * @see #serversTextAlign()
     */
    @Contract(pure = true)
    public static boolean serversText() {
        return serversText;
    }

    /**
     * Sets the servers text state.
     *
     * @param serversText Show the 'Current Account' label on the servers screen, {@code true} by default
     * @see #serversText()
     * @see #serversTextX()
     * @see #serversTextY()
     * @see #serversTextAlign()
     */
    public static void serversText(boolean serversText) {
        IConfig.serversText = serversText;
    }

    /**
     * Gets the servers text X.
     *
     * @return Custom servers screen text X position, {@code null} by default
     * @see #serversText()
     * @see #serversTextX(String)
     * @see #serversTextY()
     * @see #serversTextAlign()
     */
    @Contract(pure = true)
    @Nullable
    public static String serversTextX() {
        return serversTextX;
    }

    /**
     * Sets the servers text X.
     *
     * @param serversTextX Custom servers screen text X position, {@code null} by default
     * @see #serversText()
     * @see #serversTextX()
     * @see #serversTextY()
     * @see #serversTextAlign()
     */
    public static void serversTextX(@Nullable String serversTextX) {
        IConfig.serversTextX = (serversTextX == null || serversTextX.isBlank() ? null : Expression.SPACE_PATTERN.matcher(serversTextX.strip()).replaceAll(" "));
    }

    /**
     * Gets the servers text Y.
     *
     * @return Custom servers screen text Y position, {@code null} by default
     * @see #serversText()
     * @see #serversTextX()
     * @see #serversTextY(String)
     * @see #serversTextAlign()
     */
    @Contract(pure = true)
    @Nullable
    public static String serversTextY() {
        return serversTextY;
    }

    /**
     * Sets the servers text Y.
     *
     * @param serversTextY Custom servers screen text Y position, {@code null} by default
     * @see #serversText()
     * @see #serversTextX()
     * @see #serversTextY()
     * @see #serversTextAlign()
     */
    public static void serversTextY(@Nullable String serversTextY) {
        IConfig.serversTextY = (serversTextY == null || serversTextY.isBlank() ? null : Expression.SPACE_PATTERN.matcher(serversTextY.strip()).replaceAll(" "));
    }

    /**
     * Gets the servers text align.
     *
     * @return Alignment for servers screen text, {@link TextAlign#LEFT} by default
     * @see #serversText()
     * @see #serversTextX()
     * @see #serversTextY()
     * @see #serversTextAlign(TextAlign)
     * @see #cycleServersTextAlign(boolean)
     */
    @Contract(pure = true)
    public static TextAlign serversTextAlign() {
        return serversTextAlign;
    }

    /**
     * Sets the servers text align.
     *
     * @param serversTextAlign Alignment for servers screen text, {@link TextAlign#LEFT} by default
     * @apiNote Only for config migration, use {@link #cycleServersTextAlign(boolean)}
     * @see #serversText()
     * @see #serversTextX()
     * @see #serversTextY()
     * @see #serversTextAlign()
     * @see #cycleServersTextAlign(boolean)
     */
    @ApiStatus.Obsolete
    public static void serversTextAlign(TextAlign serversTextAlign) {
        IConfig.serversTextAlign = MoreObjects.firstNonNull(serversTextAlign, TextAlign.LEFT);
    }

    /**
     * Cycles the servers text align.
     *
     * @param back Whether to cycle backwards
     * @return New servers text alignment
     * @see #serversText()
     * @see #serversTextX()
     * @see #serversTextY()
     * @see #serversTextAlign()
     * @see #serversTextAlign(TextAlign)
     */
    @CheckReturnValue
    public static TextAlign cycleServersTextAlign(boolean back) {
        switch (serversTextAlign) {
            case LEFT: return (serversTextAlign = (back ? TextAlign.RIGHT : TextAlign.CENTER));
            case CENTER: return (serversTextAlign = (back ? TextAlign.LEFT : TextAlign.RIGHT));
            case RIGHT: return (serversTextAlign = (back ? TextAlign.CENTER : TextAlign.LEFT));
            default: return (serversTextAlign = TextAlign.LEFT);
        }
    }

    /**
     * Gets the servers button.
     *
     * @return Whether the servers screen button is enabled, {@code true} by default
     * @see #serversButton(boolean)
     * @see #serversButtonX()
     * @see #serversButtonY()
     */
    @Contract(pure = true)
    public static boolean serversButton() {
        return serversButton;
    }

    /**
     * Sets the servers button.
     *
     * @param serversButton Whether the servers screen button is enabled, {@code true} by default
     * @see #serversButton()
     * @see #serversButtonX()
     * @see #serversButtonY()
     */
    public static void serversButton(boolean serversButton) {
        IConfig.serversButton = serversButton;
    }

    /**
     * Gets the servers button X.
     *
     * @return Custom servers screen button X position, {@code null} by default
     * @see #serversButton()
     * @see #serversButtonX(String)
     * @see #serversButtonY()
     */
    @Contract(pure = true)
    @Nullable
    public static String serversButtonX() {
        return serversButtonX;
    }

    /**
     * Sets the servers button X.
     *
     * @param serversButtonX Custom servers screen button X position, {@code null} by default
     * @see #serversButton()
     * @see #serversButtonX()
     * @see #serversButtonY()
     */
    public static void serversButtonX(@Nullable String serversButtonX) {
        IConfig.serversButtonX = (serversButtonX == null || serversButtonX.isBlank() ? null : Expression.SPACE_PATTERN.matcher(serversButtonX.strip()).replaceAll(" "));
    }

    /**
     * Gets the servers button Y.
     *
     * @return Custom servers screen button Y position, {@code null} by default
     * @see #serversButton()
     * @see #serversButtonX()
     * @see #serversButtonY(String)
     */
    @Contract(pure = true)
    @Nullable
    public static String serversButtonY() {
        return serversButtonY;
    }

    /**
     * Sets the servers button Y.
     *
     * @param serversButtonY Custom servers screen button Y position, {@code null} by default
     * @see #serversButton()
     * @see #serversButtonX()
     * @see #serversButtonY()
     */
    public static void serversButtonY(@Nullable String serversButtonY) {
        IConfig.serversButtonY = (serversButtonY == null || serversButtonY.isBlank() ? null : Expression.SPACE_PATTERN.matcher(serversButtonY.strip()).replaceAll(" "));
    }

    /**
     * Gets the server mode.
     *
     * @return Current HTTP server mode, {@link ServerMode#AVAILABLE} by default
     * @see #cycleServer(boolean)
     */
    public static ServerMode server() {
        return server;
    }

    /**
     * Cycles the server mode.
     *
     * @param back Whether to cycle backwards
     * @return New server mode
     * @see #server()
     */
    @CheckReturnValue
    public static ServerMode cycleServer(boolean back) {
        switch (server) {
            case ALWAYS: return (server = (back ? ServerMode.NEVER : ServerMode.AVAILABLE));
            case AVAILABLE: return (server = (back ? ServerMode.ALWAYS : ServerMode.NEVER));
            case NEVER: return (server = (back ? ServerMode.AVAILABLE : ServerMode.ALWAYS));
            default: return (server = ServerMode.AVAILABLE);
        }
    }

    /**
     * Gets the bar nick state.
     *
     * @return Whether to show the nick in the title bar, {@code false} by default
     * @see #barNick(boolean)
     */
    @Contract(pure = true)
    public static boolean barNick() {
        return barNick;
    }

    /**
     * Sets the bar nick state.
     *
     * @param barNick Whether to show the nick in the title bar, {@code false} by default
     * @see #barNick()
     */
    public static void barNick(boolean barNick) {
        IConfig.barNick = barNick;
    }

    /**
     * Gets the password echoing state.
     *
     * @return Crypt password echoing, {@code true} by default
     * @see #passwordEchoing(boolean)
     */
    public static boolean passwordEchoing() {
        return passwordEchoing;
    }

    /**
     * Sets the password echoing state.
     *
     * @param passwordEchoing Crypt password echoing, {@code true} by default
     * @see #passwordEchoing()
     */
    public static void passwordEchoing(boolean passwordEchoing) {
        IConfig.passwordEchoing = passwordEchoing;
    }
}
