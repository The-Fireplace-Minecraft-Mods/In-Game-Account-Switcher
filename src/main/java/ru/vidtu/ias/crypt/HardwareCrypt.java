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
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * Crypt with "hardware ID".
 *
 * @author VidTu
 */
public final class HardwareCrypt implements Crypt {
    /**
     * Shared "hardware ID" crypt.
     *
     * @apiNote Use {@link #equals(Object)} for comparison
     */
    public static final HardwareCrypt INSTANCE = new HardwareCrypt();

    /**
     * Creates a new "hardware ID" crypt.
     *
     * @see #INSTANCE
     */
    public HardwareCrypt() {
        // Empty
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
            byte[] data = hardwareEncrypt(decrypted, salt);
            out.write(data);

            // Return data.
            return out.toByteArray();
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to encrypt using HardwareCrypt.", t);
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
            return hardwareDecrypt(data, salt);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to decrypt using HardwareCrypt.", t);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HardwareCrypt;
    }

    @Override
    public int hashCode() {
        return 31074107;
    }

    @Override
    public String toString() {
        return "HardwareCrypt{}";
    }

    /**
     * Encrypts the data using "HWID" and salt.
     *
     * @param decrypted Decrypted data
     * @param salt      Target salt
     * @return Encrypted data
     * @throws RuntimeException If unable to encrypt the data
     */
    private static byte[] hardwareEncrypt(byte[] decrypted, byte[] salt) {
        try {
            // Create the key.
            SecretKey key = hardwareKey(salt);

            // Create the AES.
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            // Encrypt and return.
            return cipher.doFinal(decrypted);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to encrypt data using AES via PBKDF2-hashed hardware key.", t);
        }
    }

    /**
     * Decrypts the data using "HWID" and salt.
     *
     * @param encrypted Encrypted data
     * @param salt      Target salt
     * @return Decrypted data
     * @throws RuntimeException If unable to decrypt the data
     */
    private static byte[] hardwareDecrypt(byte[] encrypted, byte[] salt) {
        try {
            // Create the key.
            SecretKey key = hardwareKey(salt);

            // Create the AES.
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            // Decrypt and return.
            return cipher.doFinal(encrypted);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to decrypt data using AES via PBKDF2-hashed hardware key.", t);
        }
    }

    /**
     * Creates a new AES key from various hardware things and salt using PBKDF2 algorithm.
     *
     * @param salt Target salt
     * @return Created AES key
     * @throws RuntimeException If unable to create the key
     */
    private static SecretKey hardwareKey(byte[] salt) {
        try {
            // Calculate the "hardware ID".
            String hwid;
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 DataOutputStream out = new DataOutputStream(byteOut)) {
                // Basic system info.
                OperatingSystemMXBean system = ManagementFactory.getOperatingSystemMXBean();
                out.write(system.getName().getBytes(StandardCharsets.UTF_8));
                out.write(system.getVersion().getBytes(StandardCharsets.UTF_8));
                out.write(system.getArch().getBytes(StandardCharsets.UTF_8));
                out.writeInt(system.getAvailableProcessors());
                out.writeInt(File.separatorChar);
                out.writeInt(File.pathSeparatorChar);
                out.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));

                // System properties.
                List<String> list = List.of("java.io.tmpdir", "native.encoding", "user.name", "user.home", "user.country",
                        "sun.io.unicode.encoding", "stderr.encoding", "sun.cpu.endian", "sun.cpu.isalist", "sun.jnu.encoding",
                        "stdout.encoding", "sun.arch.data.model", "user.language", "user.variant");
                for (String key : list) {
                    String value = System.getProperty(key, "\0\0\0\0\0\0\0\0\0");
                    out.write(value.getBytes(StandardCharsets.UTF_8));
                }

                // Environmental info.
                // Can be undefined in Mac/Linux distributions, too lazy to test, but if it's null
                // it will stay null anyway, so should be persistent.
                list = List.of("COMPUTERNAME", "PROCESSOR_ARCHITECTURE", "PROCESSOR_REVISION", "PROCESSOR_IDENTIFIER",
                        "PROCESSOR_LEVEL", "NUMBER_OF_PROCESSORS", "OS", "USERNAME", "USERDOMAIN", "USERDOMAIN_ROAMINGPROFILE",
                        "APPDATA", "HOMEPATH", "LOGONSERVER", "LOCALAPPDATA", "TEMP", "TMP");
                for (String key : list) {
                    String value = Objects.requireNonNullElse(System.getenv(key), "\0\0\0\0\0\0\0\0\0");
                    out.write(value.getBytes(StandardCharsets.UTF_8));
                }

                // Network interfaces.
                List<NetworkInterface> nets;
                try {
                    nets = NetworkInterface.networkInterfaces().toList();
                } catch (SocketException ignored) {
                    nets = List.of();
                }
                for (NetworkInterface net : nets) {
                    out.write(net.getName().getBytes(StandardCharsets.UTF_8));
                    out.write(net.getDisplayName().getBytes(StandardCharsets.UTF_8));
                    byte[] mac;
                    try {
                        mac = Objects.requireNonNullElse(net.getHardwareAddress(), new byte[0]);
                    } catch (SocketException ignored) {
                        mac = new byte[0];
                    }
                    out.write(mac);
                    try {
                        out.writeInt(net.getMTU());
                    } catch (SocketException ignored) {
                        // NO-OP
                    }
                }

                // Bake the "HWID".
                hwid = Base64.getEncoder().encodeToString(byteOut.toByteArray());
            }

            // Create a new PBKDF2WithHmacSHA512 factory.
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

            // Create a PBKDF2 key with 256 bits key (for AES) and 1_300_000 iterations.
            KeySpec spec = new PBEKeySpec(hwid.toCharArray(), salt, 1_300_000, 256);

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
