package ru.vidtu.ias.mixins;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.IASMMPos;
import ru.vidtu.ias.utils.Expression;
import the_fireplace.ias.IAS;
import the_fireplace.ias.gui.GuiAccountSelector;
import the_fireplace.ias.gui.GuiButtonWithImage;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
	private static int textX, textY;
	private TitleScreenMixin() {
		super(null);
	}
	
	@Inject(method = "init", at = @At("TAIL"))
	public void onInit(CallbackInfo ci) {
		try {
			if (StringUtils.isNotBlank(Config.textX) && StringUtils.isNotBlank(Config.textY)) {
				textX = (int) new Expression(Config.textX.replace("%width%", Integer.toString(width)).replace("%height%", Integer.toString(height))).parse(0);
				textY = (int) new Expression(Config.textY.replace("%width%", Integer.toString(width)).replace("%height%", Integer.toString(height))).parse(0);
			} else {
				textX = width / 2;
				textY = height / 4 + 48 + 72 + 12 + (IAS.modMenu?32:22);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			textX = width / 2;
			textY = height / 4 + 48 + 72 + 12 + (IAS.modMenu?32:22);
		}
		if (Config.showOnTitleScreen) {
			int btnX = width / 2 + 104;
			int btnY = height / 4 + 48 + 72 + (IAS.modMenu?IASMMPos.buttonOffset():-12);
			try {
				if (StringUtils.isNotBlank(Config.btnX) && StringUtils.isNotBlank(Config.btnY)) {
					btnX = (int) new Expression(Config.btnX.replace("%width%", Integer.toString(width)).replace("%height%", Integer.toString(height))).parse(0);
					btnY = (int) new Expression(Config.btnY.replace("%width%", Integer.toString(width)).replace("%height%", Integer.toString(height))).parse(0);
				}
			} catch (Throwable t) {
				t.printStackTrace();
				btnX = width / 2 + 104;
				btnY = height / 4 + 48 + 72 + (IAS.modMenu?IASMMPos.buttonOffset():-12);
			}
			addDrawableChild(new GuiButtonWithImage(btnX, btnY, btn -> client.setScreen(new GuiAccountSelector(this))));
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void onRender(MatrixStack ms, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		drawCenteredText(ms, textRenderer, new TranslatableText("ias.loggedinas", client.getSession().getUsername()), textX, textY, 0xFFCC8888);
	}
}
