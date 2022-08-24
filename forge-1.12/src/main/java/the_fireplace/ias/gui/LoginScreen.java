package the_fireplace.ias.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import ru.vidtu.ias.MicrosoftAuthCallback;
import ru.vidtu.ias.SharedIAS;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.Auth;
import ru.vidtu.ias.account.OfflineAccount;
import ru.vidtu.ias.gui.IASAlertScreen;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

/**
 * Screen for adding and editing accounts.
 *
 * @author evilmidget38
 * @author The_Fireplace
 * @author VidTu
 */
public class LoginScreen extends GuiScreen {
    private final GuiScreen prev;
    private final String title;
    private final String buttonText;
    private final String buttonTip;
    private final Consumer<Account> handler;
    private final MicrosoftAuthCallback callback = new MicrosoftAuthCallback();
    private GuiTextField username;
    private GuiButton offline;
    private GuiButton microsoft;
    private String state;

    public LoginScreen(GuiScreen prev, String title, String buttonText, String buttonTip, Consumer<Account> handler) {
        this.prev = prev;
        this.title = title;
        this.buttonText = buttonText;
        this.buttonTip = buttonTip;
        this.handler = handler;
    }

    @Override
    public void initGui() {
        super.initGui();
        addButton(offline = new GuiButton(0, width / 2 - 152, this.height - 28, 150, 20, buttonText));
        offline.enabled = false;
        addButton(new GuiButton(1, this.width / 2 + 2, this.height - 28, 150, 20, I18n.format("gui.cancel")));
        username = new GuiTextField(2, fontRenderer, this.width / 2 - 100, height / 2 - 12, 200, 20);
        username.setMaxStringLength(16);
        addButton(microsoft = new GuiButton(3, this.width / 2 - 50, this.height / 2 + 12, 100, 20, I18n.format("ias.loginGui.microsoft")));
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) loginOffline();
        else if (button.id == 1) mc.displayGuiScreen(prev);
        else if (button.id == 3) loginMicrosoft();
        super.actionPerformed(button);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (username.mouseClicked(mouseX, mouseY, mouseButton)) return;
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mx, int my, float delta) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, this.title, this.width / 2, 5, -1);
        drawCenteredString(fontRenderer, I18n.format("ias.loginGui.nickname"), this.width / 2, height / 2 - 22, -1);
        if (state != null) {
            drawCenteredString(fontRenderer, state, width / 2, height / 3 * 2, 0xFFFF9900);
            drawCenteredString(fontRenderer, SharedIAS.LOADING[(int) ((System.currentTimeMillis() / 50) % SharedIAS.LOADING.length)], width / 2, height / 3 * 2 + 10, 0xFFFF9900);
        }
        username.drawTextBox();
        super.drawScreen(mx, my, delta);
        if (offline.isMouseOver()) {
            drawHoveringText(fontRenderer.listFormattedStringToWidth(buttonTip, 150), mx, my);
        }
    }

    @Override
    public void keyTyped(char c, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(prev);
            return;
        }
        if (username.textboxKeyTyped(c, key)) return;
        super.keyTyped(c, key);
    }

    @Override
    public void onGuiClosed() {
        SharedIAS.EXECUTOR.execute(callback::close);
        super.onGuiClosed();
    }

    @Override
    public void updateScreen() {
        offline.enabled = !username.getText().trim().isEmpty() && state == null;
        username.setEnabled(state == null);
        microsoft.enabled = state == null;
        username.updateCursorCounter();
        super.updateScreen();
    }

    private void loginMicrosoft() {
        state = "";
        SharedIAS.EXECUTOR.execute(() -> {
            state = I18n.format("ias.loginGui.microsoft.checkBrowser");
            openURI(MicrosoftAuthCallback.MICROSOFT_AUTH_URL);
            callback.start((s, o) -> state = I18n.format(s, o), I18n.format("ias.loginGui.microsoft.canClose")).whenComplete((acc, t) -> {
                if (mc.currentScreen != this) return;
                if (t != null) {
                    mc.addScheduledTask(() -> mc.displayGuiScreen(new IASAlertScreen(() -> mc.displayGuiScreen(prev),
                            TextFormatting.RED + I18n.format("ias.error"),
                            String.valueOf(t))));
                    return;
                }
                if (acc == null) {
                    mc.addScheduledTask(() -> mc.displayGuiScreen(prev));
                    return;
                }
                mc.addScheduledTask(() -> {
                    handler.accept(acc);
                    mc.displayGuiScreen(prev);
                });
            });
        });
    }

    private void loginOffline() {
        state = "";
        SharedIAS.EXECUTOR.execute(() -> {
            state = I18n.format("ias.loginGui.offline.progress");
            Account account = new OfflineAccount(username.getText(), Auth.resolveUUID(username.getText()));
            mc.addScheduledTask(() -> {
                handler.accept(account);
                mc.displayGuiScreen(prev);
            });
        });
    }

    private void openURI(String uri) {
        try {
            Desktop.getDesktop().browse(new URI(uri));
        } catch (Throwable t) {
            Sys.openURL(uri);
        }
    }
}
