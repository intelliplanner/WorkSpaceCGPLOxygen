package com.ipssi.rfid.ui.secl.controller;

import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.json.simple.JSONObject;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.DoDetails;
import com.ipssi.rfid.beans.Mines;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.secl.data.LovDao;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;
import com.ipssi.rfid.ui.secl.data.LovDao.LovItemType;

import javafx.application.Platform;
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
import javafx.scene.transform.Scale;
import javafx.scene.web.WebView;

public class PrintController implements ControllerI,Initializable {

    @FXML
    private WebView webView;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		Platform.runLater(()->{
			String url = PrintController.class.getResource(ScreenConstant.BASE+"secl_recipt.html").toExternalForm();  
	        webView.getEngine().load(url);
		});
		
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
		return true;
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
			Mines mines = Mines.getMines(conn, tpRecord.getMinesCode(), Misc.getUndefInt());
			Pair<String,String> sourcePair = LovDao.getAutocompletePrintablePair(portNodeId, tpRecord.getMinesCode(), LovItemType.MINES);
			Pair<String,String> areaPair = mines != null ? LovDao.getAutocompletePrintablePair(portNodeId, mines.getParentAreaCode(), LovItemType.AREA) : null;
			Pair<String,String> subAreaPair = mines != null ? LovDao.getAutocompletePrintablePair(portNodeId, mines.getParentSubAreaCode(), LovItemType.SUB_AREA) : null;
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
				grossDateStr = tpRecord.getLatestLoadWbOutExit() != null ? sdf.format(tpRecord.getLatestLoadWbOutExit()) : "";
				grossTimeStr = tpRecord.getLatestLoadWbOutExit() != null ? stf.format(tpRecord.getLatestLoadWbOutExit()) : "";
				tareDateStr = tpRecord.getLatestLoadWbInExit() != null ? sdf.format(tpRecord.getLatestLoadWbInExit()) : "";
				tareTimeStr = tpRecord.getLatestLoadWbInExit() != null ? stf.format(tpRecord.getLatestLoadWbInExit()) : "";
			}else if(workstationType == Type.WorkStationType.SECL_UNLOAD_INT_WB_GROSS){
				slipName = "Siding Weighment Slip";
				destinationPair = LovDao.getAutocompletePrintablePair(portNodeId, tpRecord.getDestinationCode(), LovItemType.SIDING);
				tare = tpRecord.getUnloadTare();
				gross = tpRecord.getUnloadGross();
				net = gross - tare;
				grossDateStr = tpRecord.getLatestUnloadWbInExit() != null ? sdf.format(tpRecord.getLatestUnloadWbInExit()) : "";
				grossTimeStr = tpRecord.getLatestUnloadWbInExit() != null ? stf.format(tpRecord.getLatestUnloadWbInExit()) : "";
				tareDateStr = tpRecord.getLatestUnloadWbOutExit() != null ? sdf.format(tpRecord.getLatestUnloadWbOutExit()) : "";
				tareTimeStr = tpRecord.getLatestUnloadWbOutExit() != null ? stf.format(tpRecord.getLatestUnloadWbOutExit()) : "";
			}
			JSONObject data = new JSONObject();
			data.put("area", Misc.getParamAsString(areaName,""));
			data.put("subArea", Misc.getParamAsString(subAreaName,""));
			data.put("slipName",Misc.getParamAsString(slipName,"") );
			data.put("date",Misc.getParamAsString(dateStr,"") );
			data.put("time", Misc.getParamAsString(timeStr,""));
			data.put("vehicleName", Misc.getParamAsString(vehicleName,""));
			data.put("slipNo", Misc.getParamAsString(slipSerialNo,""));
			data.put("challanNo", Misc.getParamAsString(challanNo,""));
			data.put("doNumber", Misc.getParamAsString(doNumber,""));
			data.put("customerCode", Misc.getParamAsString(customerPair == null ? "" : customerPair.first,""));
			data.put("customerName", Misc.getParamAsString(customerPair == null ? "" : customerPair.second,""));
			data.put("gradeCode", Misc.getParamAsString(gradePair == null ? "" : gradePair.first,""));
			data.put("gradeName", Misc.getParamAsString(gradePair == null ? "" : gradePair.second,""));
			data.put("sourceCode", Misc.getParamAsString(sourcePair == null ? "" : sourcePair.first,""));
			data.put("sourceName", Misc.getParamAsString(sourcePair == null ? "" : sourcePair.second,""));
			data.put("destinationCode", Misc.getParamAsString(destinationPair == null ? "" : destinationPair.first,""));
			data.put("destinationName", Misc.getParamAsString(destinationPair == null ? "" : destinationPair.second,""));
			data.put("transporterCode", Misc.getParamAsString(transporterPair == null ? "" : transporterPair.first,""));
			data.put("transporterName", Misc.getParamAsString(transporterPair == null ? "" : transporterPair.second,""));
			data.put("gross",Misc.getParamAsString(gross+"","") );
			data.put("grossDate", Misc.getParamAsString(grossDateStr,""));
			data.put("grossTime", Misc.getParamAsString(grossTimeStr,""));
			data.put("tare", Misc.getParamAsString(tare+"",""));
			data.put("tareDate", Misc.getParamAsString(tareDateStr,""));
			data.put("tareTime", Misc.getParamAsString(tareTimeStr,""));
			data.put("net", Misc.getParamAsString(net+"",""));
	        webView.getEngine().executeScript("init('"+data.toJSONString()+"')");
			Printer printer = Printer.getDefaultPrinter();
			PageLayout pageLayout= printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
			PrinterAttributes attr = printer.getPrinterAttributes();
			PrinterJob job = PrinterJob.createPrinterJob();
			double scaleX
			= pageLayout.getPrintableWidth() / webView.getBoundsInParent().getWidth();
			double scaleY
			= pageLayout.getPrintableHeight() / webView.getBoundsInParent().getHeight();

			Scale scale = new Scale(scaleX > 1.0 ? 1.0 : scaleX, scaleY > 1.0 ? 1.0 : scaleY);
			webView.getTransforms().add(scale);
			if (job != null ) {
				boolean success = job.printPage(pageLayout, webView);
				if (success) {
					job.endJob();

				}
			}
			webView.getTransforms().remove(scale);
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
