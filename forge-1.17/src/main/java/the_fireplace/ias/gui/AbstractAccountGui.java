package the_fireplace.ias.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;
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
    private EditBox username;
    private EditBox password;
    private Button complete;
    private boolean logging;
    private Consumer<Account> handler;
    private List<FormattedCharSequence> error;

    public AbstractAccountGui(Screen prev, Component title, Consumer<Account> handler) {
        super(title);
        this.prev = prev;
        this.handler = handler;
    }

    @Override
    public void init() {
        complete = addRenderableWidget(new Button(this.width / 2 - 152, this.height - 28, 150, 20, this.title, btn -> end()));
        addRenderableWidget(new Button(this.width / 2 + 2, this.height - 28, 150, 20, new TranslatableComponent("gui.cancel"), btn -> minecraft.setScreen(prev)));
        username = addRenderableWidget(new EditBox(font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("ias.username")));
        username.setMaxLength(512);
        password = addRenderableWidget(new GuiPasswordField(font, this.width / 2 - 100, 90, 200, 20, new TranslatableComponent("ias.password")));
        password.setMaxLength(512);
        complete.active = false;
        addRenderableWidget(new Button(this.width / 2 - 50, this.height / 3 * 2, 100, 20, new TranslatableComponent("ias.msauth.btn"), btn -> minecraft.setScreen(new MSAuthScreen(prev, handler))));
    }

    @Override
    public void render(PoseStack ms, int mx, int my, float delta) {
        renderBackground(ms);
        drawCenteredString(ms, font, this.title, this.width / 2, 7, -1);
        drawCenteredString(ms, font, I18n.get("ias.username"), this.width / 2 - 130, 66, -1);
        drawCenteredString(ms, font, I18n.get("ias.password"), this.width / 2 - 130, 96, -1);
        if (error != null) {
            for (int i = 0; i < error.size(); i++) {
                font.draw(ms, error.get(i), this.width / 2 - font.width(error.get(i)) / 2, 114 + i * 10, 0xFFFF0000);
                if (i > 6) break; //Exceptions can be very large.
            }
        }
        super.render(ms, mx, my, delta);
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
        complete.setMessage(!username.getValue().trim().isEmpty() && password.getValue().isEmpty() ? this.title.copy().append(" ").append(new TranslatableComponent("ias.offline")) : this.title);
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
                    minecraft.execute(() -> error = font.split(ae.getComponent(), width - 10));
                } catch (Throwable t) {
                    IAS.LOG.warn("Unable to add account (unexpected exception)", t);
                    minecraft.execute(() -> error = font.split(new TranslatableComponent("ias.auth.unknown", t.getLocalizedMessage()), width - 10));
                }
                logging = false;
            });
        }
    }
}
