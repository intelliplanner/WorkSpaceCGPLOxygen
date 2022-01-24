package com.ipssi.rfid.ui.controller;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.database.DropDownValues;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;

public class AutoCompleteCombo extends JComboBox {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JTextField tf;
	private ComboKeyEvent keyEvent = null;
	// private Connection conn = null;
	private static final Logger log = Logger.getLogger(AutoCompleteCombo.class.getName());
	
	public AutoCompleteCombo() {
		super();
		this.setEditable(true);
		setUI(ColorArrowUI.createUI(this));
		tf = (JTextField) this.getEditor().getEditorComponent();
		tf.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				String text = tf.getText()
						+ (Character.isLetterOrDigit(e.getKeyChar()) || e.getKeyChar() == '_' ? e.getKeyChar() : "");
				if (text.length() <= 3) {
					setModel(new DefaultComboBoxModel(), tf.getText());
					hidePopup();
					// setModel(new DefaultComboBoxModel(v), "");
				} else {
					DefaultComboBoxModel m = getSuggestedModel(text);
					if (m.getSize() == 0 || hide_flag) {
						hidePopup();
						hide_flag = false;
					} else {
						setModel(m, tf.getText());
						showPopup();
					}
				}
			}

			public void keyPressed(KeyEvent e) {
				String text = tf.getText();
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_ENTER) {
					/*
					 * if(!v.contains(text)) { v.addElement(text); Collections.sort(v, new
					 * ComboItemSortHelper()); setModel(getSuggestedModel(v, text), text); }
					 */

					int val = getValue();
					/*
					 * if(Misc.isUndef(val)) tf.setBackground(Color.RED); else
					 * tf.setBackground(Color.GREEN);
					 */
					hidePopup();
					hide_flag = true;
				} else if (code == KeyEvent.VK_ESCAPE) {
					hide_flag = true;
				} else if (code == KeyEvent.VK_RIGHT) {
					/*
					 * for(int i=0;i<v.size();i++) { String str = v.elementAt(i).getLabel();
					 * if(str.startsWith(text)) { setSelectedIndex(-1); tf.setText(str); return; } }
					 */
				} else {
					tf.setBackground(Color.WHITE);
				}
				if (keyEvent != null) {
					keyEvent.onKeyPress(e);
				}
			}
		});

		// setModel(new DefaultComboBoxModel(v), "");
		/*
		 * JPanel p = new JPanel(new BorderLayout());
		 * p.setBorder(BorderFactory.createTitledBorder("AutoSuggestion Box"));
		 * p.add(combo, BorderLayout.NORTH); add(p);
		 * setBorder(BorderFactory.createEmptyBorder(5,5,5,5)); setPreferredSize(new
		 * Dimension(300, 150));
		 */
		tf.setText("");
	}

	public int getValue() {
		return DropDownValues.getComboSelectedVal(this);
	}

	public void setText(String text) {
		setText(Misc.getUndefInt(), text);
	}

	public void setText(int id, String text) {
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		m.addElement(new ComboItem(id, text));
		setModel(m, text);
		if (m != null && m.getSize() == 1)
			setSelectedIndex(0);
		else
			setSelectedIndex(-1);
	}

	public void setTextEditable(boolean flag) {
		tf.setEditable(flag);
		if (!flag) {
			setModel(new DefaultComboBoxModel(), "");
			hidePopup();
		}

	}

	public void setTextBackground(Color color) {
		tf.setBackground(color);
		// setBackground(color);
	}

	public void setTextBorder(Border border) {
		tf.setBorder(border);
	}

	private boolean hide_flag = false;

	private void setModel(DefaultComboBoxModel mdl, String str) {
		this.setModel(mdl);
		this.setSelectedIndex(-1);
		tf.setText(str);
	}

	private DefaultComboBoxModel getSuggestedModel(String text) {
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			java.util.List<ComboItem> list = getVehicleList(conn, text);
			for (int i = 0, is = list == null ? 0 : list.size(); i < is; i++) {
				ComboItem item = list.get(i);
				if (item != null && !Utils.isNull(item.getLabel())
						&& item.getLabel().toUpperCase().contains(text.toUpperCase()))
					m.addElement(item);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return m;
	}

	private ArrayList<ComboItem> getVehicleList(Connection conn, String text) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<ComboItem> vehList = new ArrayList<ComboItem>();
		String query = " select vehicle.id,vehicle.std_name from vehicle join "
				+ " (select distinct(vehicle.id) vehicle_id from vehicle "
				+ " left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) "
				+ " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) "
				+ " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) "
				+ " join port_nodes anc  on (anc.id in (" + TokenManager.portNodeId
				+ ") and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "
				+ " or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id "
				+ " where status in (1) and vehicle.std_name like '%" + text + "%'";
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				vehList.add(new ComboItem(Misc.getRsetInt(rs, 1), rs.getString(2)));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return vehList;
	}

	public static void main(String[] args) {
		ArrayList<ComboItem> source = null;
		Connection conn = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			AutoCompleteCombo com = new AutoCompleteCombo();
			com.setKeyEvent(new ComboKeyEvent() {
				@Override
				public void onKeyPress(KeyEvent e) {
					System.out.println();
					System.out.println("Selected id : ");
				}
			});
			frame.getContentPane().add(com);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (GenericException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static class ComboItemSortHelper implements Comparator<ComboItem> {
		@Override
		public int compare(com.ipssi.rfid.beans.ComboItem o1, com.ipssi.rfid.beans.ComboItem o2) {
			// TODO Auto-generated method stub
			return o1.getLabel().compareTo(o2.getLabel());
		}
	}

	public static interface ComboKeyEvent {
		void onKeyPress(KeyEvent e);
	}

	public ComboKeyEvent getKeyEvent() {
		return keyEvent;
	}

	public void setKeyEvent(ComboKeyEvent keyEvent) {
		this.keyEvent = keyEvent;
	}

	public String getText() {
		return tf.getText();
	}

	static class ColorArrowUI extends BasicComboBoxUI {

		public static ComboBoxUI createUI(JComponent c) {
			return new ColorArrowUI();
		}

		@Override
		protected JButton createArrowButton() {
			BasicArrowButton retval = new BasicArrowButton(BasicArrowButton.SOUTH, Color.cyan, Color.magenta,
					Color.yellow, Color.blue);
			retval.setVisible(false);
			retval.setEnabled(false);
			return retval;
		}
	}

	public boolean isTextEditable() {
		return tf != null ? tf.isEditable() : isEditable();
	}
}