package ru.vidtu.ias.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;

/**
 * Cryptographic utils.
 *
 * @author VidTu
 * @implNote Apart from relying on GC (deal with it, password input fields will use {@link String} anyway),
 * this should be pretty secure according to modern (2023) security standards.
 */
public final class CryptUtils {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private CryptUtils() {
        throw new AssertionError("No instances.");
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
    public static byte[] encrypt(byte[] decrypted, String password, byte[] salt) {
        try {
            // Create the key.
            SecretKey key = key(password, salt);

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
    public static byte[] decrypt(byte[] encrypted, String password, byte[] salt) {
        try {
            // Create the key.
            SecretKey key = key(password, salt);

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
    private static SecretKey key(String password, byte[] salt) {
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
