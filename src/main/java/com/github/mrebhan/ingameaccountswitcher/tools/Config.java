package com.github.mrebhan.ingameaccountswitcher.tools;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author mrebhan
 * @author The_Fireplace
 * @deprecated Inconvenient. Insure (saved user passwords). Used only for conversion from old accounts to new accounts.
 */
@Deprecated
public class Config implements Serializable {
	public static final long serialVersionUID = 0xDEADBEEF;
	public ArrayList<Pair> field_218893_c;
}
