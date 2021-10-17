package the_fireplace.ias.gui;

import java.io.File;
import java.io.FileInputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import com.github.mrebhan.ingameaccountswitcher.tools.Tools;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.Session;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.AuthException;
import ru.vidtu.ias.mixins.MinecraftClientAccessor;
import ru.vidtu.ias.utils.SkinRenderer;
import the_fireplace.ias.IAS;
/**
 * The GUI where you can log in to, add, and remove accounts
 * @author The_Fireplace
 */
public class GuiAccountSelector extends Screen {
	public final Screen prev;
	private boolean logging;
	private Text error;
	private AccountList accountsgui;
	// Buttons that can be disabled need to be here
	private ButtonWidget login;
	private ButtonWidget loginoffline;
	private ButtonWidget delete;
	private ButtonWidget edit;
	private ButtonWidget reloadskins;
	// Search
	private String prevQuery = "";
	private TextFieldWidget search;
	
	public GuiAccountSelector(Screen prev) {
		super(new TranslatableText("ias.selectaccount"));
		this.prev = prev;
	}

	@Override
	protected void init() {
		if (accountsgui == null) accountsgui = new AccountList(client, width, height);
		addDrawableChild(accountsgui);
		addDrawableChild(reloadskins = new ButtonWidget(2, 2, 120, 20, new TranslatableText("ias.reloadskins"), btn -> reloadSkins()));
		addDrawableChild(new ButtonWidget(this.width / 2 + 4 + 40, this.height - 52, 120, 20, new TranslatableText("ias.addaccount"), btn -> add()));
		addDrawableChild(login = new ButtonWidget(this.width / 2 - 154 - 10, this.height - 52, 120, 20, new TranslatableText("ias.login"), btn -> accountsgui.login()));
		addDrawableChild(edit = new ButtonWidget(this.width / 2 - 40, this.height - 52, 80, 20, new TranslatableText("ias.edit"), btn -> accountsgui.edit()));
		addDrawableChild(loginoffline = new ButtonWidget(this.width / 2 - 154 - 10, this.height - 28, 110, 20, new TranslatableText("ias.login").append(" ").append(new TranslatableText("ias.offline")), btn -> accountsgui.loginOffline()));
		addDrawableChild(new ButtonWidget(this.width / 2 + 4 + 50, this.height - 28, 110, 20, new TranslatableText("gui.cancel"), btn -> client.setScreen(prev)));
		addDrawableChild(delete = new ButtonWidget(this.width / 2 - 50, this.height - 28, 100, 20, new TranslatableText("ias.delete"), btn -> accountsgui.delete()));
		addDrawableChild(search = new TextFieldWidget(this.textRenderer, this.width / 2 - 80, 14, 160, 16, new TranslatableText("ias.search")));
	    updateButtons();
	    search.setSuggestion(I18n.translate("ias.search"));
	    accountsgui.resize(width, height);
	    accountsgui.updateAccounts();
	}

	@Override
	public void tick() {
		search.tick();
		updateButtons();
		if (!prevQuery.equals(search.getText())) {
			accountsgui.updateAccounts();
			prevQuery = search.getText();
			search.setSuggestion(search.getText().isEmpty()?I18n.translate("ias.search"):"");
		}
	}

	@Override
	public void removed() {
		Config.save(client);
	}
	
	@Override
	public void render(MatrixStack ms, int mx, int my, float delta) {
		renderBackground(ms);
		accountsgui.render(ms, mx, my, delta);
		drawCenteredText(ms, textRenderer, this.title, this.width / 2, 4, -1);
		if (error != null) {
			drawCenteredText(ms, textRenderer, error, this.width / 2, this.height - 62, 16737380);
		}
		super.render(ms, mx, my, delta);
		if (accountsgui.getSelectedOrNull() != null) {
			Account acc = accountsgui.getSelectedOrNull().account;
			RenderSystem.setShaderTexture(0, accountsgui.getSelectedOrNull().model(false));
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			Screen.drawTexture(ms, 8, height / 2 - 64 - 16, 0, 0, 64, 128, 64, 128);
			Tools.drawBorderedRect(ms, width - 8 - 64, height / 2 - 64 - 16, width - 8, height / 2 + 64 - 16, 2, -5855578, -13421773);
			if (acc.online()) drawTextWithShadow(ms, textRenderer, new TranslatableText("ias.premium"), width - 8 - 61, height / 2 - 64 - 13, 6618980);
			else drawTextWithShadow(ms, textRenderer, new TranslatableText("ias.notpremium"), width - 8 - 61, height / 2 - 64 - 13, 16737380);
			drawTextWithShadow(ms, textRenderer, new TranslatableText("ias.timesused"), width - 8 - 61, height / 2 - 64 - 15 + 12, -1);
			drawStringWithShadow(ms, textRenderer, String.valueOf(acc.uses()), width - 8 - 61, height / 2 - 64 - 15 + 21, -1);
			if (acc.uses() > 0) {
				drawTextWithShadow(ms, textRenderer, new TranslatableText("ias.lastused"), width - 8 - 61, height / 2 - 64 - 15 + 30, -1);
				drawStringWithShadow(ms, textRenderer, DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
						.format(Instant.ofEpochMilli(acc.lastUse()).atZone(ZoneId.systemDefault())) , width - 8 - 61, height / 2 - 64 - 15 + 39, -1);
			}
		}
	}

