package com.ipssi.rfid.ui.secl.controller;

import java.net.URL;
import java.sql.Connection;
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
import com.ipssi.rfid.beans.DriverDetailBean;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.beans.VehicleExtended;
import com.ipssi.rfid.beans.VehicleRFIDInfo;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.secl.data.DateUtils;
import com.ipssi.rfid.ui.secl.data.LovDao;
import com.ipssi.rfid.ui.secl.data.LovUtils;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;
import com.ipssi.rfid.ui.secl.data.TPRDataUtils;
import com.jfoenix.controls.JFXButton;
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
public class RegistrationController  implements ControllerI,Initializable{
	 	@FXML
	    private JFXTextField textBoxVehicleName;
	    @FXML
	    private ComboBox<?> comboBoxTransporter;
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
	    private ComboBox<?> comboBoxAllowedMines;
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
	    private HBox panelOthers;
	    @FXML
	    private JFXTextField textBoxPurpose;
	    MainController parent;
    private ArrayList<String> rfidCardPurposeTitles = new ArrayList<>(Arrays.asList("For Internal :","For Road Sale :","For Others :"));
    public static final int RFID_CARD_PURPOSE_INTERNAL = 0;
    public static final int RFID_CARD_PURPOSE_ROAD = 1;
    public static final int RFID_CARD_PURPOSE_OTHER = 2;

    public static String TEXT_ISSUE="Issue";
    public static String TEXT_RETURN="Return";

    Parent rootView;
    MenuItemInfo menuItemInfo;
    @Override
    public void init(MainController parent, Parent rootView, MenuItemInfo menuItemInfo) {
        this.rootView = rootView;
        this.parent = parent;
        this.menuItemInfo = menuItemInfo;
    }

