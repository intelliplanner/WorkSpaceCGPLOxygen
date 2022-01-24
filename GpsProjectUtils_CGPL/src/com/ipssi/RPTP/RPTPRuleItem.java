package com.ipssi.RPTP;

import com.ipssi.gen.utils.Misc;

public class RPTPRuleItem {
	public static int LOAD_IN = 1;
	public static int LOAD_OUT = 2;
	public static int UNLOAD_IN = 3;
	public static int UNLOAD_OUT = 4;
	 int fromEventId = Misc.getUndefInt();
	 int afterEventId = Misc.getUndefInt();
	 int loadOpStationId = Misc.getUndefInt();
	 int UnloadOpStationId = Misc.getUndefInt();
	 int loadMaterialId = Misc.getUndefInt();
	 int ruleId = Misc.getUndefInt();
	 int durLoLimit = Misc.getUndefInt();
	 int durHiLimit = Misc.getUndefInt();
	 String eventStartName = null;
	 int heuristicStatus = Misc.getUndefInt();
	 int criticality = Misc.getUndefInt();
	 int plantField = Misc.getUndefInt();	 
}
