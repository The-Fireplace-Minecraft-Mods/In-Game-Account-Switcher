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

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import ru.vidtu.ias.screen.ConfigScreen;

/**
 * Main IAS class for Forge.
 *
 * @author VidTu
 */
@Mod("ias")
public final class IASForge {
    /**
     * Creates a new mod.
     */
    @SuppressWarnings("ThisEscapedInObjectConstruction") // <- Minecraft Forge API.
    public IASForge() {
        // Not sure how long the Forge does have the "clientSideOnly" field in the TOML,
        // so I'll do an additional exception check here.
        if (FMLEnvironment.dist != Dist.CLIENT) {
            throw new UnsupportedOperationException("IAS: You've tried to load the In-Game Account Switcher mod on a server. This won't work.");
        }

        // Register events.
        MinecraftForge.EVENT_BUS.register(this);

        // Register various display tests and config hooks.
        ModLoadingContext.get().registerDisplayTest(IExtensionPoint.DisplayTest.IGNORE_ALL_VERSION);
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new ConfigScreen(screen)));

        // Create the UA and initialize.
        String modVersion = ModList.get().getModContainerById("ias")
                .map(ModContainer::getModInfo)
                .map(IModInfo::getVersion)
                .map(ArtifactVersion::toString)
                .orElse("UNKNOWN");
        String loaderVersion = ModList.get().getModContainerById("forge")
                .map(ModContainer::getModInfo)
                .map(IModInfo::getVersion)
                .map(ArtifactVersion::toString)
                .orElse("UNKNOWN");
        IASMinecraft.hackyMixinClosing = true;
        IASMinecraft.init(FMLPaths.GAMEDIR.get(), FMLPaths.CONFIGDIR.get(), "Forge", modVersion, loaderVersion);
    }

    // Register screen handlers.
    @SubscribeEvent
    public void onScreenInit(ScreenEvent.InitScreenEvent.Post event) {
        // Init.
        Screen screen = event.getScreen();
        IASMinecraft.onInit(screen.getMinecraft(), screen, event::addListener);
    }

    // Register drawer.
    @SubscribeEvent
    public void onScreenDraw(ScreenEvent.DrawScreenEvent.Post event) {
        // Draw.
        Screen screen = event.getScreen();
        IASMinecraft.onDraw(screen, screen.getMinecraft().font, event.getPoseStack());
    }
}
