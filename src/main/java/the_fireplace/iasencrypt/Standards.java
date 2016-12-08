package the_fireplace.iasencrypt;

import com.github.mrebhan.ingameaccountswitcher.tools.Config;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AccountData;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltDatabase;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 *
 * @author The_Fireplace
 *
 */
public class Standards {
	public static File IASFOLDER = Minecraft.getMinecraft().mcDataDir;
	public static final String cfgn = ".iasx";

	public static void updateFolder(){
		String dir;
		String OS = (System.getProperty("os.name")).toUpperCase();
		if(OS.contains("WIN")){
			dir=System.getenv("AppData");
		}else{
			dir=System.getProperty("user.home");
			if(OS.contains("MAC"))
				dir+="/Library/Application Support";
		}

		Standards.IASFOLDER = new File(dir);
	}

	public static void importAccounts(){
		Config olddata;
		olddata=readFromOldFile();
		if(olddata != null){
			for(AccountData data:((AltDatabase) olddata.getKey("altaccounts")).getAlts()){
				if(!hasData(data))
					AltDatabase.getInstance().getAlts().add(data);
			}
		}
		olddata=getAncientConfig();
		if(olddata != null){
			for(AccountData data:((AltDatabase) olddata.getKey("altaccounts")).getAlts()){
				if(!hasData(data))
					AltDatabase.getInstance().getAlts().add(data);
			}
		}
	}

	private static boolean hasData(AccountData data){
		for(AccountData edata:AltDatabase.getInstance().getAlts()){
			if(edata.equalsBasic(data)){
				return true;
			}
		}
		return false;
	}

	private static Config readFromOldFile() {
		File f = new File(Minecraft.getMinecraft().mcDataDir, ".ias");
		Config cfg = null;
		if (f.exists()) {
			try {
				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
				cfg = (Config) stream.readObject();
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			f.delete();
		}
		return cfg;
	}

	private static Config getAncientConfig(){
		File f = new File(Minecraft.getMinecraft().mcDataDir, "user.cfg");
		Config cfg = null;
		if (f.exists()) {
			try {
				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
				cfg = (Config) stream.readObject();
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			f.delete();
		}
		return cfg;
	}
}
