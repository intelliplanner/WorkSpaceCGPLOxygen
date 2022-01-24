package com.ipssi.rfid.ui.secl.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class SplashController implements Initializable{

    @FXML
    private Label loadingMessage;
    public void showMessages(final String message){
    	Platform.runLater(()->{
    		loadingMessage.setText(message);
    	});
    }
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
    
}
