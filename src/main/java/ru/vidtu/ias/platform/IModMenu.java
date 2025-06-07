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

//? if fabric {
package ru.vidtu.ias.platform;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.screen.ConfigScreen;

/**
 * IAS entrypoint for the ModMenu API.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see IFabric
 * @see IASConfig
 */
@ApiStatus.Internal
@NullMarked
public final class IModMenu implements ModMenuApi {
    /**
     * Creates a new entrypoint.
     */
    @Contract(pure = true)
    public IModMenu() {
        // Empty
    }

    @Contract(pure = true)
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::new;
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "IAS/IModMenu{}";
    }
}
//?}
