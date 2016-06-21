package the_fireplace.ias.account;

import net.minecraft.util.text.translation.I18n;

public class AlreadyLoggedInException extends Exception {
	@Override
	public String getLocalizedMessage(){
		return I18n.translateToLocal("ias.alreadyloggedin");
	}
}
