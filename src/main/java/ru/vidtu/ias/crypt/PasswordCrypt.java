/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2023 VidTu
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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

/**
 * Encryption with password.
 *
 * @author VidTu
 */
public final class PasswordCrypt implements Crypt {
    /**
     * Encryption password.
     */
    private final String password;

    /**
     * Creates a new password encryption encryptor.
     *
     * @param password Encryption password
     */
    public PasswordCrypt(String password) {
        this.password = password;
    }

    @Override
    public byte[] encrypt(byte[] decrypted) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Generate and write salt.
            SecureRandom random = SecureRandom.getInstanceStrong();
            byte[] salt = new byte[256];
            random.nextBytes(salt);
            out.write(salt);

            // Encrypt and write the data.
            byte[] data = passEncrypt(decrypted, this.password, salt);
            out.write(data);

            // Return data.
            return out.toByteArray();
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to encrypt using PasswordCrypt.", t);
        }
    }

    @Override
    public byte[] decrypt(byte[] encrypted) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(encrypted)) {
            // Read the salt.
            byte[] salt = new byte[256];
            int read = in.read(salt);
            if (read != 256) {
                throw new EOFException("Not enough salt bytes: " + read);
            }

            // Read the data.
            byte[] data = in.readAllBytes();

            // Decrypt and return the data.
            return passDecrypt(data, this.password, salt);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to decrypt using PasswordCrypt.", t);
        }
    }

    @Override
    public String toString() {
        return "PasswordCrypt{" +
                "password='[PASSWORD]'" +
                '}';
    }

    /**
     * Encrypts the data using password and salt.
     *
     * @param decrypted Decrypted data
     * @param password  Target password
     * @param salt      Target salt
     * @return Encrypted data
     * @throws RuntimeException If unable to encrypt the data
     */
    public static byte[] passEncrypt(byte[] decrypted, String password, byte[] salt) {
        try {
            // Create the key.
            SecretKey key = passKey(password, salt);

            // Create the AES.
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

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
     * @return Decrypted data
     * @throws RuntimeException If unable to decrypt the data
     */
    public static byte[] passDecrypt(byte[] encrypted, String password, byte[] salt) {
        try {
            // Create the key.
            SecretKey key = passKey(password, salt);

            // Create the AES.
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            // Decrypt and return.
            return cipher.doFinal(encrypted);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to decrypt data using AES via PBKDF2-hashed password.", t);
        }
    }

    /**
     * Creates a new AES key from password and salt using PBKDF2 algorithm.
     *
     * @param password Target password
     * @param salt     Target salt
     * @return Created AES key
     * @throws RuntimeException If unable to create the key
     */
    private static SecretKey passKey(String password, byte[] salt) {
        try {
            // Create a new PBKDF2WithHmacSHA512 factory.
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

            // Create a PBKDF2 key with 256 bits key (for AES) and 1_300_000 iterations.
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1_300_000, 256);

            // Create a new secret.
            byte[] secret = factory.generateSecret(spec).getEncoded();

            // Create a new key.
            return new SecretKeySpec(secret, "AES");
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to create a secret AES key from password using PBKDF2.", t);
        }
    }
}
