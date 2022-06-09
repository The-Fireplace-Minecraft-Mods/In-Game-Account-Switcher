package the_fireplace.ias;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IReorderingProcessor;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.gui.IASConfigScreen;
import ru.vidtu.ias.utils.SkinRenderer;
import the_fireplace.ias.gui.GuiAccountSelector;
import the_fireplace.ias.gui.GuiButtonWithImage;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author The_Fireplace
 */
@Mod("ias")
public class IAS {
    public static final Logger LOG = LogManager.getLogger("IAS");
    public static final Gson GSON = new Gson();
    public static final Executor EXECUTOR = Executors.newSingleThreadExecutor(r -> new Thread(r, "IAS Thread"));
    private static int textX, textY;

    public IAS() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClient);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
                () -> (minecraft, screen) -> new IASConfigScreen(screen));
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair
                .of(() -> FMLNetworkConstants.IGNORESERVERONLY, (s, b) -> true));
    }

    @SubscribeEvent
    public void onClient(FMLClientSetupEvent event) {
        Minecraft mc = event.getMinecraftSupplier().get();
        Config.load();
        SkinRenderer.loadAllAsync(mc, false, () -> {});
    }

    @SubscribeEvent
    public void onScreenInit(GuiScreenEvent.InitGuiEvent event) {
        if (event.getGui() instanceof MultiplayerScreen) {
            if (Config.showOnMPScreen) {
                event.addWidget(new GuiButtonWithImage(event.getGui().width / 2 + 4 + 76 + 79, event.getGui().height - 28, btn -> {
                    event.getGui().getMinecraft().setScreen(new GuiAccountSelector(event.getGui()));
                }));
            }
        }
        if (event.getGui() instanceof MainMenuScreen) {
            try {
                if (StringUtils.isNotBlank(Config.textX) && StringUtils.isNotBlank(Config.textY)) {
                    textX = Integer.parseInt(Config.textX);
                    textY = Integer.parseInt(Config.textY);
                } else {
                    textX = event.getGui().width / 2;
                    textY = event.getGui().height / 4 + 48 + 72 + 12 + 22;
                }
            } catch (Throwable t) {
                textX = event.getGui().width / 2;
                textY = event.getGui().height / 4 + 48 + 72 + 12 + 22;
            }
            if (Config.showOnTitleScreen) {
                int buttonX = event.getGui().width / 2 + 104;
                int buttonY = event.getGui().height / 4 + 48 + 72 - 24;
                try {
                    if (StringUtils.isNotBlank(Config.btnX) && StringUtils.isNotBlank(Config.btnY)) {
                        buttonX = Integer.parseInt(Config.btnX);
                        buttonY = Integer.parseInt(Config.btnY);
                    }
                } catch (Throwable t) {
                    buttonX = event.getGui().width / 2 + 104;
                    buttonY = event.getGui().height / 4 + 48 + 72 - 24;
                }
                event.addWidget(new GuiButtonWithImage(buttonX, buttonY, btn -> event.getGui().getMinecraft().setScreen(new GuiAccountSelector(event.getGui()))));
            }
        }
    }

    @SubscribeEvent
    public void onScreenRender(GuiScreenEvent.DrawScreenEvent event) {
        if (event.getGui() instanceof MultiplayerScreen) {
            if (event.getGui().getMinecraft().getUser().getAccessToken().equals("0") || event.getGui().getMinecraft().getUser().getAccessToken().equals("-")) {
                List<IReorderingProcessor> list = event.getGui().getMinecraft().font.split(new TranslationTextComponent("ias.offlinemode"), event.getGui().width);
                for (int i = 0; i < list.size(); i++) {
                    event.getGui().getMinecraft().font.draw(event.getMatrixStack(), list.get(i), event.getGui().width / 2 - event.getGui().getMinecraft().font.width(list.get(i)) / 2, i * 9 + 1, 16737380);
                }
            }
        }
        if (event.getGui() instanceof MainMenuScreen) {
            Screen.drawCenteredString(event.getMatrixStack(), event.getGui().getMinecraft().font, I18n.get("ias.loggedinas", event.getGui().getMinecraft().getUser().getName()), textX, textY, 0xFFCC8888);
        }
    }
}
