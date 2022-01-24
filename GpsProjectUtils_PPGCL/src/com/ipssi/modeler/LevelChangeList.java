package com.ipssi.modeler;

import java.sql.Connection;

//DEBUG13 import com.ipssi.cache.NewVehicleData;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Pair;
import com.ipssi.processor.utils.GpsData;

public class LevelChangeList extends FastList<LevelChangeEvent> {
	//below are not used at all currently
	int toEvaluteAtIntermediate = -1; //-1 ... means dont know prev state, 0 ... means no, 1 means yes
	int isStoppedAtIntegermediate = -1; //-1 .. don't know,  0 ... means No, 1 means Yes
	int toEvaluteAtEnd = -1; //-1 ... means dont know prev state, 0 ... means no, 1 means yes
	int isStoppedAtEnd = -1; //-1 .. don't know,  0 ... means No, 1 means Yes
	public static int g_checkAfterNPoints = 0;
	public int ptsSeenSinceLastCheck = 0;
	/*DEBUG13
	public  void save(Connection conn, Pair<Integer, Integer> dirtyIndicator, int vehicleId, int dimId, int ruleId, NewVehicleData dl, VehicleModelInfo vehicleInfo) throws Exception {
		if (dirtyIndicator.first == -1)
			return;
		ModelSpec spec = vehicleInfo.getModelSpec(conn, dimId);
		VehicleSpecific vehicleParam = vehicleInfo.getVehicleParam(conn, dimId);
		
		LevelChangeList eventList = vehicleInfo.getLevelChangeList(conn, dimId);
		for (int i=dirtyIndicator.first, is = dirtyIndicator.second;i<=is;i++) {
			LevelChangeEvent evt = get(i);
			if (evt != null && evt.isDirty()) {
				if (!evt.isValidPoint()) {
					GpsData temp = dl.get(conn, evt);
					GpsData endPt = evt.getEndPt();
					GpsData endPtFromData = temp;
					if (endPt != null)
					   endPtFromData = dl.get(conn, endPt);
					
					if (temp != null)
						evt = new LevelChangeEvent(evt.getEventId(), temp, evt.getAmtChange(), endPtFromData);
				}
				evt.saveEvent(conn, vehicleId, dimId, ruleId, spec, vehicleParam);
				evt.setIsDirty(false);
			}
		}
		
		//remove the delted ones
		for (int i=dirtyIndicator.second, is = dirtyIndicator.first;i>=is;i--) {
			LevelChangeEvent evt = get(i);
			
			if (evt != null && evt.getEventId() < 0)
				remove(i);		
		}
	}
	DEBUG13 */
	public LevelChangeList() {
		super();
	}

	public int getToEvaluteAtIntermediate() {
		return toEvaluteAtIntermediate;
	}

	public void setToEvaluteAtIntermediate(int toEvaluteAtIntermediate) {
		this.toEvaluteAtIntermediate = toEvaluteAtIntermediate;
	}

	public int getIsStoppedAtIntegermediate() {
		return isStoppedAtIntegermediate;
	}

	public void setIsStoppedAtIntegermediate(int isStoppedAtIntegermediate) {
		this.isStoppedAtIntegermediate = isStoppedAtIntegermediate;
	}

	public int getToEvaluteAtEnd() {
		return toEvaluteAtEnd;
	}

	public void setToEvaluteAtEnd(int toEvaluteAtEnd) {
		this.toEvaluteAtEnd = toEvaluteAtEnd;
	}

	public int getIsStoppedAtEnd() {
		return isStoppedAtEnd;
	}

	public void setIsStoppedAtEnd(int isStoppedAtEnd) {
		this.isStoppedAtEnd = isStoppedAtEnd;
	}
}
