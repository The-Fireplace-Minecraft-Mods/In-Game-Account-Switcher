/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
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

package ru.vidtu.ias.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.MicrosoftAccount;
import ru.vidtu.ias.auth.handlers.CreateHandler;
import ru.vidtu.ias.auth.microsoft.MSAuthClient;
import ru.vidtu.ias.auth.microsoft.MSAuthServer;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.crypt.Crypt;
import ru.vidtu.ias.crypt.PasswordCrypt;
import ru.vidtu.ias.legacy.LastPassRenderCallback;
import ru.vidtu.ias.legacy.LegacyTooltip;
import ru.vidtu.ias.utils.exceptions.FriendlyException;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Microsoft popup screen.
 *
 * @author VidTu
 */
final class MicrosoftPopupScreen extends Screen implements CreateHandler, LastPassRenderCallback {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("IAS/MicrosoftPopupScreen");

    /**
     * Parent screen.
     */
    private final Screen parent;

    /**
     * Last pass callbacks list.
     */
    private final List<Runnable> lastPass = new LinkedList<>();

    /**
     * Synchronization lock.
     */
    private final Object lock = new Object();

    /**
     * Account handler.
     */
    private final Consumer<Account> handler;

    /**
     * Crypt method.
     */
    private Crypt crypt;

    /**
     * MS auth client.
     */
    private MSAuthClient client;

    /**
     * MS auth server.
     */
    private MSAuthServer server;

    /**
     * Current stage.
     */
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized") // <- toString()
    private Component stage = Component.translatable(MicrosoftAccount.INITIALIZING).withStyle(ChatFormatting.YELLOW);

    /**
     * Current stage label.
     */
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized") // <- toString()
    private MultiLineLabel label;

    /**
     * Password box.
     */
    private PopupBox password;

    /**
     * Crypt password tip.
     */
    private MultiLineLabel cryptPasswordTip;

    /**
     * Non-NAN, if some sort of error is present.
     */
    private float error = Float.NaN;

    /**
     * Error note.
     */
    private MultiLineLabel errorNote;

    /**
     * Creates a new login screen.
     *
     * @param parent  Parent screen
     * @param handler Account handler
     * @param crypt   Crypt method, {@code null} to use password
     */
    MicrosoftPopupScreen(Screen parent, Consumer<Account> handler, Crypt crypt) {
        super(Component.translatable("ias.login"));
        this.parent = parent;
        this.handler = handler;
        this.crypt = crypt;
    }

    @Override
    public boolean cancelled() {
        // Bruh.
        assert this.minecraft != null;

        // Cancelled if no longer displayed.
        return this != this.minecraft.screen;
    }

    @Override
    protected void init() {
        // Bruh.
        assert this.minecraft != null;

        // Synchronize to prevent funny things.
        synchronized (this.lock) {
            // Unbake label.
            this.label = null;
        }

        // Init parent.
        if (this.parent != null) {
            this.parent.init(this.minecraft, this.width, this.height);
        }

        // Add back button.
        this.addRenderableWidget(new PopupButton(this.width / 2 - 75, this.height / 2 + 74 - 22, 150, 20,
                CommonComponents.GUI_BACK, btn -> this.onClose(), LegacyTooltip.EMPTY));

        // Add password box, if future exists.
        if (this.crypt == null) {
            // Add password box.
            this.password = new PopupBox(this.font, this.width / 2 - 100, this.height / 2 - 10 + 5, 178, 20, this.password, Component.translatable("ias.password"), () -> {
                // Prevent NPE, just in case.
                if (this.password == null || this.crypt != null) return;
                String value = this.password.getValue();
                if (value.isBlank()) return;

                // Complete the future.
                this.crypt = new PasswordCrypt(value);
                this.password = null;
                this.cryptPasswordTip = null;

                // Rebuild the UI.
                this.init(this.minecraft, this.width, this.height);
            }, true, Component.translatable("ias.password.hint").withStyle(ChatFormatting.DARK_GRAY));
            this.password.setFormatter((s, i) -> IASConfig.passwordEchoing ? FormattedCharSequence.forward("*".repeat(s.length()), Style.EMPTY) : FormattedCharSequence.EMPTY);
            this.password.setMaxLength(32);
            this.addRenderableWidget(this.password);

            // Add enter password button.
            Button enterPassword = new PopupButton(this.width / 2 - 100 + 180, this.height / 2 - 10 + 5, 20, 20, Component.literal(">>"), btn -> {
                // Prevent NPE, just in case.
                if (this.password == null || this.crypt != null) return;
                String value = this.password.getValue();
                if (value.isBlank()) return;

                // Complete the future.
                this.crypt = new PasswordCrypt(value);
                this.password = null;
                this.cryptPasswordTip = null;

                // Rebuild the UI.
                this.init(this.minecraft, this.width, this.height);
            }, LegacyTooltip.EMPTY);
            enterPassword.active = !this.password.getValue().isBlank();
            this.addRenderableWidget(enterPassword);
            this.password.setResponder(value -> enterPassword.active = !value.isBlank());

            // Create tip.
            this.cryptPasswordTip = MultiLineLabel.create(this.font, Component.translatable("ias.password.tip"), 320);
        }

        // Try to open the server.
        IAS.executor().execute(() -> {
            if (IASConfig.useServerAuth()) {
                this.server();
            } else {
                this.client();
            }
        });
    }

