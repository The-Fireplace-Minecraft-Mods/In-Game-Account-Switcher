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
     * Empty byte array for unknown MAC.
     */
    private static final byte[] EMPTY_MAC = {};

    /**
     * List of environmental keys used for hardware password.
     */
    private static final List<String> ENV = List.of("COMPUTERNAME", "PROCESSOR_ARCHITECTURE",
            "PROCESSOR_REVISION", "PROCESSOR_IDENTIFIER", "PROCESSOR_LEVEL", "NUMBER_OF_PROCESSORS", "OS", "USERNAME",
            "USERDOMAIN", "USERDOMAIN_ROAMINGPROFILE", "APPDATA", "HOMEPATH", "LOGONSERVER", "LOCALAPPDATA", "TEMP", "TMP");

    /**
     * List of system properties used for hardware password.
     */
    private static final List<String> PROPS = List.of("java.io.tmpdir", "native.encoding", "user.name",
            "user.home", "user.country", "sun.io.unicode.encoding", "stderr.encoding", "sun.cpu.endian",
            "sun.cpu.isalist", "sun.jnu.encoding", "stdout.encoding", "native.encoding", "sun.arch.data.model",
            "user.language", "user.variant");

    /**
     * Creates a new "hardware ID" crypt.
     *
     * @see #INSTANCE
     */
    private HardwareCrypt() {
        // Private
    }

    @Override
    public byte[] encrypt(byte[] decrypted) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Generate and write salt.
            SecureRandom random = SecureRandom.getInstanceStrong();
            byte[] salt = new byte[256];
            random.nextBytes(salt);
            out.write(salt);

            // Generate and write IV.
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            out.write(iv);

            // Generate the password.
            String pwd = hardwarePassword();

            // Encrypt and write the data.
            byte[] data = Crypt.pbkdfAesEncrypt(decrypted, pwd, salt, iv);
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

            // Read the IV.
            byte[] iv = new byte[16];
            read = in.read(iv);
            if (read != 16) {
                throw new EOFException("Not enough IV bytes: " + read);
            }

            // Generate the password.
            String pwd = hardwarePassword();

            // Read the data.
            byte[] data = in.readAllBytes();

            // Decrypt and return.
            return Crypt.pbkdfAesDecrypt(data, pwd, salt, iv);
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
     * Creates a from various hardware things and salt using PBKDF2 algorithm.
     *
     * @return Created AES key
     * @throws RuntimeException If unable to create the key
     */
    private static String hardwarePassword() {
        try {
            // Calculate the "hardware ID".
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
                for (String key : PROPS) {
                    String value = System.getProperty(key, "IAS_NO_DATA");
                    out.write(value.getBytes(StandardCharsets.UTF_8));
                }

                // Environmental info.
                // Can be undefined in Mac/Linux distributions, too lazy to test, but if it's null
                // it will stay null anyway, so should be persistent.
                for (String key : ENV) {
                    String value = Objects.requireNonNullElse(System.getenv(key), "IAS_NO_DATA");
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
                        mac = Objects.requireNonNullElse(net.getHardwareAddress(), EMPTY_MAC);
                    } catch (SocketException ignored) {
                        mac = EMPTY_MAC;
                    }
                    out.write(mac);
                    try {
                        out.writeInt(net.getMTU());
                    } catch (SocketException ignored) {
                        // NO-OP
                    }
                }

                // Bake and return the "HWID".
                return Base64.getEncoder().encodeToString(byteOut.toByteArray());
            }
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to create a hardware password.", t);
        }
    }
}
