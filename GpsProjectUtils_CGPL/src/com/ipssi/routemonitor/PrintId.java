package com.ipssi.routemonitor;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.TIntProcedure;

public class PrintId implements TIntProcedure {
	private	List arrayOfIds = new ArrayList() ;
	public boolean execute(int val) {
		arrayOfIds.add(val);
		//System.out.println(val);
		return false;
	}
	public List getArrayOfIds() {
		return arrayOfIds;
	}
	public void cleanArrayOfIds() {
		arrayOfIds.clear();
	}
}
