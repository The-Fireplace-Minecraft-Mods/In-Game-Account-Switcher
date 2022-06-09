package the_fireplace.ias.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.AuthException;
import ru.vidtu.ias.account.MojangAccount;
import ru.vidtu.ias.account.OfflineAccount;
import ru.vidtu.ias.gui.MSAuthScreen;
import ru.vidtu.ias.utils.Auth;
import ru.vidtu.ias.utils.SkinRenderer;
import the_fireplace.ias.IAS;

import java.util.List;
import java.util.function.Consumer;

/**
 * Screen for adding Mojang and Offline accounts.
 *
 * @author evilmidget38
 * @author The_Fireplace
 */
public class AbstractAccountGui extends Screen {
    public final Screen prev;
    private TextFieldWidget username;
    private TextFieldWidget password;
    private Button complete;
    private boolean logging;
    private Consumer<Account> handler;
    private List<String> error;

    public AbstractAccountGui(Screen prev, ITextComponent title, Consumer<Account> handler) {
        super(title);
        this.prev = prev;
        this.handler = handler;
    }

    @Override
    public void init() {
        complete = addButton(new Button(this.width / 2 - 152, this.height - 28, 150, 20, this.title.getColoredString(), btn -> end()));
        addButton(new Button(this.width / 2 + 2, this.height - 28, 150, 20, I18n.get("gui.cancel"), btn -> minecraft.setScreen(prev)));
        username = addButton(new TextFieldWidget(font, this.width / 2 - 100, 60, 200, 20, I18n.get("ias.username")));
        username.setMaxLength(512);
        password = addButton(new GuiPasswordField(font, this.width / 2 - 100, 90, 200, 20, I18n.get("ias.password")));
        password.setMaxLength(512);
        complete.active = false;
        addButton(new Button(this.width / 2 - 50, this.height / 3 * 2, 100, 20, I18n.get("ias.msauth.btn"), btn -> minecraft.setScreen(new MSAuthScreen(prev, handler))));
    }

    @Override
    public void render(int mx, int my, float delta) {
        renderBackground();
        drawCenteredString(font, this.title.getColoredString(), this.width / 2, 7, -1);
        drawCenteredString(font, I18n.get("ias.username"), this.width / 2 - 130, 66, -1);
        drawCenteredString(font, I18n.get("ias.password"), this.width / 2 - 130, 96, -1);
        if (error != null) {
            for (int i = 0; i < error.size(); i++) {
                drawCenteredString(font, error.get(i), this.width / 2, 114 + i * 10, 0xFFFF0000);
                if (i > 6) break; //Exceptions can be very large.
            }
        }
        super.render(mx, my, delta);
    }

    @Override
    public boolean keyPressed(int key, int oldkey, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.setScreen(prev);
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER) {
            if (username.isFocused()) {
                username.setFocus(false);
                password.setFocus(true);
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
        complete.active = !username.getValue().trim().isEmpty() && !logging;
        complete.setMessage(!username.getValue().trim().isEmpty() && password.getValue().isEmpty() ? this.title.copy().append(" ").append(new TranslationTextComponent("ias.offline")).getColoredString() : this.title.getColoredString());
        username.active = password.active = !logging;
        username.tick();
        password.tick();
        super.tick();
    }

    public void end() {
        if (password.getValue().isEmpty()) {
            String name = username.getValue();
            logging = true;
            IAS.EXECUTOR.execute(() -> {
                SkinRenderer.loadSkin(minecraft, name, null, false);
                minecraft.execute(() -> {
                    if (minecraft.screen == this) {
                        handler.accept(new OfflineAccount(username.getValue()));
                        minecraft.setScreen(prev);
                    }
                });
                logging = false;
            });
        } else {
            String name = username.getValue();
            String pwd = password.getValue();
            logging = true;
            IAS.EXECUTOR.execute(() -> {
                try {
                    MojangAccount ma = Auth.authMojang(name, pwd);
                    SkinRenderer.loadSkin(minecraft, ma.alias(), ma.uuid(), false);
                    minecraft.execute(() -> {
                        if (minecraft.screen == this) {
                            handler.accept(ma);
                            minecraft.setScreen(prev);
                        }
                    });
                } catch (AuthException ae) {
                    IAS.LOG.warn("Unable to add account (expected exception)", ae);
                    minecraft.execute(() -> error = font.split(ae.getComponent().getColoredString(), width - 10));
                } catch (Throwable t) {
                    IAS.LOG.warn("Unable to add account (unexpected exception)", t);
                    minecraft.execute(() -> error = font.split(I18n.get("ias.auth.unknown", t.getLocalizedMessage()), width - 10));
                }
                logging = false;
            });
        }
    }
}
