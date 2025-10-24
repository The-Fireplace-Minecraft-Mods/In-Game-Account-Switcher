/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2025 VidTu
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

package ru.vidtu.ias.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * An emulation of delayed tooltips from newer versions.
 *
 * @author VidTu
 */
public final class LegacyTooltip implements Button.OnTooltip {
    /**
     * Empty tooltip.
     */
    public static final LegacyTooltip EMPTY = new LegacyTooltip(null, null, null, 0);

    /**
     * Parent screen.
     */
    private final Screen screen;

    /**
     * Parent font.
     */
    private final Font font;

    /**
     * Tooltip component.
     */
    private List<FormattedCharSequence> tooltip;

    /**
     * Required tooltip delay in millis.
     */
    private int delay;

    /**
     * Last mouse move.
     */
    private long lastFree = Util.getMillis();

    /**
     * Creates a new tooltip.
     *
     * @param screen  Parent screen
     * @param font    Parent font
     * @param tooltip Tooltip component, {@code null} to hide
     * @param delay   Required tooltip delay in millis
     */
    public LegacyTooltip(Screen screen, Font font, Component tooltip, int delay) {
        if (screen == null || font == null) {
            this.screen = null;
            this.font = null;
            return;
        }
        this.screen = screen;
        this.font = font;
        this.tooltip = tooltip == null ? null : font.split(tooltip, screen.width / 2);
        this.delay = delay;
    }

    @Override
    public void onTooltip(Button btn, PoseStack pose, int mouseX, int mouseY) {
        this.render(btn, pose, mouseX, mouseY);
    }

    /**
     * Renders the tooltip.
     *
     * @param widget Parent widget
     * @param pose   Render pose stack
     * @param mouseX Mouse X
     * @param mouseY Mouse Y
     */
    public void render(AbstractWidget widget, PoseStack pose, int mouseX, int mouseY) {
        // Skip if no tooltip.
        if (this.screen == null || this.tooltip == null) return;

        // Skip if not hovered.
        if (!widget.isHoveredOrFocused()) {
            this.lastFree = Util.getMillis();
            return;
        }

        // Render if hovered long enough.
        if ((Util.getMillis() - this.lastFree) < this.delay) return;
        if (this.screen instanceof LastPassRenderCallback callback) {
            callback.lastPass(() -> this.screen.renderTooltip(pose, this.tooltip, mouseX, mouseY));
        } else {
            this.screen.renderTooltip(pose, this.tooltip, mouseX, mouseY);
        }
    }

    /**
     * Sets the tooltip.
     *
     * @param tooltip New tooltip, {@code null} to hide
     */
    public void tooltip(Component tooltip) {
        if (this.screen == null || this.font == null) return;
        this.tooltip = tooltip == null ? null : this.font.split(tooltip, this.screen.width / 2);
    }

    /**
     * Sets the delay.
     *
     * @param delay Required tooltip delay in millis
     */
    public void delay(int delay) {
        if (this.screen == null || this.font == null) return;
        this.delay = delay;
    }
}
