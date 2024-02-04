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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.User;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.utils.IUtils;

/**
 * Main IAS class for Fabric.
 *
 * @author VidTu
 */
@Environment(EnvType.CLIENT)
public final class IASFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
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

        // Register closer.
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> IAS.close());

        // Register screen handlers.
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            // Init.
            IASMinecraft.onInit(screen, Screens.getButtons(screen)::add);

            // Draw.
            Font font = client.font;
            ScreenEvents.afterRender(screen).register((scr, graphics, mouseX, mouseY, delta) -> IASMinecraft.onDraw(scr, font, graphics));
        });

        // Register warnings about invalid accounts.
        ClientLoginConnectionEvents.INIT.register((handler, client) -> {
            // Skip if disabled.
            if (!IASConfig.nickWarns) return;

            // Warn about invalid names.
            User user = client.getUser();
            // Mods break non-nullness.
            //noinspection ConstantValue
            String name = user != null ? user.getName() : "";
            String key = IUtils.warnKey(name);
            if (key == null) return;

            // Display the toast.
            ToastComponent toasts = client.getToasts();
            toasts.addToast(SystemToast.multiline(client, IASMinecraft.NICK_WARN, Component.literal("In-Game Account Switcher"), Component.translatable(key, name)));
        });
    }
}
