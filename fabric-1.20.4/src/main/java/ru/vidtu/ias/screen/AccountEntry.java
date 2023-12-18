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

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.account.Account;

import java.util.Objects;

/**
 * Account GUI entry.
 *
 * @author VidTu
 */
final class AccountEntry extends ObjectSelectionList.Entry<AccountEntry> {
    /**
     * Minecraft instance.
     */
    final Minecraft minecraft;

    /**
     * Parent list.
     */
    final AccountList list;

    /**
     * IAS account.
     */
    final Account account;

    /**
     * Last click time.
     */
    long clicked = Util.getMillis();

    /**
     * Creates a new account list entry widget.
     *
     * @param minecraft Minecraft instance
     * @param list      Parent list
     * @param account   IAS account
     */
    AccountEntry(Minecraft minecraft, AccountList list, Account account) {
        this.minecraft = minecraft;
        this.list = list;
        this.account = account;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        // Render the skin.
        PlayerSkin skin = this.list.skin(this);
        PlayerFaceRenderer.draw(graphics, skin, x, y, 8);

        // Get the name color.
        User user = this.minecraft.getUser();
        @SuppressWarnings("ConstantValue") // <- Mods break user non-nullness.
        int color = user != null && this.account.is(user.getProfileId(), user.getName()) ? 0xFF_00_FF_00 : 0xFF_FF_FF_FF;

        // Render the name.
        graphics.drawString(this.minecraft.font, this.account.name(), x + 10, y, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Login on double click.
        if (Util.getMillis() - this.clicked < 250L) {
            this.list.login(true);
        }

        // Set time for double click.
        this.clicked = Util.getMillis();
        return true;
    }

    @Override
    @NotNull
    public Component getNarration() {
        return Component.literal(this.account.name());
    }

    @Override
    public String toString() {
        return "AccountEntry{" +
                "account=" + this.account +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.account);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AccountEntry that)) return false;
        return Objects.equals(this.account, that.account);
    }
}
