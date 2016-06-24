package the_fireplace.ias;

import com.github.mrebhan.ingameaccountswitcher.MR;
import the_fireplace.ias.events.ClientEvents;
import the_fireplace.ias.tools.SkinTools;
import the_fireplace.iasencrypt.Standards;
import xyz.openmodloader.modloader.IMod;

/**
 * @author The_Fireplace
 */
public class IAS implements IMod {

	@Override
	public void onEnable(){
		Standards.updateFolder();

		MR.init();
		ClientEvents.init();
		Standards.importAccounts();

		if(!SkinTools.cachedir.exists())
			if(!SkinTools.cachedir.mkdirs())
				System.out.println("Skin cache directory creation failed.");
		SkinTools.cacheSkins();
	}
}
