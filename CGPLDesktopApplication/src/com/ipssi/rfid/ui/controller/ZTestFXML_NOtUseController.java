/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.ipssi.rfid.constant.ScreenConstant;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class ZTestFXML_NOtUseController  extends Application implements Initializable {

    @FXML
    private AnchorPane ANCHOR_NODE;
    @FXML
    private JFXTextField TEXT_VEHICLE_NAME;
    @FXML
    private JFXTextField TEXT_TRANSPORTER;
    @FXML
    private JFXTextField TEXT_GRADE;
    @FXML
    private JFXTextField TEXT_MINES;
    @FXML
    private JFXTextField TEXT_SUPPLIER;
    @FXML
    private JFXButton CONTROLLER_SAVE;
    @FXML
    private Label JobStatus;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	
    }    
    
    
    
    
    public static void main(String[] args) 
    {
        Application.launch(args);
    }
    private void print(Node node) 
	{
		// Define the Job Status Message
		try {
			
			
			
			JobStatus.textProperty().unbind();
			JobStatus.setText("Creating a printer job...");

			Printer printer = Printer.getDefaultPrinter();
			PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT,
					Printer.MarginType.DEFAULT);
//			double scaleX = pageLayout.getPrintableWidth() / node.getBoundsInParent().getWidth();
//			double scaleY = pageLayout.getPrintableHeight() / node.getBoundsInParent().getHeight();
//			node.getTransforms().add(new Scale(scaleX, scaleY));

			// Create a printer job for the default printer
			PrinterJob job = PrinterJob.createPrinterJob();

			if (job != null) {
				// Show the printer job status
				JobStatus.textProperty().bind(job.jobStatusProperty().asString());

				// Print the node
				boolean printed = job.printPage(node);

				if (printed) {
					// End the printer job
					job.endJob();
				} else {
					// Write Error Message
					JobStatus.textProperty().unbind();
					JobStatus.setText("Printing failed.");
				}
			} else {
				// Write Error Message
				JobStatus.setText("Could not create a printer job.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

    
	@FXML
	private void onControlKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			String controlId = getSourceId(event);
			handleActionControl(controlId);
		}
	}

	private void handleActionControl(String controlId) {
		// TODO Auto-generated method stub
		if (controlId == null) {
			return;
		}

		controlId = controlId.toUpperCase();

		switch (controlId) {
		case "CONTROLLER_SAVE":
			print(ANCHOR_NODE);
			break;
	
		default:
			break;
		}
	}




	public static String getSourceId(Event event){
		if (event == null) {
			return null;
		}
		String controllerId = null;
		if (event.getTarget() instanceof JFXTextField) {
			controllerId = ((JFXTextField) event.getSource()).getId();
		} else if (event.getTarget() instanceof JFXComboBox) {
			controllerId = ((JFXComboBox) event.getSource()).getId();
		} else if (event.getTarget() instanceof JFXCheckBox) {
			controllerId = ((JFXCheckBox) event.getSource()).getId();
		} else if (event.getTarget() instanceof JFXButton) {
			controllerId = ((JFXButton) event.getSource()).getId();
		} else if (event.getTarget() instanceof Button) {
			controllerId = ((Button) event.getSource()).getId();
		} else if (event.getTarget() instanceof TextField) {
			controllerId = ((TextField) event.getSource()).getId();
		} else if (event.getTarget() instanceof ComboBox) {
			controllerId = ((ComboBox) event.getSource()).getId();
		} else if (event.getTarget() instanceof CheckBox) {
			controllerId = ((CheckBox) event.getSource()).getId();
		}else if (event.getTarget() instanceof JFXTextArea) {
			controllerId = ((JFXTextArea) event.getSource()).getId();
		}else {
			if(event.getSource() instanceof JFXButton) {
				controllerId = ((JFXButton) event.getSource()).getId();
			}
		}
		return controllerId;
	}




	@FXML
	private void controlItemClicked(MouseEvent event) {
		 String controlId = getSourceId(event);
		 handleActionControl(controlId);
		
	}


	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource(ScreenConstant.ScreenLinks.test3));
		// initStage.setOnCloseRequest(e -> closePariparrels());
		primaryStage.setScene(new Scene(root));
//		initStage.initStyle(StageStyle.UNDECORATED);
		Image icon = new Image(getClass().getResourceAsStream("loginScreenLogo.png"));
		primaryStage.getIcons().add(icon);
		// primaryStage.getIcons().add(new Image("file:loginScreenLogo.png"));
		primaryStage.setTitle("CGPL Desktop Manager " + ScreenConstant.VERSION);
		primaryStage.setMaximized(true);
		primaryStage.show();
	
	}   

    
}
