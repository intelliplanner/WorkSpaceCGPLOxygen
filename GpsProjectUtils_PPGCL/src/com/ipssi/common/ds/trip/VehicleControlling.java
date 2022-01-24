package com.ipssi.common.ds.trip;

import java.sql.Connection;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.StopDirControl;

public class VehicleControlling {
	private String opProfileMergeId = null;
	private StopDirControl stopDirControl = null;//once calculated it will not be changed
	private OpMapping opMapping = null;
	private int vehicleId = Misc.getUndefInt();
	public VehicleControlling(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public String getOpProfileMergeId() {
		return opProfileMergeId;
	}
	public void setOpProfileMergeId(String opProfileMergeId) {
		this.opProfileMergeId = opProfileMergeId;
	}
	
	public StopDirControl getStopDirControl(Connection conn, CacheTrack.VehicleSetup vehSetup) throws Exception {
		if (stopDirControl == null) {
			int port = vehSetup.m_ownerOrgId;
			int tripProam = vehSetup.m_tripParamProfileId;
			stopDirControl = NewProfileCache.addCalculatedStopDirControl(conn, tripProam, port);
		}
		return stopDirControl;
	}
	
	public void markStopDirControlDirty(Connection conn) throws Exception {
		stopDirControl = null;
	}
	
	public OpMapping getOpMapping() {
		return opMapping;
	}
	public void setOpMapping(OpMapping opMapping) {
		this.opMapping = opMapping;
	}
	
}
