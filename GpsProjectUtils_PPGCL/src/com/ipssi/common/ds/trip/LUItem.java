package com.ipssi.common.ds.trip;

import java.io.Serializable;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import com.ipssi.RegionTest.RegionTest;
import com.ipssi.cache.DirTimeInfo;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.gen.utils.Misc;
import com.ipssi.geometry.Point;
import com.ipssi.processor.utils.GpsData;

public class LUItem implements Serializable, Comparable<LUItem> {
	
	private static final long serialVersionUID = 1L;
	
	private byte eventId;
	private byte opAreaType = 0;
	private byte dalaStrikeEtc = 0;
	private byte flags = 0;
	private int regionIdOrEngineEventId;
	//2014_06_20 		private int priority = 1;
	//2014_06_20 	private int threshold = 1;
	//CHANGE13 private GpsData gpsData = null;
	private long gpsDataRecordTime = Misc.getUndefInt();
	transient private GpsData cachedGpsData = null; //will store only for STOP_START and those at beg/end of  ... clean up will be done separately
	//private long value = 0;
	
//	private int confirmed = 0; // 0 means dont know
	
//	private LUItemAddnl dirHiInfo = null;
	
	//private DirTimeInfo dirCalcInfo= null;
	public void setAsDataLossSource(boolean isDL) {
		if (isDL)
			flags &= (byte) 0x01;
		else
			flags &= (~(byte) 0x01); 
	}
	public boolean isDataLossSource() {
		return (flags & 0x01) != 0;
	}
	private static SimpleDateFormat dbgFormatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

	private static String dbgFormat(Date dt) {
		return dt == null ? "null" : dbgFormatter.format(dt);
	}

	public static int hackConvert0123dirToEvent(int dir) {
		//0 NONE
		//5 : LO
		//6: Super
		//7: Hi
		//8: Super & Lo
		//9: Hi & LO
		if (dir >=0 && dir <= 3)
			dir = dir == 1 ? TripInfoConstants.DIR_CHANGE_EVENT_LO : dir == 2 ? TripInfoConstants.DIR_CHANGE_EVENT_HI : dir == 0 ? TripInfoConstants.DIR_CHANGE_EVENT_NONE : TripInfoConstants.DIR_CHANGE_EVENT_BOTH;
		else if (dir >= 5 && dir <= 9) 
			dir = dir == 5 ? TripInfoConstants.DIR_CHANGE_EVENT_LO 
					: dir == 6 ? TripInfoConstants.DIR_CHANGE_EVENT_SUPER
					: dir == 7 ? TripInfoConstants.DIR_CHANGE_EVENT_HI 
					: dir == 8 ? TripInfoConstants.DIR_CHANGE_EVENT_SUPER_BOTH
					: TripInfoConstants.DIR_CHANGE_EVENT_BOTH;			
		return dir;
	}
	
	public static int getDir0123(int eventId) {//returns -1 if this one is really not a stop
		//0 NONE
		//5 : LO
		//6: Super
		//7: Hi
		//8: Super & Lo
		//9: Hi & LO
		return eventId == TripInfoConstants.DIR_CHANGE_EVENT_LO ? 5 
				: eventId == TripInfoConstants.DIR_CHANGE_EVENT_HI ? 7
				:eventId == TripInfoConstants.DIR_CHANGE_EVENT_SUPER ? 6
				: eventId == TripInfoConstants.DIR_CHANGE_EVENT_BOTH  ? 9
				:eventId == TripInfoConstants.DIR_CHANGE_EVENT_SUPER_BOTH ? 8 : 0;
	}
	
