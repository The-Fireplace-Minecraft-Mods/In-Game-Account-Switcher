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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * Styled button with popup design.
 *
 * @author VidTu
 */
public final class PopupBox extends EditBox {
    /**
     * A font of IAS.
     */
    private final Font iasFont;

    /**
     * Enter action.
     */
    private final Runnable enterAction;

    /**
     * Whether to prevent copying.
     */
    private final boolean secure;

    /**
     * Box hint.
     */
    private final Component hint;

    /**
     * Creates a new box.
     *
     * @param font        Font renderer
     * @param x           Box X
     * @param y           Box Y
     * @param width       Box width
     * @param height      Box height
     * @param inherit     Previous box, if any
     * @param title       Box title
     * @param enterAction Action on enter key
     * @param secure      Whether to prevent copying
     * @param hint        Box hint, {@code null} if none
     */
    PopupBox(Font font, int x, int y, int width, int height, PopupBox inherit, Component title,
             Runnable enterAction, boolean secure, Component hint) {
        super(font, x, y, width, height, inherit, title);
        this.iasFont = font;
        this.enterAction = enterAction;
        this.secure = secure;
        this.hint = hint;
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float delta) {
        // Render background.
        int x = this.x;
        int y = this.y;
        int width = this.getWidth();
        int height = this.getHeight();
        fill(pose, x + 1, y + 1, x + width - 1, y + height - 1, 0xFF_00_00_00);
        fill(pose, x + 1, y, x + width - 1, y + 1, 0xFF_FF_FF_FF);
        fill(pose, x + 1, y + height - 1, x + width - 1, y + height, 0xFF_FF_FF_FF);
        fill(pose, x, y + 1, x + 1, y + height - 1, 0xFF_FF_FF_FF);
        fill(pose, x + width - 1, y + 1, x + width, y + height - 1, 0xFF_FF_FF_FF);

        // Render other.
        super.renderButton(pose, mouseX, mouseY, delta);

        // Render hint.
        if (this.hint != null && this.getValue().isEmpty() && !this.isFocused()) {
            this.iasFont.drawShadow(pose, this.hint, this.x + 4, this.y + (this.height - 8) / 2, -1);
        }
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        // Enter.
        if (this.enterAction != null && this.isActive() && this.isFocused() && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)) {
            // Run enter action.
            this.enterAction.run();

            // Success.
            return true;
        }

        // Prevent copy and cut if required.
        if (this.secure && (Screen.isCopy(key) || Screen.isCut(key))) {
            // Prevent it.
            return true;
        }

        // Pass to super.
        return super.keyPressed(key, scan, mods);
    }

    @Override
    @NotNull
    protected MutableComponent createNarrationMessage() {
        // Not secure - return super.
        if (!this.secure) return super.createNarrationMessage();

        // Secure, do not tell.
        return new TranslatableComponent("gui.narrate.editBox", this.getMessage(), TextComponent.EMPTY);
    }

    @Override
    public String toString() {
        return "PopupBox{" +
                "secure=" + this.secure +
                '}';
    }
}
