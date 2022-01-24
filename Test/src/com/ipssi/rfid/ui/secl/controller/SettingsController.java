package com.ipssi.rfid.ui.controller;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.secl.controller.PropertyManager.PropertyType;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;

public class SettingsController implements Initializable,ControllerI{
	@FXML
    private JFXCheckBox checkBoxReaderOnePresent;
    @FXML
    private JFXCheckBox checkBoxReaderTwoPresent;
    @FXML
    private JFXCheckBox checkBoxReaderThreePresent;
    @FXML
    private JFXCheckBox checkBoxWeighBridgePresent;
    @FXML
    private JFXCheckBox checkBoxBarrierPresent;
    @FXML
    private JFXCheckBox checkBoxCentricPresent;
    @FXML
    private JFXTextField textBoxPrinterAddr;
    @FXML
    private JFXCheckBox checkBoxLogging;
    @FXML
    private JFXCheckBox checkBoxPrintOnSave;
    @FXML
    private Label textSystemCode;
    @FXML
    private Label textSystemUID;
    @FXML
    private Label textBoxdigitizerZero;
    @FXML
    private JFXTextField textBoxDatabaseLocalHost;
    @FXML
    private JFXTextField textBoxDatabaseLocalDB;

    @FXML
    private JFXTextField textBoxDatabaseLocalUser;

    @FXML
    private JFXTextField textBoxDatabaseRemoteHost;

    @FXML
    private JFXTextField textBoxDatabaseRemoteDB;

    @FXML
    private JFXTextField textBoxDatabaseRemoteUser;

    @FXML
    private JFXTextField textBoxDatabaseLocalPort;

    @FXML
    private JFXTextField textBoxDatabaseLocalMaxConn;

    @FXML
    private JFXTextField textBoxDatabaseRemotePort;

    @FXML
    private JFXTextField textBoxDatabaseRemoteMaxConn;

    @FXML
    private JFXPasswordField textBoxDatabaseLocalPass;

    @FXML
    private JFXPasswordField textBoxDatabaseRemotePass;

    @FXML
    private Label textDatabaseMode;

    @FXML
    private JFXCheckBox checkBoxReaderOneTCPIP;

    @FXML
    private JFXCheckBox checkBoxReaderTwoTCPIP;

    @FXML
    private JFXCheckBox checkBoxReaderThreeTCPIP;

    @FXML
    private JFXTextField textBoxReaderOneHost;

    @FXML
    private JFXTextField textBoxReaderOneComm;

    @FXML
    private JFXTextField textBoxReaderOnePort;

    @FXML
    private JFXTextField textBoxReaderTwoHost;

    @FXML
    private JFXTextField textBoxReaderTwoComm;

    @FXML
    private JFXTextField textBoxReaderTwoPort;

    @FXML
    private JFXTextField textBoxReaderThreeHost;

    @FXML
    private JFXTextField textBoxReaderThreePort;

    @FXML
    private JFXTextField textBoxReaderThreeComm;

    @FXML
    private JFXTextField textBoxWeighBridgeComm;

    @FXML
    private JFXTextField textBoxWeighBridgeBaudRate;

    @FXML
    private JFXTextField textBoxWeighBridgeDataBits;

    @FXML
    private JFXTextField textBoxWeighBridgeParity;

    @FXML
    private JFXTextField textBoxWeighBridgeStopBits;
    
    @FXML
    private JFXTextField textBoxWeighBridgeDisconnectionMillis;

    @FXML
    private JFXCheckBox checkBoxSimulateWB;

    @FXML
    private JFXTextField textBoxBarrierComm;

    @FXML
    private JFXTextField textBoxBarrierBaudRate;

    @FXML
    private JFXTextField textBoxBarrierDataBits;

    @FXML
    private JFXTextField textBoxBarrierParity;

    @FXML
    private JFXTextField textBoxBarrierStopBits;

    @FXML
    private JFXTextField textBoxBarrierEntryCommand;

    @FXML
    private JFXTextField textBoxBarrierExitCommand;

    @FXML
    private JFXTextField textBoxCentricComm;

    @FXML
    private JFXTextField textBoxCentricBaudRate;

    @FXML
    private JFXTextField textBoxCentricDataBits;