    /**
     * Creates the client.
     */
    private void client() {
        try {
            // Bruh.
            assert this.minecraft != null;

            // Skip if can't.
            if (this.crypt == null || this.server != null || this.client != null) return;

            // Create the server.
            this.client = new MSAuthClient(this.crypt, this);

            // Run the client.
            this.client.start().thenAcceptAsync(auth -> {
                // Log it and display progress.
                LOGGER.info("IAS: Opening client link...");
                this.stage(MicrosoftAccount.CLIENT_BROWSER,
                        Component.literal(auth.uri().toString()).withStyle(ChatFormatting.GOLD),
                        Component.literal(auth.user()).withStyle(ChatFormatting.GOLD));

                // Copy and open link.
                Util.getPlatform().openUri(auth.uri().toString());
                this.minecraft.keyboardHandler.setClipboard(auth.user());
            }, this.minecraft).exceptionally(t -> {
                // Handle error.
                this.error(new RuntimeException("Unable to handle client.", t));
                return null;
            });
        } catch (Throwable t) {
            // Handle error.
            this.error(new RuntimeException("Unable to create client.", t));
        }
    }

    /**
     * Creates the server.
     */
    private void server() {
        try {
            // Bruh.
            assert this.minecraft != null;

            // Skip if can't.
            if (this.crypt == null || this.server != null || this.client != null) return;

            // Create the server.
            this.server = new MSAuthServer(I18n.get("ias.login.done"), this.crypt, this);

            // Run the server.
            CompletableFuture.runAsync(() -> {
                // Run the server.
                this.server.run();
            }, IAS.executor()).thenRunAsync(() -> {
                // Log it and display progress.
                LOGGER.info("IAS: Opening server link...");
                this.stage(MicrosoftAccount.BROWSER);

                // Copy and open link.
                String url = this.server.authUrl();
                Util.getPlatform().openUri(url);
                this.minecraft.keyboardHandler.setClipboard(url);
            }, this.minecraft).exceptionally(t -> {
                // Handle error.
                this.error(new RuntimeException("Unable to handle server.", t));
                return null;
            });
        } catch (Throwable t) {
            // Handle error.
            this.error(new RuntimeException("Unable to create server.", t));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.password == null) return;
        this.password.tick();
    }

    @Override
    public void onClose() {
        // Bruh.
        assert this.minecraft != null;

        // Close to parent.
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void removed() {
        // Bruh.
        assert this.minecraft != null;

        // Check if IAS ID is in clipboard.
        KeyboardHandler keyboard = this.minecraft.keyboardHandler;
        String clipboard = keyboard.getClipboard();

        // Null if it is.
        if (clipboard.toLowerCase(Locale.ROOT).contains(IAS.CLIENT_ID.toLowerCase(Locale.ROOT))) {
            keyboard.setClipboard(" ");
        }

        // Close off-thread.
        IAS.executor().execute(() -> {
            // Close the client, if any.
            if (this.client != null) {
                this.client.close();
                this.client = null;
            }

            // Close the server, if any.
            if (this.server != null) {
                this.server.close();
                this.server = null;
            }
        });
    }

    @SuppressWarnings("NonPrivateFieldAccessedInSynchronizedContext") // <- Supertype.
    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float delta) {
        // Bruh.
        assert this.minecraft != null;

        // Render parent behind.
        if (this.parent != null) {
            pose.pushPose();
            pose.translate(0.0F, 0.0F, -500.0F);
            this.parent.render(pose, 0, 0, delta);
            pose.popPose();
        }

        // Render background and widgets.
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, delta);

        // Render the title.
        pose.pushPose();
        pose.scale(2.0F, 2.0F, 2.0F);
        drawCenteredString(pose, this.font, this.title, this.width / 4, this.height / 4 - 74 / 2, 0xFF_FF_FF_FF);
        pose.popPose();

