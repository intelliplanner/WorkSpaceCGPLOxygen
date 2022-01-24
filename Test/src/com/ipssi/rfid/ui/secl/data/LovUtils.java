package com.ipssi.rfid.ui.secl.data;

import java.sql.Connection;
import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.constant.Type;

import javafx.scene.control.ComboBox;


/**
 * Created by ipssi11 on 18-Oct-16.
 */
public class LovUtils {

    private static int LOV_FOR_INTERNAL=0;
    private static int LOV_FOR_ROAD=1;
    private static void setIndex(ComboBox comboBox,int id){
        if(comboBox == null)
            return;
        comboBox.getSelectionModel();
    }
    public static int getIntValue(ComboBox comboBox){
        ComboItem c = (ComboItem) getValue(comboBox);
        return c == null ? Misc.getUndefInt() : c.getValue();
    }
    public static String getTextValue(ComboBox comboBox){
        ComboItem c = (ComboItem) getValue(comboBox);
        return c == null ? "" : c.getLabel();
    }
    private static Object getValue(ComboBox comboBox){
        if(comboBox == null)
            return null;
        return comboBox.getSelectionModel().getSelectedItem();
    }
    public static boolean isUndef(ComboBox comboBox){
        return comboBox == null || comboBox.getSelectionModel().getSelectedItem() == null
                || !(comboBox.getSelectionModel().getSelectedItem() instanceof ComboItem)
                || Misc.isUndef(((ComboItem)comboBox.getSelectionModel().getSelectedItem()).getValue());
    }
    public static void setLov(Connection conn,int portNodeId, ComboBox comboBox, LovDao.LovItemType lovItemType, int selectedValue){
        if(comboBox == null)
            return;
        Pair<Integer, ArrayList<ComboItem>> itemListPair = null;
        ArrayList<ComboItem> lovList = null;
        int itemIndex = Misc.getUndefInt();
        switch (lovItemType) {
            case RFID_TYPE:
                lovList = new ArrayList<>();
                int rfidType=Type.RFID_CARD_TYPE.TEMPORARY;
                lovList.add(new ComboItem(rfidType,Type.RFID_CARD_TYPE.getString(rfidType)));
                    rfidType=Type.RFID_CARD_TYPE.PERMANENT;
                lovList.add(new ComboItem(rfidType,Type.RFID_CARD_TYPE.getString(rfidType)));
                    rfidType=Type.RFID_CARD_TYPE.NO_TAG;
                lovList.add(new ComboItem(rfidType,Type.RFID_CARD_TYPE.getString(rfidType)));
                itemListPair = new Pair<>(findItemIndex(lovList, selectedValue),lovList);
                break;
            case RFID_ISSUING_PURPOSE:
                lovList = new ArrayList<>();
                int rfidPurpose=Type.RFID_CARD_PURPOSE.INTERNAL;
                lovList.add(new ComboItem(rfidPurpose,Type.RFID_CARD_PURPOSE.getString(rfidPurpose)));
                    rfidPurpose=Type.RFID_CARD_PURPOSE.ROAD;
                lovList.add(new ComboItem(rfidPurpose,Type.RFID_CARD_PURPOSE.getString(rfidPurpose)));
                	rfidPurpose=Type.RFID_CARD_PURPOSE.WASHERY;
                lovList.add(new ComboItem(rfidPurpose,Type.RFID_CARD_PURPOSE.getString(rfidPurpose)));
                    rfidPurpose=Type.RFID_CARD_PURPOSE.OTHER;
                lovList.add(new ComboItem(rfidPurpose,Type.RFID_CARD_PURPOSE.getString(rfidPurpose)));
                itemListPair = new Pair<>(findItemIndex(lovList, selectedValue),lovList);
                break;
            default:
                if(conn != null)
                    itemListPair = LovDao.getLovItemPair(conn, portNodeId, lovItemType, selectedValue);
                break;
        }
        if(itemListPair != null && itemListPair.second != null && itemListPair.second.size() > 0){
            comboBox.getItems().removeAll(comboBox.getItems());
            comboBox.getItems().addAll(itemListPair.second);
            
            if(!Misc.isUndef(itemListPair.first))
                comboBox.getSelectionModel().clearAndSelect(itemListPair.first);
        }
    }
    private static int findItemIndex(ArrayList<ComboItem> lovList,int value) {
		int retval = Misc.getUndefInt();
		for (int i = 0,is = lovList == null ? 0 : lovList.size(); i < is; i++) {
			if(lovList.get(i).getValue() == value){
				return i;
			}
		}
		return retval;
	}
	public static void main(String[] arg) {
        System.out.println("LovUtils");
    }
}