	public static boolean isSuppressedEvent(int dir) {
		return dir == TripInfoConstants.DIR_CHANGE_EVENT_BOTH_SUPP || dir == TripInfoConstants.DIR_CHANGE_EVENT_HI_SUPP || dir == TripInfoConstants.DIR_CHANGE_EVENT_LO_SUPP 
		|| dir == TripInfoConstants.DIR_CHANGE_EVENT_SUPER_BOTH_SUPP || dir == TripInfoConstants.DIR_CHANGE_EVENT_SUPER_SUPP; 
	}
	public static int isMeBetterDirChange(int meDir, int rhsDir) {
		//shit hack ... sometimes we pass 0,1,2,3 type stuff
		
		meDir = hackConvert0123dirToEvent(meDir);
		rhsDir = hackConvert0123dirToEvent(rhsDir);
		boolean mesupp = isSuppressedEvent(meDir);
		boolean rhssupp = isSuppressedEvent(rhsDir);
		if (mesupp)
			meDir = 0;
		if (rhssupp)
			rhsDir = 0;
		return meDir > rhsDir ? 1 : meDir == rhsDir ? 0 : -1;
		/*
		if (meDir == rhsDir || (mesupp && rhssupp)) {
			return 0;
		}
		else {
			if (rhsDir == 0 || rhsDir == TripInfoConstants.DIR_CHANGE_EVENT_NONE || isSuppressedEvent(rhsDir))
				return 1;
			else if (meDir == 0 || meDir == TripInfoConstants.DIR_CHANGE_EVENT_NONE || isSuppressedEvent(meDir))
				return -1;
			else if (meDir == TripInfoConstants.DIR_CHANGE_EVENT_BOTH)
				return 1;
			else if (rhsDir == TripInfoConstants.DIR_CHANGE_EVENT_BOTH)
				return -1;
			else if (meDir == TripInfoConstants.DIR_CHANGE_EVENT_LO)
				return 1;
			
			return -1;
		}
			*/
			
	}
	public int isMeBetterDirChange(LUItem rhs) {// 0 if cant tell, 1 if me is better than rhs, -1 if me is less than rhs
		int meDir = this.eventId;
		int rhsDir = rhs.eventId;
		return isMeBetterDirChange(meDir, rhsDir);
	}
	public static boolean isExtFromShovel(int eventId) {
		return eventId == TripInfoConstants.EXT_SHOVEL_LOAD
		|| eventId == TripInfoConstants.EXT_SHOVEL_READ
		|| eventId == TripInfoConstants.EXT_SHOVEL_UNREAD
		;
	}
	public static boolean isExtFromDirect(int eventId) {
		return eventId == TripInfoConstants.EXT_DALA_UP
		|| eventId == TripInfoConstants.EXT_DALA_DOWN
		|| eventId == TripInfoConstants.EXT_STRIKE
		;
	}
	public static boolean isExtEvent(int eventId) {
		return isExtFromShovel(eventId) || isExtFromDirect(eventId);
	}
	public static boolean isExtFromShovelStart(int eventId) {
		return eventId == TripInfoConstants.EXT_SHOVEL_LOAD
		|| eventId == TripInfoConstants.EXT_SHOVEL_READ
		;
	}
	public static boolean isExtFromShovelEnd(int eventId) {
		return eventId == TripInfoConstants.EXT_SHOVEL_UNREAD
		;
	}
	public static boolean isExtFromDirectStart(int eventId) {
		return eventId == TripInfoConstants.EXT_DALA_UP
		|| eventId == TripInfoConstants.EXT_STRIKE
		;
	}
	public static boolean isExtFromDirectEnd(int eventId) {
		return eventId == TripInfoConstants.EXT_DALA_DOWN
		;
	}

	public boolean isExtFromShovel() {
		return isExtFromShovel(eventId);
	}
	public boolean isExtFromDirect() {
		return isExtFromDirect(eventId);
	}
	public boolean isExtEvent() {
		return isExtFromShovel(eventId) || isExtFromDirect(eventId);
	}
	public boolean isExtFromShovelStart() {
		return isExtFromShovelStart(eventId);
	}
	public boolean isExtFromShovelEnd() {
		return isExtFromShovelEnd(eventId);
	}
	public boolean isExtFromDirectStart() {
		return isExtFromDirectStart(eventId);
	}
	public boolean isExtFromDirectEnd() {
		return isExtFromDirectEnd(eventId);
	}

