package com.ipssi.rfid.beans;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("work_station_properties")
public class WorkStationProperties {

	@KEY
	@PRIMARY_KEY
    @Column("workstation_id")
    private int workstationId = Misc.getUndefInt();

	@KEY
	@Column("workstation_type")
    private int workstationType = Misc.getUndefInt();
    
	@Column("name")
	private String name;
   
    @Column("value")
	private String value;

	public int getWorkstationId() {
		return workstationId;
	}

	public int getWorkstationType() {
		return workstationType;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public void setWorkstationId(int workstationId) {
		this.workstationId = workstationId;
	}

	public void setWorkstationType(int workstationType) {
		this.workstationType = workstationType;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
