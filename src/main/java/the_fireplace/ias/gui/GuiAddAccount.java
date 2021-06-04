package the_fireplace.ias.gui;

import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltDatabase;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.TranslatableText;
import ru.vidtu.iasfork.msauth.MSAuthScreen;
import the_fireplace.ias.account.ExtendedAccountData;

/**
 * The GUI where the alt is added
 * @author The_Fireplace
 * @author evilmidget38
 */
public class GuiAddAccount extends AbstractAccountGui {

	public GuiAddAccount(Screen prev)
	{
		super(prev, "ias.addaccount");
	}
	
	@Override
	public void init() {
		super.init();
		addDrawableChild(new ButtonWidget(width / 2 - 60, height / 3 * 2, 120, 20, new TranslatableText("ias.msauth.btn"), btn -> client.openScreen(new MSAuthScreen(this))));
	}

	@Override
	public void complete()
	{
		AltDatabase.getInstance().getAlts().add(new ExtendedAccountData(getUsername(), getPassword(), getUsername()));
	}
}
