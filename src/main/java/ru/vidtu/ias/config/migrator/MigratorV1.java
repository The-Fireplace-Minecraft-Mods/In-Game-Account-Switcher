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

package ru.vidtu.ias.config.migrator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.MicrosoftAccount;
import ru.vidtu.ias.account.OfflineAccount;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.config.IASStorage;
import ru.vidtu.ias.config.TextAlign;
import ru.vidtu.ias.crypt.Crypt;
import ru.vidtu.ias.crypt.DummyCrypt;
import ru.vidtu.ias.utils.GSONUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Config migrator for config version 1.
 *
 * @author VidTu
 */
final class MigratorV1 implements Migrator {
    @Override
    public void load(JsonObject json) {
        try {
            // First version doesn't actually have a version marker.
            if (json.has("version")) {
                throw new IllegalArgumentException("V1 shouldn't have any version markers: " + json.get("version"));
            }

            // Load old config.
            boolean titleText = json.has("text") ? GSONUtils.getBooleanOrThrow(json, "text") : IASConfig.titleText;
            String titleTextX = json.has("textX") ? GSONUtils.getStringOrThrow(json, "textX") : IASConfig.titleTextX;
            String titleTextY = json.has("textY") ? GSONUtils.getStringOrThrow(json, "textY") : IASConfig.titleTextY;
            TextAlign titleTextAlign = titleTextX != null && titleTextY != null ? TextAlign.CENTER : IASConfig.titleTextAlign;
            boolean titleButton = json.has("showOnTitleScreen") ? GSONUtils.getBooleanOrThrow(json, "titleScreenButton") : IASConfig.titleButton;
            String titleButtonX = json.has("btnX") ? GSONUtils.getStringOrThrow(json, "btnX") : IASConfig.titleButtonX;
            String titleButtonY = json.has("btnY") ? GSONUtils.getStringOrThrow(json, "btnY") : IASConfig.titleButtonY;

            // Load accounts.
            JsonArray rawAccounts = json.has("accounts") ? GSONUtils.getArrayOrThrow(json, "accounts") : new JsonArray(0);
            List<Account> accounts = new ArrayList<>(rawAccounts.size());
            for (JsonElement entry : rawAccounts) {
                // Extract data.
                JsonObject rawAccount = entry.getAsJsonObject();
                String type = GSONUtils.getStringOrThrow(rawAccount, "type");
                JsonObject rawData = GSONUtils.getObjectOrThrow(rawAccount, "data");

                // Create account by type.
                Account account = switch (type.toLowerCase(Locale.ROOT)) {
                    case "ias:microsoft", "ru.vidtu.ias.account.microsoftaccount" -> {
                        // Extract data.
                        UUID uuid = UUID.fromString(GSONUtils.getStringOrThrow(rawData, "uuid"));
                        String name = GSONUtils.getStringOrThrow(rawData, "username");
                        String accessToken = GSONUtils.getStringOrThrow(rawData, "accessToken");
                        String refreshToken = GSONUtils.getStringOrThrow(rawData, "refreshToken");

                        // Convert tokens.
                        byte[] unencrypted;
                        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream(accessToken.length() + refreshToken.length() + 4);
                             DataOutputStream out = new DataOutputStream(byteOut)) {
                            out.writeUTF(accessToken);
                            out.writeUTF(refreshToken);
                            unencrypted = byteOut.toByteArray();
                        }
                        byte[] data;
                        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream(unencrypted.length + 32);
                             DataOutputStream out = new DataOutputStream(byteOut)) {
                            DummyCrypt crypt = DummyCrypt.INSTANCE;
                            byte[] encrypted = crypt.encrypt(unencrypted);
                            Crypt.encrypt(out, crypt);
                            out.write(encrypted);
                            data = byteOut.toByteArray();
                        }

                        // Create.
                        yield new MicrosoftAccount(true, uuid, name, data);
                    }
                    case "ias:offline", "ru.vidtu.ias.account.offlineaccount" -> {
                        // Extract.
                        String name = GSONUtils.getStringOrThrow(rawData, "username");

                        // Create.
                        yield new OfflineAccount(name);
                    }
                    default -> null;
                };

                // Add if not-null.
                if (account == null) return;
                accounts.add(account);
            }

            // Migrate positions.
            titleButtonX = titleButtonX == null ? null : titleButtonX
                    .replace("w", "%width%")
                    .replace("W", "%width%")
                    .replace("h", "%height%")
                    .replace("H", "%height%");
            titleButtonY = titleButtonY == null ? null : titleButtonY
                    .replace("w", "%width%")
                    .replace("W", "%width%")
                    .replace("h", "%height%")
                    .replace("H", "%height%");

            // Flush config.
            IASConfig.titleText = titleText;
            IASConfig.titleTextX = titleTextX;
            IASConfig.titleTextY = titleTextY;
            IASConfig.titleTextAlign = titleTextAlign;
            IASConfig.titleButton = titleButton;
            IASConfig.titleButtonX = titleButtonX;
            IASConfig.titleButtonY = titleButtonY;

            // Flush storage.
            IASStorage.accounts.addAll(accounts);
            IASStorage.accounts = IASStorage.accounts.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
        } catch (Throwable t) {
            // Rethrow.
            String redacted = OBFUSCATE_LOGS.matcher(String.valueOf(json)).replaceAll("$1[TOKEN]");
            throw new JsonParseException("Unable to migrate V1 config: " + redacted, t);
        }
    }

    @Override
    public String toString() {
        return "MigratorV1{}";
    }
}
