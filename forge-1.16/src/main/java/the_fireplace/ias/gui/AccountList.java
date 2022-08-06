package the_fireplace.ias.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.account.Account;
import the_fireplace.ias.IAS;

import java.util.Locale;

public class AccountList extends ExtendedList<AccountList.AccountEntry> {
    public AccountList(Minecraft mc, int width, int height) {
        super(mc, width, height, 32, height - 64, 14);
    }

    public void updateAccounts(String query) {
        clearEntries();
        Config.accounts.stream()
                .filter(acc -> query.trim().isEmpty() || acc.name().toLowerCase(Locale.ROOT)
                        .startsWith(query.toLowerCase(Locale.ROOT)))
                .forEach(acc -> addEntry(new AccountEntry(acc)));
        setSelected(children().isEmpty() ? null : getEntry(0));
    }

    public void swap(int first, int second) {
        Account account = Config.accounts.get(first);
        Config.accounts.set(first, Config.accounts.get(second));
        Config.accounts.set(second, account);
        Config.save(minecraft.gameDirectory.toPath());
        AccountEntry entry = children().get(first);
        children().set(first, children().get(second));
        children().set(second, entry);
        setSelected(entry);
    }

    public class AccountEntry extends ExtendedList.AbstractListEntry<AccountEntry> {
        private final Account account;
        private ResourceLocation skin;
        private boolean slimSkin;
        public AccountEntry(Account account) {
            this.account = account;
            if (IAS.SKIN_CACHE.containsKey(account.uuid())) {
                this.skin = IAS.SKIN_CACHE.get(account.uuid());
                return;
            }
            skin = DefaultPlayerSkin.getDefaultSkin(account.uuid());
            slimSkin = DefaultPlayerSkin.getSkinModelName(account.uuid()).equalsIgnoreCase("slim");
            minecraft.getSkinManager().registerSkins(new GameProfile(account.uuid(), account.name()), (type, loc, tex) -> {
                if (type == MinecraftProfileTexture.Type.SKIN) {
                    skin = loc;
                    slimSkin = "slim".equalsIgnoreCase(tex.getMetadata("model"));
                    IAS.SKIN_CACHE.put(account.uuid(), loc);
                }
            }, true);
        }

        public Account account() {
            return account;
        }

        public ResourceLocation skin() {
            return skin;
        }

        public boolean slimSkin() {
            return slimSkin;
        }

        @Override
        public void render(MatrixStack ms, int i, int y, int x, int w, int h, int mx, int my, boolean hover, float delta) {
            int color = -1;
            if (minecraft.getUser().getName().equals(account.name())) color = 0x00FF00;
            drawString(ms, minecraft.font, account.name(), x + 10, y + 1, color);
            RenderSystem.color4f(1F, 1F, 1F, 1F);
            minecraft.getTextureManager().bind(skin());
            Screen.blit(ms, x, y + 1, 8, 8, 8, 8, 64, 64); // Head
            if (minecraft.options.getModelParts().contains(PlayerModelPart.HAT))
                Screen.blit(ms, x, y + 1, 40, 8, 8, 8, 64, 64); // Head (Overlay)
            if (getSelected() == this) {
                minecraft.getTextureManager().bind(new ResourceLocation("textures/gui/server_selection.png"));
                RenderSystem.color4f(1F, 1F, 1F, 1F);
                boolean movableDown = i + 1 < children().size();
                boolean movableUp = i > 0;
                if (movableDown) {
                    boolean hoveredDown = mx > x + w - 16 && mx < x + w - 6 && hover;
                    Screen.blit(ms, x + w - 35, y - 18, 48, hoveredDown ? 32 : 0, 32, 32, 256, 256);
                }
                if (movableUp) {
                    boolean hoveredUp = mx > x + w - (movableDown ? 28 : 16) && mx < x + w - (movableDown ? 16 : 6) && hover;
                    Screen.blit(ms, x + w - (movableDown ? 30 : 19), y - 3, 96, hoveredUp ? 32 : 0, 32, 32, 256, 256);
                }
            }
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (button == 0 && getSelected() == this) {
                int x = getRowLeft();
                int w = getRowWidth();
                int i = children().indexOf(this);
                boolean movableDown = i + 1 < children().size();
                boolean movableUp = i > 0;
                if (movableDown) {
                    boolean hoveredDown = mx > x + w - 16 && mx < x + w - 6;
                    if (hoveredDown) {
                        minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
                        swap(i, i + 1);
                    }
                }
                if (movableUp) {
                    boolean hoveredUp = mx > x + w - (movableDown ? 28 : 16) && mx < x + w - (movableDown ? 16 : 6);
                    if (hoveredUp) {
                        minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
                        swap(i, i - 1);
                    }
                }
                return true;
            }
            setSelected(this);
            return true;
        }
    }
}
