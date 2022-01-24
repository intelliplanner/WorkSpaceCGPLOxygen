package com.ipssi.rfid.beans;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Criteria;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;
import com.ipssi.rfid.db.Table.Unique;
@Table("coal_product")
public class CoalProductDetails {
	@KEY
    @GENRATED
    @PRIMARY_KEY
    @Column("id")
	private int id = Misc.getUndefInt();
	@Column("status")
	private int status = Misc.getUndefInt();
	@Column("name")
	private String name;
	@Column("sap_code")
	private String sapCode;
	@Unique
	@Column("sn")
	private String code;
	@Column("port_node_id")
	private int portNodeId = Misc.getUndefInt();
	@Column("created_by")
	private int createdBy = Misc.getUndefInt();
	@Column("created_on")
	private Date createdOn;
	@Column("updated_on")
	private Date updatedOn;
	@Column("updated_by")
	private int updatedBy = Misc.getUndefInt();
	@Column("hsn_code")
	private String hsnCode;
	public CoalProductDetails() {
		super();
	}
	public CoalProductDetails(int id, int status, String name, String sapCode, String code, int portNodeId,
			int createdBy, Date createdOn, Date updatedOn, int updatedBy) {
		super();
		this.id = id;
		this.status = status;
		this.name = name;
		this.sapCode = sapCode;
		this.code = code;
		this.portNodeId = portNodeId;
		this.createdBy = createdBy;
		this.createdOn = createdOn;
		this.updatedOn = updatedOn;
		this.updatedBy = updatedBy;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSapCode() {
		return sapCode;
	}
	public void setSapCode(String sapCode) {
		this.sapCode = sapCode;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public int getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public Date getUpdatedOn() {
		return updatedOn;
	}
	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}
	public int getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}
	@Override
	public String toString() {
		return "CoalProductDetails [id=" + id + ", status=" + status + ", name=" + name + ", sapCode=" + sapCode
				+ ", code=" + code + ", portNodeId=" + portNodeId + ", createdBy=" + createdBy + ", createdOn="
				+ createdOn + ", updatedOn=" + updatedOn + ", updatedBy=" + updatedBy + "]";
	}
	public String getHsnCode() {
		return hsnCode;
	}
	public void setHsnCode(String hsnCode) {
		this.hsnCode = hsnCode;
	}
	
	public static CoalProductDetails getCoalProduct(Connection conn,String code, int id) throws Exception{
		CoalProductDetails retval = null;
		if((code == null || code.length() == 0) && Misc.isUndef(id))
			return null;
		if(!Misc.isUndef(id)){
			retval =  (CoalProductDetails) RFIDMasterDao.get(conn, CoalProductDetails.class, id);
		}else{
			ArrayList<CoalProductDetails> list = (ArrayList<CoalProductDetails>) RFIDMasterDao.getList(conn, new CoalProductDetails(code), new Criteria(CoalProductDetails.class, " status=1", null, false, 1));
			if(list != null && list.size() > 0)
				retval =  list.get(0);
		}
		return retval;
	}
	public CoalProductDetails(String code) {
		super();
		this.code = code;
	}
	
}
