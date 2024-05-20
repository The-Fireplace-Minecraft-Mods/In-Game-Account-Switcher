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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import ru.vidtu.ias.legacy.LegacyTooltip;

/**
 * Styled button with popup design.
 *
 * @author VidTu
 */
final class PopupButton extends Button {
    /**
     * Button red color.
     */
    private float red = 1.0F;

    /**
     * Button green color.
     */
    private float green = 1.0F;

    /**
     * Button blue color.
     */
    private float blue = 1.0F;

    /**
     * Button interpolating red color.
     */
    private float currentRed = 1.0F;

    /**
     * Button interpolating green color.
     */
    private float currentGreen = 1.0F;

    /**
     * Button interpolating blue color.
     */
    private float currentBlue = 1.0F;

    /**
     * Button color multiplier.
     */
    private float multiplier = 1.0F;

    /**
     * Creates a new button.
     *
     * @param x       Button X
     * @param y       Button Y
     * @param width   Button width
     * @param height  Button height
     * @param text    Button text
     * @param press   Button press handler
     * @param tooltip Button tooltip
     */
    PopupButton(int x, int y, int width, int height, Component text, OnPress press, LegacyTooltip tooltip) {
        super(x, y, width, height, text, press, tooltip);
    }

    /**
     * Sets the button color.
     *
     * @param red     Button R
     * @param green   Button G
     * @param blue    Button B
     * @param instant Whether the color change should be instant
     */
    void color(float red, float green, float blue, boolean instant) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        if (!instant) return;
        this.currentRed = red;
        this.currentGreen = green;
        this.currentBlue = blue;
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float delta) {
        // Get values.
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int x = this.x;
        int y = this.y;
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
        fill(pose, x, y + 1, x + width, y + height - 1, color);
        fill(pose, x + 1, y, x + width - 1, y + 1, color);
        fill(pose, x + 1, y + height - 1, x + width - 1, y + height, color);

        // Render string.
        font.draw(pose, message, x + (width - font.width(message)) / 2, y + height / 2 - 4, 0xFF_00_00_00);
        this.renderToolTip(pose, mouseX, mouseY);
    }

    @Override
    public String toString() {
        return "PopupButton{" +
                "red=" + this.red +
                ", green=" + this.green +
                ", blue=" + this.blue +
                ", currentRed=" + this.currentRed +
                ", currentGreen=" + this.currentGreen +
                ", currentBlue=" + this.currentBlue +
                ", multiplier=" + this.multiplier +
                '}';
    }
}
