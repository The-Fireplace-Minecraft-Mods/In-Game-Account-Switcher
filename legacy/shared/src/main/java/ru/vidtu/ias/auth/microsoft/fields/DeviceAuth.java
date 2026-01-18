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

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.utils.GSONUtils;

import java.net.URI;
import java.time.Duration;

/**
 * Device authentication response.
 *
 * @param device   Device code
 * @param user     User code
 * @param uri      User URI
 * @param expire   Expiration duration
 * @param interval Polling interval
 * @author VidTu
 */
public record DeviceAuth(@NotNull String device, @NotNull String user, @NotNull URI uri,
                         @NotNull Duration expire, @NotNull Duration interval) {
    /**
     * Extracts the device auth from the JSON.
     *
     * @param json Target JSON
     * @return Extracted MS tokens
     * @throws JsonParseException If unable to extract
     */
    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static DeviceAuth fromJson(@NotNull JsonObject json) {
        try {
            // Extract.
            String device = GSONUtils.getStringOrThrow(json, "device_code");
            String user = GSONUtils.getStringOrThrow(json, "user_code");
            String rawVerificationUri = GSONUtils.getStringOrThrow(json, "verification_uri");
            URI uri = new URI(rawVerificationUri).parseServerAuthority();
            if (!"https".equals(uri.getScheme())) {
                throw new IllegalStateException("Invalid URL scheme: " + uri);
            }
            long rawExpire = GSONUtils.getLongOrThrow(json, "expires_in");
            Duration expire = Duration.ofSeconds(rawExpire);
            if (expire.isNegative() || expire.isZero() || expire.toDays() > 2) {
                throw new IllegalStateException("Invalid expire: " + expire + " (" + rawExpire + ")");
            }
            long rawInterval = GSONUtils.getLongOrThrow(json, "interval");
            Duration interval = Duration.ofSeconds(rawInterval);
            if (interval.isNegative() || interval.isZero() || interval.compareTo(expire) >= 0) {
                throw new IllegalStateException("Invalid interval (with expire of " + expire + ": " + interval + " (" + rawInterval + ")");
            }

            // Create.
            return new DeviceAuth(device, user, uri, expire, interval);
        } catch (Throwable t) {
            // Rethrow.
            throw new JsonParseException("Unable to parse DeviceAuth: " + json, t);
        }
    }
}
