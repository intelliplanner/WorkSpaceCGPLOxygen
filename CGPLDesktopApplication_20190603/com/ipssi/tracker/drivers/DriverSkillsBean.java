package com.ipssi.tracker.drivers;
import java.sql.Connection;

import com.ipssi.gen.utils.*;
public class DriverSkillsBean {
	
	private int id;
	private int key;
	private int value;
	private String valueName;
	private int factor1;
	private int factor2;
	public void addToString(Connection conn, SessionManager session, StringBuilder skillText, Cache _cache,DimInfo d9003, DimInfo factor1Dim, DimInfo factor2Dim) throws Exception {
		if (factor1Dim != null || factor2Dim != null) {
			skillText.append("(");
			if (factor1Dim != null && !Misc.isUndef(factor1)) {
				
				skillText.append(_cache.getAttribDisplayNameFull(session, conn, factor1Dim, factor1));
				skillText.append(",");
			}
			if (factor2Dim != null && !Misc.isUndef(factor2)) {
				skillText.append(_cache.getAttribDisplayNameFull(session, conn, factor2Dim, factor2));
			}
			skillText.append(":");
			skillText.append(_cache.getAttribDisplayNameFull(session,conn, d9003, key));
			skillText.append(")");
		}
		else {
			skillText.append(_cache.getAttribDisplayNameFull(session, conn, d9003, key));
		}
	}
	public int getId(){
		return this.id;
	}
	public void setId(int id){
		this.id = id;
	}
	public int getKey(){
		return this.key;
	}
	public void setKey(int key){
		this.key = key;
	}
	public int getValue(){
		return this.value;
	}
	public void setValue(int value){
		this.value = value;
	}
	public String getValueName(){
		return this.valueName;
	}
	public void setValueName(String valueName){
		this.valueName = valueName;
	}
	public void setFactor1(int factor1) {
		this.factor1 = factor1;
	}
	public int getFactor1() {
		return factor1;
	}
	public void setFactor2(int factor2) {
		this.factor2 = factor2;
	}
	public int getFactor2() {
		return factor2;
	}
	
}
