/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.MenuItemInfo;
import com.ipssi.rfid.beans.User;
import com.ipssi.rfid.constant.RFIDConstant;
import com.ipssi.rfid.constant.ScreenConstant;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.integration.DigitalClockExt;
import com.ipssi.rfid.logger.RFLogger;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.ui.controller.service.ControllerI;
import com.ipssi.rfid.ui.controller.service.MainControllerI;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Vicky
 */
public class MainController implements Initializable, MainControllerI {

	@FXML
	private BorderPane mainForm;
	@FXML
	private VBox menuTitleMsgContainer;
	@FXML
	private HBox mainMenuContainer;
	@FXML
	private JFXButton CONTROL_ISSUE_TAG;
	@FXML
	private JFXButton CONTROL_GATE_IN;
	@FXML
	private JFXButton CONTROL_TARE_WB;
	@FXML
	private JFXButton CONTROL_GROSS_WB;
	@FXML
	private JFXButton CONTROL_GATE_OUT;
	@FXML
	private JFXButton CONTROL_TAG_READ;
	@FXML
	private JFXButton CONTROL_SETTING;
	@FXML
	private JFXButton CONTROL_TPR_DETAILS;
	@FXML
	private JFXButton CONTROL_SALES_ORDER;
	@FXML
	public Label labelUsername;
	@FXML
	private JFXButton CONTROL_SIGN_OUT;
	@FXML
	private Label labelScreenTitle;
	@FXML
	private HBox panelError;
	@FXML
	public Label labelBlockingReason;
	@FXML
	private HBox mainActionContainer;
	@FXML
	public JFXButton CONTROL_MANUAL;
	@FXML
	public JFXButton CONTROL_SAVE;
	@FXML
	public JFXButton CONTROL_CLEAR;
	@FXML
	private Label DIGITAL_CLOCK;

	@FXML
	private HBox Main_Window_Tilte_Hbox;

	public StartApplicationPPGCL startApplicationPPGCL;
	private AnchorPane centerView = null;
	public static ControllerI currentViewController = null;
	public static String _selectedMenuId = null;
	private Button _selectedMenuControl = null;
	public static HashMap<String, MenuItemInfo> menuScreenMap = new HashMap<>();
	boolean isAlert = false;
	Object alertLock = new Object();
	public User userData = null;

	boolean issueTagDisable = false;
	boolean gateInDisable = false;
	boolean gateOutDisable = false;
	boolean tareDisable = false;
	boolean grossDisable = false;
	boolean settingDisable = false;
	boolean transporterDisable = false;
	boolean readTagDisable = false;
	boolean tprDetailsDisable = false;
	boolean salesOrderDisable = false;
	public boolean createInvoiceDisable = false;

	private DigitalClockExt digitalClock = null;
	boolean labelScreenTitleVisible = false;
	boolean mainMenuContainerVisible = false;
	boolean labelBlockingReasonVisible = false;
	boolean controlSignOutVisible = false;
	boolean mainActionContainerVisible = false;

