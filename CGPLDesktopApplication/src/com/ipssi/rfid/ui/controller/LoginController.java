/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.ui.dao.UserLogin;
import com.ipssi.rfid.constant.ScreenConstant;
import com.ipssi.rfid.constant.UIConstant.COLUR;
import com.jfoenix.controls.JFXButton;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class LoginController implements Initializable, ControllerI {

	@FXML
	private TextField TEXT_USER_NAME;
	@FXML
	private PasswordField TEXT_PASSWORD;
	@FXML
	private JFXButton CONTROL_LOGIN;
	@FXML
	public Label labelError;

	private MainController parent = null;
	
	@FXML 
	public static AnchorPane TopPane; 

	@FXML
	private JFXButton CONTROL_BLUE;
	@FXML
	private JFXButton CONTROL_RED;
	
	private static final Logger log = Logger.getLogger(LoginController.class.getName());
	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
	}

	@FXML
	private void onControlKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			String controlId = parent.getSourceId(event);
			handleActionControl(controlId);
		}
	}

	@FXML
	private void controlItemClicked(MouseEvent event) {
		 String controlId = parent.getSourceId(event);
		 handleActionControl(controlId);
		
	}

	private void handleActionControl(String controlId) {
		if (controlId == null) {
			return;
		}

		controlId = controlId.toUpperCase();

		switch (controlId) {
		case "TEXT_USER_NAME":
			TEXT_PASSWORD.requestFocus();
			break;
		case "TEXT_PASSWORD":
			login();
			break;
		case "CONTROL_LOGIN":
			login();
			break;
		case "CONTROL_RED":
			parent.changeApplicationColor("RED");
			break;
		case "CONTROL_BLUE":
			parent.changeApplicationColor("BLUE");
			break;
		default:
			break;
		}

	}

	// private void handleActionControl(Button control) {
	// if (control == null || control.isDisable() || !control.isVisible()) {
	// return;
	// }
	// String controlId = control.getId().toUpperCase();
	//
	// switch (controlId) {
	// case "TEXT_USER_NAME":
	// TEXT_PASSWORD.requestFocus();
	// break;
	// case "TEXT_PASSWORD":
	// login();
	// break;
	// case "CONTROL_LOGIN":
	// login();
	// break;
	//
	// default:
	// break;
	// }
	//
	// }
	@Override
	public void clearInputs() {

	}

	public boolean login() {
		boolean destroyIt = false;
		String username = TEXT_USER_NAME.getText();
		String password = TEXT_PASSWORD.getText();
		Connection conn = null;
		try {
			if (username.length() == 0) {
				labelError.setText("Please Enter Username !!!");
				TEXT_USER_NAME.requestFocus();
			} else if (password.length() == 0) {
				labelError.setText("Please Enter Password !!!");
				TEXT_PASSWORD.requestFocus();
			} else {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				parent.userData = UserLogin.Login(conn, username, password);
				if (parent.userData != null && !Misc.isUndef(parent.userData.getId())) {
					parent.initializeMenuControlls();
					parent.loadScreen(ScreenConstant.ScreenLinks.WELCOME_SCREEN_WINDOW,ScreenConstant.MenuItemId.MAIN_WINDOW);
					String userName = !Utils.isNull(parent.userData.getName()) ? parent.userData.getName().toUpperCase() : "";
					parent.labelUsername.setText("Welcome: " + userName);
					TokenManager.userName=parent.userData.getName();
					TokenManager.userId=parent.userData.getId();
				} else {
					labelError.setText("Incorrect Username or Password !!!");
					TEXT_PASSWORD.requestFocus();
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
		return true;

	}

	@Override
	public void init(MainController parent) {
		this.parent = parent;
	}

	@Override
	public void stopRfid() {
	}

	@Override
	public void setVehicleName(String vehicleName) {
	}

	@Override
	public void clearVehicleName() {
	}

	@Override
	public void enableController(Button controllerId, boolean enable) {
	}

	@Override
	public void enableManualEntry(boolean enable) {
	}

	@Override
	public void stopSyncTprService() {
	}

	@Override
	public void initController(SettingController parent) {
	}

	@Override
	public void requestFocusNextField() {
	}

	@Override
	public void setTitle(String title) {

	}

	@Override
	public boolean save() {
		return false;
	}

	@Override
	public void vehicleNameAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dlNoAction() {
		// TODO Auto-generated method stub
		
	}



}
