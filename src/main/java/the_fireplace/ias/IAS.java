package the_fireplace.ias;

import com.github.mrebhan.ingameaccountswitcher.MR;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import the_fireplace.ias.config.ConfigValues;
import the_fireplace.ias.events.ClientEvents;
import the_fireplace.ias.tools.Reference;
import the_fireplace.ias.tools.SkinTools;
import the_fireplace.iasencrypt.Standards;
/**
 * @author The_Fireplace
 */
@Mod(modid=Reference.MODID, name=Reference.MODNAME, clientSideOnly=true, guiFactory="the_fireplace.ias.config.IASGuiFactory", updateJSON = "http://thefireplace.bitnamiapp.com/jsons/ias.json", acceptedMinecraftVersions = "[1.11,)")
public class IAS {
	public static Configuration config;
	private static Property CASESENSITIVE_PROPERTY;
	private static Property ENABLERELOG_PROPERTY;

	public static void syncConfig(){
		ConfigValues.CASESENSITIVE = CASESENSITIVE_PROPERTY.getBoolean();
		ConfigValues.ENABLERELOG = ENABLERELOG_PROPERTY.getBoolean();
		if(config.hasChanged())
			config.save();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		CASESENSITIVE_PROPERTY = config.get(Configuration.CATEGORY_GENERAL, ConfigValues.CASESENSITIVE_NAME, ConfigValues.CASESENSITIVE_DEFAULT, I18n.format(ConfigValues.CASESENSITIVE_NAME+".tooltip"));
		ENABLERELOG_PROPERTY = config.get(Configuration.CATEGORY_GENERAL, ConfigValues.ENABLERELOG_NAME, ConfigValues.ENABLERELOG_DEFAULT, I18n.format(ConfigValues.ENABLERELOG_NAME+".tooltip"));
		syncConfig();
		if(!event.getModMetadata().version.equals("${version}"))//Dev environment needs to use a local list, to avoid issues
			Standards.updateFolder();
		else
			System.out.println("Dev environment detected!");
	}
	@EventHandler
	public void init(FMLInitializationEvent event){
		MR.init();
		MinecraftForge.EVENT_BUS.register(new ClientEvents());
		Standards.importAccounts();
	}
	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		SkinTools.cacheSkins();
	}
}
