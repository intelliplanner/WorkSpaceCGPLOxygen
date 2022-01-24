package com.ipssi.eta;

import java.io.Serializable;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.ipssi.eta.SrcDestInfo.AlertSetting;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;

public class NewETAforSrcDestItem implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	public static class WayPointETA implements Serializable {
		private static final long serialVersionUID = 1L;
		private double transitTimeFromSrc = Misc.getUndefDouble();
		private double lon;
		private double lat;
		public String toString() {
			return Misc.isUndef(transitTimeFromSrc) ? "N/A" : Double.toString(transitTimeFromSrc);
		}
		public WayPointETA(double transit, double lon, double lat) {
			transitTimeFromSrc = transit;
			this.lon = lon;
			this.lat = lat;
		}
		public double getTransitTimeFromSrc() {
			return transitTimeFromSrc;
		}
		public void setTransitTimeFromSrc(double transitTimeFromSrc) {
			this.transitTimeFromSrc = transitTimeFromSrc;
		}
		public double getLon() {
			return lon;
		}
		public void setLon(double lon) {
			this.lon = lon;
		}
		public double getLat() {
			return lat;
		}
		public void setLat(double lat) {
			this.lat = lat;
		}
	}
	public static class AlertDetails implements Serializable {
		private static final long serialVersionUID = 1L;
		private int flexId = Misc.getUndefInt();
		// if sending srcAlert or destAlert - then the KM*1000 at which specified
		
		// if for has reached intermediate or delayAlert of not reaching intermediateIndex then intermediateIndex
		//
		private long alertSentTime = Misc.getUndefInt();
		public String toString() {
			SimpleDateFormat indep = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return toString(indep);
		}
		public String toString(SimpleDateFormat indep) {
			StringBuilder sb = new StringBuilder();
			sb.append("[").append(flexId).append(",").append(alertSentTime <= 0 ? "" : indep.format(new java.util.Date(alertSentTime))).append("]");
			return sb.toString();
		}
		
		public static ArrayList<AlertDetails> getInitAlertDetails(ArrayList<Integer> flexIdList) {
			if (flexIdList == null || flexIdList.size() == 0)
				return null;
			ArrayList<AlertDetails> retval = new ArrayList<AlertDetails>();
			for (Integer iv: flexIdList) {
				retval.add(new AlertDetails(iv, Misc.getUndefInt()));
			}
			return retval;
		}
		public static ArrayList<AlertDetails> getInitAlertDetails(int lo, int hiExcl) {
			if (hiExcl <= lo)
				return null;
			ArrayList<AlertDetails> retval = new ArrayList<AlertDetails>();
			for (int iv=lo;iv<hiExcl;iv++) {
				retval.add(new AlertDetails(iv, Misc.getUndefInt()));
			}
			return retval;
		}
	
		public AlertDetails(int flexId, long alertSentTime) {
			super();
			this.flexId = flexId;
			this.alertSentTime = alertSentTime;
		}
		public int getFlexId() {
			return flexId;
		}
		public void setFlexId(int flexId) {
			this.flexId = flexId;
		}
		public long getAlertSentTime() {
			return alertSentTime;
		}
		public void setAlertSentTime(long alertSentTime) {
			this.alertSentTime = alertSentTime;
		}
		
		
	}
	private int srcDestId = Misc.getUndefInt();
	private double transitDist = Misc.getUndefDouble();
	private double transitTime = Misc.getUndefDouble();
	private double latestEstTransitTime = Misc.getUndefDouble();
	public double ourOverUserTransitEstMult = 1; //ourtransitTIme/UserTransitTime
	private ArrayList<WayPointETA> intermediateTransit = null;
	private ArrayList<NewETAEvent> etaEvents = null;
	private ArrayList<Triple<Integer, ArrayList<AlertDetails>, Integer>> alertDetails = null;//first = type of alert, 2nd=alerts sent
	//, 3rd=eventIdForWhichSent .. placeholder ... not really impletemented
	public String toString() {
		SimpleDateFormat indep = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return toString(indep);
	}
	public String toString(SimpleDateFormat indep) {
		StringBuilder sb = new StringBuilder();
		sb.append("[ETA:").append(srcDestId)
		.append(" Dist:").append(Misc.isUndef(transitDist) ? "N/A" : Double.toString(transitDist))
		.append(" Time:").append(Misc.isUndef(transitTime) ? "N/A" : Double.toString(transitTime))
		;
		sb.append("\n");
		sb.append("WP Transit:");
		for (int i=0,is=this.intermediateTransit == null ? 0 : this.intermediateTransit.size(); i<is; i++) {
			sb.append(i).append(":").append(intermediateTransit.get(i).toString());
		}
		sb.append("\n");
		sb.append("Events:");
		for (int i=0,is=this.etaEvents == null ? 0 : this.etaEvents.size(); i<is; i++) {
			sb.append(etaEvents.get(i).toString(indep));
		}
		sb.append("\n");
		sb.append("Alerts:");
		for (int i=0,is=this.alertDetails == null ? 0: this.alertDetails.size(); i<is;i++) {

			sb.append("[For:").append(SrcDestInfo.getNameForAlertTy(this.alertDetails.get(i).first)).append(":");
			ArrayList<AlertDetails> list = this.alertDetails.get(i).second;
			for (int j=0,js=list == null ? 0 : list.size(); j<js; j++) {
				if (j != 0)
					sb.append(",");
				sb.append(list.get(j).toString(indep));
			}
			sb.append("]");
		}
		sb.append("\nEndETA\n");
		return sb.toString();
	}
	public void resetAlertSent(int ty) {
		ArrayList<AlertDetails> sentItem = getAlertDetailsFor(ty);
		for (int i=0,is=sentItem == null ? 0 : sentItem.size(); i<is;i ++) {
			sentItem.get(i).setAlertSentTime(Misc.getUndefInt());
		}
	}
	
	public void resetAlertSent(int ty, int forFlex) {
		ArrayList<AlertDetails> sentItem = getAlertDetailsFor(ty);
		for (int i=0,is=sentItem == null ? 0 : sentItem.size(); i<is;i ++) {
			if (sentItem.get(i).getFlexId() == forFlex) {
				sentItem.get(i).setAlertSentTime(Misc.getUndefInt());
				break;
			}
		}
	}
	public ArrayList<AlertDetails> getAlertDetailsFor(int ty) {
		for (int i=0,is=alertDetails == null ? 0 : alertDetails.size(); i<is;i++) {
			if (alertDetails.get(i).first == ty)
				return alertDetails.get(i).second;
		}
		return null;
	}
	public int getEventIdFor(int ty) {
		for (int i=0,is=alertDetails == null ? 0 : alertDetails.size(); i<is;i++) {
			if (alertDetails.get(i).first == ty)
				return alertDetails.get(i).third;
		}
		return Misc.getUndefInt();
	}
	public Pair<ArrayList<AlertDetails>, Integer> getAlertDetailsAndEventIdFor(int ty) {
		for (int i=0,is=alertDetails == null ? 0 : alertDetails.size(); i<is;i++) {
			if (alertDetails.get(i).first == ty)
				return new Pair<ArrayList<AlertDetails>, Integer>(alertDetails.get(i).second, alertDetails.get(i).third);
		}
		return null;
	}
	public AlertDetails getAlertDetailsFor(int ty, int flexId) {
		ArrayList<AlertDetails> adlist= this.getAlertDetailsFor(ty);
		for (int i=0,is=adlist == null ? 0 : adlist.size(); i<is;i++) {
			if (adlist.get(i).getFlexId() == flexId)
				return adlist.get(i);
		}
		return null;
	}
	public void setAlertDetailsFor(int ty, ArrayList<AlertDetails> alertItems) {
		setAlertDetailsFor(ty, alertItems, Misc.getUndefInt());
	}
	public void setAlertDetailsFor(int ty, ArrayList<AlertDetails> alertItems, int eventId) {
		if (alertItems == null || alertItems.size() == 0)
			return;
		Triple<Integer, ArrayList<AlertDetails>, Integer> found = null;
		for (int i=0,is=alertDetails == null ? 0 : alertDetails.size(); i<is;i++) {
			if (alertDetails.get(i).first == ty) {
				found = alertDetails.get(i);
				break;
			}
		}
		
		if (found == null) {
			if (alertDetails == null)
				alertDetails = new ArrayList<Triple<Integer, ArrayList<AlertDetails>, Integer>>();

			found = new Triple<Integer, ArrayList<AlertDetails>, Integer>(ty, null, eventId);
			alertDetails.add(found);
		}
		found.second = alertItems;
		if (found.third != eventId && (ty == SrcDestInfo.ALERT_STOPPAGE_BACKW || ty == SrcDestInfo.ALERT_STOPPAGE_BACKW)) {
			for (int i=0,is=alertItems == null ? 0 : alertItems.size(); i<is;i++) {
				alertItems.get(i).setAlertSentTime(-1);
			}
		}
	}
	
	private double srcLon = Misc.getUndefDouble();
	private double srcLat = Misc.getUndefDouble();
	private double destLon = Misc.getUndefDouble();
	private double destLat = Misc.getUndefDouble();
	private boolean sdUsable = false;
	private boolean srcLonLatFromSD = false;
	private boolean destLonLatFromSD = false;
	
	public NewETAforSrcDestItem(int srcDestId) {
		this.srcDestId = srcDestId;
	}
	public WayPointETA getIntermediateTransitInfo(int index) {
		return this.intermediateTransit == null || index < 0 || index >= this.intermediateTransit.size() ? null : this.intermediateTransit.get(index);
	}
	public int getDirection() {//1 => forw, -1 back, 0 undetermined
		int retval = 0;
		int firstEntry = etaEvents != null && etaEvents.size() > 0 ? etaEvents.get(0).getOfIndex() : Misc.getUndefInt();
		int lastEntry= etaEvents != null && etaEvents.size() > 0 ? etaEvents.get(etaEvents.size()-1).getOfIndex() : Misc.getUndefInt();
		if (!Misc.isUndef(firstEntry)) {
			if (firstEntry == -1)
				retval = 1;
			else if (firstEntry == -2)
				retval = -1;
			else if (lastEntry == -2)
				retval = 1;
			else if (lastEntry == -1)
				retval = -1;
			else if (firstEntry == lastEntry) 
				retval = 0;
			else 
				retval = firstEntry > lastEntry ? -1 : 1;
		}
		return retval;
	}
	public boolean closeOutOthers(long outTime, int exclOfIndex) {
		boolean dirty = false;
		boolean notCheckOfIndex = Misc.isUndef(exclOfIndex);
		for (int i=0,is = etaEvents == null ? 0 : etaEvents.size(); i<is;i++) {
			int ofIndex = etaEvents.get(i).getOfIndex();
			if (etaEvents.get(i).getOutTime() <= 0 && etaEvents.get(i).getInTime() <= outTime && (notCheckOfIndex || exclOfIndex != ofIndex)) {
				etaEvents.get(i).setOutTime(outTime);
				dirty = true;
			}
		}
		return dirty;
	}
	
	public NewETAEvent getETAEvent(int ofIndex) {
		for (int i=etaEvents == null ? -1 : etaEvents.size()-1; i>=0;i--) {
			if (etaEvents.get(i).getOfIndex() == ofIndex)
				return etaEvents.get(i);
		}
		return null;
	}
	
	public NewETAEvent getLatestEvent() {
		return etaEvents == null || etaEvents.size() == 0 ? null : etaEvents.get(etaEvents.size()-1);
	}

	public boolean addETAEvent(int ofIndex, long inTime) {//if newlyAdded
		boolean dirty = false;
		if (etaEvents == null) {
			etaEvents = new ArrayList<NewETAEvent>();
		}
		NewETAEvent newEVT = new NewETAEvent(ofIndex, inTime, Misc.getUndefInt());
		int lastEqualOrSmaller = -1;

		for (int i=0,is = etaEvents.size(); i < is; i++) {
			NewETAEvent evt = etaEvents.get(i);
			long stTime = evt.getInTime();			
			if (stTime >= inTime)
				break;
			lastEqualOrSmaller = i;
		}
		NewETAEvent addAtEvent = lastEqualOrSmaller >= etaEvents.size() ? null : lastEqualOrSmaller < 0 ? null : etaEvents.get(lastEqualOrSmaller);
		boolean done = false;
		if (addAtEvent == null) {
			if (etaEvents.size() == 0)
				etaEvents.add(newEVT);
			else
				etaEvents.add(0, newEVT);
			dirty = true;
			lastEqualOrSmaller = 0;
		}
		else if (addAtEvent.getOfIndex() == ofIndex && addAtEvent.getInTime() <= inTime && (addAtEvent.getOutTime() <= 0 || addAtEvent.getOutTime() > inTime)) {
			done = true;
			//check if mergeAble with prev
			int minus1 = lastEqualOrSmaller - 1;
			NewETAEvent prev = minus1 < 0 || minus1 >= etaEvents.size() ? null : etaEvents.get(minus1);
			if (prev != null && prev.getOfIndex() == ofIndex && (prev.getOutTime() < 0 || prev.getOutTime() >= addAtEvent.getInTime())) {
				prev.setOutTime(addAtEvent.getOutTime());
				etaEvents.remove(lastEqualOrSmaller);
				lastEqualOrSmaller = minus1;
				dirty = true;
			}
		}
		else {
			if (addAtEvent.getOutTime() < 0 || addAtEvent.getOutTime() >= inTime) {
				addAtEvent.setOutTime(inTime);
				dirty = true;
			}
			if (lastEqualOrSmaller == etaEvents.size()-1)
				etaEvents.add(newEVT);
			else {
				etaEvents.add(lastEqualOrSmaller+1, newEVT);
			}
			dirty = true;
			lastEqualOrSmaller++;
		}
		return dirty;
	}
	
	public boolean migrateAlertEtcListToNew(Connection conn, NewETAforSrcDestItem otherItem, boolean otherForw, boolean meForw) {
		//return true if changed .. 
		//if otherItem != null ... then need to migrate from old to this (and assuming this has been setup properly).
		//if otherItem == null ... then need to migrate current to new because of possible SrcDestInfo changes
		boolean retval = false;
		SrcDestInfo oldInfo = otherItem == null ? null : SrcDestInfo.getSrcDestInfo(conn, otherItem.srcDestId);
		SrcDestInfo srcDestInfo = SrcDestInfo.getSrcDestInfo(conn, this.srcDestId);
		if (srcDestInfo == null) {
			System.out.println("ETA SCREWED - migrate gets Src null");
		}
		SrcDestHelper.DifferenceResult difference = getDifference(oldInfo != null ? oldInfo : srcDestInfo);
    	retval = helpMigrateEvents(otherItem, otherForw, this, meForw, difference) || retval;
		
		int ty = Misc.getUndefInt();
		ArrayList<SrcDestInfo.AlertSetting> fromList = null;
		ArrayList<NewETAforSrcDestItem.AlertDetails> oldList = null;
		ArrayList<NewETAforSrcDestItem.AlertDetails> origToList = null;
		ArrayList<NewETAforSrcDestItem.AlertDetails> toList = null;
		
		ty = SrcDestInfo.ALERT_NEARING_SRC_BACK;
		fromList = srcDestInfo.getAlertSettingCalc(conn,ty);
		toList = otherItem != null ? this.getAlertDetailsFor(ty)
			: NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetDistForSrcDestAlertAsMtr(fromList));
		if (toList == null)
			toList = NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetDistForSrcDestAlertAsMtr(fromList));
		oldList = otherItem != null ? otherItem.getAlertDetailsFor(ty) : this.getAlertDetailsFor(ty);
		retval = helpMergeOldNewAlertDetails(ty, otherItem, this, oldList, toList, difference) || retval;
		setAlertDetailsFor(ty, toList);
		
		ty = SrcDestInfo.ALERT_STOPPAGE_FORW;
		fromList = srcDestInfo.getAlertSettingCalc(conn,ty);
		Pair<ArrayList<AlertDetails>, Integer> toListEventId = otherItem != null && this.getAlertDetailsAndEventIdFor(ty) != null ? this.getAlertDetailsAndEventIdFor(ty)
			: new Pair<ArrayList<AlertDetails>, Integer> (NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetTimeMinForOtherAlert(fromList)), Misc.getUndefInt());
		if (toListEventId == null)
			toListEventId = new Pair<ArrayList<AlertDetails>, Integer> (NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetTimeMinForOtherAlert(fromList)), Misc.getUndefInt());

		Pair<ArrayList<AlertDetails>, Integer> oldListEventId = otherItem != null ? otherItem.getAlertDetailsAndEventIdFor(ty) : this.getAlertDetailsAndEventIdFor(ty);
		int oldstopEventId = oldListEventId == null ? Misc.getUndefInt() : oldListEventId.second;
		int tostopEventId = toListEventId == null ? Misc.getUndefInt() : toListEventId.second;
		if (Misc.isUndef(oldstopEventId) || Misc.isUndef(tostopEventId) || oldstopEventId != tostopEventId)
			toListEventId = new Pair<ArrayList<AlertDetails>, Integer> (NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetTimeMinForOtherAlert(fromList)), Misc.getUndefInt());
		
		toList = toListEventId == null ? null : toListEventId.first;
		retval = helpMergeOldNewAlertDetails(ty, otherItem, this, oldList, toList, difference) || retval;
		setAlertDetailsFor(ty, toList, oldstopEventId);
		
		ty = SrcDestInfo.ALERT_STOPPAGE_BACKW;
		fromList = srcDestInfo.getAlertSettingCalc(conn,ty);
		toListEventId = otherItem != null ? this.getAlertDetailsAndEventIdFor(ty)
			: new Pair<ArrayList<AlertDetails>, Integer> (NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetTimeMinForOtherAlert(fromList)), Misc.getUndefInt());
		if (toListEventId == null)
			toListEventId = new Pair<ArrayList<AlertDetails>, Integer> (NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetTimeMinForOtherAlert(fromList)), Misc.getUndefInt());

		oldListEventId = otherItem != null ? otherItem.getAlertDetailsAndEventIdFor(ty) : this.getAlertDetailsAndEventIdFor(ty);
		oldstopEventId = oldListEventId == null ? Misc.getUndefInt() : oldListEventId.second;
		tostopEventId = toListEventId == null ? Misc.getUndefInt() : toListEventId.second;
		if (Misc.isUndef(oldstopEventId) || Misc.isUndef(tostopEventId) || oldstopEventId != tostopEventId)
			toListEventId = new Pair<ArrayList<AlertDetails>, Integer> (NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetTimeMinForOtherAlert(fromList)), Misc.getUndefInt());
		toList = toListEventId == null ? null : toListEventId.first;
		retval = helpMergeOldNewAlertDetails(ty, otherItem, this, oldList, toList, difference) || retval;
		setAlertDetailsFor(ty, toList, oldstopEventId);
		
		ty = SrcDestInfo.ALERT_SRC;
		fromList = srcDestInfo.getAlertSettingCalc(conn,ty);
		toList = otherItem != null ? this.getAlertDetailsFor(ty)
			: NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetDistForSrcDestAlertAsMtr(fromList));
		if (toList == null)
			toList = NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetDistForSrcDestAlertAsMtr(fromList));
		oldList = otherItem != null ? otherItem.getAlertDetailsFor(ty) : this.getAlertDetailsFor(ty);
		retval = helpMergeOldNewAlertDetails(ty, otherItem, this, oldList, toList, difference) || retval;
		setAlertDetailsFor(ty, toList);
		
		ty = SrcDestInfo.ALERT_DEST;
		fromList = srcDestInfo.getAlertSettingCalc(conn,ty);
		toList = otherItem != null ? this.getAlertDetailsFor(ty)
			: NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetDistForSrcDestAlertAsMtr(fromList));
		if (toList == null)
			toList = NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetDistForSrcDestAlertAsMtr(fromList));
		oldList = otherItem != null ? otherItem.getAlertDetailsFor(ty) : this.getAlertDetailsFor(ty);
		retval = helpMergeOldNewAlertDetails(ty, otherItem, this, oldList, toList, difference) || retval;
		setAlertDetailsFor(ty, toList);

		ty = SrcDestInfo.ALERT_DELAY_INTERMEDIATE;
		fromList = srcDestInfo.getAlertSettingCalc(conn,ty);
		toList = otherItem != null ? this.getAlertDetailsFor(ty)
				:  NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(0, fromList == null || srcDestInfo.getWaypoints() == null ? 0 : srcDestInfo.getWaypoints().size());
		if (toList == null)
			toList = NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(0, fromList == null || srcDestInfo.getWaypoints() == null ? 0 : srcDestInfo.getWaypoints().size());
		oldList = otherItem != null ? otherItem.getAlertDetailsFor(ty) : this.getAlertDetailsFor(ty);
		retval = helpMergeOldNewAlertDetails(ty, otherItem, this, oldList, toList, difference) || retval;
		setAlertDetailsFor(ty, toList);

		ty = SrcDestInfo.ALERT_REACH_INTERMEDIATE;
		fromList = srcDestInfo.getAlertSettingCalc(conn,ty);
		toList = otherItem != null ? this.getAlertDetailsFor(ty)
				: NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(0, fromList == null || srcDestInfo.getWaypoints() == null ? 0 : srcDestInfo.getWaypoints().size());
		if (toList == null)
			toList = NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(0, fromList == null || srcDestInfo.getWaypoints() == null ? 0 : srcDestInfo.getWaypoints().size());
		oldList = otherItem != null ? otherItem.getAlertDetailsFor(ty) : this.getAlertDetailsFor(ty);
		retval = helpMergeOldNewAlertDetails(ty, otherItem, this, oldList, toList, difference) || retval;
		setAlertDetailsFor(ty, toList);

		ty = SrcDestInfo.ALERT_SKIPPED_INTERMEDIATE;
		fromList = srcDestInfo.getAlertSettingCalc(conn,ty);
		toList = otherItem != null ? this.getAlertDetailsFor(ty)
				: NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(0, fromList == null || srcDestInfo.getWaypoints() == null ? 0 : srcDestInfo.getWaypoints().size());
		if (toList == null)
			toList = NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(0, fromList == null || srcDestInfo.getWaypoints() == null ? 0 : srcDestInfo.getWaypoints().size());
		oldList = otherItem != null ? otherItem.getAlertDetailsFor(ty) : this.getAlertDetailsFor(ty);
		retval = helpMergeOldNewAlertDetails(ty, otherItem, this, oldList, toList, difference) || retval;
		setAlertDetailsFor(ty, toList);

		ty = SrcDestInfo.ALERT_NONREACH_DEST;
		fromList = srcDestInfo.getAlertSettingCalc(conn,ty);
		toList = otherItem != null ? this.getAlertDetailsFor(ty)
				: NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetTimeMinForOtherAlert(fromList));
		if (toList == null)
			toList = NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetTimeMinForOtherAlert(fromList));
		oldList = otherItem != null ? otherItem.getAlertDetailsFor(ty) : this.getAlertDetailsFor(ty);
		retval = helpMergeOldNewAlertDetails(ty, otherItem, this, oldList, toList, difference) || retval;
		setAlertDetailsFor(ty, toList);

		ty = SrcDestInfo.ALERT_DELAYED_SRC_EXIT;
		fromList = srcDestInfo.getAlertSettingCalc(conn,ty);
		toList = otherItem != null ? this.getAlertDetailsFor(ty)
				: NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetTimeMinForOtherAlert(fromList));
		if (toList == null)
			toList = NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetTimeMinForOtherAlert(fromList));
		oldList = otherItem != null ? otherItem.getAlertDetailsFor(ty) : this.getAlertDetailsFor(ty);
		retval = helpMergeOldNewAlertDetails(ty, otherItem, this, oldList, toList, difference) || retval;
		setAlertDetailsFor(ty, toList);

		ty = SrcDestInfo.ALERT_DELAYED_DEST_EXIT;
		fromList = srcDestInfo.getAlertSettingCalc(conn,ty);
		toList = otherItem != null ? this.getAlertDetailsFor(ty)
				: NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetTimeMinForOtherAlert(fromList));
		if (toList == null)
			toList = NewETAforSrcDestItem.AlertDetails.getInitAlertDetails(SrcDestInfo.helpGetTimeMinForOtherAlert(fromList));
		oldList = otherItem != null ? otherItem.getAlertDetailsFor(ty) : this.getAlertDetailsFor(ty);
		retval = helpMergeOldNewAlertDetails(ty, otherItem, this, oldList, toList, difference) || retval;
		setAlertDetailsFor(ty, toList);

		ty = SrcDestInfo.ALERT_DELAY_CONT;
		fromList = srcDestInfo.getAlertSettingCalc(conn,ty);
		toList = otherItem != null ? this.getAlertDetailsFor(ty)
				: fromList == null || fromList.size() ==0 ? null : new ArrayList<NewETAforSrcDestItem.AlertDetails>();
		if (toList == null && fromList != null && fromList.size() != 0)
			toList = new ArrayList<NewETAforSrcDestItem.AlertDetails>();
		if (toList != null ) {
			toList.add(new NewETAforSrcDestItem.AlertDetails(ty, Misc.getUndefInt()));
		}
		oldList = otherItem != null ? otherItem.getAlertDetailsFor(ty) : this.getAlertDetailsFor(ty);
		retval = helpMergeOldNewAlertDetails(ty, otherItem, this, oldList, toList, difference) || retval;
		setAlertDetailsFor(ty, toList);

		ty = SrcDestInfo.ALERT_ONEXIT_DEST;
		fromList = srcDestInfo.getAlertSettingCalc(conn,ty);
		toList = otherItem != null ? this.getAlertDetailsFor(ty)
				: fromList == null || fromList.size() ==0 ? null : new ArrayList<NewETAforSrcDestItem.AlertDetails>();
		if (toList == null && fromList != null && fromList.size() != 0)
			toList = new ArrayList<NewETAforSrcDestItem.AlertDetails>();
		if (toList != null)
			toList.add(new NewETAforSrcDestItem.AlertDetails(ty, Misc.getUndefInt()));
		oldList = otherItem != null ? otherItem.getAlertDetailsFor(ty) : this.getAlertDetailsFor(ty);
		retval = helpMergeOldNewAlertDetails(ty, otherItem, this, oldList, toList, difference) || retval;
		setAlertDetailsFor(ty, toList);

		return retval;
	}
	private static boolean helpMigrateEvents(NewETAforSrcDestItem oldItem, boolean oldForw, NewETAforSrcDestItem newItem, boolean newForw, SrcDestHelper.DifferenceResult difference) {
		//returns true if changed
		boolean retval = false;
		if (oldItem != null)
			return retval;
		ArrayList<NewETAEvent> eventList = newItem.getEtaEvents();
		//first src/dest
		for (int j=eventList == null ? -1 : eventList.size()-1;j>=0;j--) {
			NewETAEvent evt = eventList.get(j);
			if (difference.srcDiff && evt.getOfIndex() == (newForw ?-1 : -2)) {
				eventList.remove(j);
				retval = true;
			}
			if (difference.destDiff && evt.getOfIndex() == (newForw ? -2 : -1)) {
				eventList.remove(j);
				retval = true;
			}
		}
		//now intermediates ...
		for (int i=0,is=difference.oldToNewIntermediateMapping == null ? 0 : difference.oldToNewIntermediateMapping.size()
				; i<is; i++) {
			int newIdx = difference.oldToNewIntermediateMapping.get(i);
			//first change events
			if (newIdx == i)
				continue;
			for (int j=eventList == null ? -1 : eventList.size()-1;j>=0;j--) {
				NewETAEvent evt = eventList.get(j);
				if (evt.getOfIndex() == i) {
					if (newIdx < 0) {
						retval = true;
						eventList.remove(j);
					}
					else {
						retval = true;
						evt.setOfIndex(newIdx);
					}
				}
			}
		}//end updating intermediate index
		return retval;
	}
	private static boolean helpMergeOldNewAlertDetails(int ty, NewETAforSrcDestItem oldItem, NewETAforSrcDestItem newItem, ArrayList<NewETAforSrcDestItem.AlertDetails> oldList, ArrayList<NewETAforSrcDestItem.AlertDetails> newList, SrcDestHelper.DifferenceResult difference) {
		//return true if changed
		boolean retval = false;
		switch (ty) {
			
			case SrcDestInfo.ALERT_SRC: {
				if (difference.srcDiff)
					oldList = null;
				retval = helperMigrateForIncrFlex(oldList, newList);
				break;
			}
			case SrcDestInfo.ALERT_DELAYED_SRC_EXIT: {
				if (difference.srcDiff)
					oldList = null;
				retval = helperMigrateForIncrFlex(oldList, newList);
				break;
			}
			case SrcDestInfo.ALERT_DELAYED_DEST_EXIT: {
				if (difference.destDiff)
					oldList = null;
				retval = helperMigrateForIncrFlex(oldList, newList);
				break;
			}
			case SrcDestInfo.ALERT_NONREACH_DEST: {
				if (difference.destDiff)
					oldList = null;
				retval = helperMigrateForIncrFlex(oldList, newList);
				break;
			}
			case SrcDestInfo.ALERT_DEST: {
				if (difference.destDiff)
					oldList = null;
				double oldTransit = oldItem == null ? newItem.getTransitTime() : oldItem.getTransitTime();
				double newTransit = newItem.getTransitTime();
				if (!Misc.isUndef(newTransit) && !Misc.isUndef(oldTransit)
						&& (newTransit <= oldTransit+0.005))
					retval = helperMigrateForDecrFlex(oldList, newList);
				break;
			}
			case SrcDestInfo.ALERT_ONEXIT_DEST: {//if valid copy over ..
				if (difference.destDiff)
					oldList = null;
				retval = helperMigrateIthJth(oldList, newList,0,0);
				break;
			}
			case SrcDestInfo.ALERT_DELAY_INTERMEDIATE: {//
				if (oldList != null && newList != null && oldList.size() > 0 && newList.size() > 0) {
					for (int i=0,is=difference.oldToNewIntermediateMapping.size();i<is;i++) {
						int newIndex = difference.oldToNewIntermediateMapping.get(i);
						if (newIndex < 0)
							continue;
						WayPointETA oldwpETA = oldItem == null ? newItem.getIntermediateTransitInfo(i) : oldItem.getIntermediateTransitInfo(i);
						WayPointETA newwpETA = newItem.getIntermediateTransitInfo(newIndex);
						if (!Misc.isUndef(newwpETA.getTransitTimeFromSrc()) && !Misc.isUndef(oldwpETA.getTransitTimeFromSrc())
								&& (newwpETA.getTransitTimeFromSrc() <= oldwpETA.getTransitTimeFromSrc()+0.005))
							retval = helperMigrateIthJth(oldList, newList,i,newIndex);
					}
				}
				break;
			}
			case SrcDestInfo.ALERT_REACH_INTERMEDIATE:
			case SrcDestInfo.ALERT_SKIPPED_INTERMEDIATE: {
				for (int i=0,is=difference.oldToNewIntermediateMapping.size();i<is;i++) {
					int newIndex = difference.oldToNewIntermediateMapping.get(i);
					retval = helperMigrateIthJth(oldList, newList,i,newIndex);
				}
				break;
			}
			case SrcDestInfo.ALERT_DELAY_CONT: {//if valid copy over ..
				if (difference.srcDiff)
					oldList = null;
				if (difference.destDiff)
					newList = null;
				retval = helperMigrateIthJth(oldList, newList,0,0);
				break;
			}
			case SrcDestInfo.ALERT_NEARING_SRC_BACK: {
				if (difference.srcDiff)
					oldList = null;
				retval = helperMigrateForDecrFlex(oldList, newList);
				break;
			}
			case SrcDestInfo.ALERT_STOPPAGE_FORW : 
			case SrcDestInfo.ALERT_STOPPAGE_BACKW : {
				retval = helperMigrateForIncrFlex(oldList, newList);
				break;
			}
		}//end of switch
		return retval;
	}		

	private static boolean helperMigrateIthJth(ArrayList<NewETAforSrcDestItem.AlertDetails> oldList, ArrayList<NewETAforSrcDestItem.AlertDetails> newList, int indexInOld, int indexInNew) {
		NewETAforSrcDestItem.AlertDetails entry =oldList == null ||  indexInOld < 0 || indexInOld >= oldList.size() ? null :oldList.get(indexInOld);
		NewETAforSrcDestItem.AlertDetails newEntry = newList == null || indexInNew < 0 || indexInNew >= newList.size() ? null : newList.get(indexInNew);
		if (entry == null || newEntry == null || newEntry.getAlertSentTime() > 0 || entry.getAlertSentTime() <= 0)
			return false;
		newEntry.setAlertSentTime(entry.getAlertSentTime());
		return true;
	}
	
	private static boolean helperMigrateForIncrFlex(ArrayList<NewETAforSrcDestItem.AlertDetails> oldList, ArrayList<NewETAforSrcDestItem.AlertDetails> newList) {
		boolean retval = false;
		if (newList == null || newList.size() == 0 || oldList == null || oldList.size() == 0)
			return retval;
		NewETAforSrcDestItem.AlertDetails entry = null;
		for (int j=oldList.size()-1; j >= 0; j--) {
			if (oldList.get(j).getAlertSentTime() > 0) {
				entry = oldList.get(j);
				break;
			}
		}
		
		if (entry != null) {
			for (int j=newList.size()-1; j >= 0; j--) {
				NewETAforSrcDestItem.AlertDetails newEntry = newList.get(j);
				if (newEntry.getAlertSentTime() > 0)
					break;
				if (newEntry.getFlexId() <= entry.getFlexId()) {
					newEntry.setAlertSentTime(entry.getAlertSentTime());
					retval = true;
					break;
				}
			}	//check in new 
		}//old found
		return retval;
	}
	private static boolean helperSetAlertSentToUnsent(ArrayList<NewETAforSrcDestItem.AlertDetails> theList) {
		boolean retval = false;
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is;i++) {
			if (theList.get(i).getAlertSentTime() > 0) {
				theList.get(i).setAlertSentTime(-1);
				retval = true;
			}
		}
		return retval;
	}
	
	private static boolean helperMigrateForDecrFlex(ArrayList<NewETAforSrcDestItem.AlertDetails> oldList, ArrayList<NewETAforSrcDestItem.AlertDetails> newList)  {
		boolean retval = false;
		if (newList == null || newList.size() == 0 || oldList == null || oldList.size() == 0)
			return retval;
		NewETAforSrcDestItem.AlertDetails entry = null;
		for (int j=0,js=oldList.size(); j <js; j++) {
			if (oldList.get(j).getAlertSentTime() > 0) {
				entry = oldList.get(j);
				break;
			}
		}
		if (entry != null) {
			for (int j=0,js=newList.size(); j < js; j++) {
				NewETAforSrcDestItem.AlertDetails newEntry = newList.get(j);
				if (newEntry.getAlertSentTime() > 0)
					break;
				if (newEntry.getFlexId() >= entry.getFlexId()) {
					newEntry.setAlertSentTime(entry.getAlertSentTime());
					retval = true;
					break;
				}
			}	
		}
		return retval;
	}
	
	private SrcDestHelper.DifferenceResult getDifference(SrcDestInfo srcDestInfo) {
		SrcDestHelper.DifferenceResult difference = new SrcDestHelper.DifferenceResult();
		difference.srcDiff = this.srcLonLatFromSD && (!Misc.isEqual(srcDestInfo.getSrcLong(), this.srcLon) || !Misc.isEqual(srcDestInfo.getSrcLat(), this.srcLat));
		difference.destDiff = this.destLonLatFromSD && (!Misc.isEqual(srcDestInfo.getDestLong(), this.destLon) || !Misc.isEqual(srcDestInfo.getDestLat(), this.destLat));
		boolean intermediateDiff = false;
		ArrayList<SrcDestInfo.WayPoint> wplist = srcDestInfo.getWaypoints();
		ArrayList<NewETAforSrcDestItem.WayPointETA> wpetalist = this.intermediateTransit;
		difference.oldToNewIntermediateMapping = new ArrayList<Integer>();
		for (int i=0,is=wpetalist == null ? 0 : wpetalist.size(); i<is; i++) {
			int newIdx = -1;
			NewETAforSrcDestItem.WayPointETA wpeta = wpetalist.get(i);
			for (int j=0,js=wplist == null ? 0 : wplist.size(); j<js; j++) {
				SrcDestInfo.WayPoint wp = wplist.get(j);
				if (Misc.isEqual(wp.getLongitude(), wpeta.getLon()) && Misc.isEqual(wp.getLatitude(), wpeta.getLat())) {
					newIdx = j;
					break;
				}
			}
			difference.oldToNewIntermediateMapping.add(newIdx);
			if (newIdx == -1)
				difference.isSame = false;
		}
		if (difference.isSame) {
			if (
					(wplist != null && (wpetalist == null || wpetalist.size() != wplist.size()))
					|| (wpetalist != null && (wplist == null || wpetalist.size() != wplist.size()))
				)
				difference.isSame = false;
		}
		return difference;
	}

	public int getSrcDestId() {
		return srcDestId;
	}

	public void setSrcDestId(int srcDestId) {
		this.srcDestId = srcDestId;
	}

	public ArrayList<NewETAEvent> getEtaEvents() {
		return etaEvents;
	}
	public int compareTo(Object o) {
		NewETAforSrcDestItem rhs = (NewETAforSrcDestItem) o;
		return srcDestId - rhs.srcDestId;
	}
	public double getTransitDist() {
		return transitDist;
	}
	public void setTransitDist(double transitDist) {
		this.transitDist = transitDist;
	}
	public double getTransitTime() {
		return transitTime;
	}
	public void setTransitTime(double transitTime) {
		this.transitTime = transitTime;
	}
	public boolean isSdUsable() {
		return sdUsable;
	}
	public void setSdUsable(boolean sdUsable) {
		this.sdUsable = sdUsable;
	}
	public ArrayList<WayPointETA> getIntermediateTransit() {
		return intermediateTransit;
	}
	public void setIntermediateTransit(ArrayList<WayPointETA> intermediateTransit) {
		this.intermediateTransit = intermediateTransit;
	}
	public void setEtaEvents(ArrayList<NewETAEvent> etaEvents) {
		this.etaEvents = etaEvents;
	}
	public double getSrcLon() {
		return srcLon;
	}
	public void setSrcLon(double srcLon) {
		this.srcLon = srcLon;
	}
	public double getSrcLat() {
		return srcLat;
	}
	public void setSrcLat(double srcLat) {
		this.srcLat = srcLat;
	}
	public double getDestLon() {
		return destLon;
	}
	public void setDestLon(double destLon) {
		this.destLon = destLon;
	}
	public double getDestLat() {
		return destLat;
	}
	public void setDestLat(double destLat) {
		this.destLat = destLat;
	}
	
	public ArrayList<Triple<Integer, ArrayList<AlertDetails>, Integer>> getAlertDetails() {
		return alertDetails;
	}
	public void setAlertDetails(
			ArrayList<Triple<Integer, ArrayList<AlertDetails>, Integer>> alertDetails) {
		this.alertDetails = alertDetails;
	}
	public double getLatestEstTransitTime() {
		return latestEstTransitTime;
	}
	public void setLatestEstTransitTime(double latestEstTransitTime) {
		this.latestEstTransitTime = latestEstTransitTime;
	}
	public boolean isSrcLonLatFromSD() {
		return srcLonLatFromSD;
	}
	public void setSrcLonLatFromSD(boolean srcLonLatFromSD) {
		this.srcLonLatFromSD = srcLonLatFromSD;
	}
	public boolean isDestLonLatFromSD() {
		return destLonLatFromSD;
	}
	public void setDestLonLatFromSD(boolean destLonLatFromSD) {
		this.destLonLatFromSD = destLonLatFromSD;
	}
	public static void main(String[] args) throws Exception {
		Tester.callMain(args);
	}
}
