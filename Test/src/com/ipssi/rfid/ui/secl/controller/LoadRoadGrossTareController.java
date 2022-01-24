package com.ipssi.rfid.ui.secl.controller;

import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.ResourceBundle;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.TimePeriodHelper;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.DoDetails;
import com.ipssi.rfid.beans.DoDetails.LatestDOInfo;
import com.ipssi.rfid.beans.TPRTareDetails;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPRecord.WeighmentStep;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.beans.VehicleExtended;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.custom.Printer;
import com.ipssi.rfid.ui.secl.data.LovDao;
import com.ipssi.rfid.ui.secl.data.LovDao.LovItemType;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;
import com.ipssi.rfid.ui.secl.data.TPRDataUtils;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXMaskTextField;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Created by ipssi11 on 13-Oct-16.
 */
public class LoadRoadGrossTareController implements ControllerI ,Initializable{
    @FXML
    private JFXTextField textBoxVehicleName;
    @FXML
    private JFXTextField textBoxDONo;
    @FXML
    private Label textWBReading;
    @FXML
    private Label textChallanNo;
    @FXML
    private Label textCustomerCode;
    @FXML
    private Label textCustomerName;
    @FXML
    private JFXTextField textBoxSourceCode;
    @FXML
    private Label textSourceName;
    @FXML
    private Label textNoTareAllowed;
    @FXML
    private Label textLastTareDate;
    @FXML
    private Label textLastTareExpired;
    @FXML
    private Label textLastTare;
    @FXML
    private Label textTareWt;
    @FXML
    private Label textNetWt;
    @FXML
    private Label textAllotedQty;
    @FXML
    private Label textBalancedQty;
    @FXML
    private Label textDestinationCode;
    @FXML
    private Label textDestination;
    @FXML
    private Label textChallanDate;
    @FXML
    private Label textmaterialSizing;
    @FXML
    private Label textmaterialGradeName;
    @FXML
    private JFXTextField textBoxTransporter;
    /*@FXML
    private Label textTripsToday;
    @FXML
    private Label textQuota;*/
    @FXML
	private Label textGrossWt;
	@FXML
	private JFXCheckBox checkBoxTare;
	@FXML
	private JFXCheckBox checkBoxGross;
	@FXML
    private JFXMaskTextField textBoxLRNo;
	private WeighmentStep weighmentStep = WeighmentStep.gross;
	private boolean changeWeighment = false;
	@FXML
	private void toggleWeighment(ActionEvent event) {
		handleToggleWeighment(((Node)event.getSource()).getId());
	}
	@Override
	public void handleToggleWeighment(String controlId){
		WeighmentStep weighmentState = controlId.equalsIgnoreCase("checkBoxTare") ? WeighmentStep.tare : WeighmentStep.gross;
		if((weighmentState == WeighmentStep.tare && checkBoxTare.isDisable()) || (weighmentState == WeighmentStep.gross && checkBoxGross.isDisable()) )
			return;
		setWeighmentMode(weighmentState, true);
		setReadings();
	}
	private void setWeighmentMode(WeighmentStep weighmentState, boolean initTPR){
		this.weighmentStep = weighmentState;
		if(weighmentState == WeighmentStep.tare){
			checkBoxTare.setSelected(true);
			checkBoxTare.setDisable(true);
			checkBoxGross.setSelected(false);
			checkBoxGross.setDisable(false);
		}else if(weighmentState == WeighmentStep.gross){
			checkBoxGross.setSelected(true);
			checkBoxGross.setDisable(true);
			checkBoxTare.setSelected(false);
			checkBoxTare.setDisable(false);
		}else{
			checkBoxGross.setSelected(false);
			checkBoxGross.setDisable(true);
			checkBoxTare.setSelected(false);
			checkBoxTare.setDisable(true);
		}
		if(initTPR){
			parent.handleReinitTPR(false);
		}
	}
    MainController parent;
	Parent rootView;
	MenuItemInfo menuItemInfo;
    private TPRecord tpRecord=null;
    DigitalClockExt challanClock;
	@Override
    public void init(MainController parent, Parent rootView, MenuItemInfo menuItemInfo) {
        this.rootView = rootView;
        this.parent = parent;
        this.menuItemInfo = menuItemInfo;
        if(challanClock == null)
        	this.challanClock = new DigitalClockExt(textChallanDate, ScreenConstant.DATE_FORMAT_DDMMYYYY_HHMM);
    }
	public void setReadings(){
		if(weighmentStep != WeighmentStep.noWeight){
			Random r = new Random();
			int value =  r.nextInt(10)*500 + (weighmentStep == WeighmentStep.gross ? 30000 : 11000); 
			setWeighBridgeReading(value+"");
		}
	}
	@Override
	public void clearInputs() {
		changeWeighment = false;
		if(TokenManager.isSimulateWB){
			setReadings();
		}else{
			textWBReading.setText("");
		}
    	tpRecord = null;
    	tareInfo = null;
    	challanClock.stop();
//    	weighmentState = WeighmentState.Nothing;
//    	setWeighmentMode(weighmentState,false);
    	/*isGross=true;
    	setWeimentMode(isGross);*/
	}
	
