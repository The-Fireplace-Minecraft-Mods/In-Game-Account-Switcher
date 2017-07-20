package the_fireplace.ias.account;

import net.minecraft.client.resources.I18n;

public class AlreadyLoggedInException extends Exception {
	private static final long serialVersionUID = -7572892045698003265L;

	@Override
	public String getLocalizedMessage(){
		return I18n.format("ias.alreadyloggedin");
	}
}
