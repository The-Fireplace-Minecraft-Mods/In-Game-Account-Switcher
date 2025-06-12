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

package ru.vidtu.ias.platform.ui;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;

/**
 * Abstract {@link Screen} implementation designed for multiple versions.
 *
 * @author VidTu
 */
public abstract class IScreen extends Screen {
    /**
     * A duration for tooltips in version-dependant units. Currently {@code 250} milliseconds.
     */
    //? if >=1.20.6 {
    static final java.time.Duration TOOLTIP_DURATION = java.time.Duration.ofMillis(250L);
    //?} else if >=1.19.4 {
    /*static final int TOOLTIP_DURATION = 250; // Millis.
     *///?} else
    /*static final long TOOLTIP_DURATION = 250_000_000L;*/ // Nanos.

    /**
     * Creates a new screen.
     *
     * @param title Screen title to narrate and display
     */
    @Contract(pure = true)
    protected IScreen(Component title) {
        super(title);
    }

    @Override
    public final void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Render background and widgets.
        //? if <1.20.2
        /*this.renderBackground(graphics, mouseX, mouseY, delta);*/
        super.render(graphics, mouseX, mouseY, delta);

        // Render the contents.
        this.renderContents(graphics, mouseX, mouseY, delta);
    }

    protected abstract void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta);

    /**
     * Creates a new GUI checkbox instance.
     *
     * @param x       Checkbox X position
     * @param y       Checkbox Y position
     * @param message Checkbox label
     * @param tooltip Checkbox tooltip
     * @param check   Whether the checkbox is checked
     * @param handler Checkbox click handler
     * @return A new checkbox instance
     */
    @SuppressWarnings("BooleanParameter") // <- Boolean method used as a state, not as control flow. (checkbox "checked" state)
    @Contract(value = "_, _, _, _, _, _ -> new", pure = true)
    protected Checkbox createCheckbox(int x, int y, Component message, Component tooltip, boolean check, BooleanConsumer handler) {
        // Get the font.
        Font font = this.font;

        // Create.
        //? if >=1.20.4 {
        Checkbox box = Checkbox.builder(message, font) // Implicit NPE for 'message', 'font'
                .pos(x - ((font.width(message) + 24) / 2), y)
                .selected(check)
                .onValueChange((checkbox, value) -> handler.accept(value)) // Implicit NPE for 'handler'
                .build();
        //?} else {
        /*int width = font.width(message) + 24; // Implicit NPE for 'font', 'message'
        Checkbox box = new Checkbox(x - (width / 2), y, width, 20, message, check) {
            @Override
            public void onPress() {
                // Toggle the checkbox.
                super.onPress();

                // Invoke the handler.
                handler.accept(this.selected()); // Implicit NPE for 'handler'
            }

            //? if <1.19.4 {
            /^/^¹*
             * A tooltip split to {@code 170} scaled pixels wide, a value used in modern versions
             ¹^/
            private final List<FormattedCharSequence> tip = font.split(tooltip, 170); // Implicit NPE for 'tooltip'

            /^¹*
             * Last time when the mouse was NOT over this element in units of {@link System#nanoTime()}.
             ¹^/
            private long lastAway = System.nanoTime();

            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") // <- Parameter names are not provided by Mojmap.
            @Override
            public void renderButton(com.mojang.blaze3d.vertex.PoseStack graphics, int mouseX, int mouseY, float delta) {
                // Render the element itself.
                super.renderButton(graphics, mouseX, mouseY, delta);

                // Button is not hovered, update the state.
                if (!this.isHovered) {
                    this.lastAway = System.nanoTime();
                    return;
                }

                // Button is not hovered for enough time.
                if ((System.nanoTime() - this.lastAway) < TOOLTIP_DURATION) return;

                // Render the tooltip.
                tooltipRenderer.accept(this.tip); // Implicit NPE for 'tooltipRenderer'
            }
            ^///?}
        };
        *///?}
        //? if >=1.19.4 {
        box.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip));
        box.setTooltipDelay(TOOLTIP_DURATION);
        //?}
        return box;
    }
}
