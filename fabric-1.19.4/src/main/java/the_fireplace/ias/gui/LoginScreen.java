package the_fireplace.ias.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import ru.vidtu.ias.MicrosoftAuthCallback;
import ru.vidtu.ias.SharedIAS;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.Auth;
import ru.vidtu.ias.account.OfflineAccount;

import java.util.function.Consumer;

/**
 * Screen for adding and editing accounts.
 *
 * @author evilmidget38
 * @author The_Fireplace
 * @author VidTu
 */
public class LoginScreen extends Screen {
    private final Screen prev;
    private final Component buttonText;
    private final Component buttonTip;
    private final Consumer<Account> handler;
    private final MicrosoftAuthCallback callback = new MicrosoftAuthCallback();
    private EditBox username;
    private Button offline;
    private Button microsoft;
    private String state;

    public LoginScreen(Screen prev, Component title, Component buttonText, Component buttonTip, Consumer<Account> handler) {
        super(title);
        this.prev = prev;
        this.buttonText = buttonText;
        this.buttonTip = buttonTip;
        this.handler = handler;
    }

    @Override
    public void init() {
        super.init();
        addRenderableWidget(offline = Button.builder(buttonText, btn -> loginOffline()).bounds(width / 2 - 152, this.height - 28, 150, 20).tooltip(Tooltip.create(buttonTip)).build());
        offline.active = false;
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, btn -> minecraft.setScreen(prev)).bounds(this.width / 2 + 2, this.height - 28, 150, 20).build());
        username = addRenderableWidget(new EditBox(font, this.width / 2 - 100, height / 2 - 12, 200, 20, username, Component.translatable("ias.loginGui.nickname")));
        username.setMaxLength(16);
        addRenderableWidget(microsoft = Button.builder(Component.translatable("ias.loginGui.microsoft"), btn -> loginMicrosoft()).bounds(this.width / 2 - 50, this.height / 2 + 12, 100, 20).build());
    }

    @Override
    public void render(PoseStack ms, int mx, int my, float delta) {
        renderBackground(ms);
        drawCenteredString(ms, font, this.title, this.width / 2, 5, -1);
        drawCenteredString(ms, font, I18n.get("ias.loginGui.nickname"), this.width / 2, height / 2 - 22, -1);
        if (state != null) {
            drawCenteredString(ms, font, state, width / 2, height / 3 * 2, 0xFFFF9900);
            drawCenteredString(ms, font, SharedIAS.LOADING[(int) ((System.currentTimeMillis() / 50) % SharedIAS.LOADING.length)], width / 2, height / 3 * 2 + 10, 0xFFFF9900);
        }
        super.render(ms, mx, my, delta);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(prev);
    }

    @Override
    public void removed() {
        SharedIAS.EXECUTOR.execute(callback::close);
        super.removed();
    }

    @Override
    public void tick() {
        offline.active = !username.getValue().trim().isEmpty() && state == null;
        username.active = state == null;
        microsoft.active = state == null;
        username.tick();
        super.tick();
    }

    private void loginMicrosoft() {
        state = "";
        SharedIAS.EXECUTOR.execute(() -> {
            state = I18n.get("ias.loginGui.microsoft.checkBrowser");
            Util.getPlatform().openUri(MicrosoftAuthCallback.MICROSOFT_AUTH_URL);
            callback.start((s, o) -> state = I18n.get(s, o), I18n.get("ias.loginGui.microsoft.canClose")).whenComplete((acc, t) -> {
                if (minecraft.screen != this) return;
                if (t != null) {
                    minecraft.execute(() -> minecraft.setScreen(new AlertScreen(() -> minecraft.setScreen(prev),
                            Component.translatable("ias.error").withStyle(ChatFormatting.RED),
                            Component.literal(String.valueOf(t)))));
                    return;
                }
                if (acc == null) {
                    minecraft.execute(() -> minecraft.setScreen(prev));
                    return;
                }
                minecraft.execute(() -> {
                    handler.accept(acc);
                    minecraft.setScreen(prev);
                });
            });
        });
    }

    private void loginOffline() {
        state = "";
        SharedIAS.EXECUTOR.execute(() -> {
            state = I18n.get("ias.loginGui.offline.progress");
            Account account = new OfflineAccount(username.getValue(), Auth.resolveUUID(username.getValue()));
            minecraft.execute(() -> {
                handler.accept(account);
                minecraft.setScreen(prev);
            });
        });
    }
}
