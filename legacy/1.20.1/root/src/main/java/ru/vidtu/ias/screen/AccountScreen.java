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

package ru.vidtu.ias.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.config.IASStorage;
import ru.vidtu.ias.legacy.Skin;
import ru.vidtu.ias.legacy.SkinWidget;

public final class AccountScreen extends Screen {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("IAS/AccountScreen");

    /**
     * Parent screen, {@code null} if none.
     */
    private final Screen parent;

    /**
     * Search widget.
     */
    private EditBox search;

    /**
     * Account list widget.
     */
    private AccountList list;

    /**
     * Player skin widget.
     */
    private SkinWidget skin;

    /**
     * Login button.
     */
    private Button login;

    /**
     * Offline login button.
     */
    private Button offlineLogin;

    /**
     * Edit button.
     */
    private Button edit;

    /**
     * Edit button.
     */
    private Button delete;

    /**
     * Creates a new screen.
     *
     * @param parent Parent screen, {@code null} if none
     */
    public AccountScreen(Screen parent) {
        super(Component.translatable("ias.accounts"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Bruh.
        assert this.minecraft != null;

        // Disabled check.
        if (IAS.disabled()) {
            this.minecraft.setScreen(new AlertScreen(this::onClose, Component.translatable("ias.disabled.title").withStyle(ChatFormatting.RED),
                    Component.translatable("ias.disabled.text"), CommonComponents.GUI_BACK, true));
            return;
        }

        // Disclaimer.
        if (!IASStorage.gameDisclaimerShown) {
            this.minecraft.setScreen(new AlertScreen(() -> {
                // Save disclaimer.
                try {
                    IAS.gameDisclaimerShownStorage();
                } catch (Throwable t) {
                    LOGGER.error("Unable to set or write game disclaimer state.", t);
                }

                // Set screen.
                this.minecraft.setScreen(this);
            }, Component.translatable("ias.disclaimer.title").withStyle(ChatFormatting.YELLOW),
                    Component.translatable("ias.disclaimer.text"), CommonComponents.GUI_CONTINUE, false));
            return;
        }

        // Add search widget.
        this.search = new EditBox(this.font, this.width / 2 - 75, 11, 150, 20, this.search, Component.translatable("ias.accounts.search"));
        this.search.setHint(this.search.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        this.addRenderableWidget(this.search);

        // Add skin renderer.
        if (this.skin == null) {
            this.skin = new SkinWidget(5, this.height / 2 - 60, 85, 120, this.minecraft.getEntityModels(), () -> {
                // Return default if list is removed. (for whatever reason)
                if (this.list == null) return Skin.getDefault(Util.NIL_UUID);

                // Return default if nothing is selected. (for whatever reason)
                AccountEntry selected = this.list.getSelected();
                if (selected == null) return Skin.getDefault(Util.NIL_UUID);

                // Return skin of selected.
                return this.list.skin(selected);
            });
        }
        this.addRenderableWidget(this.skin);

        // Add login button.
        this.login = Button.builder(Component.translatable("ias.accounts.login"), btn -> this.list.login(true))
                .bounds(this.width / 2 - 50 - 100 - 4, this.height - 24 - 24, 100, 20).build();
        this.addRenderableWidget(this.login);

        // Add offline login button.
        this.offlineLogin = Button.builder(Component.translatable("ias.accounts.offlineLogin"), btn -> this.list.login(false))
                .bounds(this.width / 2 - 50 - 100 - 4, this.height - 24, 100, 20)
                .build();
        this.addRenderableWidget(this.offlineLogin);

        // Add edit button.
        this.edit = Button.builder(Component.translatable("ias.accounts.edit"), btn -> this.list.edit())
                .bounds(this.width / 2 - 50, this.height - 24 - 24, 100, 20)
                .build();
        this.addRenderableWidget(this.edit);

        // Add delete button.
        this.delete = Button.builder(Component.translatable("ias.accounts.delete"), btn -> this.list.delete(!Screen.hasShiftDown()))
                .bounds(this.width / 2 - 50, this.height - 24, 100, 20)
                .build();
        this.addRenderableWidget(this.delete);

        // Add edit button.
        this.addRenderableWidget(Button.builder(Component.translatable("ias.accounts.add"), btn -> this.list.add())
                .bounds(this.width / 2 + 50 + 4, this.height - 24 - 24, 100, 20)
                .build());

        // Add delete button.
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, btn -> this.minecraft.setScreen(this.parent))
                .bounds(this.width / 2 + 50 + 4, this.height - 24, 100, 20)
                .build());

        // Add account list.
        if (this.list != null) {
            this.list.updateSize(this.width, this.height - 24 - 24 - 4 - 34, 34, this.height - 24 - 24 - 4);
        } else {
            this.list = new AccountList(this, this.minecraft, this.width, this.height - 24 - 24 - 4 - 34, 34, this.height - 24 - 24 - 4, 12);
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
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);

        // Render title.
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 1, 0xFF_FF_FF_FF);
    }

    @Override
    public void tick() {
        super.tick();
        this.search.tick();
    }

    /**
     * Gets the search.
     *
     * @return Search widget
     */
    EditBox search() {
        return this.search;
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

        // Enable online login button if we can log in.
        if (selected.account().canLogin()) {
            this.login.active = true;
            this.login.setTooltip(null);
        } else {
            this.login.active = false;
            this.login.setTooltip(Tooltip.create(Component.translatable("ias.accounts.login.offline")));
            this.login.setTooltipDelay(-1);
        }

        // Show skin.
        this.skin.visible = true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        // Bruh.
        assert this.minecraft != null;

        // Shift+Down or Page Down to swap down.
        if ((key == GLFW.GLFW_KEY_DOWN && Screen.hasShiftDown()) || key == GLFW.GLFW_KEY_PAGE_DOWN) {
            this.list.swapDown(this.list.getSelected());
            return true;
        }

        // Shift+Up or Page Up to swap up.
        if ((key == GLFW.GLFW_KEY_UP && Screen.hasShiftDown()) || key == GLFW.GLFW_KEY_PAGE_UP) {
            this.list.swapUp(this.list.getSelected());
            return true;
        }

        // Ctrl+C to copy name. (Ctrl+Shift+C to copy UUID) {
        if (key == GLFW.GLFW_KEY_C && Screen.hasControlDown()) {
            AccountEntry selected = this.list.getSelected();
            if (selected != null) {
                Account account = selected.account();
                this.minecraft.keyboardHandler.setClipboard(Screen.hasShiftDown() ? account.uuid().toString() : account.name());
                return true;
            }
        }

        // Skip if handled by super.
        if (super.keyPressed(key, scan, mods)) {
            return true;
        }

        // Enter or Numpad Enter to log in.
        if (CommonInputs.selected(key)) {
            this.list.login(!Screen.hasShiftDown());
            return true;
        }

        // Delete or Numpad Minus to delete.
        if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_KP_SUBTRACT) {
            this.list.delete(!Screen.hasShiftDown());
            return true;
        }

        // CTRL+N or Numpad Plus to add.
        if ((key == GLFW.GLFW_KEY_N && Screen.hasControlDown()) || key == GLFW.GLFW_KEY_KP_ADD) {
            this.list.add();
            return true;
        }

        // CTRL+R or Numpad Asterisk to edit.
        if ((key == GLFW.GLFW_KEY_R && Screen.hasControlDown()) || key == GLFW.GLFW_KEY_KP_MULTIPLY) {
            this.list.edit();
            return true;
        }

        // Not handled.
        return false;
    }

    @Override
    public String toString() {
        return "AccountScreen{" +
                "list=" + this.list +
                '}';
    }
}
