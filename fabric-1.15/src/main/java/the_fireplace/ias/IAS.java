package the_fireplace.ias;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import ru.vidtu.ias.Config;

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
    }
}
