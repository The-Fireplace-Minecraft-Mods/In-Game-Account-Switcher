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
**A**: Minecraft 1.18.2, 1.19.2, 1.19.4, 1.20.1, 1.20.2, 1.20.4, 1.20.6, 1.21.1, 1.21.3, 1.21.4.
There's little to no chance a newer version of this mod will be ported to versions prior to 1.18
(e.g. 1.16.5, 1.12.2, 1.8.9), due to its reliance on Java 17 code.
Old mod versions (e.g. 8.0.2) are not supported and will never be supported.

**Q**: Do I need Fabric API or Quilt Standard Libraries?  
**A**: Yes, you'll need Fabric API for Fabric/Quilt. On Quilt, you can install QFAPI/QSL instead.
Obviously, you do NOT need them for Forge or NeoForge.

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

Thanks to the [wiki.vg/Microsoft_Authentication_Scheme](https://wiki.vg/Microsoft_Authentication_Scheme)
page contributors for providing useful information about Microsoft authentication.

Thanks to the [Methanol developers](https://github.com/mizosoft/methanol) for providing
a cool HTTP client we use to automatically upload 30+ files to Modrinth, CurseForge, and GitHub.
