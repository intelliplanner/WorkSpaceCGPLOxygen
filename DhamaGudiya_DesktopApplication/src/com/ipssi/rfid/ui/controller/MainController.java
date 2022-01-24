/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ipssi.beans.ComboItemNew;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.beans.MenuItemInfo;
import com.ipssi.rfid.beans.User;
import com.ipssi.rfid.constant.RFIDConstant;
import com.ipssi.rfid.constant.ScreenConstant;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.integration.DigitalClockExt;
import com.ipssi.rfid.logger.RFLogger;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.ui.dao.GateInDao;
import com.ipssi.rfid.ui.data.LovType;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class MainController implements Initializable {

	@FXML
	public BorderPane mainForm;

	@FXML
	public VBox menuTitleMsgContainer;

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
	private JFXButton CONTROL_SETTING;

	@FXML
	private JFXButton CONTROL_TRANSPORTER_DETAILS;

	@FXML
	private JFXButton CONTROL_TAG_READ;

	@FXML
	private JFXButton CONTROL_TPR_DETAILS;

	@FXML
	private JFXButton CONTROL_MPL_RFID_HANDLED_ENTRY;

	@FXML
	public static JFXButton INDICATOR_READER_1;

	@FXML
	public static JFXButton INDICATOR_READER_2;

	@FXML
	public Label labelUsername;

	@FXML
	private JFXButton CONTROL_SIGN_OUT;

	@FXML
	private Label labelScreenTitle;

	@FXML
	private Label LABEL_HSN_NO;

	@FXML
	private HBox panelError;

	@FXML
	public Label labelBlockingReason;

	@FXML
	public HBox mainActionContainer;

	@FXML
	public JFXButton CONTROL_MANUAL;
	@FXML
	public JFXButton CONTROL_SAVE;
	@FXML
	public JFXButton CONTROL_CLEAR;
	@FXML
	private Label DIGITAL_CLOCK;

	public static ControllerI currentViewController = null;
	public User userData = null;
	private int minute;
	private int hour;
	private int second;
	public static HashMap<String, MenuItemInfo> menuScreenMap = new HashMap<>();
	private AnchorPane centerView = null;
	public static String _selectedMenuId = null;
	private Button _selectedMenuControl = null;

	private DigitalClockExt digitalClock = null;
	private Dialog dialogue = null;
	private IdleMonitor idleMonitor = null;
	private StartApplication startApplication = null;

	boolean gateInDisable = false;
	boolean gateOutDisable = false;
	boolean tareDisable = false;
	boolean grossDisable = false;
	boolean settingDisable = false;
	boolean transporterDisable = false;
	boolean readTagDisable = false;
	boolean tprDetailsDisable = false;
	boolean mplRfidHandlerDisable = false;

	boolean labelScreenTitleVisible = false;
	boolean mainMenuContainerVisible = false;
	boolean labelBlockingReasonVisible = false;
	boolean controlSignOutVisible = false;
	boolean mainActionContainerVisible = false;
	boolean labelHSNVisible = false;

	boolean isAlert = false;
	Object alertLock = new Object();

	private Stage initStage = null;
	/**
	 * Initializes the controller class.
	 */
	private static final Logger log = Logger.getLogger(MainController.class.getName());

	// dhamaGodiya
	public static ArrayList<ComboItemNew> transporterList = null;
	public static ArrayList<ComboItem> gradeList = null;
	public static ArrayList<ComboItemNew> doList = null;
	public static ArrayList<ComboItemNew> minesList = null;
	//

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initSystemParameters();
		RFIDConstant.setReaderConfiguration();
		loadScreen(ScreenConstant.ScreenLinks.LOGIN_WINDOW, ScreenConstant.MenuItemId.LOGIN);
		screenComponentVisible(false, false, false, false, false, false);
		initMenuCache();
		if (digitalClock == null) {
			digitalClock = new DigitalClockExt(DIGITAL_CLOCK, "dd/MM/yyyy HH:mm:ss");
		}

	}

	public static MenuItemInfo getMenuInfo(String menuId) {
		return menuScreenMap.get(menuId);
	}

	public void initMenuCache() {// link menu with screens
		// login screen
		MenuItemInfo menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.LOGIN, ScreenConstant.ScreenTitle.LOGIN,
				ScreenConstant.ScreenLinks.LOGIN_WINDOW, false, Misc.getUndefInt(),
				ScreenConstant.MenuItemId.MenuTitle.LOGIN);
		menuScreenMap.put(ScreenConstant.MenuItemId.LOGIN, menuItem);

		// load gate
		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.GATE_IN, ScreenConstant.ScreenTitle.GATE_IN,
				ScreenConstant.ScreenLinks.GATE_IN_WINDOW, true, ScreenConstant.MenuItemId.MENU_PRIV.GATE_IN,
				ScreenConstant.MenuItemId.MenuTitle.GATE_IN);

		menuScreenMap.put(ScreenConstant.MenuItemId.GATE_IN, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.GATE_OUT, ScreenConstant.ScreenTitle.GATE_OUT,
				ScreenConstant.ScreenLinks.GATE_OUT_WINDOW, true, ScreenConstant.MenuItemId.MENU_PRIV.GATE_OUT,
				ScreenConstant.MenuItemId.MenuTitle.GATE_OUT);

		menuScreenMap.put(ScreenConstant.MenuItemId.GATE_OUT, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.TARE_WB, ScreenConstant.ScreenTitle.TARE_WB,
				ScreenConstant.ScreenLinks.TARE_WB_WINDOW, true, ScreenConstant.MenuItemId.MENU_PRIV.TARE_WB,
				ScreenConstant.MenuItemId.MenuTitle.TARE_WB);

		menuScreenMap.put(ScreenConstant.MenuItemId.TARE_WB, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.GROSS_WB, ScreenConstant.ScreenTitle.GROSS_WB,
				ScreenConstant.ScreenLinks.GROSS_WB_WINDOW, true, ScreenConstant.MenuItemId.MENU_PRIV.GROSS_WB,
				ScreenConstant.MenuItemId.MenuTitle.GROSS_WB);

		menuScreenMap.put(ScreenConstant.MenuItemId.GROSS_WB, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.TRANPORTER_WINDOW,
				ScreenConstant.ScreenTitle.TRANPORTER_WINDOW, ScreenConstant.ScreenLinks.TRANPORTER_WINDOW, true,
				ScreenConstant.MenuItemId.MENU_PRIV.TRANPORTER_WINDOW,
				ScreenConstant.MenuItemId.MenuTitle.TRANPORTER_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.TRANPORTER_WINDOW, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.READ_TAG_WINDOW,
				ScreenConstant.ScreenTitle.READ_TAG_WINDOW, ScreenConstant.ScreenLinks.READ_TAG_WINDOW, true,
				ScreenConstant.MenuItemId.MENU_PRIV.READ_TAG_WINDOW,
				ScreenConstant.MenuItemId.MenuTitle.READ_TAG_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.READ_TAG_WINDOW, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.SETTING_WINDOW, ScreenConstant.ScreenTitle.SETTING_WINDOW,
				ScreenConstant.ScreenLinks.SETTING_WINDOW, true, ScreenConstant.MenuItemId.MENU_PRIV.SETTING,
				ScreenConstant.MenuItemId.MenuTitle.SETTING_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.SETTING_WINDOW, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.WELCOME_SCREEN_WINDOW,
				ScreenConstant.ScreenTitle.WELCOME_SCREEN, ScreenConstant.ScreenLinks.WELCOME_SCREEN_WINDOW, true,
				ScreenConstant.MenuItemId.MENU_PRIV.WELCOME_SCREEN,
				ScreenConstant.MenuItemId.MenuTitle.WELCOME_SCREEN_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.WELCOME_SCREEN_WINDOW, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.DATABASE_SETTING_WINDOW,
				ScreenConstant.ScreenTitle.DATABASE_SETTING_WINDOW, ScreenConstant.ScreenLinks.DATABASE_SETTING_WINDOW,
				true, ScreenConstant.MenuItemId.MENU_PRIV.DATABASE_SETTING_WINDOW,
				ScreenConstant.MenuItemId.MenuTitle.DATABASE_SETTING_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.DATABASE_SETTING_WINDOW, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.GATE_IN_SETTING_WINDOW,
				ScreenConstant.ScreenTitle.GATE_IN_SETTING_WINDOW, ScreenConstant.ScreenLinks.GATE_IN_SETTING_WINDOW,
				true, ScreenConstant.MenuItemId.MENU_PRIV.GATE_IN_SETTING_WINDOW,
				ScreenConstant.MenuItemId.MenuTitle.GATE_IN_SETTING_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.GATE_IN_SETTING_WINDOW, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.GATE_OUT_SETTING_WINDOW,
				ScreenConstant.ScreenTitle.GATE_OUT_SETTING_WINDOW, ScreenConstant.ScreenLinks.GATE_OUT_SETTING_WINDOW,
				true, ScreenConstant.MenuItemId.MENU_PRIV.GATE_OUT_SETTING_WINDOW,
				ScreenConstant.MenuItemId.MenuTitle.GATE_OUT_SETTING_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.GATE_OUT_SETTING_WINDOW, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.WB_GROSS_SETTING_WINDOW,
				ScreenConstant.ScreenTitle.WB_GROSS_SETTING_WINDOW, ScreenConstant.ScreenLinks.WB_GROSS_SETTING_WINDOW,
				true, ScreenConstant.MenuItemId.MENU_PRIV.WB_GROSS_SETTING_WINDOW,
				ScreenConstant.MenuItemId.MenuTitle.WB_GROSS_SETTING_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.WB_GROSS_SETTING_WINDOW, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.WB_TARE_SETTING_WINDOW,
				ScreenConstant.ScreenTitle.WB_TARE_SETTING_WINDOW, ScreenConstant.ScreenLinks.WB_TARE_SETTING_WINDOW,
				true, ScreenConstant.MenuItemId.MENU_PRIV.WB_TARE_SETTING_WINDOW,
				ScreenConstant.MenuItemId.MenuTitle.WB_TARE_SETTING_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.WB_TARE_SETTING_WINDOW, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.SYSTEM_CONFIG_SETTING_WINDOW,
				ScreenConstant.ScreenTitle.SYSTEM_CONFIG_SETTING_WINDOW,
				ScreenConstant.ScreenLinks.SYSTEM_CONFIG_SETTING_WINDOW, true,
				ScreenConstant.MenuItemId.MENU_PRIV.SYSTEM_CONFIG_SETTING_WINDOW,
				ScreenConstant.MenuItemId.MenuTitle.SYSTEM_CONFIG_SETTING_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.SYSTEM_CONFIG_SETTING_WINDOW, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.RFID_READER_SETTING_WINDOW,
				ScreenConstant.ScreenTitle.RFID_READER_SETTING_WINDOW,
				ScreenConstant.ScreenLinks.RFID_READER_SETTING_WINDOW, true,
				ScreenConstant.MenuItemId.MENU_PRIV.RFID_READER_SETTING_WINDOW,
				ScreenConstant.MenuItemId.MenuTitle.RFID_READER_SETTING_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.RFID_READER_SETTING_WINDOW, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.WEIGHBRIDGE_SETTING_WINDOW,
				ScreenConstant.ScreenTitle.WEIGHBRIDGE_SETTING_WINDOW,
				ScreenConstant.ScreenLinks.WEIGHBRIDGE_SETTING_WINDOW, true,
				ScreenConstant.MenuItemId.MENU_PRIV.WEIGHBRIDGE_SETTING_WINDOW,
				ScreenConstant.MenuItemId.MenuTitle.WEIGHBRIDGE_SETTING_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.WEIGHBRIDGE_SETTING_WINDOW, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.BARRIER_SETTING_WINDOW,
				ScreenConstant.ScreenTitle.BARRIER_SETTING_WINDOW, ScreenConstant.ScreenLinks.BARRIER_SETTING_WINDOW,
				true, ScreenConstant.MenuItemId.MENU_PRIV.BARRIER_SETTING_WINDOW,
				ScreenConstant.MenuItemId.MenuTitle.BARRIER_SETTING_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.BARRIER_SETTING_WINDOW, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.TPR_DETAILS_WINDOW,
				ScreenConstant.ScreenTitle.TPR_DETAILS_WINDOW, ScreenConstant.ScreenLinks.TPR_DETAILS_WINDOW, true,
				ScreenConstant.MenuItemId.MENU_PRIV.TPR_DETAILS_WINDOW,
				ScreenConstant.MenuItemId.MenuTitle.TPR_DETAILS_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.TPR_DETAILS_WINDOW, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.MPL_RFID_HANDLER_WINDOW,
				ScreenConstant.ScreenTitle.MPL_RFID_HANDLER_WINDOW, ScreenConstant.ScreenLinks.MPL_RFID_HANDLER_WINDOW,
				true, ScreenConstant.MenuItemId.MENU_PRIV.MPL_RFID_HANDLER_WINDOW,
				ScreenConstant.MenuItemId.MenuTitle.MPL_RFID_HANDLER_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.MPL_RFID_HANDLER_WINDOW, menuItem);

	}

	public void initSystemParameters() {
		TokenManager.initSystemConfig();
		if (!TokenManager.isDebug) {
			RFLogger.init();
			RFLogger.RouteStdOutErrToFile();
		}
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
	private void controlItemClicked(MouseEvent event) {
		Button control = ((Button) event.getSource());
		handleActionControl(control);
	}

	public void initializeMenuControlls() {
		if (userData != null) {
			if (userData.isSupperUser() && _selectedMenuId != ScreenConstant.MenuItemId.MAIN_WINDOW) {
				// screenComponentVisible(false, true, false, false, false, false);
				// setMenuDisable(false, false, false, false, false, false,false);
				mainMenuContainerVisible = true;
				screenComponentVisible(labelScreenTitleVisible, mainMenuContainerVisible, labelBlockingReasonVisible,
						controlSignOutVisible, mainActionContainerVisible, labelHSNVisible);

				if (TokenManager.PROJECT_AREA != LovType.PROJECT_SIDE.CGPL) {
					gateInDisable = true;
					tareDisable = true;
					grossDisable = true;
					gateOutDisable = true;
					transporterDisable = true;
					readTagDisable = true;
					tprDetailsDisable = true;
//					settingDisable=true;
//					mplRfidHandlerDisable=true;
				} 					
				setMenuDisable(gateInDisable, tareDisable, grossDisable, gateOutDisable, settingDisable,
						transporterDisable, readTagDisable, tprDetailsDisable, mplRfidHandlerDisable);

			} else if (userData.getPrivList() != null && userData.getPrivList().size() > 0) {
				mainMenuContainerVisible = true;
				gateInDisable = gateInDisable ? true
						: userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.GATE_IN) ? false : true;
				tareDisable = tareDisable ? true
						: userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.TARE_WB) ? false : true;
				grossDisable = grossDisable ? true
						: userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.GROSS_WB) ? false : true;
				gateOutDisable = gateOutDisable ? true
						: userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.GATE_OUT) ? false : true;
				settingDisable = settingDisable ? true
						: userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.SETTING) ? false : true;
				transporterDisable = transporterDisable ? true
						: userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.TRANPORTER_WINDOW) ? false
								: true;
				readTagDisable = readTagDisable ? true
						: userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.READ_TAG_WINDOW) ? false
								: true;
				tprDetailsDisable = tprDetailsDisable ? true
						: userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.TPR_DETAILS_WINDOW)
								? false
								: true;
				mplRfidHandlerDisable = mplRfidHandlerDisable ? true
						: userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.MPL_RFID_HANDLER_WINDOW)
								? false
								: true;
				screenComponentVisible(labelScreenTitleVisible, mainMenuContainerVisible, labelBlockingReasonVisible,
						controlSignOutVisible, mainActionContainerVisible, labelHSNVisible);

				setMenuDisable(gateInDisable, tareDisable, grossDisable, gateOutDisable, settingDisable,
						transporterDisable, readTagDisable, tprDetailsDisable, mplRfidHandlerDisable);

			} else {
				screenComponentVisible(false, true, false, false, true, false);
				setMenuDisable(true, true, true, true, true, true, true, true, true);

				labelBlockingReason.setText("Please Contact with Control Room for getting menu privelege");
			}
		}
		
		
		if (TokenManager.PROJECT_AREA != LovType.PROJECT_SIDE.CGPL) { // DhamaGudiya
			setMenuVisible(false, false, false, false, true, false,false, false, true);
			CONTROL_MANUAL.setVisible(true);
		}else {
			setMenuVisible(true, true, true, true, true, true, true, true, true);
			
		}
		 
	}

	private void handleActionControl(Button control) {

		if (control == null || control.isDisable() || !control.isVisible()) {
			return;
		}
		String controlId = control.getId().toUpperCase();
		switch (controlId) {
		case "CONTROL_CLEAR":
			clearInputsAction();
			break;
		case "CONTROL_SAVE":
			saveAction();
			break;
		case "CONTROL_MANUAL":
			 manualControlAction();
			break;
		case "CONTROL_GATE_IN":
			// setMenuDisable(false);
			gateInMenuAction();
			break;

		case "CONTROL_GATE_OUT":
			gateOutMenuAction();
			break;

		case "CONTROL_TARE_WB":
			tareWBMenuAction();
			break;

		case "CONTROL_GROSS_WB":
			grossWbMenuAction();
			break;
		case "CONTROL_SIGN_OUT":
			signOutAction();
			break;
		case "CONTROL_SETTING":
			settingMenuAction();
			break;
		case "CONTROL_TRANSPORTER_DETAILS":
			transporterMenuAction();
			break;
		case "CONTROL_TAG_READ":
			tagReadMenuAction();
			break;
		case "CONTROL_TPR_DETAILS":
			tprDetailsMenuAction();
			break;

		case "CONTROL_MPL_RFID_HANDLED_ENTRY":
			// setMenuDisable(false);
			mplRfidHandlerMenuAction();
			break;
		default:
			break;
		}
	}

	

	private void tagReadMenuAction() {
		stopRFID();
		clearInputsAction();

		loadScreen(ScreenConstant.ScreenLinks.READ_TAG_WINDOW, ScreenConstant.MenuItemId.READ_TAG_WINDOW);
		// screenComponentVisible(true, true, true, true, true, true);
		// setMenuDisable(false, false, false, false, false, false,true);

		readTagDisable = true;
		labelScreenTitleVisible = true;
		mainMenuContainerVisible = true;
		controlSignOutVisible = true;
		mainActionContainerVisible = true;

		initializeMenuControlls();
		changeSaveConrollText("Read Tag");

		if (TokenManager.tagIdentifyManually) {
			setControllerDisable(false);
		} else {
			setControllerDisable(true);
		}

		// setMainActionContainerControllerVisible(TokenManager.forceManual, true,
		// true);
	}

	private void mplRfidHandlerMenuAction() {
		stopRFID();
		clearInputsAction();
		CONTROL_SAVE.setDisable(false);
		int DHAMA_GUDIYA_HANDLED = 51;
		// TokenManager.initConfig("MPL_RFID_HANDLER_TYPE",
		// Type.WorkStationType.DHAMA_GUDIYA_HANDLED);
		TokenManager.initConfig("MPL_RFID_HANDLER_TYPE", DHAMA_GUDIYA_HANDLED);
		TokenManager.currWorkStationType = DHAMA_GUDIYA_HANDLED;// coal
		loadScreen(ScreenConstant.ScreenLinks.MPL_RFID_HANDLER_WINDOW,
				ScreenConstant.MenuItemId.MPL_RFID_HANDLER_WINDOW);
		mplRfidHandlerDisable = true;
		labelScreenTitleVisible = true;
		mainMenuContainerVisible = true;
		labelBlockingReasonVisible = false;
		controlSignOutVisible = true;
		mainActionContainerVisible = true;
		labelHSNVisible = false;

		initializeMenuControlls();
	}

	private void tprDetailsMenuAction() {
		stopRFID();
		clearInputsAction();
		// TokenManager.initConfig("GATE_OUT_TYPE",
		// Type.WorkStationType.CGPL_LOAD_GATE_OUT);
		// TokenManager.currWorkStationType =
		// com.ipssi.rfid.constant.Type.WorkStationType.CGPL_LOAD_GATE_OUT;// coal
		loadScreen(ScreenConstant.ScreenLinks.TPR_DETAILS_WINDOW, ScreenConstant.MenuItemId.TPR_DETAILS_WINDOW);
		tprDetailsDisable = true;
		labelScreenTitleVisible = true;
		mainMenuContainerVisible = true;
		labelBlockingReasonVisible = false;
		controlSignOutVisible = true;
		mainActionContainerVisible = false;
		labelHSNVisible = false;

		initializeMenuControlls();
	}

	private void gateInMenuAction() {
		stopRFID();
		clearInputsAction();
		CONTROL_SAVE.setDisable(false);
		TokenManager.initConfig("GATE_IN_TYPE", Type.WorkStationType.CGPL_LOAD_GATE_IN);
		TokenManager.currWorkStationType = com.ipssi.rfid.constant.Type.WorkStationType.CGPL_LOAD_GATE_IN;// coal
		loadScreen(ScreenConstant.ScreenLinks.GATE_IN_WINDOW, ScreenConstant.MenuItemId.GATE_IN);
		// screenComponentVisible(true, true, true, true, true, true);
		// setMenuDisable(true, false, false, false, false, false,false);

		gateInDisable = true;
		labelScreenTitleVisible = true;
		mainMenuContainerVisible = true;
		labelBlockingReasonVisible = true;
		controlSignOutVisible = true;
		mainActionContainerVisible = true;
		labelHSNVisible = true;

		initializeMenuControlls();
		// setMainActionContainerControllerVisible(TokenManager.forceManual, true,
		// true);
	}

	private void gateOutMenuAction() {
		stopRFID();
		clearInputsAction();
		CONTROL_SAVE.setDisable(false);
		TokenManager.initConfig("GATE_OUT_TYPE", Type.WorkStationType.CGPL_LOAD_GATE_OUT);
		TokenManager.currWorkStationType = com.ipssi.rfid.constant.Type.WorkStationType.CGPL_LOAD_GATE_OUT;// coal
		loadScreen(ScreenConstant.ScreenLinks.GATE_OUT_WINDOW, ScreenConstant.MenuItemId.GATE_OUT);
		gateOutDisable = true;

		labelScreenTitleVisible = true;
		mainMenuContainerVisible = true;
		labelBlockingReasonVisible = true;
		controlSignOutVisible = true;
		mainActionContainerVisible = true;
		labelHSNVisible = true;

		initializeMenuControlls();
		// screenComponentVisible(true, true, true, true, true, true);
		// setMenuDisable(false, false, false, true, false, false, false);
		// setMainActionContainerControllerVisible(TokenManager.forceManual, true,
		// true);
	}

	private void tareWBMenuAction() {
		stopRFID();
		clearInputsAction();
		CONTROL_SAVE.setDisable(false);
		TokenManager.initConfig("WEIGH_BRIDGE_OUT_TYPE", Type.WorkStationType.CGPL_LOAD_WB_OUT);
		TokenManager.currWorkStationType = com.ipssi.rfid.constant.Type.WorkStationType.CGPL_LOAD_WB_OUT;// coal
		loadScreen(ScreenConstant.ScreenLinks.TARE_WB_WINDOW, ScreenConstant.MenuItemId.TARE_WB);

		tareDisable = true;
		labelScreenTitleVisible = true;
		mainMenuContainerVisible = true;
		labelBlockingReasonVisible = true;
		controlSignOutVisible = true;
		mainActionContainerVisible = true;
		labelHSNVisible = true;
		initializeMenuControlls();
		// screenComponentVisible(true, true, true, true, true, true);
		// setMenuDisable(false, true, false, false, false, false, false);
		// setMainActionContainerControllerVisible(TokenManager.forceManual, true,
		// true);
	}

	private void grossWbMenuAction() {
		stopRFID();
		clearInputsAction();
		CONTROL_SAVE.setDisable(false);
		TokenManager.initConfig("WEIGH_BRIDGE_IN_TYPE", Type.WorkStationType.CGPL_LOAD_WB_IN);
		TokenManager.currWorkStationType = com.ipssi.rfid.constant.Type.WorkStationType.CGPL_LOAD_WB_IN;// coal
		loadScreen(ScreenConstant.ScreenLinks.GROSS_WB_WINDOW, ScreenConstant.MenuItemId.GROSS_WB);

		grossDisable = true;
		labelScreenTitleVisible = true;
		mainMenuContainerVisible = true;
		labelBlockingReasonVisible = true;
		controlSignOutVisible = true;
		mainActionContainerVisible = true;
		labelHSNVisible = true;

		initializeMenuControlls();
		// screenComponentVisible(true, true, true, true, true, true);
		// setMenuDisable(false, false, true, false, false, false, false);
		// setMainActionContainerControllerVisible(TokenManager.forceManual, true,
		// true);
	}

	private void settingMenuAction() {
		stopRFID();
		clearInputsAction();
		loadScreen(ScreenConstant.ScreenLinks.SETTING_WINDOW, ScreenConstant.MenuItemId.SETTING_WINDOW);
		settingDisable = true;
		gateInDisable = true;
		gateOutDisable = true;
		tareDisable = true;
		grossDisable = true;
		transporterDisable = true;
		readTagDisable = true;
		tprDetailsDisable = true;
		controlSignOutVisible = true;
		mplRfidHandlerDisable = true;
		initializeMenuControlls();
		// screenComponentVisible(true, true, false, true, false, false);
		// setMenuVisible(false, false, false, false, false, false, false);
	}

	private void transporterMenuAction() {
		stopRFID();
		clearInputsAction();
		CONTROL_SAVE.setDisable(false);
		loadScreen(ScreenConstant.ScreenLinks.TRANPORTER_WINDOW, ScreenConstant.MenuItemId.TRANPORTER_WINDOW);
		transporterDisable = true;
		labelScreenTitleVisible = true;
		// mainMenuContainerVisible = true;
		controlSignOutVisible = true;
		mainActionContainerVisible = true;
		labelBlockingReasonVisible = true;
		initializeMenuControlls();

		// screenComponentVisible(true, true, true, true, true, true);
		// setMenuDisable(false,false,false,false,false,true, false);
	}

	public void loadScreen(String fxmlUrl, String menuId) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlUrl));
			this.centerView = fxmlLoader.load();
			this.currentViewController = fxmlLoader.getController();
			this.currentViewController.init(this);

			mainForm.setCenter(centerView);
			_selectedMenuId = menuId;
			initializeScreen();
			changeContainerColor(menuId);
			this.currentViewController.enableManualEntry(TokenManager.forceManual);
			this.currentViewController.requestFocusNextField();
			changeSaveConrollText("Save");
		} catch (IOException ex) {
			Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void loadDialogueScreen(String fxmlUrl, String menuId) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource(fxmlUrl));

			dialogue = new Dialog();
			dialogue.setOnCloseRequest(e -> dialogExit());
			dialogue.getDialogPane().setContent(root);
			dialogue.initStyle(StageStyle.TRANSPARENT);
			dialogue.show();
			_selectedMenuId = menuId;
			// initializeScreen();
			changeContainerColor(menuId);
		} catch (IOException ex) {
			Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void signOutAction() {
		stopRFID();
		clearInputsAction();
		loadScreen(ScreenConstant.ScreenLinks.LOGIN_WINDOW, ScreenConstant.MenuItemId.LOGIN);
		screenComponentVisible(false, false, false, false, false, false);
	}

	public void stopRFID() {
		try {
			this.currentViewController.stopRfid();
			this.currentViewController.stopSyncTprService();
			digitalClock.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void changeContainerColor(String menuId) {
		if (menuId.equalsIgnoreCase(ScreenConstant.MenuItemId.LOGIN)) {
			menuTitleMsgContainer.setStyle("-fx-background-color: #005A9C;");
			mainActionContainer.setStyle("-fx-background-color: #DDDDDD;");
			mainForm.setStyle("-fx-background-color: #DDDDDD;");
		} else {
			menuTitleMsgContainer.setStyle("-fx-background-color: #ffffff;");
			mainForm.setStyle("-fx-background-color: #ffffff;");
			mainActionContainer.setStyle("-fx-background-color: #ffffff;");
		}
	}

	public void screenComponentVisible(boolean screenTitleVisible, boolean mainMenuContainerVisible,
			boolean blockingReasonLabelVisible, boolean signOutControlerVisible, boolean mainActionContainerVisible,
			boolean hsnNoVisible) {
		labelScreenTitle.setVisible(screenTitleVisible);
		mainMenuContainer.setVisible(mainMenuContainerVisible);
		labelBlockingReason.setVisible(blockingReasonLabelVisible);
		CONTROL_SIGN_OUT.setVisible(signOutControlerVisible);
		mainActionContainer.setVisible(mainActionContainerVisible);
		LABEL_HSN_NO.setVisible(hsnNoVisible);
	}

	public void setMenuDisable(boolean gateInVisible, boolean tareVisible, boolean grossVisible, boolean gateOutVisible,
			boolean settingVisible, boolean transporterMenuVisible, boolean tagReadMenuVisible,
			boolean tprDetailsMenuDisable, boolean mplRfidHandlerDisable) {
		CONTROL_GATE_IN.setDisable(gateInVisible);
		CONTROL_TARE_WB.setDisable(tareVisible);
		CONTROL_GROSS_WB.setDisable(grossVisible);
		CONTROL_GATE_OUT.setDisable(gateOutVisible);
		CONTROL_SETTING.setDisable(settingVisible);
		CONTROL_TAG_READ.setDisable(tagReadMenuVisible);
		CONTROL_TRANSPORTER_DETAILS.setDisable(transporterMenuVisible);
		CONTROL_TPR_DETAILS.setDisable(tprDetailsMenuDisable);
		CONTROL_MPL_RFID_HANDLED_ENTRY.setDisable(mplRfidHandlerDisable);
	}

	public void setMenuVisible(boolean gateInVisible, boolean tareVisible, boolean grossVisible, boolean gateOutVisible,
			boolean settingMenuVisible, boolean transporterMenuVisible, boolean tagReadMenuVisible,
			boolean tprDetailsMenuVisible, boolean mplRfidHandlerDisable) {
		CONTROL_GATE_IN.setVisible(gateInVisible);
		CONTROL_TARE_WB.setVisible(tareVisible);
		CONTROL_GROSS_WB.setVisible(grossVisible);
		CONTROL_GATE_OUT.setVisible(gateOutVisible);
		CONTROL_SETTING.setVisible(settingMenuVisible);
		CONTROL_TAG_READ.setVisible(tagReadMenuVisible);
		CONTROL_TRANSPORTER_DETAILS.setVisible(transporterMenuVisible);
		CONTROL_TPR_DETAILS.setVisible(tprDetailsMenuVisible);
		CONTROL_MPL_RFID_HANDLED_ENTRY.setVisible(mplRfidHandlerDisable);
	}

	// private void setMainActionContainerControllerVisible(boolean
	// manualControlVisible, boolean saveControlVisible,
	// boolean clearControlVisible) {
	// CONTROL_MANUAL.setVisible(false);
	// CONTROL_SAVE.setVisible(saveControlVisible);
	// CONTROL_CLEAR.setVisible(clearControlVisible);
	// }

	private void clearInputsAction() {
		if (this.currentViewController != null) {
			this.currentViewController.clearInputs();
		}
		labelBlockingReason.setText("");
		readTagDisable = false;
		gateInDisable = false;
		gateOutDisable = false;
		tareDisable = false;
		grossDisable = false;
		settingDisable = false;
		transporterDisable = false;
		readTagDisable = false;
		tprDetailsDisable = false;
		mplRfidHandlerDisable = false;
		labelScreenTitleVisible = false;
		mainMenuContainerVisible = false;
		labelBlockingReasonVisible = false;
		controlSignOutVisible = false;
		mainActionContainerVisible = false;
		labelHSNVisible = false;

	}

	private void saveAction() {
		if (!this.currentViewController.save()) {
			// this.showAlert(Alert.AlertType.ERROR, "Message",
			// UIConstant.SAVE_FAILER_MESSAGE);
		}

	}
	
	private void manualControlAction() {
		if(this.currentViewController!=null)
			this.currentViewController.enableManualEntry(true);
	}

	public void showAlert(String title, String message) {
		showAlert(Alert.AlertType.INFORMATION, title, message);
	}

	public void showAlert(Alert.AlertType alertType, String title, String message) {
		if (isAlert) {
			return;
		}
		synchronized (alertLock) {
			System.out.println(Thread.currentThread().toString() + "[Alert]" + message);
			isAlert = true;
			Alert alert = new Alert(alertType);
			alert.setTitle(title);
			alert.setContentText(message);
			alert.setX(10.0);
			alert.setY(10.0);
			alert.showAndWait();
			isAlert = false;
		}
	}

	public int promptNewTest(String title, String message, String[] options) {
		if (isAlert) {
			return Misc.getUndefInt();
		}
		int retval = Misc.getUndefInt();
		synchronized (alertLock) {
			try {
				isAlert = true;
				ArrayList<ButtonType> optionButtons = null;
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle(title);
				alert.setContentText(message);
				for (int i = 0, is = options == null ? 0 : options.length; i < is; i++) {
					if (optionButtons == null) {
						optionButtons = new ArrayList<>();
					}
					ButtonType bt = new ButtonType(options[i]);
					optionButtons.add(bt);
					alert.getDialogPane().getButtonTypes().set(i, bt);
					Button optionButton = (Button) alert.getDialogPane().lookupButton(bt);
					optionButton.setOnKeyPressed(new EventHandler<KeyEvent>() {
						@Override
						public void handle(KeyEvent event) {
							if (event.getCode() == KeyCode.ENTER) {
								alert.setResult(bt);
							}
						}
					});
				}
				if (optionButtons == null) {
					return retval;
				}
				Optional<ButtonType> result = alert.showAndWait();
				for (int i = 0, is = optionButtons == null ? 0 : optionButtons.size(); i < is; i++) {
					if (optionButtons.get(i) == result.get()) {
						return i;
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				isAlert = false;
			}
		}
		return retval;
	}

	public int prompt(String title, String message, String[] options) {
		return prompt(title, message, options, Misc.getUndefDouble(), Misc.getUndefDouble());
	}

	public int prompt(String title, String message, String[] options, double x, double y) {
		if (isAlert) {
			return Misc.getUndefInt();
		}
		int retval = Misc.getUndefInt();
		synchronized (alertLock) {
			try {
				System.out.println(Thread.currentThread().toString() + "[prompt]:" + message);
				isAlert = true;
				ArrayList<ButtonType> optionButtons = null;
				for (int i = 0, is = options == null ? 0 : options.length; i < is; i++) {
					if (optionButtons == null) {
						optionButtons = new ArrayList<>();
					}
					optionButtons.add(new ButtonType(options[i]));
				}
				if (optionButtons == null) {
					return retval;
				}
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle(title);
				if (!Misc.isUndef(x)) {
					alert.setX(x);
				}
				if (!Misc.isUndef(y)) {
					alert.setY(y);
				}
				alert.setContentText(message);
				alert.getButtonTypes().setAll(optionButtons);
				Optional<ButtonType> result = alert.showAndWait();

				for (int i = 0, is = optionButtons == null ? 0 : optionButtons.size(); i < is; i++) {
					if (optionButtons.get(i) == result.get()) {
						System.out.println(Thread.currentThread().toString() + "[PROMPT][RESULT]:" + i);
						return i;
					}

				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				isAlert = false;
			}
		}
		System.out.println(Thread.currentThread().toString() + "[PROMPT][RESULT]:" + retval);
		return retval;
	}

	private void initializeScreen() {
		MenuItemInfo menuInfo = getMenuInfo(_selectedMenuId);
		if (menuInfo != null) {
			setScreenTitle(menuInfo.getScreenTitle());
			setHSN();
		}
		if (digitalClock != null && !mainActionContainer.isDisable()
				&& !_selectedMenuId.equalsIgnoreCase(ScreenConstant.MenuItemId.LOGIN)
				&& !_selectedMenuId.equalsIgnoreCase(ScreenConstant.MenuItemId.MAIN_WINDOW)) {
			digitalClock.play();
		}
		CONTROL_MANUAL.setVisible(false);
	}

	public void closePariparrels() {
		if (this.currentViewController != null) {
			// this.currentViewController.stopRfid();
			stopRFID();
		}
		GateInDao.forceSignut(TokenManager.userId, TokenManager.srcType, Integer.toString(TokenManager.systemId));
		// digitalClock.stop();
	}

	private void dialogExit() {
		if (dialogue != null) {
			dialogue.close();
		}
	}

	private void setScreenTitle(String screenTitle) {
		labelScreenTitle.setText(screenTitle);
	}

	private void setHSN() {
		LABEL_HSN_NO.setText("HSN: " + TokenManager.HSN_NO);
	}

	public static String getSourceId(Event event) {
		if (event == null) {
			return null;
		}
		String controllerId = null;
		if (event.getTarget() instanceof JFXTextField) {
			controllerId = ((JFXTextField) event.getSource()).getId();
		} else if (event.getTarget() instanceof JFXComboBox) {
			controllerId = ((JFXComboBox) event.getSource()).getId();
		} else if (event.getTarget() instanceof JFXCheckBox) {
			controllerId = ((JFXCheckBox) event.getSource()).getId();
		} else if (event.getTarget() instanceof JFXButton) {
			controllerId = ((JFXButton) event.getSource()).getId();
		} else if (event.getTarget() instanceof Button) {
			controllerId = ((Button) event.getSource()).getId();
		} else if (event.getTarget() instanceof TextField) {
			controllerId = ((TextField) event.getSource()).getId();
		} else if (event.getTarget() instanceof ComboBox) {
			controllerId = ((ComboBox) event.getSource()).getId();
		} else if (event.getTarget() instanceof CheckBox) {
			controllerId = ((CheckBox) event.getSource()).getId();
		} else if (event.getTarget() instanceof JFXTextArea) {
			controllerId = ((JFXTextArea) event.getSource()).getId();
		} else {
			if (event.getSource() instanceof JFXButton) {
				controllerId = ((JFXButton) event.getSource()).getId();
			}
		}
		return controllerId;
	}

	Object getSource(Event event) {
		if (event == null) {
			return null;
		}
		Object control = null;
		if (event.getTarget() instanceof JFXTextField) {
			control = (JFXTextField) event.getSource();
		} else if (event.getTarget() instanceof JFXComboBox) {
			control = (JFXComboBox) event.getSource();
		} else if (event.getTarget() instanceof JFXCheckBox) {
			control = (JFXCheckBox) event.getSource();
		} else if (event.getTarget() instanceof JFXButton) {
			control = (JFXButton) event.getSource();
		} else if (event.getTarget() instanceof Button) {
			control = (Button) event.getSource();
		} else if (event.getTarget() instanceof TextField) {
			control = (TextField) event.getSource();
		} else if (event.getTarget() instanceof ComboBox) {
			control = (ComboBox) event.getSource();
		} else if (event.getTarget() instanceof CheckBox) {
			control = (CheckBox) event.getSource();
		}
		return control;
	}

	public void setControllerDisable(boolean isTrue) {
		CONTROL_SAVE.setDisable(isTrue);
	}

	private void changeSaveConrollText(String tagName) {
		if (!CONTROL_SAVE.isDisable() && CONTROL_SAVE.isVisible())
			this.CONTROL_SAVE.setText(tagName);

	}

	public void changeApplicationColor(String color) {
		Platform.runLater(() -> {
			if (TokenManager.SELECTED_COLOR != null && color.equalsIgnoreCase("RED")) {
				// showAlert(Alert.AlertType.CONFIRMATION, "COLOR", "RED");

				LoginController.TopPane.getStyleClass().removeAll("topPane");
				LoginController.TopPane.getStyleClass().add("topPaneRed");
				;// setStyle("fx-background-color: #d01212");

				// CONTROL_LOGIN
				// TopPane
				// LOGIN_IMAGE
			} else if (TokenManager.SELECTED_COLOR != null && color.equalsIgnoreCase("BLUE")) {
				// showAlert(Alert.AlertType.CONFIRMATION, "COLOR", "BLUE");
				// Platform.runLater(() -> {
				LoginController.TopPane.setStyle("fx-background-color: blue");
				// });
				// CONTROL_LOGIN
				// LOGIN_IMAGE
				// TopPane
			} else {
				return;
			}
		});

	}

	public void registerIdleMonitor() {
		idleMonitor = new IdleMonitor(Duration.seconds(10), () -> showIdleMessage());
		this.startApplication.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				idleMonitor.notIdle();
			}
		});
		this.startApplication.getScene().addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				idleMonitor.notIdle();
			}
		});

		this.startApplication.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				onControlKeyPress(event);
			}
		});

	}

	private void showIdleMessage() {
		Platform.runLater(() -> {
			this.showAlert(Alert.AlertType.ERROR, "Message", "LOG OUT Action");
		});
	}

	public void init(StartApplication startApplication) {
		this.startApplication = startApplication;
	}

}
