package ru.vidtu.ias.platform;

import java.nio.file.Path;

public class IStonecutter {
    /**
     * Game root directory.
     */
    //? if fabric {
    public static final Path GAME_DIRECTORY = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir();
    //?} else if neoforge {
    /*public static final Path GAME_DIRECTORY = net.neoforged.fml.loading.FMLPaths.GAMEDIR.get();
     *///?} else
    /*public static final Path GAME_DIRECTORY = net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get();*/

    /**
     * Game config directory.
     */
    //? if fabric {
    public static final Path CONFIG_DIRECTORY = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
    //?} else if neoforge {
    /*public static final Path CONFIG_DIRECTORY = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
     *///?} else
    /*public static final Path CONFIG_DIRECTORY = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();*/
}
