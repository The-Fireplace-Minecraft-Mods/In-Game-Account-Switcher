package ru.vidtu.ias.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLockIconButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import the_fireplace.ias.IAS;

public class IASIconButton extends GuiButton {
    public IASIconButton(int buttonId, int x, int y, int width, int height) {
        super(buttonId, x, y, width, height, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        mc.getTextureManager().bindTexture(IAS.IAS_BUTTON);
        GlStateManager.color(1F, 1F, 1F);
        hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
        this.drawTexturedModalRect(xPosition, yPosition, 0, height * (!enabled ? 2 : hovered ? 1 : 0), width, height);
    }
}
