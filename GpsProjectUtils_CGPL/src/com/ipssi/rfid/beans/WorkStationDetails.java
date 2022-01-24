package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("work_station_details")
public class WorkStationDetails {

	@KEY
    @GENRATED
    @PRIMARY_KEY
    @Column("id")
    private int id = Misc.getUndefInt();
    
	@Column("name")
	private String name;
    
    @Column("comments")
	private String comments;
    
    @Column("port_node_id")
	private int portNodeId = Misc.getUndefInt();
   
    @Column("updated_on")
	private Date updatedOn; 
	
    @Column("created_on")
	private Date createdOn; 
    
	@Column("updated_by")
	private int updatedBy = Misc.getUndefInt(); 
    
	@Column("created_by")
	private int createdBy = Misc.getUndefInt();

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getComments() {
		return comments;
	}

	public int getPortNodeId() {
		return portNodeId;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public int getUpdatedBy() {
		return updatedBy;
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	} 
}
