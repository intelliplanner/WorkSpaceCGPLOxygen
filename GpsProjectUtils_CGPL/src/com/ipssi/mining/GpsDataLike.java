package com.ipssi.mining;

import java.sql.Connection;
import java.util.ArrayList;

import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.geometry.Point;
import com.ipssi.processor.utils.GpsData;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

public class GpsDataLike implements Comparable {
	public static boolean doSmoothening = false;
	public static int refCenterLon = 0;
	public static int refCenterLat = 0;
	public static double lowDistThreshKM = 0.030;
	public static int lowDistThreshDeltaLon = Misc.getUndefInt();
	public static int lowDistThreshDeltaLat = Misc.getUndefInt();
	public static int lowSqr = Misc.getUndefInt();
	
	public static long nearnessInTimeMatchMilli = 300*60*1000;
	public static long maxGapMilli = 20*60*1000;
	
	public static double highDistThreshKM = 0.060;
	public static int highDistThreshDeltaLon = Misc.getUndefInt();
	public static int highDistThreshDeltaLat = Misc.getUndefInt();
	
	public static int doubleToIntMult = 100000;
	
	private long gpsRecordTime = Misc.getUndefInt();
	private long endGpsRecordTimeIncl = Misc.getUndefInt();
	private int lonInt = Misc.getUndefInt();
	private int latInt = Misc.getUndefInt();
	private int avgLonInt = Misc.getUndefInt();
	private int avgLatInt = Misc.getUndefInt();
	private int support = 0;
	private int avgSupport = 0;
	private ArrayList<Integer> movingOpStationId = null;
	public void addMovingStation(int stationId) {
		if (movingOpStationId == null)
			movingOpStationId = new ArrayList<Integer>();
		for (Integer op: movingOpStationId)
			if (op == stationId)
				return;
		movingOpStationId.add(stationId);
	}
	public static boolean helperIsWithinThresh(int deltaLon, int deltaLat, int deltaLonThresh, int deltaLatThresh) {
		if (Misc.isUndef(deltaLon) || Misc.isUndef(deltaLat))
			return false;
		if (deltaLon < 0)
			deltaLon = 0- deltaLon;
		if (deltaLat < 0)
			deltaLat = 0-deltaLat;
		return deltaLon <= deltaLonThresh && deltaLat <= deltaLatThresh;
	}
	
	public static boolean isWithinLowDistThresh(int deltaLonInt, int deltaLatInt) {
		return helperIsWithinThresh(deltaLonInt, deltaLatInt, GpsDataLike.lowDistThreshDeltaLon, GpsDataLike.lowDistThreshDeltaLat);
	}
	
