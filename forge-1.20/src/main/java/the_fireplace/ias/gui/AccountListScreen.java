package the_fireplace.ias.gui;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.User;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.AccountProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.lwjgl.glfw.GLFW;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.account.Account;
import the_fireplace.ias.IAS;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * The GUI where you can log in to, add, edit, and remove accounts.
 *
 * @author The_Fireplace
 * @author VidTu
 */
public class AccountListScreen extends Screen {
    private static long nextSkinUpdate = System.currentTimeMillis();
    private final Screen prev;
    private AccountList list;
    private Button add;
    private Button login;
    private Button loginOffline;
    private Button delete;
    private Button edit;
    private Button reloadSkins;
    private EditBox search;
    private String state;

    public AccountListScreen(Screen prev) {
        super(Component.literal("In-Game Account Switcher"));
        this.prev = prev;
    }

    @Override
    public void init() {
        list = new AccountList(minecraft, width, height);
        addRenderableWidget(list);
        addRenderableWidget(reloadSkins = Button.builder(Component.translatable("ias.listGui.reloadSkins"), btn -> reloadSkins()).bounds(2, 2, 120, 20).build());
        addRenderableWidget(search = new EditBox(this.font, this.width / 2 - 80, 14, 160, 16, search, Component.translatable("ias.listGui.search")));
        addRenderableWidget(add = Button.builder(Component.translatable("ias.listGui.add"), btn -> add()).bounds(this.width / 2 + 4 + 40, this.height - 52, 120, 20).build());
        addRenderableWidget(login = Button.builder(Component.translatable("ias.listGui.login"), btn -> login()).bounds(this.width / 2 - 154 - 10, this.height - 52, 120, 20).build());
        addRenderableWidget(loginOffline = Button.builder(Component.translatable("ias.listGui.loginOffline"), btn -> loginOffline()).bounds(this.width / 2 - 154 - 10, this.height - 28, 110, 20).build());
        addRenderableWidget(edit = Button.builder(Component.translatable("ias.listGui.edit"), btn -> edit()).bounds(this.width / 2 - 40, this.height - 52, 80, 20).build());
        addRenderableWidget(delete = Button.builder(Component.translatable("ias.listGui.delete"), btn -> delete()).bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, btn -> minecraft.setScreen(prev)).bounds(this.width / 2 + 4 + 50, this.height - 28, 110, 20).build());
        updateButtons();
        search.setSuggestion(I18n.get("ias.listGui.search"));
        search.setResponder(s -> {
            list.updateAccounts(s);
            search.setSuggestion(s.isEmpty() ? I18n.get("ias.listGui.search") : "");
        });
        list.updateAccounts(search.getValue());
    }

    @Override
    public void tick() {
        search.tick();
        updateButtons();
    }

    @Override
    public void removed() {
        Config.save(minecraft.gameDirectory.toPath());
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        renderBackground(ctx);
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredString(font, this.title, this.width / 2, 4, -1);
        if (list.getSelected() != null) {
            boolean slim = list.getSelected().slimSkin();
            ResourceLocation skinTexture = list.getSelected().skin();
            PoseStack ms = ctx.pose();
            ms.pushPose();
            ms.scale(4, 4, 4);
            ms.translate(1, height / 8D - 16D - 4D, 0);
            ctx.blit(skinTexture, 4, 0, 8, 8, 8, 8, 64, 64); // Head
            ctx.blit(skinTexture, 4, 8, 20, 20, 8, 12, 64, 64); // Body
            ctx.blit(skinTexture, slim ? 1 : 0, 8, 44, 20, slim ? 3 : 4, 12, 64, 64); // Right Arm (Left from our perspective)
            ctx.blit(skinTexture, 12, 8, 36, 52, slim ? 3 : 4, 12, 64, 64); // Left Arm (Right from our perspective)
            ctx.blit(skinTexture, 4, 20, 4, 20, 4, 12, 64, 64); // Right Leg (Left from our perspective)
            ctx.blit(skinTexture, 8, 20, 20, 52, 4, 12, 64, 64); // Left Leg (Right from our perspective)
            if (minecraft.options.isModelPartEnabled(PlayerModelPart.HAT))
                ctx.blit(skinTexture, 4, 0, 40, 8, 8, 8, 64, 64); // Head (Overlay)
            if (minecraft.options.isModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE))
                ctx.blit(skinTexture, slim ? 1 : 0, 8, 44, 36, slim ? 3 : 4, 12, 64, 64); // Right Arm (Overlay)
            if (minecraft.options.isModelPartEnabled(PlayerModelPart.LEFT_SLEEVE))
                ctx.blit(skinTexture, 12, 8, 52, 52, slim ? 3 : 4, 12, 64, 64); // Left Arm (Overlay)
            if (minecraft.options.isModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG))
                ctx.blit(skinTexture, 4, 20, 4, 36, 4, 12, 64, 64); // Right Leg (Overlay)
            if (minecraft.options.isModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG))
                ctx.blit(skinTexture, 8, 20, 4, 52, 4, 12, 64, 64); // Left Leg (Overlay)
            ms.popPose();
        }
        if (state != null) {
            ctx.drawCenteredString(font, state, this.width / 2, this.height - 62, 0xFFFF9900);
        }
    }

    private void reloadSkins() {
        if (list.children().isEmpty() || System.currentTimeMillis() <= nextSkinUpdate || state != null) return;
        IAS.SKIN_CACHE.clear();
        list.updateAccounts(search.getValue());
        nextSkinUpdate = System.currentTimeMillis() + 15000L;
    }

    private void login() {
        if (list.getSelected() == null || state != null) return;
        Account acc = list.getSelected().account();
        updateButtons();
        state = "";
        acc.login((s, o) -> state = I18n.get(s, o)).whenComplete((d, t) -> {
            state = null;
            if (t != null) {
                minecraft.execute(() -> minecraft.setScreen(new AlertScreen(() -> minecraft.setScreen(this),
                        Component.translatable("ias.error").withStyle(ChatFormatting.RED),
                        Component.literal(String.valueOf(t)))));
                return;
            }
            minecraft.execute(() -> {
                minecraft.user = new User(d.name(), UUIDTypeAdapter.fromUUID(d.uuid()), d.accessToken(), Optional.empty(), Optional.empty(), User.Type.byName(d.userType()));
                UserApiService apiSvc = minecraft.createUserApiService(minecraft.authenticationService, new GameConfig(new GameConfig.UserData(minecraft.getUser(), null, null, null), null, null, null, null));
                minecraft.userApiService = apiSvc;
                minecraft.playerSocialManager = new PlayerSocialManager(minecraft, apiSvc);
                minecraft.profileKeyPairManager = new AccountProfileKeyPairManager(apiSvc, d.uuid(), minecraft.gameDirectory.toPath());
                minecraft.reportingContext = ReportingContext.create(ReportEnvironment.local(), apiSvc);
            });
        });
    }

    private void loginOffline() {
        if (list.getSelected() == null || state != null) return;
        Account acc = list.getSelected().account();
        minecraft.user = new User(acc.name(), UUIDTypeAdapter.fromUUID(UUID
                .nameUUIDFromBytes("OfflinePlayer".concat(acc.name()).getBytes(StandardCharsets.UTF_8))),
                "0", Optional.empty(), Optional.empty(), User.Type.LEGACY);
        UserApiService apiSvc = minecraft.createUserApiService(minecraft.authenticationService, new GameConfig(new GameConfig.UserData(minecraft.getUser(), null, null, null), null, null, null, null));
        minecraft.userApiService = apiSvc;
        minecraft.playerSocialManager = new PlayerSocialManager(minecraft, apiSvc);
        minecraft.profileKeyPairManager = new AccountProfileKeyPairManager(apiSvc, new UUID(0, 0), minecraft.gameDirectory.toPath());
        minecraft.reportingContext = ReportingContext.create(ReportEnvironment.local(), apiSvc);
    }

    private void add() {
        if (state != null) return;
        minecraft.setScreen(new LoginScreen(this, Component.translatable("ias.loginGui.add"),
                Component.translatable("ias.loginGui.add.button"),
                Component.translatable("ias.loginGui.add.button.tooltip"), acc -> {
            Config.accounts.add(acc);
            Config.save(minecraft.gameDirectory.toPath());
            list.updateAccounts(search.getValue());
        }));
    }

    public void edit() {
        if (list.getSelected() == null || state != null) return;
        Account acc = list.getSelected().account();
        minecraft.setScreen(new LoginScreen(this, Component.translatable("ias.loginGui.edit"),
                Component.translatable("ias.loginGui.edit.button"),
                Component.translatable("ias.loginGui.edit.button.tooltip"), newAcc -> {
            Config.accounts.set(Config.accounts.indexOf(acc), newAcc);
            Config.save(minecraft.gameDirectory.toPath());
        }));
    }

    public void delete() {
        if (list.getSelected() == null || state != null) return;
        Account acc = list.getSelected().account();
        if (hasShiftDown()) {
            Config.accounts.remove(acc);
            Config.save(minecraft.gameDirectory.toPath());
            updateButtons();
            list.updateAccounts(search.getValue());
            return;
        }
        minecraft.setScreen(new ConfirmScreen(b -> {
            if (b) {
                Config.accounts.remove(acc);
                updateButtons();
                list.updateAccounts(search.getValue());
            }
            minecraft.setScreen(this);
        }, Component.translatable("ias.deleteGui.title"), Component.translatable("ias.deleteGui.text", acc.name())));
    }

    private void updateButtons() {
        login.active = list.getSelected() != null && state == null;
        loginOffline.active = list.getSelected() != null;
        add.active = state == null;
        edit.active = list.getSelected() != null && state == null;
        delete.active = list.getSelected() != null && state == null;
        reloadSkins.active = list.getSelected() != null && state == null && System.currentTimeMillis() > nextSkinUpdate;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (search.isFocused()) return super.keyPressed(key, scan, mods);
        if (key == GLFW.GLFW_KEY_F5 || key == GLFW.GLFW_KEY_R) {
            reloadSkins();
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            if (Screen.hasShiftDown()) loginOffline();
            else login();
            return true;
        }
        if (key == GLFW.GLFW_KEY_A || key == GLFW.GLFW_KEY_EQUAL || key == GLFW.GLFW_KEY_KP_ADD) {
            add();
            return true;
        }
        if (key == GLFW.GLFW_KEY_PERIOD || key == GLFW.GLFW_KEY_KP_DIVIDE) {
            edit();
            return true;
        }
        if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_MINUS || key == GLFW.GLFW_KEY_KP_SUBTRACT) {
            delete();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(prev);
    }
}
