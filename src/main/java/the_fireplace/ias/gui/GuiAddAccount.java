package the_fireplace.ias.gui;

import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltDatabase;
import the_fireplace.ias.account.ExtendedAccountData;

/**
 * The GUI where the alt is added
 * @author The_Fireplace
 */
public class GuiAddAccount extends AbstractAccountGui {

	public GuiAddAccount()
	{
		super("ias.addaccount");
	}

	@Override
	public void complete()
	{
		AltDatabase.getInstance().getAlts().add(new ExtendedAccountData(getUsername(), getPassword(), getUsername()));
	}
}