    @FXML
    private JFXTextField textBoxCentricParity;

    @FXML
    private JFXTextField textBoxCentricStopBits;

    @FXML
    private JFXTextField textBoxCentricCommand;

    @FXML
    void loadSystem(ActionEvent event) {
    	try{
    		textSystemUID.setText(parent.getMainWindow().getWorkStationDetails().getUid());
    		textSystemCode.setText(parent.getMainWindow().getWorkStationDetails().getCode());
    		Properties prop = PropertyManager.getProperty(PropertyType.RfidReader);
    		if(prop != null){
    			checkBoxReaderOnePresent.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_ONE_PRESENT")));
    			checkBoxReaderTwoPresent.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_TWO_PRESENT")));    			
    			checkBoxReaderThreePresent.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_DESKTOP_PRESENT")));
    		}
    		prop = PropertyManager.getProperty(PropertyType.WeighBridge);
    		if(prop != null)
    			checkBoxWeighBridgePresent.setSelected("1".equalsIgnoreCase(prop.getProperty("PRESENT")));
    		prop = PropertyManager.getProperty(PropertyType.Barrier);
    		if(prop != null)
    			checkBoxBarrierPresent.setSelected("1".equalsIgnoreCase(prop.getProperty("PRESENT")));    			
    		prop = PropertyManager.getProperty(PropertyType.Centric);
    		if(prop != null)
    			checkBoxCentricPresent.setSelected("1".equalsIgnoreCase(prop.getProperty("PRESENT")));
    		prop = PropertyManager.getProperty(PropertyType.System);
    		if(prop != null){
    			textBoxPrinterAddr.setText(prop.getProperty("PRINTER_ADDR"));
    			checkBoxLogging.setSelected(!"1".equalsIgnoreCase(prop.getProperty("DEBUG")));
    			checkBoxPrintOnSave.setSelected("1".equalsIgnoreCase(prop.getProperty("PRINT_ON_SAVE")));
    			textBoxdigitizerZero.setText(prop.getProperty("DIGITIZER_ZERO"));
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    @FXML
    void loadDataBase(ActionEvent event) {
    	try{
    		Properties prop = PropertyManager.getProperty(PropertyType.Database);
    		if(prop != null){
    			textDatabaseMode.setText(Misc.getParamAsString(prop.getProperty("SERVER"), ""));
    			//desktop
    			textBoxDatabaseLocalHost.setText(Misc.getParamAsString(prop.getProperty("desktop.DBConn.host"), ""));
    			textBoxDatabaseLocalPort.setText(Misc.getParamAsString(prop.getProperty("desktop.DBConn.port"), ""));
    			textBoxDatabaseLocalDB.setText(Misc.getParamAsString(prop.getProperty("desktop.DBConn.Database"), ""));
    			textBoxDatabaseLocalUser.setText(Misc.getParamAsString(prop.getProperty("desktop.DBConn.userName"), ""));
    			textBoxDatabaseLocalPass.setText(Misc.getParamAsString(prop.getProperty("desktop.DBConn.password"), ""));
    			textBoxDatabaseLocalMaxConn.setText(Misc.getParamAsString(prop.getProperty("desktop.DBConn.maxConnection"), ""));
    			//remote
    			textBoxDatabaseRemoteHost.setText(Misc.getParamAsString(prop.getProperty("remote.DBConn.host"), ""));
    			textBoxDatabaseRemotePort.setText(Misc.getParamAsString(prop.getProperty("remote.DBConn.port"), ""));
    			textBoxDatabaseRemoteDB.setText(Misc.getParamAsString(prop.getProperty("remote.DBConn.Database"), ""));
    			textBoxDatabaseRemoteUser.setText(Misc.getParamAsString(prop.getProperty("remote.DBConn.userName"), ""));
    			textBoxDatabaseRemotePass.setText(Misc.getParamAsString(prop.getProperty("remote.DBConn.password"), ""));
    			textBoxDatabaseRemoteMaxConn.setText(Misc.getParamAsString(prop.getProperty("remote.DBConn.maxConnection"), ""));
    			
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    @FXML
    void loadRFID(ActionEvent event) {
    	try{
    		Properties prop = PropertyManager.getProperty(PropertyType.RfidReader);
    		if(prop != null){
    			
    			checkBoxReaderOneTCPIP.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_ONE_CONN_TYPE")));
    			textBoxReaderOneComm.setText(Misc.getParamAsString(prop.getProperty("READER_ONE_COM"),""));
    			textBoxReaderOneHost.setText(Misc.getParamAsString(prop.getProperty("READER_ONE_TCP_IP"),""));
    			textBoxReaderOnePort.setText(Misc.getParamAsString(prop.getProperty("READER_ONE_TCP_PORT"),""));
    			
    			checkBoxReaderTwoTCPIP.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_TWO_CONN_TYPE")));
    			textBoxReaderTwoComm.setText(Misc.getParamAsString(prop.getProperty("READER_TWO_COM"),""));
    			textBoxReaderTwoHost.setText(Misc.getParamAsString(prop.getProperty("READER_TWO_TCP_IP"),""));
    			textBoxReaderTwoPort.setText(Misc.getParamAsString(prop.getProperty("READER_TWO_TCP_PORT"),""));
    			
    			checkBoxReaderThreeTCPIP.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_DESKTOP_CONN_TYPE")));
    			textBoxReaderThreeComm.setText(Misc.getParamAsString(prop.getProperty("READER_DESKTOP_COM"),""));
    			textBoxReaderThreeHost.setText(Misc.getParamAsString(prop.getProperty("READER_DESKTOP_TCP_IP"),""));
    			textBoxReaderThreePort.setText(Misc.getParamAsString(prop.getProperty("READER_DESKTOP_TCP_PORT"),""));
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    @FXML
    void loadWeighBridge(ActionEvent event) {
    	try{
    		Properties prop = PropertyManager.getProperty(PropertyType.WeighBridge);
    		if(prop != null){
    			textBoxWeighBridgeComm.setText(Misc.getParamAsString(prop.getProperty("COM_PORT"),""));
    			textBoxWeighBridgeBaudRate.setText(Misc.getParamAsString(prop.getProperty("COM_BAUDRATE"),""));
    			textBoxWeighBridgeParity.setText(Misc.getParamAsString(prop.getProperty("COM_PARITY"),""));
    			textBoxWeighBridgeDataBits.setText(Misc.getParamAsString(prop.getProperty("COM_DATABITS"),""));
    			textBoxWeighBridgeStopBits.setText(Misc.getParamAsString(prop.getProperty("COM_STOPBITS"),""));
    			textBoxWeighBridgeDisconnectionMillis.setText(Misc.getParamAsString(prop.getProperty("DISCONNETION_MILLIS"),"1000"));
    			boolean simulate = "1".equalsIgnoreCase(prop.getProperty("SIMULATE_WB"));
    			checkBoxSimulateWB.setSelected(simulate);
    			TokenManager.isSimulateWB = simulate;
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    @FXML
    void loadBarrier(ActionEvent event) {
    	try{
    		Properties prop = PropertyManager.getProperty(PropertyType.Barrier);
    		if(prop != null){
    			textBoxBarrierComm.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_PORT"),""));
    			textBoxBarrierBaudRate.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_BAUDRATE"),""));
    			textBoxBarrierParity.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_PARITY"),""));
    			textBoxBarrierDataBits.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_DATABITS"),""));
    			textBoxBarrierStopBits.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_STOPBITS"),""));
    			textBoxBarrierEntryCommand.setText(Misc.getParamAsString(prop.getProperty("BARRIER_ENTRY_COMMAND"),""));
    			textBoxBarrierExitCommand.setText(Misc.getParamAsString(prop.getProperty("BARRIER_EXIT_COMMAND"),""));
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    @FXML
    void loadCentricSystem(ActionEvent event) {
    	try{
    		Properties prop = PropertyManager.getProperty(PropertyType.Centric);
    		if(prop != null){
    			textBoxCentricComm.setText(Misc.getParamAsString(prop.getProperty("COM_PORT"),""));
    			textBoxCentricBaudRate.setText(Misc.getParamAsString(prop.getProperty("COM_BAUDRATE"),""));
    			textBoxCentricParity.setText(Misc.getParamAsString(prop.getProperty("COM_PARITY"),""));
    			textBoxCentricDataBits.setText(Misc.getParamAsString(prop.getProperty("COM_DATABITS"),""));
    			textBoxCentricStopBits.setText(Misc.getParamAsString(prop.getProperty("COM_STOPBITS"),""));
    			textBoxCentricCommand.setText(Misc.getParamAsString(prop.getProperty("COMMAND"),""));
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    @FXML
    void saveSystem(ActionEvent event) {
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_ONE_PRESENT", checkBoxReaderOnePresent.isSelected() ? "1" : "0");
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_TWO_PRESENT", checkBoxReaderTwoPresent.isSelected() ? "1" : "0");
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_DESKTOP_PRESENT", checkBoxReaderThreePresent.isSelected() ? "1" : "0");
    	PropertyManager.setProperty(PropertyType.WeighBridge, "PRESENT", checkBoxWeighBridgePresent.isSelected() ? "1" : "0");
    	PropertyManager.setProperty(PropertyType.Barrier, "PRESENT", checkBoxBarrierPresent.isSelected() ? "1" : "0");
    	PropertyManager.setProperty(PropertyType.Centric, "PRESENT", checkBoxCentricPresent.isSelected() ? "1" : "0");
    	PropertyManager.setProperty(PropertyType.System,"PRINTER_ADDR",textBoxPrinterAddr.getText());
    	PropertyManager.setProperty(PropertyType.System, "DEBUG", checkBoxLogging.isSelected() ? "0" : "1");
    	PropertyManager.setProperty(PropertyType.System, "PRINT_ON_SAVE", checkBoxPrintOnSave.isSelected() ? "1" : "0");
    	PropertyManager.setProperty(PropertyType.System,"DIGITIZER_ZERO",textBoxdigitizerZero.getText());
    	loadSystem(null);
    }    
    @FXML
    void saveDataBase(ActionEvent event) {
    	PropertyManager.setProperty(PropertyType.Database, "desktop.DBConn.host",textBoxDatabaseLocalHost.getText());
    	PropertyManager.setProperty(PropertyType.Database, "desktop.DBConn.port",textBoxDatabaseLocalPort.getText());
    	PropertyManager.setProperty(PropertyType.Database, "desktop.DBConn.Database",textBoxDatabaseLocalDB.getText());
    	PropertyManager.setProperty(PropertyType.Database, "desktop.DBConn.userName",textBoxDatabaseLocalUser.getText());
    	PropertyManager.setProperty(PropertyType.Database, "desktop.DBConn.password",textBoxDatabaseLocalPass.getText());
    	PropertyManager.setProperty(PropertyType.Database, "desktop.DBConn.maxConnection",textBoxDatabaseLocalMaxConn.getText());
		//remote
    	PropertyManager.setProperty(PropertyType.Database, "remote.DBConn.host",textBoxDatabaseRemoteHost.getText());
    	PropertyManager.setProperty(PropertyType.Database, "remote.DBConn.port",textBoxDatabaseRemotePort.getText());
    	PropertyManager.setProperty(PropertyType.Database, "remote.DBConn.Database",textBoxDatabaseRemoteDB.getText());
    	PropertyManager.setProperty(PropertyType.Database, "remote.DBConn.userName",textBoxDatabaseRemoteUser.getText());
    	PropertyManager.setProperty(PropertyType.Database, "remote.DBConn.password",textBoxDatabaseRemotePass.getText());
    	PropertyManager.setProperty(PropertyType.Database, "remote.DBConn.maxConnection",textBoxDatabaseRemoteMaxConn.getText());
    	loadDataBase(null);
    }

    @FXML
    void saveRFID(ActionEvent event) {
    	
    	
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_ONE_CONN_TYPE", checkBoxReaderOneTCPIP.isSelected() ? "1" : "0");
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_ONE_COM", textBoxReaderOneComm.getText());
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_ONE_TCP_IP", textBoxReaderOneHost.getText());
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_ONE_TCP_PORT", textBoxReaderOnePort.getText());
		
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_TWO_CONN_TYPE", checkBoxReaderTwoTCPIP.isSelected() ? "1" : "0");
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_TWO_COM", textBoxReaderTwoComm.getText());
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_TWO_TCP_IP", textBoxReaderTwoHost.getText());
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_TWO_TCP_PORT", textBoxReaderTwoPort.getText());
		
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_DESKTOP_CONN_TYPE", checkBoxReaderThreeTCPIP.isSelected() ? "1" : "0");
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_DESKTOP_COM", textBoxReaderThreeComm.getText());
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_DESKTOP_TCP_IP", textBoxReaderThreeHost.getText());
    	PropertyManager.setProperty(PropertyType.RfidReader, "READER_DESKTOP_TCP_PORT", textBoxReaderThreePort.getText());
    	loadRFID(null);
    }

    @FXML
    void saveWeighBridge(ActionEvent event) {
		PropertyManager.setProperty(PropertyType.WeighBridge, "COM_PORT", textBoxWeighBridgeComm.getText());
		PropertyManager.setProperty(PropertyType.WeighBridge, "COM_BAUDRATE", textBoxWeighBridgeBaudRate.getText());
		PropertyManager.setProperty(PropertyType.WeighBridge, "COM_PARITY", textBoxWeighBridgeParity.getText());
		PropertyManager.setProperty(PropertyType.WeighBridge, "COM_DATABITS", textBoxWeighBridgeDataBits.getText());
		PropertyManager.setProperty(PropertyType.WeighBridge, "COM_STOPBITS", textBoxWeighBridgeStopBits.getText());
		PropertyManager.setProperty(PropertyType.WeighBridge, "SIMULATE_WB", checkBoxSimulateWB.isSelected() ? "1" : "0");
		PropertyManager.setProperty(PropertyType.WeighBridge, "DISCONNETION_MILLIS", textBoxWeighBridgeDisconnectionMillis.getText());
		loadWeighBridge(null);
    }
    @FXML
    void saveBarrier(ActionEvent event) {
    	PropertyManager.setProperty(PropertyType.Barrier, "BARRIER_COM_PORT", textBoxBarrierComm.getText());
    	PropertyManager.setProperty(PropertyType.Barrier, "BARRIER_COM_BAUDRATE", textBoxBarrierBaudRate.getText());
    	PropertyManager.setProperty(PropertyType.Barrier, "BARRIER_COM_PARITY", textBoxBarrierParity.getText());
    	PropertyManager.setProperty(PropertyType.Barrier, "BARRIER_COM_DATABITS", textBoxBarrierDataBits.getText());
    	PropertyManager.setProperty(PropertyType.Barrier, "BARRIER_COM_STOPBITS", textBoxBarrierStopBits.getText());
    	PropertyManager.setProperty(PropertyType.Barrier, "BARRIER_ENTRY_COMMAND", textBoxBarrierEntryCommand.getText());
    	PropertyManager.setProperty(PropertyType.Barrier, "BARRIER_EXIT_COMMAND", textBoxBarrierExitCommand.getText());
    }
    @FXML
    void saveCentricSystem(ActionEvent event) {
		PropertyManager.setProperty(PropertyType.Centric, "COM_PORT", textBoxCentricComm.getText());
		PropertyManager.setProperty(PropertyType.Centric, "COM_BAUDRATE", textBoxCentricBaudRate.getText());
		PropertyManager.setProperty(PropertyType.Centric, "COM_PARITY", textBoxCentricParity.getText());
		PropertyManager.setProperty(PropertyType.Centric, "COM_DATABITS", textBoxCentricDataBits.getText());
		PropertyManager.setProperty(PropertyType.Centric, "COM_STOPBITS", textBoxCentricStopBits.getText());
		PropertyManager.setProperty(PropertyType.Centric, "COMMAND", textBoxCentricCommand.getText());
		loadCentricSystem(null);
    }
    MainController parent;
    Parent rootView;
    MenuItemInfo menuItemInfo;
	
	public void init(MainController parent, Parent rootView, MenuItemInfo menuItemInfo) {
		// TODO Auto-generated method stub
		this.parent = parent;
		this.rootView = rootView;
		this.menuItemInfo = menuItemInfo;
		loadSystem(null);
		loadDataBase(null);
		loadRFID(null);
		loadWeighBridge(null);
		loadBarrier(null);
		loadCentricSystem(null);
	}



}