	/**
	 * Reload Skins
	 */
	private void reloadSkins() {
		Config.save(client);
		SkinRenderer.loadAllAsync(client, true, () -> accountsgui.children().forEach(ae -> {
			ae.model(true);
			ae.face(true);
		}));
	}

	/**
	 * Add an account
	 */
	private void add() {
		client.setScreen(new AbstractAccountGui(this, new TranslatableText("ias.addaccount"), acc -> {
			Config.accounts.add(acc);
			Config.save(client);
			accountsgui.updateAccounts();
		}));
	}
	
	@Override
	public boolean keyPressed(int key, int oldkey, int mods) {
		if (key == GLFW.GLFW_KEY_ESCAPE) {
			client.setScreen(prev);
			return true;
		}
		if (search.isFocused()) {
			if (key == GLFW.GLFW_KEY_ENTER && search.isFocused()) {
				search.setTextFieldFocused(false);
				return true;
			}
		} else {
			if (key == GLFW.GLFW_KEY_DELETE && delete.active) {
				accountsgui.delete();
				return true;
			}
			if (key == GLFW.GLFW_KEY_ENTER && !search.isFocused() && (login.active || loginoffline.active)) {
				if (Screen.hasShiftDown() && loginoffline.active) {
					accountsgui.loginOffline();
				} else if (login.active) {
					accountsgui.login();
				} else {
					accountsgui.loginOffline();
				}
				return true;
			}
			if (key == GLFW.GLFW_KEY_F5) {
				reloadSkins();
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
	public boolean charTyped(char charT, int mods) {
		if (!search.isFocused()) {
			if (charT == '+') {
				add();
				return true;
			}
			if (charT == '/' && edit.active) {
				accountsgui.edit();
				return true;
			}
			if (charT == 'r' || charT == 'R') {
				reloadSkins();
				return true;
			}
		}
		return super.charTyped(charT, mods);
	}

	private void updateButtons() {
		login.active = !accountsgui.empty() && accountsgui.getSelectedOrNull().account.online() && !logging;
		loginoffline.active = !accountsgui.empty();
		delete.active = !accountsgui.empty();
		edit.active = !accountsgui.empty() && accountsgui.getSelectedOrNull().account.editable();
		reloadskins.active = !accountsgui.empty();
	}

	public class AccountList extends AlwaysSelectedEntryListWidget<AccountEntry> {
		public AccountList(MinecraftClient mc, int width, int height) {
			super(mc, width, height, 32, height - 64, 14);
		}
		
		public void resize(int width, int height) {
			this.width = width;
			this.height = height;
			this.top = 32;
			this.bottom = height - 64;
		}

		public void updateAccounts() {
			clearEntries();
			Config.accounts.stream()
					.filter(acc -> search.getText().isEmpty()
							|| (Config.caseSensitiveSearch ? acc.alias().startsWith(search.getText())
									: acc.alias().toLowerCase().startsWith(search.getText().toLowerCase())))
					.forEach(acc -> addEntry(new AccountEntry(acc)));
			this.setSelected(empty()?null:getEntry(0));
		}
		
		public void login() {
			if (empty()) return;
			Account acc = getSelectedOrNull().account;
			if (!acc.online()) return;
			logging = true;
			updateButtons();
			acc.use();
			acc.login(client, t -> {
				logging = false;
				if (t == null) {
					client.setScreen(prev);
				} else if (t instanceof AuthException) {
					IAS.LOG.warn("Unable to login", t);
					error = ((AuthException) t).getText();
				} else {
					IAS.LOG.warn("Unable to login", t);
					error = new TranslatableText("ias.auth.unknown", t.toString());
				}
			});
		}
		
		public void loginOffline() {
			if (empty()) return;
			Account acc = getSelectedOrNull().account;
			acc.use();
			((MinecraftClientAccessor)client).setSession(new Session(acc.alias(), UUIDTypeAdapter.fromUUID(new UUID(0, 0)), "0", "legacy"));
		}
		
		public void edit() {
			if (empty() || !getSelectedOrNull().account.editable()) return;
			client.setScreen(new AbstractAccountGui(GuiAccountSelector.this, new TranslatableText("ias.editaccount"), acc -> {
				Config.accounts.set(Config.accounts.indexOf(getSelectedOrNull().account), acc);
			}));
		}
		
		public void delete() {
			if (empty()) return;
			Account acc = getSelectedOrNull().account;
			client.setScreen(new ConfirmScreen(b -> {
				if (b) {
					Config.accounts.remove(acc);
					updateButtons();
					updateAccounts();
				}
				client.setScreen(GuiAccountSelector.this);
			}, new TranslatableText("ias.delete.title"), new TranslatableText("ias.delete.text", acc.alias())));
		}
		
		public void swap(int first, int second) {
			Account entry = Config.accounts.get(first);
			Config.accounts.set(first, Config.accounts.get(second));
			Config.accounts.set(second, entry);
			Config.save(client);
			updateAccounts();
			setSelected(children().get(second));
		}
		
		public boolean empty() {
			return getEntryCount() == 0;
		}
	}
	
	public class AccountEntry extends AlwaysSelectedEntryListWidget.Entry<AccountEntry> {
		public Account account;
		public Identifier modelTexture, faceTexture;
		public AccountEntry(Account account) {
			this.account = account;
		}
		
		@Override
		public void render(MatrixStack ms, int i, int y, int x, int w, int h, int mx, int my, boolean hover, float delta) {
			Text s = new LiteralText(account.alias());
			int color = -1;
			if (client.getSession().getUsername().equals(account.alias())) color = 0x00FF00;
			drawTextWithShadow(ms, textRenderer, s, x + 10, y + 1, color);
			RenderSystem.setShaderTexture(0, face(false));
			Screen.drawTexture(ms, x, y + 1, 0, 0, 8, 8, 8, 8);
			if (accountsgui.getSelectedOrNull() == this) {
				RenderSystem.setShaderTexture(0, new Identifier("textures/gui/server_selection.png"));
				boolean movableDown = i + 1 < accountsgui.children().size();
				boolean movableUp = i > 0;
				if (movableDown) {
					boolean hoveredDown = mx > x + w - 16 && mx < x + w - 6 && hover;
					Screen.drawTexture(ms, x + w - 35, y - 18, 48, hoveredDown?32:0, 32, 32, 256, 256);
				}
				if (movableUp) {
					boolean hoveredUp = mx > x + w - (movableDown?28:16) && mx < x + w - (movableDown?16:6) && hover;
					Screen.drawTexture(ms, x + w - (movableDown?30:19), y - 3, 96, hoveredUp?32:0, 32, 32, 256, 256);
				}
			}
		}
		
		@Override
		public boolean mouseClicked(double mx, double my, int button) {
			if (button == 0 && accountsgui.getSelectedOrNull() == this) {
				int x = accountsgui.getRowLeft();
				int w = accountsgui.getRowWidth();
				int i = accountsgui.children().indexOf(this);
				boolean movableDown = i + 1 < accountsgui.children().size();
				boolean movableUp = i > 0;
				if (movableDown) {
					boolean hoveredDown = mx > x + w - 16 && mx < x + w - 6;
					if (hoveredDown) {
						client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1F));
						accountsgui.swap(i, i + 1);
					}
				}
				if (movableUp) {
					boolean hoveredUp = mx > x + w - (movableDown?28:16) && mx < x + w - (movableDown?16:6);
					if (hoveredUp) {
						client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1F));
						accountsgui.swap(i, i - 1);
					}
				}
				return true;
			}
			accountsgui.setSelected(this);
			return true;
		}
		
