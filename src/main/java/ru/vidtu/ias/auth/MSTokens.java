/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2023 VidTu
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

package ru.vidtu.ias.auth;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.utils.GSONUtils;

import java.lang.reflect.Type;

/**
 * A pair of Microsoft access and refresh tokens.
 *
 * @param access
 * @param refresh
 * @author VidTu
 * @apiNote The {@link #access()} provided is not suitable for {@link Account.LoginData#token()}
 * @see MSAuth#msacToMsaMsr(String)
 * @see MSAuth#msrToMsaMsr(String)
 * @see MSAuth#msaToXbl(String)
 */
public record MSTokens(String access, String refresh) {
    /**
     * GSON deserializer for {@link MSTokens}.
     *
     * @author VidTu
     * @apiNote API (decode-only) adapter
     */
    public static final class Adapter implements JsonDeserializer<MSTokens> {
        @Override
        public MSTokens deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            try {
                // Get the JSON.
                JsonObject json = element.getAsJsonObject();

                // Extract the tokens.
                String access = GSONUtils.getStringOrThrow(json, "access_token");
                String refresh = GSONUtils.getStringOrThrow(json, "refresh_token");

                // Create the tokens.
                return new MSTokens(access, refresh);
            } catch (Throwable t) {
                // Rethrow.
                throw new JsonParseException("Unable to parse Microsoft tokens: " + element, t);
            }
        }
    }
}
