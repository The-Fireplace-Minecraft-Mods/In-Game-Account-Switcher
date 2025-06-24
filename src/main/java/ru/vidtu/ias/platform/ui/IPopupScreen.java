package ru.vidtu.ias.platform.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class IPopupScreen extends IScreen {
    public IPopupScreen(Component title, Screen parent) {
        super(title, parent);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Render parent behind.
        if (this.parent != null) {
            var pose = graphics.pose();
            //? if <1.21.6 {
            /*pose.pushPose();
            pose.translate(0.0F, 0.0F, -1000.0F);
            *///?}
            this.parent.renderWithTooltip(graphics, 0, 0, delta);
            //? if >=1.21.6 {
            graphics.nextStratum();
            //?} else
            /*pose.popPose();*/
            graphics.fill(0, 0, this.width, this.height, 0x80_00_00_00);
        } else {
            super.renderBackground(graphics, mouseX, mouseY, delta);
        }
    }
}
