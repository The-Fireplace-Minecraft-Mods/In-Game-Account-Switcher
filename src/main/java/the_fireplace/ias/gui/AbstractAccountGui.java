package the_fireplace.ias.gui;

import org.lwjgl.glfw.GLFW;

import com.github.mrebhan.ingameaccountswitcher.tools.alt.AccountData;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltDatabase;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import the_fireplace.iasencrypt.EncryptionTools;

/**
 * @author evilmidget38
 * @author The_Fireplace
 */
public abstract class AbstractAccountGui extends Screen
{
	public final Screen prev;
	private TextFieldWidget username;
	private TextFieldWidget password;
	private ButtonWidget complete;
	protected boolean hasUserChanged = false;

	public AbstractAccountGui(Screen prev, String actionString)
	{
		super(new TranslatableText(actionString));
		this.prev = prev;
	}
	
	@Override
	protected void init() {
		addDrawableChild(complete = new ButtonWidget(this.width / 2 - 152, this.height - 28, 150, 20, this.title, btn -> {
			complete();
			escape();
		}));
		addDrawableChild(new ButtonWidget(this.width / 2 + 2, this.height - 28, 150, 20, new TranslatableText("gui.cancel"), btn -> escape()));
		addDrawableChild(username = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 60, 200, 20, new LiteralText("")));
		username.setMaxLength(64);
		addDrawableChild(password = new GuiPasswordField(this.textRenderer, this.width / 2 - 100, 90, 200, 20, new LiteralText("")));
		password.setMaxLength(64);
		complete.active = false;
	}
	
	@Override
	public void render(MatrixStack ms, int mx, int my, float delta) {
		renderBackground(ms);
		drawCenteredText(ms, textRenderer, this.title, this.width / 2, 7, -1);
		drawCenteredText(ms, textRenderer, new TranslatableText("ias.username"), this.width / 2 - 130, 66, -1);
		drawCenteredText(ms, textRenderer, new TranslatableText("ias.password"), this.width / 2 - 130, 96, -1);
		super.render(ms, mx, my, delta);
	}
	
	@Override
	public boolean keyPressed(int key, int oldkey, int mods) {
		if (key == GLFW.GLFW_KEY_ESCAPE) {
			escape();
		} else if (key == GLFW.GLFW_KEY_ENTER) {
			if (username.isFocused()) {
				username.setTextFieldFocused(false);
				password.setTextFieldFocused(true);
			} else if (password.isFocused() && complete.active) {
				complete();
				escape();
			}
		} else {
			if (username.isFocused()) hasUserChanged = true;
		}
		return super.keyPressed(key, oldkey, mods);
	}
	
	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	@Override
	public void tick() {
		complete.active = canComplete();
		username.tick();
		password.tick();
		super.tick();
	}

	private void escape(){
		client.openScreen(prev);
	}

	public String getUsername()
	{
		return username.getText();
	}

	public String getPassword()
	{
		return password.getText();
	}

	public void setUsername(String username)
	{
		this.username.setText(username);
	}

	public void setPassword(String password)
	{
		this.password.setText(password);
	}

	protected boolean accountNotInList(){
		for(AccountData data : AltDatabase.getInstance().getAlts())
			if(EncryptionTools.decode(data.user).equals(getUsername()))
				return false;
		return true;
	}

	public boolean canComplete()
	{
		return getUsername().length() > 0 && accountNotInList();
	}

	public abstract void complete();
}