	public boolean isRegion() {
		return !isStop(eventId) && !isExtFromShovel(eventId) && !isExtFromDirect(eventId);
	}
	public boolean isSameEvent(LUItem rhs) {
		boolean retval = false;
		if (rhs == null)
			return retval;
		
		if (eventId == TripInfoConstants.EXT_STRIKE || eventId == TripInfoConstants.EXT_SHOVEL_LOAD)
			return false;
		
		if (isStop() || isDirChange() || isExtFromDirect()) {
			return eventId == rhs.getEventId();
		}
		if (isWaitOutOrConfirmatory() && rhs.isWaitOutOrConfirmatory())
			return true;
		if (this.getRegionId() == rhs.getRegionId())
			return true;
		return false;
	}
	public int getEngineEventId() {
		return this.regionIdOrEngineEventId; //check must be done outside - unsafe //isStop() ? this.regionIdOrEngineEventId : Misc.getUndefInt();
	}
	public void setEngineEventId(int engineEventId) {
		if (isStop())
			this.regionIdOrEngineEventId  = engineEventId;
	}

	public void setEventId(int id) {
		
		if (id < 0 || isWaitOutOnly(id) || Misc.isUndef(id))
			id = TripInfoConstants.LOAD_WAIT_OUT;
		if (!isStop() && isStop(id))
			this.regionIdOrEngineEventId = 0;
		if (isStop() && !isStop(id))
			this.regionIdOrEngineEventId = Misc.getUndefInt();
		this.eventId = (byte)id;
	}
	public void setAsStopStrict(boolean isStop) {
		this.setAsDataLossSource(false);
		setEventId(isStop ? TripInfoConstants.STOP_START_EVENT : TripInfoConstants.STOP_END_EVENT);
	}
	public void setAsStopDataLoss(boolean isStop) {
		this.setAsDataLossSource(true);
		setEventId(isStop ? TripInfoConstants.g_doDataLossAsNewType ? TripInfoConstants.STOP_START_EVENT_DATALOSS : TripInfoConstants.STOP_START_EVENT : TripInfoConstants.STOP_END_EVENT);
	}
	public static boolean isConfirmatory(int eventId) {
		return eventId == TripInfoConstants.AREA_OF_CONFIRM_EXIT || eventId == TripInfoConstants.AREA_OF_WORK_EXIT;
	}
	
	public boolean isConfirmatory() {
		return isConfirmatory(eventId);
	}

	public String toString() {
		
		synchronized (dbgFormatter) {
			String name = null;
			try {
				name = this.isStop() ? "region:-1111111" : RegionTest.getRegionInfo(getRegionId(), null) != null ? RegionTest.getRegionInfo(getRegionId(), null).region.m_name : "(null)";
			}
			catch (Exception e) {
				name = "region:"+getRegionId();
			}
			
			String regionEvent = "";
			/*
			 * +",WaitIn: " +isWaitIn()+",GateIn: " +isGateIn()+",AreaIn: " +isAreaIn() +",GateOut: " +isGateOut()+",AreaOut: " +isAreaOut()+",WaitOut: " +isWaitOut()
			 */
			regionEvent = isLoad() ? " Load " : " Unload ";
			if (isAreaIn())
				regionEvent = regionEvent + "[AreaIn]";
			else if (isAreaOut())
				regionEvent = regionEvent + "[AreaOut]";
			else if (isGateIn())
				regionEvent = regionEvent + "[GateIn]";
			else if (isGateOut())
				regionEvent = regionEvent + "[GateOut]";
			else if (isWaitIn())
				regionEvent = regionEvent + "[WaitIn]";
			else if (isWaitOutOnly())
				regionEvent = regionEvent + "[WaitOut]";
			else if (isConfirmatory())
				regionEvent = regionEvent + "[Confirm]";
			else if (isStopStartStrict())
				regionEvent = regionEvent + "[Stop Start]";
			else if (isStopStartDataLoss())
				regionEvent = regionEvent + "[Stop Start DL]";
			else if (isStopEnd())
				regionEvent = regionEvent + "[Stop End]";
			else if (isDirChange())
				regionEvent = regionEvent + "[DirChange]";
			
			return "[Event: " + eventId + ",ee:"+(this.isStop() ? this.regionIdOrEngineEventId : Misc.getUndefInt())+",Region: " + name //2014_06_20 	 +",priority: " + (priority == Misc.getUndefInt() ? "-1" : priority) +
			       
			//2014_06_20 	 ",threshold: " + (threshold == Misc.getUndefInt() ? "Undef" : priority) 
			+ ", " + opAreaType + "," + dbgFormat(new Date(gpsDataRecordTime)) + ", " + regionEvent + "" + "]";
		}

	}

