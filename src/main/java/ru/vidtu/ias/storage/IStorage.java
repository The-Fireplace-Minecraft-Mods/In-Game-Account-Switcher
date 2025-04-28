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

import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.platform.IStonecutter;
import ru.vidtu.ias.storage.account.Account;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
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
     * Whether to always use English (ASCII) instead of user-selected language disclaimer.
     */
    // Addendum to fix for: https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/issues/216
    // While the fix itself is to use ONLY the user language instead of writing disclaimers in ALL languages,
    // this is the additional system property that will allow forcing English (ASCII) disclaimers.
    private static final boolean FORCE_ENGLISH_DISCLAIMER = Boolean.getBoolean("ru.vidtu.ias.forceEnglishDisclaimer");

    /**
     * Pattern for valid file names for game disclaimers.
     */
    // This regex allows from 1 to 32 characters, excluding dots, slashes (forwards/backwards), and null characters.
    // Additional filename sanity checks (e.g. disallowance of vertical bar characters) is performed by the filesystem.
    // Why does this check exists? Simple and plain: I don't want a malicious locale to allow arbitrary file writing.
    private static final Pattern VALID_NAME = Pattern.compile("^[^\0./\\\\]{1,32}$");

    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LogManager.getLogger("IAS/IStorage");

    /**
     * Account data, encrypted or not.
     */
    public static final List<Account> ACCOUNTS = new ArrayList<>(0);

    /**
     * Whether the storage already exists and the disclaimer doesn't need to be shown.
     */
    private static boolean storageExists = false;

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
                    // Decode the account.
                    Account account = Account.decode(in);
                    accounts.add(account);
                }
            }

            // Flush the accounts.
            ACCOUNTS.addAll(accounts);

            // Mark the storage as existing.
            storageExists = true;

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
            Path folder = IStonecutter.GAME_DIRECTORY.resolve("_IAS_ACCOUNTS_DO_NOT_SEND_TO_ANYONE");
            Path file = folder.resolve(".hidden/accounts_v1.do_not_send_to_anyone");

            // Load the disclaimer file.
            String lang = FORCE_ENGLISH_DISCLAIMER ? "en_us" : Minecraft.getInstance().getLanguageManager().getSelected();
            try (BufferedReader input = new BufferedReader(new InputStreamReader(Objects.requireNonNullElseGet(
                    IStorage.class.getResourceAsStream("/assets/ias/disclaimers/" + lang + ".txt"),
                    () -> IStorage.class.getResourceAsStream("/assets/ias/disclaimers/en_us.txt")), StandardCharsets.UTF_8))) {
                // Get and validate the disclaimer filename.
                String name = input.readLine();
                Preconditions.checkNotNull(name, "IAS: Disclaimer file is empty. (lang: %s)", lang);
                Preconditions.checkState(VALID_NAME.matcher(name).matches(), "IAS: Invalid disclaimer file name. (lang: %s, name: %s)", lang, name);;

                // Get the lines.
                List<String> lines = input.lines().toList();

                // Write.
                Path dfile = folder.resolve(name + ".txt");
                Preconditions.checkState(dfile.startsWith(folder) && dfile.getParent().equals(folder), "IAS: Invalid disclaimer file. (lang: %s, name: %s, file: %s)", lang, name, dfile);
                Files.createDirectories(dfile.getParent());
                Files.write(dfile, lines);
            }

            // Write the storage.
            Files.createDirectories(file.getParent());
            try (DataOutputStream out = new DataOutputStream(new DeflaterOutputStream(Files.newOutputStream(file, StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.SYNC)))) {
                // Write the length.
                out.writeShort(ACCOUNTS.size());

                // Write the accounts.
                for (Account account : ACCOUNTS) {
                    // Encode the account.
                    account.encode(out);
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

            // Mark the storage as existing.
            storageExists = true;

            // Log. (**DEBUG**)
            LOGGER.debug(IAS.IAS_MARKER, "IAS: Storage has been saved. (directory: {}, file: {})", IStonecutter.GAME_DIRECTORY, file);
        } catch (Throwable t) {
            // Log.
            LOGGER.error(IAS.IAS_MARKER, "IAS: Unable to save the IAS storage.", t);
        }
    }

    /**
     * Gets whether the storage exists.
     *
     * @return Whether the storage already exists and the disclaimer doesn't need to be shown
     */
    @Contract(pure = true)
    public static boolean storageExists() {
        return storageExists;
    }
}
