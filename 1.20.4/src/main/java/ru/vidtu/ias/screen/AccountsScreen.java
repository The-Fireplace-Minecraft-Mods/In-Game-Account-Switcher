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

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class AccountsScreen extends Screen {
    /**
     * Parent screen, {@code null} if none.
     */
    final Screen parent;

    /**
     * Search widget.
     */
    EditBox search;

    /**
     * Account list widget.
     */
    AccountList list;

    /**
     * Player skin widget.
     */
    PlayerSkinWidget skin;

    /**
     * Login button.
     */
    Button login;

    /**
     * Offline login button.
     */
    Button offlineLogin;

    /**
     * Edit button.
     */
    Button edit;

    /**
     * Edit button.
     */
    Button delete;

    /**
     * Add button.
     */
    Button add;

    /**
     * Back button.
     */
    Button back;

    /**
     * Creates a new screen.
     *
     * @param parent Parent screen, {@code null} if none
     */
    public AccountsScreen(Screen parent) {
        super(Component.translatable("ias.accounts"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Bruh.
        assert this.minecraft != null;

        // Add search widget.
        this.search = new EditBox(this.font, this.width / 2 - 75, 11, 150, 20, this.search, Component.translatable("ias.accounts.search"));
        this.search.setHint(this.search.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        this.addRenderableWidget(this.search);

        // Add skin renderer.
        if (this.skin == null) {
            this.skin = new PlayerSkinWidget(85, 120, this.minecraft.getEntityModels(), () -> {
                // Return default if list is removed. (for whatever reason)
                if (this.list == null) return DefaultPlayerSkin.get(Util.NIL_UUID);

                // Return default if nothing is selected. (for whatever reason)
                AccountEntry selected = this.list.getSelected();
                if (selected == null) return DefaultPlayerSkin.get(Util.NIL_UUID);

                // Return skin of selected.
                return this.list.skin(selected);
            });
        }
        this.skin.setPosition(5, this.height / 2 - 60);
        this.addRenderableWidget(this.skin);

        // Add login button.
        this.login = Button.builder(Component.translatable("ias.accounts.login"), button -> this.list.login(true))
                .bounds(this.width / 2 - 50 - 100 - 4, this.height - 24 - 24, 100, 20).build();
        this.addRenderableWidget(this.login);

        // Add offline login button.
        this.offlineLogin = Button.builder(Component.translatable("ias.accounts.offlineLogin"), button -> this.list.login(false))
                .bounds(this.width / 2 - 50 - 100 - 4, this.height - 24, 100, 20).build();
        this.addRenderableWidget(this.offlineLogin);

        // Add edit button.
        this.edit = Button.builder(Component.translatable("ias.accounts.edit"), button -> this.list.edit())
                .bounds(this.width / 2 - 50, this.height - 24 - 24, 100, 20).build();
        this.addRenderableWidget(this.edit);

        // Add delete button.
        this.delete = Button.builder(Component.translatable("ias.accounts.delete"), button -> this.list.delete())
                .bounds(this.width / 2 - 50, this.height - 24, 100, 20).build();
        this.addRenderableWidget(this.delete);

        // Add edit button.
        this.add = Button.builder(Component.translatable("ias.accounts.add"), button -> this.list.add())
                .bounds(this.width / 2 + 50 + 4, this.height - 24 - 24, 100, 20).build();
        this.addRenderableWidget(this.add);

        // Add delete button.
        this.back = Button.builder(CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.parent))
                .bounds(this.width / 2 + 50 + 4, this.height - 24, 100, 20).build();
        this.addRenderableWidget(this.back);

        // Add account list.
        if (this.list != null) {
            this.list.setRectangle(this.width, this.height - 24 - 24 - 4 - 34, 0, 34);
        } else {
            this.list = new AccountList(this, this.minecraft, this.width, this.height - 24 - 24 - 4 - 34, 34, 12);
        }
        this.addRenderableWidget(this.list);

        // Update the list.
        this.search.setResponder(this.list::update);
        this.list.update(this.search.getValue());
        this.updateSelected();
    }

    @Override
    public void onClose() {
        // Bruh.
        assert this.minecraft != null;

        // Close to parent.
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Render background and widgets.
        super.render(graphics, mouseX, mouseY, delta);

        // Render title.
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 1, 0xFF_FF_FF_FF);
    }

    /**
     * Updates the selected entry.
     */
    void updateSelected() {
        // Get the selected.
        AccountEntry selected = this.list != null ? this.list.getSelected() : null;

        // Nothing is selected.
        if (selected == null) {
            // Disable every button.
            this.login.active = this.offlineLogin.active = this.edit.active = this.delete.active = false;

            // Hide tooltip, if exists.
            this.login.setTooltip(null);

            // Hide skin.
            this.skin.visible = false;

            // Stop here.
            return;
        }

        // Enable always-on buttons.
        this.offlineLogin.active = this.edit.active = this.delete.active = true;

        // Enable online login button if can log in.
        if (selected.account.canLogin()) {
            this.login.active = true;
        } else {
            this.login.setTooltip(null);
        }

        // Show skin.
        this.skin.visible = true;
    }
}
