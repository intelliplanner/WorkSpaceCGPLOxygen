package com.ipssi.rfid.constant;

import com.ipssi.gen.utils.Misc;

public class ScreenConstant {

	public static final int mutexPortNo = 16132;
	public static final String VERSION = "1.6";
	public static final String DATE_FORMAT_DDMMYYYY_HHMM = "dd/MM/yyyy HH:mm";
	public static final String DATE_FORMAT_DDMMYYYY_HHMMSS = DATE_FORMAT_DDMMYYYY_HHMM + ":ss";
	public static final String MYSQL_FORMAT_YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMAT_DDMMYY = "dd-MM-yyyy";
	public static final String TIME_FORMAT_HHMMSS = "HH:mm:ss";
	public static final String BASE = "/com/ipssi/rfid/ui/view/";
	public static final String MPL_BASE = "/com/ipssi/mpl/dhama_gudiya/ui/view/";
	public static final String IMAGE_BASE = "/images/";
	public static final String styleRed = "-fx-background-color:red";
	public static final String styleGreen = "-fx-background-color:green";
	public static final String styleYellow = "-fx-background-color:yellow";

	public static String SAVE_FAILER_MESSAGE = "Some Exception occurs, unable to process your request\nplease try again";

	// answer status
	public static final int YES = 1;
	public static final int NO = 2;
	public static final int NC = 3;
	public static final int NOSELECTED = 4;
	// override status
	public static final int OVERRIDE = 2;
	public static final int BLOCKED = 3;
	public static final int NOT_BLOCKED = 1;

	public static class ScreenLinks {

		public static final String MAIN_WINDOW = BASE + "MainWindow.fxml";
		public static final String MAIN_WINDOW_OLD = BASE + "MainWindow_old.fxml";
		public static final String SPLASH = BASE + "SplashView.fxml";
		public static final String LOGIN_WINDOW = BASE + "Login.fxml";
		// public static final String LOGIN_WINDOW_NEW = BASE + "Loginin_New.fxml";
		public static final String GATE_IN_WINDOW = BASE + "GateInWindow.fxml";
		public static final String GATE_OUT_WINDOW = BASE + "GateOutWindow.fxml";
		public static final String TARE_WB_WINDOW = BASE + "TareWeighmentWindow.fxml";
		public static final String GROSS_WB_WINDOW = BASE + "GrossWeighmentWindow.fxml";
		public static final String WELCOME_SCREEN_WINDOW = BASE + "WelcomeScreen.fxml";
		public static final String WELCOME_SCREEN_DHAMA_GUDIYA_WINDOW = BASE + "WelcomeScreen_Dhama.fxml";
		public static final String test = BASE + "TestFXML.fxml";
		public static final String test2 = BASE + "TestFxml2.fxml";
		public static final String test3 = BASE + "ZTestFXML_NotUse.fxml";
		public static final String PRINT_SLIP = BASE + "PrintSlip.fxml";
		public static final String SETTING_WINDOW = BASE + "Settings.fxml";
		public static final String TRANPORTER_WINDOW = BASE + "TransporterDetailWindow.fxml";
		public static final String READ_TAG_WINDOW = BASE + "ReadTagWindow.fxml";
		public static final String TPR_DETAILS_WINDOW = BASE + "ReportViewWindow.fxml";
		public static final String MPL_RFID_HANDLER_WINDOW = MPL_BASE + "MplRfidHandlerWindowController.fxml";
		public static final String SALES_ORDER_CANCEL_WINDOW = BASE + "SalesOrderCancellationForm.fxml";

		/*----------------------------Setting Screens----------------------------------*/
		public static final String DATABASE_SETTING_WINDOW = BASE + "DatabaseSetting.fxml";
		public static final String SYSTEM_CONFIG_SETTING_WINDOW = BASE + "SystemConfigSetting.fxml";
		public static final String GATE_IN_SETTING_WINDOW = BASE + "GateInWindowSetting.fxml";
		public static final String GATE_OUT_SETTING_WINDOW = BASE + "GateOutWindowSetting.fxml";
		public static final String WB_TARE_SETTING_WINDOW = BASE + "TareWeighmentWindowSetting.fxml";
		public static final String WB_GROSS_SETTING_WINDOW = BASE + "GrossWeighmentWindowSetting.fxml";
		public static final String RFID_READER_SETTING_WINDOW = BASE + "RfidWindowSetting.fxml";
		public static final String BARRIER_SETTING_WINDOW = BASE + "BarrierWindowSetting.fxml";
		public static final String WEIGHBRIDGE_SETTING_WINDOW = BASE + "WeighBridgeWindowSetting.fxml";
		public static final String TAG_READ_SETTING_WINDOW = BASE + "ReadTagSettingWindow.fxml";
		public static final String PRINT_GATE_PASS_WINDOW = BASE + "PrintGateInPass.fxml";
		// public static final String CREATE_USER = BASE + "CreateUser.fxml";
	}

