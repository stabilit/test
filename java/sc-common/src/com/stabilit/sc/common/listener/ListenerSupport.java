package com.stabilit.sc.common.listener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class ListenerSupport<T extends EventListener> {

	protected List<T> listenerList;

	public ListenerSupport() {
		this.listenerList = new ArrayList<T>();
	}

	public boolean isEmpty() {
		return this.listenerList.isEmpty();
	}

	public void addListener(T listener) {
		listenerList.add(listener);
	}

	public void removeListener(T listener) {
		listenerList.remove(listener);
	}

}
