package com.ipssi.rfid.ui.secl.controller;

import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.ProcessStepProfile;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPRecord.WeighmentStep;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.beans.User;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.beans.VehicleRFIDInfo;
import com.ipssi.rfid.constant.RFIDConstant;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.Status.Workstate;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.constant.Type.Reader;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.integration.Barrier;
import com.ipssi.rfid.integration.WeighBridge;
import com.ipssi.rfid.integration.WeighBridgeCentric;
import com.ipssi.rfid.integration.WeighBridgeListener;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.PeripheralConnectionStatus;
import com.ipssi.rfid.readers.PeripheralConnectionStatus.PeripheralStatus;
import com.ipssi.rfid.readers.PeripheralConnectionStatus.PeripheralType;
import com.ipssi.rfid.readers.RFIDDataHandler;
import com.ipssi.rfid.readers.RFIDDataProcessor;
import com.ipssi.rfid.readers.RFIDException;
import com.ipssi.rfid.readers.RFIDMaster;
import com.ipssi.rfid.readers.TAGListener;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.custom.Printer;
import com.ipssi.rfid.ui.secl.controller.NodeExt.VehicleEntryType;
import com.ipssi.rfid.ui.secl.controller.PropertyManager.PropertyType;
import com.ipssi.rfid.ui.secl.data.LovDao;
import com.ipssi.rfid.ui.secl.data.LovUtils;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo.ActionPanelType;
import com.ipssi.rfid.ui.secl.data.NodeDBConnectionChecker;
import com.ipssi.rfid.ui.secl.data.TPRDataUtils;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXMaskTextField;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class MainController implements Initializable {
	
	@FXML
	private Label labelBlockingReason;
	@FXML
	private BorderPane mainForm;
	@FXML
	private HBox mainMenuContainer;
	@FXML
	private HBox mainActionContainer;
	/*
	@FXML
	private Label labelSystemId;
	@FXML
	private Label labelUsername;
	@FXML
	private JFXButton buttonLogout;*/
	@FXML
	private Label labelScreenTitle;
	@FXML
	private HBox panelError;
	/*@FXML
	private Label textRFIDOneConnected;
	@FXML
	private Label textRFIDTwoConnected;
	@FXML
	private Label textBarrierConnected;
	@FXML
	private Label textWeighBridgeConnected;
	@FXML
	private Label textServerConnected;
	@FXML
	private Label textRFIDThreeConnected;
	@FXML
	private Label textWeighBridgeCentricConnected;
	@FXML
	private HBox panelConnectionStatus;*/
	
	@FXML
	private JFXButton CONTROL_CLEAR;
	@FXML
	private JFXButton CONTROL_MANUAL;
	@FXML
	private JFXButton CONTROL_PRINT;
	@FXML
	private JFXButton CONTROL_SAVE;
	
	@FXML
	private JFXButton CONTROL_REPRINT;
	private ControllerI contentScreenController;
	private Button selectedMenuItem;

	private RFIDDataHandler rfidHandlerOne = null;
	private RFIDDataHandler rfidHandlerTwo = null;
	private WeighBridge weighBridge = null;
	private int readerId = 0;

	private HashMap<String, Triple<Node, ControllerI, ArrayList<NodeExt>>> cachedScreenNodes = new HashMap<>();
	private String currentMenuId;
	private ArrayList<NodeExt> actionControlList = new ArrayList<>();
	private ArrayList<NodeExt> menuControlList = new ArrayList<>();

	//form field ids
	public static String ID_VEHICLE_FIELD = "textBoxVehicleName";
	public static String ID_DO_FIELD = "textBoxDONo";
	public static String ID_TRANSPORTER_FIELD = "comboBoxTransporter";

	MainWindow mainWindow;
	SplashController splashController;
	UserSession userSession;
	private boolean printOnSave = false;

	private Token token;
	private TPRecord tpr;
	private TPRecord lastPrintingTPR = null;
	private TPRBlockManager tprBlockManager;
	private TPStep tpStep;

	private boolean vehicleBlackListed = false;
	private boolean isVehicleExist = true;
	private int isNewVehicle = Status.VEHICLE.EXISTING_RF;
	private int vehicleId = Misc.getUndefInt();
	private String vehicleName = null;
	private String epcId=null;
	private RFIDHolder rfidData = null;

	private IdleMonitors idleMonitor = null;
	final KeyCombination ctrlManual = new KeyCodeCombination(KeyCode.M,
            KeyCombination.CONTROL_DOWN);
	final KeyCombination ctrlSave = new KeyCodeCombination(KeyCode.S,
            KeyCombination.CONTROL_DOWN);
	final KeyCombination ctrlClear = new KeyCodeCombination(KeyCode.C,
            KeyCombination.CONTROL_DOWN);
	final KeyCombination ctrlPrint = new KeyCodeCombination(KeyCode.P,
            KeyCombination.CONTROL_DOWN);
	final KeyCombination ctrlTare = new KeyCodeCombination(KeyCode.T,
            KeyCombination.CONTROL_DOWN);
	final KeyCombination ctrlGross = new KeyCodeCombination(KeyCode.G,
            KeyCombination.CONTROL_DOWN);
	private HeaderController headerController;
	public JFXButton getControl(String controlId){
		switch (controlId) {
		case "CONTROL_CLEAR":
			return CONTROL_CLEAR;
		case "CONTROL_MANUAL":
			return CONTROL_MANUAL;
		case "CONTROL_REPRINT":
			return CONTROL_REPRINT;
		case "CONTROL_PRINT":
			return CONTROL_PRINT;
		case "CONTROL_SAVE":
			return CONTROL_SAVE;
		default:
			return null;
		}
	}
	public void init(MainWindow mainWindow,SplashController splashController, HeaderController headerController){
		if(this.mainWindow == null){
			this.mainWindow = mainWindow;
			this.splashController = splashController;
			this.headerController = headerController;
			this.headerController.init(this);
			initScreens();
			this.headerController.setSystemId(mainWindow.getWorkStationDetails().getUid());
//			labelSystemId.setText(mainWindow.getWorkStationDetails().getUid());
		}
	}
	
	public boolean handleCtrlKeyEvents(KeyEvent event){
        if (ctrlManual.match(event)) {
        	handleControlAction(CONTROL_MANUAL);
        	return true;
        }else if (ctrlSave.match(event)) {
        	handleControlAction(CONTROL_SAVE);
        	return true;
        }else if (ctrlPrint.match(event)) {
        	handleControlAction(CONTROL_PRINT);
        	return true;
        }else if (ctrlClear.match(event)) {
        	handleControlAction(CONTROL_CLEAR);
        	return true;
        }else if (ctrlTare.match(event)) {
        	if(contentScreenController != null)
        		contentScreenController.handleToggleWeighment("checkBoxTare");
        	return true;
        }else if (ctrlGross.match(event)) {
        	if(contentScreenController != null)
        		contentScreenController.handleToggleWeighment("checkBoxGross");
        	return true;
        }
        return false;
	}
	public MainWindow getMainWindow() {
		return mainWindow;
	}

	public void menuItemClicked(ActionEvent actionEvent) {
		//testLabel.setText("Menu Item Clicked :"+((Button)actionEvent.getSource()).getId());
		setMenu(((Button) actionEvent.getSource()));
	}
	private void setMenu(Button _selMenuItem){
		if (null != selectedMenuItem) {
			selectedMenuItem.setDisable(false);
			selectedMenuItem.setStyle("-fx-background-color: #003399;-fx-text-fill:#fff");
		}
		selectedMenuItem = _selMenuItem;
		selectedMenuItem.setStyle("-fx-background-color: #dee42d;-fx-text-fill:#000;-fx-opacity: 1;");
		selectedMenuItem.setDisable(true);
		currentMenuId = selectedMenuItem.getId();
		openMenuItem(currentMenuId);
	}
	boolean isAlert = false;
	Object alertLock = new Object();
	public void showAlert(String title, String message) {
		showAlert(Alert.AlertType.INFORMATION, title, message);
	}

	public void showAlert(Alert.AlertType alertType,String title, String message) {
		if(isAlert)
			return;
		synchronized (alertLock) {
			isAlert = true;
			Alert alert = new Alert(alertType);
			alert.setTitle(title);
			alert.setContentText(message);
			alert.showAndWait();
			isAlert = false;
		}
	}

	public int prompt(String title, String message, String[] options) {
		if(isAlert)
			return Misc.getUndefInt();
		int retval = Misc.getUndefInt();
		synchronized (alertLock) {
			try{
				isAlert = true;
				ArrayList<ButtonType> optionButtons = null;
				for (int i = 0, is = options == null ? 0 : options.length; i < is; i++) {
					if (optionButtons == null)
						optionButtons = new ArrayList<>();
					optionButtons.add(new ButtonType(options[i]));
				}
				if (optionButtons == null)
					return retval;
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle(title);
				alert.setContentText(message);
				alert.getButtonTypes().setAll(optionButtons);
				Optional<ButtonType> result = alert.showAndWait();

				for (int i = 0, is = optionButtons == null ? 0 : optionButtons.size(); i < is; i++) {
					if (optionButtons.get(i) == result.get())
						return i;

				}
			}catch(Exception ex){
				ex.printStackTrace();
			}finally{
				isAlert = false;
			}
		}
		return retval;
	}

	private void handleControlAction(Button control) {
		if(control == null || control.isDisable() || !control.isVisible() || Utils.isNull(currentMenuId) ||currentMenuId.equalsIgnoreCase(ScreenConstant.MenuItemId.LOGIN) || currentMenuId.equalsIgnoreCase(ScreenConstant.MenuItemId.SETTINGS))
			return;
		String controlId = control.getId().toUpperCase();
		switch (controlId) {
		case "CONTROL_CLEAR":
			clearForm(null, true,MainWindow.getMenuInfo(currentMenuId).getActionPanelType() == ActionPanelType.TPR,true,true,true);
			//contentScreenController.resetWeighmentMode();
			break;
		case "CONTROL_MANUAL":
			if (contentScreenController != null) {
				clearForm(null, false,MainWindow.getMenuInfo(currentMenuId).getActionPanelType() == ActionPanelType.TPR);
				contentScreenController.enableManualEntry(true);
				setFocusToFirstInputField();
				control.setDisable(true);
			}
			break;
		case "CONTROL_REPRINT":
		case "CONTROL_PRINT":
			//do print
			/*if (contentScreenController != null){
				Triple<Node, ControllerI, ArrayList<NodeExt>> screenPair = getScreen(ScreenConstant.MenuItemId.PRINT, ScreenConstant.ScreenLinks.PRINT);
				if(screenPair != null){
					ControllerI printController = screenPair.second;
					if(printController != null){
						clearFormFields(screenPair.third);
						printController.print(tpr, MainWindow.getMenuInfo(currentMenuId).getTprWorkstionConfig(readerId).getWorkstationType());
						clearForm(null, false,MainWindow.getMenuInfo(currentMenuId).getActionPanelType() == ActionPanelType.TPR);
					}
				}
			}*/
			actionPrint();
			break;
		case "CONTROL_SAVE":
			actionSave();
			break;
		default:
			break;
		}
		//showAlert("Control Action", "Action Taken :" + controlId);
	}
	private void actionSave(){
		if (contentScreenController != null) {
			boolean useTPRAction = MainWindow.getMenuInfo(currentMenuId).getActionPanelType() == ActionPanelType.TPR; 
			vehicleBlackListed = tprBlockManager != null && tprBlockManager.getBlockStatus() == UIConstant.BLOCKED;
			if(useTPRAction){
				if(( vehicleBlackListed || validateData()) && saveTPRData(false)){
					clearForm(null, false,useTPRAction,true,true,true);
					if(printOnSave)
						actionPrint();
					/*else
						requestControlActionFocus("CONTROL_PRINT");*/
				}
				
			}else{
				if(validateData() && contentScreenController.save())
					clearForm(null, false,useTPRAction,true,true,true);
			}
		}
	}
	private boolean actionPrint(){
		return Printer.print(MainWindow.getMenuInfo(currentMenuId).getTprWorkstionConfig(readerId).getWorkstationType(), mainWindow.getWorkStationDetails(), lastPrintingTPR);
	}
	public void onControlKeyPress(KeyEvent keyEvent) {
		if (keyEvent.getCode() != KeyCode.ENTER)
			return;
		Button control = ((Button) keyEvent.getSource());
		handleControlAction(control);
	}

	public void controlItemClicked(ActionEvent actionEvent) {
		Button control = ((Button) actionEvent.getSource());
		handleControlAction(control);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//initScreens();
	}
	
	private void initScreens(){
		long st = System.currentTimeMillis();
		if(splashController != null)
			splashController.showMessages("Initialize Menu");
		mainWindow.loadScreens(cachedScreenNodes, mainForm, this::requestFocusToNextField);
		System.out.println("Screen loaded..."+(System.currentTimeMillis() - st));
		st = System.currentTimeMillis();
		//clearSession();
		//System.out.println("Session cleared..."+(System.currentTimeMillis() - st));
		if(splashController != null)
			splashController.showMessages("Initializing Service");
		initServices();
		if(splashController != null)
			splashController.showMessages("Starting Services");
		startServices();
		st = System.currentTimeMillis();
		if(splashController != null)
			splashController.showMessages("Initializing Actions");
		initActionControls();
		System.out.println("Actions loaded..."+(System.currentTimeMillis() - st));
		st = System.currentTimeMillis();
		initMenuControls();
		if(splashController != null)
			splashController.showMessages("Cleaning menu");
		cleanMenu();
		System.out.println("Menu loaded..."+(System.currentTimeMillis() - st));
	}
	
	
	private void initServices(){
		RFIDMaster.setConnectionStatusHandler(this::handlePeripheralConnection);
		RFIDConstant.setReaderConfiguration();
		if (rfidHandlerOne == null) {
			rfidHandlerOne = new RFIDDataHandler(1000, Reader.IN, Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt());
			rfidHandlerList.add(rfidHandlerOne);
			rfidHandlerOne.setTagListener(rfidTagListener);
		}
		if (rfidHandlerTwo == null) {
			rfidHandlerTwo = new RFIDDataHandler(1000, Reader.OUT, Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt());
			rfidHandlerList.add(rfidHandlerTwo);
			rfidHandlerTwo.setTagListener(rfidTagListener);
		}
	}
	
	public void logout() {
		if(userSession == null || userSession.start == null)
			return;
		Connection conn = null;
		boolean destroyIt = false;
		try{
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			userSession.saveSession(conn);
		}catch(Exception ex){
			ex.printStackTrace();
			destroyIt = true;
		}finally{
			try{
				DBConnectionPool.returnConnectionToPoolNonWeb(conn,destroyIt);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		clearSession();
	}


	private void openMenuItem(String buttonId) {
		MenuItemInfo menu = MainWindow.getMenuInfo(buttonId);
		if (menu == null || menu.getScreenURL() == null || menu.getScreenURL().length() == 0)
			return;
		Triple<Node, ControllerI, ArrayList<NodeExt>> screenPair = getScreen(buttonId, menu.getScreenURL());
		if (screenPair == null)
			return;
		//TokenManager.currWorkStationType = menu.getWorkstationType();
		//TokenManager.materialCat = menu.getMaterialCat();
		labelScreenTitle.setText(menu.getScreenTitle());
		contentScreenController = screenPair.second;
		mainForm.setCenter(screenPair.first);
		//CONTROL_PRINT.setDisable(!contentScreenController.isPrintable());
		CONTROL_PRINT.setDisable(!contentScreenController.isPrintable() || printOnSave);
		//CONTROL_REPRINT.setDisable(!contentScreenController.isPrintable());
		CONTROL_REPRINT.setDisable(!contentScreenController.isPrintable() || !printOnSave);
		//clearForm(null, true,MainWindow.getMenuInfo(currentMenuId).getActionPanelType() != ActionPanelType.TPR);
		mainActionContainer.setVisible(MainWindow.getMenuInfo(currentMenuId).getActionPanelType() != ActionPanelType.NONE);
		mainActionContainer.setVisible(!contentScreenController.hideActionBar());
		handleReinitTPR(true);
		contentScreenController.resetWeighmentMode();
		
	}
	public void handleReinitTPR(boolean modifyToken){
		clearForm(null, true,true,true,modifyToken);
		if(token != null ){
			Connection conn = null;
			boolean destroyIt = false;
			try{
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				handleTprInit(conn, readerId);
			}catch(Exception ex){
				destroyIt = true;
				ex.printStackTrace();
			}finally {
				try{
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}
	}

	public boolean isLoggedIn() {
		return false;
	}

	public boolean login(User user) {
		initUserSession(user);
		headerController.setUsername("Hello, "+user.getUsername());
		headerController.setUsernameVisible(true);
		headerController.setLogoutVisible(true);
		/*labelUsername.setText("Hello, "+user.getUsername());
		labelUsername.setVisible(true);
		buttonLogout.setVisible(true);*/
		mainMenuContainer.setVisible(true);
		mainActionContainer.setVisible(true);
		//panelConnectionStatus.setVisible(true);
		if (null != selectedMenuItem) {
			selectedMenuItem.setDisable(false);
			selectedMenuItem.setStyle("-fx-background-color: #003399;-fx-text-fill:#fff");
		}
		String _selMenuId = initializeMenuItem(user);
		if(!Utils.isNull(_selMenuId)){
			NodeExt nodeExt = findNodeById(_selMenuId, menuControlList);
			setMenu((Button) (nodeExt == null ? null : nodeExt.getNode()));
		}
		if(idleMonitor != null)
			idleMonitor.startMonitoring();
		//mainForm.setCenter(node);
		//contentScreenController = controller;
		return true;
	}

	public void clearSession() {
		if(splashController != null)
		splashController.showMessages("Loading Login Screen");
		setAllMenuVisible(false);
		headerController.setUsername("");
		headerController.setUsernameVisible(false);
		headerController.setLogoutVisible(false);
/*		labelUsername.setText("");
		labelUsername.setVisible(false);
		buttonLogout.setVisible(false);*/
		labelBlockingReason.setVisible(false);
		mainMenuContainer.setVisible(false);
		mainActionContainer.setVisible(false);
		Triple<Node, ControllerI, ArrayList<NodeExt>> loginScreenPair = getScreen(ScreenConstant.MenuItemId.LOGIN, ScreenConstant.ScreenLinks.LOGIN);
		contentScreenController = loginScreenPair.second;
		mainForm.setCenter(loginScreenPair.first);
		contentScreenController.init(this, (Parent) loginScreenPair.first, null);
		labelScreenTitle.setText("");
		userSession = null;
		//panelConnectionStatus.setVisible(false);
		if(idleMonitor != null)
			idleMonitor.stopMonitoring();
	}

	public UserSession getUserSession() {
		return userSession;
	}

	public void initUserSession(User user){
		userSession = new UserSession(user.getId(), user.getName(), user.getUsername(), System.currentTimeMillis(),user.isSupperUser());
	}

	private Triple<Node, ControllerI, ArrayList<NodeExt>> getScreen(String tag, String screenUrl) {
		if (tag == null || tag.length() == 0 || !cachedScreenNodes.containsKey(tag))
			return null;
		cachedScreenNodes.get(tag).second.init(this, (Parent) cachedScreenNodes.get(tag).first, MainWindow.getMenuInfo(tag));
		cachedScreenNodes.get(tag).second.clearInputs();
		mainForm.requestFocus();
		setFocusToFirstInputField();
		return cachedScreenNodes.get(tag);
	}
	
	private String initializeMenuItem(User user) {
		String _selMenuId = null;
		try {
			ArrayList<Integer> privList = user.getPrivList();
			int position = 0;
				if (menuControlList != null && mainMenuContainer.getChildren() != null && mainMenuContainer.getChildren().size() > 0){
					for (int i = 0,is=menuControlList == null ? 0 : menuControlList.size() ; i < is; i++) {
						NodeExt menuNode = menuControlList.get(i);
						int menuPriv = menuNode.getPrivId();
						String menuTag = menuNode.getMenuTag();
						String menuTitle = menuNode.getMenuTitle();
						boolean visible = false;
						if(!Utils.isNull(menuTag)){
							if(user.isSupperUser()){
								visible = true;
								position = i;
							}else{
								visible =  privList.stream().filter(n-> n== menuPriv).count() > 0;
							}
							if(visible){
								if(_selMenuId == null){
									_selMenuId = menuTag;
								}
								mainMenuContainer.getChildren().get(position).setVisible(true);
								mainMenuContainer.getChildren().get(position).setId(menuTag);
								((JFXButton)mainMenuContainer.getChildren().get(position)).setText(menuTitle);
								position++;
							}
						}
					}
				}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return _selMenuId;
	}
	private String initializeMenuItemOld(User user) {
		String _selMenuId = null;
		try {
			ArrayList<Integer> privList = user.getPrivList();
			int lastPrivId = Misc.getUndefInt();
			int minPrivId = Integer.MAX_VALUE;
			int position = 0;
				if (mainMenuContainer != null && mainMenuContainer.getChildren() != null && mainMenuContainer.getChildren().size() > 0){
					for (int i = 0; i < mainMenuContainer.getChildren().size(); i++) {
						Button b = (Button) mainMenuContainer.getChildren().get(i);
						String tag = b.getId();
						MenuItemInfo menu = mainWindow.getMenuInfo(tag);
						boolean visible = false;
						if(menu != null){
							int privId = menu.getPrivId();
							if(user.isSupperUser()){
								visible = true;
								position = i;
							}else{
								visible =  privList.stream().filter(n-> n== privId).count() > 0;
							}
							if(visible){
								b.setVisible(true);
								if(i > position){
									mainMenuContainer.getChildren().add(position, mainMenuContainer.getChildren().remove(i));
								}
								if(!Misc.isUndef(lastPrivId) && !Misc.isUndef(privId) && privId < lastPrivId){
									mainMenuContainer.getChildren().add(i-1, mainMenuContainer.getChildren().remove(i));
								}else{
									lastPrivId = privId;
								}
								if(!Misc.isUndef(privId) && privId < minPrivId){
									_selMenuId = tag;
									minPrivId = privId;
								}
								position++;
							}
							
							
						}
					}
				}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return _selMenuId;
	}

	private void setAllMenuVisible(boolean visible) {
		if (mainMenuContainer == null || mainMenuContainer.getChildren() == null || mainMenuContainer.getChildren().size() == 0)
			return;
		for (int i = 0; i < mainMenuContainer.getChildren().size(); i++) {
			Button b = (Button) mainMenuContainer.getChildren().get(i);
			b.setVisible(visible);
		}
	}

	private void setMenuItemVisible(String menuId, boolean visible, int position) {
		if (mainMenuContainer == null || mainMenuContainer.getChildren() == null || mainMenuContainer.getChildren().size() == 0 || menuId == null || menuId.length() == 0)
			return;
		for (int i = 0; i < mainMenuContainer.getChildren().size(); i++) {
			Button b = (Button) mainMenuContainer.getChildren().get(i);
			if (menuId.equalsIgnoreCase(b.getId())) {
				b.setVisible(visible);
				if(position != i){
					mainMenuContainer.getChildren().set(position, mainMenuContainer.getChildren().remove(i));
				}
				break;
			}
		}
	}

	/*private void setActionItemVisible(String actionId, boolean visible) {
		if (mainActionContainer == null || mainActionContainer.getChildren() == null || mainActionContainer.getChildren().size() == 0 || actionId == null || actionId.length() == 0)
			return;
		for (int i = 0; i < mainActionContainer.getChildren().size(); i++) {
			Button b = (Button) mainActionContainer.getChildren().get(i);
			if (actionId.equalsIgnoreCase(b.getId())) {
				b.setVisible(visible);
				break;
			}
		}
	}*/

	private void requestFocusToNextField(KeyEvent keyEvent) {
		if(handleCtrlKeyEvents(keyEvent))
			return;
		if (contentScreenController == null)
			return;
		if (keyEvent.getCode() == KeyCode.SPACE){
			Node n = ((Node) keyEvent.getSource());
			if(n instanceof DatePicker){
				((DatePicker)n).show();
			}
			return;
		}

		if (keyEvent.getCode() != KeyCode.ENTER)
			return;
		String id = ((Node) keyEvent.getSource()).getId();
		if (id == null || id.length() == 0)
			return;
		Pair<Boolean, String> nextFocusReq = contentScreenController.requestFocusNextField(findFormFieldById(id));
		if(!handleAutoCompleteValue((Node) keyEvent.getSource()))
			return;
		if (nextFocusReq == null || !nextFocusReq.first)
			return;
		if(NodeExt.getNodeExt((Node) keyEvent.getSource()).isVehicle())
			return;
		NodeExt nextField;
		if (nextFocusReq.second != null && nextFocusReq.second.length() > 0) {
			nextField = findFormFieldById(nextFocusReq.second);
		} else {
			nextField = findNextFocusable(getFormFieldIndex(id));
		}
		if (nextField != null && nextField.getNode() != null) {
			nextField.getNode().requestFocus();
		} else {
			requestControlActionFocus("CONTROL_SAVE");
		}
	}
	public void handleNextFocusEmpty(NodeExt nextField,int currentIndex){
		if (nextField == null) {
			nextField = findNextFocusableEmpty(currentIndex);
		}
		if (nextField != null && nextField.getNode() != null) {
			nextField.getNode().requestFocus();
		} else {
			requestControlActionFocus("CONTROL_SAVE");
		}
	}
	public void handleNextFocus(NodeExt nextField,int currentIndex){
		if (nextField == null) {
			nextField = findNextFocusable(currentIndex);
		}
		if (nextField != null && nextField.getNode() != null) {
			nextField.getNode().requestFocus();
		} else {
			requestControlActionFocus("CONTROL_SAVE");
		}
	}

	public void requestFocusToField(String id) {
		NodeExt nodeExt = findFormFieldById(id);
		if (nodeExt != null && nodeExt.getNode() != null)
			nodeExt.getNode().requestFocus();
	}
	public NodeExt findNextFocusableEmpty(int currentItemIndex) {
		NodeExt nextField = null;
		if (!Misc.isUndef(currentItemIndex)) {
			nextField = getNextInputFieldEmpty(currentItemIndex + 1);
		}
		return nextField;
	}
	public NodeExt findNextFocusable(int currentItemIndex) {
		NodeExt nextField = null;
		if (!Misc.isUndef(currentItemIndex)) {
			nextField = getNextInputField(currentItemIndex + 1);
		}
		return nextField;
	}

	private int getFormFieldIndex(String id) {
		return getNodeIndex(id, getCurrentFormFieldList());
	}

	private int getNodeIndex(String id, ArrayList<NodeExt> nodeList) {
		if (id == null || id.length() == 0)
			return Misc.getUndefInt();
		for (int i = 0, is = nodeList == null ? 0 : nodeList.size(); i < is; i++) {
			if (id.equalsIgnoreCase(nodeList.get(i).getNode().getId())) {
				return i;
			}
		}
		return Misc.getUndefInt();
	}

	public NodeExt findFormFieldById(String id) {
		return findNodeById(id, getCurrentFormFieldList());
	}

	public NodeExt findNodeById(String id, ArrayList<NodeExt> nodeList) {
		int index = getNodeIndex(id, nodeList);
		return Misc.isUndef(index) ? null : nodeList.get(index);
	}

	public ArrayList<NodeExt> getAllFormFields() {
		return cachedScreenNodes.get(currentMenuId) == null ? null : cachedScreenNodes.get(currentMenuId).third;
	}

	public void setFocusToFirstInputField() {
		NodeExt nodeExt = getNextInputField(Misc.getUndefInt());
		if (nodeExt == null)
			nodeExt = findFormFieldById(ID_VEHICLE_FIELD);
		if(nodeExt == null || nodeExt.getNode() == null || !isFocusable(nodeExt))
			nodeExt = NodeExt.getNodeExt(CONTROL_SAVE);
		if (nodeExt != null && nodeExt.getNode() != null && isFocusable(nodeExt))
			nodeExt.getNode().requestFocus();
	}
	private boolean isFocusable(NodeExt nodeExt){
		if(nodeExt == null || nodeExt.getNode() == null)
			return false;
		Node n = nodeExt.getNode();
		return (!nodeExt.isNoFocus() && n != null &&!n.isDisable() &&
				((n instanceof TextField && ((TextField) n).isEditable())
						|| (n instanceof TextArea && ((TextArea) n).isEditable())
						|| (n instanceof ComboBox)
						|| (n instanceof DatePicker && ((DatePicker) n).isEditable())
						|| (n instanceof Button)
						));
	}
	private NodeExt getNextInputFieldEmpty(int index) {
		if (index < 0)
			index = 0;
		ArrayList<NodeExt> currentFormFields = getCurrentFormFieldList();
		for (int i = index, is = currentFormFields == null ? 0 : currentFormFields.size(); i < is; i++) {
			NodeExt nodeExt = currentFormFields.get(i);
			Node n = nodeExt == null ? null : nodeExt.getNode();
			if (!nodeExt.isNoFocus() && n != null && !n.isDisable() && isNullField(n) &&
					((n instanceof TextField && ((TextField) n).isEditable())
							|| (n instanceof TextArea && ((TextArea) n).isEditable())
							|| (n instanceof ComboBox)
							|| (n instanceof DatePicker && ((DatePicker) n).isEditable())
							))
				return nodeExt;
		}
		return null;
	}
	private NodeExt getNextInputField(int index) {
		if (index < 0)
			index = 0;
		ArrayList<NodeExt> currentFormFields = getCurrentFormFieldList();
		for (int i = index, is = currentFormFields == null ? 0 : currentFormFields.size(); i < is; i++) {
			NodeExt nodeExt = currentFormFields.get(i);
			Node n = nodeExt == null ? null : nodeExt.getNode();
			if (!nodeExt.isNoFocus() && n != null &&!n.isDisable() &&
					((n instanceof TextField && ((TextField) n).isEditable())
							|| (n instanceof TextArea && ((TextArea) n).isEditable())
							|| (n instanceof ComboBox)
							|| (n instanceof DatePicker && ((DatePicker) n).isEditable())
							))
				return nodeExt;
		}
		return null;
	}

	public boolean validateData() {
		boolean retval = true;
		ArrayList<NodeExt> currentFormFields = getCurrentFormFieldList();
		for (int i = 0,is=currentFormFields == null ? 0 : currentFormFields.size(); i <is ; i++) {
			Node n = currentFormFields.get(i).getNode();
			handleAutoCompleteValue(n);
			if(currentFormFields.get(i).isWBReading() && isNullField(n)){
				//to do
				showAlert(AlertType.ERROR, "Weigh Bridge Readings", "Weigh Bridge Not Working, Please Check Connection");
				retval = false;
				break;
			}
			if(currentFormFields.get(i).isMandatory() && isNullField(n)){
				showAlert(AlertType.ERROR, "Data Validity Check", "Please Fill Mandatory Fields");
				requestFocusToField(n.getId());
				retval = false;
				break;
			}
		}
		return retval;
	}
	public boolean handleAutoCompleteValue(Node n){
		boolean retval = true;
		if(n != null && n instanceof TextField){
			TextField t = (TextField) n;
			NodeExt nodeExt = NodeExt.getNodeExt(n);
			if(nodeExt != null && nodeExt.isAutoComplete()){
				Pair<Boolean, String> suggestedPair = LovDao.getSuggestionPairByCode(mainWindow.getWorkStationDetails().getPortNodeId(), LovDao.getAutoCompleteValue(t), nodeExt.getAutoCompleteSrc());
				Pair<String, String> codeNamePair = LovDao.getCodeNamePair(suggestedPair.second);
				String code = codeNamePair.first;
				String name = codeNamePair.second;
				if(suggestedPair == null || !suggestedPair.first){
					code = "";
					name = "";
					retval = !nodeExt.isMandatory();
				}
				NodeExt nameField = findFormFieldById(nodeExt.getAutoCompleteNameField());
				if(nameField != null && nameField.getNode() != null){
					if(nameField.getNode() instanceof TextField){
						((TextField)nameField.getNode()).setText(name);
					}else if(nameField.getNode() instanceof Label){
						((Label)nameField.getNode()).setText(name);
					}
					((TextField)nodeExt.getNode()).setText(code);
				}
				if(!Utils.isNull(nodeExt.getAutoCompleteNameField())){}else if(contentScreenController != null){
					contentScreenController.handleAutoComplete(nodeExt,codeNamePair);
				}
			}
		}
		return retval;
	}
	public boolean isNullField(Node n){
		return (n == null)
				||  (n instanceof JFXMaskTextField && (Utils.isNull(((JFXMaskTextField) n).getText()) || ((JFXMaskTextField) n).getText().indexOf('_') >= 0))
				|| (n instanceof TextField && Utils.isNull(((TextField) n).getText()))
				|| (n instanceof Label && Utils.isNull(((Label) n).getText()))
				|| (n instanceof TextArea && Utils.isNull(((TextArea) n).getText()))
				|| (n instanceof ComboBox && LovUtils.isUndef(((ComboBox<?>) n)))
				|| (n instanceof DatePicker &&  ((DatePicker) n).getValue() == null);
	}
	private void clearFormFields(ArrayList<NodeExt> formFields){
		if(formFields != null){
			formFields.forEach(nodeExt -> clearFormField(nodeExt));
		}
	}
	private void clearFormField(NodeExt nodeExt){

		Node n = nodeExt == null ? null : nodeExt.getNode();
		if(nodeExt.isField() && n != null && !Utils.isNull(n.getAccessibleText())&& n.getAccessibleText().indexOf("field") >= 0){
			if (n instanceof Label){
				if(!(//TokenManager.isSimulateWB && 
						nodeExt.isWBReading()))
					((Label) n).setText("");
			}
			else if (n instanceof JFXMaskTextField){
				JFXMaskTextField field = ((JFXMaskTextField) n);
				field.clear();
				field.setMask("");
				field.setMask(nodeExt.getMask());
			}
			else if (n instanceof TextField)
				((TextField) n).clear();
			else if (n instanceof TextArea)
				((TextArea) n).clear();
			else if (n instanceof ComboBox)
				((ComboBox<?>) n).setValue(null);
			else if (n instanceof DatePicker)
				((DatePicker) n).setValue(null);
			else if (n instanceof JFXCheckBox)
				((JFXCheckBox) n).setSelected(false);
		}
	}
	private void clearCurrentFormFields() {
		clearFormFields(getCurrentFormFieldList());
	}
	public void removeFromCachedNodes(Node node){
		if(node == null)
			return;
		ArrayList<NodeExt> removableFields = null;
		if(node instanceof Parent)
			removableFields = MainWindow.getAllNodes((Parent) node);
		else{
			removableFields = new ArrayList<>(Arrays.asList(NodeExt.getNodeExt(node)));
		}
		ArrayList<NodeExt> currentFormFields = getCurrentFormFieldList();
		if(currentFormFields != null){
			for (int i = 0,is=currentFormFields == null ? 0 : currentFormFields.size(); i < is; i++) {
				Node n = currentFormFields.get(i).getNode();
				String id = n.getId();
				for (int k = 0, ks = removableFields == null ? 0 : removableFields.size(); k < ks; k++) {
					if (id.equalsIgnoreCase(removableFields.get(k).getNode().getId())) {
						clearFormField(currentFormFields.remove(i));
						i--;
						is--;
						break;
					}
				}
			}
		}
	}
	public void addToCachedNodes(Node node) {
		if (node == null)
			return;
		ArrayList<NodeExt> addableFields = null;
		if (node instanceof Parent)
			addableFields = MainWindow.getAllNodes((Parent) node);
		else {
			addableFields = new ArrayList<>(Arrays.asList(NodeExt.getNodeExt(node)));
		}
		ArrayList<NodeExt> currentFormFields = getCurrentFormFieldList();
		if(currentFormFields != null){
			for (int i = 0,is=addableFields == null ? 0 : addableFields.size(); i <is ; i++) {
				NodeExt n = addableFields.get(i);
				n.getNode().setOnKeyPressed(this::requestFocusToNextField);
				currentFormFields.add(n);
			}
		}
	}
	private ArrayList<NodeExt> getCurrentFormFieldList(){
		return cachedScreenNodes.get(currentMenuId) == null ? null : cachedScreenNodes.get(currentMenuId).third;
	}

	private void initActionControls(){
		actionControlList = MainWindow.getAllNodes(mainActionContainer);
		if(actionControlList != null){
			actionControlList.forEach(node -> {
				node.getNode().focusedProperty().addListener((observable, oldValue, newValue) -> {
					if (!newValue)
						node.getNode().setStyle("-fx-background-color: #000;");
					else
						node.getNode().setStyle("-fx-background-color:#b76303");
				});
				node.getNode().hoverProperty().addListener((observable, oldValue, newValue) -> {
					if (!newValue)
						node.getNode().setStyle("-fx-background-color: #000;");
					else
						node.getNode().setStyle("-fx-background-color:#b76303");
				});
			});
		}

	}
	private void initMenuControls(){
		menuControlList = MainWindow.getAllNodes(mainMenuContainer);
		if(menuControlList != null){
			menuControlList.forEach(nodeExt -> {
				Node node = nodeExt.getNode();
				String menuTag = node.getId();
				MenuItemInfo menu = MainWindow.getMenuInfo(menuTag);
				nodeExt.setPrivId(menu == null ? Misc.getUndefInt() : menu.getPrivId());
				nodeExt.setMenuTitle(menu == null ? "" : menu.getMenuTitle());
				nodeExt.setMenuTag(menu == null ? "" : menu.getMenuTag());
				node.hoverProperty().addListener((observable, oldValue, newValue) -> {
					if(!node.isDisable()) {
						if (!newValue)
							node.setStyle("-fx-background-color: #003399;-fx-text-fill:#fff");
						else
							node.setStyle("-fx-background-color:  #b76303;-fx-text-fill:#000");
					}
				});
			});
		}

	}
	private NodeExt getMenuControllByPriv(int privId){
		NodeExt retval = null;
		for(int i=0,is=menuControlList != null?menuControlList.size() : 0; i<is; i++){
			NodeExt nodeExt = menuControlList.get(i);	
			if(nodeExt.getPrivId() == privId)
					retval = nodeExt;
		}
		return retval;
	}
	private void cleanMenu(){
		for (int i = 0,is=menuControlList == null ? 0 : menuControlList.size(); i < is; i++) {
			String id = menuControlList.get(i).getNode() == null ? null : menuControlList.get(i).getNode().getId();
			if(Utils.isNull(id) || MainWindow.getMenuInfo(id) == null){
				menuControlList.remove(i);
				i--;
				is--;
				if(!Utils.isNull(id)){
					for (int j = 0,js=mainMenuContainer == null || mainMenuContainer.getChildren() == null ? 0 : mainMenuContainer.getChildren().size(); j < js; j++) {
						if(id.equalsIgnoreCase(mainMenuContainer.getChildren().get(j).getId())){
							mainMenuContainer.getChildren().remove(j);
							break;
						}
					}
				}
			}
		}
	}
	private NodeExt getControlActionById(String id){
		return findNodeById(id,actionControlList);
	}
	private boolean requestControlActionFocus(String id){
		boolean retval = false;
		NodeExt nodeExt = getControlActionById(id);
		Node n = nodeExt == null ? null : nodeExt.getNode();
		if(n != null && (n instanceof Button) && !((Button) n).isDisabled()) {
			retval=true;
			n.requestFocus();
		}
		return retval;
	}

	private ArrayList<RFIDDataHandler> rfidHandlerList = new ArrayList<>();
	public void stopRFIDService(){
		rfidHandlerList.forEach(n->n.stop());
		if (weighBridge == null) {
			weighBridge.stopWeighBridge();
		}
	}
	private void processVehicle(int readerId , Token _token, TPRecord _tpr, TPStep _tps, TPRBlockManager _tprBlockManager,boolean allowSave){
		Connection conn = null;
		boolean destroyIt = false;
		try{
			this.readerId = readerId;
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			MenuItemInfo menuItemInfo = MainWindow.getMenuInfo(currentMenuId);
			Vehicle veh = Vehicle.getVehicle(conn, _tpr.getVehicleId());
			TPRWorkstationConfig tprWorkstationConfig = getWorkstationConfigByReaderId(conn,readerId,veh);// menuItemInfo.getTprWorkstionConfig(Misc.isUndef(readerId) ? Reader.IN : readerId);
			if(!Misc.isUndef(readerId) && !menuItemInfo.isNotifyRfidUpdates(readerId)){
				TokenManager.returnToken(conn, _token);
				return ;
			}
			token = _token;
			tprBlockManager = _tprBlockManager;
			tpStep = _tps;
			tpr = _tpr;
			if(tprWorkstationConfig.isReturnCard() && veh != null && veh.getCardType() == Type.RFID_CARD_TYPE.TEMPORARY){
				returnTag(conn, veh);
			}
			if(tprBlockManager != null){
				tprBlockManager.addQuestions(TPRDataUtils.getQuestionResponse(conn, mainWindow.getWorkStationDetails(), mainWindow.getWorkStationDetails().getMinesCode(), _tpr, veh, tprWorkstationConfig.getWorkstationType(), Status.TPRQuestion.isAccessAllowed,tprWorkstationConfig.getMaterialCat()));
				tprBlockManager.addQuestions(TPRDataUtils.getQuestionResponse(conn, mainWindow.getWorkStationDetails(), mainWindow.getWorkStationDetails().getMinesCode(), _tpr, veh, tprWorkstationConfig.getWorkstationType(), Status.TPRQuestion.isNoTareAllowed,tprWorkstationConfig.getMaterialCat()));
				tprBlockManager.addQuestions(TPRDataUtils.getQuestionResponse(conn, mainWindow.getWorkStationDetails(), mainWindow.getWorkStationDetails().getMinesCode(), _tpr, veh, tprWorkstationConfig.getWorkstationType(), Status.TPRQuestion.isDoValid,tprWorkstationConfig.getMaterialCat()));
				tprBlockManager.addQuestions(TPRDataUtils.getQuestionResponse(conn, mainWindow.getWorkStationDetails(), mainWindow.getWorkStationDetails().getMinesCode(), _tpr, veh, tprWorkstationConfig.getWorkstationType(), Status.TPRQuestion.isTempCardReturned,tprWorkstationConfig.getMaterialCat()));
			}
			if(!menuItemInfo.isProcessAuto()){
				NodeExt nodeExt = findFormFieldById(ID_VEHICLE_FIELD);
				TextField textBoxVehicleName =(TextField)( nodeExt == null ? null : nodeExt.getNode());
				if(textBoxVehicleName != null){
					//textBoxVehicleName.setEditable(false);
					textBoxVehicleName.setDisable(true);
				}
			}
			if(null != contentScreenController)
				contentScreenController.populateTPRData(readerId, conn,tpr, tpStep, veh);
			int blockStatus = tprBlockManager == null ? Misc.getUndefInt() : tprBlockManager.getBlockStatus();
			String blockingReason = tprBlockManager == null ? null : tprBlockManager.getBlockingReason(false);
			vehicleBlackListed = blockStatus == UIConstant.BLOCKED;
			labelBlockingReason.setText("");
			labelBlockingReason.setVisible(false);
			if(vehicleBlackListed){
				labelBlockingReason.setVisible(true);
				labelBlockingReason.setText(blockingReason);
			}
			
			//overrides.setText(vehicleBlackListed ? "BLOCKED" : "NOT_BLOCKED");
			if(!menuItemInfo.isProcessAuto()){
				if(allowSave && !TokenManager.isSimulateWB && TPRecord.isWeighment(tprWorkstationConfig.getWorkstationType())){
					allowSave = weighBridge != null && weighBridge.isEmpty() ;
				}
				CONTROL_SAVE.setDisable(!allowSave);
				CONTROL_SAVE.setText(vehicleBlackListed ? "Override" : "Save");
				if(vehicleBlackListed)
					CONTROL_SAVE.requestFocus();
				else{
					//setFocusToFirstInputField();
					if(allowSave)
						handleNextFocusEmpty(null,0);
					else
						CONTROL_CLEAR.requestFocus();
				}
			}else{
				if(!vehicleBlackListed ){//|| veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.INTERNAL){
					if(veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.ROAD || veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.WASHERY){
						tpr.setDoNumber(veh.getDoAssigned());
					}
					saveTPRData(true);
					clearForm(conn, false, true, false);
					//Barrier.ChangeSignal(readerId);
				}else{
					TokenManager.returnToken(conn, _token);
				}

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
	private TAGListener rfidTagListener = new TAGListener() {
		@Override
		public void manageTag(int readerId , Connection _dbConnection, Token _token, TPRecord _tpr, TPStep _tps, TPRBlockManager _tprBlockManager, boolean isCurrentThread, boolean allowSave) {
			if(!isCurrentThread){
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						processVehicle(readerId, _token, _tpr, _tps, _tprBlockManager,allowSave);
					}
				});
			}else{
				processVehicle(readerId, _token, _tpr, _tps, _tprBlockManager,allowSave);
			}
		}

		@Override
		public boolean showMessage(int readerId, String message) {
			Platform.runLater(new Runnable() {
				public void run() {
					Connection _dbConnection = null;
					boolean destroyIt = false;
					try{
						_dbConnection = DBConnectionPool.getConnectionFromPoolNonWeb();
						MenuItemInfo menuItemInfo = MainWindow.getMenuInfo(currentMenuId);
						//labelBlockingReason.setVisible(true);
						//labelBlockingReason.setText(message);
						if(!menuItemInfo.isProcessAuto()){
							showAlert("System Message", message);
						}
//						clearForm(_dbConnection,false,true,!menuItemInfo.isProcessAuto(),true,false);
					}catch(Exception ex){
						ex.printStackTrace();
						destroyIt = true;
					}finally{
						try{
							DBConnectionPool.returnConnectionToPoolNonWeb(_dbConnection, destroyIt);
						}catch(Exception ex){
							ex.printStackTrace();
						}
					}
				}
			});
			return false;
		}

		@Override
		public void setVehicleName(int readerId, String vehicleName) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if(null != contentScreenController){
						contentScreenController.setVehicleName(vehicleName);
					}
				}
			});

		}

		@Override
		public void clearVehicleName(int readerId) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if(null != contentScreenController){
						contentScreenController.clearVehicleName();
					}
				}
			});

		}

		@Override
		public int promptMessage(int readerId, String message, Object[] options) {
			return Misc.getUndefInt();
		}

		@Override
		public void clear(int readerId, boolean clearToken, Connection conn) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Connection _dbConnection = null;
					boolean destroyIt = false;
					try{
						_dbConnection = DBConnectionPool.getConnectionFromPoolNonWeb();
						clearForm(_dbConnection,clearToken,true,true,true,true);
						
					}catch(Exception ex){
						ex.printStackTrace();
						destroyIt = true;
					}finally{
						try{
							DBConnectionPool.returnConnectionToPoolNonWeb(_dbConnection, destroyIt);
						}catch(Exception ex){
							ex.printStackTrace();
						}
					}
				}
			});

		}

		@Override
		public int mergeData(int readerId, long sessionId, String epc, RFIDHolder data) {
			// TODO Auto-generated method stub
			return Misc.getUndefInt();
		}

		@Override
		public TPRWorkstationConfig getWorkstationConfig(Connection conn, int readerId, Vehicle veh) {
			return getWorkstationConfigByReaderId(conn, readerId,veh);
		}

		@Override
		public void varfiyVehicle(int readerId, Vehicle veh) {
			if(veh == null)
				return;
			// TODO Auto-generated method stub
			Platform.runLater(new Runnable() {
                
				@Override
				public void run() {
					Connection _dbConnection = null;
					boolean destroyIt = false;
					try{
						_dbConnection = DBConnectionPool.getConnectionFromPoolNonWeb();
						String[] options = {" No ", "  Yes  "};
						int answer = prompt("Data Saving Confirmation", "Uninitialized  RFID Card.\nIs Vehicle Number" + veh.getVehicleName() + " on gate ???.\n please varify", options);
						if (answer == 1) {
							RFIDMasterDao.executeQuery(_dbConnection, "update vehicle set card_init=1 where id="+veh.getId());
							veh.setId(1);
						}
					}catch(Exception ex){
						ex.printStackTrace();
						destroyIt = true;
					}finally{
						try{
							DBConnectionPool.returnConnectionToPoolNonWeb(_dbConnection, destroyIt);
						}catch(Exception ex){
							ex.printStackTrace();
						}
					}
				}
			});
		}

		@Override
		public boolean isSystemReady() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public ProcessStepProfile getProcessStepProfile(int materialCat) {
			return mainWindow != null && mainWindow.getWorkStationDetails() != null && mainWindow.getWorkStationDetails().getWorkStationProfile() != null ? mainWindow.getWorkStationDetails().getWorkStationProfile().getProcessStepProfile(materialCat) : ProcessStepProfile.getStandardProcessStepByMaterialCat(materialCat);
		}
	};
	private TPRWorkstationConfig getWorkstationConfigByReaderId(Connection conn, int readerId, Vehicle veh){
		if(MainWindow.getMenuInfo(currentMenuId) == null || contentScreenController == null){
			return null;
		}else{
			TPRWorkstationConfig tprWorkstationConfig = contentScreenController.getWorkstationConfig(conn, readerId, veh);
			if(tprWorkstationConfig == null){
				tprWorkstationConfig = MainWindow.getMenuInfo(currentMenuId).getTprWorkstionConfig(readerId);
			}
			return tprWorkstationConfig;
		}
	}
