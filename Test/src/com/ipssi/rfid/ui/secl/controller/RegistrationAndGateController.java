package com.ipssi.rfid.ui.secl.controller;

import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.beans.DoDetails;
import com.ipssi.rfid.beans.DoDetails.LatestDOInfo;
import com.ipssi.rfid.beans.DriverDetailBean;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.beans.VehicleExtended;
import com.ipssi.rfid.beans.VehicleRFIDInfo;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.custom.Printer;
import com.ipssi.rfid.ui.secl.data.DateUtils;
import com.ipssi.rfid.ui.secl.data.LovDao;
import com.ipssi.rfid.ui.secl.data.LovDao.LovItemType;
import com.ipssi.rfid.ui.secl.data.LovUtils;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;
import com.ipssi.rfid.ui.secl.data.TPRDataUtils;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXMaskTextField;
import com.jfoenix.controls.JFXTextField;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Created by ipssi11 on 13-Oct-16.
 */
public class RegistrationAndGateController  implements ControllerI,Initializable{
	 	@FXML
	    private JFXTextField textBoxVehicleName;
	    @FXML
	    private JFXTextField textBoxTransporter;
	    @FXML
	    private JFXTextField textBoxInsauranceNo;
	    @FXML
	    private DatePicker datePickerInsauranceValidTill;
	    @FXML
	    private JFXTextField textBoxRoadPermitNo;
	    @FXML
	    private DatePicker datePickerRoadPermitValidTill;
	    @FXML
	    private JFXTextField textBoxDriverName;
	    @FXML
	    private JFXTextField textBoxDriverDLNo;
	    @FXML
	    private DatePicker dateDLValidTill;
	    @FXML
	    private JFXTextField textBoxDriverMobile;
	    @FXML
	    private ComboBox<?> comboBoxRFIDType;
	    @FXML
	    private ComboBox<?> comboBoxRFIDCardPurpose;
	    @FXML
	    private Label labelRFIDEpc;
	    @FXML
	    private JFXButton buttonIssueTag;
	    @FXML
	    private VBox panelRFIDPurpose;
	    @FXML
	    private Label labelRFIDPurposeTitle;
	    @FXML
	    private VBox panelInternal;
	    @FXML
	    private TextField textBoxMines;
	    @FXML
	    private DatePicker datePickerTagValidFrom;
	    @FXML
	    private DatePicker datePickerTagValidTo;
	    @FXML
	    private VBox panelRoadSell;
	    @FXML
	    private JFXTextField textBoxDONo;
	    @FXML
	    private Label labelCustomer;
	    @FXML
	    private Label labelDestination;
	    @FXML
	    private Label labelDOAllotedQTY;
	    @FXML
	    private Label labelDOQTYRemaining;
	    @FXML
	    private Label labelDOActiveFrom;
	    @FXML
	    private Label labelDOLapseDate;
	    @FXML
	    private Label labelDOResult;
	    @FXML
	    private Label labelPreferedWB;
	    @FXML
	    private HBox panelOthers;
	    @FXML
	    private JFXTextField textBoxPurpose;
	    @FXML
	    private JFXCheckBox checkBoxVehicleOnGate;
	    
	    @FXML GateMinController gateInController;
	    @FXML GateMinController gateOutController;
	    @FXML private JFXMaskTextField textBoxGatePass;
	    @FXML private JFXMaskTextField textBoxMinTare;
	    @FXML private JFXMaskTextField textBoxMaxGross;
	    MainController parent;
    private ArrayList<String> rfidCardPurposeTitles = new ArrayList<>(Arrays.asList("For Internal :","For Road Sale :","For Washery :","For Others :"));
    public static final int RFID_CARD_PURPOSE_INTERNAL = 0;
    public static final int RFID_CARD_PURPOSE_ROAD = 1;
    public static final int RFID_CARD_PURPOSE_OTHER = 2;

    public static String TEXT_ISSUE="Issue";
    public static String TEXT_RETURN="Return";