	private static final Logger log = Logger.getLogger(StartApplicationPPGCL.class.getName());

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// loadScreen(ScreenConstant.ScreenLinks.LOGIN_WINDOW,
		// ScreenConstant.MenuItemId.LOGIN);
		// initMenuCache();
		// screenComponentVisible(false, false, false, false, false);
		initSystemParameters();
		// TokenManager.initDatabaseConfig();
		RFIDConstant.setReaderConfiguration();
		loadScreen(ScreenConstant.ScreenLinks.LOGIN_WINDOW, ScreenConstant.MenuItemId.LOGIN);
		screenComponentVisible(false, false, false, false, false);
		initMenuCache();
		if (digitalClock == null) {
			digitalClock = new DigitalClockExt(DIGITAL_CLOCK, "dd/MM/yyyy HH:mm:ss");
		}
	}

	public void initSystemParameters() {
		TokenManager.initSystemConfig();
		if (!TokenManager.isDebug) {
			RFLogger.init();
			RFLogger.RouteStdOutErrToFile();
		}
	}

	@Override
	public void init(StartApplicationPPGCL startApplicationPPGCL) {
		this.startApplicationPPGCL = startApplicationPPGCL;
	}

	@Override
	public void loadScreen(String fxmlUrl, String menuId) {
		try {

			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlUrl));
			this.centerView = fxmlLoader.load();

			setApplicationAsScreenResolution(fxmlLoader, menuId);

			this.currentViewController = fxmlLoader.getController();

			this.currentViewController.init(this);
			this._selectedMenuId = menuId;
			mainForm.setCenter(centerView);
			initializeScreen();
			this.currentViewController.enableManualEntry(TokenManager.forceManual);
			this.currentViewController.requestFocusNextField();
			// changeSaveConrollText("Save");
		} catch (IOException ex) {
			log.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void setApplicationAsScreenResolution(FXMLLoader fxmlLoader, String menuId) {
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		if (!menuId.equalsIgnoreCase(ScreenConstant.MenuItemId.WELCOME_SCREEN_WINDOW)
				&& !menuId.equalsIgnoreCase(ScreenConstant.MenuItemId.SETTING_WINDOW)
				&& !menuId.equalsIgnoreCase(ScreenConstant.MenuItemId.LOGIN)) {
			double widh = screenSize.getWidth();
			double heigt = screenSize.getHeight();
			this.centerView.setMaxWidth(widh);
			this.centerView.setMaxHeight(heigt);
			this.centerView.setLayoutY(50);
		}
		if (menuId.equalsIgnoreCase(ScreenConstant.MenuItemId.LOGIN)) {
			HBox loginHbox = (HBox) fxmlLoader.getNamespace().get("LOGIN_HBOX");
			HBox loginTitleHbox = (HBox) fxmlLoader.getNamespace().get("LOGIN_TITLE_HBOX");
			AnchorPane topPane = (AnchorPane) fxmlLoader.getNamespace().get("TopPane");
			topPane.setPrefHeight(screenSize.getHeight() / 4);
			topPane.setPrefWidth(screenSize.getWidth());
			loginHbox.setPrefWidth(screenSize.getWidth());
			loginTitleHbox.setPrefWidth(screenSize.getWidth());
		}

		if (menuId.equalsIgnoreCase(ScreenConstant.MenuItemId.WELCOME_SCREEN_WINDOW)) {
			VBox WELCOME_VBOX = (VBox) fxmlLoader.getNamespace().get("WELCOME_VBOX");
			WELCOME_VBOX.setPrefHeight(screenSize.getHeight() / 2);
			WELCOME_VBOX.setPrefWidth(screenSize.getWidth());
		}

	}

	@Override
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

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.SALES_ORDER_WINDOW,
				ScreenConstant.ScreenTitle.SALES_ORDER_WINDOW, ScreenConstant.ScreenLinks.SALES_ORDER_WINDOW, true,
				ScreenConstant.MenuItemId.MENU_PRIV.SALES_ORDER_WINDOW,
				ScreenConstant.MenuItemId.MenuTitle.SALES_ORDER_WINDOW);

		menuScreenMap.put(ScreenConstant.MenuItemId.SALES_ORDER_WINDOW, menuItem);

	}

	@Override
	public void initializeScreen() {
		changeContainerColor();
		MenuItemInfo menuInfo = getMenuInfo(_selectedMenuId);
		if (menuInfo != null) {
			setScreenTitle(menuInfo.getScreenTitle());
			// setHSN();
		}
		if (digitalClock != null && !mainActionContainer.isDisable()
				&& !_selectedMenuId.equalsIgnoreCase(ScreenConstant.MenuItemId.LOGIN)
				&& !_selectedMenuId.equalsIgnoreCase(ScreenConstant.MenuItemId.MAIN_WINDOW)) {
			digitalClock.play();
		}
		CONTROL_MANUAL.setVisible(false);
	}

	public static MenuItemInfo getMenuInfo(String menuId) {
		return menuScreenMap.get(menuId);
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

	@Override
	public void setScreenTitle(String screenTitle) {
		labelScreenTitle.setText(screenTitle);
	}

	@Override
	public void registerIdleMonitor() {

	}

	@Override
	public int prompt(String title, String message, String[] options) {
		return prompt(title, message, options, Misc.getUndefDouble(), Misc.getUndefDouble());
	}

	@Override
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
						System.out.println(Thread.currentThread().toString() + "[PROMPT][RESULT]: " + i);
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

	@Override
	public void showAlert(String title, String message) {
		showAlert(Alert.AlertType.INFORMATION, title, message);
	}

	@Override
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

	@Override
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

	@Override
	public void screenComponentVisible(boolean screenTitleVisible, boolean mainMenuContainerVisible,
			boolean blockingReasonLabelVisible, boolean signOutControlerVisible, boolean mainActionContainerVisible) {
		labelScreenTitle.setVisible(screenTitleVisible);
		mainMenuContainer.setVisible(mainMenuContainerVisible);
		labelBlockingReason.setVisible(blockingReasonLabelVisible);
		CONTROL_SIGN_OUT.setVisible(signOutControlerVisible);
		mainActionContainer.setVisible(mainActionContainerVisible);
	}

	@Override
	public String getSourceId(Event event) {
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

	@Override
	public void initializeMenuControlls() {
		if (userData != null) {
			if (userData.isSupperUser() && _selectedMenuId != ScreenConstant.MenuItemId.MAIN_WINDOW) {
				// screenComponentVisible(false, true, false, false, false, false);
				// setMenuDisable(false, false, false, false, false, false,false);
				mainMenuContainerVisible = true;
				screenComponentVisible(labelScreenTitleVisible, mainMenuContainerVisible, labelBlockingReasonVisible,
						controlSignOutVisible, mainActionContainerVisible);

				setMenuDisable(gateInDisable, tareDisable, grossDisable, gateOutDisable, settingDisable,
						transporterDisable, readTagDisable, tprDetailsDisable, issueTagDisable, salesOrderDisable);

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
				issueTagDisable = issueTagDisable ? true
						: userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.ISSUE_TAG_WINDOW) ? false
								: true;

				createInvoiceDisable = createInvoiceDisable ? true
						: userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.ALLOW_CREATE_INVOICE)
								? false
								: true;
				salesOrderDisable = salesOrderDisable ? true
						: userData.getPrivList().contains(ScreenConstant.MenuItemId.MENU_PRIV.SALES_ORDER_WINDOW)
								? false
								: true;
				screenComponentVisible(labelScreenTitleVisible, mainMenuContainerVisible, labelBlockingReasonVisible,
						controlSignOutVisible, mainActionContainerVisible);

				setMenuDisable(gateInDisable, tareDisable, grossDisable, gateOutDisable, settingDisable,
						transporterDisable, readTagDisable, tprDetailsDisable, issueTagDisable, salesOrderDisable);

			} else {
				screenComponentVisible(false, true, false, false, false);
				setMenuDisable(true, true, true, true, true, true, true, true, true, true);

				labelBlockingReason.setText("Please Contact with Control Room for getting menu privelege");
			}
		}
		setMenuVisible(true, true, true, true, true, true, true, true, true);
		// setMenuVisible(false, false, false, false, false, false,false);
	}

	@Override
	public void setMenuDisable(boolean gateInVisible, boolean tareVisible, boolean grossVisible, boolean gateOutVisible,
			boolean settingVisible, boolean transporterMenuVisible, boolean tagReadMenuVisible,
			boolean tprDetailsMenuDisable, boolean tagIssueMenuDisable, boolean salesOrderDisable) {
		CONTROL_GATE_IN.setDisable(gateInVisible);
		CONTROL_TARE_WB.setDisable(tareVisible);
		CONTROL_GROSS_WB.setDisable(grossVisible);
		CONTROL_GATE_OUT.setDisable(gateOutVisible);
		CONTROL_SETTING.setDisable(settingVisible);
		CONTROL_TAG_READ.setDisable(tagReadMenuVisible);
		CONTROL_TPR_DETAILS.setDisable(tprDetailsMenuDisable);
		CONTROL_ISSUE_TAG.setDisable(tagIssueMenuDisable);
		CONTROL_SALES_ORDER.setDisable(salesOrderDisable);
	}

	@Override
	public void setMenuVisible(boolean _gateInVisible, boolean _tareVisible, boolean _grossVisible,
			boolean _gateOutVisible, boolean _settingMenuVisible, boolean _tagReadMenuVisible,
			boolean _tprDetailsMenuVisible, boolean _issueTagVisible, boolean _salesOrderVisible) {
		CONTROL_GATE_IN.setVisible(_gateInVisible);
		CONTROL_TARE_WB.setVisible(_tareVisible);
		CONTROL_GROSS_WB.setVisible(_grossVisible);
		CONTROL_GATE_OUT.setVisible(_gateOutVisible);
		CONTROL_SETTING.setVisible(_settingMenuVisible);
		CONTROL_TAG_READ.setVisible(_tagReadMenuVisible);
		CONTROL_TPR_DETAILS.setVisible(_tprDetailsMenuVisible);
		CONTROL_ISSUE_TAG.setVisible(false);
		CONTROL_SALES_ORDER.setVisible(_salesOrderVisible);
	}

	@Override
	public void signOutAction() {
		stopRFID();
		clearInputsAction();
		loadScreen(ScreenConstant.ScreenLinks.LOGIN_WINDOW, ScreenConstant.MenuItemId.LOGIN);
		screenComponentVisible(false, false, false, false, false);
	}

	@Override
	public void clearInputsAction() {
		if (this.currentViewController != null) {
			this.currentViewController.clearInputs();

		}
		labelBlockingReason.setText("");
		issueTagDisable = false;
		readTagDisable = false;
		gateInDisable = false;
		gateOutDisable = false;
		tareDisable = false;
		grossDisable = false;
		settingDisable = false;
		salesOrderDisable = false;
		transporterDisable = false;
		readTagDisable = false;
		tprDetailsDisable = false;
		labelScreenTitleVisible = false;
		mainMenuContainerVisible = false;
		labelBlockingReasonVisible = false;
		controlSignOutVisible = false;
		mainActionContainerVisible = false;
		
	}

	@Override
	public void stopRFID() {
		try {
			MainController.currentViewController.stopRfid();
			// MainController.currentViewController.stopSyncTprService();
			digitalClock.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void changeContainerColor() {
		if (_selectedMenuId.equalsIgnoreCase(ScreenConstant.MenuItemId.LOGIN)) {
			menuTitleMsgContainer.setStyle("-fx-background-color: #005A9C;");
			mainActionContainer.setStyle("-fx-background-color: #DDDDDD;");
			mainForm.setStyle("-fx-background-color: #DDDDDD;");
		} else {
			menuTitleMsgContainer.setStyle("-fx-background-color: #ffffff;");
			mainForm.setStyle("-fx-background-color: #ffffff;");
			mainActionContainer.setStyle("-fx-background-color: #ffffff;");
		}
	}

	@Override
	public void saveAction() {
		if (!this.currentViewController.save()) {
			// this.showAlert(Alert.AlertType.ERROR, "Message",
			// UIConstant.SAVE_FAILER_MESSAGE);
		}

	}

	@Override
	public void handleActionControl(Button control) {
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
			// manualControlAction();
			break;
		case "CONTROL_GATE_IN":
			gateInMenuAction();
			changeSaveConrollText("Save");
			break;
		case "CONTROL_GATE_OUT":
			gateOutMenuAction();
			changeSaveConrollText("Save");
			break;
		case "CONTROL_TARE_WB":
			tareWBMenuAction();
			changeSaveConrollText("Save");
			break;
		case "CONTROL_GROSS_WB":
			grossWbMenuAction();
			changeSaveConrollText("Save");
			break;
		case "CONTROL_SIGN_OUT":
			signOutAction();
			break;
		case "CONTROL_SETTING":
			settingMenuAction();
			break;
		case "CONTROL_TPR_DETAILS":
			tprDetailsMenuAction();
			break;
		case "CONTROL_TAG_READ":
			tagReadMenuAction();
			changeSaveConrollText("Read");
			break;
		case "CONTROL_SALES_ORDER":
			salesOrderMenuAction();
			break;
		default:
			break;
		}
	}

	private void salesOrderMenuAction() {
		stopRFID();
		clearInputsAction();
		CONTROL_SAVE.setDisable(false);
		CONTROL_SAVE.setText("Save");
//		TokenManager.initConfig("GATE_IN_TYPE", Type.WorkStationType.CGPL_LOAD_GATE_IN);
//		TokenManager.currWorkStationType = Type.WorkStationType.CGPL;// coal
		loadScreen(ScreenConstant.ScreenLinks.SALES_ORDER_WINDOW, ScreenConstant.MenuItemId.SALES_ORDER_WINDOW);
		salesOrderDisable = true;
		labelScreenTitleVisible = true;
		mainMenuContainerVisible = true;
		labelBlockingReasonVisible = true;
		controlSignOutVisible = true;
		mainActionContainerVisible = true;
		initializeMenuControlls();
		setMenuVisible(true, true, true, true, true, true, true, true, true);
	
	}

	private void gateInMenuAction() {
		stopRFID();
		clearInputsAction();
		CONTROL_SAVE.setDisable(false);
		TokenManager.initConfig("GATE_IN_TYPE", Type.WorkStationType.CGPL_LOAD_GATE_IN);
		TokenManager.currWorkStationType = Type.WorkStationType.CGPL_LOAD_GATE_IN;// coal
		loadScreen(ScreenConstant.ScreenLinks.GATE_IN_WINDOW, ScreenConstant.MenuItemId.GATE_IN);
		gateInDisable = true;
		labelScreenTitleVisible = true;
		mainMenuContainerVisible = true;
		labelBlockingReasonVisible = true;
		controlSignOutVisible = true;
		mainActionContainerVisible = true;
		initializeMenuControlls();
		setMenuVisible(true, true, true, true, true, true, true, true, true);
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
		initializeMenuControlls();
		setMenuVisible(true, true, true, true, true, true, true, true, true);
	}

	private void tareWBMenuAction() {
		stopRFID();
		clearInputsAction();
		CONTROL_SAVE.setDisable(false);
		TokenManager.initWeighBridgeConfig();
		TokenManager.initConfig("WEIGH_BRIDGE_OUT_TYPE", Type.WorkStationType.CGPL_LOAD_WB_OUT);
		TokenManager.currWorkStationType = com.ipssi.rfid.constant.Type.WorkStationType.CGPL_LOAD_WB_OUT;// coal
		loadScreen(ScreenConstant.ScreenLinks.TARE_WB_WINDOW, ScreenConstant.MenuItemId.TARE_WB);
		tareDisable = true;
		labelScreenTitleVisible = true;
		mainMenuContainerVisible = true;
		labelBlockingReasonVisible = true;
		controlSignOutVisible = true;
		mainActionContainerVisible = true;
		initializeMenuControlls();
		setMenuVisible(true, true, true, true, true, true, true, true, true);
	}

	private void grossWbMenuAction() {
		stopRFID();
		clearInputsAction();
		CONTROL_SAVE.setDisable(false);
		TokenManager.initWeighBridgeConfig();
		TokenManager.initConfig("WEIGH_BRIDGE_IN_TYPE", Type.WorkStationType.CGPL_LOAD_WB_IN);
		TokenManager.currWorkStationType = com.ipssi.rfid.constant.Type.WorkStationType.CGPL_LOAD_WB_IN;// coal
		loadScreen(ScreenConstant.ScreenLinks.GROSS_WB_WINDOW, ScreenConstant.MenuItemId.GROSS_WB);
		grossDisable = true;
		labelScreenTitleVisible = true;
		mainMenuContainerVisible = true;
		labelBlockingReasonVisible = true;
		controlSignOutVisible = true;
		mainActionContainerVisible = true;
		initializeMenuControlls();
		setMenuVisible(true, true, true, true, true, true, true, true, true);
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
		labelBlockingReasonVisible = true;
		initializeMenuControlls();
		// changeSaveConrollText("Read Tag");

		if (TokenManager.tagIdentifyManually) {
			setControllerDisable(false);
		} else {
			setControllerDisable(true);
		}
	}

	private void changeSaveConrollText(String tagName) {
		if (!CONTROL_SAVE.isDisable() && CONTROL_SAVE.isVisible())
			this.CONTROL_SAVE.setText(tagName);

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
		issueTagDisable = true;
		salesOrderDisable = true;
		controlSignOutVisible = true;
		initializeMenuControlls();
		setMenuVisible(true, true, true, true, true, true, true, true, true);
	}

	private void tprDetailsMenuAction() {
		stopRFID();
		clearInputsAction();
		loadScreen(ScreenConstant.ScreenLinks.TPR_DETAILS_WINDOW, ScreenConstant.MenuItemId.TPR_DETAILS_WINDOW);
		tprDetailsDisable = true;
		labelScreenTitleVisible = true;
		mainMenuContainerVisible = true;
		labelBlockingReasonVisible = false;
		controlSignOutVisible = true;
		mainActionContainerVisible = false;
		initializeMenuControlls();
	}

	public void setControllerDisable(boolean isTrue) {
		CONTROL_SAVE.setDisable(isTrue);
	}
}
