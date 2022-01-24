package com.ipssi.simulHelper;

import java.sql.Connection;

public class GenerateData {
	long tsTipper = 0;
	long tsShovel = 0;
	long tsBLE = 0;
	long tsLoadEvent = 0;
	public GenerateData generateAndGetTS(Connection conn, long currTS) throws Exception {
		return new GenerateData();
	}
	public long getTsTipper() {
		return tsTipper;
	}
	public void setTsTipper(long tsTipper) {
		this.tsTipper = tsTipper;
	}
	public long getTsShovel() {
		return tsShovel;
	}
	public void setTsShovel(long tsShovel) {
		this.tsShovel = tsShovel;
	}
	public long getTsBLE() {
		return tsBLE;
	}
	public void setTsBLE(long tsBLE) {
		this.tsBLE = tsBLE;
	}
	public long getTsLoadEvent() {
		return tsLoadEvent;
	}
	public void setTsLoadEvent(long tsLoadEvent) {
		this.tsLoadEvent = tsLoadEvent;
	}
}
