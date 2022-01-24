/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.dao;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.DimConfigInfo.Pair;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.RFIDTagInfo;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.readers.RFIDConfig;
import com.ipssi.rfid.readers.RFIDException;
import com.ipssi.rfid.readers.RFIDMaster;

/**
 *
 * @author Vi$ky
 */
public class IssueTag {
	private int readerId = 0;

	public int tagIssued(Vehicle vehicleBean) throws RFIDException {
		int isIssued = Misc.getUndefInt();
		RFIDHolder Holder = new RFIDHolder();
		Holder.setVehicleName(vehicleBean.getVehicleName());
		Holder.setAvgGross((int) Math.round(vehicleBean.getAvgGross()));
		Holder.setAvgTare((int) Math.round(vehicleBean.getAvgTare()));
		Holder.setTransporterId(vehicleBean.getTransporterId());
		if (RFIDMaster.getDesktopReader() != null) {
			ArrayList<String> tags = RFIDMaster.getDesktopReader().getRFIDTagList();
			int size = tags == null ? 0 : tags.size();
			if (size == 1) {
				String s = tags.get(0);
				vehicleBean.setEpcId(s);
				Holder.setEpcId(s);
				RFIDTagInfo rfidTagInfo = Holder.createTag(0);
				boolean isWrite = RFIDMaster.getDesktopReader().writeCardG2(rfidTagInfo, 5);
				if (isWrite) {
					isIssued = 0;// issued
				} else {
					isIssued = 1;// Not Issued;
				}
			} else if (size == 0) {
				isIssued = 4;// multiple tags
			} else {
				isIssued = 3;
			}
		} else {
			isIssued = 2;// reader Not Connected
		}
		return isIssued;
	}

	
	
	public int writeTprOnCard(RFIDHolder Holder) throws RFIDException {
		int isIssued = Misc.getUndefInt();
		if (RFIDMaster.getDesktopReader() != null) {
			ArrayList<String> tags = RFIDMaster.getDesktopReader().getRFIDTagList();
			for (String s : tags) {
				Holder.setEpcId(s);
				RFIDTagInfo rfidTagInfo = Holder.createTag(0);
				boolean isWrite = RFIDMaster.getDesktopReader().writeCardG2(rfidTagInfo, 5);
				if (isWrite) {
					isIssued = 0;// issued
				} else {
					isIssued = 1;// Not Issued;
				}
			}
		} else {
			isIssued = 2;// reader Not Connected
		}
		return isIssued;
	}

	public com.ipssi.gen.utils.Pair<Integer, String> getTagEPC() throws RFIDException {
		int isIssued = Misc.getUndefInt();
		String epc = "";
		if (RFIDMaster.getDesktopReader() != null) {
			ArrayList<String> tags = RFIDMaster.getDesktopReader().getRFIDTagList();
			int size = tags == null ? 0 : tags.size();

			if (size == 1) {
				epc = tags.get(0);
				if (epc != null && epc.length() > 0) {
					isIssued = 0;// issued
				} else {
					isIssued = 1;// Not Issued;
				}
			} else if (size == 0) {
				isIssued = 4;// multiple tags
			} else {
				isIssued = 3;
			}
		} else {
			isIssued = 2;// reader Not Connected
		}
		return new com.ipssi.gen.utils.Pair<Integer, String>(isIssued, epc);
	}

}
