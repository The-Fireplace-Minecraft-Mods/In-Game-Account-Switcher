package ru.vidtu.ias;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.auth.MSAuth;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.config.IASStorage;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Shared IAS class.
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
    private static ExecutorService executor = null;

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
        executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "IAS Executor Thread"));
    }

    /**
     * Closes the IAS.
     */
    public static void close() {
        // Shutdown the executor.
        shutdown:
        try {
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
    }

    /**
     * Gets the async executor for IAS.
     *
     * @return IAS executor
     * @throws NullPointerException If the executor is not available
     */
    public static Executor executor() {
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