    Parent rootView;
    MenuItemInfo menuItemInfo;
    @Override
    public void init(MainController parent, Parent rootView, MenuItemInfo menuItemInfo) {
        if(this.rootView == null){
        	this.rootView = rootView;
        	this.parent = parent;
        	this.menuItemInfo = menuItemInfo;
        	gateInController.init(parent, "Gate Entry Information", Type.Reader.IN, menuItemInfo.isLoad() ? Type.RFID_AREA_TYPE.LOAD : Type.RFID_AREA_TYPE.UNLOAD);
            gateOutController.init(parent,"Gate Exit Information", Type.Reader.OUT,menuItemInfo.isLoad() ? Type.RFID_AREA_TYPE.LOAD : Type.RFID_AREA_TYPE.UNLOAD);
            int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
        	LovUtils.setLov(null, portNodeId,comboBoxRFIDCardPurpose, LovDao.LovItemType.RFID_ISSUING_PURPOSE,Misc.getUndefInt());
            LovUtils.setLov(null, portNodeId,comboBoxRFIDType, LovDao.LovItemType.RFID_TYPE,Misc.getUndefInt());
            comboBoxRFIDCardPurpose.setOnAction((event) -> {
                clearRFIDCardPurpose();
                setPurposePanel(comboBoxRFIDCardPurpose.getValue() != null  &&  comboBoxRFIDCardPurpose.getValue() instanceof  ComboItem ? ((ComboItem)comboBoxRFIDCardPurpose.getValue()).getValue() : Misc.getUndefInt());

            });
        }
    }

