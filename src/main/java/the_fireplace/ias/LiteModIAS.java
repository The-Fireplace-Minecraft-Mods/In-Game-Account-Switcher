package the_fireplace.ias;

import com.github.mrebhan.ingameaccountswitcher.MR;
import com.github.mrebhan.ingameaccountswitcher.tools.Config;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mumfrey.liteloader.Configurable;
import com.mumfrey.liteloader.ShutdownListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.modconfig.ConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import the_fireplace.ias.gui.GuiPanelConfig;
import the_fireplace.ias.tools.Reference;
import the_fireplace.ias.tools.SkinTools;
import the_fireplace.iasencrypt.Standards;

import java.io.File;

/**
 * @author The_Fireplace
 */
@ExposableOptions(strategy = ConfigStrategy.Unversioned, filename="ias.json")
public class LiteModIAS implements Tickable, ShutdownListener, Configurable {
	public static LiteModIAS instance;
	@Expose
	@SerializedName("case_sensitive")
	public boolean CASESENSITIVE = false;
	@Expose
	@SerializedName("enable_relog")
	public boolean ENABLERELOG = true;

	public LiteModIAS(){
		instance = this;
	}

	@Override
	public void onTick(Minecraft mc, float partialTicks, boolean inGame, boolean clock) {
		if(!inGame){
			GuiScreen screen = mc.currentScreen;
			if (screen instanceof GuiMainMenu) {
				screen.drawCenteredString(mc.fontRendererObj, I18n.format("ias.loggedinas") + mc.getSession().getUsername()+".", screen.width / 2, screen.height / 4 + 48 + 72 + 12 + 22, 0xFFCC8888);
			}else if(screen instanceof GuiMultiplayer){
				if (mc.getSession().getToken().equals("0")) {
					screen.drawCenteredString(mc.fontRendererObj, I18n.format("ias.offlinemode"), screen.width / 2, 10, 16737380);
				}
			}
		}
	}

	@Override
	public String getVersion() {
		return null;
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

	@Override
	public void onShutDown() {
		Config.save();
	}

	@Override
	public Class<? extends ConfigPanel> getConfigPanelClass() {
		return GuiPanelConfig.class;
	}
}
