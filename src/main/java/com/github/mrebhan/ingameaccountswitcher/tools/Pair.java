package com.github.mrebhan.ingameaccountswitcher.tools;

import java.io.Serializable;

import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltDatabase;

/**
 * Simple Pair system with 2 variables.
 * @author MRebhan
 * @author The_Fireplace
 * 
 * @param <V1> First variable (mostly {@link String})
 * @param <V2> Second variable
 * @deprecated Inconvenient. Insure (saved user passwords). Used only for conversion from old accounts to new accounts.
 */
@Deprecated
public class Pair implements Serializable {
	private static final long serialVersionUID = 2586850598481149380L;
	public String obj1;
	public AltDatabase obj2;
}
