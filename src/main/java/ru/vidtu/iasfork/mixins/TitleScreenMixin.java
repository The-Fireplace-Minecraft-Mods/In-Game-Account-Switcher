package ru.vidtu.iasfork.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.mrebhan.ingameaccountswitcher.tools.Config;

import jdk.jshell.JShell;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import ru.vidtu.iasfork.IASMMPos;
import the_fireplace.ias.config.ConfigValues;
import the_fireplace.ias.gui.GuiAccountSelector;
import the_fireplace.ias.gui.GuiButtonWithImage;
import the_fireplace.ias.tools.SkinTools;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
	private static boolean skinsLoaded, modMenu = false;
	private static int textX, textY;
	protected TitleScreenMixin(Text title) {
		super(title);
	}
	
	@Inject(method = "init", at = @At("TAIL"))
	public void onInit(CallbackInfo ci) {
		if (!skinsLoaded) {
			SkinTools.cacheSkins(false);
			modMenu = FabricLoader.getInstance().isModLoaded("modmenu");
			skinsLoaded = true;
		}
		textX = textY = Integer.MIN_VALUE;
		new Thread(() -> { //VERY, VERY HACKY CODE, but no external dependencies.
			if (ConfigValues.TEXT_X.isEmpty() && ConfigValues.TEXT_Y.isEmpty()) {
				textX = width / 2;
				textY = height / 4 + 48 + 72 + 12 + (modMenu?32:22);
				return;
			}
			try (JShell js = JShell.create()) {
	            SnippetEvent s1 = js.eval(ConfigValues.TEXT_X.replace("%width%", Integer.toString(width)).replace("%height%", Integer.toString(height))).stream().filter(c -> c.status() == Status.VALID).findAny().orElse(null);
	            SnippetEvent s2 = js.eval(ConfigValues.TEXT_Y.replace("%width%", Integer.toString(width)).replace("%height%", Integer.toString(height))).stream().filter(c -> c.status() == Status.VALID).findAny().orElse(null);
	            if (s1 != null && s2 != null) {
	            	textX = Integer.parseInt(s1.value());
	            	textY = Integer.parseInt(s2.value());
	            } else {
	            	textX = width / 2;
	    			textY = height / 4 + 48 + 72 + 12 + (modMenu?32:22);
	            }
			} catch (Throwable t) {
				textX = width / 2;
				textY = height / 4 + 48 + 72 + 12 + (modMenu?32:22);
			}
		}).start();
		addDrawableChild(new GuiButtonWithImage(width / 2 + 104, height / 4 + 48 + 72 + (modMenu?IASMMPos.buttonOffset():-12), btn -> {
			if (Config.getInstance() == null) {
				Config.load();
			}
			client.setScreen(new GuiAccountSelector(this));
		}));
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void onRender(MatrixStack ms, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		drawCenteredText(ms, textRenderer, new TranslatableText("ias.loggedinas").append(" ").append(client.getSession().getUsername()).append("."), textX, textY, 0xFFCC8888);
	}
}
