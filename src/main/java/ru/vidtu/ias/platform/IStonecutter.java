/*
 * In-Game Account Switcher is a third-party mod for Minecraft Java Edition that
 * allows you to change your logged in account in-game, without restarting it.
 *
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

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.UUID;

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
     * Game root directory.
     */
    //? if fabric {
    public static final Path GAME_DIRECTORY = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir();
    //?} elif neoforge {
    /*public static final Path GAME_DIRECTORY = net.neoforged.fml.loading.FMLPaths.GAMEDIR.get();
     *///?} else
    /*public static final Path GAME_DIRECTORY = net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get();*/

    /**
     * Game config directory.
     */
    //? if fabric {
    public static final Path CONFIG_DIRECTORY = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
    //?} elif neoforge {
    /*public static final Path CONFIG_DIRECTORY = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
     *///?} else
    /*public static final Path CONFIG_DIRECTORY = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();*/

    /**
     * A UUID consisting of zero values only:
     * "{@code 00000000-0000-0000-0000-000000000000}"
     */
    //? if >=1.21.11 {
    public static final UUID NIL_UUID = net.minecraft.util.Util.NIL_UUID;
    //?} else
    /*public static final UUID NIL_UUID = net.minecraft.Util.NIL_UUID;*/

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

    /**
     * Creates a new identifier (resource location) with the {@code "ias"} namespace and the given path.
     *
     * @param path The identifier path to use
     * @return A newly created identifier
     */
    @Contract(pure = true)
    //? if >=1.21.11 {
    public static net.minecraft.resources.Identifier identifier(String path) {
    //?} else
    /*public static net.minecraft.resources.ResourceLocation identifier(String path) {*/
        // Validate.
        assert path != null : "IAS: Parameter 'path' is null.";

        // Create.
        //? if >=1.21.11 {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath("ias", path);
        //?} else
        /*return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ias", path);*/
    }

    /**
     * Gets the internal clock in millis. Basically a {@link System#nanoTime()}, but in millis.
     *
     * @return Internal non-wall clock time in millis
     */
    @Contract(pure = true)
    public static long internalMillisClock() {
        //? if >=1.21.11 {
        return net.minecraft.util.Util.getMillis();
        //?} else
        /*return net.minecraft.Util.getMillis();*/
    }

    public static void renderMultilineLabelCentered(MultiLineLabel label, GuiGraphics graphics, int x, int y) {
        //? if >=1.21.11 {
        label.visitLines(net.minecraft.client.gui.TextAlignment.CENTER, x, y, 9, graphics.textRenderer());
        //?} elif >=1.21.10 {
        /*label.render(graphics, MultiLineLabel.Align.CENTER, x, y, 9, /^unused=^/false, -1);*/
        //?} else
        /*label.renderCentered(graphics, x, y);*/
    }

    public static void openUrl(String url) {
        //? if >=1.21.11 {
        net.minecraft.util.Util.getPlatform().openUri(url);
        //?} else
        /*net.minecraft.Util.getPlatform().openUri(url);*/
    }
}
