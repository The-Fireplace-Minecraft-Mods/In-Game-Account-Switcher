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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GameShuttingDownEvent;
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
        // Check for plain Forge.
        boolean allowOnForge = Boolean.getBoolean("ias.allowNeoForgeVersionOnForge");
        if (!allowOnForge) {
            String forgeModName = ModList.get().getModContainerById("forge")
                    .map(ModContainer::getModInfo)
                    .map(IModInfo::getDisplayName)
                    .orElse("UNKNOWN");
            boolean isProbablyMcForge = "forge".equalsIgnoreCase(forgeModName);
            if (isProbablyMcForge) {
                throw new IllegalStateException("IAS: You've tried to use NeoForge version of the In-Game Account Switcher mod with plain Forge. This is not supported. The IAS mod has its own separate Forge version, use that one. If you still want to use NeoForge version on Forge without any support, add '-Dias.allowNeoForgeVersionOnForge=true' to your game JVM start-up flags.");
            }
        }

        // Not sure how long the Forge does have the "clientSideOnly" field in the TOML,
        // so I'll do an additional exception check here.
        if (FMLEnvironment.dist != Dist.CLIENT) {
            throw new UnsupportedOperationException("IAS: You've tried to load the In-Game Account Switcher mod on a server. This won't work.");
        }

        // Register events.
        MinecraftForge.EVENT_BUS.register(this);

        // Register various display tests and config hooks.
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (version, server) -> true));
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen)));

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
