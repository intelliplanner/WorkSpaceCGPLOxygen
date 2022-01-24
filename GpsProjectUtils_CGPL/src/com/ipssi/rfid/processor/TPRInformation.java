package com.ipssi.rfid.processor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.MaterialProcessSeqBean;
import com.ipssi.rfid.beans.ProcessStepProfile;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRWeighmentRecord;
import com.ipssi.rfid.beans.TPRWeighmentRecord.StepType;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.TprChallanData;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.beans.VehicleRFIDInfo;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.Status.TPR;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.db.Criteria;
import com.ipssi.rfid.db.RFIDMasterDao;

public class TPRInformation {

	private static long dependentStationTprThreshold = 30 * 60 * 1000;
	private static long sameStationTprThreshold = 150 * 60 * 1000;
	private static final int newLatestTprWebThreshold = 60 * 60 * 1000;
	private static final String CHALLAN_SERVER_CODE = "1";
	private static final String CHALLAN_LOCAL_CODE = "2";

	// private static final boolean traceToDB = true;

	public static void setSameStationTprThresholdMinutes(long minutes) {
		if (!Misc.isUndef(minutes))
			sameStationTprThreshold = minutes * 60 * 1000;
	}

	public static long getSameStationTprThresholdMinutes() {
		return sameStationTprThreshold;
	}

	public static void setDependentStationTprThresholdMinutes(long minutes) {
		if (!Misc.isUndef(minutes))
			dependentStationTprThreshold = minutes * 60 * 1000;
	}

	public static long getDependentStationTprThresholdMinutes() {
		return dependentStationTprThreshold;
	}

	private static final ArrayList<Pair<Integer, Integer>> processList = new ArrayList<Pair<Integer, Integer>>();
	static {
		processList.add(new Pair<Integer, Integer>(
				Type.WorkStationType.GATE_IN_TYPE,
				Type.WorkStationType.WEIGH_BRIDGE_OUT_TYPE));
		processList.add(new Pair<Integer, Integer>(
				Type.WorkStationType.STONE_TARE_WT_TYPE,
				Type.WorkStationType.STONE_GROSS_WT_TYPE));
		processList.add(new Pair<Integer, Integer>(
				Type.WorkStationType.FLY_ASH_IN_TYPE,
				Type.WorkStationType.FLY_ASH_GROSS_WT_TYPE));
		processList.add(new Pair<Integer, Integer>(
				Type.WorkStationType.FIRST_WEIGHTMENT_TYPE,
				Type.WorkStationType.SECOND_WEIGHTMENT_TYPE));
		processList.add(new Pair<Integer, Integer>(
				Type.WorkStationType.SECL_LOAD_GATE_IN,
				Type.WorkStationType.SECL_UNLOAD_GATE_OUT));// coal Internal
		processList.add(new Pair<Integer, Integer>(
				Type.WorkStationType.SECL_LOAD_GATE_IN,
				Type.WorkStationType.SECL_LOAD_GATE_OUT));// Coal Road
		processList.add(new Pair<Integer, Integer>(
				Type.WorkStationType.SECL_LOAD_GATE_IN,
				Type.WorkStationType.SECL_LOAD_GATE_OUT));// Coal Washery
	}