	public LUItem(long data) {
		this.eventId = Misc.getUndefByte();
		this.regionIdOrEngineEventId = LUItem.isStop(eventId) ? 0 : Misc.getUndefInt();
		//CHANGE13 this.gpsData = data;
			gpsDataRecordTime = data;
			//2014_06_20 			this.priority = Misc.getUndefInt();
		this.opAreaType = Misc.getUndefByte();
		//2014_06_20 			this.threshold = Misc.getUndefInt();
	}

	public LUItem(GpsData data) {
		this.eventId = Misc.getUndefByte();
		this.regionIdOrEngineEventId = Misc.getUndefInt();
		//CHANGE13 this.gpsData = data;
		if (data != null) {
			gpsDataRecordTime = data.getGps_Record_Time();
			//value =  (long)(data.getValue()*1000);
		}
		//2014_06_20 			this.priority = Misc.getUndefInt();
		this.opAreaType = Misc.getUndefByte();
		//2014_06_20 			this.threshold = Misc.getUndefInt();
	}
	public void setAllInfo(int eventId, int regionId, GpsData gpsData, int priority, int threshold, int opAreaType) {
		this.regionIdOrEngineEventId = regionId;
		if (eventId == TripInfoConstants.UNLOAD_WAIT_OUT || Misc.isUndef(eventId))
			eventId = TripInfoConstants.LOAD_WAIT_OUT;
		byte evB =  Misc.isUndef(eventId)? Misc.getUndefByte() : (byte) eventId;
		setEventId(evB);
		if (gpsData != null) {
			//CHANGE13 this.gpsData = gpsData;
			this.gpsDataRecordTime = gpsData.getGps_Record_Time();
			//this.value = (int)(gpsData.getValue()*1000);
		}
		//2014_06_20 			this.priority = priority;
		//2014_06_20 			this.threshold = threshold;
		this.opAreaType = Misc.isUndef(opAreaType) ? Misc.getUndefByte() : (byte) opAreaType;
	}
	public LUItem(int eventId, int regionId, GpsData gpsData, int priority, int threshold, int opAreaType) {
		this.regionIdOrEngineEventId = regionId;
		if (eventId == TripInfoConstants.UNLOAD_WAIT_OUT || Misc.isUndef(eventId))
			eventId = TripInfoConstants.LOAD_WAIT_OUT;
		byte evB =  Misc.isUndef(eventId)? Misc.getUndefByte() : (byte) eventId;
		setEventId(evB);

		//CHANGE13 this.gpsData = gpsData;
		if (gpsData != null) {
			gpsDataRecordTime = gpsData.getGps_Record_Time();
		//	value =  (long)(gpsData.getValue()*1000);
		}
		else {
			gpsDataRecordTime = Misc.getUndefInt();
			//value = 0;
		}
		
		//2014_06_20 			this.priority = priority;
		//2014_06_20 			this.threshold = threshold;
		this.opAreaType = Misc.isUndef(opAreaType) ? Misc.getUndefByte() : (byte) opAreaType;
	}
	public LUItem(int eventId, int regionId, LUItem timeValOfThis, int priority, int threshold, int opAreaType) {
		this.regionIdOrEngineEventId = regionId;
		if (eventId == TripInfoConstants.UNLOAD_WAIT_OUT || Misc.isUndef(eventId))
			eventId = TripInfoConstants.LOAD_WAIT_OUT;
		byte evB =  Misc.isUndef(eventId)? Misc.getUndefByte() : (byte) eventId;
		setEventId(evB);

		//CHANGE13 this.gpsData = gpsData;
		if (timeValOfThis != null) {
			gpsDataRecordTime = timeValOfThis.getTime();
		//	value =  (long)(timeValOfThis.getValue()*1000);
		}
		else {
			gpsDataRecordTime = Misc.getUndefInt();
			//value = 0;
		}
		
		//2014_06_20 			this.priority = priority;
		//2014_06_20 			this.threshold = threshold;
		this.opAreaType = Misc.isUndef(opAreaType) ? Misc.getUndefByte() : (byte) opAreaType;
	}
	public void setArtificial(int millSecTimeAdj) {
		//CHANGE13 this.gpsData = new GpsData(gpsData);
		//CHANGE13 long dt = gpsData.getGps_Record_Time();
		//CHANGE13 dt = dt+millSecTimeAdj;
		//CHANGE13 gpsData.setGps_Record_Time(dt);
		gpsDataRecordTime += millSecTimeAdj;
	}

