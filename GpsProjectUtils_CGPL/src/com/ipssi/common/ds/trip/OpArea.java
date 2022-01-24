package com.ipssi.common.ds.trip;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;

public class OpArea {
	private static int WBMASK1 = 0x1;
	private static int WBMASK2 = 0x2;
	private static int WBMASK3 = 0x4;
	private static int WAITINSIDE_MASK = 0x8;
	private static int WAITBEFEXIT_MASK = 0x10;
	public int id;
	public int materialId;
	public int priority;
	public int thresholdMilliSec = 0;
	private int opAreaType = 0; //0 => op area, otherwise bit or-ed ... no getter/setter
	private ArrayList<OpArea> overLappingAreaList = new ArrayList<OpArea>();
	public boolean doHaveOverlappingArea = false;
	public long startDate = Misc.getUndefInt();
	public long endDate = Misc.getUndefInt();
	public OpArea(int id, int materialId, int priority, int thresholdMilliSec, int opAreaType) {
		this.id = id;
		this.materialId = materialId;
		this.priority = priority;
		this.thresholdMilliSec = thresholdMilliSec;
		this.opAreaType = opAreaType;
	}
	public OpArea(int id, int materialId, int priority, int thresholdMilliSec, int opAreaType, long start, long end) {
		this.id = id;
		this.materialId = materialId;
		this.priority = priority;
		this.thresholdMilliSec = thresholdMilliSec;
		this.opAreaType = opAreaType;
		this.startDate = start;
		this.endDate = end;
	}
	
	public ArrayList<OpArea> getOverLappingAreaList() {
		return overLappingAreaList;
	}
	public void addOverLappingArea(OpArea opArea) {
		this.doHaveOverlappingArea = true;
		this.overLappingAreaList.add(opArea);
	}
	public static boolean isWaitBefExit(int opAreaType) {
		return (opAreaType & WAITBEFEXIT_MASK) != 0;
	}
	public static boolean isInsideWait(int opAreaType) {
		return (opAreaType & WAITINSIDE_MASK) != 0;
	}
	public static boolean isWB1(int opAreaType) {
		return (opAreaType & WBMASK1) != 0;
	}
	
	public static boolean isWB2(int opAreaType) {
		return (opAreaType & WBMASK2) != 0;
	}
	
	public static boolean isWB3(int opAreaType) {
		return (opAreaType & WBMASK3) != 0;
	}
	
	public static boolean isWB(int opAreaType) {
		return (opAreaType & 0xFF) != 0;
	}
	
	public static boolean isNormal(int opAreaType) {
		return (opAreaType & 0xFF) == 0;
	}

	public boolean isInsideWait() {
		return isInsideWait(opAreaType);
	}
	 
	public boolean isWB1() {
		return isWB1(opAreaType);
	}
	public boolean isWB2() {
		return isWB2(opAreaType);
	}
	
	public boolean isWB3() {
		return isWB3(opAreaType);
	}
	
	public boolean isNormal() {
		return isNormal(opAreaType);
	}
	
	public boolean isWB() {
		return isWB(opAreaType);
	}
	
	public int getOpAreaType() {
		return opAreaType;
	}
	 public boolean isValidForDate(long dt) {
		   return (Misc.isUndef(dt) || (Misc.isUndef(startDate) && Misc.isUndef(endDate)) 
				   || (Misc.isUndef(startDate) && !Misc.isUndef(endDate) && dt < endDate) 
				   || (Misc.isUndef(endDate) && !Misc.isUndef(startDate) && dt > startDate)
				   || (dt > startDate && dt < endDate)
				   );
	   }
	
}
