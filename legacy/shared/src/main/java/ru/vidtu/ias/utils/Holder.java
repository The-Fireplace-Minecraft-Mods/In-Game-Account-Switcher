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

package ru.vidtu.ias.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @Contract(pure = true)
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
    @Contract(pure = true)
    public Holder(T value) {
        this.value = value;
    }

    /**
     * Gets the hold value.
     *
     * @return Hold value
     * @see #set(Object)
     */
    @Contract(pure = true)
    public T get() {
        return this.value;
    }

    /**
     * Sets the hold value.
     *
     * @param value Hold value
     * @see #get()
     */
    public void set(T value) {
        this.value = value;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Holder<?> that)) return false;
        return Objects.equals(this.value, that.value);
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String toString() {
        return "Holder{" +
                "value=" + this.value +
                '}';
    }
}
