package the_fireplace.ias;

import com.google.gson.Gson;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.utils.SkinRenderer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author The_Fireplace
 */
public class IAS implements ClientModInitializer {
    public static final Logger LOG = LogManager.getLogger("IAS");
    public static final boolean MOD_MENU = FabricLoader.getInstance().isModLoaded("modmenu");
    public static final Gson GSON = new Gson();
    public static final Executor EXECUTOR = Executors.newSingleThreadExecutor(r -> new Thread(r, "IAS Thread"));

    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(mc -> {
            Config.load();
            SkinRenderer.loadAllAsync(mc, false, () -> {});
        });
    }
}
