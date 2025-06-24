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

//? if forge {
/*package ru.vidtu.ias.platform;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.IASMinecraft;
import ru.vidtu.ias.screen.ConfigScreen;

/^*
 * Main IAS class for Forge.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see IAS
 ^/
@ApiStatus.Internal
@Mod("ias")
@NullMarked
public final class IForge {
    /^*
     * Logger for this class.
     ^/
    private static final Logger LOGGER = LogManager.getLogger("IAS/IForge");

    //? if hackyNeoForge {
    /^/^¹*
     * Creates and loads a new mod.
     *
     * @param container Current mod container
     * @param bus       Loading event bus
     ¹^/
    public IForge(net.minecraftforge.fml.ModContainer container, net.minecraftforge.eventbus.api.IEventBus bus) {
        // Validate.
        assert container != null : "IAS: Parameter 'container' is null. (bus: " + bus + ", mod: " + this + ')';
        assert bus != null : "IAS: Parameter 'bus' is null. (container: " + container + ", mod: " + this + ')';
    ^///?} else if >=1.19.2 && (!1.20.2) {
    /^*
     * Creates and loads a new mod.
     *
     * @param ctx Loading context
     ^/
    public IForge(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext ctx) {
        // Validate.
        assert ctx != null : "IAS: Parameter 'ctx' is null. (mod: " + this + ')';
    //?} else {
    /^/^¹*
     * Creates a new mod.
     ¹^/
    public IForge() {
    ^///?}
        // Log.
        long start = System.nanoTime();
        LOGGER.info("IAS: Loading... (platform: forge)");

        // Not sure how long the Forge does have the "clientSideOnly" field in the TOML,
        // so I'll do an additional exception check here.
        if (FMLEnvironment.dist != Dist.CLIENT) {
            throw new UnsupportedOperationException("IAS: You've tried to load the In-Game Account Switcher mod on a server. This won't work.");
        }

        // Register the handlers.
        //? if >=1.21.6 {
        ScreenEvent.Init.Post.BUS.addListener(event -> {
            Screen screen = event.getScreen();
            IASMinecraft.onInit(screen.getMinecraft(), screen, event::addListener);
        });
        ScreenEvent.Render.Post.BUS.addListener(event -> {
            Screen screen = event.getScreen();
            IASMinecraft.onDraw(screen, screen.getMinecraft().font, event.getGuiGraphics());
        });
        GameShuttingDownEvent.BUS.addListener(event -> IAS.close());
        //?} else {
        /^net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((ScreenEvent.Init.Post event) -> {
            Screen screen = event.getScreen();
            IASMinecraft.onInit(screen.getMinecraft(), screen, event::addListener);
        });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((ScreenEvent.Render.Post event) -> {
            Screen screen = event.getScreen();
            IASMinecraft.onDraw(screen, screen.getMinecraft().font, event.getGuiGraphics());
        });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((GameShuttingDownEvent event) -> IAS.close());
        ^///?}

        // Register the config screen.
        //? if hackyNeoForge {
        /^container.registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class, () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen)));
        container.registerExtensionPoint(net.minecraftforge.fml.IExtensionPoint.DisplayTest.class, () -> new net.minecraftforge.fml.IExtensionPoint.DisplayTest(() -> net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORESERVERONLY, (version, fromServer) -> true));
        ^///?} else if 1.20.2 {
        /^net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class, () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen)));
        net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.fml.IExtensionPoint.DisplayTest.class, () -> new net.minecraftforge.fml.IExtensionPoint.DisplayTest(() -> net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORESERVERONLY, (version, fromServer) -> true));
        ^///?} else if >=1.19.2 {
        ctx.registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class, () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory(ConfigScreen::new));
        ctx.registerDisplayTest(net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORE_SERVER_VERSION);
        //?} else {
        /^net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory.class, () -> new net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory(ConfigScreen::new));
        net.minecraftforge.fml.ModLoadingContext.get().registerDisplayTest(net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORE_SERVER_VERSION);
        ^///?}

        // Create the UA and initialize.
        IAS.init();
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "IAS/IForge{}";
    }
}
*///?}