	public static boolean isWithinHighDistThresh(int deltaLonInt, int deltaLatInt) {
		return helperIsWithinThresh(deltaLonInt, deltaLatInt, GpsDataLike.highDistThreshDeltaLon, GpsDataLike.highDistThreshDeltaLat);
	}
	

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(new java.util.Date(gpsRecordTime)).append(endGpsRecordTimeIncl == gpsRecordTime ? " Same end:":" End:"+(new java.util.Date(this.endGpsRecordTimeIncl)))
		.append(" ").append(lonInt).append(",").append(latInt).append(",").append(support).append(" Avg:")
		.append(avgLonInt).append(",").append(avgLatInt).append(",").append(avgSupport)
		;
		return sb.toString();
	}
	public static int cmpFuzzyDist(int lhsLonDelta, int lhsLatDelta, int rhsLonDelta, int rhsLatDelta) {
		//-1 if lhs < rhs, 0 if equal, 1 if otherwise
		int retval = -1;
		if (Misc.isUndef(rhsLonDelta) || Misc.isUndef(rhsLatDelta)) {
			retval = -1;
		}
		else if (Misc.isUndef(lhsLonDelta) || Misc.isUndef(lhsLatDelta)) {
			retval = 1;
		}
		else {
			int lhsSqr = lhsLonDelta*lhsLonDelta+lhsLatDelta*lhsLatDelta;
			int rhsSqr = rhsLonDelta*rhsLonDelta+rhsLatDelta+rhsLatDelta;
			retval = lhsSqr <= lowSqr && rhsSqr <= lowSqr ? 0 : lhsSqr - rhsSqr; 
		}
		return retval;
	}
	private boolean inAvgWindow(GpsDataLike rhsPt) {
		long gap = rhsPt.getGpsRecordTime() - this.endGpsRecordTimeIncl;
		boolean inDistThresh = GpsDataLike.isWithinHighDistThresh(this.getAvgLonInt()-rhsPt.getAvgLonInt(), this.getAvgLatInt()-rhsPt.getAvgLatInt());
		return gap < maxGapMilli && inDistThresh;
	}
	protected void helperAddNearPt(GpsDataLike newPt, Station station) {
		int suppPlus1 = support+1;
		double f2 = 1.0/suppPlus1;
		double f1 = support*f2;
		double lon = (double)lonInt*f1;
		double lat = (double)latInt * f1;
		lon += (double)newPt.lonInt*f2;
		lat += (double)newPt.latInt*f2;
		lonInt = (int)Math.round(lon);
		latInt = (int)Math.round(lat);
		support = suppPlus1;
		if (newPt.getGpsRecordTime() < gpsRecordTime)
			this.gpsRecordTime = newPt.getGpsRecordTime();
		else if (newPt.getGpsRecordTime() > this.endGpsRecordTimeIncl)
			this.endGpsRecordTimeIncl = newPt.getGpsRecordTime();
		this.avgSupport++;
		station.helperSetMinMaxOnChange(this);
	}
	
	protected static void helperDoSmoothingFrom(FastList<GpsDataLike>theList, int from, Station station) {
		//returns inclusive change
		if (!doSmoothening)
			return;
		for (int i=from,is=theList.size();i<is;i++) {
			GpsDataLike curr = theList.get(i);
			double currLon = curr.lonInt;
			double currLat = curr.latInt;
			int currCount = curr.support;
			for (int j=i-1;j>=0;j--) {
				GpsDataLike windowPt = theList.get(j);
				if (windowPt != null && windowPt.inAvgWindow(curr)) {
					double f1 = (double)currCount/(double)(currCount+windowPt.support);
					double f2 = (double)windowPt.support/(double)(currCount+windowPt.support);
					currLon = currLon*f1+windowPt.lonInt*f2;
					currLat = currLat*f1+windowPt.latInt*f2;
					currCount += windowPt.support;
				}
				else {
					break;
				}
			}
			int oldCurrAvgLat = curr.avgLatInt;
			int oldCurrAvgLon = curr.avgLonInt;
			curr.avgLonInt = (int) Math.round(currLon);
			curr.avgLatInt = (int) Math.round(currLat);
			station.helperSetMinMaxOnChange(curr);
			curr.avgSupport = currCount;
			if (oldCurrAvgLon == curr.avgLonInt && oldCurrAvgLat == curr.avgLatInt) {
				break;
			}
			
		}
	}
	public GpsDataLike() {		
	}
	public GpsDataLike(GpsDataLike other) {
		this.gpsRecordTime = other.gpsRecordTime;
		this.endGpsRecordTimeIncl = other.endGpsRecordTimeIncl;
		this.lonInt = other.lonInt;
		this.latInt = other.latInt;
		this.avgLatInt = other.avgLatInt;
		this.avgLonInt = other.avgLonInt;
		this.avgSupport = other.avgSupport;
		this.support = other.support;
	}
	public GpsDataLike(long ts) {
		this.gpsRecordTime = ts;
		this.endGpsRecordTimeIncl = this.gpsRecordTime;
	}
	public GpsDataLike(GpsData data) {
		this.gpsRecordTime = data.getGps_Record_Time();
		this.lonInt = (int)(data.getLongitude()*doubleToIntMult);
		this.latInt = (int)(data.getLatitude()*doubleToIntMult);
		this.avgLonInt = this.lonInt;
		this.avgLatInt = this.latInt;
		this.endGpsRecordTimeIncl = this.gpsRecordTime;
	}
	public GpsDataLike(long ts, double lon, double lat) {
		this.gpsRecordTime = ts;
		this.lonInt = (int)(lon*doubleToIntMult);
		this.latInt = (int)(lat*doubleToIntMult);
		this.avgLonInt = this.lonInt;
		this.avgLatInt = this.latInt;
		this.endGpsRecordTimeIncl = this.gpsRecordTime;
	}
	
	public int compareTo(Object o) {
		GpsDataLike rhs = (GpsDataLike) o;
		long cmp = gpsRecordTime - rhs.gpsRecordTime;
		return cmp < 0 ? -1 : cmp == 0 ? 0 : 1;
	}
	public long getGpsRecordTime() {
		return gpsRecordTime;
	}
	public void setGpsRecordTime(long gpsRecordTime) {
		this.gpsRecordTime = gpsRecordTime;
	}
	public int getSupport() {
		return support;
	}
	public void setSupport(int support) {
		this.support = support;
	}
	public int getAvgLonInt() {
		return avgLonInt;
	}
	public int getAvgLatInt() {
		return avgLatInt;
	}
	public long getEndGpsRecordTimeIncl() {
		return endGpsRecordTimeIncl;
	}
	public void setEndGpsRecordTimeIncl(long endGpsRecordTimeIncl) {
		this.endGpsRecordTimeIncl = endGpsRecordTimeIncl;
	}
	public int getAvgSupport() {
		return avgSupport;
	}
	public void setAvgSupport(int avgSupport) {
		this.avgSupport = avgSupport;
	}
	public ArrayList<Integer> getMovingOpStationId() {
		return movingOpStationId;
	}
	public void setMovingOpStationId(ArrayList<Integer> movingOpStationId) {
		this.movingOpStationId = movingOpStationId;
	}
}