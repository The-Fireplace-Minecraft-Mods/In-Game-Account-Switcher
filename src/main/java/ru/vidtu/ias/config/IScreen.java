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

package ru.vidtu.ias.config;

import com.google.common.base.Strings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.platform.IStonecutter;
import ru.vidtu.ias.utils.Expression;

import java.time.Duration;
import java.util.List;

/**
 * IAS config screen.
 *
 * @author VidTu
 */
public final class IScreen extends Screen {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LogManager.getLogger("IAS/IScreen");

    /**
     * Parent screen, {@code null} if none.
     */
    private final Screen parent;

    /**
     * Creates a new screen.
     *
     * @param parent Parent screen, {@code null} if none
     */
    public IScreen(Screen parent) {
        super(Component.translatable("ias.config"));
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

        // Title Text.
        int centerX = (this.width / 2);
        this.addRenderableWidget(IStonecutter.guiCheckbox(this.font, centerX, 20, IStonecutter.translate("ias.title.text"),
                IStonecutter.translate("ias.title.text.tip"), IConfig.titleText(),
                IConfig::titleText, this::tooltip));

        // Title Text X.
        int leftThirdX = (centerX - 151);
        EditBox titleTextX = new EditBox(this.font, leftThirdX, 20 + 24, 98, 20, Component.translatable("ias.title.text.x"));
        titleTextX.setHint(titleTextX.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        titleTextX.setTooltip(Tooltip.create(Component.translatable("ias.title.text.x.tip", Component.translatable("key.keyboard.left.alt"))));
        titleTextX.setTooltipDelay(Duration.ofMillis(250L));
        titleTextX.setMaxLength(128);
        titleTextX.setResponder(value -> {
            // Update the value.
            IConfig.titleTextX(value);

            // Update the color.
            titleTextX.setTextColor(Expression.positionValidityColor(value, this.width, this.height, true));
        });
        titleTextX.setValue(Strings.nullToEmpty(IConfig.titleTextX()));
        this.addRenderableWidget(titleTextX);

        // Title Text Y.
        int middleThirdX = (centerX - 49);
        EditBox titleTextY = new EditBox(this.font, middleThirdX, 20 + 24, 98, 20, Component.translatable("ias.title.text.y"));
        titleTextY.setHint(titleTextY.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        titleTextY.setTooltip(Tooltip.create(Component.translatable("ias.title.text.y.tip", Component.translatable("key.keyboard.left.alt"))));
        titleTextY.setTooltipDelay(Duration.ofMillis(250L));
        titleTextY.setMaxLength(128);
        titleTextY.setResponder(value -> {
            // Update the value.
            IConfig.titleTextY(value);

            // Update the color.
            titleTextY.setTextColor(Expression.positionValidityColor(value, this.width, this.height, false));
        });
        titleTextY.setValue(Strings.nullToEmpty(IConfig.titleTextY()));
        this.addRenderableWidget(titleTextY);

        // Title Text Align.
        int rightThirdX = (centerX + 50);
        TextAlign titleAlign = IConfig.titleTextAlign();
        this.addRenderableWidget(IStonecutter.guiButton(this.font, rightThirdX, 20 + 24, 98, 20, titleAlign.titleLabel(), titleAlign.titleTip(), (button, tipSetter) -> {
            // Update the alignment.
            TextAlign newAlign = IConfig.cycleTitleTextAlign(/*back=*/hasShiftDown());

            // Update the label and tooltip.
            button.setMessage(newAlign.titleLabel());
            tipSetter.accept(newAlign.titleTip());
        }, this::tooltip));

        // Title Button.
        this.addRenderableWidget(IStonecutter.guiCheckbox(this.font, centerX, 20 + (24 * 2), IStonecutter.translate("ias.title.button"),
                IStonecutter.translate("ias.title.button.tip"), IConfig.titleButton(),
                IConfig::titleButton, this::tooltip));

        // Title Button X.
        int leftX = (centerX - 100);
        EditBox titleButtonX = new EditBox(this.font, leftX, 20 + (24 * 3), 98, 20, Component.translatable("ias.title.button.x"));
        titleButtonX.setHint(titleButtonX.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        titleButtonX.setTooltip(Tooltip.create(Component.translatable("ias.title.button.x.tip", Component.translatable("key.keyboard.left.alt"))));
        titleButtonX.setTooltipDelay(Duration.ofMillis(250L));
        titleButtonX.setMaxLength(128);
        titleButtonX.setResponder(value -> {
            // Update the value.
            IConfig.titleButtonX(value);

            // Update the color.
            titleButtonX.setTextColor(Expression.positionValidityColor(value, this.width, this.height, true));
        });
        titleButtonX.setValue(Strings.nullToEmpty(IConfig.titleButtonX()));
        this.addRenderableWidget(titleButtonX);

        // Title Button Y.
        int rightX = (centerX + 2);
        EditBox titleButtonY = new EditBox(this.font, rightX, 20 + (24 * 3), 98, 20, Component.translatable("ias.title.button.y"));
        titleButtonY.setHint(titleButtonY.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        titleButtonY.setTooltip(Tooltip.create(Component.translatable("ias.title.button.y.tip", Component.translatable("key.keyboard.left.alt"))));
        titleButtonY.setTooltipDelay(Duration.ofMillis(250L));
        titleButtonY.setMaxLength(128);
        titleButtonY.setResponder(value -> {
            // Update the value.
            IConfig.titleButtonY(value);

            // Update the color.
            titleButtonY.setTextColor(Expression.positionValidityColor(value, this.width, this.height, false));
        });
        titleButtonY.setValue(Strings.nullToEmpty(IConfig.titleButtonY()));
        this.addRenderableWidget(titleButtonY);

        // Servers Text.
        this.addRenderableWidget(IStonecutter.guiCheckbox(this.font, centerX, 20 + (24 * 4), IStonecutter.translate("ias.servers.text"),
                IStonecutter.translate("ias.servers.text.tip"), IConfig.serversText(),
                IConfig::serversText, this::tooltip));

        // Servers Text X.
        EditBox serversTextX = new EditBox(this.font, leftThirdX, 20 + (24 * 5), 98, 20, Component.translatable("ias.servers.text.x"));
        serversTextX.setHint(serversTextX.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        serversTextX.setTooltip(Tooltip.create(Component.translatable("ias.servers.text.x.tip", Component.translatable("key.keyboard.left.alt"))));
        serversTextX.setTooltipDelay(Duration.ofMillis(250L));
        serversTextX.setMaxLength(128);
        serversTextX.setResponder(value -> {
            // Update the value.
            IConfig.serversTextX(value);

            // Update the color.
            serversTextX.setTextColor(Expression.positionValidityColor(value, this.width, this.height, true));
        });
        serversTextX.setValue(Strings.nullToEmpty(IConfig.serversTextX()));
        this.addRenderableWidget(serversTextX);

        // Servers Text Y.
        EditBox serversTextY = new EditBox(this.font, middleThirdX, 20 + (24 * 5), 98, 20, Component.translatable("ias.servers.text.y"));
        serversTextY.setHint(serversTextY.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        serversTextY.setTooltip(Tooltip.create(Component.translatable("ias.servers.text.y.tip", Component.translatable("key.keyboard.left.alt"))));
        serversTextY.setTooltipDelay(Duration.ofMillis(250L));
        serversTextY.setMaxLength(128);
        serversTextY.setResponder(value -> {
            // Update the value.
            IConfig.serversTextY(value);

            // Update the color.
            serversTextY.setTextColor(Expression.positionValidityColor(value, this.width, this.height, false));
        });
        serversTextY.setValue(Strings.nullToEmpty(IConfig.serversTextY()));
        this.addRenderableWidget(serversTextY);

        // Servers Text Align.
        TextAlign serversAlign = IConfig.serversTextAlign();
        this.addRenderableWidget(IStonecutter.guiButton(this.font, rightThirdX, 20 + (24 * 5), 98, 20, serversAlign.serversLabel(), serversAlign.serversTip(), (button, tipSetter) -> {
            // Update the alignment.
            TextAlign newAlign = IConfig.cycleServersTextAlign(/*back=*/hasShiftDown());

            // Update the label and tooltip.
            button.setMessage(newAlign.serversLabel());
            tipSetter.accept(newAlign.serversTip());
        }, this::tooltip));

        // Servers Button.
        this.addRenderableWidget(IStonecutter.guiCheckbox(this.font, centerX, 20 + (24 * 6), IStonecutter.translate("ias.servers.button"),
                IStonecutter.translate("ias.servers.button.tip"), IConfig.serversButton(),
                IConfig::serversButton, this::tooltip));

        // Servers Button X.
        EditBox serversButtonX = new EditBox(this.font, leftX, 20 + (24 * 7), 98, 20, Component.translatable("ias.servers.button.x"));
        serversButtonX.setHint(serversButtonX.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        serversButtonX.setTooltip(Tooltip.create(Component.translatable("ias.servers.button.x.tip", Component.translatable("key.keyboard.left.alt"))));
        serversButtonX.setTooltipDelay(Duration.ofMillis(250L));
        serversButtonX.setMaxLength(128);
        serversButtonX.setResponder(value -> {
            // Update the value.
            IConfig.serversButtonX(value);

            // Update the color.
            serversButtonX.setTextColor(Expression.positionValidityColor(value, this.width, this.height, true));
        });
        serversButtonX.setValue(Strings.nullToEmpty(IConfig.serversButtonX()));
        this.addRenderableWidget(serversButtonX);

        // Servers Button Y.
        EditBox serversButtonY = new EditBox(this.font, rightX, 20 + (24 * 7), 98, 20, Component.translatable("ias.servers.button.y"));
        serversButtonY.setHint(serversButtonY.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        serversButtonY.setTooltip(Tooltip.create(Component.translatable("ias.servers.button.y.tip", Component.translatable("key.keyboard.left.alt"))));
        serversButtonY.setTooltipDelay(Duration.ofMillis(250L));
        serversButtonY.setMaxLength(128);
        serversButtonY.setResponder(value -> {
            // Update the value.
            IConfig.serversButtonY(value);

            // Update the color.
            serversButtonY.setTextColor(Expression.positionValidityColor(value, this.width, this.height, false));
        });
        serversButtonY.setValue(Strings.nullToEmpty(IConfig.serversButtonY()));
        this.addRenderableWidget(serversButtonY);

        // Sun Server.
        ServerMode mode = IConfig.server();
        this.addRenderableWidget(IStonecutter.guiButton(this.font, leftX, 20 + (24 * 8), 200, 20, mode.label(), mode.tip(), (button, tipSetter) -> {
            // Update the mode.
            ServerMode newMode = IConfig.cycleServer(/*back=*/hasShiftDown());

            // Update the label and tooltip.
            button.setMessage(newMode.label());
            tipSetter.accept(newMode.tip());
        }, this::tooltip));

        // Password Echoing.
        this.addRenderableWidget(IStonecutter.guiCheckbox(this.font, centerX, 20 + (24 * 9), IStonecutter.translate("ias.passwordEchoing"),
                IStonecutter.translate("ias.passwordEchoing.tip"), IConfig.passwordEchoing(),
                IConfig::passwordEchoing, this::tooltip));

        // Bar Nick.
        this.addRenderableWidget(IStonecutter.guiCheckbox(this.font, centerX, 20 + (24 * 10), IStonecutter.translate("ias.barNick"),
                IStonecutter.translate("ias.barNick.tip"), IConfig.barNick(),
                IConfig::barNick, this::tooltip));

        // Add done button.
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 24, 200, 20)
                .build());
    }

    @Override
    public void onClose() {
        // Bruh.
        assert this.minecraft != null;

        // Save config.
        try {
            IConfig.save();
        } catch (Throwable t) {
            LOGGER.error("IAS: Unable to save config.", t);
        }

        // Close to parent.
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Render background and widgets.
        super.render(graphics, mouseX, mouseY, delta);

        // Render title.
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 5, 0xFF_FF_FF_FF);

        // Render current mouse pos if alt is held.
        if (Screen.hasAltDown()) {
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(0.0F, 0.0F, 2.0F);
            graphics.renderTooltip(this.font, Component.translatable("ias.config.mousePos", mouseX, mouseY), mouseX, mouseY);
            pose.popPose();
        }
    }

    private void tooltip(List<FormattedCharSequence> tooltip) {
        // Currently NO-OP, will be handy <1.19.4
    }

    @Override
    public String toString() {
        return "ConfigScreen{}";
    }
}
