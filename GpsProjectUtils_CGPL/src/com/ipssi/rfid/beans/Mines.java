package com.ipssi.rfid.beans;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.db.DBSchemaManager;
import com.ipssi.rfid.db.DBSchemaManager.DBObjectCursor;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;
import com.ipssi.rfid.db.Table.Unique;
@Table("mines_details")
public class Mines {
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
	
	@Column("type")
	private int type=Misc.getUndefInt();
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
	@Column("parent_area_code")
	private String parentAreaCode;
	@Column("parent_sub_area_code")
	private String parentSubAreaCode;
	@Column("parent_mines_code")
	private String parentMinesCode;
	@Column("address")
	private String address;
	@Column("tin_number")
	private String tinNumber;
	@Column("central_excise_reg_no")
	private String centralExciseRegNo;
	@Column("central_excise_goods")
	private String centralExciseGoods;
	@Column("assessee")
	private String assessee;
	@Column("cst_no")
	private String cstNo;
	@Column("vat_no")
	private String vatNo;
	@Column("project_name")
	private String projectName;
	@Column("address_range")
	private String addressRange;
	@Column("address_division")
	private String addressDivision;
	@Column("commissionerate")
	private String commissionerate;
	@Column("dmf_rate")
	private double dmfRate = Misc.getUndefDouble();
	@Column("nmet_rate")
	private double nmetRate = Misc.getUndefDouble();
	@Column("excise_duty_rate")
	private double exciseDutyRate = Misc.getUndefDouble();
	@Column("education_cess_rate")
	private double educationCessRate = Misc.getUndefDouble();
	@Column("higher_education_cess_rate")
	private double higherEducationCessRate = Misc.getUndefDouble();
	@Column("area_desc")
	private String areaDesc;
	@Column("updated_on")
    private Date updatedOn;
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
	@Column("unit_code")
	private String unitCode;
	@Column("tare_freq_int")
	private double tareFreqInt = Misc.getUndefDouble();
	@Column("closing_step")
	private int closingStep = Misc.getUndefInt();
	@Column("mines_address")
	private String minesAddress;
	
	@Column("fin_year")
	private int finYear=Misc.getUndefInt();
	
	
	private String printingName;
	
	private String printingAdd1;
	
	private String printingAdd2;
	
