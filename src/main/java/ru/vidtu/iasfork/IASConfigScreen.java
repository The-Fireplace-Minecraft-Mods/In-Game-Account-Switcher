package ru.vidtu.iasfork;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import the_fireplace.ias.IAS;
import the_fireplace.ias.config.ConfigValues;

public class IASConfigScreen extends Screen {
	public final Screen prev;
	public CheckboxWidget caseS, relog, mpscreen;
	public TextFieldWidget textX, textY;
	public IASConfigScreen(Screen prev) {
		super(new LiteralText("ias.properties"));
		this.prev = prev;
	}
	
	@Override
	protected void init() {
		addDrawableChild(caseS = new CheckboxWidget(width / 2 - textRenderer.getWidth(new TranslatableText(ConfigValues.CASESENSITIVE_NAME)) / 2 - 24, 40, 20, 20, new TranslatableText(ConfigValues.CASESENSITIVE_NAME), ConfigValues.CASESENSITIVE));
		addDrawableChild(relog = new CheckboxWidget(width / 2 - textRenderer.getWidth(new TranslatableText(ConfigValues.ENABLERELOG_NAME)) / 2 - 24, 60, 20, 20, new TranslatableText(ConfigValues.ENABLERELOG_NAME), ConfigValues.ENABLERELOG));
		addDrawableChild(textX = new TextFieldWidget(textRenderer, width / 2 - 100, 90, 98, 20, new LiteralText("X")));
		addDrawableChild(textY = new TextFieldWidget(textRenderer, width / 2 + 2, 90, 98, 20, new LiteralText("Y")));
		addDrawableChild(mpscreen = new CheckboxWidget(width / 2 - textRenderer.getWidth(new TranslatableText(ConfigValues.SHOW_ON_MULTIPLAYER_SCREEN_NAME)) / 2 - 24, 112, 20, 20, new TranslatableText(ConfigValues.SHOW_ON_MULTIPLAYER_SCREEN_NAME), ConfigValues.SHOW_ON_MULTIPLAYER_SCREEN));
		addDrawableChild(new ButtonWidget(width / 2 - 75, height - 24, 150, 20, new TranslatableText("gui.done"), btn -> {
			client.setScreen(prev);
		}));
		if (ConfigValues.TEXT_X != null) textX.setText(ConfigValues.TEXT_X);
		if (ConfigValues.TEXT_Y != null) textY.setText(ConfigValues.TEXT_Y);
	}
	
	@Override
	public void tick() {
		textX.tick();
		textY.tick();
		super.tick();
	}
	
	@Override
	public void removed() {
		ConfigValues.CASESENSITIVE = caseS.isChecked();
		ConfigValues.ENABLERELOG = relog.isChecked();
		ConfigValues.TEXT_X = textX.getText();
		ConfigValues.TEXT_Y = textY.getText();
		ConfigValues.SHOW_ON_MULTIPLAYER_SCREEN = mpscreen.isChecked();
		IAS.syncConfig(true);
	}
	
	@Override
	public void render(MatrixStack ms, int mx, int my, float delta) {
		renderBackground(ms);
		drawCenteredText(ms, textRenderer, this.title, width / 2, 10, -1);
		drawCenteredText(ms, textRenderer, new TranslatableText(ConfigValues.TEXT_POS_NAME), width / 2, 80, -1);
		super.render(ms, mx, my, delta);
	}
}
