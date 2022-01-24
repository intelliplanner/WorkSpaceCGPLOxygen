package com.ipssi.rfid.ui.secl.controller;

import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.DoDetails;
import com.ipssi.rfid.beans.Mines;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.secl.data.LovDao;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;
import com.ipssi.rfid.ui.secl.data.LovDao.LovItemType;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterAttributes;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.transform.Scale;

public class PrintDocController implements ControllerI,Initializable{
    @FXML
    private Label textArea;
    @FXML
    private Label textSubAreaName;
    @FXML
    private Label textSlipName;
    @FXML
    private Label textDate;
    @FXML
    private Label textVehicleName;
    @FXML
    private Label textChallanNo;
    @FXML
    private Label textCustomerCode;
    @FXML
    private Label textProductCode;
    @FXML
    private Label textSourceCode;
    @FXML
    private Label textDestinationCode;
    @FXML
    private Label textTransporterCode;
    @FXML
    private Label textGross;
    @FXML
    private Label textTare;
    @FXML
    private Label textTareDate;
    @FXML
    private Label textNet;
    @FXML
    private Label textCustomerName;
    @FXML
    private Label textProductName;
    @FXML
    private Label textScourceName;
    @FXML
    private Label textDestinationName;
    @FXML
    private Label textTransporterName;
    @FXML
    private Label textGrossDate;
    @FXML
    private Label textTime;
    @FXML
    private Label textSerialNo;
    @FXML
    private Label textGrossTime;
    @FXML
    private Label textTareTime;
    @FXML
    private Label labelDoNo;
    @FXML
    private Label textDONo;
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	MainController parent;
	Node rootView;
	MenuItemInfo menuItemInfo;
	@Override
	public void init(MainController parent, Parent rootView, MenuItemInfo menuItemInfo) {
		// TODO Auto-generated method stub
		this.parent = parent;
		this.rootView = rootView;
		this.menuItemInfo = menuItemInfo;
	}

