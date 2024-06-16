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

package ru.vidtu.ias.legacy;

import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Legacy helper class of a player skin.
 *
 * @param skin Skin location
 * @param slim Skin slimness
 * @author VidTu
 */
public record Skin(ResourceLocation skin, boolean slim) {
    /**
     * Gets the default skin for UUID.
     *
     * @param uuid Target UUID
     * @return Default skin for target UUID
     */
    public static Skin getDefault(UUID uuid) {
        ResourceLocation skin = DefaultPlayerSkin.getDefaultSkin(uuid);
        boolean slim = "slim".equalsIgnoreCase(DefaultPlayerSkin.getSkinModelName(uuid));
        return new Skin(skin, slim);
    }
}
