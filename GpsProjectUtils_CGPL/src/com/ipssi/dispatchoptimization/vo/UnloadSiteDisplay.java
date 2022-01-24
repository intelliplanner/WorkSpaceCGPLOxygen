package com.ipssi.dispatchoptimization.vo;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;

public class UnloadSiteDisplay {
	private SiteVO site = null;

	public String getShovelName() {
		return finData[0];
	}
	public String getIconName() {
		//green - if working, yellow - if there is non crit alert, red if not working, gray if data delayed
		return "icon_unload.png";		
	}
	public String getLeftHoverText() {
		StringBuilder sb = new StringBuilder();
		sb.append("Site Name");
		sb.append("<hr noshade size=\"1\" width=\"100%\">");
		sb.append("# Vehicles Assigned / # of Vehicles inside / Recent Avg Turnaround time(s)");
		sb.append("<hr noshade size=\"1\" width=\"100%\">");
		sb.append("Recent Tonnes/Hr recvd / Shift Tonnage Desp");
		return sb.toString();
		//line 2:# Vehicles Assigned, # waits, Avg cycle time 
		//line 3:# Waits in future, recent rate of tonnes/Hr, cumm tonnes in Shift [LATER]
	}
	public String getRightHoverText() {
		StringBuilder sb = new StringBuilder();
		ArrayList<Integer> dumpersAssigned = null;
		try {
			for (int i=0,is=dumpersAssigned==null?0:dumpersAssigned.size(); i<is; i++) {
				if (i != 0) {
					sb.append("<hr noshade size=\"1\" width=\"100%\">");
				}
				DumperInfoVo dumperInfo =(DumperInfoVo) OperatorDashboardMU.getVehicleInfo(dumpersAssigned.get(i));
				sb.append(dumperInfo.getHoverText(null, 1));
			}
		}
		catch (Exception e) {
			
		}
		return sb.toString();
		
	}
	public String getIconHoverText() {// shovels hover info
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_TIME_ONLY_FORMAT);
		sb.append(siteName);
		
		
		sb.append("# Veh Assigned   :").append(Misc.printInt(this.vehiclesAssigned, "N/A")).append("<br/>");
		sb.append("Curr QLen            :").append(Misc.printInt(this.currWait,"N/A")).append("<br/>");
		sb.append("Cycle time(s)       :").append(Misc.printInt(this.avgCycleTime, "N/A")).append("<br/>");
		sb.append("Curr Recv (T/hr)  :").append(Misc.printDouble(this.tonnesPerHour, "N/A", 1)).append("<br/>");
		sb.append("Curr Recv (#/hr)  :").append(Misc.printDouble(this.loadsPerHour, "N/A", 1)).append("<br/>");
		sb.append("Shift Tonne           :").append(Misc.printDouble(this.totTonnesInShift, "N/A", 1)).append("<br/>");
		sb.append("Last Exit at         :").append(Misc.printDate(sdf, this.latestDispAt, "N/A")).append("<br/>");
		
		
		return sb.toString();
	}
	
	
	public String getBlinkRate() {// blink rate else boolean doBlink
		return  "0";
	}
	public String getFirstLineStatistics() {// SiteName [PitName]
		return finData[1];
	}
	public String getSecondLineStatistics() {// Second line Statistics
		return finData[2];
	}
	public String getThirdLineStatistics() {// Third line Statistics, if any
		return finData[3];
	}
	private String[] finData = null;
	private String siteName = null;
	private int vehiclesAssigned = 0;
	private int currWait = 0;
	private int avgCycleTime;
	private double loadsPerHour;
	private double tonnesPerHour;
	private int totTripsInShift;
	private double totTonnesInShift;
	private long latestDispAt;
	private boolean notWorking = false;
	public String helpCalcFinDataLine2() {
//		sb.append("# Vehicles Assigned / # of Vehicles inside / Recent Avg Turnaround time(s)");
//		sb.append("<hr noshade size=\"1\" width=\"100%\">");
//		sb.append("Recent Tonnes/Hr recvd / Shift Tonnage Desp");
		StringBuilder sb = new StringBuilder();
		sb.append(Misc.printInt(this.vehiclesAssigned, "N/A")).append(" / ");
		sb.append(Misc.printInt(this.currWait, "N/A")).append(" / ");
		sb.append(Misc.printInt(this.avgCycleTime, "N/A")).append("s ");
	    
	    return sb.toString();
	}
	public String helpCalcFinDataLine3() {
//		sb.append("# Vehicles Assigned / # of Vehicles inside / Recent Avg Turnaround time(s)");
//		sb.append("<hr noshade size=\"1\" width=\"100%\">");
//		sb.append("Recent Tonnes/Hr recvd / Shift Tonnage Desp");
		StringBuilder sb = new StringBuilder();
		sb.append(Misc.printDouble(this.tonnesPerHour, "N/A",1)).append(" / ");
		sb.append(Misc.printDouble(this.totTonnesInShift, "N/A",1));
		return sb.toString();
	}
	public UnloadSiteDisplay(SiteVO site, Connection conn, long now) throws Exception {
		this.site = site;
//		calc(conn, now);
		setupDisp();
		
		this.site = site;
		this.siteName = site.getName();
//		this.currWait = OPDashHelper.getDimValue(conn, shovel.getId(), 76107).getIntVal();
//		this.qlenWhenMeLeavesAndComesBack =OPDashHelper.getDimValue(conn, shovel.getId(), 76108).getIntVal();
//		this.avgCycleTime=OPDashHelper.getDimValue(conn, shovel.getId(), 82535).getIntVal();
//		this.loadsPerHour=OPDashHelper.getDimValue(conn, shovel.getId(), 83137).getIntVal();
//		this.tonnesPerHour=OPDashHelper.getDimValue(conn, shovel.getId(), 82531).getIntVal();
//		this.totTripsInShift=OPDashHelper.getDimValue(conn, shovel.getId(), 83137).getIntVal();
//		this.totTonnesInShift=OPDashHelper.getDimValue(conn, shovel.getId(), 76109).getDoubleVal();
//		this.fuellingNeededAt=OPDashHelper.getDimValue(conn, shovel.getId(), 76110).getIntVal();
//		this.latestDispAt=OPDashHelper.getDimValue(conn, shovel.getId(), 76111).getIntVal();
//		this.normEvent =OPDashHelper.getDimValue(conn, shovel.getId(), 76112).getIntVal()==1?true:false;
//		this.critEvent =OPDashHelper.getDimValue(conn, shovel.getId(), 76113).getIntVal()==1?true:false;
//		this.notWorking=OPDashHelper.getDimValue(conn, shovel.getId(), 76114).getIntVal()==1?true:false;
//		this.latestDataAt =OPDashHelper.getDimValue(conn, shovel.getId(), 76115).getIntVal();
//		this.latestPosAt =OPDashHelper.getDimValue(conn, shovel.getId(), 76116).getStringVal();
//		this.showOpt =OPDashHelper.getDimValue(conn, shovel.getId(), 82535).getIntVal()==1?true:false;
		
	}
	private void setupDisp() {
		finData = new String[4];
		finData[0] = null;
		finData[1] = null;
		finData[2] = null;
		finData[3] = null;
		int idxUsed = 0;
		if (siteName != null && siteName.length() > 0) {
			finData[idxUsed++] = siteName;
		}
		finData[idxUsed++] = helpCalcFinDataLine2();
		finData[idxUsed++] = helpCalcFinDataLine3();
	}
	
}
