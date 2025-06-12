package ru.vidtu.ias.platform.ui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class IButton extends Button {
    public IButton(int x, int y, int width, int height, Component message,
                   @Nullable Component tooltip, Runnable handler) {
        this(x, y, width, height, message, tooltip, ignored -> handler.run());
    }

    public IButton(int x, int y, int width, int height, Component message,
                   @Nullable Component tooltip, Consumer<IButton> handler) {
        super(x, y, width, height, message, button -> handler.accept((IButton) button), DEFAULT_NARRATION);
        this.setTooltipDelay(IScreen.TOOLTIP_DURATION);
        if (tooltip == null) return;
        this.setTooltip(Tooltip.create(tooltip));
    }
}
