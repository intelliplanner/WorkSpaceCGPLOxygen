package com.ipssi.rfid.ui.controller;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.MenuItemInfo;
import java.util.logging.Logger;
import com.ipssi.rfid.constant.ScreenConstant;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import java.util.HashMap;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class StartApplication_old extends Application {

	public JFXTextField testBoxUserName;
	public JFXPasswordField testBoxPassword;
	public Label labelError;
	private JFXButton buttonLogin;
	private static final Logger log = Logger.getLogger(StartApplication_old.class.getName());
	private static HashMap<String, MenuItemInfo> menuScreenMap = new HashMap<>();

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource(ScreenConstant.ScreenLinks.LOGIN_WINDOW));
		primaryStage.setScene(new Scene(root));
		// primaryStage.sizeToScene();
		// initMenuCache();
		primaryStage.show();
	}

	public static void main(String args[]) {
		launch(args);
	}

}
