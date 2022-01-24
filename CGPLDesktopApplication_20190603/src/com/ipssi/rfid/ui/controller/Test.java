/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.ipssi.rfid.constant.ScreenConstant;
import com.jfoenix.controls.JFXButton;
import java.io.FileReader;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 *
 * @author IPSSI
 */

public class Test extends Application {

	@FXML
	private Pane HEADER1;
	@FXML
	private JFXButton CONTROL_SAVE;

	Invocable invocable = null;

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource(ScreenConstant.ScreenLinks.test2));
		Scene scene = new Scene(root, 1300, 600);
		primaryStage.setScene(scene);
		// scene.getStylesheets().add("style.css");
		ScriptEngine ee = new ScriptEngineManager().getEngineByName("Nashorn");
		// Reading Nashorn file
		ee.eval(new FileReader("main.js"));
		invocable = (Invocable) ee;
		// calling a function
		invocable.invokeFunction("functionDemo1");
		// calling a function and passing variable as well.
		invocable.invokeFunction("functionDemo2", "Nashorn");
		// primaryStage.sizeToScene();
		// HEADER1.getStyleClass().add("linear-grad1");
		// CONTROL_SAVE.getStyleClass().add("button1");
		primaryStage.show();
	}

	public static void main(String args[]) {
		launch(args);
	}
}
