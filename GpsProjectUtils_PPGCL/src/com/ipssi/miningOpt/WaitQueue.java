package com.ipssi.miningOpt;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;


public class WaitQueue {
	private ArrayList<WaitItem> queue = null;
	private int maxSz = Misc.getUndefInt();
	
	public WaitQueue(int sz) {
		queue = new ArrayList<WaitItem>(sz <= 0 ? 10 : sz);
		this.maxSz = sz;
	}
	public static class Iterator {
		private int idx = 0;
		private ArrayList<WaitItem> theList = null;
		private Iterator(ArrayList<WaitItem> items) {
			theList = items;
			idx = 0;
		}
		public boolean hasNext() {
			return theList != null && theList.size() > idx;
		}
		public WaitItem next() {
			return theList.get(idx++);
		}
	}
	public Iterator iterator() {
		return new Iterator(queue);
	}
	synchronized public WaitItem getByVehicleId(int vehicleId) {
		int idx = indexOf(vehicleId);
		return idx >= 0 ? queue.get(idx) : null;
	}
	synchronized public boolean isAtBegOfQ(int vehicleId) {
		boolean retval = false;
		int idx = indexOf(vehicleId);
		return idx == 0;
	}
	synchronized public  int indexOf(int vehicleId) {
		for (int i=0,is=queue == null ? 0 : queue.size(); i<is; i++) {
			if (queue.get(i).getVehicleId() == vehicleId)
				return i;
		}
		return -1;
	}
	synchronized private  int lastIndexOf(int vehicleId) {
		for (int i=queue == null ? -1 : queue.size()-1; i>=0; i--) {
			if (queue.get(i).getVehicleId() == vehicleId)
				return i;
		}
		return -1;
	}
	synchronized private  int indexOf(WaitItem item) {
		for (int i=0,is=queue == null ? 0 : queue.size(); i<is; i++) {
			if (queue.get(i).equals(item))
				return i;
		}
		return -1;
	}
	synchronized public void clear() {
		queue.clear();
	}
	synchronized public WaitItem remove(int vehicleId) {
		int idx = indexOf(vehicleId);
		if (idx >= 0) {
			WaitItem retval = queue.get(idx);
			queue.remove(idx);
			return retval;
		}
		return null;
	}
	
	synchronized public WaitItem removeLast(int vehicleId) {
		int idx = lastIndexOf(vehicleId);
		if (idx >= 0) {
			WaitItem retval = queue.get(idx);
			queue.remove(idx);
			return retval;
		}
		return null;
	}
	synchronized public WaitItem remove(WaitItem item) {
		int idx = indexOf(item);
		if (idx >= 0) {
			WaitItem retval = queue.get(idx);
			queue.remove(idx);
			return retval;
		}
		return null;
	}
	
	synchronized public void add(WaitItem item) {
		remove(item);
		
		int is = queue.size();
		if (maxSz > 0 && (is == maxSz)) {
			this.queue.remove(0);
			is = maxSz-1;
		}
		int insertBefore = 0;
		for (;insertBefore < is; insertBefore++) {
			WaitItem entry = queue.get(insertBefore);
			int cmp = item.compareTo(entry);
			if (cmp > 0)
				break;
		}
		if (insertBefore == is) 
			queue.add(item);
		else 
			queue.add(insertBefore, item);
	}
	
}
