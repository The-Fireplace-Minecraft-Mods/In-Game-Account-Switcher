package com.github.mrebhan.ingameaccountswitcher.tools.alt;

import java.io.Serializable;
/**
 * @author mrebhan
 * @author The_Fireplace
 * @deprecated Inconvenient. Insure (saved user passwords). Used only for conversion from old accounts to new accounts.
 */
@Deprecated
public class AccountData implements Serializable {
	public static final long serialVersionUID = 0xF72DEBAC;
	public String user;
	public String pass;
	public String alias;
}
