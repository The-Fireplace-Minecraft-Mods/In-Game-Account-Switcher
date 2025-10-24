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

package ru.vidtu.ias.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.account.Account;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * IAS account storage.
 *
 * @author VidTu
 */
public final class IASStorage {
    /**
     * Disclaimer for files.
     */
    @NotNull
    private static final String DISCLAIMER = """
            > ENGLISH
            Notification about security of accounts stored in the "In-Game Account Switcher" mod:
            UNDER NO CIRCUMSTANCES SHOULD YOU SEND THIS FOLDER TO *ANYONE* (INCLUDING DEVELOPERS OF THIS MOD),
            EVEN IF IT APPEARS THAT THIS FOLDER IS FULLY EMPTY.
            IF YOU ACCIDENTALLY SENT THIS FOLDER TO ANYONE, PLEASE, VISIT THE FOLLOWING WEBSITE:
            https://account.microsoft.com/security
            AND CHANGE YOUR PASSWORD, THEN VISIT THE FOLLOWING WEBSITE:
            https://account.live.com/consent/manage
            AND REVOKE THE PERMISSIONS (ACCESS) TO THE "In-Game Account Switcher" APPLICATION,
            AND/OR ANY OTHER THAT YOU DO CAN'T RECOGNIZE OR YOU SUSPECT IT COULD ACCESS YOUR GAME ACCOUNT.
            AFTER REVOKING ACCESS YOU SHOULD *NOT* USE THIS MODIFICATION FOR 31 DAYS.
            (If you suspect someone has got access to your game account, revoke ALL permissions
            for ALL applications and do *NOT* launch the game for 31 days at all)
            
            
            
            > РУССКИЙ (RUSSIAN)
            Уведомление о безопасности аккаунтов из мода "In-Game Account Switcher":
            НИ ПРИ КАКИХ ОБСТОЯТЕЛЬСТВАХ НЕ ОТПРАВЛЯЙТЕ ЭТУ ПАПКУ *КОМУ-ЛИБО* (В ТОМ ЧИСЛЕ И РАЗРАБОТЧИКАМ ЭТОГО МОДА),
            ДАЖЕ ЕСЛИ ВАМ КАЖЕТСЯ, ЧТО ЭТА ПАПКА ПОЛНОСТЬЮ ПУСТАЯ.
            ЕСЛИ ВЫ СЛУЧАЙНО ОТПРАВИЛИ ЭТУ ПАПКУ КОМУ-ЛИБО, ПОЖАЛУЙСТА, ЗАЙДИТЕ НА СЛЕДУЮЩИЙ ВЕБСАЙТ:
            https://account.microsoft.com/security
            И СМЕНИТЕ СВОЙ ПАРОЛЬ, ПОТОМ ЗАЙДИТЕ НА СЛЕДУЮЩИЙ ВЕБСАЙТ:
            https://account.live.com/consent/manage
            И ОТЗОВИТЕ РАЗРЕШЕНИЯ (ДОСТУП) К ПРИЛОЖЕНИЮ "In-Game Account Switcher"
            И/ИЛИ ЛЮБОМУ ДРУГОМУ, КОТОРОЕ ВЫ НЕ МОЖЕТЕ ОПОЗНАТЬ ИЛИ ПОДОЗРЕВАЕТЕ, ЧТО ОНО МОЖЕТ
            ПОЛУЧИТЬ ДОСТУП К ВАШЕМУ ИГРОВОМУ АККАУНТУ.
            ПОСЛЕ ОТЗЫВА ДОСТУПА ВЫ *НЕ* ДОЛЖНЫ ИСПОЛЬЗОВАТЬ ЭТУ МОДИФИКАЦИЮ КАК МИНИМУМ 31 ДЕНЬ.
            (Если вы подозреваете, что кто-то получил доступ к вашему игровому аккаунту, отзовите ВСЕ разрешения
            для ВСЕХ приложений и *НЕ* запускайте игру вообще как минимум 31 день)
            """;

    /**
     * Disclaimer file names.
     */
    @NotNull
    @Unmodifiable
    private static final List<String> DISCLAIMER_FILE_NAMES = List.of(
            "READ_ME_IMPORTANT.txt", // English
            "ПРОЧТИ_МЕНЯ_ВАЖНО.txt" // Russian
    );

    /**
     * Logger for this class.
     */
    @NotNull
    public static final Logger LOGGER = LoggerFactory.getLogger("IAS/IASStorage");

    /**
     * Account data, encrypted or not.
     */
    @NotNull
    public static final List<Account> ACCOUNTS = new ArrayList<>(0);

    /**
     * Whether the game disclaimer was shown.
     */
    public static boolean gameDisclaimerShown = false;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    @Contract(value = "-> fail", pure = true)
    private IASStorage() {
        throw new AssertionError("No instances.");
    }

