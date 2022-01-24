package com.ipssi.common.ds.trip;

import java.io.Serializable;
import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;

public class AddnlMiningInfo  implements Serializable {
	public static int SITEID_IN_ALLSITES = 0x1;
	public static int DESTID_IN_ALLSITES = 0x2;
	public static int SHOVEL_OVERLAP_ASSIGNEMENT = 0x4;
	private static final long serialVersionUID = 1L;
	private int siteId = Misc.getUndefInt();
	private int destId = Misc.getUndefInt();
	//siteId in AllSites, destId in AllSites, allShovels Overlaps ShovelsPerAssignment Only, allSovels Overlaps and site in AllSites
	public static int QUALITY_CODE_CALC_FLAG = 0x1;
	public static int SITE_MATCH_FLAG = 0x2;
	public static int DEST_MATCH_FLAG = 0x4;
	public static int SHOVEL_MATCH_FLAG = 0x8;
	public static int HAS_LOAD = 0x10;
	public static int HAS_UNLOAD = 0x20;
	
	private int matchQualityCode = Misc.getUndefInt();
	private int[] allShovelsVehicleId = null;
	private int[] allSites = null;
	private int[] shovelVehicleIdPerAssignment = null;
	void getMemoryUsageNested(StringBuilder sb) {
		sb.append(" allShovelsVehicleIdSz:").append(allShovelsVehicleId == null ? 0 : allShovelsVehicleId.length);
		sb.append(" allSitesSz:").append(allSites == null ? 0 : allSites.length);
		sb.append(" shovelVehicleIdPerAssignmentSz:").append(shovelVehicleIdPerAssignment == null ? 0 : shovelVehicleIdPerAssignment.length);
	}
	public int getScoreForComp(boolean isLoad) {
		calcMatchQualityIfNeeded();
		int retval = isLoad ?
					( 
							hasShovelMatch() ?  10: hasSiteMatch() ? 8 
							: this.allShovelsVehicleId != null && this.allShovelsVehicleId.length > 0 ? 6 : this.hasLoad() ? 4 : 0
					)
				:
					(
							hasDestMatch() ? 10  : hasUnload() ? 4 : 0
					)
					;
			return retval;
	}
	private void calcMatchQualityIfNeeded() {
		if ((matchQualityCode & QUALITY_CODE_CALC_FLAG) != 0)
			return;
		matchQualityCode |= QUALITY_CODE_CALC_FLAG;
		
		boolean siteInAllSite = this.isInList(allSites, siteId);
		boolean destInAllSite = this.isInList(allSites, destId);
		boolean shovelOverlap = false;
		for (int i=0,is = allShovelsVehicleId == null ? 0 : allShovelsVehicleId.length; i<is; i++) {
			if (isInList(allSites, allShovelsVehicleId[i])) {
				shovelOverlap = true;
				break;
			}
		}
		if (siteInAllSite) 
			matchQualityCode |= SITE_MATCH_FLAG;
		if (destInAllSite)
			matchQualityCode |= DEST_MATCH_FLAG;
		if (shovelOverlap)
			matchQualityCode |= SHOVEL_MATCH_FLAG;
	}
	public void setHasLoad() {
		matchQualityCode &= HAS_LOAD;
	}
	public void setHasUnload() {
		matchQualityCode &= HAS_UNLOAD;
	}
	public boolean hasSiteMatch() {
		this.calcMatchQualityIfNeeded();
		return (matchQualityCode & SITE_MATCH_FLAG) > 0; 
	}
	public boolean hasShovelMatch() {
		this.calcMatchQualityIfNeeded();
		return (matchQualityCode & SHOVEL_MATCH_FLAG) > 0; 
	}
	public boolean hasDestMatch() {
		this.calcMatchQualityIfNeeded();
		return (matchQualityCode & DEST_MATCH_FLAG) > 0; 
	}
	public boolean hasLoad() {
		return (matchQualityCode & HAS_LOAD) > 0; 
	}
	public boolean hasUnload() {
		return (matchQualityCode & HAS_UNLOAD) > 0; 
	}
	public static boolean isInList(int theList[], int v) {
		for (int i=0,is = theList == null ? 0 : theList.length; i<is;i++) {
			if (theList[i] == v)
				return true;
		}
		return false;
	}
	public AddnlMiningInfo() {
		
	}
	public AddnlMiningInfo(int src, int dest) {
		this.siteId = src;
		this.destId = dest;
	}
	public int getSiteId() {
		return siteId;
	}
	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}
	public int getDestId() {
		return destId;
	}
	public void setDestId(int destId) {
		this.destId = destId;
	}
	public int[] getAllShovelsVehicleId() {
		return allShovelsVehicleId;
	}
	public void setAllShovelsVehicleId(int[] allShovelsVehicleId) {
		this.allShovelsVehicleId = allShovelsVehicleId;
	}
	public void setAllShovelsVehicleId(ArrayList<Integer> list) {
		if (list == null || list.size() == 0)
			this.allShovelsVehicleId = null;
		else {
			this.allShovelsVehicleId = new int[list.size()];
			for (int i=0,is=list.size(); i<is; i++)
				this.allShovelsVehicleId[i] = list.get(i);
		}
	}
	public int[] getAllSites() {
		return allSites;
	}
	public void setAllSites(int[] allSites) {
		this.allSites = allSites;
	}
	public void setAllSites(ArrayList<Integer> list) {
		if (list == null || list.size() == 0)
			this.allSites = null;
		else {
			this.allSites = new int[list.size()];
			for (int i=0,is=list.size(); i<is; i++)
				this.allSites[i] = list.get(i);
		}
	}
	public int[] getShovelsPerAssignment() {
		return shovelVehicleIdPerAssignment;
	}
	public void setShovelsPerAssignment(int[] shovelsPerAssignment) {
		this.shovelVehicleIdPerAssignment = shovelsPerAssignment;
	}
	public void setShovelsPerAssignment(ArrayList<Integer> list) {
		if (list == null || list.size() == 0) {
			this.shovelVehicleIdPerAssignment = null;
		}
		else {
			this.shovelVehicleIdPerAssignment = new int[list.size()];
			for (int i=0,is=list.size(); i<is; i++)
				this.shovelVehicleIdPerAssignment[i] = list.get(i);
		}
	}
	public int getMatchQualityCode() {
		return matchQualityCode;
	}
	public void setMatchQualityCode(int matchQualityCode) {
		this.matchQualityCode = matchQualityCode;
	}
	public int[] getShovelVehicleIdPerAssignment() {
		return shovelVehicleIdPerAssignment;
	}
	public void setShovelVehicleIdPerAssignment(int[] shovelVehicleIdPerAssignment) {
		this.shovelVehicleIdPerAssignment = shovelVehicleIdPerAssignment;
	}
}
