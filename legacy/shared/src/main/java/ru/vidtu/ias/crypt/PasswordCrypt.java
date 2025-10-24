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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.security.SecureRandom;

/**
 * Encryption with password.
 *
 * @author VidTu
 */
public final class PasswordCrypt implements Crypt {
    /**
     * Encryption password.
     */
    @NotNull
    private final String password;

    /**
     * Creates a new password encryption encryptor.
     *
     * @param password Encryption password
     * @throws IllegalArgumentException If {@code password} is blank
     */
    @Contract(pure = true)
    public PasswordCrypt(@NotNull String password) {
        if (password.isBlank()) {
            throw new IllegalArgumentException("Password is blank.");
        }
        this.password = password;
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String type() {
        return "ias:password_crypt_v1";
    }

    @Contract(value = "-> null", pure = true)
    @Override
    @Nullable
    public Crypt migrate() {
        return null;
    }

    @Contract(value = "-> false", pure = true)
    @Override
    public boolean insecure() {
        return false;
    }

    @Contract(pure = true)
    @Override
    public byte @NotNull [] encrypt(byte @NotNull [] decrypted) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Generate and write salt.
            SecureRandom random = SecureRandom.getInstanceStrong();
            byte[] salt = new byte[128];
            random.nextBytes(salt);
            out.write(salt);

            // Generate and write IV.
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            out.write(iv);

            // Encrypt and write the data.
            byte[] data = Crypt.pbkdfAesEncrypt(decrypted, this.password, salt, iv);
            out.write(data);

            // Return data.
            return out.toByteArray();
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to encrypt using PasswordCrypt.", t);
        }
    }

    @Contract(pure = true)
    @Override
    public byte @NotNull [] decrypt(byte @NotNull [] encrypted) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(encrypted)) {
            // Read the salt.
            byte[] salt = new byte[128];
            int read = in.read(salt);
            if (read != 128) {
                throw new EOFException("Not enough salt bytes: " + read);
            }

            // Read the IV.
            byte[] iv = new byte[16];
            read = in.read(iv);
            if (read != 16) {
                throw new EOFException("Not enough IV bytes: " + read);
            }

            // Read the data.
            byte[] data = in.readAllBytes();

            // Decrypt and return the data.
            return Crypt.pbkdfAesDecrypt(data, this.password, salt, iv);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to decrypt using PasswordCrypt.", t);
        }
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String toString() {
        return "PasswordCrypt{" +
                "password='[PASSWORD]'" +
                '}';
    }
}