	@Override
	public void populateTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep, Vehicle vehicle) {
		if(tpRecord == null)
            return;
		changeWeighment = true;
        VehicleExtended vehicleExt;
        try {
        	this.tpRecord = tpRecord;
        	if(TokenManager.isSimulateWB){
			/*	if(weighmentStep == WeighmentStep.gross)
					textGrossWt.setText(textWBReading.getText());
				else if(weighmentStep == WeighmentStep.tare)
					textTareWt.setText(textWBReading.getText());*/
        		setReadings();	
			}
        	SimpleDateFormat sdf = new SimpleDateFormat(ScreenConstant.DATE_FORMAT_DDMMYYYY_HHMM);
			vehicleExt = vehicle == null ? null : vehicle.getVehicleExt();
			textBoxVehicleName.setText(tpRecord.getVehicleName());
			textChallanNo.setText(tpRecord.getChallanNo());
			textChallanDate.setText(tpRecord.getChallanDate() == null ? "" : sdf.format(tpRecord.getChallanDate()));
            //textTareWt.setText(Misc.isUndef(tpRecord.getLoadTare()) ? "" : tpRecord.getLoadTare()+"");
            int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
            String transporter = !Utils.isNull(tpRecord.getTransporterCode()) ?  LovDao.getAutocompletePrintable(portNodeId, tpRecord.getTransporterCode(), LovItemType.TRANSPORTER) : vehicleExt == null || vehicleExt.getTransporterCode() == null ? "" : LovDao.getAutocompletePrintable(portNodeId, vehicleExt.getTransporterCode(), LovItemType.TRANSPORTER);
			textBoxTransporter.setText(transporter);
			
			if(Utils.isNull(tpRecord.getLrNo())){
				textBoxLRNo.setMask(new SimpleDateFormat("dd").format(new Date())+parent.getMainWindow().getWorkStationDetails().getLrPrefixFirst()+"DDD"+parent.getMainWindow().getWorkStationDetails().getLrPrefixSecond()+"DDD");
			}else{
				textBoxLRNo.setText(tpRecord.getLrNo());
			}
            handleDoInformation(conn, tpRecord != null && !Utils.isNull(tpRecord.getDoNumber())? tpRecord.getDoNumber() : vehicle != null ? vehicle.getDoAssigned() : null,Misc.getUndefInt() );
            String sourceName = !Utils.isNull(tpRecord.getMinesCode()) ? tpRecord.getMinesCode() : parent.getMainWindow().getWorkStationDetails().getMinesCode();
			if(!Utils.isNull(sourceName)){
				Pair<String, String> source = LovDao.getAutocompletePrintablePair(portNodeId, sourceName, LovItemType.MINES);
				textBoxSourceCode.setText(source == null ? "" : source.first);
				textSourceName.setText(source == null ? "" : source.second);
			}
            handleTareWt(conn, tpRecord, vehicle);
            calculateWeights();
            if(tpRecord.getChallanDate() == null)
            	challanClock.play();
        }catch (Exception ex){
            ex.printStackTrace();
        }
	}
	TPRTareDetails tareInfo = null;
	private void handleTareWt(Connection conn,TPRecord tpRecord,Vehicle vehicle) throws Exception{
		tareInfo = TPRTareDetails.getTareInfo(conn, tpRecord, vehicle, true, Type.TPRMATERIAL.COAL_ROAD);
		if(tareInfo != null){
			SimpleDateFormat sdf = new SimpleDateFormat(ScreenConstant.DATE_FORMAT_DDMMYYYY_HHMM);
			textNoTareAllowed.setText(tareInfo.isNoTareAllowed() ? "Yes" : "No");
			textLastTare.setText(Misc.isUndef(tareInfo.getTare()) ? "" : tareInfo.getTare()+"");
			textLastTareDate.setText(Misc.isUndef(tareInfo.getLastTareOn()) ? "" : (sdf.format(new Date(tareInfo.getLastTareOn()))));
			textLastTareExpired.setText(Misc.isUndef(tareInfo.getTareExpiareOn()) ? "" : (sdf.format(new Date(tareInfo.getTareExpiareOn()))));
			if(tareInfo.isNoTareAllowed() && (weighmentStep == WeighmentStep.gross)){
				textTareWt.setText(Misc.isUndef(tareInfo.getTare()) ? "" : tareInfo.getTare()+"");
			}
		}
		
	}
	@Override
	public void setVehicleName(String vehicleName) {
		textBoxVehicleName.setText(vehicleName);
	}

	@Override
	public void clearVehicleName() {
		textBoxVehicleName.setText("");
	}

	@Override
	public boolean setTPRAndSaveNonTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep) throws Exception {
		if(tpRecord == null || Misc.isUndef(tpRecord.getVehicleId()))
            return false;
		changeWeighment = false;
    	double capturedWt = Misc.getParamAsDouble(textWBReading.getText());
		Vehicle veh = Vehicle.getVehicle(conn, tpRecord.getVehicleId());
		if(weighmentStep == WeighmentStep.tare){
			double avgTare = TPRTareDetails.getAvgTare(conn, true, tpRecord.getVehicleId());//veh.getAvgTare();
			if(Misc.isUndef(avgTare))
				avgTare = veh.getMinTare();
			double change = Misc.isUndef(avgTare) ? Misc.getUndefDouble() : Math.abs(avgTare-capturedWt);
			boolean isHigh = avgTare < capturedWt; 
			if(!Misc.isUndef(change) && !Misc.isUndef(parent.getMainWindow().getWorkStationDetails().getTareChangeThresHold()) && change > parent.getMainWindow().getWorkStationDetails().getTareChangeThresHold()){
				parent.showAlert(Alert.AlertType.WARNING, "System Exception Message", (isHigh ? UIConstant.MORE_TARE_FOUND : UIConstant.LESS_TARE_FOUND)+"\nAcceptable Tare Weight="+avgTare);
			}
			
		}else if(weighmentStep == WeighmentStep.gross){
			double maxGross = veh.getMinGross();
			double change = Misc.isUndef(maxGross) ? Misc.getUndefDouble() : capturedWt - maxGross;
			if(!Misc.isUndef(change) && !Misc.isUndef(parent.getMainWindow().getWorkStationDetails().getGrossChangeThresHold()) && change > parent.getMainWindow().getWorkStationDetails().getGrossChangeThresHold()){
				parent.showAlert(Alert.AlertType.ERROR, "System Exception Message", UIConstant.MORE_GROSS_FOUND+"\nAcceptable Gross Weight="+maxGross);
				changeWeighment = true;
				return false;
			}
		}
        String[] options = {"  Save  ", "  Cancel  "};
        String msg = "Vehicle Name: " + textBoxVehicleName.getText() + "\nTransporter: " + textBoxTransporter.getText() + "\nCaptured Weight: " + Misc.printDouble(capturedWt,false);
        int responseVehicleDialog = parent.prompt("Data Saving Confirmation", msg, options);
        if (responseVehicleDialog == 1) {
            changeWeighment = true;
        	return false;
        }
        DoDetails doDetails = DoDetails.getDODetails(conn, textBoxDONo.getText(), Misc.getUndefInt());
        /*int materialGrade = doDetails == null ? Misc.getUndefInt() : doDetails.getGrade();
		int mines = doDetails == null ? Misc.getUndefInt() : doDetails.getSourceMines();
		int destination = doDetails == null ? Misc.getUndefInt() : doDetails.getDestination();
		int doId = doDetails.getId();
		tpRecord.setDoId(doId);
		tpRecord.setRfDOId(doId);
		tpRecord.setMinesId(mines);
		tpRecord.setRfMinesId(mines);
		tpRecord.setMaterialGradeId(materialGrade);
		tpRecord.setRfGrade(materialGrade);
		tpRecord.setPlantId(destination);
		int transporterId = LovUtils.getIntValue(comboBoxTransporter);
		tpRecord.setTransporterId(transporterId);
		tpRecord.setRfTransporterId(transporterId);*/
        tpRecord.setMaterialCat(menuItemInfo.getTprWorkstionConfig(readerId).getMaterialCat());
		double loadGross = capturedWt;
		double loadTare = tpRecord.getLoadTare();
		tpRecord.setDoId(doDetails == null ? Misc.getUndefInt() : doDetails.getId());
		tpRecord.setDoNumber(textBoxDONo.getText());
		tpRecord.setMinesCode(LovDao.getAutoCompleteValue(textBoxSourceCode));
		tpRecord.setRfMinesCode(LovDao.getAutoCompleteValue(textBoxSourceCode));
		tpRecord.setGradeCode(LovDao.getAutoCompleteValue(textmaterialGradeName));
		tpRecord.setRfGradeCode(LovDao.getAutoCompleteValue(textmaterialGradeName));
		tpRecord.setDestinationCode(LovDao.getAutoCompleteValue(textDestinationCode));
		tpRecord.setTransporterCode(LovDao.getAutoCompleteValue(textBoxTransporter));
		tpRecord.setRfTransporterCode(LovDao.getAutoCompleteValue(textBoxTransporter));
//		tpRecord.setLoadGross(capturedWt);
//        tpRecord.setRfLoadGross(capturedWt);
		tpRecord.setLrNo(textBoxLRNo.getText());
        if(weighmentStep == WeighmentStep.gross){
			tpRecord.setLoadGross(capturedWt);
			tpRecord.setRfLoadGross(capturedWt);
			if(tareInfo.isNoTareAllowed()){
	        	if(tpRecord.getLoadTare() <= 0){
	        		loadTare = tareInfo.getTare();
	        		tpRecord.setLoadTare(tareInfo.getTare());
	        		tpRecord.setRfLoadTare(tareInfo.getTare());
	        		tpRecord.setLatestLoadWbInExit(Misc.isUndef(tareInfo.getLastTareOn()) ? null : new Date(tareInfo.getLastTareOn()));
	        	}
	        	double net = Misc.isUndef(loadGross) || Misc.isUndef(loadTare) ? Misc.getUndefDouble() : loadGross - loadTare;
				if(!Misc.isUndef(net)){
					//int shiftId = ShiftInformation.getFirstShiftId(parent.getMainWindow().getWorkStationDetails().getPortNodeId(), conn, Misc.SCOPE_SHIFT, new Date());
					//ShiftBean shiftBean = ShiftInformation.getShiftById(parent.getMainWindow().getWorkStationDetails().getPortNodeId(), shiftId, conn);
					Date st = new Date();
//					TimePeriodHelper.setBegOfDate(st, Misc.SCOPE_SHIFT, shiftBean);
					TimePeriodHelper.setBegOfDate(st, Misc.SCOPE_DAY);
					String dayStart = Printer.getTimeStr(st);
					TPRDataUtils.updateDORemaining(conn, doDetails, parent.getMainWindow().getWorkStationDetails().getCode(), net);
					RFIDMasterDao.executeQuery(conn, "INSERT INTO current_do_status (do_id,do_number,wb_code,lifted_qty,last_lifted_on,last_lifted_qty,last_lifted_vehicle_id,last_lifted_tpr_id,trips_count_daily,trips_count) VALUES ("+doDetails.getId()+",'"+doDetails.getDoNumber()+"', '"+parent.getMainWindow().getWorkStationDetails().getCode()+"',"+net+",now(),"+net+","+tpRecord.getVehicleId()+","+tpRecord.getTprId()+",1,1) ON DUPLICATE KEY UPDATE lifted_qty=lifted_qty+"+net+",last_lifted_on=now(),last_lifted_qty="+net+",last_lifted_vehicle_id="+tpRecord.getVehicleId()+",last_lifted_tpr_id="+tpRecord.getTprId()+", trips_count_daily=(case when (timestampdiff(second, concat(date(last_lifted_on), ' "+dayStart+"'), last_lifted_on) < 0 or timestampdiff(second, DATE_ADD(concat(date(last_lifted_on), ' "+dayStart+"'),INTERVAL 86399 second),now()) > 0) then 1 else  trips_count_daily+1 end), trips_count=trips_count+1");
				}
	        	return true;
			}else{
				parent.showAlert(Alert.AlertType.WARNING, "System Exception Message", UIConstant.NO_TARE_FOUND);
				return false;
			}
		}else if(weighmentStep == WeighmentStep.tare){
			tpRecord.setLoadTare(capturedWt);
			tpRecord.setRfLoadTare(capturedWt);
	    	RFIDMasterDao.executeQuery(conn, "update vehicle set flyash_tare="+capturedWt+", flyash_tare_time=now() where id="+tpRecord.getVehicleId());
	    	return true;
		}
        return false;
	}

	@Override
	public void enableManualEntry(boolean enable) {
//		textBoxVehicleName.setEditable(enable);
		textBoxVehicleName.setDisable(!enable);
	}

	@Override
	public Pair<Boolean, String> requestFocusNextField(NodeExt currentField) {

        boolean next = true;
        String nextFieldId = null;
        Connection conn = null;
        boolean destroyIt = false;
        try{
        	conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        	if(currentField.isVehicle()){
        		Triple<Boolean, Integer, Integer> vehicleTriple = parent.checkAndGetVehiclId((TextField) currentField.getNode());
        		if(vehicleTriple != null && !Misc.isUndef(vehicleTriple.third)){
        			next = true;
        			parent.handleManualEntry(conn, Misc.getUndefInt(), vehicleTriple.first, vehicleTriple.second, vehicleTriple.third, ((TextField)currentField.getNode()).getText());
        		}else{
        			next = false;
        		}
        	}else if(currentField.isAutoComplete() && currentField.getAutoCompleteSrc() == LovItemType.DO){
        		//
        		next = handleDoInformation(conn, ((TextField)currentField.getNode()).getText(),Misc.getUndefInt());
        		if(!next)
        			((TextField)currentField.getNode()).setText("");
        	}
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
        return new Pair<>(next,nextFieldId);
    }
	private boolean handleDoInformation(Connection conn,String doNumber, int doId) throws Exception{
		boolean next = false;
		DoDetails doDetails = DoDetails.getDODetails(conn, doNumber, doId);
		if(doDetails != null){
			LatestDOInfo latestDOInfo = DoDetails.getLatestDOInfo(conn,  doNumber, parent.getMainWindow().getWorkStationDetails().getCode());
			Pair<Integer,String> isDoValid = doDetails.isDOValid(conn,parent.getMainWindow().getWorkStationDetails().getCode(),latestDOInfo);
			int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
			if(isDoValid == null || isDoValid.first != UIConstant.YES){
				textBoxDONo.setText("");
				textBoxSourceCode.setText("");
				textSourceName.setText("");
//				textWashery.setText("");
				textBoxDONo.setText("");
				textCustomerName.setText("");
				textCustomerCode.setText("");
				textDestination.setText("");
				textDestinationCode.setText("");
				textAllotedQty.setText("");
				textBalancedQty.setText("");
				textmaterialSizing.setText("");
				textmaterialGradeName.setText("");
//				textQuota.setText("");
			}else{
				Pair<String, String> customer = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getCustomerCode(), LovItemType.CUSTOMER);
				Pair<String, String> destination = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getDestinationCode(), LovItemType.ROAD_DEST);
				String grade = LovDao.getAutocompletePrintable(portNodeId, doDetails.getGradeCode(), LovItemType.MATERIAL_GRADE);
				Pair<String, String> source = LovDao.getAutocompletePrintablePair(portNodeId, (tpRecord != null && !Utils.isNull(this.tpRecord.getMinesCode())) ? tpRecord.getMinesCode() : doDetails.getSourceCode(), LovItemType.MINES);
				textBoxSourceCode.setText(source == null ? "" : Printer.print(source.first));
				textSourceName.setText(source == null ? "" : Printer.print(source.second));
				textBoxDONo.setText(doDetails.getDoNumber());
				textCustomerName.setText(customer == null ? "" : Printer.print(customer.second));
				textCustomerCode.setText(customer == null ? "" : Printer.print(customer.first));
				textDestination.setText(destination == null ? "" : Printer.print(destination.second));
				textDestinationCode.setText(destination == null ? "" : Printer.print(destination.first));
				textmaterialSizing.setText(Printer.print(doDetails.getCoalSize()));
				textmaterialGradeName.setText(Printer.print(grade));
//				textWashery.setText(LovDao.getAutocompletePrintable(portNodeId, (tpRecord != null && !Utils.isNull(this.tpRecord.getWasheryCode())) ? tpRecord.getWasheryCode() : doDetails.getWasheryCode(), LovItemType.WASHERY));
				//textAllotedQty.setText(doDetails.getDoDate() == null ? "" : sdf.format(doDetails.getDoDate()));
				textAllotedQty.setText(Misc.isUndef(doDetails.getQtyAlloc(null)) ? "" : doDetails.getQtyAlloc(null)+"");
				double liftedQty = latestDOInfo != null ? latestDOInfo.getLiftedQty() : Misc.getUndefDouble();
				//double doRemainingQty = Misc.isUndef(doDetails.getQtyAlloc(parent.getMainWindow().getWorkStationDetails().getCode())) ? Misc.getUndefDouble() : doDetails.getQtyAlloc(parent.getMainWindow().getWorkStationDetails().getCode()) - ((Misc.isUndef(liftedQty) ? 0.0 : liftedQty) );//+ (Misc.isUndef(doDetails.getQtyAlreadyLifted()) ? 0.0 : doDetails.getQtyAlreadyLifted()) );
				double doRemainingQty = doDetails.getQtyAlloc(parent.getMainWindow().getWorkStationDetails().getCode());
				textBalancedQty.setText(Printer.printDouble(doRemainingQty));
//				textQuota.setText(Printer.print(doDetails.getQuota()));
//				textTripsToday.setText(Printer.print(doDetails.getLatestDoInfo() == null ? Misc.getUndefInt() : doDetails.getLatestDoInfo().getDailyTripsCount()));
				next = true;
			}
			
		}
		return next;
	}

	@Override
	public boolean isPrintable() {
		return true;
	}

	@Override
	public boolean isManualEntry() {
		return true;
	}

	@Override
	public boolean print(TPRecord tpRecord, int workStationTypeId) {
		return false;
	}

	@Override
	public HashMap<Integer, Integer> getBlockingQuestions() {
		return null;
	}

	@Override
	public ArrayList<String> getInstruction() {
		return null;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@Override
	public void setWeighBridgeReading(String reading) {
		// TODO Auto-generated method stub
		int val = Misc.getParamAsInt(reading);
		if (!Misc.isUndef(val)) {
			int currVal = Misc.getParamAsInt(textWBReading.getText());
			if (Misc.isUndef(currVal) || (currVal >= val ? currVal - val : val - currVal) >= 10 || val == 0) {
				textWBReading.setText((double)val/1000.0 + "");
				if(changeWeighment){
					if(weighmentStep == WeighmentStep.gross){
						//textTareWt.setText("");
						textGrossWt.setText((double)val/1000.0 + "");
					}else if(weighmentStep == WeighmentStep.tare){
						//textGrossWt.setText("");
						textTareWt.setText((double)val/1000.0 + "");
					}
					calculateWeights();
				}
				//to do other
			}
		}
	}
	private void calculateWeights(){
		double tare = Misc.getParamAsDouble(textTareWt.getText());
		double gross = Misc.getParamAsDouble(textGrossWt.getText());
		double net = !Misc.isUndef(tare) && !Misc.isUndef(gross) ? (gross-tare) : Misc.getUndefDouble();
		textNetWt.setText(Misc.isUndef(net) ? "" : net+"");
	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hideActionBar() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public TPRWorkstationConfig getWorkstationConfig(Connection conn, int readerId, Vehicle veh) {
		if(parent == null)
			return null ;
		if(weighmentStep == WeighmentStep.noWeight){
			TPRTareDetails tareInfo = null;
			try {
				tareInfo = TPRTareDetails.getTareInfo(conn, null, veh, true, Type.TPRMATERIAL.COAL_ROAD);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setWeighmentMode(tareInfo != null && tareInfo.isNoTareAllowed() ? WeighmentStep.gross : WeighmentStep.tare ,false);
		}
		return new TPRWorkstationConfig(weighmentStep == WeighmentStep.gross ? Type.WorkStationType.SECL_LOAD_ROAD_WB_GROSS : Type.WorkStationType.SECL_LOAD_ROAD_WB_TARE, Type.TPRMATERIAL.COAL_ROAD);
	}
	@Override
	public void handleAutoComplete(NodeExt nodeExt, Pair<String, String> codeNamePair) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void resetWeighmentMode() {
		// TODO Auto-generated method stub
		setWeighmentMode(WeighmentStep.noWeight, false);
	}
}
