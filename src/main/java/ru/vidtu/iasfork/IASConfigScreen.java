package ru.vidtu.iasfork;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import the_fireplace.ias.IAS;
import the_fireplace.ias.config.ConfigValues;

public class IASConfigScreen extends GuiScreen {
	public final GuiScreen prev;
	public GuiCheckBox caseS, relog;
	public GuiTextField textX, textY;
	public IASConfigScreen(GuiScreen prev) {
		this.prev = prev;
	}
	
	@Override
	public void initGui() {
		addButton(caseS = new GuiCheckBox(-1, width / 2 - fontRenderer.getStringWidth(I18n.format(ConfigValues.CASESENSITIVE_NAME)) / 2 - 10, 40, I18n.format(ConfigValues.CASESENSITIVE_NAME), ConfigValues.CASESENSITIVE));
		addButton(relog = new GuiCheckBox(-2, width / 2 - fontRenderer.getStringWidth(I18n.format(ConfigValues.ENABLERELOG_NAME)) / 2 - 10, 60, I18n.format(ConfigValues.ENABLERELOG_NAME), ConfigValues.ENABLERELOG));
		textX = new GuiTextField(-3, fontRenderer, width / 2 - 100, 90, 98, 20);
		textY = new GuiTextField(-4, fontRenderer, width / 2 + 2, 90, 98, 20);
		addButton(new GuiButton(0, width / 2 - 75, height - 24, 150, 20, I18n.format("gui.done")));
		if (ConfigValues.TEXT_X != null) textX.setText(ConfigValues.TEXT_X);
		if (ConfigValues.TEXT_Y != null) textY.setText(ConfigValues.TEXT_Y);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) mc.displayGuiScreen(prev);
	}
	
	@Override
	public void onGuiClosed() {
		ConfigValues.CASESENSITIVE = caseS.isChecked();
		ConfigValues.ENABLERELOG = relog.isChecked();
		ConfigValues.TEXT_X = textX.getText();
		ConfigValues.TEXT_Y = textY.getText();
		IAS.syncConfig(true);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		textX.textboxKeyTyped(typedChar, keyCode);
		textY.textboxKeyTyped(typedChar, keyCode);
		super.keyTyped(typedChar, keyCode);
	}
	
	@Override
	public void updateScreen() {
		textX.updateCursorCounter();
		textY.updateCursorCounter();
		super.updateScreen();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		textX.mouseClicked(mouseX, mouseY, mouseButton);
		textY.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void drawScreen(int mx, int my, float delta) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, "ias.properties", width / 2, 10, -1);
		drawCenteredString(fontRenderer, I18n.format(ConfigValues.TEXT_POS_NAME), width / 2, 80, -1);
		textX.drawTextBox();
		textY.drawTextBox();
		super.drawScreen(mx, my, delta);
	}
}
