/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2023 VidTu
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
     * IAS logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger("IAS");

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
        // Write the disclaimers.
        IASStorage.disclaimers(gamePath);

        // Read the config.
        IASConfig.loadSafe(configPath);

        // Read the storage.
        IASStorage.loadSafe(gamePath);

        // Create the executor.
        executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "IAS Executor Thread"));
    }

    /**
     * Closes the IAS.
     *
     * @param gamePath Game directory
     */
    public static void close(Path gamePath) {
        // Shutdown the executor.
        shutdown:
        try {
            if (executor == null) break shutdown;
            executor.shutdown();
            if (executor.awaitTermination(30L, TimeUnit.SECONDS)) break shutdown;
            LOG.warn("Unable to shutdown IAS executor. Shutting down forcefully...");
            executor.shutdownNow();
            if (executor.awaitTermination(30L, TimeUnit.SECONDS)) break shutdown;
            LOG.error("Unable to shutdown IAS executor forcefully.");
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        executor = null;

        // Destroy the UA.
        userAgent = null;

        // Write the disclaimers.
        IASStorage.disclaimers(gamePath);
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
        userAgent = IAS.USER_AGENT_TEMPLATE.formatted(version, SESSION, loader, loaderVersion, gameVersion, Runtime.version().toString());
        LOG.info("IAS user agent: {}", userAgent);
    }
}