        // Render password OR label.
        if (this.crypt == null && this.password != null && this.cryptPasswordTip != null) {
            drawCenteredString(pose, this.font, this.password.getMessage(), this.width / 2, this.height / 2 - 10 - 5, 0xFF_FF_FF_FF);
            pose.pushPose();
            pose.scale(0.5F, 0.5F, 0.5F);
            this.cryptPasswordTip.renderCentered(pose, this.width, this.height + 40, 10, 0xFF_FF_FF_00);
            pose.popPose();
        } else {
            // Synchronize to prevent funny things.
            synchronized (this.lock) {
                // Label is unbaked.
                if (this.label == null) {
                    // Get the component.
                    Component component = Objects.requireNonNullElse(this.stage, Component.empty());

                    // Bake the label.
                    this.label = MultiLineLabel.create(this.font, component, 240);

                    // Narrate.
                    this.minecraft.getNarrator().sayNow(component);
                }

                // Render the label.
                this.label.renderCentered(pose, this.width / 2, (this.height - this.label.getLineCount() * 9) / 2 - 4, 9, 0xFF_FF_FF_FF);
            }

            // Render the error note, if errored.
            if (Float.isFinite(this.error)) {
                // Create it first.
                if (this.errorNote == null) {
                    this.errorNote = MultiLineLabel.create(this.font, Component.translatable("ias.error.note").withStyle(ChatFormatting.AQUA), 245);
                }

                // Wow, opacity. So fluent.
                // For what purpose?
                int opacityMask;
                if (this.error < 1.0F) {
                    this.error = Math.min(this.error + delta * 0.1F, 1.0F);
                    int opacity = Math.max(9, (int) (this.error * this.error * this.error * this.error * 255.0F));
                    opacityMask = opacity << 24;
                } else {
                    opacityMask = -16777216;
                }

                // Render BG.
                int w = this.errorNote.getWidth() / 4 + 2;
                int h = (this.errorNote.getLineCount() * 9) / 2 + 1;
                int cx = this.width / 2;
                int sy = this.height / 2 + 87;
                fill(pose, cx - w, sy, cx + w, sy + h, 0x101010 | opacityMask);
                fill(pose, cx - w + 1, sy - 1, cx + w - 1, sy, 0x101010 | opacityMask);
                fill(pose, cx - w + 1, sy + h, cx + w - 1, sy + h + 1, 0x101010 | opacityMask);

                // Render scaled.
                pose.pushPose();
                pose.scale(0.5F, 0.5F, 0.5F);
                this.errorNote.renderCentered(pose, this.width, this.height + 174, 9, 0xFF_FF_FF | opacityMask);
                pose.popPose();
            }
        }

        // Last pass.
        for (Runnable callback : this.lastPass) {
            callback.run();
        }
        this.lastPass.clear();
    }

    @Override
    public void renderBackground(PoseStack pose) {
        // Bruh.
        assert this.minecraft != null;

        // Render transparent background if parent exists.
        if (this.parent != null) {
            // Render gradient.
            fill(pose, 0, 0, this.width, this.height, 0x80_00_00_00);
        } else {
            super.renderBackground(pose);
        }

        // Render "form".
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        fill(pose, centerX - 125, centerY - 75, centerX + 125, centerY + 75, 0xF8_20_20_30);
        fill(pose, centerX - 124, centerY - 76, centerX + 124, centerY - 75, 0xF8_20_20_30);
        fill(pose, centerX - 124, centerY + 75, centerX + 124, centerY + 76, 0xF8_20_20_30);
    }

    @Override
    public void stage(String stage, Object... args) {
        // Bruh.
        assert this.minecraft != null;

        // Skip if not current screen.
        if (this != this.minecraft.screen) return;

        // Try to focus.
        if (MicrosoftAccount.PROCESSING.equals(stage)) {
            this.minecraft.execute(() -> {
                try {
                    long ptr = this.minecraft.getWindow().getWindow();
                    GLFW.glfwRequestWindowAttention(ptr);
                    GLFW.glfwFocusWindow(ptr);
                    GLFW.glfwRequestWindowAttention(ptr);
                } catch (Throwable ignored) {
                    // NO-OP
                }
            });
        }

        // Flush the stage.
        Component component = Component.translatable(stage, args).withStyle(ChatFormatting.YELLOW);
        synchronized (this.lock) {
            this.stage = component;
            this.label = null;
        }
    }

    @Override
    public void success(MicrosoftAccount account) {
        // Bruh.
        assert this.minecraft != null;

        // Skip if not current screen.
        if (this != this.minecraft.screen) return;

        // Write disclaimers.
        this.stage(MicrosoftAccount.FINALIZING);

        // Schedule on main.
        this.minecraft.execute(() -> {
            // Skip if not current screen.
            if (this != this.minecraft.screen) return;

            // Call the callback.
            this.handler.accept(account);
        });
    }

    @Override
    public void error(Throwable error) {
        // Bruh.
        assert this.minecraft != null;

        // Log it.
        LOGGER.error("IAS: Create error.", error);

        // Skip if not current screen.
        if (this != this.minecraft.screen) return;

        // Flush the stage.
        FriendlyException probable = FriendlyException.friendlyInChain(error);
        String key = probable != null ? probable.key() : "ias.error";
        Component component = Component.translatable(key).withStyle(ChatFormatting.RED);
        synchronized (this.lock) {
            this.stage = component;
            this.label = null;
            this.error = 0.0F;
        }
    }

    @Override
    public void lastPass(@NotNull Runnable callback) {
        this.lastPass.add(callback);
    }

    @Override
    public String toString() {
        return "MicrosoftPopupScreen{" +
                "crypt=" + this.crypt +
                ", client=" + this.client +
                ", server=" + this.server +
                ", stage=" + this.stage +
                ", label=" + this.label +
                '}';
    }
}
