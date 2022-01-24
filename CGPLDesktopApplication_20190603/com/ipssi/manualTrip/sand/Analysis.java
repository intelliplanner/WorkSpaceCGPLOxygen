package com.ipssi.manualTrip.sand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.Value;

public class Analysis {
	public static boolean g_doExcatDate = true;
	private static int minusWindowMin = -120;
	private static int plusWindowMin = +120;
	private static int plusWindowUinMin = +720;
	public static long baseProcessTime = 30*60*1000;//(45*60*1000);
	public static long lowBaseProcessTime = 18*60*1000;
	public static long maxIN_OUT_TIME = (long)(1.1*24*60*60*1000);
	public static long maxPROCESS_TIME = (long)(0.5*24*60*60*1000);
	public static long maxLeadTime = (long)(5*60*60*1000);
	private ArrayList<Requirements> dataList = null;
	private Map<String, Integer> opsNameToId = null;
	private Map<Integer, Integer> opsOfInterest = null;
	private Map<String, ReferenceData> allReferenceData = null;
	private boolean loadGpsData = false;
	private ArrayList<Pair<Integer, SimpleTripData>> markedForDelete = new ArrayList<Pair<Integer, SimpleTripData>>();
	private ArrayList<Triple<Pair<Integer,Requirements>, Integer, SimpleTripData>> masterBreakList = null;//1st.1st = vehicle, 1st.2nd = reqId, 2nd = appr, 3 tripData
	
	public void fullControllerPrepprocess(Connection conn, int portNodeId, String nameStartWith, int materialId, String excludeName) {
		dataList = Preprocess.readData(conn, portNodeId, materialId);
		Pair<Map<String, Integer>, Map<Integer, Integer>> pr = Preprocess.getOpsNameToId(conn, portNodeId, nameStartWith, materialId, excludeName); 
		opsNameToId = pr.first;
		opsOfInterest = pr.second;
		Preprocess.preprocess(conn, dataList, opsNameToId);
		for (Requirements req:dataList) {
			if (req.getFromId() > 0) {
				Integer iv = new Integer(req.getFromId());
				opsOfInterest.put(iv,iv);
			}
			if (req.getFromId2() > 0) {
				Integer iv = new Integer(req.getFromId2());
				opsOfInterest.put(iv,iv);
			}
			if (req.getToId() > 0) {
				Integer iv = new Integer(req.getToId());
				opsOfInterest.put(iv,iv);
			}
		}
		Requirements.sort(dataList);
	}
	
	public void fullControllerAnalysis(Connection conn, int portNodeId, String nameStartWith, boolean breakTrip, boolean doRef, int materialId, String excludeName, double leadFactor, boolean autoCreateMapped) {
		fullControllerPrepprocess(conn, portNodeId, nameStartWith, materialId, excludeName);
		loadGpsData = false;

		this.prepAndLoadReferenceData(conn, loadGpsData, doRef, leadFactor);
		if (breakTrip) {
			this.masterBreakList = new ArrayList<Triple<Pair<Integer,Requirements>, Integer, SimpleTripData>> ();
			//1st.1st = vehicle, 1st.2nd = reqId, 2nd = appr, 3 tripData
		}
		createTripStrategy(conn, breakTrip, leadFactor);
		if (autoCreateMapped) {
			this.createAndSaveTrip(conn, dataList, true);
		}
	}

	public void fullControllerCreate(Connection conn, int portNodeId, String nameStartWith, String xmlStr, boolean doRef, int materialId, String excludeName, double leadFactor) throws Exception {
		fullControllerPrepprocess(conn, portNodeId, nameStartWith, materialId, excludeName);
		loadGpsData = false;
		this.prepAndLoadReferenceData(conn, loadGpsData, doRef, leadFactor);
		Pair<ArrayList<Requirements>, ArrayList<MiscInner.Pair>> saveModTripList = getDataListFromXML(conn, portNodeId, xmlStr,leadFactor);
		this.createAndSaveTrip(conn, saveModTripList.first, false);
	}

	private void rememberForDelete(int vehicleId, SimpleTripData trip) {
		markedForDelete.add(new Pair<Integer, SimpleTripData>(vehicleId, trip));
	}
	private static class HelperStrategy implements Comparable {
		public long dt;
		public int dateStartIndex; //in todoList
		public int dateEndIndexExcl; //in todoList
		public int need = 0;
		public int avTrip = 0;
		public int preDatedTrip = 0;
		public int postDatedTrip = 0;
		public int tripListStartIndex = 0;
		public int tripListEndIndexIncl = 0;
		public int tripsBefDate = 0;
		public int tripsOnDate = 0;
		public int tripsAftDate = 0;
		public int tripOnDateStartIndex = -1;
		public int tripOnDateEndIndexIncl = -1;
		public int tripBefDateEndIndexIncl = -1;
		public int tripAftDateStartIndexIncl = -1;
		public HelperStrategy(long dt, int dateStartIndex, int dateEndIndexExcl, int need, int avTrip, int preDatedTrip, int postDatedTrip, int tripListStartIndex, int tripListEndIndexIncl, int tripsBefDate, int tripsOnDate, int tripsAftDate, int tripOnDateStartIndex, int tripOnDateEndIndexIncl, int tripBefDateEndIndexIncl, int tripAftDateStartIndexIncl) {
			this.dt = dt;
			this.dateStartIndex = dateStartIndex;
			this.dateEndIndexExcl = dateEndIndexExcl;
			this.need = need;
			this.avTrip = avTrip;
			this.preDatedTrip = preDatedTrip;
			this.postDatedTrip = postDatedTrip;
			this.tripListStartIndex = tripListStartIndex;
			this.tripListEndIndexIncl = tripListEndIndexIncl;
			this.tripsBefDate = tripsBefDate;
			this.tripsOnDate = tripsOnDate;
			this.tripsAftDate = tripsAftDate;
			this.tripOnDateStartIndex = tripOnDateStartIndex;
			this.tripOnDateEndIndexIncl = tripOnDateEndIndexIncl;
			this.tripBefDateEndIndexIncl = tripBefDateEndIndexIncl;
			this.tripAftDateStartIndexIncl = tripAftDateStartIndexIncl;
		}
		@Override
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			HelperStrategy rhs = (HelperStrategy) o;
			if (true)
				return this.dt < rhs.dt ? -1 : this.dt == rhs.dt ? 0 : 1; 
			int lhsShort = this.tripsOnDate-this.need;
			int rhsShort = rhs.tripsOnDate-rhs.need;
			if (lhsShort == rhsShort) {
				lhsShort+= this.tripsBefDate;
				rhsShort += rhs.tripsBefDate;
			}
			if (lhsShort == rhsShort) {
				lhsShort+= this.tripsAftDate;
				rhsShort += rhs.tripsAftDate;
			}
			return lhsShort == rhsShort ? this.dt < rhs.dt ? -1 : this.dt == rhs.dt ? 0 : 1 : lhsShort-rhsShort; 
		}
	}
	
	private static void helperAddLHSBound(ArrayList<SimpleTripData> retval, SimpleTripData lhsBound) {
		if (retval.size() > 0 && lhsBound.getEarliest() >= retval.get(0).getEarliest())
			return;
		if (retval.size() == 0)
			retval.add(lhsBound);
		else
			retval.add(0, lhsBound);
	}
	private static void helperAddRHSBound(ArrayList<SimpleTripData> retval, SimpleTripData rhsBound) {
		if (retval.size() > 0 && rhsBound.getEarliest() <= retval.get(retval.size()-1).getEarliest())
			return;
		retval.add(rhsBound);
	}
	
	private static ArrayList<SimpleTripData> helperGetTripsForReporting(FastList<SimpleTripData> tripData, int stIndex, int enIndexIncl) {
		ArrayList<SimpleTripData> retval = new ArrayList<SimpleTripData>();
		if (stIndex >= 0 && enIndexIncl >= 0) {
			for (int i=stIndex;i<=enIndexIncl;i++) {
				SimpleTripData tr = tripData.get(i);
				if (tr == null)
					break;
				retval.add(tr);
			}
		}
		return retval;
	}
	
	private void breakTrip(Connection conn, FastList<SimpleTripData> tripData, ArrayList<Pair<Integer, SimpleTripData>> modTripDataList, int vehicleId) {
		for (int i=0,is=tripData.size();i<tripData.size();i++) {
			SimpleTripData data = tripData.get(i);
			if (data == null)
				continue;
			data.setAssignedToReq(Misc.getUndefInt());
			if (data.canBreak(opsOfInterest)) {
				SimpleTripData recTrip = new SimpleTripData(data.getTripId(), data.getLop(), data.getLgin(), data.getLgout(), data.getUop(), data.getUgin(), data.getUgout(), data.getMaterialId(), Misc.getUndefInt());
				modTripDataList.add(new Pair<Integer, SimpleTripData>(vehicleId, recTrip));
				long lt = data.getLgout() > 0 && data.getLgin() > 0 ? data.getLgout() - data.getLgin() : Misc.getUndefInt();
				long lu = data.getUgin() > 0 && data.getLgout() > 0 ? data.getUgin() - data.getLgout() : Misc.getUndefInt();
				long ut = data.getUgout() > 0 && data.getUgin() > 0 ? data.getUgout() - data.getUgin() : Misc.getUndefInt();
				int maxAt = 0;//0 => lt, 1 => lu, 2 => ut
				if (lu > lt) {
					if (ut > lu)
						maxAt = 2;
					else
						maxAt = 1;
				}
				else {
					if (lt > ut)
						maxAt = 0;
					else
						maxAt = 2;
				}
				boolean toDel = false;
				if (maxAt == 1) {//break ..
					//int tripId, int lop, long lgin, long lgout, int uop,long ugin, long ugout
					SimpleTripData newTrip = new SimpleTripData(data.getTripId(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), data.getUop(), data.getUgin(), data.getUgout(), data.getMaterialId(), Misc.getUndefInt());
					data.setUop(Misc.getUndefInt());
					data.setUgin(Misc.getUndefInt());
					data.setUgout(Misc.getUndefInt());
					tripData.add(newTrip);
					data.setModAction(SimpleTripData.G_BREAK_TRIP);
					recTrip.setModAction(SimpleTripData.G_BREAK_TRIP);
				}
				else if (maxAt == 2) {//get rid of U
					if (data.getLgin() <= 0)
						toDel = true;
					if (!toDel) {
						data.setUop(Misc.getUndefInt());
						data.setUgin(Misc.getUndefInt());
						data.setUgout(Misc.getUndefInt());
						data.setModAction(SimpleTripData.G_DROP_UNLOAD);
						recTrip.setModAction(SimpleTripData.G_DROP_UNLOAD);
					}
					else {
						tripData.remove(i);
						i--;
						recTrip.setModAction(SimpleTripData.G_DELETE_TRIP);
					}
				}
				else {//get rid of L
					if (data.getUgin() <= 0)
						toDel = true;
					if (!toDel) {

						data.setLop(Misc.getUndefInt());
						data.setLgin(Misc.getUndefInt());
						data.setLgout(Misc.getUndefInt());
						data.setModAction(SimpleTripData.G_DROP_LOAD);
						recTrip.setModAction(SimpleTripData.G_DROP_LOAD);
					}
					else {
						tripData.remove(i);
						i--;
						recTrip.setModAction(SimpleTripData.G_DELETE_TRIP);
					}
				}
				
			}
		}
	}
	
	private void createTripStrategy(Connection conn, boolean breakTrip, double leadFactor) {
		if (g_doExcatDate) {
			createTripStrategyDVCFullUIn(conn, breakTrip, leadFactor);
			return;
		}
		if (leadFactor <= 0.001)
			leadFactor = 1;
		int endExcl = 0;
		boolean exactDateInReq = false;
		long leftMarginMS = (long)(1*3600*1000*(leadFactor < 1.2 ? leadFactor : 2.5));
		long rightMarginMS = (long)(1*3600*1000*(leadFactor < 1.2 ? leadFactor : 2.5));
		for (int is = dataList.size(); endExcl < is; ) {
			Triple<Integer, Integer, Integer> bounds = getBounds(endExcl);
			int vehicleId = bounds.first;
			int start = bounds.second;
			endExcl = bounds.third;
			if (start == endExcl)
				break;
			Requirements stReq = dataList.get(start);
			Requirements enReq = dataList.get(endExcl-1);
			long tsstart = stReq.getDate();//+this.minusWindowMin;
			long tsen = enReq.getDate();
			tsen += 24*3600*1000;//+this.plusWindowMin;
			
			FastList<SimpleTripData> tripData = SimpleTripData.getTrips(conn, vehicleId, tsstart, tsen, opsOfInterest);
			ArrayList<HelperStrategy> stratHelper = new ArrayList<HelperStrategy>();
			for (int j=start;j<endExcl;) {//j set to nextJ inside loop the index of the next Date
				Requirements req = dataList.get(j);
				
				if (req.hasFullDate()) {
					j++;
					continue;
				}
				int nextJ = j+1;
				//first get all entries corresponding to same date ..
				long dtTs = req.getDate();//-(long)(24.5*60.0*60.0*1000.0);
				long dtTsEnd = dtTs+(long)(24*60.0*60.0*1000.0);
				int cnt = req.getCount();
				for (;nextJ<endExcl;nextJ++) {
					Requirements innerReq = dataList.get(nextJ);
					if (innerReq.hasFullDate())
						continue;
					if (innerReq.isValid()) {
						if (innerReq.getDate() > dtTs)
							break;
						cnt += innerReq.getCount();
					}
				}
				//get available Trips ... we will get trips that have some overlap
				int tripsOnDate = 0;
				int tripsAftDate = 0;
				int tripsBefDate =0;
				int refIndex = tripData.indexOf(new SimpleTripData(dtTs)).first;
				int stIndex = -1;
				int enIndex = -1;
				int tripOnDateStartIndex = -1;
				int tripOnDateEndIndexIncl = -1;
				int tripBefDateEndIndexIncl = -1;
				int tripAftDateStartIndexIncl = -1;
				for (int t1=refIndex;;t1--) {
					SimpleTripData temp = tripData.get(t1);
					if (temp == null)
						break;
	//				long lin = temp.getLgin() > 0 ? temp.getLgin() : temp.getUgin() - (long)(leadFactor*Analysis.maxLeadTime)-Analysis.baseProcessTime;
					long uout = temp.getUgout() > 0 ? temp.getUgout() : temp.getLgout() > 0 ?
							 temp.getLgout() + (long)(leadFactor*Analysis.maxLeadTime)+(long)(leadFactor*Analysis.baseProcessTime)
							 : temp.getLgin() + (long)(leadFactor*Analysis.maxLeadTime) + (long)(leadFactor*Analysis.baseProcessTime)+ (long)(leadFactor*Analysis.baseProcessTime);
								
					//long uin = temp.getUgin() > 0 ? temp.getUgin() : temp.getLgout() > 0 ? temp.getLgout()+(long)(leadFactor*Analysis.maxLeadTime) : temp.getLgin() + (long)(leadFactor*Analysis.maxLeadTime)-Analysis.baseProcessTime;
					long gap =uout-dtTs;
					if (gap < -1*leftMarginMS && temp.getLatest() < dtTs)
						break;
					if (gap < 0*3600*1000) {
						if (tripBefDateEndIndexIncl < 0 || tripBefDateEndIndexIncl < t1)
							tripBefDateEndIndexIncl = t1;
						tripsBefDate++;
					}
					else if (gap > 24*3600*1000){
						tripsAftDate++;
						if (tripAftDateStartIndexIncl < 0 || tripAftDateStartIndexIncl > t1)
							tripAftDateStartIndexIncl = t1;
					}
					else {
						tripsOnDate++;
						if (tripOnDateStartIndex < 0 || tripOnDateStartIndex > t1)
							tripOnDateStartIndex = t1;
						if (tripOnDateEndIndexIncl < 0 || tripOnDateEndIndexIncl < t1)
							tripOnDateEndIndexIncl = t1;
					}
					
					if (stIndex < 0 || stIndex > t1)
						stIndex = t1;
					if (enIndex < 0 || enIndex < t1)
						enIndex = t1;
				}
				for (int t1=refIndex+1;;t1++) {
					SimpleTripData temp = tripData.get(t1);
					if (temp == null)
						break;
					long uout = temp.getUgout() > 0 ? temp.getUgout() : temp.getLgout() > 0 ?
							 temp.getLgout() + (long)(leadFactor*Analysis.maxLeadTime)+(long)(leadFactor*Analysis.baseProcessTime)
							 : temp.getLgin() + (long)(leadFactor*Analysis.maxLeadTime) + (long)(leadFactor*Analysis.baseProcessTime) +(long)(leadFactor*Analysis.baseProcessTime);
								
					long gap =uout-dtTs;
//					long uin = temp.getUgin() > 0 ? temp.getUgin() : temp.getLgout() > 0 ? temp.getLgout()+(long)(leadFactor*Analysis.maxLeadTime) : temp.getLgin() + (long)(leadFactor*Analysis.maxLeadTime)-Analysis.baseProcessTime;
//					long gap =uin-dtTs;

					if (gap > rightMarginMS+24*3600*1000)
						break;
					if (gap < 0*3600*1000) {
						if (tripBefDateEndIndexIncl < 0 || tripBefDateEndIndexIncl < t1)
							tripBefDateEndIndexIncl = t1;
						tripsBefDate++;
					}
					else if (gap > (24+0)*3600*1000) {
						if (tripAftDateStartIndexIncl < 0 || tripAftDateStartIndexIncl > t1)
							tripAftDateStartIndexIncl = t1;
						tripsAftDate++;
					}
					else {
						tripsOnDate++;
						if (tripOnDateStartIndex < 0 || tripOnDateStartIndex > t1)
							tripOnDateStartIndex = t1;
						if (tripOnDateEndIndexIncl < 0 || tripOnDateEndIndexIncl < t1)
							tripOnDateEndIndexIncl = t1;
					}
					if (stIndex < 0 || stIndex > t1)
						stIndex = t1;
					if (enIndex < 0 || enIndex < t1)
						enIndex = t1;
				}
				int avTrip = enIndex-stIndex+1;
				if (stIndex == -1)
					avTrip = 0;
				int preDated = 0;
				int postDated = 0;
				stratHelper.add(new HelperStrategy(dtTs, j, nextJ, cnt, avTrip, preDated, postDated, stIndex, enIndex, tripsBefDate, tripsOnDate, tripsAftDate, tripOnDateStartIndex, tripOnDateEndIndexIncl, tripBefDateEndIndexIncl, tripAftDateStartIndexIncl));
//				stratHelper.add(new HelperStrategy(dtTs, j, nextJ, cnt, avTrip, preDated, postDated, tripOnDateStartIndex, tripOnDateEndIndexIncl, 0, tripsOnDate, 0, tripOnDateStartIndex, tripOnDateEndIndexIncl, -1, -));
				j = nextJ;
				
			}//for each vehicle
			coreCreateStrategy(conn, tripData, start, endExcl, stratHelper, vehicleId, leadFactor, masterBreakList);
		}//for all items in dataList
	}
	private void createTripStrategyBefUCC(Connection conn, boolean breakTrip, double leadFactor) {
		if (false) {
			createTripStrategyDVCFullUIn(conn, breakTrip, leadFactor);
			return;
		}
		if (leadFactor <= 0.001)
			leadFactor = 1;
		int endExcl = 0;
		boolean exactDateInReq = false;
		long leftMarginMS = (long)(8*3600*1000*(leadFactor < 1.2 ? leadFactor : 2.5));
		long rightMarginMS = (long)(8*3600*1000*(leadFactor < 1.2 ? leadFactor : 2.5));
		for (int is = dataList.size(); endExcl < is; ) {
			Triple<Integer, Integer, Integer> bounds = getBounds(endExcl);
			int vehicleId = bounds.first;
			int start = bounds.second;
			endExcl = bounds.third;
			if (start == endExcl)
				break;
			Requirements stReq = dataList.get(start);
			Requirements enReq = dataList.get(endExcl-1);
			long tsstart = stReq.getDate();//+this.minusWindowMin;
			long tsen = enReq.getDate();
			tsen += 24*3600*1000;//+this.plusWindowMin;
			
			FastList<SimpleTripData> tripData = SimpleTripData.getTrips(conn, vehicleId, tsstart, tsen, opsOfInterest);
			ArrayList<HelperStrategy> stratHelper = new ArrayList<HelperStrategy>();
			for (int j=start;j<endExcl;) {//j set to nextJ inside loop the index of the next Date
				Requirements req = dataList.get(j);
				int nextJ = j+1;
				//first get all entries corresponding to same date ..
				long dtTs = req.getDate();
				long dtTsEnd = dtTs+24*60*60*1000;
				int cnt = req.getCount();
				for (;nextJ<endExcl;nextJ++) {
					Requirements innerReq = dataList.get(nextJ);
					if (innerReq.isValid()) {
						if (innerReq.getDate() > dtTs)
							break;
						cnt += innerReq.getCount();
					}
				}
				//get available Trips ... we will get trips that have some overlap
				int tripsOnDate = 0;
				int tripsAftDate = 0;
				int tripsBefDate =0;
				int refIndex = tripData.indexOf(new SimpleTripData(dtTs)).first;
				int stIndex = -1;
				int enIndex = -1;
				int tripOnDateStartIndex = -1;
				int tripOnDateEndIndexIncl = -1;
				int tripBefDateEndIndexIncl = -1;
				int tripAftDateStartIndexIncl = -1;
				for (int t1=refIndex;;t1--) {
					SimpleTripData temp = tripData.get(t1);
					if (temp == null)
						break;
					long lin = temp.getLgin() > 0 ? temp.getLgin() : temp.getUgin() - (long)(leadFactor*Analysis.maxLeadTime)-Analysis.baseProcessTime;
					
					//long uin = temp.getUgin() > 0 ? temp.getUgin() : temp.getLgout() > 0 ? temp.getLgout()+(long)(leadFactor*Analysis.maxLeadTime) : temp.getLgin() + (long)(leadFactor*Analysis.maxLeadTime)-Analysis.baseProcessTime;
					long gap =lin-dtTs;
					if (gap < -1*leftMarginMS && temp.getLatest() < dtTs)
						break;
					if (gap < 0*3600*1000) {
						if (tripBefDateEndIndexIncl < 0 || tripBefDateEndIndexIncl < t1)
							tripBefDateEndIndexIncl = t1;
						tripsBefDate++;
					}
					else if (gap > 24*3600*1000){
						tripsAftDate++;
						if (tripAftDateStartIndexIncl < 0 || tripAftDateStartIndexIncl > t1)
							tripAftDateStartIndexIncl = t1;
					}
					else {
						tripsOnDate++;
						if (tripOnDateStartIndex < 0 || tripOnDateStartIndex > t1)
							tripOnDateStartIndex = t1;
						if (tripOnDateEndIndexIncl < 0 || tripOnDateEndIndexIncl < t1)
							tripOnDateEndIndexIncl = t1;
					}
					
					if (stIndex < 0 || stIndex > t1)
						stIndex = t1;
					if (enIndex < 0 || enIndex < t1)
						enIndex = t1;
				}
				for (int t1=refIndex+1;;t1++) {
					SimpleTripData temp = tripData.get(t1);
					if (temp == null)
						break;
					long lin = temp.getLgin() > 0 ? temp.getLgin() : temp.getUgin() - (long)(leadFactor*Analysis.maxLeadTime)-Analysis.baseProcessTime;
					long gap =lin-dtTs;
//					long uin = temp.getUgin() > 0 ? temp.getUgin() : temp.getLgout() > 0 ? temp.getLgout()+(long)(leadFactor*Analysis.maxLeadTime) : temp.getLgin() + (long)(leadFactor*Analysis.maxLeadTime)-Analysis.baseProcessTime;
//					long gap =uin-dtTs;

					if (gap > rightMarginMS+24*3600*1000)
						break;
					if (gap < 0*3600*1000) {
						if (tripBefDateEndIndexIncl < 0 || tripBefDateEndIndexIncl < t1)
							tripBefDateEndIndexIncl = t1;
						tripsBefDate++;
					}
					else if (gap > (24+0)*3600*1000) {
						if (tripAftDateStartIndexIncl < 0 || tripAftDateStartIndexIncl > t1)
							tripAftDateStartIndexIncl = t1;
						tripsAftDate++;
					}
					else {
						tripsOnDate++;
						if (tripOnDateStartIndex < 0 || tripOnDateStartIndex > t1)
							tripOnDateStartIndex = t1;
						if (tripOnDateEndIndexIncl < 0 || tripOnDateEndIndexIncl < t1)
							tripOnDateEndIndexIncl = t1;
					}
					if (stIndex < 0 || stIndex > t1)
						stIndex = t1;
					if (enIndex < 0 || enIndex < t1)
						enIndex = t1;
				}
				int avTrip = enIndex-stIndex+1;
				if (stIndex == -1)
					avTrip = 0;
				int preDated = 0;
				int postDated = 0;
				stratHelper.add(new HelperStrategy(dtTs, j, nextJ, cnt, avTrip, preDated, postDated, stIndex, enIndex, tripsBefDate, tripsOnDate, tripsAftDate, tripOnDateStartIndex, tripOnDateEndIndexIncl, tripBefDateEndIndexIncl, tripAftDateStartIndexIncl));
//				stratHelper.add(new HelperStrategy(dtTs, j, nextJ, cnt, avTrip, preDated, postDated, tripOnDateStartIndex, tripOnDateEndIndexIncl, 0, tripsOnDate, 0, tripOnDateStartIndex, tripOnDateEndIndexIncl, -1, -));
				j = nextJ;
				
			}//for each vehicle
			coreCreateStrategy(conn, tripData, start, endExcl, stratHelper, vehicleId, leadFactor, masterBreakList);
		}//for all items in dataList
	}

