package com.ipssi.rfid.sync;

public class DatabaseColumn {
	public static final int INTEGER = 0;
	public static final int DOUBLE = 1;
	public static final int STRING = 2;
	public static final int DATE = 3;
	private int type;
	private String colName;
	private Object colVal;
	public int getType() {
		return type;
	}
	public String getColName() {
		return colName;
	}
	public Object getColVal() {
		
		switch(type){
			case STRING : break;
			case DATE : break;
		}
		return colVal;
	}
	public void setType(int type) {
		this.type = type;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}
	public void setColVal(Object colVal) {
		this.colVal = colVal;
	}
}
