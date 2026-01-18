/*
 * In-Game Account Switcher is a third-party mod for Minecraft Java Edition that
 * allows you to change your logged in account in-game, without restarting it.
 *
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2026 VidTu
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
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

    //? if hacky_neoforge {
    /^/^¹*
     * Creates and loads a new mod.
     *
     * @param container Current mod container
     * @param bus       Loading event bus
     * @apiNote Do not call, called by Forge
     ¹^/
    public IForge(net.minecraftforge.fml.ModContainer container, net.minecraftforge.eventbus.api.IEventBus bus) {
        // Validate.
        assert container != null : "IAS: Parameter 'container' is null. (bus: " + bus + ", mod: " + this + ')';
        assert bus != null : "IAS: Parameter 'bus' is null. (container: " + container + ", mod: " + this + ')';
    ^///?} elif >=1.19.2 && (!1.20.2) {
    /^*
     * Creates and loads a new mod.
     *
     * @param ctx Loading context
     * @apiNote Do not call, called by Forge
     ^/
    public IForge(FMLJavaModLoadingContext ctx) {
        // Validate.
        assert ctx != null : "IAS: Parameter 'ctx' is null. (mod: " + this + ')';
    //?} else {
    /^/^¹*
     * Creates a new mod.
     *
     * @apiNote Do not call, called by Forge
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

        // Init the mod.
        IASMinecraft.init();

        // Register the shutdown handler.
        GameShuttingDownEvent.BUS.addListener(event -> IAS.close());

        // Register screen handlers.
        ScreenEvent.Init.Post.BUS.addListener(event -> {
            Screen screen = event.getScreen();
            IASMinecraft.onInit(screen.getMinecraft(), screen, event::addListener);
        });
        ScreenEvent.Render.Post.BUS.addListener(event -> {
            Screen screen = event.getScreen();
            IASMinecraft.onDraw(screen, screen.getFont(), event.getGuiGraphics());
        });

        // Register the config screen.
        //? if hacky_neoforge {
        /^container.registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class, () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen)));
        container.registerExtensionPoint(net.minecraftforge.fml.IExtensionPoint.DisplayTest.class, () -> new net.minecraftforge.fml.IExtensionPoint.DisplayTest(() -> net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORESERVERONLY, (version, fromServer) -> true));
        ^///?} elif 1.20.2 {
        /^net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class, () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen)));
        net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.fml.IExtensionPoint.DisplayTest.class, () -> new net.minecraftforge.fml.IExtensionPoint.DisplayTest(() -> net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORESERVERONLY, (version, fromServer) -> true));
        ^///?} elif >=1.19.2 {
        ctx.registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class, () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory(ConfigScreen::new));
        ctx.registerDisplayTest(net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORE_SERVER_VERSION);
        //?} elif >=1.18.2 {
        /^net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory.class, () -> new net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory(ConfigScreen::new));
        net.minecraftforge.fml.ModLoadingContext.get().registerDisplayTest(net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORE_SERVER_VERSION);
        ^///?} elif >=1.17.1 {
        /^net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.fmlclient.ConfigGuiHandler.ConfigGuiFactory.class, () -> new net.minecraftforge.fmlclient.ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new ConfigScreen(screen)));
        net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.fml.IExtensionPoint.DisplayTest.class, () -> new net.minecraftforge.fml.IExtensionPoint.DisplayTest(() -> net.minecraftforge.fmllegacy.network.FMLNetworkConstants.IGNORESERVERONLY, (version, fromServer) -> true));
        ^///?} else {
        /^net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.fml.ExtensionPoint.CONFIGGUIFACTORY, () -> (minecraft, screen) -> new ConfigScreen(screen));
        net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.fml.ExtensionPoint.DISPLAYTEST, () -> org.apache.commons.lang3.tuple.Pair.of(() -> net.minecraftforge.fml.network.FMLNetworkConstants.IGNORESERVERONLY, (version, fromServer) -> true));
        ^///?}

        // Done.
        LOGGER.info("IAS: Loaded. ({} ms)", (System.nanoTime() - start) / 1_000_000L);
    }

    // TODO(VidTu): Remove when migration finishes.
    // MIGRATION NOTICE: This class doesn't support annotation-based events, because I don't like them.

    @Contract(pure = true)
    @Override
    public String toString() {
        return "IAS/IForge{}";
    }
}
*///?}
