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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Styled button with popup design.
 *
 * @author VidTu
 */
final class PopupButton extends Button {
    /**
     * Button red color.
     */
    float red = 1.0F;

    /**
     * Button green color.
     */
    float green = 1.0F;

    /**
     * Button blue color.
     */
    float blue = 1.0F;

    /**
     * Button interpolating red color.
     */
    float currentRed = 1.0F;

    /**
     * Button interpolating green color.
     */
    float currentGreen = 1.0F;

    /**
     * Button interpolating blue color.
     */
    float currentBlue = 1.0F;

    /**
     * Button color multiplier.
     */
    float multiplier = 1.0F;

    /**
     * Creates a new button.
     *
     * @param x         Button X
     * @param y         Button Y
     * @param width     Button width
     * @param height    Button height
     * @param text      Button text
     * @param press     Button press handler
     * @param narration Button narration
     */
    PopupButton(int x, int y, int width, int height, Component text, OnPress press, CreateNarration narration) {
        super(x, y, width, height, text, press, narration);
    }

    /**
     * Sets the button color.
     *
     * @param red Button R
     * @param green Button G
     * @param blue Button B
     * @return {@code this}
     */
    PopupButton color(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Get values.
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int x = this.getX();
        int y = this.getY();
        int width = this.getWidth();
        int height = this.getHeight();
        Component message = this.getMessage();

        // Adjust color.
        if (this.isHoveredOrFocused() && this.isActive()) {
            this.multiplier = Mth.clamp(this.multiplier - (delta * 0.25F), 0.75F, 1.0F);
        } else {
            this.multiplier = Mth.clamp(this.multiplier + (delta * 0.25F), 0.75F, 1.0F);
        }
        this.currentRed += (this.red - this.currentRed) * delta;
        this.currentGreen += (this.green - this.currentGreen) * delta;
        this.currentBlue += (this.blue - this.currentBlue) * delta;

        // Render background.
        int r = (int) (this.multiplier * 255.0F * this.currentRed);
        int g = (int) (this.multiplier * 255.0F * this.currentGreen);
        int b = (int) (this.multiplier * 255.0F * this.currentBlue);
        int color = -16777216 | r << 16 | g << 8 | b;
        graphics.fill(x, y + 1, x + width, y + height - 1, color);
        graphics.fill(x + 1, y, x + width - 1, y + 1, color);
        graphics.fill(x + 1, y + height - 1, x + width - 1, y + height, color);

        // Render string.
        graphics.drawString(font, message, x + (width - font.width(message)) / 2, y + height / 2 - 4, 0xFF_00_00_00, false);
    }
}
