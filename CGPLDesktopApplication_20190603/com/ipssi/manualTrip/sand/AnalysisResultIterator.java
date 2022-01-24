package com.ipssi.manualTrip.sand;

import java.sql.Connection;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;

public class AnalysisResultIterator {
	private Analysis controller;
	private int rowIdx = 0;
	private int tripIdx = -1;
	public AnalysisResultIterator(Analysis controller) {
		this.controller = controller;
	}
	public boolean next() {
		boolean retval = false;
		tripIdx++;
		Requirements row = null;
		for (;rowIdx < controller.getDataList().size(); rowIdx++) {
			row = controller.getDataList().get(rowIdx);
			if (!row.isValid()) {
				row = null;
				continue;
			}
			if (tripIdx >= row.getTripPlan().size()) {
				tripIdx = 0;
				row = null;
				continue;
			}
			break;
		}
		return row != null;
	}
	public UIData getData(Connection conn) {
		Requirements row = rowIdx >= controller.getDataList().size() ? null : controller.getDataList().get(rowIdx);
		Requirements.TripCreateStrategy strategy = row == null || tripIdx >= row.getTripPlan().size() || !row.isValid() ? null: row.getTripPlan().get(tripIdx);
		if (row == null)
			return null;
		int dataId = row.getId();
		String dataRequirements = row.getUserDataString();
		int strategyCode = strategy == null ? Misc.getUndefInt() : strategy.getStrategy();
		int mappedTripId = strategy == null || strategy.getBase() == null ? Misc.getUndefInt() : strategy.getBase().getTripId();//strategy == null || strategy.getReferenceData() == null? Misc.getUndefInt() : strategy.getReferenceData().getTripId();
		long relevantTimeStamp = strategy == null ? Misc.getUndefInt() : strategy.getTargetDate();
		Pair<Pair<Long, Long>, Pair<Long, Long>> timings = strategy == null ? new Pair<Pair<Long, Long>, Pair<Long, Long>>(new Pair<Long, Long>((long)Misc.getUndefInt(), (long)Misc.getUndefInt()), new Pair<Long, Long>((long)Misc.getUndefInt(), (long)Misc.getUndefInt())) 
				:strategy.getEstTimings();
		long lgin = timings.first.first;
		long lgout = timings.first.second;
		long ugin = timings.second.first;;
		long ugout =  timings.second.second;;
		String boundingTripInfo = strategy == null ? null : strategy.getRelevantBoundingTripInfo();
		int refTripId = strategy != null && strategy.getReferenceData() != null ? strategy.getReferenceData().getTripId() : Misc.getUndefInt();  
		String addnlParam = strategy.getAddnlParam();
		
		UIData result = new UIData(dataId, dataRequirements, strategyCode,
				mappedTripId, relevantTimeStamp, lgin, lgout,
				ugin, ugout, boundingTripInfo, refTripId, addnlParam);
		return result;
	}
	//Select, Data Id, Data Requirements, Strategy, Ref TripId, RefTimestamp, Est Lin, Est Lout, Est Uin, Est Uout, Bounding Trip
	public static class UIData {
		int dataId;
		String dataRequirements;
		int strategyCode;
		int mappedTripId;
		long relevantTimeStamp;
		long lgin;
		long lgout;
		long ugin;
		long ugout;
		String boundingTripInfo;
		int refTripId;
		String addnlParam = null;
		public UIData(int dataId, String dataRequirements, int strategyCode,
				int mappedTripId, long relevantTimeStamp, long lgin, long lgout,
				long ugin, long ugout, String boundingTripInfo, int refTripId, String addnlParam) {
			super();
			this.dataId = dataId;
			this.dataRequirements = dataRequirements;
			this.strategyCode = strategyCode;
			this.mappedTripId = mappedTripId;
			this.relevantTimeStamp = relevantTimeStamp;
			this.lgin = lgin;
			this.lgout = lgout;
			this.ugin = ugin;
			this.ugout = ugout;
			this.boundingTripInfo = boundingTripInfo;
			this.refTripId = refTripId;
			this.addnlParam = addnlParam;
		}

		public int getDataId() {
			return dataId;
		}
		public String getDataRequirements() {
			return dataRequirements;
		}
		public int getStrategyCode() {
			return strategyCode;
		}
		public int getMappedTripid() {
			return mappedTripId;
		}
		public long getRelevantTimeStamp() {
			return relevantTimeStamp;
		}
		public long getLgin() {
			return lgin;
		}
		public long getLgout() {
			return lgout;
		}
		public long getUgin() {
			return ugin;
		}
		public long getUgout() {
			return ugout;
		}
		public String getBoundingTripInfo() {
			return boundingTripInfo;
		}

		public int getRefTripId() {
			return refTripId;
		}

		public String getAddnlParam() {
			return addnlParam;
		}

		public void setAddnlParam(String addnlParam) {
			this.addnlParam = addnlParam;
		}
		
	}

}
