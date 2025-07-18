# In-Game Account Switcher

In-Game Account Switcher allows you to change which account you are logged in to in-game,
without having to restart Minecraft.

## Dependencies

**Fabric**: [Fabric API](https://modrinth.com/mod/fabric-api) (Required),
[Mod Menu](https://modrinth.com/mod/modmenu) (Optional)  
**Quilt**: [QFAPI/QSL](https://modrinth.com/mod/qsl) (Required),
[Mod Menu](https://modrinth.com/mod/modmenu) (Optional)  
**Forge**: (none)  
**NeoForge**: (none)

## FAQ

**Q**: I need help, have some questions, or something else.  
**A**: You can look at the docs for
[Terms and Privacy](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/blob/main/docs/TERMS.md),
[Crypt](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/blob/main/docs/CRYPT.md),
[Stolen Accounts](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/blob/main/docs/STOLEN.md),
[Common Errors](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/blob/main/docs/ERRORS.md),
[Secure Log Sharing](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/blob/main/docs/LOG_SHARING.md),
or you can join the [Discord server](https://discord.gg/TpU2nEkSPk).

**Q**: Where can I download this mod?  
**A**: [Modrinth](https://modrinth.com/mod/in-game-account-switcher),
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/in-game-account-switcher),
[GitHub](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher).
You can also find unstable builds at [GitHub Actions](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/actions),
to download them you'll need a GitHub account.

**Q**: Which mod loaders are supported?  
**A**: Forge, Fabric, NeoForge. Quilt should work too.

**Q**: Which versions are supported?  
**A**: Minecraft 1.18.2, 1.19.2, 1.19.4, 1.20.1, 1.20.2, 1.20.4, 1.20.6, 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8.
Old mod versions (for MC 1.8.9, 1.12.2, etc.) are not supported.

**Q**: Do I need Fabric API or Quilt Standard Libraries?  
**A**: Yes, you'll need Fabric API for Fabric/Quilt. On Quilt, you can install QFAPI/QSL instead.
Obviously, you do NOT need them for Forge or NeoForge.

**Q**: Help, I have shared my account with someone! Someone is using my account via In-Game Account Switcher!  
**A**: Read the "[Stolen Accounts](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/blob/main/docs/STOLEN.md)" page.

**Q**: I'm using an old version (for 1.8.9/1.12.2) of the mod and it gives me the "SSLHandshakeException" error, how do I fix it?  
**A**: You need to update your Java 8 to a newer build. You can use any Java vendor that you want, there's little to no difference, popular choices:
Eclipse Temurin ([Windows](https://adoptium.net/temurin/releases/?package=jre&version=8&os=windows),
[macOS](https://adoptium.net/temurin/releases/?package=jre&version=8&os=mac),
[Linux](https://adoptium.net/temurin/releases/?package=jre&version=8&os=linux)),
Azul Zulu ([Windows](https://www.azul.com/downloads/?version=java-8-lts&os=windows&package=jre#zulu),
[macOS](https://www.azul.com/downloads/?version=java-8-lts&os=macos&package=jre#zulu),
[Linux](https://www.azul.com/downloads/?version=java-8-lts&os=linux&package=jre#zulu)),
Amazon Corretto ([Windows](https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/windows-install.html),
[macOS](https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/macos-install.html),
[Linux](https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/linux-info.html)).

**Q**: I'm using Feather/Lunar Client and the mod doesn't work properly (the button doesn't appear, the account list is empty, etc), when you will fix this?  
**A**: These clients break lots of things. Since they are closed-source, we cannot add compatibility with them.
You should ask client developers to fix any issues. Many of this clients have their own built-in account switcher, use it instead.

**Q**: Can I import/export my accounts from/into a file? Can I use access tokens/cookies to add Microsoft accounts? Will you add this?  
**A**: No. This kind of behavior is either shady or prohibited by [Minecraft EULA](https://minecraft.net/eula).

**Q**: Is this mod open source?  
**A**: [Yes.](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher) (Licensed
under [GNU LGPLv3](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/blob/main/LICENSE))

**Q**: I've found a bug.  
**A**: Report it [here](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/issues). If you are
not sure if this is a bug, you can join the [Discord](https://discord.gg/TpU2nEkSPk). If you think this bug is
critical enough not to be disclosed publicly, please, report it as described in the
[security policy](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/blob/main/SECURITY.md).

## Screenshots

![main menu](https://i.imgur.com/DX06VoG.png)
![account selector](https://i.imgur.com/5hiQ6Om.png)

## Credits

Thanks to the [minecraft.wiki/Microsoft_authentication](https://minecraft.wiki/Microsoft_authentication)
(previously was on wiki.vg) page contributors for providing useful information about Microsoft authentication.

Thanks to the [Methanol developers](https://github.com/mizosoft/methanol) for providing
a cool HTTP client we use to automatically upload 30+ files to Modrinth, CurseForge, and GitHub.
