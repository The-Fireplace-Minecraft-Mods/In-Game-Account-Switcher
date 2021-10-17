package the_fireplace.ias.gui;

import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.AuthException;
import ru.vidtu.ias.account.MojangAccount;
import ru.vidtu.ias.account.OfflineAccount;
import ru.vidtu.ias.gui.MSAuthScreen;
import ru.vidtu.ias.utils.Auth;
import ru.vidtu.ias.utils.SkinRenderer;
import the_fireplace.ias.IAS;

/**
 * Screen for adding Mojang and Offline accounts.
 * @author evilmidget38
 * @author The_Fireplace
 */
public class AbstractAccountGui extends Screen {
	public final Screen prev;
	private TextFieldWidget username;
	private TextFieldWidget password;
	private ButtonWidget complete;
	private boolean logging;
	private Consumer<Account> handler;
	private List<OrderedText> error;
	
	public AbstractAccountGui(Screen prev, Text title, Consumer<Account> handler) {
		super(title);
		this.prev = prev;
		this.handler = handler;
	}
	
	@Override
	public void init() {
		complete = addDrawableChild(new ButtonWidget(this.width / 2 - 152, this.height - 28, 150, 20, this.title, btn -> end()));
		addDrawableChild(new ButtonWidget(this.width / 2 + 2, this.height - 28, 150, 20, new TranslatableText("gui.cancel"), btn -> client.setScreen(prev)));
		username = addDrawableChild(new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 60, 200, 20, new TranslatableText("ias.username")));
		username.setMaxLength(512);
		password = addDrawableChild(new GuiPasswordField(this.textRenderer, this.width / 2 - 100, 90, 200, 20, new TranslatableText("ias.password")));
		password.setMaxLength(512);
		complete.active = false;
		addDrawableChild(new ButtonWidget(this.width / 2 - 50, this.height / 3 * 2, 100, 20, new TranslatableText("ias.msauth.btn"), btn -> client.setScreen(new MSAuthScreen(prev, handler))));
	}
	
	@Override
	public void render(MatrixStack ms, int mx, int my, float delta) {
		renderBackground(ms);
		drawCenteredText(ms, textRenderer, this.title, this.width / 2, 7, -1);
		drawCenteredText(ms, textRenderer, new TranslatableText("ias.username"), this.width / 2 - 130, 66, -1);
		drawCenteredText(ms, textRenderer, new TranslatableText("ias.password"), this.width / 2 - 130, 96, -1);
		if (error != null) {
			for (int i = 0; i < error.size(); i++) {
				textRenderer.drawWithShadow(ms, error.get(i), this.width / 2 - textRenderer.getWidth(error.get(i)) / 2, 114 + i * 10, 0xFFFF0000);
				if (i > 6) break; //Exceptions can be very large.
			}
		}
		super.render(ms, mx, my, delta);
	}
	
	@Override
	public boolean keyPressed(int key, int oldkey, int mods) {
		if (key == GLFW.GLFW_KEY_ESCAPE) {
			client.setScreen(prev);
			return true;
		}
		if (key == GLFW.GLFW_KEY_ENTER) {
			if (username.isFocused()) {
				username.setTextFieldFocused(false);
				password.setTextFieldFocused(true);
				return true;
			}
			if (password.isFocused() && complete.active) {
				end();
				return true;
			}
		}
		return super.keyPressed(key, oldkey, mods);
	}
	
	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	@Override
	public void tick() {
		complete.active = !username.getText().trim().isEmpty() && !logging;
		complete.setMessage(!username.getText().trim().isEmpty() && password.getText().isEmpty()?this.title.copy().append(" ").append(new TranslatableText("ias.offline")):this.title);
		username.active = password.active = !logging;
		username.tick();
		password.tick();
		super.tick();
	}

	public void end() {
		if (password.getText().isEmpty()) {
			String name = username.getText();
			if (Config.accounts.stream().anyMatch(acc -> acc.alias().equalsIgnoreCase(name))) {
				error = textRenderer.wrapLines(new TranslatableText("ias.auth.alreadyexists"), width - 10);
				return;
			}
			logging = true;
			new Thread(() -> {
				SkinRenderer.loadSkin(client, name, null, false);
				client.execute(() -> {
					if (client.currentScreen == this) {
						handler.accept(new OfflineAccount(username.getText()));
						client.setScreen(prev);
					}
				});
				logging = false;
			}).start();
		} else {
			String name = username.getText();
			String pwd = password.getText(); 
			logging = true;
			new Thread(() -> {
				try {
					MojangAccount ma = Auth.authMojang(name, pwd);
					SkinRenderer.loadSkin(client, ma.alias(), ma.uuid(), false);
					if (Config.accounts.stream().anyMatch(acc -> acc.alias().equalsIgnoreCase(name)))
						throw new AuthException(new TranslatableText("ias.auth.alreadyexists"));
					client.execute(() -> {
						if (client.currentScreen == this) {
							handler.accept(ma);
							client.setScreen(prev);
						}
					});
				} catch (AuthException ae) {
					IAS.LOG.warn("Unable to add account (expected exception)", ae);
					client.execute(() -> error = textRenderer.wrapLines(ae.getText(), width - 10));
				} catch (Throwable t) {
					IAS.LOG.warn("Unable to add account (unexpected exception)", t);
					client.execute(() -> error = textRenderer.wrapLines(new TranslatableText("ias.auth.unknown", t.getLocalizedMessage()), width - 10));
				}
				logging = false;
			}, "IAS Mojang Auth Thread").start();
		}
	}
}
