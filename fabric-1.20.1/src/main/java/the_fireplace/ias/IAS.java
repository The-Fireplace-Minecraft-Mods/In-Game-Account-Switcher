package the_fireplace.ias;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.Expression;
import ru.vidtu.ias.IASModMenuCompat;
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
public class IAS implements ClientModInitializer {
    public static final ResourceLocation IAS_BUTTON = new ResourceLocation("ias", "textures/gui/iasbutton.png");
    public static final Map<UUID, ResourceLocation> SKIN_CACHE = new HashMap<>();
    public static boolean modMenu;
    public void onInitializeClient() {
        modMenu = FabricLoader.getInstance().isModLoaded("modmenu");
        ClientLifecycleEvents.CLIENT_STARTED.register(mc -> {
            Config.load(mc.gameDirectory.toPath());
        });
        ScreenEvents.AFTER_INIT.register((mc, screen, w, h) -> {
            Button temp;
            if (screen instanceof JoinMultiplayerScreen && Config.multiplayerScreenButton) {
                int bx = w / 2 + 4 + 76 + 79;
                int by = h - 28;
                try {
                    bx = (int) Expression.parseWidthHeight(Config.titleScreenButtonX, screen.width, screen.height);
                    by = (int) Expression.parseWidthHeight(Config.titleScreenButtonY, screen.width, screen.height);
                } catch (Throwable t) {
                    bx = w / 2 + 4 + 76 + 79;
                    by = h - 28;
                }
                temp = new ImageButton(bx, by, 20, 20, 0, 0, 20, IAS_BUTTON,
                        256, 256, btn -> mc.setScreen(new AccountListScreen(screen)),
                        Component.literal("In-Game Account Switcher"));
                temp.setTooltip(Tooltip.create(Component.literal("In-Game Account Switcher")));
                Screens.getButtons(screen).add(temp);
            }
            if (screen instanceof TitleScreen) {
                if (Config.titleScreenButton) {
                    int bx = w / 2 + 104;
                    int by = h / 4 + 48 + 72 + (IAS.modMenu ? IASModMenuCompat.buttonOffset() : -24);
                    try {
                        bx = (int) Expression.parseWidthHeight(Config.titleScreenButtonX, screen.width, screen.height);
                        by = (int) Expression.parseWidthHeight(Config.titleScreenButtonY, screen.width, screen.height);
                    } catch (Throwable t) {
                        bx = w / 2 + 104;
                        by = h / 4 + 48 + 72 + (IAS.modMenu ? IASModMenuCompat.buttonOffset() : -24);
                    }
                    temp = new ImageButton(bx, by, 20, 20, 0, 0, 20, IAS_BUTTON,
                            256, 256, btn -> mc.setScreen(new AccountListScreen(screen)),
                            Component.literal("In-Game Account Switcher"));
                    temp.setTooltip(Tooltip.create(Component.literal("In-Game Account Switcher")));
                    Screens.getButtons(screen).add(temp);
                }
                if (Config.titleScreenText) {
                    try {
                        int tx = (int) Expression.parseWidthHeight(Config.titleScreenTextX, screen.width, screen.height);
                        int ty = (int) Expression.parseWidthHeight(Config.titleScreenTextY, screen.width, screen.height);
                        ScreenEvents.afterRender(screen).register((s, ms, mx, my, delta) -> {
                            if (Config.titleScreenTextAlignment == Config.Alignment.LEFT) {
                                ms.drawString(mc.font, Component.translatable("ias.title", mc.getUser().getName()), tx, ty, 0xFFCC8888);
                                return;
                            }
                            if (Config.titleScreenTextAlignment == Config.Alignment.RIGHT) {
                                ms.drawString(mc.font, Component.translatable("ias.title", mc.getUser().getName()), tx - mc.font.width(Component.translatable("ias.title", mc.getUser().getName())), ty, 0xFFCC8888);
                                return;
                            }
                            ms.drawCenteredString(mc.font, Component.translatable("ias.title", mc.getUser().getName()), tx, ty, 0xFFCC8888);
                        });
                    } catch (Throwable t) {
                        int tx = w / 2;
                        int ty = h / 4 + 48 + 72 + 12 + (IAS.modMenu ? 32 : 22);
                        ScreenEvents.afterRender(screen).register((s, ms, mx, my, delta) -> {
                            ms.drawCenteredString(mc.font, Component.translatable("ias.title", mc.getUser().getName()), tx, ty, 0xFFCC8888);
                        });
                    }
                }
            }
        });
    }
}
