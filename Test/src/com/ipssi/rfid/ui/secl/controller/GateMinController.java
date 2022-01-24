package com.ipssi.rfid.ui.secl.controller;

import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.processor.Utils;
import com.jfoenix.controls.JFXButton;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class GateMinController implements Initializable {
	@FXML
	private Label textTitle;
	@FXML
	private Label textVehicleName;
	@FXML
	private Label textCardType;
	@FXML
	private Label labelTareDate;
	@FXML
	private Label textDateTime;
	@FXML
	private Label labelTare;
	@FXML
	private Label textTareWt;
	@FXML
	private Label labelGross;
	@FXML
	private Label textGrossWt;
	@FXML
	private JFXButton buttonOpenGate;
	private int areaType;
	private int readerId;
    private MainController mainController;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

	}
	public void init(MainController mainController, String title,int readerId, int areaType){
		this.readerId = readerId;
		this.areaType = areaType;
		textTitle.setText(title);
		this.mainController = mainController;
		if(areaType != Type.RFID_AREA_TYPE.LOAD){
			labelTare.setText("L Net :");
			labelGross.setText("U Net :");
		}
		//buttonOpenGate.setDisable(true);
	}

	public void setView(TPRecord tpr,Vehicle vehicle){
		if(tpr == null || Utils.isNull(tpr.getVehicleName()))
			return;
		clear();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		textVehicleName.setText(tpr.getVehicleName());
		textDateTime.setText(sdf.format(new Date()));
		double wtOne = Misc.getUndefDouble();
		double wtTwo = Misc.getUndefDouble();
		if(areaType == Type.RFID_AREA_TYPE.LOAD){
			if(readerId == Type.Reader.OUT){
				wtOne = tpr.getLoadTare();
				wtTwo = tpr.getLoadGross();
			}
		}else{

			wtOne = !Misc.isUndef(tpr.getLoadTare()) && !Misc.isUndef(tpr.getLoadGross()) ? (tpr.getLoadGross()-tpr.getLoadTare()) : Misc.getUndefDouble();
			if(readerId == Type.Reader.OUT)
				wtTwo = !Misc.isUndef(tpr.getUnloadTare()) && !Misc.isUndef(tpr.getUnloadGross()) ? (tpr.getUnloadGross()-tpr.getUnloadTare()) : Misc.getUndefDouble();
		}

		textTareWt.setText(Misc.isUndef(wtOne) ? "" : wtOne+"");
		textGrossWt.setText(Misc.isUndef(wtTwo) ? "" : wtTwo+"");
		if(vehicle != null){
			textCardType.setText(Misc.isUndef(vehicle.getCardType()) ? "" : Type.RFID_CARD_TYPE.getString(vehicle.getCardType()) );
		}
	}

	public void clear(){
		textVehicleName.setText("");
		textCardType.setText("");
		textTareWt.setText("");
		textGrossWt.setText("");
		textDateTime.setText("");
	}
	@FXML
	void manualEntry(ActionEvent event) {

	}

	@FXML
	void vehicleNameKeyPressed(KeyEvent event) {
		if(mainController != null && mainController.handleCtrlKeyEvents(event))
			return;
		if (event.getCode() != KeyCode.ENTER)
            return;
		Connection conn = null;
        boolean destroyIt = false;
        try{
        	conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        	NodeExt currentField = NodeExt.getNodeExt((Node)event.getSource());
        	if(currentField.isVehicle()){
        		Triple<Boolean, Integer, Integer> vehicleTriple = mainController.checkAndGetVehiclId((TextField) currentField.getNode());
        		if(vehicleTriple != null && !Misc.isUndef(vehicleTriple.third)){
        			mainController.handleManualEntry(conn, readerId, vehicleTriple.first, vehicleTriple.second, vehicleTriple.third, ((TextField)currentField.getNode()).getText());
        		}
        		((TextField) currentField.getNode()).setText("");
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
}