    @Override
    public void clearInputs() {
        removeAllRFIDPurposePanel();
        vehicleId = Misc.getUndefInt();
        buttonIssueTag.setText(TEXT_ISSUE);
        buttonIssueTag.setDisable(true);
        vehicleId = Misc.getUndefInt();
        vehRFIDInfo = null;
        driverDetailBean = null;
        doId = Misc.getUndefInt();
        vehicle = null;
    }
    int doId = Misc.getUndefInt();
    int vehicleId = Misc.getUndefInt();
    VehicleRFIDInfo vehRFIDInfo = null;
    DriverDetailBean driverDetailBean = null;
    Vehicle vehicle = null;
    @Override
    public void populateTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep, Vehicle vehicle) {}

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
            if(vehExt == null) {
                vehExt = new VehicleExtended();
                vehExt.setVehicleId(vehicleId);
            }
            vehExt.setTransporter_id(LovUtils.getIntValue(comboBoxTransporter));
            vehExt.setInsurance_number(textBoxInsauranceNo.getText());
            vehExt.setPermit1_number(textBoxRoadPermitNo.getText());
            vehExt.setInsurance_number_expiry(DateUtils.asDate(datePickerInsauranceValidTill.getValue()));
            vehExt.setPermit1_number_expiry(DateUtils.asDate(datePickerRoadPermitValidTill.getValue()));
            if(Misc.isUndef(vehicleId))
                RFIDMasterDao.insert(conn,vehExt);
            else
                RFIDMasterDao.update(conn,vehExt);
        }
    }
    private void saveRFIDVehicleInfo(Connection conn) throws Exception {
        if(vehRFIDInfo != null && !Misc.isUndef(vehRFIDInfo.getVehicleId())){
            vehRFIDInfo.setDriverId(driverDetailBean == null ? Misc.getUndefInt() : driverDetailBean.getId());
            vehRFIDInfo.setDoAssigned(textBoxDONo.getText());
            vehRFIDInfo.setCardType(LovUtils.getIntValue(comboBoxRFIDType));
            vehRFIDInfo.setCardIssuedFor(LovUtils.getIntValue(comboBoxRFIDCardPurpose));
            vehRFIDInfo.setAllowedMines(LovUtils.getIntValue(comboBoxAllowedMines));
            vehRFIDInfo.setIssueDate(DateUtils.asDate(datePickerTagValidFrom.getValue()));
            vehRFIDInfo.setIssueDate(DateUtils.asDate(datePickerTagValidTo.getValue()));
            vehRFIDInfo.setPurpose(textBoxPurpose.getText());
            RFIDMasterDao.update(conn,vehRFIDInfo);
        }
    }

    @Override
    public void enableManualEntry(boolean enable) {
        textBoxVehicleName.setEditable(enable);
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
        			initScreen(conn, vehicleTriple.third);
        			next = true;
        			//parent.handleManualEntry(conn, Misc.getUndefInt(), vehicleTriple.first, vehicleTriple.second, vehicleTriple.third, ((TextField)currentField.getNode()).getText());
        		}else{
        			next = false;
        		}
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
    private void initScreen(Connection conn,int vehicleId) throws Exception{
    	if(Misc.isUndef(vehicleId))
    		return;
    	vehicle = Vehicle.getVehicle(conn, vehicleId);
    	textBoxVehicleName.setText(vehicle.getStdName());
    	VehicleExtended vehicleExt;
    	VehicleRFIDInfo vehRFIDInfo;
    	DriverDetailBean driverDetailBean = null;
    	vehicleExt = vehicle == null ? null : vehicle.getVehicleExt();
    	driverDetailBean = (DriverDetailBean) RFIDMasterDao.get(conn,DriverDetailBean.class,vehicle == null ? Misc.getUndefInt() : vehicle.getPreferedDriver());
    	//doDetails = (DoDetails) RFIDMasterDao.get(conn, DoDetails.class, vehRFIDInfo == null ? Misc.getUndefInt() : vehRFIDInfo.getId());
    	int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
    	LovUtils.setLov(conn, portNodeId, comboBoxTransporter, LovDao.LovItemType.TRANSPORTER, vehicleExt == null ? Misc.getUndefInt() : vehicleExt.getTransporter_id());
    	LovUtils.setLov(null, portNodeId,comboBoxRFIDCardPurpose, LovDao.LovItemType.RFID_ISSUING_PURPOSE, vehicle == null? Misc.getUndefInt() : vehicle.getCardPurpose());
    	LovUtils.setLov(null, portNodeId,comboBoxRFIDType, LovDao.LovItemType.RFID_TYPE, vehicle == null? Misc.getUndefInt() : vehicle.getCardType());
    	LovUtils.setLov(conn,portNodeId, comboBoxAllowedMines, LovDao.LovItemType.MINES, vehicle == null? Misc.getUndefInt() : vehicle.getPreferedMines() );
//    	textBoxPurpose.setText(Misc.getParamAsString(vehicle == null ? null : vehicle.getPurpose(),""));
    	textBoxInsauranceNo.setText(Misc.getParamAsString(vehicleExt == null ? null : vehicleExt.getInsurance_number(),""));
    	textBoxRoadPermitNo.setText(Misc.getParamAsString(vehicleExt == null ? null : vehicleExt.getPermit1_number(),""));
    	datePickerInsauranceValidTill.setValue(vehicleExt == null ? null : DateUtils.asLocalDate(vehicleExt.getInsurance_number_expiry()));
    	datePickerRoadPermitValidTill.setValue(vehicleExt == null ? null : DateUtils.asLocalDate(vehicleExt.getPermit1_number_expiry()));
    	textBoxDriverName.setText(Misc.getParamAsString(driverDetailBean == null ? null : driverDetailBean.getDriver_name(),""));
    	textBoxDriverDLNo.setText(Misc.getParamAsString(driverDetailBean == null ? null : driverDetailBean.getDriver_dl_number(),""));
    	textBoxDriverMobile.setText(Misc.getParamAsString(driverDetailBean == null ? null : driverDetailBean.getDriver_mobile_one(),""));
    	dateDLValidTill.setValue(driverDetailBean == null ? null : DateUtils.asLocalDate(driverDetailBean.getDl_expiry_date()));
    	buttonIssueTag.setDisable(false);
    }
    @Override
    public boolean isPrintable() {
        return true;
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
    	int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
    	LovUtils.setLov(null, portNodeId,comboBoxRFIDCardPurpose, LovDao.LovItemType.RFID_ISSUING_PURPOSE,Misc.getUndefInt());
        LovUtils.setLov(null, portNodeId,comboBoxRFIDType, LovDao.LovItemType.RFID_TYPE,Misc.getUndefInt());
        comboBoxRFIDCardPurpose.setOnAction((event) -> {
            clearRFIDCardPurpose();
            setPurposePanel(comboBoxRFIDCardPurpose.getValue() != null  &&  comboBoxRFIDCardPurpose.getValue() instanceof  ComboItem ? ((ComboItem)comboBoxRFIDCardPurpose.getValue()).getValue() : Misc.getUndefInt());

        });
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
        if(paneId == RFID_CARD_PURPOSE_INTERNAL) {
            panelRFIDPurpose.getChildren().add(panelInternal);
            parent.addToCachedNodes(panelInternal);
        }
        else if(paneId == RFID_CARD_PURPOSE_ROAD) {
            panelRFIDPurpose.getChildren().add(panelRoadSell);
            parent.addToCachedNodes(panelRoadSell);
        }
        else if(paneId == RFID_CARD_PURPOSE_OTHER) {
            panelRFIDPurpose.getChildren().add(panelOthers);
            parent.addToCachedNodes(panelOthers);
        }
    }
    public void IssueTag(ActionEvent actionEvent) {
        parent.issueTag(vehicle);
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
			TPRDataUtils.saveDriverDetails(conn,portNodeId,driverDetailBean);
			doId = TPRDataUtils.getDoId(conn,textBoxDONo.getText());
			
			saveVehicleExt(conn);
			saveRFIDVehicleInfo(conn);
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
