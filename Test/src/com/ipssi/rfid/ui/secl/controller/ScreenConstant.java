package com.ipssi.rfid.ui.secl.controller;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;

/**
 * Created by ipssi11 on 12-Oct-16.
 */
public class ScreenConstant {
	public static final String VERSION = "1.0.1";
	public static final String DATE_FORMAT_DDMMYYYY_HHMM = "dd/MM/yyyy HH:mm";
	public static final String DATE_FORMAT_DDMMYYYY_HHMMSS = DATE_FORMAT_DDMMYYYY_HHMM+":ss";
	public static final String DATE_FORMAT_DDMMYY = "dd-MM-yyyy";
	public static final String TIME_FORMAT_HHMMSS = "HH:mm:ss";
	public static final String BASE = "/com/ipssi/rfid/ui/secl/view/";
    
	public static class ScreenLinks {
		
        public static final String APPLICATION_CSS = BASE + "application.css";
        public static final String MAIN_PERIPHERAL = BASE + "peripheral.fxml";
        public static final String MAIN_WINDOW = BASE + "mainScreen.fxml";
        public static final String SPLASH = BASE + "splash.fxml";
        public static final String LOGIN = BASE + "login.fxml";
        public static final String REGISTRATION = BASE + "registration.fxml";
        public static final String REGISTRATION_AND_GATE = BASE + "registration_and_gate.fxml";
        public static final String LOAD_GATE = REGISTRATION_AND_GATE;//BASE + "loadGate.fxml";
        public static final String UNLOAD_GATE = REGISTRATION_AND_GATE;//BASE + "unloadGate.fxml";
        public static final String LOAD_INTERNAL_GROSS_TARE = BASE + "loadInternalGrossTare.fxml";
        public static final String LOAD_INTERNAL_GROSS = BASE + "loadInternalGross.fxml";
        public static final String LOAD_INTERNAL_TARE = BASE + "loadInternalTare.fxml";
        public static final String LOAD_ROAD_GROSS_TARE = BASE + "loadRoadGrossTare.fxml";
        public static final String LOAD_ROAD_GROSS = BASE + "loadRoadGross.fxml";
        public static final String LOAD_ROAD_TARE = BASE + "loadRoadTare.fxml";
        public static final String LOAD_WASHERY_GROSS_TARE = BASE + "loadWasheryGrossTare.fxml";
        public static final String UNLOAD_INTERNAL_GROSS_TARE = BASE + "unloadInternalGrossTare.fxml";
        public static final String UNLOAD_INTERNAL_GROSS = BASE + "unloadInternalGross.fxml";
        public static final String UNLOAD_INTERNAL_TARE = BASE + "unloadInternalTare.fxml";
        public static final String UNLOAD_ROAD_GROSS = BASE + "unloadRoadGross.fxml";
        public static final String UNLOAD_ROAD_TARE = BASE + "unloadRoadTare.fxml";
        public static final String PRINT = BASE + "print.fxml";//"printDocOne.fxml";
        public static final String SETTINGS = BASE + "Settings.fxml";
    }
    public static class ScreenTitle{
    	public static final String LOGIN = "Login";
        public static final String HOME = "Home";
        public static final String REGISTRATION = "Registration";
        public static final String REGISTRATION_AND_GATE = "Registration & Gate";
        public static final String LOAD_GATE = "Load Gate In/Out";
        public static final String UNLOAD_GATE = "Unload Gate In/Out";
        public static final String LOAD_INTERNAL_GROSS_TARE = "Load Internal Weighment";
        public static final String LOAD_INTERNAL_GROSS = "Load Internal Gross Weighment";
        public static final String LOAD_INTERNAL_TARE = "Load Internal Tare Weighment";
        public static final String LOAD_ROAD_GROSS_TARE = "Road Sale Weighment";
        public static final String LOAD_ROAD_GROSS = "Road Gross Weighment";
        public static final String LOAD_ROAD_TARE = "Road Tare Weighment";
        public static final String LOAD_WASHERY_GROSS_TARE = "Washery Weighment";
        public static final String UNLOAD_INTERNAL_GROSS_TARE = "Siding Weighment";
        public static final String UNLOAD_INTERNAL_GROSS = "Siding Gross Weighment";
        public static final String UNLOAD_INTERNAL_TARE = "Siding Tare Weighment";
        public static final String UNLOAD_ROAD_GROSS = "Road Gross Weighment";
        public static final String UNLOAD_ROAD_TARE = "Road Tare Weighment";
        public static final String PRINT = "Print";
        public static final String SETTINGS = "Settings";
    }
    public static class MenuItemId{
        public static final String LOGIN = "LOGIN";
        public static final String HOME = "HOME";
        public static final String REGISTRATION = "MENU_REGISTRATION";
        public static final String REGISTRATION_AND_GATE = "MENU_REGISTRATION_AND_GATE";
        public static final String LOAD_GATE = "MENU_LOAD_GATE";
        public static final String UNLOAD_GATE = "MENU_UNLOAD_GATE";
        public static final String LOAD_INTERNAL_GROSS_TARE = "MENU_LOAD_INTERNAL_GROSS_TARE";
        public static final String LOAD_INTERNAL_GROSS = "MENU_LOAD_INTERNAL_GROSS";
        public static final String LOAD_INTERNAL_TARE = "MENU_LOAD_INTERNAL_TARE";
        public static final String LOAD_ROAD_GROSS_TARE = "MENU_LOAD_ROAD_GROSS_TARE";
        public static final String LOAD_ROAD_GROSS = "MENU_LOAD_ROAD_GROSS";
        public static final String LOAD_ROAD_TARE = "MENU_LOAD_ROAD_TARE";
        public static final String LOAD_WASHERY_GROSS_TARE = "MENU_LOAD_WASHERY_GROSS_TARE";
        public static final String UNLOAD_INTERNAL_GROSS_TARE = "MENU_UNLOAD_INTERNAL_GROSS_TARE";
        public static final String UNLOAD_INTERNAL_GROSS = "MENU_UNLOAD_INTERNAL_GROSS";
        public static final String UNLOAD_INTERNAL_TARE = "MENU_UNLOAD_INTERNAL_TARE";
        public static final String UNLOAD_ROAD_GROSS = "MENU_UNLOAD_ROAD_GROSS";
        public static final String UNLOAD_ROAD_TARE = "MENU_UNLOAD_ROAD_TARE";
        public static final String PRINT = "MENU_PRINT";
        public static final String SETTINGS = "MENU_SETTINGS";
        
