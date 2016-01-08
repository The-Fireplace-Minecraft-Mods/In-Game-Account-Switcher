package com.github.mrebhan.ingameaccountswitcher.tools.queuing;

/**
 * The QueueElement class for the {@link CallQueue}.
 * @author mr
 * @see CallQueue
 */

public abstract class QueueElement {
	private int counter;

	public QueueElement(int delay) {
		this.counter = delay;
	}

	public final void onTick() {
		this.counter--;
	}

	public final int getCounter() {
		return this.counter;
	}

	public abstract void onCall();
}
