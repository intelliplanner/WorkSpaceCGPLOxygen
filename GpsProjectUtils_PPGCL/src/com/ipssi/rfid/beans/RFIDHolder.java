package com.ipssi.rfid.beans;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.common.ds.rule.GpsPlusViolations;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.db.Criteria;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;
import com.ipssi.rfid.processor.Utils;

@Table("rfid_handheld_log")
public class RFIDHolder {
	public static final byte CARD_WRITE_START = 0x10;
	public static final byte CARD_WRITE_COMPLETE = 0x20;
	public static final byte CARD_CLEAR = 0x00;

	public static final int RF_DATA_EXIST = 0x01;
	public static final int RF_DATA_VALID = 0x02;
	public static final int RF_DATA_UNIQUE = 0x04;
	public static final int RF_DATA_USEABLE = 0x07;

	private int formatDelemiter;// value 0-3

	private int avgTare;// value 0-511

	private int avgGross;// value 0-511

	@Column("vehicle_name")
	private String vehicleName;// 10 character

	@Column("mines")
	private int minesId;// value 0-63

	@Column("device_id")
	private int deviceId;// value 0-63

	@Column("transporter")
	private int transporterId;// value 0-63

	@Column("record_time")
	private Date datetime;// value 0-4294967295

	@Column("do_id")
	private int doId;// value 0-4294967295

	@KEY
	@PRIMARY_KEY
	@Column("id")
	private int generatedId;// value 0-4294967295

	@Column("record_id")
	private int id;

	@Column("challan_id")
	private String challanId;// 12 char

	@Column("lr_id")
	private String LRID;// 12 char

	@Column("grade")
	private int grade;// value 0-63

	@Column("material")
	private int material;// value 0-3

	@Column("load_tare")
	private int loadTare;// value 8191

	@Column("load_gross")
	private int loadGross;// value 8191

	@Column("pre_mines")
	private int preMinesId;// value 0-63

	@Column("pre_device_id")
	private int preDeviceId;// value 0-63

	@Column("pre_record_id")
	private int preRecordId;// value 0-4294967295

	@Column("pre_challan_id")
	private String preChallanId;// 12 char

	@Column("write_status")
	private boolean validityFlag = false;// value 0-255

	@Column("vehicle_id")
	private int vehicleId = Misc.getUndefInt();

	@Column("epc_id")
	private String epcId;

	@Column("is_data")
	private boolean isDataOnCard = false;

	@Column("updated_on")
	private Date updatedOn;

	@Column("created_on")
	private Date createdOn;

	@Column("updated_by")
	private int updatedBy = Misc.getUndefInt();

	@Column("lr_date")
	private Date lrDate;

	@Column("wb_challan_no")
	private String wbChallanNo;

	private int rfidId = 0;
	private int state = 0;
	private boolean isInitialized = false;

	private int refTPRId = Misc.getUndefInt();

	public RFIDHolder() {

	}

	public RFIDHolder(String vehicleName, RFIDTagInfo tagInfo) {
		this.vehicleName = vehicleName;
		loadDataHolder(tagInfo, this);
	}

