/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.mpl.dhama_gudiya.services.HttpClientServicesImpl;
import com.ipssi.mpl.dhama_gudiya.services.XmlParser;
import com.ipssi.rfid.constant.ScreenConstant;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.ui.dao.UserLogin;
import com.ipssi.rfid.ui.dao.UserLoginClient;
import com.ipssi.rfid.ui.data.LovType;
import com.jfoenix.controls.JFXButton;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
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
	@FXML
	public Label LABEL_LOGIN;

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
		LABEL_LOGIN.setText(LovType.PROJECT_SIDE.getProjectName(TokenManager.PROJECT_AREA));
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

				if (TokenManager.PROJECT_AREA == LovType.PROJECT_SIDE.DHAMA_GUDIYA) {//
					parent.userData = UserLoginClient.Login(username, password);
					try {
						if (parent.userData != null)
							loadDeviceData();
						// Thread.sleep(10000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					conn = DBConnectionPool.getConnectionFromPoolNonWeb();
					parent.userData = UserLogin.Login(conn, username, password);// CGPL
				}

				if (parent.userData != null && !Misc.isUndef(parent.userData.getId())) {
					parent.initializeMenuControlls();
					parent.loadScreen(ScreenConstant.ScreenLinks.WELCOME_SCREEN_DHAMA_GUDIYA_WINDOW,
							ScreenConstant.MenuItemId.MAIN_WINDOW);
					String userName = !Utils.isNull(parent.userData.getName()) ? parent.userData.getName().toUpperCase()
							: "";
					parent.labelUsername.setText("Welcome: " + userName);
					TokenManager.userName = parent.userData.getName();
					TokenManager.userId = parent.userData.getId();
				} else {
					labelError.setText("Incorrect Username or Password !!!");
					TEXT_PASSWORD.requestFocus();
				}

			}
		} catch (Exception e) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", e.toString());
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (GenericException ex) {
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
	}

	@Override
	public void dlNoAction() {
	}

	private void loadDeviceData() {
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		String transportData = HttpClientServicesImpl.getTransporter(parent.userData.getId());
		XmlParser.transporterDataParser(transportData);
		String minesData = HttpClientServicesImpl.getMinesData(parent.userData.getId());
		XmlParser.minesDataParser(minesData);
		String gradeData = HttpClientServicesImpl.getGrades(parent.userData.getId());
		XmlParser.gradeDataParser(gradeData);
		String doData = HttpClientServicesImpl.getDONumbers(parent.userData.getId());
		XmlParser.doNoDataParser(doData);
		// }
		// }).start();
	}

}
