package the_fireplace.ias;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.Expression;
import ru.vidtu.ias.gui.IASConfigScreen;
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
@Mod("ias")
public class IAS {
    public static final ResourceLocation IAS_BUTTON = new ResourceLocation("ias", "textures/gui/iasbutton.png");
    public static final Map<UUID, ResourceLocation> SKIN_CACHE = new HashMap<>();
    private static int tx;
    private static int ty;

    public IAS() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClient);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new IASConfigScreen(screen)));
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (s, b) -> true));
    }

    @SubscribeEvent
    public void onClient(FMLClientSetupEvent event) {
        Config.load(Minecraft.getInstance().gameDirectory.toPath());
    }

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.InitScreenEvent.Post event) {
        if (event.getScreen() instanceof JoinMultiplayerScreen && Config.multiplayerScreenButton) {
            int bx = event.getScreen().width / 2 + 4 + 76 + 79;
            int by = event.getScreen().height - 28;
            try {
                bx = (int) Expression.parseWidthHeight(Config.titleScreenButtonX, event.getScreen().width, event.getScreen().height);
                by = (int) Expression.parseWidthHeight(Config.titleScreenButtonY, event.getScreen().width, event.getScreen().height);
            } catch (Throwable t) {
                bx = event.getScreen().width / 2 + 4 + 76 + 79;
                by = event.getScreen().height - 28;
            }
            event.addListener(new ImageButton(bx, by, 20, 20, 0, 0, 20, IAS_BUTTON,
                    256, 256, btn -> event.getScreen().getMinecraft().setScreen(new AccountListScreen(event.getScreen())), (button, ms, mx, my)
                    -> event.getScreen().renderTooltip(ms, new TextComponent("In-Game Account Switcher"), mx, my),
                    new TextComponent("In-Game Account Switcher")));
        }
        if (event.getScreen() instanceof TitleScreen) {
            if (Config.titleScreenButton) {
                int bx = event.getScreen().width / 2 + 104;
                int by = event.getScreen().height / 4 + 48 + 72 + -24;
                try {
                    bx = (int) Expression.parseWidthHeight(Config.titleScreenButtonX, event.getScreen().width, event.getScreen().height);
                    by = (int) Expression.parseWidthHeight(Config.titleScreenButtonY, event.getScreen().width, event.getScreen().height);
                } catch (Throwable t) {
                    bx = event.getScreen().width / 2 + 104;
                    by = event.getScreen().height / 4 + 48 + 72 + -24;
                }
                event.addListener(new ImageButton(bx, by, 20, 20, 0, 0, 20, IAS_BUTTON,
                        256, 256, btn -> event.getScreen().getMinecraft().setScreen(new AccountListScreen(event.getScreen())), (button, ms, mx, my)
                        -> event.getScreen().renderTooltip(ms, new TextComponent("In-Game Account Switcher"), mx, my),
                        new TextComponent("In-Game Account Switcher")));
            }
            if (Config.titleScreenText) {
                try {
                    tx = (int) Expression.parseWidthHeight(Config.titleScreenTextX, event.getScreen().width, event.getScreen().height);
                    ty = (int) Expression.parseWidthHeight(Config.titleScreenTextY, event.getScreen().width, event.getScreen().height);
                } catch (Throwable t) {
                    tx = event.getScreen().width / 2;
                    ty = event.getScreen().height / 4 + 48 + 72 + 12 + 22;
                }
            }
        }
    }

    @SubscribeEvent
    public void onScreenRender(ScreenEvent.DrawScreenEvent.Post event) {
        if (event.getScreen() instanceof TitleScreen && Config.titleScreenText) {
            if (Config.titleScreenTextAlignment == Config.Alignment.LEFT) {
                Gui.drawString(event.getPoseStack(), event.getScreen().getMinecraft().font, new TranslatableComponent("ias.title", event.getScreen().getMinecraft().getUser().getName()), tx, ty, 0xFFCC8888);
                return;
            }
            if (Config.titleScreenTextAlignment == Config.Alignment.RIGHT) {
                Gui.drawString(event.getPoseStack(), event.getScreen().getMinecraft().font, new TranslatableComponent("ias.title", event.getScreen().getMinecraft().getUser().getName()), tx - event.getScreen().getMinecraft().font.width(new TranslatableComponent("ias.title", event.getScreen().getMinecraft().getUser().getName())), ty, 0xFFCC8888);
                return;
            }
            Gui.drawCenteredString(event.getPoseStack(), event.getScreen().getMinecraft().font, new TranslatableComponent("ias.title", event.getScreen().getMinecraft().getUser().getName()), tx, ty, 0xFFCC8888);
        }
    }
}
