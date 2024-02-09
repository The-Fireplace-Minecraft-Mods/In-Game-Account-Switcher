# Crypt

Crypt (aka Encryption) was added in IAS version 9 to prevent account malware stealing and accidental account sharing by
sharing config files. This is not a perfect solution, but it should work against most cases. Each account can be crypted
with a different password or different encryption method. Offline accounts can NOT be encrypted. There are two (or
three, if you wish) types of crypts. If you forget your password (for "Password Crypt") or change your hardware
(for "Hardware Crypt"), you will need to add your account into the mod again.

## Types

### Password Encryption

Password crypt will use any password provided by you (we DO not recommend using password from your Microsoft
account or any other account, just create a new random password) to encrypt your account. This password is never sent
over the internet and all processes are performed on your computer (much like encrypting a ZIP archive) to protect your
account from various malware (such as stealers) or accidental sharing with other people. This is the most secure
encryption option possible as it will protect you from most malware (the only exception is any possible malware running
on your PC right at the moment of encrypting/decrypting) or accidental sharing. The encryption strength depends on your
password strength. The password itself is never stored anywhere on the disk, only in RAM.

### Hardware Crypt

Hardware crypt will use your current OS and hardware configuration to encrypt your account. This data is never sent
over the internet and all processes are performed on your computer to protect your account from various malware (such as
stealers) or accidental sharing with other people. This is less secure option than password encryption, as some
specifically designed malware could store various PC details. It will most likely protect you from other malware and
accidental sharing though. This is more convenient since you don't need to enter any passwords, but it's less secure.

### No Crypt

No crypt will NOT crypt your account at all and will store your tokens on the disk AS-IS in PLAINTEXT. This is
**UNSAFE** and, as such, is disabled by default in the configuration and, even when enabled, requires pressing
additional keybind to acknowledge the risks. It may be useful if you encrypt your disc or your Minecraft folder.
This *WON'T* protect you from any malware or accidental sharing.

## Comparison

|              |                         Password Crypt                          |                         Hardware Crypt                         |                 No Crypt                  |
|:------------:|:---------------------------------------------------------------:|:--------------------------------------------------------------:|:-----------------------------------------:|
|   Security   |                          Pretty secure                          |                       Most likely secure                       |                 Insecure                  |
| Convenience  | Can be inconvenient (you need to enter the password every time) | Most likely convenient (unless you change your hardware often) |              Most convenient              |
| Recommended  |                               Yes                               |                              Yes                               |                    No                     |
| Designed For |                            Paranoids                            |                         Everyday Usage                         | People who use "123456" as their password |

## Technical Details

No crypt accounts are stored as-is so there are no too much to say about them.
Both password and hardware encryption uses this encryption/decryption method.

1. Read (or create if hardware) the password.
2. Hash it using `PBKDF2WithHmacSHA512` with 500_000 iterations, 2048 bits (256 bytes) of salt to 256 bits (32 bytes) key.
3. Encrypt/decrypt it using `AES/GCM/NoPadding` with 128 bits (16 bytes) of IV and 128 bits (16 bytes) of authentication tag.
4. Done.

The IV and the salt are generated using strong SecureRandom for each account and stored alongside each account.

### Hardware Password Generation

Hardware encryption use hardware details as "passwords". These are generated from your:

- OS architecture. (`System.getProperty("os.arch")`)
- Available processors. (`Runtime.availableProcessors()`)
- System file, path, and line separator characters.
- The following Java system properties: `"java.io.tmpdir", "native.encoding", "user.name",
  "user.home", "user.country", "sun.io.unicode.encoding", "stderr.encoding", "sun.cpu.endian",
  "sun.cpu.isalist", "sun.jnu.encoding", "stdout.encoding", "sun.arch.data.model",
  "user.language", "user.variant", "minecraft.inGameAccountSwitcher.veryNerdySystemProperty"`.
- The values from the following environmental keys: `"COMPUTERNAME", "PROCESSOR_ARCHITECTURE",
  "PROCESSOR_REVISION", "PROCESSOR_IDENTIFIER", "PROCESSOR_LEVEL", "NUMBER_OF_PROCESSORS", "OS", "USERNAME",
  "USERDOMAIN", "USERDOMAIN_ROAMINGPROFILE", "APPDATA", "HOMEPATH", "LOGONSERVER", "LOCALAPPDATA", "TEMP", "TMP",
  "MINECRAFT_IN_GAME_ACCOUNT_SWITCHER_VERY_NERDY_SYSTEM_ENV"`.
- Names (`NetworkInterface.getName()`), display names (`NetworkInterface.getDisplayName()`), MAC
  addresses (`NetworkInterface.getHardwareAddress()`), MTUs (`NetworkInterface.getMTU()`) of all network interfaces
  (`NetworkInterface.networkInterfaces().toList()`) in your device. (if available; ordering is preserved)
- OS bitness. (provided by OSHI if available)
- OS family. (provided by OSHI if available)
- OS manufacturer. (provided by OSHI if available)
- Hardware UUID. (provided by OSHI if available)
- Hardware manufacturer. (provided by OSHI if available)
- Hardware model. (provided by OSHI if available)
- Motherboard serial number. (provided by OSHI if available)
- Motherboard manufacturer. (provided by OSHI if available)
- Motherboard model. (provided by OSHI if available)
- Motherboard revision. (provided by OSHI if available)
- BIOS name. (provided by OSHI if available)
- BIOS description. (provided by OSHI if available)
- BIOS manufacturer. (provided by OSHI if available)
- Names, models, serial numbers, and total sizes in bytes of all disks. (ordering is preserved; provided by OSHI if available)
- Names, IDs, vendors and maximum VRAM sizes in bytes of all graphics cards. (ordering is preserved; provided by OSHI if available)

## Disclaimer

I (VidTu who wrote this encryption and docs) am NOT a security expert or major. Feel free to look at the source code
(e.g. into [Crypt.java](../src/main/java/ru/vidtu/ias/crypt/Crypt.java) file) and search for possible security
vulnerabilities. You can report those as described in the [security policy](../SECURITY.md).

If you think your token and/or account info is already stolen or want to prevent it from being stolen,
take a look at the [Stolen](STOLEN.md) page.
