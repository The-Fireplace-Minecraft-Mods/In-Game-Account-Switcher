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

package ru.vidtu.ias.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.crypt.DummyCrypt;
import ru.vidtu.ias.crypt.HardwareCrypt;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Microsoft crypt type popup screen.
 *
 * @author VidTu
 */
final class MicrosoftCryptPopupScreen extends Screen {
    /**
     * Parent screen.
     */
    private final Screen parent;

    /**
     * Account handler.
     */
    private final Consumer<Account> handler;

    /**
     * No encryption.
     */
    private PopupButton plain;

    /**
     * Creates a new add screen.
     *
     * @param parent  Parent screen
     * @param handler Account handler
     */
    MicrosoftCryptPopupScreen(Screen parent, Consumer<Account> handler) {
        super(Component.translatable("ias.microsoft"));
        this.parent = parent;
        this.handler = handler;
    }

    @Override
    protected void init() {
        // Bruh.
        assert this.minecraft != null;

        // Init parent.
        if (this.parent != null) {
            this.parent.init(this.minecraft, this.width, this.height);
        }

        // Add password button.
        PopupButton button = new PopupButton(this.width / 2 - 75, this.height / 2 - 24 - 12, 150, 20,
                Component.translatable("ias.microsoft.password"), btn -> this.minecraft.setScreen(new MicrosoftPopupScreen(this.parent, this.handler, null)), Supplier::get);
        button.setTooltip(Tooltip.create(Component.translatable("ias.microsoft.password.tip")));
        button.setTooltipDelay(250);
        button.color(0.5F, 1.0F, 0.5F, true);
        this.addRenderableWidget(button);

        // Add hardware button.
        button = new PopupButton(this.width / 2 - 75, this.height / 2 - 12, 150, 20,
                Component.translatable("ias.microsoft.hardware"), btn -> this.minecraft.setScreen(new MicrosoftPopupScreen(this.parent, this.handler, HardwareCrypt.INSTANCE)), Supplier::get);
        button.setTooltip(Tooltip.create(Component.translatable("ias.microsoft.hardware.tip")));
        button.setTooltipDelay(250);
        button.color(1.0F, 1.0F, 0.5F, true);
        this.addRenderableWidget(button);

        // Add plain button.
        this.plain = new PopupButton(this.width / 2 - 75, this.height / 2 + 12, 150, 20,
                Component.translatable("ias.microsoft.plain"), btn -> this.minecraft.setScreen(new MicrosoftPopupScreen(this.parent, this.handler, DummyCrypt.INSTANCE)), Supplier::get);
        if (IASConfig.allowNoCrypt) {
            this.plain.setTooltip(Tooltip.create(Component.translatable("ias.microsoft.plain.tip.off", Component.translatable("key.keyboard.left.alt"), GLFW.glfwGetKeyName(GLFW.GLFW_KEY_Y, GLFW.GLFW_KEY_UNKNOWN))));
        } else {
            this.plain.setTooltip(Tooltip.create(Component.translatable("ias.microsoft.plain.tip.no")));
        }
        this.plain.setTooltipDelay(250);
        this.plain.color(1.0F, 0.5F, 0.5F, true);
        this.plain.active = false;
        this.addRenderableWidget(this.plain);

        // Add cancel button.
        this.addRenderableWidget(new PopupButton(this.width / 2 - 75, this.height / 2 + 79 - 22, 150, 20,
                CommonComponents.GUI_CANCEL, btn -> this.onClose(), Supplier::get));
    }

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
        drawCenteredString(pose, this.font, this.title, this.width / 4, this.height / 4 - 79 / 2, 0xFF_FF_FF_FF);
        pose.popPose();
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
        fill(pose, centerX - 80, centerY - 80, centerX + 80, centerY + 80, 0xF8_20_20_30);
        fill(pose, centerX - 79, centerY - 81, centerX + 79, centerY - 80, 0xF8_20_20_30);
        fill(pose, centerX - 79, centerY + 80, centerX + 79, centerY + 81, 0xF8_20_20_30);
    }

    @Override
    public void onClose() {
        // Bruh.
        assert this.minecraft != null;

        // Close to parent.
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        // Enable plain.
        if (key == GLFW.GLFW_KEY_Y && IASConfig.allowNoCrypt && this.plain != null && !this.plain.isActive() && Screen.hasAltDown()) {
            // Activate button.
            this.plain.active = true;

            // Recolor button and update tooltip.
            this.plain.setTooltip(Tooltip.create(Component.translatable("ias.microsoft.plain.tip.on")));
            this.plain.setTooltipDelay(250);
            this.plain.color(1.0F, 0.25F, 0.25F, false);
        }

        // Pass-through.
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public String toString() {
        return "MicrosoftCryptPopupScreen{}";
    }
}
