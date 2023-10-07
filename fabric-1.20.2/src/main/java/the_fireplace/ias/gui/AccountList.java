package the_fireplace.ias.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.PlayerModelPart;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.SharedIAS;
import ru.vidtu.ias.account.Account;
import the_fireplace.ias.IAS;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class AccountList extends ObjectSelectionList<AccountList.AccountEntry> {
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

    public class AccountEntry extends ObjectSelectionList.Entry<AccountEntry> {
        static final ResourceLocation MOVE_UP_HIGHLIGHTED_SPRITE = new ResourceLocation("transferable_list/move_up_highlighted");
        static final ResourceLocation MOVE_UP_SPRITE = new ResourceLocation("transferable_list/move_up");
        static final ResourceLocation MOVE_DOWN_HIGHLIGHTED_SPRITE = new ResourceLocation("transferable_list/move_down_highlighted");
        static final ResourceLocation MOVE_DOWN_SPRITE = new ResourceLocation("transferable_list/move_down");
        private final Account account;
        private PlayerSkin skin;

        public AccountEntry(Account account) {
            this.account = account;
            if (IAS.SKIN_CACHE.containsKey(account.uuid())) {
                this.skin = IAS.SKIN_CACHE.get(account.uuid());
                return;
            }
            skin = DefaultPlayerSkin.get(account.uuid());
            CompletableFuture.supplyAsync(() -> {
                ProfileResult result = minecraft.getMinecraftSessionService().fetchProfile(account.uuid(), false);
                if (result == null) return null;
                return result.profile();
            }, SharedIAS.EXECUTOR).thenComposeAsync(profile -> {
                if (profile == null) return CompletableFuture.completedFuture(DefaultPlayerSkin.get(account.uuid()));
                return minecraft.getSkinManager().getOrLoad(profile);
            }, minecraft).thenAcceptAsync(skin -> {
                this.skin = skin;
                IAS.SKIN_CACHE.put(account.uuid(), skin);
            }, minecraft);
        }

        public Account account() {
            return account;
        }

        public PlayerSkin skin() {
            return skin;
        }

        @Override
        public void render(GuiGraphics ctx, int i, int y, int x, int w, int h, int mx, int my, boolean hover, float delta) {
            int color = -1;
            if (minecraft.getUser().getName().equals(account.name())) color = 0x00FF00;
            ctx.drawString(minecraft.font, account.name(), x + 10, y + 1, color);
            ResourceLocation tex = skin.texture();
            ctx.blit(tex, x, y + 1, 8, 8, 8, 8, 64, 64); // Head
            if (minecraft.options.isModelPartEnabled(PlayerModelPart.HAT))
                ctx.blit(tex, x, y + 1, 40, 8, 8, 8, 64, 64); // Head (Overlay)
            if (getSelected() == this) {
                boolean movableDown = i + 1 < children().size();
                boolean movableUp = i > 0;
                if (movableDown) {
                    boolean hoveredDown = mx > x + w - 16 && mx < x + w - 6 && hover;
                    ctx.blitSprite(hoveredDown ? MOVE_DOWN_HIGHLIGHTED_SPRITE : MOVE_DOWN_SPRITE, x + w - 35, y - 18, 32, 32);
                }
                if (movableUp) {
                    boolean hoveredUp = mx > x + w - (movableDown ? 28 : 16) && mx < x + w - (movableDown ? 16 : 6) && hover;
                    ctx.blitSprite(hoveredUp ? MOVE_UP_HIGHLIGHTED_SPRITE : MOVE_UP_SPRITE, x + w - (movableDown ? 30 : 19) - 16, y - 3, 96, 32, 32);
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
                        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
                        swap(i, i + 1);
                    }
                }
                if (movableUp) {
                    boolean hoveredUp = mx > x + w - (movableDown ? 28 : 16) && mx < x + w - (movableDown ? 16 : 6);
                    if (hoveredUp) {
                        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
                        swap(i, i - 1);
                    }
                }
                return true;
            }
            setSelected(this);
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(account.name());
        }
    }
}