	public LUItem(LUItem item) {
		this.eventId = (byte) item.getEventId();
		
		this.regionIdOrEngineEventId = item.getRegionId();
		//CHANGE13 this.gpsData = item.getGpsData();
		this.gpsDataRecordTime = item.gpsDataRecordTime;
		//this.value = item.value;
		//2014_06_20 			this.priority = item.getPriority();
		//2014_06_20 			this.threshold = item.getThreshold();
		this.opAreaType = (byte) item.getOpAreaType();
	}

	public LUItem(LUItem item, boolean deepCopy) {// deep copy
		this.eventId = (byte)item.getEventId();
		this.regionIdOrEngineEventId = item.getRegionId();
		//2014_06_20 			this.priority = item.getPriority();
		//2014_06_20 			this.threshold = item.getThreshold();
		this.opAreaType = (byte) item.getOpAreaType();

		/*
		 * private Point point; private Date gps_Record_Time; private Date gpsRecvTime; private DimensionInfo dimensionInfo; private ChannelTypeEnum sourceChannel; private String
		 * name; private int gpsRecordingId; private double speed = Misc.getUndefDouble(); private double orientation = Misc.getUndefDouble(); private transient ModelState
		 * modelState = null; private transient String strData = null;
		 */
		/*CHANGE13
		GpsData old = item.getGpsData();
		GpsData newData = new GpsData();
//		newData.setPoint(new Point(old.getPoint().getX(), old.getPoint().getY()));
		newData.setLongitude(old.getLongitude());
		newData.setLatitude(old.getLatitude());
		newData.setGps_Record_Time(old.getGps_Record_Time());
		newData.setGpsRecvTime(old.getGpsRecvTime());
		newData.setDimensionInfo(old.getDimId(), old.getValue());
	//	newData.setName(old.getName());
		newData.setGpsRecordingId(old.getGpsRecordingId());
		newData.setSpeed(old.getSpeed());
		newData.setOrientation(old.getOrientation());
		newData.setModelState(old.getModelState(null));
		newData.setStrData(old.getStrData());
		this.gpsData = newData;
		*/
		this.gpsDataRecordTime = item.gpsDataRecordTime; 
		//this.value = item.value;
		
	}

