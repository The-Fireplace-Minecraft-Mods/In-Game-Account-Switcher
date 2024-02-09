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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
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
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("IAS/HardwareCrypt");

    /**
     * Empty byte array for unknown MAC.
     */
    private static final byte[] EMPTY_MAC = {};

    /**
     * List of environmental keys used for hardware password.
     */
    private static final List<String> ENV = List.of("COMPUTERNAME", "PROCESSOR_ARCHITECTURE",
            "PROCESSOR_REVISION", "PROCESSOR_IDENTIFIER", "PROCESSOR_LEVEL", "NUMBER_OF_PROCESSORS", "OS", "USERNAME",
            "USERDOMAIN", "USERDOMAIN_ROAMINGPROFILE", "APPDATA", "HOMEPATH", "LOGONSERVER", "LOCALAPPDATA", "TEMP", "TMP",
            "MINECRAFT_IN_GAME_ACCOUNT_SWITCHER_VERY_NERDY_SYSTEM_ENV");

    /**
     * List of system properties used for hardware password.
     */
    private static final List<String> PROPS = List.of("java.io.tmpdir", "native.encoding", "user.name",
            "user.home", "user.country", "sun.io.unicode.encoding", "stderr.encoding", "sun.cpu.endian",
            "sun.cpu.isalist", "sun.jnu.encoding", "stdout.encoding", "sun.arch.data.model",
            "user.language", "user.variant", "minecraft.inGameAccountSwitcher.veryNerdySystemProperty");

    /**
     * Creates a new "hardware ID" crypt.
     *
     * @see #INSTANCE
     */
    private HardwareCrypt() {
        // Private
    }

    @Override
    public boolean insecure() {
        // Hopefully.
        return false;
    }

    @Override
    public byte[] encrypt(byte[] decrypted) {
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
     * Creates a key from various hardware things.
     *
     * @return Created password
     * @throws RuntimeException If unable to create the password
     */
    private static String hardwarePassword() {
        try {
            // Calculate the "hardware ID".
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 DataOutputStream out = new DataOutputStream(byteOut)) {
                // Basic system info.
                OperatingSystemMXBean system = ManagementFactory.getOperatingSystemMXBean();
                // Not using OS name - Windows 10/11 can be updated.
                // Not using OS version - Linux kernels can be updated with most package managers.
                out.write(system.getArch().getBytes(StandardCharsets.UTF_8));
                out.writeInt(system.getAvailableProcessors());
                out.writeShort(File.separatorChar);
                out.writeShort(File.pathSeparatorChar);
                out.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));

                // System properties.
                for (String key : PROPS) {
                    String value = System.getProperty(key);
                    if (value == null) continue;
                    out.write(value.getBytes(StandardCharsets.UTF_8));
                }

                // Environmental info.
                // Can be undefined in Mac/Linux distributions, too lazy to test, but if it's null
                // it will stay null anyway, so should be persistent.
                for (String key : ENV) {
                    String value = System.getenv(key);
                    if (value == null) continue;
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
                    if (net.isVirtual() || net.isLoopback()) continue;
                    out.write(net.getName().getBytes(StandardCharsets.UTF_8));
                    String displayName = net.getDisplayName();
                    if (displayName != null) {
                        out.write(displayName.getBytes(StandardCharsets.UTF_8));
                    }
                    byte[] mac = EMPTY_MAC;
                    try {
                        mac = Objects.requireNonNullElse(net.getHardwareAddress(), EMPTY_MAC);
                    } catch (SocketException e) {
                        // Log into trace. (disabled for MOST users)
                        LOGGER.trace("Unable to get MAC: {}", net, e);
                    }
                    out.write(mac);
                    try {
                        out.writeInt(net.getMTU());
                    } catch (SocketException e) {
                        // Log into trace. (disabled for MOST users)
                        LOGGER.trace("Unable to get MTU: {}", net, e);
                    }
                }

                // OSHI data, if available.
                try {
                    // Generate the basic OSHI data.
                    Class<?> sysInfoClass = Class.forName("oshi.SystemInfo");
                    Class<?> osClass = Class.forName("oshi.software.os.OperatingSystem");
                    Class<?> hwLayerClass = Class.forName("oshi.hardware.HardwareAbstractionLayer");
                    Object sysInfo = sysInfoClass.getConstructor().newInstance();
                    Object os = sysInfoClass.getMethod("getOperatingSystem").invoke(sysInfo);
                    Object hwLayer = sysInfoClass.getMethod("getHardware").invoke(sysInfo);

                    // Generate other OSHI data.
                    Class<?> sysClass = Class.forName("oshi.hardware.ComputerSystem");
                    Object sys = hwLayerClass.getMethod("getComputerSystem").invoke(hwLayer);
                    Class<?> boardClass = Class.forName("oshi.hardware.Baseboard");
                    Object board = sysClass.getMethod("getBaseboard").invoke(sys);
                    Class<?> firmwareClass = Class.forName("oshi.hardware.Firmware");
                    Object firmware = sysClass.getMethod("getFirmware").invoke(sys);
                    Class<?> diskClass = Class.forName("oshi.hardware.HWDiskStore");
                    List<?> disks = (List<?>) hwLayerClass.getMethod("getDiskStores").invoke(hwLayer);
                    Method diskNameMethod = diskClass.getMethod("getName");
                    Method diskModelMethod = diskClass.getMethod("getModel");
                    Method diskSerialMethod = diskClass.getMethod("getSerial");
                    Method diskSizeMethod = diskClass.getMethod("getSize");
                    // Not using displays - some cheap ones report unplugging when turned off.
                    Class<?> cardClass = Class.forName("oshi.hardware.GraphicsCard");
                    List<?> cards = (List<?>) hwLayerClass.getMethod("getGraphicsCards").invoke(hwLayer);
                    Method cardNameMethod = cardClass.getMethod("getName");
                    Method cardIdMethod = cardClass.getMethod("getDeviceId");
                    Method cardVendorMethod = cardClass.getMethod("getVendor");
                    Method cardRamMethod = cardClass.getMethod("getVRam");

                    // Extract OS data.
                    int osBit = (int) osClass.getMethod("getBitness").invoke(os);
                    String osFamily = (String) osClass.getMethod("getFamily").invoke(os);
                    String osManufacturer = (String) osClass.getMethod("getManufacturer").invoke(os);
                    out.writeInt(osBit);
                    out.write(osFamily.getBytes(StandardCharsets.UTF_8));
                    out.write(osManufacturer.getBytes(StandardCharsets.UTF_8));

                    // Extract Sys data.
                    String hwUid = (String) sysClass.getMethod("getHardwareUUID").invoke(sys);
                    String hwManufacturer = (String) sysClass.getMethod("getManufacturer").invoke(sys);
                    String hwModel = (String) sysClass.getMethod("getModel").invoke(sys);
                    out.write(hwUid.getBytes(StandardCharsets.UTF_8));
                    out.write(hwManufacturer.getBytes(StandardCharsets.UTF_8));
                    out.write(hwModel.getBytes(StandardCharsets.UTF_8));

                    // Extract Board data.
                    String boardSerial = (String) boardClass.getMethod("getSerialNumber").invoke(board);
                    String boardManufacturer = (String) boardClass.getMethod("getManufacturer").invoke(board);
                    String boardModel = (String) boardClass.getMethod("getModel").invoke(board);
                    String boardVersion = (String) boardClass.getMethod("getVersion").invoke(board);
                    out.write(boardSerial.getBytes(StandardCharsets.UTF_8));
                    out.write(boardManufacturer.getBytes(StandardCharsets.UTF_8));
                    out.write(boardModel.getBytes(StandardCharsets.UTF_8));
                    out.write(boardVersion.getBytes(StandardCharsets.UTF_8));

                    // Extract Firmware data.
                    String firmwareName = (String) firmwareClass.getMethod("getName").invoke(firmware);
                    // Not using BIOS version and release date - can be updated.
                    String firmwareDescription = (String) firmwareClass.getMethod("getDescription").invoke(firmware);
                    String firmwareManufacturer = (String) firmwareClass.getMethod("getManufacturer").invoke(firmware);
                    out.write(firmwareName.getBytes(StandardCharsets.UTF_8));
                    out.write(firmwareDescription.getBytes(StandardCharsets.UTF_8));
                    out.write(firmwareManufacturer.getBytes(StandardCharsets.UTF_8));

                    // Extract DiscStore[] data.
                    // Not using partitions - can be changed.
                    for (Object disk : disks) {
                        String diskName = (String) diskNameMethod.invoke(disk);
                        String diskModel = (String) diskModelMethod.invoke(disk);
                        String diskSerial = (String) diskSerialMethod.invoke(disk);
                        long diskSize = (long) diskSizeMethod.invoke(disk);
                        out.write(diskName.getBytes(StandardCharsets.UTF_8));
                        out.write(diskModel.getBytes(StandardCharsets.UTF_8));
                        out.write(diskSerial.getBytes(StandardCharsets.UTF_8));
                        out.writeLong(diskSize);
                    }

                    // Extract GraphicsCard[] data.
                    // Not using version - can be a driver.
                    for (Object card : cards) {
                        String cardName = (String) cardNameMethod.invoke(card);
                        String cardId = (String) cardIdMethod.invoke(card);
                        String cardVendor = (String) cardVendorMethod.invoke(card);
                        long cardRam = (long) cardRamMethod.invoke(card);
                        out.write(cardName.getBytes(StandardCharsets.UTF_8));
                        out.write(cardId.getBytes(StandardCharsets.UTF_8));
                        out.write(cardVendor.getBytes(StandardCharsets.UTF_8));
                        out.writeLong(cardRam);
                    }
                } catch (Throwable t) {
                    // Log into trace. (disabled for MOST users)
                    LOGGER.trace("Unable to write OSHI data.", t);
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
