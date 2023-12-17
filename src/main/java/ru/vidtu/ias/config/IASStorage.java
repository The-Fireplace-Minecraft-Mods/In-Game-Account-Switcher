package ru.vidtu.ias.config;

import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.MicrosoftAccount;
import ru.vidtu.ias.account.OfflineAccount;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
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
    private static final String DISCLAIMER = """
            > ENGLISH
            UNDER NO CIRCUMSTANCES SHOULD YOU SEND THE "accounts.donotsend" FILE OR THIS FOLDER TO *ANYONE*.
            IF YOU ACCIDENTALLY SENT THIS FILE OR FOLDER TO ANYONE, PLEASE, VISIT THE FOLLOWING WEBSITE:
            https://account.microsoft.com/security
            AND CHANGE YOUR PASSWORD, THEN VISIT THE FOLLOWING WEBSITE:
            https://account.live.com/consent/manage
            AND REVOKE THE PERMISSIONS (ACCESS) TO THE "In-Game Account Switcher" APPLICATION,
            AND/OR ANY OTHER THAT YOU DO CAN'T RECOGNIZE OR YOU SUSPECT IT COULD ACCESS YOUR GAME ACCOUNT.
            AFTER REVOKING ACCESS YOU SHOULD *NOT* USE THIS MODIFICATION FOR 31 DAYS.
            (If you suspect someone has got access to your game account, revoke ALL permissions
            for ALL applications and do *NOT* launch the game for 31 days at all)
            
            > РУССКИЙ (RUSSIAN)
            НИ ПРИ КАКИХ ОБСТОЯТЕЛЬСТВАХ НЕ ОТПРАВЛЯЙТЕ ФАЙЛ "accounts.donotsend" ИЛИ ЭТУ ПАПКУ *КОМУ-ЛИБО*.
            ЕСЛИ ВЫ СЛУЧАЙНО ОТПРАВИЛИ ЭТОТ ФАЙЛ ИЛИ ПАПКУ КОМУ-ЛИБО, ПОЖАЛУЙСТА, ЗАЙДИТЕ НА СЛЕДУЮЩИЙ ВЕБСАЙТ:
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
    private static final List<String> DISCLAIMER_FILE_NAMES = List.of(
            "READ_ME_IMPORTANT.txt", // English
            "ПРОЧТИ_МЕНЯ_ВАЖНО.txt" // Russian
    );

    /**
     * Account data, encrypted or not.
     */
    private static List<Account> accounts;

    /**
     * Creates a new storage for GSON.
     */
    private IASStorage() {
        // Private
    }

    /**
     * Writes the disclaimers.
     *
     * @param path Game directory
     * @throws RuntimeException If unable to write the disclaimers
     */
    public static void disclaimers(Path path) {
        try {
            // Get the path.
            path = path.resolve(".ias");

            // Create the path.
            Files.createDirectories(path);

            // Write every name.
            for (String name : DISCLAIMER_FILE_NAMES) {
                // Resolve the file.
                Path file = path.resolve(name);

                // Write the disclaimer.
                Files.writeString(file, DISCLAIMER, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE,
                        StandardOpenOption.SYNC, StandardOpenOption.DSYNC);
            }
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to write IAS disclaimers.", t);
        }
    }

    /**
     * Loads the storage, suppressing and logging any errors.
     *
     * @param path Game directory
     * @return Whether the config has been loaded without errors
     */
    public static boolean loadSafe(Path path) {
        try {
            // Try to load config.
            load(path);

            // Return success.
            return true;
        } catch (Throwable t) {
            // Log it.
            IAS.LOG.error("Unable to load IAS storage.", t);

            // Return fail.
            return false;
        }
    }

    /**
     * Loads the config.
     *
     * @param path Game directory
     * @throws RuntimeException If unable to load the config
     */
    public static void load(Path path) {
        try {
            // Get the file.
            Path file = path.resolve(".ias").resolve("accounts_v1.donotsend");

            // Skip if doesn't exist.
            if (!Files.isRegularFile(file)) return;

            // Read the data.
            byte[] data = Files.readAllBytes(file);

            // Decode the data.
            try (DataInputStream in = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(data)))) {
                // Read the length. (Hopefully 65535 accounts is enough)
                int length = in.readUnsignedShort();
                List<Account> list = new ArrayList<>(length);

                // Read all accounts.
                for (int i = 0; i < length; i++) {
                    // Read the type.
                    String type = in.readUTF();

                    // Read the account.
                    Account account = readTypedAccount(type, in);

                    // Add the account.
                    list.add(account);
                }

                // Flush the list.
                accounts = list;
            }
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to load IAS storage.", t);
        }
    }

    /**
     * Saves the config, suppressing and logging any errors.
     *
     * @param path Game directory
     * @return Whether the config has been saved without errors
     */
    public static boolean saveSafe(Path path) {
        try {
            // Try to load config.
            save(path);

            // Return success.
            return true;
        } catch (Throwable t) {
            // Log it.
            IAS.LOG.error("Unable to save IAS storage.", t);

            // Return fail.
            return false;
        }
    }

    /**
     * Saves the storage.
     *
     * @param path Game directory
     * @throws RuntimeException If unable to save the config
     */
    public static void save(Path path) {
        try {
            // Get the file.
            Path file = path.resolve(".ias").resolve("accounts_v1.donotsend");

            // Encode the data.
            byte[] data;
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 DeflaterOutputStream defOut = new DeflaterOutputStream(byteOut);
                 DataOutputStream out = new DataOutputStream(defOut)) {
                // Capture the list.
                List<Account> list = List.copyOf(accounts);

                // Write the length.
                out.writeShort(list.size());

                // Write the accounts.
                for (Account account : list) {
                    // Get the account type.
                    String type = accountType(account);

                    // Write the account type.
                    out.writeUTF(type);

                    // Write the account.
                    account.write(out);
                }

                // Flush the data.
                defOut.finish();
                data = byteOut.toByteArray();
            }

            // Create parent directories.
            Files.createDirectories(file.getParent());

            // Write the data.
            Files.write(file, data, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE,
                    StandardOpenOption.SYNC, StandardOpenOption.DSYNC);
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to save IAS storage.", t);
        }
    }

    /**
     * Reads the account by its type.
     *
     * @param type Account type
     * @param in   Target input
     * @return Read account
     * @throws IllegalArgumentException On invalid account type
     * @throws IOException              On I/O exception
     */
    private static Account readTypedAccount(String type, DataInput in) throws IOException {
        return switch (type) {
            case "ias:offline" -> OfflineAccount.read(in);
            case "ias:microsoft" -> MicrosoftAccount.read(in);
            default -> throw new IllegalArgumentException("Unknown account type: " + type);
        };
    }

    /**
     * Gets the account type from its class.
     *
     * @param account Target account
     * @return Account type
     * @throws IllegalArgumentException On invalid account type
     */
    private static String accountType(Account account) {
        if (account instanceof OfflineAccount) return "ias:offline";
        if (account instanceof MicrosoftAccount) return "ias:microsoft";
        throw new IllegalArgumentException("Unknown account type: " + account + " (" + (account != null ? account.getClass() : null) + ")");
    }
}
