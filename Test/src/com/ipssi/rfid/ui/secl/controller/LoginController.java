package com.ipssi.rfid.ui.secl.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.User;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;
import com.ipssi.rfid.ui.secl.data.TPRDataUtils;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.TextAlignment;

/**
 * Created by ipssi11 on 12-Oct-16.
 */
public class LoginController implements ControllerI {
    public JFXTextField testBoxUserName;
    public JFXPasswordField testBoxPassword;
    public Label labelError;
    private MainController parent;
    @FXML
    private JFXButton buttonLogin;

    public static void main(String[] arg) {
        System.out.println("loginController");
    }
    public void login(ActionEvent actionEvent) {
        doLogin();
    }
    private void doLogin() {
        String username = testBoxUserName.getText();
        String password = testBoxPassword.getText();
        Connection conn = null;
        boolean destroyIt = false;

        User user;
        try {
        	if (username.length() == 0) {
                labelError.setText("Please Enter Username !!!");
                testBoxUserName.requestFocus();

            } else if (password.length() == 0) {
                labelError.setText("Please Enter Password !!!");
                testBoxPassword.requestFocus();
            } else {
                conn = DBConnectionPool.getConnectionFromPoolNonWeb();
                user = TPRDataUtils.Login(conn, username, password);
                if (user != null &&  !Misc.isUndef(user.getId())) {
//                    RFIDConstant.setReaderConfiguration();
//                    TokenManager.userId = user.getId();
//                    TokenManager.userName = user.getUsername();
                    clearInputs();
                    parent.login(user);
                } else {
                    labelError.setText("Incorrect Username or Password !!!");
                    testBoxPassword.requestFocus();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            destroyIt = true;
        } finally {
            try {
                DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    Parent rootView;
    @Override
    public void init(MainController parent, Parent rootView, MenuItemInfo menuItemInfo) {
        this.rootView = rootView;
        this.parent = parent;
        if(parent == null || parent.getMainWindow() == null || parent.getMainWindow().getWorkStationDetails() == null || Utils.isNull(parent.getMainWindow().getWorkStationDetails().getCode()) || parent.getMainWindow().getWorkStationDetails().getPortNodeId() == 2){
        	labelError.setText("Unregistered System Found !!!");
    		testBoxPassword.setDisable(true);
    		testBoxUserName.setDisable(true);
    		buttonLogin.setDisable(true);
        }else{
        	labelError.setText("");
        	testBoxPassword.setDisable(false);
    		testBoxUserName.setDisable(false);
    		buttonLogin.setDisable(false);
        }
    }

    @Override
    public void clearInputs() {
        labelError.setText("");
        testBoxUserName.setText("");
        testBoxPassword.setText("");
    }

    public void populateTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep, Vehicle vehicle) {

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

    public void onKeyLogin(KeyEvent keyEvent) {
       if(keyEvent.getCode() == KeyCode.ENTER )
            doLogin();
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
		return true;
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
