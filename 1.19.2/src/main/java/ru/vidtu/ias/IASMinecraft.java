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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.auth.LoginData;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.legacy.LegacyTooltip;
import ru.vidtu.ias.mixins.MinecraftAccessor;
import ru.vidtu.ias.screen.AccountScreen;
import ru.vidtu.ias.utils.Expression;
import ru.vidtu.ias.utils.IUtils;

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
    public static final SystemToast.SystemToastIds NICK_WARN = SystemToast.SystemToastIds.UNSECURE_SERVER_WARNING;

    /**
     * Main mod UI sprite.
     */
    public static final ResourceLocation SPRITE = new ResourceLocation("ias", "textures/gui/sprite.png");

    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("IAS/IASMinecraft");

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
     * Closes the IAS.
     *
     * @param minecraft Minecraft instance4
     */
    public static void close(@NotNull Minecraft minecraft) {
        // Set screen.
        Screen prevScreen = minecraft.screen;
        minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("ias.closing")));

        // Unload.
        IAS.close();

        // Unset screen.
        minecraft.forceSetScreen(prevScreen);
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
                        if (!(child instanceof AbstractWidget w) || child instanceof AbstractSelectionList<?>) continue;

                        // Skip if not overlapping.
                        int x1 = w.x - 4;
                        int y1 = w.y - 4;
                        int x2 = x1 + w.getWidth() + 8;
                        int y2 = y1 + w.getHeight() + 8;
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
            ImageButton button = new ImageButton(x, y, 20, 20, 0, 0, 20, SPRITE, 256, 256, btn -> minecraft.setScreen(new AccountScreen(screen)), new LegacyTooltip(screen, minecraft.font, Component.literal("In-Game Account Switcher"), 250), Component.literal("In-Game Account Switcher"));
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
                        if (!(child instanceof AbstractWidget w) || child instanceof AbstractSelectionList<?>) continue;

                        // Skip if not overlapping.
                        int x1 = w.x - 4;
                        int y1 = w.y - 4;
                        int x2 = x1 + w.getWidth() + 8;
                        int y2 = y1 + w.getHeight() + 8;
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
            ImageButton button = new ImageButton(x, y, 20, 20, 0, 0, 20, SPRITE, 256, 256, btn -> minecraft.setScreen(new AccountScreen(screen)), new LegacyTooltip(screen, minecraft.font, Component.literal("In-Game Account Switcher"), 250), Component.literal("In-Game Account Switcher"));
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
     * @param screen Target screen
     * @param font   Screen font
     * @param pose   Drawing stack
     */
    @SuppressWarnings("ChainOfInstanceofChecks") // <- Abstraction for Minecraft is not possible.
    public static void onDraw(Screen screen, Font font, PoseStack pose) {
        if (IASConfig.titleText && screen instanceof TitleScreen) {
            font.drawShadow(pose, text, textX, textY, 0xFF_CC_88_88);
        }
        if (IASConfig.serversText && screen instanceof JoinMultiplayerScreen) {
            font.drawShadow(pose, text, textX, textY, 0xFF_CC_88_88);
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
                minecraft.cameraEntity != null || minecraft.gameMode != null || minecraft.hasSingleplayerServer()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Changing accounts in world."));
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
            User user = new User(data.name(), data.uuid().toString(), data.token(), Optional.empty(), Optional.empty(), type);

            // Create various services.
            @SuppressWarnings("CastToIncompatibleInterface") // <- Mixin Accessor.
            MinecraftAccessor accessor = (MinecraftAccessor) minecraft;
            UserApiService apiService;
            if (online) {
                try {
                    apiService = accessor.ias$authenticationService().createUserApiService(data.token());
                } catch (Throwable t) {
                    // Rethrow.
                    throw new RuntimeException("Unable to create user API service.", t);
                }
            } else {
                apiService = UserApiService.OFFLINE;
            }
            PlayerSocialManager social = new PlayerSocialManager(minecraft, apiService);
            ProfileKeyPairManager keyPair = new ProfileKeyPairManager(apiService, user.getProfileId(), minecraft.gameDirectory.toPath());
            ReportingContext reporting = ReportingContext.create(ReportEnvironment.local(), apiService);
            GameProfile gameProfile = minecraft.getMinecraftSessionService().fillProfileProperties(user.getGameProfile(), false);
            PropertyMap propertyMap = new PropertyMap();
            propertyMap.putAll(gameProfile.getProperties());

            // Schedule to the main thread
            minecraft.execute(() -> {
                // Flush everything.
                LOGGER.info("IAS: Flushing user...");
                accessor.ias$user(user);
                accessor.ias$userApiService(apiService);
                accessor.ias$profileProperties(propertyMap);
                accessor.ias$playerSocialManager(social);
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
