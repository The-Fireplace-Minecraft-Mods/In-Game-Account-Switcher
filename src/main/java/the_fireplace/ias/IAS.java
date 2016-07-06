package the_fireplace.ias;

import com.github.mrebhan.ingameaccountswitcher.MR;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;
import net.minecraft.client.Minecraft;
import the_fireplace.ias.events.ClientEvents;
import the_fireplace.ias.tools.Reference;
import the_fireplace.ias.tools.SkinTools;
import the_fireplace.iasencrypt.Standards;

import java.io.File;

/**
 * @author The_Fireplace
 */
@ExposableOptions(strategy = ConfigStrategy.Unversioned, filename="ias.json")
public class IAS implements Tickable {
	public static IAS instance;
	@Expose
	@SerializedName("case_sensitive")
	public boolean CASESENSITIVE = false;
	@Expose
	@SerializedName("enable_relog")
	public boolean ENABLERELOG = true;

	public IAS(){
		instance = this;
	}

	@Override
	public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
		if(!inGame)
			ClientEvents.onTick(minecraft);
	}

	@Override
	public String getVersion() {
		return Reference.VERSION;
	}

	@Override
	public void init(File configPath) {
		//PreInit
		Standards.updateFolder();
		//Init
		MR.init();
		Standards.importAccounts();
		//PostInit
		if(!SkinTools.cachedir.exists())
			if(!SkinTools.cachedir.mkdirs())
				System.out.println("Skin cache directory creation failed.");
		SkinTools.cacheSkins();
	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {

	}

	@Override
	public String getName() {
		return Reference.MODNAME;
	}
}
