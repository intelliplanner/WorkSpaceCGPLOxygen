package com.ipssi.rfid.ui.controller;

import java.awt.FlowLayout;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class DisconnectionDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	/**
	 * Launch the application.
	 */
	private JLabel message = null;

	public static void main(String[] args) {
		try {
			DisconnectionDialog dialog = new DisconnectionDialog("");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public void stop() {
		this.dispose();
	}

	public DisconnectionDialog(String str) {
		setModal(true);
		setBounds(100, 100, 446, 155);
		message = new JLabel(str);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(message,
				GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(message,
				GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE));
		getContentPane().setLayout(groupLayout);
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		// setVisible(true);
	}
}
