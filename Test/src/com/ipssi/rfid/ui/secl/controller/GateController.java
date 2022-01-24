package com.ipssi.rfid.ui.secl.controller;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.processor.Utils;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class GateController implements Initializable {
	@FXML private Label textVehicleName;
	@FXML private Label textCardType;
	@FXML private Label textPurpose;
	@FXML private Label textTareDate;
	@FXML private Label textGrossDate;
	@FXML private Label textTareWt;
	@FXML private Label textGrossWt;
	@FXML private Label textTitle;
	@FXML private Label labelTareDate;
	@FXML private Label labelGrossDate;
	@FXML private Label labelTare;
	@FXML private Label labelGross;
	private int areaType;
	private int readerId;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	public void init(String title,int readerId, int areaType){
		this.readerId = readerId;
		this.areaType = areaType;
		textTitle.setText(title);
		if(areaType != Type.RFID_AREA_TYPE.LOAD){
			labelGrossDate.setText("Load Gross Date :");
			labelTareDate.setText("Unload Gross Date :");
			labelTare.setText("Load Net :");
			labelGross.setText("Unload Net :");
		}
	}
	
	public void setView(TPRecord tpr,Vehicle vehicle){
		if(tpr == null || Utils.isNull(tpr.getVehicleName()))
			return;
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		textVehicleName.setText(tpr.getVehicleName());
		Date dateOne = null;
		Date dateTwo = null;
		double wtOne = Misc.getUndefDouble();
		double wtTwo = Misc.getUndefDouble();
		if(areaType == Type.RFID_AREA_TYPE.LOAD){
			dateOne = tpr.getLatestLoadWbInExit();
			dateTwo = tpr.getLatestLoadWbOutExit();
			wtOne = tpr.getLoadTare();
			wtTwo = tpr.getUnloadGross();
		}else{
			dateOne = tpr.getLatestLoadWbOutExit();
			dateTwo = tpr.getLatestUnloadWbOutExit();
			wtOne = !Misc.isUndef(tpr.getLoadTare()) && !Misc.isUndef(tpr.getLoadGross()) ? (tpr.getLoadGross()-tpr.getLoadTare()) : Misc.getUndefDouble();
			wtTwo = !Misc.isUndef(tpr.getUnloadTare()) && !Misc.isUndef(tpr.getUnloadGross()) ? (tpr.getUnloadGross()-tpr.getUnloadTare()) : Misc.getUndefDouble();
		}
		textTareDate.setText(dateOne == null ? "" : sdf.format(dateOne) );
		textGrossDate.setText(dateTwo == null ? "" : sdf.format(dateTwo) );
		textTareWt.setText(Misc.isUndef(wtOne) ? "" : wtOne+"");
		textGrossWt.setText(Misc.isUndef(wtTwo) ? "" : wtTwo+"");
		if(vehicle != null ){
			textCardType.setText(Misc.isUndef(vehicle.getCardType()) ? "" : Type.RFID_CARD_TYPE.getString(vehicle.getCardType()) );
			textPurpose.setText(Misc.isUndef(vehicle.getCardPurpose()) ? "" : Type.RFID_CARD_PURPOSE.getString(vehicle.getCardPurpose()) );
		}
	}
	
	public void clear(){
		textVehicleName.setText("");
		textCardType.setText("");
		textPurpose.setText("");
		textTareDate.setText("");
		textGrossDate.setText("");
		textTareWt.setText("");
		textGrossWt.setText("");
	}
	
	
	
}
