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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.auth.microsoft.MSAuth;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.config.IASStorage;
import ru.vidtu.ias.utils.Holder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Main IAS class.
 *
 * @author VidTu
 */
public final class IAS {
    /**
     * IAS static Microsoft application ID.
     */
    @NotNull
    public static final String CLIENT_ID = "54fd49e4-2103-4044-9603-2b028c814ec3";

    /**
     * Request timeout.
     */
    @NotNull
    public static final Duration TIMEOUT = Duration.ofSeconds(Long.getLong("ias.timeout", 15L));

    /**
     * Logger for this class.
     */
    @NotNull
    private static final Logger LOGGER = LoggerFactory.getLogger("IAS");

    /**
     * Random session. Used in {@link #USER_AGENT_TEMPLATE}.
     */
    @NotNull
    private static final UUID SESSION = UUID.randomUUID();

    /**
     * Template for {@link #userAgent}.
     */
    @NotNull
    private static final String USER_AGENT_TEMPLATE = "IAS/%s (Session: %s; Loader: %s %s; Minecraft %s; Java %s)";

    /**
     * Forge (and NeoForge) call {@code GameShuttingDownEvent} from {@code Minecraft.stop()},
     * which in vanilla and Fabric sets {@code Minecraft.running} to {@code false}).
     * <p>
     * Then we {@code Minecraft.forceSetScreen()} to our screen (because we can shut down up to 10 seconds)
     * and this calls {@code Minecraft.stop()} if window has been closed (not via "Quit Game") button,
     * leading to infinite recursion loop, which won't resolve with exceptions.
     * <p>
     * Fabric (and Quilt) doesn't have this issue, because it invokes its
     * {@code ClientLifecycleEvents#CLIENT_STOPPING} in actual {@code stop()} method,
     * which aren't called twice. Another reason to choose Fabric.
     * <p>
     * This field is global because it's almost in every version.
     * It is set before {@code forceSetScreen()} to prevent recursion and stackoverflow.
     */
    public static boolean shuttingDownHack = false; // Originally named "hackyWorkaroundForForgeForceSetScreenStackOverflow".

    /**
     * IAS executor.
     */
    @Nullable
    private static ScheduledExecutorService executor;

    /**
     * Current IAS user agent.
     */
    @Nullable
    private static String userAgent;

    /**
     * Current IAS game directory.
     */
    @Nullable
    private static Path gameDirectory;

    /**
     * Current IAS config directory.
     */
    @Nullable
    private static Path configDirectory;

    /**
     * Whether the mod is disabled remotely.
     */
    @SuppressWarnings("NegativelyNamedBooleanVariable") // <- The negative naming is intended.
    private static boolean disabled = false;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    @Contract(value = "-> fail", pure = true)
    private IAS() {
        throw new AssertionError("No instances.");
    }

