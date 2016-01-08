package com.github.mrebhan.ingameaccountswitcher.tools.queuing;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple call queue.
 * @author mr
 */

public class CallQueue {
	private static final List queueList = new ArrayList<QueueElement>();

	public static void onTick() {
		for (Object aQueueList : queueList) {
			QueueElement element = (QueueElement) aQueueList;
			element.onTick();
		}

		handleFinishedTasks();
	}

	public static void addToQueue(QueueElement element) {
		if (element instanceof QueueElement) {
			queueList.add(element);
		}
	}

	private static void handleFinishedTasks() {
		for (int i = 0; i < queueList.size(); i++) {
			QueueElement element = (QueueElement) queueList.get(i);
			if (element.getCounter() < 0) {
				element.onCall();
				queueList.remove(i);
			}
		}
	}
}
