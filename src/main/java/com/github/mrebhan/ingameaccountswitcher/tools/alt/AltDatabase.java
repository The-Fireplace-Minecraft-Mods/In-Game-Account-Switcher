package com.github.mrebhan.ingameaccountswitcher.tools.alt;

import java.io.Serializable;
import java.util.ArrayList;
/**
 * @author mrebhan
 * @author The_Fireplace
 * @deprecated Inconvenient. Insure (saved user passwords). Used only for conversion from old accounts to new accounts.
 */
@Deprecated
public class AltDatabase implements Serializable {	
	public static final long serialVersionUID = 0xA17DA7AB;
	public ArrayList<AccountData> altList;
}
