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

//? if neoforge {
/*package ru.vidtu.ias.platform;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.IASMinecraft;
import ru.vidtu.ias.screen.ConfigScreen;

/^*
 * Main IAS class for NeoForge.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see IAS
 ^/
@ApiStatus.Internal
//? if >=1.20.6 {
@Mod(value = "ias", dist = Dist.CLIENT)
//?} else
/^@Mod("ias")^/
@NullMarked
public final class INeoForge {
    /^*
     * Logger for this class.
     ^/
    private static final Logger LOGGER = LogManager.getLogger("IAS/INeoForge");

    /^*
     * Creates and loads a new mod.
     *
     * @param dist      Current physical side
     * @param container Mod container
     * @param bus       Mod-specific event bus
     * @apiNote Do not call, called by NeoForge
     ^/
    public INeoForge(Dist dist, ModContainer container, IEventBus bus) {
        // Validate.
        assert dist != null : "HCsCR: Parameter 'dist' is null. (container: " + container + ", bus: " + bus + ", mod: " + this + ')';
        assert container != null : "HCsCR: Parameter 'container' is null. (dist: " + dist + ", bus: " + bus + ", mod: " + this + ')';
        assert bus != null : "HCsCR: Parameter 'bus' is null. (dist: " + dist + ", container: " + container + ", mod: " + this + ')';

        // Log.
        long start = System.nanoTime();
        LOGGER.info("IAS: Loading... (platform: neoforge)");

        // Not sure how long the Forge does have the "clientSideOnly" field in the TOML,
        // so I'll do an additional exception check here.
        if (dist != Dist.CLIENT) { // Implicit null-UOE for 'dist'
            throw new UnsupportedOperationException("IAS: You've tried to load the In-Game Account Switcher mod on a server. This won't work.");
        }

        // Register the shutdown handler.
        NeoForge.EVENT_BUS.addListener(GameShuttingDownEvent.class, event -> IAS.close());

        // Register screen handlers.
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, event -> {
            Screen screen = event.getScreen();
            IASMinecraft.onInit(screen.getMinecraft(), screen, event::addListener);
        });
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Post.class, event -> {
            Screen screen = event.getScreen();
            IASMinecraft.onDraw(screen, screen.getFont(), event.getGuiGraphics());
        });

        // Register the config screen.
        //? if >=1.20.6 {
        container.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class, (modOrGame, parent) -> new ConfigScreen(parent)); // Implicit NPE for 'container'
        //?} else {
        /^container.registerExtensionPoint(net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory.class, () -> new net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory((game, screen) -> new ConfigScreen(screen))); // Implicit NPE for 'container'
        container.registerExtensionPoint(net.neoforged.fml.IExtensionPoint.DisplayTest.class, () -> new net.neoforged.fml.IExtensionPoint.DisplayTest(() -> net.neoforged.fml.IExtensionPoint.DisplayTest.IGNORESERVERONLY, (version, fromServer) -> true));
        ^///?}

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

        // Done.
        LOGGER.info("IAS: Loaded. ({} ms)", (System.nanoTime() - start) / 1_000_000L);
    }

    // TODO(VidTu): Remove when migration finishes.
    // MIGRATION NOTICE: This class doesn't support annotation-based events, because I don't like them.

    @Contract(pure = true)
    @Override
    public String toString() {
        return "IAS/INeoForge{}";
    }
}
*///?}
