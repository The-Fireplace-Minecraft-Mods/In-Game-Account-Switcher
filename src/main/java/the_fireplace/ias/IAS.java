package the_fireplace.ias;

import com.github.mrebhan.ingameaccountswitcher.MR;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import the_fireplace.ias.config.ConfigValues;
import the_fireplace.ias.events.FMLEvents;
import the_fireplace.ias.events.ForgeEvents;
import the_fireplace.ias.tools.Reference;
import the_fireplace.ias.tools.SkinTools;

import java.io.IOException;
import java.nio.file.Files;
/**
 * @author The_Fireplace
 */
@Mod(modid=Reference.MODID, name=Reference.MODNAME, version=Reference.VERSION, clientSideOnly=true, guiFactory="the_fireplace.ias.config.IASGuiFactory")
public class IAS {
	@Instance(value=Reference.MODID)
	public static IAS instance;

	public static Configuration config;
	private static Property CASESENSITIVE_PROPERTY;

	public static void syncConfig(){
		ConfigValues.CASESENSITIVE = CASESENSITIVE_PROPERTY.getBoolean();
		if(config.hasChanged())
			config.save();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		CASESENSITIVE_PROPERTY = config.get(Configuration.CATEGORY_GENERAL, ConfigValues.CASESENSITIVE_NAME, ConfigValues.CASESENSITIVE_DEFAULT, ConfigValues.CASESENSITIVE_NAME+".tooltip");
		syncConfig();
	}
	@EventHandler
	public void init(FMLInitializationEvent event){
		MR.init();
		FMLCommonHandler.instance().bus().register(new FMLEvents());
		MinecraftForge.EVENT_BUS.register(new ForgeEvents());
	}
	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		try {
			Files.createDirectory(SkinTools.cachedir.toPath());
		} catch (IOException e) {}
		SkinTools.cacheSkins();
	}
}
