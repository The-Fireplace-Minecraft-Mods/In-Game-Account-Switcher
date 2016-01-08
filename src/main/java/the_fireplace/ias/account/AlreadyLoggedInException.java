package the_fireplace.ias.account;

import net.minecraft.util.StatCollector;

public class AlreadyLoggedInException extends Exception {
	@Override
	public String getLocalizedMessage(){
		return StatCollector.translateToLocal("ias.alreadyloggedin");
	}
}
