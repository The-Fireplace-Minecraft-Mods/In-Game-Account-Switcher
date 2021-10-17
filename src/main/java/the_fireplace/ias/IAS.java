package the_fireplace.ias;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.utils.Converter;
import ru.vidtu.ias.utils.SkinRenderer;
/**
 * @author The_Fireplace
 */
public class IAS implements ClientModInitializer {
	public static final boolean modMenu = FabricLoader.getInstance().isModLoaded("modmenu");
	public static final Gson GSON = new Gson();
	public static final Logger LOG = LogManager.getLogger("IAS");
	public void onInitializeClient() {
		ClientLifecycleEvents.CLIENT_STARTED.register(mc -> {
			Config.load(mc);
			Converter.convert(mc);
			SkinRenderer.loadAllAsync(MinecraftClient.getInstance(), false, () -> {});
		});
	}
}
