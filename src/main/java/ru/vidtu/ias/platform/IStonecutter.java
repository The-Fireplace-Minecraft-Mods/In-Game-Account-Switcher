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

package ru.vidtu.ias.platform;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

/**
 * A helper class that contains methods that depend on Stonecutter, a Java source code preprocessor.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class IStonecutter {
    /**
     * Game config directory.
     */
    //? if fabric {
    public static final Path CONFIG_DIRECTORY = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
    //?} else if neoforge {
    /*public static final Path CONFIG_DIRECTORY = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
     *///?} else
    /*public static final Path CONFIG_DIRECTORY = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();*/

    /**
     * Game root directory.
     */
    //? if fabric {
    public static final Path ROOT_DIRECTORY = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir();
    //?} else if neoforge {
    /*public static final Path ROOT_DIRECTORY = net.neoforged.fml.loading.FMLPaths.GAMEDIR.get();
     *///?} else
    /*public static final Path ROOT_DIRECTORY = net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get();*/

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private IStonecutter() {
        throw new AssertionError("IAS: No instances.");
    }

    public static ResourceLocation newIdentifier(String path) {
        //? if >=1.21.1 || (forge && (!hackyNeoForge) && >=1.18.2 && (!1.20.2)) {
        return ResourceLocation.fromNamespaceAndPath("ias", path);
        //?} else
        /*return new ResourceLocation("ias", path);*/
    }
}
