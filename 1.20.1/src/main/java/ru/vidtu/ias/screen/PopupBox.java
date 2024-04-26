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

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * Styled button with popup design.
 *
 * @author VidTu
 */
final class PopupBox extends EditBox {
    /**
     * Enter action.
     */
    private final Runnable enterAction;

    /**
     * Whether to prevent copying.
     */
    private final boolean secure;

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
     */
    PopupBox(Font font, int x, int y, int width, int height, PopupBox inherit, Component title, Runnable enterAction, boolean secure) {
        super(font, x, y, width, height, inherit, title);
        this.enterAction = enterAction;
        this.secure = secure;
        this.setBordered(false);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Render background.
        int x = this.getX();
        int y = this.getY();
        int width = this.getWidth();
        int height = this.getHeight();
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF_00_00_00);
        graphics.fill(x + 1, y, x + width - 1, y + 1, 0xFF_FF_FF_FF);
        graphics.fill(x + 1, y + height - 1, x + width - 1, y + height, 0xFF_FF_FF_FF);
        graphics.fill(x, y + 1, x + 1, y + height - 1, 0xFF_FF_FF_FF);
        graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, 0xFF_FF_FF_FF);

        // Render other.
        super.renderWidget(graphics, mouseX, mouseY, delta);
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
        return Component.translatable("gui.narrate.editBox", this.getMessage(), Component.empty());
    }

    @Override
    public String toString() {
        return "PopupBox{" +
                ", secure=" + this.secure +
                '}';
    }
}
