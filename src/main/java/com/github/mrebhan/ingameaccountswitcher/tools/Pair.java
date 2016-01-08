package com.github.mrebhan.ingameaccountswitcher.tools;

import java.io.Serializable;

/**
 * Simple Pair system with 2 variables.
 * @author mr
 * 
 * @param <V1> First variable (mostly {@link String})
 * @param <V2> Second variable
 */

public class Pair<V1, V2> implements Serializable {
	private V1 obj1;
	private V2 obj2;

	public Pair(V1 obj1, V2 obj2) {
		this.obj1 = obj1;
		this.obj2 = obj2;
	}

	public V1 getValue1() {
		return this.obj1;
	}

	public V2 getValue2() {
		return this.obj2;
	}

	public void setValue1(V1 value) {
		this.obj1 = value;
	}

	public void setValue2(V2 value) {
		this.obj2 = value;
	}

	@Override public String toString() { 
		return Pair.class.getName() + "@" + Integer.toHexString(this.hashCode()) + " [" + this.obj1.toString() + ", " + this.obj2.toString() + "]";
	}
}