	public static String getVehicleByTag(RFIDTagInfo tagInfo) {
		if (tagInfo == null || tagInfo.userData == null
				|| tagInfo.userData.length <= 0)
			return null;
		String retval = null;
		try {

			String binaryString = Utils
					.getBinaryStrFromByteArray(tagInfo.userData);
			String del = Utils.DecodingBinaryToSpecial(binaryString.substring(
					0, 2));
			if (del.equalsIgnoreCase("@"))
				retval = Utils.Decoding6BinaryToString(
						binaryString.substring(2, 62)).replaceAll("@", "")
						.trim();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return retval;
	}

	public void loadDataHolder(RFIDTagInfo tagInfo, RFIDHolder dataHolder) {
		if (tagInfo == null || dataHolder == null)
			return;
		try {
			if (tagInfo != null && tagInfo.userData != null) {
				byte[] data = tagInfo.userData;
				byte[] epc = tagInfo.epcId;
				if (epc != null && epc.length > 0) {
					epcId = Utils.ByteArrayToHexString(epc);
				}
				String binaryString = Utils.getBinaryStrFromByteArray(data);
				String del = Utils.DecodingBinaryToSpecial(binaryString
						.substring(0, 2));
				if (del.equalsIgnoreCase("@")) {
					dataHolder.vehicleName = Utils.Decoding6BinaryToString(
							binaryString.substring(2, 62)).replaceAll("@", "")
							.trim();
					dataHolder.avgTare = Utils.DecodingBinaryToInt(binaryString
							.substring(62, 71), 9);
					dataHolder.avgGross = Utils.DecodingBinaryToInt(
							binaryString.substring(71, 80), 9);
					dataHolder.minesId = Utils.DecodingBinaryToInt(binaryString
							.substring(80, 86), 6);
					dataHolder.deviceId = Utils.DecodingBinaryToInt(
							binaryString.substring(86, 92), 6);
					dataHolder.transporterId = Utils.DecodingBinaryToInt(
							binaryString.substring(92, 98), 6);
					dataHolder.datetime = Utils.getDateTime(Utils
							.DecodingBinaryToInt(binaryString
									.substring(98, 130), 32));
					dataHolder.doId = Utils.DecodingBinaryToInt(binaryString
							.substring(130, 162), 32);
					dataHolder.id = Utils.DecodingBinaryToInt(binaryString
							.substring(162, 194), 32);
					dataHolder.challanId = Utils.Decoding6BinaryToString(
							binaryString.substring(194, 266)).replaceAll("@",
							"").trim();

					dataHolder.LRID = Utils.Decoding6BinaryToString(
							binaryString.substring(266, 338)).replaceAll("@",
							"").trim();
					dataHolder.grade = Utils.DecodingBinaryToInt(binaryString
							.substring(338, 344), 6);
					dataHolder.material = Utils.DecodingBinaryToInt(
							binaryString.substring(344, 346), 2);
					// dataHolder.loadTare =
					// Utils.DecodingBinaryToInt(binaryString.substring(346,
					// 359), 13);
					// dataHolder.loadGross =
					// Utils.DecodingBinaryToInt(binaryString.substring(359,
					// 372), 13);
					dataHolder.loadTare = tagInfo.loadTare;
					dataHolder.loadGross = tagInfo.loadGross;
					dataHolder.wbChallanNo = tagInfo.wbChallanNo;
					dataHolder.preMinesId = Utils.DecodingBinaryToInt(
							binaryString.substring(372, 378), 6);
					dataHolder.preDeviceId = Utils.DecodingBinaryToInt(
							binaryString.substring(378, 384), 6);
					dataHolder.preRecordId = Utils.DecodingBinaryToInt(
							binaryString.substring(384, 416), 32);
					dataHolder.preChallanId = Utils.Decoding6BinaryToString(
							binaryString.substring(416, 488)).replaceAll("@",
							"").trim();
					dataHolder.isDataOnCard = Utils.DecodingBinaryToInt(
							binaryString.substring(488, 496), 8) == 1;
					byte a = Byte
							.parseByte(binaryString.substring(496, 504), 2);
					byte b = Byte
							.parseByte(binaryString.substring(504, 512), 2);
					if ((a == CARD_WRITE_COMPLETE)
							&& (b == CARD_WRITE_COMPLETE))
						dataHolder.validityFlag = true;
					// if(loadTare > 0)
					// loadTare = loadTare * 10;
					// if(loadGross > 0)
					// loadGross = loadGross * 10;
					dataHolder.isInitialized = true;
					dataHolder.setLrDate(dataHolder.getDatetime());
				} else if (del.equalsIgnoreCase("_")) {
					dataHolder.vehicleName = Utils.Decoding6BinaryToString(
							binaryString.substring(2, 62)).replaceAll("@", "")
							.trim();
					dataHolder.avgTare = Utils.DecodingBinaryToInt(binaryString
							.substring(62, 71), 9);
					dataHolder.avgGross = Utils.DecodingBinaryToInt(
							binaryString.substring(71, 80), 9);

					dataHolder.minesId = Utils.DecodingBinaryToInt(binaryString
							.substring(80, 86), 6);
					dataHolder.deviceId = Utils.DecodingBinaryToInt(
							binaryString.substring(86, 92), 6);
					dataHolder.transporterId = Utils.DecodingBinaryToInt(
							binaryString.substring(92, 98), 6);
					dataHolder.datetime = Utils.getDateTime(Utils
							.DecodingBinaryToInt(binaryString
									.substring(98, 130), 32));
					dataHolder.doId = Utils.DecodingBinaryToInt(binaryString
							.substring(130, 162), 32);
					dataHolder.id = Utils.DecodingBinaryToInt(binaryString
							.substring(162, 194), 32);
					dataHolder.challanId = Utils.Decoding6BinaryToString(
							binaryString.substring(194, 266)).replaceAll("@",
							"").trim();

					dataHolder.LRID = Utils.Decoding6BinaryToString(
							binaryString.substring(266, 338)).replaceAll("@",
							"").trim();
					dataHolder.grade = Utils.DecodingBinaryToInt(binaryString
							.substring(338, 344), 6);
					dataHolder.material = Utils.DecodingBinaryToInt(
							binaryString.substring(344, 346), 2);
					dataHolder.loadTare = Utils.DecodingBinaryToInt(
							binaryString.substring(346, 362), 16);
					dataHolder.loadGross = Utils.DecodingBinaryToInt(
							binaryString.substring(362, 378), 16);
					dataHolder.preMinesId = Utils.DecodingBinaryToInt(
							binaryString.substring(378, 384), 6);
					dataHolder.preDeviceId = Utils.DecodingBinaryToInt(
							binaryString.substring(384, 390), 6);
					dataHolder.preRecordId = Utils.DecodingBinaryToInt(
							binaryString.substring(390, 416), 26);
					dataHolder.preChallanId = Utils.Decoding6BinaryToString(
							binaryString.substring(416, 488)).replaceAll("@",
							"").trim();// wb challan no
					dataHolder.isDataOnCard = Utils.DecodingBinaryToInt(
							binaryString.substring(488, 496), 8) == 1;
					byte a = Byte
							.parseByte(binaryString.substring(496, 504), 2);
					byte b = Byte
							.parseByte(binaryString.substring(504, 512), 2);
					if ((a == CARD_WRITE_COMPLETE)
							&& (b == CARD_WRITE_COMPLETE))
						dataHolder.validityFlag = true;

					dataHolder.isInitialized = true;
					dataHolder.setLrDate(dataHolder.getDatetime());
				} else if (del.equalsIgnoreCase("#")) {
					dataHolder.vehicleName = Utils.Decoding6BinaryToString(
							binaryString.substring(2, 62)).replaceAll("@", "")
							.trim();
					dataHolder.avgTare = Utils.DecodingBinaryToInt(binaryString
							.substring(62, 71), 9);
					dataHolder.avgGross = Utils.DecodingBinaryToInt(
							binaryString.substring(71, 80), 9);
					dataHolder.minesId = Utils.DecodingBinaryToInt(binaryString
							.substring(80, 86), 6);
					dataHolder.deviceId = Utils.DecodingBinaryToInt(
							binaryString.substring(86, 92), 6);
					dataHolder.transporterId = Utils.DecodingBinaryToInt(
							binaryString.substring(92, 98), 6);
					dataHolder.datetime = Utils.getDateTime(Utils
							.DecodingBinaryToInt(binaryString
									.substring(98, 130), 32));
					System.out.println("holder date time ->  " + dataHolder.datetime);
					dataHolder.doId = Utils.DecodingBinaryToInt(binaryString
							.substring(130, 162), 32);
					dataHolder.createdOn = Utils.getDateTime(Utils
							.DecodingBinaryToInt(binaryString.substring(162,
									194), 32));
					System.out.println("holder createdOn time ->  " + dataHolder.createdOn);
					dataHolder.challanId = Utils.Decoding6BinaryToString(
							binaryString.substring(194, 266)).replaceAll("@",
							"").trim();

					dataHolder.LRID = Utils.Decoding6BinaryToString(
							binaryString.substring(266, 338)).replaceAll("@",
							"").trim();
					System.out.println("holder date time ->  " + dataHolder.LRID);
					dataHolder.grade = Utils.DecodingBinaryToInt(binaryString
							.substring(338, 344), 6);
					dataHolder.material = Utils.DecodingBinaryToInt(
							binaryString.substring(344, 346), 2);
					// dataHolder.loadTare =
					// Utils.DecodingBinaryToInt(binaryString.substring(346,
					// 359), 13);
					// dataHolder.loadGross =
					// Utils.DecodingBinaryToInt(binaryString.substring(359,
					// 372), 13);
					dataHolder.loadTare = tagInfo.loadTare;
					dataHolder.loadGross = tagInfo.loadGross;
					dataHolder.wbChallanNo = tagInfo.wbChallanNo;
					dataHolder.preMinesId = Utils.DecodingBinaryToInt(
							binaryString.substring(372, 378), 6);
					dataHolder.preDeviceId = Utils.DecodingBinaryToInt(
							binaryString.substring(378, 384), 6);
					dataHolder.preRecordId = Utils.DecodingBinaryToInt(
							binaryString.substring(384, 416), 32);
					dataHolder.preChallanId = Utils.Decoding6BinaryToString(
							binaryString.substring(416, 488)).replaceAll("@",
							"").trim();
					dataHolder.isDataOnCard = Utils.DecodingBinaryToInt(
							binaryString.substring(488, 496), 8) == 1;
					byte a = Byte
							.parseByte(binaryString.substring(496, 504), 2);
					byte b = Byte
							.parseByte(binaryString.substring(504, 512), 2);
					if ((a == CARD_WRITE_COMPLETE)
							&& (b == CARD_WRITE_COMPLETE))
						dataHolder.validityFlag = true;
					dataHolder.isInitialized = true;
					dataHolder.setLrDate(dataHolder.getDatetime());
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public RFIDTagInfo createTag(int readerId) {
		RFIDTagInfo tagInfo = null;
		StringBuilder binary = null;
		if (vehicleName != null) {
			binary = new StringBuilder();
			tagInfo = new RFIDTagInfo();
			if (vehicleName != null && vehicleName.length() > 0) {
				tagInfo.epcId = Utils.HexStringToByteArray(epcId);
				tagInfo.tagId = null;
				binary.append(Utils.EncodingSpecialToBinary("_"));// 2
				binary.append(Utils.Encoding6StringToBinary(Utils.getStdString(
						vehicleName, 10)));// 60
				binary.append(Utils.EncodingIntToBinary(avgTare, 9));// 9
				binary.append(Utils.EncodingIntToBinary(avgGross, 9));// 9
				binary.append(Utils.EncodingIntToBinary(minesId, 6));// 6
				binary.append(Utils.EncodingIntToBinary(deviceId, 6));// 6
				binary.append(Utils.EncodingIntToBinary(transporterId, 6));// 6
				binary.append(Utils.EncodingIntToBinary(Utils
						.getDateTimeLong(datetime), 32));// 32
				binary.append(Utils.EncodingIntToBinary(doId, 32));// 32
				binary.append(Utils.EncodingIntToBinary(id, 32));// 32
				binary.append(Utils.Encoding6StringToBinary(Utils.getStdString(
						challanId, 12)));// 72
				binary.append(Utils.Encoding6StringToBinary(Utils.getStdString(
						LRID, 12)));// 72
				binary.append(Utils.EncodingIntToBinary(grade, 6));// 6
				binary.append(Utils.EncodingIntToBinary(material, 2));// 2
				binary.append(Utils.EncodingIntToBinary(
						(loadTare > 0 ? loadTare : 0), 16));// 13
				binary.append(Utils.EncodingIntToBinary(
						(loadGross > 0 ? loadGross : 0), 16));// 13
				binary.append(Utils.EncodingIntToBinary(preMinesId, 6));// 6
				binary.append(Utils.EncodingIntToBinary(preDeviceId, 6));// 6
				binary.append(Utils.EncodingIntToBinary(preRecordId, 26));// 32
				binary.append(Utils.Encoding6StringToBinary(Utils.getStdString(
						preChallanId, 12)));// 72
				binary.append(Utils
						.EncodingIntToBinary(isDataOnCard ? 1 : 0, 8));// 8
				tagInfo.userData = Utils.GetBytesFromBinaryString(binary
						.toString());
			}
		}

		return tagInfo;
	}

	public void clear(int readerId) {
	}

	public int getFormatDelemiter() {
		return formatDelemiter;
	}

	public String getVehicleName() {
		return vehicleName;
	}

	public int getAvgTare() {
		return avgTare;
	}

	public int getAvgGross() {
		return avgGross;
	}

	public int getMinesId() {
		return minesId;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public int getTransporterId() {
		return transporterId;
	}

	public Date getDatetime() {
		return datetime;
	}

	public int getDoId() {
		return doId;
	}

	public int getId() {
		return id;
	}

	public String getChallanId() {
		return challanId;
	}

	public String getLRID() {
		return LRID;
	}

	public int getGrade() {
		return grade;
	}

	public int getMaterial() {
		return material;
	}

	public String getWbChallanNo() {
		return wbChallanNo;
	}

	public void setWbChallanNo(String wbChallanNo) {
		this.wbChallanNo = wbChallanNo;
	}

	public int getPreMinesId() {
		return preMinesId;
	}

	public int getPreDeviceId() {
		return preDeviceId;
	}

	public int getPreRecordId() {
		return preRecordId;
	}

	public String getPreChallanId() {
		return preChallanId;
	}

	public boolean getValidityFlag() {
		return validityFlag;
	}

	public int getVehicleId() {
		return vehicleId;
	}

	public String getEpcId() {
		return epcId;
	}

	public int getRfidId() {
		return rfidId;
	}

	public int getState() {
		return state;
	}

	public boolean isInitialized() {
		return isInitialized;
	}

	public boolean isDataOnCard() {
		return isDataOnCard;
	}

	public void setFormatDelemiter(int formatDelemiter) {
		this.formatDelemiter = formatDelemiter;
	}

	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}

	public void setAvgTare(int avgTare) {
		this.avgTare = avgTare;
	}

	public void setAvgGross(int avgGross) {
		this.avgGross = avgGross;
	}

	public void setMinesId(int minesId) {
		this.minesId = minesId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public void setTransporterId(int transporterId) {
		this.transporterId = transporterId;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

	public void setDoId(int doId) {
		this.doId = doId;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setChallanId(String challanId) {
		this.challanId = challanId;
	}

	public void setLRID(String lRID) {
		LRID = lRID;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public void setMaterial(int material) {
		this.material = material;
	}

	public void setPreMinesId(int preMinesId) {
		this.preMinesId = preMinesId;
	}

	public void setPreDeviceId(int preDeviceId) {
		this.preDeviceId = preDeviceId;
	}

	public void setPreRecordId(int preRecordId) {
		this.preRecordId = preRecordId;
	}

	public void setPreChallanId(String preChallanId) {
		this.preChallanId = preChallanId;
	}

	public void setValidityFlag(boolean validityFlag) {
		this.validityFlag = validityFlag;
	}

	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	public void setEpcId(String epcId) {
		this.epcId = epcId;
	}

	public void setRfidId(int rfidId) {
		this.rfidId = rfidId;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	public void setDataOnCard(boolean isDataOnCard) {
		this.isDataOnCard = isDataOnCard;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public int getUpdatedBy() {
		return updatedBy;
	}

	public Date getLrDate() {
		return lrDate;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}

	public void setLrDate(Date lrDate) {
		this.lrDate = lrDate;
	}

	public String getRecordKey() {
		return (deviceId + "@"
				+ (datetime != null ? datetime.getTime() : Misc.getUndefInt())
				+ "@" + id);
	}

	public TPRecord getMatchingLeftTprId(Connection conn) {
		try {
			TPRecord tpr = new TPRecord();
			// tpr.setStatus(Misc.getUndefInt());//to be checked
			Criteria cr = new Criteria(TPRecord.class);
			cr.setWhrClause(" (challan_no like '" + challanId
					+ "' and mines_id=" + minesId + " )  and tpr_id not in ("
					+ refTPRId + ")");
			cr.setOrderByClause("tp_record.tpr_create_date");
			cr.setDesc(true);
			ArrayList<Object> list = (ArrayList<Object>) RFIDMasterDao.select(
					conn, tpr, cr);
			for (int i = 0, is = list == null ? 0 : list.size(); i < is; i++) {
				int matchedTPRId = ((TPRecord) list.get(i)).getTprId();
				if (!Misc.isUndef(matchedTPRId) && refTPRId != matchedTPRId) {
					return ((TPRecord) list.get(i));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public TPRecord getMatchingRightTprId(Connection conn) {
		try {
			TPRecord tpr = new TPRecord();
			// tpr.setStatus(Misc.getUndefInt());//to be checked
			Criteria cr = new Criteria(TPRecord.class);
			cr.setWhrClause(" (rf_record_key like '" + getRecordKey()
					+ "' )  and tpr_id not in (" + refTPRId + ") ");
			cr.setOrderByClause("tp_record.tpr_create_date");
			cr.setDesc(true);
			ArrayList<Object> list = (ArrayList<Object>) RFIDMasterDao.select(
					conn, tpr, cr);
			for (int i = 0, is = list == null ? 0 : list.size(); i < is; i++) {
				int matchedTPRId = ((TPRecord) list.get(i)).getTprId();
				if (!Misc.isUndef(matchedTPRId) && refTPRId != matchedTPRId) {
					return ((TPRecord) list.get(i));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public int getConflictingTPRId(Connection conn) {
		try {
			TPRecord tpr = new TPRecord();
			// tpr.setStatus(Misc.getUndefInt());//to be checked
			Criteria cr = new Criteria(TPRecord.class);
			cr.setWhrClause(" (challan_no like '" + challanId
					+ "' and mines_id=" + minesId + " )  and tpr_id not in ("
					+ refTPRId + ") ");
			cr.setOrderByClause("tp_record.tpr_create_date");
			cr.setDesc(true);
			ArrayList<Object> list = (ArrayList<Object>) RFIDMasterDao.select(
					conn, tpr, cr);
			if (list != null && list.size() > 0) {
				if (Misc.isUndef(refTPRId)) {
					return ((TPRecord) list.get(0)).getTprId();
				} else {
					for (int i = 0, is = list == null ? 0 : list.size(); i < is; i++) {
						int matchedTPRId = ((TPRecord) list.get(i)).getTprId();
						if (!Misc.isUndef(matchedTPRId)
								&& refTPRId != matchedTPRId) {
							return matchedTPRId;
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return Misc.getUndefInt();
	}

	/*
	 * public int getMatchingTprId(Connection conn){ try{ TPRecord tpr = new
	 * TPRecord(); //tpr.setStatus(Misc.getUndefInt());//to be checked Criteria
	 * cr = new Criteria(TPRecord.class);
	 * cr.setWhrClause(" ((challan_no like '"+
	 * challanId+"' and mines_id="+minesId
	 * +" ) or (rf_challan_id like '"+challanId
	 * +"' and rf_mines_id="+minesId+" )) and tpr_id not in ("+refTPRId+") ");
	 * cr.setOrderByClause("tp_record.tpr_create_date"); cr.setDesc(true);
	 * ArrayList<Object> list = (ArrayList<Object>) RFIDMasterDao.select(conn,
	 * tpr,cr); if(list != null && list.size() > 0){ if(Misc.isUndef(refTPRId)){
	 * return ((TPRecord)list.get(0)).getTprId(); }else{ for(int i=0,is = list
	 * == null ? 0 : list.size(); i< is;i++){ int matchedTPRId =
	 * ((TPRecord)list.get(i)).getTprId(); if(!Misc.isUndef(matchedTPRId) &&
	 * refTPRId != matchedTPRId ){ return matchedTPRId; } } } } }catch(Exception
	 * ex){ ex.printStackTrace(); } return Misc.getUndefInt(); }
	 */

	public int isDataUseful(Connection conn, boolean isWeb) {
		return (isDataOnCard ? RF_DATA_EXIST : 0)
				| ((validityFlag || isWeb) ? RF_DATA_VALID : 0)
				| ((Misc.isUndef(getConflictingTPRId(conn)) || true) ? RF_DATA_UNIQUE
						: 0);
	}

	/*
	 * public boolean isDataUseful(Connection conn,boolean isWeb){ return
	 * isDataOnCard && (validityFlag || isWeb) &&
	 * (Misc.isUndef(getMatchingTprId(conn))); }
	 */
	public boolean isDataUseful() {
		return isDataOnCard && validityFlag;
	}

	public boolean isEquals(String s1, String s2) {
		return (s1 == null && s2 == null)
				|| (s1 != null && s2 != null && s1.trim().equalsIgnoreCase(
						s2.trim()));
	}

	public boolean isEquals(Date t1, Date t2) {
		return (t1 == null && t2 == null)
				|| ((t1 != null && t2 != null) && ((t1.getTime() > t2.getTime()) ? (t1
						.getTime() - t2.getTime()) <= 60 * 60 * 1000
						: (t2.getTime() - t1.getTime()) <= 60 * 60 * 1000));
	}

	public static boolean isChallanDateEquals(Date date1, Date date2) {
		if (date1 == null || date2 == null)
			return false;
		long milliInDay = 24 * 60 * 60 * 1000;
		long milliFor11_30 = (47 / 2) * 60 * 60 * 1000;
		long d1 = date1.getTime() / milliInDay;
		long d2 = date2.getTime() / milliInDay;
		long t1 = date1.getTime() % milliInDay;
		long t2 = date2.getTime() % milliInDay;
		return (d1 == d2) || ((d1 - d2) == 1 && t2 > milliFor11_30);// || (
																	// (d2-d1)
																	// == 1 &&
																	// t1 >
																	// milliFor11_30);
	}

	public boolean isAllowedToFillTprRHS(Connection conn, int minesId,
			long comboStart, StringBuilder sb) {
		if (Misc.isUndef(minesId) || Misc.isUndef(comboStart) || conn == null
				|| datetime == null)
			return false;
		try {
			Pair<Double, Double> tatInfo = GpsPlusViolations.getTatSpecInfo(
					conn, minesId);
			long tatTime = tatInfo != null && !Misc.isUndef(tatInfo.second) ? (long) (tatInfo.second * 60 * 60 * 1000)
					: (long) GpsPlusViolations.g_defaultTripDurHr * 60 * 60 * 1000;
			if (sb != null) {
				sb.append("tatInfo:" + tatTime + "\n");
			}
			return !Misc.isUndef(tatTime)
					&& ((comboStart - datetime.getTime()) < tatTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isMergeable(RFIDHolder right) {
		return (right != null && this.vehicleId == right.vehicleId
				&& this.minesId == right.getMinesId() && isChallanDateEquals(
				this.datetime, right.getDatetime()))
				&& (isEquals(this.challanId, right.getChallanId())
				// ||
				// isEquals(this.LRID, right.getLRID())
				);
	}

	public boolean isMatched(RFIDHolder right) {

		return (right != null && isEquals(this.challanId, right.getChallanId())
				&& isEquals(this.LRID, right.getLRID())
				&& this.minesId == right.getMinesId()
				&& this.transporterId == right.getTransporterId()
				&& isEquals(this.datetime, right.getDatetime())
				&& this.doId == right.getDoId() && grade == right.getGrade()
				&& loadTare == right.getLoadTare() && loadGross == right
				.getLoadGross());
	}

	public boolean equalsIgnoreChallanNumber(RFIDHolder right) {

		return (
		// isEquals(this.vehicleName, right.getVehicleName())
		right != null && this.minesId == right.getMinesId()
				&& this.transporterId == right.getTransporterId()
				&& isEquals(this.datetime, right.getDatetime())
				&& this.doId == right.getDoId() && grade == right.getGrade()
				&& loadTare == right.getLoadTare() && loadGross == right
				.getLoadGross());
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n@@@RFIDHolder@@@").append("\n");
		sb.append("[\n");
		sb.append("Vehicle NAME: " + getVehicleName()).append("\n");
		sb.append("Avg Tare : " + getAvgTare()).append("\n");
		sb.append("Avg Gross:" + getAvgGross()).append("\n");
		sb.append("deviceId : " + getDeviceId()).append("\n");
		sb.append("MinesId: " + getMinesId()).append("\n");
		sb.append("doId : " + getDoId()).append("\n");
		sb.append("transporterId : " + getTransporterId()).append("\n");
		sb.append("GradeId : " + getGrade()).append("\n");
		sb.append("challan Id: " + getChallanId()).append("\n");
		sb.append("LRID : " + getLRID()).append("\n");
		sb.append("load Tare : " + getLoadTare()).append("\n");
		sb.append("load Gross : " + getLoadGross()).append("\n");
		sb.append("Is Data : " + (isDataOnCard() ? "YES" : "NO")).append("\n");
		sb.append("Data Valid: " + (getValidityFlag() ? "YES" : "NO")).append(
				"\n");
		if (getDatetime() != null) {
			sb.append("Challan Date : " + getDatetime().toLocaleString())
					.append("\n");
		}
		sb.append("]\n");
		return sb.toString();
	}

	public void printData() {
		System.out.println("Vehicle NAME: " + getVehicleName());
		System.out.println("Avg Tare : " + getAvgTare());
		System.out.println("Avg Gross:" + getAvgGross());
		System.out.println("transporterId : " + getTransporterId());
		System.out.println("deviceId : " + getDeviceId());
		System.out.println("MinesId: " + getMinesId());
		System.out.println("challan Id: " + getChallanId());
		System.out.println("LRID : " + getLRID());
		System.out.println("load Tare : " + getLoadTare());
		System.out.println("load Gross : " + getLoadGross());
		System.out.println("Is Data : " + (isDataOnCard() ? "YES" : "NO"));
		System.out.println("Data Valid: " + (getValidityFlag() ? "YES" : "NO"));
		if (getDatetime() != null) {
			System.out.println("Challan Date : "
					+ getDatetime().toLocaleString());
		}
	}

	public static boolean isCardValid(RFIDTagInfo tagInfo) {
		boolean retval = false;
		if (tagInfo == null)
			return false;
		try {
			if (tagInfo != null && tagInfo.userData != null) {
				byte[] data = tagInfo.userData;
				String binaryString = Utils.getBinaryStrFromByteArray(data);
				String del = Utils.DecodingBinaryToSpecial(binaryString
						.substring(0, 2));
				retval = del.equalsIgnoreCase("@");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return retval;
	}

	public int getGeneratedId() {
		return generatedId;
	}

	public void setGeneratedId(int generatedId) {
		this.generatedId = generatedId;
	}

	public int getRefTPRId() {
		return refTPRId;
	}

	public void setRefTPRId(int refTPRId) {
		this.refTPRId = refTPRId;
	}

	public void changeWtInkg() {
		// change wt. in tons
		if (Misc.isUndef(this.getLoadGross()) && this.getLoadGross() < 50)
			this.setLoadGross(this.getLoadGross() * 1000);

		if (Misc.isUndef(this.getLoadTare()) && this.getLoadTare() < 50)
			this.setLoadTare(this.getLoadTare() * 1000);
	}

	public void changeWtInTons() {
		if (this.getLoadGross() > 8000)
			this.setLoadGross(this.getLoadGross() / 1000);

		if (this.getLoadTare() > 8000)
			this.setLoadTare(this.getLoadTare() / 1000);
	}

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-mm hh:mm");

	public RFIDHolder(int vehicleId, String vehicleName, int minesId,
			String dateTime, String challanNo, String lrNo, int transporter,
			int grade, boolean isDatauseful) {
		this.vehicleName = vehicleName;
		this.vehicleId = vehicleId;
		this.minesId = minesId;
		this.datetime = getDateFromStr(dateTime);
		this.challanId = challanNo;
		this.LRID = lrNo;
		this.transporterId = transporter;
		this.grade = grade;
		this.validityFlag = isDatauseful;
		this.isDataOnCard = isDatauseful;
	}

	public Date getDateFromStr(String dateTime) {
		if (Utils.isNull(dateTime))
			return null;
		try {
			return sdf.parse(dateTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public int getLoadTare() {
		return loadTare;
	}

	public void setLoadTare(int loadTare) {
		this.loadTare = loadTare;
	}

	public int getLoadGross() {
		return loadGross;
	}

	public void setLoadGross(int loadGross) {
		this.loadGross = loadGross;
	}

}
