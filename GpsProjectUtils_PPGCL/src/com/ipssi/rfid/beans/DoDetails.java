package com.ipssi.rfid.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.db.Criteria;
import com.ipssi.rfid.db.DBSchemaManager;
import com.ipssi.rfid.db.DBSchemaManager.DBObjectCursor;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;
import com.ipssi.rfid.processor.Utils;

@Table("mines_do_details")
public class DoDetails {
	
	public static enum DoType{
		WASHERY,
		ROADSALE
	}
	
	public static enum TaxationType{
		EXCISE,
		GST
	}
	public static final double MinTaransactionalQty = 1.0/1000.0;
	@KEY
	@GENRATED
	@PRIMARY_KEY
	@Column("id")
	private int id=Misc.getUndefInt();  
	@Column("do_number")
	private String doNumber;
	@Column("do_date")
	private Date doDate;
	@Column("do_release_no")
	private String doReleaseNo;
	@Column("do_release_date")
	private Date doReleaseDate;
	@Column("validity_date")
	private Date validityDate;
	@Column("type_of_consumer")
	private int typeOfConsumer=Misc.getUndefInt();
	@Column("customer")
	private int customer=Misc.getUndefInt();
	@Column("customer_ref")
	private String customerRef;
	@Column("customer_contact_person")
	private String custustomerContactPerson;
	@Column("grade")
	private int grade = Misc.getUndefInt();
	@Column("coal_size")
	private String coalSize;
	@Column("source_mines")
	private int sourceMines = Misc.getUndefInt();
	@Column("washery")   
	private int washery = Misc.getUndefInt();
	@Column("qty_alloc")
	private double qtyAlloc = Misc.getUndefDouble(); 
	@Column("qty_already_lifted")
	private double qtyAlreadyLifted = Misc.getUndefDouble();
	@Column("quota")
	private int quota = Misc.getUndefInt();
	@Column("rate")
	private double rate = Misc.getUndefDouble();
	@Column("transport_charge")
	private double transportCharge = Misc.getUndefDouble();
	@Column("sizing_charge")
	private double sizingCharge = Misc.getUndefDouble();
	@Column("silo_charge")
	private double siloCharge = Misc.getUndefDouble();
	@Column("dump_charge")
	private double dumpingCharge = Misc.getUndefDouble();
	@Column("stc_charge")
	private double stcCharge = Misc.getUndefDouble();
	@Column("terminal_charge")
	private double terminalCharge = Misc.getUndefDouble();
	@Column("forest_cess")
	private double forestCess = Misc.getUndefDouble();
	@Column("stow_ed")
	private double stowingEd = Misc.getUndefDouble();
	@Column("avap")
	private double avap = Misc.getUndefDouble();
	@Column("allow_no_tare")
	private int allowNoTare = Misc.getUndefInt();
	@Column("max_tare_gap")
	private double maxTareGap = Misc.getUndefDouble();
	@Column("destination")
	private int destination = Misc.getUndefInt();
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
	@Column("updated_on")
	private Date updatedOn;
	
	@Column("customer_code")
	private String customerCode;
	@Column("grade_code")
	private String gradeCode;
	@Column("source_code")
	private String sourceCode;
	@Column("washery_code")
	private String washeryCode;
	
	@Column("destination_code")
	private String destinationCode;
	@Column("prefered_wb_1")
	private String preferedWb1;
	@Column("prefered_wb_2")
	private String preferedWb2;
	@Column("prefered_wb_3")
	private String preferedWb3;
	@Column("prefered_wb_4")
	private String preferedWb4;

	@Column("prefered_wb_1_qty")
	private double preferedWb1Qty=Misc.getUndefDouble();
	@Column("prefered_wb_2_qty")
	private double preferedWb2Qty=Misc.getUndefDouble();
	@Column("prefered_wb_3_qty")
	private double preferedWb3Qty=Misc.getUndefDouble();
	@Column("prefered_wb_4_qty")
	private double preferedWb4Qty=Misc.getUndefDouble();
	
	@Column("delivery_point")
	private String deliveryPoint;
	
	@Column("material")
	private String material;
	
	@Column("royalty_charge")
	private double royaltyCharge = Misc.getUndefDouble();
	
	@Column("transport_mode")
	private int transportMode = Misc.getUndefInt();
	
	
	private double totQtyRemaining = Misc.getUndefDouble();
//	private LatestDOInfo latestDoInfo = null;

	/*public LatestDOInfo getLatestDoInfo() {
		return latestDoInfo;
	}
	public void setLatestDoInfo(LatestDOInfo latestDoInfo) {
		this.latestDoInfo = latestDoInfo;
	}*/
	@Column("lock_status")
	private int lockStatus = Misc.getUndefInt();
	@Column("lock_changed_at")
	private Date lockChangedAt = null;
	private int doDbStatus = Misc.getUndefInt();// server mines_do_details  {1 = succ : 0 = fail}
	@Column("allocation_approval_status")
	private int allocation_approval_status = Misc.getUndefInt(); 
	@Column("do_type")
	private int doType = Misc.getUndefInt();
	@Column("origianl_qty") 
	private double origianlQty = Misc.getUndefDouble();
	@Column("taxation_type") 
	private int taxationType = Misc.getUndefInt();
	@Column("other_charges") 
	private double otherCharges = Misc.getUndefDouble();
	@Column("sgst_rate") 
	private double sgstRate = Misc.getUndefDouble();
	@Column("cgst_rate") 
	private double cgstRate = Misc.getUndefDouble();
	@Column("igst_rate") 
	private double igstRate = Misc.getUndefDouble();
	@Column("state_compensation_cess") 
	private double stateCompensationCess = Misc.getUndefDouble();
	
