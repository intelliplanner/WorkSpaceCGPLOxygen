package com.ipssi.userNameUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ipssi.gen.utils.*;


public class Utils {
	private static ConcurrentHashMap<String, String> g_nameDictionary = new ConcurrentHashMap<String, String>();
	private static long g_lastLoadDict = -1;
	public static IdInfo getIdInfo(TextInfo textInfo, int portNodeIdOfChallan, int cdhId, boolean isDest, Connection conn, StopDirControl stopDirControl) {
		if (textInfo == null || textInfo.isNull())
			return new IdInfo();
		ArrayList<Integer> portNodes = new ArrayList<Integer> ();
		portNodes.add(portNodeIdOfChallan);
		ArrayList<Integer> opType = new ArrayList<Integer> ();
		if (isDest)
			opType.add(2);
		else
			opType.add(1);
		opType.add(11);
		opType.add(15);
		opType.add(16);
		opType.add(17);
		opType.add(24);
		
		IdInfo retval = Utils.getIdInfo(cdhId, textInfo, portNodes, opType, null, true, conn, isDest, stopDirControl);
		
		return retval;
	}
		
	private static IdInfo getIdInfo(int cdhId, TextInfo textInfo, ArrayList<Integer> portNodes, ArrayList<Integer> opTypes, ArrayList<Integer> lmTypes, boolean toSave, Connection conn, boolean isDest, StopDirControl stopDirControl) {
		if (stopDirControl != null && ( (isDest && !stopDirControl.isLookupChallanDestAddress()) || ( !isDest && !stopDirControl.isLookupChallanSrcAddress()) ))
			return null;
		IdInfo retval = null;
		int retvalId = Misc.getUndefInt();
		try {
			//1. if textInfo itemCode is vald then check in challan_dest_helper for portnodes else do an exact match check
			//2. Check of op_station of type of load/unload and hybrids  available for portNodes have name matches - if so use that
			//3. Check if landmarks available for portNodes have name matches
				//2/3.5. what is name match  - mostUseful part of name like match and then the longest name match
			//4. Else look in challan_dest_helper for any partial matches and choose the best quality
			//5. Else look in shapefile_poins for any partial matches
			//6. Else look in landmarks for any partial matches
			
			String addressItemCode = textInfo.getAddressItemCode();
			IdInfo infoFromExactMatch = null;
			if (cdhId > 0) {
				infoFromExactMatch = retval = getIdInfoById(cdhId, conn );
			}
			else if (addressItemCode != null && addressItemCode.length() > 0) {
				infoFromExactMatch = retval = getIdInfoByAddressItemCode(addressItemCode, conn, isDest);
			} 
			else {
				retval = infoFromExactMatch = getIdInfoByExactMatch(textInfo, conn, isDest);
			}//check if there is an exact match on all field
			
			if (retval == null || retval.getDestId() <= 0) {
				//look it up
				
				//rajeev 20140608 retval = Utils.getIdInfoByCustLookup(conn, textInfo, portNodes, opTypes, lmTypes);
				if (retval == null && Utils.doingOrient(conn, portNodes)) {
					retval = Utils.getIdInfoByOpMatchOrient(conn, textInfo);
				}
				
				
				if (retval == null)
						retval = Utils.getIdInfoByPartialMatch(conn, textInfo, portNodes, isDest, Utils.doingOrient(conn, portNodes));
				if (retval != null && infoFromExactMatch != null)
					retval.setId(infoFromExactMatch.getId());
				if (retval != null && toSave && retval.getDestId() >= 0 && ( (stopDirControl == null || isDest && stopDirControl.isInsertChallanDestAddress()) || (!isDest && stopDirControl.isInsertChallanSrcAddress()) )) {
					
					Utils.saveCDHInfo(conn, textInfo, retval, portNodes.get(0), isDest);
				}
			}
			//else need to guess by various heuristics
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		populateAlertInfoToBeRemoved(conn,retval); //instead will use CDHEmailInfo
		return retval;
	}
	
	private static HashMap<String, String> stateTo2digit = new HashMap<String, String>();
	private static HashMap<String, String> stateTo2digitro2digit = new HashMap<String, String>();
	public static String get2digitStateCode(String name) {
		String retval = stateTo2digit.get(name);
		if (retval == null)
			retval = stateTo2digitro2digit.get(name);
		return retval;
	}
	
	static HashMap<String, String> ignoreWord = new HashMap<String, String> ();
	static {
		init();
	}
	public static void init() {
		ignoreWord.put("LAFARGE", "LAFARGE");
		ignoreWord.put("DUMP", "DUMP");
//		ignoreWord.put("CFA", "CFA");
//		ignoreWord.put("CFAA", "CFAA");
		ignoreWord.put("DEALER", "DEALER");
		ignoreWord.put("TRANSPORT", "TRANSPORT");
		ignoreWord.put("LTD", "LTD");
		ignoreWord.put("LIMITED", "LIMITED");
		ignoreWord.put("LTD.", "LTD.");
		ignoreWord.put("DEALER", "DEALER");
		ignoreWord.put("THE", "THE");
		ignoreWord.put("ENTERPRISE", "ENTERPRISE");
		ignoreWord.put("OS", "OS");
		ignoreWord.put("O/S", "O/S");
		ignoreWord.put("DEPOT", "DEPOT");
		stateTo2digit.put("Andhra Pradesh".toUpperCase(),"AP".trim());
		stateTo2digit.put("Arunachal Pradesh".toUpperCase(),"AR".trim());
		stateTo2digit.put("Assam".toUpperCase(),"AS".trim());
		stateTo2digit.put("Bihar".toUpperCase(),"BR".trim());
		
		stateTo2digit.put("Chhattisgarh".toUpperCase(),"CG".trim());
		stateTo2digit.put("CHHATISGARH".toUpperCase(),"CG".trim());

		stateTo2digit.put("Goa".toUpperCase(),"GA".trim());
		stateTo2digit.put("Gujarat".toUpperCase(),"GJ".trim());
		stateTo2digit.put("Haryana".toUpperCase(),"HR".trim());
		stateTo2digit.put("Himachal Pradesh".toUpperCase(),"HP".trim());
		stateTo2digit.put("Jammu & Kashmir".toUpperCase(),"JK".trim());
		stateTo2digit.put("Jharkhand".toUpperCase(),"JH".trim());
		stateTo2digit.put("Karnataka".toUpperCase(),"	KA".trim());
		stateTo2digit.put("Kerala".toUpperCase(),"KL".trim());
		stateTo2digit.put("Madhya Pradesh".toUpperCase(),"MP".trim());
		stateTo2digit.put("Maharashtra".toUpperCase(),"MH".trim());
		stateTo2digit.put("Manipur".toUpperCase(),"MN".trim());
		stateTo2digit.put("Meghalaya".toUpperCase(),"ML".trim());
		stateTo2digit.put("Mizoram".toUpperCase(),"MZ".trim());
		stateTo2digit.put("Nagaland".toUpperCase(),"NL".trim());
		stateTo2digit.put("Odisha".toUpperCase(),"OR".trim());
		stateTo2digit.put("Punjab".toUpperCase(),"PB".trim());
		stateTo2digit.put("Rajasthan".toUpperCase(),"RJ".trim());
		stateTo2digit.put("Sikkim".toUpperCase(),"SK".trim());
		stateTo2digit.put("Tamil Nadu".toUpperCase(),"TN".trim());
		stateTo2digit.put("Tripura".toUpperCase(),"TR".trim());
		stateTo2digit.put("Uttarakhand".toUpperCase(),"UT".trim());
		stateTo2digit.put("Uttaranchal".toUpperCase(),"UT".trim());
		stateTo2digit.put("Uttar Pradesh".toUpperCase(),"UP".trim());
		stateTo2digit.put("West Bengal".toUpperCase(),"WB".trim());
		stateTo2digit.put("Andaman & Nicobar".toUpperCase(),"AN".trim());
		stateTo2digit.put("Chandigarh".toUpperCase(),"CH".trim());
		stateTo2digit.put("Dadra and Nagar Haveli".toUpperCase(),"DN".trim());
		stateTo2digit.put("Daman & Diu".toUpperCase(),"DD".trim());
		stateTo2digit.put("Delhi".toUpperCase(),"DL".trim());
		stateTo2digit.put("Lakshadweep".toUpperCase(),"LD".trim());
		stateTo2digit.put("Puducherry".toUpperCase(),"PY".trim());
		Collection<String> vals = stateTo2digit.values();
		for (String val : vals) {
			stateTo2digitro2digit.put(val, val);
		}
	}
	public static boolean toIgnoreUserName(String name) {
		return ignoreWord.containsKey(name);
	}
	private static Pattern g_ignpattern = Pattern.compile(
		"(\\b)(DIST|DISTT|DISTRICT|VILL|VILLAGE|VIL|TEHSIL|PO|P.O|P.O.|POST|POST OFFICE|PS|P.S|P.S.|POLICE STATION|POLICE)(\\b)(\\W*)(\\w+)"
			);
	private static Pattern g_uselessCharPatternAtBeg = Pattern.compile("^\\W+");
	private static Pattern g_uselessCharPatternAtEnd = Pattern.compile("\\W+$");
		
	
	
	
	public static ArrayList<String> getDictName(Connection conn, ArrayList<String> userLocName) {
		ArrayList<String> retval = new ArrayList<String>();
		for (int i=0,is=userLocName == null ? 0 : userLocName.size();i<is;i++)
			retval.add(getDictName(conn, userLocName.get(i)));
		return retval;
	}
	
	public static void loadDictName(Connection conn, boolean force) {
		String retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			
			long currTS = System.currentTimeMillis();
			if (g_lastLoadDict < 0 || g_lastLoadDict < currTS-600000 || force) {
				ps = conn.prepareStatement("select standardized_name, user_name from name_dictionary");
				rs = ps.executeQuery();
				while (rs.next()) {
					g_nameDictionary.put(rs.getString(2), rs.getString(1));
				}
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
				g_lastLoadDict = currTS;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	
	public static String getDictName(Connection conn, String userLocName) {
		String retval = null;
		loadDictName(conn, false);
		retval = userLocName == null || userLocName.length() == 0 ? userLocName : Utils.g_nameDictionary.get(userLocName);
		return retval == null ? userLocName : retval;
	}
	
	public static void populateAlertInfoToBeRemoved(Connection conn, IdInfo idInfo) {
		try {
			if (idInfo == null)
				return;
			PreparedStatement ps = conn.prepareStatement("select cpe.alert_mail_id, cpe.alert_phone from challan_dest_helper cdh join cdh_email_phone cpe on (cdh.ref_item_code = cpe.ref_item_code) where cdh.id=?");
			ps.setInt(1, idInfo.getId());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				idInfo.addAlertMailId(rs.getString("alert_mail_id"));
				idInfo.addAlertPhone(rs.getString("alert_phone"));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	public static IdInfo getIdInfoById(int idInfoId, Connection conn) {
		IdInfo retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(Queries.GET_IDINFO_BY_ID);
			ps.setInt(1, idInfoId);
			rs = ps.executeQuery();
			if (rs.next()) {
				retval = new IdInfo();
				int opid = Misc.getRsetInt(rs, "op_station_id");
				int lmid = Misc.getRsetInt(rs, "landmark_id");
				int spsid = Misc.getRsetInt(rs, "shape_point_id");
				int destId = Misc.getUndefInt();
				byte destType = 0;//1 => Landmark, 2 => Shapefile point, 3 => op_station
				if (!Misc.isUndef(opid)) {
					destId = opid;
					destType = 3;
				}
				else if (!Misc.isUndef(lmid)) {
					destId = lmid;
					destType = 1;
				}
				else if (!Misc.isUndef(spsid)) {
					destId = spsid;
					destType = 2;
				}
				retval.setDestId(destId);
				retval.setDestIdType(destType);
				retval.setId(rs.getInt("id"));
				retval.setMatchQuality((byte)rs.getInt("map_quality"));
				retval.setLongitude(Misc.getRsetDouble(rs, "lon"));
				retval.setLatitude(Misc.getRsetDouble(rs, "lat"));
			//	retval.setAlertMailId(rs.getString("alert_mail_id")); populated from separate table
			//	retval.setAlertPhone(rs.getString("alert_phone")); populated from separate table

			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	
	

	
	private static IdInfo getIdInfoByAddressItemCode(String addressItemCode, Connection conn, boolean isDest) {
		int retvalId = Misc.getUndefInt();
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(Queries.GET_IDINFO_BY_ADDRESS_ITEMCODE);
			ps.setString(1, addressItemCode);
			ps.setInt(2, isDest ? 1 : 0);
			rs = ps.executeQuery();
			if (rs.next()) {
				 retvalId = rs.getInt(1);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		if (!Misc.isUndef(retvalId))
			return getIdInfoById(retvalId, conn);
		else 
			return null;
	}
	private static boolean doingOrient(Connection conn, ArrayList<Integer> portNodes) throws Exception {
		Cache cache = Cache.getCacheInstance(conn);
		for (int i=0,is=portNodes == null ? 0 : portNodes.size(); i<is; i++) {
			int pno = portNodes.get(i);
			boolean retval = cache.isAncestor(conn, pno, 803);
			if (retval)
				return true;
		}
		return false;
	}
	
	private static IdInfo getIdInfoByOpMatchOrient(Connection conn, TextInfo textInfo) throws Exception {
		//[OCL-GODOWN] - Bangalore , D5116
	//	String nameToCheck = "[OCL-GODOWN] - "+textInfo.getCity()+" , "+textInfo.getCustName();
		String nameToCheck = "[OCL-GODOWN] - %"+" , "+textInfo.getCustName();
		PreparedStatement ps = conn.prepareStatement("select op_station.id, op_station.name, (regions.lowerX+regions.upperX)/2.0 lon, (regions.lowerY+regions.upperY)/2.0 lat, op_station.alt_name  from op_station join regions on (op_station.gate_reg_id = regions.id) where (op_station.name like ? ) and op_station.status=1");
		ps.setString(1, nameToCheck);
		ResultSet rs = ps.executeQuery();
		IdInfo currBest = null;
		if (rs.next()) {
			currBest = new IdInfo();
			currBest.setDestId(rs.getInt(1));
			currBest.setLongitude(rs.getDouble(3));
			currBest.setLatitude(rs.getDouble(4));
			
			//private byte destIdType; //1 => Landmark, 2 => Shapefile point, 3 => op_station
			//private byte matchQuality; //1=>District HQ, 2=>City, 3=>locality, 4=>exact
			currBest.setDestIdType((byte)(3));
			currBest.setMatchQuality((byte)10);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		return currBest;
	}
	
	private static IdInfo getIdInfoByCustLookup(Connection conn, TextInfo textInfo, ArrayList<Integer> portNodes, ArrayList<Integer> optype, ArrayList<Integer> landmarkSubType) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		IdInfo currBest = null;
		try {
			ArrayList<String> custNameParts = textInfo.getCustNameParts();
			
			int prevBestMatchScore = -1;

			if (custNameParts != null && custNameParts.size() > 0) {
				ps = null;
				rs = null;
				for (int art=0;art<2;art++) {
					ps = getLookupQByCust(conn, art == 0, custNameParts, portNodes, art == 0 ? optype : landmarkSubType, textInfo.getState());
					rs = ps.executeQuery();
					
					while (rs.next()) {
//						" select op_station.id, op_station.name, (regions.lowerX+regions.upperX)/2.0 lon, (regions.lowerY+regions.upperY)/2.0 lat, op_station.alt_name "+
						int destId = rs.getInt(1);
						String destName = rs.getString(2);
						double lon = rs.getDouble(3);
						double lat = rs.getDouble(4);
						String altName = rs.getString(5);
						String distName = rs.getString(6);
						String stateName = rs.getString(7);
						int matchScore = 0;
						
						if (!textInfo.getCustName().equalsIgnoreCase(destName)) {
							int prevMatch = -1;
							boolean foundAllMatch = true;
							for (int i=0,is=custNameParts.size(); i<is;i++) {
								String part = custNameParts.get(i);
								if (part == null)
									continue;
								int pos = destName.indexOf(part);
								if (pos >= 0) {
									if (pos > prevMatch) {
										prevMatch = pos;
										matchScore += 3;
									}
									else {
										matchScore += 1;
										foundAllMatch = false;
									}
								}
								else {
									foundAllMatch = false;
								}
							}
							if (foundAllMatch)
								matchScore = 100;
						}
						else {
							matchScore = 100;
						}
						if (matchScore > 0) {//get district and state of the point in question to see if it is appropriate
							if (art == 0) {
								//TODO ... get district and state of opstation and see if it matches 
							}
							else {
								if ((distName != null && textInfo.getDistrict() != null && distName.indexOf(textInfo.getDistrict()) <0 ) ||
										(stateName != null && textInfo.getState() != null && stateName.indexOf(textInfo.getState()) <0)
										)
									matchScore = -1;
							}
						}
						if (matchScore <= 0)
							continue;
						boolean meBetterThanBest = currBest == null || matchScore > prevBestMatchScore;

						if (meBetterThanBest) {
							if (currBest == null)
								currBest = new IdInfo();
							currBest.setDestId(destId);
							//private byte destIdType; //1 => Landmark, 2 => Shapefile point, 3 => op_station
							//private byte matchQuality; //1=>District HQ, 2=>City, 3=>locality, 4=>exact
							currBest.setDestIdType((byte)(art == 0? 3: 1));
							prevBestMatchScore = matchScore;
							currBest.setMatchQuality((byte)10);
						}
						if (prevBestMatchScore == 100)
							break;
					}
					rs = Misc.closeRS(rs);
					ps = Misc.closePS(ps);
					if (prevBestMatchScore == 100)
						break;
				}
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
			}
			if (prevBestMatchScore < 100) //TODO - merge custName with loc based look up
				currBest = null;
			return currBest;			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return currBest;
	}
    //TODO get by custNameExactMatch or destItemCode
	public static IdInfo getIdInfoByPartialMatch(Connection conn, TextInfo textInfo, ArrayList<Integer> portNodes, boolean isDest, boolean doingOrient) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		IdInfo currBest = null;
		try {
			//1. then look in landmarks and
			//2. then look in shapefileBean
			//3. look in challan_dest_helper ...
			
			
			if (textInfo == null)
				return null;
			
			ArrayList<String> addressParts = doingOrient ? new ArrayList<String>() : textInfo.preProcess(conn);			
			ArrayList<String> cityParts = new ArrayList<String>();
			if (textInfo.getCity() != null) {
				String str = textInfo.getCity();
				int idx = str.toUpperCase().lastIndexOf(" BULK");
				if (idx >= 0) {
					str = str.substring(0, idx);
					str = str.trim();
				}
				str = str.replaceAll("\\(", ",");
				str = str.replaceAll("\\)","");
				String comp[] = str.split(",");
				for (int i1=0,i1s=comp.length;i1<i1s;i1++) {
					String s1 = comp[i1];
					if (s1 != null)
						s1 = s1.trim();
					if (s1 == null || s1.length() == 0)
						continue;
					cityParts.add(s1);
				}
			}
			ArrayList<String> districtParts = new ArrayList<String>();
			if (textInfo.getDistrict() != null)
				districtParts.add(textInfo.getDistrict());
			ArrayList<String> addressPartsDict = getDictName(conn, addressParts);
			ArrayList<String> cityPartsDict = getDictName(conn, cityParts);
			ArrayList<String> districtPartsDict = getDictName(conn, districtParts);
			StringBuilder addressStdSpace = new StringBuilder();
			StringBuilder addressNormSpace = new StringBuilder();
			for (int i=0,is=addressPartsDict.size();i<is;i++) {
				if (addressStdSpace.length() != 0)
					addressStdSpace.append(" ");
				addressStdSpace.append(addressPartsDict.get(i));
				if (addressNormSpace.length() != 0)
					addressNormSpace.append(" ");
				addressNormSpace.append(addressParts.get(i));
			}
			String addressStdSpaceStr = addressStdSpace.toString();
			
			int currBestApproach = -1;
			MiscInner.Pair currBestMatchResult = new MiscInner.Pair(-1,-1);
			String currBestLocality = null;
			String currBestCity = null;
			String currBestDistrict = null;
			String currBestState = null;
			MatchResultInfo currBestResult = null;
			boolean foundExactMatchOrient = false;
			if (true) {
				for (int art=0;art<3;art++) {
					//approach = 1 => landmarks, 2=> CDH, 3=>Shapefile
					//int currApproach = art == 0 ? 1:art == 1 ? 3 : 2;
					int currApproach = art == 0 ? 1:art == 1 ? 3 : 2;
					ps = getLookupQByAddress(conn, currApproach, portNodes, isDest, addressParts, cityParts, districtParts, addressPartsDict, cityPartsDict, districtPartsDict, textInfo.getState());
					rs = ps.executeQuery();
					
					while (rs.next()) {
						//select landmarks.id, null, null, null, landmarks.name, landmarks.district_name, landmarks.state_name, (landmarks.lowerX + landmarks.upperX)/2.0 lon, (landmarks.lowerY+landmarks.upperY)/2.0 lat 
//						" select cdh.id, cdh.name, cdh.line1, cdh.locality, cdh.city, cdh.district, cdh.state, null lon, null lat "+
						int id = rs.getInt(1);
						String line1 = rs.getString(3);
						String dataLocality = rs.getString(4);
						String dataCity = rs.getString(5);
						String dataDistrict = rs.getString(6);
						String dataState = rs.getString(7);
						
						if (dataLocality == null || dataLocality.length() == 0)
							dataLocality = dataCity;
						if (dataLocality == null)
							dataLocality = line1;
						MatchResultInfo matchResult = MatchResultInfo.getMatchResult(conn, dataLocality, dataCity, dataDistrict, dataState, cityParts, districtParts, cityPartsDict, districtPartsDict, textInfo.getState(), addressStdSpaceStr, doingOrient, art == 0);
						boolean toRepl = currBestResult == null || matchResult.isBetterThan(currBestResult);
						
						if (doingOrient && currApproach == 1 && dataLocality != null && textInfo != null) {
							//check if dataLocality starts with textInfo.custName+- and ends with dest_city
							String dlUpper = dataLocality.toUpperCase();
							
							if (dataLocality != null && textInfo != null && dlUpper.startsWith(textInfo.getCustName()+"-") && dlUpper.endsWith(","+textInfo.getCity()))
								foundExactMatchOrient = true;
						}
						if (foundExactMatchOrient)
							toRepl = true;
						if (toRepl) {
							currBestResult = matchResult;
							currBestApproach = currApproach;
							currBestCity = dataCity;
							currBestLocality = dataLocality;
							if (currBest == null)
								currBest = new IdInfo();
							currBest.setDestId(id);
							currBest.setMatchQuality((byte)3);
							currBest.setDestIdType((byte)(currApproach == 1 ? 1 : 2));
							currBest.setLongitude(Misc.getRsetDouble(rs, "lon"));
							currBest.setLatitude(Misc.getRsetDouble(rs, "lat"));
						}
						if (foundExactMatchOrient)
							break;
					}
					rs = Misc.closeRS(rs);
					ps = Misc.closePS(ps);
					if (foundExactMatchOrient)
						break;
				}
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
			}
			if (currBest != null) {
				if (currBestApproach == 2) {//from cdh ... get
					currBest = Utils.getIdInfoById(currBest.getDestId(), conn);
					currBest.setId(Misc.getUndefInt());
				}
				else {
					currBest.setDestIdType((byte)(currBestApproach == 1 ? 1 : 2));
				}
			}
			if (currBest == null)
				currBest = new IdInfo();
			return currBest;			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		if (currBest == null)
			currBest = new IdInfo();
		return currBest;
	}
	
	
	
	public static class MatchResultInfo {
		public int indexOfMinMatchInAsked = -1;
		public int indexOfClosestDataString = -1;
		public int countOfMatch = 0;
		public boolean searchIsValid = false;
		public byte districtMatches = -1;//not known
		public byte stateMatches = -1;//not known
		public boolean isOrientLikeEndAtLandmark = false;
		
		public boolean isBetterThan(MatchResultInfo rhs) {
			boolean retval = false;
			if (this.searchIsValid != rhs.searchIsValid)
				return this.searchIsValid;
			if (rhs.stateMatches == 0 && (this.stateMatches == 1 || this.stateMatches == -1))
				return true;
			if (this.stateMatches == 0 && (rhs.stateMatches == 1 || rhs.stateMatches == -1))
				return false;
		   if (this.isOrientLikeEndAtLandmark != rhs.isOrientLikeEndAtLandmark)
			   return this.isOrientLikeEndAtLandmark;
			//if (rhs.districtMatches == 0 && (this.districtMatches == 1 || this.districtMatches == -1))
			//	return true;
			//if (this.districtMatches == 0 && (rhs.districtMatches == 1 || rhs.districtMatches == -1))
			//	return false;
			if (this.countOfMatch != rhs.countOfMatch)
				return countOfMatch > rhs.countOfMatch;
			if (indexOfClosestDataString != rhs.indexOfClosestDataString)
				return this.indexOfClosestDataString < rhs.indexOfClosestDataString;
				
			if (this.indexOfMinMatchInAsked != rhs.indexOfMinMatchInAsked) {
				return this.indexOfMinMatchInAsked < rhs.indexOfMinMatchInAsked;
			}
			return false;
			
		}
		//MatchResultInfo.getMatchResult(dataLocality, dataCity, dataDistrict, dataState, addressParts, cityParts, districtParts, addressPartsDict, cityPartsDict, districtPartsDict, textInfo.getState());
		public static MatchResultInfo getMatchResult(Connection conn, String dataLocality, String dataCity, String dataDistrict, String dataState, ArrayList<String> cityParts, ArrayList<String> districtParts, ArrayList<String> cityPartsDict, ArrayList<String> districtPartsDict, String state, String addressStdSpaceSeparated, boolean doingOrient, boolean doingLandmark) {
			MatchResultInfo retval = new MatchResultInfo();
			retval.calcMatchResult(conn, dataLocality, dataCity, dataDistrict, dataState, cityParts, districtParts, cityPartsDict, districtPartsDict, state, addressStdSpaceSeparated, doingOrient, doingLandmark);
			return retval;
		}
		public static HashMap<String,String> g_noBreakWork = new HashMap<String, String>();
		static {
			
			g_noBreakWork.put("ROAD", Misc.emptyString);
			g_noBreakWork.put("RD", Misc.emptyString);
			g_noBreakWork.put("RD.", Misc.emptyString);
			g_noBreakWork.put("EXPRESSWAY", Misc.emptyString);
			g_noBreakWork.put("EXPWY", Misc.emptyString);
			g_noBreakWork.put("EXPY.", Misc.emptyString);
			g_noBreakWork.put("HIGHWAY", Misc.emptyString);
			g_noBreakWork.put("STREET", Misc.emptyString);
			g_noBreakWork.put("ST", Misc.emptyString);
			g_noBreakWork.put("ST.", Misc.emptyString);
			g_noBreakWork.put("LANE", Misc.emptyString);
			g_noBreakWork.put("LN", Misc.emptyString);
			g_noBreakWork.put("LN.", Misc.emptyString);
			g_noBreakWork.put("BAZAR", Misc.emptyString);
			g_noBreakWork.put("BAZAAR", Misc.emptyString);
			g_noBreakWork.put("PUR", Misc.emptyString);
			g_noBreakWork.put("HAT", Misc.emptyString);
			g_noBreakWork.put("GANJ", Misc.emptyString);
			g_noBreakWork.put("GUNJ", Misc.emptyString);
		}
		public static int wordBoundaryMatch(String inThis, String pattern) {
			int len = pattern.length();
			for (int p=0,ps=inThis.length(); p<ps; p++) {
				int pos = inThis.indexOf(pattern, p);
				if (pos >= 0) {
					int chPrev = pos == 0 ? -1 : inThis.charAt(pos-1);
					int nextPos = pos+len;
					int chNext = nextPos >= ps ? -1 : inThis.charAt(nextPos);
					if ((chPrev == -1 || !Character.isLetterOrDigit(chPrev)) && (chNext == -1 || !Character.isLetterOrDigit(chNext))) {
						if (chNext != -1 && inThis.length() > nextPos+1) {
							//make sure the next word is not in g_noBreakWord
							int spaceIdx = inThis.indexOf(' ', nextPos+1);
							String nextWord = inThis.substring(nextPos+1, spaceIdx == -1 ? inThis.length() : spaceIdx);
							nextWord = nextWord.trim();
							if (!g_noBreakWork.containsKey(nextWord))
								return pos;
						}
						else {
							return pos;
						}
					}
							
					p = nextPos-1;
				}
			}
			return -1;
		}

		public void calcMatchResult(Connection conn, String dataLocality, String dataCity, String dataDistrict, String dataState, ArrayList<String> cityParts, ArrayList<String> districtParts, ArrayList<String> cityPartsDict, ArrayList<String> districtPartsDict, String state, String addressStdSpaceSeparated, boolean doingOrient, boolean doingLandmark) {
			//1. Will look in the dataLocality and broadly check the last part of dataLocality matches 
			//2. and then check what is the earliest match. There cant be more than gap of 1 between consecutive matches.
			//3. Because dataLocality may contain district or state - either in dataLocality or address given-
			//4. unless ignoring of that causes 0 remaining
			//To determine better - 1st match is given pref, then larger match and then district/state maches
			//
			if (dataLocality == null)
				dataLocality = "";
			dataLocality = dataLocality.replaceAll("(\\[)(.)*(\\])","$2"); //get rid of name parts
			String[] dataLocalityParts = dataLocality.split(",");
			int indexOfMinMatchInAsked = -1; 
			int prevPartMatchIndex = -1;
			byte stateMatches = -1;			
			byte districtMatches = -1;
			boolean matchIsValid = true;
			int matchCount = 0;
			int distMatchIndexInData = -1;
			int distMatchIndexInAsked = -1;
			int currMaxGapBeforeNoMatch = 1;
			for (int i=dataLocalityParts.length-1;i>=0;i--) {
				String dp = dataLocalityParts[i];
				dp = dp.replaceAll("^\\W+", "");
				dp = dp.replaceAll("\\W+$", "");
				if (dp.length() == 0)
					continue;
				dp = Utils.getDictName(conn, dp);
				int matchPos = this.wordBoundaryMatch(addressStdSpaceSeparated, dp);
				if (matchPos < 0 && cityPartsDict != null && cityPartsDict.size() > 0 && dp.equals(cityPartsDict.get(0))) {
					matchPos = addressStdSpaceSeparated.length();
					if (doingOrient && doingLandmark) {
						if (i == dataLocalityParts.length-1) {
							this.isOrientLikeEndAtLandmark = true;
						}
						else if (i==dataLocalityParts.length-2){
							String end = dataLocalityParts[dataLocalityParts.length-1];
							if (end != null && cityPartsDict.size() > 1 &&  end.equals(cityPartsDict.get(1)))
								this.isOrientLikeEndAtLandmark = true;
						}
					}
				}
				boolean dpIsDistOrStateSoIgnoreMustMatchLastCritieria = false;

				if (state != null && state.equals(dp)) {
					matchPos = addressStdSpaceSeparated.length()+2;
					dpIsDistOrStateSoIgnoreMustMatchLastCritieria = true;
					stateMatches = 1;
					currMaxGapBeforeNoMatch++;
				}
				else if (districtParts != null && districtParts.size() > 0 && districtPartsDict.get(0).equals(dp)) {
					matchPos = addressStdSpaceSeparated.length()+1;
					dpIsDistOrStateSoIgnoreMustMatchLastCritieria = true;
					districtMatches =1;
					currMaxGapBeforeNoMatch++;
					distMatchIndexInData = i;
					distMatchIndexInAsked = matchPos;

				}
				else if (dp.equals(dataState)) {
					dpIsDistOrStateSoIgnoreMustMatchLastCritieria = true;
					if (matchPos >= 0) {
						matchPos = addressStdSpaceSeparated.length()+2;
						stateMatches = 1;
					}
					currMaxGapBeforeNoMatch++;
				}
				else if (dp.equals(dataDistrict)) {
					dpIsDistOrStateSoIgnoreMustMatchLastCritieria = true;
					if (matchPos >= 0) {
						matchPos = addressStdSpaceSeparated.length()+1;
						districtMatches = 1;
						distMatchIndexInData = i;
						distMatchIndexInAsked = matchPos;
						matchIsValid = true;
						matchCount++;
					}
					currMaxGapBeforeNoMatch++;
				}
				if (dpIsDistOrStateSoIgnoreMustMatchLastCritieria) {
					if (matchPos >= 0 && prevPartMatchIndex < 0) 
						indexOfMinMatchInAsked = matchPos;
					continue;
				}
				
				if (matchPos < 0 && ((prevPartMatchIndex < 0 && dataLocalityParts.length > (i+currMaxGapBeforeNoMatch)) || prevPartMatchIndex > (i+currMaxGapBeforeNoMatch))) {
					break;
				}
				if (matchPos >= 0) {
					prevPartMatchIndex = i;
					if (indexOfMinMatchInAsked > matchPos || indexOfMinMatchInAsked < 0) {
						indexOfMinMatchInAsked = matchPos;
						
					}
					matchIsValid = true;
					matchCount++;
					currMaxGapBeforeNoMatch = 1;
				}
			}//for each part of dataLocality
			if (prevPartMatchIndex < 0 && distMatchIndexInData >= 0) {
				prevPartMatchIndex = distMatchIndexInData;
				indexOfMinMatchInAsked = distMatchIndexInAsked;
			}
			this.districtMatches = districtMatches;
			this.stateMatches = stateMatches;
			
			this.indexOfClosestDataString = prevPartMatchIndex < 0 ?  Integer.MAX_VALUE : prevPartMatchIndex;
			this.indexOfMinMatchInAsked = indexOfMinMatchInAsked < 0 ?  Integer.MAX_VALUE :  indexOfMinMatchInAsked;
			this.searchIsValid = matchIsValid;
			this.districtMatches = districtMatches;
			this.stateMatches = stateMatches;
			this.countOfMatch = matchCount;
		
		}	
	}		
	
	private static PreparedStatement getLookupQByCust(Connection conn, boolean doingByOp, ArrayList<String> custNameParts, ArrayList<Integer> portNodes, ArrayList<Integer> types, String state) {
		PreparedStatement ps = null;
		try {
			String baseQ = doingByOp ? Queries.GET_ID_LIKE_INFO_FROM_OPSTATION_CUST : Queries.GET_ID_LIKE_INFO_FROM_LANDMARKS_CUST;
			StringBuilder temp = new StringBuilder();
			if (portNodes == null || portNodes.size() == 0)
				temp.append(Misc.G_TOP_LEVEL_PORT);
			else {
				Misc.convertInListToStr(portNodes, temp);
			}
			ArrayList<String> params = new ArrayList<String>();
			StringBuilder paramSB = new StringBuilder();
			helperAddToParamAndReplClause(conn, custNameParts, custNameParts, doingByOp ? "op_station" : "landmarks", "name", paramSB, params);
			if (paramSB.length() != 0)
				baseQ += "and ("+paramSB+") ";
			baseQ = baseQ.replaceAll("#ANC_ID", temp.toString());
			temp.setLength(0);
			if (types == null || types.size() == 0) {
				baseQ = baseQ.replaceAll("#MAPPING_TYPE", "");
			}
			else {
				temp.append(doingByOp ? " and opstation_mapping.type in (" : " and landmarks.sub_type in (");
				Misc.convertInListToStr(types, temp);
				temp.append(") ");
				baseQ = baseQ.replaceAll("#MAPPING_TYPE", temp.toString());
			}
			ps = conn.prepareStatement(baseQ);
			int baseParam = 1;
			for (int i=0,is=params.size();i<is;i++)
				ps.setString(baseParam++, params.get(i));
			if (!doingByOp) {
				ps.setString(baseParam++, state);
				ps.setString(baseParam++, state);
			}
		}
		catch (Exception e) {
			ps = Misc.closePS(ps);
		}
		return ps;
	}
	private static void helperAddToParamAndReplClause(Connection conn, ArrayList<String> strList, ArrayList<String> dictList, String table, String col, StringBuilder sb, ArrayList<String> params) {
		for (int i=0,is=strList.size();i<is;i++) {
			String str = strList.get(i);
			String dict = dictList.get(i);
			if (str != null) {
				params.add(str);
				if (sb.length() != 0)
					sb.append(" or ");
				sb.append(table).append(".").append(col).append(" like ? ");
				if (dict != null && !dict.equals(str)) {
					params.add(dict);
					sb.append(" or ").append(table).append(".").append(col).append(" like ? ");
				}
			}
		}
	}

	private static PreparedStatement getLookupQByAddress(Connection conn, int approach, ArrayList<Integer> portNodes, boolean isDest
			, ArrayList<String> addressParts, ArrayList<String> city, ArrayList<String> district, ArrayList<String> addressPartsDict, ArrayList<String> cityDict, ArrayList<String> districtDict
			,String state
			) {
		//approach = 1 => landmarks, 2=> CDH, 3=>Shapefile
		//params are not set
		PreparedStatement ps = null;
		try {
			String baseQ = approach == 1 ? Queries.GET_ID_LIKE_INFO_FROM_LANDMARKS : approach == 2 ? Queries.GET_ID_LIKE_INFO_FROM_CDH : Queries.GET_ID_LIKE_INFO_FROM_SHP;
			StringBuilder temp = new StringBuilder();
			if (portNodes == null || portNodes.size() == 0)
				temp.append(Misc.G_TOP_LEVEL_PORT);
			else {
				Misc.convertInListToStr(portNodes, temp);
			}
			//approach = 1 => landmarks, 2=> CDH, 3=>Shapefile
			
			if (approach != 3)
				baseQ = baseQ.replaceAll("#ANC_ID", temp.toString());
			String replString = null;
			ArrayList<String> params = new ArrayList<String>();
			StringBuilder sb = new StringBuilder();
			if (approach == 1) {//landmarks ... check if
				helperAddToParamAndReplClause(conn, addressParts, addressPartsDict, "landmarks", "name", sb, params);
				helperAddToParamAndReplClause(conn, city, cityDict, "landmarks", "name", sb, params);
				helperAddToParamAndReplClause(conn, district, districtDict, "landmarks", "name", sb, params);
			}
			else if (approach == 2) {//cdh
				helperAddToParamAndReplClause(conn, addressParts, addressPartsDict, "cdh", "locality", sb, params);
				helperAddToParamAndReplClause(conn, city, cityDict, "cdh", "city", sb, params);
				helperAddToParamAndReplClause(conn, district, districtDict, "cdh", "district", sb, params);			
			}
			else if (approach == 3) {//shape
				helperAddToParamAndReplClause(conn, addressParts, addressPartsDict, "shp", "name", sb, params);
				helperAddToParamAndReplClause(conn, city, cityDict, "shp", "name", sb, params);
				helperAddToParamAndReplClause(conn, district, districtDict, "shp", "name", sb, params);
			}
			if (sb.length() == 0)
				sb.append(" 1=1 ");
			baseQ = baseQ.replaceAll("#NAME_LOOKUP_CLAUSE", sb.toString());
			ps = conn.prepareStatement(baseQ);
			int colIndex = 1;
			for (int i=0,is=params.size();i<is;i++) {
				ps.setString(colIndex++, "%"+params.get(i)+"%");
			}
			ps.setString(colIndex++, state);
			ps.setString(colIndex++, state);
			if (approach == 2)
				ps.setInt(colIndex++, isDest ? 1 : 0);
		}
		catch (Exception e) {
			ps = null;
			e.printStackTrace();
			
		}
		return ps;
	}
	
	public static void saveCDHInfo(Connection conn, TextInfo textInfo, IdInfo idInfo, int portNodeId, boolean doInDest) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (idInfo.getId() < 0) {//we need insert ... else ... we need to update
				ps = conn.prepareStatement("insert into challan_dest_helper (port_node_id, ref_item_code,name,line1,locality,city,district,state, is_dest) values (?,?,?,?,?,?,?,?,?)");
				ps.setInt(1, portNodeId);
				ps.setString(2, textInfo.getAddressItemCode(127));
				ps.setString(3, textInfo.getCustName(127));
				ps.setString(4, null);
				StringBuilder sb = new StringBuilder();
				for (int i=0,is=textInfo.getLineCount();i<is;i++) {
					String l = textInfo.getLine(i, 127);
					if (l == null)
						continue;
					if (sb.length() != 0)
						sb.append(",");
					sb.append(l);
				}
				if (sb.length() > 127)
					sb.setLength(127);
				ps.setString(5, sb.toString());
				ps.setString(6, textInfo.getCity(127));
				ps.setString(7, textInfo.getDistrict(127));
				ps.setString(8, textInfo.getState(127));
				ps.setInt(9, doInDest ? 1 : 0);
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();
				if (rs.next()) {
					idInfo.setId(rs.getInt(1));
				}
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
			}
			//now update the matchQuality part
			ps = conn.prepareStatement("update challan_dest_helper set landmark_id=?, shape_point_id=?, op_station_id=?, map_quality=? where id=?");
			byte destIdType = idInfo.getDestIdType();
			Misc.setParamInt(ps, Misc.getUndefInt(), 1);
			Misc.setParamInt(ps, Misc.getUndefInt(), 2);
			Misc.setParamInt(ps, Misc.getUndefInt(), 3);
			Misc.setParamInt(ps, idInfo.getDestId(), destIdType == 3 ? 3 : destIdType == 2 ? 2 : 1);
			Misc.setParamInt(ps, idInfo.getMatchQuality(),4);
			ps.setInt(5, idInfo.getId());
			ps.execute();
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}

	public static void main(String[] args) {
		Connection conn = null;
	 boolean destroyIt = false;
	   try {
		   conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		   if (!conn.getAutoCommit())
			   conn.setAutoCommit(true);
	//	   PreparedStatement ps = conn.prepareStatement("select distinct null, dest_code, consignee, dest_addr_1, dest_addr_2, dest_addr_3, dest_addr_4, dest_city, dest_state from challan_details where 1=1 and (dest_state = 'West Bengal' or dest_state='JHARKHAND') and dest_code in ('12901351', '21100112','22901782','12903227','12900886','22901104') /*and dest_code in ('12902070','21100145','11102401','12902070','12901081','12901017','21100334','12901088')*/");
		   PreparedStatement ps = conn.prepareStatement("select distinct null, dest_code, consignee, dest_addr_1, dest_addr_2, dest_addr_3, dest_addr_4, dest_city, dest_state from challan_details where 1=1 and (dest_state = 'West Bengal' or dest_state='JHARKHAND') /*and dest_code in ('12900886', '22901782','12903227') and dest_code in ('12902070','21100145','11102401','12902070','12901081','12901017','21100334','12901088')*/");
		   ResultSet rs = ps.executeQuery();
		   while (rs.next()) {
			   String destCode = (rs.getString(2));
			   String destDesc = (rs.getString(3));
			   String destAdd1 = (rs.getString(4));
			   String destAdd2 = (rs.getString(5));
			   String destAdd3 = (rs.getString(6));
			   String destAdd4 = (rs.getString(7));
			  
			   String destCity = (rs.getString(8));
			   String destState = (rs.getString(9));
			   TextInfo textInfo = new TextInfo();
			   textInfo.setAddressItemCode(destCode);
			   textInfo.setCity(destCity);
			   textInfo.setState(destState);
			   textInfo.setCustName(destDesc);
			   textInfo.setLine(destAdd1, 0);
			   textInfo.setLine(destAdd2, 1);
			   textInfo.setLine(destAdd3, 2);
			   textInfo.setLine(destAdd4, 3);
			   //doSpecialProcessing(conn, 481,1,textInfo, destAdd1, destAdd2, destAdd3, destAdd4, destCity);
			   getIdInfo(textInfo, 481, Misc.getUndefInt(), true, conn, null);
		   }
		   rs = Misc.closeRS(rs);
		   ps = Misc.closePS(ps);
	   }
	   catch (Exception e) {
		 e.printStackTrace();  
		 destroyIt = true;
	   }
	   finally {
		   if (conn != null) { 
			   try {
				   DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			   }
		   	  catch (Exception e) {
		   	  }
		  }
	   }
	}
	
	public static class Queries {
		public static final String GET_DICT_NAME = "select standardized_name from name_dictionary where user_name=?";
		public static final String GET_IDINFO_BY_ID  = " select cdh.id, cdh.port_node_id, cdh.ref_item_code, cdh.landmark_id, cdh.shape_point_id, cdh.op_station_id, cdh.map_quality, "+
		" cdh.name, cdh.line1, cdh.locality, cdh.city, cdh.district,cdh.state "+
		" ,(case when ops.id is not null then (op_reg.lowerX+op_reg.upperX)/2.0 "+
		"       when lm.id is not null then (lm.lowerX+lm.upperX)/2.0 "+
		"       when sps.id is not null then (sps.longitude) "+
		"  else null end) lon "+
		" ,(case when ops.id is not null then (op_reg.lowerY+op_reg.upperY)/2.0 "+
		"       when lm.id is not null then (lm.lowerY+lm.upperY)/2.0 "+
		"       when sps.id is not null then (sps.latitude) "+
		"  else null end) lat "+
		//" alert_mail_id, alert_phone "+ //populated from else where
		" from challan_dest_helper cdh "+
		" left outer join op_station ops on (ops.id = cdh.op_station_id) "+
		" left outer join regions op_reg on (op_reg.id = ops.gate_reg_id) "+
		" left outer join landmarks lm on (lm.id = cdh.landmark_id) "+
		" left outer join shapefile_points sps on (sps.id = cdh.shape_point_id) "+
		" where cdh.id = ? "
		;
		public static final String GET_IDINFO_BY_EXACT_MATCH = "select cdh.id from challan_dest_helper cdh where (name = ? or (name is null and ? is null)) "+
		" and (locality = ? or ((locality is null or locality='') and ? is null))"+
		" and (city = ? or ((city is null or city='') and ? is null))"+
		" and (district = ? or ((district is null or district='') and ? is null))"+
		" and (state = ? or ((state is null or state='') and ? is null)) "+
		" and is_dest = ? "
		
		;
		
		public static final String GET_IDINFO_BY_ADDRESS_ITEMCODE = "select cdh.id from challan_dest_helper cdh where ref_item_code = ? and is_dest=?";

		public static final String GET_ID_LIKE_INFO_FROM_OPSTATION_CUST =
			" select op_station.id, op_station.name, (regions.lowerX+regions.upperX)/2.0 lon, (regions.lowerY+regions.upperY)/2.0 lat, null alt_name, null district_name, null state_name "+
			" from op_station join regions on (op_station.gate_reg_id = regions.id) "+
			" join opstation_mapping on (opstation_mapping.op_station_id = op_station.id) "+
			" join port_nodes opmleaf on (opmleaf.id = opstation_mapping.port_node_id) "+
			" join port_nodes opmanc on (opmanc.lhs_number <= opmleaf.lhs_number and opmanc.rhs_number >= opmleaf.rhs_number) "+
			" where op_station.status in (1)  "+
			" and (opmanc.id in (#ANC_ID)) "+
			" #MAPPING_TYPE ";//and (opstation_mapping.type in (1,2)) "+
//			" and (#NAME_CLAUSEop_station.name like ? "//'%xyz%' "
		;

		public static final String GET_ID_LIKE_INFO_FROM_LANDMARKS_CUST =
			" select landmarks.id, landmarks.name, (landmarks.lowerX+landmarks.upperX)/2.0 lon, (landmarks.lowerY+landmarks.upperY)/2.0 lat, null alt_name, landmarks.district_name, landmarks.state_name  "+
			" from landmarks "+
			" join port_nodes opmleaf on (opmleaf.id = landmarks.port_node_id) "+
			" join port_nodes opmanc on (opmanc.lhs_number <= opmleaf.lhs_number and opmanc.rhs_number >= opmleaf.rhs_number) "+
			" where "+
			" (landmarks.state_name = ? or ? is null) "+
			" and (opmanc.id in (#ANC_ID)) "+
			" #MAPPING_TYPE " +// and (landmarks.sub_type in (1,2)) "+
			" order by getRankShapeLM(state_name) "
		;
		public static final String GET_ID_LIKE_INFO_FROM_CDH =
			" select cdh.id, cdh.name, cdh.line1, cdh.locality, cdh.city, cdh.district, cdh.state, null lon, null lat "+
			" from challan_dest_helper cdh "+
			" join port_nodes opmleaf on (opmleaf.id = cdh.port_node_id) "+
			" join port_nodes opmanc on (opmanc.lhs_number <= opmleaf.lhs_number and opmanc.rhs_number >= opmleaf.rhs_number) "+
			" where "+
			"  (opmanc.id in (#ANC_ID)) "+
			" and ( "+
			"#NAME_LOOKUP_CLAUSE "+
//			" (? is not null and cdh.city like ?) "+ //city
//			" or (? is not null and cdh.city like ?) "+ //dict city
//			" or (? is not null and cdh.city like ?) "+ //dict district
//			" or (? is not null and cdh.city like ?) "+ //dict district
			" )  and (state=? or ? is null)  "+
			" and (is_dest=?) "
			;
		public static final String GET_ID_LIKE_INFO_FROM_LANDMARKS =
			" select landmarks.id, null, null, null, landmarks.name, landmarks.district_name, landmarks.state_name, (landmarks.lowerX + landmarks.upperX)/2.0 lon, (landmarks.lowerY+landmarks.upperY)/2.0 lat "+
			" from landmarks "+
			" join port_nodes opmanc on (opmanc.id = landmarks.port_node_id) "+
			" join port_nodes opmleaf on (opmanc.lhs_number <= opmleaf.lhs_number and opmanc.rhs_number >= opmleaf.rhs_number) "+
			" where "+
			" (opmleaf.id in (#ANC_ID)) "+
			" and ( "+
			"#NAME_LOOKUP_CLAUSE " +
//			" (? is not null and landmarks.name like ?) "+//'%xyz%' "+  //city
//			" or (? is not null and landmarks.name like ?) "+//'%xyz%' "+  //dict city
//			" or (? is not null and landmarks.name like ?) "+//'%xyz%' "+  //district
//			" or (? is not null and landmarks.name like ?) "+//'%xyz%' "+  //dict district
			" )  and (state_name=? or ? is null) order by opmleaf.lhs_number " 
			;
		public static final String GET_ID_LIKE_INFO_FROM_SHP =
			" select shp.id, null, null, null, shp.name, shp.district_name, shp.state_name, longitude lon, latitude lat "+
			" from shapefile_points shp "+
			" where shp.state_name <> 'Unknown' and shp.district_name <> 'Unknown' "+
			" and ("+
			"#NAME_LOOKUP_CLAUSE " +			
//			" (? is not null and shp.name like ?) "+// 'xyz' "+ //city
//			" or (? is not null and shp.name like ?) "+//'xyz' //dict city
//			" or (? is not null and shp.name like ?) "+//'xyz' //district
//			" or (? is not null and shp.name like ?) "+//'xyz' //dict nam		
			" ) and (state_name=? or ? is null) order by getRankShapeLM(state_name) "
		;
	}
	
	 private static IdInfo getIdInfoByExactMatch(TextInfo textInfo, Connection conn, boolean isDest) {
		 PreparedStatement ps = null;
		 ResultSet rs = null;
		 
		int retvalId = Misc.getUndefInt();
		try {
			ps = conn.prepareStatement(Queries.GET_IDINFO_BY_EXACT_MATCH);
			int colIndex = 1;
			StringBuilder sb = new StringBuilder();
			for (int i=0,is=textInfo.getLineCount();i<is;i++) {
				String l = textInfo.getLine(i, 127);
				if (l == null)
					continue;
				if (sb.length() != 0)
					sb.append(",");
				sb.append(l);
			}
			if (sb.length() == 0)
				sb = null;
			ps.setString(colIndex++, textInfo.getCustName());
			ps.setString(colIndex++, textInfo.getCustName());
			ps.setString(colIndex++, sb == null ? null : sb.toString());
			ps.setString(colIndex++, sb == null ? null : sb.toString());
			ps.setString(colIndex++, textInfo.getCity());
			ps.setString(colIndex++, textInfo.getCity());
			ps.setString(colIndex++, textInfo.getDistrict());
			ps.setString(colIndex++, textInfo.getDistrict());
			ps.setString(colIndex++, textInfo.getState());
			ps.setString(colIndex++, textInfo.getState());
			ps.setInt(colIndex++, isDest ? 1 : 0);
			rs = ps.executeQuery();
			if (rs.next()) {
				 retvalId = rs.getInt(1);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		if (!Misc.isUndef(retvalId))
			return getIdInfoById(retvalId, conn);
		else 
			return null;
	}
}
