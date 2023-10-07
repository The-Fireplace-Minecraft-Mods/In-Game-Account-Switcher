package ru.vidtu.ias;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.auth.ms.MSAuth;
import ru.vidtu.ias.config.IASConfig;

import java.util.Objects;
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
    private static final String CLIENT_ID = "54fd49e4-2103-4044-9603-2b028c814ec3";
    private static final String REDIRECT_URI = "http://localhost:59125";
    private static final Logger LOG = LoggerFactory.getLogger("IAS");
    private static ExecutorService executor;
    private static String userAgent;
    private static IASConfig config;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private IAS() {
        throw new AssertionError("No instances.");
    }

    /**
     * Initializes the IAS shared class.
     */
    public static void init() {
        executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "IAS Executor Thread"));
    }

    /**
     * Closes the IAS shared class, shutting down the executor.
     */
    public static void close() {
        executor.shutdown();
        shutdown:
        try {
            if (executor.awaitTermination(30L, TimeUnit.SECONDS)) break shutdown;
            LOG.warn("Unable to shut down IAS executor. Shutting down forcefully...");
            executor.shutdownNow();
            if (executor.awaitTermination(30L, TimeUnit.SECONDS)) break shutdown;
            LOG.error("Unable to shut down IAS executor forcefully.");
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        executor = null;
        userAgent = null;
    }

    /**
     * Gets the SLF4J logger for IAS.
     *
     * @return IAS logger
     */
    @Contract(pure = true)
    @NotNull
    public static Logger log() {
        return LOG;
    }

    /**
     * Gets the async executor for IAS.
     *
     * @return IAS executor
     * @throws IllegalStateException If the executor has been shut down
     */
    @Contract(pure = true)
    @NotNull
    public static Executor executor() {
        if (executor.isShutdown()) {
            throw new IllegalStateException("IAS executor has been shut down.");
        }
        return executor;
    }

    /**
     * Gets the current client ID for {@link MSAuth}.
     *
     * @return Current Microsoft client ID
     */
    @Contract(pure = true)
    @NotNull
    public static String clientId() {
        return CLIENT_ID;
    }

    /**
     * Gets the current redirect URI for {@link MSAuth}.
     *
     * @return Current Microsoft redirect URI
     */
    @Contract(pure = true)
    public static String redirectUri() {
        return REDIRECT_URI;
    }

    /**
     * Gets the user agent for usage in {@link MSAuth}.
     *
     * @return Current {@code User-Agent} value for HTTP requests
     * @throws NullPointerException If user agent wasn't set via {@link #userAgent(String)}
     */
    @Contract(pure = true)
    @NotNull
    public static String userAgent() {
        Objects.requireNonNull(userAgent, "IAS user agent is not set.");
        return userAgent;
    }

    /**
     * Sets the user agent for usage in {@link MSAuth}.
     *
     * @param userAgent New {@code User-Agent} value for HTTP requests
     */
    public static void userAgent(@NotNull String userAgent) {
        IAS.userAgent = userAgent;
    }
}
