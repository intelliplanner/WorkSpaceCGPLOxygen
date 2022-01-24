package com.ipssi.segAnalysis;

//import gnu.trove.TIntProcedure;

import java.util.ArrayList;
import java.util.List;

//class PrintId implements TIntProcedure {
class PrintId  {
	
	private	List arrayOfIds = new ArrayList() ;
	public boolean execute(int val) {
		arrayOfIds.add(val);
		//System.out.println(val);
		return false;
	}
	public List getArrayOfIds() {
		return arrayOfIds;
	}
	public void cleanArrayOfIds()
	{
		arrayOfIds.clear();
		
	}
    }