	public void flip(boolean prevIsLoad) {
		switch (this.eventId) {
		case TripInfoConstants.LOAD_AREA_IN:
			this.setEventId(prevIsLoad ? TripInfoConstants.UNLOAD_AREA_IN : TripInfoConstants.LOAD_AREA_IN);
			break;
		case TripInfoConstants.LOAD_AREA_OUT:
			this.setEventId(prevIsLoad ? TripInfoConstants.UNLOAD_AREA_OUT : TripInfoConstants.LOAD_AREA_OUT);
			break;
		case TripInfoConstants.LOAD_WAIT_IN:
			this.setEventId(prevIsLoad ? TripInfoConstants.UNLOAD_WAIT_IN : TripInfoConstants.LOAD_WAIT_IN);
			break;
		case TripInfoConstants.LOAD_WAIT_OUT:
			this.setEventId(prevIsLoad ? TripInfoConstants.UNLOAD_WAIT_OUT : TripInfoConstants.LOAD_WAIT_OUT);
			break;
		case TripInfoConstants.LOAD_GATE_IN:
			this.setEventId(prevIsLoad ? TripInfoConstants.UNLOAD_GATE_IN : TripInfoConstants.LOAD_GATE_IN);
			break;
		case TripInfoConstants.LOAD_GATE_OUT:
			this.setEventId(prevIsLoad ?   TripInfoConstants.UNLOAD_GATE_OUT : TripInfoConstants.LOAD_GATE_OUT);
			break;
		case TripInfoConstants.UNLOAD_AREA_IN:
			this.setEventId( !prevIsLoad ? TripInfoConstants.LOAD_AREA_IN : TripInfoConstants.UNLOAD_AREA_IN);
			break;
		case TripInfoConstants.UNLOAD_AREA_OUT:
			this.setEventId( !prevIsLoad ? TripInfoConstants.LOAD_AREA_OUT : TripInfoConstants.UNLOAD_AREA_OUT);
			break;
		case TripInfoConstants.UNLOAD_WAIT_IN:
			this.setEventId(!prevIsLoad ? TripInfoConstants.LOAD_WAIT_IN : TripInfoConstants.UNLOAD_WAIT_IN);
			break;
		case TripInfoConstants.UNLOAD_WAIT_OUT:
			this.setEventId(!prevIsLoad ? TripInfoConstants.LOAD_WAIT_OUT :TripInfoConstants.UNLOAD_WAIT_OUT );
			break;
		case TripInfoConstants.UNLOAD_GATE_IN:
			this.setEventId(!prevIsLoad ? TripInfoConstants.LOAD_GATE_IN : TripInfoConstants.UNLOAD_GATE_IN);
			break;
		case TripInfoConstants.UNLOAD_GATE_OUT:
			this.setEventId(!prevIsLoad ? TripInfoConstants.LOAD_GATE_OUT  : TripInfoConstants.UNLOAD_GATE_OUT);
			break;
		}
	}

//2014_06_20	public void markArtifical(long time) {
	//2014_06_20			//CHANGE13 this.gpsData.setGps_Record_Time(this.getTime() + time);
	//2014_06_20			gpsDataRecordTime += time;
	//2014_06_20			this.isArtifical = true;
	//2014_06_20		}

	public boolean isLoad() {
		return eventId < TripInfoConstants.UNLOAD_WAIT_IN;
	}

	public boolean isWaitOutOrConfirmatory() {
		return isWaitOutOnly(eventId) || isConfirmatory(eventId);
	}

	public boolean isWaitOutOnly() {
		return isWaitOutOnly(eventId);
	}
	
	public boolean isWaitIn() {
		return eventId == TripInfoConstants.LOAD_WAIT_IN || eventId == TripInfoConstants.UNLOAD_WAIT_IN;
	}

	public boolean isAreaIn() {
		return eventId == TripInfoConstants.LOAD_AREA_IN || eventId == TripInfoConstants.UNLOAD_AREA_IN;
	}

	public boolean isAreaOut() {
		return eventId == TripInfoConstants.LOAD_AREA_OUT || eventId == TripInfoConstants.UNLOAD_AREA_OUT;
	}

	public boolean isGateIn() {
		return eventId == TripInfoConstants.LOAD_GATE_IN || eventId == TripInfoConstants.UNLOAD_GATE_IN;
	}

	public boolean isGateOut() {
		return eventId == TripInfoConstants.LOAD_GATE_OUT || eventId == TripInfoConstants.UNLOAD_GATE_OUT;
	}
	
