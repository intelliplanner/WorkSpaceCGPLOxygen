package com.ipssi.reporting.trip;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class FixedSizeQueue extends ArrayBlockingQueue<Map> {
	private static final long serialVersionUID = 1L;
	public int size;

	public FixedSizeQueue(int size) {
		super(size);
		this.size = size;
	}

	@SuppressWarnings("unchecked")
	@Override
	synchronized public boolean add(Map m) {
		if (super.size() == this.size)
			this.remove();
		return super.add(m);
	}

}
