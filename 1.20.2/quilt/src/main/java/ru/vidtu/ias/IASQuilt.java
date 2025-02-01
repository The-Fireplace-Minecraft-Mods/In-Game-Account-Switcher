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

package ru.vidtu.ias;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.Version;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.screen.api.client.ScreenEvents;

/**
 * Main IAS class for Quilt.
 *
 * @author VidTu
 */
public final class IASQuilt implements ClientModInitializer {
    @Override
    public void onInitializeClient(@NotNull ModContainer mod) {
        // Create the UA and initialize.
        String modVersion = mod.metadata().version().raw();
        String loaderVersion = QuiltLoader.getModContainer("quilt_loader")
                .map(ModContainer::metadata)
                .map(ModMetadata::version)
                .map(Version::raw)
                .orElse("UNKNOWN");
        IASMinecraft.init(QuiltLoader.getGameDir(), QuiltLoader.getConfigDir(), "Quilt", modVersion, loaderVersion);

        // Register closer.
        ClientLifecycleEvents.STOPPING.register(client -> IAS.close());

        // Register screen handlers.
        ScreenEvents.AFTER_INIT.register((screen, client, firstInit) -> {
            // Init.
            IASMinecraft.onInit(client, screen, screen.getButtons()::add);
        });

        // Register drawer.
        ScreenEvents.AFTER_RENDER.register((screen, graphics, mouseX, mouseY, tickDelta) -> {
            // Draw.
            IASMinecraft.onDraw(screen, screen.getClient().font, graphics);
        });
    }
}
