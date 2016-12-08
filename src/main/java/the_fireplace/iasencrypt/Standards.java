package the_fireplace.iasencrypt;

import com.github.mrebhan.ingameaccountswitcher.tools.Config;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AccountData;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltDatabase;
import net.minecraft.client.Minecraft;
import the_fireplace.ias.account.ExtendedAccountData;

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
		processData(getConfigV3());
		processData(getConfigV2());
		processData(getConfigV1());
	}

	private static boolean hasData(AccountData data){
		for(AccountData edata:AltDatabase.getInstance().getAlts()){
			if(edata.equalsBasic(data)){
				return true;
			}
		}
		return false;
	}

	private static void processData(Config olddata){
		if(olddata != null){
			for(AccountData data:((AltDatabase) olddata.getKey("altaccounts")).getAlts()){
				AccountData data2 = convertData(data);
				if(!hasData(data2))
					AltDatabase.getInstance().getAlts().add(data2);
			}
		}
	}

	private static ExtendedAccountData convertData(AccountData oldData){
		if(oldData instanceof ExtendedAccountData)
			return new ExtendedAccountData(EncryptionTools.decodeOld(oldData.user), EncryptionTools.decodeOld(oldData.pass), oldData.alias, ((ExtendedAccountData) oldData).useCount, ((ExtendedAccountData) oldData).lastused, ((ExtendedAccountData) oldData).premium);
		else
			return new ExtendedAccountData(EncryptionTools.decodeOld(oldData.user), EncryptionTools.decodeOld(oldData.pass), oldData.alias);
	}

	private static Config getConfigV3() {
		File f = new File(IASFOLDER, ".ias");
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

	private static Config getConfigV2() {
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

	private static Config getConfigV1(){
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
