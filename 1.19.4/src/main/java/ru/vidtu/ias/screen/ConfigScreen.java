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

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.config.ServerMode;
import ru.vidtu.ias.config.TextAlign;
import ru.vidtu.ias.utils.Expression;

import java.util.Objects;

/**
 * IAS config screen.
 *
 * @author VidTu
 */
public final class ConfigScreen extends Screen {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("IAS/ConfigScreen");

    /**
     * Parent screen, {@code null} if none.
     */
    private final Screen parent;

    /**
     * Title text X.
     */
    private EditBox titleTextX;

    /**
     * Title text Y.
     */
    private EditBox titleTextY;

    /**
     * Title text align.
     */
    private Button titleTextAlign;

    /**
     * Title button X.
     */
    private EditBox titleButtonX;

    /**
     * Title button Y.
     */
    private EditBox titleButtonY;

    /**
     * Servers text X.
     */
    private EditBox serversTextX;

    /**
     * Servers text Y.
     */
    private EditBox serversTextY;

    /**
     * Servers text align.
     */
    private Button serversTextAlign;

    /**
     * Servers button X.
     */
    private EditBox serversButtonX;

    /**
     * Servers button Y.
     */
    private EditBox serversButtonY;

    /**
     * Creates a new screen.
     *
     * @param parent Parent screen, {@code null} if none
     */
    public ConfigScreen(Screen parent) {
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
        CallbackCheckbox box = new CallbackCheckbox(this.font, 5, 20, Component.translatable("ias.config.titleText"), IASConfig.titleText, value -> {
            IASConfig.titleText = value;
            this.titleTextX.active = value;
            this.titleTextY.active = value;
            this.titleTextX.setEditable(value);
            this.titleTextY.setEditable(value);
            this.titleTextAlign.active = value;
        });
        box.setTooltip(Tooltip.create(Component.translatable("ias.config.titleText.tip")));
        box.setTooltipDelay(250);
        this.addRenderableWidget(box);

        // Title Text X.
        this.titleTextX = new EditBox(this.font, 9 + box.getWidth(), 20, 75, 20, this.titleTextX, Component.translatable("ias.config.titleText.x"));
        this.titleTextX.setHint(this.titleTextX.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        this.titleTextX.setTooltip(Tooltip.create(Component.translatable("ias.config.titleText.x.tip", Component.translatable("key.keyboard.left.alt"))));
        this.titleTextX.setTooltipDelay(250);
        this.titleTextX.active = box.selected();
        this.titleTextX.setEditable(box.selected());
        this.titleTextX.setMaxLength(128);
        this.titleTextX.setResponder(value -> {
            value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            IASConfig.titleTextX = value;
            this.titleTextX.setTextColor(Expression.positionValidityColor(value, this.width, this.height, true));
        });
        this.titleTextX.setValue(Objects.requireNonNullElse(IASConfig.titleTextX, ""));
        this.addRenderableWidget(this.titleTextX);

        // Title Text Y.
        this.titleTextY = new EditBox(this.font, 88 + box.getWidth(), 20, 75, 20, this.titleTextY, Component.translatable("ias.config.titleText.y"));
        this.titleTextY.setHint(this.titleTextY.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        this.titleTextY.setTooltip(Tooltip.create(Component.translatable("ias.config.titleText.y.tip", Component.translatable("key.keyboard.left.alt"))));
        this.titleTextY.setTooltipDelay(250);
        this.titleTextY.active = box.selected();
        this.titleTextY.setEditable(box.selected());
        this.titleTextY.setMaxLength(128);
        this.titleTextY.setResponder(value -> {
            value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            IASConfig.titleTextY = value;
            this.titleTextY.setTextColor(Expression.positionValidityColor(value, this.width, this.height, false));
        });
        this.titleTextY.setValue(Objects.requireNonNullElse(IASConfig.titleTextY, ""));
        this.addRenderableWidget(this.titleTextY);

        // Title Text Align.
        this.titleTextAlign = Button.builder(CommonComponents.optionNameValue(Component.translatable("ias.config.titleTextAlign"), Component.translatable(IASConfig.titleTextAlign.toString())), btn -> {
                    // This could be implemented with indexing, but there aren't too many options.
                    IASConfig.titleTextAlign = switch (IASConfig.titleTextAlign) {
                        case LEFT -> TextAlign.CENTER;
                        case CENTER -> TextAlign.RIGHT;
                        case RIGHT -> TextAlign.LEFT;
                    };
                    btn.setMessage(CommonComponents.optionNameValue(Component.translatable("ias.config.titleTextAlign"), Component.translatable(IASConfig.titleTextAlign.toString())));
                })
                .bounds(167 + box.getWidth(), 20, Math.min(150, Math.max(20, this.width - 171 - box.getWidth())), 20)
                .build();
        this.titleTextAlign.active = box.selected();
        this.titleTextAlign.setTooltip(Tooltip.create(Component.translatable("ias.config.titleTextAlign.tip")));
        this.titleTextAlign.setTooltipDelay(250);
        this.addRenderableWidget(this.titleTextAlign);

        // Title Button.
        box = new CallbackCheckbox(this.font, 5, 44, Component.translatable("ias.config.titleButton"), IASConfig.titleButton, value -> {
            IASConfig.titleButton = value;
            this.titleButtonX.active = value;
            this.titleButtonY.active = value;
            this.titleButtonX.setEditable(value);
            this.titleButtonY.setEditable(value);
        });
        box.setTooltip(Tooltip.create(Component.translatable("ias.config.titleButton.tip")));
        box.setTooltipDelay(250);
        this.addRenderableWidget(box);

        // Title Button X.
        this.titleButtonX = new EditBox(this.font, 9 + box.getWidth(), 44, 75, 20, this.titleButtonX, Component.translatable("ias.config.titleButton.x"));
        this.titleButtonX.setHint(this.titleButtonX.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        this.titleButtonX.setTooltip(Tooltip.create(Component.translatable("ias.config.titleButton.x.tip", Component.translatable("key.keyboard.left.alt"))));
        this.titleButtonX.setTooltipDelay(250);
        this.titleButtonX.active = box.selected();
        this.titleButtonX.setEditable(box.selected());
        this.titleButtonX.setMaxLength(128);
        this.titleButtonX.setResponder(value -> {
            value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            IASConfig.titleButtonX = value;
            this.titleButtonX.setTextColor(Expression.positionValidityColor(value, this.width, this.height, true));
        });
        this.titleButtonX.setValue(Objects.requireNonNullElse(IASConfig.titleButtonX, ""));
        this.addRenderableWidget(this.titleButtonX);

        // Title Button Y.
        this.titleButtonY = new EditBox(this.font, 88 + box.getWidth(), 44, 75, 20, this.titleButtonY, Component.translatable("ias.config.titleButton.y"));
        this.titleButtonY.setHint(this.titleButtonY.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        this.titleButtonY.setTooltip(Tooltip.create(Component.translatable("ias.config.titleButton.y.tip", Component.translatable("key.keyboard.left.alt"))));
        this.titleButtonY.setTooltipDelay(250);
        this.titleButtonY.active = box.selected();
        this.titleButtonY.setEditable(box.selected());
        this.titleButtonY.setMaxLength(128);
        this.titleButtonY.setResponder(value -> {
            value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            IASConfig.titleButtonY = value;
            this.titleButtonY.setTextColor(Expression.positionValidityColor(value, this.width, this.height, false));
        });
        this.titleButtonY.setValue(Objects.requireNonNullElse(IASConfig.titleButtonY, ""));
        this.addRenderableWidget(this.titleButtonY);

        // Servers Text.
        box = new CallbackCheckbox(this.font, 5, 68, Component.translatable("ias.config.serversText"), IASConfig.serversText, value -> {
            IASConfig.serversText = value;
            this.serversTextX.active = value;
            this.serversTextY.active = value;
            this.serversTextX.setEditable(value);
            this.serversTextY.setEditable(value);
            this.serversTextAlign.active = value;
        });
        box.setTooltip(Tooltip.create(Component.translatable("ias.config.serversText.tip")));
        box.setTooltipDelay(250);
        this.addRenderableWidget(box);

        // Servers Text X.
        this.serversTextX = new EditBox(this.font, 9 + box.getWidth(), 68, 75, 20, this.serversTextX, Component.translatable("ias.config.serversText.x"));
        this.serversTextX.setHint(this.serversTextX.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        this.serversTextX.setTooltip(Tooltip.create(Component.translatable("ias.config.serversText.x.tip", Component.translatable("key.keyboard.left.alt"))));
        this.serversTextX.setTooltipDelay(250);
        this.serversTextX.active = box.selected();
        this.serversTextX.setEditable(box.selected());
        this.serversTextX.setMaxLength(128);
        this.serversTextX.setResponder(value -> {
            value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            IASConfig.serversTextX = value;
            this.serversTextX.setTextColor(Expression.positionValidityColor(value, this.width, this.height, true));
        });
        this.serversTextX.setValue(Objects.requireNonNullElse(IASConfig.serversTextX, ""));
        this.addRenderableWidget(this.serversTextX);

        // Servers Text Y.
        this.serversTextY = new EditBox(this.font, 88 + box.getWidth(), 68, 75, 20, this.serversTextY, Component.translatable("ias.config.serversText.y"));
        this.serversTextY.setHint(this.serversTextY.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        this.serversTextY.setTooltip(Tooltip.create(Component.translatable("ias.config.serversText.y.tip", Component.translatable("key.keyboard.left.alt"))));
        this.serversTextY.setTooltipDelay(250);
        this.serversTextY.active = box.selected();
        this.serversTextY.setEditable(box.selected());
        this.serversTextY.setMaxLength(128);
        this.serversTextY.setResponder(value -> {
            value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            IASConfig.serversTextY = value;
            this.serversTextY.setTextColor(Expression.positionValidityColor(value, this.width, this.height, false));
        });
        this.serversTextY.setValue(Objects.requireNonNullElse(IASConfig.serversTextY, ""));
        this.addRenderableWidget(this.serversTextY);

        // Servers Text Align.
        this.serversTextAlign = Button.builder(CommonComponents.optionNameValue(Component.translatable("ias.config.serversTextAlign"), Component.translatable(IASConfig.serversTextAlign.toString())), btn -> {
                    // This could be implemented with indexing, but there aren't too many options.
                    IASConfig.serversTextAlign = switch (IASConfig.serversTextAlign) {
                        case LEFT -> TextAlign.CENTER;
                        case CENTER -> TextAlign.RIGHT;
                        case RIGHT -> TextAlign.LEFT;
                    };
                    btn.setMessage(CommonComponents.optionNameValue(Component.translatable("ias.config.serversTextAlign"), Component.translatable(IASConfig.serversTextAlign.toString())));
                })
                .bounds(167 + box.getWidth(), 68, Math.min(150, Math.max(20, this.width - 171 - box.getWidth())), 20)
                .build();
        this.serversTextAlign.active = box.selected();
        this.serversTextAlign.setTooltip(Tooltip.create(Component.translatable("ias.config.serversTextAlign.tip")));
        this.serversTextAlign.setTooltipDelay(250);
        this.addRenderableWidget(this.serversTextAlign);

        // Servers Button.
        box = new CallbackCheckbox(this.font, 5, 92, Component.translatable("ias.config.serversButton"), IASConfig.serversButton, value -> {
            IASConfig.serversButton = value;
            this.serversButtonX.active = value;
            this.serversButtonY.active = value;
            this.serversButtonX.setEditable(value);
            this.serversButtonY.setEditable(value);
        });
        box.setTooltip(Tooltip.create(Component.translatable("ias.config.serversButton.tip")));
        box.setTooltipDelay(250);
        this.addRenderableWidget(box);

        // Servers Button X.
        this.serversButtonX = new EditBox(this.font, 9 + box.getWidth(), 92, 75, 20, this.serversButtonX, Component.translatable("ias.config.serversButton.x"));
        this.serversButtonX.setHint(this.serversButtonX.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        this.serversButtonX.setTooltip(Tooltip.create(Component.translatable("ias.config.serversButton.x.tip", Component.translatable("key.keyboard.left.alt"))));
        this.serversButtonX.setTooltipDelay(250);
        this.serversButtonX.active = box.selected();
        this.serversButtonX.setEditable(box.selected());
        this.serversButtonX.setMaxLength(128);
        this.serversButtonX.setResponder(value -> {
            value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            IASConfig.serversButtonX = value;
            this.serversButtonX.setTextColor(Expression.positionValidityColor(value, this.width, this.height, true));
        });
        this.serversButtonX.setValue(Objects.requireNonNullElse(IASConfig.serversButtonX, ""));
        this.addRenderableWidget(this.serversButtonX);

        // Servers Button Y.
        this.serversButtonY = new EditBox(this.font, 88 + box.getWidth(), 92, 75, 20, this.serversButtonY, Component.translatable("ias.config.serversButton.y"));
        this.serversButtonY.setHint(this.serversButtonY.getMessage().copy().withStyle(ChatFormatting.DARK_GRAY));
        this.serversButtonY.setTooltip(Tooltip.create(Component.translatable("ias.config.serversButton.y.tip", Component.translatable("key.keyboard.left.alt"))));
        this.serversButtonY.setTooltipDelay(250);
        this.serversButtonY.active = box.selected();
        this.serversButtonY.setEditable(box.selected());
        this.serversButtonY.setMaxLength(128);
        this.serversButtonY.setResponder(value -> {
            value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            IASConfig.serversButtonY = value;
            this.serversButtonY.setTextColor(Expression.positionValidityColor(value, this.width, this.height, false));
        });
        this.serversButtonY.setValue(Objects.requireNonNullElse(IASConfig.serversButtonY, ""));
        this.addRenderableWidget(this.serversButtonY);

        // No Crypt Button.
        box = new CallbackCheckbox(this.font, 5, 116, Component.translatable("ias.config.allowNoCrypt"), IASConfig.allowNoCrypt, value -> IASConfig.allowNoCrypt = value);
        box.setTooltip(Tooltip.create(Component.translatable("ias.config.allowNoCrypt.tip")));
        box.setTooltipDelay(250);
        this.addRenderableWidget(box);

        // Sun Server.
        Button button = Button.builder(CommonComponents.optionNameValue(Component.translatable("ias.config.server"), Component.translatable(IASConfig.server.toString())), btn -> {
            // Update the value.
            IASConfig.server = switch (IASConfig.server) {
                case ALWAYS -> ServerMode.AVAILABLE;
                case AVAILABLE -> ServerMode.NEVER;
                case NEVER -> ServerMode.ALWAYS;
            };

            // Set the message.
            btn.setMessage(CommonComponents.optionNameValue(Component.translatable("ias.config.server"), Component.translatable(IASConfig.server.toString())));
        }).bounds(9 + box.getWidth(), 116, 200, 20).tooltip(Tooltip.create(Component.translatable("ias.config.server.tip"))).build();
        button.setTooltipDelay(250);
        this.addRenderableWidget(button);

        // Nick Warns.
        box = new CallbackCheckbox(this.font, 5, 140, Component.translatable("ias.config.nickWarns"), IASConfig.nickWarns, value -> IASConfig.nickWarns = value);
        box.setTooltip(Tooltip.create(Component.translatable("ias.config.nickWarns.tip")));
        box.setTooltipDelay(250);
        this.addRenderableWidget(box);

        // Password Echoing.
        box = new CallbackCheckbox(this.font, 10 + box.getWidth(), 140, Component.translatable("ias.config.passwordEchoing"), IASConfig.passwordEchoing, value -> IASConfig.passwordEchoing = value);
        box.setTooltip(Tooltip.create(Component.translatable("ias.config.passwordEchoing.tip")));
        box.setTooltipDelay(250);
        this.addRenderableWidget(box);

        // Unexpected Pigs.
        box = new CallbackCheckbox(this.font, 5, 164, Component.translatable("ias.config.unexpectedPigs"), IASConfig.unexpectedPigs, value -> {
            IASConfig.unexpectedPigs = value;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(value ? SoundEvents.PIG_AMBIENT : SoundEvents.PIG_DEATH, 1.0F));
        });
        box.setTooltip(Tooltip.create(Component.translatable("ias.config.unexpectedPigs.tip")));
        box.setTooltipDelay(250);
        this.addRenderableWidget(box);

        // Bar Name.
        box = new CallbackCheckbox(this.font, 5, 188, Component.translatable("ias.config.barNick"), IASConfig.barNick, value -> {
            IASConfig.barNick = value;
            this.minecraft.updateTitle();
        });
        box.setTooltip(Tooltip.create(Component.translatable("ias.config.barNick.tip")));
        box.setTooltipDelay(250);
        this.addRenderableWidget(box);

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
            IAS.disclaimersStorage();
            IAS.saveConfig();
        } catch (Throwable t) {
            LOGGER.error("IAS: Unable to save config.", t);
        }

        // Close to parent.
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float delta) {
        // Render background and widgets.
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, delta);

        // Render title.
        drawCenteredString(pose, this.font, this.title, this.width / 2, 5, 0xFF_FF_FF_FF);

        // Render current mouse pos if alt is held.
        if (Screen.hasAltDown()) {
            pose.pushPose();
            pose.translate(0.0F, 0.0F, 2.0F);
            this.renderTooltip(pose, Component.translatable("ias.config.mousePos", mouseX, mouseY), mouseX, mouseY);
            pose.popPose();
        }
    }

    @Override
    public String toString() {
        return "ConfigScreen{}";
    }

    /**
     * Checkbox with a callback.
     *
     * @author Vidtu
     */
    private static final class CallbackCheckbox extends Checkbox {
        /**
         * Check callback.
         */
        private final BooleanConsumer callback;

        /**
         * Creates a new check callback.
         *
         * @param font Target font
         * @param x        Target X
         * @param y        Target Y
         * @param label    Checkbox label
         * @param check    Checkbox check status
         * @param callback Checkbox check callback
         */
        private CallbackCheckbox(Font font, int x, int y, Component label, boolean check, BooleanConsumer callback) {
            super(x, y, font.width(label) + 24, 20, label, check);
            this.callback = callback;
        }

        @Override
        public void onPress() {
            super.onPress();
            this.callback.accept(this.selected());
        }
    }
}
