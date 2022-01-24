/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.constant;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author Vi$ky
 */
public class UIConstant {
	public static final int OVERRIDE = 2;
	public static final int BLOCKED = 3;
	public static final int NOT_BLOCKED = 1;

	public static final int mines = 40001;
	public static final int transporter = 40002;
	public static final int grade = 40003;
	public static final int no_number = 40004;

	public static final String formTitle = "PPGCL_APPLICATION";
	
	public static final String SAVE = "save";
	public static String G_DEFAULT_DATE_FORMAT = "dd/MM/yy"; // ALSO BE SURE TO SET VARIABLE IN PROFILE.JS and C++
	public static String G_DEFAULT_DATE_FORMAT_HHMM = G_DEFAULT_DATE_FORMAT + " HH:mm";
	public static Font vehicleLabel = new java.awt.Font("Segoe UI", 0, 24);
	public static Font headingFont = new java.awt.Font("Arial", 0, 48);
	public static Font subHeadingFont = new java.awt.Font("Segoe UI", 1, 22);
	public static Font labelFont = new java.awt.Font("Segoe UI", 0, 18);
	public static Font buttonFont = new java.awt.Font("Arial", 0, 16);
	public static Font textFont = new java.awt.Font("Segoe UI", 0, 18);
	public static Font textFontSmall = new java.awt.Font("Segoe UI", 0, 16);
	public static Font labeltextFont = new java.awt.Font("Arial", 0, 16);
	public static Font labeltextFontTemp = new java.awt.Font("Arial", 0, 15);
	public static Color headingFontColor = new java.awt.Color(0, 51, 102);
	public static Color subHeadingFontColor = new java.awt.Color(0, 51, 102);
	public static Color labelFontColor = new java.awt.Color(0, 51, 102);
	public static Color buttonFontColor = new java.awt.Color(0, 51, 102);
	public static Color textFontColor = new java.awt.Color(0, 51, 153);
	public static Color labeltextFontColor = new java.awt.Color(0, 51, 102);
	public static Color noActionPanelColor = new Color(255, 6, 4);
	public static Color focusPanelColor = new Color(102, 255, 0);
	public static Color PanelWhite = Color.WHITE;
	public static String dialogTitle = "Confirm Dialog";
	public static final int DRIVER = 1;
	public static final int HELPER = 2;

	public static final int YES = 1;
	public static final int NO = 2;
	public static final int NC = 3;
	public static final int NOSELECTED = 4;

	public static final DateFormat inFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
	public static final DateFormat outFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
	public static final SimpleDateFormat defaultFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
	public static final SimpleDateFormat requireFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
	public static final DateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	public static final DateFormat displayFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public static final DateFormat slipFormat = new SimpleDateFormat("dd/MM/yyyy");
	public static final DateFormat timeFormat = new SimpleDateFormat("HH:mm");
	public static final DateFormat timeFormatWithSec = new SimpleDateFormat("HH:mm:ss");
	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static final DateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy");
	public static final int TOTAL_WB_COUNT = 2;
	public static Color PanelYellow = Color.YELLOW;
	public static Color PanelDarkGreen = new java.awt.Color(0, 153, 0);
	public static String SAVE_FAILER_MESSAGE = "Some Exception occurs, unable to process your request\nplease try again";
	public static String SCAN_TAG_MESSAGE = "Some Exception occurs, unable to process Scan\nplease try again";
	public static String SAP_EXCEPTION_MESSAGE = "Some Exception occurs, unable to process your Sap request";
	

	public static int showConfirmDialog(Component parent, String message) {
		String title = "Confirmation";
		JOptionPane optionPane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
		JDialog dialog = optionPane.createDialog(parent, title);
		Set forwardTraversalKeys = new HashSet(
				dialog.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardTraversalKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.VK_UNDEFINED));
		dialog.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardTraversalKeys);

		Set backwardTraversalKeys = new HashSet(
				dialog.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
		backwardTraversalKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_LEFT, KeyEvent.VK_UNDEFINED));
		dialog.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardTraversalKeys);
		listComponents(dialog);
		dialog.setVisible(true);
		dialog.dispose();

		Integer ret = (Integer) optionPane.getValue();
		if (ret == null) {
			return JOptionPane.NO_OPTION;
		}
		return ret.intValue();
	}

	private static void listComponents(java.awt.Container c) {
		if (c == null)
			return;
		for (java.awt.Component cc : c.getComponents())
			listComponents((java.awt.Container) cc);
		if (c instanceof javax.swing.JButton) {
			javax.swing.JButton btn = (javax.swing.JButton) c;
			javax.swing.InputMap inputMap = btn.getInputMap(javax.swing.JComponent.WHEN_FOCUSED);
			javax.swing.Action spaceAction = btn.getActionMap()
					.get(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, 0));
			inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), spaceAction);
		}
	}
	
	
	public static enum COLUR{
		BLUE,RED
	}
}
