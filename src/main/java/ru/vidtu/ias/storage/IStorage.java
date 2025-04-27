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

package ru.vidtu.ias.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.platform.IStonecutter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * IAS account storage.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class IStorage {
    /**
     * Disclaimer for files.
     *
     * @deprecated Disclaimer text should be localized via files, not with hardcoding
     */
    @Deprecated(forRemoval = true)
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
     *
     * @deprecated Disclaimer file names should be localized via files, not with hardcoding and some filesystems (Linux withOUT utf-8) doesn't support all unicode
     */
    @Deprecated(forRemoval = true)
    @Unmodifiable
    private static final List<String> DISCLAIMER_FILE_NAMES = List.of(
            "READ_ME_IMPORTANT.txt", // English
            "ПРОЧТИ_МЕНЯ_ВАЖНО.txt" // Russian
    );

    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LogManager.getLogger("IAS/IStorage");

    /**
     * Account data, encrypted or not.
     */
    public static final List<Account> ACCOUNTS = new ArrayList<>(0);

    /**
     * @deprecated A better alternative needs to be designed
     */
    @Deprecated(forRemoval = true)
    public static boolean gameDisclaimerShown = false;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private IStorage() {
        throw new AssertionError("No instances.");
    }

    /**
     * Writes the disclaimers.
     *
     * @throws RuntimeException If unable to write the disclaimers
     * @deprecated Disclaimers should be localized via files, not hardcoded
     */
    @Deprecated(forRemoval = true)
    public static void disclaimers() {
        try {
            // Log.
            LOGGER.debug("IAS: Writing disclaimers into {}...", IStonecutter.GAME_DIRECTORY);

            // Get the path.
            var path = IStonecutter.GAME_DIRECTORY.resolve("_IAS_ACCOUNTS_DO_NOT_SEND_TO_ANYONE");

            // Create the path.
            Files.createDirectories(path);

            // Write every name.
            for (String name : DISCLAIMER_FILE_NAMES) {
                // Resolve the file.
                Path file = path.resolve(name);

                // Write the disclaimer.
                Files.writeString(file, DISCLAIMER, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE,
                        StandardOpenOption.SYNC, StandardOpenOption.DSYNC, LinkOption.NOFOLLOW_LINKS);
            }

            // Log.
            LOGGER.debug("IAS: Disclaimers ({}) written to {}.", DISCLAIMER_FILE_NAMES, path);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to write IAS disclaimers.", t);
        }
    }

    /**
     * Loads the storage, suppressing and logging any errors.
     *
     * @see #save()
     */
    public static void load() {
        try {
            // Log. (**TRACE**)
            LOGGER.trace(IAS.IAS_MARKER, "IAS: Loading the storage... (directory: {})", IStonecutter.GAME_DIRECTORY);

            // Resolve the file.
            Path file = IStonecutter.GAME_DIRECTORY.resolve("_IAS_ACCOUNTS_DO_NOT_SEND_TO_ANYONE/.hidden/accounts_v1.do_not_send_to_anyone");

            // Read the storage.
            Set<Account> accounts;
            try (DataInputStream in = new DataInputStream(new InflaterInputStream(Files.newInputStream(file)))) {
                // Read the length.
                int length = in.readUnsignedShort();
                accounts = LinkedHashSet.newLinkedHashSet(length);

                // Read the accounts.
                for (int i = 0; i < length; i++) {
                    // Read and add the account.
                    Account account = Account.readTyped(in);
                    accounts.add(account);
                }
            }

            // Flush the accounts.
            ACCOUNTS.addAll(accounts);

            // Log. (**DEBUG**)
            LOGGER.debug(IAS.IAS_MARKER, "IAS: Storage has been loaded. (directory: {}, file: {})", IStonecutter.GAME_DIRECTORY, file);
        } catch (NoSuchFileException nsfe) {
            // Log. (**DEBUG**)
            LOGGER.debug(IAS.IAS_MARKER, "IAS: Ignoring missing IAS storage.", nsfe);
        } catch (Throwable t) {
            // Log.
            LOGGER.error(IAS.IAS_MARKER, "IAS: Unable to load the IAS storage.", t);
        }
    }

    /**
     * Saves the storage, suppressing and logging any errors.
     *
     * @see #load()
     */
    public static void save() {
        try {
            // Log. (**TRACE**)
            LOGGER.trace(IAS.IAS_MARKER, "IAS: Saving the storage... (directory: {})", IStonecutter.GAME_DIRECTORY);

            // Resolve the file.
            Path file = IStonecutter.GAME_DIRECTORY.resolve("_IAS_ACCOUNTS_DO_NOT_SEND_TO_ANYONE/.hidden/accounts_v1.do_not_send_to_anyone");

            // Write the storage.
            Files.createDirectories(file.getParent());
            try (DataOutputStream out = new DataOutputStream(new DeflaterOutputStream(Files.newOutputStream(file, StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.SYNC)))) {
                // Write the length.
                out.writeShort(ACCOUNTS.size());

                // Write the accounts.
                for (Account account : ACCOUNTS) {
                    // Write typed.
                    Account.writeTyped(out, account);
                }
            }

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

            // Log. (**DEBUG**)
            LOGGER.debug(IAS.IAS_MARKER, "IAS: Storage has been saved. (directory: {}, file: {})", IStonecutter.GAME_DIRECTORY, file);
        } catch (Throwable t) {
            // Log.
            LOGGER.error(IAS.IAS_MARKER, "IAS: Unable to save the IAS storage.", t);
        }
    }
}
