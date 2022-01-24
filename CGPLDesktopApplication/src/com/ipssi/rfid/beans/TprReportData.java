package com.ipssi.rfid.beans;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ipssi.cgplSap.RecordType.MessageType;
import com.ipssi.rfid.constant.ScreenConstant;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.ui.controller.ReportViewController;
import com.ipssi.rfid.ui.controller.SalesOrderCancellationFormController;
import com.ipssi.rfid.ui.dao.GateInDao;
import com.jfoenix.controls.JFXButton;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TprReportData {

	private SimpleStringProperty tprId;
	private SimpleStringProperty invoiceStatus;
	private SimpleStringProperty invoiceNo;
	private SimpleStringProperty customer;
	private SimpleStringProperty lineItem;
	private SimpleStringProperty salesOrder;
	private SimpleStringProperty transporterName;
	private SimpleStringProperty tprStatus;
	private SimpleStringProperty vehicleName;
	private SimpleStringProperty loadTare;
	private SimpleStringProperty loadGross;
	private Date comboStart;
	private Date getLatestLoadWbInExit;
//	private int sapStatus;
	private int reportingStatus;
	private JFXButton actionButton;
	public ReportViewController reportViewController = null;
	public TprReportData(ReportViewController reportViewController,String tprIds, String openCLose, String invoiceStatuss, String invoiceNos,
			String customers, String lineItems, String salesOrders, String transporterNames, int statuss,
			String vehicleNames, String loadTares, String loadGrosss, Date comboStarts, Date getLatestLoadWbInExits,int reportingStatus) {
		this.reportViewController=reportViewController;
		tprId = new SimpleStringProperty(tprIds);
		tprStatus = new SimpleStringProperty(openCLose);
		invoiceStatus = new SimpleStringProperty(invoiceStatuss);
		invoiceNo = new SimpleStringProperty(invoiceNos);
		customer = new SimpleStringProperty(customers);
		lineItem = new SimpleStringProperty(lineItems);
		salesOrder = new SimpleStringProperty(salesOrders);
		transporterName = new SimpleStringProperty(transporterNames);
		tprStatus = new SimpleStringProperty(openCLose);
		
		vehicleName = new SimpleStringProperty(vehicleNames);
		loadTare = new SimpleStringProperty(loadTares);
		loadGross = new SimpleStringProperty(loadGrosss);
		this.reportingStatus=reportingStatus;
//		this.status =statuss;
		this.comboStart = comboStarts;
		this.getLatestLoadWbInExit = getLatestLoadWbInExits;
		if (this.reportingStatus != MessageType.SUCCESS && !Utils.isNull(loadTares) && !Utils.isNull(loadGrosss)) {
			if(GateInDao.checkSapQuantityAvailability(this)) {
				actionButton = new JFXButton("Create");
				actionButton.getStyleClass().add("fx-controller-button");
				actionButton.setOnAction(e -> {
					Platform.runLater(()->{
						reportViewController.createInvoice(this);
					});
				});
			}
		}else if(this.reportingStatus == MessageType.SUCCESS && !Utils.isNull(loadTares) && !Utils.isNull(loadGrosss)){
			actionButton = new JFXButton("Cancel");
			actionButton.getStyleClass().add("fx-controller-report-button");
			actionButton.setOnAction(e -> {
				openDialogWindow(this);
//				ReportViewController.cancelInvoice(this);
			});
		}
		// actionButton.addEventHandler(ActionEvent.ACTION, (e)->{});
	}

//	public int getReportingStatus() {
//		return reportingStatus;
//	}
//
//	public void setReportingStatus(int reportingStatus) {
//		this.reportingStatus = reportingStatus;
//	}

	

	public  void openDialogWindow(TprReportData tprData) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(ScreenConstant.ScreenLinks.SALES_ORDER_CANCEL_WINDOW));
		Parent parent;
		try {
			parent = fxmlLoader.load();
			SalesOrderCancellationFormController dialogController = fxmlLoader.<SalesOrderCancellationFormController>getController();
		    dialogController.initData(tprData);
		    dialogController.setTitle(ScreenConstant.ScreenTitle.CANCEL_INVOICE);
			Scene scene = new Scene(parent, 480, 380);
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(scene);
			
			stage.setTitle("Cancel Invoice");
			stage.setResizable(false);
			stage.showAndWait();

		} catch (IOException ex) {
			Logger.getLogger(TprReportData.class.getName()).log(Level.SEVERE, null, ex);
		}
	
	}

//	public String getStatus() {
//		return status.get();
//	}
//
//	public void setStatus(SimpleStringProperty status) {
//		this.status = status;
//	}

	public Date getComboStart() {
		return comboStart;
	}

	public void setComboStart(Date comboStart) {
		this.comboStart = comboStart;
	}



	public String getLoadTare() {
		return loadTare.get();
	}

	public void setLoadTare(SimpleStringProperty loadTare) {
		this.loadTare = loadTare;
	}

	public String getLoadGross() {
		return loadGross.get();
	}

	public void setLoadGross(SimpleStringProperty loadGross) {
		this.loadGross = loadGross;
	}

	public JFXButton getActionButton() {
		return actionButton;
	}

	public void setActionButton(JFXButton actionButton) {
		this.actionButton = actionButton;
	}

	public String getTprId() {
		return tprId.get();
	}

	public void setTprId(SimpleStringProperty tprId) {
		this.tprId = tprId;
	}
	

	public String getInvoiceStatus() {
		return invoiceStatus.get();
	}

	public void setInvoiceStatus(SimpleStringProperty invoiceStatus) {
		this.invoiceStatus = invoiceStatus;
	}

	public String getInvoiceNo() {
		return invoiceNo.get();
	}

	public void setInvoiceNo(SimpleStringProperty invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public String getCustomer() {
		return customer.get();
	}

	public void setCustomer(SimpleStringProperty customer) {
		this.customer = customer;
	}

	public String getLineItem() {
		return lineItem.get();
	}

	public void setLineItem(SimpleStringProperty lineItem) {
		this.lineItem = lineItem;
	}

	public String getSalesOrder() {
		return salesOrder.get();
	}

	public void setSalesOrder(SimpleStringProperty salesOrder) {
		this.salesOrder = salesOrder;
	}

	public String getVehicleName() {
		return vehicleName.get();
	}

	public void setVehicleName(SimpleStringProperty vehicleName) {
		this.vehicleName = vehicleName;
	}

	public String getTransporterName() {
		return transporterName.get();
	}

	public void setTransporterName(SimpleStringProperty transporterName) {
		this.transporterName = transporterName;
	}

	public String getTprStatus() {
		return tprStatus.get();
	}

	public void setTprStatus(SimpleStringProperty tprStatus) {
		this.tprStatus = tprStatus;
	}

	@Override
	public String toString() {
		return tprId.get();
	}

	public Date getGetLatestLoadWbInExit() {
		return getLatestLoadWbInExit;
	}

	public void setGetLatestLoadWbInExit(Date getLatestLoadWbInExit) {
		this.getLatestLoadWbInExit = getLatestLoadWbInExit;
	}

	// public static class EventHandlerImpl implements
	// javafx.event.EventHandler<ActionEvent>{
	// @Override
	// public void handle(ActionEvent event) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// }


	


	
}