	@Column("other_charges1_pre_tax_permt") 
	private double otherCharges1PreTaxPermt = Misc.getUndefDouble();
	@Column("other_charges2_pre_tax_permt") 
	private double otherCharges2PreTaxPermt = Misc.getUndefDouble();
	@Column("other_charges1_post_tax_permt") 
	private double otherCharges1PostTaxPermt = Misc.getUndefDouble();
	@Column("other_charges2_post_tax_permt") 
	private double otherCharges2PostTaxPermt = Misc.getUndefDouble();

	@Column("gst_cutover_date") 
	private Date gstCutoverDate;
	@Column("tot_value") 
	private double totValue = Misc.getUndefDouble();
	@Column("tot_value_paid") 
	private double totValuePaid = Misc.getUndefDouble();
	@Column("gst_autoadjustment_done") 
	private int gstAutoadjustmentDone = Misc.getUndefInt();
	@Column("tot_value_gst") 
	private double totValueGst = Misc.getUndefDouble();
	@Column("arv_id") 
	private String arvId;
	@Column("consginee") 
	private String consginee;
	@Column("consginee_address") 
	private String consgineeAddress;
	@Column("consginee_state") 
	private String consgineeState;
	
	@Column("sadak_tax")
	private double sadakTax = Misc.getUndefDouble();
	
	@Column("int_field2")//use as allowed_tare_anywhere[1=allowed else not allowed] 
	private int intField2=Misc.getUndefInt();
	
	@Column("dmf")
	private double dmf = Misc.getUndefDouble();
	
	@Column("nmet")
	private double nmet = Misc.getUndefDouble();
	
	public int getAllocation_approval_status() {
		return allocation_approval_status;
	}
	public void setAllocation_approval_status(int allocation_approval_status) {
		this.allocation_approval_status = allocation_approval_status;
	}
	public Date getLockChangedAt() {
		return lockChangedAt;
	}
	public void setLockChangedAt(Date lockChangedAt) {
		this.lockChangedAt = lockChangedAt;
	}
	public int getDoDbStatus() {
		return doDbStatus;
	}
	public void setDoDbStatus(int doDbStatus) {
		this.doDbStatus = doDbStatus;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDoNumber() {
		return doNumber;
	}
	public void setDoNumber(String doNumber) {
		this.doNumber = doNumber;
	}
	public Date getDoDate() {
		return doDate;
	}
	public void setDoDate(Date doDate) {
		this.doDate = doDate;
	}
	public String getDoReleaseNo() {
		return doReleaseNo;
	}
	public void setDoReleaseNo(String doReleaseNo) {
		this.doReleaseNo = doReleaseNo;
	}
	public Date getDoReleaseDate() {
		return doReleaseDate;
	}
	public void setDoReleaseDate(Date doReleaseDate) {
		this.doReleaseDate = doReleaseDate;
	}
	public int getTypeOfConsumer() {
		return typeOfConsumer;
	}
	public void setTypeOfConsumer(int typeOfConsumer) {
		this.typeOfConsumer = typeOfConsumer;
	}
	public int getCustomer() {
		return customer;
	}
	public void setCustomer(int customer) {
		this.customer = customer;
	}
	public String getCustomerRef() {
		return customerRef;
	}
	public void setCustomerRef(String customerRef) {
		this.customerRef = customerRef;
	}
	public String getCustustomerContactPerson() {
		return custustomerContactPerson;
	}
	public void setCustustomerContactPerson(String custustomerContactPerson) {
		this.custustomerContactPerson = custustomerContactPerson;
	}
	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}
	public String getCoalSize() {
		return coalSize;
	}
	public void setCoalSize(String coalSize) {
		this.coalSize = coalSize;
	}
	public int getSourceMines() {
		return sourceMines;
	}
	public void setSourceMines(int sourceMines) {
		this.sourceMines = sourceMines;
	}
	public int getWashery() {
		return washery;
	}
	public void setWashery(int washery) {
		this.washery = washery;
	}
	public double getQtyAlloc(String wbCode) {
		if(wbCode == null || wbCode.length() <= 0)
			return qtyAlloc;
		if(wbCode.equalsIgnoreCase(preferedWb1))
			return preferedWb1Qty;
		else if(wbCode.equalsIgnoreCase(preferedWb2))
			return preferedWb2Qty;
		else if(wbCode.equalsIgnoreCase(preferedWb3))
			return preferedWb3Qty;
		else if(wbCode.equalsIgnoreCase(preferedWb4))
			return preferedWb4Qty;
		return Misc.getUndefDouble();
	}
	public void setQtyAlloc(double qtyAlloc) {
		this.qtyAlloc = qtyAlloc;
	}
	public double getQtyAlreadyLifted() {
		return qtyAlreadyLifted;
	}
	public void setQtyAlreadyLifted(double qtyAlreadyLifted) {
		this.qtyAlreadyLifted = qtyAlreadyLifted;
	}
	public int getQuota() {
		return quota;
	}
	public void setQuota(int quota) {
		this.quota = quota;
	}
	public double getRate() {
		return rate;
	}
	public void setRate(double rate) {
		this.rate = rate;
	}
	public double getTransportCharge() {
		return transportCharge;
	}
	public void setTransportCharge(double transportCharge) {
		this.transportCharge = transportCharge;
	}
	public double getSizingCharge() {
		return sizingCharge;
	}
	public void setSizingCharge(double sizingCharge) {
		this.sizingCharge = sizingCharge;
	}
	public double getSiloCharge() {
		return siloCharge;
	}
	public void setSiloCharge(double siloCharge) {
		this.siloCharge = siloCharge;
	}
	public double getDumpingCharge() {
		return dumpingCharge;
	}
	public void setDumpingCharge(double dumpingCharge) {
		this.dumpingCharge = dumpingCharge;
	}
	public double getTerminalCharge() {
		return terminalCharge;
	}
	public void setTerminalCharge(double terminalCharge) {
		this.terminalCharge = terminalCharge;
	}
	public double getForestCess() {
		return forestCess;
	}
	public void setForestCess(double forestCess) {
		this.forestCess = forestCess;
	}
	public double getStowingEd() {
		return stowingEd;
	}
	public void setStowingEd(double stowingEd) {
		this.stowingEd = stowingEd;
	}
	public double getaVap() {
		return avap;
	}
	public void setaVap(double aVap) {
		this.avap = aVap;
	}
	public int getAllowNoTare() {
		return allowNoTare;
	}
	public void setAllowNoTare(int allowNoTare) {
		this.allowNoTare = allowNoTare;
	}
	public double getMaxTareGap() {
		return maxTareGap;
	}
	public void setMaxTareGap(double maxTareGap) {
		this.maxTareGap = maxTareGap;
	}
	public int getDestination() {
		return destination;
	}
	public void setDestination(int destination) {
		this.destination = destination;
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
	public Date getUpdatedOn() {
		return updatedOn;
	}
	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}  