private void createTripStrategyMulti(Connection conn, boolean breakTrip, double leadFactor) {
		
		if (leadFactor <= 0.001)
			leadFactor = 1;
		int endExcl = 0;
		for (int is = dataList.size(); endExcl < is; ) {
			Triple<Integer, Integer, Integer> bounds = getBounds(endExcl);
			int vehicleId = bounds.first;
			int start = bounds.second;
			endExcl = bounds.third;
			if (start == endExcl)
				break;
			Requirements stReq = dataList.get(start);
			Requirements enReq = dataList.get(endExcl-1);
			long tsstart = stReq.getDate();//+this.minusWindowMin;
			long tsen = enReq.getDate();
			
			long tsStForTripGet = tsstart - 2*24*3600*1000;
			long tsEnForTripGet = tsen+2*24*3600*1000;
			FastList<SimpleTripData> allTripsForVehicle = SimpleTripData.getTrips(conn, vehicleId, tsStForTripGet, tsEnForTripGet, null);
			int currTripIndex = 0;
			//first for all req get the options possible for exiting trips unloading at that point
			for (int j=start;j<endExcl;) {//j set to nextJ inside loop the index of the next Date
				Requirements req = dataList.get(j);
				long dtTs = req.getDate();
				long dtTsEnd = dtTs+24*60*60*1000;
				boolean seenAllTrip = false;
				int nextJ = j+1;
				
				//first get all entries corresponding to same date ..
				int cnt = req.getCount();
				for (;nextJ<endExcl;nextJ++) {
					Requirements innerReq = dataList.get(nextJ);
					if (innerReq.isValid()) {
						if (innerReq.getDate() > dtTs)
							break;
						cnt += innerReq.getCount();
					}
				}
				req.sameDateEndIndexExcl = nextJ;
				req.sameDateTotCount = cnt;
				for (;;currTripIndex++) {
					SimpleTripData dat = allTripsForVehicle.get(currTripIndex);
					if (dat == null) {
						seenAllTrip = true;
						break;
					}
					long ugin = dat.getUgin();
					if (ugin <= 0)
						continue;
					if (ugin > dtTsEnd) {
						break;
					}
					if (ugin < dtTs)
						continue;
					if (dat.getUop() != req.getToId())
						continue;
					if ((ugin >= dtTs && ugin < dtTsEnd) || (dat.getUgout() > 0 && dat.getUgout() >= dtTs && dat.getUgout() < dtTsEnd)) {
						req.options.add(dat);
					}
				}
				j = nextJ;
			}
			long leadTime =  (long)(leadFactor*Analysis.maxLeadTime);			
			//now get possible number of trips that can be created for a given day
			for (int j=start;j<endExcl;) {//j set to nextJ inside loop the index of the next Date
				Requirements req = dataList.get(j);
				long dtTs = req.getDate();
				long dtTsEnd = dtTs+24*60*60*1000;
				boolean seenAllTrip = false;
				int nextJ = req.sameDateEndIndexExcl;
				int refIndexLast = allTripsForVehicle.indexOf(new SimpleTripData(dtTsEnd-1)).first;
				SimpleTripData last = allTripsForVehicle.get(refIndexLast);
				long nextUInTS = last == null ? dtTs : last.getUgout() > 0 ? last.getUgout()+leadTime+baseProcessTime+leadTime : last.getLgout()+leadTime;
				SimpleTripData next = allTripsForVehicle.get(refIndexLast+1);
				long ofNextMaxLoadInTime = next == null ? Long.MAX_VALUE : next.getLgin() > 0 ? next.getLgin() : next.getUgin()-baseProcessTime-leadTime;
				while (nextUInTS < dtTsEnd) {
					long gap = ofNextMaxLoadInTime - nextUInTS;
					if (gap > (leadTime+baseProcessTime)) {//will have enough  time to goto L of actual next trip
						req.options.add(new SimpleTripData(nextUInTS));
						nextUInTS += 2*(leadTime+baseProcessTime);
					}
					else {
						break;
					}
				}
				//now go back and see if it is possible to add trips
				long prevUOutTS = last == null ? dtTs-1 : last.getLgin() > 0 ? last.getLgin()-leadTime 
						: last.getUgin()-leadTime-baseProcessTime-leadTime;
						;
				int idx = 1;
				while (prevUOutTS >= dtTs) {
					SimpleTripData prev = allTripsForVehicle.get(refIndexLast-idx);
					long outImpliedByPrev = prev != null ? prev.getUgout() > 0 ? prev.getUgout() : prev.getLgout()+leadTime+baseProcessTime
							: Long.MIN_VALUE;
					if (outImpliedByPrev < prevUOutTS) {
						req.options.add(new SimpleTripData(prevUOutTS-baseProcessTime));
					}
					else {
						prevUOutTS = prev == null ? prevUOutTS-2*(leadTime+baseProcessTime) : prev.getLgin() > 0 ? prev.getLgin()-leadTime 
								: prev.getUgin()-leadTime-baseProcessTime-leadTime;
								;
					}
					idx++;
				}
				j = nextJ;
			}//for each day
			
//			coreCreateStrategy(conn, tripData, start, endExcl, stratHelper, vehicleId, leadFactor, masterBreakList);
		}//for each vehicle
	}
