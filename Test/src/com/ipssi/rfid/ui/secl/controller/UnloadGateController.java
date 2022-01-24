package com.ipssi.rfid.ui.secl.controller;

import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;

import javafx.fxml.FXML;
import javafx.scene.Parent;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ipssi11 on 13-Oct-16.
 */
public class UnloadGateController implements ControllerI {
    MainController parent;
    Parent rootView;
    @FXML GateController gateInController;
    @FXML GateController gateOutController;
    MenuItemInfo menuItemInfo;
    @Override
    public void init(MainController parent, Parent rootView, MenuItemInfo menuItemInfo) {
        this.rootView = rootView;
        this.parent = parent;
        this.menuItemInfo = menuItemInfo;
        gateInController.init("Gate Entry Information", Type.Reader.IN, Type.RFID_AREA_TYPE.UNLOAD);
        gateOutController.init("Gate Exit Information", Type.Reader.OUT, Type.RFID_AREA_TYPE.UNLOAD);
    }

    @Override
    public void clearInputs() {

    }
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
        return false;
    }

    @Override
    public void enableManualEntry(boolean enable) {
//        textBoxVehicleName.setEditable(enable);
    }

    @Override
    public Pair<Boolean, String> requestFocusNextField(NodeExt currentField) {
        return null;
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
	public void setWeighBridgeReading(String reading) {
		// TODO Auto-generated method stub
		
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