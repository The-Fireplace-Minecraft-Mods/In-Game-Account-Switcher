/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2023 VidTu
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.MicrosoftAccount;
import ru.vidtu.ias.auth.MSAuthServer;
import ru.vidtu.ias.config.IASStorage;
import ru.vidtu.ias.crypt.Crypt;
import ru.vidtu.ias.crypt.PasswordCrypt;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Microsoft popup screen.
 *
 * @author VidTu
 */
final class MicrosoftPopupScreen extends Screen implements MSAuthServer.CreateHandler {
    /**
     * Parent screen.
     */
    private final Screen parent;

    /**
     * Crypt method.
     */
    private Crypt crypt;

    /**
     * MS auth server.
     */
    private MSAuthServer server;

    /**
     * Cancel button.
     */
    private Button cancel;

    /**
     * Current stage.
     */
    private Component stage = Component.translatable(MicrosoftAccount.INITIALIZING);

    /**
     * Current stage label.
     */
    private MultiLineLabel label;

    /**
     * Password box.
     */
    private PopupBox password;

    /**
     * Enter password button
     */
    private Button enterPassword;

    /**
     * Creates a new login screen.
     *
     * @param parent Parent screen
     * @param crypt  Crypt method, {@code null} to use password
     */
    MicrosoftPopupScreen(Screen parent, Crypt crypt) {
        super(Component.translatable("ias.login"));
        this.parent = parent;
        this.crypt = crypt;
    }

    @Override
    protected void init() {
        // Bruh.
        assert this.minecraft != null;

        // Init parent.
        if (this.parent != null) {
            this.parent.init(this.minecraft, this.width, this.height);
        }

        // Add back button.
        this.cancel = new PopupButton(this.width / 2 - 75, this.height / 2 + 49 - 22, 150, 20,
                CommonComponents.GUI_BACK, button -> this.onClose(), Supplier::get);
        addRenderableWidget(this.cancel);

        // Add password box, if future exists.
        if (this.crypt == null) {
            // Add password box.
            this.password = new PopupBox(this.font, this.width / 2 - 100, this.height / 2 - 10 + 5, 178, 20, this.password, Component.translatable("ias.password"), () -> {
                // Prevent NPE, just in case.
                if (this.password == null || this.crypt != null) return;

                // Complete the future.
                this.crypt = new PasswordCrypt(this.password.getValue());
                this.password = null;
                this.enterPassword = null;

                // Rebuild the UI.
                this.init(this.minecraft, this.width, this.height);
            });
            this.password.secure = true;
            this.password.setHint(Component.translatable("ias.password.hint").withStyle(ChatFormatting.DARK_GRAY));
            this.password.setFormatter((s, i) -> FormattedCharSequence.forward("*".repeat(s.length()), Style.EMPTY));
            this.password.setMaxLength(32);
            this.addRenderableWidget(this.password);

            // Add enter password button.
            this.enterPassword = new PopupButton(this.width / 2 - 100 + 180, this.height / 2 - 10 + 5, 20, 20, Component.literal(">>"), button -> {
                // Prevent NPE, just in case.
                if (this.password == null || this.crypt != null) return;

                // Complete the future.
                this.crypt = new PasswordCrypt(this.password.getValue());
                this.password = null;
                this.enterPassword = null;

                // Rebuild the UI.
                this.init(this.minecraft, this.width, this.height);
            }, Supplier::get);
            this.addRenderableWidget(this.enterPassword);
        }

        // Try to open the server.
        this.server();
    }