		public Identifier model(boolean forceReload) {
			if (forceReload) {
				client.getTextureManager().destroyTexture(modelTexture);
				modelTexture = null;
			}
			if (modelTexture == null) {
				File model = new File(new File(client.runDirectory, "cachedImages/models"), account.alias() + ".png");
				File face = new File(new File(client.runDirectory, "cachedImages/faces"), account.alias() + ".png");
				SkinRenderer.loadSkin(client, account.alias(), account.uuid(), model, face, false);
				try (FileInputStream fis = new FileInputStream(model); NativeImage ni = NativeImage.read(fis)) {
					NativeImageBackedTexture nibt = new NativeImageBackedTexture(ni);
					modelTexture = client.getTextureManager().registerDynamicTexture("iasmodel_" + account.alias().hashCode(), nibt);
				} catch (Throwable t) {
					IAS.LOG.warn("Unable to bake skin model: " + account.alias(), t);
					modelTexture = new Identifier("iaserror", "skin");
				}
			}
			return modelTexture;
		}
		
		public Identifier face(boolean forceReload) {
			if (forceReload) {
				client.getTextureManager().destroyTexture(faceTexture);
				faceTexture = null;
			}
			if (faceTexture == null) {
				File model = new File(new File(client.runDirectory, "cachedImages/models"), account.alias() + ".png");
				File face = new File(new File(client.runDirectory, "cachedImages/faces"), account.alias() + ".png");
				SkinRenderer.loadSkin(client, account.alias(), account.uuid(), model, face, false);
				try (FileInputStream fis = new FileInputStream(face); NativeImage ni = NativeImage.read(fis)) {
					NativeImageBackedTexture nibt = new NativeImageBackedTexture(ni);
					faceTexture = client.getTextureManager().registerDynamicTexture("iasface_" + account.alias().hashCode(), nibt);
				} catch (Throwable t) {
					IAS.LOG.warn("Unable to bake skin face: " + account.alias(), t);
					faceTexture = new Identifier("iaserror", "skin");
				}
			}
			return faceTexture;
		}

		@Override
		public Text getNarration() {
			return new LiteralText(account.alias());
		}
		
	}
}
