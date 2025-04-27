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

package ru.vidtu.ias.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.ias.platform.IStonecutter;
import ru.vidtu.ias.utils.IUtils;

import java.util.Locale;

/**
 * Internal Sun HTTP server mode.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see IASConfig#server
 */
@ApiStatus.Internal
@NullMarked
public enum ServerMode {
    /**
     * Always use HTTP server, never use Microsoft device auth.
     */
    ALWAYS,

    /**
     * Use HTTP server if {@link IUtils#canUseSunServer()}, otherwise use Microsoft device auth.
     */
    AVAILABLE,

    /**
     * Never use HTTP server, always use Microsoft device auth.
     */
    NEVER;

    /**
     * Mode button label.
     */
    private final Component label;

    /**
     * Mode button tip.
     */
    private final Component tip;

    /**
     * Creates a new mode.
     */
    @Contract(pure = true)
    ServerMode() {
        // Create the translation key.
        String key = ("ias.server." + this.name().toLowerCase(Locale.ROOT));

        // Create the components.
        this.label = IStonecutter.translate("options.generic_value", IStonecutter.translate("ias.server"), IStonecutter.translate(key.intern()));
        this.tip = IStonecutter.translate((key + ".tip").intern());
    }

    /**
     * Gets the button label for this mode.
     *
     * @return Mode button label
     * @see #tip()
     * @see IScreen
     */
    @Contract(pure = true)
    Component label() {
        return this.label;
    }

    /**
     * Gets the button tooltip for this mode.
     *
     * @return Mode button tip
     * @see #label()
     * @see IScreen
     */
    @Contract(pure = true)
    Component tip() {
        return this.tip;
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "IAS/ServerMode{" +
                "name='" + this.name() + '\'' +
                ", ordinal=" + this.ordinal() +
                ", label=" + this.label +
                ", tip=" + this.tip +
                '}';
    }
}
