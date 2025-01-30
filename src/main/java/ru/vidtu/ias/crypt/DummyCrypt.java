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

package ru.vidtu.ias.crypt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Dummy crypt.
 *
 * @author VidTu
 */
public final class DummyCrypt implements Crypt {
    /**
     * Shared dummy crypt.
     *
     * @apiNote Use {@link #equals(Object)} for comparison
     */
    @NotNull
    public static final DummyCrypt INSTANCE = new DummyCrypt();

    /**
     * Creates a new dummy crypt.
     *
     * @see #INSTANCE
     */
    @Contract(pure = true)
    private DummyCrypt() {
        // Private
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String type() {
        return "ias:dummy_crypt_v1";
    }

    @Contract(value = "-> null", pure = true)
    @Override
    @Nullable
    public Crypt migrate() {
        return null;
    }

    @Contract(value = "-> true", pure = true)
    @Override
    public boolean insecure() {
        return true;
    }

    @Contract(value = "_ -> param1", pure = true)
    @Override
    public byte @NotNull [] encrypt(byte @NotNull [] decrypted) {
        return decrypted;
    }

    @Contract(value = "_ -> param1", pure = true)
    @Override
    public byte @NotNull [] decrypt(byte @NotNull [] encrypted) {
        return encrypted;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof DummyCrypt;
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        return 158798543;
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String toString() {
        return "DummyCrypt{}";
    }
}
