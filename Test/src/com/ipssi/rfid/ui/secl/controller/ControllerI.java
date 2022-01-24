package com.ipssi.rfid.ui.secl.controller;

import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;

import javafx.scene.Parent;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ipssi11 on 12-Oct-16.
 */
public interface ControllerI {
    void init(MainController parent,Parent rootView, MenuItemInfo menuItemInfo);
    void clearInputs();
    void populateTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep, Vehicle vehicle);
    void setVehicleName(String vehicleName);
    void clearVehicleName();
    boolean setTPRAndSaveNonTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep) throws Exception;
    void enableManualEntry(boolean enable);
    Pair<Boolean,String> requestFocusNextField(NodeExt currentField);
    boolean isPrintable();
    boolean hideActionBar();
    boolean isManualEntry();
    boolean print(TPRecord tpRecord, int workStationTypeId);
    HashMap<Integer,Integer> getBlockingQuestions();
    ArrayList<String> getInstruction();
    void setWeighBridgeReading(String reading);
	boolean save();
	TPRWorkstationConfig getWorkstationConfig(Connection conn, int readerId, Vehicle veh);
	void handleAutoComplete(NodeExt nodeExt, Pair<String, String> codeNamePair);
	void resetWeighmentMode();
	default void handleToggleWeighment(String controlId){
		
	}
}
