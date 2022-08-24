package the_fireplace.ias.gui;

import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.Session;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.gui.IASAlertScreen;
import the_fireplace.ias.IAS;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * The GUI where you can log in to, add, edit, and remove accounts.
 *
 * @author The_Fireplace
 * @author VidTu
 */
public class AccountListScreen extends GuiScreen {
    private static long nextSkinUpdate = System.currentTimeMillis();
    private final GuiScreen prev;
    private AccountList list;
    private GuiButton add;
    private GuiButton login;
    private GuiButton loginOffline;
    private GuiButton delete;
    private GuiButton edit;
    private GuiButton reloadSkins;
    private GuiTextField search;
    private String state;

    public AccountListScreen(GuiScreen prev) {
        this.prev = prev;
    }

    @Override
    public void initGui() {
        list = new AccountList(mc, width, height);
        addButton(reloadSkins = new GuiButton(0, 2, 2, 120, 20, I18n.format("ias.listGui.reloadSkins")));
        search = new GuiTextField(1, this.fontRenderer, this.width / 2 - 80, 14, 160, 16);
        addButton(add = new GuiButton(2, this.width / 2 + 4 + 40, this.height - 52, 120, 20, I18n.format("ias.listGui.add")));
        addButton(login = new GuiButton(3, this.width / 2 - 154 - 10, this.height - 52, 120, 20, I18n.format("ias.listGui.login")));
        addButton(loginOffline = new GuiButton(4, this.width / 2 - 154 - 10, this.height - 28, 110, 20, I18n.format("ias.listGui.loginOffline")));
        addButton(edit = new GuiButton(5, this.width / 2 - 40, this.height - 52, 80, 20, I18n.format("ias.listGui.edit")));
        addButton(delete = new GuiButton(6, this.width / 2 - 50, this.height - 28, 100, 20, I18n.format("ias.listGui.delete")));
        addButton(new GuiButton(7, this.width / 2 + 4 + 50, this.height - 28, 110, 20, I18n.format("gui.cancel")));
        updateButtons();
        search.setGuiResponder(new GuiPageButtonList.GuiResponder() {
            @Override
            public void setEntryValue(int id, boolean value) {}
            @Override
            public void setEntryValue(int id, float value) {}
            @Override
            public void setEntryValue(int id, String value) {
                list.updateAccounts(value);
            }
        });
        list.updateAccounts(search.getText());
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) reloadSkins();
        else if (button.id == 2) add();
        else if (button.id == 3) login();
        else if (button.id == 4) loginOffline();
        else if (button.id == 5) edit();
        else if (button.id == 6) delete();
        else if (button.id == 7) mc.displayGuiScreen(prev);
        super.actionPerformed(button);
    }

    @Override
    public void mouseClicked(int mx, int my, int btn) throws IOException {
        if (list.mouseClicked(mx, my, btn) || search.mouseClicked(mx, my, btn)) return;
        super.mouseClicked(mx, my, btn);
    }

    @Override
    public void handleMouseInput() throws IOException {
        list.handleMouseInput();
        super.handleMouseInput();
    }

    @Override
    public void updateScreen() {
        search.updateCursorCounter();
        updateButtons();
    }

    @Override
    public void onGuiClosed() {
        Config.save(mc.gameDir.toPath());
    }

    @Override
    public void drawScreen(int mx, int my, float delta) {
        drawDefaultBackground();
        list.drawScreen(mx, my, delta);
        search.drawTextBox();
        if (search.getText().isEmpty()) drawString(fontRenderer, I18n.format("ias.listGui.search"), search.x + 4, search.y + (search.height - 8) / 2, 0xFF808080);
        super.drawScreen(mx, my, delta);
        drawCenteredString(fontRenderer, "In-Game Account Switcher", this.width / 2, 4, -1);
        if (list.selectedElement() >= 0) {
            mc.getTextureManager().bindTexture(list.entries.get(list.selectedElement()).skin());
            GlStateManager.color(1F, 1F, 1F, 1F);
            boolean slim = list.entries.get(list.selectedElement()).slimSkin();
            GlStateManager.pushMatrix();
            GlStateManager.scale(4, 4, 4);
            GlStateManager.translate(1, height / 8D - 16D - 4D, 0);
            GuiScreen.drawModalRectWithCustomSizedTexture(4, 0, 8, 8, 8, 8, 64, 64); // Head
            GuiScreen.drawModalRectWithCustomSizedTexture(4, 8, 20, 20, 8, 12, 64, 64); // Body
            GuiScreen.drawModalRectWithCustomSizedTexture(slim ? 1 : 0, 8, 44, 20, slim ? 3 : 4, 12, 64, 64); // Right Arm (Left from our perspective)
            GuiScreen.drawModalRectWithCustomSizedTexture(12, 8, 36, 52, slim ? 3 : 4, 12, 64, 64); // Left Arm (Right from our perspective)
            GuiScreen.drawModalRectWithCustomSizedTexture(4, 20, 4, 20, 4, 12, 64, 64); // Right Leg (Left from our perspective)
            GuiScreen.drawModalRectWithCustomSizedTexture(8, 20, 20, 52, 4, 12, 64, 64); // Left Leg (Right from our perspective)
            if (mc.gameSettings.getModelParts().contains(EnumPlayerModelParts.HAT))
                GuiScreen.drawModalRectWithCustomSizedTexture(4, 0, 40, 8, 8, 8, 64, 64); // Head (Overlay)
            if (mc.gameSettings.getModelParts().contains(EnumPlayerModelParts.RIGHT_SLEEVE))
                GuiScreen.drawModalRectWithCustomSizedTexture(slim ? 1 : 0, 8, 44, 36, slim ? 3 : 4, 12, 64, 64); // Right Arm (Overlay)
            if (mc.gameSettings.getModelParts().contains(EnumPlayerModelParts.LEFT_SLEEVE))
                GuiScreen.drawModalRectWithCustomSizedTexture(12, 8, 52, 52, slim ? 3 : 4, 12, 64, 64); // Left Arm (Overlay)
            if (mc.gameSettings.getModelParts().contains(EnumPlayerModelParts.RIGHT_PANTS_LEG))
                GuiScreen.drawModalRectWithCustomSizedTexture(4, 20, 4, 36, 4, 12, 64, 64); // Right Leg (Overlay)
            if (mc.gameSettings.getModelParts().contains(EnumPlayerModelParts.LEFT_PANTS_LEG))
                GuiScreen.drawModalRectWithCustomSizedTexture(8, 20, 4, 52, 4, 12, 64, 64); // Left Leg (Overlay)
            GlStateManager.popMatrix();
        }
        if (state != null) {
            drawCenteredString(fontRenderer, state, this.width / 2, this.height - 62, 0xFFFF9900);
        }
    }

    private void reloadSkins() {
        if (list.entries.isEmpty() || System.currentTimeMillis() <= nextSkinUpdate || state != null) return;
        IAS.SKIN_CACHE.clear();
        list.updateAccounts(search.getText());
        nextSkinUpdate = System.currentTimeMillis() + 15000L;
    }

    private void login() {
        if (list.selectedElement() < 0 || state != null) return;
        Account acc = list.entries.get(list.selectedElement()).account();
        updateButtons();
        state = "";
        acc.login((s, o) -> state = I18n.format(s, o)).whenComplete((d, t) -> {
            state = null;
            if (t != null) {
                mc.addScheduledTask(() -> mc.displayGuiScreen(new IASAlertScreen(() -> mc.displayGuiScreen(this),
                        TextFormatting.RED + I18n.format("ias.error"),
                        String.valueOf(t))));
                return;
            }
            mc.addScheduledTask(() -> {
                mc.session = new Session(d.name(), UUIDTypeAdapter.fromUUID(d.uuid()), d.accessToken(), d.userType());
            });
        });
    }

    private void loginOffline() {
        if (list.selectedElement() < 0 || state != null) return;
        Account acc = list.entries.get(list.selectedElement()).account();
        mc.session = new Session(acc.name(), UUIDTypeAdapter.fromUUID(UUID
                .nameUUIDFromBytes("OfflinePlayer".concat(acc.name()).getBytes(StandardCharsets.UTF_8))),
                "0", "legacy");
    }

    private void add() {
        if (state != null) return;
        mc.displayGuiScreen(new LoginScreen(this, I18n.format("ias.loginGui.add"),
                I18n.format("ias.loginGui.add.button"),
                I18n.format("ias.loginGui.add.button.tooltip"), acc -> {
            Config.accounts.add(acc);
            Config.save(mc.gameDir.toPath());
            list.updateAccounts(search.getText());
        }));
    }

    public void edit() {
        if (list.selectedElement() < 0 || state != null) return;
        Account acc = list.entries.get(list.selectedElement()).account();
        mc.displayGuiScreen(new LoginScreen(this, I18n.format("ias.loginGui.edit"),
                I18n.format("ias.loginGui.edit.button"),
                I18n.format("ias.loginGui.edit.button.tooltip"), newAcc -> {
            Config.accounts.set(Config.accounts.indexOf(acc), newAcc);
            Config.save(mc.gameDir.toPath());
        }));
    }

    public void delete() {
        if (list.selectedElement() < 0 || state != null) return;
        Account acc = list.entries.get(list.selectedElement()).account();
        if (isShiftKeyDown()) {
            Config.accounts.remove(acc);
            Config.save(mc.gameDir.toPath());
            updateButtons();
            list.updateAccounts(search.getText());
            return;
        }
        mc.displayGuiScreen(new GuiYesNo((b, id) -> {
            if (b) {
                Config.accounts.remove(acc);
                updateButtons();
                list.updateAccounts(search.getText());
            }
            mc.displayGuiScreen(this);
        }, I18n.format("ias.deleteGui.title"), I18n.format("ias.deleteGui.text", acc.name()), 0));
    }

    private void updateButtons() {
        login.enabled = list.selectedElement() >= 0 && state == null;
        loginOffline.enabled = list.selectedElement() >= 0;
        add.enabled = state == null;
        edit.enabled = list.selectedElement() >= 0 && state == null;
        delete.enabled = list.selectedElement() >= 0 && state == null;
        reloadSkins.enabled = list.selectedElement() >= 0 && state == null && System.currentTimeMillis() > nextSkinUpdate;
    }

    @Override
    public void keyTyped(char c, int key) throws IOException {
        if (search.textboxKeyTyped(c, key)) return;
        if (key == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(prev);
            return;
        }
        if (key == Keyboard.KEY_F5 || key == Keyboard.KEY_R) {
            reloadSkins();
            return;
        }
        if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
            if (GuiScreen.isShiftKeyDown()) loginOffline();
            else login();
            return;
        }
        if (key == Keyboard.KEY_A || key == Keyboard.KEY_EQUALS || key == Keyboard.KEY_ADD) {
            add();
            return;
        }
        if (key == Keyboard.KEY_PERIOD || key == Keyboard.KEY_DIVIDE) {
            edit();
            return;
        }
        if (key == Keyboard.KEY_DELETE || key == Keyboard.KEY_MINUS || key == Keyboard.KEY_SUBTRACT) {
            delete();
            return;
        }
        super.keyTyped(c, key);
    }
}
