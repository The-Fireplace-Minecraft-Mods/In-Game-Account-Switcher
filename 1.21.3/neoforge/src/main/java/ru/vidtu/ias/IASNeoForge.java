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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import ru.vidtu.ias.screen.ConfigScreen;

/**
 * Main IAS class for NeoForge.
 *
 * @author VidTu
 */
@Mod("ias")
public final class IASNeoForge {
    /**
     * Creates a new mod.
     */
    @SuppressWarnings("ThisEscapedInObjectConstruction") // <- Minecraft Forge API.
    public IASNeoForge() {
        // New versions don't need the NeoForge/Forge difference check, because
        // packages are different. The newest NeoForge version also
        // change its location to "neoforge.mods.toml" from "mods.toml".

        // Not sure how long the Forge does have the "clientSideOnly" field in the TOML,
        // so I'll do an additional exception check here.
        if (FMLEnvironment.dist != Dist.CLIENT) {
            throw new UnsupportedOperationException("IAS: You've tried to load the In-Game Account Switcher mod on a server. This won't work.");
        }

        // Register events.
        NeoForge.EVENT_BUS.register(this);

        // Register various display tests and config hooks.
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (minecraft, screen) -> new ConfigScreen(screen));

        // Create the UA and initialize.
        String modVersion = ModList.get().getModContainerById("ias")
                .map(ModContainer::getModInfo)
                .map(IModInfo::getVersion)
                .map(ArtifactVersion::toString)
                .orElse("UNKNOWN");
        String loaderVersion = ModList.get().getModContainerById("neoforge")
                .map(ModContainer::getModInfo)
                .map(IModInfo::getVersion)
                .map(ArtifactVersion::toString)
                .orElse("UNKNOWN");
        IASMinecraft.init(FMLPaths.GAMEDIR.get(), FMLPaths.CONFIGDIR.get(), "NeoForge", modVersion, loaderVersion);
    }

    // Register closer.
    @SubscribeEvent
    public void onShutDown(GameShuttingDownEvent event) {
        // Close.
        IASMinecraft.close(Minecraft.getInstance());
    }

    // Register screen handlers.
    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event) {
        // Init.
        Screen screen = event.getScreen();
        IASMinecraft.onInit(screen.getMinecraft(), screen, event::addListener);
    }

    // Register drawer.
    @SubscribeEvent
    public void onScreenDraw(ScreenEvent.Render.Post event) {
        // Draw.
        Screen screen = event.getScreen();
        IASMinecraft.onDraw(screen, screen.getMinecraft().font, event.getGuiGraphics());
    }
}
