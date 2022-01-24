package com.ipssi.simulHelper;

import java.text.SimpleDateFormat;
import java.util.LinkedList;

import com.ipssi.gen.utils.Misc;

public class SimInstruction {
	public static final int TYPE_SHOVEL = 0;
	public static final int TYPE_UOP = 1;
	public static final int TYPE_REST = 2;
	public static final int TYPE_INDETERMINATE = 3;
	public static final int APPLY_AFT_LU_COMPLETE = 0;
	public static final int APPLY_NOW = 1;
	private int forDumperId = -1;
	private long instrTime = -1;
	private int applyMode = APPLY_AFT_LU_COMPLETE;
	private int toId = Misc.getUndefInt();
	private int toType = TYPE_UOP;
	public boolean isManual = false;
	private int returnId = Misc.getUndefInt();
	public String toString() {
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
		sb.append("At:").append(sdf.format(Misc.longToUtilDate(instrTime)))
		.append(",").append(applyMode == APPLY_AFT_LU_COMPLETE ?"LU":"Now")
		.append(",To:").append(toType == TYPE_SHOVEL ? "S" : toType == TYPE_UOP ?"U":"R").append(",").append(toId).append(" Ret:").append(returnId)
		;
		return sb.toString();
	}
	public int getReturnId() {
		return returnId;
	}
	public long getInstrTime() {
		return instrTime;
	}
	public void setInstrTime(long instrTime) {
		this.instrTime = instrTime;
	}
	public int getApplyMode() {
		return applyMode;
	}
	public void setApplyMode(int applyMode) {
		this.applyMode = applyMode;
	}
	public int getToId() {
		return toId;
	}
	public void setToId(int toId) {
		this.toId = toId;
	}
	public int getToType() {
		return toType;
	}
	public void setToType(int toType) {
		this.toType = toType;
	}
	public SimInstruction(int forDumperId, long instrTime, int applyMode, int toId, int toType, int returnId) {
		super();
		this.forDumperId = forDumperId;
		this.instrTime = instrTime;
		this.applyMode = applyMode;
		this.toId = toId;
		this.toType = toType;
		this.returnId = returnId;
	}
	public int getForDumperId() {
		return forDumperId;
	}
	public void setForDumperId(int forDumperId) {
		this.forDumperId = forDumperId;
	}
	
}
