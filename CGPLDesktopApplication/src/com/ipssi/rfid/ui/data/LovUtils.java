package com.ipssi.rfid.ui.data;

import com.ipssi.cgplSap.RecordType;
import com.ipssi.gen.utils.DBConnectionPool;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.ui.controller.MainController;
import com.jfoenix.controls.JFXComboBox;

import javafx.scene.control.ComboBox;

/**
 * Created by ipssi11 on 18-Oct-16.
 */
public class LovUtils {

	private static int LOV_FOR_INTERNAL = 0;
	private static int LOV_FOR_ROAD = 1;
	private static final Logger log = Logger.getLogger(LovUtils.class.getName());
	
	private static void setIndex(ComboBox comboBox, int id) {
		if (comboBox == null) {
			return;
		}
		comboBox.getSelectionModel();
	}

	public static int getIntValue(ComboBox comboBox) {
		ComboItem c = (ComboItem) getValue(comboBox);
		return c == null ? Misc.getUndefInt() : c.getValue();
	}

	public static String getTextValue(ComboBox comboBox) {
		ComboItem c = (ComboItem) getValue(comboBox);
		return c == null ? "" : c.getLabel();
	}

	private static Object getValue(ComboBox comboBox) {
		if (comboBox == null) {
			return null;
		}
		return comboBox.getSelectionModel().getSelectedItem();
	}

	public static boolean isUndef(ComboBox comboBox) {
		return comboBox == null || comboBox.getSelectionModel().getSelectedItem() == null
				|| !(comboBox.getSelectionModel().getSelectedItem() instanceof ComboItem)
				|| Misc.isUndef(((ComboItem) comboBox.getSelectionModel().getSelectedItem()).getValue());
	}

