package ru.vidtu.iasfork.msauth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

public class MSAuthScreen extends GuiScreen {
	public static final String[] symbols = new String[]{"▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃", "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
			"_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅", "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆", "_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇", "_ _ _ _ _ ▃ ▄ ▅ ▆ ▇ █",
			"_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇", "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆", "_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅", "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
			"▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃", "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _", "▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _", "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
			"▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _", "█ ▇ ▆ ▅ ▄ ▃ _ _ _ _ _", "▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _", "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
			"▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _", "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _"};
	public GuiScreen prev;
	public List<String> text = new ArrayList<>();
	public boolean endTask = false;
	public int tick;
	public MSAuthScreen(GuiScreen prev) {
		this.prev = prev;
		AuthSys.start(this);
	}
	
	@Override
	public void initGui() {
		addButton(new GuiButton(0, width / 2 - 50, height - 24, 100, 20, I18n.format("gui.cancel")));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) mc.displayGuiScreen(prev);
	}
	
	@Override
	public void updateScreen() {
		tick++;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float delta) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.format("ias.msauth.title"), width / 2, 10, -1);
		for (int i = 0; i < text.size(); i++) {
			drawCenteredString(fontRenderer, text.get(i), width / 2, height / 2 + i * 10, -1);
		}
		if (!endTask) drawCenteredString(fontRenderer, symbols[tick % symbols.length], width / 2, height / 3 * 2, -256);
		super.drawScreen(mouseX, mouseY, delta);
	}

	@Override
	public void onGuiClosed() {
		AuthSys.stop();
		super.onGuiClosed();
	}
	
	public void setState(String s) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.addScheduledTask(() -> this.text = mc.fontRenderer.listFormattedStringToWidth(I18n.format(s), width));
	}
	
	public void error(String error) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.addScheduledTask(() -> {
			this.text = mc.fontRenderer.listFormattedStringToWidth(TextFormatting.RED + I18n.format("ias.msauth.error", error), width);
			endTask = true;
		});
	}
}
