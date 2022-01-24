package com.ipssi.rfid.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.DoDetails.LatestDOInfo;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.processor.InvoicePrintUtils;
import com.ipssi.rfid.processor.InvoicePrintUtils.Alignment;
import com.ipssi.rfid.processor.NumberToWordsConverter;
import com.ipssi.rfid.processor.Utils;

public class SeclTprInvoice {
	private int tprId; 
	private String invoiceNo; 
	private Date invoiceDate; 
	private String doNumber; 
	private String issueNumber; 
	private double loadGross = Misc.getUndefDouble(); 
	private double loadTare = Misc.getUndefDouble(); 
	private double loadNet = Misc.getUndefDouble(); 
	private Date loadGrossTime; 
	private double coalValue = Misc.getUndefDouble(); 
	private double sizing = Misc.getUndefDouble(); 
	private double silo = Misc.getUndefDouble(); 
	private double stc = Misc.getUndefDouble(); 
	private double royalty = Misc.getUndefDouble(); 
	private double dmf = Misc.getUndefDouble(); 
	private double nmet = Misc.getUndefDouble(); 
	private double sed = 0;//Misc.getUndefDouble(); 
	private double otherPreTax = Misc.getUndefDouble(); 
	private double terminalTax = Misc.getUndefDouble(); 
	private double forestCess = Misc.getUndefDouble(); 
	private double dumping = Misc.getUndefDouble(); 
	private double avap = Misc.getUndefDouble(); 
	private double sadakTax = Misc.getUndefDouble(); 
	private double otherPreTax2 = Misc.getUndefDouble(); 
	private double taxableValue = Misc.getUndefDouble(); 
	private double sgst = Misc.getUndefDouble(); 
	private double cgst = Misc.getUndefDouble(); 
	private double igst = Misc.getUndefDouble(); 
	private double stateCompCess = Misc.getUndefDouble(); 
	private double otherCharges = Misc.getUndefDouble(); 
	private double grossSaleValue = Misc.getUndefDouble(); 
	private double doubleField1 = Misc.getUndefDouble();  
	private double doubleField2 = Misc.getUndefDouble(); 
	private double doubleField3 = Misc.getUndefDouble();
	private double otherPostTaxOne = Misc.getUndefDouble();
	private double otherPostTaxTwo = Misc.getUndefDouble();
	private String vehicleName = null;
	private Date updatedOn; 
	private int updatedBy = Misc.getUndefInt();
	private String custGstNo;
	private int invoiceLocked = Misc.getUndefInt();
	public int getTprId() {
		return tprId;
	}
	public void setTprId(int tprId) {
		this.tprId = tprId;
	}
	public String getInvoiceNo() {
		return invoiceNo;
	}
	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}
	public Date getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public String getDoNumber() {
		return doNumber;
	}
	public void setDoNumber(String doNumber) {
		this.doNumber = doNumber;
	}
	public String getIssueNumber() {
		return issueNumber;
	}
	public void setIssueNumber(String issueNumber) {
		this.issueNumber = issueNumber;
	}
	public double getLoadGross() {
		return loadGross;
	}
	public void setLoadGross(double loadGross) {
		this.loadGross = loadGross;
	}
	public double getLoadTare() {
		return loadTare;
	}
	public void setLoadTare(double loadTare) {
		this.loadTare = loadTare;
	}
	public double getLoadNet() {
		return loadNet;
	}
	public void setLoadNet(double loadNet) {
		this.loadNet = loadNet;
	}
	public Date getLoadGrossTime() {
		return loadGrossTime;
	}
	public void setLoadGrossTime(Date loadGrossTime) {
		this.loadGrossTime = loadGrossTime;
	}
	public double getCoalValue() {
		return coalValue;
	}
	public void setCoalValue(double coalValue) {
		this.coalValue = coalValue;
	}
	public double getSizing() {
		return sizing;
	}
	public void setSizing(double sizing) {
		this.sizing = sizing;
	}
	public double getSilo() {
		return silo;
	}
	public void setSilo(double silo) {
		this.silo = silo;
	}
	public double getStc() {
		return stc;
	}
	public void setStc(double stc) {
		this.stc = stc;
	}
	public double getRoyalty() {
		return royalty;
	}
	public void setRoyalty(double royalty) {
		this.royalty = royalty;
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
	public double getSed() {
		return sed;
	}
	public void setSed(double sed) {
		this.sed = sed;
	}
	public double getOtherPreTax() {
		return otherPreTax;
	}
	public void setOtherPreTax(double otherPreTax) {
		this.otherPreTax = otherPreTax;
	}
	public double getTerminalTax() {
		return terminalTax;
	}
	public void setTerminalTax(double terminalTax) {
		this.terminalTax = terminalTax;
	}
	public double getForestCess() {
		return forestCess;
	}
	public void setForestCess(double forestCess) {
		this.forestCess = forestCess;
	}
	public double getAvap() {
		return avap;
	}
	public void setAvap(double avap) {
		this.avap = avap;
	}
	public double getSadakTax() {
		return sadakTax;
	}
	public void setSadakTax(double sadakTax) {
		this.sadakTax = sadakTax;
	}
	public double getOtherPreTax2() {
		return otherPreTax2;
	}
	public void setOtherPreTax2(double otherPreTax2) {
		this.otherPreTax2 = otherPreTax2;
	}
	public double getTaxableValue() {
		return taxableValue;
	}
	public void setTaxableValue(double taxableValue) {
		this.taxableValue = taxableValue;
	}
	public double getSgst() {
		return sgst;
	}
	public void setSgst(double sgst) {
		this.sgst = sgst;
	}
	public double getCgst() {
		return cgst;
	}
	public void setCgst(double cgst) {
		this.cgst = cgst;
	}
	public double getIgst() {
		return igst;
	}
	public void setIgst(double igst) {
		this.igst = igst;
	}
	public double getStateCompCess() {
		return stateCompCess;
	}
	public void setStateCompCess(double stateCompCess) {
		this.stateCompCess = stateCompCess;
	}
	public double getOtherCharges() {
		return otherCharges;
	}
	public void setOtherCharges(double otherCharges) {
		this.otherCharges = otherCharges;
	}
	public double getGrossSaleValue() {
		return grossSaleValue;
	}
	public void setGrossSaleValue(double grossSaleValue) {
		this.grossSaleValue = grossSaleValue;
	}
	public double getDoubleField1() {
		return doubleField1;
	}
	public void setDoubleField1(double doubleField1) {
		this.doubleField1 = doubleField1;
	}
	public double getDoubleField2() {
		return doubleField2;
	}
	public void setDoubleField2(double doubleField2) {
		this.doubleField2 = doubleField2;
	}
	public double getDoubleField3() {
		return doubleField3;
	}
	public void setDoubleField3(double doubleField3) {
		this.doubleField3 = doubleField3;
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
	public String getCustGstNo() {
		return custGstNo;
	}
	public void setCustGstNo(String custGstNo) {
		this.custGstNo = custGstNo;
	}
	
	public int getInvoiceLocked() {
		return invoiceLocked;
	}
	public void setInvoiceLocked(int invoiceLocked) {
		this.invoiceLocked = invoiceLocked;
	}
	public SeclTprInvoice(int tprId, String invoiceNo, Date invoiceDate, String doNumber, String issueNumber,
			double loadGross, double loadTare, double loadNet, Date loadGrossTime, double coalValue, double sizing,
			double silo, double stc, double royalty, double dmf, double nmet, double sed, double otherPreTax,
			double terminalTax, double forestCess, double avap, double sadakTax, double otherPreTax2,
			double taxableValue, double sgst, double cgst, double igst, double stateCompCess, double otherCharges,
			double grossSaleValue, double doubleField1, double doubleField2, double doubleField3, Date updatedOn,
			int updatedBy) {
		super();
		this.tprId = tprId;
		this.invoiceNo = invoiceNo;
		this.invoiceDate = invoiceDate;
		this.doNumber = doNumber;
		this.issueNumber = issueNumber;
		this.loadGross = loadGross;
		this.loadTare = loadTare;
		this.loadNet = loadNet;
		this.loadGrossTime = loadGrossTime;
		this.coalValue = coalValue;
		this.sizing = sizing;
		this.silo = silo;
		this.stc = stc;
		this.royalty = royalty;
		this.dmf = dmf;
		this.nmet = nmet;
		this.sed = 0.0;//sed;
		this.otherPreTax = otherPreTax;
		this.terminalTax = terminalTax;
		this.forestCess = forestCess;
		this.avap = avap;
		this.sadakTax = sadakTax;
		this.otherPreTax2 = otherPreTax2;
		this.taxableValue = taxableValue;
		this.sgst = sgst;
		this.cgst = cgst;
		this.igst = igst;
		this.stateCompCess = stateCompCess;
		this.otherCharges = otherCharges;
		this.grossSaleValue = grossSaleValue;
		this.doubleField1 = doubleField1;
		this.doubleField2 = doubleField2;
		this.doubleField3 = doubleField3;
		this.updatedOn = updatedOn;
		this.updatedBy = updatedBy;
	}
	public SeclTprInvoice() {
		super();
	} 
	
	public static SeclTprInvoice getSeclTprInvoice(Connection conn, TPRecord tpRecord,DoDetails doDetails,Mines mines) throws Exception{
		if(tpRecord == null || doDetails == null || mines == null)
			return null;
		CustomerDetails custDetails = CustomerDetails.getCustomer(conn, doDetails.getCustomerCode(), Misc.getUndefInt());
		int tprId = tpRecord.getTprId();
		String invoiceNo = tpRecord.getInvoiceNumber(); 
		Date invoiceDate = tpRecord.getLatestLoadWbOutExit(); 
		String doNumber = tpRecord.getDoNumber(); 
		String issueNumber = doDetails.getDoReleaseNo(); 
		double loadGross = tpRecord.getLoadGross(); 
		double loadTare = tpRecord.getLoadTare(); 
		double net = loadGross-loadTare; 
		Date loadGrossTime = tpRecord.getLatestLoadWbOutExit(); 
		double otherCharges = Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getOtherCharges()) ? 0.0 : doDetails.getOtherCharges());
		double otherPreTax = Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getOtherCharges1PreTaxPermt()) ? 0.0 : doDetails.getOtherCharges1PreTaxPermt()); 
		double otherPreTax2 = Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getOtherCharges2PreTaxPermt()) ? 0.0 : doDetails.getOtherCharges2PreTaxPermt());
		double otherPostTax1 = Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getOtherCharges1PostTaxPermt()) ? 0.0 : doDetails.getOtherCharges1PostTaxPermt()); 
		double otherPostTax2 = Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getOtherCharges2PostTaxPermt()) ? 0.0 : doDetails.getOtherCharges2PostTaxPermt());
		double doubleField1 = Misc.getUndefDouble();  
		double doubleField2 = Misc.getUndefDouble(); 
		double doubleField3 = Misc.getUndefDouble();
		double coalValue = Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getRate()) ? 0.0 : doDetails.getRate());
		double sizing = Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getSizingCharge()) ? 0.0 : doDetails.getSizingCharge());
		double silo = Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getSiloCharge()) ? 0.0 : doDetails.getSiloCharge());
		double stc = Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getStcCharge()) ? 0.0 : doDetails.getStcCharge());
		double dumpingCharge = Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getDumpingCharge()) ? 0.0 : doDetails.getDumpingCharge());
		double royalty = Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getRoyaltyCharge()) ? 0.0 : doDetails.getRoyaltyCharge());
		
		double dmfRate = !Misc.isUndef(doDetails.getDmf()) ? doDetails.getDmf() : Misc.isUndef(mines.getDmfRate()) ? 0.0 : mines.getDmfRate();
		double nmetRate = !Misc.isUndef(doDetails.getNmet()) ? doDetails.getNmet() : Misc.isUndef(mines.getNmetRate()) ? 0.0 : mines.getNmetRate();
		
		//double dmf = Misc.isUndef(royalty) ? 0.0 : royalty* ((Misc.isUndef(mines.getDmfRate()) ? 0.0 : mines.getDmfRate())/100);
		//double nmet = Misc.isUndef(royalty) ? 0.0 : royalty* ((Misc.isUndef(mines.getNmetRate()) ? 0.0 :  mines.getNmetRate())/100);
		double dmf = royalty * (dmfRate/100);
		double nmet = royalty * (nmetRate/100);
		double sed = 0.0;//Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getStowingEd()) ? 0.0 : doDetails.getStowingEd());
		double terminalTax = Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getTerminalCharge()) ? 0.0 : doDetails.getTerminalCharge());
		double forestCess = Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getForestCess()) ? 0.0 : doDetails.getForestCess());
		double sadakTax = Misc.isUndef(net) || coalValue < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getSadakTax()) ? 0.0 : doDetails.getSadakTax()); 
		double avap = Misc.isUndef(net) || net < 0.0 ? 0.0 : (net)*(Misc.isUndef(doDetails.getaVap()) ? 0.0 : doDetails.getaVap());
		
		double taxableValue = coalValue + silo + dumpingCharge + stc + sizing + royalty + dmf + nmet + sed + terminalTax + forestCess + avap + sadakTax + (Misc.isUndef(otherCharges) ? 0.0 : otherCharges) + (Misc.isUndef(otherPreTax) ? 0.0 : otherPreTax) + (Misc.isUndef(otherPreTax2) ? 0.0 : otherPreTax2);
		
		/*double sgst = Misc.isUndef(doDetails.getSgstRate()) || doDetails.getSgstRate() < 0.0 ? 0.0 : taxableValue * (doDetails.getSgstRate()/100) ;
		double cgst = Misc.isUndef(doDetails.getCgstRate()) || doDetails.getCgstRate() < 0.0 ? 0.0 : taxableValue * (doDetails.getCgstRate()/100) ;
		double igst = Misc.isUndef(doDetails.getIgstRate()) || doDetails.getIgstRate() < 0.0 ? 0.0 : taxableValue * (doDetails.getIgstRate()/100) ;
		*/

		double sgst = Math.round(Misc.isUndef(doDetails.getSgstRate()) || doDetails.getSgstRate() < 0.0 ? 0.0 : taxableValue * (doDetails.getSgstRate()/100)) ;
		double cgst =Math.round(Misc.isUndef(doDetails.getCgstRate()) || doDetails.getCgstRate() < 0.0 ? 0.0 : taxableValue * (doDetails.getCgstRate()/100)) ;
		double igst =Math.round(Misc.isUndef(doDetails.getIgstRate()) || doDetails.getIgstRate() < 0.0 ? 0.0 : taxableValue * (doDetails.getIgstRate()/100)) ;
		
		double stateCompCess = Misc.isUndef(doDetails.getStateCompensationCess()) || doDetails.getStateCompensationCess() < 0.0 ? 0.0 : net * (doDetails.getStateCompensationCess()) ;
		double grossSaleValue = taxableValue + sgst + cgst + igst + stateCompCess + (Misc.isUndef(otherPostTax1) ? 0.0 : otherPostTax1) + (Misc.isUndef(otherPostTax2) ? 0.0 : otherPostTax2);
		
		SeclTprInvoice retval = new SeclTprInvoice();
		retval.tprId = tprId;
		retval.invoiceNo = invoiceNo;
		retval.invoiceDate = invoiceDate;
		retval.doNumber = doNumber;
		retval.issueNumber = issueNumber;
		retval.loadGross = loadGross;
		retval.loadTare = loadTare;
		retval.loadNet = net;
		retval.loadGrossTime = loadGrossTime;
		retval.coalValue = coalValue;
		retval.sizing = sizing;
		retval.silo = silo;
		retval.stc = stc;
		retval.royalty = royalty;
		retval.dmf = dmf;
		retval.nmet = nmet;
		retval.sed = 0.0;//sed;
		retval.otherCharges = otherCharges;
		retval.otherPreTax = otherPreTax;
		retval.terminalTax = terminalTax;
		retval.forestCess = forestCess;
		retval.dumping = dumpingCharge;
		retval.avap = avap;
		retval.sadakTax = sadakTax;
		retval.otherPreTax2 = otherPreTax2;
		retval.taxableValue = taxableValue;
		retval.sgst = sgst;
		retval.cgst = cgst;
		retval.igst = igst;
		retval.stateCompCess = stateCompCess;
		retval.grossSaleValue = grossSaleValue;
		retval.doubleField1 = doubleField1;
		retval.doubleField2 = doubleField2;
		retval.doubleField3 = doubleField3;
		retval.vehicleName = tpRecord.getVehicleName();
		retval.otherPostTaxOne = otherPostTax1;
		retval.otherPostTaxTwo = otherPostTax2;
		retval.custGstNo = custDetails == null ? null : custDetails.getGstNo();
		return retval;
	}
