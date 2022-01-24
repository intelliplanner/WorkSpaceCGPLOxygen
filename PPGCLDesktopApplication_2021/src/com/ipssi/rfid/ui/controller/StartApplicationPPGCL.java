package com.ipssi.rfid.ui.controller;

import java.awt.Dimension;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

import com.ipssi.rfid.constant.ScreenConstant;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.ui.dao.GateInDao;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class StartApplicationPPGCL extends Application {

	public JFXTextField testBoxUserName;

	public JFXPasswordField testBoxPassword;

	public Label labelError;

	private JFXButton buttonLogin;

	Scene scene;

	Stage mainStag;

	private MainController mainController = null;

	private static final Logger log = Logger.getLogger(StartApplicationPPGCL.class.getName());

	@Override
	public void start(final Stage initStage) throws Exception {
		try {
			ServerSocket ss = null;
			try {
				ss = new ServerSocket(ScreenConstant.mutexPortNo);
				final ServerSocket ss1 = ss;
				new Thread(new Runnable() {
					@Override
					public void run() {
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
				showMainStage(initStage);
			} else {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Multiple Instances runnig");
				alert.setContentText("please close running instance");
				alert.showAndWait();
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.info("Error: " + e);
		}
	}

	private void showMainStage(Stage initStage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(ScreenConstant.ScreenLinks.MAIN_WINDOW));
		Parent root = fxmlLoader.load();
		mainController = fxmlLoader.getController();
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		double widh = screenSize.getWidth();
		double heigt = screenSize.getHeight();
		// HBox tilteHbox = (HBox)
		// fxmlLoader.getNamespace().get("Main_Window_Tilte_Hbox");
		// tilteHbox.setPrefWidth(screenSize.getWidth());
		scene = new Scene(root, widh, heigt);

		initStage.setScene(scene);
		mainController.init(this);
		Image icon = new Image(getClass().getResourceAsStream(ScreenConstant.SCREEN_IMAGES.APPLICATION_ICON));
		initStage.getIcons().add(icon);
		initStage.setTitle("PPGCL Desktop Manager " + ScreenConstant.VERSION);
		initStage.setMaximized(true);
		initStage.show();
		if (mainController != null)
			mainController.registerIdleMonitor();
	}

	public Scene getScene() {
		return scene;
	}

	public Stage getMainStag() {
		return mainStag;
	}

	public static void main(String args[]) {
		launch(args);
	}

	private void closePariparrels() {
		if (MainController._selectedMenuId != ScreenConstant.MenuItemId.LOGIN
				&& MainController.currentViewController != null) {
			MainController.currentViewController.stopRfid();
			MainController.currentViewController.stopSyncTprService();
			MainController.currentViewController.stopRfid();
		}
		GateInDao.forceSignut(TokenManager.userId, TokenManager.srcType, Integer.toString(TokenManager.systemId));
		// if (initTask != null) {
		// initTask.cancel();
		// }
		System.exit(0);
	}

	@Override
	public void stop() {
		closePariparrels();
	}
}
