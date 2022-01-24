package com.ipssi.rfid.ui.controller;

import java.awt.Color;
import java.io.IOException;
import java.net.ServerSocket;

import com.ipssi.rfid.constant.ScreenConstant;
import com.ipssi.rfid.constant.UIConstant.COLUR;
import com.ipssi.rfid.processor.TokenManager;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class StartApplication extends Application {

	public JFXTextField testBoxUserName;
	public JFXPasswordField testBoxPassword;
	public Label labelError;
	private JFXButton buttonLogin;
//	Task<ObservableList<String>> initTask = null;
	Scene scene;
	Stage mainStag;
	
	
	
	@Override
	public void start(final Stage initStage) throws Exception {
		try {
//			  Parent root = FXMLLoader.load(getClass().getResource(ScreenConstant.ScreenLinks.SPLASH));
//              Scene scene = new Scene(root);
//              initStage.setScene(scene);
//              initStage.show();
//              Thread.sleep(10000);
//              ap.getScene().getWindow().hide();
			ServerSocket ss = null;
			try {
				ss = new ServerSocket(ScreenConstant.mutexPortNo);
				final ServerSocket ss1 = ss;
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							ss1.accept();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (ss != null) {
				// initTask = new Task<ObservableList<String>>() {
				// @Override
				// protected ObservableList<String> call() throws Exception {
				// updateMessage("Starting System . . .");
				// return null;
				// }
				// };
				
	              showMainStage(initStage);
				  
				// new Thread(initTask).start();
			} else {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Multiple Instances runnig");
				alert.setContentText("please close running instance");
				alert.showAndWait();
			}
		} catch (IOException e) {
				e.printStackTrace();
			}
		
	}


	private void showMainStage(Stage initStage) throws IOException {

		Parent root = FXMLLoader.load(getClass().getResource(ScreenConstant.ScreenLinks.MAIN_WINDOW));
		// initStage.setOnCloseRequest(e -> closePariparrels());
		initStage.setScene(new Scene(root));
		Image icon = new Image(getClass().getResourceAsStream("loginScreenLogo.png"));
		initStage.getIcons().add(icon);
		// primaryStage.getIcons().add(new Image("file:loginScreenLogo.png"));
		initStage.setTitle("CGPL Desktop Manager " + ScreenConstant.VERSION);
		initStage.show();
	}

	public static void main(String args[]) {
		launch(args);
	}

	private void closePariparrels() {
		if (MainController._selectedMenuId != ScreenConstant.MenuItemId.LOGIN
				&& MainController.currentViewController != null) {
			MainController.currentViewController.stopRfid();
		}

//		if (initTask != null) {
//			initTask.cancel();
//		}
		System.exit(0);
	}

	@Override
	public void stop() {
		closePariparrels();
	}

	

	
	

}