	public DoDetails(int id) {
		super();
		this.id = id;
	}

	public DoDetails(String doNumber) {
		super();
		this.doNumber = doNumber;
	}
	public DoDetails(int id, String doNumber) {
		super();
		this.id = id;
		this.doNumber = doNumber;
	}

	public DoDetails() {
		super();
	}
	public static DoDetails getDODetails(Connection conn,String doNumber,int doId, boolean apprvd, int tprId) throws Exception{
		if((doNumber == null || doNumber.length() == 0) && Misc.isUndef(doId))
			return null;
		DoDetails doDetails = null;
		if(!Misc.isUndef(doId)){
			doDetails =  (DoDetails) RFIDMasterDao.get(conn, DoDetails.class, doId);
		}else{
			ArrayList<DoDetails> doList = (ArrayList<DoDetails>) RFIDMasterDao.getList(conn, new DoDetails(doNumber), new Criteria(SECLWorkstationDetails.class, null, null, false, 1),apprvd);
			if(doList != null && doList.size() > 0)
				doDetails =  doList.get(0);
		}
		if(doDetails != null){
			double doRemainingQty = (Misc.isUndef(doDetails.preferedWb1Qty) ? 0.0 : doDetails.preferedWb1Qty) 
					  + (Misc.isUndef(doDetails.preferedWb2Qty) ? 0.0 : doDetails.preferedWb2Qty) 
					  + (Misc.isUndef(doDetails.preferedWb3Qty) ? 0.0 : doDetails.preferedWb3Qty) 
		              + (Misc.isUndef(doDetails.preferedWb4Qty) ? 0.0 : doDetails.preferedWb4Qty);
			doDetails.totQtyRemaining = doRemainingQty;
		}
		handleRates(conn,doDetails,tprId);
		/*if(doDetails != null){
			doDetails.setLatestDoInfo(LatestDOInfo.getLatestDOInfo(conn,doDetails.getPortNodeId(), doDetails.getDoNumber(),Misc.getUndefInt(),wbCode));
		}*/
		return doDetails;
	}
	
