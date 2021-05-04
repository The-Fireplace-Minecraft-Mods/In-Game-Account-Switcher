package the_fireplace.ias.gui;

import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltDatabase;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import ru.vidtu.iasfork.msauth.MSAuthScreen;
import the_fireplace.ias.account.ExtendedAccountData;

/**
 * The GUI where the alt is added
 * @author The_Fireplace
 * @author evilmidget38
 */
public class GuiAddAccount extends AbstractAccountGui {

	public GuiAddAccount()
	{
		super("ias.addaccount");
	}
	
	@Override
	public void initGui() {
		super.initGui();
		addButton(new GuiButton(13, width / 2 - 60, height / 3 * 2, 120, 20, I18n.format("ias.msauth.btn")));
	}
	
	@Override
	public void actionPerformed(GuiButton button) {
		if (button.id == 13) {
			mc.displayGuiScreen(new MSAuthScreen(this));
			return;
		}
		super.actionPerformed(button);
	}

	@Override
	public void complete()
	{
		AltDatabase.getInstance().getAlts().add(new ExtendedAccountData(getUsername(), getPassword(), getUsername()));
	}
}
