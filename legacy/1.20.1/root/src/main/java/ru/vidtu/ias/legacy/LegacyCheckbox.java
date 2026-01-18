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

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;

/**
 * Checkbox with a callback.
 *
 * @author VidTu
 */
public final class LegacyCheckbox extends Checkbox {
    /**
     * Check callback.
     */
    private final BooleanConsumer callback;

    /**
     * Creates a new check callback.
     *
     * @param font     Target font
     * @param x        Target X
     * @param y        Target Y
     * @param label    Checkbox label
     * @param check    Checkbox check status
     * @param callback Checkbox check callback
     */
    public LegacyCheckbox(Font font, int x, int y, Component label, boolean check, BooleanConsumer callback) {
        super(x, y, font.width(label) + 24, 20, label, check);
        this.callback = callback;
    }

    @Override
    public void onPress() {
        super.onPress();
        this.callback.accept(this.selected());
    }
}
