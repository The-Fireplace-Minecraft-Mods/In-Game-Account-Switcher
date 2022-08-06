package the_fireplace.ias;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
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
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
                () -> (minecraft, screen) -> new IASConfigScreen(screen));
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (s, b) -> true));
    }

    @SubscribeEvent
    public void onClient(FMLClientSetupEvent event) {
        Config.load(Minecraft.getInstance().gameDirectory.toPath());
    }

    @SubscribeEvent
    public void onScreenInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof MultiplayerScreen && Config.multiplayerScreenButton) {
            int bx = event.getGui().width / 2 + 4 + 76 + 79;
            int by = event.getGui().height - 28;
            try {
                bx = (int) Expression.parseWidthHeight(Config.titleScreenButtonX, event.getGui().width, event.getGui().height);
                by = (int) Expression.parseWidthHeight(Config.titleScreenButtonY, event.getGui().width, event.getGui().height);
            } catch (Throwable t) {
                bx = event.getGui().width / 2 + 4 + 76 + 79;
                by = event.getGui().height - 28;
            }
            event.addWidget(new ImageButton(bx, by, 20, 20, 0, 0, 20, IAS_BUTTON,
                    256, 256, btn -> event.getGui().getMinecraft().setScreen(new AccountListScreen(event.getGui())), (button, ms, mx, my)
                    -> event.getGui().renderTooltip(ms, new StringTextComponent("In-Game Account Switcher"), mx, my),
                    new StringTextComponent("In-Game Account Switcher")));
        }
        if (event.getGui() instanceof MainMenuScreen) {
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
                event.addWidget(new ImageButton(bx, by, 20, 20, 0, 0, 20, IAS_BUTTON,
                        256, 256, btn -> event.getGui().getMinecraft().setScreen(new AccountListScreen(event.getGui())), (button, ms, mx, my)
                        -> event.getGui().renderTooltip(ms, new StringTextComponent("In-Game Account Switcher"), mx, my),
                        new StringTextComponent("In-Game Account Switcher")));
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
    public void onScreenRender(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.getGui() instanceof MainMenuScreen && Config.titleScreenText) {
            if (Config.titleScreenTextAlignment == Config.Alignment.LEFT) {
                AbstractGui.drawString(event.getMatrixStack(), event.getGui().getMinecraft().font, new TranslationTextComponent("ias.title", event.getGui().getMinecraft().getUser().getName()), tx, ty, 0xFFCC8888);
                return;
            }
            if (Config.titleScreenTextAlignment == Config.Alignment.RIGHT) {
                AbstractGui.drawString(event.getMatrixStack(), event.getGui().getMinecraft().font, new TranslationTextComponent("ias.title", event.getGui().getMinecraft().getUser().getName()), tx - event.getGui().getMinecraft().font.width(new TranslationTextComponent("ias.title", event.getGui().getMinecraft().getUser().getName())), ty, 0xFFCC8888);
                return;
            }
            AbstractGui.drawCenteredString(event.getMatrixStack(), event.getGui().getMinecraft().font, new TranslationTextComponent("ias.title", event.getGui().getMinecraft().getUser().getName()), tx, ty, 0xFFCC8888);
        }
    }
}