	public static class ScreenTitle {

		public static final String LOGIN = "Login";
		public static final String GATE_IN = "GATE-IN";
		public static final String GATE_OUT = "GATE-OUT";
		public static final String TARE_WB = "TARE-WEIGHMENT";
		public static final String GROSS_WB = "GROSS-WEIGHMENT";
		public static final String MAIN_WINDOW = "WELCOME TO CGPL";
		public static final String WELCOME_SCREEN = "WELCOME TO CGPL";
		public static final String SETTING_WINDOW = "APPLICATION CONFIGURATION";
		public static final String DATABASE_SETTING_WINDOW = "DATABASE CONFIGURATION";
		public static final String SYSTEM_CONFIG_SETTING_WINDOW = "SYSTEM CONFIGURATION";
		public static final String GATE_IN_SETTING_WINDOW = "GATE-IN CONFIGURATION";
		public static final String GATE_OUT_SETTING_WINDOW = "GATE-OUT CONFIGURATION";
		public static final String WB_TARE_SETTING_WINDOW = "WEIGH-BRIDGE TARE CONFIGURATION";
		public static final String WB_GROSS_SETTING_WINDOW = "WEIGH-BRIDGE GROSS CONFIGURATION";
		public static final String RFID_READER_SETTING_WINDOW = "READER CONFIGURATION";
		public static final String BARRIER_SETTING_WINDOW = "BOOM BARRIER CONFIGURATION";
		public static final String WEIGHBRIDGE_SETTING_WINDOW = "WEIGHBRIDGE CONFIGURATION";
		public static final String TAG_READ_SETTING_WINDOW = "TAG READ CONFIGURATION";
		public static final String TRANPORTER_WINDOW = "TRANSPORTER_DETAILS";
		public static final String READ_TAG_WINDOW = "TAG_INFORMATION";
		public static final String TPR_DETAILS_WINDOW = "TPR TRANSACTIONAL DATA";
		public static final String CANCEL_INVOICE = "CANCEL INVOICE";
		public static final String GATE_PASS = "Gate Pass";
		public static final String MPL_RFID_HANDLER_WINDOW = "RFID HANDLED";
	}

	public static class MenuItemId {

		public static final String WELCOME_SCREEN_WINDOW = "WELCOME_SCREEN_WINDOW";
		public static final String LOGIN = "LOGIN";
		public static final String GATE_IN = "MENU_GATE_IN";
		public static final String GATE_OUT = "MENU_GATE_OUT";
		public static final String TARE_WB = "MENU_TARE_WB";
		public static final String GROSS_WB = "MENU_GROSS_WB";
		public static final String MAIN_WINDOW = "MAIN_WINDOW";
		public static final String TRANPORTER_WINDOW = "TRANSPORTER_DETAILS";
		public static final String READ_TAG_WINDOW = "TAG_INFORMATION";
		public static final String TPR_DETAILS_WINDOW = "TPR_DETAILS";
		public static final String MPL_RFID_HANDLER_WINDOW = "RFID_HANDLER_DETAILS";

		public static final String SETTING_WINDOW = "SETTING_WINDOW";
		public static final String DATABASE_SETTING_WINDOW = "SETTING_DATABASE";
		public static final String SYSTEM_CONFIG_SETTING_WINDOW = "SETTING_SYSTEM_CONFIG";
		public static final String GATE_IN_SETTING_WINDOW = "SETTING_GATE_IN";
		public static final String GATE_OUT_SETTING_WINDOW = "SETTING_GATE_OUT";
		public static final String WB_TARE_SETTING_WINDOW = "SETTING_WB_TARE";
		public static final String WB_GROSS_SETTING_WINDOW = "SETTING_WB_GROSS";
		public static final String RFID_READER_SETTING_WINDOW = "SETTING_RFID_READER";
		public static final String BARRIER_SETTING_WINDOW = "SETTING_BARRIER";
		public static final String WEIGHBRIDGE_SETTING_WINDOW = "SETTING_WEIGHBRIDGE";
		public static final String TAG_READ_SETTING_WINDOW = "SETTING_READ_TAG";

