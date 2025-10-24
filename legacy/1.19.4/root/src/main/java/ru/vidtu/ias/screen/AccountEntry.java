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

package ru.vidtu.ias.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.IASMinecraft;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.legacy.Skin;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Account GUI entry.
 *
 * @author VidTu
 */
final class AccountEntry extends ObjectSelectionList.Entry<AccountEntry> {
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
     * Account tooltip.
     */
    private final List<FormattedCharSequence> tooltip;

    /**
     * Last click time.
     */
    private long clicked = Util.getMillis();

    /**
     * Last non-hovered time.
     */
    private long lastFree = System.nanoTime();

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
        this.tooltip = Stream.of(
                CommonComponents.optionNameValue(Component.translatable("ias.accounts.tip.nick"), Component.literal(this.account.name())),
                CommonComponents.optionNameValue(Component.translatable("ias.accounts.tip.uuid"), Component.literal(this.account.uuid().toString())),
                CommonComponents.optionNameValue(Component.translatable("ias.accounts.tip.type"), Component.translatable(this.account.typeTipKey()))
        ).map(Component::getVisualOrderText).toList();
    }

    @Override
    public void render(PoseStack pose, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        // Render tooltip.
        if (hovered) {
            if ((System.nanoTime() - this.lastFree) >= 500_000_000L) {
                this.list.screen().setTooltipForNextRenderPass(this.tooltip);
            }
        } else {
            this.lastFree = System.nanoTime();
        }

        // Render the skin.
        Skin skin = this.list.skin(this);
        RenderSystem.setShaderTexture(0, skin.skin());
        PlayerFaceRenderer.draw(pose, x, y, 8);

        // Get the name color.
        User user = this.minecraft.getUser();
        int color;
        // Mods break user non-nullness.
        //noinspection ConstantValue
        if (user == null || !this.account.name().equalsIgnoreCase(user.getName())) {
            color = 0xFF_FF_FF_FF;
        } else if (this.account.uuid().equals(user.getProfileId())) {
            color = 0xFF_00_FF_00;
        } else if (this.account.name().equals(user.getName())) {
            color = 0xFF_FF_FF_00;
        } else {
            color = 0xFF_FF_80_00;
        }

        // Render name.
        this.minecraft.font.drawShadow(pose, this.account.name(), x + 10, y, color);

        // Render warning if insecure.
        if (this.account.insecure()) {
            boolean warning = (System.nanoTime() / 1_000_000_000L) % 2L == 0;
            RenderSystem.setShaderTexture(0, IASMinecraft.SPRITE);
            GuiComponent.blit(pose, x - 6, y - 1, 31, warning ? 10 : 0, 2, 10);
            if (mouseX >= x - 10 && mouseX <= x && mouseY >= y && mouseY <= y + height) {
                this.list.screen().setTooltipForNextRenderPass(Tooltip.create(Component.translatable("ias.accounts.tip.insecure")), DefaultTooltipPositioner.INSTANCE, true);
            }
        }

        // Render only for focused, selected or hovered.
        if (this.equals(this.list.getFocused()) || this.equals(this.list.getSelected())) {
            // Render up widget.
            int upV;
            int upX = x + width - 28;
            if (this == this.list.children().get(0)) {
                upV = 14;
            } else if (mouseX >= upX && mouseY >= y && mouseX <= upX + 11 && mouseY <= y + height) {
                upV = 7;
            } else {
                upV = 0;
            }
            RenderSystem.setShaderTexture(0, IASMinecraft.SPRITE);
            GuiComponent.blit(pose, upX, y, 20, upV, 11, 7);

            // Render down widget.
            int downV;
            int downX = x + width - 15;
            if (this == this.list.children().get(this.list.children().size() - 1)) {
                downV = 35;
            } else if (mouseX >= downX && mouseY >= y && mouseX <= downX + 11 && mouseY <= y + height) {
                downV = 28;
            } else {
                downV = 21;
            }
            RenderSystem.setShaderTexture(0, IASMinecraft.SPRITE);
            GuiComponent.blit(pose, downX, y, 20, downV, 11, 7);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Swap if selected.
        if (this.equals(this.list.getFocused()) || this.equals(this.list.getSelected())) {
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
        }

        // Login on double click.
        if (Util.getMillis() - this.clicked < 250L) {
            this.list.login(!Screen.hasShiftDown());
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
