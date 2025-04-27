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

import java.util.Locale;

/**
 * "Current account" text label alignment.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see IASConfig#titleTextAlign
 * @see IASConfig#serversTextAlign
 */
@ApiStatus.Internal
@NullMarked
public enum TextAlign {
    /**
     * Text is left-aligned.
     */
    LEFT,

    /**
     * Text is center-aligned.
     */
    CENTER,

    /**
     * Text is right-aligned.
     */
    RIGHT;

    /**
     * Title screen alignment button label.
     */
    private final Component titleLabel;

    /**
     * Title screen alignment button tip.
     */
    private final Component titleTip;

    /**
     * Servers (multiplayer) screen alignment button label.
     */
    private final Component serversLabel;

    /**
     * Servers (multiplayer) screen alignment button tip.
     */
    private final Component serversTip;

    /**
     * Creates a new alignment.
     */
    @Contract(pure = true)
    TextAlign() {
        // Create the translation key.
        String key = ("ias.align." + this.name().toLowerCase(Locale.ROOT));

        // Create the components.
        Component type = IStonecutter.translate(key.intern());
        this.titleLabel = IStonecutter.translate("options.generic_value", IStonecutter.translate("ias.align.title"), type);
        this.titleTip = IStonecutter.translate((key + ".title.tip").intern());
        this.serversLabel = IStonecutter.translate("options.generic_value", IStonecutter.translate("ias.align.servers"), type);
        this.serversTip = IStonecutter.translate((key + ".servers.tip").intern());
    }

    /**
     * Gets the button label for this alignment for the title screen.
     *
     * @return Title screen alignment button label
     * @see #titleTip()
     * @see #serversLabel()
     * @see #serversTip()
     * @see IScreen
     */
    @Contract(pure = true)
    Component titleLabel() {
        return this.titleLabel;
    }

    /**
     * Gets the button tooltip for this alignment for the title screen.
     *
     * @return Mode button tip
     * @see #titleLabel()
     * @see #serversLabel()
     * @see #serversTip()
     * @see IScreen
     */
    @Contract(pure = true)
    Component titleTip() {
        return this.titleTip;
    }

    /**
     * Gets the button label for this alignment for the servers (multiplayer) screen.
     *
     * @return Servers (multiplayer) screen alignment button label
     * @see #titleLabel()
     * @see #titleTip()
     * @see #serversTip()
     * @see IScreen
     */
    @Contract(pure = true)
    Component serversLabel() {
        return this.serversLabel;
    }

    /**
     * Gets the button tooltip for this alignment for the servers (multiplayer) screen.
     *
     * @return Mode button tip
     * @see #titleLabel()
     * @see #titleTip()
     * @see #serversLabel()
     * @see IScreen
     */
    @Contract(pure = true)
    Component serversTip() {
        return this.serversTip;
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "IAS/TextAlign{" +
                "name='" + this.name() + '\'' +
                ", ordinal=" + this.ordinal() +
                ", titleLabel=" + this.titleLabel +
                ", titleTip=" + this.titleTip +
                ", serversLabel=" + this.serversLabel +
                ", serversTip=" + this.serversTip +
                '}';
    }
}
