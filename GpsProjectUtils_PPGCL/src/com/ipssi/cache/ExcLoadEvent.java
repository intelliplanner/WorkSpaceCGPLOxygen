package com.ipssi.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.sql.ResultSet;

import com.ipssi.common.ds.trip.ShovelSequence;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.processor.utils.GpsData;

public class ExcLoadEvent extends OtherData implements Comparable {
	public static final byte EVENT_IGNORE_ONLY = 1;
	public static final byte EVENT_OK = 0;
	public static final byte EVENT_NOT_CALC = -1;
	public static final byte EVENT_LEFT_IGNORE_MASK = 0x40;
	public static final byte EVENT_RIGHT_IGNORE_MASK = 0x20;
	
	public static final int MIN_CYCLE_GAP_MS = 11*1000;
	public static final int MAX_CYCLE_GAP_MS = 90 * 1000; //was 75 20190125
	public static final int MIN_ENDCYCLE_STOPEND_MS = 0 * 1000; //WAS 25 20190108 fin on 5 ..566@15 20190126 was 5
	public static final int MAX_ENDCYCLE_STOPEND_MS = 600 * 1000; //566@180 was 30*60
	public final static String GET_LOAD_EVENT_SEL = "select exc_load_event.id, exc_load_event.vehicle_id, exc_load_event.gps_record_time, exc_load_event.updated_on "+
	", exc_load_event.quality, exc_load_event.dig_prior_sec, exc_load_event.stick_in_sec, exc_load_event.swing_sec, exc_load_event.boom_up, exc_load_event.close_dur, exc_load_event.truck_vehicle_id "+
	",exc_load_event.src_of_truck, exc_load_event.bleRSSI, exc_load_event.strikeTimeGapSec "
		;
	public static class QInterpret {
		boolean openOPPattern;
		boolean openOTPattern;
		boolean close;
		boolean up;
		boolean stick;
		boolean swing;
		boolean isProper;
		public QInterpret(int q) {
			boolean doingv4 =  (q & 0x040) != 0;
			if (doingv4)
				q = (q & 0x03F);
			if (doingv4) {
				this.openOPPattern = ((q & 0x02) != 0);
				this.openOTPattern = ((q & 0x01) != 0);
				this.close = ((q & 0x04) != 0);
				this.up = ((q & 0x08) != 0);
				this.stick = ((q & 0x010) != 0);
				this.swing = ((q & 0x020) != 0);
			} 
			else {
				
				this.openOPPattern = true;//although we did not check in v3
				this.openOTPattern = true;
				this.close = true;
				this.up =  ((q & 0x01) != 0);
				this.stick = ((q & 0x02) != 0);
				this.swing = ((q & 0x04) != 0);
				
			}
			this.isProper =this.openOPPattern && this.openOTPattern && this.close && this.up && this.stick;
				//this.openOPPattern  && this.close && this.up && this.stick;
		}
		public boolean isMeBetter(QInterpret other) {
			QInterpret me = this;//moving code from elsewhere
			if (me.isOpenOPPattern() != other.isOpenOPPattern())
				return me.isOpenOPPattern();
			if (me.isOpenOTPattern() != other.isOpenOTPattern())
				return me.isOpenOTPattern();
			if (me.isClose() != other.isClose())
				return me.isClose();
			if (me.isUp() != other.isUp())
				return me.isUp();
			if (me.isStick() != other.isStick())
				return me.isStick();
			if (me.isSwing() != other.isSwing())
				return me.isSwing();
			return false;
		}
		public QInterpret(boolean openOPPattern, boolean openOTPattern,
				boolean close, boolean up, boolean stick, boolean swing) {
			super();
			this.openOPPattern = openOPPattern;
			this.openOTPattern = openOTPattern;
			this.close = close;
			this.up = up;
			this.stick = stick;
			this.swing = swing;
		}
		public boolean isProper() {
			return isProper;
		}
		public boolean isOpenOPPattern() {
			return openOPPattern;
		}
		
		public boolean isOpenOTPattern() {
			return openOTPattern;
		}
		
		public boolean isClose() {
			return close;
		}
		
		public boolean isUp() {
			return up;
		}
		
		public boolean isStick() {
			return stick;
		}
		