	public static void setLov(Connection conn, int portNodeId, JFXComboBox comboBox, LovDao.LovItemType lovItemType,
			int selectedValue, int otherParamVal) {
		if (comboBox == null) {
			comboBox = new JFXComboBox<ComboItem>();
		}

		Pair<Integer, ArrayList<ComboItem>> itemListPair = null;
		ArrayList<ComboItem> lovList = null;
		int itemIndex = Misc.getUndefInt();
		switch (lovItemType) {
		case DATA_ENTRY_TYPE:
			lovList = new ArrayList<ComboItem>();
			int entryType = LovType.DataEntry.AUTO;
			lovList.add(new ComboItem(entryType, LovType.DataEntry.getStr(entryType)));
			entryType = LovType.DataEntry.MANUAL;
			lovList.add(new ComboItem(entryType, LovType.DataEntry.getStr(entryType)));

			itemListPair = new Pair<Integer, ArrayList<ComboItem>>(findItemIndex(lovList, selectedValue), lovList);
			break;
		case PRINTER:
			lovList = new ArrayList<ComboItem>();
			entryType = LovType.DeviceState.DISABLE;
			lovList.add(new ComboItem(entryType, LovType.DeviceState.getStr(entryType)));
			entryType = LovType.DeviceState.ENABLE;
			lovList.add(new ComboItem(entryType, LovType.DeviceState.getStr(entryType)));

			itemListPair = new Pair<Integer, ArrayList<ComboItem>>(findItemIndex(lovList, selectedValue), lovList);
			break;
		case AUTO_COMPLETE_VEHICLE:
			lovList = new ArrayList<ComboItem>();
			entryType = LovType.DataEntry.AUTO;
			lovList.add(new ComboItem(entryType, LovType.DataEntry.getStrAutoComplete(entryType)));
			entryType = LovType.DataEntry.MANUAL;
			lovList.add(new ComboItem(entryType, LovType.DataEntry.getStrAutoComplete(entryType)));
			itemListPair = new Pair<Integer, ArrayList<ComboItem>>(findItemIndex(lovList, selectedValue), lovList);
			break;
		case PROCESSING_STATUS:
			lovList = new ArrayList<ComboItem>();
			entryType = LovType.Status.ANY;
			lovList.add(new ComboItem(entryType, LovType.Status.getStr(entryType)));
			entryType = LovType.Status.REPORTED;
			lovList.add(new ComboItem(entryType, LovType.Status.getStr(entryType)));
			entryType = LovType.Status.DISPATCHED;
			lovList.add(new ComboItem(entryType, LovType.Status.getStr(entryType)));
			entryType = LovType.Status.IN_PLANT;
			lovList.add(new ComboItem(entryType, LovType.Status.getStr(entryType)));
			entryType = LovType.Status.UNDER_VERIFICATION;
			lovList.add(new ComboItem(entryType, LovType.Status.getStr(entryType)));
			entryType = LovType.Status.CANCELLED;
			lovList.add(new ComboItem(entryType, LovType.Status.getStr(entryType)));
			entryType = LovType.Status.CLOSED;
			lovList.add(new ComboItem(entryType, LovType.Status.getStr(entryType)));
			itemListPair = new Pair<Integer, ArrayList<ComboItem>>(findItemIndex(lovList, selectedValue), lovList);
			break;
		
			
		case OPEN_CLOSE:
			lovList = new ArrayList<ComboItem>();
			entryType = LovType.TprStatus.ANY;
			lovList.add(new ComboItem(entryType, LovType.TprStatus.getStr(entryType)));
			entryType = LovType.TprStatus.IN_PLANT;
			lovList.add(new ComboItem(entryType, LovType.TprStatus.getStr(entryType)));
			entryType = LovType.TprStatus.DISPATCHED;
			lovList.add(new ComboItem(entryType, LovType.TprStatus.getStr(entryType)));
			entryType = LovType.TprStatus.COMPLETED;
			lovList.add(new ComboItem(entryType, LovType.TprStatus.getStr(entryType)));
			itemListPair = new Pair<Integer, ArrayList<ComboItem>>(findItemIndex(lovList, selectedValue), lovList);
			break;	
		
		case INVOICE_STATUS:	
			lovList = new ArrayList<ComboItem>();
			entryType = RecordType.MessageType.ANY;
			lovList.add(new ComboItem(entryType, RecordType.MessageType.getStr(entryType)));
			entryType = RecordType.MessageType.NO_RESPONSE;
			lovList.add(new ComboItem(entryType, RecordType.MessageType.getStr(entryType)));
			entryType = RecordType.MessageType.SUCCESS;
			lovList.add(new ComboItem(entryType, RecordType.MessageType.getStr(entryType)));
			entryType = RecordType.MessageType.FAILED;
			lovList.add(new ComboItem(entryType, RecordType.MessageType.getStr(entryType)));
			entryType = RecordType.MessageType.CANCEL;
			lovList.add(new ComboItem(entryType, RecordType.MessageType.getStr(entryType)));
			itemListPair = new Pair<Integer, ArrayList<ComboItem>>(findItemIndex(lovList, selectedValue), lovList);
			break;

		case INVOICE_CANCEL:
			lovList = new ArrayList<ComboItem>();
			entryType = LovType.Reason_60284.TRANSPORTER_CHANGE;
			lovList.add(new ComboItem(entryType, LovType.Reason_60284.getStr(entryType)));
			entryType = LovType.Reason_60284.TARE_WEIGHT_CHANGE;
			lovList.add(new ComboItem(entryType, LovType.Reason_60284.getStr(entryType)));
			entryType = LovType.Reason_60284.GROSS_WEIGHT_CHANGE;
			lovList.add(new ComboItem(entryType, LovType.Reason_60284.getStr(entryType)));
			entryType = LovType.Reason_60284.DUPLICATE;
			lovList.add(new ComboItem(entryType, LovType.Reason_60284.getStr(entryType)));
			entryType = LovType.Reason_60284.MERGED;
			lovList.add(new ComboItem(entryType, LovType.Reason_60284.getStr(entryType)));
			entryType = LovType.Reason_60284.USER_ERROR;
			lovList.add(new ComboItem(entryType, LovType.Reason_60284.getStr(entryType)));
			entryType = LovType.Reason_60284.TESTING_TPR;
			lovList.add(new ComboItem(entryType, LovType.Reason_60284.getStr(entryType)));
			entryType = LovType.Reason_60284.SYSTEM_ERROR;
			lovList.add(new ComboItem(entryType, LovType.Reason_60284.getStr(entryType)));

			itemListPair = new Pair<Integer, ArrayList<ComboItem>>(findItemIndex(lovList, selectedValue), lovList);
			break;
		case HOUR:
        	lovList = new ArrayList<ComboItem>();
        	lovList.add(new ComboItem(Misc.getUndefInt(),"-"));
        	for (int i = 0; i < 24; i++) {
        		lovList.add(new ComboItem(i,i<9? "0"+i : i+""));
			}
        	itemListPair = new Pair<Integer, ArrayList<ComboItem>>(findItemIndex(lovList, selectedValue),lovList);
        	break;
        case MINUTE:
        	lovList = new ArrayList<ComboItem>();
        	lovList.add(new ComboItem(Misc.getUndefInt(),"-"));
        	for (int i = 0; i < 60; i++) {
        		lovList.add(new ComboItem(i,i<9? "0"+i : i+""));
			}
        	itemListPair = new Pair<Integer, ArrayList<ComboItem>>(findItemIndex(lovList, selectedValue),lovList);
        	break;
		default:
			if (conn != null) {
				itemListPair = LovDao.getLovItemPair(conn, portNodeId, lovItemType, selectedValue, otherParamVal);
			}
			break;
		}
		if (itemListPair != null && itemListPair.second != null && itemListPair.second.size() > 0) {
			comboBox.getItems().removeAll(comboBox.getItems());
			comboBox.getItems().addAll(itemListPair.second);

			if (!Misc.isUndef(itemListPair.first)) {
				comboBox.getSelectionModel().clearAndSelect(itemListPair.first);
			}
		}
	}

