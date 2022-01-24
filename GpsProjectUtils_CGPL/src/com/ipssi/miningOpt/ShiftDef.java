package com.ipssi.miningOpt;

import java.sql.ResultSet;

import com.ipssi.gen.utils.Misc;

public class ShiftDef {
	private int id;
	private String name;
	private int startHr;
	private int startMin;
	private int endHr;
	private int endMin;
	public ShiftDef(int id, String name, int startHr, int startMin, int endHr,
			int endMin) {
		super();
		this.id = id;
		this.name = name;
		this.startHr = startHr;
		this.startMin = startMin;
		this.endHr = endHr;
		this.endMin = endMin;
	}
	public static ShiftDef read(ResultSet rs) throws Exception {
		return new ShiftDef(rs.getInt(1), rs.getString(2), Misc.getRsetInt(rs, 3), Misc.getRsetInt(rs, 4), Misc.getRsetInt(rs, 5), Misc.getRsetInt(rs, 6));
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getStartHr() {
		return startHr;
	}
	public void setStartHr(int startHr) {
		this.startHr = startHr;
	}
	public int getStartMin() {
		return startMin;
	}
	public void setStartMin(int startMin) {
		this.startMin = startMin;
	}
	public int getEndHr() {
		return endHr;
	}
	public void setEndHr(int endHr) {
		this.endHr = endHr;
	}
	public int getEndMin() {
		return endMin;
	}
	public void setEndMin(int endMin) {
		this.endMin = endMin;
	}
	
}