/*
 for each day if options = cnt, remove
   N1 .. o1,o2,o3
   N2 .. o1,o4,o5
   
 */
	private static boolean isTripNotAllowed(Requirements req, SimpleTripData tripData) {
		return (
		(!req.isLoadChangeAllowed() && tripData.getLop() > 0 && tripData.getLop() != req.getFromId() && tripData.getLop() != req.getFromId2())
		||
		(!req.isUnloadChangeAllowed() && tripData.getUop() > 0 && tripData.getUop() != req.getToId())
		||
		(!req.isMaterialChangeAllowed() && tripData.getMaterialId() > 0 && req.getMaterialId() > 0 && tripData.getMaterialId() != req.getMaterialId())
		)
		;
		
	}
	private void coreCreateStrategy(Connection conn, FastList<SimpleTripData> tripData, int start, int endExcl, ArrayList<HelperStrategy> stratHelper, int vehicleId, double leadFactor, ArrayList<Triple<Pair<Integer, Requirements>, Integer, SimpleTripData>> masterBreakList) {
		//masterBreakList = first vehicleId, 2nd = approach (1=L del, 2 u del, 3 = true break)
		//if null then not to remember break
		//now we have needs/av but still not op to op level ..
		Collections.sort(stratHelper); //order by desc order of needs
		boolean doSeparateOnDateMapping = true;
		boolean doSimpleMapping = true;
		boolean reqDateIsExact = false;
		double origLeadFactor = leadFactor;
		for (int onDateArt=0, ods = doSeparateOnDateMapping ? 2 : 1;onDateArt<ods;onDateArt++) {//
			//if onDateArt == 0, do only mapping to current Date
			//onDateArt == 1, do only for pre/post and breaking/creation of trips
			
			for (int j=0,js = stratHelper.size();j<js;j++) {
				HelperStrategy curr = stratHelper.get(j);
				int totCnt = curr.need;
				int numDeleted = 0;
				//go through each entry in the day and map to existing trips based on reasonable matches
				//1.on exact match of lop/uop, 
				//2.then lop but non match uop 
				//3.and then non match lop but match uop
				//4.then for extending valid L but empty U to U (if enough time gap)
				//5.then for extending valid U but empty L to L (if enough time gap)
				//No8w if still remaining then create new trips
				//
				
				ArrayList<SimpleTripData> tripsForDayVehicle = helperGetTripsForReporting(tripData, curr.tripListStartIndex, curr.tripListEndIndexIncl);
				for (int k=curr.dateStartIndex; k<curr.dateEndIndexExcl;k++) {//go through each entry in the day and map to existing trips based on reasonable matches
					Requirements req = dataList.get(k);
					if (!req.isValid())
						continue;
					if (req.hasFullDate())
						continue;
					ReferenceData refData = this.getReferenceData(conn, req.getFromId(), req.getToId(), leadFactor);
					if (refData != null)
						leadFactor = refData.getBaseFactor();
					else
						leadFactor = origLeadFactor;
					double adjFactor = 0.6;
					long leadTime = (long)(leadFactor*(double)(refData == null ? Analysis.maxLeadTime : refData.getUgin()-refData.getLgout()));
					if (leadTime > Analysis.maxLeadTime)
						leadTime = Analysis.maxLeadTime;
					int cnt = req.getCount();
					if (cnt == 0)
						continue;
					if (cnt <= req.getTripId().size()) {
						
						SimpleTripData refTrip = null;
						for (int t1=0,t1s=tripsForDayVehicle.size();t1<t1s;t1++) {
							if (tripsForDayVehicle.get(t1).getTripId() == req.getTripId().get(0)) {
								refTrip = tripsForDayVehicle.get(t1);
								break;
							}
						}
						if (refTrip != null && refTrip.getLgin() > 0 && refTrip.getUgin() > 0) {
							cnt = 0;
							totCnt = 0;
							curr.need = totCnt;
							req.setCount(cnt);
							refTrip = SimpleTripData.getTripDataById(conn, req.getTripId().get(0));
							req.addStrategy(refTrip, Requirements.TripCreateStrategy.G_ALREADY_MAPPED, Misc.getUndefInt(), refData, tripsForDayVehicle, null);
							continue;
						}
						else {
							try {
								PreparedStatement ps = conn.prepareStatement("delete from manual_trip_assigned where manual_trip_desired_id = ?");
								ps.setInt(1, req.getId());
								ps.execute();
								ps = Misc.closePS(ps);
								req.getTripId().clear();
								if (refTrip != null) {
									refTrip.setAssignedToReq(Misc.getUndefInt());
								}
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					//1.on exact match of lop/uop,
					for (int art=doSeparateOnDateMapping ? (onDateArt == 0 ? 0 : 1) : 0, arts = doSeparateOnDateMapping ? (onDateArt == 0 ? 1 : 3) : 3;art<arts;art++) {
						if (cnt <= 0)
							break;
						int stindex = art == 0 ? curr.tripOnDateStartIndex : art == 1 ? curr.tripListStartIndex :  curr.tripAftDateStartIndexIncl;
						
						int enindex = art == 0 ? curr.tripOnDateEndIndexIncl : art == 1 ? curr.tripBefDateEndIndexIncl: curr.tripListEndIndexIncl;
						
						if (stindex < 0 || enindex < 0)
							continue;
						ArrayList<Integer> tripIndexOrder = new ArrayList<Integer>();
						if (art == 0) {//onDate
							for (int l=stindex; l<=enindex;l++) {
								tripIndexOrder.add(l);
							}
						}
						else if (art == 1) {//bef Date
							for (int l=enindex; l>=stindex;l--) {
								tripIndexOrder.add(l);
							}
						}
						else {//aft Date
							for (int l=stindex; l<=enindex;l++) {
								tripIndexOrder.add(l);
							}
						}
						for (int l1=0,l1s=tripIndexOrder.size(); l1<l1s;l1++) {
							int l = tripIndexOrder.get(l1);
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq())) 
								continue;
							if (doSimpleMapping) {
								if (!Misc.isUndef(trip.getLop()) && !Misc.isUndef(trip.getUop()) && 
										//(trip.getLop() == req.getFromId() || trip.getLop() == req.getFromId2()) && 
										trip.getUop() == req.getToId()) {
									cnt--;
									totCnt--;
									curr.need = totCnt;
									req.setCount(cnt);
									trip.setAssignedToReq(req.getId());
									req.addStrategy(trip, Requirements.TripCreateStrategy.G_TO_MAP, Misc.getUndefInt(), refData, tripsForDayVehicle, null);
									if (cnt == 0)
										break;
								}
							}
						}

						//3.and then non match lop but match uop .. change L
						if (false && cnt > 0) {
							for (int l=stindex,ls=enindex; l<=ls;l++) {
								SimpleTripData trip = tripData.get(l);
								if (trip == null)
									continue;
								if (isTripNotAllowed(req, trip)) {
									trip.setAssignedToReq(-3);
								}
								else if (trip.getAssignedToReq() == -3) {
									trip.setAssignedToReq(Misc.getUndefInt());
								}
								if (!Misc.isUndef(trip.getAssignedToReq())) 
									continue;
								if (doSimpleMapping) {
									if (trip.getUop() == req.getToId() && !Misc.isUndef(trip.getLop())) {
										cnt--;
										totCnt--;
										curr.need = totCnt;
										req.setCount(cnt);
										trip.setAssignedToReq(req.getId());
										req.addStrategy(trip, Requirements.TripCreateStrategy.G_CHANGE_LOAD, Misc.getUndefInt(), refData, tripsForDayVehicle, null);
										
									}
								}
								if (cnt <= 0)
									break;
							}	
						}
						
						//5.then for extending valid U but empty L to L (if enough time gap)
						if (cnt > 0) {//now go through matching U but no L ... see if extensible ... will be extensible if there is enough time gap between this and next assigned stuff
							//if not the trip itself will be marked for deletion ...
							long dtTs = req.getDate();
							long dtTsEnd = dtTs+24*3600*1000;
							for (int l=curr.tripListEndIndexIncl; l>=curr.tripListStartIndex;l--) {//l may be
								SimpleTripData trip = tripData.get(l);
								if (trip == null)
									continue;
								if (!Misc.isUndef(trip.getAssignedToReq()) || !Misc.isUndef(trip.getLop()))
									continue;
								
								if (isTripNotAllowed(req, trip)) {
									//trip.setAssignedToReq(-3);
									continue;
								}
								//else if (trip.getAssignedToReq() == -3) {
								//	trip.setAssignedToReq(Misc.getUndefInt());
								//}
								if (!Misc.isUndef(trip.getAssignedToReq()) || !Misc.isUndef(trip.getLop()))
									continue;
								
								if (!Misc.isUndef(trip.getUop()) && trip.getUop() == req.getToId() && trip.getUgin() > 0) {
									//mean we may want to extend to u ... check if there is enough gap between this and next
									if (trip.getUgout() < dtTs || trip.getUgin() > dtTsEnd)
										continue;
									for (int t1=l-1;;t1--) {
										SimpleTripData prev = tripData.get(t1);
										if (prev == null)
											break;									
										long gap = 0;
										long gapThresh = 0;
										if (prev.getUgout() > 0) {//from prev gone to some L and this unload
											gap = trip.getUgin() - prev.getUgout();
											gapThresh = (long)(adjFactor*leadTime+(prev.getMaterialId() != req.getMaterialId() ? 10*60*1000 : adjFactor*leadTime)+adjFactor*baseProcessTime);
										}
										else if (prev.getLgout() > 0) {//from prev must have gone to unload, then back to load and then back to this unload
											gap =  trip.getUgin() - prev.getLgout();
											gapThresh = (long)(2*adjFactor*(leadTime+baseProcessTime));//3*leadTime+3*baseProcessTime;
										}
										
										if (gap > gapThresh) {//viable
											req.addStrategy(trip, Requirements.TripCreateStrategy.G_EXTEND_TO_L, trip.getUgin(), refData, tripsForDayVehicle, Double.toString(adjFactor));
											trip.setLgout(trip.getUgin()-(long)(adjFactor*leadTime));
											trip.setLgout(trip.getLgout()-(long)(adjFactor*baseProcessTime));
											trip.setAssignedToReq(req.getId());
											cnt--;
											totCnt--;
											curr.need = totCnt;
											req.setCount(cnt);
											break;
										}
										else {
											if (true)
												break;
											if (prev.getAssignedToReq() >= -1) {//not viable .. mark this to be deleted
												//trip.setAssignedToReq(-2);
												break;
											}
											else {//mark next to be deleted
												//prev.setAssignedToReq(-2);
												break;
											}
										}
									}//checking if enough gap going forward
								}//if matching L found
								if (cnt == 0)
									break;
							}//for each trip
						}//if cnt > 0
						
						
						//2.then lop but non match uop 					
						if (false && cnt > 0) {
							for (int l=stindex,ls=enindex; l<=ls;l++) {
								SimpleTripData trip = tripData.get(l);
								if (trip == null)
									continue;
								if (isTripNotAllowed(req, trip)) {
									trip.setAssignedToReq(-3);
								}
								else if (trip.getAssignedToReq() == -3) {
									trip.setAssignedToReq(Misc.getUndefInt());
								}
								if (!Misc.isUndef(trip.getAssignedToReq())) 
									continue;
								if (doSimpleMapping) {
									if (!Misc.isUndef(trip.getLop()) && (trip.getLop() == req.getFromId()  || trip.getLop() == req.getFromId2()) && !Misc.isUndef(trip.getUop())) {
										cnt--;
										totCnt--;
										curr.need = totCnt;
										req.setCount(cnt);
										trip.setAssignedToReq(req.getId());
										req.addStrategy(trip, Requirements.TripCreateStrategy.G_CHANGE_UNLOAD, Misc.getUndefInt(), refData, tripsForDayVehicle, null);
										if (cnt == 0)
											break;
									}
								}
							}	
						}

						
					
						//4.then for extending valid L but empty U to U (if enough time gap)
						if (true && cnt > 0) {//... see if extensible ... will be extensible if there is enough time gap between this and next assigned stuff
							//if not the trip itself will be marked for deletion ...
							for (int l=curr.tripListStartIndex,ls=curr.tripListEndIndexIncl; l<=ls;l++) {//l may be
								SimpleTripData trip = tripData.get(l);
								if (trip == null)
									continue;
								if (isTripNotAllowed(req, trip)) {
									//trip.setAssignedToReq(-3);
									continue;
								}
								//else if (trip.getAssignedToReq() == -3) {
								//	trip.setAssignedToReq(Misc.getUndefInt());
								//}
								if (!Misc.isUndef(trip.getAssignedToReq()) || !Misc.isUndef(trip.getUop()))
									continue;
								
								if (!Misc.isUndef(trip.getLop()) 
										//&& (trip.getLop() == req.getFromId() || trip.getLop() == req.getFromId2()) 
										&& (trip.getMaterialId() == req.getMaterialId())
										&& trip.getLgout() > 0) {
									//mean we may want to extend to u ... check if there is enough gap between this and next
									SimpleTripData next = null;
									
									boolean usableNext = false;
									double targetFactor = 1;
									for (int t1=l+1;;t1++) {
										next = tripData.get(t1);
										if (next == null)
											break;
										long gap = 0;
										long gapThresh = 0;
										long fullGapThresh = 0;
										double innerFactor = 1;
										if (next.getLgin() > 0) {//must have gone to unload, then back to load
											gap = next.getLgin() - trip.getLgout();
											gapThresh = (long)(adjFactor*leadTime)+ (long)(next.getMaterialId() != req.getMaterialId() ? 10*60*1000 : adjFactor*leadTime)+(long)(adjFactor*baseProcessTime);
											fullGapThresh = (long)(1*leadTime)+ (long)(next.getMaterialId() != req.getMaterialId() ? 10*60*1000 : 1*leadTime)+(long)(1*baseProcessTime);
										}
										else if (next.getUgin() > 0) {//must have gone to unload, back to load and then back to this unload!!
											gap = next.getUgin() - trip.getLgout();
											gapThresh = (long)(adjFactor*leadTime) + (long)(2*30*60*1000+2*adjFactor*baseProcessTime);
											fullGapThresh = (long)(leadTime) + (long)(2*30*60*1000+2*baseProcessTime);
										}
										if (gap > gapThresh) {//viable
											usableNext = true;
											targetFactor = gap > fullGapThresh ? 1 : (double)gap/(double)fullGapThresh;
											break;
											
										}
										else {
											if (true)
												break;
											if (next.getAssignedToReq() >= -1) {//not viable .. mark this to be deleted
												trip.setAssignedToReq(-2);
												break;
											}
											else {//mark next to be deleted
												next.setAssignedToReq(-2);
												break;
											}
										}
									}//checking if enough gap going forward
									if (next == null) {
										long loadedUin = Math.max(trip.getLgout()+(long)(adjFactor*leadTime), req.getTs()-30*60*1000);
										targetFactor = (double)loadedUin/(double) leadTime;
									}
									
									if (usableNext) {
										
										req.addStrategy(trip, Requirements.TripCreateStrategy.G_EXTEND_TO_U, trip.getLgout(), refData, tripsForDayVehicle, Double.toString(targetFactor));
										trip.setAssignedToReq(req.getId());
										trip.setUgin(trip.getLgout()+(long)(targetFactor*leadTime));
										trip.setUgout(trip.getUgin()+(long)(targetFactor*baseProcessTime));
										cnt--;
										totCnt--;
										curr.need = totCnt;
										req.setCount(cnt);
									}
								}//if matching L found
								if (cnt == 0)
									break;
							}//for each trip
						}//if cnt > 0
						
						
						//4.extend L to U as well as change L
						if (false && cnt > 0) {//... see if extensible ... will be extensible if there is enough time gap between this and next assigned stuff
							//if not the trip itself will be marked for deletion ...
							for (int l=curr.tripListStartIndex,ls=curr.tripListEndIndexIncl; l<=ls;l++) {//l may be
								SimpleTripData trip = tripData.get(l);
								if (trip == null)
									continue;
								if (isTripNotAllowed(req, trip)) {
									trip.setAssignedToReq(-3);
								}
								else if (trip.getAssignedToReq() == -3) {
									trip.setAssignedToReq(Misc.getUndefInt());
								}
								if (!Misc.isUndef(trip.getAssignedToReq()) || !Misc.isUndef(trip.getUop()))
									continue;
								if (trip.getMaterialId() != req.getMaterialId())
									continue;
								long ugin = trip.getLgout()+2*leadTime;
								if (ugin < req.getTs()-30*60*1000 || ugin > req.getTs()+24*60*1000)
									continue;
								if (trip.getLgout() > 0) {
									//mean we may want to extend to u ... check if there is enough gap between this and next
									SimpleTripData next = null;
									
									boolean usableNext = false;
									double targetFactor = 1;
									for (int t1=l+1;;t1++) {
										next = tripData.get(t1);
										if (next == null)
											break;
										long gap = 0;
										long gapThresh = 0;
										long fullGapThresh = 0;
										double innerFactor = 1;
										if (next.getLgin() > 0) {//must have gone to unload, then back to load
											gap = next.getLgin() - trip.getLgout();
											gapThresh = (long)(adjFactor*leadTime)+ (long)(next.getMaterialId() != req.getMaterialId() ? 10*60*1000 : adjFactor*leadTime)+(long)(adjFactor*baseProcessTime);
											fullGapThresh = (long)(1*leadTime)+ (long)(next.getMaterialId() != req.getMaterialId() ? 10*60*1000 : 1*leadTime)+(long)(1*baseProcessTime);
										}
										else if (next.getUgin() > 0) {//must have gone to unload, back to load and then back to this unload!!
											gap = next.getUgin() - trip.getLgout();
											gapThresh = (long)(adjFactor*leadTime) + (long)(2*30*60*1000+2*adjFactor*baseProcessTime);
											fullGapThresh = (long)(leadTime) + (long)(2*30*60*1000+2*baseProcessTime);
										}
										if (gap > gapThresh) {//viable
											usableNext = true;
											targetFactor = gap > fullGapThresh ? 1 : (double)gap/(double)fullGapThresh;
											break;
											
										}
										else {
											if (next.getAssignedToReq() >= -1) {//not viable .. mark this to be deleted
												trip.setAssignedToReq(-2);
												break;
											}
											else {//mark next to be deleted
												next.setAssignedToReq(-2);
												break;
											}
										}
									}//checking if enough gap going forward
									if (next == null) {
										long loadedUin = Math.max(trip.getLgout()+(long)(adjFactor*leadTime), req.getTs()-30*60*1000);
										targetFactor = (double)loadedUin/(double) leadTime;
									}
									
									if (usableNext) {
										
										req.addStrategy(trip, Requirements.TripCreateStrategy.G_CHANGE_L_EXTEND, trip.getLgout(), refData, tripsForDayVehicle, Double.toString(targetFactor));
										trip.setAssignedToReq(req.getId());
										cnt--;
										totCnt--;
										curr.need = totCnt;
										req.setCount(cnt);
									}
								}//if matching L found
								if (cnt == 0)
									break;
							}//for each trip
						}//if cnt > 0
	
						//5.then for extending valid U but empty L to L (if enough time gap)
						if (false && cnt > 0) {//now go through matching U but no L ... see if extensible ... will be extensible if there is enough time gap between this and next assigned stuff
							//if not the trip itself will be marked for deletion ...
							for (int l=curr.tripListEndIndexIncl; l>=curr.tripListStartIndex;l--) {//l may be
								SimpleTripData trip = tripData.get(l);
								if (trip == null)
									continue;
								if (isTripNotAllowed(req, trip)) {
									trip.setAssignedToReq(-3);
								}
								else if (trip.getAssignedToReq() == -3) {
									trip.setAssignedToReq(Misc.getUndefInt());
								}
								if (!Misc.isUndef(trip.getAssignedToReq()) || !Misc.isUndef(trip.getLop()))
									continue;
								long ugout = trip.getUgout();
								long ugin = trip.getUgin();
								if (ugout < 0) {
									ugout = trip.getLgout()+leadTime+Analysis.baseProcessTime;
									ugin = ugout - Analysis.baseProcessTime;
								}
									
								if (ugout < req.getTs()-30*60*1000)
									continue;
								if (ugin > req.getToId()+24*60*1000)
									continue;
								if (trip.getUgin() > 0) {
									//mean we may want to extend to u ... check if there is enough gap between this and next
									long dtTs = req.getDate();
									long dtTsEnd = dtTs+24*3600*1000;
									if (trip.getUgout()<dtTs || trip.getUgin() > dtTsEnd)
										continue;
									for (int t1=l-1;;t1--) {
										SimpleTripData prev = tripData.get(t1);
										if (prev == null)
											break;									
										long gap = 0;
										long gapThresh = 0;
										if (prev.getUgout() > 0) {//from prev gone to some L and this unload
											gap = trip.getUgin() - prev.getUgout();
											gapThresh = 2*leadTime+1*baseProcessTime;
										}
										else if (prev.getLgout() > 0) {//from prev must have gone to unload, then back to load and then back to this unload
											gap =  trip.getUgin() - prev.getLgout();
											gapThresh = leadTime;//3*leadTime+3*baseProcessTime;
										}
										
										if (gap > gapThresh) {//viable
											req.addStrategy(trip, Requirements.TripCreateStrategy.G_CHANGE_U_EXTEND, trip.getUgin(), refData, tripsForDayVehicle, null);
											trip.setAssignedToReq(req.getId());
											cnt--;
											totCnt--;
											curr.need = totCnt;
											req.setCount(cnt);
											break;
										}
										else {
											if (prev.getAssignedToReq() >= -1) {//not viable .. mark this to be deleted
												trip.setAssignedToReq(-2);
												break;
											}
											else {//mark next to be deleted
												prev.setAssignedToReq(-2);
											}
										}
									}//checking if enough gap going forward
								}//if matching L found
								if (cnt == 0)
									break;
							}//for each trip
						}//if cnt > 0
						if (cnt <= 0)
							break;
					}
					
				}
				if (doSeparateOnDateMapping && onDateArt  == 0)
					continue;
				//Now to see if we need to create trip
				
				if (totCnt > 0) {//we will have to create brand new
					//since we are not creating trips immediately and inorder to make life simpler we will go through trip and 
					//identify markers against which U can be set without overlapping stuff
					//then we will go thru entries for the day and assign each successively from the back
					ArrayList<Triple<Long, Pair<Long, Double>, Pair<SimpleTripData, SimpleTripData>>> umarkersForCreate = new ArrayList<Triple<Long, Pair<Long, Double>, Pair<SimpleTripData, SimpleTripData>>>();
					//2nd.first = Ucreate, 2nd.second = multFactorForDur
					//1st = Lin shld be at least more than this 
					//3rd = bounding ltrip/rtrip for creation of trip
					double avgLeadTimeTemp = 0;
					int countForLeadTimeTemp = 0;
					
					for (int k=curr.dateStartIndex,ks=curr.dateEndIndexExcl; k<ks;k++) {
						Requirements req = dataList.get(k);
						if (!req.isValid())
							continue;
						if (req.getCount()<=0) {// == req.getTripPlan().size()) {
							continue; //already addressed
						}
						ReferenceData refData = this.getReferenceData(conn, req.getFromId(), req.getToId(), leadFactor);
						
						long temp= (long)(leadFactor  *  (refData == null ? Analysis.maxLeadTime : refData.getUgin()-refData.getLgout()));
						if (temp > Analysis.maxLeadTime)
							temp = Analysis.maxLeadTime;
						avgLeadTimeTemp += temp;
						countForLeadTimeTemp++;
					}
					double adjFactor = 0.6;
					
					long stdAvgLeadTime = (long)((double)avgLeadTimeTemp*adjFactor/(double)countForLeadTimeTemp);
					long adjbaseProcessTime = (long)(adjFactor*baseProcessTime);
					long dtTs = curr.dt;
					long dtTsEnd = dtTs+24*60*60*1000;
					//dtTs += Analysis.minusWindowMin*60*1000;
					//dtTsEnd += plusWindowMin*60*1000;
					SimpleTripData maxRHSBound = null;
					SimpleTripData minLHSBound = null;
					double currMultFactor = 1;//we will reduce to 0.9,0.8,0.7
					ArrayList<Pair<MiscInner.Pair, SimpleTripData>> breakAbleList = new ArrayList<Pair<MiscInner.Pair, SimpleTripData>>();
					for (int art =0;art<1;art++) {
						if (totCnt <= 0)
							break;
						currMultFactor = 1;//1-art*0.1;
						//note that there may be opportunity to create stuff after last trip (but before window) and before first trip
						//note also that we are NOT deleting things that are to be deleted or are unused (and hence presumed to be deleted because they are wrong)
						long avgLeadTime = (long)(currMultFactor*stdAvgLeadTime);
						for (int l=curr.tripListStartIndex-1;l <= curr.tripListEndIndexIncl;l++) {//not the minus 1 at start
							SimpleTripData lhsTrip = tripData.get(l);
												
							long stBase = dtTs-0*3600*1000;
							long st = stBase;
							MiscInner.Pair breakRes = null;//lhsTrip == null ? null : checkIfBreakAble(lhsTrip, stBase, stdAvgLeadTime, baseProcessTime);
							if (breakRes != null && breakRes.first > 0 && breakRes.second > 0) {
								breakAbleList.add(new Pair<MiscInner.Pair, SimpleTripData>(breakRes, lhsTrip));
							}
							if (lhsTrip != null) {
								if (lhsTrip.getUgout() > 0) {
									st = lhsTrip.getUgout()+stdAvgLeadTime;
								}
								else if (lhsTrip.getUgin() > 0) {
									st = lhsTrip.getUgin()+stdAvgLeadTime+adjbaseProcessTime;
								}
								else if (lhsTrip.getLgout() > 0) {
									st = lhsTrip.getLgout()+2*stdAvgLeadTime+adjbaseProcessTime;
								}
								else if (lhsTrip.getLgin() > 0) {
									st = lhsTrip.getLgin()+2*stdAvgLeadTime+2*adjbaseProcessTime;
								}
							}
							SimpleTripData rhsTrip = tripData.get(l+1);
							long enBase = dtTsEnd+2*stdAvgLeadTime+2*adjbaseProcessTime;
							long en = enBase;
							if (rhsTrip != null) {
								if (rhsTrip.getLgin() > 0) {
									en = rhsTrip.getLgin();
								}
								else if (rhsTrip.getUgin() > 0) {
									en = rhsTrip.getUgin()-stdAvgLeadTime-adjbaseProcessTime;
								}							
							}
							long adjSt = Math.max(stBase, st);
							long adjEn = Math.min(en, enBase);
							long gapAv = adjEn-adjSt;
							long gapDes = 2*stdAvgLeadTime+2*adjbaseProcessTime;
							if (gapAv < gapDes)
								continue;
							int maxAccom = (int)(gapAv/gapDes);
							long buffer = gapAv - maxAccom*gapDes;
							for (long prevSt = adjSt;gapAv > 0;) {
								long desiredLin = prevSt + (long)((double)buffer/(double)maxAccom*Math.random());
								long desiredUgin = desiredLin+adjbaseProcessTime+stdAvgLeadTime;
								umarkersForCreate.add(new Triple<Long, Pair<Long, Double>, Pair<SimpleTripData, SimpleTripData>>(desiredLin+adjbaseProcessTime,new Pair<Long, Double>(desiredUgin, (double) stdAvgLeadTime), new Pair<SimpleTripData, SimpleTripData>(lhsTrip, rhsTrip)));
								prevSt = desiredUgin+adjbaseProcessTime+stdAvgLeadTime;
								gapAv -= 2*adjbaseProcessTime+2*stdAvgLeadTime;
								totCnt--;
								curr.need = totCnt;
								SimpleTripData dummyTripDat = new SimpleTripData(Misc.getUndefInt(), Misc.getUndefInt(), desiredLin, desiredLin+adjbaseProcessTime, Misc.getUndefInt(), desiredUgin, desiredUgin+adjbaseProcessTime, Misc.getUndefInt(), Misc.getUndefInt());
								if (minLHSBound == null) {
									maxRHSBound = minLHSBound = dummyTripDat; 
								}
								maxRHSBound = dummyTripDat;
								if (totCnt <= 0)
									break;
							}
							if (totCnt <= 0)
								break;
						}//for all trips
					}
					
					int idx = 0;
					int unkCount = 0;
					if (minLHSBound != null)
						Analysis.helperAddLHSBound(tripsForDayVehicle, minLHSBound);
					if (maxRHSBound != null)
						Analysis.helperAddRHSBound(tripsForDayVehicle, maxRHSBound);
					
					for (int k=curr.dateStartIndex; k<curr.dateEndIndexExcl;k++) {//end might change
						Requirements req = dataList.get(k);
						if (!req.isValid())
							continue;
						if (req.getCount() <=0 ) {// == req.getTripPlan().size()) {
							continue; //already addressed
						}
						if (idx >= umarkersForCreate.size()) {
							req.addStrategy(null, Requirements.TripCreateStrategy.G_UNKNOWN, Misc.getUndefInt(), this.getReferenceData(conn, req.getFromId(), req.getToId(), leadFactor), tripsForDayVehicle, null);
							unkCount++;
						}
						else {
							long refTS = umarkersForCreate.get(idx).second.first;
							double fac = umarkersForCreate.get(idx).second.second;
							String addnlParam = Double.toString(fac);
							if (refTS >= req.getDate() && refTS < req.getDate()+24*3600*1000) {
								req.addStrategy(null, Requirements.TripCreateStrategy.G_CREATE_FULL, refTS, this.getReferenceData(conn, req.getFromId(), req.getToId(), leadFactor), tripsForDayVehicle, addnlParam);
								long ugin = refTS;
								long ugout = refTS+(long)(adjbaseProcessTime);
								long lgout = ugin - (long)fac;
								long lgin = lgout-(long)(adjbaseProcessTime);
								SimpleTripData dummyTrip = new SimpleTripData(1, req.getFromId(), lgin, lgout, req.getToId(),ugin, ugout, Misc.getUndefInt(), req.getId());
								tripData.add(dummyTrip);
								int indexAdded = tripData.indexOf(dummyTrip).first;
								for (int j1=0,j1s = stratHelper.size();j1<j1s;j1++) {
									HelperStrategy curr1 = stratHelper.get(j1);
									if (curr1.tripAftDateStartIndexIncl >= indexAdded)
										curr1.tripAftDateStartIndexIncl++;
									if (curr1.tripBefDateEndIndexIncl >= indexAdded)
										curr1.tripBefDateEndIndexIncl++;
									if (curr1.tripListEndIndexIncl >= indexAdded)
										curr1.tripListEndIndexIncl++;
									if (curr1.tripListStartIndex >= indexAdded)
										curr1.tripListStartIndex++;
									if (curr1.tripOnDateEndIndexIncl >= indexAdded)
										curr1.tripOnDateEndIndexIncl++;
									if (curr1.tripOnDateStartIndex >= indexAdded)
										curr1.tripOnDateStartIndex++;
									
								}
								dummyTrip.setAssignedToReq(req.getId()); 
							}
							
							
						}
						idx++;
					}//for each entry for day
					if (masterBreakList != null) {
						Requirements refReq = dataList.get(curr.dateStartIndex);
						while (breakAbleList.size() > 0 && unkCount > 0) {
							int cmax = -1;
							int t2 = -1;
							for (int t1=0,t1s=breakAbleList.size(); t1<t1s;t1++) {
								if (breakAbleList.get(t1).first.second > cmax) {
									cmax = breakAbleList.get(t1).first.second;
									t2 = t1;
								}
							}
							//check if already exists ... if so dont add
							boolean exists = false;
							for (int t3=0,t3s=masterBreakList.size(); t3<t3s;t3++) {
								if (masterBreakList.get(t3).third.getTripId() == breakAbleList.get(t2).second.getTripId()) {
									exists = true;
									break;
								}
							}
							if (!exists) {
								masterBreakList.add(new Triple<Pair<Integer,Requirements>, Integer, SimpleTripData>(new Pair<Integer, Requirements>(vehicleId, refReq), breakAbleList.get(t2).first.first, breakAbleList.get(t2).second));
							}
							unkCount -= breakAbleList.get(t2).first.second;
							breakAbleList.remove(t2);
						}
					}
				}//if full created needed
			}//for all days;
		}
	}
	
	private MiscInner.Pair checkIfBreakAble(SimpleTripData trip, long dtTsAdj, long leadMilli, long processMilli) {
		//1st = -1 if nothing delete L, 2nd delete U, 3rd break.
		//2nd = no of trips that can be obtaing
		long dtTsEndAdj = dtTsAdj + 24*3600*1000;
		int ifLDelPossibleCnt = 0;
	    int ifUDelPossibleCnt = 0;
	    int ifLUBreakPossibleCnt = 0;
		long lgap = trip.getLgin() > 0 && trip.getLgout() > 0 
		&& ((trip.getLgin() >= dtTsAdj && trip.getLgin() < dtTsEndAdj) ||
				(trip.getLgout() >= dtTsAdj && trip.getLgout() < dtTsEndAdj) ||
				(trip.getLgin() < dtTsAdj && trip.getLgout() > dtTsEndAdj)
				)
				? trip.getLgout() - trip.getLgin() : Misc.getUndefInt();
		
		
		long ugap = trip.getUgin() > 0 && trip.getUgout() > 0
		&& ((trip.getUgin() >= dtTsAdj && trip.getUgin() < dtTsEndAdj) ||
				(trip.getUgout() >= dtTsAdj && trip.getUgout() < dtTsEndAdj) ||
				(trip.getUgin() < dtTsAdj && trip.getUgout() > dtTsEndAdj)
				)
		? trip.getUgout() - trip.getUgin() : Misc.getUndefInt();
		long lugap = trip.getUgin() > 0 && trip.getLgout() > 0
		&& ((trip.getLgout() >= dtTsAdj && trip.getLgout() < dtTsEndAdj) ||
				(trip.getUgin() >= dtTsAdj && trip.getUgin() < dtTsEndAdj) ||
				(trip.getLgout() < dtTsAdj && trip.getUgin() > dtTsEndAdj)
				)
		? trip.getUgin()-trip.getLgout() : Misc.getUndefInt();
	    if (lgap > 0) {
			//L process + transit + process + transit+process
			ifLDelPossibleCnt = (int)(lgap/(2*leadMilli+3*processMilli));
		}
		if (ugap > 0) {
			//U process + transit + process + transit+process
			ifUDelPossibleCnt = (int)(ugap/(2*leadMilli+3*processMilli));
			long estLOfFirstTrip = trip.getLgin()+processMilli+leadMilli;
			long estLOfLastTrip = trip.getLgout()-processMilli-leadMilli-processMilli-leadMilli-processMilli;
			
			boolean valid = (estLOfFirstTrip > dtTsAdj && estLOfFirstTrip < dtTsEndAdj) || 
			(estLOfLastTrip > dtTsAdj && estLOfLastTrip < dtTsEndAdj) ||
			(estLOfFirstTrip < estLOfLastTrip && estLOfFirstTrip < dtTsAdj && estLOfLastTrip > dtTsEndAdj);
			if (!valid)
				ifLDelPossibleCnt = 0;
		}
		if (lugap > 0) {
		//transit u + u process +l transit + l process + transit to u
			ifLUBreakPossibleCnt = (int)(lugap/(3*leadMilli +2*processMilli));
		}
		int first = -1;
		int second = 0;
		if (ifLDelPossibleCnt > 0) {
			first = 1;
			second = ifLDelPossibleCnt;
		}
		if (ifUDelPossibleCnt > second) {
			first = 2;
			second = ifUDelPossibleCnt;
		}
		if (ifLUBreakPossibleCnt > second) {
			first = 3;
			second = ifLUBreakPossibleCnt;
		}
		if ((first == 1 && trip.getUop() < 0) || (first == 2 && trip.getLop() < 0))
			first = 4;
		return new MiscInner.Pair(first, second);
	}
	private void coreCreateStrategyOrig(Connection conn, FastList<SimpleTripData> tripData, int start, int endExcl, ArrayList<HelperStrategy> stratHelper, boolean tripAlreadyBroken, ArrayList<Pair<Integer, SimpleTripData>> modTripList, int vehicleId, double leadFactor) {
		//now we have needs/av but still not op to op level ..
		Collections.sort(stratHelper); //order by desc order of needs
		for (int j=0,js = stratHelper.size();j<js;j++) {
			HelperStrategy curr = stratHelper.get(j);
			int totCnt = curr.need;
			int numDeleted = 0;
			//go through each entry in the day and map to existing trips based on reasonable matches
			//1.on exact match of lop/uop, 
			//2.then lop but non match uop 
			//3.and then non match lop but match uop
			//4.then for extending valid L but empty U to U (if enough time gap)
			//5.then for extending valid U but empty L to L (if enough time gap)
			//Now if still remaining then create new trips
			//
			ArrayList<SimpleTripData> tripsForDayVehicle = helperGetTripsForReporting(tripData, curr.tripListStartIndex, curr.tripListEndIndexIncl);
			for (int k=curr.dateStartIndex,ks=curr.dateEndIndexExcl; k<ks;k++) {//go through each entry in the day and map to existing trips based on reasonable matches
				Requirements req = dataList.get(k);
				if (!req.isValid())
					continue;
				ReferenceData refData = this.getReferenceData(conn, req.getFromId(), req.getToId(), leadFactor);
				long leadTime = refData == null ? (long)(leadFactor*Analysis.maxLeadTime) : (refData.getUgin()-refData.getLgout());
				int cnt = req.getCount();
				
				//1.on exact match of lop/uop,
				for (int art=0;art<3;art++) {
					if (cnt <= 0)
						break;
					int stindex = art == 0 ? curr.tripOnDateStartIndex : art == 1 ? curr.tripListStartIndex :  curr.tripAftDateStartIndexIncl;
					
					int enindex = art == 0 ? curr.tripOnDateEndIndexIncl : art == 1 ? curr.tripBefDateEndIndexIncl: curr.tripListEndIndexIncl;
					
					if (stindex < 0 || enindex < 0)
						continue;
					ArrayList<Integer> tripIndexOrder = new ArrayList<Integer>();
					if (art == 0) {//onDate
						for (int l=stindex; l<=enindex;l++) {
							tripIndexOrder.add(l);
						}
					}
					else if (art == 1) {//bef Date
						for (int l=enindex; l>=stindex;l--) {
							tripIndexOrder.add(l);
						}
					}
					else {//aft Date
						for (int l=stindex; l<=enindex;l++) {
							tripIndexOrder.add(l);
						}
					}
					for (int l1=0,l1s=tripIndexOrder.size(); l1<l1s;l1++) {
						int l = tripIndexOrder.get(l1);
						SimpleTripData trip = tripData.get(l);
						if (trip == null)
							continue;
						if (!Misc.isUndef(trip.getAssignedToReq())) 
							continue;
						if ((trip.getLop() == req.getFromId() || trip.getLop() == req.getFromId2()) && trip.getUop() == req.getToId()) {
							cnt--;
							totCnt--;
							trip.setAssignedToReq(req.getId());
							req.addStrategy(trip, Requirements.TripCreateStrategy.G_TO_MAP, Misc.getUndefInt(), refData, tripsForDayVehicle, null);
							if (cnt == 0)
								break;
						}
					}
					
					//2.then lop but non match uop 					
					if (cnt > 0) {
						for (int l=stindex,ls=enindex; l<=ls;l++) {
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq())) 
								continue;
							if ((trip.getLop() == req.getFromId()  || trip.getLop() == req.getFromId2()) && !Misc.isUndef(trip.getUop())) {
								cnt--;
								totCnt--;
								trip.setAssignedToReq(req.getId());
								req.addStrategy(trip, Requirements.TripCreateStrategy.G_CHANGE_UNLOAD, Misc.getUndefInt(), refData, tripsForDayVehicle, null);
								if (cnt == 0)
									break;
							}
						}	
					}
					
				
					//4.then for extending valid L but empty U to U (if enough time gap)
					if (cnt > 0) {//... see if extensible ... will be extensible if there is enough time gap between this and next assigned stuff
						//if not the trip itself will be marked for deletion ...
						for (int l=curr.tripListStartIndex,ls=curr.tripListEndIndexIncl; l<=ls;l++) {//l may be
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq()) || !Misc.isUndef(trip.getUop()))
								continue;
							if ((trip.getLop() == req.getFromId() || trip.getLop() == req.getFromId2()) && trip.getLgout() > 0) {
								//mean we may want to extend to u ... check if there is enough gap between this and next								
								for (int t1=l+1;;t1++) {
									SimpleTripData next = tripData.get(t1);
									if (next == null)
										break;									
									long gap = 0;
									long gapThresh = 0;
									if (next.getLgin() > 0) {//must have gone to unload, then back to load
										gap = next.getLgin() - trip.getLgout();
										gapThresh = 2*leadTime+1*baseProcessTime;
									}
									else if (next.getUgin() > 0) {//must have gone to unload, back to load and then back to this unload!!
										gap = next.getUgin() - trip.getLgout();
										gapThresh = 3*leadTime+2*baseProcessTime;
									}
									if (gap > gapThresh) {//viable
										req.addStrategy(trip, Requirements.TripCreateStrategy.G_EXTEND_TO_U, trip.getLgout(), refData, tripsForDayVehicle, null);
										trip.setAssignedToReq(req.getId());
										cnt--;
										totCnt--;
										break;
									}
									else {
										if (next.getAssignedToReq() >= -1) {//not viable .. mark this to be deleted
											trip.setAssignedToReq(-2);
											break;
										}
										else {//mark next to be deleted
											next.setAssignedToReq(-2);
										}
									}
								}//checking if enough gap going forward
							}//if matching L found
							if (cnt == 0)
								break;
						}//for each trip
					}//if cnt > 0
					
					//5.then for extending valid U but empty L to L (if enough time gap)
					if (cnt > 0) {//now go through matching U but no L ... see if extensible ... will be extensible if there is enough time gap between this and next assigned stuff
						//if not the trip itself will be marked for deletion ...
						for (int l=curr.tripListEndIndexIncl; l>=curr.tripListStartIndex;l--) {//l may be
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq()) || !Misc.isUndef(trip.getLop()))
								continue;
							if (trip.getUop() == req.getToId() && trip.getUgin() > 0) {
								//mean we may want to extend to u ... check if there is enough gap between this and next								
								for (int t1=l-1;;t1--) {
									SimpleTripData prev = tripData.get(t1);
									if (prev == null)
										break;									
									long gap = 0;
									long gapThresh = 0;
									if (prev.getUgout() > 0) {//from prev gone to some L and this unload
										gap = trip.getUgin() - prev.getUgout();
										gapThresh = 2*leadTime+1*baseProcessTime;
									}
									else if (prev.getLgout() > 0) {//from prev must have gone to unload, then back to load and then back to this unload
										gap =  trip.getUgin() - prev.getLgout();
										gapThresh = 3*leadTime+3*baseProcessTime;
									}
									
									if (gap > gapThresh) {//viable
										req.addStrategy(trip, Requirements.TripCreateStrategy.G_EXTEND_TO_L, trip.getUgin(), refData, tripsForDayVehicle, null);
										trip.setAssignedToReq(req.getId());
										cnt--;
										totCnt--;
										break;
									}
									else {
										if (prev.getAssignedToReq() >= -1) {//not viable .. mark this to be deleted
											trip.setAssignedToReq(-2);
											break;
										}
										else {//mark next to be deleted
											prev.setAssignedToReq(-2);
										}
									}
								}//checking if enough gap going forward
							}//if matching L found
							if (cnt == 0)
								break;
						}//for each trip
					}//if cnt > 0
					
					//3.and then non match lop but match uop
					if (cnt > 0) {
						for (int l=stindex,ls=enindex; l<=ls;l++) {
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq())) 
								continue;
							if (trip.getUop() == req.getToId() && !Misc.isUndef(trip.getLop())) {
								cnt--;
								totCnt--;
								trip.setAssignedToReq(req.getId());
								req.addStrategy(trip, Requirements.TripCreateStrategy.G_CHANGE_LOAD, Misc.getUndefInt(), refData, tripsForDayVehicle, null);
								
							}
							if (cnt <= 0)
								break;
						}	
					}
					
					//4.extend L to U as well as change L
					if (cnt > 0) {//... see if extensible ... will be extensible if there is enough time gap between this and next assigned stuff
						//if not the trip itself will be marked for deletion ...
						for (int l=curr.tripListStartIndex,ls=curr.tripListEndIndexIncl; l<=ls;l++) {//l may be
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq()) || !Misc.isUndef(trip.getUop()))
								continue;
							if (trip.getLgout() > 0) {
								//mean we may want to extend to u ... check if there is enough gap between this and next								
								for (int t1=l+1;;t1++) {
									SimpleTripData next = tripData.get(t1);
									if (next == null)
										break;									
									long gap = 0;
									long gapThresh = 0;
									if (next.getLgin() > 0) {//must have gone to unload, then back to load
										gap = next.getLgin() - trip.getLgout();
										gapThresh = 2*leadTime+1*baseProcessTime;
									}
									else if (next.getUgin() > 0) {//must have gone to unload, back to load and then back to this unload!!
										gap = next.getUgin() - trip.getLgout();
										gapThresh = 3*leadTime+2*baseProcessTime;
									}
									if (gap > gapThresh) {//viable
										req.addStrategy(trip, Requirements.TripCreateStrategy.G_CHANGE_L_EXTEND, trip.getLgout(), refData, tripsForDayVehicle, null);
										trip.setAssignedToReq(req.getId());
										cnt--;
										totCnt--;
										break;
									}
									else {
										if (next.getAssignedToReq() >= -1) {//not viable .. mark this to be deleted
											trip.setAssignedToReq(-2);
											break;
										}
										else {//mark next to be deleted
											next.setAssignedToReq(-2);
										}
									}
								}//checking if enough gap going forward
							}//if matching L found
							if (cnt == 0)
								break;
						}//for each trip
					}//if cnt > 0

					//5.then for extending valid U but empty L to L (if enough time gap)
					if (cnt > 0) {//now go through matching U but no L ... see if extensible ... will be extensible if there is enough time gap between this and next assigned stuff
						//if not the trip itself will be marked for deletion ...
						for (int l=curr.tripListEndIndexIncl; l>=curr.tripListStartIndex;l--) {//l may be
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq()) || !Misc.isUndef(trip.getLop()))
								continue;
							if (trip.getUgin() > 0) {
								//mean we may want to extend to u ... check if there is enough gap between this and next								
								for (int t1=l-1;;t1--) {
									SimpleTripData prev = tripData.get(t1);
									if (prev == null)
										break;									
									long gap = 0;
									long gapThresh = 0;
									if (prev.getUgout() > 0) {//from prev gone to some L and this unload
										gap = trip.getUgin() - prev.getUgout();
										gapThresh = 2*leadTime+1*baseProcessTime;
									}
									else if (prev.getLgout() > 0) {//from prev must have gone to unload, then back to load and then back to this unload
										gap =  trip.getUgin() - prev.getLgout();
										gapThresh = 3*leadTime+3*baseProcessTime;
									}
									
									if (gap > gapThresh) {//viable
										req.addStrategy(trip, Requirements.TripCreateStrategy.G_CHANGE_U_EXTEND, trip.getUgin(), refData, tripsForDayVehicle, null);
										trip.setAssignedToReq(req.getId());
										cnt--;
										totCnt--;
										break;
									}
									else {
										if (prev.getAssignedToReq() >= -1) {//not viable .. mark this to be deleted
											trip.setAssignedToReq(-2);
											break;
										}
										else {//mark next to be deleted
											prev.setAssignedToReq(-2);
										}
									}
								}//checking if enough gap going forward
							}//if matching L found
							if (cnt == 0)
								break;
						}//for each trip
					}//if cnt > 0
					if (cnt <= 0)
						break;
				}
				
			}

			//Now to see if we need to create trip
			if (totCnt > 0) {//we will have to create brand new
				//since we are not creating trips immediately and inorder to make life simpler we will go through trip and 
				//identify markers against which U can be set without overlapping stuff
				//then we will go thru entries for the day and assign each successively from the back
				ArrayList<Triple<Long, Pair<Long, Double>, Pair<SimpleTripData, SimpleTripData>>> umarkersForCreate = new ArrayList<Triple<Long, Pair<Long, Double>, Pair<SimpleTripData, SimpleTripData>>>();
				//2nd.first = Ucreate, 2nd.second = multFactorForDur
				//1st = Lin shld be at least more than this 
				//3rd = bounding ltrip/rtrip for creation of trip
				double avgLeadTimeTemp = 0;
				int countForLeadTimeTemp = 0;
				
				for (int k=curr.dateStartIndex,ks=curr.dateEndIndexExcl; k<ks;k++) {
					Requirements req = dataList.get(k);
					if (!req.isValid())
						continue;
					if (req.getCount() == req.getTripPlan().size()) {
						continue; //already addressed
					}
					ReferenceData refData = this.getReferenceData(conn, req.getFromId(), req.getToId(), leadFactor);
					avgLeadTimeTemp += (refData == null ? (long)(leadFactor*Analysis.maxLeadTime) : (refData.getUgin()-refData.getLgout()));
					countForLeadTimeTemp++;
				}
				long stdAvgLeadTime = (long)(avgLeadTimeTemp/countForLeadTimeTemp);
				long dtTs = curr.dt;
				long dtTsEnd = dtTs+24*60*60*1000;
				//dtTs += Analysis.minusWindowMin*60*1000;
				//dtTsEnd += plusWindowMin*60*1000;
				SimpleTripData maxRHSBound = null;
				SimpleTripData minLHSBound = null;
				double currMultFactor = 1;//we will reduce to 0.9,0.8,0.7
				for (int art =0;art<1;art++) {
					if (totCnt <= 0)
						break;
					currMultFactor = 1;//1-art*0.1;
					//note that there may be opportunity to create stuff after last trip (but before window) and before first trip
					//note also that we are NOT deleting things that are to be deleted or are unused (and hence presumed to be deleted because they are wrong)
					long avgLeadTime = (long)(currMultFactor*stdAvgLeadTime);
					for (int l=curr.tripListEndIndexIncl+1; l>=curr.tripListStartIndex;l--) {//not the plus 1 at end
						//get the right trip
						SimpleTripData rhsTrip = null;
						
						if (l == curr.tripListEndIndexIncl+1) {
							SimpleTripData next = tripData.get(new SimpleTripData(dtTsEnd), 1);
							if (next != null) {
								rhsTrip = next;
							}
							else {
								rhsTrip = new SimpleTripData(Misc.getUndefInt(), Misc.getUndefInt(), dtTsEnd+1*60*60*100+avgLeadTime, Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt());
							}
						}
						else {
							rhsTrip = tripData.get(l);
							if (rhsTrip == null)
								continue; 
							if (rhsTrip.getAssignedToReq() < -1)
								continue; //not really an end marker
						}
						
						//get lhsTrip
						SimpleTripData lhsTrip = null;
						for (int l1=l-1; l1>=0;l1--) {//not the plus 1 at end
							//get the right trip
							SimpleTripData tripTemp = tripData.get(l1);
							if (tripTemp == null)
								continue;
							if (tripTemp.getEarliest() < dtTs) {
								lhsTrip = tripTemp;
								break;
							}
							else if (tripTemp.getAssignedToReq() >= -1) {
								lhsTrip = tripTemp;
								break;
							}
						}
						if (lhsTrip == null) {
							lhsTrip = new SimpleTripData(Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), dtTs-1*baseProcessTime-avgLeadTime, Misc.getUndefInt(), Misc.getUndefInt());
						}
						if (maxRHSBound == null || maxRHSBound.compareTo(rhsTrip) < 0)
							maxRHSBound = rhsTrip;
						if (minLHSBound == null || minLHSBound.compareTo(lhsTrip) > 0)
							minLHSBound = lhsTrip;
						//now see whether trip can be created 
						boolean rhsIsLoad = rhsTrip.getLgin() > 0;
						long rhsTS = rhsIsLoad ? rhsTrip.getLgin() : rhsTrip.getUgin();
						java.util.Date dbgrh1 = Misc.longToUtilDate(rhsTS);
						boolean lhsIsUnload = lhsTrip.getUgout() > 0;
						long lhsTS = lhsIsUnload ? lhsTrip.getUgout() : lhsTrip.getLgout();
						java.util.Date dbglh1 = Misc.longToUtilDate(lhsTS);
						if (!lhsIsUnload) {//we need to add lead LHS_L->LHS_U + process:LHS_U to make things comp .. and then can treat lhsAsUnload
							lhsTS += avgLeadTime+1*baseProcessTime;
						}
						if (!rhsIsLoad) {//we need to add buffer for process RHSL + lead:RHSL->RHSU
							rhsTS -= (avgLeadTime+1*baseProcessTime);
						}
						java.util.Date dbgrh2 = Misc.longToUtilDate(rhsTS);
						java.util.Date dbglh2 = Misc.longToUtilDate(lhsTS);
						//adjust rhsTS going beyond boundary
						if (rhsIsLoad) { //newly created's U +processigTime+lead to L of rhs
							rhsTS = Math.min(rhsTS, dtTsEnd+2*baseProcessTime+2*avgLeadTime);
						}
						else {//new created's U+processingTime + lead to L+processing time + lead to Unload
							rhsTS = Math.min(rhsTS, dtTsEnd+3*baseProcessTime+3*avgLeadTime);
						}
						java.util.Date dbgrh3 = Misc.longToUtilDate(rhsTS);
						
						//whew now finally figure out the thresh suitable for lhsTS and rhsTS						
						long gapThresh = 3*avgLeadTime+2*baseProcessTime; //lead:LHSU -> New L + process:new L+lead:New L->New U+process:New U+lead:New U->RHSL
						int dbgGapThreshMin = (int)(gapThresh/(60*1000));
						long gap = rhsTS-lhsTS;
						int gapThreshMin = (int)(gap/(60*1000));
						double possibleFits = (int) ((double)gap/(double)gapThresh);
						long additionalDelta = 0;//possibleFits > 1 &&totCnt > 1 ? 0 : (gap-gapThresh)/2;
						long prevLMarker = rhsTS;
						while (gap > gapThresh) {//will break out
							long tsref = prevLMarker - baseProcessTime-avgLeadTime-additionalDelta;
							long newLMarker = tsref - baseProcessTime-avgLeadTime-additionalDelta;
							java.util.Date dbgts1 = new java.util.Date(prevLMarker);
							java.util.Date dbgts2 = new java.util.Date(newLMarker);
							java.util.Date dbgts3 = new java.util.Date(tsref);
							java.util.Date dbgts4 = new java.util.Date(rhsTS);
							if (newLMarker > dtTsEnd) {
								prevLMarker = newLMarker;
								continue;
							}
							if (!(tsref > dtTs && newLMarker < dtTsEnd)) {
								break;
							}
							//if (newLMarker < lhsTS || (newLMarker+additionalDelta) < (dtTs+Analysis.minusWindowMin))
							//	break;
							umarkersForCreate.add(new Triple<Long, Pair<Long, Double>, Pair<SimpleTripData, SimpleTripData>>(lhsTS+(long)((avgLeadTime+baseProcessTime)*0.75),new Pair<Long, Double>(tsref, currMultFactor), new Pair<SimpleTripData, SimpleTripData>(lhsTrip, rhsTrip)));
							prevLMarker = newLMarker;
							totCnt--;
							if (totCnt == 0)
								break;
						}
						if (totCnt == 0)
							break;
					}//for all trips
				}
				if (totCnt > 0 && !tripAlreadyBroken && true) {//remove false ... break
					this.breakTrip(conn, tripData, modTripList, vehicleId);
					for (int t1=start;t1<endExcl;t1++) {
						Requirements req = dataList.get(t1);
						req.resetStrategy();
					}
					coreCreateStrategyOrig(conn, tripData, start, endExcl, stratHelper, true, modTripList, vehicleId, leadFactor);
					return;
				}
				else {
					int idx = 0;
					Analysis.helperAddLHSBound(tripsForDayVehicle, minLHSBound);
					Analysis.helperAddRHSBound(tripsForDayVehicle, maxRHSBound);
					
					for (int k=curr.dateStartIndex,ks=curr.dateEndIndexExcl; k<ks;k++) {
						Requirements req = dataList.get(k);
						if (!req.isValid())
							continue;
						if (req.getCount() == req.getTripPlan().size()) {
							continue; //already addressed
						}
						if (idx >= umarkersForCreate.size()) {
							req.addStrategy(null, Requirements.TripCreateStrategy.G_UNKNOWN, Misc.getUndefInt(), this.getReferenceData(conn, req.getFromId(), req.getToId(), leadFactor), tripsForDayVehicle, null);
						}
						else {
							long refTS = umarkersForCreate.get(idx).second.first;
							double fac = umarkersForCreate.get(idx).second.second;
							String addnlParam = Double.toString(fac);
							req.addStrategy(null, Requirements.TripCreateStrategy.G_CREATE_FULL, refTS, this.getReferenceData(conn, req.getFromId(), req.getToId(), leadFactor), tripsForDayVehicle, addnlParam);
						}
						idx++;
					}//for each entry for day
				}
			}//if full created needed
		}//for all days;
	}

	private void createTripStrategyBefBreakTripToo(Connection conn) {
		int endExcl = 0;
		for (int is = dataList.size(); endExcl < is; ) {
			Triple<Integer, Integer, Integer> bounds = getBounds(endExcl);
			int vehicleId = bounds.first;
			int start = bounds.second;
			endExcl = bounds.third;
			if (start == endExcl)
				break;
			Requirements stReq = dataList.get(start);
			Requirements enReq = dataList.get(endExcl-1);
			long tsstart = stReq.getDate()+this.minusWindowMin;
			long tsen = enReq.getDate();
			tsen += 24*3600*1000+this.plusWindowMin;
			
			FastList<SimpleTripData> tripData = SimpleTripData.getTrips(conn, vehicleId, tsstart, tsen, opsOfInterest);
			ArrayList<HelperStrategy> stratHelper = new ArrayList<HelperStrategy>();
			for (int j=start;j<endExcl;) {//j set to nextJ inside loop the index of the next Date
				Requirements req = dataList.get(j);
				int nextJ = j+1;
				//first get all entries corresponding to same date ..
				long dtTs = req.getDate();
				long dtTsEnd = dtTs+24*60*60*1000;
				int cnt = req.getCount();
				for (;nextJ<endExcl;nextJ++) {
					Requirements innerReq = dataList.get(nextJ);
					if (innerReq.isValid()) {
						if (innerReq.getDate() > dtTs)
							break;
						cnt += innerReq.getCount();
					}
				}
				//get available Trips ... we will get trips that have some overlap
				int refIndex = tripData.indexOf(new SimpleTripData(dtTs)).first;
				int stIndex = -1;
				int enIndex = -1;
				for (int t1=refIndex;;t1--) {
					SimpleTripData temp = tripData.get(t1);
					if (temp == null)
						break;
					long lin = temp.getLgin() > 0 ? temp.getLgin() : temp.getUgin() - Analysis.maxLeadTime-Analysis.baseProcessTime;
					long uout = temp.getUgout() > 0 ? temp.getUgout() : temp.getLgout()+Analysis.maxLeadTime+Analysis.baseProcessTime;
					if (lin <= dtTsEnd && uout >= dtTs) {
						if (stIndex < 0 || stIndex > t1)
							stIndex = t1;
						if (enIndex < 0 || enIndex < t1)
							enIndex = t1;
					}
					else {
						break;
					}
				}
				for (int t1=refIndex+1;;t1++) {
					SimpleTripData temp = tripData.get(t1);
					if (temp == null)
						break;
					long lin = temp.getLgin() > 0 ? temp.getLgin() : temp.getUgin() - Analysis.maxLeadTime-Analysis.baseProcessTime;
					long uout = temp.getUgout() > 0 ? temp.getUgout() : temp.getLgout()+Analysis.maxLeadTime+Analysis.baseProcessTime;
					if (lin <= dtTsEnd && uout >= dtTs) {
						if (stIndex < 0 || stIndex > t1)
							stIndex = t1;
						if (enIndex < 0 || enIndex < t1)
							enIndex = t1;
					}
					else {
						break;
					}
				}
				int avTrip = enIndex-stIndex+1;
				if (stIndex == -1)
					avTrip = 0;
				int preDated = 0;
				int postDated = 0;
				stratHelper.add(new HelperStrategy(dtTs, j, nextJ, cnt, avTrip, preDated, postDated, stIndex, enIndex,0,0,0,-1,-1,-1,-1));
				j = nextJ;
			}//for each vehicle
			//now we have needs/av but still not op to op level ..
			Collections.sort(stratHelper); //order by desc order of needs
			for (int j=0,js = stratHelper.size();j<js;j++) {
				HelperStrategy curr = stratHelper.get(j);
				int totCnt = curr.need;
				int numDeleted = 0;
				//go through each entry in the day and map to existing trips based on reasonable matches
				//1.on exact match of lop/uop, 
				//2.then lop but non match uop 
				//3.and then non match lop but match uop
				//4.then for extending valid L but empty U to U (if enough time gap)
				//5.then for extending valid U but empty L to L (if enough time gap)
				//Now if still remaining then create new trips
				//
				ArrayList<SimpleTripData> tripsForDayVehicle = helperGetTripsForReporting(tripData, curr.tripListStartIndex, curr.tripListEndIndexIncl);
				for (int k=curr.dateStartIndex,ks=curr.dateEndIndexExcl; k<ks;k++) {//go through each entry in the day and map to existing trips based on reasonable matches
					Requirements req = dataList.get(k);
					if (!req.isValid())
						continue;
					ReferenceData refData = this.getReferenceData(conn, req.getFromId(), req.getToId(), 1);
					long leadTime = refData == null ? 4*60*60*1000 : (refData.getUgin()-refData.getLgout());
					int cnt = req.getCount();

					//1.on exact match of lop/uop, 
					for (int l=curr.tripListStartIndex,ls=curr.tripListEndIndexIncl; l<=ls;l++) {
						SimpleTripData trip = tripData.get(l);
						if (trip == null)
							continue;
						if (!Misc.isUndef(trip.getAssignedToReq())) 
							continue;
						if (trip.getLop() == req.getFromId() && trip.getUop() == req.getToId()) {
							cnt--;
							totCnt--;
							trip.setAssignedToReq(req.getId());
							req.addStrategy(trip, Requirements.TripCreateStrategy.G_TO_MAP, Misc.getUndefInt(), refData, tripsForDayVehicle, null);
							if (cnt == 0)
								break;
						}
					}
					
					//2.then lop but non match uop 					
					if (cnt > 0) {
						for (int l=curr.tripListStartIndex,ls=curr.tripListEndIndexIncl; l<=ls;l++) {
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq())) 
								continue;
							if (trip.getLop() == req.getFromId() && !Misc.isUndef(trip.getUop())) {
								cnt--;
								totCnt--;
								trip.setAssignedToReq(req.getId());
								req.addStrategy(trip, Requirements.TripCreateStrategy.G_CHANGE_UNLOAD, Misc.getUndefInt(), refData, tripsForDayVehicle, null);
								if (cnt == 0)
									break;
							}
						}	
					}
					
					//3.and then non match lop but match uop
					if (cnt > 0) {
						for (int l=curr.tripListStartIndex,ls=curr.tripListEndIndexIncl; l<=ls;l++) {
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq())) 
								continue;
							if (trip.getUop() == req.getToId() && !Misc.isUndef(trip.getLop())) {
								cnt--;
								totCnt--;
								trip.setAssignedToReq(req.getId());
								req.addStrategy(trip, Requirements.TripCreateStrategy.G_CHANGE_LOAD, Misc.getUndefInt(), refData, tripsForDayVehicle, null);
							}
						}	
					}
					
					//4.then for extending valid L but empty U to U (if enough time gap)
					if (cnt > 0) {//... see if extensible ... will be extensible if there is enough time gap between this and next assigned stuff
						//if not the trip itself will be marked for deletion ...
						for (int l=curr.tripListStartIndex,ls=curr.tripListEndIndexIncl; l<=ls;l++) {//l may be
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq()) || !Misc.isUndef(trip.getUop()))
								continue;
							if (trip.getLop() == req.getFromId() && trip.getLgout() > 0) {
								//mean we may want to extend to u ... check if there is enough gap between this and next								
								for (int t1=l+1;;t1++) {
									SimpleTripData next = tripData.get(t1);
									if (next == null)
										break;									
									long gap = 0;
									long gapThresh = 0;
									if (next.getLgin() > 0) {//must have gone to unload, then back to load
										gap = next.getLgin() - trip.getLgout();
										gapThresh = 2*leadTime+1*baseProcessTime;
									}
									else if (next.getUgin() > 0) {//must have gone to unload, back to load and then back to this unload!!
										gap = next.getUgin() - trip.getLgout();
										gapThresh = 3*leadTime+2*baseProcessTime;
									}
									if (gap > gapThresh) {//viable
										req.addStrategy(trip, Requirements.TripCreateStrategy.G_EXTEND_TO_U, trip.getLgout(), refData, tripsForDayVehicle, null);
										trip.setAssignedToReq(req.getId());
										cnt--;
										totCnt--;
										break;
									}
									else {
										if (next.getAssignedToReq() >= -1) {//not viable .. mark this to be deleted
											trip.setAssignedToReq(-2);
											break;
										}
										else {//mark next to be deleted
											next.setAssignedToReq(-2);
										}
									}
								}//checking if enough gap going forward
							}//if matching L found
							if (cnt == 0)
								break;
						}//for each trip
					}//if cnt > 0
					
					//5.then for extending valid U but empty L to L (if enough time gap)
					if (cnt > 0) {//now go through matching U but no L ... see if extensible ... will be extensible if there is enough time gap between this and next assigned stuff
						//if not the trip itself will be marked for deletion ...
						for (int l=curr.tripListEndIndexIncl; l>=curr.tripListStartIndex;l--) {//l may be
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq()) || !Misc.isUndef(trip.getLop()))
								continue;
							if (trip.getUop() == req.getToId() && trip.getUgin() > 0) {
								//mean we may want to extend to u ... check if there is enough gap between this and next								
								for (int t1=l-1;;t1--) {
									SimpleTripData prev = tripData.get(t1);
									if (prev == null)
										break;									
									long gap = 0;
									long gapThresh = 0;
									if (prev.getUgout() > 0) {//from prev gone to some L and this unload
										gap = trip.getUgin() - prev.getUgout();
										gapThresh = 2*leadTime+1*baseProcessTime;
									}
									else if (prev.getLgout() > 0) {//from prev must have gone to unload, then back to load and then back to this unload
										gap =  trip.getUgin() - prev.getLgout();
										gapThresh = 3*leadTime+3*baseProcessTime;
									}
									
									if (gap > gapThresh) {//viable
										req.addStrategy(trip, Requirements.TripCreateStrategy.G_EXTEND_TO_L, trip.getUgin(), refData, tripsForDayVehicle, null);
										trip.setAssignedToReq(req.getId());
										cnt--;
										totCnt--;
										break;
									}
									else {
										if (prev.getAssignedToReq() >= -1) {//not viable .. mark this to be deleted
											trip.setAssignedToReq(-2);
											break;
										}
										else {//mark next to be deleted
											prev.setAssignedToReq(-2);
										}
									}
								}//checking if enough gap going forward
							}//if matching L found
							if (cnt == 0)
								break;
						}//for each trip
					}//if cnt > 0
				}

				//Now to see if we need to create trip
				if (totCnt > 0) {//we will have to create brand new
					//since we are not creating trips immediately and inorder to make life simpler we will go through trip and 
					//identify markers against which U can be set without overlapping stuff
					//then we will go thru entries for the day and assign each successively from the back
					ArrayList<Triple<Long, Pair<Long, Double>, Pair<SimpleTripData, SimpleTripData>>> umarkersForCreate = new ArrayList<Triple<Long, Pair<Long, Double>, Pair<SimpleTripData, SimpleTripData>>>();
					//2nd.first = Ucreate, 2nd.second = multFactorForDur
					//1st = Lin shld be at least more than this 
					//3rd = bounding ltrip/rtrip for creation of trip
					double avgLeadTimeTemp = 0;
					int countForLeadTimeTemp = 0;
					double currMultFactor = 1;//we will reduce to 0.9,0.8,0.7
					for (int k=curr.dateStartIndex,ks=curr.dateEndIndexExcl; k<ks;k++) {
						Requirements req = dataList.get(k);
						if (!req.isValid())
							continue;
						if (req.getCount() == req.getTripPlan().size()) {
							continue; //already addressed
						}
						ReferenceData refData = this.getReferenceData(conn, req.getFromId(), req.getToId(),1);
						avgLeadTimeTemp += (refData == null ? 4*60*60*1000 : (refData.getUgin()-refData.getLgout()));
						countForLeadTimeTemp++;
					}
					long stdAvgLeadTime = (long)(avgLeadTimeTemp/countForLeadTimeTemp);
					long dtTs = curr.dt;
					long dtTsEnd = dtTs+24*60*60*1000;
					dtTs += Analysis.minusWindowMin*60*1000;
					dtTsEnd = dtTsEnd+plusWindowMin*60*1000;
					SimpleTripData maxRHSBound = null;
					SimpleTripData minLHSBound = null;
					
					for (int art =0;art<4;art++) {
						if (totCnt <= 0)
							break;
						currMultFactor -= art*0.1;
						//note that there may be opportunity to create stuff after last trip (but before window) and before first trip
						//note also that we are NOT deleting things that are to be deleted or are unused (and hence presumed to be deleted because they are wrong)
						long avgLeadTime = (long)(currMultFactor*stdAvgLeadTime);
						for (int l=curr.tripListEndIndexIncl+1; l>=curr.tripListStartIndex;l--) {//not the plus 1 at end
							//get the right trip
							SimpleTripData rhsTrip = null;
							if (l == curr.tripListEndIndexIncl+1) {
								SimpleTripData next = tripData.get(new SimpleTripData(dtTsEnd), 1);
								if (next != null) {
									rhsTrip = next;
								}
								else {
									rhsTrip = new SimpleTripData(Misc.getUndefInt(), Misc.getUndefInt(), dtTsEnd+1*60*60*100+avgLeadTime, Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt());
								}
							}
							else {
								rhsTrip = tripData.get(l);
								if (rhsTrip == null)
									continue; 
								if (rhsTrip.getAssignedToReq() < -1)
									continue; //not really an end marker
							}
							
							//get lhsTrip
							SimpleTripData lhsTrip = null;
							for (int l1=l-1; l1>=0;l1--) {//not the plus 1 at end
								//get the right trip
								SimpleTripData tripTemp = tripData.get(l1);
								if (tripTemp == null)
									continue;
								if (tripTemp.getEarliest() < dtTs) {
									lhsTrip = tripTemp;
									break;
								}
								else if (tripTemp.getAssignedToReq() >= -1) {
									lhsTrip = tripTemp;
									break;
								}
							}
							if (lhsTrip == null) {
								lhsTrip = new SimpleTripData(Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), dtTs-1*baseProcessTime-avgLeadTime, Misc.getUndefInt(), Misc.getUndefInt());
							}
							if (maxRHSBound == null || maxRHSBound.compareTo(rhsTrip) < 0)
								maxRHSBound = rhsTrip;
							if (minLHSBound == null || minLHSBound.compareTo(lhsTrip) > 0)
								minLHSBound = lhsTrip;
							//now see whether trip can be created 
							boolean rhsIsLoad = rhsTrip.getLgin() > 0;
							long rhsTS = rhsIsLoad ? rhsTrip.getLgin() : rhsTrip.getUgin();
							boolean lhsIsUnload = lhsTrip.getUgout() > 0;
							long lhsTS = lhsIsUnload ? lhsTrip.getUgout() : lhsTrip.getLgout();
							if (!lhsIsUnload) {//we need to add lead LHS_L->LHS_U + process:LHS_U to make things comp .. and then can treat lhsAsUnload
								lhsTS += avgLeadTime+1*baseProcessTime;
							}
							if (!rhsIsLoad) {//we need to add buffer for process RHSL + lead:RHSL->RHSU
								rhsTS -= (avgLeadTime+1*baseProcessTime);
							}
							//adjust rhsTS going beyond boundary
							if (rhsIsLoad) { //newly created's U +processigTime+lead to L of rhs
								rhsTS = Math.min(rhsTS, dtTsEnd+baseProcessTime+avgLeadTime);
							}
							else {//new created's U+processingTime + lead to L+processing time + lead to Unload
								rhsTS = Math.min(rhsTS, dtTsEnd+2*baseProcessTime+2*avgLeadTime);
							}
							
							
							//whew now finally figure out the thresh suitable for lhsTS and rhsTS						
							long gapThresh = 3*avgLeadTime+2*baseProcessTime; //lead:LHSU -> New L + process:new L+lead:New L->New U+process:New U+lead:New U->RHSL
							int dbgGapThreshMin = (int)(gapThresh/(60*1000));
							long gap = rhsTS-lhsTS;
							int gapThreshMin = (int)(gap/(60*1000));
							double possibleFits = (int) ((double)gap/(double)gapThresh);
							long additionalDelta = possibleFits > 1 &&totCnt > 1 ? 0 : (gap-gapThresh)/2;
							long prevLMarker = rhsTS;
							while (gap > gapThresh) {//will break out
								long tsref = prevLMarker - baseProcessTime-avgLeadTime-additionalDelta;
								long newLMarker = tsref - baseProcessTime-avgLeadTime-additionalDelta;
								if (newLMarker < lhsTS || (newLMarker+additionalDelta) < (dtTs+Analysis.minusWindowMin))
									break;
								umarkersForCreate.add(new Triple<Long, Pair<Long, Double>, Pair<SimpleTripData, SimpleTripData>>(lhsTS+(long)((avgLeadTime+baseProcessTime)*0.75),new Pair<Long, Double>(tsref, currMultFactor), new Pair<SimpleTripData, SimpleTripData>(lhsTrip, rhsTrip)));
								prevLMarker = newLMarker;
								totCnt--;
								if (totCnt == 0)
									break;
							}
							if (totCnt == 0)
								break;
						}//for all trips
					}
					int idx = 0;
					Analysis.helperAddLHSBound(tripsForDayVehicle, minLHSBound);
					Analysis.helperAddRHSBound(tripsForDayVehicle, maxRHSBound);
					
					for (int k=curr.dateStartIndex,ks=curr.dateEndIndexExcl; k<ks;k++) {
						Requirements req = dataList.get(k);
						if (!req.isValid())
							continue;
						if (req.getCount() == req.getTripPlan().size()) {
							continue; //already addressed
						}
						if (idx >= umarkersForCreate.size()) {
							req.addStrategy(null, Requirements.TripCreateStrategy.G_UNKNOWN, Misc.getUndefInt(), this.getReferenceData(conn, req.getFromId(), req.getToId(),1), tripsForDayVehicle, null);
						}
						else {
							long refTS = umarkersForCreate.get(idx).second.first;
							double fac = umarkersForCreate.get(idx).second.second;
							String addnlParam = Double.toString(fac);
							req.addStrategy(null, Requirements.TripCreateStrategy.G_CREATE_FULL, refTS, this.getReferenceData(conn, req.getFromId(), req.getToId(),1), tripsForDayVehicle, addnlParam);
						}
						idx++;
					}//for each entry for day
				}//if full created needed
			}//for all days;
			//now remember trips being marked for delete
			for (int l=tripData.size()-1;l>=0;l--) {
				SimpleTripData trip = tripData.get(l);
				if (trip.getAssignedToReq() ==-2 || Misc.isUndef(trip.getAssignedToReq())) {
					this.rememberForDelete(vehicleId, trip);
					tripData.remove(l);
				}
			}
		}//for all items in dataList
	}

