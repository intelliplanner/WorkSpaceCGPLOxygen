/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.ipssi.rfid.constant.ScreenConstant;
import com.jfoenix.controls.JFXButton;
import java.io.FileReader;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class TestFxml2Controller extends Application {

	@FXML
	private JFXButton CONTROL_GATE_IN;

	Invocable invocable = null;

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource(ScreenConstant.ScreenLinks.test2));
		Scene scene = new Scene(root, 1300, 600);
		primaryStage.setScene(scene);
		// scene.getStylesheets().add("style.css");
		// ScriptEngine ee = new ScriptEngineManager().getEngineByName("Nashorn");
		// Reading Nashorn file
		// ee.eval(new FileReader("main.js"));
		// invocable = (Invocable)ee;
		// calling a function

		// calling a function and passing variable as well.

		// primaryStage.sizeToScene();
		// HEADER1.getStyleClass().add("linear-grad1");
		// CONTROL_SAVE.getStyleClass().add("button1");
		primaryStage.show();

	}

	@FXML
	private void gateInAction(MouseEvent event) throws ScriptException, NoSuchMethodException {
		// /invocable.invokeFunction("functionDemo2","Nashorn");
	}

	/**
	 * Initializes the controller class.
	 */

	public static void main(String args[]) {
		launch(args);
	}
}
