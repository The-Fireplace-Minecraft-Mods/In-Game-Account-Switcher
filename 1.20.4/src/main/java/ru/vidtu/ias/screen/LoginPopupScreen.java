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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.IASMinecraft;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.MicrosoftAccount;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Login popup screen.
 *
 * @author VidTu
 */
final class LoginPopupScreen extends Screen implements Account.LoginHandler {
    /**
     * Parent screen.
     */
    private final Screen parent;

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
     * Password future.
     */
    private CompletableFuture<String> passFuture;

    /**
     * Creates a new login screen.
     *
     * @param parent Parent screen
     */
    LoginPopupScreen(Screen parent) {
        super(Component.translatable("ias.login"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Bruh.
        assert this.minecraft != null;

        // Init parent.
        if (this.parent != null) {
            this.parent.init(this.minecraft, this.width, this.height);
        }

        // Add cancel button.
        this.cancel = new PopupButton(this.width / 2 - 75, this.height / 2 + 49 - 22, 150, 20,
                CommonComponents.GUI_CANCEL, button -> this.onClose(), Supplier::get);
        addRenderableWidget(this.cancel);

        // Add password box, if future exists.
        if (this.passFuture != null) {
            // Add password box.
            this.password = new PopupBox(this.font, this.width / 2 - 100, this.height / 2 - 10 + 5, 178, 20, this.password, Component.translatable("ias.password"), () -> {
                // Prevent NPE, just in case.
                if (this.passFuture == null || this.password == null) return;

                // Complete the future.
                this.passFuture.complete(this.password.getValue());
            });
            this.password.secure = true;
            this.password.setHint(Component.translatable("ias.password.hint").withStyle(ChatFormatting.DARK_GRAY));
            this.password.setFormatter((s, i) -> FormattedCharSequence.forward("*".repeat(s.length()), Style.EMPTY));
            this.password.setMaxLength(32);
            this.addRenderableWidget(this.password);

            // Add enter password button.
            this.enterPassword = new PopupButton(this.width / 2 - 100 + 180, this.height / 2 - 10 + 5, 20, 20, Component.literal(">>"), button -> {
                // Prevent NPE, just in case.
                if (this.passFuture == null || this.password == null) return;

                // Complete the future.
                this.passFuture.complete(this.password.getValue());
            }, Supplier::get);
            this.addRenderableWidget(this.enterPassword);
        }
    }

    @Override
    public void onClose() {
        // Bruh.
        assert this.minecraft != null;

        // Complete password future with cancel, if any.
        if (this.passFuture != null) {
            this.passFuture.complete(null);
        }

        // Close to parent.
        this.minecraft.setScreen(this.parent);
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
        if (this.passFuture != null && this.password != null && this.enterPassword != null) {
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
    public CompletableFuture<String> password() {
        // Bruh.
        assert this.minecraft != null;

        // Return current future if exists.
        if (this.passFuture != null) return this.passFuture;

        // Create a new future.
        this.passFuture = new CompletableFuture<>();

        // Inject into pass future.
        this.passFuture.thenAcceptAsync(password -> {
            // Remove future on completion.
            this.passFuture = null;
            this.password = null;
            this.enterPassword = null;

            // Redraw.
            this.init(this.minecraft, this.width, this.height);
        }, this.minecraft);

        // Schedule rerender.
        this.minecraft.execute(() -> this.init(this.minecraft, this.width, this.height));

        // Return created future.
        return this.passFuture;
    }

    @Override
    public void success(Account.LoginData data) {
        // Bruh.
        assert this.minecraft != null;

        // Skip if not current screen.
        if (this != this.minecraft.screen) return;

        // User cancelled.
        if (data == null) {
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

        // Log in.
        this.stage(MicrosoftAccount.SERVICES);
        IASMinecraft.account(this.minecraft, data).thenRunAsync(() -> {
            // Skip if not current screen.
            if (this != this.minecraft.screen) return;

            // Back to parent screen.
            this.minecraft.setScreen(this.parent);
        }, this.minecraft).exceptionally(ex -> {
            // Handle error on error.
            this.error(new RuntimeException("Unable to change account.", ex));

            // Nothing...
            return null;
        });
    }

    @Override
    public void error(Throwable error) {
        // Bruh.
        assert this.minecraft != null;

        // Log it.
        IAS.LOG.error("IAS: Login error.", error);

        // Skip if not current screen.
        if (this != this.minecraft.screen) return;

        // Flush the stage.
        synchronized (this) {
            this.stage = Component.translatable("ias.error").withStyle(ChatFormatting.RED);
            this.label = null;
        }
    }

}