/*
	private void createTripStrategyOrig(Connection conn) {
		int endExcl = 0;
		for (int is = dataList.size(); endExcl < is; ) {
			Triple<Integer, Integer, Integer> bounds = getBounds(endExcl);
			int vehicleId = bounds.first;
			int start = bounds.second;
			endExcl = bounds.third;
			if (start == endExcl)
				break;
			Requirements stReq = dataList.get(start);
			Requirements enReq = dataList.get(endExcl-1);
			long tsstart = stReq.getDate()+this.minusWindowMin;
			long tsen = enReq.getDate();
			tsen += 24*3600*1000+this.plusWindowMin;
			
			FastList<SimpleTripData> tripData = SimpleTripData.getTrips(conn, vehicleId, tsstart, tsen, opsOfInterest);
			ArrayList<HelperStrategy> stratHelper = new ArrayList<HelperStrategy>();
			for (int j=start;j<endExcl;) {//j set to nextJ inside loop the index of the next Date
				Requirements req = dataList.get(j);
				int nextJ = j+1;
				//first get all entries corresponding to same date ..
				long dtTs = req.getDate();
				long dtTsEnd = dtTs+24*60*60*1000;
				int cnt = req.getCount();
				for (;nextJ<endExcl;nextJ++) {
					Requirements innerReq = dataList.get(nextJ);
					if (innerReq.isValid()) {
						if (innerReq.getDate() > dtTs)
							break;
						cnt += innerReq.getCount();
					}
				}
				//get available Trips
				long st = dtTs+Analysis.minusWindowMin*60*1000;
				long en = dtTsEnd+Analysis.plusWindowMin*60*1000;
				Pair<Integer, Boolean> stIndexPr = tripData.indexOf(new SimpleTripData(st));
				Pair<Integer, Boolean> enIndexPr = tripData.indexOf(new SimpleTripData(en));
				int stIndex = stIndexPr.second ? stIndexPr.first : stIndexPr.first+1;
				int enIndex = enIndexPr.first;
				int avTrip = 0;
				int preDated = 0;
				int postDated = 0;
				for (int k=stIndex;k<=enIndex;k++) {
					SimpleTripData trip = tripData.get(k);
					if (trip == null)
						continue;
					if (Misc.isUndef(trip.getAssignedToReq())) {
						avTrip++;
						if (trip.getUgInOrSuitable() < dtTs)
							preDated++;
						if (trip.getUgInOrSuitable() > dtTsEnd)
							postDated++;
					}
				}
				stratHelper.add(new HelperStrategy(dtTs, j, nextJ, cnt, avTrip, preDated, postDated, stIndex, enIndex));
				j = nextJ;
			}//for each vehicle
			//now we have needs/av but still not op to op level .. 
			for (int j=0,js = stratHelper.size();j<js;j++) {
				HelperStrategy curr = stratHelper.get(j);
				int totCnt = curr.need;
				int numDeleted = 0;
				//go through each entry in the day and map to existing trips based on reasonable matches
				//1.on exact match of lop/uop, 
				//2.then lop but non match uop 
				//3.and then non match lop but match uop
				//4.then for extending valid L but empty U to U (if enough time gap)
				//5.then for extending valid U but empty L to L (if enough time gap)
				//Now if still remaining then create new trips
				//
				for (int k=curr.dateStartIndex,ks=curr.dateEndIndexExcl; k<ks;k++) {//go through each entry in the day and map to existing trips based on reasonable matches
					Requirements req = dataList.get(k);
					if (!req.isValid())
						continue;
					ReferenceData refData = this.getReferenceData(conn, req.getFromId(), req.getToId());
					long leadTime = refData == null ? 4*60*60*1000 : (refData.getUgin()-refData.getLgout());
					int cnt = req.getCount();

					//1.on exact match of lop/uop, 
					for (int l=curr.tripListStartIndex,ls=curr.tripListEndIndexIncl; l<=ls;l++) {
						SimpleTripData trip = tripData.get(l);
						if (trip == null)
							continue;
						if (!Misc.isUndef(trip.getAssignedToReq())) 
							continue;
						if (trip.getLop() == req.getFromId() && trip.getUop() == req.getToId()) {
							cnt--;
							totCnt--;
							trip.setAssignedToReq(req.getId());
							req.addStrategy(trip, Requirements.TripCreateStrategy.G_TO_MAP, Misc.getUndefInt(), refData, tripData.get(l-1), tripData.get(l+1));
							if (cnt == 0)
								break;
						}
					}
					
					//2.then lop but non match uop 					
					if (cnt > 0) {
						for (int l=curr.tripListStartIndex,ls=curr.tripListEndIndexIncl; l<=ls;l++) {
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq())) 
								continue;
							if (trip.getLop() == req.getFromId() && !Misc.isUndef(trip.getUop())) {
								cnt--;
								totCnt--;
								trip.setAssignedToReq(req.getId());
								req.addStrategy(trip, Requirements.TripCreateStrategy.G_CHANGE_UNLOAD, Misc.getUndefInt(), refData, tripData.get(l-1), tripData.get(l+1));
								if (cnt == 0)
									break;
							}
						}	
					}
					
					//3.and then non match lop but match uop
					if (cnt > 0) {
						for (int l=curr.tripListStartIndex,ls=curr.tripListEndIndexIncl; l<=ls;l++) {
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq())) 
								continue;
							if (trip.getUop() == req.getToId() && !Misc.isUndef(trip.getLop())) {
								cnt--;
								totCnt--;
								trip.setAssignedToReq(req.getId());
								req.addStrategy(trip, Requirements.TripCreateStrategy.G_CHANGE_LOAD, Misc.getUndefInt(), refData, tripData.get(l-1), tripData.get(l+1));
							}
						}	
					}
					
					//4.then for extending valid L but empty U to U (if enough time gap)
					if (cnt > 0) {//... see if extensible ... will be extensible if there is enough time gap between this and next assigned stuff
						//if not the trip itself will be marked for deletion ...
						for (int l=curr.tripListStartIndex,ls=curr.tripListEndIndexIncl; l<=ls;l++) {//l may be
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq()) || !Misc.isUndef(trip.getUop()))
								continue;
							if (trip.getLop() == req.getFromId() && trip.getLgout() > 0) {
								//mean we may want to extend to u ... check if there is enough gap between this and next								
								for (int t1=l+1;;t1++) {
									SimpleTripData next = tripData.get(t1);
									if (next == null)
										break;									
									long gap = 0;
									long gapThresh = 0;
									if (next.getLgin() > 0) {//must have gone to unload, then back to load
										gap = next.getLgin() - trip.getLgout();
										gapThresh = 2*leadTime+1*baseProcessTime;
									}
									else if (next.getUgin() > 0) {//must have gone to unload, back to load and then back to this unload!!
										gap = next.getUgin() - trip.getLgout();
										gapThresh = 3*leadTime+2*baseProcessTime;
									}
									if (gap > gapThresh) {//viable
										req.addStrategy(trip, Requirements.TripCreateStrategy.G_EXTEND_TO_U, trip.getLgout(), refData, null, next);
										trip.setAssignedToReq(req.getId());
										cnt--;
										totCnt--;
										break;
									}
									else {
										if (next.getAssignedToReq() >= -1) {//not viable .. mark this to be deleted
											trip.setAssignedToReq(-2);
											break;
										}
										else {//mark next to be deleted
											next.setAssignedToReq(-2);
										}
									}
								}//checking if enough gap going forward
							}//if matching L found
							if (cnt == 0)
								break;
						}//for each trip
					}//if cnt > 0
					
					//5.then for extending valid U but empty L to L (if enough time gap)
					if (cnt > 0) {//now go through matching U but no L ... see if extensible ... will be extensible if there is enough time gap between this and next assigned stuff
						//if not the trip itself will be marked for deletion ...
						for (int l=curr.tripListEndIndexIncl; l>=curr.tripListStartIndex;l--) {//l may be
							SimpleTripData trip = tripData.get(l);
							if (trip == null)
								continue;
							if (!Misc.isUndef(trip.getAssignedToReq()) || !Misc.isUndef(trip.getLop()))
								continue;
							if (trip.getUop() == req.getToId() && trip.getUgin() > 0) {
								//mean we may want to extend to u ... check if there is enough gap between this and next								
								for (int t1=l-1;;t1--) {
									SimpleTripData prev = tripData.get(t1);
									if (prev == null)
										break;									
									long gap = 0;
									long gapThresh = 0;
									if (prev.getUgout() > 0) {//from prev gone to some L and this unload
										gap = trip.getUgin() - prev.getUgout();
										gapThresh = 2*leadTime+1*baseProcessTime;
									}
									else if (prev.getLgout() > 0) {//from prev must have gone to unload, then back to load and then back to this unload
										gap =  trip.getUgin() - prev.getLgout();
										gapThresh = 3*leadTime+3*baseProcessTime;
									}
									
									if (gap > gapThresh) {//viable
										req.addStrategy(trip, Requirements.TripCreateStrategy.G_EXTEND_TO_L, trip.getUgin(), refData, prev, null);
										trip.setAssignedToReq(req.getId());
										cnt--;
										totCnt--;
										break;
									}
									else {
										if (prev.getAssignedToReq() >= -1) {//not viable .. mark this to be deleted
											trip.setAssignedToReq(-2);
											break;
										}
										else {//mark next to be deleted
											prev.setAssignedToReq(-2);
										}
									}
								}//checking if enough gap going forward
							}//if matching L found
							if (cnt == 0)
								break;
						}//for each trip
					}//if cnt > 0
				}

				//Now to see if we need to create trip
				if (totCnt > 0) {//we will have to create brand new
					//since we are not creating trips immediately and inorder to make life simpler we will go through trip and 
					//identify markers against which U can be set without overlapping stuff
					//then we will go thru entries for the day and assign each successively from the back
					ArrayList<Triple<Long,Long, Pair<SimpleTripData, SimpleTripData>>> umarkersForCreate = new ArrayList<Triple<Long, Long, Pair<SimpleTripData, SimpleTripData>>>();
					//2nd = Ucreate, //1st = Lin shld be at least more than this 
					//3rd = bounding ltrip/rtrip for creation of trip
					double avgLeadTimeTemp = 0;
					int countForLeadTimeTemp = 0;
					for (int k=curr.dateStartIndex,ks=curr.dateEndIndexExcl; k<ks;k++) {
						Requirements req = dataList.get(k);
						if (!req.isValid())
							continue;
						if (req.getCount() == req.getTripPlan().size()) {
							continue; //already addressed
						}
						ReferenceData refData = this.getReferenceData(conn, req.getFromId(), req.getToId());
						avgLeadTimeTemp += (refData == null ? 4*60*60*1000 : (refData.getUgin()-refData.getLgout()));
						countForLeadTimeTemp++;
					}
					long avgLeadTime = (long)(avgLeadTimeTemp/countForLeadTimeTemp);
					//note that there may be opportunity to create stuff after last trip (but before window) and before first trip
					//note also that we are NOT deleting things that are to be deleted or are unused (and hence presumed to be deleted because they are wrong)

					long dtTs = curr.dt;
					long dtTsEnd = dtTs+24*60*60*1000;
					dtTs += Analysis.minusWindowMin*60*1000;
					dtTsEnd = dtTsEnd+plusWindowMin*60*1000;
					for (int l=curr.tripListEndIndexIncl+1; l>=curr.tripListStartIndex;l--) {//not the plus 1 at end
						//get the right trip
						SimpleTripData rhsTrip = null;
						if (l == curr.tripListEndIndexIncl+1) {
							SimpleTripData next = tripData.get(new SimpleTripData(dtTsEnd), 1);
							if (next != null) {
								rhsTrip = next;
							}
							else {
								rhsTrip = new SimpleTripData(Misc.getUndefInt(), Misc.getUndefInt(), dtTsEnd+1*60*60*100+avgLeadTime, Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt());
							}
						}
						else {
							rhsTrip = tripData.get(l);
							if (rhsTrip == null)
								continue; 
							if (rhsTrip.getAssignedToReq() < -1)
								continue; //not really an end marker
						}
						
						//get lhsTrip
						SimpleTripData lhsTrip = null;
						for (int l1=l-1; l1>=0;l1--) {//not the plus 1 at end
							//get the right trip
							SimpleTripData tripTemp = tripData.get(l1);
							if (tripTemp == null)
								continue;
							if (tripTemp.getUgInOrSuitable() < dtTs) {
								lhsTrip = tripTemp;
								break;
							}
							else if (tripTemp.getAssignedToReq() >= -1) {
								lhsTrip = tripTemp;
								break;
							}
						}
						if (lhsTrip == null) {
							lhsTrip = new SimpleTripData(Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), dtTs-1*baseProcessTime-avgLeadTime);
						}
						//now see whether trip can be created 
						boolean rhsIsLoad = rhsTrip.getLgin() > 0;
						long rhsTS = rhsIsLoad ? rhsTrip.getLgin() : rhsTrip.getUgin();
						boolean lhsIsUnload = lhsTrip.getUgout() > 0;
						long lhsTS = lhsIsUnload ? lhsTrip.getUgout() : lhsTrip.getLgout();
						//adjust rhsTS going beyond boundary
						if (rhsIsLoad) { //newly created's U +processigTime+lead to L of rhs
							rhsTS = Math.min(rhsTS, dtTsEnd+baseProcessTime+avgLeadTime);
						}
						else {//new created's U+processingTime + lead to L+processing time + lead to Unload
							rhsTS = Math.min(rhsTS, dtTsEnd+2*baseProcessTime+2*avgLeadTime);
						}
						//adjust lhsTS going beyond left boundary
						if (lhsIsUnload) { //u of lhs to newly created L lead+newly created L processtime+lead to unload
							lhsTS = Math.max(lhsTS, dtTs-2*avgLeadTime-1*baseProcessTime);
						}
						else { //lead of l of lhs to U + process time at U + lead to new L + process at new L + lead to Unload
							lhsTS = Math.max(lhsTS, dtTs-3*avgLeadTime - 1*baseProcessTime);
						}
						if (!lhsIsUnload) {//we need to add lead LHS_L->LHS_U + process:LHS_U to make things comp .. and then can treat lhsAsUnload
							lhsTS += avgLeadTime+1*baseProcessTime;
						}
						if (!rhsIsLoad) {//we need to add buffer for process RHSL + lead:RHSL->RHSU
							rhsTS -= (avgLeadTime+1*baseProcessTime);
						}
						//whew now finally figure out the thresh suitable for lhsTS and rhsTS						
						long gapThresh = 3*avgLeadTime+2*baseProcessTime; //lead:LHSU -> New L + process:new L+lead:New L->New U+process:New U+lead:New U->RHSL
						int dbgGapThreshMin = (int)(gapThresh/(60*1000));
						long gap = rhsTS-lhsTS;
						int gapThreshMin = (int)(gap/(60*1000));
						double possibleFits = (int) ((double)gap/(double)gapThresh);
						long additionalDelta = possibleFits > 1 &&totCnt > 1 ? 0 : (gap-gapThresh)/2;
						long prevLMarker = rhsTS;
						while (gap > gapThresh) {//will break out
							long tsref = prevLMarker - baseProcessTime-avgLeadTime-additionalDelta;
							long newLMarker = tsref - baseProcessTime-avgLeadTime-additionalDelta;
							if (newLMarker < lhsTS)
								break;
							umarkersForCreate.add(new Triple<Long, Long, Pair<SimpleTripData, SimpleTripData>>(lhsTS+(long)((avgLeadTime+baseProcessTime)*0.75),tsref, new Pair<SimpleTripData, SimpleTripData>(lhsTrip, rhsTrip)));
							prevLMarker = newLMarker;
							totCnt--;
							if (totCnt == 0)
								break;
						}
						if (totCnt == 0)
							break;
					}//for all trips
					int idx = 0;
					for (int k=curr.dateStartIndex,ks=curr.dateEndIndexExcl; k<ks;k++) {
						Requirements req = dataList.get(k);
						if (!req.isValid())
							continue;
						if (req.getCount() == req.getTripPlan().size()) {
							continue; //already addressed
						}
						if (idx >= umarkersForCreate.size()) {
							SimpleTripData lhs = tripData.get(curr.tripListStartIndex);
							SimpleTripData rhs = tripData.get(curr.tripListEndIndexIncl);
							req.addStrategy(null, Requirements.TripCreateStrategy.G_UNKNOWN, Misc.getUndefInt(), this.getReferenceData(conn, req.getFromId(), req.getToId()), lhs, rhs);
						}
						else {
							req.addStrategy(null, Requirements.TripCreateStrategy.G_CREATE_FULL, umarkersForCreate.get(idx).second, this.getReferenceData(conn, req.getFromId(), req.getToId()), umarkersForCreate.get(idx).third.first, umarkersForCreate.get(idx).third.second);
						}
						idx++;
					}//for each entry for day
				}//if full created needed
			}//for all days;
			//now remember trips being marked for delete
			for (int l=tripData.size()-1;l>=0;l--) {
				SimpleTripData trip = tripData.get(l);
				if (trip.getAssignedToReq() ==-2 || Misc.isUndef(trip.getAssignedToReq())) {
					this.rememberForDelete(vehicleId, trip);
					tripData.remove(l);
				}
			}
		}//for all items in dataList
	}
*/
	public ReferenceData getReferenceData(Connection conn, int fromId, int toId, double leadFactor) {
		if (allReferenceData == null) {
			prepAndLoadReferenceData(conn, loadGpsData, false, leadFactor);	
		}
		ReferenceData retval = allReferenceData.get(getKey(fromId, toId));
		if (retval == null) {
			retval = ReferenceData.createDummy(fromId, toId, leadFactor);
			allReferenceData.put(getKey(fromId, toId), retval);
		}
		return retval;
	}

	public Triple<Integer, Integer, Integer> getBounds( int prevEndExcl) {
		//first = vehicleId, second = startIncl, third = endExcl
		int vehicleId = Misc.getUndefInt();
		int start = prevEndExcl;
		int endExcl = start;
		boolean firstFound = false;
		for (int i=prevEndExcl,is = dataList.size();i<is;i++) {
			Requirements req = dataList.get(i);
			if (req.isValid()) {
				if (firstFound && vehicleId != req.getVehicleId()) {
					break;
				}
				if (!firstFound) {
					start = i;
					vehicleId = req.getVehicleId();
					firstFound = true;
				}
				endExcl = i+1;
				
				
			}
		}
		if (start == endExcl) {
			start = dataList.size();
			endExcl = start;
		}
		return new Triple<Integer, Integer, Integer>(vehicleId, start, endExcl);
	}
	public void prepAndLoadReferenceData(Connection conn, boolean loadGpsData, boolean doCreateRef, double leadFactor) {
		if (dataList == null)
			return;
		if (this.allReferenceData != null)
			return;
		allReferenceData = new HashMap<String, ReferenceData>();
		if (false) //OLD DVC was true
			return;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			StringBuilder sb = new StringBuilder();
			Pair<StringBuilder, StringBuilder> fromToList = getFromToList(dataList);
			if (fromToList.first.length() == 0 || fromToList.second.length() == 0)
				return;
			if (false && doCreateRef) {
			//1 make sure eta_setup_op_to_op is populated
			//2 then if eta dist within +- 5% then weightage to tot lead time else etc etc
				sb.setLength(0);
				sb.append(" insert into eta_setup_op_to_op(lopid, uopid, load_lead_dist, load_lead_minute) ( ")
					.append(" select trip_info.load_gate_op, trip_info.unload_gate_op, avg(l2.attribute_value-l1.attribute_value)*1.1 d1, avg(timestampdiff(minute, load_gate_out, unload_gate_in)) ts ")
					.append(" from ( ")
					.append(" select til.load_gate_op, til.unload_gate_op from ")
					.append(" (select distinct load_gate_op, unload_gate_op from trip_info join op_station lop on (lop.id = load_gate_op) join op_station uop on (uop.id = unload_gate_op) ")
					.append(" where lop.name not like '%-stop%' and uop.name not like '%-stop%' ")
					.append(" and lop.id in (");
				sb.append(fromToList.first);
				sb.append(" ) ");
				sb.append(" and uop.id in (");
				sb.append(fromToList.second);
				sb.append(") and (trip_info.art = 0) ");
				sb.append(" ) til ")
					.append(" where not exists (select 1 from eta_setup_op_to_op est where est.lopid = til.load_gate_op and est.uopid = til.unload_gate_op) ")
					.append(" ) lopl join trip_info on (lopl.load_gate_op = trip_info.load_gate_op and lopl.unload_gate_op = trip_info.unload_gate_op) ")
					.append(" left outer join logged_data l1 on (l1.vehicle_id = trip_info.vehicle_id and l1.attribute_id=0 and l1.gps_record_time = load_gate_out) ")
					.append(" left outer join logged_data l2 on (l2.vehicle_id = trip_info.vehicle_id and l2.attribute_id=0 and l2.gps_record_time = unload_gate_in) ")
					.append(" where (trip_info.art = 0) ")
					.append(" group by trip_info.load_gate_op, trip_info.unload_gate_op ")
					.append(" ) ")
				;
				System.out.println("[PREPREF 1]"+sb);
				ps = conn.prepareStatement(sb.toString());
				ps.execute();
				ps = Misc.closePS(ps);
					
				sb.setLength(0);
				sb.append(" update trip_info ti join eta_setup_op_to_op est on (est.lopid = ti.load_gate_op and est.uopid = ti.unload_gate_op) ")
					.append(" left outer join logged_data l1 on (l1.vehicle_id = ti.vehicle_id and l1.attribute_id=0 and l1.gps_record_time = load_gate_out) ")
					.append(" left outer join logged_data l2 on (l2.vehicle_id = ti.vehicle_id and l2.attribute_id=0 and l2.gps_record_time = unload_gate_in) ")
					.append(" set ti.ref_score = cast(abs(l2.attribute_value-l1.attribute_value - est.load_lead_dist)/est.load_lead_dist*100/3 as decimal(10,0))*3 ")
					.append(" where ti.load_gate_op in ( ")
					.append(fromToList.first)
					.append(" ) ")
					.append(" and ti.unload_gate_op in ( ")
					.append(fromToList.second)
					.append(" ) and (ti.art=0) ")
					;
				System.out.println("[PREPREF 2]"+sb);
				ps = conn.prepareStatement(sb.toString());
				ps.execute();
				ps = Misc.closePS(ps);
			}
			
			
			sb.setLength(0);
			sb.append("select mrt.id, mrt.vehicle_id, mrt.load_gate_op, mrt.unload_gate_op, mrt.load_gate_in, mrt.load_gate_out, mrt.unload_gate_in, mrt.unload_gate_out, mta.mi ");
                //sb.append(" from trip_info join (select load_gate_op lop, unload_gate_op uop, min(ref_score) rsc from trip_info where load_gate_op in ( ");
                //sb.append(fromToList.first);
                //sb.append(" ) and unload_gate_op in ( ");
                //sb.append(fromToList.second);
                //sb.append(" ) and trip_info.art = 0 group by load_gate_op, unload_gate_op) scrlist ")
                //.append("on (trip_info.load_gate_op = scrlist.lop and trip_info.unload_gate_op = scrlist.uop and trip_info.ref_score = scrlist.rsc) where trip_info.art=0 ")
			   sb.append(" from manual_ref_trip mrt left outer join manual_time_adjust mta on (mta.lop_id=mrt.load_gate_op and mta.uop_id=mrt.unload_gate_op) ")
                .append("order by mrt.load_gate_op, mrt.unload_gate_op, timestampdiff(minute, mrt.load_gate_in, mrt.unload_gate_out) asc, mrt.load_gate_in, mta.mi, mrt.id desc");
                System.out.println("[PREPREF 3]"+sb);
            ps = conn.prepareStatement(sb.toString());
            rs = ps.executeQuery();
            int prevMrtId = Misc.getUndefInt();
            while (rs.next()) {
            	int tripId = Misc.getRsetInt(rs, 1);
            	if (tripId == prevMrtId)
            		continue;
            	prevMrtId = tripId;
            	int vehicleId = Misc.getRsetInt(rs, 2);
            	int lop = Misc.getRsetInt(rs, 3);
            	int uop = Misc.getRsetInt(rs, 4);
            	long lgin = Misc.sqlToLong(rs.getTimestamp(5));
            	long lgout = Misc.sqlToLong(rs.getTimestamp(6));
            	long ugin = Misc.sqlToLong(rs.getTimestamp(7));
            	long ugout = Misc.sqlToLong(rs.getTimestamp(8));
            	double desiredMinMi = Misc.getRsetDouble(rs, 9);
            	double min = (double)(ugin-lgout)/(60.0*1000.0);
            	double factor = 0.99;
            	if (!Misc.isUndef(min) && min > desiredMinMi) {
            		factor = (double)desiredMinMi/min;
            	}
            	String key = getKey(lop, uop);
            	ReferenceData dat = allReferenceData.get(key);
            	if (dat != null)
            		continue;
            	
            	dat = new ReferenceData(lop, uop, lgin, lgout, ugin, ugout, vehicleId, tripId, factor);
            	if (loadGpsData)
            		dat.getLoggedData(conn);
            	allReferenceData.put(key, dat);
            }
            rs = Misc.closeRS(rs);
            ps = Misc.closePS(ps);
            
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			
		}
	}
	
	
	private static String getKey(int fromId, int toId) {
		return fromId+"_"+toId;
	}
	private static Pair<StringBuilder, StringBuilder> getFromToList(ArrayList<Requirements> todoList) {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		for (Requirements req:todoList) {
			int fromId = req.getFromId();
			int toId = req.getToId();
			if (Misc.isUndef(fromId) || Misc.isUndef(toId))
				continue;
			if (!Misc.isUndef(fromId) && sb1.indexOf(Integer.toString(fromId)) < 0) {
				if (sb1.length() != 0)
					sb1.append(",");
				sb1.append(fromId);
			}
			if (!Misc.isUndef(toId) && sb2.indexOf(Integer.toString(toId)) < 0) {
				if (sb2.length() != 0)
					sb2.append(",");
				sb2.append(toId);
			}
		}
		return new Pair<StringBuilder, StringBuilder>(sb1, sb2);
	}
	
	public ArrayList<Requirements> getDataList() {
		return dataList;
	}

	public void setDataList(ArrayList<Requirements> dataList) {
		this.dataList = dataList;
	}

	public Map<String, Integer> getOpsNameToId() {
		return opsNameToId;
	}

	public void setOpsNameToId(Map<String, Integer> opsNameToId) {
		this.opsNameToId = opsNameToId;
	}

	public Map<Integer, Integer> getOpsOfInterest() {
		return opsOfInterest;
	}

	public void setOpsOfInterest(Map<Integer, Integer> opsOfInterest) {
		this.opsOfInterest = opsOfInterest;
	}

	public ArrayList<Pair<Integer, SimpleTripData>> getMarkedForDelete() {
		return markedForDelete;
	}

	public void setMarkedForDelete(
			ArrayList<Pair<Integer, SimpleTripData>> markedForDelete) {
		this.markedForDelete = markedForDelete;
	}
	
	public Map<Integer, Requirements> getMapOfRequirementsByDataId() {
		Map<Integer, Requirements> retval = new HashMap<Integer, Requirements>();
		for (Requirements req:dataList) {
			retval.put(req.getId(), req);
		}
		return retval;
	}
	
	public Map<Integer, ReferenceData> getMapOfReferenceDataByTripId() {
		Map<Integer, ReferenceData> retval = new HashMap<Integer, ReferenceData>();
		Collection<ReferenceData> values = this.allReferenceData.values();
		for (ReferenceData dat: values) {
			int id = dat.getTripId();
			if (Misc.isUndef(id))
				continue;
			retval.put(id, dat);
		}
		return retval;
	}
	
	public ReferenceData getReferenceDataById(Connection conn, Map<Integer, ReferenceData> allRef, int fromId, int toId, int tripId, double leadFactor) throws SQLException {
		ReferenceData retval = Misc.isUndef(tripId) ? this.getReferenceData(conn, fromId, toId, leadFactor) : allRef.get(tripId);
		if (retval == null) {//read from data
			PreparedStatement ps = conn.prepareStatement("select id, vehicle_id, load_gate_op, unload_gate_op, load_gate_in, load_gate_out, unload_gate_in, unload_gate_out from trip_info where id = ?");
			ps.setInt(1, tripId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				//tripId = Misc.getRsetInt(rs, 1);
            	int vehicleId = Misc.getRsetInt(rs, 2);
            	int lop = Misc.getRsetInt(rs, 3);
            	int uop = Misc.getRsetInt(rs, 4);
            	long lgin = Misc.sqlToLong(rs.getTimestamp(5));
            	long lgout = Misc.sqlToLong(rs.getTimestamp(6));
            	long ugin = Misc.sqlToLong(rs.getTimestamp(7));
            	long ugout = Misc.sqlToLong(rs.getTimestamp(8));
            	String key = getKey(lop, uop);
            	ReferenceData dat = new ReferenceData(lop, uop, lgin, lgout, ugin, ugout, vehicleId, tripId, leadFactor);
            	if (loadGpsData)
            		dat.getLoggedData(conn);
            	allRef.put(tripId, dat);
            	retval = dat;//copy n paste of code :(
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		if (retval == null) {
			retval = getReferenceData(conn, fromId, toId, leadFactor);
			if (!Misc.isUndef(tripId))
				allRef.put(tripId, retval);
		}
		return retval;
	}
	
	public Pair<ArrayList<Requirements>, ArrayList<MiscInner.Pair>> getDataListFromXML(Connection conn, int portNodeId, String xmlStr, double leadFactor) throws Exception {
		ArrayList<Requirements> retval = new ArrayList<Requirements>();
		ArrayList<MiscInner.Pair> modTrip = new ArrayList<MiscInner.Pair>();
		
		Document xmlDoc = MyXMLHelper.loadFromString(xmlStr);
		if (xmlDoc == null || xmlDoc.getDocumentElement() == null)
			return new Pair<ArrayList<Requirements>, ArrayList<MiscInner.Pair>>(retval, modTrip);
		SimpleDateFormat sdfHHMM = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		Map<Integer, ReferenceData> allRef = getMapOfReferenceDataByTripId();
		Map<Integer, Requirements> dataIdMap = getMapOfRequirementsByDataId();
		
		for (Node n = xmlDoc.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != 1)
				continue;
			Element e1 = (Element)n;
			String tagName = e1.getTagName();
			for (Node en=e1.getFirstChild(); en != null; en = en.getNextSibling()) {
				if (en.getNodeType() != 1)
					continue;
				Element e = (Element) en;
			
				if ("mod".equals(tagName)) {
					int tripId = Misc.getParamAsInt(e.getAttribute("mod_trip_id"));
					int modCode = Misc.getParamAsInt(e.getAttribute("mod_code"));
					modTrip.add(new MiscInner.Pair(tripId, modCode));
				}
				else {
					int strategy = Misc.getParamAsInt(e.getAttribute("strategy_code"));
					if (strategy == Requirements.TripCreateStrategy.G_ALREADY_MAPPED || strategy == Requirements.TripCreateStrategy.G_DELETE_THIS || strategy == Requirements.TripCreateStrategy.G_UNKNOWN)
						continue;
					int dataId = Misc.getParamAsInt(e.getAttribute("data_id"));
					if (Misc.isUndef(dataId))
						continue;
					int mappedTripId = Misc.getParamAsInt(e.getAttribute("mapped_trip_id"));
					int refTripId = Misc.getParamAsInt(e.getAttribute("ref_trip_id"));
					String relevantTS = Misc.getParamAsString(e.getAttribute("relevant_ts"), null);
					java.util.Date dt = Misc.getParamAsDate(relevantTS, null, sdfHHMM);
					long dtTs = dt == null ? Misc.getUndefInt() : dt.getTime();
					Requirements asAskedByUser = dataIdMap.get(dataId);
					String addnlParam = Misc.getParamAsString(e.getAttribute("addnl_param"), null);
					if (asAskedByUser == null)
						continue;
					Requirements updated = new Requirements(dataId, portNodeId, null, null,
							null, null, asAskedByUser.getCount(), null,
							asAskedByUser.getVehicleId(), asAskedByUser.getTs(), asAskedByUser.getFromId(), asAskedByUser.getToId(), Misc.getUndefInt(), Misc.getUndefInt(), true, true, true, Misc.getUndefInt());
					retval.add(updated);
					SimpleTripData base = !Misc.isUndef(mappedTripId) ? SimpleTripData.getTripDataById(conn, mappedTripId) : null;
					ReferenceData referenceData = getReferenceDataById(conn, allRef, asAskedByUser.getFromId(), asAskedByUser.getToId(), refTripId, leadFactor); 
					updated.addStrategy(base, strategy, dtTs, referenceData, null, addnlParam);
				}
			}
		}
		return new Pair<ArrayList<Requirements>, ArrayList<MiscInner.Pair>>(retval, modTrip);
	}

	public void breakAndSaveTrip(Connection conn,  ArrayList<MiscInner.Pair> modTripList) {
		HashMap<Integer,Integer> brokenupTripMap = new HashMap<Integer, Integer>();
		for (MiscInner.Pair pr : modTripList) {
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				int tripId = pr.first;
				int modCode = pr.second;
				if (modCode == SimpleTripData.G_BREAK_TRIP) {
					ps = conn.prepareStatement("insert into trip_info(vehicle_id, shift_date, unload_area_wait_in, unload_area_wait_out, unload_gate_in, unload_gate_out, unload_gate_op, unload_area_in, unload_area_out, unload_material_guess) (select vehicle_id, shift_date, unload_area_wait_in, unload_area_wait_out, unload_gate_in, unload_gate_out, unload_gate_op, unload_area_in, unload_area_out, unload_material_guess from trip_info where id= ?)");
					ps.setInt(1, tripId);
					ps.execute();
					rs = ps.getGeneratedKeys();
					if (rs.next()) {
						brokenupTripMap.put(tripId, rs.getInt(1));
					}
					rs = Misc.closeRS(rs);
					ps = Misc.closePS(ps);
					ps = conn.prepareStatement("update trip_info set unload_area_guess=null, unload_gate_in=null, unload_gate_out=null, unload_area_wait_in=null, unload_area_wait_out=null,unload_gate_op=null,unload_area_in=null, unload_area_out=null, unload_material_guess=null where id=?"); 
					ps.setInt(1, tripId);
					ps.execute();
					ps = Misc.closePS(ps);
				}
				else if (modCode == SimpleTripData.G_DROP_LOAD) {
					ps = conn.prepareStatement("update trip_info set load_area_guess = null, load_gate_in=null, load_gate_out=null, load_area_wait_in=null, load_area_wait_out=null,unload_gate_op=null,load_area_in=null, load_area_out=null, load_material_guess=null where id=?"); 
					ps.setInt(1, tripId);
					ps.execute();
					ps = Misc.closePS(ps);
				}
				else if (modCode == SimpleTripData.G_DROP_UNLOAD) {
					ps = conn.prepareStatement("update trip_info set unload_area_guess=null, unload_gate_in=null, unload_gate_out=null, unload_area_wait_in=null, unload_area_wait_out=null,unload_gate_op=null,unload_area_in=null, unload_area_out=null, unload_material_guess=null where id=?"); 
					ps.setInt(1, tripId);
					ps.execute();
					ps = Misc.closePS(ps);
				}
				else if (modCode == SimpleTripData.G_DELETE_TRIP) {
					ps = conn.prepareStatement(" delete from trip_info where id=?"); 
					ps.setInt(1, tripId);
					ps.execute();
					ps = Misc.closePS(ps);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				ps = Misc.closePS(ps);
				rs = Misc.closeRS(rs);
			}
		}
	}
	
	public void createAndSaveTrip(Connection conn, ArrayList<Requirements> saveList, boolean doOnlyMapped) {
		if (!Analysis.g_doExcatDate) {
			this.createAndSaveTripNonDVC(conn, saveList, doOnlyMapped);
			return;
		}
		for (Requirements req:saveList) {
			if (!req.hasFullDate())
				continue;
			for (Requirements.TripCreateStrategy strategy : req.getTripPlan()) {
				PreparedStatement ps = null;
				try {
					int approach = strategy.getStrategy();
					if (approach == Requirements.TripCreateStrategy.G_ALREADY_MAPPED || approach == Requirements.TripCreateStrategy.G_UNKNOWN)
						continue;
					if (doOnlyMapped && approach != Requirements.TripCreateStrategy.G_TO_MAP)
						continue;
					
					SimpleTripData mappedTrip = strategy.getBase();
					ReferenceData referenceData = strategy.getReferenceData();
					double leadFactor = referenceData == null ? 1 : referenceData.getBaseFactor();
					long leadTime = (long)(leadFactor* (double) ( referenceData == null ? Analysis.maxLeadTime :  referenceData.getUgin()-referenceData.getLgout()));
					if (leadTime > Analysis.maxLeadTime)
						leadTime = Analysis.maxLeadTime;
					long processTime = Analysis.baseProcessTime;
					double adjFactor = Misc.getParamAsDouble(strategy.getAddnlParam(), 0.99);

					//DVC exact Ugin
					//referenceData = ReferenceData.createDummy(req.getFromId(), req.getToId(), Misc.getParamAsDouble(strategy.getAddnlParam(), 1));
					
					long relevantTS = strategy.getTargetDate();
					int tripId = mappedTrip == null ? Misc.getUndefInt() : mappedTrip.getTripId();
	//				if (brokenupTripMap.containsKey(tripId))
	//					tripId = brokenupTripMap.get(tripId);
					ps = conn.prepareStatement("delete from manual_trip_assigned where manual_trip_desired_id=?");
					ps.setInt(1,req.getId());
					ps.execute();
					ps = Misc.closePS(ps);
					if (approach == Requirements.TripCreateStrategy.G_TO_MAP) {
						ps = conn.prepareStatement("insert into manual_trip_assigned (manual_trip_desired_id, trip_info_id,src) values (?,?,0)");
						ps.setInt(1,req.getId());
						ps.setInt(2, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
						strategy.setStrategy(Requirements.TripCreateStrategy.G_ALREADY_MAPPED);
					}
					if (approach == Requirements.TripCreateStrategy.G_CHANGE_LOAD) {
						ps = conn.prepareStatement("update trip_info set load_gate_op = ?, art = ? where id = ?");
						ps.setInt(1,req.getFromId());
						ps.setInt(2, req.getId());
						ps.setInt(3, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
						ps = conn.prepareStatement("insert into manual_trip_assigned (manual_trip_desired_id, trip_info_id,src) values (?,?,1)");
						ps.setInt(1,req.getId());
						ps.setInt(2, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
					}
					else if (approach == Requirements.TripCreateStrategy.G_CHANGE_UNLOAD) {
						ps = conn.prepareStatement("update trip_info set unload_gate_op = ?, art = ? where id = ?");
						ps.setInt(1,req.getToId());
						ps.setInt(2, req.getId());
						ps.setInt(3, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
						ps = conn.prepareStatement("insert into manual_trip_assigned (manual_trip_desired_id, trip_info_id,src) values (?,?,2)");
						ps.setInt(1,req.getId());
						ps.setInt(2, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
					}
					else if (approach == Requirements.TripCreateStrategy.G_EXTEND_TO_U || approach == Requirements.TripCreateStrategy.G_CHANGE_L_EXTEND) {
						String s1 = "update trip_info set unload_gate_op=?, unload_gate_in = ?, unload_area_wait_in = ?, unload_gate_out=?, unload_area_wait_out=?, art=?, shift_date=?";
						if (approach == Requirements.TripCreateStrategy.G_CHANGE_L_EXTEND) {
							s1 += ", load_gate_op=?";
						}
						s1 += " where id=?";
						//L to U
						boolean useAdjProcessTimeForCreate = true;
						long gapAfterProcessForUgout = (long)(5.0*60.0*1000.0*(1-0.1*Math.random()));
						long ugout = req.getTs()+gapAfterProcessForUgout;
						long ugin = ugout-(long)((adjFactor > 0.6 ? 0.6 : adjFactor) * (double) Analysis.baseProcessTime * (1-0.1*Math.random()));
						//long lgout = ugin-(long)(adjFactor*leadTime * (1-0.1*Math.random()));
						//long lgin = lgout - (long)(adjFactor * (double) Analysis.baseProcessTime * (1-0.1*Math.random()));
						
						ps = conn.prepareStatement(s1);
						int op = req.getToId();
						int colIndex = 1;
						long shiftDate = Requirements.getDate(ugin);
						Timestamp shiftDateTS = Misc.longToSqlDate(shiftDate);
						java.sql.Timestamp inTS = Misc.longToSqlDate(ugin);
						java.sql.Timestamp outTS = Misc.longToSqlDate(ugout);
						
						ps.setInt(colIndex++, op);
						ps.setTimestamp(colIndex++, inTS);
						ps.setTimestamp(colIndex++, inTS);
						ps.setTimestamp(colIndex++, outTS);
						ps.setTimestamp(colIndex++, outTS);
						ps.setInt(colIndex++, req.getId());
						ps.setTimestamp(colIndex++, shiftDateTS);
						if (approach == Requirements.TripCreateStrategy.G_CHANGE_L_EXTEND) {
							ps.setInt(colIndex++, req.getFromId());
						}
						ps.setInt(colIndex++, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
						ps = conn.prepareStatement("insert into manual_trip_assigned (manual_trip_desired_id, trip_info_id,src) values (?,?,3)");
						ps.setInt(1,req.getId());
						ps.setInt(2, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
					}
					else if (approach == Requirements.TripCreateStrategy.G_EXTEND_TO_L || approach == Requirements.TripCreateStrategy.G_CHANGE_U_EXTEND) {
						String s1 = "update trip_info set load_gate_op=?, load_gate_in = ?, load_area_wait_in = ?, load_gate_out=?, load_area_wait_out=?, art=?";
						if (approach == Requirements.TripCreateStrategy.G_CHANGE_U_EXTEND)
							s1 += ", unload_gate_op = ? ";
						s1 += " where id=?";
						ps = conn.prepareStatement(s1);
						int op = req.getFromId();
						
						long lgout = mappedTrip.getUgin() - (long)(adjFactor*(double)leadTime* (1-0.1*Math.random()));
						long lgin = lgout -  (long)(adjFactor*(double)Analysis.baseProcessTime* (1-0.1*Math.random()));
						java.sql.Timestamp inTS = Misc.longToSqlDate(lgin);
						java.sql.Timestamp outTS = Misc.longToSqlDate(lgout);
						int colIndex = 1;
						ps.setInt(colIndex++, op);
						ps.setTimestamp(colIndex++, inTS);
						ps.setTimestamp(colIndex++, inTS);
						ps.setTimestamp(colIndex++, outTS);
						ps.setTimestamp(colIndex++, outTS);
						ps.setInt(colIndex++, req.getId());
						if (approach == Requirements.TripCreateStrategy.G_CHANGE_U_EXTEND)
							ps.setInt(colIndex++, req.getToId());
						ps.setInt(colIndex++, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
						ps = conn.prepareStatement("insert into manual_trip_assigned (manual_trip_desired_id, trip_info_id,src) values (?,?,4)");
						ps.setInt(1,req.getId());
						ps.setInt(2, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
					}
					else if (approach == Requirements.TripCreateStrategy.G_CREATE_FULL) {
						boolean useAdjProcessTimeForCreate = true;
						long gapAfterProcessForUgout = (long)(5.0*60.0*1000.0*(1-0.1*Math.random()));
						long ugout = req.getTs()+gapAfterProcessForUgout;
						long ugin = ugout-(long)(adjFactor * (double) Analysis.baseProcessTime * (1-0.1*Math.random()));
						long lgout = ugin-(long)(adjFactor*(double)leadTime * (1-0.1*Math.random()));
						long lgin = lgout - (long)(adjFactor * (double) Analysis.baseProcessTime * (1-0.1*Math.random()));
						
						
						long shiftDate = Requirements.getDate(ugin);
						Timestamp shiftDateTS = Misc.longToSqlDate(shiftDate);
						Timestamp uginTS = Misc.longToSqlDate(ugin);
						Timestamp ugoutTS = Misc.longToSqlDate(ugout);
						Timestamp lginTS = Misc.longToSqlDate(lgin);
						Timestamp lgoutTS = Misc.longToSqlDate(lgout);
						int lop = req.getFromId();
						int uop = req.getToId();
						
						ps = conn.prepareStatement("insert into trip_info(vehicle_id, load_gate_op, load_area_wait_in, load_gate_in, load_gate_out, load_area_wait_out, unload_gate_op, unload_area_wait_in, unload_gate_in, unload_gate_out, unload_area_wait_out,art,shift_date,shift) values "+
								"(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
						int colIndex = 1;
						ps.setInt(colIndex++, req.getVehicleId());
						ps.setInt(colIndex++, lop);
						ps.setTimestamp(colIndex++, lginTS);
						ps.setTimestamp(colIndex++, lginTS);
						ps.setTimestamp(colIndex++, lgoutTS);
						ps.setTimestamp(colIndex++, lgoutTS);
						ps.setInt(colIndex++, uop);
						ps.setTimestamp(colIndex++, uginTS);
						ps.setTimestamp(colIndex++, uginTS);
						ps.setTimestamp(colIndex++, ugoutTS);
						ps.setTimestamp(colIndex++, ugoutTS);
						
						ps.setInt(colIndex++, req.getId());
						ps.setTimestamp(colIndex++, shiftDateTS);
						ps.setInt(colIndex++, 0);
						ps.execute();
						ResultSet rs1 = ps.getGeneratedKeys();
						int newTripId = rs1.next() ? rs1.getInt(1) : Misc.getUndefInt();
						rs1 = Misc.closeRS(rs1);
						ps = Misc.closePS(ps);
						ps = conn.prepareStatement("insert into manual_trip_assigned (manual_trip_desired_id, trip_info_id,src) values (?,?,5)");
						ps.setInt(1,req.getId());
						ps.setInt(2, newTripId);
						ps.execute();
						ps = Misc.closePS(ps);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					ps = Misc.closePS(ps);
				}
			}//for each strategy
		}//for each req
	}

	public void createAndSaveTripNonDVC(Connection conn, ArrayList<Requirements> saveList, boolean doOnlyMapped) {
		for (Requirements req:saveList) {
			if (req.hasFullDate())
				continue;
			for (Requirements.TripCreateStrategy strategy : req.getTripPlan()) {
				PreparedStatement ps = null;
				try {
					int approach = strategy.getStrategy();
					if (approach == Requirements.TripCreateStrategy.G_ALREADY_MAPPED || approach == Requirements.TripCreateStrategy.G_UNKNOWN)
						continue;
					if (doOnlyMapped && approach != Requirements.TripCreateStrategy.G_TO_MAP)
						continue;
					
					SimpleTripData mappedTrip = strategy.getBase();
					ReferenceData referenceData = strategy.getReferenceData();
					if (referenceData == null)
						referenceData = ReferenceData.createDummy(req.getFromId(), req.getToId(), Misc.getParamAsDouble(strategy.getAddnlParam(), 1));
					
					double leadFactor = referenceData == null ? 1 : referenceData.getBaseFactor();
					long leadTime = (long)(leadFactor* (double) ( referenceData == null ? Analysis.maxLeadTime :  referenceData.getUgin()-referenceData.getLgout()));
					if (leadTime > Analysis.maxLeadTime)
						leadTime = Analysis.maxLeadTime;
					long processTime = Analysis.baseProcessTime;
					double adjFactor = Misc.getParamAsDouble(strategy.getAddnlParam(), 0.99);


					long relevantTS = strategy.getTargetDate();
					int tripId = mappedTrip == null ? Misc.getUndefInt() : mappedTrip.getTripId();
	//				if (brokenupTripMap.containsKey(tripId))
	//					tripId = brokenupTripMap.get(tripId);
					ps = conn.prepareStatement("delete from manual_trip_assigned where manual_trip_desired_id=?");
					ps.setInt(1,req.getId());
					ps.execute();
					ps = Misc.closePS(ps);
					if (approach == Requirements.TripCreateStrategy.G_TO_MAP) {
						ps = conn.prepareStatement("insert into manual_trip_assigned (manual_trip_desired_id, trip_info_id,src) values (?,?,0)");
						ps.setInt(1,req.getId());
						ps.setInt(2, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
						strategy.setStrategy(Requirements.TripCreateStrategy.G_ALREADY_MAPPED);
					}
					if (approach == Requirements.TripCreateStrategy.G_CHANGE_LOAD) {
						ps = conn.prepareStatement("update trip_info set load_gate_op = ?, art = ? where id = ?");
						ps.setInt(1,req.getFromId());
						ps.setInt(2, req.getId());
						ps.setInt(3, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
						ps = conn.prepareStatement("insert into manual_trip_assigned (manual_trip_desired_id, trip_info_id,src) values (?,?,1)");
						ps.setInt(1,req.getId());
						ps.setInt(2, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
					}
					else if (approach == Requirements.TripCreateStrategy.G_CHANGE_UNLOAD) {
						ps = conn.prepareStatement("update trip_info set unload_gate_op = ?, art = ? where id = ?");
						ps.setInt(1,req.getToId());
						ps.setInt(2, req.getId());
						ps.setInt(3, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
						ps = conn.prepareStatement("insert into manual_trip_assigned (manual_trip_desired_id, trip_info_id,src) values (?,?,2)");
						ps.setInt(1,req.getId());
						ps.setInt(2, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
					}
					else if (approach == Requirements.TripCreateStrategy.G_EXTEND_TO_U || approach == Requirements.TripCreateStrategy.G_CHANGE_L_EXTEND) {
						String s1 = "update trip_info set unload_gate_op=?, unload_gate_in = ?, unload_area_wait_in = ?, unload_gate_out=?, unload_area_wait_out=?, art=?, shift_date=?";
						if (approach == Requirements.TripCreateStrategy.G_CHANGE_L_EXTEND) {
							s1 += ", load_gate_op=?";
						}
						s1 += " where id=?";
						ps = conn.prepareStatement(s1);
						int op = req.getToId();
						long lgoutToUgin = (long)(adjFactor * leadTime);
						lgoutToUgin = (long)(lgoutToUgin*(1-Math.random()*0.1));
						long uginToUgOut = (long)(Analysis.baseProcessTime*adjFactor);
						uginToUgOut = (long)(uginToUgOut*(1-Math.random()*0.1));
						int colIndex = 1;
						if (relevantTS <= 0)
							relevantTS =mappedTrip.getLgout(); 
						long gin = relevantTS+lgoutToUgin;
						Timestamp ginTS = Misc.longToSqlDate(gin);
						long gout =gin+uginToUgOut;
						Timestamp goutTS = Misc.longToSqlDate(gout);
						long shiftDate = Requirements.getDate(gin);
						Timestamp shiftDateTS = Misc.longToSqlDate(shiftDate);
						ps.setInt(colIndex++, op);
						ps.setTimestamp(colIndex++, ginTS);
						ps.setTimestamp(colIndex++, ginTS);
						ps.setTimestamp(colIndex++, goutTS);
						ps.setTimestamp(colIndex++, goutTS);
						ps.setInt(colIndex++, req.getId());
						ps.setTimestamp(colIndex++, shiftDateTS);
						if (approach == Requirements.TripCreateStrategy.G_CHANGE_L_EXTEND) {
							ps.setInt(colIndex++, req.getFromId());
						}
						ps.setInt(colIndex++, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
						ps = conn.prepareStatement("insert into manual_trip_assigned (manual_trip_desired_id, trip_info_id,src) values (?,?,3)");
						ps.setInt(1,req.getId());
						ps.setInt(2, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
					}
					else if (approach == Requirements.TripCreateStrategy.G_EXTEND_TO_L || approach == Requirements.TripCreateStrategy.G_CHANGE_U_EXTEND) {
						String s1 = "update trip_info set load_gate_op=?, load_gate_in = ?, load_area_wait_in = ?, load_gate_out=?, load_area_wait_out=?, art=?";
						if (approach == Requirements.TripCreateStrategy.G_CHANGE_U_EXTEND)
							s1 += ", unload_gate_op = ? ";
						s1 += " where id=?";
						ps = conn.prepareStatement(s1);
						int op = req.getFromId();
						
						long lgoutToUgin = (long)(adjFactor * leadTime);
						lgoutToUgin = (long)(lgoutToUgin*(1-Math.random()*0.1));
						long uginToUgOut = (long)(Analysis.baseProcessTime*adjFactor);
						uginToUgOut = (long)(uginToUgOut*(1-Math.random()*0.1));

						int colIndex = 1;
						if (relevantTS <= 0)
							relevantTS =mappedTrip.getUgin();
						long gout = relevantTS-lgoutToUgin;
						Timestamp goutTS = Misc.longToSqlDate(gout);
						long gin =gout-uginToUgOut;
						Timestamp ginTS = Misc.longToSqlDate(gin);
						ps.setInt(colIndex++, op);
						ps.setTimestamp(colIndex++, ginTS);
						ps.setTimestamp(colIndex++, ginTS);
						ps.setTimestamp(colIndex++, goutTS);
						ps.setTimestamp(colIndex++, goutTS);
						ps.setInt(colIndex++, req.getId());
						if (approach == Requirements.TripCreateStrategy.G_CHANGE_U_EXTEND)
							ps.setInt(colIndex++, req.getToId());
						ps.setInt(colIndex++, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
						ps = conn.prepareStatement("insert into manual_trip_assigned (manual_trip_desired_id, trip_info_id,src) values (?,?,4)");
						ps.setInt(1,req.getId());
						ps.setInt(2, tripId);
						ps.execute();
						ps = Misc.closePS(ps);
					}
					else if (approach == Requirements.TripCreateStrategy.G_CREATE_FULL) {
						//double multFactor = Misc.getParamAsDouble(strategy.getAddnlParam(),1);
						double multFactor = 0.6;
						adjFactor = multFactor;
						double lead =  Misc.getParamAsDouble(strategy.getAddnlParam(),leadTime*multFactor);
						long lgoutToUgin = (long)(lead);
						lgoutToUgin = (long)(lgoutToUgin*(1-Math.random()*0.1));
						long uginToUgOut = (long)(Analysis.baseProcessTime*multFactor);
						uginToUgOut = (long)(uginToUgOut*(1-Math.random()*0.1));

						long lginToLgOut = (long)(Analysis.baseProcessTime*multFactor);
						lginToLgOut = (long)(lginToLgOut*(1-Math.random()*0.1));
						long ugin = relevantTS;
						long ugout = ugin+uginToUgOut;
						long lgout = ugin-lgoutToUgin;
						long lgin = lgout -lginToLgOut;
						long shiftDate = Requirements.getDate(ugin);
						Timestamp shiftDateTS = Misc.longToSqlDate(shiftDate);
						Timestamp uginTS = Misc.longToSqlDate(ugin);
						Timestamp ugoutTS = Misc.longToSqlDate(ugout);
						Timestamp lginTS = Misc.longToSqlDate(lgin);
						Timestamp lgoutTS = Misc.longToSqlDate(lgout);
						int lop = req.getFromId();
						int uop = req.getToId();
						
						ps = conn.prepareStatement("insert into trip_info(vehicle_id, load_gate_op, load_area_wait_in, load_gate_in, load_gate_out, load_area_wait_out, unload_gate_op, unload_area_wait_in, unload_gate_in, unload_gate_out, unload_area_wait_out,art,shift_date,shift) values "+
								"(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
						int colIndex = 1;
						ps.setInt(colIndex++, req.getVehicleId());
						ps.setInt(colIndex++, lop);
						ps.setTimestamp(colIndex++, lginTS);
						ps.setTimestamp(colIndex++, lginTS);
						ps.setTimestamp(colIndex++, lgoutTS);
						ps.setTimestamp(colIndex++, lgoutTS);
						ps.setInt(colIndex++, uop);
						ps.setTimestamp(colIndex++, uginTS);
						ps.setTimestamp(colIndex++, uginTS);
						ps.setTimestamp(colIndex++, ugoutTS);
						ps.setTimestamp(colIndex++, ugoutTS);
						
						ps.setInt(colIndex++, req.getId());
						ps.setTimestamp(colIndex++, shiftDateTS);
						ps.setInt(colIndex++, 0);
						ps.execute();
						ResultSet rs1 = ps.getGeneratedKeys();
						int newTripId = rs1.next() ? rs1.getInt(1) : Misc.getUndefInt();
						rs1 = Misc.closeRS(rs1);
						ps = Misc.closePS(ps);
						ps = conn.prepareStatement("insert into manual_trip_assigned (manual_trip_desired_id, trip_info_id,src) values (?,?,5)");
						ps.setInt(1,req.getId());
						ps.setInt(2, newTripId);
						ps.execute();
						ps = Misc.closePS(ps);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					ps = Misc.closePS(ps);
				}
			}//for each strategy
		}//for each req
	}

	
	public ArrayList<Triple<Pair<Integer, Requirements>, Integer, SimpleTripData>> getMasterBreakList() {
		return masterBreakList;
	}

	public void setMasterBreakList(
			ArrayList<Triple<Pair<Integer, Requirements>, Integer, SimpleTripData>> masterBreakList) {
		this.masterBreakList = masterBreakList;
	}

    private void createTripStrategyDVCFullUIn(Connection conn, boolean breakTrip, double leadFactor) {
		
		leadFactor = 1;
		int endExcl = 0;
		for (int is = dataList.size(); endExcl < is; ) {
			Triple<Integer, Integer, Integer> bounds = getBounds(endExcl);
			int vehicleId = bounds.first;
			int start = bounds.second;
			endExcl = bounds.third;
			if (start == endExcl)
				break;
			
			Requirements stReq = dataList.get(start);
			Requirements enReq = dataList.get(endExcl-1);
			long tsstart = stReq.getDate();//+this.minusWindowMin;
			long tsen = enReq.getDate();
			tsen += 24*3600*1000;//+this.plusWindowMin;
			
			FastList<SimpleTripData> tripData = SimpleTripData.getTrips(conn, vehicleId, tsstart-24*60*60*1000, tsen, opsOfInterest);
			coreCreateStrategyExact(conn, tripData, start, endExcl, null, vehicleId, leadFactor, masterBreakList);
		}//for all items in dataList
	}

    private void coreCreateStrategyExact(Connection conn, FastList<SimpleTripData> tripData, int start, int endExcl, ArrayList<HelperStrategy> stratHelper, int vehicleId, double leadFactor, ArrayList<Triple<Pair<Integer, Requirements>, Integer, SimpleTripData>> masterBreakList) {
		//masterBreakList = first vehicleId, 2nd = approach (1=L del, 2 u del, 3 = true break)
		//if null then not to remember break
		//now we have needs/av but still not op to op level ..
    	double origLeadFactor = leadFactor;
    	for (int i=start;i<endExcl;i++) {//j set to nextJ inside loop the index of the next Date
			Requirements req = dataList.get(i);
			if (!req.isValid())
					continue;
			boolean hasFullDate = req.hasFullDate();
			if (!hasFullDate)
				continue;
			ReferenceData refData = this.getReferenceData(conn, req.getFromId(), req.getToId(), leadFactor);
			if (refData != null && refData.getBaseFactor() < origLeadFactor)
				leadFactor = refData.getBaseFactor();
			else
				leadFactor = origLeadFactor;
			long leadTime = (long)(leadFactor * (double) (refData == null ? ( Analysis.maxLeadTime) : (refData.getUgin()-refData.getLgout())));
			if (leadTime > Analysis.maxLeadTime)
				leadTime = Analysis.maxLeadTime;
			int cnt = req.getCount();
			if (cnt == 0)
				continue;
			ArrayList<Integer> tripIds = req.getTripId();
			SimpleTripData extendMeToLOrU = null;
			if (tripIds.size() > 0) {
				SimpleTripData refTrip = SimpleTripData.getTripDataById(conn, req.getTripId().get(0));
				if (refTrip.getUgin() <= 0 || refTrip.getLgin() <= 0) {
					extendMeToLOrU = refTrip;
				}
				else {
					req.addStrategy(refTrip, Requirements.TripCreateStrategy.G_ALREADY_MAPPED, Misc.getUndefInt(), refData, null, null);
					continue;
				}

			}
			boolean doFixedLtoUExt = extendMeToLOrU != null && extendMeToLOrU.getUgin() <= 0;
			boolean doFixedUtoLExt = extendMeToLOrU != null && extendMeToLOrU.getLgout() <= 0;
			SimpleTripData lhsTrip = null;
			Pair<Integer, Boolean> index = tripData.indexOf(new SimpleTripData(req.getTs()));
			lhsTrip = tripData.get(index.first);
			boolean mappedElsewhere = lhsTrip != null && lhsTrip.getAssignedToReq() >= -1;
			boolean inRange = lhsTrip != null && !mappedElsewhere &&  lhsTrip.getUgin() > 0 && lhsTrip.getUgin() <= req.getTs() &&(lhsTrip.getUgout() > req.getTs() || lhsTrip.getUgout() <= 0);
			
			if (extendMeToLOrU != null && extendMeToLOrU.getUgin() > 0) {
				lhsTrip = extendMeToLOrU;
				inRange = true;
			}
			else if (extendMeToLOrU != null && extendMeToLOrU.getLgout() > 0) {
				lhsTrip = extendMeToLOrU;
				inRange = false;
			}
			if (!inRange && lhsTrip != null && lhsTrip.getUgin() > 0) {
				
					long gapOnRight = req.getTs() - (lhsTrip.getUgout() > 0 ? lhsTrip.getUgout() : lhsTrip.getUgin());
					if (gapOnRight > 0 && !mappedElsewhere && gapOnRight < 60*60*1000)
						inRange = true;
			}
			if (!inRange && extendMeToLOrU == null) {
				SimpleTripData rhsTrip = tripData.get(index.first+1);
				boolean rhsmappedElsewhere = rhsTrip != null && rhsTrip.getAssignedToReq() >= -1;
				if (rhsTrip != null && !rhsmappedElsewhere && rhsTrip.getUgin() > 0 && (rhsTrip.getUgin()-req.getTs() < 60*60*1000)) {
					inRange = true;
					lhsTrip = rhsTrip;
					index.first = index.first+1;
					mappedElsewhere = rhsmappedElsewhere;
				}
			}
			double minFactor = 0.6;
			
			long adjLeadTimeForLExt = (long)(minFactor*(double)leadTime);
			long adjProcessTime = (long)(minFactor*(double)Analysis.baseProcessTime);
			boolean isUsable = false;
			double adjFactor = 0.99;
			boolean simpleMapping = false;
			long genericOtherLeadTime = 40*60*1000;
			long genericOtherMaterialLeadTime = 5*60*1000;
			
			long adjGenericOtherLeadTime = (long)(minFactor*(double)genericOtherLeadTime);
			long adjGenericOtherMaterialLeadTime = 5*60*1000;
			long gapAfterProcessForUgout = 5*60*1000;
			if (!inRange) {
				//will need to create
				//or extend to U
				if (lhsTrip != null) {
					long gapAv = req.getTs() - lhsTrip.getLatest();
					long gapDesired = lhsTrip.getUgin() > 0 ?
							genericOtherLeadTime+Analysis.baseProcessTime+leadTime+Analysis.baseProcessTime-gapAfterProcessForUgout//goto load, get loaded, come to ugin
							:
								(lhsTrip.getMaterialId() == req.getMaterialId() ? genericOtherLeadTime : genericOtherMaterialLeadTime)+Analysis.baseProcessTime+genericOtherLeadTime+Analysis.baseProcessTime+leadTime-gapAfterProcessForUgout //goto unload, unload, come back, load, goto unload
							;
//					gapDesired += (10*60*1000); //for internal work
					long adjGapDesired = lhsTrip.getUgin() > 0 ?
							adjGenericOtherLeadTime+adjProcessTime+adjGenericOtherLeadTime//goto load, get loaded, come to ugin
							:
								(lhsTrip.getMaterialId() == req.getMaterialId() ? adjGenericOtherLeadTime : adjGenericOtherMaterialLeadTime)+adjProcessTime+adjGenericOtherLeadTime+adjProcessTime+adjLeadTimeForLExt //goto unload, unload, come back, load, goto unload
							;
					if (gapAv >= adjGapDesired || (extendMeToLOrU != null && gapAv > 0 && gapAv > 0.4*gapDesired))
						isUsable = true;
					if (gapAv < gapDesired)
						adjFactor = (double)gapAv/(double)gapDesired;
				}
				else {
					isUsable = true;
				}
				if (isUsable) { //check if rhs trip allows enough time to unload and get there
					SimpleTripData nextTrip = tripData.get(index.first+1);
					boolean useAdjProcessTimeForCreate = false;
					if (nextTrip != null) {
						long gapAv = nextTrip.getEarliest() - req.getTs()+gapAfterProcessForUgout;
						long gapDesired = nextTrip.getLgin() > 0 ?
									nextTrip.getMaterialId() == req.getMaterialId() ? adjGenericOtherLeadTime : adjGenericOtherMaterialLeadTime//unload, goto load
									:
									adjGenericOtherLeadTime+adjProcessTime+(nextTrip.getMaterialId() == req.getMaterialId() ? adjGenericOtherLeadTime : adjGenericOtherMaterialLeadTime)//unload, goto load, load, goto unload
									;
						if ((gapAv < gapDesired && extendMeToLOrU == null) || (gapAv < 0.4*gapDesired && extendMeToLOrU != null))
							isUsable = false;
					}
					if (isUsable) {
						useAdjProcessTimeForCreate = true;
						long ugout = req.getTs()+gapAfterProcessForUgout;
						long ugin = ugout-(long)(adjFactor * (double) Analysis.baseProcessTime);
						long lgout = req.getTs()-(long)(adjFactor*(double)leadTime);
						long lgin = lgout - (long)(adjFactor * (double) Analysis.baseProcessTime);
						if (extendMeToLOrU == null) {
						SimpleTripData dummyTrip = new SimpleTripData(1, req.getFromId(), lgin, lgout, req.getToId(),ugin, ugout, Misc.getUndefInt(), req.getId());
						tripData.add(dummyTrip);
						dummyTrip.setAssignedToReq(req.getId());
						}
						else {
							extendMeToLOrU.setUgin(ugin);
							extendMeToLOrU.setUgout(ugout);
							extendMeToLOrU.setAssignedToReq(req.getId());
						}
						
					}
				}
			}
			else {//lhsTrip's unload is mapped ... check if there is a load associate
				if (lhsTrip != null && !mappedElsewhere && lhsTrip.getLgin() > 0) {
					isUsable = true;
					simpleMapping = true;
				}
				else {
					SimpleTripData prevTrip = tripData.get(index.first-1);
					if (prevTrip != null) {
						long gapAv = lhsTrip.getEarliest() - prevTrip.getLatest();
						 
						
						long gapDesired = 
								
								prevTrip.getUgin() > 0 ?
								adjGenericOtherLeadTime+adjProcessTime+adjLeadTimeForLExt//goto load, load, come to unload
									: adjGenericOtherLeadTime+adjProcessTime+adjGenericOtherLeadTime+adjProcessTime+adjLeadTimeForLExt
									;
						if (gapAv >= gapDesired)
							isUsable = true;
						if (extendMeToLOrU != null && !isUsable && gapAv > (0.67*gapDesired))//0.67 = 0.4/0.6
							isUsable = true;
						long fullgapDesired =
							 prevTrip.getUgin() > 0 ?
									 genericOtherLeadTime+Analysis.baseProcessTime+leadTime//goto load, load, come to unload
											: genericOtherLeadTime+Analysis.baseProcessTime+genericOtherLeadTime+Analysis.baseProcessTime+leadTime
											;
						if (fullgapDesired > gapAv && isUsable)
							adjFactor = (double)gapAv/(double)fullgapDesired - 0.01;
						
					}
					else {
						isUsable = true;
					}
					if (isUsable) {
						lhsTrip.setLgout(lhsTrip.getUgin() - (long)(adjFactor*(double)leadTime));
						lhsTrip.setLgin(lhsTrip.getLgout() - (long)(adjFactor*(double)Analysis.baseProcessTime));
						lhsTrip.setAssignedToReq(req.getId());
					}
				}
			}	
			if (isUsable) {
				if (inRange) {
					if (simpleMapping) {
						lhsTrip.setAssignedToReq(req.getId());
						
						PreparedStatement ps = null;
						try {
							ps = conn.prepareStatement("insert into manual_trip_assigned (manual_trip_desired_id, trip_info_id) values (?,?)");
							ps.setInt(1,req.getId());
							ps.setInt(2, lhsTrip.getTripId());
							ps.execute();
							ps = Misc.closePS(ps);
							req.addStrategy(lhsTrip, Requirements.TripCreateStrategy.G_ALREADY_MAPPED, Misc.getUndefInt(), refData, null, null);
						}
						catch (Exception e) {
							req.addStrategy(lhsTrip, Requirements.TripCreateStrategy.G_TO_MAP, Misc.getUndefInt(), refData, null, null);
							e.printStackTrace();
						}
						finally {
							ps = Misc.closePS(ps);
						}
					}
					else {//extend U to L
						req.addStrategy(lhsTrip, Requirements.TripCreateStrategy.G_EXTEND_TO_L, lhsTrip.getUgin(), refData, null, Double.toString(adjFactor));
						lhsTrip.setAssignedToReq(req.getId());
					}
				}
				else {//create brand new
					if (extendMeToLOrU != null)
						req.addStrategy(lhsTrip, Requirements.TripCreateStrategy.G_EXTEND_TO_U, lhsTrip.getLgout(), refData, null, Double.toString(adjFactor));
					else
						req.addStrategy(null, Requirements.TripCreateStrategy.G_CREATE_FULL, req.getTs(), this.getReferenceData(conn, req.getFromId(), req.getToId(), leadFactor), null, Double.toString(adjFactor));
				}
			}
			else {//cant be mapped
				req.addStrategy(null, Requirements.TripCreateStrategy.G_UNKNOWN, Misc.getUndefInt(), this.getReferenceData(conn, req.getFromId(), req.getToId(), leadFactor), null, null);
			}
    	}//for all trips for vehicle
	}//end of func
}//end of class