    @Override
    public void clearInputs() {
        removeAllRFIDPurposePanel();
        vehicleId = Misc.getUndefInt();
        buttonIssueTag.setText(TEXT_ISSUE);
        buttonIssueTag.setDisable(true);
        vehicleId = Misc.getUndefInt();
        driverDetailBean = null;
        doId = Misc.getUndefInt();
        vehicle = null;
        textBoxVehicleName.setEditable(true);
    }
    int doId = Misc.getUndefInt();
    int vehicleId = Misc.getUndefInt();
    DriverDetailBean driverDetailBean = null;
    Vehicle vehicle = null;
    @Override
    public void populateTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep, Vehicle vehicle) {
    	if(readerId == Type.Reader.IN){
    		gateInController.setView(tpRecord, vehicle);
    	}else{
    		gateOutController.setView(tpRecord, vehicle);
    	}
    }

    @Override
    public void setVehicleName(String vehicleName) {

    }

    @Override
    public void clearVehicleName() {

    }

    @Override
    public boolean setTPRAndSaveNonTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep) throws Exception {
        
        return true;
    }

    private void saveVehicleExt(Connection conn) throws Exception {
        if(!Misc.isUndef(vehicleId)){
            VehicleExtended vehExt = (VehicleExtended) RFIDMasterDao.get(conn,VehicleExtended.class,vehicleId);
            boolean insertNew = false;
            if(vehExt == null) {
                vehExt = new VehicleExtended();
                vehExt.setVehicleId(vehicleId);
                insertNew = true;
            }
//            vehExt.setTransporter_id(LovUtils.getIntValue(comboBoxTransporter));
            vehExt.setTransporterCode(LovDao.getAutoCompleteValue(textBoxTransporter));
            vehExt.setInsurance_number(textBoxInsauranceNo.getText());
            vehExt.setPermit1_number(textBoxRoadPermitNo.getText());
            vehExt.setInsurance_number_expiry(DateUtils.asDate(datePickerInsauranceValidTill.getValue()));
            vehExt.setPermit1_number_expiry(DateUtils.asDate(datePickerRoadPermitValidTill.getValue()));
            if(insertNew)
                RFIDMasterDao.insert(conn,vehExt);
            else
                RFIDMasterDao.update(conn,vehExt);
        }
    }
    private void updateVehicle(Connection conn) throws Exception {
        if(vehicle != null){
        	vehicle.setPreferedDriver(driverDetailBean == null ? Misc.getUndefInt() : driverDetailBean.getId());
        	int cardType = LovUtils.getIntValue(comboBoxRFIDType);
        	int cardPurpose = LovUtils.getIntValue(comboBoxRFIDCardPurpose);
        	vehicle.setCardType(cardType);
        	vehicle.setCardPurpose(cardPurpose);
        	vehicle.setDoAssigned(vehicle.getCardPurpose() == Type.RFID_CARD_PURPOSE.ROAD ? textBoxDONo.getText() : null);
        	vehicle.setPreferedMinesCode(LovDao.getAutoCompleteValue(textBoxMines));
        	//vehicle.setPreferedMines(vehicle.getCardPurpose() == Type.RFID_CARD_PURPOSE.INTERNAL ? LovUtils.getIntValue(comboBoxAllowedMines) : Misc.getUndefInt());
        	vehicle.setCardInitDate(vehicle.getCardPurpose() == Type.RFID_CARD_PURPOSE.INTERNAL ? DateUtils.asDate(datePickerTagValidFrom.getValue()) : null);
        	vehicle.setCardExpiaryDate(vehicle.getCardPurpose() == Type.RFID_CARD_PURPOSE.INTERNAL ? DateUtils.asDate(datePickerTagValidTo.getValue()) : null);
        	vehicle.setVehicleOnGate(checkBoxVehicleOnGate.isSelected() ? 1 : 0);
        	vehicle.setMinGross(Misc.getParamAsDouble(textBoxMaxGross.getText()));
        	vehicle.setMinTare(Misc.getParamAsDouble(textBoxMinTare.getText()));
        	RFIDMasterDao.update(conn,vehicle);
        }
    }
    private int saveRFIDVehicleInfo(Connection conn,VehicleRFIDInfo vehRFIDInfo) throws Exception {
    	if(Misc.isUndef(vehicleId))
    		return Misc.getUndefInt();
    	vehRFIDInfo.setStatus(1);
    	vehRFIDInfo.setDriverId(driverDetailBean == null ? Misc.getUndefInt() : driverDetailBean.getId());
    	vehRFIDInfo.setDoAssigned(textBoxDONo.getText());
    	vehRFIDInfo.setCardType(LovUtils.getIntValue(comboBoxRFIDType));
    	vehRFIDInfo.setCardIssuedFor(LovUtils.getIntValue(comboBoxRFIDCardPurpose));
    	vehRFIDInfo.setAllowedMinesCode(LovDao.getAutoCompleteValue(textBoxMines));
//    	vehRFIDInfo.setAllowedMines(LovUtils.getIntValue(comboBoxAllowedMines));
    	vehRFIDInfo.setIssueDate(DateUtils.asDate(datePickerTagValidFrom.getValue()));
    	vehRFIDInfo.setValidUpto(DateUtils.asDate(datePickerTagValidTo.getValue()));
    	vehRFIDInfo.setPurpose(textBoxPurpose.getText());
        RFIDMasterDao.insert(conn,vehRFIDInfo);
        return vehRFIDInfo.getId();
    }

    @Override
    public void enableManualEntry(boolean enable) {
        //textBoxVehicleName.setEditable(enable);
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
        		Triple<Boolean, Integer, Integer> vehicleTriple = parent.checkAndGetVehiclId(true,(TextField) currentField.getNode());
        		if(vehicleTriple != null && !Misc.isUndef(vehicleTriple.third)){
        			textBoxVehicleName.setEditable(false);
        			initScreen(conn, vehicleTriple.third);
        			next = true;
        			parent.setFocusToFirstInputField();
        			//parent.handleManualEntry(conn, Misc.getUndefInt(), vehicleTriple.first, vehicleTriple.second, vehicleTriple.third, ((TextField)currentField.getNode()).getText());
        		}else{
        			next = false;
        		}
        	}else if(currentField.isAutoComplete() && (currentField.getAutoCompleteSrc() == LovItemType.DO || currentField.getAutoCompleteSrc() == LovItemType.DO_ALL)){
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
    private boolean handleDoInformation(Connection conn,String doNumber,int doId) throws Exception{
    	boolean next = false;
    	DoDetails doDetails = DoDetails.getDODetails(conn, doNumber, doId);
    	if(doDetails != null){
    		LatestDOInfo latestDOInfo = DoDetails.getLatestDOInfo(conn,  doNumber, null);
    		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    		doId = doDetails.getId();
    		int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
    		Pair<String, String> customer = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getCustomerCode(), LovItemType.CUSTOMER);
			Pair<String, String> destination = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getDestinationCode(), LovItemType.ROAD_DEST);
    		labelDOActiveFrom.setText(doDetails.getDoDate() == null ? "" : sdf.format(doDetails.getDoDate()));
    		labelDOLapseDate.setText(doDetails.getDoReleaseDate() == null ? "" : sdf.format(doDetails.getDoReleaseDate()));
    		labelCustomer.setText(customer == null ? "" :customer.second);
    		labelDestination.setText(destination == null ? "" :destination.second);
    		double qtyAllocated = doDetails.getQtyAlloc(null);
    		double liftedQty = latestDOInfo != null ? latestDOInfo.getLiftedQty() : Misc.getUndefDouble();
			double doRemainingQty = Misc.isUndef(qtyAllocated) ? Misc.getUndefDouble() : qtyAllocated - ((Misc.isUndef(liftedQty) ? 0.0 : liftedQty) + (Misc.isUndef(doDetails.getQtyAlreadyLifted()) ? 0.0 : doDetails.getQtyAlreadyLifted()) );
    		labelDOAllotedQTY.setText(Printer.printDouble(qtyAllocated));
			labelDOQTYRemaining.setText(Printer.printDouble(doRemainingQty));
			Pair<Integer,String> isDoValid = doDetails.isDOValid(conn,null,latestDOInfo);
    		if(isDoValid == null || isDoValid.first != UIConstant.YES)
    			textBoxDONo.setText("");
    		else
    			textBoxDONo.setText(doDetails.getDoNumber());
    		if(isDoValid  == null || isDoValid.first != UIConstant.YES){
    			labelDOResult.setStyle("-fx-text-fill:darkred;");
    		}else{
    			labelDOResult.setStyle("-fx-text-fill:darkgreen");
    		}
    		labelDOResult.setText(isDoValid == null ? "" : isDoValid.second);
    		labelPreferedWB.setText(!Utils.isNull(doDetails.getPreferedWb1()) ? doDetails.getPreferedWb1() : !Utils.isNull(doDetails.getPreferedWb2()) ? doDetails.getPreferedWb2() : !Utils.isNull(doDetails.getPreferedWb3()) ?  doDetails.getPreferedWb3() : !Utils.isAadharNumbervalidate(doDetails.getPreferedWb4()) ? doDetails.getPreferedWb4() : "" );
    		next = true;
    	}
    	return next;
    }
    private void initScreen(Connection conn,int vehicleId) throws Exception{
    	if(Misc.isUndef(vehicleId))
    		return;
    	this.vehicleId = vehicleId;
    	this.vehicle = Vehicle.getVehicle(conn, vehicleId);
    	textBoxVehicleName.setText(vehicle.getStdName());
    	VehicleExtended vehicleExt;
    	vehicleExt = vehicle == null ? null : vehicle.getVehicleExt();
    	driverDetailBean = (DriverDetailBean) RFIDMasterDao.get(conn,DriverDetailBean.class,vehicle == null ? Misc.getUndefInt() : vehicle.getPreferedDriver());
    	//doDetails = (DoDetails) RFIDMasterDao.get(conn, DoDetails.class, vehRFIDInfo == null ? Misc.getUndefInt() : vehRFIDInfo.getId());
    	int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
    	
    	textBoxTransporter.setText(vehicleExt == null || vehicleExt.getTransporterCode() == null ? "" : LovDao.getAutocompletePrintable(portNodeId, vehicleExt.getTransporterCode(), LovItemType.TRANSPORTER));
    	textBoxMines.setText(vehicle == null || vehicle.getPreferedMinesCode() == null ? "" : LovDao.getAutocompletePrintable(portNodeId, vehicle.getPreferedMinesCode(),LovItemType.ALLOWED_AREA));
//    	LovUtils.setLov(conn,portNodeId, comboBoxTransporter, LovDao.LovItemType.TRANSPORTER, vehicleExt == null ? Misc.getUndefInt() : vehicleExt.getTransporter_id());
//    	LovUtils.setLov(conn,portNodeId, comboBoxAllowedMines, LovDao.LovItemType.ALLOWED_AREA, vehicle == null? Misc.getUndefInt() : vehicle.getPreferedMines() );
    	
    	LovUtils.setLov(null,portNodeId,comboBoxRFIDCardPurpose, LovDao.LovItemType.RFID_ISSUING_PURPOSE, vehicle == null? Misc.getUndefInt() : vehicle.getCardPurpose());
    	LovUtils.setLov(null,portNodeId,comboBoxRFIDType, LovDao.LovItemType.RFID_TYPE, vehicle == null? Misc.getUndefInt() : vehicle.getCardType());
    	
//    	textBoxPurpose.setText(Misc.getParamAsString(vehicle == null ? null : vehicle.getPurpose(),""));
    	labelRFIDEpc.setText(vehicle.getEpcId());
    	textBoxInsauranceNo.setText(Misc.getParamAsString(vehicleExt == null ? null : vehicleExt.getInsurance_number(),""));
    	textBoxRoadPermitNo.setText(Misc.getParamAsString(vehicleExt == null ? null : vehicleExt.getPermit1_number(),""));
    	if(!Utils.isNull(vehicle.getMinTare()))
    		textBoxMinTare.setText(Printer.printDouble(vehicle.getMinTare()));
    	if(!Utils.isNull(vehicle.getMinGross()))
    		textBoxMaxGross.setText(Printer.printDouble(vehicle.getMinGross()));
    	datePickerInsauranceValidTill.setValue(vehicleExt == null ? null : DateUtils.asLocalDate(vehicleExt.getInsurance_number_expiry()));
    	datePickerRoadPermitValidTill.setValue(vehicleExt == null ? null : DateUtils.asLocalDate(vehicleExt.getPermit1_number_expiry()));
    	textBoxDriverName.setText(Misc.getParamAsString(driverDetailBean == null ? null : driverDetailBean.getDriver_name(),""));
    	textBoxDriverDLNo.setText(Misc.getParamAsString(driverDetailBean == null ? null : driverDetailBean.getDriver_dl_number(),""));
    	textBoxDriverMobile.setText(Misc.getParamAsString(driverDetailBean == null ? null : driverDetailBean.getDriver_mobile_one(),""));
    	dateDLValidTill.setValue(driverDetailBean == null ? null : DateUtils.asLocalDate(driverDetailBean.getDl_expiry_date()));
    	datePickerTagValidFrom.setValue(vehicle == null ? null : DateUtils.asLocalDate(vehicle.getCardInitDate()));
    	datePickerTagValidTo.setValue(vehicle == null ? null : DateUtils.asLocalDate(vehicle.getCardExpiaryDate()));
    	buttonIssueTag.setDisable(false);
    	parent.getControl("CONTROL_SAVE").setDisable(false);
    	handleDoInformation(conn, vehicle.getDoAssigned(),Misc.getUndefInt());
    }
    @Override
    public boolean isPrintable() {
        return false;
    }

    @Override
    public boolean isManualEntry() {
        return false;
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
        //TextFields.bindAutoCompletion(textBoxVehicleName, t-> LovDao.getVehicleSuggestion(t.getUserText()));
        //TextFields.bindAutoCompletion(textBoxDONo, t-> LovDao.getDoSuggestion(t.getUserText()));
    	
    }
    private void clearRFIDCardPurpose(){
        /*comboBoxAllowedMines.setValue(null);
        datePickerTagValidFrom.setValue(null);
        datePickerTagValidTo.setValue(null);*/

        textBoxDONo.setText("");
        textBoxPurpose.setText("");
        labelCustomer.setText("");
        labelDestination.setText("");
        labelDOAllotedQTY.setText("");
        labelDOQTYRemaining.setText("");
        labelDOActiveFrom.setText("");
        labelDOLapseDate.setText("");
        labelDOResult.setText("");
        labelPreferedWB.setText("");
        /*panelInternal.setVisible(false);
        panelRoadSell.setVisible(false);
        panelOthers.setVisible(false);*/
    }
    private void removeAllRFIDPurposePanel(){
        for (int i = 0,is = panelRFIDPurpose.getChildren() == null ? 0 : panelRFIDPurpose.getChildren().size(); i < is; i++) {
            if(panelRFIDPurpose.getChildren().get(i).getId().equalsIgnoreCase(labelRFIDPurposeTitle.getId()))
                continue;
            parent.removeFromCachedNodes(panelRFIDPurpose.getChildren().remove(i));
            i--;
            is--;
//            panelRFIDPurpose.getChildren().remove(i);
        }
    }
    private void setPurposePanel(int paneId){
        if(Misc.isUndef(paneId))
            return;
        removeAllRFIDPurposePanel();
        labelRFIDPurposeTitle.setText(rfidCardPurposeTitles.get(paneId));
        if(paneId == Type.RFID_CARD_PURPOSE.INTERNAL) {
            panelRFIDPurpose.getChildren().add(panelInternal);
            parent.addToCachedNodes(panelInternal);
        }
        else if(paneId == Type.RFID_CARD_PURPOSE.ROAD || paneId == Type.RFID_CARD_PURPOSE.WASHERY) {
            panelRFIDPurpose.getChildren().add(panelRoadSell);
            parent.addToCachedNodes(panelRoadSell);
            textBoxGatePass.setMask(new SimpleDateFormat("dd").format(new Date())+parent.getMainWindow().getWorkStationDetails().getLrPrefixFirst()+"DDD");
        }
        else if(paneId == Type.RFID_CARD_PURPOSE.OTHER) {
            panelRFIDPurpose.getChildren().add(panelOthers);
            parent.addToCachedNodes(panelOthers);
        }
    }
    public void IssueTag(ActionEvent actionEvent) {
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		if(parent.validateData() && parent.issueTag(vehicle)){
    			//RFIDMasterDao.executeQuery(conn, "update vehicle set last_epc=rfid_epc where rfid_epc like '" + vehicle.getEpcId() + "'");
                RFIDMasterDao.executeQuery(conn, "update vehicle set rfid_epc=null where rfid_epc like '" + vehicle.getEpcId() + "'");
                RFIDMasterDao.executeQuery(conn, "update vehicle_rfid_info set status=2 , return_date=now()  where epc_id like '" + vehicle.getEpcId() + "' and status=1");
                RFIDMasterDao.update(conn, vehicle, false);
                VehicleRFIDInfo vehicleRFIDInfo = null;
                if(Misc.isUndef(vehicle.getRfidInfoId()) || vehicle.getLastEPC() == null && !vehicle.getLastEPC().equalsIgnoreCase(vehicle.getEpcId()) ){
                	vehicleRFIDInfo = new VehicleRFIDInfo();
                	vehicleRFIDInfo.setVehicleId(vehicle.getId());
                	vehicleRFIDInfo.setEpcId(vehicle.getEpcId());
                }else{
                	vehicleRFIDInfo = (VehicleRFIDInfo) RFIDMasterDao.get(conn, VehicleRFIDInfo.class, vehicle.getRfidInfoId());
                }
                vehicle.setRfidInfoId(saveRFIDVehicleInfo(conn,vehicleRFIDInfo));
                updateVehicle(conn);
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
    }

	@Override
	public void setWeighBridgeReading(String reading) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean save() {
		Connection conn = null;
		boolean destroyIt = false;
		try{
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			driverDetailBean = new DriverDetailBean();
			driverDetailBean.setDriver_name(textBoxDriverName.getText());
			driverDetailBean.setDriver_dl_number(textBoxDriverDLNo.getText());
			driverDetailBean.setDriver_mobile_one(textBoxDriverMobile.getText());
			driverDetailBean.setDl_expiry_date(DateUtils.asDate(dateDLValidTill.getValue()));
			int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
			TPRDataUtils.saveDriverDetails(conn, portNodeId,driverDetailBean);
			doId = TPRDataUtils.getDoId(conn,textBoxDONo.getText());
			vehicle.setPreferedDriver(driverDetailBean.getId());
			updateVehicle(conn);
			saveVehicleExt(conn);
			return true;
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
		return false;
	}

	@Override
	public boolean hideActionBar() {
		// TODO Auto-generated method stub
		return false;
	}
	public void clearWorkstation(ActionEvent actionEvent) {
		if(parent != null){
			Connection conn = null;
			boolean destroyIt = false;
			try{
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				gateInController.clear();
				gateOutController.clear();
				parent.clearForm(conn,true,true,false);
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
		}
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
