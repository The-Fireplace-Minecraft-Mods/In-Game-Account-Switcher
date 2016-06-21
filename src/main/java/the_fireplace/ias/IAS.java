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

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
/**
 * @author The_Fireplace
 */
@Mod(modid=Reference.MODID, name=Reference.MODNAME, clientSideOnly=true, guiFactory="the_fireplace.ias.config.IASGuiFactory", updateJSON = "http://caterpillar.bitnamiapp.com/jsons/ias.json")
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
		Standards.updateFolder();
	}
	@EventHandler
	public void init(FMLInitializationEvent event){
		MR.init();
		MinecraftForge.EVENT_BUS.register(new ClientEvents());
		Standards.importAccounts();
	}
	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		try {
			Files.createDirectory(SkinTools.cachedir.toPath());
		}catch (FileAlreadyExistsException e){
			System.out.println("Skin cache found.");
		}catch (IOException e) {
			e.printStackTrace();
		}
		SkinTools.cacheSkins();
	}
}
