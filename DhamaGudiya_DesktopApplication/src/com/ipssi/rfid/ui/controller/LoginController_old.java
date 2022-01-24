package com.ipssi.rfid.ui.controller;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.User;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.ui.dao.UserLogin;
import com.ipssi.rfid.constant.ScreenConstant;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LoginController_old implements Initializable {

	@FXML
	private JFXButton CONTROL_LOGIN;
	@FXML
	private JFXTextField testBoxUserName;
	@FXML
	private JFXPasswordField testBoxPassword;
	@FXML
	public Label labelError;

	public MainController_old mainController = null;
	private static BorderPane contentLayout;
	Parent root = null;
	FXMLLoader fxmlLoader = null;
	private static User userData = null;

	// Scr
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// initializeMainWindow();
		initSystemParameters();
		// initControllers();
	}

	public void initSystemParameters() {
		TokenManager.initSystemConfig();
//		if (!TokenManager.isDebug) {
//			RFLogger.init();
//			RFLogger.RouteStdOutErrToFile();
//		}

	}

	public void initControllers() {
		// FXMLLoader fxmlLoader = new
		// FXMLLoader(getClass().getResource(ScreenConstant.ScreenLinks.MAIN_WINDOW));
		// contentLayout = fxmlLoader.load();
		// parent = fxmlLoader.getController();\

		try {
			mainController = new MainController_old();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void doLogin(Event event) {
		boolean destroyIt = false;
		String username = testBoxUserName.getText();
		String password = testBoxPassword.getText();
		Connection conn = null;
		try {
			if (username.length() == 0) {
				labelError.setText("Please Enter Username !!!");
				testBoxUserName.requestFocus();

			} else if (password.length() == 0) {
				labelError.setText("Please Enter Password !!!");
				testBoxPassword.requestFocus();
			} else {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				userData = UserLogin.Login(conn, username, password);
				if (userData != null && !Misc.isUndef(userData.getId())) {
					// root =
					// FXMLLoader.load(getClass().getResource(ScreenConstant.ScreenLinks.MAIN_WINDOW));
					fxmlLoader = new FXMLLoader(getClass().getResource(ScreenConstant.ScreenLinks.MAIN_WINDOW_OLD));
					root = fxmlLoader.load();
					mainController = (MainController_old) fxmlLoader.getController();
					mainController.initializeController(userData, mainController);
					Stage stage = new Stage();
					stage.setTitle("My New Stage Title");
					stage.setScene(new Scene(root));
					stage.show();
					((Node) (event.getSource())).getScene().getWindow().hide();
				} else {
					labelError.setText("Incorrect Username or Password !!!");
					testBoxPassword.requestFocus();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	@FXML
	public void onLoginKeyPress(KeyEvent keyEvent) {
		if (keyEvent.getCode() != KeyCode.ENTER) {
			return;
		}
		Button control = ((Button) keyEvent.getSource());
		doLogin((Event) keyEvent);
	}

	@FXML
	public void onLoginMouseClicked(ActionEvent actionEvent) {
		Button control = ((Button) actionEvent.getSource());
		doLogin((Event) actionEvent);
	}

}
