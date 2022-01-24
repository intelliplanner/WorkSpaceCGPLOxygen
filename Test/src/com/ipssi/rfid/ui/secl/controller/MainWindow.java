package com.ipssi.rfid.ui.secl.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.controlsfx.control.textfield.TextFields;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Triple;
import com.ipssi.logger.RFLogger;
import com.ipssi.rfid.beans.SECLWorkstationDetails;
import com.ipssi.rfid.beans.WorkstationIpDetails;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.PeripheralConnectionStatus.PeripheralStatus;
import com.ipssi.rfid.readers.PeripheralConnectionStatus.PeripheralType;
import com.ipssi.rfid.readers.RFIDMaster;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.secl.controller.PropertyManager.PropertyType;
import com.ipssi.rfid.ui.secl.data.LovDao;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo.ActionPanelType;
import com.ipssi.rfid.ui.secl.data.TPRDataUtils;
import com.jfoenix.controls.CustomDecorator;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainWindow extends Application {
	private static String workStationCode = null;
	private static BorderPane contentLayout;
	private static HashMap<String, MenuItemInfo> menuScreenMap = new HashMap<>();
	private SECLWorkstationDetails workStationDetails = null;
	private int idleThresholdSec = 300;// sec
	private int serverConnThresholdSec = 300;// sec
	private boolean remoteConnected = true;

	public SECLWorkstationDetails getWorkStationDetails() {
		return workStationDetails;
	}

	public void initScreenCache() {// link menu with screens
		TPRWorkstationConfig tprWorkstationConfig = null;
		// login screen
		MenuItemInfo menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.LOGIN, ScreenConstant.ScreenTitle.LOGIN,
				ScreenConstant.ScreenLinks.LOGIN, false, Misc.getUndefInt(), ScreenConstant.MenuItemId.MenuTitle.LOGIN);
		menuScreenMap.put(ScreenConstant.MenuItemId.LOGIN, menuItem);

		// load gate
		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.LOAD_GATE, ScreenConstant.ScreenTitle.LOAD_GATE,
				ScreenConstant.ScreenLinks.LOAD_GATE, true, ScreenConstant.MenuItemId.PRIV.LOAD_GATE,
				ScreenConstant.MenuItemId.MenuTitle.LOAD_GATE);
		tprWorkstationConfig = new TPRWorkstationConfig(Type.WorkStationType.SECL_LOAD_GATE_IN, Misc.getUndefInt());
		tprWorkstationConfig.setInitCard(true);
		menuItem.registerReaderNotification(Type.Reader.IN, true, tprWorkstationConfig);
		tprWorkstationConfig = new TPRWorkstationConfig(Type.WorkStationType.SECL_LOAD_GATE_OUT, Misc.getUndefInt());
		tprWorkstationConfig.setReturnCard(true);
		menuItem.registerReaderNotification(Type.Reader.OUT, true, tprWorkstationConfig);
		menuItem.setProcessAuto(true);
		menuItem.setLoad(true);
		menuItem.setActionPanelType(ActionPanelType.NON_TPR);
		menuScreenMap.put(ScreenConstant.MenuItemId.LOAD_GATE, menuItem);

		// Internal Load Gross-Tare
		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.LOAD_INTERNAL_GROSS_TARE,
				ScreenConstant.ScreenTitle.LOAD_INTERNAL_GROSS_TARE,
				ScreenConstant.ScreenLinks.LOAD_INTERNAL_GROSS_TARE, false,
				ScreenConstant.MenuItemId.PRIV.LOAD_INTERNAL_GROSS_TARE,
				ScreenConstant.MenuItemId.MenuTitle.LOAD_INTERNAL_GROSS_TARE);
		tprWorkstationConfig = new TPRWorkstationConfig(Type.WorkStationType.SECL_LOAD_INT_WB_GROSS,
				Type.TPRMATERIAL.COAL_INTERNAL);
		menuItem.registerReaderNotification(Type.Reader.IN, true, tprWorkstationConfig);
		menuItem.registerReaderNotification(Type.Reader.OUT, true, tprWorkstationConfig);
		menuItem.setLoad(true);
		menuScreenMap.put(ScreenConstant.MenuItemId.LOAD_INTERNAL_GROSS_TARE, menuItem);


		// Road Load Gross-Tare
		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.LOAD_ROAD_GROSS_TARE,
				ScreenConstant.ScreenTitle.LOAD_ROAD_GROSS_TARE, ScreenConstant.ScreenLinks.LOAD_ROAD_GROSS_TARE, false,
				ScreenConstant.MenuItemId.PRIV.LOAD_ROAD_GROSS_TARE,
				ScreenConstant.MenuItemId.MenuTitle.LOAD_ROAD_GROSS_TARE);
		tprWorkstationConfig = new TPRWorkstationConfig(Type.WorkStationType.SECL_LOAD_ROAD_WB_GROSS,
				Type.TPRMATERIAL.COAL_ROAD);
		menuItem.registerReaderNotification(Type.Reader.IN, true, tprWorkstationConfig);
		menuItem.registerReaderNotification(Type.Reader.OUT, true, tprWorkstationConfig);
		menuItem.setLoad(true);
		menuScreenMap.put(ScreenConstant.MenuItemId.LOAD_ROAD_GROSS_TARE, menuItem);

		// Washery Gross-Tare
		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.LOAD_WASHERY_GROSS_TARE,
				ScreenConstant.ScreenTitle.LOAD_WASHERY_GROSS_TARE, ScreenConstant.ScreenLinks.LOAD_WASHERY_GROSS_TARE,
				false, ScreenConstant.MenuItemId.PRIV.LOAD_WASHERY_GROSS_TARE,
				ScreenConstant.MenuItemId.MenuTitle.LOAD_WASHERY_GROSS_TARE);
		tprWorkstationConfig = new TPRWorkstationConfig(Type.WorkStationType.SECL_LOAD_WASHERY_GROSS,
				Type.TPRMATERIAL.COAL_WASHERY);
		menuItem.registerReaderNotification(Type.Reader.IN, true, tprWorkstationConfig);
		menuItem.registerReaderNotification(Type.Reader.OUT, true, tprWorkstationConfig);
		menuItem.setLoad(true);
		menuScreenMap.put(ScreenConstant.MenuItemId.LOAD_WASHERY_GROSS_TARE, menuItem);




		/*		// Internal Load Tare
		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.LOAD_INTERNAL_TARE,
				ScreenConstant.ScreenTitle.LOAD_INTERNAL_TARE, ScreenConstant.ScreenLinks.LOAD_INTERNAL_TARE, false,
				ScreenConstant.MenuItemId.PRIV.LOAD_INTERNAL_TARE,
				ScreenConstant.MenuItemId.MenuTitle.LOAD_INTERNAL_TARE);
		tprWorkstationConfig = new TPRWorkstationConfig(Type.WorkStationType.SECL_LOAD_INT_WB_TARE,
				Type.TPRMATERIAL.COAL_INTERNAL);
		menuItem.registerReaderNotification(Type.Reader.IN, true, tprWorkstationConfig);
		menuItem.registerReaderNotification(Type.Reader.OUT, true, tprWorkstationConfig);
		menuItem.setLoad(true);
		menuScreenMap.put(ScreenConstant.MenuItemId.LOAD_INTERNAL_TARE, menuItem);

		// Internal Load Gross
		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.LOAD_INTERNAL_GROSS,
				ScreenConstant.ScreenTitle.LOAD_INTERNAL_GROSS, ScreenConstant.ScreenLinks.LOAD_INTERNAL_GROSS, false,
				ScreenConstant.MenuItemId.PRIV.LOAD_INTERNAL_GROSS,
				ScreenConstant.MenuItemId.MenuTitle.LOAD_INTERNAL_GROSS);
		tprWorkstationConfig = new TPRWorkstationConfig(Type.WorkStationType.SECL_LOAD_INT_WB_GROSS,
				Type.TPRMATERIAL.COAL_INTERNAL);
		menuItem.registerReaderNotification(Type.Reader.IN, true, tprWorkstationConfig);
		menuItem.registerReaderNotification(Type.Reader.OUT, true, tprWorkstationConfig);
		menuItem.setLoad(true);
		menuScreenMap.put(ScreenConstant.MenuItemId.LOAD_INTERNAL_GROSS, menuItem);


		// Road Load Tare
		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.LOAD_ROAD_TARE, ScreenConstant.ScreenTitle.LOAD_ROAD_TARE,
				ScreenConstant.ScreenLinks.LOAD_ROAD_TARE, false, ScreenConstant.MenuItemId.PRIV.LOAD_ROAD_TARE,
				ScreenConstant.MenuItemId.MenuTitle.LOAD_ROAD_TARE);
		tprWorkstationConfig = new TPRWorkstationConfig(Type.WorkStationType.SECL_LOAD_ROAD_WB_TARE,
				Type.TPRMATERIAL.COAL_ROAD);
		menuItem.registerReaderNotification(Type.Reader.IN, true, tprWorkstationConfig);
		menuItem.registerReaderNotification(Type.Reader.OUT, true, tprWorkstationConfig);
		menuItem.setLoad(true);
		menuScreenMap.put(ScreenConstant.MenuItemId.LOAD_ROAD_TARE, menuItem);

		// Road Load Gross
		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.LOAD_ROAD_GROSS,
				ScreenConstant.ScreenTitle.LOAD_ROAD_GROSS, ScreenConstant.ScreenLinks.LOAD_ROAD_GROSS, false,
				ScreenConstant.MenuItemId.PRIV.LOAD_ROAD_GROSS, ScreenConstant.MenuItemId.MenuTitle.LOAD_ROAD_GROSS);
		tprWorkstationConfig = new TPRWorkstationConfig(Type.WorkStationType.SECL_LOAD_ROAD_WB_GROSS,
				Type.TPRMATERIAL.COAL_ROAD);
		menuItem.registerReaderNotification(Type.Reader.IN, true, tprWorkstationConfig);
		menuItem.registerReaderNotification(Type.Reader.OUT, true, tprWorkstationConfig);
		menuItem.setLoad(true);
		menuScreenMap.put(ScreenConstant.MenuItemId.LOAD_ROAD_GROSS, menuItem);

				// Unload internal gross
		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.UNLOAD_INTERNAL_GROSS,
				ScreenConstant.ScreenTitle.UNLOAD_INTERNAL_GROSS, ScreenConstant.ScreenLinks.UNLOAD_INTERNAL_GROSS,
				true, ScreenConstant.MenuItemId.PRIV.UNLOAD_INTERNAL_GROSS,
				ScreenConstant.MenuItemId.MenuTitle.UNLOAD_INTERNAL_GROSS);
		tprWorkstationConfig = new TPRWorkstationConfig(Type.WorkStationType.SECL_UNLOAD_INT_WB_GROSS,
				Type.TPRMATERIAL.COAL_INTERNAL);
		menuItem.registerReaderNotification(Type.Reader.IN, true, tprWorkstationConfig);
		menuItem.registerReaderNotification(Type.Reader.OUT, true, tprWorkstationConfig);
		menuItem.setLoad(false);
		menuScreenMap.put(ScreenConstant.MenuItemId.UNLOAD_INTERNAL_GROSS, menuItem);

		// Unload internal tare
		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.UNLOAD_INTERNAL_TARE,
				ScreenConstant.ScreenTitle.UNLOAD_INTERNAL_TARE, ScreenConstant.ScreenLinks.UNLOAD_INTERNAL_TARE, true,
				ScreenConstant.MenuItemId.PRIV.UNLOAD_INTERNAL_TARE,
				ScreenConstant.MenuItemId.MenuTitle.UNLOAD_INTERNAL_TARE);
		tprWorkstationConfig = new TPRWorkstationConfig(Type.WorkStationType.SECL_UNLOAD_INT_WB_TARE,
				Type.TPRMATERIAL.COAL_INTERNAL);
		menuItem.registerReaderNotification(Type.Reader.IN, true, tprWorkstationConfig);
		menuItem.registerReaderNotification(Type.Reader.OUT, true, tprWorkstationConfig);
		menuItem.setLoad(false);
		menuScreenMap.put(ScreenConstant.MenuItemId.UNLOAD_INTERNAL_TARE, menuItem);
		 */

		// Unload Gate
		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.UNLOAD_GATE, ScreenConstant.ScreenTitle.UNLOAD_GATE,
				ScreenConstant.ScreenLinks.UNLOAD_GATE, true, ScreenConstant.MenuItemId.PRIV.UNLOAD_GATE,
				ScreenConstant.MenuItemId.MenuTitle.UNLOAD_GATE);
		tprWorkstationConfig = new TPRWorkstationConfig(Type.WorkStationType.SECL_UNLOAD_GATE_IN, Misc.getUndefInt());
		tprWorkstationConfig.setInitCard(true);
		menuItem.registerReaderNotification(Type.Reader.IN, true, tprWorkstationConfig);
		tprWorkstationConfig = new TPRWorkstationConfig(Type.WorkStationType.SECL_UNLOAD_GATE_OUT, Misc.getUndefInt());
		tprWorkstationConfig.setReturnCard(true);
		menuItem.registerReaderNotification(Type.Reader.OUT, true, tprWorkstationConfig);
		menuItem.setProcessAuto(true);
		menuItem.setLoad(false);
		menuItem.setActionPanelType(ActionPanelType.NON_TPR);
		menuScreenMap.put(ScreenConstant.MenuItemId.UNLOAD_GATE, menuItem);

		// Unload internal gross-tare
		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.UNLOAD_INTERNAL_GROSS_TARE,
				ScreenConstant.ScreenTitle.UNLOAD_INTERNAL_GROSS_TARE,
				ScreenConstant.ScreenLinks.UNLOAD_INTERNAL_GROSS_TARE, true,
				ScreenConstant.MenuItemId.PRIV.UNLOAD_INTERNAL_GROSS_TARE,
				ScreenConstant.MenuItemId.MenuTitle.UNLOAD_INTERNAL_GROSS_TARE);
		tprWorkstationConfig = new TPRWorkstationConfig(Type.WorkStationType.SECL_UNLOAD_INT_WB_GROSS,
				Type.TPRMATERIAL.COAL_INTERNAL);
		menuItem.registerReaderNotification(Type.Reader.IN, true, tprWorkstationConfig);
		menuItem.registerReaderNotification(Type.Reader.OUT, true, tprWorkstationConfig);
		menuItem.setLoad(false);
		menuScreenMap.put(ScreenConstant.MenuItemId.UNLOAD_INTERNAL_GROSS_TARE, menuItem);

		menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.SETTINGS, ScreenConstant.ScreenTitle.SETTINGS,
				ScreenConstant.ScreenLinks.SETTINGS, false, ScreenConstant.MenuItemId.PRIV.SETTINGS,
				ScreenConstant.MenuItemId.MenuTitle.SETTINGS);
		menuItem.setScreenTitle("Settings");
		menuScreenMap.put(ScreenConstant.MenuItemId.SETTINGS, menuItem);
		/*menuItem = new MenuItemInfo(ScreenConstant.MenuItemId.PRINT, ScreenConstant.ScreenTitle.PRINT,
				ScreenConstant.ScreenLinks.PRINT, false, ScreenConstant.MenuItemId.PRIV.PRINT,
				ScreenConstant.MenuItemId.MenuTitle.PRINT);
		menuItem.setScreenTitle("Print");
		menuScreenMap.put(ScreenConstant.MenuItemId.PRINT, menuItem);*/
	}

	private MainController mainController = null;
	private static int portNodeId = Misc.getUndefInt();
	Scene scene;
	Stage mainStag;
	private SplashController splashController = null;
	private HeaderController headerController;
	private AnchorPane peripheralPanel;


	public void initSystem() throws IOException{
		if(splashController != null)
			splashController.showMessages("Init System Properties");
		TokenManager.initSystemConfigNew();
		if (!TokenManager.isDebug) {
			RFLogger.init();
			RFLogger.RouteStdOutErrToFile();
		}
		TPRInformation.setSameStationTprThresholdMinutes(Misc.getParamAsLong(PropertyManager.getPropertyVal(PropertyType.System, "SAME_STATION_MINUTES"), 60));
		/*
		 * Properties systemProp = PropertyManager.getProperty(PropertyType.System);
		 * String currentVersion = systemProp.getProperty("system_version");
		 * String updateVersion = systemProp.getProperty("update_version");
		 * if(Utils.isNull(currentVersion) ||
		 * !ScreenConstant.VERSION.equalsIgnoreCase(currentVersion)){
		 * currentVersion = ScreenConstant.VERSION;
		 * PropertyManager.setProperty(PropertyType.System, "system_version",
		 * currentVersion);
		 * if(ScreenConstant.VERSION.equalsIgnoreCase(updateVersion))
		 * PropertyManager.setProperty(PropertyType.System, "update", "0"); }
		 * boolean doUpdate =
		 * "1".equalsIgnoreCase(systemProp.getProperty("update"));
		 * System.out.println("going to update = "+doUpdate); if(doUpdate){
		 * updateApplication();
		 * 
		 * }
		 */
		if(splashController != null)
			splashController.showMessages("Init Screens");
		initScreenCache();
		if(splashController != null)
			splashController.showMessages("Set Running Mode");
		String instance = PropertyManager.getPropertyVal(PropertyType.Database, "SERVER");
		remoteConnected = instance.equalsIgnoreCase("remote");
		System.out.println("Instance Running Mode :" + instance);
		if (isRemoteConnected()) {
			String server = PropertyManager.getPropertyVal(PropertyType.Database, instance + ".DBConn.host");
			int port = Misc
					.getParamAsInt(PropertyManager.getPropertyVal(PropertyType.Database, instance + ".DBConn.port"));
			System.out.println("instance:" + instance + "," + server + "," + port);
			remoteConnected = !Utils.isNull(server) && !Utils.isNull(port) && RFIDMaster.ishostAvailable(server, port);
			if (!remoteConnected) {
				instance = "desktop";
				PropertyManager.setProperty(PropertyType.Database, "SERVER", instance);
			}
		}
		Misc.setServerName(instance);
		System.out.println("Config file:" + PropertyManager.BASE + "\\new_conn.property");
		// Misc.setConnProperty(getClass().getResource("/new_conn.property").toURI().toString().replace("file:/",""));
		Misc.setConnProperty(PropertyManager.BASE + "\\new_conn.property");
		Connection conn = null;
		boolean destroyIt = false;
		boolean serverConnected = false;
		try {
			if(splashController != null)
				splashController.showMessages("Initialize workstation parameters");
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			this.workStationDetails = SECLWorkstationDetails.getWorkStation(conn);
			if (this.workStationDetails != null) {
				workStationCode = workStationDetails.getCode();
				if (!Misc.isUndef(this.workStationDetails.getIdleThresholdSeconds()))
					this.idleThresholdSec = this.workStationDetails.getIdleThresholdSeconds();
				if (!Misc.isUndef(this.workStationDetails.getServerConnThresholdSec()))
					this.serverConnThresholdSec = this.workStationDetails.getServerConnThresholdSec();
				WorkstationIpDetails workstationIpDetails = workStationDetails.getWorkStationIpDetails();
				if (workstationIpDetails != null && remoteConnected) {
					//
					String nodeName = "desktop";
					Properties prop = PropertyManager.getProperty(PropertyType.Database);
					String database = prop.getProperty(nodeName + ".DBConn.Database");
					String user = prop.getProperty(nodeName + ".DBConn.userName");
					String pass = prop.getProperty(nodeName + ".DBConn.password");
					String port = prop.getProperty(nodeName + ".DBConn.port");
					workstationIpDetails.setIp(SECLWorkstationDetails.getCurrentIP());
					workstationIpDetails.setDb(database);
					workstationIpDetails.setPort(port);
					workstationIpDetails.setUser(user);
					workstationIpDetails.setPassword(pass);
					try {
						RFIDMasterDao.update(conn, workstationIpDetails);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			if (isRemoteConnected()) {
				if(splashController != null)
					splashController.showMessages("Send recorded data to server");
				TPRDataUtils.sendTprDataToServer(workStationDetails.getCode());
			}
			TokenManager.useSECLRFIDReaderProcess = true;

			if (Misc.isUndef(this.workStationDetails.getPortNodeId()))
				this.workStationDetails.setPortNodeId(TokenManager.portNodeId);
			else
				TokenManager.portNodeId = this.getWorkStationDetails().getPortNodeId();
			portNodeId = this.getWorkStationDetails().getPortNodeId();
			if (Misc.isUndef(this.workStationDetails.getMinesId()))
				this.workStationDetails.setMinesId(TokenManager.minesId);

			/*
			 * if (TokenManager.syncClock) { new SyncClockService().start(); }
			 */
			serverConnected = "remote".equalsIgnoreCase(instance);
		} catch (Exception ex) {
			ex.printStackTrace();
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if(splashController != null)
			splashController.showMessages("Loading Main Screen");
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(ScreenConstant.ScreenLinks.MAIN_WINDOW));
		contentLayout = fxmlLoader.load();
		mainController = fxmlLoader.getController();
		if (mainController != null) {
			FXMLLoader peripheralFXMLLoader = new FXMLLoader(getClass().getResource(ScreenConstant.ScreenLinks.MAIN_PERIPHERAL));
			peripheralPanel = peripheralFXMLLoader.load();
			headerController = peripheralFXMLLoader.getController();
			mainController.init(this,splashController,headerController);
			mainController.clearSession();
			if (serverConnected)
				mainController.handlePeripheralConnection(Misc.getUndefInt(), PeripheralType.DATABASE,
						PeripheralStatus.CONNECTED);
		}
	}

	public static MenuItemInfo getMenuInfo(String menuId) {
		return menuScreenMap.get(menuId);
	}

	public void loadScreens(HashMap<String, Triple<Node, ControllerI, ArrayList<NodeExt>>> cachedScreenNodes,
			BorderPane mainForm, EventHandler<KeyEvent> onEnterKeyPress) {

		menuScreenMap.forEach((menuId, menu) -> {
			try {
				System.out.println(menuId);
				FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource(menu.getScreenURL()));
				Node node = fxmlLoader.load();
				ArrayList<NodeExt> formFields = getAllNodes((Parent) node);
				cachedScreenNodes.put(menuId, new Triple<>(node, fxmlLoader.getController(), formFields));
				mainForm.setCenter(node);
				formFields.forEach(nodeExt -> {
					if (nodeExt != null && nodeExt.getNode() != null && nodeExt.isField()) {
						nodeExt.getNode().disableProperty().addListener((observable, oldValue, newValue) -> {
							if (newValue)
								nodeExt.getNode().setStyle("-fx-opacity: 1;");
						});
						if (!nodeExt.isNoFocus())
							nodeExt.getNode().setOnKeyPressed(onEnterKeyPress);
						if (nodeExt.isAutoComplete() && nodeExt.getAutoCompleteSrc() != null
								&& nodeExt.getNode() instanceof TextField) {
							TextFields.bindAutoCompletion((TextField) nodeExt.getNode(), t -> !Utils.isNull(nodeExt.getAutoCompleteNameField()) ?
									(LovDao.getFieldSuggestion(portNodeId, t.getUserText(), nodeExt.getAutoCompleteSrc()))
									:(LovDao.getFieldSuggestionMerged(portNodeId, t.getUserText(), nodeExt.getAutoCompleteSrc())));

						}
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		mainForm.setCenter(null);
	}

	public static ArrayList<NodeExt> getAllNodes(Parent root) {
		ArrayList<NodeExt> nodes = new ArrayList<NodeExt>();
		addAllDescendents(root, nodes);
		return nodes;
	}

	private static void addAllDescendents(Parent parent, ArrayList<NodeExt> nodes) {
		for (Node node : parent.getChildrenUnmodifiable()) {
			if (node instanceof Control && node.getId() != null && node.getId().length() > 0)
				nodes.add(NodeExt.getNodeExt(node));
			if (node instanceof Parent)
				addAllDescendents((Parent) node, nodes);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	public Scene getScene() {
		return scene;
	}

	public Stage getMainStag() {
		return mainStag;
	}

	public void updateApplication() {
		try {
			System.out.println("start updator");
			final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			final File target = new File(MainWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			final File upateJar = new File(PropertyManager.BASE + "updator.jar");
			/* is it a jar file? */
			if (upateJar == null || !upateJar.getName().endsWith(".jar"))
				return;
			/* Build command: java -jar application.jar */
			PropertyManager.setUpdator();
			System.out.println("start updator " + upateJar);
			PropertyManager.setProperty(PropertyType.System, "update_file",
					System.getProperty("java.io.tmpdir").replaceAll(" ", "%20") + "SeclDesktop.jar");
			PropertyManager.setProperty(PropertyType.System, "target_file", target.getPath().replaceAll(" ", "%20"));
			final ArrayList<String> command = new ArrayList<String>();
			command.add(javaBin);
			command.add("-jar");
			command.add(upateJar.getPath());
			System.out.println(javaBin + " -jar " + upateJar.getPath());
			final ProcessBuilder builder = new ProcessBuilder(command);
			builder.start();
			System.out.println("going to restart");
			System.exit(0);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void restartApplication() {
		try {
			final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			final File currentJar = new File(
					MainWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			/* is it a jar file? */
			if (!currentJar.getName().endsWith(".jar"))
				return;
			/* Build command: java -jar application.jar */
			final ArrayList<String> command = new ArrayList<String>();
			command.add(javaBin);
			command.add("-jar");
			command.add(currentJar.getPath());
			System.out.println(javaBin + " -jar " + currentJar.getPath());
			final ProcessBuilder builder = new ProcessBuilder(command);
			builder.start();
			System.exit(0);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public int getIdleThresholdSec() {
		return idleThresholdSec;
	}

	public int getServerConnThresholdSec() {
		return serverConnThresholdSec;
	}

	public boolean isRemoteConnected() {
		return remoteConnected;
	}
	private static final int SPLASH_WIDTH = 400;
	private static final int SPLASH_HEIGHT = 250;

	@Override
	public void start(final Stage initStage) throws Exception {
		final Task<ObservableList<String>> initTask = new Task<ObservableList<String>>() {
			@Override
			protected ObservableList<String> call() throws InterruptedException {
				updateMessage("Starting System . . .");
				try {
					initSystem();
					System.out.println();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};

		showSplash(
				initStage,
				initTask,
				() -> showMainStage()
				);
		new Thread(initTask).start();
	}

	
	private void showMainStage() {
		mainStag = new Stage();
		CustomDecorator decorator = new CustomDecorator(mainStag, contentLayout);
		decorator.setCustomMaximize(true);
		decorator.getButtonsContainer().getChildren().set(0,peripheralPanel);
		this.scene = new Scene(decorator, 800, 600);
		this.scene.getStylesheets().add(MainWindow.class.getResource(ScreenConstant.BASE + "css/main.css").toExternalForm());
		mainStag.setMinWidth(800);
		mainStag.setMinHeight(600);
		mainStag.setScene(scene);
		decorator.setMaximized(true);
		mainStag.setTitle("Secl Desktop Manager " + ScreenConstant.VERSION);
		mainStag.show();
		if(mainController != null){
			mainController.registerIdleMonitor();
			mainController.setPrintOnSave("1".equalsIgnoreCase(PropertyManager.getPropertyVal(PropertyType.System, "PRINT_ON_SAVE")));
		}
		decorator.setOnCloseButtonAction(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.exit(0);
			}
		});
	}
	private void showSplash(
			final Stage initStage,
			Task<?> task,
			InitCompletionHandler initCompletionHandler
			) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(ScreenConstant.ScreenLinks.SPLASH));
		StackPane mainScreenlayout = fxmlLoader.load();
		this.splashController = fxmlLoader.getController();
		task.stateProperty().addListener((observableValue, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				initStage.toFront();
				FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), mainScreenlayout);
				fadeSplash.setFromValue(1.0);
				fadeSplash.setToValue(0.0);
				fadeSplash.setOnFinished(actionEvent -> initStage.hide());
				fadeSplash.play();

				initCompletionHandler.complete();
			} // todo add code to gracefully handle other task states.
		});

		//JFXDecorator decorator = new JFXDecorator(mainStag, mainScreenlayout);
		Scene splashScene = new Scene(mainScreenlayout, Color.TRANSPARENT);
		splashScene.getStylesheets().add(MainWindow.class.getResource(ScreenConstant.BASE + "css/main.css").toExternalForm());
		final Rectangle2D bounds = Screen.getPrimary().getBounds();
		initStage.setScene(splashScene);
		initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
		initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
		initStage.initStyle(StageStyle.TRANSPARENT);
		initStage.setAlwaysOnTop(true);
		initStage.show();
	}

	public interface InitCompletionHandler {
		void complete();
	}

	public static String getWorkStationCode() {
		return workStationCode;
	}

	
	
}