	private static void handleRates(Connection conn,DoDetails doDetails, int tprId) throws Exception {
		if(doDetails == null || Misc.isUndef(tprId) || Utils.isNull(doDetails.getDoNumber()))
			return;
		DBObjectCursor doRatesReader = null;
		try{
			StringBuilder query = new StringBuilder();
			query.append("select mdh.* from mines_do_details_hist mdh join tp_record tp on (mdh.start <= tp.latest_load_wb_out_out and mdh.end >= tp.latest_load_wb_out_out) where tp.tpr_id=? and mdh.do_number=? ");
			ArrayList<Pair<Object,Integer>> params = new ArrayList<Pair<Object,Integer>>();
			params.add(new Pair<Object, Integer>(tprId, java.sql.Types.INTEGER));
			params.add(new Pair<Object, Integer>(doDetails.getDoNumber(), java.sql.Types.VARCHAR));
			doRatesReader = DBSchemaManager.fetch(conn, DoDetails.class, "",query,params);
			if(doRatesReader != null){
				if(doRatesReader.next()){
					DoDetails rates = doRatesReader.read();
					if(rates != null){
						doDetails.rate = rates.rate;
						doDetails.transportCharge = rates.transportCharge;
						doDetails.sizingCharge = rates.sizingCharge;
						doDetails.siloCharge = rates.siloCharge;
						doDetails.dumpingCharge = rates.dumpingCharge;
						doDetails.stcCharge = rates.stcCharge;
						doDetails.terminalCharge = rates.terminalCharge;
						doDetails.forestCess = rates.forestCess;
						doDetails.stowingEd = rates.stowingEd;
						doDetails.avap = rates.avap;
						doDetails.royaltyCharge = rates.royaltyCharge;
						doDetails.otherCharges = rates.otherCharges;
						doDetails.sgstRate = rates.sgstRate;
						doDetails.cgstRate = rates.cgstRate;
						doDetails.igstRate = rates.igstRate;
						doDetails.stateCompensationCess = rates.stateCompensationCess;
						doDetails.otherCharges1PreTaxPermt = rates.otherCharges1PreTaxPermt;
						doDetails.otherCharges2PreTaxPermt = rates.otherCharges2PreTaxPermt;
						doDetails.otherCharges1PostTaxPermt = rates.otherCharges1PostTaxPermt;
						doDetails.otherCharges2PostTaxPermt = rates.otherCharges2PostTaxPermt;
						doDetails.sadakTax = rates.sadakTax;
						doDetails.dmf = rates.dmf;
						doDetails.nmet = rates.nmet;
					}
				}
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if(doRatesReader != null)
				doRatesReader.close();
		}
	}
	public double getTotQtyRemaining() {
		return totQtyRemaining;
	}
	public void setTotQtyRemaining(double totQtyRemaining) {
		this.totQtyRemaining = totQtyRemaining;
	}

	@Table("current_do_status")
	public static class LatestDOInfo{
		@KEY
		@PRIMARY_KEY
		@Column("do_id")
		private int doId = Misc.getUndefInt();
		@Column("do_number")
		private String doNumber;
		@Column("lifted_qty")
		private double liftedQty = Misc.getUndefDouble();
		@Column("last_lifted_on")
		private Date lastLiftedOn;
		@Column("last_lifted_qty")
		private double lastLiftedQty= Misc.getUndefDouble();
		@Column("last_lifted_vehicle_id")
		private int lastLiftedVehicleId = Misc.getUndefInt();
		@Column("last_lifted_tpr_id")
		private int lastLiftedTprId = Misc.getUndefInt();
		@Column("trips_count_daily")
		private int dailyTripsCount = Misc.getUndefInt();
		@Column("trips_count")
		private int tripsCount = Misc.getUndefInt();
		@Column("wb_code")
		private String wbCode;
		@Column("current_allocation")
		private double allocatedQty = Misc.getUndefDouble();
		@Column("client_allocation_qty")
		private double clientQty = Misc.getUndefDouble();
		private double alreadyLiftedQty = Misc.getUndefDouble();
		private double doAllocationQty = Misc.getUndefDouble();
		
		public double getRemaingQty(){
//			double qtyAllocated = Misc.isUndef(getAllocatedQty())  ? Misc.isUndef(getDoAllocationQty()) ? 0.0 : getDoAllocationQty() : getAllocatedQty();
			double qtyAllocated = Misc.isUndef(getDoAllocationQty())  ? Misc.isUndef(getAllocatedQty()) ? 0.0 : getAllocatedQty() : getDoAllocationQty();
			double qtyAlreadyLifted = Misc.isUndef(getAlreadyLiftedQty()) ?  0.0  : getAlreadyLiftedQty();
			double liftedQty = Misc.isUndef(getLiftedQty()) ? 0.0 : getLiftedQty();
			double doRemainingQty = (qtyAllocated - (qtyAlreadyLifted+liftedQty) ); 
			return doRemainingQty < 0.0 ? Misc.getUndefDouble() : doRemainingQty; 
		}

		public double getAllocatedQty() {
			return allocatedQty;
		}
		public void setAllocatedQty(double allocatedQty) {
			this.allocatedQty = allocatedQty;
		}
		public double getAlreadyLiftedQty() {
			return alreadyLiftedQty;
		}
		public void setAlreadyLiftedQty(double alreadyLiftedQty) {
			this.alreadyLiftedQty = alreadyLiftedQty;
		}
		public int getDoId() {
			return doId;
		}
		public void setDoId(int doId) {
			this.doId = doId;
		}
		public String getDoNumber() {
			return doNumber;
		}
		public void setDoNumber(String doNumber) {
			this.doNumber = doNumber;
		}
		
		public double getLiftedQty() {
			return liftedQty;
		}
		public void setLiftedQty(double liftedQty) {
			this.liftedQty = liftedQty;
		}
		public Date getLastLiftedOn() {
			return lastLiftedOn;
		}
		public void setLastLiftedOn(Date lastLiftedOn) {
			this.lastLiftedOn = lastLiftedOn;
		}
		public double getLastLiftedQty() {
			return lastLiftedQty;
		}
		public void setLastLiftedQty(double lastLiftedQty) {
			this.lastLiftedQty = lastLiftedQty;
		}
		public int getLastLiftedVehicleId() {
			return lastLiftedVehicleId;
		}
		public void setLastLiftedVehicleId(int lastLiftedVehicleId) {
			this.lastLiftedVehicleId = lastLiftedVehicleId;
		}
		public int getLastLiftedTprId() {
			return lastLiftedTprId;
		}
		public void setLastLiftedTprId(int lastLiftedTprId) {
			this.lastLiftedTprId = lastLiftedTprId;
		}
		public LatestDOInfo() {
			super();
		}
		public LatestDOInfo(int doId) {
			super();
			this.doId = doId;
		}
		public LatestDOInfo(String doNumber) {
			super();
			this.doNumber = doNumber;
		}
		public LatestDOInfo(String doNumber,String wbCode) {
			super();
			this.doNumber = doNumber;
			this.wbCode = wbCode;
		}
		public String getWbCode() {
			return wbCode;
		}
		public void setWbCode(String wbCode) {
			this.wbCode = wbCode;
		}
		public int getDailyTripsCount() {
			return dailyTripsCount;
		}
		public void setDailyTripsCount(int dailyTripsCount) {
			this.dailyTripsCount = dailyTripsCount;
		}
		public int getTripsCount() {
			return tripsCount;
		}
		public void setTripsCount(int tripsCount) {
			this.tripsCount = tripsCount;
		}

		public double getClientQty() {
			return clientQty;
		}

		public void setClientQty(double clientQty) {
			this.clientQty = clientQty;
		}

		public double getDoAllocationQty() {
			return doAllocationQty;
		}

		public void setDoAllocationQty(double doAllocationQty) {
			this.doAllocationQty = doAllocationQty;
		}
		
	}
	public static int getVehiclInsideForDo(Connection conn,String doNumber){
		int retval = Misc.getUndefInt();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("select count(*) from tp_record where do_number=? and status=1 and combo_start between date(now()) and now()");
			ps.setString(1, doNumber);
			rs = ps.executeQuery();
			if(rs.next())
				retval = Misc.getRsetInt(rs, 1);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	
	}
	public Pair<Integer,String> isDOValid(Connection conn,String wbCode, LatestDOInfo lastestDoInfo) throws Exception {
		return isDOValid(conn, wbCode, lastestDoInfo, false);
	}
	public Pair<Integer,String> isDOValid(Connection conn,String wbCode, LatestDOInfo lastestDoInfo, boolean useQuota) throws Exception {
		StringBuilder resultStr = new StringBuilder();
		int isDOValid = Type.ANSWER.NO;
		double doRemainingQty = lastestDoInfo == null ? Misc.getUndefDouble() : lastestDoInfo.getRemaingQty(); 
		boolean doExpired = (doReleaseDate == null || doReleaseDate.getTime() > System.currentTimeMillis() || validityDate == null  || validityDate.getTime() < System.currentTimeMillis() );
		boolean doExhausted = (Misc.isUndef(doRemainingQty) ||  doRemainingQty < DoDetails.MinTaransactionalQty);
		int noOfVehicleInside = useQuota ? getVehiclInsideForDo(conn, doNumber) : Misc.getUndefInt();
		boolean isQuotaFull = useQuota ? !Misc.isUndef(quota) && !Misc.isUndef(noOfVehicleInside) && noOfVehicleInside >= quota : false;
		if(doExpired || doExhausted || isQuotaFull || getLockStatus() == 1){
			if(doExpired)
				resultStr.append("Expired");
			if(doExhausted){
				if(resultStr != null && resultStr.length() > 0)
					resultStr.append(" and ");
				resultStr.append(" Exhausted");
			}
			if(isQuotaFull){
				if(resultStr != null && resultStr.length() > 0)
					resultStr.append(" and ");
				resultStr.append(" Quota Full("+noOfVehicleInside+"/"+(Misc.isUndef(quota) ? "NA" : quota)+")");
			}
			if(getLockStatus() == 1){
				if(resultStr != null && resultStr.length() > 0)
					resultStr.append(" and ");
				resultStr.append(" Locked");
			}
			
			isDOValid = Type.ANSWER.NO;
		}else{
			isDOValid = Type.ANSWER.YES;
			resultStr.append("Valid DO , Quota("+noOfVehicleInside+"/"+(Misc.isUndef(quota) ? "NA" : quota)+")");
		}

		return new Pair<Integer,String>(isDOValid, resultStr.toString());
	}
	public String getCustomerCode() {
		return customerCode;
	}
	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}
	public String getGradeCode() {
		return gradeCode;
	}
	public void setGradeCode(String gradeCode) {
		this.gradeCode = gradeCode;
	}
	public String getSourceCode() {
		return sourceCode;
	}
	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}
	public String getWasheryCode() {
		return washeryCode;
	}
	public void setWasheryCode(String washeryCode) {
		this.washeryCode = washeryCode;
	}
	public String getDestinationCode() {
		return destinationCode;
	}
	public void setDestinationCode(String destinationCode) {
		this.destinationCode = destinationCode;
	}
	public String getPreferedWb1() {
		return preferedWb1;
	}
	public void setPreferedWb1(String preferedWb1) {
		this.preferedWb1 = preferedWb1;
	}
	public String getPreferedWb2() {
		return preferedWb2;
	}
	public void setPreferedWb2(String preferedWb2) {
		this.preferedWb2 = preferedWb2;
	}
	public String getPreferedWb3() {
		return preferedWb3;
	}
	public void setPreferedWb3(String preferedWb3) {
		this.preferedWb3 = preferedWb3;
	}
	public String getPreferedWb4() {
		return preferedWb4;
	}
	public void setPreferedWb4(String preferedWb4) {
		this.preferedWb4 = preferedWb4;
	}
	public String getDeliveryPoint() {
		return deliveryPoint;
	}
	public void setDeliveryPoint(String deliveryPoint) {
		this.deliveryPoint = deliveryPoint;
	}
	public double getRoyaltyCharge() {
		return royaltyCharge;
	}
	public void setRoyaltyCharge(double royaltyCharge) {
		this.royaltyCharge = royaltyCharge;
	}
	public String getMaterial() {
		return material;
	}
	public void setMaterial(String material) {
		this.material = material;
	}
	public double getStcCharge() {
		return stcCharge;
	}
	public void setStcCharge(double stcCharge) {
		this.stcCharge = stcCharge;
	}
	public Date getValidityDate() {
		return validityDate;
	}
	public void setValidityDate(Date validityDate) {
		this.validityDate = validityDate;
	}
	public int getTransportMode() {
		return transportMode;
	}
	public void setTransportMode(int transportMode) {
		this.transportMode = transportMode;
	}
	public double getPreferedWb1Qty() {
		return preferedWb1Qty;
	}
	public void setPreferedWb1Qty(double preferedWb1Qty) {
		this.preferedWb1Qty = preferedWb1Qty;
	}
	public double getPreferedWb2Qty() {
		return preferedWb2Qty;
	}
	public void setPreferedWb2Qty(double preferedWb2Qty) {
		this.preferedWb2Qty = preferedWb2Qty;
	}
	public double getPreferedWb3Qty() {
		return preferedWb3Qty;
	}
	public void setPreferedWb3Qty(double preferedWb3Qty) {
		this.preferedWb3Qty = preferedWb3Qty;
	}
	public double getPreferedWb4Qty() {
		return preferedWb4Qty;
	}
	public void setPreferedWb4Qty(double preferedWb4Qty) {
		this.preferedWb4Qty = preferedWb4Qty;
	}
	
	public static double getQtyAfterThisTrip(Connection conn,int vehicleId, String doNumber, String wbCode, long after){
		if(Misc.isUndef(vehicleId) || Utils.isNull(doNumber) || Utils.isNull(wbCode) || Misc.isUndef(after))
			return Misc.getUndefDouble();
		double retval = Misc.getUndefDouble();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("select sum((case when load_tare is not null and load_gross is not null and load_gross > load_tare then (load_gross-load_tare) else 0.0 end)) from tp_record where load_wb_out_name=? and do_number=? and latest_load_wb_out_out > ? and status=?");
			ps.setString(1, wbCode);
			ps.setString(2, doNumber);
			ps.setTimestamp(3, new Timestamp(after));
			Misc.setParamInt(ps, Status.ACTIVE, 4);
			rs = ps.executeQuery();
			if(rs.next()){
				retval = Misc.getRsetDouble(rs, 1);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	public static LatestDOInfo getLatestDOInfo(Connection conn, String doNumber ,String wbCode, boolean apprvd) throws Exception{
		return getLatestDOInfo(conn, doNumber, wbCode, apprvd, false);
	}
	public static LatestDOInfo getLatestDOInfo(Connection conn, String doNumber ,String wbCode, boolean apprvd,boolean forUpdate) throws Exception{
		LatestDOInfo retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			if(wbCode != null && wbCode.length() > 0){
				ps = conn.prepareStatement("select cds.current_allocation,null,cds.lifted_qty, cds.last_lifted_on, cds.last_lifted_qty,cds.last_lifted_vehicle_id,cds.last_lifted_tpr_id,cds.trips_count_daily,cds.trips_count,null from current_do_status"+(apprvd ? "_apprvd": "")+" cds  where cds.wb_code=? and cds.do_number=? "+(forUpdate ? "for update" : ""));
				ps.setString(1, wbCode);
				ps.setString(2, doNumber);
			}else{
				ps = conn.prepareStatement("select cds.current_allocation,mdd.qty_already_lifted,cds.lifted_qty, cds.last_lifted_on, cds.last_lifted_qty,cds.last_lifted_vehicle_id,cds.last_lifted_tpr_id,cds.trips_count_daily,cds.trips_count,mdd.qty_alloc from mines_do_details"+(apprvd ? "_apprvd": "")+" mdd left outer join "
						+" (select do_number, sum(current_allocation) current_allocation,sum(lifted_qty) lifted_qty, max(last_lifted_on) last_lifted_on, max(last_lifted_qty) last_lifted_qty,last_lifted_vehicle_id,max(last_lifted_tpr_id) last_lifted_tpr_id,sum(trips_count_daily) trips_count_daily,sum(trips_count) trips_count from current_do_status"+(apprvd ? "_apprvd": "")+"  where do_number=? group by do_number) cds on (mdd.do_number = cds.do_number) where mdd.do_number=?");
				ps.setString(1, doNumber);
				ps.setString(2, doNumber);
			}
			rs = ps.executeQuery();
			if(rs.next()){
				retval = new LatestDOInfo(doNumber,wbCode);
				retval.setAllocatedQty(Misc.getRsetDouble(rs, 1));
				retval.setAlreadyLiftedQty(Misc.getRsetDouble(rs, 2));
				retval.setLiftedQty(Misc.getRsetDouble(rs, 3));
				retval.setLastLiftedOn(Misc.getDate(rs, 4));
				retval.setLastLiftedQty(Misc.getRsetDouble(rs, 5));
				retval.setLastLiftedVehicleId(Misc.getRsetInt(rs, 6));
				retval.setLastLiftedTprId(Misc.getRsetInt(rs, 7));
				retval.setDailyTripsCount(Misc.getRsetInt(rs, 8));
				retval.setTripsCount(Misc.getRsetInt(rs, 9));
				retval.setDoAllocationQty(Misc.getRsetDouble(rs, 10));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	public static ArrayList<LatestDOInfo> getLatestAllocationForDo(Connection conn, String doNumber , boolean apprvd) throws Exception{
		if(doNumber == null || doNumber.length() <= 0)
			return null;
		ArrayList<LatestDOInfo> retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		LatestDOInfo latestDOInfo = null;
		try{
			ps = conn.prepareStatement("select cds.current_allocation,null,cds.lifted_qty, cds.last_lifted_on, cds.last_lifted_qty,cds.last_lifted_vehicle_id,cds.last_lifted_tpr_id,cds.trips_count_daily,cds.trips_count,cds.wb_code from current_do_status"+(apprvd ? "_apprvd": "")+" cds  where cds.do_number=?");
			ps.setString(1, doNumber);
			rs = ps.executeQuery();
			while(rs.next()){
				latestDOInfo = new LatestDOInfo(doNumber,null);
				latestDOInfo.setAllocatedQty(Misc.getRsetDouble(rs, 1));
				latestDOInfo.setAlreadyLiftedQty(Misc.getRsetDouble(rs, 2));
				latestDOInfo.setLiftedQty(Misc.getRsetDouble(rs, 3));
				latestDOInfo.setLastLiftedOn(Misc.getDate(rs, 4));
				latestDOInfo.setLastLiftedQty(Misc.getRsetDouble(rs, 5));
				latestDOInfo.setLastLiftedVehicleId(Misc.getRsetInt(rs, 6));
				latestDOInfo.setLastLiftedTprId(Misc.getRsetInt(rs, 7));
				latestDOInfo.setDailyTripsCount(Misc.getRsetInt(rs, 8));
				latestDOInfo.setTripsCount(Misc.getRsetInt(rs, 9));
				latestDOInfo.setWbCode(rs.getString(10));
				if(retval == null)
					retval = new ArrayList<DoDetails.LatestDOInfo>();
				retval.add(latestDOInfo);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	/*public static double getRemaingQty(Connection conn, String doNumber ,String wbCode) throws Exception{
		double retval = Misc.getUndefDouble();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			if(wbCode != null && wbCode.length() > 0){
				ps = conn.prepareStatement("select "
						+ " ("
						+ " case when prefered_wb_1  like ? then prefered_wb_1_qty "
						+ "	when prefered_wb_2  like ? then prefered_wb_2_qty "
						+ "	when prefered_wb_3  like ? then prefered_wb_3_qty "
						+ "	when prefered_wb_4  like ? then prefered_wb_4_qty "
						+ " else 0.0 end ) from mines_do_details where (prefered_wb_1  like ? or prefered_wb_2  like ? or prefered_wb_3  like ? or prefered_wb_4  like ?) and do_number like ?");
				ps.setString(1, wbCode);
				ps.setString(2, wbCode);
				ps.setString(3, wbCode);
				ps.setString(4, wbCode);
				ps.setString(5, wbCode);
				ps.setString(6, wbCode);
				ps.setString(7, wbCode);
				ps.setString(8, wbCode);
				ps.setString(9, doNumber);
			}else{
				ps = conn.prepareStatement("select (case when (prefered_wb_1_qty is null and prefered_wb_2_qty is null and prefered_wb_3_qty is null and prefered_wb_4_qty is null) then ((case when qty_alloc is null then 0.0 else qty_alloc end)-(case when qty_already_lifted is null then 0.0 else qty_already_lifted end)) else "
						+ "("
						+ "(case when prefered_wb_1_qty is null then 0.0 else prefered_wb_1_qty end) +"
						+ "(case when prefered_wb_2_qty is null then 0.0 else prefered_wb_2_qty end) +"
						+ "(case when prefered_wb_3_qty is null then 0.0 else prefered_wb_3_qty end) +"
						+ "(case when prefered_wb_4_qty is null then 0.0 else prefered_wb_4_qty end)"
						+ ")"
						+ "end) from mines_do_details where do_number like ? ");
				ps.setString(1, doNumber);
			}
			rs = ps.executeQuery();
			if(rs.next()){
				retval = Misc.getRsetDouble(rs, 1);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}*/
	public static boolean isWBUseThisDo(Connection conn, String doNumber ,String wbCode) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		if(wbCode == null || wbCode.length() <= 0 || doNumber == null || doNumber.length() <= 0)
			return false;
		try{
			ps = conn.prepareStatement("select 1 from current_do_status where wb_code=? and do_number=?");
			ps.setString(1, wbCode);
			ps.setString(2, doNumber);
			rs = ps.executeQuery();
			if(rs.next()){
				return true;
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return false;
	}
	public int getLockStatus() {
		return lockStatus;
	}
	public void setLockStatus(int lockStatus) {
		this.lockStatus = lockStatus;
	}
	public double getQtyAlloc() {
		return qtyAlloc;
	}
	public static boolean checkLock(Connection local,Connection remote, String doNumber, boolean apprvd) throws Exception{
		LatestDOInfo completeDoInfo = getLatestDOInfo(remote, doNumber, null, apprvd);
		double qtyAlreadyLifted = completeDoInfo == null ? 0.0 : completeDoInfo.getAlreadyLiftedQty();
		double doAllocation = completeDoInfo == null ? 0.0 : completeDoInfo.getDoAllocationQty();
		double maxLiftable = doAllocation - qtyAlreadyLifted;
		double totalAllocated = completeDoInfo == null ? 0.0 : completeDoInfo.getAllocatedQty();
		double totalLifted = completeDoInfo == null ? 0.0 : completeDoInfo.getLiftedQty();
		return (maxLiftable < totalAllocated || maxLiftable < totalLifted);
	}
	public int getDoType() {
		return doType;
	}
	public void setDoType(int doType) {
		this.doType = doType;
	}
	
	public static int getDoId(Connection conn,String doNumber) throws Exception{
		if(conn == null || doNumber == null || doNumber.trim().length() <= 0)
			return Misc.getUndefInt();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("select id from mines_do_details where do_number=?");
			ps.setString(1, doNumber);
			rs = ps.executeQuery();
			if(rs.next()){
				return Misc.getRsetInt(rs, 1);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return Misc.getUndefInt();
	}
	public double getOrigianlQty() {
		return origianlQty;
	}
	public void setOrigianlQty(double origianlQty) {
		this.origianlQty = origianlQty;
	}
	public int getTaxationType() {
		return taxationType;
	}
	public void setTaxationType(int taxationType) {
		this.taxationType = taxationType;
	}
	public double getOtherCharges() {
		return otherCharges;
	}
	public void setOtherCharges(double otherCharges) {
		this.otherCharges = otherCharges;
	}
	public double getSgstRate() {
		return sgstRate;
	}
	public void setSgstRate(double sgstRate) {
		this.sgstRate = sgstRate;
	}
	public double getCgstRate() {
		return cgstRate;
	}
	public void setCgstRate(double cgstRate) {
		this.cgstRate = cgstRate;
	}
	public double getIgstRate() {
		return igstRate;
	}
	public void setIgstRate(double igstRate) {
		this.igstRate = igstRate;
	}
	public double getStateCompensationCess() {
		return stateCompensationCess;
	}
	public void setStateCompensationCess(double stateCompensationCess) {
		this.stateCompensationCess = stateCompensationCess;
	}
	public double getOtherCharges1PreTaxPermt() {
		return otherCharges1PreTaxPermt;
	}
	public void setOtherCharges1PreTaxPermt(double otherCharges1PreTaxPermt) {
		this.otherCharges1PreTaxPermt = otherCharges1PreTaxPermt;
	}
	public double getOtherCharges2PreTaxPermt() {
		return otherCharges2PreTaxPermt;
	}
	public void setOtherCharges2PreTaxPermt(double otherCharges2PreTaxPermt) {
		this.otherCharges2PreTaxPermt = otherCharges2PreTaxPermt;
	}
	public double getOtherCharges1PostTaxPermt() {
		return otherCharges1PostTaxPermt;
	}
	public void setOtherCharges1PostTaxPermt(double otherCharges1PostTaxPermt) {
		this.otherCharges1PostTaxPermt = otherCharges1PostTaxPermt;
	}
	public double getOtherCharges2PostTaxPermt() {
		return otherCharges2PostTaxPermt;
	}
	public void setOtherCharges2PostTaxPermt(double otherCharges2PostTaxPermt) {
		this.otherCharges2PostTaxPermt = otherCharges2PostTaxPermt;
	}
	public Date getGstCutoverDate() {
		return gstCutoverDate;
	}
	public void setGstCutoverDate(Date gstCutoverDate) {
		this.gstCutoverDate = gstCutoverDate;
	}
	public double getTotValue() {
		return totValue;
	}
	public void setTotValue(double totValue) {
		this.totValue = totValue;
	}
	public double getTotValuePaid() {
		return totValuePaid;
	}
	public void setTotValuePaid(double totValuePaid) {
		this.totValuePaid = totValuePaid;
	}
	public int getGstAutoadjustmentDone() {
		return gstAutoadjustmentDone;
	}
	public void setGstAutoadjustmentDone(int gstAutoadjustmentDone) {
		this.gstAutoadjustmentDone = gstAutoadjustmentDone;
	}
	public double getTotValueGst() {
		return totValueGst;
	}
	public void setTotValueGst(double totValueGst) {
		this.totValueGst = totValueGst;
	}
	public String getArvId() {
		return arvId;
	}
	public void setArvId(String arvId) {
		this.arvId = arvId;
	}
	public String getConsginee() {
		return consginee;
	}
	public void setConsginee(String consginee) {
		this.consginee = consginee;
	}
	public String getConsgineeAddress() {
		return consgineeAddress;
	}
	public void setConsgineeAddress(String consgineeAddress) {
		this.consgineeAddress = consgineeAddress;
	}
	public String getConsgineeState() {
		return consgineeState;
	}
	public void setConsgineeState(String consgineeState) {
		this.consgineeState = consgineeState;
	}
	public double getSadakTax() {
		return sadakTax;
	}
	public void setSadakTax(double sadakTax) {
		this.sadakTax = sadakTax;
	}
	public int getIntField2() {
		return intField2;
	}
	public void setIntField2(int intField2) {
		this.intField2 = intField2;
	}
	public double getDmf() {
		return dmf;
	}
	public void setDmf(double dmf) {
		this.dmf = dmf;
	}
	public double getNmet() {
		return nmet;
	}
	public void setNmet(double nmet) {
		this.nmet = nmet;
	}
	
}
