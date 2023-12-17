package ru.vidtu.ias.config;

/**
 * Title text alignment.
 *
 * @author VidTu
 */
public enum TextAlign {
    /**
     * Text is left-aligned.
     */
    LEFT("ias.config.textAlign.left"),

    /**
     * Text is center-aligned.
     */
    CENTER("ias.config.textAlign.center"),

    /**
     * Text is right-aligned.
     */
    RIGHT("ias.config.textAlign.right");

    /**
     * Alignment translation key.
     */
    private final String key;

    /**
     * Creates a new alignment.
     *
     * @param key Alignment translation key
     */
    TextAlign(String key) {
        this.key = key;
    }

    /**
     * Gets the translation key.
     *
     * @return Translation key
     */
    @Override
    public String toString() {
        return this.key;
    }
}
