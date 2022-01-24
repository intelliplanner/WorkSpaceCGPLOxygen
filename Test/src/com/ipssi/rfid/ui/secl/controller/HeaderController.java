package com.ipssi.rfid.ui.secl.controller;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.PeripheralConnectionStatus;
import com.ipssi.rfid.readers.PeripheralConnectionStatus.PeripheralStatus;
import com.ipssi.rfid.readers.PeripheralConnectionStatus.PeripheralType;
import com.ipssi.rfid.ui.secl.controller.PropertyManager.PropertyType;
import com.jfoenix.controls.JFXButton;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class HeaderController {
	@FXML
    private HBox panelConnectionStatus;

    @FXML
    private Label textRFIDOneConnected;

    @FXML
    private Label textRFIDTwoConnected;

    @FXML
    private Label textRFIDThreeConnected;

    @FXML
    private Label textBarrierConnected;

    @FXML
    private Label textWeighBridgeConnected;

    @FXML
    private Label textWeighBridgeCentricConnected;

    @FXML
    private Label textServerConnected;

    @FXML
    private Label labelSystemId;

    @FXML
    private Label labelUsername;

    @FXML
    private JFXButton buttonLogout;
    @FXML
    private Label labelApplicationVersion;

    

    @FXML
    void logout(ActionEvent event) {
    	parent.logout();
    }
    public void setSystemId(String systemId){
    	labelSystemId.setText(systemId);
    }
    public void setUsername(String username){
    	labelUsername.setText(username);
    }
    
    public void setLogoutVisible(boolean visible){
    	buttonLogout.setVisible(visible);
    }
    public void setUsernameVisible(boolean visible){
    	labelUsername.setVisible(visible);
    }
    
    
    private MainController parent;
    public void init(MainController parent){
    	this.parent = parent;
    	labelApplicationVersion.setText(ScreenConstant.VERSION);
    }
    private String styleConnected = "-fx-background-color:green;";
	private String styleDisconnected = "-fx-background-color:red";
	public void handlePeripheralConnection(final int _id,final PeripheralType _type, final PeripheralStatus _status){
		int id =_id;
		PeripheralType type = _type;
		PeripheralStatus status = _status;
		String style = status == PeripheralStatus.CONNECTED ? styleConnected : styleDisconnected;
		if(!PeripheralConnectionStatus.updatePeripheralStatus(_id, _type, _status, type == PeripheralType.DATABASE ? parent.getMainWindow().getServerConnThresholdSec() * 1000 : Misc.getUndefInt()))
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
			if(status == PeripheralStatus.DISCONNECTED){
				parent.showAlert("Digitizer Connection", "Disconnected From Digitizer. please check connection");
			}
		}else if(PeripheralType.WCS == type){
			textWeighBridgeCentricConnected.setStyle(style);
		}else if(PeripheralType.DATABASE == type){
			String message = null;
			String instance = null;
			if(parent.getMainWindow().isRemoteConnected()){
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
				int ans = parent.prompt("Connection switch", message, new String[]{"No","Yes"});
				if(ans == 1){
					PropertyManager.setProperty(PropertyType.Database,"SERVER", instance);
					parent.getMainWindow().restartApplication();
				}
			}
			textServerConnected.setStyle(style);
		}
	}

}
