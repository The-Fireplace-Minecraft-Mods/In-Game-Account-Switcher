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

package ru.vidtu.ias.crypt;

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
    public static final DummyCrypt INSTANCE = new DummyCrypt();

    /**
     * Creates a new dummy crypt.
     *
     * @see #INSTANCE
     */
    private DummyCrypt() {
        // Private
    }

    @Override
    public boolean insecure() {
        return true;
    }

    @Override
    public byte[] encrypt(byte[] decrypted) {
        return decrypted;
    }

    @Override
    public byte[] decrypt(byte[] encrypted) {
        return encrypted;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DummyCrypt;
    }

    @Override
    public int hashCode() {
        return 158798543;
    }

    @Override
    public String toString() {
        return "DummyCrypt{}";
    }
}
