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

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.telemetry.ClientTelemetryManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.auth.LoginData;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.mixins.MinecraftAccessor;
import ru.vidtu.ias.screen.AccountScreen;
import ru.vidtu.ias.utils.Expression;
import ru.vidtu.ias.utils.IUtils;
import ru.vidtu.ias.utils.exceptions.FriendlyException;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Main IAS class for Minecraft.
 *
 * @author VidTu
 */
public final class IASMinecraft {
    /**
     * Toast for nick warning.
     */
    public static final SystemToast.SystemToastId NICK_WARN = new SystemToast.SystemToastId(10000L);

    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("IAS/IASMinecraft");

    /**
     * Button widget sprites.
     */
    private static final WidgetSprites BUTTON = new WidgetSprites(
            new ResourceLocation("ias", "button_plain"),
            new ResourceLocation("ias", "button_disabled"),
            new ResourceLocation("ias", "button_focus")
    );

    /**
     * Text X.
     */
    private static int textX;

    /**
     * Text Y.
     */
    private static int textY;

    /**
     * Current text.
     */
    private static Component text = Component.translatable("ias.title", "(not loaded for some reason)");

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private IASMinecraft() {
        throw new AssertionError("No instances.");
    }

    /**
     * Initializes the IAS.
     *
     * @param gameDir       Game directory
     * @param configDir     Config directory
     * @param loader        Loader name
     * @param loaderVersion Loader version
     * @param modVersion    Mod version
     */
    public static void init(Path gameDir, Path configDir, String loader, String modVersion, String loaderVersion) {
        // Log the info.
        String gameVersion = SharedConstants.getCurrentVersion().getName();
        LOGGER.info("IAS: Booting up... (version: {}, loader: {}, loader version: {}, game version: {})", modVersion, loader, loaderVersion, gameVersion);

        // Initialize the IAS.
        IAS.init(gameDir, configDir, modVersion, loader, loaderVersion, gameVersion);
    }

