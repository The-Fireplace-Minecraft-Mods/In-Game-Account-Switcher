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

package ru.vidtu.ias;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.auth.MSAuth;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.config.IASStorage;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main IAS class.
 *
 * @author VidTu
 */
public final class IAS {
    /**
     * IAS static Microsoft application ID.
     */
    public static final String CLIENT_ID = "54fd49e4-2103-4044-9603-2b028c814ec3";

    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("IAS");

    /**
     * Session. Used in {@link #USER_AGENT_TEMPLATE}
     */
    private static final UUID SESSION = UUID.randomUUID();

    /**
     * Template for {@link #userAgent}.
     */
    private static final String USER_AGENT_TEMPLATE = "IAS/%s (%s; %s %s; Minecraft %s; Java %s)";

    /**
     * IAS executor.
     */
    private static ScheduledExecutorService executor = null;

    /**
     * Current IAS user agent.
     */
    private static String userAgent = null;

    /**
     * Current IAS game directory.
     */
    private static Path gameDirectory;

    /**
     * Current IAS config directory.
     */
    private static Path configDirectory;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private IAS() {
        throw new AssertionError("No instances.");
    }

    /**
     * Initializes the IAS.
     *
     * @param gamePath   Game directory
     * @param configPath Config directory
     */
    public static void init(Path gamePath, Path configPath) {
        // Initialize the dirs.
        gameDirectory = gamePath;
        configDirectory = configPath;

        // Write the disclaimers.
        disclaimersStorage();

        // Read the config.
        loadConfigSafe();

        // Read the storage.
        loadStorageSafe();

        // Create the executor.
        executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "IAS Executor Thread"));
    }

    /**
     * Closes the IAS.
     */
    @SuppressWarnings("VariableNotUsedInsideIf") // <- Used in declared methods.
    public static void close() {
        // Shutdown the executor.
        shutdown:
        try {
            if (executor == null) break shutdown;
            executor.shutdown();
            if (executor.awaitTermination(30L, TimeUnit.SECONDS)) break shutdown;
            LOGGER.warn("Unable to shutdown IAS executor. Shutting down forcefully...");
            executor.shutdownNow();
            if (executor.awaitTermination(30L, TimeUnit.SECONDS)) break shutdown;
            LOGGER.error("Unable to shutdown IAS executor forcefully.");
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        executor = null;

        // Destroy the UA.
        userAgent = null;

        // Write the disclaimers, if we can.
        if (gameDirectory != null) {
            disclaimersStorage();
        }
    }

    /**
     * Gets the async executor for IAS.
     *
     * @return IAS executor
     * @throws NullPointerException If the executor is not available
     */
    public static ScheduledExecutorService executor() {
        Objects.requireNonNull(executor, "IAS executor is not available.");
        return executor;
    }

    /**
     * Gets the user agent for usage in {@link MSAuth}.
     *
     * @return Current {@code User-Agent} value for HTTP requests
     * @throws NullPointerException If user agent wasn't set
     */
    public static String userAgent() {
        Objects.requireNonNull(userAgent, "IAS user agent is not set.");
        return userAgent;
    }

    /**
     * Sets the user agent for usage in {@link MSAuth}.
     *
     * @param version       Mod version
     * @param loader        Mod loader
     * @param loaderVersion Mod loader version
     * @param gameVersion   Game version
     */
    public static void userAgent(String version, String loader, String loaderVersion, String gameVersion) {
        userAgent = USER_AGENT_TEMPLATE.formatted(version, SESSION, loader, loaderVersion, gameVersion, Runtime.version().toString());
        LOGGER.info("IAS user agent: {}", userAgent);
    }

    /**
     * Delegates to {@link IASConfig#loadSafe(Path)} with {@link #configDirectory}.
     *
     * @return Whether the config has been loaded without errors
     */
    public static boolean loadConfigSafe() {
        return IASConfig.loadSafe(configDirectory);
    }

    /**
     * Delegates to {@link IASConfig#load(Path)} with {@link #configDirectory}.
     *
     * @throws RuntimeException If unable to load the config
     */
    public static void loadConfig() {
        IASConfig.load(configDirectory);
    }

    /**
     * Delegates to {@link IASConfig#saveSafe(Path)} with {@link #configDirectory}.
     *
     * @return Whether the config has been saved without errors
     */
    public static boolean saveConfigSafe() {
        return IASConfig.saveSafe(configDirectory);
    }

    /**
     * Delegates to {@link IASConfig#save(Path)} with {@link #configDirectory}.
     *
     * @throws RuntimeException If unable to save the config
     */
    public static void saveConfig() {
        IASConfig.save(configDirectory);
    }

    /**
     * Delegates to {@link IASStorage#loadSafe(Path)} with {@link #gameDirectory}.
     *
     * @return Whether the storage has been loaded without errors
     */
    public static boolean loadStorageSafe() {
        return IASStorage.loadSafe(gameDirectory);
    }

    /**
     * Delegates to {@link IASStorage#load(Path)} with {@link #gameDirectory}.
     *
     * @throws RuntimeException If unable to load the storage
     */
    public static void loadStorage() {
        IASStorage.load(gameDirectory);
    }

    /**
     * Delegates to {@link IASStorage#saveSafe(Path)} with {@link #gameDirectory}.
     *
     * @return Whether the storage has been saved without errors
     */
    public static boolean saveStorageSafe() {
        return IASStorage.saveSafe(gameDirectory);
    }

    /**
     * Delegates to {@link IASStorage#save(Path)} with {@link #gameDirectory}.
     *
     * @throws RuntimeException If unable to save the storage
     */
    public static void saveStorage() {
        IASStorage.save(gameDirectory);
    }

    /**
     * Delegates to {@link IASStorage#disclaimers(Path)} with {@link #gameDirectory}.
     *
     * @throws RuntimeException If unable to write the disclaimers
     */
    public static void disclaimersStorage() {
        IASStorage.disclaimers(gameDirectory);
    }
}
