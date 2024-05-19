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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.OfflineAccount;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.legacy.LegacyTooltip;

import java.util.function.Consumer;

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
     * Account handler.
     */
    private final Consumer<Account> handler;

    /**
     * Name box.
     */
    private PopupBox name;

    /**
     * Done button.
     */
    private PopupButton done;

    /**
     * Done tooltip.
     */
    private LegacyTooltip doneTooltip;

    /**
     * Creates a new add screen.
     *
     * @param parent  Parent screen
     * @param handler Account handler
     */
    OfflinePopupScreen(Screen parent, Consumer<Account> handler) {
        super(new TranslatableComponent("ias.offline"));
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

        // Add name box.
        //noinspection StringConcatenationMissingWhitespace
        this.name = new PopupBox(this.font, this.width / 2 - 75, this.height / 2 - 10 + 5, 148, 20, this.name, new TranslatableComponent("ias.offline.nick"), this::done, false, new TextComponent(IASConfig.unexpectedPigs ? ("Boar" + this.hashCode()) : "Steve").withStyle(ChatFormatting.DARK_GRAY));
        this.name.setMaxLength(16);
        this.addRenderableWidget(this.name);

        // Add done button.
        this.doneTooltip = new LegacyTooltip(this, this.font, null, -1);
        this.done = new PopupButton(this.width / 2 - 75, this.height / 2 + 49 - 22, 74, 20,
                CommonComponents.GUI_DONE, btn -> this.done(), this.doneTooltip);
        this.done.color(1.0F, 0.5F, 0.5F, true);
        this.addRenderableWidget(this.done);

        // Add cancel button.
        PopupButton button = new PopupButton(this.width / 2 + 1, this.height / 2 + 49 - 22, 74, 20,
                CommonComponents.GUI_CANCEL, btn -> this.onClose(), LegacyTooltip.EMPTY);
        button.color(1.0F, 1.0F, 1.0F, true);
        this.addRenderableWidget(button);

        // Update.
        this.name.setResponder(value -> this.type(false));
        this.type(true);
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

        // Create and accept.
        this.handler.accept(new OfflineAccount(value));
    }

    /**
     * Updates the {@link #done} button.
     *
     * @param instant Whether the color change should be instant
     */
    private void type(boolean instant) {
        // Prevent NPE.
        if (this.done == null || this.name == null) return;

        // Get the name.
        String value = this.name.getValue();

        // Don't allow blank.
        if (value.isBlank()) {
            // Disable button.
            this.done.active = false;

            // Tooltip.
            this.doneTooltip.tooltip(new TranslatableComponent("ias.offline.nick.blank"));

            // Update color.
            this.done.color(1.0F, 0.5F, 0.5F, instant);

            // Don't process.
            return;
        }

        // Enable button.
        this.done.active = true;

        // Check for short.
        int length = value.length();
        if (length < 3) {
            // Enable if ALT is hold.
            if (Screen.hasAltDown()) {
                this.done.active = true;
                this.done.color(0.75F, 0.75F, 0.25F, instant);
            } else {
                this.done.active = false;
                this.done.color(1.0F, 1.0F, 0.5F, instant);
            }

            // Tooltip.
            this.doneTooltip.tooltip(new TranslatableComponent("ias.offline.nick.short", new TranslatableComponent("key.keyboard.left.alt")));

            // Don't process.
            return;
        }

        // Check for long.
        if (length > 16) {
            // Enable if ALT is hold.
            if (Screen.hasAltDown()) {
                this.done.active = true;
                this.done.color(0.75F, 0.75F, 0.25F, instant);
            } else {
                this.done.active = false;
                this.done.color(1.0F, 1.0F, 0.5F, instant);
            }

            // Tooltip.
            this.doneTooltip.tooltip(new TranslatableComponent("ias.offline.nick.long", new TranslatableComponent("key.keyboard.left.alt")));

            // Don't process.
            return;
        }

        // Check for characters.
        for (int i = 0; i < length; i++) {
            // Skip allowed chars.
            int c = value.codePointAt(i);
            if (c == '_' || c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') continue;
            this.done.active = Screen.hasAltDown();

            // Enable if ALT is hold.
            if (Screen.hasAltDown()) {
                this.done.active = true;
                this.done.color(0.75F, 0.75F, 0.25F, instant);
            } else {
                this.done.active = false;
                this.done.color(1.0F, 1.0F, 0.5F, instant);
            }

            // Tooltip.
            this.doneTooltip.tooltip(new TranslatableComponent("ias.offline.nick.chars", Character.toString(c), new TranslatableComponent("key.keyboard.left.alt")));

            // Don't process.
            return;
        }

        // Update color.
        this.done.color(0.5F, 1.0F, 0.5F, instant);

        // Tooltip.
        this.doneTooltip.tooltip(null);
    }

    @Override
    public void tick() {
        super.tick();
        this.name.tick();
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        boolean res = super.keyPressed(key, scan, mods);
        if (key == GLFW.GLFW_KEY_LEFT_ALT || key == GLFW.GLFW_KEY_RIGHT_ALT) {
            this.type(false);
        }
        return res;
    }

    @Override
    public boolean keyReleased(int key, int scan, int mods) {
        boolean res = super.keyReleased(key, scan, mods);
        if (key == GLFW.GLFW_KEY_LEFT_ALT || key == GLFW.GLFW_KEY_RIGHT_ALT) {
            this.type(false);
        }
        return res;
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
        drawCenteredString(pose, this.font, this.title, this.width / 4, this.height / 4 - 49 / 2, 0xFF_FF_FF_FF);
        pose.popPose();

        // Render the nick title.
        if (this.name != null) {
            drawCenteredString(pose, this.font, this.name.getMessage(), this.width / 2, this.height / 2 - 10 - 5, 0xFF_FF_FF_FF);
        }
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
        fill(pose, centerX - 80, centerY - 50, centerX + 80, centerY + 50, 0xF8_20_20_30);
        fill(pose, centerX - 79, centerY - 51, centerX + 79, centerY - 50, 0xF8_20_20_30);
        fill(pose, centerX - 79, centerY + 50, centerX + 79, centerY + 51, 0xF8_20_20_30);
    }

    @Override
    public void onClose() {
        // Bruh.
        assert this.minecraft != null;

        // Close to parent.
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public String toString() {
        return "OfflinePopupScreen{}";
    }
}