	public static boolean isNonRegionEvent(int eventId) {
		return eventId >= TripInfoConstants.STOP_START_EVENT && eventId <= TripInfoConstants.STOP_START_EVENT_DATALOSS;
		//return eventId == TripInfoConstants.STOP_START_EVENT || eventId == TripInfoConstants.STOP_END_EVENT || 
		//eventId == TripInfoConstants.DIR_CHANGE_EVENT_LO
		 //|| eventId == TripInfoConstants.DIR_CHANGE_EVENT_HI  || eventId == TripInfoConstants.DIR_CHANGE_EVENT_BOTH
		 //|| eventId == TripInfoConstants.DIR_CHANGE_EVENT_LO_SUPP
		 //|| eventId == TripInfoConstants.DIR_CHANGE_EVENT_HI_SUPP  || eventId == TripInfoConstants.DIR_CHANGE_EVENT_BOTH_SUPP
		 //|| eventId == TripInfoConstants.DIR_CHANGE_EVENT_NONE;
	}
	
	public static boolean isStopStartExt(int eventId) {
		return eventId == TripInfoConstants.STOP_START_EVENT || eventId == TripInfoConstants.STOP_START_EVENT_DATALOSS;
	}
	public boolean isStopStartStrict() {
		return isStopStartStrict(eventId);
	}
	public boolean isStopStartDataLoss() {
		return isStopStartDataLoss(eventId);
	}
	public static boolean isStopStartStrict(int eventId) {
		return eventId == TripInfoConstants.STOP_START_EVENT;
	}
	public static boolean isStopStartDataLoss(int eventId) {
		return eventId == TripInfoConstants.STOP_START_EVENT_DATALOSS;
	}
	public static boolean isStopEnd(int eventId) {
		return eventId == TripInfoConstants.STOP_END_EVENT;	
	}
	
	public static boolean isStop(int eventId) {
		return eventId == TripInfoConstants.STOP_START_EVENT || eventId == TripInfoConstants.STOP_END_EVENT || eventId == TripInfoConstants.STOP_START_EVENT_DATALOSS;
	}
	
	public static boolean isDirChange(int eventId) {
		return eventId >= TripInfoConstants.DIR_CHANGE_EVENT_LO && eventId <= TripInfoConstants.DIR_CHANGE_EVENT_NONE;
		//return eventId == TripInfoConstants.DIR_CHANGE_EVENT_LO || eventId == TripInfoConstants.DIR_CHANGE_EVENT_HI || eventId == TripInfoConstants.DIR_CHANGE_EVENT_BOTH
		// || eventId == TripInfoConstants.DIR_CHANGE_EVENT_LO_SUPP
		// || eventId == TripInfoConstants.DIR_CHANGE_EVENT_HI_SUPP  || eventId == TripInfoConstants.DIR_CHANGE_EVENT_BOTH_SUPP;	
	}
	
	public boolean isNonRegionEvent() {
		return isNonRegionEvent(eventId);
	}
	
	public  boolean isStopStartExt() {
		return isStopStartExt(eventId);
	}
	
	public  boolean isStopEnd() {
		return isStopEnd(eventId);
	}
	
	public  boolean isStop() {
		return isStop(eventId);
	}
	
	public  boolean isDirChange() {
		return isDirChange(eventId);
	}
	
	public static boolean isWaitOutOnly(int eventId) {
		return eventId == TripInfoConstants.LOAD_WAIT_OUT || eventId == TripInfoConstants.UNLOAD_WAIT_OUT || Misc.isUndef(eventId);
	}
	
	public boolean isOut() {
		return isWaitOutOrConfirmatory() || isGateOut() || isAreaOut();
	}

	public boolean isWB() { // duplicate code rel to OpArea .. fix it
		return OpArea.isWB(opAreaType);
	}

	public boolean isNormal() { // duplicate code rel to OpArea .. fix it
		return OpArea.isNormal(opAreaType);
	}

	public boolean isInsideWait() {
		return OpArea.isInsideWait(opAreaType);
	}
	public boolean isWB1() {
		return OpArea.isWB1(opAreaType);
	}

	public boolean isWB2() {
		return OpArea.isWB2(opAreaType);
	}

	public boolean isWB3() {
		return OpArea.isWB3(opAreaType);
	}

	public void setAsMoved() {
		opAreaType |= 0x100;
	}

