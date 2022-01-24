package com.ipssi.reporting.trip;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;

public class TR {
	private String id = null;
	private ArrayList<TD> rowData = new ArrayList<TD>();
	private int classId = Misc.getUndefInt();
	private int isSpecialFlag = 0;
	private static int IS_GROUP_PIVOT_CHANGE_MASK = 0x1;
	public String toString() {
		return this.rowData.toString();
	}
	public boolean isPivotChange() {
		return (isSpecialFlag & IS_GROUP_PIVOT_CHANGE_MASK) != 0;
	}
	public void setPivotChange(boolean val) {
		if (val) {
			this.isSpecialFlag = this.isSpecialFlag | IS_GROUP_PIVOT_CHANGE_MASK;
		}
		else {
			this.isSpecialFlag = this.isSpecialFlag & ~IS_GROUP_PIVOT_CHANGE_MASK;
		}
	}
	public void setRowData(int idx, TD td) {
		if (rowData == null)
			rowData = new ArrayList<TD>();
		if (idx >= rowData.size()) {
			rowData.add(td);
		}
		else {
			rowData.add(idx, td);
		}
	}
	public void setButNotAddRowData(int idx, TD td) {
		if (rowData == null)
			rowData = new ArrayList<TD>();
		if (idx < rowData.size()) {
			rowData.set(idx, td);
		}
	}
	
	public void remove(int idx) {
		if (rowData != null && idx >= 0 && idx < rowData.size())
			rowData.remove(idx);
	}
	public void setRowData(TD td)
	{
		if (rowData == null)
			rowData = new ArrayList<TD>();
		rowData.add(td);
	}
	public ArrayList<TD> getRowData()
	{
		return this.rowData;
	}
	public TD get(int index)
	{
		return index < 0 || index >= rowData.size() ? null : this.rowData.get(index);
	}
	public void setClassId(int classId){
		this.classId = classId;
	}
	public int getClassId(){
		return this.classId;
	}
	public void setId(String id){
		this.id = id; 
	}
	public String getId(){
		return this.id;
	}

}
