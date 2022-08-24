package the_fireplace.ias;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.Expression;
import ru.vidtu.ias.gui.IASIconButton;
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
        Config.load(Minecraft.getMinecraft().mcDataDir.toPath());
    }

    @SubscribeEvent
    public void onScreenInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.gui instanceof GuiMultiplayer && Config.multiplayerScreenButton) {
            int bx = event.gui.width / 2 + 4 + 76 + 79;
            int by = event.gui.height - 28;
            try {
                bx = (int) Expression.parseWidthHeight(Config.titleScreenButtonX, event.gui.width, event.gui.height);
                by = (int) Expression.parseWidthHeight(Config.titleScreenButtonY, event.gui.width, event.gui.height);
            } catch (Throwable t) {
                bx = event.gui.width / 2 + 4 + 76 + 79;
                by = event.gui.height - 28;
            }
            event.buttonList.add(button = new IASIconButton(104027, bx, by, 20, 20));
        }
        if (event.gui instanceof GuiMainMenu) {
            if (Config.titleScreenButton) {
                int bx = event.gui.width / 2 + 104;
                int by = event.gui.height / 4 + 48 + 72 + -24;
                try {
                    bx = (int) Expression.parseWidthHeight(Config.titleScreenButtonX, event.gui.width, event.gui.height);
                    by = (int) Expression.parseWidthHeight(Config.titleScreenButtonY, event.gui.width, event.gui.height);
                } catch (Throwable t) {
                    bx = event.gui.width / 2 + 104;
                    by = event.gui.height / 4 + 48 + 72 + -24;
                }
                event.buttonList.add(button = new IASIconButton(104027, bx, by, 20, 20));
            }
            if (Config.titleScreenText) {
                try {
                    tx = (int) Expression.parseWidthHeight(Config.titleScreenTextX, event.gui.width, event.gui.height);
                    ty = (int) Expression.parseWidthHeight(Config.titleScreenTextY, event.gui.width, event.gui.height);
                } catch (Throwable t) {
                    tx = event.gui.width / 2;
                    ty = event.gui.height / 4 + 48 + 72 + 12 + 22;
                }
            }
        }
    }

    @SubscribeEvent
    public void onScreenAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if ((event.gui instanceof GuiMainMenu || event.gui instanceof GuiMultiplayer) && event.button.id == 104027) {
            event.gui.mc.displayGuiScreen(new AccountListScreen(event.gui));
        }
    }

    @SubscribeEvent
    public void onScreenRender(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.gui instanceof GuiMainMenu && Config.titleScreenText) {
            if (Config.titleScreenTextAlignment == Config.Alignment.LEFT) {
                event.gui.drawString(event.gui.mc.fontRendererObj, I18n.format("ias.title", event.gui.mc.getSession().getUsername()), tx, ty, 0xFFCC8888);
            } else if (Config.titleScreenTextAlignment == Config.Alignment.RIGHT) {
                event.gui.drawString(event.gui.mc.fontRendererObj, I18n.format("ias.title", event.gui.mc.getSession().getUsername()), tx - event.gui.mc.fontRendererObj.getStringWidth(I18n.format("ias.title", event.gui.mc.getSession().getUsername())), ty, 0xFFCC8888);
            } else {
                event.gui.drawCenteredString(event.gui.mc.fontRendererObj, I18n.format("ias.title", event.gui.mc.getSession().getUsername()), tx, ty, 0xFFCC8888);
            }
        }
        if (button != null && event.gui.buttonList.contains(button) && button.isMouseOver()) {
            event.gui.drawCreativeTabHoveringText("In-Game Account Switcher", event.mouseX, event.mouseY);
        }
    }
}