        public static class MenuTitle{
        	public static final String LOGIN = "Login";
            public static final String HOME = "Home";
            public static final String REGISTRATION = "Registration";
            public static final String REGISTRATION_AND_GATE = "Reg. & Gate";
            public static final String LOAD_GATE = "Load Gate In/Out";
            public static final String UNLOAD_GATE = "Unload Gate In/Out";
            public static final String LOAD_INTERNAL_GROSS_TARE = "Local";
            public static final String LOAD_INTERNAL_GROSS = "Load Int Gross";
            public static final String LOAD_INTERNAL_TARE = "Load Int Tare";
            public static final String LOAD_ROAD_GROSS_TARE = "Road Sale";
            public static final String LOAD_ROAD_GROSS = "Load Road Gross";
            public static final String LOAD_ROAD_TARE = "Load Road Tare";
            public static final String LOAD_WASHERY_GROSS_TARE = "Washery";
            public static final String UNLOAD_INTERNAL_GROSS_TARE = "Siding";
            public static final String UNLOAD_INTERNAL_GROSS = "Unload Int Gross";
            public static final String UNLOAD_INTERNAL_TARE = "Unload Int Tare";
            public static final String UNLOAD_ROAD_GROSS = "Unload Road Gross";
            public static final String UNLOAD_ROAD_TARE = "Unload Road Tare";
            public static final String PRINT = "Print";
            public static final String SETTINGS = "Settings";
        }
        public static class PRIV{
            public static final int LOGIN = Misc.getUndefInt();
            public static final int HOME = Misc.getUndefInt();
            public static final int REGISTRATION = 70051;
            public static final int REGISTRATION_AND_GATE = 70050;
            public static final int LOAD_GATE = 70052;
            public static final int UNLOAD_GATE = 70057;
            public static final int LOAD_INTERNAL_GROSS_TARE = 70054;
            public static final int LOAD_INTERNAL_GROSS = 70054;
            public static final int LOAD_INTERNAL_TARE = 70053;
            public static final int LOAD_ROAD_GROSS_TARE = 70056;
            public static final int LOAD_ROAD_GROSS = 70056;
            public static final int LOAD_ROAD_TARE = 70055;
            public static final int LOAD_WASHERY_GROSS_TARE = 70085;
            public static final int UNLOAD_INTERNAL_GROSS_TARE = 70058;
            public static final int UNLOAD_INTERNAL_GROSS = 70058;
            public static final int UNLOAD_INTERNAL_TARE = 70059;
            public static final int UNLOAD_ROAD_GROSS = Misc.getUndefInt(); 
            public static final int UNLOAD_ROAD_TARE = Misc.getUndefInt(); 
            public static final int PRINT = Misc.getUndefInt();
            public static final int SETTINGS = Misc.getUndefInt();
        }
    }
    
}