    /**
     * Called on title screen initialization.
     *
     * @param minecraft   Minecraft instance
     * @param screen      Target screen
     * @param buttonAdder Adder function
     */
    @SuppressWarnings({"ChainOfInstanceofChecks", "ConstantValue"}) // <- Abstraction for Minecraft is not possible, mods break user non-nullness.
    public static void onInit(Minecraft minecraft, Screen screen, Consumer<Button> buttonAdder) {
        // Add title button.
        if (IASConfig.titleButton && screen instanceof TitleScreen) {
            // Calculate the position.
            int width = screen.width;
            int height = screen.height;
            Integer x = Expression.parsePosition(IASConfig.titleButtonX, width, height);
            Integer y = Expression.parsePosition(IASConfig.titleButtonY, width, height);

            // Couldn't parse position.
            if (x == null || y == null) {
                // Use default position.
                x = width / 2 + 104;
                y = height / 4 + 72;

                // Move out of any overlapping elements.
                for (int i = 0; i < 64; i++) {
                    boolean overlapping = false;
                    for (GuiEventListener child : screen.children()) {
                        // Skip if doesn't have pos.
                        if (!(child instanceof LayoutElement le) || child instanceof AbstractSelectionList<?>) continue;

                        // Skip if not overlapping.
                        int x1 = le.getX() - 4;
                        int y1 = le.getY() - 4;
                        int x2 = x1 + le.getWidth() + 8;
                        int y2 = y1 + le.getHeight() + 8;
                        if (x < x1 || y < y1 || (x + 20) > x2 || (y + 20) > y2) continue;

                        // Otherwise move.
                        x = Math.max(x, x2);
                        overlapping = true;
                    }
                    if (overlapping) continue;
                    break;
                }
            }

            // Add the button.
            ImageButton button = new ImageButton(x, y, 20, 20, BUTTON, btn -> minecraft.setScreen(new AccountScreen(screen)), Component.literal("In-Game Account Switcher"));
            button.setTooltip(Tooltip.create(button.getMessage()));
            button.setTooltipDelay(250);
            buttonAdder.accept(button);
        }

        // Add servers button.
        if (IASConfig.serversButton && screen instanceof JoinMultiplayerScreen) {
            // Calculate the position.
            int width = screen.width;
            int height = screen.height;
            Integer x = Expression.parsePosition(IASConfig.serversButtonX, width, height);
            Integer y = Expression.parsePosition(IASConfig.serversButtonY, width, height);

            // Couldn't parse position.
            if (x == null || y == null) {
                // Use default position.
                x = width / 2 + 158;
                y = height - 30;

                // Move out of any overlapping elements.
                for (int i = 0; i < 64; i++) {
                    boolean overlapping = false;
                    for (GuiEventListener child : screen.children()) {
                        // Skip if doesn't have pos.
                        if (!(child instanceof LayoutElement le) || child instanceof AbstractSelectionList<?>) continue;

                        // Skip if not overlapping.
                        int x1 = le.getX() - 4;
                        int y1 = le.getY() - 4;
                        int x2 = x1 + le.getWidth() + 8;
                        int y2 = y1 + le.getHeight() + 8;
                        if (x < x1 || y < y1 || (x + 20) > x2 || (y + 20) > y2) continue;

                        // Otherwise move.
                        x = Math.max(x, x2);
                        overlapping = true;
                    }
                    if (overlapping) continue;
                    break;
                }
            }

            // Add the button.
            ImageButton button = new ImageButton(x, y, 20, 20, BUTTON, btn -> minecraft.setScreen(new AccountScreen(screen)), Component.literal("In-Game Account Switcher"));
            button.setTooltip(Tooltip.create(button.getMessage()));
            button.setTooltipDelay(250);
            buttonAdder.accept(button);
        }

        // Add title text.
        if (IASConfig.titleText && screen instanceof TitleScreen) {
            // Calculate the position.
            int width = screen.width;
            int height = screen.height;
            Integer cx = Expression.parsePosition(IASConfig.titleTextX, width, height);
            Integer cy = Expression.parsePosition(IASConfig.titleTextY, width, height);
            Font font = minecraft.font;
            User user = minecraft.getUser();
            text = Component.translatable("ias.title", user != null ? user.getName() : "(broken by mods)");
            textX = cx == null || cy == null ? (width - font.width(text)) / 2 : switch (IASConfig.titleTextAlign) {
                case LEFT -> cx;
                case CENTER -> cx - font.width(text) / 2;
                case RIGHT -> cx - font.width(text);
            };
            textY = cx == null || cy == null ? height / 4 + 164 : cy;
        }

        // Add servers text.
        if (IASConfig.serversText && screen instanceof JoinMultiplayerScreen) {
            // Calculate the position.
            int width = screen.width;
            int height = screen.height;
            Integer cx = Expression.parsePosition(IASConfig.serversTextX, width, height);
            Integer cy = Expression.parsePosition(IASConfig.serversTextY, width, height);
            Font font = minecraft.font;
            User user = minecraft.getUser();
            text = Component.translatable("ias.title", user != null ? user.getName() : "(broken by mods)");
            textX = cx == null || cy == null ? (width - font.width(text)) / 2 : switch (IASConfig.serversTextAlign) {
                case LEFT -> cx;
                case CENTER -> cx - font.width(text) / 2;
                case RIGHT -> cx - font.width(text);
            };
            textY = cx == null || cy == null ? 5 : cy;
        }

        // Warn about invalid names.
        if (!IASConfig.nickWarns || !(screen instanceof ConnectScreen) || minecraft.getToasts().getToast(SystemToast.class, NICK_WARN) != null) return;
        User user = minecraft.getUser();
        // Mods break non-nullness.
        //noinspection ConstantValue
        String name = user != null ? user.getName() : "";
        String key = IUtils.warnKey(name);
        if (key == null) return;

        // Display the toast.
        ToastComponent toasts = minecraft.getToasts();
        toasts.addToast(SystemToast.multiline(minecraft, NICK_WARN, Component.literal("In-Game Account Switcher"), Component.translatable(key, name)));
    }