//	private String styleConnected = "-fx-background-color:green;";
//	private String styleDisconnected = "-fx-background-color:red";
	public void handlePeripheralConnection(final int _id,final PeripheralType _type, final PeripheralStatus _status){
		Platform.runLater(()->{
			this.headerController.handlePeripheralConnection(_id, _type, _status);
			/*int id =_id;
			PeripheralType type = _type;
			PeripheralStatus status = _status;
			String style = status == PeripheralStatus.CONNECTED ? styleConnected : styleDisconnected;
			if(!PeripheralConnectionStatus.updatePeripheralStatus(_id, _type, _status, type == PeripheralType.DATABASE ? mainWindow.getServerConnThresholdSec() * 1000 : Misc.getUndefInt()))
				return;
			if(PeripheralType.RFID == type){
				switch (id) {
				case 0:
					textRFIDOneConnected.setStyle(style);
					break;
				case 1:
					textRFIDTwoConnected.setStyle(style);
					break;
				case 2:
					textRFIDThreeConnected.setStyle(style);
					break;
				default:
					break;
				}
			}else if(PeripheralType.BARRIER == type){
				textBarrierConnected.setStyle(style);
			}else if(PeripheralType.WEIGHBRIDGE == type){
				textWeighBridgeConnected.setStyle(style);
			}else if(PeripheralType.WCS == type){
				textWeighBridgeCentricConnected.setStyle(style);
			}else if(PeripheralType.DATABASE == type){
				String message = null;
				String instance = null;
				if(mainWindow.isRemoteConnected()){
					if(status == PeripheralStatus.DISCONNECTED){//switch to local 
						message = "Server disconnected. Are you want to switch in local mode";
						instance = "desktop";
					}
				}else{
					if(status == PeripheralStatus.CONNECTED){//switch to local
						message = "Server connected. Are you want to switch in sever mode";
						instance = "remote";
					}
				}
				if(!Utils.isNull(message) && !Utils.isNull(instance)){
					int ans = prompt("Connection switch", message, new String[]{"No","Yes"});
					if(ans == 1){
						PropertyManager.setProperty(PropertyType.Database,"SERVER", instance);
						mainWindow.restartApplication();
					}
				}
				textServerConnected.setStyle(style);
			}*/
			
		});
	}
	NodeDBConnectionChecker serverConnectionChecker = null;
	private void startServices(){
		//new Thread(()->{
			try {
				System.out.println("Going to Start Services...");
				Properties prop = PropertyManager.getProperty(PropertyType.RfidReader);
	    		if(prop != null){
	    			if("1".equalsIgnoreCase(prop.getProperty("READER_ONE_PRESENT"))){
	    				rfidHandlerOne.start();
	    				
	    			}
	    			if("1".equalsIgnoreCase(prop.getProperty("READER_TWO_PRESENT"))){
	    				rfidHandlerTwo.start();
	    			}
	    			if("1".equalsIgnoreCase(prop.getProperty("READER_DESKTOP_PRESENT"))){
	    				//to do
	    			}
	    		}
	    		prop = PropertyManager.getProperty(PropertyType.WeighBridge);
	    		if(prop != null && "1".equalsIgnoreCase(prop.getProperty("PRESENT"))){
					if (weighBridge == null) {
						weighBridge = new WeighBridge();
						weighBridge.setConnectionStatusHandler(this::handlePeripheralConnection);
						weighBridge.setListener(new WeighBridgeListener() {
							@Override
							public void changeValue(String str) {
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										if(contentScreenController != null){
											contentScreenController.setWeighBridgeReading(str);
										}
									}
								});
							}

							@Override
							public void showDisconnection() {

							}

							@Override
							public void removeDisconnection() {

							}

							@Override
							public void isWBEmpty(boolean isEmpty) {
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										if(contentScreenController != null && token != null && token.getStatus() != Status.Token.PROCESSED)
											CONTROL_SAVE.setDisable(false);
									}
								});
								
							}
						});
					}
					weighBridge.startWeighBridge();
					weighBridge.startMonitor(10*1000);
	    		}
	    		prop = PropertyManager.getProperty(PropertyType.Barrier);
	    		if(prop != null && "1".equalsIgnoreCase(prop.getProperty("PRESENT"))){
	    			Barrier.setConnectionStatusHandler(this::handlePeripheralConnection);
	    			Barrier.startMonitor(10*1000);
	    		}
	    		prop = PropertyManager.getProperty(PropertyType.Centric);
	    		if(prop != null && "1".equalsIgnoreCase(prop.getProperty("PRESENT"))){
	    			WeighBridgeCentric.setConnectionStatusHandler(this::handlePeripheralConnection);
	    			WeighBridgeCentric.startMonitor(10*1000);
	    		}
				serverConnectionChecker = new NodeDBConnectionChecker("remote",30*1000, 30*1000,this::handlePeripheralConnection);
				serverConnectionChecker.start();
				System.out.println("Services Started...");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		//}).start();
	}


	private void clearForm(Connection conn, boolean clearToken,boolean clearTPRdata){
		clearForm(conn, clearToken, clearTPRdata, true);
	}
	public void clearForm(Connection conn, boolean clearToken,boolean clearTPRdata,boolean clearScreenData){
		clearForm(conn, clearToken, clearTPRdata, clearScreenData,true);
	}
	public void clearForm(Connection conn, boolean clearToken,boolean clearTPRdata,boolean clearScreenData, boolean modifyToken){
		clearForm(conn, clearToken, clearTPRdata, clearScreenData, modifyToken, false);
	}
	public void clearForm(Connection conn, boolean clearToken,boolean clearTPRdata,boolean clearScreenData, boolean modifyToken, boolean resetWeighmentStep){
		try {
			if (null != contentScreenController) {
				if(clearScreenData){
					labelBlockingReason.setVisible(false);
					contentScreenController.clearInputs();
					if(resetWeighmentStep)
						contentScreenController.resetWeighmentMode();
					clearCurrentFormFields();
					NodeExt nodeExt = findFormFieldById(ID_VEHICLE_FIELD);
					Node n = nodeExt == null ? null : nodeExt.getNode();
					if(n != null && n instanceof TextField){
						//((TextField) n).setEditable(ScreenConstant.MenuItemId.REGISTRATION.equalsIgnoreCase(currentMenuId));
						if(nodeExt.isVehicle()){
							//((TextField) n).setEditable(nodeExt.getVehicleEntryType() == VehicleEntryType.MANUAL ? true : false );
							((TextField) n).setDisable(nodeExt.getVehicleEntryType() == VehicleEntryType.MANUAL ? false : true );
						}
					}
					//save button
					CONTROL_SAVE.setText("Save");
					labelBlockingReason.setText("");
					CONTROL_SAVE.setDisable(true);
					if(resetWeighmentStep)
						setFocusToFirstInputField();
					if (currentMenuId.equalsIgnoreCase(ScreenConstant.MenuItemId.REGISTRATION)
							|| currentMenuId.equalsIgnoreCase(ScreenConstant.MenuItemId.REGISTRATION_AND_GATE))
						CONTROL_MANUAL.setVisible(false);
					else {
						CONTROL_MANUAL.setVisible(true);
						CONTROL_MANUAL.setDisable(!contentScreenController.isManualEntry());
					}
				}
				if(clearTPRdata)
					clearTPRSession(clearToken, modifyToken);
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	private void clearTPRSession(boolean clearWorkstation, boolean modifyToken){
		Connection conn = null;
		boolean destroyIt = false;
		try{
			//tpr related fields
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			readerId = 0;
			tpr = null;
			tprBlockManager = null;
			tpStep = null;
			if(modifyToken){
				//vehicle Related fields
				vehicleBlackListed = false;
				isVehicleExist = false;
				isNewVehicle = Status.VEHICLE.EXISTING_RF;
				vehicleId = Misc.getUndefInt();
				vehicleName = null;
				epcId = null;
				rfidData = null;
				if (clearWorkstation) {
					TokenManager.clearWorkstation();
				} else {
					if (token != null)
						TokenManager.returnToken(conn, token);
				}
				token = null;
			}
			//vehicle Related fields
			/*vehicleBlackListed = false;
			isVehicleExist = false;
			isNewVehicle = Status.VEHICLE.EXISTING_RF;
			vehicleId = Misc.getUndefInt();
			vehicleName = null;
			epcId = null;
			rfidData = null;*/
		}catch(Exception ex){
			ex.printStackTrace();
			destroyIt = true;
		}finally{
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	private boolean saveTPRData(boolean isProcessAuto) {
		boolean retval = false;
		Connection conn = null;
		Connection local = null;
		boolean destroyIt = false;
		System.out.println("########### Save TPR DATA ##########");
		try {
			long currTimeServerMillis = System.currentTimeMillis();
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			boolean isOverrideReq = "Override".equals(CONTROL_SAVE.getText());
			HashMap<Integer, Integer> questions = null;
			Vehicle veh = Vehicle.getVehicle(conn, tpr.getVehicleId());
			TPRWorkstationConfig tprWorkstationConfig = getWorkstationConfigByReaderId(conn,readerId,veh); //MainWindow.getMenuInfo(currentMenuId).getTprWorkstionConfig(readerId);
			int workStationType = tprWorkstationConfig.getWorkstationType();
			int matetialCat = tprWorkstationConfig.getMaterialCat();
			if(Misc.isUndef(matetialCat) && veh != null){
				matetialCat = !Misc.isUndef(tpr.getMaterialCat()) ? tpr.getMaterialCat() : veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.ROAD ? Type.TPRMATERIAL.COAL_ROAD : 
					veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.WASHERY ? Type.TPRMATERIAL.COAL_WASHERY :
						Type.TPRMATERIAL.COAL_INTERNAL; 
			}
			tpr.setTprCreateType(veh.getCardPurpose());
			if (!isOverrideReq && contentScreenController != null) {
				if(!contentScreenController.setTPRAndSaveNonTPRData(readerId, conn,tpr, tpStep )){
					destroyIt = true;
					return false;
				}
				questions = contentScreenController.getBlockingQuestions();
				tpr.setWorkStationOutTime(workStationType, new Date(System.currentTimeMillis()));
				tpr.setPreStepType(workStationType);
				tpr.setWorkStationCode(workStationType, mainWindow.getWorkStationDetails().getCode());
			}
			if(Misc.isUndef(tpr.getWorkStationInTime(workStationType)))
				tpr.setWorkStationInTime(workStationType, new Date(token.getLastSeen()));
			
			tpr.setRfidInfoId(veh == null ? Misc.getUndefInt() : veh.getRfidInfoId());
			if(matetialCat != Type.TPRMATERIAL.COAL_INTERNAL && workStationType == Type.WorkStationType.SECL_LOAD_GATE_OUT){//close
				tpr.setTprStatus(Status.TPR.CLOSE);
			}else if(workStationType == Type.WorkStationType.SECL_UNLOAD_GATE_OUT){
				tpr.setTprStatus(Status.TPR.CLOSE);
			}
			boolean isNew = Misc.isUndef(tpr.getTprId());
			TPRInformation.insertUpdateTpr(conn, tpr);
			if(isNew){
				String challanNo = TPRInformation.getChallanNo(TokenManager.workingModeServer, mainWindow.getWorkStationDetails().getCode(), mainWindow.getWorkStationDetails().getChallanSeries()+"", tpr.getTprId() );
				Date challanDate = new Date(System.currentTimeMillis());
				tpr.setChallanNo(challanNo);
				tpr.setChallanDate(challanDate);
				tpr.setRfChallanDate(challanDate);
				tpr.setRfChallanId(challanNo);
				TPRInformation.insertUpdateTpr(conn, tpr);
			}
			if(veh != null){ 
				if(veh.getVehicleRFIDInfo() != null && !Misc.isUndef(veh.getVehicleRFIDInfo().getId()) && Misc.isUndef(veh.getVehicleRFIDInfo().getIssuedTprId())){
					RFIDMasterDao.executeQuery(conn, "update vehicle_rfid_info set issued_tpr_id="+tpr.getTprId()+" where id="+veh.getVehicleRFIDInfo().getId());
				}
				if(tpr.getWeighmentStep() == WeighmentStep.tare){
					RFIDMasterDao.executeQuery(conn, "update vehicle set last_tare_tpr="+tpr.getTprId()+" where id="+veh.getId());
				}else if(tpr.getWeighmentStep() == WeighmentStep.gross){
					RFIDMasterDao.executeQuery(conn, "update vehicle set last_tare_tpr=null where id="+veh.getId());
				}
			}
			InsertTPRQuestionDetails(conn, readerId, questions, veh);
			InsertTPRStep(conn,readerId, isOverrideReq,currTimeServerMillis,mainWindow.getWorkStationDetails().getId(),workStationType,matetialCat);
			if (tprBlockManager != null) {
				tprBlockManager.calculateBlocking(conn);
				tprBlockManager.setTprBlockStatus(conn, tpr.getTprId(), userSession != null ? userSession.getUserId() : Misc.getUndefInt());
			}
			lastPrintingTPR = tpr;
			//save tpr to localDB
			if(mainWindow.isRemoteConnected()){
				local = NodeDBConnectionChecker.getDBConnection("desktop");
				tpr.setRemoteTPRId(tpr.getTprId());
				tpr.setTprId(Misc.getUndefInt());
				RFIDMasterDao.insert(local, tpr);
				tpr.setTprId(tpr.getRemoteTPRId());
			}
			
			retval = true;
			if(TPRecord.isWeighment(tprWorkstationConfig.getWorkstationType()) && weighBridge != null ){
				weighBridge.setEmpty(false);;
			}
			if(!isProcessAuto)
				showAlert("Message", "Data saved.....");
			Barrier.ChangeSignal(readerId);
			CONTROL_SAVE.setDisable(true);
		} catch (Exception ex) {
			if(!isProcessAuto)
				showAlert(Alert.AlertType.ERROR, "System Exception Message", UIConstant.SAVE_FAILER_MESSAGE);
			//JOptionPane.showMessageDialog(null, UIConstant.SAVE_FAILER_MESSAGE);
			ex.printStackTrace();
			destroyIt = true;
			retval =false;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				NodeDBConnectionChecker.returnConnection(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return retval;
	}
	private boolean InsertTPRQuestionDetails(Connection conn, int readerId, HashMap<Integer, Integer> quesAnsList , Vehicle veh) throws Exception {
		boolean isInsert = false;
		if(quesAnsList == null)
			return isInsert;
		TPRWorkstationConfig tprWorkstationConfig = getWorkstationConfigByReaderId(conn,readerId,veh);// MainWindow.getMenuInfo(currentMenuId).getTprWorkstionConfig(readerId);
		for (Map.Entry<Integer, Integer> entry : quesAnsList.entrySet()) {
			Integer questionId = entry.getKey();
			Integer answerId = entry.getValue();
			TPRDataUtils.updateTPRQuestion(conn, tpr.getTprId(), tprWorkstationConfig.getWorkstationType(), questionId, answerId, (userSession != null ? userSession.getUserId() : Misc.getUndefInt()));
		}
		return isInsert;
	}
	private int InsertTPRStep(Connection conn,int readerId, boolean override,long currTimeServerMillis,int workstationId,int workStationType,int materialCat) throws Exception {
		tpStep.setWorkStationId(workstationId);
		tpStep.setWorkStationType(workStationType);
		tpStep.setExitTime(new Date(currTimeServerMillis));
		tpStep.setUpdatedOn(new Date(currTimeServerMillis));
		tpStep.setUpdatedBy(userSession != null ? userSession.getUserId() : Misc.getUndefInt());
		tpStep.setMaterialCat(materialCat);
		tpStep.setSaveStatus(override ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
		tpStep.setHasValidRf(isNewVehicle == Status.VEHICLE.EXISTING_RF ? 1 : 0);
		RFIDMasterDao.insert(conn, tpStep,false);
		RFIDMasterDao.insert(conn, tpStep,true);
		return tpStep.getId();
	}


	public Triple<Boolean,Integer,Integer> checkAndGetVehiclId(TextField textBoxVehicleName) throws Exception {
		return checkAndGetVehiclId(false, textBoxVehicleName);
	}
	public Triple<Boolean,Integer,Integer> checkAndGetVehiclId(boolean createNewVehicle, TextField textBoxVehicleName) throws Exception {
		if(!textBoxVehicleName.isEditable())
			return null;
		int vehId = Misc.getUndefInt();
		Connection conn = null;
		boolean destroyIt = false;
		boolean _isVehicleExist = false;
		int _isNewVehicle = Status.VEHICLE.EXISTING_MANUAL;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			Pair<Integer, String> vehPair = null;
			if (Utils.isNull(textBoxVehicleName.getText())) {
				showAlert("Field Validation","Please Enter Vehicle !!!");
				return null;
			} else {
				String vehName;
				vehName = CacheTrack.standardizeName(textBoxVehicleName.getText());
				textBoxVehicleName.setText(vehName);
				vehPair = TPRInformation.getVehicle(conn, null, vehName);
				if (vehPair != null) {
					vehId = vehPair.first;
				}
				_isVehicleExist = !Misc.isUndef(vehId);
				//boolean createNewVehicle = currentMenuItem.isCreateNewVehicle();
				if (!_isVehicleExist ) {
					if(createNewVehicle) {
						String[] options = {"  Re-Enter  ", "  Continue  "};
						String msg = " Vehicle Not Exist ";
						int responseVehicleDialog = prompt("New Vehicle Creation", msg, options);
						if (responseVehicleDialog == 0) {
							textBoxVehicleName.setText("");
							return null;
						} else if (responseVehicleDialog == 1) {
							_isNewVehicle = Status.VEHICLE.NEW_MANUAL;
							_isVehicleExist = false;
							vehId = TPRDataUtils.insertVehicleSCEL(conn, vehName, (userSession != null ? userSession.getUserId() : Misc.getUndefInt()),mainWindow.getWorkStationDetails().getPortNodeId());
							//textBoxVehicleName.setEditable(false);
						}
					}else{
						textBoxVehicleName.setText("");
						return null;
					}
				}else{
					_isVehicleExist = true;
					_isNewVehicle = Status.VEHICLE.EXISTING_MANUAL;

				}
				//handleManualEntry(conn,_isVehicleExist,_isNewVehicle,vehId,vehName);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			destroyIt = true;
			throw ex;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return new Triple<Boolean,Integer,Integer>(isVehicleExist,_isNewVehicle, vehId);
	}

	public void handleManualEntry(Connection conn,int readerId, boolean isVehicleExist, int isNewVehicle, int vehId, String vehName) throws Exception {
		this.vehicleId = vehId;
		this.vehicleName = vehName;
		this.isVehicleExist = isVehicleExist;
		this.isNewVehicle = isNewVehicle;
		handleTprInit(conn,readerId);
		//request forced token for manual data
	}

	private void handleTprInit(Connection conn,int readerId) throws Exception {
		if(token == null || token.getStatus() == Status.Token.PROCESSED)
			token = TokenManager.createToken(conn,epcId,vehicleName,true);
		Vehicle veh = Vehicle.getVehicle(conn, vehicleId);
		if(rfidHandlerOne != null) {
			RFIDDataProcessor rfidDataProcessor =  rfidHandlerOne.getRfidProcessor();
			TPRWorkstationConfig tprWorkstationConfig = getWorkstationConfigByReaderId(conn,readerId,veh);// MainWindow.getMenuInfo(currentMenuId).getTprWorkstionConfig(Misc.isUndef(readerId) ? Type.Reader.IN : readerId);
			int materialCat = tprWorkstationConfig.getMaterialCat();
			if(Misc.isUndef(materialCat)){
            	if(veh != null){
            		materialCat = veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.ROAD ? Type.TPRMATERIAL.COAL_ROAD : 
        				veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.WASHERY ? Type.TPRMATERIAL.COAL_WASHERY :
        				Type.TPRMATERIAL.COAL_INTERNAL; 
            	}
			}
			rfidDataProcessor.processVehicleAndNotify(readerId, conn,token,epcId,vehicleId,rfidData,vehicleName,tprWorkstationConfig.getWorkstationType(),materialCat,null);
		}
	}
	public boolean issueTag(Vehicle vehicle)  {
		Connection conn = null;
		boolean destroyIt = false;
		int vehicleId= vehicle.getId();
		try{
			try {
				String instruction = "";
				if(!Misc.isUndef(vehicleId)) {
					conn = DBConnectionPool.getConnectionFromPoolNonWeb();
					vehicle.setUpdatedBy(userSession != null ? userSession.getUserId() : Misc.getUndefInt());
					vehicle.setRfid_issue_date(new Date());
					TPRDataUtils.TAG_ISSUE_RESPONSE response = TPRDataUtils.issueRFIDTag(vehicle);
					switch (response) {
					case ISSUED:
						showAlert("RFID TAG Issue","Tag issued.....");
						return true;
					case NOT_ISSUED:
						instruction = "Unable to write on card.please properly put card on reader";
						break;
					case READER_NOT_CONNECTED:
						instruction = "Reader Not Connected. Please connect reader";
						break;
					case NO_TAG:
						instruction = "No Tag on Reader.please put card on reader";
						break;
					case MULTIPLE_TAG:
						instruction = "There is multiple Tag on reader";
						break;
					}
				}else{
					instruction = "please fill vehicle Name then press Enter";
				}
				if(!Utils.isNull(instruction)){//alert
					instruction += " and try again. ";
					showAlert("RFID TAG Issue Failed",instruction);
				}
			} catch (RFIDException ex) {
				destroyIt = true;
				ex.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
			}finally {
				try{
					//showAlert("RFID TAG Issue Failed","Unable to issue card please try again.");
					DBConnectionPool.returnConnectionToPoolNonWeb(conn,destroyIt);
				}catch (Exception ex){
					ex.printStackTrace();
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return false;
	}


	public void setVehicleRFIDInfo(Connection conn,VehicleRFIDInfo vehRFIDInfo,TPRecord tpr) throws Exception{
		//to do
		if(vehRFIDInfo == null )
			return;
		int vehicleId = !Misc.isUndef(vehRFIDInfo.getVehicleId()) ?  vehRFIDInfo.getVehicleId() : (tpr == null ? Misc.getUndefInt() : tpr.getVehicleId());
		if(Misc.isUndef(vehRFIDInfo.getId())){
			RFIDMasterDao.insert(conn, vehRFIDInfo);
		}else{
			RFIDMasterDao.update(conn, vehRFIDInfo);
		}
		int vehRFIDInfoId = vehRFIDInfo.getId();
		RFIDMasterDao.executeQuery(conn, "update vehicle set rfid_info_id="+vehRFIDInfoId+" where vehicle_id="+vehicleId);
		if(tpr != null && !Misc.isUndef(vehRFIDInfoId)){
			RFIDMasterDao.executeQuery(conn, "update tp_record set rfid_info_id="+vehRFIDInfoId+" where tpr_id="+tpr.getTprId());
			RFIDMasterDao.executeQuery(conn, "update tp_record_apprvd set rfid_info_id="+vehRFIDInfoId+" where tpr_id="+tpr.getTprId());
		}
	}
	@Table("user_session")
	public class UserSession{
		@Column("user_id")
		private int userId = Misc.getUndefInt();
		private String name;
		private String userName;
		private boolean isSuper = false;
		private long loggedInTime;
		private long loggedOutTime;
		private long lastSeen;
		@Column("start")
		private Date start;
		@Column("end")
		private Date end;
		public int getUserId() {
			return userId;
		}
		public String getName() {
			return name;
		}
		public String getUserName() {
			return userName;
		}
		public long getLoggedInTime() {
			return loggedInTime;
		}
		public long getLoggedOutTime() {
			return loggedOutTime;
		}
		public void setLoggedOutTime(long loggedOutTime) {
			this.loggedOutTime = loggedOutTime;
		}
		public long getLastSeen() {
			return lastSeen;
		}
		public void setLastSeen(long lastSeen) {
			this.lastSeen = lastSeen;
		}
		public Date getStart() {
			return start;
		}
		public void setStart(Date start) {
			this.start = start;
		}
		public Date getEnd() {
			return end;
		}
		public void setEnd(Date end) {
			this.end = end;
		}
		public boolean isSuper() {
			return isSuper;
		}
		private UserSession(int userId, String name, String userName, long loggedInTime,boolean isSuper) {
			super();
			this.userId = userId;
			this.name = name;
			this.userName = userName;
			this.loggedInTime = loggedInTime;
			this.isSuper = isSuper;
			this.start = new Date(loggedInTime);
		}
		public void saveSession(Connection conn) throws Exception{
			if(this.start != null){
				this.end = new Date(System.currentTimeMillis());
				RFIDMasterDao.insert(conn, this);
				start = null;
				end = null;
			}
		}
	}
	private void showIdleMessage(){
		Platform.runLater(()->{
			if(userSession == null || userSession.start == null)
				return;
			Connection conn = null;
			boolean destroyIt = false;
			try{
				idleMonitor.startMonitoring();
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				userSession.saveSession(conn);
				Date idleFrom = new Date(System.currentTimeMillis() - mainWindow.getIdleThresholdSec()*1000);
				showAlert("Idle", "System Idle Since "+new SimpleDateFormat(ScreenConstant.DATE_FORMAT_DDMMYYYY_HHMMSS).format(idleFrom));
				userSession.setStart(new Date(System.currentTimeMillis()));
				idleMonitor.startMonitoring();
			}catch(Exception ex){
				ex.printStackTrace();
				destroyIt = true;
			}finally{
				try{
					DBConnectionPool.returnConnectionToPoolNonWeb(conn,destroyIt);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});
	}
	private boolean returnTag(Connection conn, Vehicle veh) throws Exception {
		try{
			if(veh != null){
				if(RFIDMaster.getDesktopReader() != null){
					while(prompt("Return Tag", "Try to return Tag", new String[]{"No", "Yes"}) == 1){
						ArrayList<String> tags = RFIDMaster.getDesktopReader().getRFIDTagList();
						if(tags == null || tags.size() == 0)
							continue;
						long i = tags.stream().filter(s->s.equalsIgnoreCase(veh.getEpcId())).count();
						if(i > 0){
							veh.setEpcId(null);
							veh.setRfid_issue_date(null);
							veh.setPreferedDriver(Misc.getUndefInt());
				        	veh.setCardType(Misc.getUndefInt());
				        	veh.setCardPurpose(Misc.getUndefInt());
				        	veh.setDoAssigned(null);
				        	veh.setPreferedMinesCode(null);
				        	veh.setCardInitDate(null);
				        	veh.setCardExpiaryDate(null);
				        	veh.setVehicleOnGate(Misc.getUndefInt());
				        	veh.setRfidInfoId(Misc.getUndefInt());
				        	RFIDMasterDao.update(conn,veh);
				        	VehicleRFIDInfo vehRFIDInfo = veh.getVehicleRFIDInfo();
				        	if(vehRFIDInfo != null){
				        		RFIDMasterDao.executeQuery(conn, "update vehicle_rfid_info set status=2 , return_date=now()  where id="+vehRFIDInfo.getId());
				        	}
				        	return true;
						}
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
        return false;
    }
	public boolean isPrintOnSave() {
		return printOnSave;
	}
	public void setPrintOnSave(boolean printOnSave) {
		this.printOnSave = printOnSave;
	}
	
	public void registerIdleMonitor(){
		idleMonitor = new IdleMonitors(Duration.seconds(mainWindow.getIdleThresholdSec()), ()->showIdleMessage());
		this.mainWindow.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				idleMonitor.notIdle();
			}
		});
		this.mainWindow.getScene().addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				idleMonitor.notIdle();
			}
		});

		mainWindow.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
	        public void handle(KeyEvent event) {
	        	handleCtrlKeyEvents(event);
	        }
	    });
	
	}
	
}
