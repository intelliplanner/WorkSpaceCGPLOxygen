package com.ipssi.cache;

import java.io.Serializable;
import java.sql.Connection;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.TrackMisc;
import com.ipssi.geometry.Point;
import com.ipssi.processor.utils.GpsData;

public class DirTimeInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	/*
	private long inSuperDir = 0;
	private long inHiDir = 0;
	private long inLoDir = 0;
	private long outLoDir = 0;
	private long outHiDir = 0;
	private long outSuperDir = 0;
	*/
	private GpsData inSuperDir = null;
	private GpsData inHiDir = null;
	private GpsData inLoDir = null;
	private GpsData outLoDir = null;
	private GpsData outHiDir = null;
	private GpsData outSuperDir = null;
	private byte needsCalculationMask = 0;
	private static final byte G_MASK_IN_LO = (byte) 0x1;
	private static final byte G_MASK_IN_HI = (byte) 0x2;
	private static final byte G_MASK_IN_SUPER = (byte) 0x4;
	private static final byte G_MASK_OUT_LO = (byte) 0x8;
	private static final byte G_MASK_OUT_HI = (byte) 0x10;
	private static final byte G_MASK_OUT_SUPER = (byte) 0x20;
	
	public boolean isInLoInvalid() {
		return (needsCalculationMask & G_MASK_IN_LO) != 0;
	}
	public boolean isInHiInvalid() {
		return  (needsCalculationMask & G_MASK_IN_HI) != 0;
	}
	public boolean isInSuperInvalid() {
		return  (needsCalculationMask & G_MASK_IN_SUPER) != 0;
	}
	public boolean isOutLoInvalid() {
		return  (needsCalculationMask & G_MASK_OUT_LO) != 0;
	}
	public boolean isOutHiInvalid() {
		return  (needsCalculationMask & G_MASK_OUT_HI) != 0;
	}
	public boolean isOutSuperInvalid() {
		return  (needsCalculationMask & G_MASK_OUT_SUPER) != 0;
	}
	public boolean isInvalidCalc() {
		return  needsCalculationMask != 0;
	}
	public void setInLoInvalid(boolean invalid) {
		if (invalid)
			needsCalculationMask |= G_MASK_IN_LO;
		else
			needsCalculationMask &= ~G_MASK_IN_LO;
	}
	public void setInHiInvalid(boolean invalid) {
		if (invalid)
			needsCalculationMask |= G_MASK_IN_HI;
		else
			needsCalculationMask &= ~G_MASK_IN_HI;
	}
	public void setInSuperInvalid(boolean invalid) {
		if (invalid)
			needsCalculationMask |= G_MASK_IN_SUPER;
		else
			needsCalculationMask &= ~G_MASK_IN_SUPER;
	}
	public void setOutLoInvalid(boolean invalid) {
		if (invalid)
			needsCalculationMask |= G_MASK_OUT_LO;
		else
			needsCalculationMask &= ~G_MASK_OUT_LO;
	}
	public void setOutHiInvalid(boolean invalid) {
		if (invalid)
			needsCalculationMask |= G_MASK_OUT_HI;
		else
			needsCalculationMask &= ~G_MASK_OUT_HI;
	}
	public void setOutSuperInvalid(boolean invalid) {
		if (invalid)
			needsCalculationMask |= G_MASK_OUT_SUPER;
		else
			needsCalculationMask &= ~G_MASK_OUT_SUPER;
	}
	/*
	public void invalidate(GpsData stopPtInList, GpsData newDataPointInList, NewVehicleData dataList, Connection conn,StopDirControl stopDirControl) {
		long stTS = stopPtInList.getGps_Record_Time();
		long ndpTS = newDataPointInList.getGps_Record_Time();
		Point loBox = null;
		Point hiBox = null;
		Point superBox = null;
		Point stopPt = null;
		
		if (ndpTS <= stTS) { //only inDir can be impacted
			//because pts are added removed from dataList, we need to check if the point that exists afer each dir pt matches the pt representing ndpTS
			if (inLoDir > 0) {
				GpsData ptAfter = dataList.get(conn, new GpsData(inLoDir),+1);
				if (ptAfter != null && ptAfter.getGps_Record_Time() == ndpTS) {
					if (stopPt == null)
						stopPt = stopPtInList.getPoint();
					if (loBox == null) {
						double dist = stopDirControl.getDirChangeDetectThreshKM(stopPtInList.getPoint(), null);
						loBox = TrackMisc.getBoxAroundRange(stopPt, dist);
					}
					if (!dataList.isGpsInBound(ptAfter, stopPtInList, loBox.getLatitude(), loBox.getLongitude())){
						inLoDir = ptAfter.getGps_Record_Time();
						this.setInLoInvalid(true);
					}
				}
			}
			if (inHiDir > 0) {
				GpsData ptAfter = dataList.get(conn, new GpsData(inHiDir),+1);
				if (ptAfter != null && ptAfter.getGps_Record_Time() == ndpTS) {
					if (stopPt == null)
						stopPt = stopPtInList.getPoint();
					if (hiBox == null) {
						double dist = stopDirControl.getDirChangeDetectThreshKMHi(stopPtInList.getPoint(), null);
						hiBox = TrackMisc.getBoxAroundRange(stopPt, dist);
					}
					if (!dataList.isGpsInBound(ptAfter, stopPtInList, hiBox.getLatitude(), hiBox.getLongitude())){
						inHiDir = ptAfter.getGps_Record_Time();
						this.setInHiInvalid(true);
					}
				}
			}
			if (inSuperDir > 0) {
				GpsData ptAfter = dataList.get(conn, new GpsData(inSuperDir),+1);
				if (ptAfter != null && ptAfter.getGps_Record_Time() == ndpTS) {
					if (stopPt == null)
						stopPt = stopPtInList.getPoint();
					if (superBox == null) {
						double dist = stopDirControl.getDirChangeDetectThreshKMSuper();
						superBox = TrackMisc.getBoxAroundRange(stopPt, dist);
					}
					if (!dataList.isGpsInBound(ptAfter, stopPtInList, superBox.getLatitude(), superBox.getLongitude())){
						inSuperDir = ptAfter.getGps_Record_Time();
						this.setInSuperInvalid(true);
					}
				}
			}
		}
		if (ndpTS >= stTS) { //only inDir can be impacted
			//because pts are added removed from dataList, we need to check if the point that exists afer each dir pt matches the pt representing ndpTS
			if (outLoDir > 0) {
				GpsData ptBefore = dataList.get(conn, new GpsData(outLoDir),-1);
				if (ptBefore != null && ptBefore.getGps_Record_Time() == ndpTS) {
					if (stopPt == null)
						stopPt = stopPtInList.getPoint();
					if (loBox == null) {
						double dist = stopDirControl.getDirChangeDetectThreshKM(stopPtInList.getPoint(), null);
						loBox = TrackMisc.getBoxAroundRange(stopPt, dist);
					}
					if (!dataList.isGpsInBound(ptBefore, stopPtInList, loBox.getLatitude(), loBox.getLongitude())){
						outLoDir = ptBefore.getGps_Record_Time();
						this.setOutLoInvalid(true);
					}
				}
			}
			if (outHiDir > 0) {
				GpsData ptBefore = dataList.get(conn, new GpsData(outHiDir),-1);
				if (ptBefore != null && ptBefore.getGps_Record_Time() == ndpTS) {
					if (stopPt == null)
						stopPt = stopPtInList.getPoint();
					if (hiBox == null) {
						double dist = stopDirControl.getDirChangeDetectThreshKMHi(stopPtInList.getPoint(), null);
						hiBox = TrackMisc.getBoxAroundRange(stopPt, dist);
					}
					if (!dataList.isGpsInBound(ptBefore, stopPtInList, hiBox.getLatitude(), hiBox.getLongitude())){
						outHiDir = ptBefore.getGps_Record_Time();
						this.setOutHiInvalid(true);
					}
				}
			}
			if (outSuperDir > 0) {
				GpsData ptBefore = dataList.get(conn, new GpsData(outSuperDir),-1);
				if (ptBefore != null && ptBefore.getGps_Record_Time() == ndpTS) {
					if (stopPt == null)
						stopPt = stopPtInList.getPoint();
					if (superBox == null) {
						double dist = stopDirControl.getDirChangeDetectThreshKMSuper();
						superBox = TrackMisc.getBoxAroundRange(stopPt, dist);
					}
					if (!dataList.isGpsInBound(ptBefore, stopPtInList, superBox.getLatitude(), superBox.getLongitude())){
						outSuperDir = ptBefore.getGps_Record_Time();
						this.setOutSuperInvalid(true);
					}
				}
			}
		}
	}
	public long getInSuperDirTime() {
		return inSuperDir;
	}
	public long getInHiDirTime() {
		return inHiDir;
	}
	public long getInLoDirTime() {
		return inLoDir;
	}
	public long getOutSuperDirTime() {
		return outSuperDir;
	}
	public long getOutHiDirTime() {
		return outHiDir;
	}
	public long getOutLoDirTime() {
		return outLoDir;
	}
	public GpsData getInSuperDir(Connection conn, NewVehicleData vdp) {
		return inSuperDir > 0 ? vdp.get(conn, new GpsData(inSuperDir)) : null;
	}
	public void setInSuperDir(GpsData inSuperDir) {
		this.inSuperDir = inSuperDir == null ? 0 : inSuperDir.getGps_Record_Time();
	}
	public GpsData getInHiDir(Connection conn, NewVehicleData vdp) {
		return inHiDir > 0 ? vdp.get(conn, new GpsData(inHiDir)) : null;
	}
	public void setInHiDir(GpsData inHiDir) {
		this.inHiDir = inHiDir == null ? 0 : inHiDir.getGps_Record_Time();
	}
	public GpsData getInLoDir(Connection conn, NewVehicleData vdp) {
		return inLoDir > 0 ? vdp.get(conn, new GpsData(inLoDir)) : null;
	}
	public void setInLoDir(GpsData inLoDir) {
		this.inLoDir = inLoDir == null ? 0 : inLoDir.getGps_Record_Time();
	}
	public GpsData getOutLoDir(Connection conn, NewVehicleData vdp) {
		return outLoDir > 0 ? vdp.get(conn, new GpsData(outLoDir)) : null;
	}
	public void setOutLoDir(GpsData outLoDir) {
		this.outLoDir = outLoDir == null ? 0 : outLoDir.getGps_Record_Time();
	}
	public GpsData getOutHiDir(Connection conn, NewVehicleData vdp) {
		return outHiDir > 0 ? vdp.get(conn, new GpsData(outHiDir)) : null;
	}
	public void setOutHiDir(GpsData outHiDir) {
		this.outHiDir = outHiDir == null ? 0 : outHiDir.getGps_Record_Time();
	}
	public GpsData getOutSuperDir(Connection conn, NewVehicleData vdp) {
		return outSuperDir > 0 ? vdp.get(conn, new GpsData(outSuperDir)) : null;
	}
	public void setOutSuperDir(GpsData outSuperDir) {
		this.outSuperDir = outSuperDir == null ? 0 : outSuperDir.getGps_Record_Time();
	}
	public DirTimeInfo() {
		
	}
	public DirTimeInfo(GpsData inSuperDir, GpsData inHiDir,
			GpsData inLoDir, GpsData outLoDir, GpsData outHiDir,
			GpsData outSuperDir) {
		super();
		this.inSuperDir = inSuperDir == null ? 0 : inSuperDir.getGps_Record_Time();
		this.inHiDir = inHiDir == null ? 0 : inHiDir.getGps_Record_Time();
		this.inLoDir = inLoDir == null ? 0 : inLoDir.getGps_Record_Time();
		this.outLoDir = outLoDir == null ? 0 : outLoDir.getGps_Record_Time();
		this.outHiDir = outHiDir == null ? 0 : outHiDir.getGps_Record_Time();
		this.outSuperDir = outSuperDir == null ? 0 : outSuperDir.getGps_Record_Time();
	}
	*/
	
	public void invalidate(GpsData stopPtInList, GpsData newDataPointInList, NewVehicleData dataList, Connection conn,StopDirControl stopDirControl) {
		long stTS = stopPtInList.getGps_Record_Time();
		long ndpTS = newDataPointInList.getGps_Record_Time();
		Point loBox = null;
		Point hiBox = null;
		Point superBox = null;
		Point stopPt = null;
		
		if (ndpTS <= stTS) { //only inDir can be impacted
			//because pts are added removed from dataList, we need to check if the point that exists afer each dir pt matches the pt representing ndpTS
			if (inLoDir != null) {
				GpsData ptAfter = dataList.get(conn, inLoDir,+1);
				if (ptAfter != null && ptAfter.getGps_Record_Time() == ndpTS) {
					if (stopPt == null)
						stopPt = stopPtInList.getPoint();
					if (loBox == null) {
						double dist = stopDirControl.getDirChangeDetectThreshKM(stopPtInList.getPoint(), null);
						loBox = TrackMisc.getBoxAroundRange(stopPt, dist);
					}
					if (!dataList.isGpsInBound(ptAfter, stopPtInList, loBox.getLatitude(), loBox.getLongitude())){
						inLoDir = ptAfter;
						this.setInLoInvalid(true);
					}
				}
			}
			if (inHiDir != null) {
				GpsData ptAfter = dataList.get(conn, inHiDir,+1);
				if (ptAfter != null && ptAfter.getGps_Record_Time() == ndpTS) {
					if (stopPt == null)
						stopPt = stopPtInList.getPoint();
					if (hiBox == null) {
						double dist = stopDirControl.getDirChangeDetectThreshKMHi(stopPtInList.getPoint(), null);
						hiBox = TrackMisc.getBoxAroundRange(stopPt, dist);
					}
					if (!dataList.isGpsInBound(ptAfter, stopPtInList, hiBox.getLatitude(), hiBox.getLongitude())){
						inHiDir = ptAfter;
						this.setInHiInvalid(true);
					}
				}
			}
			if (inSuperDir != null) {
				GpsData ptAfter = dataList.get(conn, inSuperDir,+1);
				if (ptAfter != null && ptAfter.getGps_Record_Time() == ndpTS) {
					if (stopPt == null)
						stopPt = stopPtInList.getPoint();
					if (superBox == null) {
						double dist = stopDirControl.getDirChangeDetectThreshKMSuper();
						superBox = TrackMisc.getBoxAroundRange(stopPt, dist);
					}
					if (!dataList.isGpsInBound(ptAfter, stopPtInList, superBox.getLatitude(), superBox.getLongitude())){
						inSuperDir = ptAfter;
						this.setInSuperInvalid(true);
					}
				}
			}
		}
		if (ndpTS >= stTS) { //only inDir can be impacted
			//because pts are added removed from dataList, we need to check if the point that exists afer each dir pt matches the pt representing ndpTS
			if (outLoDir != null) {
				GpsData ptBefore = dataList.get(conn, outLoDir,-1);
				if (ptBefore != null && ptBefore.getGps_Record_Time() == ndpTS) {
					if (stopPt == null)
						stopPt = stopPtInList.getPoint();
					if (loBox == null) {
						double dist = stopDirControl.getDirChangeDetectThreshKM(stopPtInList.getPoint(), null);
						loBox = TrackMisc.getBoxAroundRange(stopPt, dist);
					}
					if (!dataList.isGpsInBound(ptBefore, stopPtInList, loBox.getLatitude(), loBox.getLongitude())){
						outLoDir = ptBefore;
						this.setOutLoInvalid(true);
					}
				}
			}
			if (outHiDir != null) {
				GpsData ptBefore = dataList.get(conn, outHiDir,-1);
				if (ptBefore != null && ptBefore.getGps_Record_Time() == ndpTS) {
					if (stopPt == null)
						stopPt = stopPtInList.getPoint();
					if (hiBox == null) {
						double dist = stopDirControl.getDirChangeDetectThreshKMHi(stopPtInList.getPoint(), null);
						hiBox = TrackMisc.getBoxAroundRange(stopPt, dist);
					}
					if (!dataList.isGpsInBound(ptBefore, stopPtInList, hiBox.getLatitude(), hiBox.getLongitude())){
						outHiDir = ptBefore;
						this.setOutHiInvalid(true);
					}
				}
			}
			if (outSuperDir != null) {
				GpsData ptBefore = dataList.get(conn, outSuperDir,-1);
				if (ptBefore != null && ptBefore.getGps_Record_Time() == ndpTS) {
					if (stopPt == null)
						stopPt = stopPtInList.getPoint();
					if (superBox == null) {
						double dist = stopDirControl.getDirChangeDetectThreshKMSuper();
						superBox = TrackMisc.getBoxAroundRange(stopPt, dist);
					}
					if (!dataList.isGpsInBound(ptBefore, stopPtInList, superBox.getLatitude(), superBox.getLongitude())){
						outSuperDir = ptBefore;
						this.setOutSuperInvalid(true);
					}
				}
			}
		}
	}
	public long getInSuperDirTime() {
		return inSuperDir == null ? Misc.getUndefInt() : inSuperDir.getGps_Record_Time();
	}
	public long getInHiDirTime() {
		return inHiDir == null ? Misc.getUndefInt() : inHiDir.getGps_Record_Time();
	}
	public long getInLoDirTime() {
		return inLoDir == null ? Misc.getUndefInt() : inLoDir.getGps_Record_Time();
	}
	public long getOutSuperDirTime() {
		return outSuperDir == null ? Misc.getUndefInt() : outSuperDir.getGps_Record_Time();
	}
	public long getOutHiDirTime() {
		return outHiDir == null ? Misc.getUndefInt() : outHiDir.getGps_Record_Time();
	}
	public long getOutLoDirTime() {
		return outLoDir == null ? Misc.getUndefInt() : outLoDir.getGps_Record_Time();
	}
	public GpsData getInSuperDir(Connection conn, NewVehicleData vdp) {
		return inSuperDir;
	}
	public void setInSuperDir(GpsData inSuperDir) {
		this.inSuperDir = inSuperDir;
	}
	public GpsData getInHiDir(Connection conn, NewVehicleData vdp) {
		return inHiDir;
	}
	public void setInHiDir(GpsData inHiDir) {
		this.inHiDir = inHiDir;
	}
	public GpsData getInLoDir(Connection conn, NewVehicleData vdp) {
		return inLoDir;
	}
	public void setInLoDir(GpsData inLoDir) {
		this.inLoDir = inLoDir;
	}
	public GpsData getOutLoDir(Connection conn, NewVehicleData vdp) {
		return outLoDir;
	}
	public void setOutLoDir(GpsData outLoDir) {
		this.outLoDir = outLoDir;
	}
	public GpsData getOutHiDir(Connection conn, NewVehicleData vdp) {
		return outHiDir;
	}
	public void setOutHiDir(GpsData outHiDir) {
		this.outHiDir = outHiDir;
	}
	public GpsData getOutSuperDir(Connection conn, NewVehicleData vdp) {
		return outSuperDir;
	}
	public void setOutSuperDir(GpsData outSuperDir) {
		this.outSuperDir = outSuperDir;
	}
	public DirTimeInfo() {
		
	}
	public DirTimeInfo(GpsData inSuperDir, GpsData inHiDir,
			GpsData inLoDir, GpsData outLoDir, GpsData outHiDir,
			GpsData outSuperDir) {
		super();
		this.inSuperDir = inSuperDir;
		this.inHiDir = inHiDir;
		this.inLoDir = inLoDir;
		this.outLoDir = outLoDir;
		this.outHiDir = outHiDir;
		this.outSuperDir = outSuperDir;
	}
}
