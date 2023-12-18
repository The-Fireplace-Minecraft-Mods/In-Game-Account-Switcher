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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.OfflineAccount;
import ru.vidtu.ias.config.IASStorage;

import java.util.function.Supplier;

/**
 * Offline popup screen.
 *
 * @author VidTu
 */
public final class OfflinePopupScreen extends Screen {
    /**
     * Parent screen.
     */
    private final Screen parent;

    /**
     * Name box.
     */
    private PopupBox name;

    /**
     * Done button.
     */
    private PopupButton done;

    /**
     * Cancel button.
     */
    private PopupButton cancel;

    /**
     * Creates a new add screen.
     *
     * @param parent Parent screen
     */
    OfflinePopupScreen(Screen parent) {
        super(Component.translatable("ias.offline"));
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

        // Add name box.
        this.name = new PopupBox(this.font, this.width / 2 - 75, this.height / 2 - 10 + 5, 148, 20, this.name, Component.translatable("ias.offline.name"), this::done);
        this.name.setMaxLength(16);
        this.addRenderableWidget(this.name);

        // Add done button.
        this.done = new PopupButton(this.width / 2 - 75, this.height / 2 + 49 - 22, 74, 20,
                CommonComponents.GUI_DONE, button -> this.done(), Supplier::get);
        this.done.color(1.0F, 0.5F, 0.5F);
        addRenderableWidget(this.done);

        // Add cancel button.
        this.cancel = new PopupButton(this.width / 2 + 1, this.height / 2 + 49 - 22, 74, 20,
                CommonComponents.GUI_CANCEL, button -> this.onClose(), Supplier::get);
        this.cancel.color(1.0F, 1.0F, 1.0F);
        addRenderableWidget(this.cancel);

        // Update.
        this.name.setResponder(value -> this.type());
        this.type();
    }

    /**
     * Adds the account.
     * Does nothing if name is empty.
     */
    private void done() {
        // Bruh.
        assert this.minecraft != null;

        // Prevent NPE.
        if (this.name == null) return;

        // Get the name.
        String value = this.name.getValue();

        // Don't allow blank.
        if (value.isBlank()) return;

        // Add account.
        IASStorage.accounts.add(OfflineAccount.create(value));
        IAS.saveStorageSafe();
        IAS.disclaimersStorage();

        // Close to parent.
        this.minecraft.setScreen(this.parent);
    }

    /**
     * Updates the {@link #done} button.
     */
    private void type() {
        // Prevent NPE.
        if (this.done == null || this.name == null) return;

        // Get the name.
        String value = this.name.getValue();

        // Don't allow blank.
        if (value.isBlank()) {
            // Disable button.
            this.done.active = false;

            // Tooltip.
            this.done.setTooltip(Tooltip.create(Component.translatable("ias.offline.name.blank")));

            // Update color.
            this.done.color(1.0F, 0.5F, 0.5F);

            // Don't process.
            return;
        }

        // Enable button.
        this.done.active = true;

        // Check for short.
        int length = value.length();
        if (length < 3) {
            // Update color.
            this.done.color(1.0F, 1.0F, 0.5F);

            // Tooltip.
            this.done.setTooltip(Tooltip.create(Component.translatable("ias.offline.name.short")));

            // Don't process.
            return;
        }

        // Check for long.
        if (length > 16) {
            // Update color.
            this.done.color(1.0F, 1.0F, 0.5F);

            // Tooltip.
            this.done.setTooltip(Tooltip.create(Component.translatable("ias.offline.name.long")));

            // Don't process.
            return;
        }

        // Check for characters.
        for (int i = 0; i < length; i++) {
            // Skip allowed chars.
            char c = value.charAt(i);
            if (c == '_' || c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') continue;

            // Update color.
            this.done.color(1.0F, 1.0F, 0.5F);

            // Tooltip.
            this.done.setTooltip(Tooltip.create(Component.translatable("ias.offline.name.character", c)));

            // Don't process.
            return;
        }

        // Update color.
        this.done.color(0.5F, 1.0F, 0.5F);

        // Tooltip.
        this.done.setTooltip(null);
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

        // Render the nick title.
        if (this.name != null) {
            graphics.drawCenteredString(this.font, this.name.getMessage(), this.width / 2, this.height / 2 - 10 - 5, 0xFF_FF_FF_FF);
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
        graphics.fill(centerX - 80, centerY - 50, centerX + 80, centerY + 50, 0xF8_20_20_30);
        graphics.fill(centerX - 79, centerY - 51, centerX + 79, centerY - 50, 0xF8_20_20_30);
        graphics.fill(centerX - 79, centerY + 50, centerX + 79, centerY + 51, 0xF8_20_20_30);
    }

    @Override
    public void onClose() {
        // Bruh.
        assert this.minecraft != null;

        // Close to parent.
        this.minecraft.setScreen(this.parent);
    }
}
