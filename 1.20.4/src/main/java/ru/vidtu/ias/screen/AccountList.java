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

package ru.vidtu.ias.screen;

import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.config.IASStorage;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Account GUI list.
 *
 * @author VidTu
 */
final class AccountList extends ObjectSelectionList<AccountEntry> {
    /**
     * Skins cache.
     */
    private static final Map<UUID, PlayerSkin> SKINS = new WeakHashMap<>();

    /**
     * Parent screen.
     */
    final AccountsScreen screen;

    /**
     * Creates a new accounts list widget.
     *
     * @param minecraft Minecraft instance
     * @param width     List width
     * @param height    List height
     * @param offset    List Y offset
     * @param item      Entry height
     */
    AccountList(AccountsScreen screen, Minecraft minecraft, int width, int height, int offset, int item) {
        super(minecraft, width, height, offset, item);
        this.screen = screen;
        this.update(this.screen.search.getValue());
    }

    @Override
    public int getRowWidth() {
        return Math.min(super.getRowWidth(), this.screen.width - (85 + 10) * 2);
    }

    @Override
    public void setSelected(@Nullable AccountEntry entry) {
        // Select.
        super.setSelected(entry);

        // Notify parent.
        this.screen.updateSelected();
    }

    /**
     * Update the list by query.
     *
     * @param query Search query
     */
    void update(String query) {
        // Add all if blank.
        if (query == null || query.isBlank()) {
            // Add every account.
            AccountEntry selected = this.getSelected();
            this.replaceEntries(IASStorage.accounts.stream()
                    .map(account -> new AccountEntry(this.minecraft, this, account))
                    .toList());
            this.setSelected(selected);

            // Notify the root.
            this.screen.updateSelected();

            // Don't process search.
            return;
        }

        // Lowercase query.
        String lowerQuery = query.toLowerCase(Locale.ROOT);

        // Add every account.
        AccountEntry selected = this.getSelected();
        this.replaceEntries(IASStorage.accounts.stream()
                .filter(account -> account.name().toLowerCase(Locale.ROOT).contains(lowerQuery))
                .sorted((f, s) -> Boolean.compare(
                        s.name().toLowerCase(Locale.ROOT).startsWith(lowerQuery),
                        f.name().toLowerCase(Locale.ROOT).startsWith(lowerQuery)
                ))
                .map(account -> new AccountEntry(this.minecraft, this, account))
                .toList());
        this.setSelected(selected);

        // Notify the root.
        this.screen.updateSelected();
    }

    /**
     * Log in to this account.
     *
     * @param online Whether to try using online authentication
     * @apiNote The {@code online} parameter may be ignored if the current account doesn't support online authentication
     */
    void login(boolean online) {
        // Skip if nothing is selected.
        AccountEntry selected = this.getSelected();
        if (selected == null) return;
        Account account = selected.account;

        // Check if should log in online.
        if (online && account.canLogin()) {
            // Initialize and set the login screen.
            LoginPopupScreen login = new LoginPopupScreen(this.screen);
            this.minecraft.setScreen(login);

            // Start login.
            IAS.executor().execute(() -> account.login(login));

            // Don't process further.
            return;
        }

        // Initialize and set the login screen.
        LoginPopupScreen login = new LoginPopupScreen(this.screen);
        this.minecraft.setScreen(login);

        // Login offline.
        Account.LoginData data = new Account.LoginData(account.name(), account.uuid(), "ias:offline", Account.LoginData.LEGACY);
        login.success(data);
    }

    void edit() {
        TinyFileDialogs.tinyfd_messageBox("IAS", "Not implemented yet.", "ok", "error", true);
    }

    /**
     * Deletes the selected account.
     * Does nothing if nothing is selected.
     */
    void delete() {
        // Skip if nothing is selected.
        AccountEntry selected = this.getSelected();
        if (selected == null) return;
        Account account = selected.account;

        // Skip confirmation if shift is pressed.
        if (Screen.hasShiftDown()) {
            IASStorage.accounts.remove(account);
            IAS.saveStorageSafe();
            IAS.disclaimersStorage();
            this.update(this.screen.search.getValue());
            return;
        }

        // Display confirmation screen.
        this.minecraft.setScreen(new ConfirmScreen(result -> {
            // Delete if confirmed.
            if (result) {
                IASStorage.accounts.remove(account);
                IAS.saveStorageSafe();
                IAS.disclaimersStorage();
                this.update(this.screen.search.getValue());
            }

            // Display accounts screen again.
            this.minecraft.setScreen(this.screen);
        }, Component.translatable("ias.delete", account.name()),
                Component.translatable("ias.delete.hint", Component.translatable("key.keyboard.left.shift"))));
    }

    /**
     * Opens the account adding screen.
     */
    void add() {
        this.minecraft.setScreen(new AddPopupScreen(this.screen));
    }

    /**
     * Gets the skin for the account entry.
     *
     * @param entry Target account entry
     * @return Player skin, fetched or default
     */
    PlayerSkin skin(AccountEntry entry) {
        // Get and return the skin if already stored.
        UUID uuid = entry.account.uuid();
        PlayerSkin skin = SKINS.get(uuid);
        if (skin != null) return skin;

        // Quickly put the replacer to avoid fetch spam.
        skin = DefaultPlayerSkin.get(uuid);
        SKINS.put(uuid, skin);

        // Skip fetching offline skins.
        if (uuid.version() != 4) return skin;

        // Load the skin.
        CompletableFuture.supplyAsync(() -> {
            // Fetch the profile
            ProfileResult result = this.minecraft.getMinecraftSessionService().fetchProfile(uuid, false);

            // Skip if profile is null.
            if (result == null) return null;

            // Return the profile.
            return result.profile();
        }, IAS.executor()).thenComposeAsync(profile -> {
            // Skip if profile is null.
            if (profile == null) return CompletableFuture.completedFuture(null);

            // Load the skin.
            return this.minecraft.getSkinManager().getOrLoad(profile);
        }, IAS.executor()).thenAcceptAsync(loaded -> {
            // Skip if skin is null.
            if (loaded == null) return;

            // Put into map.
            SKINS.put(uuid, loaded);
        }, this.minecraft).exceptionally(t -> {
            // Log it.
            IAS.LOG.warn("IAS: Unable to load skin: {}", entry, t);

            // Return null.
            return null;
        });

        // Return quick skin.
        return skin;
    }
}
