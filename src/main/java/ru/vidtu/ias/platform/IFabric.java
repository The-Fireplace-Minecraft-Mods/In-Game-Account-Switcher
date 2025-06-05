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

//? if fabric {
package ru.vidtu.ias.platform;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.IASMinecraft;

/**
 * Main IAS class for Fabric.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see IAS
 * @see IModMenu
 */
@ApiStatus.Internal
@NullMarked
public final class IFabric implements ClientModInitializer {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LogManager.getLogger("IAS/IFabric");

    /**
     * Creates a new mod.
     */
    @Contract(pure = true)
    public IFabric() {
        // Empty
    }

    @Override
    public void onInitializeClient() {
        // Log.
        long start = System.nanoTime();
        LOGGER.info("IAS: Loading... (platform: fabric)");

        // Register the screen handlers.
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            IASMinecraft.onInit(client, screen, Screens.getButtons(screen)::add);
            if (!(screen instanceof TitleScreen) && !(screen instanceof JoinMultiplayerScreen)) return;
            Font font = client.font;
            ScreenEvents.afterRender(screen).register((innerScreen, graphics, mouseX, mouseY, delta) -> {
                IASMinecraft.onDraw(innerScreen, font, graphics);
            });
        });

        // Register the shutdown hook.
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> IAS.close());

        // Create the UA and initialize.
        String modVersion = FabricLoader.getInstance().getModContainer("ias")
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getVersion)
                .map(Version::getFriendlyString)
                .orElse("UNKNOWN");
        String loaderVersion = FabricLoader.getInstance().getModContainer("fabricloader")
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getVersion)
                .map(Version::getFriendlyString)
                .orElse("UNKNOWN");
        IASMinecraft.init(FabricLoader.getInstance().getGameDir(), FabricLoader.getInstance().getConfigDir(), "Fabric", modVersion, loaderVersion);
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "IAS/IFabric{}";
    }
}
//?}
