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
@Table("customer_details")
public class CustomerDetails {
	public static enum TYPE{
		MINES,
		SIDING,
		WASHERY,
		SUB_AREA,
		AREA
	}
	
	@KEY
	@GENRATED
	@PRIMARY_KEY
	@Column("id")
	private int id = Misc.getUndefInt();
	@Column("name")
	private String name;
	@Column("sap_code")
	private String sapCode;
	@Unique
	@Column("sn")
	private String code;
	@Column("port_node_id")
	private int portNodeId=Misc.getUndefInt();
	@Column("status")
	private int status=Misc.getUndefInt();
	@Column("created_by")
	private int createdBy=Misc.getUndefInt();
	@Column("created_on")
	private Date createdOn;
	@Column("updated_by")
	private int updatedBy=Misc.getUndefInt();
	@Column("address")
	private String address;
	@Column("gst_no")
	private String gstNo;
	
	@Column("str_field1")
	private String strField1;
	@Column("str_field2")
	private String strField2;
	@Column("state")
	private String state;
	@Column("state_gst_code")
	private String stateGstCode;
	
	public CustomerDetails(){
	}
	public CustomerDetails(String code){
		this.code = code;
	}
	public static CustomerDetails getCustomer(Connection conn,String code,int id) throws Exception{
		if((code == null || code.length() == 0) && Misc.isUndef(id))
			return null;
		CustomerDetails retval = null;
		if(!Misc.isUndef(id)){
			retval =  (CustomerDetails) RFIDMasterDao.get(conn, CustomerDetails.class, id);
		}else{
			ArrayList<CustomerDetails> list = (ArrayList<CustomerDetails>) RFIDMasterDao.getList(conn, new CustomerDetails(code), new Criteria(CustomerDetails.class, " status=1", null, false, 1));
			if(list != null && list.size() > 0)
				retval =  list.get(0);
		}
		return retval;
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
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
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
	public int getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getGstNo() {
		return gstNo;
	}
	public void setGstNo(String gstNo) {
		this.gstNo = gstNo;
	}
	public String getStrField1() {
		return strField1;
	}
	public void setStrField1(String strField1) {
		this.strField1 = strField1;
	}
	public String getStrField2() {
		return strField2;
	}
	public void setStrField2(String strField2) {
		this.strField2 = strField2;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getStateGstCode() {
		return stateGstCode;
	}
	public void setStateGstCode(String stateGstCode) {
		this.stateGstCode = stateGstCode;
	}
	
}
