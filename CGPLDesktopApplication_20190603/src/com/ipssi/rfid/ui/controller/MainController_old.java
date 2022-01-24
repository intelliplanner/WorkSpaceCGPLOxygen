/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.ipssi.rfid.beans.MenuItemInfo;
import com.ipssi.rfid.beans.User;
import com.ipssi.rfid.constant.RFIDConstant;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.constant.ScreenConstant;
import com.jfoenix.controls.JFXButton;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class MainController_old implements Initializable {

	@FXML
	private BorderPane mainForm;
	@FXML
	private HBox mainMenuContainer;
	@FXML
	private JFXButton CONTROL_GATE_IN;
	@FXML
	private JFXButton CONTROL_TARE_WB;
	@FXML
	private JFXButton CONTROL_GROSS_WB;
	@FXML
	private JFXButton CONTROL_GATE_OUT;
	@FXML
	private Label labelUsername;
	@FXML
	private JFXButton CONTROL_SIGN_OUT;
	@FXML
	private Label labelScreenTitle;
	@FXML
	private HBox panelError;
	@FXML
	private Label labelBlockingReason;
	@FXML
	private HBox mainActionContainer;
	@FXML
	private JFXButton CONTROL_SAVE;
	@FXML
	private JFXButton CONTROL_CLEAR;

	private User userData;
	private static HashMap<String, MenuItemInfo> menuScreenMap = new HashMap<>();
	private AnchorPane centerView = null;
	private String _selectedMenuId = null;
	private Button _selectedMenuControl = null;
	private static final Logger log = Logger.getLogger(MainController_old.class.getName());
	private boolean isAlert = false;
	Object alertLock = new Object();
	private MainController_old parent;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// labelUsername.setText(Utils.isNull(userData.getUsername()) ? "" :
		// userData.getUsername());
		initializeMenuControlls();
		initializeSystemParameter();
	}

	@FXML
	private void onControlKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			Button control = ((Button) event.getSource());
			_selectedMenuControl = control;
			handleActionControl(control);
		}
	}

	@FXML
	private void controlItemClicked(ActionEvent event) {
		Button control = ((Button) event.getSource());
		handleActionControl(control);
	}

	void initializeController(User userData, MainController_old parent) {
		this.userData = userData;
		this.parent = parent;
	}

	private void initializeMenuControlls() {
		if (userData != null) {
			if (userData.isSupperUser()) {
				CONTROL_GATE_IN.setVisible(true);
				CONTROL_GATE_OUT.setVisible(true);
				CONTROL_GROSS_WB.setVisible(true);
				CONTROL_TARE_WB.setVisible(true);
			} else if (userData.getPrivList().size() > 0) {
				if (userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.GATE_IN)) {
					// CONTROL_GATE_IN.setDisable(false);
					CONTROL_GATE_IN.setVisible(true);
					CONTROL_GATE_IN.setStyle("-fx-background-color: #0000b3;-fx-text-fill:#ffffff;-fx-opacity: 1;");

				} else {
					// CONTROL_GATE_IN.setDisable(true);
					CONTROL_GATE_IN.setVisible(false);
					CONTROL_GATE_IN.setStyle("-fx-background-color: #003399;-fx-text-fill:#fff");
				}
				if (userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.TARE_WB)) {
					// CONTROL_WB_IN.setDisable(false);
					CONTROL_TARE_WB.setVisible(true);
					CONTROL_TARE_WB.setStyle("-fx-background-color: #0000b3;-fx-text-fill:#ffffff;-fx-opacity: 1;");
				} else {
					// CONTROL_WB_IN.setDisable(true);
					CONTROL_TARE_WB.setVisible(false);
					CONTROL_TARE_WB.setStyle("-fx-background-color: #003399;-fx-text-fill:#fff");

				}
				if (userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.GROSS_WB)) {
					// CONTROL_WB_OUT.setDisable(false);
					CONTROL_GROSS_WB.setVisible(true);
					CONTROL_GROSS_WB.setStyle("-fx-background-color: #0000b3;-fx-text-fill:#ffffff;-fx-opacity: 1;");
				} else {
					// CONTROL_WB_OUT.setDisable(true);
					CONTROL_GROSS_WB.setVisible(false);
					CONTROL_GROSS_WB.setStyle("-fx-background-color: #003399;-fx-text-fill:#fff");
				}
				if (userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.GATE_OUT)) {
					// CONTROL_GATE_OUT.setDisable(false);
					CONTROL_GATE_OUT.setVisible(true);
					CONTROL_GATE_OUT.setStyle("-fx-background-color: #0000b3;-fx-text-fill:#ffffff;-fx-opacity: 1;");
				} else {
					// CONTROL_GATE_OUT.setDisable(true);
					CONTROL_GATE_OUT.setVisible(false);
					CONTROL_GATE_OUT.setStyle("-fx-background-color: #003399;-fx-text-fill:#fff");
				}
			} else {
				CONTROL_GATE_IN.setVisible(false);
				CONTROL_GATE_OUT.setVisible(false);
				CONTROL_TARE_WB.setVisible(false);
				CONTROL_GROSS_WB.setVisible(false);
			}

			if (_selectedMenuControl != null) {
				_selectedMenuControl.setDisable(true);
				_selectedMenuControl.setVisible(true);
				_selectedMenuControl.setStyle("-fx-background-color: #dee42d;-fx-text-fill:#000;-fx-opacity: 1;");
			} else {
				mainActionContainer.setVisible(false);
			}
		}
	}

	private void initializeSystemParameter() {
		RFIDConstant.setReaderConfiguration();
	}

	private void handleActionControl(Button control) {

		// if (control == null || control.isDisable() || !control.isVisible() ||
		// Utils.isNull(_selectedMenuId)
		// || _selectedMenuId.equalsIgnoreCase(ScreenConstant.MenuItemId.LOGIN)) {
		// return;
		// }
		String controlId = control.getId().toUpperCase();

		switch (controlId) {
		case "CONTROL_CLEAR":
			break;
		case "CONTROL_SAVE":
			break;

		case "CONTROL_GATE_IN":
			setMenuVisible(false);
			gateInMenuAction();
			break;

		case "CONTROL_GATE_OUT":
			setMenuVisible(false);
			gateOutMenuAction();
			break;

		case "CONTROL_TARE_WB":
			setMenuVisible(false);
			tareWBMenuAction();
			break;

		case "CONTROL_GROSS_WB":
			setMenuVisible(false);
			grossWbMenuAction();
			break;
		case "CONTROL_SIGN_OUT":
			setMenuVisible(true);
			signOutAction();
			break;
		default:
			break;
		}

		// showAlert("Control Action", "Action Taken :" + controlId);
	}

	private void gateInMenuAction() {
		TokenManager.initConfig("GATE_IN_TYPE", Type.WorkStationType.GATE_IN_TYPE);
		TokenManager.materialCat = com.ipssi.rfid.constant.Type.TPRMATERIAL.COAL;// coal
		TokenManager.currWorkStationType = com.ipssi.rfid.constant.Type.WorkStationType.GATE_IN_TYPE;// coal
		loadScreen(ScreenConstant.ScreenLinks.GATE_IN_WINDOW, ScreenConstant.MenuItemId.GATE_IN);
		initializeScreen();
	}

	private void setMenuVisible(boolean status) {
		CONTROL_GATE_IN.setVisible(status);
		CONTROL_TARE_WB.setVisible(status);
		CONTROL_GROSS_WB.setVisible(status);
		CONTROL_GATE_OUT.setVisible(status);
	}

	private void gateOutMenuAction() {
		TokenManager.initConfig("GATE_OUT_TYPE", Type.WorkStationType.GATE_OUT_TYPE);
		TokenManager.materialCat = com.ipssi.rfid.constant.Type.TPRMATERIAL.COAL;// coal
		TokenManager.currWorkStationType = com.ipssi.rfid.constant.Type.WorkStationType.GATE_OUT_TYPE;// coal
		loadScreen(ScreenConstant.ScreenLinks.GATE_OUT_WINDOW, ScreenConstant.MenuItemId.GATE_OUT);
		initializeScreen();
	}

	private void tareWBMenuAction() {
		TokenManager.initConfig("WEIGH_BRIDGE_OUT_TYPE", Type.WorkStationType.WEIGH_BRIDGE_OUT_TYPE);
		TokenManager.materialCat = com.ipssi.rfid.constant.Type.TPRMATERIAL.COAL;// coal
		TokenManager.currWorkStationType = com.ipssi.rfid.constant.Type.WorkStationType.WEIGH_BRIDGE_OUT_TYPE;// coal
		loadScreen(ScreenConstant.ScreenLinks.TARE_WB_WINDOW, ScreenConstant.MenuItemId.TARE_WB);
		initializeScreen();
	}

	private void grossWbMenuAction() {
		TokenManager.initConfig("WEIGH_BRIDGE_IN_TYPE", Type.WorkStationType.WEIGH_BRIDGE_OUT_TYPE);
		TokenManager.materialCat = com.ipssi.rfid.constant.Type.TPRMATERIAL.COAL;// coal
		TokenManager.currWorkStationType = com.ipssi.rfid.constant.Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE;// coal
		loadScreen(ScreenConstant.ScreenLinks.GROSS_WB_WINDOW, ScreenConstant.MenuItemId.GROSS_WB);
		initializeScreen();
	}

	private void loadScreen(String fxmlUrl, String menuId) {
		try {
			centerView = FXMLLoader.load(getClass().getResource(fxmlUrl));
			_selectedMenuId = menuId;
			mainForm.setCenter(centerView);
		} catch (IOException ex) {
			Logger.getLogger(MainController_old.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void actionSave() {
		// ObservableList<Node> parenB = mainForm.getChildren();
		// mainForm.getChildren().remove(2);
		// mainForm.getChildren().add(2, "hello");
		// ObservableList<Node> childsHB = centerView.getChildren();
		// JFXTextField vehicleName = (JFXTextField) childsHB.get(0);
		// JFXTextField minesName = (JFXTextField) childsHB.get(1);
		// JFXTextField tprId = (JFXTextField) childsHB.get(2);
		// JFXTextField transporter = (JFXTextField) childsHB.get(3);
		// showAlert("TextBox Values: ", "vehicleName-" + vehicleName.getText() + "
		// ,transporter-" + transporter.getText() + " ,minesName-" + minesName.getText()
		// + " ,tprId-" + tprId.getText());
	}

	private void initializeScreen() {
		MenuItemInfo menuInfo = MainController.getMenuInfo(_selectedMenuId);
		if (menuInfo != null) {
			labelScreenTitle.setText(menuInfo.getScreenTitle());
			mainActionContainer.setVisible(true);
		}
	}

	private void stopRfid() {
		try {
			// if (rfidHandler != null) {
			// rfidHandler.stop();
			// }
			// if (weighBridge != null) {
			// weighBridge.stopWeighBridge();
			// }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void signOutAction() {
		labelScreenTitle.setText("Welcome To CGPL");
		mainForm.setCenter(null);
	}
}
