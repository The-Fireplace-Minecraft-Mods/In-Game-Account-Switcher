package the_fireplace.ias.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.legacy.SkinLoader;
import the_fireplace.ias.IAS;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountList extends GuiListExtended {
    public final Minecraft mc = Minecraft.getMinecraft();
    public final List<AccountEntry> entries = new ArrayList<>();
    public int selected;
    public AccountList(Minecraft mc, int width, int height) {
        super(mc, width, height, 32, height - 64, 14);
    }

    public void updateAccounts(String query) {
        entries.clear();
        Config.accounts.stream()
                .filter(acc -> query.trim().isEmpty() || acc.name().toLowerCase(Locale.ROOT)
                        .startsWith(query.toLowerCase(Locale.ROOT)))
                .forEach(acc -> entries.add(new AccountEntry(acc)));
        selected = entries.isEmpty() ? -1 : 0;
    }

    public void swap(int first, int second) {
        Account account = Config.accounts.get(first);
        Config.accounts.set(first, Config.accounts.get(second));
        Config.accounts.set(second, account);
        Config.save(mc.mcDataDir.toPath());
        AccountEntry entry = entries.get(first);
        entries.set(first, entries.get(second));
        entries.set(second, entry);
        selected = second;
    }

    @Override
    public int getSize() {
        return entries.size();
    }

    @Override
    public IGuiListEntry getListEntry(int index) {
        return entries.get(index);
    }

    public int selectedElement() {
        return selected;
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return slotIndex == selected;
    }

    public class AccountEntry implements IGuiListEntry {
        private final Account account;
        private ResourceLocation skin;
        private boolean slimSkin;
        public AccountEntry(Account account) {
            this.account = account;
            if (IAS.SKIN_CACHE.containsKey(account.uuid())) {
                this.skin = IAS.SKIN_CACHE.get(account.uuid());
                return;
            }
            skin = AbstractClientPlayer.locationStevePng;
            SkinLoader.loadSkin(account.uuid()).thenAccept(en -> {
                if (en == null) return;
                BufferedImage skinImage = new ImageBufferDownload().parseUserSkin(en.getKey());
                slimSkin = en.getValue();
                mc.func_152344_a(() -> {
                    skin = mc.getTextureManager().getDynamicTextureLocation("ias_skin:" + account.uuid().toString()
                            .replace("-", ""), new DynamicTexture(skinImage));
                    IAS.SKIN_CACHE.put(account.uuid(), skin);
                }); // mc.addScheduledTask
            });
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
        public void drawEntry(int i, int x, int y, int w, int h, Tessellator tess, int mx, int my, boolean hover) {
            int color = -1;
            if (mc.getSession().getUsername().equals(account.name())) color = 0x00FF00;
            mc.fontRenderer.drawString(account.name(), x + 10, y + 1, color);
            GL11.glColor4f(1F, 1F, 1F, 1F);
            mc.getTextureManager().bindTexture(skin());
            // FIXME:
//            GuiScreen.func_146110_a(x, y + 1, 8, 8, 8, 8, 64, 64); // Head -> GuiScreen.drawModalRectWithCustomSizedTexture
//            GuiScreen.func_146110_a(x, y + 1, 40, 8, 8, 8, 64, 64); // Head (Overlay) -> GuiScreen.drawModalRectWithCustomSizedTexture
            if (selected == i) {
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/resource_packs.png"));
                GL11.glColor4f(1F, 1F, 1F, 1F);
                boolean movableDown = i + 1 < entries.size();
                boolean movableUp = i > 0;
                if (movableDown) {
                    boolean hoveredDown = mx > x + w - 16 && mx < x + w - 6 && hover;
                    // FIXME:
//                    GuiScreen.func_146110_a(x + w - 35, y - 18, 48, hoveredDown ? 32 : 0, 32, 32, 256, 256); // drawModalRectWithCustomSizedTexture
                }
                if (movableUp) {
                    boolean hoveredUp = mx > x + w - (movableDown ? 28 : 16) && mx < x + w - (movableDown ? 16 : 6) && hover;
                    // FIXME:
//                    GuiScreen.func_146110_a(x + w - (movableDown ? 30 : 19), y - 3, 96, hoveredUp ? 32 : 0, 32, 32, 256, 256); // drawModalRectWithCustomSizedTexture
                }
            }
        }

        @Override
        public boolean mousePressed(int i, int mx, int my, int button, int rx, int ry) {
            if (button == 0 && selected == i) {
                int w = getListWidth();
                boolean movableDown = i + 1 < entries.size();
                boolean movableUp = i > 0;
                if (movableDown) {
                    boolean hoveredDown = rx > w - 16 && rx < w - 6;
                    if (hoveredDown) {
                        mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1F)); // PositionedSoundRecord.create
                        swap(i, i + 1);
                    }
                }
                if (movableUp) {
                    boolean hoveredUp = rx > w - (movableDown ? 28 : 16) && rx < w - (movableDown ? 16 : 6);
                    if (hoveredUp) {
                        mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1F)); // PositionedSoundRecord.create
                        swap(i, i - 1);
                    }
                }
                return true;
            }
            selected = i;
            return true;
        }

        @Override
        public void mouseReleased(int i, int x, int y, int btn, int rx, int ry) {

        }
    }
}
