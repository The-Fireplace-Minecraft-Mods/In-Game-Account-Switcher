package ru.vidtu.ias.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.vidtu.ias.IAS;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;

/**
 * IAS config.
 *
 * @author VidTu
 */
public class IASConfig {
    /**
     * Config GSON.
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
    public static String titleTextX = null;

    /**
     * Custom title screen text Y position, {@code null} by default.
     */
    public static String titleTextY = null;

    /**
     * Alignment for title screen text, {@link TextAlign#CENTER} by default.
     */
    public static TextAlign titleTextAlign = TextAlign.CENTER;

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
    public static boolean serversText = false;

    /**
     * Custom servers screen text X position, {@code null} by default.
     */
    public static String serversTextX = null;

    /**
     * Custom servers screen text Y position, {@code null} by default.
     */
    public static String serversTextY = null;

    /**
     * Alignment for servers screen text, {@link TextAlign#CENTER} by default.
     */
    public static TextAlign serversTextAlign = TextAlign.CENTER;

    /**
     * Whether the servers screen button is enabled, {@code false} by default.
     */
    public static boolean serversButton = false;

    /**
     * Custom servers screen button X position, {@code null} by default.
     */
    public static String serversButtonX = null;

    /**
     * Custom servers screen button Y position, {@code null} by default.
     */
    public static String serversButtonY = null;

    /**
     * Creates a new config for GSON.
     */
    private IASConfig() {
        // Private
    }

    /**
     * Loads the config, suppressing and logging any errors.
     *
     * @param path Config directory (not file)
     * @return Whether the config has been loaded without errors
     */
    public static boolean loadSafe(Path path) {
        try {
            // Try to load config.
            load(path);

            // Return success.
            return true;
        } catch (Throwable t) {
            // Log it.
            IAS.LOG.error("Unable to load IAS config.", t);

            // Return fail.
            return false;
        }
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

            // Skip if doesn't exist.
            if (!Files.isRegularFile(file)) return;

            // Read the file.
            String value = Files.readString(file);

            // Hacky JSON reading.
            GSON.fromJson(value, IASConfig.class);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to load IAS config.", t);
        }
    }

    /**
     * Saves the config, suppressing and logging any errors.
     *
     * @param path Config directory (not file)
     * @return Whether the config has been saved without errors
     */
    public static boolean saveSafe(Path path) {
        try {
            // Try to load config.
            save(path);

            // Return success.
            return true;
        } catch (Throwable t) {
            // Log it.
            IAS.LOG.error("Unable to save IAS config.", t);

            // Return fail.
            return false;
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
            Path file = path.resolve("ias.json");

            // Hacky JSON writing.
            @SuppressWarnings("InstantiationOfUtilityClass")
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
