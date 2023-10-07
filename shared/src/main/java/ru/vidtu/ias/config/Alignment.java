package ru.vidtu.ias.config;

import com.google.common.base.Enums;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Text alignment.
 *
 * @author VidTu
 */
public enum Alignment {
    LEFT("ias.titleTextAlign.left"),
    CENTER("ias.titleTextAlign.center"),
    RIGHT("ias.titleTextAlign.right");

    private final String key;

    /**
     * Creates a new alignment.
     *
     * @param key Alignment translation key
     */
    Alignment(@NotNull String key) {
        this.key = key;
    }

    /**
     * Gets the translation key.
     *
     * @return Translation key
     */
    @Contract(pure = true)
    @Override
    @NotNull
    public String toString() {
        return key;
    }

    /**
     * Get text alignment by name.
     *
     * @param name     Alignment name
     * @param fallback Fallback value
     * @return Alignment found by name, <code>fallback</code> if not found
     * @deprecated Use {@link Enums#getIfPresent(Class, String)}
     */
    @Contract(pure = true)
    @Deprecated(forRemoval = true)
    public static @NotNull Alignment getOr(@NotNull String name, @NotNull Alignment fallback) {
        for (Alignment v : values()) {
            if (v.name().equalsIgnoreCase(name)) return v;
        }
        return fallback;
    }
}
