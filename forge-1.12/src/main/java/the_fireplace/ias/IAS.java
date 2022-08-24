package the_fireplace.ias;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.GuiAccessDenied;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.Expression;
import the_fireplace.ias.gui.AccountListScreen;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Main In-Game Account Switcher class.
 *
 * @author The_Fireplace
 * @author VidTu
 */
@Mod(modid = "ias", name = "In-Game Account Switcher", version = "D7.29 :P", clientSideOnly = true, useMetadata = true,
        guiFactory = "ru.vidtu.ias.gui.IASGuiFactory", acceptedMinecraftVersions = "1.12.2",
        updateJSON = "https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/raw/main/updater-forge.json")
public class IAS {
    public static final ResourceLocation IAS_BUTTON = new ResourceLocation("ias", "textures/gui/iasbutton.png");
    public static final Map<UUID, ResourceLocation> SKIN_CACHE = new HashMap<>();
    private static int tx;
    private static int ty;
    private static GuiButton button;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        Config.load(Minecraft.getMinecraft().gameDir.toPath());
    }

    @SubscribeEvent
    public void onScreenInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiMultiplayer && Config.multiplayerScreenButton) {
            int bx = event.getGui().width / 2 + 4 + 76 + 79;
            int by = event.getGui().height - 28;
            try {
                bx = (int) Expression.parseWidthHeight(Config.titleScreenButtonX, event.getGui().width, event.getGui().height);
                by = (int) Expression.parseWidthHeight(Config.titleScreenButtonY, event.getGui().width, event.getGui().height);
            } catch (Throwable t) {
                bx = event.getGui().width / 2 + 4 + 76 + 79;
                by = event.getGui().height - 28;
            }
            event.getButtonList().add(button = new GuiButtonImage(104027, bx, by, 20, 20, 0, 0, 20,
                    IAS_BUTTON) {
                @Override
                public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
                    GlStateManager.color(1F, 1F, 1F); // Anti-Mojang.
                    super.drawButton(mc, mouseX, mouseY, partialTicks);
                }
            });
        }
        if (event.getGui() instanceof GuiMainMenu) {
            if (Config.titleScreenButton) {
                int bx = event.getGui().width / 2 + 104;
                int by = event.getGui().height / 4 + 48 + 72 + -24;
                try {
                    bx = (int) Expression.parseWidthHeight(Config.titleScreenButtonX, event.getGui().width, event.getGui().height);
                    by = (int) Expression.parseWidthHeight(Config.titleScreenButtonY, event.getGui().width, event.getGui().height);
                } catch (Throwable t) {
                    bx = event.getGui().width / 2 + 104;
                    by = event.getGui().height / 4 + 48 + 72 + -24;
                }
                event.getButtonList().add(button = new GuiButtonImage(104027, bx, by, 20, 20, 0, 0, 20,
                        IAS_BUTTON) {
                    @Override
                    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
                        GlStateManager.color(1F, 1F, 1F); // Anti-Mojang.
                        super.drawButton(mc, mouseX, mouseY, partialTicks);
                    }
                });
            }
            if (Config.titleScreenText) {
                try {
                    tx = (int) Expression.parseWidthHeight(Config.titleScreenTextX, event.getGui().width, event.getGui().height);
                    ty = (int) Expression.parseWidthHeight(Config.titleScreenTextY, event.getGui().width, event.getGui().height);
                } catch (Throwable t) {
                    tx = event.getGui().width / 2;
                    ty = event.getGui().height / 4 + 48 + 72 + 12 + 22;
                }
            }
        }
    }

    @SubscribeEvent
    public void onScreenAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if ((event.getGui() instanceof GuiMainMenu || event.getGui() instanceof GuiMultiplayer) && event.getButton().id == 104027) {
            event.getGui().mc.displayGuiScreen(new AccountListScreen(event.getGui()));
        }
    }

    @SubscribeEvent
    public void onScreenRender(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.getGui() instanceof GuiMainMenu && Config.titleScreenText) {
            if (Config.titleScreenTextAlignment == Config.Alignment.LEFT) {
                event.getGui().drawString(event.getGui().mc.fontRenderer, I18n.format("ias.title", event.getGui().mc.getSession().getUsername()), tx, ty, 0xFFCC8888);
            } else if (Config.titleScreenTextAlignment == Config.Alignment.RIGHT) {
                event.getGui().drawString(event.getGui().mc.fontRenderer, I18n.format("ias.title", event.getGui().mc.getSession().getUsername()), tx - event.getGui().mc.fontRenderer.getStringWidth(I18n.format("ias.title", event.getGui().mc.getSession().getUsername())), ty, 0xFFCC8888);
            } else {
                event.getGui().drawCenteredString(event.getGui().mc.fontRenderer, I18n.format("ias.title", event.getGui().mc.getSession().getUsername()), tx, ty, 0xFFCC8888);
            }
        }
        if (button != null && event.getGui().buttonList.contains(button) && button.isMouseOver()) {
            event.getGui().drawHoveringText("In-Game Account Switcher", event.getMouseX(), event.getMouseY());
        }
    }
}