//	create table secl_tpr_invoice_apprvd(tpr_id int, invoice_no varchar(32), invoice_date timestamp null default null, do_number varchar(32), issue_number varchar(32), load_gross double, load_tare double, load_net double, load_gross_time timestamp null default null, coal_value double, sizing double, silo double, stc double, royalty double, dmf double, nmet double, sed double, other_pre_tax double, terminal_tax double, forest_cess double, avap double, sadak_tax double, other_pre_tax_2 double, taxable_value double, sgst double, cgst double, igst double, state_comp_cess double, other_charges double, gross_sale_value double, double_field1 double,  double_field2 double, double_field3 double ,updated_on timestamp null default null, updated_by timestamp null default null, primary key(tpr_id) );
//	alter table secl_tpr_invoice add column vehicle_name varchar(24);

	private static String INVOICE_COL_CSV =  "invoice_no,invoice_date, do_number, issue_number, load_gross, load_tare, load_net, load_gross_time, coal_value, sizing, silo, stc, royalty,dmf, nmet, sed,other_pre_tax,terminal_tax,forest_cess,avap,sadak_tax,other_pre_tax_2, taxable_value,sgst,cgst,igst,state_comp_cess,other_charges,gross_sale_value, vehicle_name,other_charges1_post_tax_permt,other_charges2_post_tax_permt,customer_gst_no,dumping_charge,updated_on, tpr_id";
	private static String UPDATE_INVOICE = "update secl_tpr_invoice set invoice_no=?,invoice_date=?, do_number=?, issue_number=?, load_gross=?, load_tare=?, load_net=?, load_gross_time=?, coal_value=?, sizing=?, silo=?, stc=?, royalty=?,dmf=?, nmet=?, sed=?,other_pre_tax=?,terminal_tax=?,forest_cess=?,avap=?,sadak_tax=?,other_pre_tax_2=?, taxable_value=?,sgst=?,cgst=?,igst=?,state_comp_cess=?,other_charges=?,gross_sale_value=?,vehicle_name=?,other_charges1_post_tax_permt=?,other_charges2_post_tax_permt=?,customer_gst_no=?,dumping_charge=?,updated_on=now() where tpr_id=?";
	private static String INSERT_INVOICE = "insert into secl_tpr_invoice ("+INVOICE_COL_CSV+") values " +
			"  (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now(),?)";
	private static String UPDATE_INVOICE_APPRVD = UPDATE_INVOICE.replace("secl_tpr_invoice", "secl_tpr_invoice_apprvd");
	private static String INSERT_INVOICE_APPRVD = INSERT_INVOICE.replace("secl_tpr_invoice", "secl_tpr_invoice_apprvd");
	private static String INSERT_INVOICE_HIST = "insert into secl_tpr_invoice_hist ("+INVOICE_COL_CSV+") (select "+INVOICE_COL_CSV.replaceAll("updated_on", "now()")+" from secl_tpr_invoice where tpr_id=? )";
	private static String UPDATE_TPR_INVOICE_STATUS = "update tp_record set is_invoice_given=? where tpr_id=?";
	private static String UPDATE_TPR_APPRVD_INVOICE_STATUS = UPDATE_TPR_INVOICE_STATUS.replace("tp_record", "tp_record_apprvd");
	private static String FETCH_INVOICE_BY_TPR_ID="select "+INVOICE_COL_CSV+", invoice_locked from secl_tpr_invoice where tpr_id=? order by updated_on desc limit 1";
	
	public void save(Connection conn) throws Exception {
		save(conn, false);
	}
	public void save(Connection conn, boolean isGiven) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			
			for (int art=0;art<2;art++) {
				ps = conn.prepareStatement(art == 0 ? "select tpr_id from secl_tpr_invoice where tpr_id = ?" : "select tpr_id from secl_tpr_invoice_apprvd where tpr_id = ?");
				ps.setInt(1, this.getTprId());
				rs = ps.executeQuery();
				boolean hasRecord = rs.next();
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
				if(art == 0 && hasRecord){
				//	saveAsHist(conn, this.tprId);
					if(!isGiven)
						updateTprInvoiceStatus(conn, tprId, 0);
				}
				ps = conn.prepareStatement(hasRecord ? (art == 0 ? UPDATE_INVOICE : UPDATE_INVOICE_APPRVD): (art == 0 ? INSERT_INVOICE : INSERT_INVOICE_APPRVD));
	//tpr_id, invoice_no,invoice_date, do_number, issue_number, load_gross, load_tare, load_net, load_gross_time, coal_value, sizing, silo, stc, royalty
				//,dmf, nmet, sed,other_pre_tax,terminal_tax,forest_cess,avap,sadak_tax,other_pre_tax_2, taxable_value,sgst,cgst,igst,state_comp_cess,other_charges,gross_sale_value, vehicle_name,updated_on)  
				int colIndex = 1;
				ps.setString(colIndex++,this.invoiceNo);
				ps.setTimestamp(colIndex++,Misc.utilToSqlDate(this.invoiceDate));
				ps.setString(colIndex++,this.doNumber);
				ps.setString(colIndex++,this.issueNumber);
				ps.setDouble(colIndex++,this.loadGross);
				ps.setDouble(colIndex++,this.loadTare);
				ps.setDouble(colIndex++,this.loadNet);
				ps.setTimestamp(colIndex++,Misc.utilToSqlDate(this.loadGrossTime));
				ps.setDouble(colIndex++,this.coalValue);
				ps.setDouble(colIndex++,this.sizing);
				ps.setDouble(colIndex++,this.silo);
				ps.setDouble(colIndex++, this.stc);
				ps.setDouble(colIndex++,this.royalty);
				ps.setDouble(colIndex++,this.dmf);
				ps.setDouble(colIndex++,this.nmet);
				ps.setDouble(colIndex++,0.0);//this.sed);
				ps.setDouble(colIndex++,this.otherPreTax);
				ps.setDouble(colIndex++,this.terminalTax);
				ps.setDouble(colIndex++,this.forestCess);
				ps.setDouble(colIndex++,this.avap);
				ps.setDouble(colIndex++,this.sadakTax);
				ps.setDouble(colIndex++,this.otherPreTax2);
				ps.setDouble(colIndex++,this.taxableValue);
				ps.setDouble(colIndex++,this.sgst);
				ps.setDouble(colIndex++,this.igst);
				ps.setDouble(colIndex++, cgst);
				ps.setDouble(colIndex++,this.stateCompCess);
				ps.setDouble(colIndex++,this.otherCharges);
				ps.setDouble(colIndex++,this.grossSaleValue);
				ps.setString(colIndex++,this.vehicleName);
				Misc.setParamDouble(ps, this.otherPostTaxOne, colIndex++);
				Misc.setParamDouble(ps, this.otherPostTaxTwo, colIndex++);
				ps.setString(colIndex++,this.custGstNo);
				Misc.setParamDouble(ps, this.dumping, colIndex++);
				ps.setInt(colIndex++,this.tprId);
				System.out.println(Thread.currentThread().toString()+"["+DBConnectionPool.getPrintableConnectionStr(conn)+"]"+ps.toString());
				ps.executeUpdate();
				ps = Misc.closePS(ps);
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			
		}
	}
	public static void saveAsHist(Connection conn, int tprId) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(INSERT_INVOICE_HIST);
			ps.setInt(1,tprId);
			System.out.println(Thread.currentThread().toString()+"["+DBConnectionPool.getPrintableConnectionStr(conn)+"]"+ps.toString());
			ps.executeUpdate();
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	public static void updateTprInvoiceStatus(Connection conn, int tprId, int status) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			for (int art=0;art<2;art++) {
				ps = conn.prepareStatement(art == 0 ? UPDATE_TPR_INVOICE_STATUS : UPDATE_TPR_APPRVD_INVOICE_STATUS);
				ps.setInt(1,status);
				ps.setInt(2,tprId);
				System.out.println(Thread.currentThread().toString()+"["+DBConnectionPool.getPrintableConnectionStr(conn)+"]"+ps.toString());
				ps.executeUpdate();
				ps = Misc.closePS(ps);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			
		}
	}
	public String getVehicleName() {
		return vehicleName;
	}
	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	public double getOtherPostTaxOne() {
		return otherPostTaxOne;
	}
	public void setOtherPostTaxOne(double otherPostTaxOne) {
		this.otherPostTaxOne = otherPostTaxOne;
	}
	public double getOtherPostTaxTwo() {
		return otherPostTaxTwo;
	}
	public void setOtherPostTaxTwo(double otherPostTaxTwo) {
		this.otherPostTaxTwo = otherPostTaxTwo;
	}
	
	public static SeclTprInvoice getSeclTPRInvoiceByTPRId(Connection conn, int tprId) throws Exception{
		SeclTprInvoice retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement(FETCH_INVOICE_BY_TPR_ID);
			Misc.setParamInt(ps, tprId, 1);
			rs = ps.executeQuery();
			/*invoice_no,invoice_date, do_number, issue_number, load_gross, load_tare, load_net, load_gross_time, 
			coal_value, sizing, silo, stc, royalty,dmf, nmet, sed,
			other_pre_tax,terminal_tax,forest_cess,avap,sadak_tax,other_pre_tax_2, taxable_value,sgst,
			cgst,igst,state_comp_cess,other_charges,gross_sale_value, vehicle_name,other_charges1_post_tax_permt,other_charges2_post_tax_permt,
			customer_gst_no,dumping_charge,updated_on,
			tpr_id*/
			if(rs.next()){
				int index = 1;
				if(retval == null)
					retval = new SeclTprInvoice();
				retval.invoiceNo = rs.getString(index++);
				retval.invoiceDate = Misc.getDate(rs, index++);
				retval.doNumber = rs.getString(index++);
				retval.issueNumber = rs.getString(index++);
				retval.loadGross = Misc.getRsetDouble(rs, index++);
				retval.loadTare = Misc.getRsetDouble(rs, index++);
				retval.loadNet = Misc.getRsetDouble(rs, index++);
				retval.loadGrossTime = Misc.getDate(rs, index++);
				retval.coalValue = Misc.getRsetDouble(rs, index++);
				retval.sizing = Misc.getRsetDouble(rs, index++);
				retval.silo = Misc.getRsetDouble(rs, index++);
				retval.stc = Misc.getRsetDouble(rs, index++);
				retval.royalty = Misc.getRsetDouble(rs, index++);
				retval.dmf = Misc.getRsetDouble(rs, index++);
				retval.nmet = Misc.getRsetDouble(rs, index++);
				retval.sed = Misc.getRsetDouble(rs, index++);
				retval.otherPreTax = Misc.getRsetDouble(rs, index++);
				retval.terminalTax = Misc.getRsetDouble(rs, index++);
				retval.forestCess = Misc.getRsetDouble(rs, index++);
				retval.avap = Misc.getRsetDouble(rs, index++);
				retval.sadakTax = Misc.getRsetDouble(rs, index++);
				retval.otherPreTax2 = Misc.getRsetDouble(rs, index++);
				retval.taxableValue = Misc.getRsetDouble(rs, index++);
				retval.sgst = Misc.getRsetDouble(rs, index++);
				retval.igst = Misc.getRsetDouble(rs, index++);//its interchanged with igst and cgst
				retval.cgst = Misc.getRsetDouble(rs, index++);//its interchanged with igst and cgst
				retval.stateCompCess = Misc.getRsetDouble(rs, index++);
				retval.otherCharges = Misc.getRsetDouble(rs, index++);
				retval.grossSaleValue = Misc.getRsetDouble(rs, index++);
				retval.vehicleName = rs.getString(index++);
				retval.otherPostTaxOne = Misc.getRsetDouble(rs, index++);
				retval.otherPostTaxTwo = Misc.getRsetDouble(rs, index++);
				retval.custGstNo = rs.getString(index++);
				retval.dumping = Misc.getRsetDouble(rs, index++);
				retval.updatedOn = Misc.getDate(rs, index++);
				retval.tprId = Misc.getRsetInt(rs, index++);
				retval.invoiceLocked = Misc.getRsetInt(rs, index++);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	public static void main(String[] arg){
		Connection conn = null;
		boolean destroyIt = false;
		try{
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			SeclTprInvoice inv = getSeclTPRInvoiceByTPRId(conn, 11901);
			System.out.println(inv.toString());
		}catch(Exception ex){
			ex.printStackTrace();
			destroyIt = true;
		}finally{
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (GenericException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	@Override
	public String toString() {
		return "SeclTprInvoice [tprId=" + tprId + ", invoiceNo=" + invoiceNo + ", invoiceDate=" + invoiceDate
				+ ", doNumber=" + doNumber + ", issueNumber=" + issueNumber + ", loadGross=" + loadGross + ", loadTare="
				+ loadTare + ", loadNet=" + loadNet + ", loadGrossTime=" + loadGrossTime + ", coalValue=" + coalValue
				+ ", sizing=" + sizing + ", silo=" + silo + ", stc=" + stc + ", royalty=" + royalty + ", dmf=" + dmf
				+ ", nmet=" + nmet + ", sed=" + sed + ", otherPreTax=" + otherPreTax + ", terminalTax=" + terminalTax
				+ ", forestCess=" + forestCess + ", dumping=" + dumping + ", avap=" + avap + ", sadakTax=" + sadakTax
				+ ", otherPreTax2=" + otherPreTax2 + ", taxableValue=" + taxableValue + ", sgst=" + sgst + ", cgst="
				+ cgst + ", igst=" + igst + ", stateCompCess=" + stateCompCess + ", otherCharges=" + otherCharges
				+ ", grossSaleValue=" + grossSaleValue + ", doubleField1=" + doubleField1 + ", doubleField2="
				+ doubleField2 + ", doubleField3=" + doubleField3 + ", otherPostTaxOne=" + otherPostTaxOne
				+ ", otherPostTaxTwo=" + otherPostTaxTwo + ", vehicleName=" + vehicleName + ", updatedOn=" + updatedOn
				+ ", updatedBy=" + updatedBy + ", custGstNo=" + custGstNo + "]";
	}
	public double getRate(double charges){
		if(Misc.isUndef(this.loadNet) || this.loadNet <= 0.001 || Misc.isUndef(charges))
			return Misc.getUndefDouble();
		return charges/this.loadNet;
	}
	
	public double getNmetRate(){
		if(Misc.isUndef(this.royalty) || this.royalty <= 0.001)
			return Misc.getUndefDouble();
		return (this.nmet/this.royalty)*100;
	}
	public double getDmfRate(){
		if(Misc.isUndef(this.royalty) || this.royalty <= 0.001)
			return Misc.getUndefDouble();
		return (this.dmf/this.royalty)*100;
		
	}
	public double getDumping() {
		return dumping;
	}
	public void setDumping(double dumping) {
		this.dumping = dumping;
	}
	
	
	public double getTaxRate(double tax){
		if(Misc.isUndef(this.taxableValue) || this.taxableValue <= 0.001 || Misc.isUndef(tax))
			return Misc.getUndefDouble();
		return (tax/this.taxableValue) * 100 ;
	}
	
	public static String getGstPrintData(Connection conn, int workstationType,SECLWorkstationDetails workStation,TPRecord tpr, int cpi, boolean printGeneratedChallanNo,DoDetails doDetails, Mines mines, SeclTprInvoice inv) throws Exception{
		if(doDetails == null || inv == null )
			return null;
		CustomerDetails customer = null;
		String[] consigneeName = null;
		String[] consigneeAddress = null;
		String[] minesAddress = null;
		double doQty = Misc.getUndefDouble();
		double balanceQty = Misc.getUndefDouble();
		double qtyLifted = Misc.getUndefDouble();
		CoalProductDetails coalProduct = null;
		StringBuilder sb = new StringBuilder();
		boolean isEmpty = inv.getLoadNet() <= 0.001 && tpr.getLatestLoadWbOutExit() != null;
		customer = CustomerDetails.getCustomer(conn, doDetails.getCustomerCode(), Misc.getUndefInt());
		consigneeName = InvoicePrintUtils.splitStringByLength(Misc.getParamAsString(customer == null ? null : customer.getName(),""), InvoicePrintUtils.getCharPerLine(cpi)/3-1);
		consigneeAddress = InvoicePrintUtils.splitStringByLength(Misc.getParamAsString(customer == null ? null : customer.getAddress(),""), InvoicePrintUtils.getCharPerLine(cpi)/3-1);
		minesAddress = InvoicePrintUtils.splitStringByLength(Misc.getParamAsString((mines == null ? "" : mines.getAreaDesc()+",")+(mines == null ? "" : mines.getMinesAddress()),""), InvoicePrintUtils.getCharPerLine(cpi)/3-1);
		String wbCode = tpr.getLoadWbOutName();
		LatestDOInfo latestDoInfo = DoDetails.getLatestDOInfo(conn,  doDetails.getDoNumber(), wbCode/*workStation.getCode()*/,true);
		doQty = latestDoInfo.getAllocatedQty();// Misc.isUndef(doDetails.getQtyAlloc(wbCode)) ? 0.0 : doDetails.getQtyAlloc(wbCode);
		balanceQty = latestDoInfo.getRemaingQty();//doDetails.getTotQtyRemaining()+(Misc.isUndef(doDetails.getQtyAlreadyLifted()) ? 0.0 : doDetails.getQtyAlreadyLifted()); //Misc.isUndef(qtyAllocated) ? Misc.getUndefDouble() : qtyAllocated - ((Misc.isUndef(liftedQty) ? 0.0 : liftedQty) + (Misc.isUndef(doDetails.getQtyAlreadyLifted()) ? 0.0 : doDetails.getQtyAlreadyLifted()) );
		qtyLifted = latestDoInfo.getLiftedQty();//Misc.isUndef(doQty) || Misc.isUndef(balanceQty) ? Misc.getUndefDouble() : doQty - balanceQty;
		double liftedAfterMe = DoDetails.getQtyAfterThisTrip(conn, tpr.getVehicleId(), doDetails.getDoNumber(), wbCode, tpr.getLatestLoadWbOutExit() == null ? Misc.getUndefInt() : tpr.getLatestLoadWbOutExit().getTime());
		if(!Misc.isUndef(liftedAfterMe)){
			if(!Misc.isUndef(balanceQty)){
				balanceQty += liftedAfterMe;
			}else{

			}
			if(!Misc.isUndef(qtyLifted)){
				qtyLifted = qtyLifted - liftedAfterMe;
			}else{

			}
		}
		coalProduct = CoalProductDetails.getCoalProduct(conn, doDetails.getMaterial(), Misc.getUndefInt());
		//to do
		String original = "Original for Buyer";
		String duplicate = "Duplicate for Transporter";
		String triplicate = "Triplicate for Supplier";

		sb.append(new char[]{(char)27,'<'});
		sb.append("\r\n");
		String firstLine = InvoicePrintUtils.getAlignStr("SOUTH EASTERN COALFIELDS LIMITED", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi));
		firstLine = firstLine.substring(0, firstLine.length()-original.length())+original;
		String secondLine = InvoicePrintUtils.getAlignStr("CIN:"+InvoicePrintUtils.print(workStation.getCoorporateIdentityNumber()), InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi));
		secondLine = secondLine.substring(0, secondLine.length()-duplicate.length())+duplicate;
		String thirdLine = InvoicePrintUtils.getAlignStr("Tax Invoice(under section 31 of CGST Act)", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi));
		thirdLine = thirdLine.substring(0, thirdLine.length()-triplicate.length())+triplicate;
		sb.append(firstLine+"\n");
		sb.append(secondLine+"\n");
		sb.append(thirdLine+"\n");
		sb.append("\n");
		sb.append(InvoicePrintUtils.getAlignStr("Weighment Serial No. : ", InvoicePrintUtils.Alignment.Left));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(tpr.getChallanNo()), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)-(23+15+3+10)));
		sb.append(InvoicePrintUtils.getAlignStr("Weighbridge No.", InvoicePrintUtils.Alignment.Right,15));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(workStation.getCode()), InvoicePrintUtils.Alignment.Left,10)+"\n");

		sb.append(InvoicePrintUtils.getline(cpi));

		sb.append(InvoicePrintUtils.getAlignStr("Name & Address of Mine:", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)/3));
		sb.append(InvoicePrintUtils.getAlignStr("Buyer:", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)/3));
		sb.append(InvoicePrintUtils.getAlignStr("Invoice#:", InvoicePrintUtils.Alignment.Left)+"\n");


		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(mines.getName()), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)/3));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(InvoicePrintUtils.getStringAt(consigneeName, 0)), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)/3));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(tpr.getInvoiceNumber()), InvoicePrintUtils.Alignment.Left)+"\n");

		Date now = inv.getInvoiceDate(); //new Date(System.currentTimeMillis());
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(InvoicePrintUtils.getStringAt(minesAddress, 0)), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)/3));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(InvoicePrintUtils.getStringAt(consigneeAddress, 0)), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)/3));
		sb.append(InvoicePrintUtils.getAlignStr("Date:"+InvoicePrintUtils.getDateStr(now)+" Time:"+InvoicePrintUtils.getTimeStr(now), InvoicePrintUtils.Alignment.Left)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(InvoicePrintUtils.getStringAt(minesAddress, 1)), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)/3));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(InvoicePrintUtils.getStringAt(consigneeAddress, 1)), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)/3));
		sb.append(InvoicePrintUtils.getAlignStr("ARV#:"+InvoicePrintUtils.print(doDetails.getArvId()), InvoicePrintUtils.Alignment.Left)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr("GSTN:"+InvoicePrintUtils.print(mines.getGstNo()), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)/3));
		sb.append(InvoicePrintUtils.getAlignStr("GSTN:"+InvoicePrintUtils.print(customer.getGstNo()), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)/3));
		sb.append(InvoicePrintUtils.getAlignStr("Mining Pass#:"+InvoicePrintUtils.print(printGeneratedChallanNo ? tpr.getChallanNo() : Misc.getParamAsString(Utils.isNull(tpr.getLrNo()) ? "" : tpr.getLrNo().replaceAll("#@", ""),"")), InvoicePrintUtils.Alignment.Left)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr("State Code:"+InvoicePrintUtils.print(mines.getStateGstCode()), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)/3));
		sb.append(InvoicePrintUtils.getAlignStr("State Code:"+InvoicePrintUtils.print(customer.getStateGstCode()), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)/3));
		sb.append(InvoicePrintUtils.getAlignStr("Reverse Charge:No", InvoicePrintUtils.Alignment.Left)+"\n");


		/*		sb.append(InvoicePrintUtils.getAlignStr("Consignee:", InvoicePrintUtils.Alignment.Left));
		if(Utils.isNull(doDetails.getConsginee())){
			sb.append("Same as buyer.");
		}else{
			sb.append(InvoicePrintUtils.print(doDetails.getConsginee()));
			sb.append(",");
			sb.append(InvoicePrintUtils.print(doDetails.getConsgineeAddress()));
			sb.append(",State Code:");
			sb.append(InvoicePrintUtils.print(doDetails.getConsgineeState()));
		}
		sb.append("\n");*/
		sb.append(InvoicePrintUtils.getline(cpi));
		sb.append(InvoicePrintUtils.getAlignStr("DO No. & Date", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi)/6));
		sb.append(InvoicePrintUtils.getAlignStr("DO Valid", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi)/6));
		sb.append(InvoicePrintUtils.getAlignStr("Delivery", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi)/6));
		sb.append(InvoicePrintUtils.getAlignStr("DO Qty", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi)/6));
		sb.append(InvoicePrintUtils.getAlignStr("Balance Qty", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi)/6));
		sb.append(InvoicePrintUtils.getAlignStr("Customer Code", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi)/6)+"\n");

		sb.append(new char[]{(char)27,'E'}).append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(doDetails.getDoNumber()), InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi)/6)).append(new char[]{(char)27,'F'});
		sb.append(InvoicePrintUtils.getAlignStr("Upto", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi)/6));
		sb.append(InvoicePrintUtils.getAlignStr("Point", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi)/6));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(doQty)+"MT", InvoicePrintUtils.Alignment.Center,(InvoicePrintUtils.getCharPerLine(cpi)/6)));
		sb.append(InvoicePrintUtils.getAlignStr("to be lifted", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi)/6));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(customer != null ? customer.getSapCode() : null), InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi)/6)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(InvoicePrintUtils.getDateStr(doDetails.getDoDate())), InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi)/6));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(InvoicePrintUtils.getDateStr(doDetails.getValidityDate())), InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi)/6));
		sb.append(InvoicePrintUtils.getAlignStr(Misc.getParamAsString(doDetails.getDeliveryPoint(),""), InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.getCharPerLine(cpi)/6));
		sb.append(InvoicePrintUtils.getAlignStr(Utils.isNull(doDetails.getDoReleaseNo())? "" : "Issue No:", InvoicePrintUtils.Alignment.Center));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(doDetails.getDoReleaseNo()), InvoicePrintUtils.Alignment.Center,(InvoicePrintUtils.getCharPerLine(cpi)/6)-9));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(balanceQty)+"MT", InvoicePrintUtils.Alignment.Center,(InvoicePrintUtils.getCharPerLine(cpi)/6)));
		sb.append(InvoicePrintUtils.getAlignStr("Prog.", InvoicePrintUtils.Alignment.Left));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(qtyLifted)+"MT", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)/6-5)+"\n");

		sb.append(InvoicePrintUtils.getline(cpi));

		sb.append(InvoicePrintUtils.getAlignStr("Destination(Consignee)", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		//sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(destinationPair == null || Utils.isNull(destinationPair.second) ? tpr.getDestinationCode() : destinationPair.second), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getRightHalfWidth(cpi))+"\n");
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(!Utils.isNull(tpr.getDestinationCode()) ? InvoicePrintUtils.print(tpr.getDestinationCode()) : InvoicePrintUtils.print(doDetails.getDestinationCode()))+" "+InvoicePrintUtils.print(!Utils.isNull(tpr.getDestinationStateCode()) ? InvoicePrintUtils.print(tpr.getDestinationStateCode()) : InvoicePrintUtils.print(doDetails.getConsgineeState())), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getRightHalfWidth(cpi))+"\n");

		sb.append(InvoicePrintUtils.getAlignStr("Specification of Goods", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(coalProduct == null ? "Bituminous Coal" : coalProduct.getName()),Alignment.Left)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr("HSN Code", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(coalProduct == null || Utils.isNull(coalProduct.getHsnCode()) ? "27011200" : coalProduct.getHsnCode()), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getRightHalfWidth(cpi))+"\n");

		sb.append(InvoicePrintUtils.getAlignStr("Size", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(Misc.getParamAsString(doDetails.getCoalSize(),"")), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getRightHalfWidth(cpi))+"\n");

		sb.append(InvoicePrintUtils.getAlignStr("Grade", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(!Utils.isNull(tpr.getGradeCode()) ? tpr.getGradeCode() : doDetails.getGradeCode()), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getRightHalfWidth(cpi))+"\n");

		sb.append(InvoicePrintUtils.getAlignStr("Type Of Consumer", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(Type.MinesDoDetails.TypeOfConsumer.getString(doDetails.getTypeOfConsumer())), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getRightHalfWidth(cpi))+"\n");

		sb.append(InvoicePrintUtils.getAlignStr("Gross Weight", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(new char[]{(char)27,'E'});
		sb.append(isEmpty ? "EMPTY" : InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getLoadGross())+"MT", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.docAmountWidth));
		sb.append(new char[]{(char)27,'F'});
		sb.append(InvoicePrintUtils.getAlignStr(" Date", InvoicePrintUtils.Alignment.Left));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(InvoicePrintUtils.getDateStr(inv.getLoadGrossTime())), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.docDateWidth));
		sb.append(InvoicePrintUtils.getAlignStr(" Time", InvoicePrintUtils.Alignment.Left));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(InvoicePrintUtils.getTimeStr(inv.getLoadGrossTime())), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.docTimeWidth)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr("Tare Weight", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(new char[]{(char)27,'E'});
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getLoadTare())+"MT", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.docAmountWidth));
		sb.append(new char[]{(char)27,'F'});
		sb.append(InvoicePrintUtils.getAlignStr(" Date", InvoicePrintUtils.Alignment.Left));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(InvoicePrintUtils.getDateStr(tpr.getLatestLoadWbInExit())), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.docDateWidth));
		sb.append(InvoicePrintUtils.getAlignStr(" Time", InvoicePrintUtils.Alignment.Left));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(InvoicePrintUtils.getTimeStr(tpr.getLatestLoadWbInExit())), InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.docTimeWidth)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr("Net Weight", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(new char[]{(char)27,'E'});
		sb.append(isEmpty ? "EMPTY" : InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getLoadNet())+"MT", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.docAmountWidth));
		sb.append(new char[]{(char)27,'F'});
		sb.append("\n");

		sb.append(InvoicePrintUtils.getAlignStr("Rate", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getCoalValue())), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth));
		sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr(" 1. Coal Value", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getCoalValue()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr(" 2. Sizing Charges", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-12));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getSizing())), InvoicePrintUtils.Alignment.Left,7));
		sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getSizing()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr(" 3. SILO Charges", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-12));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getSilo())), InvoicePrintUtils.Alignment.Left,7));
		sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,5));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getSilo()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr(" 4. STC Charge", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-12));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getStc())), InvoicePrintUtils.Alignment.Left,7));
		sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,5));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getStc()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr(" 5. Dumping Charge", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-12));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getDumping())), InvoicePrintUtils.Alignment.Left,7));
		sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,5));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getDumping()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr(" 6. Royalty", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-12));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getRoyalty())), InvoicePrintUtils.Alignment.Left,7));
		sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,5));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRoyalty()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr(" 7. DMF", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(2+5+16)));
		sb.append(InvoicePrintUtils.getAlignStr("@ ", InvoicePrintUtils.Alignment.Left,2));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getDmfRate()), InvoicePrintUtils.Alignment.Left,5));
		sb.append(InvoicePrintUtils.getAlignStr(" % of 6 above", InvoicePrintUtils.Alignment.Left,16));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getDmf()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr(" 8. NMET", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(2+5+16)));
		sb.append(InvoicePrintUtils.getAlignStr("@ ", InvoicePrintUtils.Alignment.Left,2));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getNmetRate()), InvoicePrintUtils.Alignment.Left,5));
		sb.append(InvoicePrintUtils.getAlignStr(" % of 6 above", InvoicePrintUtils.Alignment.Left,16));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getNmet()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr(" 9. Stowing Excise Duty", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(5+5+2)));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getSed())), InvoicePrintUtils.Alignment.Left,7));
		sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,5));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getSed()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr("10. Terminal Tax", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(5+5+2)));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getTerminalTax())), InvoicePrintUtils.Alignment.Left,7));
		sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,5));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getTerminalTax()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr("11. Forest Cess", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(5+5+2)));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getForestCess())), InvoicePrintUtils.Alignment.Left,7));
		sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,5));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getForestCess()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");

		/*sb.append(InvoicePrintUtils.getAlignStr("12. MP Sadak Tax", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(2+5+16)));
		sb.append(InvoicePrintUtils.getAlignStr("@ ", InvoicePrintUtils.Alignment.Left,2));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(sadakTaxRate), InvoicePrintUtils.Alignment.Left,5));
		sb.append(InvoicePrintUtils.getAlignStr(" % of 1 above", InvoicePrintUtils.Alignment.Left,16));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(sadakTax), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");*/
		sb.append(InvoicePrintUtils.getAlignStr("12. MP Sadak Tax", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(2+5+5)));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getSadakTax())), InvoicePrintUtils.Alignment.Left,7));
		sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,5));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getSadakTax()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");

		sb.append(InvoicePrintUtils.getAlignStr("13. CG Paryavaran & Vikas Upkar", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(5+5+2)));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getAvap())), InvoicePrintUtils.Alignment.Left,7));
		sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,5));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getAvap()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");

		int itemCount = 13;
		if(!Misc.isUndef(inv.getOtherPreTax())){
			itemCount++;
			sb.append(InvoicePrintUtils.getAlignStr((itemCount)+". Pre Charges One", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(2+5+5)));
			sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getOtherPreTax())), InvoicePrintUtils.Alignment.Left,7));
			sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,5));
			sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
			sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getOtherPreTax()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");
		}

		if(!Misc.isUndef(inv.getOtherPreTax2())){
			itemCount++;
			sb.append(InvoicePrintUtils.getAlignStr((itemCount)+". Pre Charges Two", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(2+5+5)));
			sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getOtherPreTax2())), InvoicePrintUtils.Alignment.Left,7));
			sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,5));
			sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
			sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getOtherPreTax2()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");
		}
		
		if(!Misc.isUndef(inv.getOtherCharges())){
			itemCount++;
			sb.append(InvoicePrintUtils.getAlignStr((itemCount)+". Other Charges", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(2+5+5)));
			sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getOtherCharges())), InvoicePrintUtils.Alignment.Left,7));
			sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,5));
			sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
			sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getOtherCharges()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");
		}

		sb.append("\n");
		sb.append(InvoicePrintUtils.getAlignStr("Taxable Value(A) [1 to "+itemCount+"]", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(7)));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getTaxableValue()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth));
		sb.append(InvoicePrintUtils.getAlignStr("("+InvoicePrintUtils.print(NumberToWordsConverter.Convert((int)inv.getTaxableValue()))+")", InvoicePrintUtils.Alignment.Left));
		sb.append("\n");

		sb.append(InvoicePrintUtils.getAlignStr(" S.G.S.T", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(4+10+7)));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.printDoubleOneDecimal(inv.getTaxRate(inv.getSgst()),0.0), InvoicePrintUtils.Alignment.Right,4));
		sb.append(InvoicePrintUtils.getAlignStr(" % on (A)", InvoicePrintUtils.Alignment.Left,10));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getSgst()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth));
		sb.append(InvoicePrintUtils.getAlignStr("("+InvoicePrintUtils.print(NumberToWordsConverter.Convert((int)inv.getSgst()))+")", InvoicePrintUtils.Alignment.Left));
		sb.append("\n");

		sb.append(InvoicePrintUtils.getAlignStr(" C.G.S.T", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(4+10+7)));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.printDoubleOneDecimal(inv.getTaxRate(inv.getCgst()),0.0), InvoicePrintUtils.Alignment.Right,4));
		sb.append(InvoicePrintUtils.getAlignStr(" % on (A)", InvoicePrintUtils.Alignment.Left,10));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getCgst()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth));
		sb.append(InvoicePrintUtils.getAlignStr("("+InvoicePrintUtils.print(NumberToWordsConverter.Convert((int)inv.getCgst()))+")", InvoicePrintUtils.Alignment.Left));
		sb.append("\n");

		sb.append(InvoicePrintUtils.getAlignStr(" I.G.S.T", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(4+10+7)));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.printDoubleOneDecimal(inv.getTaxRate(inv.getIgst()),0.0), InvoicePrintUtils.Alignment.Right,4));
		sb.append(InvoicePrintUtils.getAlignStr(" % on (A)", InvoicePrintUtils.Alignment.Left,10));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getIgst()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth));
		sb.append(InvoicePrintUtils.getAlignStr("("+InvoicePrintUtils.print(NumberToWordsConverter.Convert((int)inv.getIgst()))+")", InvoicePrintUtils.Alignment.Left));
		sb.append("\n");

		sb.append(InvoicePrintUtils.getAlignStr(" State Compensation cess", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(4+10+7)));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getStateCompCess())), InvoicePrintUtils.Alignment.Right,7));
		sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,7));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getStateCompCess()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth));
		sb.append(InvoicePrintUtils.getAlignStr("("+InvoicePrintUtils.print(NumberToWordsConverter.Convert((int)inv.getStateCompCess()))+")", InvoicePrintUtils.Alignment.Left));
		sb.append("\n");
		if(!Misc.isUndef(inv.getOtherPostTaxOne())){
			itemCount++;
			sb.append(InvoicePrintUtils.getAlignStr((itemCount)+". post Charges One", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(2+5+5)));
			sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getOtherPostTaxOne())), InvoicePrintUtils.Alignment.Left,7));
			sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,5));
			sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
			sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getOtherPostTaxOne()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");
		}

		if(!Misc.isUndef(inv.getOtherPostTaxTwo())){
			itemCount++;
			sb.append(InvoicePrintUtils.getAlignStr((itemCount)+". post Charges Two", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-(2+5+5)));
			sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getRate(inv.getOtherPostTaxTwo())), InvoicePrintUtils.Alignment.Left,7));
			sb.append(InvoicePrintUtils.getAlignStr(" / MT", InvoicePrintUtils.Alignment.Left,5));
			sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
			sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getOtherPostTaxTwo()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth)+"\n");
		}
		sb.append(InvoicePrintUtils.getAlignStr(" Gross Sale Value", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getLeftHalfWidth(cpi)-7));
		sb.append(InvoicePrintUtils.getAlignStr(":", InvoicePrintUtils.Alignment.Center,InvoicePrintUtils.docColonWith));
		sb.append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(inv.getGrossSaleValue()), InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.docAmountWidth));
		sb.append(InvoicePrintUtils.getAlignStr("("+InvoicePrintUtils.print(com.ipssi.rfid.processor.NumberToWordsConverter.Convert((int)inv.getGrossSaleValue()))+")", InvoicePrintUtils.Alignment.Left));
		sb.append("\n");

		sb.append(InvoicePrintUtils.getline(cpi));
		sb.append(InvoicePrintUtils.getAlignStr("**Certified that particulars given above are true and correct. ", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi) - (16+16)));
		sb.append(InvoicePrintUtils.getAlignStr("Truck No.", InvoicePrintUtils.Alignment.Center,16));
		sb.append(InvoicePrintUtils.getAlignStr("For "+mines.getProjectName(), InvoicePrintUtils.Alignment.Center,16)+"\n");
		sb.append(InvoicePrintUtils.getAlignStr(" ", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi) - (16+16)));
		sb.append(new char[]{(char)27,'E'}).append(InvoicePrintUtils.getAlignStr(InvoicePrintUtils.print(Misc.getParamAsString(tpr.getVehicleName(),"")), InvoicePrintUtils.Alignment.Center,16)).append(new char[]{(char)27,'F'});
		sb.append(InvoicePrintUtils.getAlignStr("project", InvoicePrintUtils.Alignment.Right,16)+"\n");
		sb.append("\n");
		sb.append("\n");
		sb.append(InvoicePrintUtils.getAlignStr("(Verified By)", InvoicePrintUtils.Alignment.Left,InvoicePrintUtils.getCharPerLine(cpi)/2));
		sb.append(InvoicePrintUtils.getAlignStr("(Authorised Signatory)", InvoicePrintUtils.Alignment.Right,InvoicePrintUtils.getCharPerLine(cpi)/2)+"\n");
		sb.append(InvoicePrintUtils.getline(cpi));
		return sb.toString();
	}
}
