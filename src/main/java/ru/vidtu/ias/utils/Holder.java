package ru.vidtu.ias.utils;

import java.util.Objects;

/**
 * Mutable data holder.
 *
 * @param <T> Hold data type
 * @author VidTu
 * @apiNote Exists because local vars can't be modified in lambdas
 */
public final class Holder<T> {
    /**
     * Hold value.
     */
    private T value;

    /**
     * Creates a new holder that holds {@code null}.
     */
    public Holder() {
        this.value = null;
    }

    /**
     * Creates a new holder.
     *
     * @param value Hold value
     * @see #get()
     * @see #set(Object)
     */
    public Holder(T value) {
        this.value = value;
    }

    /**
     * Gets the hold value.
     *
     * @return Hold value
     * @see #get()
     */
    public T get() {
        return this.value;
    }

    /**
     * Sets the hold value.
     *
     * @param value Hold value
     * @see #set(Object)
     */
    public void set(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Holder<?> that)) return false;
        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }

    @Override
    public String toString() {
        return "Holder{" +
                "value=" + this.value +
                '}';
    }
}