    /**
     * Writes the disclaimers.
     *
     * @param path Game directory
     * @throws RuntimeException If unable to write the disclaimers
     */
    public static void disclaimers(@NotNull Path path) {
        try {
            // Log.
            LOGGER.debug("IAS: Writing disclaimers into {}...", path);

            // Get the path.
            path = path.resolve("_IAS_ACCOUNTS_DO_NOT_SEND_TO_ANYONE");

            // Create the path.
            Files.createDirectories(path);

            // Write every name.
            for (String name : DISCLAIMER_FILE_NAMES) {
                // Wrap.
                try {
                    // Resolve the file.
                    Path file = path.resolve(name);

                    // Write the disclaimer.
                    Files.writeString(file, DISCLAIMER, StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE,
                            StandardOpenOption.SYNC, StandardOpenOption.DSYNC, LinkOption.NOFOLLOW_LINKS);
                } catch (Throwable t) {
                    if (!name.equals("READ_ME_IMPORTANT.txt")) continue;
                    throw t;
                }
            }

            // Log.
            LOGGER.debug("IAS: Disclaimers ({}) written to {}.", DISCLAIMER_FILE_NAMES, path);
        } catch (Throwable t) {
            // Log. (**ERROR**)
            LOGGER.error("Unable to write IAS disclaimers.", t);
        }
    }

    /**
     * Loads the storage.
     *
     * @param path Game directory
     * @throws RuntimeException If unable to load the storage
     */
    public static void load(@NotNull Path path) {
        try {
            // Log.
            LOGGER.debug("IAS: Loading storage for {}...", path);

            // Get the file.
            Path folder = path.resolve("_IAS_ACCOUNTS_DO_NOT_SEND_TO_ANYONE/.hidden");
            Path file = folder.resolve("accounts_v1.do_not_send_to_anyone");
            gameDisclaimerShown = Files.isRegularFile(folder.resolve("game_disclaimer_shown"), LinkOption.NOFOLLOW_LINKS);

            // Skip if it doesn't exist.
            if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                LOGGER.debug("IAS: Storage not found. Saving...");
                save(path);
                return;
            }
            file = file.toRealPath(LinkOption.NOFOLLOW_LINKS);

            // Read the data.
            byte[] data = Files.readAllBytes(file);

            // Decode the data.
            try (DataInputStream in = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(data)))) {
                // Read the length. (Hopefully 65535 accounts is enough)
                int length = in.readUnsignedShort();
                List<Account> list = new ArrayList<>(length);

                // Read all accounts.
                for (int i = 0; i < length; i++) {
                    // Read typed.
                    list.add(Account.readTyped(in));
                }

                // Flush the list.
                ACCOUNTS.addAll(list);

                // Deduplicate.
                Set<Account> set = new HashSet<>(ACCOUNTS.size());
                ACCOUNTS.removeIf(Predicate.not(set::add));

                // Log.
                LOGGER.debug("IAS: Loaded {} (currently: {}) accounts from {}.", list.size(), ACCOUNTS.size(), file);
            }
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to load IAS storage.", t);
        }
    }

    /**
     * Saves the storage.
     *
     * @param path Game directory
     * @throws RuntimeException If unable to save the storage
     */
    public static void save(@NotNull Path path) {
        try {
            // Log.
            LOGGER.debug("IAS: Saving storage into {}...", path);

            // Get the file.
            Path file = path.resolve("_IAS_ACCOUNTS_DO_NOT_SEND_TO_ANYONE/.hidden/accounts_v1.do_not_send_to_anyone");

            // Encode the data.
            byte[] data;
            Account[] list;
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 DeflaterOutputStream defOut = new DeflaterOutputStream(byteOut);
                 DataOutputStream out = new DataOutputStream(defOut)) {
                // Capture the list.
                list = ACCOUNTS.toArray(Account[]::new);

                // Write the length.
                out.writeShort(list.length);

                // Write the accounts.
                for (Account account : list) {
                    // Write typed.
                    Account.writeTyped(out, account);
                }

                // Flush the data.
                defOut.finish();
                data = byteOut.toByteArray();
            }

            // Create parent directories.
            Files.createDirectories(file.getParent());

            // Try to make folder hidden on Windows. (already hidden by name on UNIX-like)
            try {
                Files.setAttribute(file.getParent(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
            } catch (Throwable ignored) {
                // Ignored
            }

            // Try to make folder EXTRA hidden on Windows. (already hidden by name on UNIX-like)
            try {
                Files.setAttribute(file.getParent(), "dos:system", true, LinkOption.NOFOLLOW_LINKS);
            } catch (Throwable ignored) {
                // Ignored
            }

            // Write the data.
            Files.write(file, data, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE,
                    StandardOpenOption.SYNC, StandardOpenOption.DSYNC, LinkOption.NOFOLLOW_LINKS);

            // Log it.
            LOGGER.debug("IAS: Saved {} accounts to {}.", list.length, file);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to save IAS storage.", t);
        }
    }

    /**
     * Sets the {@link #gameDisclaimerShown} to {@code true} and writes the persistent state file.
     *
     * @param path Game directory
     * @throws RuntimeException If unable to set or write game disclaimer shown persistent state
     */
    public static void gameDisclaimerShown(@NotNull Path path) {
        try {
            // Log it.
            LOGGER.debug("IAS: Marking in-game disclaimers as shown into {}...", path);

            // Set the parameter.
            gameDisclaimerShown = true;

            // Create the file.
            Path file = path.resolve("_IAS_ACCOUNTS_DO_NOT_SEND_TO_ANYONE/.hidden/game_disclaimer_shown");
            Files.createDirectories(file.getParent());
            Files.createFile(file);

            // Log it.
            LOGGER.debug("IAS: Marked in-game disclaimers as shown to {}.", file);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to mark game disclaimer as shown.", t);
        }
    }
}
