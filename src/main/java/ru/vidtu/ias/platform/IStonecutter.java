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

package ru.vidtu.ias.platform;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A helper class that contains methods that depend on Stonecutter, a Java source code preprocessor.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class IStonecutter {
    /**
     * Game root directory.
     */
    //? if fabric {
    public static final Path GAME_DIRECTORY = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir();
    //?} else if neoforge {
    /*public static final Path GAME_DIRECTORY = net.neoforged.fml.loading.FMLPaths.GAMEDIR.get();
     *///?} else
    /*public static final Path GAME_DIRECTORY = net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get();*/

    /**
     * Game config directory.
     */
    //? if fabric {
    public static final Path CONFIG_DIRECTORY = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
    //?} else if neoforge {
    /*public static final Path CONFIG_DIRECTORY = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
     *///?} else
    /*public static final Path CONFIG_DIRECTORY = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();*/

    /**
     * A duration for tooltips in version-dependant units. Currently {@code 250} milliseconds.
     */
    //? if >=1.20.6 {
    private static final java.time.Duration TOOLTIP_DURATION = java.time.Duration.ofMillis(250L);
    //?} else if >=1.19.4 {
    /*public static final int TOOLTIP_DURATION = 250; // Millis.
    *///?} else
    /*public static final long TOOLTIP_DURATION = 250_000_000L;*/ // Nanos.

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private IStonecutter() {
        throw new AssertionError("No instances.");
    }

    /**
     * Creates a new identifier (resource location) with the "{@code ias}" namespace.
     *
     * @param path Identifier path (value)
     * @return A new identifier
     */
    @Contract(value = "_ -> new", pure = true)
    public static ResourceLocation newIdentifier(String path) {
        //? if >=1.21.1 || (forge && (!legacyNeoForge) && >=1.18.2 && !1.20.2) {
        return ResourceLocation.fromNamespaceAndPath("ias", path);
        //?} else
        /*return new ResourceLocation("ias", path);*/
    }

    /**
     * Creates a new translatable component.
     *
     * @param key Translation key
     * @return A new translatable component
     */
    @Contract(value = "_ -> new", pure = true)
    public static MutableComponent translate(String key) {
        //? if >=1.19.2 {
        return Component.translatable(key);
        //?} else
        /*return new net.minecraft.network.chat.TranslatableComponent(key);*/
    }

    /**
     * Creates a new translatable component.
     *
     * @param key  Translation key
     * @param args Translation args
     * @return A new translatable component
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static MutableComponent translate(String key, Object... args) {
        //? if >=1.19.2 {
        return Component.translatable(key, args);
        //?} else
        /*return new net.minecraft.network.chat.TranslatableComponent(key, args);*/
    }
    
    /**
     * Creates a new GUI button instance.
     *
     * @param font            Font renderer used by the GUI
     * @param x               Button X position
     * @param y               Button Y position
     * @param width           Button width in scaled pixels
     * @param height          Button height in scaled pixels
     * @param message         Button label
     * @param tooltip         Button tooltip
     * @param handler         Button click handler (button itself and tooltip setter)
     * @param tooltipRenderer Last pass tooltip renderer
     * @return A new button instance
     */
    @Contract(value = "_, _, _, _, _, _, _, _, _ -> new", pure = true)
    public static Button guiButton(@SuppressWarnings("unused") Font font, int x, int y, int width, int height, // <- Used before 1.19.4.
                                   Component message, Component tooltip, BiConsumer<Button, Consumer<Component>> handler,
                                   @SuppressWarnings("unused") Consumer<List<FormattedCharSequence>> tooltipRenderer) { // <- Used before 1.19.4.
        //? if >=1.19.4 {
        Button button = Button.builder(message, btn -> handler.accept(btn, tip -> {
            btn.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tip));
            btn.setTooltipDelay(TOOLTIP_DURATION);
        })).tooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip)).bounds(x, y, width, height).build();
        button.setTooltipDelay(TOOLTIP_DURATION);
        return button;
        //?} else {
        /*org.apache.commons.lang3.mutable.Mutable<List<FormattedCharSequence>> tipHolder = new org.apache.commons.lang3.mutable.MutableObject<>(font.split(tooltip, 170));
        return new Button(x, y, width, height, message, btn -> handler.accept(btn, tip -> tipHolder.setValue(font.split(tip, 170)))) {
            /^*
             * Last time when the mouse was NOT over this element in units of {@link System#nanoTime()}.
             ^/
            private long lastAway = System.nanoTime();

            @Override
            public void renderButton(com.mojang.blaze3d.vertex.PoseStack graphics, int mouseX, int mouseY, float delta) {
                // Render the element itself.
                super.renderButton(graphics, mouseX, mouseY, delta);

                // Button is not hovered, update the state.
                if (!this.isHovered) {
                    this.lastAway = System.nanoTime();
                    return;
                }

                // Button is not hovered for enough time.
                if ((System.nanoTime() - this.lastAway) < TOOLTIP_DURATION) return;

                // Render the tooltip.
                tooltipRenderer.accept(tipHolder.getValue());
            }
        };
        *///?}
    }

    /**
     * Creates a new GUI checkbox instance.
     *
     * @param font            Font renderer used by the GUI
     * @param x               Checkbox X position
     * @param y               Checkbox Y position
     * @param message         Checkbox label
     * @param tooltip         Checkbox tooltip
     * @param check           Whether the checkbox is checked
     * @param handler         Checkbox click handler
     * @param tooltipRenderer Last pass tooltip renderer
     * @return A new checkbox instance
     */
    @SuppressWarnings("BooleanParameter") // <- Boolean method used as a state, not as control flow. (checkbox "checked" state)
    @Contract(value = "_, _, _, _, _, _, _, _ -> new", pure = true)
    public static Checkbox guiCheckbox(Font font, int x, int y, Component message, Component tooltip,
                                       boolean check, BooleanConsumer handler,
                                       @SuppressWarnings("unused") Consumer<List<FormattedCharSequence>> tooltipRenderer) { // <- Used before 1.19.4.
        //? if >=1.20.4 {
        Checkbox box = Checkbox.builder(message, font)
                .pos(x - ((font.width(message) + 24) / 2), y)
                .selected(check)
                .onValueChange((checkbox, value) -> handler.accept(value))
                .build();
        //?} else {
        /*int width = font.width(message) + 24;
        Checkbox box = new Checkbox(x - (width / 2), y, width, 20, message, check) {
            @Override
            public void onPress() {
                // Toggle the checkbox.
                super.onPress();

                // Invoke the handler.
                handler.accept(this.selected());
            }

            //? if <1.19.4 {
            /^/^¹*
             * A tooltip split to {@code 170} scaled pixels wide, a value used in modern versions
             ¹^/
            private final List<FormattedCharSequence> tip = font.split(tooltip, 170);

            /^¹*
             * Last time when the mouse was NOT over this element in units of {@link System#nanoTime()}.
             ¹^/
            private long lastAway = System.nanoTime();

            @Override
            public void renderButton(com.mojang.blaze3d.vertex.PoseStack graphics, int mouseX, int mouseY, float delta) {
                // Render the element itself.
                super.renderButton(graphics, mouseX, mouseY, delta);

                // Button is not hovered, update the state.
                if (!this.isHovered) {
                    this.lastAway = System.nanoTime();
                    return;
                }

                // Button is not hovered for enough time.
                if ((System.nanoTime() - this.lastAway) < TOOLTIP_DURATION) return;

                // Render the tooltip.
                tooltipRenderer.accept(this.tip);
            }
            ^///?}
        };
        *///?}
        //? if >=1.19.4 {
        box.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip));
        box.setTooltipDelay(TOOLTIP_DURATION);
        //?}
        return box;
    }
}
