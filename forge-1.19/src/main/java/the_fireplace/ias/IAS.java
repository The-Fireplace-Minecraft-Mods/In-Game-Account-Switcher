package the_fireplace.ias;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
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
import org.apache.commons.lang3.StringUtils;
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
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new IASConfigScreen(screen)));
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (s, b) -> true));
    }

    @SubscribeEvent
    public void onClient(FMLClientSetupEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Config.load();
        SkinRenderer.loadAllAsync(mc, false, () -> {});
    }

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.InitScreenEvent event) {
        if (event.getScreen() instanceof JoinMultiplayerScreen) {
            if (Config.showOnMPScreen) {
                event.addListener(new GuiButtonWithImage(event.getScreen().width / 2 + 4 + 76 + 79, event.getScreen().height - 28, btn -> {
                    event.getScreen().getMinecraft().setScreen(new GuiAccountSelector(event.getScreen()));
                }));
            }
        }
        if (event.getScreen() instanceof TitleScreen) {
            try {
                if (StringUtils.isNotBlank(Config.textX) && StringUtils.isNotBlank(Config.textY)) {
                    textX = Integer.parseInt(Config.textX);
                    textY = Integer.parseInt(Config.textY);
                } else {
                    textX = event.getScreen().width / 2;
                    textY = event.getScreen().height / 4 + 48 + 72 + 12 + 22;
                }
            } catch (Throwable t) {
                textX = event.getScreen().width / 2;
                textY = event.getScreen().height / 4 + 48 + 72 + 12 + 22;
            }
            if (Config.showOnTitleScreen) {
                int buttonX = event.getScreen().width / 2 + 104;
                int buttonY = event.getScreen().height / 4 + 48 + 72 - 24;
                try {
                    if (StringUtils.isNotBlank(Config.btnX) && StringUtils.isNotBlank(Config.btnY)) {
                        buttonX = Integer.parseInt(Config.btnX);
                        buttonY = Integer.parseInt(Config.btnY);
                    }
                } catch (Throwable t) {
                    buttonX = event.getScreen().width / 2 + 104;
                    buttonY = event.getScreen().height / 4 + 48 + 72 - 24;
                }
                event.addListener(new GuiButtonWithImage(buttonX, buttonY, btn -> event.getScreen().getMinecraft().setScreen(new GuiAccountSelector(event.getScreen()))));
            }
        }
    }

    @SubscribeEvent
    public void onScreenRender(ScreenEvent.DrawScreenEvent event) {
        if (event.getScreen() instanceof JoinMultiplayerScreen) {
            if (event.getScreen().getMinecraft().getUser().getAccessToken().equals("0") || event.getScreen().getMinecraft().getUser().getAccessToken().equals("-")) {
                List<FormattedCharSequence> list = event.getScreen().getMinecraft().font.split(Component.translatable("ias.offlinemode"), event.getScreen().width);
                for (int i = 0; i < list.size(); i++) {
                    event.getScreen().getMinecraft().font.draw(event.getPoseStack(), list.get(i), event.getScreen().width / 2 - event.getScreen().getMinecraft().font.width(list.get(i)) / 2, i * 9 + 1, 16737380);
                }
            }
        }
        if (event.getScreen() instanceof TitleScreen) {
            Screen.drawCenteredString(event.getPoseStack(), event.getScreen().getMinecraft().font, I18n.get("ias.loggedinas", event.getScreen().getMinecraft().getUser().getName()), textX, textY, 0xFFCC8888);
        }
    }
}