	public void unsetAsMoved() {
		opAreaType &= ~0x100;
	}

	public void setAsStopped() {
		opAreaType |= 0x200;
	}

	public void unsetAsStopped() {
		opAreaType &= ~0x200;
	}

	public void setAsIgnOff() {
		opAreaType |= 0x400;
	}

	public void unsetAsIgnOff() {
		opAreaType &= ~0x400;
	}

	public boolean hasMoved() {
		return (opAreaType & 0x100) != 0;
	}

	public boolean hasStopped() {
		return (opAreaType & 0x200) != 0;
	}

	public int compareTo(LUItem rhs) {
		//CHANGE13 return gpsData.compareTo(rhs.getGpsData());
		long diff = gpsDataRecordTime - rhs.gpsDataRecordTime;
		return diff < 0 ? -1 : diff == 0 ? 0 : 1;
	}

	public int getEventId() {
		return this.eventId;
	}

	public int getRegionId() {
		return this.regionIdOrEngineEventId;
	}

	//public GpsData getGpsData() {
	//	return gpsData;
	//}
	
	public void setGpsData(GpsData gpsData) {
		//CHANGE13 this.gpsData = gpsData;
		if (gpsData != null) {
			gpsDataRecordTime = gpsData.getGps_Record_Time();
		//	value =  (long)(gpsData.getValue()*1000);
		}
		else {
			gpsDataRecordTime = Misc.getUndefInt();
			//value = 0;
		}
	}

	public long getGpsDataRecordTime() {
		return gpsDataRecordTime;
	}
	
	public void setGpsDataRecordTime(long tm) {
		this.gpsDataRecordTime = tm;
	}
	public void setGpsDataRecordTime(GpsData gpsData) {
		this.gpsDataRecordTime = gpsData == null ? Misc.getUndefInt() : gpsData.getGps_Record_Time();
	}
	public long getTime() {
		//CHANGE13 return gpsData == null ? Misc.getUndefInt() : gpsData.getGps_Record_Time();
		return gpsDataRecordTime;
	}
	//public double getValue() {
	//	return (double)value/1000.0;
	//}
	public GpsData getCachedGpsData() {
		return cachedGpsData;
	}
	public GpsData getGpsData(Connection conn, NewVehicleData vdp) {
		if (gpsDataRecordTime <= 0)
			return null;
		if (cachedGpsData != null)
			return cachedGpsData;
		long mod = gpsDataRecordTime % 1000;
		long tm = mod == 0 ? gpsDataRecordTime : gpsDataRecordTime/1000 * 1000;
		if (mod > 500)
			tm += 1000;
		GpsData retval =  vdp.get(conn, new GpsData(tm));
		cachedGpsData = retval;
		return retval;
	}
	
	public void clearCachedGpsData() {
		cachedGpsData = null;
	}
	public int getPriority(OpStationBean ops) {
		ArrayList<OpArea> opareas = ops.getRegionIdsListDontAdd();
		for (int i=0,is= opareas == null ? 0 : opareas.size(); i<is; i++) {
			OpArea opa = opareas.get(i);
			if (opa.id == this.getRegionId())
				return opa.priority;
		}
		return Misc.getUndefInt();
	}

	public int getThreshold(OpStationBean ops) {
		ArrayList<OpArea> opareas = ops.getRegionIdsListDontAdd();
		for (int i=0,is= opareas == null ? 0 : opareas.size(); i<is; i++) {
			OpArea opa = opareas.get(i);
			if (opa.id == this.getRegionId())
				return opa.thresholdMilliSec;
		}
		return Misc.getUndefInt();
	}

	public void setOpAreaType(int opAreaType) {
		this.opAreaType = (byte) opAreaType;
	}

	public int getOpAreaType() {
		return opAreaType;
	}

//20140708	public DirTimeInfo getDirCalcInfo() {
	//20140708		return dirCalcInfo;
	//20140708	}

//20140708	public void setDirCalcInfo(DirTimeInfo dirCalcInfo) {
//20140708		this.dirCalcInfo = dirCalcInfo;
//20140708	}


}