		public boolean isSwing() {
			return swing;
		}
		
	}
	
	
	private int digPriorSec;
	private int stickInSec;
	private int swingSec;
	private int boomUpSec;
	private int closeSec;
	private int truckVehicleId = Misc.getUndefInt();
	private byte quality = 0;
	private byte ignoreBecauseNeighbour = ExcLoadEvent.EVENT_NOT_CALC;
	private byte bleRSSI = 127;
	private byte strikeTimeGapSec = 127;
	private QInterpret qinterpret;
	
	
	public boolean isMeBetterThan(ExcLoadEvent other) {
		return this.getQInterpret().isMeBetter(other.getQInterpret());
	}
	public boolean isProperLoad() {
		return this.getQInterpret().isProper();
	}
	//public int  getQualityLevelNotUse() {
		// B0 - upDuringProper
	    // B1 - inDuringProper
	    // B2 - swingProper
		//QInterpret qinterpret = this.getQInterpret();
	    //return qinterpret.isUp() && qinterpret.isStick() && qinterpret.isClose() && qinterpret.isOpenOTPattern() && qinterpret.isOpenOTPattern() ?  PROPER_LOAD : qinterpret.isUp() && qinterpret.isOpenOPPattern() ?  ACCEPTABLE_LOAD : PROB_FALSE_LOAD;  
	//}
	public static ExcLoadEvent read(Connection conn, ResultSet rs) throws Exception {
//		public final static String GET_LOAD_EVENT_SEL = "select exc_load_event.id, exc_load_event.vehicle_id, exc_load_event.gps_record_time, exc_load_event.updated_on "+
//		", exc_load_event.quality, exc_load_event.dig_prior_sec, exc_load_event.stick_in_sec, exc_load_event.swing_sec, exc_load_event.boom_up, exc_load_event.close_dur, exc_load_event.truck_vehicle_id "
		int q = Misc.getRsetInt(rs, "quality");
		if (false) {
			if (!Misc.isUndef(q)) {
				q = (q &0x0FF);
				if (q >= 100) {
					q -= 100; 
				}
				q = q | 0x040;
			}
		}
		if (Misc.isUndef(q))
			q = Misc.getUndefByte();
		
		ExcLoadEvent retval = new ExcLoadEvent(Misc.getRsetInt(rs, "id"), Misc.getRsetInt(rs, "vehicle_id"), Misc.sqlToLong(rs.getTimestamp("gps_record_time")),
				Misc.sqlToLong(rs.getTimestamp("updated_on")), q,Misc.getRsetInt(rs, "dig_prior_sec"), Misc.getRsetInt(rs, "stick_in_sec"),
				Misc.getRsetInt(rs, "swing_sec"), Misc.getRsetInt(rs, "boom_up"),Misc.getRsetInt(rs, "close_dur")) 
		;
		 
		retval.setTruckVehicleId(Misc.getRsetInt(rs, "truck_vehicle_id"));
		retval.setBleRSSI((byte)Misc.getRsetInt(rs, "bleRSSI"));
		retval.setStrikeTimeGapSec((byte)Misc.getRsetInt(rs, "strikeTimeGapSec"));
		return retval;
	}
	public void saveToDBExclTruckVehicleId(Connection conn) throws Exception {
		saveToDBExclTruckVehicleId(conn, null, null, null,null);
	}
	public void saveToDBExclTruckVehicleId(Connection conn, NewExcLoadEventMgmt loadEventCache, NewVehicleData inactiveVDT, NewVehicleData vdt, CacheTrack.VehicleSetup vehSetup) throws Exception {
//		public final static String GET_LOAD_EVENT_SEL = "select exc_load_event.id, exc_load_event.vehicle_id, exc_load_event.gps_record_time, exc_load_event.updated_on "+
//		", exc_load_event.quality, exc_load_event.dig_prior_sec, exc_load_event.stick_in_sec, exc_load_event.swing_sec, exc_load_event.boom_up, exc_load_event.close_dur, exc_load_event.truck_vehicle_id "
		int durInactiveDurPrior = 0;
		int durRelPrev = Misc.getUndefInt();
		String locName = null;
		long tsGoodQualityPrevTS = 0;
		long tsAnyQualityPrevTS = 0;
		try {
			if (vehSetup == null)
				vehSetup = CacheTrack.VehicleSetup.getSetup(this.getVehicleId(), conn);
			if (loadEventCache == null)
				loadEventCache = NewExcLoadEventMgmt.getLoadEventList(conn, getVehicleId(), false);
			if (inactiveVDT == null || vdt == null) {
				VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, this.getVehicleId(), true, false);
				inactiveVDT = inactiveVDT == null ? vdf.getDataList(conn, this.getVehicleId(), Misc.EXC_IDLING_DIM_ID, false) : inactiveVDT;
				vdt = vdt == null ? vdf.getDataList(conn, this.getVehicleId(), 0, false) : vdt;
			}
			
			GpsData ref = new GpsData(this.getGpsRecordTime());
			if (vdt != null && vehSetup != null) {
				GpsData pt = vdt.get(conn, ref);
				locName = pt == null ? null : pt.getName(conn, this.getVehicleId(), vehSetup);
				if (locName != null) {
					locName = locName.trim();
					locName = locName.substring(Math.min(locName.length(), 199));
				}
			}
			if (loadEventCache != null && inactiveVDT != null) {
				int priorIndex = 0;
				ExcLoadEvent prior = loadEventCache.get(conn, this);
				if (prior != null && prior.compareTo(this) == 0) {
					priorIndex--;
					prior = loadEventCache.get(conn,this,priorIndex);
				}
				if (prior != null) {
					tsAnyQualityPrevTS = prior.getGpsRecordTime();
					ExcLoadEvent tempPrior = prior;
					
					do {
						if (tempPrior.getIgnoreBecauseNeighbour(conn,loadEventCache) == EVENT_OK) {
							tsGoodQualityPrevTS = prior.getGpsRecordTime();
							break;
						}
						priorIndex--;
						tempPrior = loadEventCache.get(conn,this,priorIndex);
					}
					while (tempPrior != null);
				}
				//check if inactive between prior & me
				if (ref == null)
					ref = new GpsData(this.getGpsRecordTime());
				GpsData priorInactive = null;
				long priorEventBookMark = prior == null ? 0 : prior.getGpsRecordTime();
				GpsData prevInactiveData = null;
				for (int i=0;;i--) {
					GpsData currInactiveData =  inactiveVDT.get(conn, ref, i);
					if (currInactiveData == null)
						break;
					if (currInactiveData.getValue() > 0.5) {
						int gap =(int)(((prevInactiveData == null ? this.getGpsRecordTime() : prevInactiveData.getGps_Record_Time())-(currInactiveData.getGps_Record_Time() <= priorEventBookMark ? priorEventBookMark : currInactiveData.getGps_Record_Time()))/1000);
						if (gap > ShovelSequence.MIN_GAP_BETWEEN_IDLINGPT_BEF_ASSUMINGACTIVE)
							durInactiveDurPrior += gap;
					}
					if (currInactiveData.getGps_Record_Time() <= priorEventBookMark)
						break;
					prevInactiveData = currInactiveData;
				}
				durRelPrev = 0;
				if (prior != null) {
					durRelPrev = (int)(this.getGpsRecordTime()-prior.getGpsRecordTime())/1000;
				}
				if (durRelPrev < durInactiveDurPrior)
					durRelPrev = durInactiveDurPrior;
				durRelPrev -= durInactiveDurPrior;
				if (durRelPrev > 3*60) {
					durInactiveDurPrior = (durRelPrev+durInactiveDurPrior)-3*60;//effective endTS - currTS - 3*60
					durRelPrev = 3*60;
				}
				
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		PreparedStatement ps = null;
		if (!Misc.isUndef(id)) {
			ps = conn.prepareStatement("update exc_load_event set vehicle_id=?, gps_record_time=?,updated_on=?, quality=?,dig_prior_sec=?,stick_in_sec=?/20,swing_sec=?/20,boom_up=?/20,close_dur=?/20, gap_rel_prior_load=?, inactive_dur_in_between=?, loc_name=?, prev_goodq_cycle=?,prev_anyq_cycle=? where id=?");
		}
		else {
			ps = conn.prepareStatement("insert into exc_load_event(vehicle_id, gps_record_time, updated_on, quality,dig_prior_sec, stick_in_sec, swing_sec, boom_up, close_dur,gap_rel_prior_load,inactive_dur_in_between,loc_name,prev_goodq_cycle,prev_anyq_cycle) values (?,?,?,?,?,?/20,?/20,?/20,?/20,?,?,?,?,?)");
		}
		int colPos = 1;
		Misc.setParamInt(ps, vehicleId, colPos++);
		ps.setTimestamp(colPos++, Misc.longToSqlDate(gpsRecordTime));
		ps.setTimestamp(colPos++, Misc.longToSqlDate(gpsRecvTime));
		Misc.setParamInt(ps, quality, colPos++);
		Misc.setParamInt(ps, this.digPriorSec, colPos++);
		Misc.setParamInt(ps, this.stickInSec, colPos++);
		Misc.setParamInt(ps, this.swingSec, colPos++);
		Misc.setParamInt(ps, this.boomUpSec, colPos++);
		Misc.setParamInt(ps, this.closeSec, colPos++);
		Misc.setParamInt(ps, durRelPrev, colPos++);
		Misc.setParamInt(ps, durInactiveDurPrior, colPos++);
		ps.setString(colPos++, locName);

		ps.setTimestamp(colPos++, Misc.longToSqlDate(tsGoodQualityPrevTS));
		ps.setTimestamp(colPos++, Misc.longToSqlDate(tsAnyQualityPrevTS));
		if (!Misc.isUndef(id)) {
			ps.setInt(colPos++, this.id);
			ps.executeUpdate();
		}
		else {
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			this.id = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
			rs = Misc.closeRS(rs);
		}
		ps = Misc.closePS(ps);
	}

	public void updateTimingExclTruckVehicleId(Connection conn, NewExcLoadEventMgmt loadEventCache, NewVehicleData inactiveVDT, NewVehicleData vdt, CacheTrack.VehicleSetup vehSetup) throws Exception {
		if (Misc.isUndef(id))
			return;

		int durInactiveDurPrior = 0;
		int durRelPrev = Misc.getUndefInt();
		long tsGoodQualityPrevTS = 0;
		long tsAnyQualityPrevTS = 0;
		try {
			if (vehSetup == null)
				vehSetup = CacheTrack.VehicleSetup.getSetup(this.getVehicleId(), conn);
			if (loadEventCache == null)
				loadEventCache = NewExcLoadEventMgmt.getLoadEventList(conn, getVehicleId(), false);
			if (inactiveVDT == null || vdt == null) {
				VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, this.getVehicleId(), true, false);
				inactiveVDT = inactiveVDT == null ? vdf.getDataList(conn, this.getVehicleId(), Misc.EXC_IDLING_DIM_ID, false) : inactiveVDT;
				vdt = vdt == null ? vdf.getDataList(conn, this.getVehicleId(), 0, false) : vdt;
			}
			
			GpsData ref = new GpsData(this.getGpsRecordTime());
			if (loadEventCache != null && inactiveVDT != null) {
				int priorIndex = 0;
				ExcLoadEvent prior = loadEventCache.get(conn, this);
				if (prior != null && prior.compareTo(this) == 0) {
					priorIndex--;
					prior = loadEventCache.get(conn,this,priorIndex);
				}
				if (prior != null) {
					tsAnyQualityPrevTS = prior.getGpsRecordTime();
					ExcLoadEvent tempPrior = prior;
					
					do {
						if (tempPrior.getIgnoreBecauseNeighbour(conn,loadEventCache) == EVENT_OK) {
							tsGoodQualityPrevTS = prior.getGpsRecordTime();
							break;
						}
						priorIndex--;
						tempPrior = loadEventCache.get(conn,this,priorIndex);
					}
					while (tempPrior != null);
				}
				//check if inactive between prior & me
				if (ref == null)
					ref = new GpsData(this.getGpsRecordTime());
				GpsData priorInactive = null;
				long priorEventBookMark = prior == null ? 0 : prior.getGpsRecordTime();
				GpsData prevInactiveData = null;
				for (int i=0;;i--) {
					GpsData currInactiveData =  inactiveVDT.get(conn, ref, i);
					if (currInactiveData == null)
						break;
					if (currInactiveData.getValue() > 0.5) {
						int gap =(int)(((prevInactiveData == null ? this.getGpsRecordTime() : prevInactiveData.getGps_Record_Time())-(currInactiveData.getGps_Record_Time() <= priorEventBookMark ? priorEventBookMark : currInactiveData.getGps_Record_Time()))/1000);
						if (gap > ShovelSequence.MIN_GAP_BETWEEN_IDLINGPT_BEF_ASSUMINGACTIVE)
							durInactiveDurPrior += gap;
					}
					if (currInactiveData.getGps_Record_Time() <= priorEventBookMark)
						break;
					prevInactiveData = currInactiveData;
				}
				durRelPrev = 0;
				if (prior != null) {
					durRelPrev = (int)(this.getGpsRecordTime()-prior.getGpsRecordTime())/1000;
				}
				if (durRelPrev < durInactiveDurPrior)
					durRelPrev = durInactiveDurPrior;
				durRelPrev -= durInactiveDurPrior;
				if (durRelPrev > 3*60) {
					durInactiveDurPrior = (durRelPrev+durInactiveDurPrior)-3*60;//effective endTS - currTS - 3*60
					durRelPrev = 3*60;
				}
				
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		PreparedStatement ps = null;
		ps = conn.prepareStatement("update exc_load_event set  gap_rel_prior_load=?, inactive_dur_in_between=?,  prev_goodq_cycle=?,prev_anyq_cycle=? where id=?");
		int colPos = 1;
		Misc.setParamInt(ps, durRelPrev, colPos++);
		Misc.setParamInt(ps, durInactiveDurPrior, colPos++);

		ps.setTimestamp(colPos++, Misc.longToSqlDate(tsGoodQualityPrevTS));
		ps.setTimestamp(colPos++, Misc.longToSqlDate(tsAnyQualityPrevTS));
		ps.setInt(colPos++, this.id);
		ps.executeUpdate();
		ps = Misc.closePS(ps);
	}

	public int compareTo(Object obj) {		
		ExcLoadEvent p = (ExcLoadEvent)obj;
		int retval = 0;
		retval = this.gpsRecordTime < p.gpsRecordTime ? -1 : this.gpsRecordTime == p.gpsRecordTime ? 0 : 1;
		return retval;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdfTime = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
		sb.append("[")
		.append("id:").append(id)
		.append(",id:").append(vehicleId)
		.append(",rt:").append(this.gpsRecordTime <= 0 ? "null" : sdfTime.format(new java.util.Date(gpsRecordTime)))
		.append(",upd:").append(this.gpsRecvTime <= 0 ? "null" : sdfTime.format(new java.util.Date(gpsRecvTime)))
		.append(",Q:").append(Integer.toBinaryString(quality)).append(",P:").append(this.isProperLoad()).append(",QL:").append(this.getQualityOld())
		.append(",dig:").append(digPriorSec)
		.append(",IN:").append(stickInSec)
		.append(",Swing:").append(swingSec)
		.append(",Up:").append(boomUpSec)
		.append(",Close:").append(closeSec)
		.append("]");
		return sb.toString();
	}

	public int getId() {
		return id;
	}
	public QInterpret getQInterpret() {
		if (this.qinterpret == null)
			this.qinterpret = new QInterpret(quality == 6 && this.stickInSec >= 3 && this.swingSec >= 6 ? 7 : quality);
		return this.qinterpret;
	}
	public boolean isV4Q() {
		return (quality & 0x040) != 0;
	}
	public int getQualityOld() {
		return quality == 6 && this.stickInSec >= 3 && this.swingSec >= 6 ? 7 : quality;
		//return quality;
	}
	public void setQuality(int quality) {
		this.quality = (byte) quality;
		
	}
	public int getDigPriorSec() {
		return digPriorSec;
	}
	public void setDigPriorSec(int digPriorSec) {
		this.digPriorSec = digPriorSec;
	}
	public int getStickInSec() {
		return stickInSec;
	}
	public void setStickInSec(int stickInSec) {
		this.stickInSec = stickInSec;
	}
	public int getSwingSec() {
		return swingSec;
	}
	public void setSwingSec(int swingSec) {
		this.swingSec = swingSec;
	}
	public int getBoomUpSec() {
		return boomUpSec;
	}
	public void setBoomUpSec(int boomUpSec) {
		this.boomUpSec = boomUpSec;
	}
	public int getCloseSec() {
		return closeSec;
	}
	public void setCloseSec(int closeSec) {
		this.closeSec = closeSec;
	}
	public int getTruckVehicleId() {
		return truckVehicleId;
	}
	public void setTruckVehicleId(int truckVehicleId) {
		this.truckVehicleId = truckVehicleId;
	}
	public ExcLoadEvent(long gpsRecordTime) {
		this.gpsRecordTime = gpsRecordTime;
	}
	public ExcLoadEvent(int id, int vehicleId, long gpsRecordTime,
			long updatedOnTime, int quality, int digPriorSec, int stickInSec,
			int swingSec, int boomUpSec, int closeSec) {
		super();
		this.id = id;
		this.vehicleId = vehicleId;
		this.gpsRecordTime = gpsRecordTime;
		this.gpsRecvTime = updatedOnTime;
		this.quality = (byte) quality;
		this.digPriorSec = digPriorSec;
		this.stickInSec = stickInSec;
		this.swingSec = swingSec;
		this.boomUpSec = boomUpSec;
		this.closeSec = closeSec;
	}
	public byte getBleRSSI() {
		return bleRSSI;
	}
	public void setBleRSSI(byte bleRSSI) {
		this.bleRSSI = bleRSSI;
	}
	public byte getStrikeTimeGapSec() {
		return strikeTimeGapSec;
	}
	public void setStrikeTimeGapSec(byte strikeTimeGapSec) {
		this.strikeTimeGapSec = strikeTimeGapSec;
	}
	public byte getIgnoreBecauseNeighbour(Connection conn, NewExcLoadEventMgmt loadEvents) {
		
		if (ignoreBecauseNeighbour == EVENT_NOT_CALC) {
			
			ExcLoadEvent prevProper = null;
			ExcLoadEvent nextProper = null;
			long meTS = this.getGpsRecordTime();
			for (int i=-1;;i--) {
				ExcLoadEvent temp = loadEvents.get(conn, this, i);
				if (temp == null)
					break;
				if ((meTS - temp.getGpsRecordTime()) > MAX_CYCLE_GAP_MS)
					break;
				if (temp.isProperLoad()) {
					prevProper = temp;
					break;
				}
			}
			for (int i=1;;i++) {
				ExcLoadEvent temp = loadEvents.get(conn, this, i);
				if (temp == null)
					break;
				if ((temp.getGpsRecordTime() - meTS) > MAX_CYCLE_GAP_MS)
					break;
				if (temp.isProperLoad()) {
					nextProper = temp;
					break;
				}
			}
			long mePrevGap = prevProper != null ? meTS - prevProper.getGpsRecordTime() : 0;
			long meNextGap = nextProper != null ? nextProper.getGpsRecordTime() - meTS : 0;
			boolean mePrevShortGap = mePrevGap > 0 && mePrevGap <= MIN_CYCLE_GAP_MS;
			boolean meNextShortGap = meNextGap > 0 && meNextGap <= MIN_CYCLE_GAP_MS;
			boolean meFullOPOT = this.isProperLoad() && (this.getQualityOld() & 0x03) == 0x03;
			boolean prevFullOPOT = mePrevShortGap && prevProper != null ? (prevProper.getQualityOld() & 0x03) == 0x03 : false;
			boolean nextFullOPOT = meNextShortGap && nextProper != null ? (nextProper.getQualityOld() & 0x03) == 0x03 : false;
			
			boolean leftIgnore = false;
			boolean rightIgnore = false;
			byte retval = -1;
			if (this.isProperLoad()) {
				if (mePrevGap <= 0 || mePrevGap > MAX_CYCLE_GAP_MS)
					leftIgnore = true;
				else if (mePrevShortGap) {//if curr is proper and prev is not full proper then prev would have been
					if (meFullOPOT == prevFullOPOT) {
						retval = EVENT_IGNORE_ONLY;
					}
					else if (prevFullOPOT) {
						retval = EVENT_IGNORE_ONLY;
					}
				}
				else if (meNextShortGap) {
					if (meFullOPOT == nextFullOPOT) {
					}
					else if (nextFullOPOT) {
						retval = EVENT_IGNORE_ONLY;
					}
				}
			}
			
			if (meNextGap <= 0 || meNextGap > MAX_CYCLE_GAP_MS)
				rightIgnore = true;
			if (retval == EVENT_NOT_CALC) {
				boolean meIsProper = this.isProperLoad();
				if (!meIsProper)
					retval = EVENT_IGNORE_ONLY;
				else
					retval = EVENT_OK;
			}
			if (leftIgnore)
				retval |= ExcLoadEvent.EVENT_LEFT_IGNORE_MASK;
			if (rightIgnore)
				retval |= ExcLoadEvent.EVENT_RIGHT_IGNORE_MASK;
			this.ignoreBecauseNeighbour = retval;
		}
		return ignoreBecauseNeighbour;
	}
	
	public void setIgnoreBecauseNeighbour(byte ignoreBecauseNeighbour) {
		this.ignoreBecauseNeighbour = ignoreBecauseNeighbour;
	}
		

}
