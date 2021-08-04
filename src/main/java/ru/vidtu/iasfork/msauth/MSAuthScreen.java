package ru.vidtu.iasfork.msauth;

import java.util.ArrayList;
import java.util.List;

import com.github.mrebhan.ingameaccountswitcher.MR;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.Session;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import ru.vidtu.iasfork.msauth.AuthSys.MicrosoftAuthException;
import the_fireplace.ias.gui.AbstractAccountGui;
import the_fireplace.ias.gui.GuiAccountSelector;
import the_fireplace.iasencrypt.EncryptionTools;

public class MSAuthScreen extends Screen implements MSAuthHandler {
	public static final String[] symbols = new String[]{"▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃", "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
			"_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅", "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆", "_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇", "_ _ _ _ _ ▃ ▄ ▅ ▆ ▇ █",
			"_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇", "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆", "_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅", "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
			"▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃", "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _", "▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _", "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
			"▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _", "█ ▇ ▆ ▅ ▄ ▃ _ _ _ _ _", "▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _", "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
			"▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _", "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _"};
	private static final Identifier DEMO_BG = new Identifier("textures/gui/demo_background.png");
	
	public Screen prev;
	public List<OrderedText> text = new ArrayList<>();
	public boolean endTask = false;
	public int tick;
	public final boolean add;
	public boolean cancelButton = true;
	
	public MSAuthScreen(Screen prev) {
		super(new TranslatableText("ias.msauth.title"));
		this.prev = prev;
		this.add = true;
		AuthSys.start(this);
	}
	
	public MSAuthScreen(Screen prev, String token, String refresh) {
		super(new TranslatableText("ias.msauth.title"));
		this.prev = prev;
		this.add = false;
		AuthSys.start(token, refresh, this);
	}
	
	@Override
	public void init() {
		addDrawableChild(new ButtonWidget(width / 2 - 50, (this.height + 114) / 2, 100, 20, new TranslatableText("gui.cancel"), btn -> client.setScreen(prev))).active = cancelButton;
	}
	
	@Override
	public void tick() {
		tick++;
		((ButtonWidget)children().get(0)).active = cancelButton;
	}
	
	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	@Override
	public void init(MinecraftClient minecraftClient, int i, int j) {
		prev.init(minecraftClient, i, j);
		super.init(minecraftClient, i, j);
	}
	
	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float delta) {
		renderBackground(ms);
		
		if (prev != null) prev.render(ms, 0, 0, delta);
		fill(ms, 0, 0, width, height, Integer.MIN_VALUE);
		
		RenderSystem.setShaderTexture(0, DEMO_BG);
		drawTexture(ms, (this.width - 248) / 2, (this.height - 166) / 2, 0, 0, 248, 166);
		
		textRenderer.draw(ms, this.title, width / 2 - textRenderer.getWidth(this.title) / 2, (this.height - 156) / 2, -16777216);
		for (int i = 0; i < text.size(); i++) {
			textRenderer.draw(ms, text.get(i), width / 2 - textRenderer.getWidth(text.get(i)) / 2, height / 2 + i * 10 - text.size() * 5, 0xFF353535);
		}
		if (!endTask) textRenderer.draw(ms, symbols[tick % symbols.length], width / 2 - textRenderer.getWidth(symbols[tick % symbols.length]) / 2, height - 10, 0xFFFF9900);
		super.render(ms, mouseX, mouseY, delta);
	}

	@Override
	public void removed() {
		AuthSys.stop();
		prev.removed();
		super.removed();
	}
	
	@Override
	public void setState(String s) {
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.execute(() -> this.text = mc.textRenderer.wrapLines(new TranslatableText(s), 240));
	}

	@Override
	public void error(Throwable t) {
		cancelButton = true;
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.execute(() -> {
			endTask = true;
			if (t instanceof MicrosoftAuthException) {
				this.text = mc.textRenderer.wrapLines(new TranslatableText("ias.msauth.error", t.getMessage()).formatted(Formatting.DARK_RED), 240);
			} else {
				this.text = mc.textRenderer.wrapLines(new TranslatableText("ias.msauth.error", t.toString()).formatted(Formatting.DARK_RED), 240);
			}
		});
	}

	@Override
	public void success(String name, String uuid, String token, String refresh) {
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.execute(() -> {
			if (add) {
				MicrosoftAccount.msaccounts.add(new MicrosoftAccount(name, EncryptionTools.encode(token), EncryptionTools.encode(refresh)));
				mc.setScreen(new GuiAccountSelector(prev instanceof AbstractAccountGui?(((AbstractAccountGui)prev).prev instanceof GuiAccountSelector?((GuiAccountSelector)((AbstractAccountGui)prev).prev).prev:((AbstractAccountGui)prev).prev):prev));
			} else {
				MR.setSession(new Session(name, uuid, token, "mojang"));
				mc.setScreen(null);
			}
		});
	}

	@Override
	public void cancellble(boolean b) {
		this.cancelButton = b;
	}
}
