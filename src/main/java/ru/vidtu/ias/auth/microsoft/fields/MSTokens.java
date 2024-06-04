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

package ru.vidtu.ias.auth.microsoft.fields;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.auth.LoginData;
import ru.vidtu.ias.auth.microsoft.MSAuth;
import ru.vidtu.ias.utils.GSONUtils;

/**
 * A pair of Microsoft access and refresh tokens.
 *
 * @param access
 * @param refresh
 * @author VidTu
 * @apiNote The {@link #access()} provided is not suitable for {@link LoginData#token()}
 * @see MSAuth#msacToMsaMsr(String, String)
 * @see MSAuth#msrToMsaMsr(String)
 * @see MSAuth#msaToXbl(String)
 */
public record MSTokens(@NotNull String access, @NotNull String refresh) {
    /**
     * Extracts the MS tokens from the JSON.
     *
     * @param json Target JSON
     * @return Extracted MS tokens
     * @throws JsonParseException If unable to extract
     */
    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static MSTokens fromJson(@NotNull JsonObject json) {
        try {
            // Extract the tokens.
            String access = GSONUtils.getStringOrThrow(json, "access_token");
            String refresh = GSONUtils.getStringOrThrow(json, "refresh_token");

            // Create the tokens.
            return new MSTokens(access, refresh);
        } catch (Throwable t) {
            // Rethrow.
            throw new JsonParseException("Unable to parse MSTokens: " + json, t);
        }
    }
}
