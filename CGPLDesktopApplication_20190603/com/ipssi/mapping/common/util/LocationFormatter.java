package com.ipssi.mapping.common.util;

import java.util.ArrayList;
import java.util.HashMap;

import com.ipssi.gen.utils.Misc;

public class LocationFormatter {

	public static HashMap<Integer, ArrayList<String>> namesToBeExcluded = null;

	public static void init() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("RAIGARH");
		namesToBeExcluded = new HashMap<Integer, ArrayList<String>>();
		namesToBeExcluded.put(6, list);
		
		ArrayList<String> list1 = new ArrayList<String>();
		list1.add("GAUTAMBUDH NAGAR,UTTAR PRADESH");
		list1.add("Sahibabad,UTTAR PRADESH");
		
		namesToBeExcluded.put(23, list1);
		
		ArrayList<String> list2 = new ArrayList<String>();
		list2.add("DHENKANAL,ORISSA");
		list2.add("ANUGUL,ORISSA");
		namesToBeExcluded.put(25, list2);
		
		ArrayList<String> list3 = new ArrayList<String>();
		list3.add("KORBA,CHHATTISGARH");
		namesToBeExcluded.put(24, list3);
		
	}

	public static String formatLocation(String location, int portNodeId) {
		if ( namesToBeExcluded == null){
			init();
			if ( namesToBeExcluded == null ){
				return location;
			}
		}
		String origLocation = location;
		try{
		ArrayList<String> list = namesToBeExcluded.get(portNodeId);
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				if (location.indexOf(list.get(i)) > 0) {
					if (location.indexOf("KM") > 0) {
						System.out.println(location.substring(location.indexOf(",") + 1, location.indexOf("KM")));
						double distance = Misc.getParamAsDouble(location.substring(location.indexOf(",") + 1, location.indexOf("KM")));
						if (distance < 2) {
							location = location.substring(0, location.indexOf(","));
						} else {
							location = location.substring(0, location.indexOf("KM") + 5);
						}
						break;
					} else if (location.indexOf(" on ") > 0) {
						location = location.substring(0, location.indexOf(" on "));
						break;
					} else if (location.indexOf(" in ") > 0) {
						location = location.substring(0, location.indexOf(" in "));
						break;
					}
				}
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
			location = origLocation; 
		}
		return location;
	}

}
