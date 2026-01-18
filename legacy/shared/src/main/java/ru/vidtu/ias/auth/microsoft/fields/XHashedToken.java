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

package ru.vidtu.ias.auth.microsoft.fields;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.auth.microsoft.MSAuth;
import ru.vidtu.ias.utils.GSONUtils;

/**
 * XBL or XSTS token paired with user hash.
 *
 * @param token Target token
 * @param hash  User hash
 * @author VidTu
 * @see MSAuth#msaToXbl(String)
 * @see MSAuth#xblToXsts(String, String)
 * @see MSAuth#xstsToMca(String, String)
 */
public record XHashedToken(@NotNull String token, @NotNull String hash) {
    /**
     * Extracts the MS tokens from the JSON.
     *
     * @param json Target JSON
     * @return Extracted MS tokens
     * @throws JsonParseException If unable to extract
     */
    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static XHashedToken fromJson(@NotNull JsonObject json) {
        try {
            // Extract the token.
            String token = GSONUtils.getStringOrThrow(json, "Token");
            JsonObject displayClaims = GSONUtils.getObjectOrThrow(json, "DisplayClaims");

            // Extract the XUI (UHS) token.
            JsonArray xui = GSONUtils.getArrayOrThrow(displayClaims, "xui");
            int size = xui.size();
            if (size != 1) {
                throw new IllegalArgumentException("Unexpected 'xui' size: " + size);
            }
            JsonElement xuiEntry = xui.get(0);
            if (!xuiEntry.isJsonObject()) {
                throw new IllegalArgumentException("Entry of 'xui' is not a JSON object: " + xuiEntry);
            }
            JsonObject xuiJson = xuiEntry.getAsJsonObject();
            String uhs = GSONUtils.getStringOrThrow(xuiJson, "uhs");

            // Create the token.
            return new XHashedToken(token, uhs);
        } catch (Throwable t) {
            // Rethrow.
            throw new JsonParseException("Unable to parse XHashedToken: " + json, t);
        }
    }
}
