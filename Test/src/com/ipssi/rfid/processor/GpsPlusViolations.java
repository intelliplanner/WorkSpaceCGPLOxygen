package com.ipssi.rfid.processor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import com.ipssi.gen.utils.Pair;
//usage: getGpsPlusViolations to get violations object and call relevant methods
public class GpsPlusViolations {

    public static GpsPlusViolations getGpsPlusViolatins(Connection conn, int vehicleId, int tprId, long currTime) throws Exception {
        return new GpsPlusViolations(vehicleId, tprId, currTime);
    }

    public Pair<ResultEnum, String> getMarkForQC(Connection conn) throws Exception {
        return new Pair<ResultEnum, String>(ResultEnum.GREEN, "No");
    }

    public Pair<ResultEnum, String> getDriverHours(Connection conn, int driverId) throws Exception {
        return new Pair<ResultEnum, String>(ResultEnum.GREEN, "N/A");
    }

    public Pair<ResultEnum, String> getSafetyViolations(Connection conn) throws Exception {
        return new Pair<ResultEnum, String>(ResultEnum.GREEN, "No");
    }

    public Pair<ResultEnum, String> getGpsQCViolationsSummary(Connection conn, double grossWt) throws Exception {
        return new Pair<ResultEnum, String>(ResultEnum.GREEN, "No");
    }

    public Pair<ResultEnum, ArrayList<String>> getGpsQCViolationsDetailed(Connection conn, double grossWt) throws Exception {
        ArrayList<String> retval = new ArrayList<String>();
        retval.add("None");
        return new Pair<ResultEnum, ArrayList<String>>(ResultEnum.GREEN, retval);
    }

    public Pair<ResultEnum, String> getGpsRepairNeeded(Connection conn) throws Exception {
        return new Pair<ResultEnum, String>(ResultEnum.GREEN, "No");
    }

    public Pair<ResultEnum, String> getGpsIsTracking(Connection conn) throws Exception {
        return new Pair<ResultEnum, String>(ResultEnum.GREEN, "Yes");
    }

    public Pair<ResultEnum, String> getTATDistance(Connection conn) throws Exception {
        return new Pair<ResultEnum, String>(ResultEnum.GREEN, "100 km/104 km");
    }

    public Pair<ResultEnum, String> getTATTiming(Connection conn) throws Exception {
        return new Pair<ResultEnum, String>(ResultEnum.GREEN, "2hr:35m/2hr:40m");
    }

    public Pair<ResultEnum, String> getViolationBlocked(Connection conn) throws Exception {
        return new Pair<ResultEnum, String>(ResultEnum.GREEN, "No");
    }

    private GpsPlusViolations(int vehicleId, int tprId, long currTime) {
    }
}
