package com.ipssi.rfid.beans;

import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;


@Table("hopper_details")
public class HopperDetails {
	@KEY
    @GENRATED
    @PRIMARY_KEY
    @Column("id")
	int id;

	@Column("name")
	String hopperName;

	@Column("wave_src")
	String waveSrc;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getHopperName() {
		return hopperName;
	}

	public void setHopperName(String hopperName) {
		this.hopperName = hopperName;
	}

	public String getWaveSrc() {
		return waveSrc;
	}

	public void setWaveSrc(String waveSrc) {
		this.waveSrc = waveSrc;
	}
}