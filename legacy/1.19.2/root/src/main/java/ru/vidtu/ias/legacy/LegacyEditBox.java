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

package ru.vidtu.ias.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * An edit box with a tooltip.
 *
 * @author VidTu
 */
public final class LegacyEditBox extends EditBox {
    /**
     * A font of IAS.
     */
    private final Font iasFont;

    /**
     * Box tooltip.
     */
    private final LegacyTooltip tooltip;

    /**
     * Box hint.
     */
    private final Component hint;

    /**
     * Creates a new edit box.
     *
     * @param font      Target font
     * @param x         Box X
     * @param y         Box Y
     * @param width     Box width
     * @param height    Box height
     * @param inherit   Box inheriting
     * @param component Box title
     * @param tooltip   Box tooltip
     * @param hint      Box hint, {@code null} if none
     */
    public LegacyEditBox(Font font, int x, int y, int width, int height, @Nullable EditBox inherit, Component component,
                         LegacyTooltip tooltip, Component hint) {
        super(font, x, y, width, height, inherit, component);
        this.iasFont = font;
        this.tooltip = tooltip;
        this.hint = hint;
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float delta) {
        super.renderButton(pose, mouseX, mouseY, delta);
        if (this.hint != null && this.getValue().isEmpty() && !this.isFocused()) {
            this.iasFont.drawShadow(pose, this.hint, this.x + 4, this.y + (this.height - 8) / 2, -1);
        }
        this.tooltip.render(this, pose, mouseX, mouseY);
    }
}