		public static class MenuTitle {

			public static final String LOGIN = "Login";
			public static final String GATE_IN = "GATE_IN";
			public static final String GATE_OUT = "GATE_OUT";
			public static final String TARE_WB = "TARE_WB";
			public static final String GROSS_WB = "GROSS_WB";
			public static final String MAIN_WINDOW = "MAIN_WINDOW";
			public static final String WELCOME_SCREEN_WINDOW = "WELCOME_SCREEN_WINDOW";
			public static final String SETTING_WINDOW = "SETTING_WINDOW";
			public static final String DATABASE_SETTING_WINDOW = "SETTING_DATABASE";
			public static final String SYSTEM_CONFIG_SETTING_WINDOW = "SETTING_SYSTEM_CONFIG";
			public static final String GATE_IN_SETTING_WINDOW = "SETTING_GATE_IN";
			public static final String GATE_OUT_SETTING_WINDOW = "SETTING_GATE_OUT";
			public static final String WB_TARE_SETTING_WINDOW = "SETTING_WB_TARE";
			public static final String WB_GROSS_SETTING_WINDOW = "SETTING_WB_GROSS";
			public static final String RFID_READER_SETTING_WINDOW = "RFID_READER_SETTING_WINDOW";
			public static final String BARRIER_SETTING_WINDOW = "BARRIER_SETTING_WINDOW";
			public static final String WEIGHBRIDGE_SETTING_WINDOW = "WEIGHBRIDGE_SETTING_WINDOW";
			public static final String TRANPORTER_WINDOW = "TRANSPORTER_DETAILS_WINDOW";
			public static final String READ_TAG_WINDOW = "TAG_INFORMATION_WINDOW";
			public static final String TPR_DETAILS_WINDOW = "TPR_DETAILS_WINDOW";
			public static final String TAG_READ_SETTING_WINDOW = "TAG_READ_SETTING_WINDOW";
			public static final String MPL_RFID_HANDLER_WINDOW = "MPL_RFID_HANDLER_WINDOW";
		}

		public static class MENU_PRIV {

			public static final int LOGIN = Misc.getUndefInt();
			public static final int GATE_IN = 80101;
			public static final int GATE_OUT = 80102;
			public static final int TARE_WB = 80103;
			public static final int GROSS_WB = 80104;
			public static final int SETTING = 80105;
			public static final int TRANPORTER_WINDOW = 80106;
			public static final int READ_TAG_WINDOW = 80107;
			public static final int TPR_DETAILS_WINDOW = 80108;
			public static final int MPL_RFID_HANDLER_WINDOW = 80109;
			public static final int MAIN_WINDOW = Misc.getUndefInt();
			public static final int WELCOME_SCREEN = Misc.getUndefInt();
			public static final int SETTING_SCREEN = Misc.getUndefInt();
			public static final int DATABASE_SETTING_WINDOW = Misc.getUndefInt();
			public static final int SYSTEM_CONFIG_SETTING_WINDOW = Misc.getUndefInt();
			public static final int GATE_IN_SETTING_WINDOW = Misc.getUndefInt();
			public static final int GATE_OUT_SETTING_WINDOW = Misc.getUndefInt();
			public static final int WB_TARE_SETTING_WINDOW = Misc.getUndefInt();
			public static final int WB_GROSS_SETTING_WINDOW = Misc.getUndefInt();
			public static final int RFID_READER_SETTING_WINDOW = Misc.getUndefInt();
			public static final int BARRIER_SETTING_WINDOW = Misc.getUndefInt();
			public static final int WEIGHBRIDGE_SETTING_WINDOW = Misc.getUndefInt();
			public static final int TAG_READ_SETTING_WINDOW = Misc.getUndefInt();

		}

		
	
	}

}
