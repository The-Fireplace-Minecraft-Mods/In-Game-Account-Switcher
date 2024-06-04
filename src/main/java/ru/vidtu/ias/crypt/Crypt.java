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

import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.utils.exceptions.FriendlyException;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataInput;
import java.security.spec.KeySpec;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Crypt method.
 *
 * @author VidTu
 */
public sealed interface Crypt permits DummyCrypt, HardwareCrypt, PasswordCrypt {
    /**
     * Gets whether this crypt is insecure.
     *
     * @return Whether this crypt is insecure
     */
    @Contract(pure = true)
    boolean insecure();

    /**
     * Gets the crypt storage type.
     *
     * @return Crypt storage namespace
     */
    @Contract(pure = true)
    @NotNull
    String type();

    /**
     * Gets the account preferred migration.
     *
     * @return Account preferred migration, {@code null} if it shouldn't be migrated
     */
    @Contract(pure = true)
    @Nullable
    Crypt migrate();

    /**
     * Encrypts the value.
     *
     * @param decrypted Decrypted data
     * @return Encrypted value
     * @throws RuntimeException On encryption error
     */
    @Contract(pure = true)
    byte @NotNull [] encrypt(byte @NotNull [] decrypted);

    /**
     * Decrypts the value.
     *
     * @param encrypted Encrypted data
     * @return Decrypted value
     * @throws RuntimeException On decryption error
     */
    @Contract(pure = true)
    byte @NotNull [] decrypt(byte @NotNull [] encrypted);

    /**
     * Reads the typed crypt.
     *
     * @param input    Target input
     * @param password Password provider, if required
     * @return Future will contain crypt on success, will contain {@code null} if {@code password} returns {@code null} on request, will complete exceptionally on error or unknown crypt type
     */
    @CheckReturnValue
    @NotNull
    static CompletableFuture<Crypt> readType(@NotNull DataInput input, @NotNull Supplier<CompletableFuture<String>> password) {
        try {
            // Read type.
            String type = input.readUTF();

            // Create
            return switch (type) {
                case "ias:dummy_crypt_v1" -> CompletableFuture.completedFuture(DummyCrypt.INSTANCE);
                case "ias:hardware_crypt_v1" -> CompletableFuture.completedFuture(HardwareCrypt.INSTANCE_V1);
                case "ias:hardware_crypt_v2" -> CompletableFuture.completedFuture(HardwareCrypt.INSTANCE_V2);
                case "ias:password_crypt_v1" -> password.get().thenApplyAsync(pass -> pass == null ? null : new PasswordCrypt(pass), IAS.executor());
                default -> CompletableFuture.failedFuture(new IllegalArgumentException("Unknown crypt type: " + type));
            };
        } catch (Throwable t) {
            return CompletableFuture.failedFuture(new RuntimeException("Unable to read typed crypt.", t));
        }
    }

    /**
     * Encrypts the data using password and salt.
     *
     * @param decrypted Decrypted data
     * @param password  Target password
     * @param salt      Target salt
     * @param iv        IV for AES
     * @return Encrypted data
     * @throws RuntimeException If unable to encrypt the data
     */
    @Contract(pure = true)
    static byte @NotNull [] pbkdfAesEncrypt(byte @NotNull [] decrypted, @NotNull String password,
                                            byte @NotNull [] salt, byte @NotNull [] iv) {
        try {
            // Create the key.
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 500_000, 256);
            byte[] secret = factory.generateSecret(spec).getEncoded();
            SecretKey key = new SecretKeySpec(secret, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));

            // Encrypt and return.
            return cipher.doFinal(decrypted);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to encrypt data using AES via PBKDF2-hashed password.", t);
        }
    }

    /**
     * Decrypts the data using password and salt.
     *
     * @param encrypted Encrypted data
     * @param password  Target password
     * @param salt      Target salt
     * @param iv        IV for AES
     * @return Decrypted data
     * @throws RuntimeException If unable to decrypt the data
     */
    @Contract(pure = true)
    static byte @NotNull [] pbkdfAesDecrypt(byte @NotNull [] encrypted, @NotNull String password,
                                            byte @NotNull [] salt, byte @NotNull [] iv) {
        try {
            // Create the key.
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 500_000, 256);
            byte[] secret = factory.generateSecret(spec).getEncoded();
            SecretKey key = new SecretKeySpec(secret, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));

            // Decrypt and return.
            try {
                return cipher.doFinal(encrypted);
            } catch (AEADBadTagException e) {
                // Probable case - bad password.
                throw new FriendlyException("Unable to do AES final decrypt.", e, "ias.error.decrypt");
            }
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to decrypt data using AES via PBKDF2-hashed password.", t);
        }
    }
}