	public static Pair<Integer, String> getVehicle(Connection conn,
			String epcId, String vehicleName) {
		ArrayList<Object> list = null;
		try {
			Vehicle veh = new Vehicle();
			veh.setStatus(1);
			if (epcId != null && epcId.length() > 0
					&& !epcId.equalsIgnoreCase("E000000000000000000000E0")) {
				veh.setEpcId(epcId);
				list = (ArrayList<Object>) RFIDMasterDao.select(conn, veh);
				if (list != null && list.size() > 0) {
					return new Pair<Integer, String>(((Vehicle) list.get(0))
							.getId(), ((Vehicle) list.get(0)).getVehicleName());
				}
				veh.setEpcId(null);
			}
			if (vehicleName != null && vehicleName.length() > 0) {
				veh.setStdName(CacheTrack.standardizeName(vehicleName));
				// veh.setVehicleName(vehicleName);
				list = (ArrayList<Object>) RFIDMasterDao.select(conn, veh);
				if (list != null && list.size() > 0) {
					return new Pair<Integer, String>(((Vehicle) list.get(0))
							.getId(), ((Vehicle) list.get(0)).getVehicleName());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/*
	 * public static Triple<TPRecord, Integer, Boolean> getLatestTPR(Connection
	 * conn, String vehicleName, RFIDHolder holder,boolean create) { return
	 * getLatestTPR(conn, vehicleName, holder, create,false,Misc.getUndefInt());
	 * } public static Triple<TPRecord, Integer, Boolean>
	 * getLatestTPR(Connection conn, String vehicleName, RFIDHolder
	 * holder,boolean create,boolean isWeb) { return getLatestTPR(conn,
	 * vehicleName, holder, create,isWeb,Misc.getUndefInt()); } public static
	 * Triple<TPRecord, Integer, Boolean> getLatestTPR(Connection conn, String
	 * vehicleName, RFIDHolder holder,boolean create, boolean isWeb, int
	 * workstationTypeId) { return getLatestTPR(conn, vehicleName, holder,
	 * create, isWeb, workstationTypeId, 0); }
	 */
	public static Triple<TPRecord, Integer, Boolean> getLatestNonWeb(
			Connection conn, String vehicleName, RFIDHolder holder,
			int workstationTypeId, int materialCat) throws Exception {
		int vehId = Misc.getUndefInt();
		Pair<Integer, String> vehPair = null;
		try {
			if (holder != null && !Misc.isUndef(holder.getVehicleId())) {
				vehId = holder.getVehicleId();
				System.out.println("HOLDER INFORMATION  : " + vehId);
			} else {
				vehPair = getVehicle(conn, (holder != null ? holder.getEpcId()
						: null), vehicleName); // CacheTrack.VehicleSetup.getSetupByStdName(stdName,
				// conn);
				if (vehPair != null) {
					vehId = vehPair.first;
					vehicleName = vehPair.second;
				}
				System.out.println("GetVehicle INFORMATION  : " + vehId);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return getLatestNonWeb(conn, vehId, holder, vehicleName,
				workstationTypeId, materialCat); // (conn, vehId,
		// holder,vehicleName,create,isWeb,workstationTypeId);
	}

	public static TPRecord createTpr(Connection conn, int vehicleId,
			RFIDHolder holder, String vehicleName, int isLatest, boolean isWeb,
			boolean isDataUseful, boolean updateOnlyRFEntry, int materialCat,
			ProcessStepProfile processStepProfile, String orgName)
			throws Exception {
		return updateTpr(conn, vehicleId, holder, vehicleName, new TPRecord(),
				isLatest, isWeb, isDataUseful, updateOnlyRFEntry, materialCat,
				processStepProfile, orgName);// (conn, vehicleId, holder,
		// vehicleName, new TPRecord(),
		// isLatest, isDataUseful);
	}

	public static TPRecord createTpr(Connection conn, int vehicleId,
			RFIDHolder holder, String vehicleName, int isLatest, boolean isWeb,
			boolean isDataUseful, boolean updateOnlyRFEntry, int materialCat,
			ProcessStepProfile processStepProfile) throws Exception {
		return updateTpr(conn, vehicleId, holder, vehicleName, new TPRecord(),
				isLatest, isWeb, isDataUseful, updateOnlyRFEntry, materialCat,
				processStepProfile);// (conn, vehicleId, holder,
		// vehicleName, new TPRecord(),
		// isLatest, isDataUseful);
	}

	private static TPRecord updateTpr(Connection conn, int vehicleId,
			RFIDHolder holder, String vehicleName, TPRecord tpr, int isLatest,
			boolean isWeb, boolean isDataUseful, boolean updateOnlyRFEntry,
			int materialCat, ProcessStepProfile processStepProfile)
			throws Exception {
		TPRecord retval = null;
		// try{
		retval = tpr;
		if (Misc.isUndef(tpr.getTprId())) {
			retval.setVehicleId(vehicleId);
			retval.setVehicleName(vehicleName);
			retval.setTprCreateDate(new Date());
			retval.setTprStatus(TPR.OPEN);
			if (!Misc.isUndef(isLatest))
				retval.setLatest(isLatest);
			MaterialProcessSeqBean materialProcessSeqBean = new MaterialProcessSeqBean();
			materialProcessSeqBean.setMaterialType(materialCat);
			materialProcessSeqBean.setStatus(1);

			int nextWorkStationType = 1;
			if (processStepProfile != null
					&& processStepProfile.getProcessStep() != null
					&& processStepProfile.getProcessStep().size() > 0) {
				nextWorkStationType = processStepProfile.getProcessStep()
						.get(0);
			} else {
				ArrayList<MaterialProcessSeqBean> matProcessSeqList = (ArrayList<MaterialProcessSeqBean>) RFIDMasterDao
						.getList(conn, materialProcessSeqBean, null);
				for (int i = 0, is = matProcessSeqList == null ? 0
						: matProcessSeqList.size(); i < is; i++) {
					if (matProcessSeqList.get(i).getSeq() == 1) {
						nextWorkStationType = matProcessSeqList.get(i)
								.getWorkstationType();
						break;
					}
				}
			}
			retval.setNextStepType(nextWorkStationType);
		}
		if (isDataUseful) {
			if (!updateOnlyRFEntry) {
				retval.setTransporterId(holder.getTransporterId());
				retval.setMinesId(holder.getMinesId());
				retval.setMaterialGradeId(holder.getGrade());
				// retval.setVehicleName(vehicleName);//
				retval.setChallanDate(holder.getDatetime());
				retval.setChallanNo(holder.getChallanId());
				retval.setLrDate(holder.getLrDate());
				retval.setLrNo(holder.getLRID());
				retval.setLoadTare(holder.getLoadTare());
				retval.setLoadGross(holder.getLoadGross());
				retval.setWbChallanNo(holder.getPreChallanId());
				retval.setDoId(holder.getDoId());
			}
			// update rf fields also
			retval.setRfVehicleName(holder.getVehicleName());
			retval.setRfVehicleId(holder.getVehicleId());
			retval.setRfTransporterId(holder.getTransporterId());
			retval.setRfMinesId(holder.getMinesId());

			retval.setRfChallanId(holder.getChallanId());
			retval.setRfLRDate(holder.getLrDate());
			retval.setRfLRId(holder.getLRID());
			retval.setRfLoadTare(holder.getLoadTare());
			retval.setRfLoadGross(holder.getLoadGross());
			retval.setRfDeviceId(holder.getDeviceId());
			retval.setRfDOId(holder.getDoId());
			retval.setRfRecordId(holder.getId());
			retval.setRfRecordKey(holder.getRecordKey());
			retval.setM_trip_id(holder.getId());
			retval.setIsMergedWithHHTpr(1);
			retval.setHhDeviceId(holder.getDeviceId());

			retval.setRfGrade(holder.getGrade());
			retval.setRfChallanDate(holder.getDatetime());

			if (isWeb)
				retval.setHhTprMergedTime(new Date());
			else
				retval.setRfCardDataMergeTime(new Date());
			retval.setSrcDeviceLogId(holder.getGeneratedId());
			// tpr.set
		}
		if (Misc.isUndef(retval.getMaterialCat())) {
			if (!Misc.isUndef(materialCat)) {
				retval.setMaterialCat(materialCat);
			} else if (holder != null && !Misc.isUndef(holder.getMaterial())) {
				retval.setMaterialCat(holder.getMaterial());
			} else {
				retval.setMaterialCat(Type.TPRMATERIAL.COAL);
			}
		}
		/*
		 * if(retval.getTprId() <= 0){ MaterialProcessSeqBean
		 * materialProcessSeqBean = new MaterialProcessSeqBean();
		 * materialProcessSeqBean.setMaterialType(materialCat);
		 * materialProcessSeqBean.setStatus(1);
		 * ArrayList<MaterialProcessSeqBean> matProcessSeqList =
		 * (ArrayList<MaterialProcessSeqBean>) RFIDMasterDao.getList(conn,
		 * materialProcessSeqBean, null); int nextWorkStationType = 1; for(int
		 * i=0,is=matProcessSeqList == null ? 0 : matProcessSeqList.size();
		 * i<is; i++){ if(matProcessSeqList.get(i).getSeq() == 1){
		 * nextWorkStationType = matProcessSeqList.get(i).getWorkstationType();
		 * break; } } retval.setNextStepType(nextWorkStationType); }
		 */
		if (Misc.isUndef(retval.getTprId()))// review
			retval.setRfAlsoOnCard(holder == null ? 0 : (isDataUseful ? 1 : 2));// 0-no
		// RF
		// Data
		// ,1-useful
		// rf
		// data
		// ,2-invalid
		// rf
		// data
		/*
		 * }catch(Exception ex){ ex.printStackTrace(); }
		 */
		return retval;
	}

	private static TPRecord updateTpr(Connection conn, int vehicleId,
			RFIDHolder holder, String vehicleName, TPRecord tpr, int isLatest,
			boolean isWeb, boolean isDataUseful, boolean updateOnlyRFEntry,
			int materialCat, ProcessStepProfile processStepProfile,
			String orgName) throws Exception {
		TPRecord retval = null;
		// try{
		retval = tpr;
		if (Misc.isUndef(tpr.getTprId())) {
			retval.setVehicleId(vehicleId);
			retval.setVehicleName(vehicleName);
			retval.setTprCreateDate(new Date());
			retval.setTprStatus(TPR.OPEN);
			if (!Misc.isUndef(isLatest))
				retval.setLatest(isLatest);
			MaterialProcessSeqBean materialProcessSeqBean = new MaterialProcessSeqBean();
			materialProcessSeqBean.setMaterialType(materialCat);
			materialProcessSeqBean.setStatus(1);

			int nextWorkStationType = 1;
			if (processStepProfile != null
					&& processStepProfile.getProcessStep() != null
					&& processStepProfile.getProcessStep().size() > 0) {
				nextWorkStationType = processStepProfile.getProcessStep()
						.get(0);
			} else {
				ArrayList<MaterialProcessSeqBean> matProcessSeqList = (ArrayList<MaterialProcessSeqBean>) RFIDMasterDao
						.getList(conn, materialProcessSeqBean, null);
				for (int i = 0, is = matProcessSeqList == null ? 0
						: matProcessSeqList.size(); i < is; i++) {
					if (matProcessSeqList.get(i).getSeq() == 1) {
						nextWorkStationType = matProcessSeqList.get(i)
								.getWorkstationType();
						break;
					}
				}
			}
			retval.setNextStepType(nextWorkStationType);
		}
		if (isDataUseful) {
			if (!updateOnlyRFEntry) {
				retval.setTransporterId(holder.getTransporterId());
				retval.setMinesId(holder.getMinesId());
				retval.setMaterialGradeId(holder.getGrade());
				// retval.setVehicleName(vehicleName);//
				retval.setChallanDate(holder.getDatetime());
				retval.setChallanNo(holder.getChallanId());
				retval.setLrDate(holder.getLrDate());
				retval.setLrNo(holder.getLRID());
				retval.setLoadTare(holder.getLoadTare());
				retval.setLoadGross(holder.getLoadGross());
				retval.setWbChallanNo(holder.getPreChallanId());
				retval.setDoId(holder.getDoId());
			}
			// update rf fields also
			retval.setRfVehicleName(holder.getVehicleName());
			retval.setRfVehicleId(holder.getVehicleId());
			retval.setRfTransporterId(holder.getTransporterId());
			retval.setRfMinesId(holder.getMinesId());

			retval.setRfChallanId(holder.getChallanId());
			retval.setRfLRDate(holder.getLrDate());
			retval.setRfLRId(holder.getLRID());
			retval.setRfLoadTare(holder.getLoadTare());
			retval.setRfLoadGross(holder.getLoadGross());
			retval.setRfDeviceId(holder.getDeviceId());
			retval.setRfDOId(holder.getDoId());
			retval.setRfRecordId(holder.getId());
			retval.setRfRecordKey(holder.getRecordKey());
			retval.setM_trip_id(holder.getId());
			retval.setIsMergedWithHHTpr(1);
			retval.setHhDeviceId(holder.getDeviceId());
			retval.setDestination(holder.getGrade());
			retval.setLoadGrossTime(holder.getCreatedOn());
			retval.setLoadTareTime(holder.getDatetime());
			retval.setRfChallanDate(holder.getDatetime());

			if (isWeb)
				retval.setHhTprMergedTime(new Date());
			else
				retval.setRfCardDataMergeTime(new Date());
			retval.setSrcDeviceLogId(holder.getGeneratedId());
			// tpr.set
		}
		if (Misc.isUndef(retval.getMaterialCat())) {
			if (!Misc.isUndef(materialCat)) {
				retval.setMaterialCat(materialCat);
			} else if (holder != null && !Misc.isUndef(holder.getMaterial())) {
				retval.setMaterialCat(holder.getMaterial());
			} else {
				retval.setMaterialCat(Type.TPRMATERIAL.COAL);
			}
		}
		/*
		 * if(retval.getTprId() <= 0){ MaterialProcessSeqBean
		 * materialProcessSeqBean = new MaterialProcessSeqBean();
		 * materialProcessSeqBean.setMaterialType(materialCat);
		 * materialProcessSeqBean.setStatus(1);
		 * ArrayList<MaterialProcessSeqBean> matProcessSeqList =
		 * (ArrayList<MaterialProcessSeqBean>) RFIDMasterDao.getList(conn,
		 * materialProcessSeqBean, null); int nextWorkStationType = 1; for(int
		 * i=0,is=matProcessSeqList == null ? 0 : matProcessSeqList.size();
		 * i<is; i++){ if(matProcessSeqList.get(i).getSeq() == 1){
		 * nextWorkStationType = matProcessSeqList.get(i).getWorkstationType();
		 * break; } } retval.setNextStepType(nextWorkStationType); }
		 */
		if (Misc.isUndef(retval.getTprId()))// review
			retval.setRfAlsoOnCard(holder == null ? 0 : (isDataUseful ? 1 : 2));// 0-no
		// RF
		// Data
		// ,1-useful
		// rf
		// data
		// ,2-invalid
		// rf
		// data
		/*
		 * }catch(Exception ex){ ex.printStackTrace(); }
		 */
		return retval;
	}

	/*
	 * synchronized public static Triple<TPRecord, Integer, Boolean>
	 * getLatestTPROld(Connection conn, int vehicleId, RFIDHolder holder,String
	 * vehicleName,boolean create,boolean isWeb, int workstationTypeId, int
	 * materialCat) { StringBuilder sb = new StringBuilder(); TPRecord tpr =
	 * null; TPRecord latestTpr = null; int status = Status.VALIDATION.NO_ISSUE;
	 * boolean isHHSync = false; ArrayList<Object> list = null; boolean isFound
	 * = false; int isLatest = Misc.getUndefInt(); boolean isDataUseful = false;
	 * boolean fillEmptyTPR = false; boolean isSameStationProcessing = false;
	 * sb.
	 * append("\n@@@@GET Latest TPR["+vehicleId+","+vehicleName+"]").append("\n"
	 * ); try { if (!Misc.isUndef(vehicleId)) { if(holder != null){ isDataUseful
	 * = holder.isDataUseful(conn, isWeb) == RFIDHolder.RF_DATA_USEABLE;
	 * sb.append("[Holder KEY]:"+holder.getRecordKey()).append("\n");
	 * sb.append("[Holder DATA VALIDITY]:"+isDataUseful).append("\n"); } tpr =
	 * new TPRecord(); tpr.setVehicleId(vehicleId); tpr.setTprStatus(TPR.OPEN);
	 * //tpr.setMaterialCat(materialCat); Criteria cr = new
	 * Criteria(TPRecord.class);cr.setOrderByClause(
	 * "coalesce(tp_record.challan_date,tp_record.tpr_create_date)");
	 * cr.setDesc(true); list = (ArrayList<Object>) RFIDMasterDao.select(conn,
	 * tpr,cr); sb.append("Record Found:"+(list == null ? 0 :
	 * list.size())).append("\n"); for(int i=0,is=list == null ? 0 :
	 * list.size();i<is;i++){ TPRecord tprEntry = (TPRecord) list.get(i);
	 * if(tprEntry.isLatest() == 1){ latestTpr = tprEntry;
	 * sb.append("[Latest Found]:"+latestTpr.getTprId()).append("\n"); }
	 * if(isDataUseful){ RFIDHolder tempRfHolder = tprEntry.getHolderRFData();
	 * RFIDHolder tempManualHolder = tprEntry.getHolderManualData();
	 * sb.append("[Match With Existing TPR]:"+tprEntry.getTprId()).append("\n");
	 * if(tempRfHolder != null ){ sb.append("@@[TPR RF DATA]@@").append("\n");
	 * sb.append("[TPR RF KEY]:"+tempRfHolder.getRecordKey()).append("\n");
	 * sb.append
	 * ("[TPR RF Match]:"+tempRfHolder.equalsIgnoreChallanNumber(holder)
	 * ).append("\n"); sb.append(tempRfHolder.toString());
	 * if(!tempRfHolder.getRecordKey().equalsIgnoreCase(holder.getRecordKey())){
	 * // if(tempRfHolder.equalsIgnoreChallanNumber(holder)){
	 * if(tempRfHolder.isMergeable(holder)){ isFound = true; //update rf and
	 * manual part of tpr only updateTpr(conn, vehicleId, holder, vehicleName,
	 * tprEntry, isLatest,isWeb, isDataUseful,true,materialCat); } }else{
	 * isFound = true; } sb.append("[TPR RF DATA Merge]:"+isFound).append("\n");
	 * }else if(tempManualHolder != null){
	 * sb.append("@@[TPR Manual DATA]@@").append("\n");
	 * sb.append("[TPR Manual KEY]:"
	 * +tempManualHolder.getRecordKey()).append("\n");
	 * sb.append("[TPR Manual Match]:"
	 * +tempManualHolder.equalsIgnoreChallanNumber(holder)).append("\n");
	 * sb.append(tempManualHolder.toString()); //tempManualHolder.printData();
	 * //if(tempManualHolder.equalsIgnoreChallanNumber(holder) ){
	 * if(tempManualHolder.isMergeable(holder)){ isFound = true; //update rf
	 * part of tpr only updateTpr(conn, vehicleId, holder, vehicleName,
	 * tprEntry, isLatest,isWeb, isDataUseful,true,materialCat); }
	 * sb.append("[TPR Manual DATA Merge]:"+isFound).append("\n"); }else
	 * if(latestTpr != null){
	 * //System.out.println("[TPR Challan Data is not present]:"); long rfTime =
	 * holder != null && holder.getDatetime() != null ?
	 * holder.getDatetime().getTime() : Misc.getUndefInt(); long latestTprTime =
	 * latestTpr != null ? (latestTpr.getChallanDate() != null ?
	 * latestTpr.getChallanDate().getTime() :
	 * (latestTpr.getEarliestUnloadGateInEntry() != null ?
	 * latestTpr.getEarliestUnloadGateInEntry().getTime() : Misc.getUndefInt()))
	 * : Misc.getUndefInt(); if(!Misc.isUndef(rfTime) &&
	 * !Misc.isUndef(latestTprTime) && (latestTprTime - rfTime) >= 0 ){ isFound
	 * = true; fillEmptyTPR = true; }
	 * sb.append("[Empty TPR Found]:"+fillEmptyTPR).append("\n"); } } }//end of
	 * searching for existing open record
	 * sb.append("[Latest TPR]:"+latestTpr).append("\n");
	 * sb.append("@@@[toCreateNew]@@@").append("\n");
	 * sb.append("workstationTypeId:"+workstationTypeId).append("\n");
	 * sb.append("create:"+create).append("\n");
	 * sb.append("isDataUseful:"+isDataUseful).append("\n");
	 * sb.append("isFound:"+isFound).append("\n");
	 * sb.append("isWeb:"+isWeb).append("\n"); isSameStationProcessing =
	 * isSameWorkStation(latestTpr, workstationTypeId); boolean toCreateNew =
	 * isWeb ? create && isDataUseful && !isFound : ( ( (latestTpr == null &&
	 * workstationTypeId != Type.WorkStationType.GATE_OUT_TYPE ) ||
	 * (!isSameStationProcessing // &&
	 * isGreaterThanEqualsProcessedOld(latestTpr, workstationTypeId); &&
	 * isGreaterThanEqualsProcessed(latestTpr, workstationTypeId, materialCat))
	 * ) ||(isDataUseful && !isFound) //in case latest is null, has no plant in,
	 * tag has undelivered challan ... we record this //but if this is older
	 * than latest then the data on tag will NOT become latest
	 * ||(workstationTypeId != Type.WorkStationType.GATE_OUT_TYPE &&
	 * workstationTypeId != Type.WorkStationType.REGISTRATION && latestTpr !=
	 * null && latestTpr.getMaterialCat() != materialCat) ) ;
	 * sb.append("toCreateNew:"+toCreateNew).append("\n"); if (toCreateNew) {
	 * TPRecord newTpr = createTpr(conn, vehicleId, holder, vehicleName,
	 * 0,isWeb, isDataUseful,false,materialCat);
	 * sb.append("[New TPR Created]").append("\n"); boolean toMakeLatest =
	 * latestTpr == null || !isWeb; if (!toMakeLatest && isDataUseful) { long
	 * newTprTime = holder != null && holder.getDatetime() != null ?
	 * holder.getDatetime().getTime() : Misc.getUndefInt(); long latestTprTime =
	 * latestTpr != null ? (latestTpr.getChallanDate() != null ?
	 * latestTpr.getChallanDate().getTime() :
	 * (latestTpr.getEarliestUnloadGateInEntry() != null ?
	 * latestTpr.getEarliestUnloadGateInEntry().getTime() : Misc.getUndefInt()))
	 * : Misc.getUndefInt(); if(!Misc.isUndef(newTprTime) &&
	 * !Misc.isUndef(latestTprTime) && (newTprTime - latestTprTime) >=
	 * (newLatestTprWebThreshold) ) toMakeLatest = true; } if (toMakeLatest) {
	 * newTpr.setLatest(1); latestTpr = newTpr; } sb.append(newTpr.toString());
	 * }else{//to avoid empty tpr filling when creating new one if(fillEmptyTPR
	 * && latestTpr != null){ sb.append("@@@[Fill Empty TPR]@@@").append("\n");
	 * updateTpr(conn, vehicleId, holder, vehicleName, latestTpr,
	 * latestTpr.isLatest(),isWeb, isDataUseful,false,materialCat); } } }//if
	 * proper vehicleId } catch (Exception ex) { ex.printStackTrace(); }finally{
	 * if(sb != null) System.out.println(sb.toString()); }
	 * 
	 * return new Triple<TPRecord, Integer, Boolean>(latestTpr, status,
	 * isSameStationProcessing); }
	 */
	synchronized public static TPRecord getLatestTPRForView(Connection conn,
			int vehicleId) {
		TPRecord latestTpr = null;
		try {
			TPRecord tpr = new TPRecord();
			tpr.setVehicleId(vehicleId);
			tpr.setTprStatus(TPR.OPEN);
			tpr.setStatus(Status.ACTIVE);
			Criteria cr = new Criteria(TPRecord.class);
			cr
					.setOrderByClause("coalesce(tp_record.combo_end,tp_record.combo_start,tp_record.tpr_create_date,tp_record.challan_date)");
			cr.setDesc(true);
			ArrayList<Object> list = (ArrayList<Object>) RFIDMasterDao.select(
					conn, tpr, cr);
			for (int i = 0, is = list == null ? 0 : list.size(); i < is; i++) {
				TPRecord tprEntry = (TPRecord) list.get(i);
				if (tprEntry.isLatest() == 1) {
					latestTpr = tprEntry;
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return latestTpr;
	}

	synchronized public static TPRecord getLastCompletedTPRForView(Connection conn,
			int vehicleId) {
		TPRecord latestTpr = null;
		try {
			TPRecord tpr = new TPRecord();
			tpr.setVehicleId(vehicleId);
			tpr.setTprStatus(TPR.CLOSE);
			tpr.setStatus(Status.ACTIVE);
			Criteria cr = new Criteria(TPRecord.class);
			cr.setOrderByClause("coalesce(tp_record.combo_end,tp_record.combo_start,tp_record.tpr_create_date,tp_record.challan_date)");
			cr.setDesc(true);
			cr.setLimit(1);
			ArrayList<Object> list = (ArrayList<Object>) RFIDMasterDao.select(
					conn, tpr, cr);
			for (int i = 0, is = list == null ? 0 : list.size(); i < is; i++) {
				TPRecord tprEntry = (TPRecord) list.get(i);
				if (tprEntry.isLatest() == 1) {
					latestTpr = tprEntry;
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return latestTpr;
	}

	synchronized public static boolean updateDOCurrentStatus(Connection conn,
			int vehicleId, int tprId, String doNumber, String wbCode,
			double currentAllocation, double net, boolean apprvd)
			throws SQLException {
		if (Utils.isNull(wbCode) || Utils.isNull(doNumber) || Misc.isUndef(net))
			return false;
		PreparedStatement ps = null;
		try {
			if (createDoAllocationIfNotExist(conn, doNumber, wbCode,
					currentAllocation, apprvd)) {
				ps = conn
						.prepareStatement("update current_do_status"
								+ (apprvd ? "_apprvd" : "")
								+ " set lifted_qty=(case when lifted_qty is null then 0 else lifted_qty end)+?,"
								+ "	last_lifted_qty=?,last_lifted_vehicle_id=?,last_lifted_tpr_id=?,"
								+ " trips_count=(case when trips_count is null then 0 else trips_count end)+1,last_lifted_on=now() "
								+ " where do_number=?  and wb_code=?");
				Misc.setParamDouble(ps, net, 1);
				Misc.setParamDouble(ps, net, 2);
				Misc.setParamInt(ps, vehicleId, 3);
				Misc.setParamInt(ps, tprId, 4);
				ps.setString(5, doNumber);
				ps.setString(6, wbCode);
				System.out.println(Thread.currentThread().toString() + "["
						+ DBConnectionPool.getPrintableConnectionStr(conn)
						+ "]" + ps.toString());
				ps.executeUpdate();
			}
		} catch (SQLException ex) {
			throw ex;
		} finally {
			Misc.closePS(ps);
		}
		return true;
	}

	synchronized public static boolean createDoAllocationIfNotExist(
			Connection conn, String doNumber, String wbCode,
			double allocationQty, boolean apprvd) throws SQLException {
		if (Utils.isNull(wbCode) || Utils.isNull(doNumber))
			return false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean isExist = false;
		try {
			ps = conn.prepareStatement("select 1 from current_do_status"
					+ (apprvd ? "_apprvd" : "")
					+ " where do_number=? and wb_code=?");
			ps.setString(1, doNumber);
			ps.setString(2, wbCode);
			rs = ps.executeQuery();
			isExist = rs.next();
			Misc.closeRS(rs);
			if (!isExist) {
				ps.clearParameters();
				ps = conn.prepareStatement("insert into current_do_status"
						+ (apprvd ? "_apprvd" : "") + ""
						+ " (do_number, wb_code, current_allocation) "
						+ " values (?, ?, ?) ");
				ps.setString(1, doNumber);
				ps.setString(2, wbCode);
				Misc.setParamDouble(ps, allocationQty, 3);
				System.out.println(Thread.currentThread().toString() + "["
						+ DBConnectionPool.getPrintableConnectionStr(conn)
						+ "]" + ps.toString());
				ps.executeUpdate();
				isExist = true;
			} else {
				Misc.closePS(ps);
			}
		} catch (SQLException ex) {
			throw ex;
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return isExist;
	}

	public static boolean updateDOAlloction(Connection conn, String doNumber,
			String wbCode, double allocationQty, boolean apprvd)
			throws SQLException {
		if (Utils.isNull(wbCode) || Utils.isNull(doNumber)
				|| Misc.isUndef(allocationQty))
			return false;
		PreparedStatement ps = null;
		try {
			if (createDoAllocationIfNotExist(conn, doNumber, wbCode,
					allocationQty, apprvd)) {
				ps = conn
						.prepareStatement("UPDATE current_do_status"
								+ (apprvd ? "_apprvd" : "")
								+ ""
								+ " set current_allocation=? where do_number=? and wb_code=? ");
				Misc.setParamDouble(ps, allocationQty, 1);
				ps.setString(2, doNumber);
				ps.setString(3, wbCode);
				System.out.println(Thread.currentThread().toString() + "["
						+ DBConnectionPool.getPrintableConnectionStr(conn)
						+ "]" + ps.toString());
				ps.executeUpdate();
				Misc.closePS(ps);
			}
		} catch (SQLException ex) {
			throw ex;
		} finally {
			Misc.closePS(ps);
		}
		return true;
	}

	synchronized public static boolean setDOCurrentStatusQtyOnly(
			Connection conn, int vehicleId, int tprId, String doNumber,
			String wbCode, double lifted, double lastLifted,
			double currAllocation, long lastLiftedOn, int totTrips,
			boolean apprvd) throws SQLException {
		if (Utils.isNull(wbCode) || Utils.isNull(doNumber))
			return false;
		PreparedStatement ps = null;
		try {
			if (createDoAllocationIfNotExist(conn, doNumber, wbCode,
					currAllocation, apprvd)) {
				String query = "UPDATE current_do_status"
						+ (apprvd ? "_apprvd" : "")
						+ " set "
						+ " lifted_qty=?, last_lifted_on=?, last_lifted_qty=?, last_lifted_vehicle_id=?, last_lifted_tpr_id=?, "
						+ " trips_count=? where do_number=? and wb_code=?";
				/*
				 * String query =
				 * "update current_do_status"+(apprvd?"_apprvd":""
				 * )+" set lifted_qty=?," +
				 * "	last_lifted_qty=?,last_lifted_vehicle_id=?,last_lifted_tpr_id=?,"
				 * +
				 * " trips_count=(case when trips_count is null then 0 else trips_count end)+1,last_lifted_on=now() "
				 * + " where do_number=?  and wb_code=?";
				 */
				ps = conn.prepareStatement(query);
				Misc.setParamDouble(ps, lifted, 1);
				ps.setTimestamp(2, new Timestamp(Misc.isUndef(lastLiftedOn)
						|| lastLiftedOn < 0 ? System.currentTimeMillis()
						: lastLiftedOn));
				Misc.setParamDouble(ps, lastLifted, 3);
				Misc.setParamInt(ps, vehicleId, 4);
				Misc.setParamInt(ps, tprId, 5);
				Misc.setParamInt(ps, totTrips, 6);
				ps.setString(7, doNumber);
				ps.setString(8, wbCode);
				System.out.println(Thread.currentThread().toString() + "["
						+ DBConnectionPool.getPrintableConnectionStr(conn)
						+ "]" + ps.toString());
				ps.executeUpdate();
			}
		} catch (SQLException ex) {
			throw ex;
		} finally {
			Misc.closePS(ps);
		}
		return true;
	}

	/*
	 * synchronized public static int handleMergeSECL(Connection conn,TPRecord
	 * record, long clockGap) throws Exception{ return handleMergeSECL(conn,
	 * record, clockGap, null); }
	 */
	synchronized public static int handleMergeSECL(Connection conn,
			TPRecord record, long clockGap, Connection localConn,
			int workstationId) throws Exception {
		if (record == null)
			return Misc.getUndefInt();
		String vehicleName = record.getVehicleName();
		try {
			// update vehicleId
			int vehicleId = getVehicleByName(conn, vehicleName);
			if (Misc.isUndef(vehicleId) && localConn != null) {
				// create new
				Vehicle veh = (Vehicle) RFIDMasterDao.get(localConn,
						Vehicle.class, record.getVehicleId());
				vehicleId = handleMergeVehicle(conn, veh, workstationId, 0l);
			}
			record.setRecordSrc(workstationId);
			record.setVehicleId(vehicleId);
			// get merged id if possible;
			String modifiedChallanNo = null;
			int mergedId = getTprIdByChallan(conn, record.getChallanNo(),
					vehicleId);

			System.out.println(Thread.currentThread().toString()
					+ "[MERGE][TPR][ID][local][remote][BY][CHALLAN]["
					+ record.getTprId() + "][" + mergedId + "]");
			if (Misc.isUndef(mergedId)) {// find strict timming based merge tpr

				TPRWeighmentRecord localLoadGateIn = record
						.getWeighmentStepByType(StepType.loadGateIn);
				TPRWeighmentRecord localLoadWBOut = record
						.getWeighmentStepByType(StepType.loadWBOut);
				TPRWeighmentRecord localLoadGateOut = record
						.getWeighmentStepByType(StepType.loadGateOut);
				TPRWeighmentRecord localUnloadGateIn = record
						.getWeighmentStepByType(StepType.unloadGateIn);
				TPRWeighmentRecord localUnloadWBIn = record
						.getWeighmentStepByType(StepType.unloadWBIn);
				TPRWeighmentRecord localUnloadGateOut = record
						.getWeighmentStepByType(StepType.unloadGateOut);
				if (localLoadWBOut != null && !localLoadWBOut.isNull()) {
					mergedId = getTprByStep(conn, vehicleId,
							StepType.loadWBOut, localLoadWBOut.getOutTime(),
							localLoadWBOut.getOutTime());
					modifiedChallanNo = record.getChallanNo();
				}
				if (Misc.isUndef(mergedId) && localLoadGateIn != null
						&& !localLoadGateIn.isNull()
						&& localLoadGateOut != null
						&& !localLoadGateOut.isNull()) {
					mergedId = getTprByStep(conn, vehicleId,
							StepType.loadGateOut, localLoadGateIn.getOutTime(),
							localLoadGateOut.getOutTime());
				}
				if (Misc.isUndef(mergedId) && localUnloadWBIn != null
						&& !localUnloadWBIn.isNull()) {
					mergedId = getTprByStep(conn, vehicleId,
							StepType.unloadWBIn, localUnloadWBIn.getOutTime(),
							localUnloadWBIn.getOutTime());
					modifiedChallanNo = record.getChallanNo();
				}
				if (Misc.isUndef(mergedId) && localUnloadGateIn != null
						&& !localUnloadGateIn.isNull()
						&& localUnloadGateOut != null
						&& !localUnloadGateOut.isNull()) {
					mergedId = getTprByStep(conn, vehicleId,
							StepType.unloadGateOut, localUnloadGateIn
									.getOutTime(), localUnloadGateOut
									.getOutTime());
				}
				System.out.println(Thread.currentThread().toString()
						+ "[MERGE][TPR][ID][local][remote][BY][TIME][MERGE]["
						+ record.getTprId() + "][" + mergedId + "]");
			}
			record.setTprId(mergedId);
			TPRecord mergedTPR = (TPRecord) RFIDMasterDao.get(conn,
					TPRecord.class, mergedId);
			// double net = (Misc.isUndef(record.getLoadGross()) ||
			// Misc.isUndef(record.getLoadTare())) ? Misc.getUndefDouble() :
			// record.getLoadGross()-record.getLoadTare();
			// boolean addThis = mergedTPR == null ||
			// mergedTPR.getComboEnd().getTime() <
			// (record.getComboEnd().getTime() + clockGap) ;
			// boolean updateDoQty = !Misc.isUndef(net) &&
			// (record.getMaterialCat() == Type.TPRMATERIAL.COAL_ROAD ||
			// record.getMaterialCat() == Type.TPRMATERIAL.COAL_WASHERY) &&
			// (mergedTPR == null || Misc.isUndef(mergedTPR.getLoadGross()) ||
			// Misc.isUndef(mergedTPR.getLoadTare()));
			if (!Misc.isUndef(vehicleId)) {
				TPRecord latestTpr = getLatestTPRForView(conn, vehicleId);
				if (latestTpr != null) {
					if (record.getTprStatus() == TPR.OPEN
							&& record.getStatus() == Status.ACTIVE) {
						if (latestTpr.getTprId() == mergedId
								|| (latestTpr.getComboEnd().getTime() <= (record
										.getComboEnd().getTime() + clockGap))) {
							record.setLatest(1);
						} else {
							record.setLatest(0);
							record.setTprStatus(2);
						}
					} else {
						record.setLatest(0);
						record.setTprStatus(2);
					}
				}
				if (mergedTPR != null) {
					if ((!Utils.isNull(mergedTPR.getLoadWbInName())
							&& !Utils.isNull(record.getLoadWbInName()) && !mergedTPR
							.getLoadWbInName().equalsIgnoreCase(
									record.getLoadWbInName()))
							|| (!Utils.isNull(mergedTPR.getLoadWbOutName())
									&& !Utils.isNull(record.getLoadWbOutName()) && !mergedTPR
									.getLoadWbOutName().equalsIgnoreCase(
											record.getLoadWbOutName()))
							|| (!Utils.isNull(mergedTPR.getUnloadWbInName())
									&& !Utils
											.isNull(record.getUnloadWbInName()) && !mergedTPR
									.getUnloadWbInName().equalsIgnoreCase(
											record.getUnloadWbInName()))
							|| (!Utils.isNull(mergedTPR.getUnloadWbOutName())
									&& !Utils.isNull(record
											.getUnloadWbOutName()) && !mergedTPR
									.getUnloadWbOutName().equalsIgnoreCase(
											record.getUnloadWbOutName()))) {
						if (latestTpr != null
								&& (latestTpr.getComboEnd().getTime() <= (record
										.getComboEnd().getTime() + clockGap))) {
							clearLatestTprForVehicle(conn, vehicleId, true,
									false);
							clearLatestTprForVehicle(conn, vehicleId, true,
									true);
						}
						record.setTprId(Misc.getUndefInt());
						record.setChallanNo(getModChallanNo(record
								.getChallanNo()));
						insertUpdateTpr(conn, record, true);// close already
						// open
					} else {
						if (record.getIsLatest() == 1
								&& mergedTPR.getIsLatest() != 1) {
							clearLatestTprForVehicle(conn, vehicleId, true,
									false);
							clearLatestTprForVehicle(conn, vehicleId, true,
									true);
							updateTPRStatus(conn, record.getTprStatus(), record
									.getIsLatest(), record.getStatus(),
									mergedId, workstationId, false);
							updateTPRStatus(conn, record.getTprStatus(), record
									.getIsLatest(), record.getStatus(),
									mergedId, workstationId, true);
						}
						if (modifiedChallanNo != null
								&& modifiedChallanNo.length() > 0
								&& !modifiedChallanNo
										.equalsIgnoreCase(mergedTPR
												.getChallanNo())) {
							updateChallanNo(conn, modifiedChallanNo, mergedId,
									workstationId, false);
							updateChallanNo(conn, modifiedChallanNo, mergedId,
									workstationId, true);
						}
						TPRWeighmentRecord remoteTprStep = null;
						TPRWeighmentRecord localTprStep = null;
						boolean isChallanUpdated = false;
						TprChallanData mergeTPRChallanData = mergedTPR
								.getTprChallanData();
						TprChallanData remoteTPRChallanData = record
								.getTprChallanData();
						for (int i = 0, is = StepType.values() == null ? 0
								: StepType.values().length; i < is; i++) {
							remoteTprStep = mergedTPR
									.getWeighmentStepByType(StepType.values()[i]);
							localTprStep = record
									.getWeighmentStepByType(StepType.values()[i]);
							if ((localTprStep != null && !localTprStep.isNull())
									&& (remoteTprStep == null
											|| remoteTprStep.isNull() || (((localTprStep
											.getTime().getTime() + clockGap) - remoteTprStep
											.getTime().getTime()) > 1000))) {
								if (!isChallanUpdated
										&& (StepType.values()[i] == StepType.loadWBOut || StepType
												.values()[i] == StepType.unloadWBOut)) {
									updateTprNonWeighment(conn,
											remoteTPRChallanData, mergedId,
											workstationId, false);
									updateTprNonWeighment(conn,
											remoteTPRChallanData, mergedId,
											workstationId, true);
									isChallanUpdated = true;
								}
								updateTprWeighment(conn, localTprStep,
										mergedId, workstationId, false);
								updateTprWeighment(conn, localTprStep,
										mergedId, workstationId, true);
							}
						}
						if (!isChallanUpdated
								&& (remoteTPRChallanData.getMinesCode() != null && remoteTPRChallanData
										.getMinesCode().length() > 0)
								&& (mergeTPRChallanData == null
										|| mergeTPRChallanData.getMinesCode() == null || mergeTPRChallanData
										.getMinesCode().length() <= 0)) {
							updateTprNonWeighment(conn, remoteTPRChallanData,
									mergedId, workstationId, false);
							updateTprNonWeighment(conn, remoteTPRChallanData,
									mergedId, workstationId, true);
						}
					}
				} else {
					insertUpdateTpr(conn, record, true);// close already open
				}

				/*
				 * if(addThis){ insertUpdateTpr(conn, record, false);
				 * if(updateDoQty){ DoDetails doDetails =
				 * DoDetails.getDODetails(conn, record.getDoNumber(),
				 * Misc.getUndefInt()); if(doDetails != null){
				 * updateDOCurrentStatus(conn, vehicleId, record.getTprId(),
				 * doDetails.getDoNumber(), record.getLoadWbOutName(), net); } }
				 * }
				 */
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return record.getTprId();
	}

	private static int getTprByStep(Connection conn, int vehicleId,
			StepType step, Date inTime, Date outTime) {
		int retval = Misc.getUndefInt();
		String query = null;
		switch (step) {
		// case loadWBIn : query = new String("");break;
		// case loadRfWBIn : query = new String("");break;
		case loadWBOut:
			query = new String(
					"select tpr_id from tp_record where latest_load_gate_in_out<= ? and latest_load_gate_out_out>=? and vehicle_id=? and load_gross is null order by latest_load_gate_out_out desc limit 1");
			break;
		// case loadRfWBOut : query = new String("");break;
		case unloadWBIn:
			query = new String(
					"select tpr_id from tp_record where latest_unload_gate_in_out<= ? and latest_unload_gate_out_out>=? and vehicle_id=? and unload_gross is null order by latest_unload_gate_out_out desc limit 1");
			break;
		// case unloadWBOut : query = new String("");break;
		// to do later
		// case loadGateIn : query = new String("");break;
		// case loadYardIn : query = new String("");break;
		// case loadYardOut : query = new String("");break;
		case loadGateOut:
			query = new String(
					"select tpr_id from tp_record where latest_load_wb_out_out between ? and ? and vehicle_id=? and latest_load_gate_out_out is null order by latest_load_gate_out_out desc limit 1");
			break;
		// case unloadGateIn : query = new String("");break;
		// case unloadYardIn : query = new String("");break;
		// case unloadYardOut : query = new String("");break;
		case unloadGateOut:
			query = new String(
					"select tpr_id from tp_record where latest_unload_wb_out_out between ? and ? and vehicle_id=? and latest_unload_gate_out_out is null order by latest_unload_gate_out_out desc limit 1");
			break;
		default:
			break;
		}
		if (query == null || query.length() <= 0)
			return retval;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(query);
			ps.setTimestamp(1, inTime == null ? null : new Timestamp(inTime
					.getTime()));
			ps.setTimestamp(2, outTime == null ? null : new Timestamp(outTime
					.getTime()));
			Misc.setParamInt(ps, vehicleId, 3);
			System.out.println(Thread.currentThread().toString() + "["
					+ DBConnectionPool.getPrintableConnectionStr(conn) + "]"
					+ ps.toString());
			rs = ps.executeQuery();
			if (rs.next()) {
				retval = Misc.getRsetInt(rs, 1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}

	public static void updateTPRStatus(Connection conn, int tprStatus,
			int isLatest, int status, int tprId, int syncStation, boolean apprvd)
			throws Exception {
		if (Misc.isUndef(tprId))
			return;
		String query = new String(
				"update tp_record"
						+ (apprvd ? "_apprvd" : "")
						+ " set "
						+ " tpr_status=?, is_latest=?, status=?, last_sync_station=?,record_src=?, last_sync_at=now() where tpr_id=?");
		PreparedStatement ps = null;
		int index = 1;
		try {
			ps = conn.prepareStatement(query);
			Misc.setParamInt(ps, tprStatus, index++);
			Misc.setParamInt(ps, isLatest, index++);
			Misc.setParamInt(ps, status, index++);
			Misc.setParamInt(ps, syncStation, index++);
			Misc.setParamInt(ps, syncStation, index++);
			Misc.setParamInt(ps, tprId, index++);
			System.out.println(Thread.currentThread().toString() + "["
					+ DBConnectionPool.getPrintableConnectionStr(conn) + "]"
					+ ps.toString());
			ps.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			Misc.closePS(ps);
		}
	}

	public static void updateChallanNo(Connection conn, String challanNo,
			int tprId, int syncStation, boolean apprvd) throws Exception {
		if (challanNo == null || challanNo.length() <= 0 || Misc.isUndef(tprId))
			return;
		String query = new String(
				"update tp_record"
						+ (apprvd ? "_apprvd" : "")
						+ " set "
						+ " challan_no=?, last_sync_station=?,record_src=?, last_sync_at=now() where tpr_id=?");
		PreparedStatement ps = null;
		int index = 1;
		try {
			ps = conn.prepareStatement(query);
			ps.setString(index++, challanNo);
			Misc.setParamInt(ps, syncStation, index++);
			Misc.setParamInt(ps, syncStation, index++);
			Misc.setParamInt(ps, tprId, index++);
			System.out.println(Thread.currentThread().toString() + "["
					+ DBConnectionPool.getPrintableConnectionStr(conn) + "]"
					+ ps.toString());
			ps.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			Misc.closePS(ps);
		}
	}

	public static void updateTprNonWeighment(Connection conn,
			TprChallanData tprChallanData, int tprId, int syncStation,
			boolean apprvd) throws Exception {
		if (tprChallanData == null || Misc.isUndef(tprId))
			return;
		String query = new String(
				"update tp_record"
						+ (apprvd ? "_apprvd" : "")
						+ " set "
						+ " material_cat=?, do_number=?, mines_code=?, rf_mines_code=?, "
						+ " grade_code=?, rf_grade_code=?, product_code=?, rf_product_code=?, "
						+ " transporter_code=?, rf_transporter_code=?, "
						+ " destination_code=?,rf_destination_code=?,customer_code=?, "
						+ " washery_code=?, lr_no=?, rf_lr_id=?, last_sync_station=?,record_src=?,invoice_number=?, last_sync_at=now() where tpr_id=?");
		PreparedStatement ps = null;
		int index = 1;
		try {
			ps = conn.prepareStatement(query);
			Misc.setParamInt(ps, tprChallanData.getMaterialCat(), index++);
			ps.setString(index++, tprChallanData.getDoNumber());
			ps.setString(index++, tprChallanData.getMinesCode());
			ps.setString(index++, tprChallanData.getRfMinesCode());
			ps.setString(index++, tprChallanData.getGradeCode());
			ps.setString(index++, tprChallanData.getRfGradeCode());
			ps.setString(index++, tprChallanData.getProductCode());
			ps.setString(index++, tprChallanData.getRfProductCode());
			ps.setString(index++, tprChallanData.getTransporterCode());
			ps.setString(index++, tprChallanData.getRfTransporterCode());
			ps.setString(index++, tprChallanData.getDestinationCode());
			ps.setString(index++, tprChallanData.getRfDestinationCode());
			ps.setString(index++, tprChallanData.getCustomerCode());
			ps.setString(index++, tprChallanData.getWasheryCode());
			ps.setString(index++, tprChallanData.getLrNo());
			ps.setString(index++, tprChallanData.getRfLrNo());
			Misc.setParamInt(ps, syncStation, index++);
			Misc.setParamInt(ps, syncStation, index++);
			ps.setString(index++, tprChallanData.getInvoiceNumber());
			Misc.setParamInt(ps, tprId, index++);
			System.out.println(Thread.currentThread().toString() + "["
					+ DBConnectionPool.getPrintableConnectionStr(conn) + "]"
					+ ps.toString());
			ps.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			Misc.closePS(ps);
		}
	}

	public static void updateTprWeighment(Connection conn,
			TPRWeighmentRecord tprStep, int tprId, int syncStation,
			boolean apprvd) throws Exception {
		if (tprStep == null || tprStep.isNull() || Misc.isUndef(tprId))
			return;
		String query = null;
		switch (tprStep.getStepType()) {
		case loadWBIn:
			query = new String(
					"update tp_record"
							+ (apprvd ? "_apprvd" : "")
							+ " set earliest_load_wb_in_in=?,latest_load_wb_in_out=?,load_wb_in_name=?,load_tare=?,last_sync_station=?,record_src=?,last_sync_at=now()  where tpr_id=?");
			break;
		case loadRfWBIn:
			query = new String(
					"update tp_record"
							+ (apprvd ? "_apprvd" : "")
							+ " set earliest_load_wb_in_in=?,latest_load_wb_in_out=?,load_wb_in_name=?,rf_load_tare=?,last_sync_station=?,record_src=?,last_sync_at=now()  where tpr_id=?");
			break;
		case loadWBOut:
			query = new String(
					"update tp_record"
							+ (apprvd ? "_apprvd" : "")
							+ " set earliest_load_wb_out_in=?,latest_load_wb_out_out=?,load_wb_out_name=?,load_gross=?,last_sync_station=?,record_src=?,last_sync_at=now() where tpr_id=?");
			break;
		case loadRfWBOut:
			query = new String(
					"update tp_record"
							+ (apprvd ? "_apprvd" : "")
							+ " set earliest_load_wb_out_in=?,latest_load_wb_out_out=?,load_wb_out_name=?,rf_load_gross=?,last_sync_station=?,record_src=?,last_sync_at=now() where tpr_id=?");
			break;
		case unloadWBIn:
			query = new String(
					"update tp_record"
							+ (apprvd ? "_apprvd" : "")
							+ " set earliest_unload_wb_in_in=?,latest_unload_wb_in_out=?,unload_wb_in_name=?,unload_gross=?,last_sync_station=?,record_src=?,last_sync_at=now() where tpr_id=?");
			break;
		case unloadWBOut:
			query = new String(
					"update tp_record"
							+ (apprvd ? "_apprvd" : "")
							+ " set earliest_unload_wb_out_in=?,latest_unload_wb_out_out=?,unload_wb_out_name=?,unload_tare=?,last_sync_station=?,record_src=?,last_sync_at=now() where tpr_id=?");
			break;
		// to do later
		case loadGateIn:
			query = new String(
					"update tp_record"
							+ (apprvd ? "_apprvd" : "")
							+ " set earliest_load_gate_in_in=?,latest_load_gate_in_out=?,load_gate_in_name=?,last_sync_station=?,record_src=?,last_sync_at=now() where tpr_id=?");
			break;
		case loadYardIn:
			query = new String(
					"update tp_record"
							+ (apprvd ? "_apprvd" : "")
							+ " set earliest_load_yard_in_in=?,latest_load_yard_in_out=?,load_yard_in_name=?,last_sync_station=?,record_src=?,last_sync_at=now() where tpr_id=?");
			break;
		case loadYardOut:
			query = new String(
					"update tp_record"
							+ (apprvd ? "_apprvd" : "")
							+ " set earliest_load_yard_out_in=?,latest_load_yard_out_out=?,load_yard_out_name=?,last_sync_station=?,record_src=?,last_sync_at=now() where tpr_id=?");
			break;
		case loadGateOut:
			query = new String(
					"update tp_record"
							+ (apprvd ? "_apprvd" : "")
							+ " set earliest_load_gate_out_in=?,latest_load_gate_out_out=?,load_gate_out_name=?,last_sync_station=?,record_src=?,last_sync_at=now() where tpr_id=?");
			break;
		case unloadGateIn:
			query = new String(
					"update tp_record"
							+ (apprvd ? "_apprvd" : "")
							+ " set earliest_unload_gate_in_in=?,latest_unload_gate_in_out=?,unload_gate_in_name=?,last_sync_station=?,record_src=?,last_sync_at=now() where tpr_id=?");
			break;
		case unloadYardIn:
			query = new String(
					"update tp_record"
							+ (apprvd ? "_apprvd" : "")
							+ " set earliest_unload_yard_in_in=?,latest_unload_yard_in_out=?,unload_yard_in_name=?,last_sync_station=?,record_src=?,last_sync_at=now() where tpr_id=?");
			break;
		case unloadYardOut:
			query = new String(
					"update tp_record"
							+ (apprvd ? "_apprvd" : "")
							+ " set earliest_unload_yard_out_in=?,latest_unload_yard_out_out=?,unload_yard_out_name=?,last_sync_station=?,record_src=?,last_sync_at=now() where tpr_id=?");
			break;
		case unloadGateOut:
			query = new String(
					"update tp_record"
							+ (apprvd ? "_apprvd" : "")
							+ " set earliest_unload_gate_out_in=?,latest_unload_gate_out_out=?,unload_gate_out_name=?,last_sync_station=?,record_src=?,last_sync_at=now() where tpr_id=?");
			break;
		default:
			break;
		}
		if (query == null)
			return;
		PreparedStatement ps = null;
		int index = 1;
		try {
			ps = conn.prepareStatement(query);
			ps.setTimestamp(index++, tprStep.getInTime() == null ? null
					: new Timestamp(tprStep.getInTime().getTime()));
			ps.setTimestamp(index++, tprStep.getOutTime() == null ? null
					: new Timestamp(tprStep.getOutTime().getTime()));
			ps.setString(index++, tprStep.getStation());
			if (ps.getParameterMetaData().getParameterCount() > 6)
				Misc.setParamDouble(ps, tprStep.getWeight(), index++);
			Misc.setParamInt(ps, syncStation, index++);
			Misc.setParamInt(ps, syncStation, index++);
			Misc.setParamInt(ps, tprId, index++);
			System.out.println(Thread.currentThread().toString() + "["
					+ DBConnectionPool.getPrintableConnectionStr(conn) + "]"
					+ ps.toString());
			ps.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			Misc.closePS(ps);
		}
	}

	synchronized public static int handleMergeVehicle(Connection conn,
			Vehicle vehicle, int workstationId, long clockGap) throws Exception {
		if (vehicle == null)
			return Misc.getUndefInt();
		String vehicleName = vehicle.getStdName();
		if (vehicleName == null || vehicleName.trim().length() <= 0)
			return Misc.getUndefInt();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int vehicleId = Misc.getUndefInt();
		int vehicleRfidInfoId = Misc.getUndefInt();
		try {
			// update vehicleId
			vehicleId = getVehicleByName(conn, vehicleName);
			boolean addRFIDInfo = false;
			Vehicle remoteVehicle = null;
			if (Misc.isUndef(vehicleId)) {
				ps = conn
						.prepareStatement(
								"insert into vehicle (name,std_name,status,customer_id,record_src) values (?,?,?,?,?)",
								PreparedStatement.RETURN_GENERATED_KEYS);
				ps.setString(1, vehicle.getVehicleName());
				ps.setString(2, vehicle.getStdName());
				Misc.setParamInt(ps, vehicle.getStatus(), 3);
				Misc.setParamInt(ps, vehicle.getCustomerId(), 4);
				Misc.setParamInt(ps, workstationId, 5);
				System.out.println(Thread.currentThread().toString() + "["
						+ DBConnectionPool.getPrintableConnectionStr(conn)
						+ "]" + ps.toString());
				ps.execute();
				rs = ps.getGeneratedKeys();
				if (rs.next()) {
					vehicleId = Misc.getRsetInt(rs, 1);
				}
				Misc.closeRS(rs);
				Misc.closePS(ps);
				ps = conn
						.prepareStatement("insert into vehicle_extended (vehicle_id,transporter_code,insurance_number,permit1_number,insurance_number_expiry,permit1_number_expiry) values (?,?,?,?,?,?)");
				Misc.setParamInt(ps, vehicleId, 1);
				ps.setString(2, vehicle.getVehicleExt() == null ? null
						: vehicle.getVehicleExt().getTransporterCode());
				ps.setString(3, vehicle.getVehicleExt() == null ? null
						: vehicle.getVehicleExt().getInsurance_number());
				ps.setString(4, vehicle.getVehicleExt() == null ? null
						: vehicle.getVehicleExt().getPermit1_number());
				ps
						.setTimestamp(
								5,
								vehicle.getVehicleExt() == null
										|| vehicle.getVehicleExt()
												.getInsurance_number_expiry() == null ? null
										: new java.sql.Timestamp(vehicle
												.getVehicleExt()
												.getInsurance_number_expiry()
												.getTime()));
				ps
						.setTimestamp(
								6,
								vehicle.getVehicleExt() == null
										|| vehicle.getVehicleExt()
												.getPermit1_number_expiry() == null ? null
										: new java.sql.Timestamp(vehicle
												.getVehicleExt()
												.getPermit1_number_expiry()
												.getTime()));
				System.out.println(Thread.currentThread().toString() + "["
						+ DBConnectionPool.getPrintableConnectionStr(conn)
						+ "]" + ps.toString());
				ps.execute();
				Misc.closePS(ps);
				addRFIDInfo = true;
			} else {
				remoteVehicle = (Vehicle) RFIDMasterDao.get(conn,
						Vehicle.class, vehicleId);
				addRFIDInfo = !Misc.isUndef(vehicle.getCardType())
				// && !vehicle.isRFEquals(remoteVehicle)
						&& (vehicle.getUpdatedOn() != null && (remoteVehicle
								.getUpdatedOn() == null || (remoteVehicle
								.getUpdatedOn().getTime() < vehicle
								.getUpdatedOn().getTime())));
			}
			if (addRFIDInfo) {
				if (vehicle.getRfUpdatedOn() != null
						&& (remoteVehicle == null
								|| remoteVehicle.getRfUpdatedOn() == null || (remoteVehicle
								.getRfUpdatedOn().getTime() < vehicle
								.getRfUpdatedOn().getTime()))) {
					if (true || vehicle.getRfid_issue_date() != null) {
						/*
						 * ps =conn.prepareStatement(
						 * "update vehicle set last_epc=rfid_epc, rfid_epc=null, record_src=? where rfid_epc=?"
						 * ); Misc.setParamInt(ps, workstationId, 1);
						 * ps.setString(2, vehicle.getEpcId());
						 * ps.executeUpdate();
						 */
						ps = conn
								.prepareStatement("update vehicle_rfid_info set status=2 , return_date=now()  where epc_id=? and status=1");
						ps.setString(1, vehicle.getEpcId());
						ps.executeUpdate();
						// update vehicle rfid fields
						ps = conn
								.prepareStatement("update vehicle set rfid_epc=?, rfid_issue_date=?, rfid_temp_status=? ,"
										+ " card_type=?, card_purpose=?, do_assigned=?, card_validity_type=?, card_expiary_date=?, "
										+ " card_init=?, rfid_info_id=?, card_init_date=?, is_vehicle_on_gate=?, prefered_mines_code=?, "
										+ " last_tare_tpr=?, min_tare=?, min_gross=?, record_src=?, tag_init_challan=?,gate_pass_number=?, src_record_time=? where id=? and (src_record_time is null or src_record_time < ?)");
						ps.setString(1, vehicle.getEpcId());
						ps
								.setTimestamp(
										2,
										vehicle.getRfid_issue_date() == null ? null
												: new java.sql.Timestamp(
														vehicle
																.getRfid_issue_date()
																.getTime()));
						Misc.setParamInt(ps, vehicle.getRfidTempStatus(), 3);
						Misc.setParamInt(ps, vehicle.getCardType(), 4);
						Misc.setParamInt(ps, vehicle.getCardPurpose(), 5);
						ps.setString(6, vehicle.getDoAssigned());
						Misc.setParamInt(ps, vehicle.getCardValidityType(), 7);
						ps
								.setTimestamp(
										8,
										vehicle.getCardExpiaryDate() == null ? null
												: new java.sql.Timestamp(
														vehicle
																.getCardExpiaryDate()
																.getTime()));
						Misc.setParamInt(ps, vehicle.getCardInit(), 9);
						Misc.setParamInt(ps, vehicleRfidInfoId, 10);
						ps.setTimestamp(11,
								vehicle.getCardInitDate() == null ? null
										: new java.sql.Timestamp(vehicle
												.getCardInitDate().getTime()));
						Misc.setParamInt(ps, vehicle.getVehicleOnGate(), 12);
						ps.setString(13, vehicle.getPreferedMinesCode());
						Misc.setParamInt(ps, vehicle.getLastTareTPR(), 14);
						Misc.setParamDouble(ps, vehicle.getMinTare(), 15);
						Misc.setParamDouble(ps, vehicle.getMinGross(), 16);
						Misc.setParamInt(ps, workstationId, 17);
						ps.setString(18, vehicle.getTagInitChallan());
						ps.setString(19, vehicle.getGatePassNumber());
						ps.setTimestamp(20,
								vehicle.getSrcRecordTime() == null ? null
										: new Timestamp(vehicle
												.getSrcRecordTime().getTime()));
						Misc.setParamInt(ps, vehicleId, 21);
						ps.setTimestamp(22,
								vehicle.getSrcRecordTime() == null ? null
										: new Timestamp(vehicle
												.getSrcRecordTime().getTime()));
						System.out.println(Thread.currentThread().toString()
								+ "["
								+ DBConnectionPool
										.getPrintableConnectionStr(conn) + "]"
								+ ps.toString());
						ps.executeUpdate();
						Misc.closePS(ps);
						// update vehicle ext
						ps = conn.prepareStatement("update vehicle_extended "
								+ " set transporter_code=?,"
								+ " insurance_number=?," + " permit1_number=?,"
								+ " insurance_number_expiry=?,"
								+ " permit1_number_expiry=?"
								+ " where vehicle_id=?");
						ps.setString(1, vehicle.getVehicleExt() == null ? null
								: vehicle.getVehicleExt().getTransporterCode());
						ps
								.setString(2,
										vehicle.getVehicleExt() == null ? null
												: vehicle.getVehicleExt()
														.getInsurance_number());
						ps.setString(3, vehicle.getVehicleExt() == null ? null
								: vehicle.getVehicleExt().getPermit1_number());
						ps
								.setTimestamp(
										4,
										vehicle.getVehicleExt() == null
												|| vehicle
														.getVehicleExt()
														.getInsurance_number_expiry() == null ? null
												: new java.sql.Timestamp(
														vehicle
																.getVehicleExt()
																.getInsurance_number_expiry()
																.getTime()));
						ps
								.setTimestamp(
										5,
										vehicle.getVehicleExt() == null
												|| vehicle
														.getVehicleExt()
														.getPermit1_number_expiry() == null ? null
												: new java.sql.Timestamp(
														vehicle
																.getVehicleExt()
																.getPermit1_number_expiry()
																.getTime()));
						Misc.setParamInt(ps, vehicleId, 6);
						System.out.println(Thread.currentThread().toString()
								+ "["
								+ DBConnectionPool
										.getPrintableConnectionStr(conn) + "]"
								+ ps.toString());
						ps.executeUpdate();
						Misc.closePS(ps);
						// insert rfidinfo
						VehicleRFIDInfo vehRFIDInfo = new VehicleRFIDInfo();
						vehRFIDInfo.setVehicleId(vehicleId);
						vehRFIDInfo.setEpcId(vehicle.getEpcId());
						vehRFIDInfo.setStatus(vehicle.getStatus());
						vehRFIDInfo.setDriverId(vehicle.getPreferedDriver());
						vehRFIDInfo.setDoAssigned(vehicle.getDoAssigned());
						vehRFIDInfo.setCardType(vehicle.getCardType());
						vehRFIDInfo.setCardIssuedFor(vehicle.getCardPurpose());
						vehRFIDInfo
								.setPurpose(vehicle.getVehicleRFIDInfo() == null ? null
										: vehicle.getVehicleRFIDInfo()
												.getPurpose());
						vehRFIDInfo.setIssueDate(new Date(System
								.currentTimeMillis()));
						vehRFIDInfo.setCretedOn(new Date(System
								.currentTimeMillis()));
						vehRFIDInfo.setAllowedMinesCode(vehicle
								.getPreferedMinesCode());
						RFIDMasterDao.insert(conn, vehRFIDInfo);
						vehicleRfidInfoId = vehRFIDInfo.getId();
					} else {
						// return tag
						ps = conn
								.prepareStatement("update vehicle set rfid_epc=null, rfid_issue_date=null, rfid_temp_status=null, card_type=null, card_purpose=null, do_assigned=null, "
										+ " card_validity_type=null, card_expiary_date=null, card_init=null, rfid_info_id=null, card_init_date=null, "
										+ " is_vehicle_on_gate=null, prefered_mines_code=null, last_tare_tpr=null, min_tare=null, min_gross=null,tag_init_challan=null, "
										+ " record_src=? where id=? ");
						Misc.setParamInt(ps, workstationId, 1);
						Misc.setParamInt(ps, vehicleId, 2);
						ps.executeUpdate();
						ps = conn
								.prepareStatement("update vehicle_rfid_info set status=2 , return_date=now()  where vehicle_id=? and status=1");
						Misc.setParamInt(ps, vehicleId, 1);
						ps.executeUpdate();
						Misc.closePS(ps);
					}

				} else {
					ps = conn
							.prepareStatement("update vehicle set "
									+ " card_type=(case when ? is null then card_type else ? end), "
									+ " card_purpose=(case when ? is null then card_purpose else ? end), "
									+ " do_assigned=(case when ? is null then do_assigned else ? end), "
									+ " card_validity_type=(case when ? is null then card_validity_type else ? end), "
									+ " card_expiary_date=(case when ? is null then card_expiary_date else ? end), "
									+ " card_init=(case when ? is null then card_init else ? end), "
									+ " rfid_info_id=(case when ? is null then rfid_info_id else ? end), "
									+ " card_init_date=(case when ? is null then card_init_date else ? end), "
									+ " is_vehicle_on_gate=(case when ? is null then is_vehicle_on_gate else ? end), "
									+ " prefered_mines_code=(case when ? is null then prefered_mines_code else ? end), "
									+ " last_tare_tpr=(case when ? is null then last_tare_tpr else ? end), "
									+ " min_tare=(case when ? is null then min_tare else ? end), "
									+ " min_gross=(case when ? is null then min_gross else ? end), "
									+ " tag_init_challan=(case when ? is null then tag_init_challan else ? end), "
									+ " gate_pass_number=(case when ? is null then gate_pass_number else ? end),"
									+ " record_src=?, src_record_time=? "
									+ " where id=? and (src_record_time is null or src_record_time < ?) ");
					int index = 1;
					Misc.setParamInt(ps, vehicle.getCardType(), index++);
					Misc.setParamInt(ps, vehicle.getCardType(), index++);
					Misc.setParamInt(ps, vehicle.getCardPurpose(), index++);
					Misc.setParamInt(ps, vehicle.getCardPurpose(), index++);
					ps.setString(index++, vehicle.getDoAssigned());
					ps.setString(index++, vehicle.getDoAssigned());
					Misc
							.setParamInt(ps, vehicle.getCardValidityType(),
									index++);
					Misc
							.setParamInt(ps, vehicle.getCardValidityType(),
									index++);
					ps.setTimestamp(index++,
							vehicle.getCardExpiaryDate() == null ? null
									: new java.sql.Timestamp(vehicle
											.getCardExpiaryDate().getTime()));
					ps.setTimestamp(index++,
							vehicle.getCardExpiaryDate() == null ? null
									: new java.sql.Timestamp(vehicle
											.getCardExpiaryDate().getTime()));
					Misc.setParamInt(ps, vehicle.getCardInit(), index++);
					Misc.setParamInt(ps, vehicle.getCardInit(), index++);
					Misc.setParamInt(ps, vehicleRfidInfoId, index++);
					Misc.setParamInt(ps, vehicleRfidInfoId, index++);
					ps.setTimestamp(index++,
							vehicle.getCardInitDate() == null ? null
									: new java.sql.Timestamp(vehicle
											.getCardInitDate().getTime()));
					ps.setTimestamp(index++,
							vehicle.getCardInitDate() == null ? null
									: new java.sql.Timestamp(vehicle
											.getCardInitDate().getTime()));
					Misc.setParamInt(ps, vehicle.getVehicleOnGate(), index++);
					Misc.setParamInt(ps, vehicle.getVehicleOnGate(), index++);
					ps.setString(index++, vehicle.getPreferedMinesCode());
					ps.setString(index++, vehicle.getPreferedMinesCode());
					Misc.setParamInt(ps, vehicle.getLastTareTPR(), index++);
					Misc.setParamInt(ps, vehicle.getLastTareTPR(), index++);
					Misc.setParamDouble(ps, vehicle.getMinTare(), index++);
					Misc.setParamDouble(ps, vehicle.getMinTare(), index++);
					Misc.setParamDouble(ps, vehicle.getMinGross(), index++);
					Misc.setParamDouble(ps, vehicle.getMinGross(), index++);
					ps.setString(index++, vehicle.getTagInitChallan());
					ps.setString(index++, vehicle.getTagInitChallan());
					ps.setString(index++, vehicle.getGatePassNumber());
					ps.setString(index++, vehicle.getGatePassNumber());
					Misc.setParamInt(ps, workstationId, index++);
					ps.setTimestamp(index++,
							vehicle.getSrcRecordTime() == null ? null
									: new java.sql.Timestamp(vehicle
											.getSrcRecordTime().getTime()));
					Misc.setParamInt(ps, vehicleId, index++);
					ps.setTimestamp(index++,
							vehicle.getSrcRecordTime() == null ? null
									: new java.sql.Timestamp(vehicle
											.getSrcRecordTime().getTime()));
					System.out.println(Thread.currentThread().toString() + "["
							+ DBConnectionPool.getPrintableConnectionStr(conn)
							+ "]" + ps.toString());
					ps.executeUpdate();
					Misc.closePS(ps);
					index = 1;
					ps = conn
							.prepareStatement("update vehicle_extended "
									+ " set transporter_code=(case when ? is null then transporter_code else ? end),"
									+ " insurance_number=(case when ? is null then insurance_number else ? end),"
									+ " permit1_number=(case when ? is null then permit1_number else ? end),"
									+ " insurance_number_expiry=(case when ? is null then insurance_number_expiry else ? end),"
									+ " permit1_number_expiry=(case when ? is null then permit1_number_expiry else ? end)"
									+ " where vehicle_id=?");
					ps.setString(1, vehicle.getVehicleExt() == null ? null
							: vehicle.getVehicleExt().getTransporterCode());
					ps.setString(2, vehicle.getVehicleExt() == null ? null
							: vehicle.getVehicleExt().getTransporterCode());

					ps.setString(3, vehicle.getVehicleExt() == null ? null
							: vehicle.getVehicleExt().getInsurance_number());
					ps.setString(4, vehicle.getVehicleExt() == null ? null
							: vehicle.getVehicleExt().getInsurance_number());

					ps.setString(5, vehicle.getVehicleExt() == null ? null
							: vehicle.getVehicleExt().getPermit1_number());
					ps.setString(6, vehicle.getVehicleExt() == null ? null
							: vehicle.getVehicleExt().getPermit1_number());

					ps
							.setTimestamp(
									7,
									vehicle.getVehicleExt() == null
											|| vehicle
													.getVehicleExt()
													.getInsurance_number_expiry() == null ? null
											: new java.sql.Timestamp(
													vehicle
															.getVehicleExt()
															.getInsurance_number_expiry()
															.getTime()));
					ps
							.setTimestamp(
									8,
									vehicle.getVehicleExt() == null
											|| vehicle
													.getVehicleExt()
													.getInsurance_number_expiry() == null ? null
											: new java.sql.Timestamp(
													vehicle
															.getVehicleExt()
															.getInsurance_number_expiry()
															.getTime()));

					ps.setTimestamp(9, vehicle.getVehicleExt() == null
							|| vehicle.getVehicleExt()
									.getPermit1_number_expiry() == null ? null
							: new java.sql.Timestamp(vehicle.getVehicleExt()
									.getPermit1_number_expiry().getTime()));
					ps.setTimestamp(10, vehicle.getVehicleExt() == null
							|| vehicle.getVehicleExt()
									.getPermit1_number_expiry() == null ? null
							: new java.sql.Timestamp(vehicle.getVehicleExt()
									.getPermit1_number_expiry().getTime()));

					Misc.setParamInt(ps, vehicleId, 11);
					System.out.println(Thread.currentThread().toString() + "["
							+ DBConnectionPool.getPrintableConnectionStr(conn)
							+ "]" + ps.toString());
					ps.executeUpdate();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return vehicleId;
	}

	public static int getVehicleByName(Connection conn, String stdName) {
		if (stdName == null || stdName.trim().length() <= 0)
			return Misc.getUndefInt();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn
					.prepareStatement("select id from vehicle where std_name=? and status=1");
			ps.setString(1, CacheTrack.standardizeName(stdName));
			System.out.println("[TPR][INFOMATION][GET]][VEHICLE][STD_NAME]["
					+ DBConnectionPool.getPrintableConnectionStr(conn) + "]:"
					+ ps);
			rs = ps.executeQuery();
			if (rs.next()) {
				return Misc.getRsetInt(rs, 1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return Misc.getUndefInt();
	}

	public static int getTprIdByChallan(Connection conn, String challanNo,
			int vehicleId) {
		if (challanNo == null || challanNo.trim().length() <= 0)
			return Misc.getUndefInt();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// ps =
			// conn.prepareStatement("select tpr_id from tp_record where vehicle_id=? and challan_no = ? order by tpr_id desc limit 1");
			ps = conn
					.prepareStatement("select tpr_id from tp_record where challan_no = ? order by combo_end desc limit 1");
			int count = 1;
			// Misc.setParamInt(ps, vehicleId, count++);
			ps.setString(count++, challanNo);
			System.out.println(Thread.currentThread().toString() + "["
					+ DBConnectionPool.getPrintableConnectionStr(conn) + "]"
					+ ps.toString());
			rs = ps.executeQuery();
			if (rs.next()) {
				return Misc.getRsetInt(rs, 1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return Misc.getUndefInt();
	}

	synchronized public static TPRecord getTPRForHHWeb(Connection conn,
			int vehicleId, RFIDHolder holder, String vehicleName, String orgName)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		TPRecord tpr = null;
		TPRecord latestTpr = null;
		ArrayList<Object> list = null;
		boolean isFound = false;
		boolean isDataUseful = false;
		TPRecord tprAgainstHHLog = null;
		int holdlerDataValidity = Misc.getUndefInt();
		sb.append(
				"Get_Mergable_Tpr_against_HH[" + vehicleId + "," + vehicleName
						+ "]").append("\n");
		try {
			if (!Misc.isUndef(vehicleId)) {
				if (holder != null) {
					holdlerDataValidity = holder.isDataUseful(conn, true);
					isDataUseful = holdlerDataValidity == RFIDHolder.RF_DATA_USEABLE;
					sb.append(
							"HH_log[" + holder.getGeneratedId() + "]:"
									+ holder.getRecordKey() + ","
									+ holder.getChallanId() + ","
									+ isDataUseful).append("\n");
				}
				tpr = new TPRecord();
				tpr.setVehicleId(vehicleId);
				tpr.setTprStatus(TPR.OPEN);
				tpr.setStatus(Status.ACTIVE);
				Criteria cr = new Criteria(TPRecord.class);
				cr
						.setOrderByClause("coalesce(tp_record.combo_end,tp_record.combo_start,tp_record.tpr_create_date,tp_record.challan_date)");
				cr.setDesc(true);
				list = (ArrayList<Object>) RFIDMasterDao.select(conn, tpr, cr);
				for (int i = 0, is = list == null ? 0 : list.size(); i < is; i++) {
					TPRecord tprEntry = (TPRecord) list.get(i);
					if (tprEntry.isLatest() == 1) {
						latestTpr = tprEntry;
						break;
					}
				}
				sb.append("latest:"
						+ (latestTpr == null ? null : "("
								+ latestTpr.getTprId() + ","
								+ latestTpr.getMinesId() + ","
								+ latestTpr.getChallanNo() + ")") + "\n");
				Pair<Integer, TPRecord> mergePair = mergeWithExisting(conn,
						latestTpr, vehicleId, vehicleName, holder,
						isDataUseful, true, Misc.getUndefInt(), null, sb);
				isFound = mergePair.second != null;// mergePair.first ==
				// Status.TPR_MERGE_STATUS.MERGED;
				if (isFound) {
					tprAgainstHHLog = mergePair.second;
				}
				boolean toCreateNew = isDataUseful && !isFound;
				sb.append("toCreateNew:" + toCreateNew).append("\n");
				if (toCreateNew) {
					TPRecord newTpr = createTpr(conn, vehicleId, holder,
							vehicleName, 0, true, (isDataUseful && !isFound),
							false, Type.TPRMATERIAL.COAL, null, orgName);
					sb.append("New_TPR_Created").append("\n");
					long lastLatestTime = Misc.getUndefInt();
					if (latestTpr == null) {
						lastLatestTime = getLastLatestTime(conn, vehicleId);
						sb.append("Get_lastLatestTime:" + lastLatestTime)
								.append("\n");
					}
					boolean toMakeLatest = latestTpr == null
							&& Misc.isUndef(lastLatestTime);
					// sb.append("Make_Latest"+toMakeLatest).append("\n");
					if (!toMakeLatest && isDataUseful) {
						long newTprTime = holder != null
								&& holder.getDatetime() != null ? holder
								.getDatetime().getTime() : Misc.getUndefInt();
						long latestTprTime = latestTpr == null ? lastLatestTime
								: latestTpr.getLastProcessedTime(); // (
						// latestTpr.getComboEnd()
						// != null ?
						// latestTpr.getComboEnd().getTime()
						// :
						// (latestTpr.getChallanDate()
						// != null ?
						// latestTpr.getChallanDate().getTime()
						// :
						// Misc.getUndefInt()))
						// ;
						sb.append(
								"HH_Log[" + holder.getGeneratedId() + "]:"
										+ newTprTime).append("\n");
						sb.append(
								"Curr_TPR["
										+ (latestTpr != null ? latestTpr
												.getTprId() : null) + "]:"
										+ latestTprTime).append("\n");
						if (!Misc.isUndef(newTprTime)
								&& !Misc.isUndef(latestTprTime)
								&& (newTprTime - latestTprTime) >= (newLatestTprWebThreshold))
							toMakeLatest = true;

					}
					sb.append("Make_Latest:" + toMakeLatest).append("\n");
					if (toMakeLatest) {
						newTpr.setLatest(1);
						newTpr.setStatus(Status.ACTIVE);
						latestTpr = newTpr;
					}
					tprAgainstHHLog = newTpr;
					// sb.append(newTpr.toString());
				} else {// 
					if (latestTpr != null
							&& mergePair.first == Status.TPR_MERGE_STATUS.FILL_BOTH_CURRENT
							|| mergePair.first == Status.TPR_MERGE_STATUS.FILL_RHS_CURRENT) {
						updateTpr(
								conn,
								vehicleId,
								holder,
								vehicleName,
								latestTpr,
								latestTpr.isLatest(),
								true,
								isDataUseful,
								mergePair.first == Status.TPR_MERGE_STATUS.FILL_RHS_CURRENT,
								Type.TPRMATERIAL.COAL, null, orgName);
						tprAgainstHHLog = latestTpr;
						sb.append("Merge_With_latest:" + latestTpr.getTprId())
								.append("\n");
					}
				}

				if (tprAgainstHHLog == null) {// SHOULD NOT GET HERE
					if (holder != null) {
						sb.append("[BUG]exec_unreachable_code:"
								+ holder.getVehicleId() + ","
								+ holder.getChallanId());
						holder.printData();
						tprAgainstHHLog = createTpr(conn, vehicleId, holder,
								vehicleName, 0, true, isDataUseful, false,
								Type.TPRMATERIAL.COAL, null, orgName);
						if (tprAgainstHHLog != null)
							tprAgainstHHLog.setStatus(100);
					}
				}
				if (tprAgainstHHLog != null) {
					tprAgainstHHLog.setDebugStr(sb.toString());
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (sb != null)
				System.out.println(sb.toString());
		}

		return tprAgainstHHLog;
	}

	synchronized public static TPRecord getTPRForHHWeb(Connection conn,
			int vehicleId, RFIDHolder holder, String vehicleName)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		TPRecord tpr = null;
		TPRecord latestTpr = null;
		ArrayList<Object> list = null;
		boolean isFound = false;
		boolean isDataUseful = false;
		TPRecord tprAgainstHHLog = null;
		int holdlerDataValidity = Misc.getUndefInt();
		sb.append(
				"Get_Mergable_Tpr_against_HH[" + vehicleId + "," + vehicleName
						+ "]").append("\n");
		try {
			if (!Misc.isUndef(vehicleId)) {
				if (holder != null) {
					holdlerDataValidity = holder.isDataUseful(conn, true);
					isDataUseful = holdlerDataValidity == RFIDHolder.RF_DATA_USEABLE;
					sb.append(
							"HH_log[" + holder.getGeneratedId() + "]:"
									+ holder.getRecordKey() + ","
									+ holder.getChallanId() + ","
									+ isDataUseful).append("\n");
				}
				tpr = new TPRecord();
				tpr.setVehicleId(vehicleId);
				tpr.setTprStatus(TPR.OPEN);
				tpr.setStatus(Status.ACTIVE);
				Criteria cr = new Criteria(TPRecord.class);
				cr
						.setOrderByClause("coalesce(tp_record.combo_end,tp_record.combo_start,tp_record.tpr_create_date,tp_record.challan_date)");
				cr.setDesc(true);
				list = (ArrayList<Object>) RFIDMasterDao.select(conn, tpr, cr);
				for (int i = 0, is = list == null ? 0 : list.size(); i < is; i++) {
					TPRecord tprEntry = (TPRecord) list.get(i);
					if (tprEntry.isLatest() == 1) {
						latestTpr = tprEntry;
						break;
					}
				}
				sb.append("latest:"
						+ (latestTpr == null ? null : "("
								+ latestTpr.getTprId() + ","
								+ latestTpr.getMinesId() + ","
								+ latestTpr.getChallanNo() + ")") + "\n");
				Pair<Integer, TPRecord> mergePair = mergeWithExisting(conn,
						latestTpr, vehicleId, vehicleName, holder,
						isDataUseful, true, Misc.getUndefInt(), null, sb);
				isFound = mergePair.second != null;// mergePair.first ==
				// Status.TPR_MERGE_STATUS.MERGED;
				if (isFound) {
					tprAgainstHHLog = mergePair.second;
				}
				boolean toCreateNew = isDataUseful && !isFound;
				sb.append("toCreateNew:" + toCreateNew).append("\n");
				if (toCreateNew) {
					TPRecord newTpr = createTpr(conn, vehicleId, holder,
							vehicleName, 0, true, (isDataUseful && !isFound),
							false, Type.TPRMATERIAL.COAL, null);
					sb.append("New_TPR_Created").append("\n");
					long lastLatestTime = Misc.getUndefInt();
					if (latestTpr == null) {
						lastLatestTime = getLastLatestTime(conn, vehicleId);
						sb.append("Get_lastLatestTime:" + lastLatestTime)
								.append("\n");
					}
					boolean toMakeLatest = latestTpr == null
							&& Misc.isUndef(lastLatestTime);
					// sb.append("Make_Latest"+toMakeLatest).append("\n");
					if (!toMakeLatest && isDataUseful) {
						long newTprTime = holder != null
								&& holder.getDatetime() != null ? holder
								.getDatetime().getTime() : Misc.getUndefInt();
						long latestTprTime = latestTpr == null ? lastLatestTime
								: latestTpr.getLastProcessedTime(); // (
						// latestTpr.getComboEnd()
						// != null ?
						// latestTpr.getComboEnd().getTime()
						// :
						// (latestTpr.getChallanDate()
						// != null ?
						// latestTpr.getChallanDate().getTime()
						// :
						// Misc.getUndefInt()))
						// ;
						sb.append(
								"HH_Log[" + holder.getGeneratedId() + "]:"
										+ newTprTime).append("\n");
						sb.append(
								"Curr_TPR["
										+ (latestTpr != null ? latestTpr
												.getTprId() : null) + "]:"
										+ latestTprTime).append("\n");
						if (!Misc.isUndef(newTprTime)
								&& !Misc.isUndef(latestTprTime)
								&& (newTprTime - latestTprTime) >= (newLatestTprWebThreshold))
							toMakeLatest = true;

					}
					sb.append("Make_Latest:" + toMakeLatest).append("\n");
					if (toMakeLatest) {
						newTpr.setLatest(1);
						newTpr.setStatus(Status.ACTIVE);
						latestTpr = newTpr;
					}
					tprAgainstHHLog = newTpr;
					// sb.append(newTpr.toString());
				} else {// 
					if (latestTpr != null
							&& mergePair.first == Status.TPR_MERGE_STATUS.FILL_BOTH_CURRENT
							|| mergePair.first == Status.TPR_MERGE_STATUS.FILL_RHS_CURRENT) {
						updateTpr(
								conn,
								vehicleId,
								holder,
								vehicleName,
								latestTpr,
								latestTpr.isLatest(),
								true,
								isDataUseful,
								mergePair.first == Status.TPR_MERGE_STATUS.FILL_RHS_CURRENT,
								Type.TPRMATERIAL.COAL, null);
						tprAgainstHHLog = latestTpr;
						sb.append("Merge_With_latest:" + latestTpr.getTprId())
								.append("\n");
					}
				}

				if (tprAgainstHHLog == null) {// SHOULD NOT GET HERE
					if (holder != null) {
						sb.append("[BUG]exec_unreachable_code:"
								+ holder.getVehicleId() + ","
								+ holder.getChallanId());
						holder.printData();
						tprAgainstHHLog = createTpr(conn, vehicleId, holder,
								vehicleName, 0, true, isDataUseful, false,
								Type.TPRMATERIAL.COAL, null);
						if (tprAgainstHHLog != null)
							tprAgainstHHLog.setStatus(100);
					}
				}
				if (tprAgainstHHLog != null) {
					tprAgainstHHLog.setDebugStr(sb.toString());
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (sb != null)
				System.out.println(sb.toString());
		}

		return tprAgainstHHLog;
	}

	public static Triple<TPRecord, Integer, Boolean> getLatestNonWeb(
			Connection conn, int vehicleId, RFIDHolder holder,
			String vehicleName, int workstationTypeId, int materialCat)
			throws Exception {
		return getLatestNonWeb(conn, vehicleId, holder, vehicleName,
				workstationTypeId, materialCat, true, true);
	}

	public static Triple<TPRecord, Integer, Boolean> getLatestNonWeb(
			Connection conn, int vehicleId, RFIDHolder holder,
			String vehicleName, int workstationTypeId, int materialCat,
			boolean useMPLMerge) throws Exception {
		return getLatestNonWeb(conn, vehicleId, holder, vehicleName,
				workstationTypeId, materialCat, useMPLMerge, true);
	}

	public static Triple<TPRecord, Integer, Boolean> getLatestNonWeb(
			Connection conn, int vehicleId, RFIDHolder holder,
			String vehicleName, int workstationTypeId, int materialCat,
			boolean useMPLMerge, boolean createNew) throws Exception {
		return getLatestNonWeb(conn, vehicleId, holder, vehicleName,
				workstationTypeId, materialCat, useMPLMerge, createNew, null);
	}

	synchronized public static Triple<TPRecord, Integer, Boolean> getLatestNonWeb(
			Connection conn, int vehicleId, RFIDHolder holder,
			String vehicleName, int workstationTypeId, int materialCat,
			boolean useMPLMerge, boolean createNew,
			ProcessStepProfile processStepProfile) throws Exception {
		StringBuilder sb = new StringBuilder();
		TPRecord tpr = null;
		TPRecord latestTpr = null;
		int status = Status.VALIDATION.NO_ISSUE;
		ArrayList<Object> list = null;
		boolean isFound = false;
		boolean isDataUseful = false;
		boolean isSameStationProcessing = false;
		int holdlerDataValidity = Misc.getUndefInt();
		sb.append(
				"Get_Latest[" + vehicleId + "," + vehicleName + ","
						+ workstationTypeId + ","
						+ Type.WorkStationType.getString(workstationTypeId)
						+ "," + materialCat + "]").append("\n");
		int isMultiple = Misc.getUndefInt();
		int openTPRCout = 0;
		try {
			if (!Misc.isUndef(vehicleId)) {
				if (holder != null) {
					holdlerDataValidity = holder.isDataUseful(conn, false);
					isDataUseful = holdlerDataValidity == RFIDHolder.RF_DATA_USEABLE;
					sb.append(
							"Card_Data:" + holder.getRecordKey() + ","
									+ holder.getChallanId() + ","
									+ isDataUseful).append("\n");
				} else {
					sb.append("Card_Data_Is_Null\n");
				}
				tpr = new TPRecord();
				tpr.setVehicleId(vehicleId);
				tpr.setTprStatus(TPR.OPEN);
				tpr.setStatus(Status.ACTIVE);
				Criteria cr = new Criteria(TPRecord.class);
				cr
						.setOrderByClause("coalesce(tp_record.combo_end,tp_record.combo_start,tp_record.tpr_create_date,tp_record.challan_date)");
				cr.setDesc(true);
				list = (ArrayList<Object>) RFIDMasterDao.select(conn, tpr, cr);
				for (int i = 0, is = list == null ? 0 : list.size(); i < is; i++) {
					TPRecord tprEntry = (TPRecord) list.get(i);
					if (workstationTypeId == Type.WorkStationType.GATE_IN_TYPE
							|| workstationTypeId == Type.WorkStationType.FLY_ASH_IN_TYPE
							|| workstationTypeId == Type.WorkStationType.REGISTRATION)
						openTPRCout++;
					else if (tprEntry.getComboStart() != null)
						openTPRCout++;
					if (tprEntry.isLatest() == 1 && latestTpr == null) {
						latestTpr = tprEntry;
						// break;
					}
				}

				isSameStationProcessing = isSameWorkStation(latestTpr,
						workstationTypeId);
				sb.append("isSameStationProcessing:" + isSameStationProcessing
						+ "\n");
				boolean toCreateNew = createNew
						&& (((latestTpr == null && workstationTypeId != Type.WorkStationType.GATE_OUT_TYPE) || (!isSameStationProcessing
						// && isGreaterThanEqualsProcessedOld(latestTpr,
						// workstationTypeId);
						&& isGreaterThanEqualsProcessed(latestTpr,
								workstationTypeId, materialCat,
								processStepProfile))) || (workstationTypeId != Type.WorkStationType.SECL_REG
								&& workstationTypeId != Type.WorkStationType.SECL_LOAD_GATE_IN
								&& workstationTypeId != Type.WorkStationType.SECL_LOAD_GATE_OUT
								&& workstationTypeId != Type.WorkStationType.SECL_UNLOAD_GATE_IN
								&& workstationTypeId != Type.WorkStationType.SECL_UNLOAD_GATE_OUT
								&& workstationTypeId != Type.WorkStationType.GATE_OUT_TYPE
								&& workstationTypeId != Type.WorkStationType.REGISTRATION
								&& latestTpr != null && (!Misc
								.isUndef(latestTpr.getMaterialCat())
								&& !Misc.isUndef(materialCat) && latestTpr
								.getMaterialCat() != materialCat)));
				sb.append("toCreateNew:" + toCreateNew).append("\n");
				if (toCreateNew) {
					// isDataUseful passed as false so that holder is not merged
					// right now - instead will be merged with mergedProc depe
					// on TAT
					TPRecord newTpr = createTpr(conn, vehicleId, holder,
							vehicleName, 0, false, false, false, materialCat,
							processStepProfile);
					newTpr.setLastLatestTprId(latestTpr == null ? Misc
							.getUndefInt() : latestTpr.getTprId());
					openTPRCout++;

					// boolean toMakeLatest = latestTpr == null || !isWeb;
					boolean toMakeLatest = true;// latestTpr == null || holder
					// == null;

					if (toMakeLatest) {
						newTpr.setLatest(1);
						latestTpr = newTpr;
					}
					sb.append("New_Latest_Tpr_Obj_Created\n");
					// sb.append(newTpr.toString());
				}
				sb.append("latest:"
						+ (latestTpr == null ? null : "("
								+ latestTpr.getTprId()
								+ ","
								+ latestTpr.getMinesId()
								+ ","
								+ latestTpr.getChallanNo()
								+ ","
								+ (latestTpr != null ? latestTpr
										.getComboStart() : Misc.getUndefInt())
								+ ")") + "\n");

				// mpl specific
				if (useMPLMerge) {
					Pair<Integer, TPRecord> mergePair = mergeWithExisting(conn,
							latestTpr, vehicleId, vehicleName, holder,
							isDataUseful, false, materialCat,
							processStepProfile, sb);
					isFound = mergePair.second != null;// mergePair.first ==
					// Status.TPR_MERGE_STATUS.MERGED;
					if (isFound
							&& mergePair.second.getTprId() != latestTpr
									.getTprId()) {
						sb.append("Card_Merge_With_Non_Latest_DB_Commit")
								.append("\n");
						insertUpdateTpr(conn, mergePair.second);
						conn.commit();
					} else if (mergePair.first == Status.TPR_MERGE_STATUS.CREATE_NEW) {
						TPRecord newTpr = createTpr(conn, vehicleId, holder,
								vehicleName, 0, false, true, false,
								materialCat, processStepProfile);
						boolean toMakeLatest = false;
						if (!toMakeLatest && isDataUseful) {
							long newTprTime = holder != null
									&& holder.getDatetime() != null ? holder
									.getDatetime().getTime() : Misc
									.getUndefInt();
							long latestTprTime = latestTpr != null
									&& latestTpr.getChallanDate() != null ? latestTpr
									.getChallanDate().getTime()
									: Misc.getUndefInt(); // (
							// latestTpr.getComboEnd()
							// != null ?
							// latestTpr.getComboEnd().getTime()
							// :
							// (latestTpr.getChallanDate()
							// != null ?
							// latestTpr.getChallanDate().getTime()
							// :
							// Misc.getUndefInt()))
							// ;
							sb.append(
									"Card[" + holder.getGeneratedId() + "]:"
											+ newTprTime).append("\n");
							sb.append(
									"Latest_TPR["
											+ (latestTpr != null ? latestTpr
													.getTprId() : null) + "]:"
											+ latestTprTime).append("\n");
							if (!Misc.isUndef(newTprTime)
									&& (Misc.isUndef(latestTprTime) || (newTprTime - latestTprTime) >= 1000))
								toMakeLatest = true;
						}
						sb.append("Make_Latest:" + toMakeLatest).append("\n");
						if (toMakeLatest) {
							newTpr.setLatest(1);
							latestTpr = newTpr;
						} else {
							insertUpdateTpr(conn, newTpr);
							conn.commit();
						}
						sb.append("Create_New_TPR_Against_Card_Data:"
								+ newTpr.getTprId() + "\n");
					}
					if (mergePair.first == Status.TPR_MERGE_STATUS.FILL_BOTH_CURRENT
							|| mergePair.first == Status.TPR_MERGE_STATUS.FILL_RHS_CURRENT) {
						updateTpr(
								conn,
								vehicleId,
								holder,
								vehicleName,
								latestTpr,
								latestTpr.isLatest(),
								false,
								isDataUseful,
								mergePair.first == Status.TPR_MERGE_STATUS.FILL_RHS_CURRENT,
								materialCat, processStepProfile);
						sb.append("Card_Merge_With_Latest").append("\n");
					}
				}
			}
			// mpl specific end
			if (latestTpr != null) {
				isMultiple = openTPRCout > 1 ? 1 : Misc.getUndefInt();
				sb.append("Multi_Open:" + (openTPRCout > 1)).append("\n");
				latestTpr.setIsMultipleOpenTPR(isMultiple);
				latestTpr.changeWtInTons();
				latestTpr.setDebugStr(sb.toString());
				latestTpr.setWorkingArea(workstationTypeId);
				latestTpr.setWeighmentStep(workstationTypeId);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			if (sb != null)
				System.out.println(sb.toString());
		}

		return new Triple<TPRecord, Integer, Boolean>(latestTpr, status,
				isSameStationProcessing);
	}

	private static Pair<Integer, TPRecord> mergeWithExisting(Connection conn,
			TPRecord latestTPR, int vehicleId, String vehicleName,
			RFIDHolder holder, boolean isDataUseful, boolean isWeb,
			int materialCat, ProcessStepProfile processStepProfile,
			StringBuilder sb, String orgName) {
		if (holder == null || Misc.isUndef(holder.getVehicleId())
				|| !isDataUseful)
			return new Pair<Integer, TPRecord>(Misc.getUndefInt(), null);
		try {

			// check in rhs
			TPRecord rightMatchedTPRId = holder.getMatchingRightTprId(conn);
			if (rightMatchedTPRId != null
					&& !Misc.isUndef(rightMatchedTPRId.getTprId())) {
				sb.append("Merge_RHS_key[" + rightMatchedTPRId.getTprId() + ","
						+ rightMatchedTPRId.getVehicleId() + "]\n");
				return new Pair<Integer, TPRecord>(
						Status.TPR_MERGE_STATUS.MERGED, rightMatchedTPRId);
			} else {
				sb.append("Not_Merge_RHS\n");
			}

			// check in lhs
			TPRecord leftMatchedTPRId = holder.getMatchingLeftTprId(conn);
			if (leftMatchedTPRId != null
					&& !Misc.isUndef(leftMatchedTPRId.getTprId())) {
				sb.append("Merge_LHS_CM[" + leftMatchedTPRId.getTprId() + ","
						+ leftMatchedTPRId.getVehicleId() + "]\n");
				// if rhs empty
				if (leftMatchedTPRId.isRightEmpty()) {
					sb.append("RHS_EMPTY[" + leftMatchedTPRId.getTprId()
							+ "]\n");
					updateTpr(conn, vehicleId, holder, vehicleName,
							leftMatchedTPRId, Misc.getUndefInt(), isWeb,
							isDataUseful, true, materialCat,
							processStepProfile, orgName);
					return new Pair<Integer, TPRecord>(
							Status.TPR_MERGE_STATUS.MERGED, leftMatchedTPRId);
				} else {// holder == lhs but holder != rhs
					sb.append("RHS_NON_EMPTY_Unmergable["
							+ leftMatchedTPRId.getTprId() + "]\n");
					TPRecord errorTPR = createTpr(conn, vehicleId, holder,
							vehicleName, Misc.getUndefInt(), isWeb,
							isDataUseful, false, materialCat,
							processStepProfile, orgName);
					errorTPR.setStatus(100);// UNMEREGABLE
					return new Pair<Integer, TPRecord>(
							Status.TPR_MERGE_STATUS.UNMEREGABLE, errorTPR);
				}
			} else {
				sb.append("Not_Merge_LHS\n");
			}
			if (latestTPR != null
					&& latestTPR.isEmpty()
					&& (latestTPR.getMaterialCat() == holder.getMaterial())
					&& ((latestTPR.getComboStart() != null && holder
							.isAllowedToFillTprRHS(conn, holder.getMinesId(),
									latestTPR.getComboStart().getTime(), sb)) || !isWeb) // in
			// case
			// of
			// newly
			// created
			// tpr
			// having
			// card
			// data.
			) {
				sb.append("Merge_Latest_FULL_EMPTY[" + latestTPR.getTprId()
						+ "]\n");
				return new Pair<Integer, TPRecord>(
						Status.TPR_MERGE_STATUS.FILL_BOTH_CURRENT, latestTPR);// in
				// case
				// of
				// non
				// web,
				// we
				// want
				// the
				// update
				// to
				// latestTPR
				// to
				// happen
				// upon
				// save
			} else {
				sb.append("Not_FULL_Fill_Latest\n");
			}
			// check if it is merged with latestTPR that is left fill, rt empty
			// and holder from card
			if (!isWeb
					&& latestTPR != null
					&& (latestTPR.getMaterialCat() == holder.getMaterial())
					&& latestTPR.isRightEmpty()
					&& !latestTPR.isLeftEmpty()
					&& latestTPR.getComboStart() != null
					&& holder.isAllowedToFillTprRHS(conn, latestTPR
							.getMinesId(), latestTPR.getComboStart().getTime(),
							sb)) {
				sb.append("Merge_Latest_RHS_EMPTY_NON_WEB["
						+ latestTPR.getTprId() + "]\n");
				return new Pair<Integer, TPRecord>(
						Status.TPR_MERGE_STATUS.FILL_RHS_CURRENT, latestTPR);// //in
				// case
				// of
				// non
				// web,
				// we
				// want
				// the
				// update
				// to
				// latestTPR
				// to
				// happen
				// upon
				// save
			} else {
				sb.append("Not_RHS_Fill_Latest\n");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		sb.append("Not_Mergable_Create_New\n");
		return new Pair<Integer, TPRecord>(Status.TPR_MERGE_STATUS.CREATE_NEW,
				null);
	}

	private static Pair<Integer, TPRecord> mergeWithExisting(Connection conn,
			TPRecord latestTPR, int vehicleId, String vehicleName,
			RFIDHolder holder, boolean isDataUseful, boolean isWeb,
			int materialCat, ProcessStepProfile processStepProfile,
			StringBuilder sb) {
		if (holder == null || Misc.isUndef(holder.getVehicleId())
				|| !isDataUseful)
			return new Pair<Integer, TPRecord>(Misc.getUndefInt(), null);
		try {

			// check in rhs
			TPRecord rightMatchedTPRId = holder.getMatchingRightTprId(conn);
			if (rightMatchedTPRId != null
					&& !Misc.isUndef(rightMatchedTPRId.getTprId())) {
				sb.append("Merge_RHS_key[" + rightMatchedTPRId.getTprId() + ","
						+ rightMatchedTPRId.getVehicleId() + "]\n");
				return new Pair<Integer, TPRecord>(
						Status.TPR_MERGE_STATUS.MERGED, rightMatchedTPRId);
			} else {
				sb.append("Not_Merge_RHS\n");
			}

			// check in lhs
			TPRecord leftMatchedTPRId = holder.getMatchingLeftTprId(conn);
			if (leftMatchedTPRId != null
					&& !Misc.isUndef(leftMatchedTPRId.getTprId())) {
				sb.append("Merge_LHS_CM[" + leftMatchedTPRId.getTprId() + ","
						+ leftMatchedTPRId.getVehicleId() + "]\n");
				// if rhs empty
				if (leftMatchedTPRId.isRightEmpty()) {
					sb.append("RHS_EMPTY[" + leftMatchedTPRId.getTprId()
							+ "]\n");
					updateTpr(conn, vehicleId, holder, vehicleName,
							leftMatchedTPRId, Misc.getUndefInt(), isWeb,
							isDataUseful, true, materialCat, processStepProfile);
					return new Pair<Integer, TPRecord>(
							Status.TPR_MERGE_STATUS.MERGED, leftMatchedTPRId);
				} else {// holder == lhs but holder != rhs
					sb.append("RHS_NON_EMPTY_Unmergable["
							+ leftMatchedTPRId.getTprId() + "]\n");
					TPRecord errorTPR = createTpr(conn, vehicleId, holder,
							vehicleName, Misc.getUndefInt(), isWeb,
							isDataUseful, false, materialCat,
							processStepProfile);
					errorTPR.setStatus(100);// UNMEREGABLE
					return new Pair<Integer, TPRecord>(
							Status.TPR_MERGE_STATUS.UNMEREGABLE, errorTPR);
				}
			} else {
				sb.append("Not_Merge_LHS\n");
			}
			if (latestTPR != null
					&& latestTPR.isEmpty()
					&& (latestTPR.getMaterialCat() == holder.getMaterial())
					&& ((latestTPR.getComboStart() != null && holder
							.isAllowedToFillTprRHS(conn, holder.getMinesId(),
									latestTPR.getComboStart().getTime(), sb)) || !isWeb) // in
			// case
			// of
			// newly
			// created
			// tpr
			// having
			// card
			// data.
			) {
				sb.append("Merge_Latest_FULL_EMPTY[" + latestTPR.getTprId()
						+ "]\n");
				return new Pair<Integer, TPRecord>(
						Status.TPR_MERGE_STATUS.FILL_BOTH_CURRENT, latestTPR);// in
				// case
				// of
				// non
				// web,
				// we
				// want
				// the
				// update
				// to
				// latestTPR
				// to
				// happen
				// upon
				// save
			} else {
				sb.append("Not_FULL_Fill_Latest\n");
			}
			// check if it is merged with latestTPR that is left fill, rt empty
			// and holder from card
			if (!isWeb
					&& latestTPR != null
					&& (latestTPR.getMaterialCat() == holder.getMaterial())
					&& latestTPR.isRightEmpty()
					&& !latestTPR.isLeftEmpty()
					&& latestTPR.getComboStart() != null
					&& holder.isAllowedToFillTprRHS(conn, latestTPR
							.getMinesId(), latestTPR.getComboStart().getTime(),
							sb)) {
				sb.append("Merge_Latest_RHS_EMPTY_NON_WEB["
						+ latestTPR.getTprId() + "]\n");
				return new Pair<Integer, TPRecord>(
						Status.TPR_MERGE_STATUS.FILL_RHS_CURRENT, latestTPR);// //in
				// case
				// of
				// non
				// web,
				// we
				// want
				// the
				// update
				// to
				// latestTPR
				// to
				// happen
				// upon
				// save
			} else {
				sb.append("Not_RHS_Fill_Latest\n");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		sb.append("Not_Mergable_Create_New\n");
		return new Pair<Integer, TPRecord>(Status.TPR_MERGE_STATUS.CREATE_NEW,
				null);
	}

	public static void insertUpdateTpr(Connection conn, TPRecord tpr)
			throws Exception {
		insertUpdateTpr(conn, tpr, false);
	}

	public static void insertUpdateTpr(Connection conn, TPRecord tpr,
			boolean closeAlreadyOpen) throws Exception {
		handleUpdateTPRInserUpdate(conn, tpr, closeAlreadyOpen, false);
		handleUpdateTPRInserUpdate(conn, tpr, closeAlreadyOpen, true);
	}

	public static void handleUpdateTPRInserUpdate(Connection conn,
			TPRecord tpr, boolean closeAlreadyOpen, boolean doApprvd)
			throws Exception {
		try {
			if (tpr == null)
				return;
			// change wt. in tons
			tpr.changeWtInTons();
			if (!isTprExist(conn, tpr.getTprId(), doApprvd)) {
				if (tpr.isLatest() == 1) {
					clearLatestTprForVehicle(conn, tpr.getVehicleId(),
							closeAlreadyOpen, doApprvd);
				}
				RFIDMasterDao.insert(conn, tpr, doApprvd);
				System.out.println("[NEW TPR INSERTED]:" + tpr != null ? tpr
						.getTprId() : Misc.getUndefInt());
			} else {
				RFIDMasterDao.update(conn, tpr, doApprvd);
				System.out.println("[TPR Updated]:" + tpr != null ? tpr
						.getTprId() : Misc.getUndefInt());
			}
			if (tpr.getDebugStr() != null) {
				saveTraceToDB(conn, tpr.getTprId(), tpr.getVehicleId(), tpr
						.getDebugStr());
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public static boolean isTprExist(Connection conn, int tprId, boolean apprvd) {
		if (Misc.isUndef(tprId))
			return false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select 1 from tp_record"
					+ (apprvd ? "_apprvd" : "") + " where tpr_id=?");
			Misc.setParamInt(ps, tprId, 1);
			rs = ps.executeQuery();
			if (rs.next())
				return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return false;
	}

	public static void clearLatestTprForVehicle(Connection conn, int vehicleId,
			boolean closeAllOpen, boolean doApprvd) throws Exception {
		PreparedStatement ps = null;
		try {
			if (closeAllOpen)
				ps = conn
						.prepareStatement("update tp_record"
								+ (doApprvd ? "_apprvd" : "")
								+ " set tpr_status=2 where tpr_status = 0 and vehicle_id=?");
			else
				ps = conn
						.prepareStatement("update tp_record"
								+ (doApprvd ? "_apprvd" : "")
								+ " set is_latest=0 where tpr_status = 0 and vehicle_id=?");
			Misc.setParamInt(ps, vehicleId, 1);
			System.out.println("[Query]["
					+ DBConnectionPool.getPrintableConnectionStr(conn) + "]:"
					+ ps.toString());
			ps.executeUpdate();
			ps.clearParameters();
			/*
			 * Misc.closePS(ps); if(closeAllOpen) ps =conn.prepareStatement(
			 * "update tp_record_apprvd set tpr_status=2 where tpr_status = 0 and vehicle_id=?"
			 * ); else ps =conn.prepareStatement(
			 * "update tp_record_apprvd set is_latest=0 where tpr_status = 0 and vehicle_id=?"
			 * ); Misc.setParamInt(ps, vehicleId, 1); ps.executeUpdate();
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			Misc.closePS(ps);
		}
	}

	synchronized public static TPStep getTpStep(Connection conn, TPRecord tpr,
			int workStationType, int workStationId, int updatedBy) {
		TPStep retval = null;
		// try {
		retval = new TPStep();
		retval.setTprId(tpr.getTprId());
		retval.setVehicleId(tpr.getVehicleId());
		retval.setEntryTime(new Date());
		retval.setWorkStationType(workStationType);
		retval.setWorkStationId(workStationId);
		retval.setUpdatedBy(updatedBy);
		/*
		 * }catch(Exception ex){ ex.printStackTrace(); }
		 */
		return retval;
	}

	public static void main(String[] arg) throws GenericException {
		boolean destroyIt = false;
		Connection conn = null;
		Pair<Integer, String> vehPair = null;
		int vehicleId = 23661;
		try {

			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			TPRecord _tpr = TPRInformation.getLastCompletedTPRForView(conn, 6);
			System.out.println(_tpr.getTprId());
			
			if(false) {
			
			if (false) {
				RFIDHolder cardData = new RFIDHolder();
				RFIDHolder manualData = new RFIDHolder();
				manualData.setRefTPRId(79085);
				manualData.setMinesId(2);
				manualData.setChallanId("20178321");
				manualData.setVehicleId(23517);
				int id = manualData.getConflictingTPRId(conn);

				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date d = sdf.parse("2016-03-12 13:14:11");
				System.out.println(d.getTime());

				if (true)
					return;
			}
			RFIDHolder hh1 = new RFIDHolder(vehicleId, "JH10AM4297", 1,
					"2016-03-18 08:30", "ch2201", "lr2201", 1, 1, true);
			RFIDHolder hh2 = new RFIDHolder(vehicleId, "JH10AM4297", 1,
					"2016-03-18 15:30", "ch2202", "lr2202", 1, 1, true);
			RFIDHolder hh3 = new RFIDHolder(vehicleId, "JH10AM4297", 1,
					"2016-03-18 18:30", "ch2203", "lr2203", 1, 1, true);
			RFIDHolder hh4 = new RFIDHolder(vehicleId, "JH10AM4297", 1,
					"2016-03-18 20:30", "ch2204", "lr2204", 1, 1, true);
			RFIDHolder hh5 = new RFIDHolder(vehicleId, "JH10AM4297", 1,
					"2016-03-18 22:30", "ch2205", "lr2205", 1, 1, true);

			ArrayList<RFIDHolder> webData = new ArrayList<RFIDHolder>();
			// webData.add(hh1);
			webData.add(hh2);
			webData.add(hh3);
			webData.add(hh4);
			webData.add(hh5);

			// first trip
			Triple<TPRecord, Integer, Boolean> tpr1 = getLatestNonWeb(conn,
					vehicleId, null, "", Type.WorkStationType.GATE_IN_TYPE,
					Type.TPRMATERIAL.COAL);
			if (tpr1 != null) {// gateIn
				TPRecord gIn = tpr1.first;
				gIn.setEarliestUnloadGateInEntry(hh1
						.getDateFromStr("2016-03-18 10:25"));
				gIn.setLatestUnloadGateInExit(hh1
						.getDateFromStr("2016-03-18 10:26"));
				gIn.setComboStart(hh1.getDateFromStr("2016-03-18 10:25"));
				// gIn.setTprCreateDate(hh1.getDateFromStr("2016-03-18 10:25"));
				gIn.setTprStatus(0);
				insertUpdateTpr(conn, gIn);
				conn.commit();
			}

			TPRecord tpr_hh1 = TPRInformation.getTPRForHHWeb(conn, vehicleId,
					hh1, hh1.getVehicleName());
			if (tpr_hh1 != null && hh1 != null) {
				TPRInformation.insertUpdateTpr(conn, tpr_hh1);
				conn.commit();
			}
			tpr1 = getLatestNonWeb(conn, vehicleId, hh2, "",
					Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE,
					Type.TPRMATERIAL.COAL);
			if (tpr1 != null) {// wbIn
				TPRecord wIn = tpr1.first;
				// wIn.setChallanNo("ch2201");
				// wIn.setChallanDate(hh1.getDateFromStr("2016-03-18 09:45"));
				// wIn.setLrNo("lr2201");
				wIn.setMinesId(1);
				wIn.setTransporterId(1);
				wIn.setMaterialGradeId(1);
				wIn.setTprStatus(2);
				wIn.setEarliestUnloadWbInEntry(hh1
						.getDateFromStr("2016-03-18 10:28"));
				wIn.setLatestUnloadWbInExit(hh1
						.getDateFromStr("2016-03-18 10:30"));
				wIn.setComboEnd(hh1.getDateFromStr("2016-03-18 10:30"));
				insertUpdateTpr(conn, wIn);
				conn.commit();
			}
			// second trip
			tpr1 = getLatestNonWeb(conn, vehicleId, null, "",
					Type.WorkStationType.GATE_IN_TYPE, Type.TPRMATERIAL.COAL);
			if (tpr1 != null) {// gateIn
				TPRecord gIn = tpr1.first;
				gIn.setEarliestUnloadGateInEntry(hh1
						.getDateFromStr("2016-03-18 12:25"));
				gIn.setLatestUnloadGateInExit(hh1
						.getDateFromStr("2016-03-18 12:26"));
				gIn.setComboStart(hh1.getDateFromStr("2016-03-18 12:25"));
				// gIn.setTprCreateDate(hh1.getDateFromStr("2016-03-18 12:25"));
				gIn.setTprStatus(0);
				insertUpdateTpr(conn, gIn);
				conn.commit();
			}
			tpr1 = getLatestNonWeb(conn, vehicleId, hh2, "",
					Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE,
					Type.TPRMATERIAL.COAL);
			if (tpr1 != null) {// wbIn
				TPRecord wIn = tpr1.first;
				/*
				 * wIn.setChallanNo("ch2202");
				 * wIn.setChallanDate(hh1.getDateFromStr("2016-03-18 11:45"));
				 * wIn.setLrNo("lr2202");
				 */
				wIn.setMinesId(1);
				wIn.setTransporterId(1);
				wIn.setMaterialGradeId(1);
				// wIn.setTprStatus(2);
				wIn.setEarliestUnloadWbInEntry(hh1
						.getDateFromStr("2016-03-18 12:28"));
				wIn.setLatestUnloadWbInExit(hh1
						.getDateFromStr("2016-03-18 12:30"));
				wIn.setComboEnd(hh1.getDateFromStr("2016-03-18 12:30"));
				insertUpdateTpr(conn, wIn);
				conn.commit();
			}
			for (RFIDHolder holder : webData) {
				TPRecord tpr = TPRInformation.getTPRForHHWeb(conn, vehicleId,
						holder, holder.getVehicleName());
				if (tpr != null && holder != null) {
					TPRInformation.insertUpdateTpr(conn, tpr);
					conn.commit();
				}
			}

			if (true)
				return;

			tpr1 = getLatestNonWeb(conn, vehicleId, null, "",
					Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE,
					Type.TPRMATERIAL.COAL);
			if (tpr1 != null) {
				TPRecord yIn = tpr1.first;
				yIn.setEarliestUnloadYardInEntry(new Date());
				yIn.setLatestUnloadYardInExit(new Date());
			}
			tpr1 = getLatestNonWeb(conn, vehicleId, null, "",
					Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE,
					Type.TPRMATERIAL.COAL);
			if (tpr1 != null) {
				TPRecord wIn = tpr1.first;
				wIn.setEarliestUnloadWbInEntry(new Date());
				wIn.setLatestUnloadWbInExit(new Date());
			}
			tpr1 = getLatestNonWeb(conn, vehicleId, null, "",
					Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE,
					Type.TPRMATERIAL.COAL);
			if (tpr1 != null) {
				TPRecord wIn = tpr1.first;
				wIn.setEarliestUnloadWbInEntry(new Date());
				wIn.setLatestUnloadWbInExit(new Date());
			}
			tpr1 = getLatestNonWeb(conn, vehicleId, null, "",
					Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE,
					Type.TPRMATERIAL.COAL);
			if (tpr1 != null) {
				TPRecord wIn = tpr1.first;
				wIn.setEarliestUnloadWbInEntry(new Date());
				wIn.setLatestUnloadWbInExit(new Date());
			}
			tpr1 = getLatestNonWeb(conn, vehicleId, null, "",
					Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE,
					Type.TPRMATERIAL.COAL);
			if (tpr1 != null) {
				TPRecord wIn = tpr1.first;
				wIn.setEarliestUnloadWbInEntry(new Date());
				wIn.setLatestUnloadWbInExit(new Date());
			}

			Pair<Integer, String> bed = TPRUtils.getBedAllignment(conn, 1, 2,
					3, 4);
			System.out.println(vehicleId);
		
		 	}
		} catch (Exception ex) {
			ex.printStackTrace();
			destroyIt = true;
		} finally {
			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
		}
	}

	public static boolean isSameWorkStation(TPRecord tpr, int workStationType) {
		if (tpr == null || Misc.isUndef(workStationType)
				|| tpr.getPreStepType() != workStationType)
			return false;
		long currMillis = System.currentTimeMillis();
		long lastProcessedTime = tpr.getWorkStationOutTime(workStationType);
		return !Misc.isUndef(lastProcessedTime)
				&& ((currMillis - lastProcessedTime) < sameStationTprThreshold);
	}

	public static boolean isGreaterThanEqualsProcessed(TPRecord tpr,
			int workStationType, int materialCat,
			ProcessStepProfile processStepProfile) {
		if (tpr == null || Misc.isUndef(workStationType))
			return false;
		if (workStationType == Type.WorkStationType.GATE_OUT_TYPE)
			materialCat = tpr.getMaterialCat();
		if (Misc.isUndef(materialCat))// ( materialCat < Type.TPRMATERIAL.COAL
			// || materialCat >
			// Type.TPRMATERIAL.COAL_WASHERY)
			return false;
		if (processStepProfile != null
				&& processStepProfile.getProcessStep() != null) {
			boolean found = false;
			if (false && materialCat == Type.TPRMATERIAL.COAL_OTHER) {
				if (!Misc.isUndef(tpr.getWorkStationOutTime(workStationType))) {
					return true;
				}
			} else {
				for (int i = 0; i < processStepProfile.getProcessStep().size(); i++) {
					int processStepId = processStepProfile.getProcessStep()
							.get(i);
					if (!found) {
						found = processStepId == workStationType;
						if (!found)
							continue;
					}
					if (!Misc.isUndef(tpr.getWorkStationOutTime(processStepId))) {
						return true;
					}
				}
			}
			return false;
		}
		Pair<Integer, Integer> startEnd = processList.get(materialCat);
		if (workStationType < startEnd.first
				|| workStationType > startEnd.second)
			return false;
		for (int i = workStationType; i <= startEnd.second; i++) {
			if (!Misc.isUndef(tpr.getWorkStationOutTime(i))) {
				return true;
			}
		}
		return false;
	}

	private static boolean isGreaterThanEqualsProcessedOld(TPRecord latestTpr,
			int workstationTypeId) {
		return
		// (!isSameStationProcessing &&
		// !Misc.isUndef(lastWorkstationProcessedTime))
		(
		// create
		// &&
		((
		// no latest exist - use this for in plant processing
		// latestTpr.getEarliestUnloadGateInEntry() != null
		latestTpr.getLatestUnloadGateInExit() != null && workstationTypeId <= Type.WorkStationType.GATE_IN_TYPE)
				|| (
				// latestTpr.getEarliestUnloadWbInEntry() != null
				latestTpr.getLatestUnloadWbInExit() != null && workstationTypeId <= Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE)
				|| (
				// latestTpr.getEarliestUnloadYardInEntry() != null
				latestTpr.getLatestUnloadYardInExit() != null && workstationTypeId <= Type.WorkStationType.YARD_IN_TYPE)
				|| (
				// latestTpr.getEarliestUnloadYardOutEntry() != null
				latestTpr.getLatestUnloadYardOutExit() != null && workstationTypeId <= Type.WorkStationType.YARD_OUT_TYPE)
				|| (
				// latestTpr.getEarliestUnloadWbOutEntry() != null
				latestTpr.getLatestUnloadWbOutExit() != null && workstationTypeId <= Type.WorkStationType.WEIGH_BRIDGE_OUT_TYPE)
				|| (
				// latestTpr.getEarliestUnloadGateOutEntry() != null
				latestTpr.getLatestUnloadGateOutExit() != null && workstationTypeId < Type.WorkStationType.GATE_OUT_TYPE)
				|| (
				// latestTpr.getEarliestUnloadGateOutEntry() != null
				latestTpr.getLatestUnloadGateOutExit() != null && workstationTypeId < Type.WorkStationType.GATE_OUT_TYPE)
				|| (
				// latestTpr.getEarliestUnloadGateOutEntry() != null
				latestTpr.getLatestLoadGateInExit() != null
						&& workstationTypeId > Type.WorkStationType.GATE_IN_TYPE && workstationTypeId < Type.WorkStationType.FLY_ASH_IN_TYPE)
				|| (
				// latestTpr.getEarliestUnloadGateOutEntry() != null
				latestTpr.getLatestLoadWbInExit() != null
						&& workstationTypeId > Type.WorkStationType.GATE_IN_TYPE && workstationTypeId < Type.WorkStationType.FLY_ASH_TARE_WT_TYPE) || (
		// latestTpr.getEarliestUnloadGateOutEntry() != null
		latestTpr.getLatestLoadWbOutExit() != null
				&& workstationTypeId > Type.WorkStationType.GATE_IN_TYPE && workstationTypeId < Type.WorkStationType.FLY_ASH_GROSS_WT_TYPE)));
	}

	public static boolean closeTPR(Connection conn, int tprId) throws Exception {
		if (conn == null || Misc.isUndef(tprId))
			return false;
		try {
			RFIDMasterDao.executeQuery(conn,
					"update tp_record set tpr_status=2 where tpr_id=" + tprId);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return true;
	}

	private static void saveTraceToDB(Connection conn, int tprId,
			int vehicleId, String trace) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select flag from merge_debug_flag");
			rs = ps.executeQuery();
			boolean insert = false;
			if (rs.next()) {
				insert = Misc.getRsetInt(rs, 1) == 1;
			}
			if (insert) {
				ps.clearParameters();
				ps = null;
				ps = conn
						.prepareStatement("insert into merge_process_log(tpr_id,vehicle_id,trace,updated_on) values (?,?,?,now())");
				Misc.setParamInt(ps, tprId, 1);
				Misc.setParamInt(ps, tprId, 2);
				ps.setString(3, trace);
				ps.execute();
			}
			Misc.closePS(ps);
			Misc.closeRS(rs);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static long getLastLatestTime(Connection conn, int vehicleId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		long retval = Misc.getUndefInt();
		try {
			ps = conn
					.prepareStatement("select coalesce(tp_record.combo_end,tp_record.combo_start,tp_record.tpr_create_date,tp_record.challan_date) last_time from tp_record where vehicle_id=? and is_latest=1 and status=1 order by coalesce(tp_record.combo_end,tp_record.combo_start,tp_record.tpr_create_date,tp_record.challan_date) desc  limit 1");
			Misc.setParamInt(ps, vehicleId, 1);
			rs = ps.executeQuery();
			if (rs.next()) {
				retval = Misc.getDateInLong(rs, 1);
			}
			Misc.closePS(ps);
			Misc.closeRS(rs);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return retval;
	}

	public static String getChallanNo(boolean isServer, String stationCode,
			String serverCode, String seriesNo, int uniqueNumber) {
		StringBuilder challanNo = new StringBuilder();
		challanNo.append(isServer ? "1" : "2");
		challanNo.append(Utils.getStdString(stationCode, 10, "0"));
		challanNo.append(Utils.getStdString(serverCode, 1, "0"));
		challanNo.append(Utils.getStdString(seriesNo, 1, "0"));
		challanNo.append(Utils.getStdString(uniqueNumber + "", 8, "0"));
		return challanNo.toString();
	}

	public static String getInvoiceNo(String unitCode, String stationCode,
			String finacialYear, int uniqueNumber) {
		String commodity = "C";
		String mode = "2";
		StringBuilder retval = new StringBuilder();
		retval.append(Utils.getStdString(unitCode, 4, "0"));
		retval.append(Utils.getStdString(finacialYear, 2, "0"));
		retval.append(Utils.getStdString(stationCode, 2, "0"));
		retval.append(Utils.getStdString(commodity, 1, "0"));
		retval.append(Utils.getStdString(mode, 1, "0"));
		retval.append(Utils.getStdString(Misc.isUndef(uniqueNumber) ? "1"
				: uniqueNumber + "", 6, "0"));
		return retval.toString();
	}

	public static void setTPRNextStepType(Connection conn, int tprId,
			int nextStep, boolean apprvd) throws Exception {
		if (Misc.isUndef(tprId))
			return;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update tp_record"
					+ (apprvd ? "_apprvd" : "")
					+ " set next_step_type=? where tpr_id=?");
			Misc.setParamInt(ps, nextStep, 1);
			Misc.setParamInt(ps, tprId, 2);
			System.out.println("[UPD][NEXT][STEP]" + ps.toString());
			ps.executeUpdate();
		} catch (Exception ex) {
			throw ex;
		} finally {
			Misc.closePS(ps);
		}
	}

	public static String getModChallanNo(String challanNo) {
		String retval = challanNo;
		if (retval != null && retval.length() > 0) {
			int series = Misc.getParamAsInt(retval.substring(0, 1));
			retval = (Misc.isUndef(series) || series < 2 ? "3" : ""
					+ (series + 1))
					+ retval.substring(1);
		}
		return retval;
	}

	public static void setChallanNo(Connection conn, int tprId,
			String challanNo, boolean isApprvd) throws SQLException {
		if (Misc.isUndef(tprId) || Utils.isNull(challanNo))
			return;
		PreparedStatement ps = conn.prepareStatement("update tp_record"
				+ (isApprvd ? "_apprvd" : "")
				+ " set challan_no=?, remote_tpr_id=null where tpr_id=?");
		ps.setString(1, challanNo);
		Misc.setParamInt(ps, tprId, 2);
		ps.executeUpdate();
	}

	public static int getVehicleForUpdate(Connection conn, int vehicleId)
			throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int retval = Misc.getUndefInt();
		try {
			ps = conn
					.prepareStatement("select id from vehicle where id=? and status=1 order by updated_on limit 1 for update");
			Misc.setParamInt(ps, vehicleId, 1);
			rs = ps.executeQuery();
			if (rs.next())
				retval = Misc.getRsetInt(rs, 1);
		} catch (Exception ex) {
			throw ex;
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}

	synchronized public static void processTPRMerge(Connection toConn,
			Connection fromConn, int tprId, int recordSrc, long clockGap)
			throws Exception {
		System.out.println("[Merge][TPR]-id-" + tprId + "-start");
		PreparedStatement psRemote = null;
		psRemote = toConn
				.prepareStatement("update tp_record set remote_tpr_id=null where tpr_id=?");
		try {
			TPRecord tpr = (TPRecord) RFIDMasterDao.get(fromConn,
					TPRecord.class, tprId);
			int remoteId = TPRInformation.handleMergeSECL(toConn, tpr,
					clockGap, fromConn, recordSrc);
			updateRemoteTPRUpd(fromConn, tprId, remoteId, tpr.getChallanNo(),
					false);
			updateRemoteTPRUpd(fromConn, tprId, remoteId, tpr.getChallanNo(),
					true);

			Misc.setParamInt(psRemote, remoteId, 1);
			psRemote.executeUpdate();
			psRemote.clearParameters();

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			Misc.closePS(psRemote);
		}
		System.out.println("[Merge][TPR]-id-" + tprId + "-end");
	}

	public static void updateRemoteTPRUpd(Connection conn, int tprId,
			int remoteTPRId, String challanNo, boolean isApprvd)
			throws Exception {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update tp_record"
					+ (isApprvd ? "_apprvd" : "")
					+ " set remote_tpr_id=?,challan_no=? where tpr_id=?");
			Misc.setParamInt(ps, remoteTPRId, 1);
			ps.setString(2, challanNo);
			Misc.setParamInt(ps, tprId, 3);
			System.out.println(Thread.currentThread().toString() + "["
					+ DBConnectionPool.getPrintableConnectionStr(conn) + "]"
					+ ps.toString());
			ps.executeUpdate();
		} catch (Exception ex) {
			throw ex;
		} finally {
			Misc.closePS(ps);
		}
	}
}
