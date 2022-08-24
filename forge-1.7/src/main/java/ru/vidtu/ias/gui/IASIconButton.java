package ru.vidtu.ias.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;
import the_fireplace.ias.IAS;

public class IASIconButton extends GuiButton {
    public IASIconButton(int buttonId, int x, int y, int width, int height) {
        super(buttonId, x, y, width, height, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        mc.getTextureManager().bindTexture(IAS.IAS_BUTTON);
        GL11.glColor3f(1F, 1F, 1F);
        field_146123_n = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height; // hovered = ...
        this.drawTexturedModalRect(xPosition, yPosition, 0, height * (!enabled ? 2 : field_146123_n ? 1 : 0), width, height); // ... hovered ? 1 : 0 ...
    }
}
