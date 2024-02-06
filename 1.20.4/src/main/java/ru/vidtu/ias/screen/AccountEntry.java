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

package ru.vidtu.ias.screen;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
     * Up button sprites.
     */
    private static final WidgetSprites UP = new WidgetSprites(
            new ResourceLocation("ias", "up_plain"),
            new ResourceLocation("ias", "up_disabled"),
            new ResourceLocation("ias", "up_focus")
    );

    /**
     * Down button sprites.
     */
    private static final WidgetSprites DOWN = new WidgetSprites(
            new ResourceLocation("ias", "down_plain"),
            new ResourceLocation("ias", "down_disabled"),
            new ResourceLocation("ias", "down_focus")
    );

    /**
     * Minecraft instance.
     */
    private final Minecraft minecraft;

    /**
     * Parent list.
     */
    private final AccountList list;

    /**
     * IAS account.
     */
    private final Account account;

    /**
     * Last click time.
     */
    private long clicked = Util.getMillis();

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

        // Render only for focused, selected or hovered.
        if (this.equals(this.list.getFocused()) || this.equals(this.list.getSelected())) {
            // Render up widget.
            ResourceLocation upTexture;
            int upX = x + width - 28;
            if (this == this.list.children().get(0)) {
                upTexture = UP.disabled();
            } else if (mouseX >= upX && mouseY >= y && mouseX <= upX + 11 && mouseY <= y + height) {
                upTexture = UP.enabledFocused();
            } else {
                upTexture = UP.enabled();
            }
            graphics.blitSprite(upTexture, upX, y, 11, 7);

            // Render down widget.
            ResourceLocation downTexture;
            int downX = x + width - 15;
            if (this == this.list.children().get(this.list.children().size() - 1)) {
                downTexture = DOWN.disabled();
            } else if (mouseX >= downX && mouseY >= y && mouseX <= downX + 11 && mouseY <= y + height) {
                downTexture = DOWN.enabledFocused();
            } else {
                downTexture = DOWN.enabled();
            }
            graphics.blitSprite(downTexture, downX, y, 11, 7);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int right = this.list.getRowRight();

        // Up widget.
        int upX = right - 28;
        if (mouseX >= upX && mouseX <= upX + 11) {
            this.list.swapUp(this);
            return true;
        }

        // Down widget.
        int downX = right - 15;
        if (mouseX >= downX && mouseX <= downX + 11) {
            this.list.swapDown(this);
            return true;
        }

        // Login on double click.
        if (Util.getMillis() - this.clicked < 250L) {
            this.list.login(Screen.hasShiftDown());
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

    /**
     * Gets the account.
     *
     * @return IAS account
     */
    Account account() {
        return this.account;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AccountEntry that)) return false;
        return Objects.equals(this.account, that.account);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.account);
    }

    @Override
    public String toString() {
        return "AccountEntry{" +
                "account=" + this.account +
                '}';
    }
}