	public static void setLovNew(Connection conn, int portNodeId, JFXComboBox comboBox, LovDao.LovItemType lovItemType,
			int selectedValue, int otherParamVal, String selectedText) {
		if (comboBox == null) {
			comboBox = new JFXComboBox<ComboItem>();
		}

		Pair<Integer, ArrayList<ComboItem>> itemListPair = null;
		ArrayList<ComboItem> lovList = null;
		int itemIndex = Misc.getUndefInt();
		switch (lovItemType) {
		case VEHICLE:
			// lovList = new ArrayList<ComboItem>();
			break;
		default:
			if (conn != null) {
				itemListPair = LovDao.getLovItemPairNew(conn, portNodeId, lovItemType, selectedValue, otherParamVal,
						selectedText);
			}
			break;
		}
		if (itemListPair != null && itemListPair.second != null && itemListPair.second.size() > 0) {
			comboBox.getItems().removeAll(comboBox.getItems());
			comboBox.getItems().addAll(itemListPair.second);

			if (!Misc.isUndef(itemListPair.first)) {
				comboBox.getSelectionModel().clearAndSelect(itemListPair.first);
			}
		}
	}

	private static int findItemIndex(ArrayList<ComboItem> lovList, int value) {
		int retval = Misc.getUndefInt();
		for (int i = 0, is = lovList == null ? 0 : lovList.size(); i < is; i++) {
			if (lovList.get(i).getValue() == value) {
				return i;
			}
		}
		return retval;
	}

	public static void initializeComboBox(JFXComboBox combox, LovDao.LovItemType lovItemType, int selectedValue,
			int otherParamVal) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			LovUtils.setLov(conn, TokenManager.portNodeId, combox, lovItemType, selectedValue, otherParamVal);
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

	public static void initializeComboBox(JFXComboBox combox, LovDao.LovItemType lovItemType, int selectedValue,
			int otherParamVal, String selectedText) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			LovUtils.setLovNew(conn, TokenManager.portNodeId, combox, lovItemType, selectedValue, otherParamVal,
					selectedText);
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

	public static void main1(String[] arg) {
		System.out.println("LovUtils");
	}
}