    /**
     * Create the server.
     */
    private void server() {
        try {
            // Bruh.
            assert this.minecraft != null;

            // Skip if can't.
            if (this.crypt == null || this.server != null) return;

            // Create the server.
            this.server = new MSAuthServer(I18n.get("ias.microsoft.done"), this.crypt, this);

            // Run the server.
            CompletableFuture.runAsync(() -> {
                // Run the server.
                this.server.run();
            }, IAS.executor()).thenRunAsync(() -> {
                // Log it and display progress.
                IAS.LOG.info("IAS: Opening link...");
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
    public void onClose() {
        // Bruh.
        assert this.minecraft != null;

        // Close to parent.
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void removed() {
        // Close the server, if any.
        IAS.executor().execute(() -> {
            if (this.server != null) {
                this.server.close();
            }
        });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Bruh.
        assert this.minecraft != null;
        PoseStack pose = graphics.pose();

        // Render parent behind.
        if (this.parent != null) {
            pose.pushPose();
            pose.translate(0.0F, 0.0F, -1000.0F);
            this.parent.render(graphics, 0, 0, delta);
            pose.popPose();
        }

        // Render background and widgets.
        super.render(graphics, mouseX, mouseY, delta);

        // Render the title.
        pose.pushPose();
        pose.scale(2.0F, 2.0F, 2.0F);
        graphics.drawCenteredString(this.font, this.title, this.width / 4, this.height / 4 - 49 / 2, 0xFF_FF_FF_FF);
        pose.popPose();

        // Render password OR label.
        if (this.crypt == null && this.password != null && this.enterPassword != null) {
            graphics.drawCenteredString(this.font, this.password.getMessage(), this.width / 2, this.height / 2 - 10 - 5, 0xFF_FF_FF_FF);
        } else {
            // Synchronize to prevent funny things.
            synchronized (this) {
                // Label is unbaked.
                if (this.label == null) {
                    // Get the component.
                    Component component = Objects.requireNonNullElse(this.stage, Component.empty());

                    // Bake the label.
                    this.label = MultiLineLabel.create(this.font, component, 200, 5);

                    // Narrate.
                    this.minecraft.getNarrator().say(component);
                }

                // Render the label.
                this.label.renderCentered(graphics, this.width / 2, (this.height - this.label.getLineCount() * 9) / 2, 9, 0xFF_FF_FF_FF);
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Bruh.
        assert this.minecraft != null;

        // Render transparent background if parent exists.
        if (this.parent != null) {
            // Render gradient.
            graphics.fill(0, 0, this.width, this.height, 0x80_00_00_00);
        } else if (this.minecraft.level != null) {
            this.renderTransparentBackground(graphics);
        } else {
            this.renderDirtBackground(graphics);
        }

        // Render "form".
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        graphics.fill(centerX - 105, centerY - 50, centerX + 105, centerY + 50, 0xF8_20_20_30);
        graphics.fill(centerX - 104, centerY - 51, centerX + 104, centerY - 50, 0xF8_20_20_30);
        graphics.fill(centerX - 104, centerY + 50, centerX + 104, centerY + 51, 0xF8_20_20_30);
    }

    @Override
    public void stage(String stage) {
        // Bruh.
        assert this.minecraft != null;

        // Skip if not current screen.
        if (this != this.minecraft.screen) return;

        // Flush the stage.
        synchronized (this) {
            this.stage = Component.translatable(stage).withStyle(ChatFormatting.YELLOW);
            this.label = null;
        }
    }

    @Override
    public void success(MicrosoftAccount account) {
        // Bruh.
        assert this.minecraft != null;

        // Skip if not current screen.
        if (this != this.minecraft.screen) return;

        // User cancelled.
        if (account == null) {
            // Schedule on main.
            this.minecraft.execute(() -> {
                // Skip if not current screen.
                if (this != this.minecraft.screen) return;

                // Back to parent screen.
                this.minecraft.setScreen(this.parent);
            });

            // Don't log in.
            return;
        }

        // Write disclaimers.
        this.stage(MicrosoftAccount.FINALIZING);

        // Schedule on main.
        this.minecraft.execute(() -> {
            // Add the account and save it.
            IASStorage.accounts.add(account);
            IAS.saveStorageSafe();
            IAS.disclaimersStorage();

            // Back to parent screen.
            this.minecraft.setScreen(this.parent);
        });
    }

    @Override
    public void error(Throwable error) {
        // Bruh.
        assert this.minecraft != null;

        // Log it.
        IAS.LOG.error("IAS: Create error.", error);

        // Skip if not current screen.
        if (this != this.minecraft.screen) return;

        // Flush the stage.
        synchronized (this) {
            this.stage = Component.translatable("ias.error").withStyle(ChatFormatting.RED);
            this.label = null;
        }
    }

}
