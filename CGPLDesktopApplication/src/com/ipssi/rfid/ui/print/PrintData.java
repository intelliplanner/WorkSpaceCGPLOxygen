/*
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.print;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.print.PrinterException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.processor.TokenManager;

/**
 *
 * @author Vi$ky
 */
public class PrintData extends javax.swing.JDialog {

	TPRecord tpRecord = null;
	private String vehicleNo = "";
	private String tprId = "";
	private String description = "";
	private String igstRate = "";
	private String igstAmt = "";
	private String sgstAmt = "";
	private String sgstRate = "";
	private String cgstRate = "";
	private String cgstAmt = "";
	private String ratePerItem = "";
	private String hsn = "";
	private String quantity = "";
	private String inTime = "";
	private String outTime = "";
	private String transporter = "";
	private String invoiceDate = "";
	private String dueInvoiceDate = "";

	/**
	 * Creates new form PrintData
	 */
	// public PrintData(java.awt.Frame parent, boolean modal, TPRecord tpRecord,
	// String supplierNetWt , String shortWts, String acceptedWt) {
	public PrintData(java.awt.Frame parent, boolean modal, TPRecord tpRecord, String supplierNetWt, String shortWts) {
		super(parent, modal);
		initComponents();
		textPane.setBackground(Color.WHITE);
		 this.tpRecord = tpRecord;
		// this.shortWts = shortWts;
		// this.supplierNetWt = supplierNetWt;
		// this.acceptedWt = acceptedWt;
		// initializeVariables();
		center();
		initializeVariables();
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				printData();
			}
		});
		// print();

	}

	protected void center() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension us = getSize();
		int x = (screen.width - us.width) / 2;
		int y = (screen.height - us.height) / 2;
		setLocation(x, y);
	}

	void printData() {
		StyledDocument doc = textPane.getStyledDocument();
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style regular = doc.addStyle("regular", def);

		// Create an italic style
		Style italic = doc.addStyle("italic", regular);
		StyleConstants.setItalic(italic, true);

		// Create a bold style
		Style bold = doc.addStyle("bold", regular);
		StyleConstants.setBold(bold, true);

		// Create a small style
		Style small = doc.addStyle("small", regular);
		StyleConstants.setFontSize(small, 10);
		// StyleConstants.setAlignment(small, StyleConstants.ALIGN_CENTER);
		Style medium = doc.addStyle("small", regular);
		StyleConstants.setFontSize(medium, 10);
		// StyleConstants.setAlignment(medium, StyleConstants.ALIGN_CENTER);
		// Create a large style
		Style large = doc.addStyle("large", bold);
		StyleConstants.setFontSize(large, 12);
		// StyleConstants.setAlignment(large, StyleConstants.ALIGN_CENTER);

		Style label = doc.addStyle("large", bold);
		StyleConstants.setFontSize(label, 10);
		// StyleConstants.setAlignment(label, StyleConstants.ALIGN_RIGHT);
		Style text = doc.addStyle("large", regular);
		StyleConstants.setFontSize(text, 10);
		// Create a superscript style
		Style superscript = doc.addStyle("superscript", regular);
		StyleConstants.setSuperscript(superscript, true);
		// Create a highlight style
		Style highlight = doc.addStyle("highlight", regular);
		StyleConstants.setBackground(highlight, Color.yellow);
		try {
			doc.setLogicalStyle(8, regular);
			doc.insertString(0, "           COASTAL GUJARAT POWER LIMITED\n", large);
			doc.insertString(doc.getLength(), "                     (A TATA POWER COMPANY)\n", medium);
			doc.insertString(doc.getLength(), "              4000 MW UMP Project, Tunda-Vandh Road\n", small);
			doc.insertString(doc.getLength(), "            Villaage Tunda Kutch 370435 ,Gujarat India\n\n",
					small);
//			doc.insertString(doc.getLength(),
//					"---------------------------------" + "----------------------------------------------------\n",
//					regular);

			doc.insertString(doc.getLength(), getLabelString("Tpr-Id: ", 22), label);
			doc.insertString(doc.getLength(), getString(tprId, 18), text);
			doc.insertString(doc.getLength(), getLabelString("Vehicle#: ", 18), label);
			doc.insertString(doc.getLength(), vehicleNo + "\n", text);

			doc.insertString(doc.getLength(), getLabelString("Date of Invoice: ", 22), label);
			doc.insertString(doc.getLength(), getString(invoiceDate, 18), text);
			doc.insertString(doc.getLength(), getLabelString("Due Date: ", 18), label);
			doc.insertString(doc.getLength(), dueInvoiceDate + "\n", text);

			doc.insertString(doc.getLength(), getLabelString("Transporter: ", 22), label);
			doc.insertString(doc.getLength(), getString(transporter, 18), text);
			doc.insertString(doc.getLength(), getLabelString("HSN: ", 18), label);
			doc.insertString(doc.getLength(), hsn + "\n", text);
//			doc.insertString(doc.getLength(),
//					"-------------------------" + "------------------------------------------------------------\n",
//					regular);

			doc.insertString(doc.getLength(), getLabelString("Bulker Intime: ", 22), label);
			doc.insertString(doc.getLength(), getString(inTime, 18), text);
			doc.insertString(doc.getLength(), getLabelString("Bulker Outtime: ", 18), label);
			doc.insertString(doc.getLength(), outTime + "\n\n", text);

			
//			doc.insertString(doc.getLength(), getLabelString("Qty: ", 18), label);
//			doc.insertString(doc.getLength(), quantity + "\n", text);

//			doc.insertString(doc.getLength(), getLabelString("Unit: ", 22), label);
//			doc.insertString(doc.getLength(), getString("To", 18), text);
//			doc.insertString(doc.getLength(), getLabelString("Rate(Per Item): ", 18), label);
//			doc.insertString(doc.getLength(), ratePerItem + "\n", text);

			// doc.insertString(doc.getLength(), getLabelString("Challan Wt: ",60), label);
			// doc.insertString(doc.getLength(), challanWt + "\n", text);

//			doc.insertString(doc.getLength(),
//					"-------------------------------------" + "------------------------------------------------\n",
//					regular);

//			doc.insertString(doc.getLength(), getLabelString("CGST Rate(%): ", 22), label);
//			doc.insertString(doc.getLength(), getString(cgstRate, 18), text);
//			doc.insertString(doc.getLength(), getLabelString("CGST Amt(INR): ", 18), label);
//			doc.insertString(doc.getLength(), cgstAmt + "\n", text);
//
//			doc.insertString(doc.getLength(), getLabelString("SGST Rate(%): ", 22), label);
//			doc.insertString(doc.getLength(), getString(sgstRate, 18), text);
//			doc.insertString(doc.getLength(), getLabelString("SGST Amt(INR): ", 18), label);
//			doc.insertString(doc.getLength(), sgstAmt + "\n", text);
//
//			doc.insertString(doc.getLength(), getLabelString("IGST Rate(%): ", 22), label);
//			doc.insertString(doc.getLength(), getString(igstRate, 18), text);
//			doc.insertString(doc.getLength(), getLabelString("IGST Amt(INR): ", 18), label);
//			doc.insertString(doc.getLength(), igstAmt + "\n", text);

//			doc.insertString(doc.getLength(),
//					"-------------------------" + "------------------------------------------------------------\n",
//					regular);

			doc.insertString(doc.getLength(), getLabelString("Description of Goods", 22), label);
			doc.insertString(doc.getLength(), getString("", 18), text);
			doc.insertString(doc.getLength(), getLabelString("Authorised Signatory", 18), label);
			doc.insertString(doc.getLength(), "" + "\n", text);
			
			doc.insertString(doc.getLength(), getLabelString(description, 22), label);
			doc.insertString(doc.getLength(), getString("", 18), text);
			

			print();
		} catch (BadLocationException e) {
			e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
		} finally {
			this.dispose();
		}
	}

	private String getString(String str1, int defaultLength) {
		int strLen = str1.length();
		int diffLength = defaultLength - strLen;
		String str2 = " ";
		String str3 = "";
		for (int i = 0, is = str1 == null ? 0 : diffLength; i < is; i++) {
			str3 += str2;
		}
		str1 += str3;
		System.out.print("New Length " + str1.length());
		return str1;
	}

	private String getLabelString(String str1, int defaultLength) {
		int strLen = str1.length();
		int diffLength = defaultLength - strLen;
		String str2 = " ";
		String str3 = "";
		for (int i = 0, is = str1 == null ? 0 : diffLength; i < is; i++) {
			str3 += str2;
		}
		str3 = str3 + str1;
		System.out.print("New Length " + str3.length());
		return str3;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		panel1 = new java.awt.Panel();
		jScrollPane1 = new javax.swing.JScrollPane();
		textPane = new javax.swing.JTextPane();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		textPane.setEditable(false);
		jScrollPane1.setViewportView(textPane);

		javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
		panel1.setLayout(panel1Layout);
		panel1Layout.setHorizontalGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panel1Layout.createSequentialGroup().addContainerGap()
						.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 625, Short.MAX_VALUE)
						.addContainerGap()));
		panel1Layout.setVerticalGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						panel1Layout.createSequentialGroup()
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 433,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(88, 88, 88)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(0, 0, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				panel1, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		Connection conn = null;
		ArrayList<Object> list = null;
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
//				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
//				TPRecord tpr = new TPRecord();
//				tpr.setTprId(1);
//				list	= (ArrayList<Object> )RFIDMasterDao.select(conn, tpr);
//					if (list != null && list.size() > 0) {
//						tpr = (TPRecord) list.get(0);
//					}
//					new PrintData(null, true, tpr, "", "").setVisible(true);
			} catch (Exception ex) {
				 Logger.getLogger(PrintData.class.getName()).log(Level.SEVERE,
				 null, ex);
			}
		 finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (GenericException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// </editor-fold>

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				PrintData dialog = new PrintData(new javax.swing.JFrame(), true, null, "", "");
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
					@Override
					public void windowClosing(java.awt.event.WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}

	void print() {
		try {
			textPane.setContentType("text/plain");
			boolean done = textPane.print();
			if (done) {
				System.out.println("Printing is done");
			} else {
				System.out.println("Error while printing");
			}
		} catch (PrinterException ex) {
			Logger.getLogger(PrintData.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JScrollPane jScrollPane1;
	private java.awt.Panel panel1;
	private javax.swing.JTextPane textPane;
	// End of variables declaration//GEN-END:variables

	private void initializeVariables() {
		if (tpRecord != null) {
			Connection conn = null;
			boolean destroyIt = false;
			try {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();


				// if(tpRecord.getEarliestUnloadGateInEntry() != null){
				// gateInTime =
				// UIConstant.displayFormat.format(tpRecord.getEarliestUnloadGateInEntry());
				// }
				
				// if(tpRecord.getLatestUnloadWbInExit() != null){
				// grossTime =
				// UIConstant.displayFormat.format(tpRecord.getLatestUnloadWbInExit());
				// }
				// if(tpRecord.getLatestUnloadWbOutExit() != null){
				// tareTime =
				// UIConstant.displayFormat.format(tpRecord.getLatestUnloadWbOutExit());
				// }
			
			tprId = Misc.getPrintableInt(tpRecord.getTprId());
	 		vehicleNo = tpRecord.getVehicleName();
	 		igstRate = "";
			igstAmt = "";
			sgstAmt = "";
			sgstRate = "";
			cgstRate = "";
			cgstAmt = "";
			ratePerItem = "";
			hsn = TokenManager.HSN_NO;
			
//			quantity = ;
			inTime = UIConstant.timeFormat.format(tpRecord.getComboStart());
			outTime = UIConstant.timeFormat.format(new Date());
			transporter = Misc.getParamAsString(tpRecord.getTransporterCode());
			invoiceDate =  UIConstant.timeFormat.format(new Date());;
			dueInvoiceDate = "30 Days Credit Limit";
			description = "FLY ASH";				
				
				
				
				// shortWt = shortWts;

				// double calNetWt =GateInDao.calculateNetWt(tpRecord.getUnloadGross(),
				// tpRecord.getUnloadTare());
				// netWt = Misc.getPrintableDouble(calNetWt);
				//
				// double calTotalShort
				// =GateInDao.calculateTotalShort(Misc.getParamAsDouble(supplierNetWt),
				// calNetWt);
				// shortWt = Misc.getPrintableDouble(calTotalShort);
				//
				// double acceptedWt =
				// GateInDao.calculateAcceptedNetWt(Misc.getParamAsDouble(supplierNetWt),
				// calNetWt);
				// acceptWt = Misc.getPrintableDouble(acceptedWt);

			} catch (Exception ex) {
				destroyIt = true;
				ex.printStackTrace();
			} finally {
				try {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		// else{
		// supplierNet = supplierNetWt;
		// shortWt = shortWts;
		// }
	}

}