    /**
     * Called on title screen drawing.
     *
     * @param screen   Target screen
     * @param font     Screen font
     * @param graphics Drawing graphics
     */
    @SuppressWarnings("ChainOfInstanceofChecks") // <- Abstraction for Minecraft is not possible.
    public static void onDraw(Screen screen, Font font, GuiGraphics graphics) {
        if (IASConfig.titleText && screen instanceof TitleScreen) {
            graphics.drawString(font, text, textX, textY, 0xFF_CC_88_88);
        }
        if (IASConfig.serversText && screen instanceof JoinMultiplayerScreen) {
            graphics.drawString(font, text, textX, textY, 0xFF_CC_88_88);
        }
    }

    /**
     * Logins into the minecraft.
     * Can be called from any thread.
     *
     * @param minecraft Minecraft instance
     * @param data      Login data
     * @return Future for logging in
     */
    public static CompletableFuture<Void> account(Minecraft minecraft, LoginData data) {
        // Check if not in-game.
        LOGGER.info("IAS: Received login request: {}", data);
        if (minecraft.player != null || minecraft.level != null || minecraft.getConnection() != null ||
                minecraft.cameraEntity != null || minecraft.gameMode != null || minecraft.isSingleplayer()) {
            return CompletableFuture.failedFuture(new FriendlyException("Changing accounts in world.", "ias.error.world"));
        }

        // Create everything async, because it lags.
        return CompletableFuture.runAsync(() -> {
            // Create the user.
            LOGGER.info("IAS: Creating user...");
            // I have no idea what are the OPTIONAL fields and the game
            // works FINE without them, even with chat reporting and parental control, etc.
            // etc., it may be some telemetry, it may be something else. If something is broken by this
            // feel free to submit an issue, if someone knows what this is, feel free to PR a fix,
            // I'm too lazy to fix anything related to telemetry or chat signatures/reports.
            boolean online = data.online();
            User.Type type = online ? User.Type.MSA : User.Type.LEGACY;
            User user = new User(data.name(), data.uuid(), data.token(), Optional.empty(), Optional.empty(), type);

            // Create various services.
            CompletableFuture<ProfileResult> profile = CompletableFuture.completedFuture(online ? minecraft.getMinecraftSessionService().fetchProfile(data.uuid(), true) : null);
            @SuppressWarnings("CastToIncompatibleInterface") // <- Mixin Accessor.
            MinecraftAccessor accessor = (MinecraftAccessor) minecraft;
            UserApiService apiService = online ? accessor.ias$authenticationService().createUserApiService(data.token()) : UserApiService.OFFLINE;
            UserApiService.UserProperties properties;
            try {
                properties = apiService.fetchProperties();
            } catch (Throwable ignored) {
                properties = UserApiService.OFFLINE_PROPERTIES;
            }
            CompletableFuture<UserApiService.UserProperties> propertiesFuture = CompletableFuture.completedFuture(properties);
            PlayerSocialManager social = new PlayerSocialManager(minecraft, apiService);
            ClientTelemetryManager telemetry = new ClientTelemetryManager(minecraft, apiService, user);
            ProfileKeyPairManager keyPair = ProfileKeyPairManager.create(apiService, user, minecraft.gameDirectory.toPath());
            ReportingContext reporting = ReportingContext.create(ReportEnvironment.local(), apiService);

            // Schedule to the main thread
            minecraft.execute(() -> {
                // Flush everything.
                LOGGER.info("IAS: Flushing user...");
                accessor.ias$user(user);
                accessor.ias$profileFuture(profile);
                accessor.ias$userApiService(apiService);
                accessor.ias$userPropertiesFuture(propertiesFuture);
                accessor.ias$playerSocialManager(social);
                accessor.ias$telemetryManager(telemetry);
                accessor.ias$profileKeyPairManager(keyPair);
                accessor.ias$reportingContext(reporting);
                minecraft.updateTitle();
                LOGGER.info("IAS: Flushed user.");
            });
        }, IAS.executor()).exceptionally(t -> {
            // Log it.
            LOGGER.error("IAS: Unable to log in: {}.", data, t);

            // Rethrow.
            throw new RuntimeException("Unable to change account to: " + data, t);
        });
    }
}
