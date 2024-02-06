/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2024 VidTu
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

package ru.vidtu.ias;

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

/**
 * Main IAS class for Quilt.
 *
 * @author VidTu
 */
@SuppressWarnings("deprecation") // <- QSL is not out for 1.20.4.
public final class IASQuilt implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Create the UA and initialize.
        String modVersion = FabricLoader.getInstance().getModContainer("ias")
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getVersion)
                .map(Version::getFriendlyString)
                .orElse("UNKNOWN");
        String loaderVersion = FabricLoader.getInstance().getModContainer("quilt_loader")
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getVersion)
                .map(Version::getFriendlyString)
                .orElse("UNKNOWN");
        IASMinecraft.init(FabricLoader.getInstance().getGameDir(), FabricLoader.getInstance().getConfigDir(), "Quilt", modVersion, loaderVersion);

        // Register closer.
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> IAS.close());

        // Register screen handlers.
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            // Init.
            IASMinecraft.onInit(client, screen, Screens.getButtons(screen)::add);

            // Register drawer.
            if (screen instanceof TitleScreen || screen instanceof JoinMultiplayerScreen) {
                // Draw.
                Font font = client.font;
                ScreenEvents.afterRender(screen).register((scr, graphics, mouseX, mouseY, delta) -> IASMinecraft.onDraw(scr, font, graphics));
            }
        });
    }
}