    /**
     * Initializes the IAS.
     *
     * @param gamePath      Game directory
     * @param configPath    Config directory
     * @param version       Mod version
     * @param loader        Mod loader
     * @param loaderVersion Mod loader version
     * @param gameVersion   Game version
     */
    public static void init(@NotNull Path gamePath, @NotNull Path configPath, @NotNull String version,
                            @NotNull String loader, @NotNull String loaderVersion, @NotNull String gameVersion) {
        // Log.
        LOGGER.info("IAS: Initializing IAS...");

        // Initialize the dirs.
        gameDirectory = gamePath;
        configDirectory = configPath;

        // Set up IAS.
        userAgent = USER_AGENT_TEMPLATE.formatted(version, SESSION, loader, loaderVersion, gameVersion, Runtime.version().toString());
        LOGGER.debug("IAS: Current user agent: {}", userAgent);

        // Write the disclaimers.
        try {
            disclaimersStorage();
        } catch (Throwable t) {
            LOGGER.error("IAS: Unable to write disclaimers.", t);
        }

        // Read the config.
        try {
            loadConfig();
        } catch (Throwable t) {
            LOGGER.error("IAS: Unable to load IAS config.", t);
        }

        // Read the storage.
        try {
            loadStorage();
        } catch (Throwable t) {
            LOGGER.error("IAS: Unable to load IAS storage.", t);
        }

        // Create the executor.
        executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "IAS"));

        // Perform initial loading.
        if (Boolean.getBoolean("ias.skipDisableScanning")) {
            LOGGER.debug("IAS: Skipped IAS remote scanning because system property is set.");
            return;
        }
        Holder<ScheduledFuture<?>> task = new Holder<>();
        task.set(executor.scheduleWithFixedDelay(() -> {
            // Perform scanning, if allowed.
            try {
                // Skip if not allowed or already disabled.
                if (disabled || Boolean.getBoolean("ias.skipDisableScanning")) {
                    LOGGER.debug("IAS: Skipped IAS remote scanning because system property is set or the mod is already disabled.");
                    return;
                }

                // Create the client.
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(TIMEOUT)
                        .version(HttpClient.Version.HTTP_2)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .executor(Runnable::run)
                        .build();

                // Send the request.
                HttpResponse<Stream<String>> response = client.send(HttpRequest.newBuilder()
                        .uri(new URI("https://raw.githubusercontent.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/main/.ias/disabled_v1"))
                        .header("User-Agent", userAgent())
                        .timeout(TIMEOUT)
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofLines());

                // Validate the code.
                int code = response.statusCode();
                if (code < 200 || code > 299) return;

                // Check the lines.
                disabled = disabled || response.body().anyMatch(line -> {
                    line = line.strip();
                    return "ALL".equalsIgnoreCase(line) || version.equalsIgnoreCase(line);
                });

                // Return if normal.
                if (!disabled) {
                    LOGGER.debug("IAS: Completed remote disabling check. Not disabled.");
                    return;
                }

                // Log and stop task if disabled.
                LOGGER.error("IAS: The In-Game Account Switcher mod has been disabled by remote due to serious issues. Please, see the mod page for more information. ({})", version);
                ScheduledFuture<?> actualTask = task.get();
                if (actualTask == null) return;
                actualTask.cancel(false);
            } catch (Throwable t) {
                // Log into debug.
                LOGGER.debug("IAS: Unable to perform remote disabling check.", t);
            }
        }, 0L, 60L, TimeUnit.MINUTES));

        // Log.
        LOGGER.info("IAS: IAS has been loaded.");
    }

    /**
     * Closes the IAS.
     */
    public static void close() {
        // Log.
        LOGGER.info("IAS: Closing IAS...");

        // Shutdown the executor.
        shutdown:
        try {
            // Skip if doesn't exist.
            ScheduledExecutorService executor = IAS.executor;
            if (executor == null) break shutdown;

            // Shutdown.
            LOGGER.info("IAS: Shutting down IAS executor...");
            executor.shutdown();
            if (executor.awaitTermination(30L, TimeUnit.SECONDS)) {
                LOGGER.info("IAS: IAS executor shut down.");
                break shutdown;
            }

            // Shutdown forcefully.
            LOGGER.warn("IAS: Unable to shutdown IAS executor. Shutting down forcefully...");
            executor.shutdownNow();
            if (executor.awaitTermination(30L, TimeUnit.SECONDS)) {
                LOGGER.info("IAS: IAS executor shut down forcefully.");
                break shutdown;
            }

            // Unable to shut down.
            LOGGER.error("IAS: Unable to shutdown IAS executor forcefully.");
        } catch (InterruptedException e) {
            // Log.
            LOGGER.error("IAS: IAS executor interrupted while shutting down. Shutting down forcefully...", e);

            // Kill, if exists.
            ScheduledExecutorService executor = IAS.executor;
            if (executor != null) {
                executor.shutdownNow();
            }

            // Preserve interruption.
            Thread.currentThread().interrupt();
        }
        executor = null;

        // Destroy the UA.
        userAgent = null;

        // Write the disclaimers, if we can.
        if (gameDirectory != null) {
            try {
                disclaimersStorage();
            } catch (Throwable ignored) {
                // NO-OP
            }
        }

        // Log.
        LOGGER.info("IAS: IAS has been unloaded.");
    }

    /**
     * Gets the async executor for IAS.
     *
     * @return IAS executor
     * @throws NullPointerException If the executor is not available
     */
    @Contract(pure = true)
    @NotNull
    public static ScheduledExecutorService executor() {
        ScheduledExecutorService executor = IAS.executor;
        Objects.requireNonNull(executor, "IAS executor is not available.");
        return executor;
    }

    /**
     * Gets the user agent for usage in {@link MSAuth}.
     *
     * @return Current {@code User-Agent} value for HTTP requests
     * @throws NullPointerException If user agent wasn't set
     */
    @Contract(pure = true)
    @NotNull
    public static String userAgent() {
        String userAgent = IAS.userAgent;
        Objects.requireNonNull(userAgent, "IAS user agent is not set.");
        return userAgent;
    }

    /**
     * Gets the disabled state.
     *
     * @return Whether the mod is disabled remotely
     */
    @Contract(pure = true)
    public static boolean disabled() {
        return disabled;
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
     * Delegates to {@link IASConfig#save(Path)} with {@link #configDirectory}.
     *
     * @throws RuntimeException If unable to save the config
     */
    public static void saveConfig() {
        IASConfig.save(configDirectory);
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

    /**
     * Delegates to {@link IASStorage#gameDisclaimerShown(Path)} with {@link #gameDirectory}.
     *
     * @throws RuntimeException If unable to set or write game disclaimer shown persistent state
     */
    public static void gameDisclaimerShownStorage() {
        IASStorage.gameDisclaimerShown(gameDirectory);
    }
}