	public Date getUpdatedOn() {
		return updatedOn;
	}
	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	
	public int getUpdatedBy() {
		return updatedBy;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
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
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
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
	public String getParentAreaCode() {
		return parentAreaCode;
	}
	public void setParentAreaCode(String parentAreaCode) {
		this.parentAreaCode = parentAreaCode;
	}
	public String getParentSubAreaCode() {
		return parentSubAreaCode;
	}
	public void setParentSubAreaCode(String parentSubAreaCode) {
		this.parentSubAreaCode = parentSubAreaCode;
	}
	public String getParentMinesCode() {
		return parentMinesCode;
	}
	public void setParentMinesCode(String parentMinesCode) {
		this.parentMinesCode = parentMinesCode;
	}
	public Mines(){
	}
	public Mines(String code){
		this.code = code;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getTinNumber() {
		return tinNumber;
	}
	public void setTinNumber(String tinNumber) {
		this.tinNumber = tinNumber;
	}
	public String getCentralExciseRegNo() {
		return centralExciseRegNo;
	}
	public void setCentralExciseRegNo(String centralExciseRegNo) {
		this.centralExciseRegNo = centralExciseRegNo;
	}
	public String getAssessee() {
		return assessee;
	}
	public void setAssessee(String assessee) {
		this.assessee = assessee;
	}
	public String getCstNo() {
		return cstNo;
	}
	public void setCstNo(String cstNo) {
		this.cstNo = cstNo;
	}
	public String getVatNo() {
		return vatNo;
	}
	public void setVatNo(String vatNo) {
		this.vatNo = vatNo;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public String getAddressRange() {
		return addressRange;
	}
	public void setAddressRange(String addressRange) {
		this.addressRange = addressRange;
	}
	public String getAddressDivision() {
		return addressDivision;
	}
	public void setAddressDivision(String addressDivision) {
		this.addressDivision = addressDivision;
	}
	public String getCommissionerate() {
		return commissionerate;
	}
	public void setCommissionerate(String commissionerate) {
		this.commissionerate = commissionerate;
	}
	public double getDmfRate() {
		return dmfRate;
	}
	public void setDmfRate(double dmfRate) {
		this.dmfRate = dmfRate;
	}
	public double getNmetRate() {
		return nmetRate;
	}
	public void setNmetRate(double nmetRate) {
		this.nmetRate = nmetRate;
	}
	public double getExciseDutyRate() {
		return exciseDutyRate;
	}
	public void setExciseDutyRate(double exciseDutyRate) {
		this.exciseDutyRate = exciseDutyRate;
	}
	public double getEducationCessRate() {
		return educationCessRate;
	}
	public void setEducationCessRate(double educationCessRate) {
		this.educationCessRate = educationCessRate;
	}
	public double getHigherEducationCessRate() {
		return higherEducationCessRate;
	}
	public void setHigherEducationCessRate(double higherEducationCessRate) {
		this.higherEducationCessRate = higherEducationCessRate;
	}
	public String getCentralExciseGoods() {
		return centralExciseGoods;
	}
	public void setCentralExciseGoods(String centralExciseGoods) {
		this.centralExciseGoods = centralExciseGoods;
	}
	public String getAreaDesc() {
		return areaDesc;
	}
	public void setAreaDesc(String areaDesc) {
		this.areaDesc = areaDesc;
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
	public String getUnitCode() {
		return unitCode;
	}
	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}	
	
	public static Mines getMines(Connection conn, String code, int id) throws Exception{
		if((code == null || code.length() == 0) && Misc.isUndef(id))
			return null;
		DBObjectCursor mines = null;
		try{
			StringBuilder query = new StringBuilder();
			query.append("select m.id, m.name, m.status, m.created_on, m.updated_on, m.updated_by,m.port_node_id,m.created_by,m.supplier_id,m.sap_code,m.comments,m.parent_area_code,m.parent_sub_area_code,m.parent_mines_code,"
					+ "	(case when m.address is not null then m.address when pa.address is not null then pa.address when psa.address is not null then psa.address else pa.address end) address ,"
					+ " (case when m.tin_number is not null then m.tin_number when pa.tin_number is not null then pa.tin_number when psa.tin_number is not null then psa.tin_number else pa.tin_number end) tin_number,"
					+ " (case when m.central_excise_reg_no is not null then m.central_excise_reg_no when pa.central_excise_reg_no is not null then pa.central_excise_reg_no when psa.central_excise_reg_no is not null then psa.central_excise_reg_no else pa.central_excise_reg_no end) central_excise_reg_no,"
					+ " (case when m.assessee is not null then m.assessee when pa.assessee is not null then pa.assessee when psa.assessee is not null then psa.assessee else pa.assessee end) assessee,"
					+ " (case when m.cst_no is not null then m.cst_no when pa.cst_no is not null then pa.cst_no when psa.cst_no is not null then psa.cst_no else pa.cst_no end) cst_no,"
					+ " (case when m.vat_no is not null then m.vat_no when pa.vat_no is not null then pa.vat_no when psa.vat_no is not null then psa.vat_no else pa.vat_no end) vat_no,"
					+ " (case when m.project_name is not null then m.project_name when pa.project_name is not null then pa.project_name when psa.project_name is not null then psa.project_name else pa.project_name end) project_name,"
					+ " (case when m.address_range is not null then m.address_range when pa.address_range is not null then pa.address_range when psa.address_range is not null then psa.address_range else pa.address_range end) address_range,"
					+ " (case when m.address_division is not null then m.address_division when pa.address_division is not null then pa.address_division when psa.address_division is not null then psa.address_division else pa.address_division end) address_division,"
					+ " (case when m.commissionerate is not null then m.commissionerate when pa.commissionerate is not null then pa.commissionerate when psa.commissionerate is not null then psa.commissionerate else pa.commissionerate end) commissionerate,"
					+ " (case when m.central_excise_goods is not null then m.central_excise_goods when pa.central_excise_goods is not null then pa.central_excise_goods when psa.central_excise_goods is not null then psa.central_excise_goods else pa.central_excise_goods end) central_excise_goods,"
					+ " (case when m.dmf_rate is not null then m.dmf_rate when pa.dmf_rate is not null then pa.dmf_rate when psa.dmf_rate is not null then psa.dmf_rate else pa.dmf_rate end) dmf_rate,"
					+ " (case when m.nmet_rate is not null then m.nmet_rate when pa.nmet_rate is not null then pa.nmet_rate when psa.nmet_rate is not null then psa.nmet_rate else pa.nmet_rate end) nmet_rate,"
					+ " (case when m.excise_duty_rate is not null then m.excise_duty_rate when pa.excise_duty_rate is not null then pa.excise_duty_rate when psa.excise_duty_rate is not null then psa.excise_duty_rate else pa.excise_duty_rate end) excise_duty_rate,"
					+ " (case when m.education_cess_rate is not null then m.education_cess_rate when pa.education_cess_rate is not null then pa.education_cess_rate when psa.education_cess_rate is not null then psa.education_cess_rate else pa.education_cess_rate end) education_cess_rate,"
					+ " (case when m.higher_education_cess_rate is not null then m.higher_education_cess_rate when pa.higher_education_cess_rate is not null then pa.higher_education_cess_rate when psa.higher_education_cess_rate is not null then psa.higher_education_cess_rate else pa.higher_education_cess_rate end) higher_education_cess_rate,"
					+ " m.min_retry_hours,m.temp_remote_id,m.record_src,m.tare_freq_int,m.closing_step,"
					+ " (case when m.area_desc is not null then m.area_desc when pa.area_desc is not null then pa.area_desc when psa.area_desc is not null then psa.area_desc else pa.area_desc end) area_desc,"
					+ " (case when m.gst_no is not null then m.gst_no when pa.gst_no is not null then pa.gst_no when psa.gst_no is not null then psa.gst_no else pa.gst_no end) gst_no,"
					+ " (case when m.str_field1 is not null then m.str_field1 when pa.str_field1 is not null then pa.str_field1 when psa.str_field1 is not null then psa.str_field1 else pa.str_field1 end) str_field1,"
					+ " (case when m.str_field2 is not null then m.str_field2 when pa.str_field2 is not null then pa.str_field2 when psa.str_field2 is not null then psa.str_field2 else pa.str_field2 end) str_field2,"
					+ " (case when m.state is not null then m.state when pa.state is not null then pa.state when psa.state is not null then psa.state else pa.state end) state,"
					+ " (case when m.state_gst_code is not null then m.state_gst_code when pa.state_gst_code is not null then pa.state_gst_code when psa.state_gst_code is not null then psa.state_gst_code else pa.state_gst_code end) state_gst_code,"
					+ " (case when m.mines_address is not null then m.mines_address when pa.mines_address is not null then pa.mines_address when psa.mines_address is not null then psa.mines_address else pa.mines_address end) mines_address,"
					+ " m.unit_code "
					+ " from mines_details m left outer join mines_details pm on (m.parent_mines_code=pm.sn) "
					+ " left outer join mines_details psa on (m.parent_sub_area_code=psa.sn) "
					+ " left outer join mines_details pa on (m.parent_area_code=pa.sn) where (? is null or m.id=? ) and m.sn=?");
			ArrayList<Pair<Object,Integer>> params = new ArrayList<Pair<Object,Integer>>();
			params.add(new Pair<Object, Integer>(Misc.isUndef(id) ? null : id, java.sql.Types.INTEGER));
			params.add(new Pair<Object, Integer>(Misc.isUndef(id) ? null : id, java.sql.Types.INTEGER));
			params.add(new Pair<Object, Integer>(code, java.sql.Types.VARCHAR));
			mines = DBSchemaManager.fetch(conn, Mines.class, "",query,params);
			while (mines.next()) {
				return mines.read();
			}
		}catch(Exception ex){
			throw ex;
		}finally{
			if(mines != null)
				mines.close();
		}
		return null;
	}
	public double getTareFreqInt() {
		return tareFreqInt;
	}
	public void setTareFreqInt(double tareFreqInt) {
		this.tareFreqInt = tareFreqInt;
	}
	public int getClosingStep() {
		return closingStep;
	}
	public void setClosingStep(int closingStep) {
		this.closingStep = closingStep;
	}
	public String getMinesAddress() {
		return minesAddress;
	}
	public void setMinesAddress(String minesAddress) {
		this.minesAddress = minesAddress;
	}
	public String getPrintingName() {
		return printingName;
	}
	public void setPrintingName(String printingName) {
		this.printingName = printingName;
	}
	public String getPrintingAdd1() {
		return printingAdd1;
	}
	public void setPrintingAdd1(String printingAdd1) {
		this.printingAdd1 = printingAdd1;
	}
	public String getPrintingAdd2() {
		return printingAdd2;
	}
	public void setPrintingAdd2(String printingAdd2) {
		this.printingAdd2 = printingAdd2;
	}
	public int getFinYear() {
		return finYear;
	}
	public void setFinYear(int finYear) {
		this.finYear = finYear;
	}
	
}
