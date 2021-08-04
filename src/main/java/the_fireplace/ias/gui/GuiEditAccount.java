package the_fireplace.ias.gui;

import com.github.mrebhan.ingameaccountswitcher.tools.alt.AccountData;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltDatabase;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;
import the_fireplace.ias.account.ExtendedAccountData;
import the_fireplace.ias.tools.JavaTools;
import the_fireplace.iasencrypt.EncryptionTools;
/**
 * The GUI where the alt is added
 * @author The_Fireplace
 * @author evilmidget38
 */
public class GuiEditAccount extends AbstractAccountGui {
	private final ExtendedAccountData data;
	private final int selectedIndex;

	public GuiEditAccount(Screen prev, int index){
		super(prev, new TranslatableText("ias.editaccount"));
		this.selectedIndex=index;
		AccountData data = AltDatabase.getInstance().getAlts().get(index);

		if(data instanceof ExtendedAccountData){
			this.data = (ExtendedAccountData) data;
		}else{
			this.data = new ExtendedAccountData(data.user, data.pass, data.alias, 0, JavaTools.getDate(), null);
		}
	}
	
	@Override
	protected void init() {
		super.init();
		setUsername(EncryptionTools.decode(data.user));
		setPassword(EncryptionTools.decode(data.pass));
	}

	@Override
	public void complete()
	{
		AltDatabase.getInstance().getAlts().set(selectedIndex, new ExtendedAccountData(getUsername(), getPassword(), hasUserChanged ? getUsername() : data.alias, data.useCount, data.lastused, data.premium));
	}
	
	@Override
	public boolean canComplete() {
		return getUsername().length() > 0;
	}

}