	@Override
	public void clearInputs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void populateTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep, Vehicle vehicle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVehicleName(String vehicleName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearVehicleName() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setTPRAndSaveNonTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void enableManualEntry(boolean enable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Pair<Boolean, String> requestFocusNextField(NodeExt currentField) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPrintable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hideActionBar() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isManualEntry() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean print(TPRecord tpRecord, int workstationType) {
		// TODO Auto-generated method stub
		Connection conn = null;
		boolean destroyIt = false;
		boolean retval = false;
		try{
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
			labelDoNo.setVisible(false);
			Mines mines = Mines.getMines(conn, tpRecord.getMinesCode(), Misc.getUndefInt());
			Pair<String,String> sourcePair = LovDao.getAutocompletePrintablePair(portNodeId, tpRecord.getMinesCode(), LovItemType.MINES);
			Pair<String,String> areaPair = LovDao.getAutocompletePrintablePair(portNodeId, mines.getParentAreaCode(), LovItemType.AREA);
			Pair<String,String> subAreaPair = LovDao.getAutocompletePrintablePair(portNodeId, mines.getParentSubAreaCode(), LovItemType.SUB_AREA);
			String areaName = areaPair == null ? "" : areaPair.second;
			String subAreaName = subAreaPair == null ? "" : subAreaPair.second;
			String slipName = "";
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat stf = new SimpleDateFormat("HH:mm");
			String dateStr = sdf.format(date);
			String timeStr = stf.format(date);
			String vehicleName = tpRecord.getVehicleName();
			String slipSerialNo = tpRecord.getTprId()+"";
			String challanNo = Misc.getParamAsString(tpRecord.getChallanNo());
			String doNumber = Misc.getParamAsString(tpRecord.getDoNumber(),"");
			Pair<String,String> transporterPair = LovDao.getAutocompletePrintablePair(portNodeId, tpRecord.getTransporterCode(), LovItemType.TRANSPORTER);
			Pair<String,String> gradePair = LovDao.getAutocompletePrintablePair(portNodeId, tpRecord.getGradeCode(), LovItemType.MATERIAL_GRADE);
			Pair<String,String> destinationPair = null;
			Pair<String,String> customerPair = null;
			double tare = 0.0;
			double gross = 0.0;
			double net = 0.0;
			String grossDateStr = "";
			String grossTimeStr = "";
			String tareDateStr = "";
			String tareTimeStr = "";
			if(workstationType == Type.WorkStationType.SECL_LOAD_INT_WB_GROSS || workstationType == Type.WorkStationType.SECL_LOAD_ROAD_WB_GROSS){
				if(workstationType == Type.WorkStationType.SECL_LOAD_INT_WB_GROSS){
					slipName = "Internal Shiting Slip";
					destinationPair = LovDao.getAutocompletePrintablePair(portNodeId, tpRecord.getDestinationCode(), LovItemType.SIDING);
				}else {
					labelDoNo.setVisible(true);
					slipName = "Road Weighment Slip";
					DoDetails doDetails = DoDetails.getDODetails(conn, doNumber, Misc.getUndefInt());
					if(doDetails != null){
						customerPair = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getCustomerCode(), LovItemType.CUSTOMER);
						destinationPair = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getDestinationCode(), LovItemType.ROAD_DEST);
						gradePair = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getGradeCode(), LovItemType.MATERIAL_GRADE);
					}
				}
				tare = tpRecord.getLoadTare();
				gross = tpRecord.getLoadGross();
				net = gross - tare;
				grossDateStr = sdf.format(tpRecord.getLatestLoadWbOutExit());
				grossTimeStr = stf.format(tpRecord.getLatestLoadWbOutExit());
				tareDateStr = sdf.format(tpRecord.getLatestLoadWbInExit());
				tareTimeStr = stf.format(tpRecord.getLatestLoadWbInExit());
			}else if(workstationType == Type.WorkStationType.SECL_UNLOAD_INT_WB_GROSS){
				slipName = "Siding Weighment Slip";
				destinationPair = LovDao.getAutocompletePrintablePair(portNodeId, tpRecord.getDestinationCode(), LovItemType.SIDING);
				tare = tpRecord.getUnloadTare();
				gross = tpRecord.getUnloadGross();
				net = gross - tare;
				grossDateStr = sdf.format(tpRecord.getLatestUnloadWbOutExit());
				grossTimeStr = stf.format(tpRecord.getLatestUnloadWbOutExit());
				tareDateStr = sdf.format(tpRecord.getLatestUnloadWbInExit());
				tareTimeStr = stf.format(tpRecord.getLatestUnloadWbInExit());
			}
			textArea.setText(Misc.getParamAsString(areaName,""));
			textSubAreaName.setText(Misc.getParamAsString(subAreaName,""));
			textDate.setText(Misc.getParamAsString(dateStr,""));
			textTime.setText(Misc.getParamAsString(timeStr,""));
			textSlipName.setText(Misc.getParamAsString(slipName,""));
			textVehicleName.setText(Misc.getParamAsString(vehicleName,""));
			textSerialNo.setText(Misc.getParamAsString(slipSerialNo,""));
			textChallanNo.setText(Misc.getParamAsString(challanNo,""));
			textDONo.setText(Misc.getParamAsString(doNumber,""));
			textTransporterCode.setText(Misc.getParamAsString(transporterPair == null ? "" : transporterPair.first,""));
			textTransporterName.setText(Misc.getParamAsString(transporterPair == null ? "" : transporterPair.second,""));
			textCustomerCode.setText(Misc.getParamAsString(customerPair == null ? "" : customerPair.first,""));
			textCustomerName.setText(Misc.getParamAsString(customerPair == null ? "" : customerPair.second,""));
			textProductCode.setText(Misc.getParamAsString(gradePair == null ? "" : gradePair.first,""));
			textProductName.setText(Misc.getParamAsString(gradePair == null ? "" : gradePair.second,""));
			textSourceCode.setText(Misc.getParamAsString(sourcePair == null ? "" : sourcePair.first,""));
			textScourceName.setText(Misc.getParamAsString(sourcePair == null ? "" : sourcePair.second,""));
			textDestinationCode.setText(Misc.getParamAsString(destinationPair == null ? "" : destinationPair.first,""));
			textDestinationName.setText(Misc.getParamAsString(destinationPair == null ? "" : destinationPair.second,""));
			textGross.setText(Misc.getParamAsString(gross+"",""));
			textGrossDate.setText(Misc.getParamAsString(grossDateStr,""));
			textGrossTime.setText(Misc.getParamAsString(grossTimeStr,""));
			textTare.setText(Misc.getParamAsString(tare+"",""));
			textTareDate.setText(Misc.getParamAsString(tareDateStr,""));
			textTareTime.setText(Misc.getParamAsString(tareTimeStr,""));
			textNet.setText(Misc.getParamAsString(net+"",""));
			Printer printer = Printer.getDefaultPrinter();
			PageLayout pageLayout= printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
			PrinterAttributes attr = printer.getPrinterAttributes();
			PrinterJob job = PrinterJob.createPrinterJob();
			double scaleX
			= pageLayout.getPrintableWidth() / rootView.getBoundsInParent().getWidth();
			double scaleY
			= pageLayout.getPrintableHeight() / rootView.getBoundsInParent().getHeight();

			Scale scale = new Scale(scaleX > 1.0 ? 1.0 : scaleX, scaleY > 1.0 ? 1.0 : scaleY);
			rootView.getTransforms().add(scale);
			if (job != null ) {
				boolean success = job.printPage(pageLayout, rootView);
				if (success) {
					job.endJob();

				}
			}
			rootView.getTransforms().remove(scale);
			retval = true;
		}catch(Exception ex){
			ex.printStackTrace();
			destroyIt = true;
		}finally{
			try{
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return retval;
	}

	@Override
	public HashMap<Integer, Integer> getBlockingQuestions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getInstruction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWeighBridgeReading(String reading) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TPRWorkstationConfig getWorkstationConfig(Connection conn, int readerId, Vehicle veh) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleAutoComplete(NodeExt nodeExt, Pair<String, String> codeNamePair) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetWeighmentMode() {
		// TODO Auto-generated method stub
		
	}
	
}
