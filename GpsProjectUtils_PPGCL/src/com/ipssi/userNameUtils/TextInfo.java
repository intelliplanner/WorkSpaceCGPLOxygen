package com.ipssi.userNameUtils;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ipssi.gen.utils.Misc;

public class TextInfo {
	private String custName;
	private String line[] = {null, null, null, null};
	private String city;
	private String district;
	private String state;
	private String addressItemCode;
	private ArrayList<String> processedCombo;
	private ArrayList<String> dictProcessedCombo;
	public boolean isNull() {
		boolean retval = (custName == null || custName.trim().length() == 0) 
		&& (city == null || city.trim().length() == 0)
		&& (district == null || district.trim().length() == 0)
		&& (state == null || state.trim().length() == 0)
		&& (addressItemCode == null || addressItemCode.trim().length() == 0)
		;
		if (retval) {
			for (int i=0,is=line.length; i<is; i++) {
				retval = retval && (line[i] == null || line[i].trim().length() == 0);
				if (!retval)
					break;
			}
		}
		return retval;
	}
	public String toString() {
		return addressItemCode+","+custName+","+line[0]+","+line[1]+","+line[2]+","+line[3]+","+city+","+district+","+state;
	}
	public static String cleanupName(String userLocName) {
		try {
			//Various heuristics ... to implement ... currently will capitalizes
			if (userLocName != null) {
				userLocName = userLocName.replaceAll("([^A-Za-z0-9_\\s]+)(\\s+)", "$1").replaceAll("(\\s)+[-:\\.,&;_](\\s)+","$2").replaceAll(":"," ").replaceAll("\\s{2,}"," ").toUpperCase().trim();
				if (userLocName.length() == 0)
					userLocName = null;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat i
		}
		return userLocName;
	}
	
	public ArrayList<String> preProcess(Connection conn) throws Exception {
		custName = cleanupName(custName);
		//for (int art=0,arts = line.length;art<arts;art++) .. already done
		//	line[art] = cleanupName(line[art]);
		//city = cleanupName(city);
		//district = cleanupName(district);
		//state = cleanupName(state);
		String stdName = Utils.getDictName(conn,city);
		if (stdName != null) {
			if (stdName.equals("NORTH 24 PARGANAS")) {
				//city = ("BARASAT");
				this.district = stdName;
			}
			else if (stdName.equals("SOUTH 24 PARGANAS")) {
				//city = ("DIAMOND HARBOUR");
				this.district = stdName;
			}
			else if (stdName.equals("MURSHIDABAD")) {
				this.district = stdName;
				this.city = "BAHARAMPUR";
			}
		}

		//1st get delivery
		String adjLine[]=  new String[line.length];
		String delString = null;
		for (int art=0,arts=line.length; art< arts; art++) {
			String a = line[art];
			adjLine[art] = line[art];
			if (a == null)
				continue;
//			a= a.replaceAll("\\b(VILL|VIL|VILLAGE)\\.{0,1}\\W*\\&\\W*[PA]\\.{0,1}[OST]\\.{0,1}\\W+", ",");			
//BUGGY			a= a.replaceAll("\\b([PA]\\.{0,1}[OST])|(VILL|VIL|VILLAGE)\\.{0,1}\\W*\\&\\W*([PA]\\.{0,1}[OST])|(VILL|VIL|VILLAGE)\\.{0,1}\\W+", ",");
			a= a.replaceAll("\\b(DIST|AT|DISTT|DISTRICT|VILL|VILLAGE|VIL|TEHSIL|PO|P.O|POST|POST OFFICE|PS|P.S|POLICE STATION|POLICE)\\W+", ",");
			a=a.replaceAll("(\\w)+(\\s)+(PUR)", "$1$3");
			a=a.replaceAll("(\\w)+(\\s)+(HAT)", "$1$3");
			a=a.replaceAll("(\\w)+(\\s)+(PORE)", "$1$3");
			a=a.replaceAll("\\bDELIVERY\\b\\W+(\\bAT\\b)*\\W*"," DELIVERY ");
			int idx = a.indexOf("DELIVERY ");
			if (idx >= 0) {
				String del = a.substring(idx+9);
				del = del.replaceAll("^\\W+", "");
				del = del.replaceAll("\\W+$", "");
				if (del != null && del.length() > 0) {
					adjLine[0] = del;
					for (int l1=1;l1<arts;l1++)
						adjLine[l1] = null;
					break;
				}
				a = a.substring(0, idx).trim();
				adjLine[art] = a;
			}
			else {
				adjLine[art] = a;
			}
		}
		HashMap<String, String> currSeen = new HashMap<String, String>();
		ArrayList<String> retval = new ArrayList<String>();
		//if (city != null) .. shit screws up things for xxx,city,state in loc and city also in getCity
		//	currSeen.put(city, city); .. screws up thins
		//if (state != null)
		//	currSeen.put(state, state);
		if (false && custName == null) {
			int idx = adjLine[0].indexOf(",");
			if (idx < 0) {
				custName = adjLine[0];
				adjLine[0] = null;
			}
			else {
				custName = adjLine[0].substring(0, idx);
				adjLine[0] = adjLine[0].substring(idx+1);
			}
			
			//custName = retval.get(0);
			//retval.remove(0);
		}
		for (int art=0,arts=adjLine.length; art< arts; art++) {
			String sline = adjLine[art];
			helpPreProcess(sline, retval, currSeen);
			
		}
		return retval;
	}
	
	public static void helpPreProcess(String sline, ArrayList<String> retval, HashMap<String, String> currSeen) {
		if (sline != null)
			sline = sline.trim();
		if (sline != null && sline.length() == 0)
			sline = null;
		if (sline == null)
			return;
		sline = sline.replaceAll("-", ",");
		String processed[] = sline.split("[\\[\\],()\\s]+");
		for (int i=0,is=processed.length;i<is;i++) {
			if (processed[i] != null) {
				String s = processed[i];
				s = s.replaceAll("^\\W+", "");
				s = s.replaceAll("\\W+$", "");
				if (Utils.toIgnoreUserName(s))
					continue;
				if (s.length() != 0 && Misc.isUndef(Misc.getParamAsInt(s)) && (currSeen == null || (!currSeen.containsKey(s) && !currSeen.containsKey(Utils.get2digitStateCode(s))))) {
					retval.add(s);
				}
			}
		}
	}
	public ArrayList<String> getCustNameParts() throws Exception {//assumes capitalized
		if (this.custName == null)
			return null;
		String[] parts = null;
		ArrayList<String> retval = new ArrayList<String>();
		try {
			parts = custName.split("[,\\s]+");
			String part = null;
			for (int i=0,is = parts == null ? 0 : parts.length; i<is;parts[i++] = part) {
				part = parts[i];
				if (part == null) {
					continue;
				}
				part = part.replaceAll("^\\W+", "");
				part = part.replaceAll("\\W+$", "");
				if (part.length() == 0) {
					continue;
				}
				if (!Utils.toIgnoreUserName(part)) {
					retval.add(part);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return retval;
	}

	public static void main(String[] args) {
		String a = null;
		a = "POLSONDA";
		
		a= a.replaceAll("\\b(([PA](\\.){0,1}[OST])|VILL|VIL|VILLAGE) (\\.){0,1}\\W*\\&\\W*(([PA](\\.){0,1}[OST])|VILL|VIL|VILLAGE)(\\.){0,1}\\W+", ",");
		a = "PO&PS asdasd";
		a= a.replaceAll("\\b(([PA](\\.{0,1})[OST])|VILL|VIL|VILLAGE) (\\.{0,1})\\W*&\\W*(([PA](\\.{0,1})[OST])|VILL|VIL|VILLAGE)(\\.{0,1})\\W+", ",");
		a = "POLSONDA";
		a= a.replaceAll("\\b(DIST|AT|DISTT|DISTRICT|VILL|VILLAGE|VIL|TEHSIL|PO|P.O|POST|POST OFFICE|PS|P.S|POLICE STATION|POLICE)\\W+", ",");
		a = "PO LSONDA";
		a= a.replaceAll("\\b(DIST|AT|DISTT|DISTRICT|VILL|VILLAGE|VIL|TEHSIL|PO|P.O|POST|POST OFFICE|PS|P.S|POLICE STATION|POLICE)\\W+", ",");
		a = "PO&PS asdasd";
		a= a.replaceAll("\\b(DIST|AT|DISTT|DISTRICT|VILL|VILLAGE|VIL|TEHSIL|PO|P.O|POST|POST OFFICE|PS|P.S|POLICE STATION|POLICE)\\W+", ",");
		a = "P.O LSONDA";
		a= a.replaceAll("\\b(DIST|AT|DISTT|DISTRICT|VILL|VILLAGE|VIL|TEHSIL|PO|P.O|POST|POST OFFICE|PS|P.S|POLICE STATION|POLICE)\\W+", ",");
		a = "P.O. LSONDA";
		a= a.replaceAll("\\b(DIST|AT|DISTT|DISTRICT|VILL|VILLAGE|VIL|TEHSIL|PO|P.O|POST|POST OFFICE|PS|P.S|POLICE STATION|POLICE)\\W+", ",");
		a = "P.O.LSONDA";
		a= a.replaceAll("\\b(DIST|AT|DISTT|DISTRICT|VILL|VILLAGE|VIL|TEHSIL|PO|P.O|POST|POST OFFICE|PS|P.S|POLICE STATION|POLICE)\\W+", ",");

		a= "@#a";
		a = a.replaceAll("^\\W+", "");
		a = "a12@#";
		a = a.replaceAll("\\W+$", "");

		a = "VILL & P.O. BIBHISANPUR";
		a= a.replaceAll("\\b(([PA]\\.{0,1}[OST])|(VILL|VIL|VILLAGE))\\.{0,1}\\W*\\&\\W*(([PA]\\.{0,1}[OST])|(VILL|VIL|VILLAGE))\\.{0,1}\\W+", ",");

//		a= a.replaceAll("(\\bP\\.{0,1}O\\.{0,1}\\b)(\\W*\\&\\W*)(\\bPS\\b)\\W*", ",");
		//a= a.replaceAll("\\bP\\.{0,1}O\\.{0,1}\\W*\\&\\W*P\\.{0,1}S\\.{0,1}\\W+", ",");
		a = "PO&PS asd";
		a= a.replaceAll("\\b[PA]\\.{0,1}[OST]\\.{0,1}\\W*\\&\\W*[PA]\\.{0,1}[OST]\\.{0,1}\\W+", ",");
		
		a = "P.O &PS asd";
		a= a.replaceAll("\\b[PA]\\.{0,1}[OST]\\.{0,1}\\W*\\&\\W*[PA]\\.{0,1}[OST]\\.{0,1}\\W+", ",");
		a = "P.O. &PS asd";
		a= a.replaceAll("\\b[PA]\\.{0,1}[OST]\\.{0,1}\\W*\\&\\W*[PA]\\.{0,1}[OST]\\.{0,1}\\W+", ",");
		a = "P.O.&PS asd";
		a= a.replaceAll("\\b[PA]\\.{0,1}[OST]\\.{0,1}\\W*\\&\\W*[PA]\\.{0,1}[OST]\\.{0,1}\\W+", ",");
		a = "P.O.&P.S.asd";
		a= a.replaceAll("\\b[PA]\\.{0,1}[OST]\\.{0,1}\\W*\\&\\W*[PA]\\.{0,1}[OST]\\.{0,1}\\W+", ",");
		a = "P.O.&P.S. asd";
		a= a.replaceAll("\\b[PA]\\.{0,1}[OST]\\.{0,1}\\W*\\&\\W*[PA]\\.{0,1}[OST]\\.{0,1}\\W+", ",");
		a = "P.O.&P.S asd";
		a= a.replaceAll("\\b[PA]\\.{0,1}[OST]\\.{0,1}\\W*\\&\\W*[PA]\\.{0,1}[OST]\\.{0,1}\\W+", ",");
		a = "P.O.&P.S: asd";
		a= a.replaceAll("\\b[PA]\\.{0,1}[OST]\\.{0,1}\\W*\\&\\W*[PA]\\.{0,1}[OST]\\.{0,1}\\W+", ",");
		a = "P.O.&P.S :asd";
		a= a.replaceAll("\\b[PA]\\.{0,1}[OST]\\.{0,1}\\W*\\&\\W*[PA]\\.{0,1}[OST]\\.{0,1}\\W+", ",");
		a = "P.O.&P.S:asd";
		a= a.replaceAll("\\b[PA]\\.{0,1}[OST]\\.{0,1}\\W*\\&\\W*[PA]\\.{0,1}[OST]\\.{0,1}\\W+", ",");
		a = "P.O.&P.S : asd";
		a= a.replaceAll("\\b[PA]\\.{0,1}[OST]\\.{0,1}\\W*\\&\\W*[PA]\\.{0,1}[OST]\\.{0,1}\\W+", ",");

		a = "DELIVERY AT ONDA";
		//a=a.replaceAll("\\bDELIVERY\\b(AT\\b)*"," DELIVERY ");
		a=a.replaceAll("\\bDELIVERY\\b\\W+(\\bAT\\b)*\\W*","DELIVERY ");
		System.out.println(a);
		a = "DELIVERY AT: ONDA";
		a=a.replaceAll("\\bDELIVERY\\b\\W+(\\bAT\\b)*\\W*","DELIVERY ");
		a = "DELIVERY : ONDA";
		a=a.replaceAll("\\bDELIVERY\\b\\W+(\\bAT\\b)*\\W*","DELIVERY ");
		a = "DELIVERY- ONDA";
		a=a.replaceAll("\\bDELIVERY\\b\\W+(\\bAT\\b)*\\W*","DELIVERY ");
		a = "DELIVERY -ONDA";
		a=a.replaceAll("\\bDELIVERY\\b\\W+(\\bAT\\b)*\\W*"," DELIVERY ");
		a = "DELIVERY ONDA";
		a=a.replaceAll("\\bDELIVERY\\b\\W+(\\bAT\\b)*\\W*"," DELIVERY ");
		a = "DELIVERY-ONDA";
		a=a.replaceAll("\\bDELIVERY\\b\\W+(\\bAT\\b)*\\W*"," DELIVERY ");
		
		
		System.out.println(a);
	}
	public String getCustName() {
		return custName;
	}
	public String getCustName(int len) {
		return custName == null || custName.length() <= len ? custName : custName.substring(0,len);
	}
	public void setCustName(String custName) {
		this.custName = cleanupName(custName);
	}
	public String getLine(int idx) {
		return line[idx];
	}
	public String getLine(int idx, int len) {
		return line[idx] == null || line[idx].length() <= len ? line[idx] : line[idx].substring(0, len);
	}
	public void setLine(String line, int idx) {
		this.line[idx] = cleanupName(line);
		for (int i=0,is=this.line == null || city == null ? 0 : this.line.length;i<is;i++) {
			if (this.line[i] != null && this.line[i].toUpperCase().indexOf(city.toUpperCase()) >= 0) {
				city = null;
				break;
			}
		}
	}
	public String getCity() {
		return city;
	}
	public String getCity(int len) {
		return city == null || city.length() < len ? city : city.substring(0, len);
	}
	public void setCity(String city) {
		this.city = cleanupName(city);
		for (int i=0,is=this.line == null || city == null ? 0 : line.length;i<is;i++) {
			if (line[i] != null && line[i].toUpperCase().indexOf(city.toUpperCase()) >= 0) {
				city = null;
				break;
			}
		}
	}
	public String getDistrict() {
		return district;
	}
	public String getDistrict(int len) {
		return district == null || district.length() <= len ? district  : district.substring(0, len);
	}
	public void setDistrict(String district) {
		this.district = cleanupName(district);
	}
	public String getState() {
		return state;
	}
	public String getState(int len) {
		return state == null || state.length() <= len ? state : state.substring(0, len);
	}
	public void setState(String state) {
		this.state = Utils.get2digitStateCode(cleanupName(state));
	}
	public String getAddressItemCode() {
		return addressItemCode;
	}
	public String getAddressItemCode(int len) {
		return addressItemCode == null || addressItemCode.length() <= len ? addressItemCode : addressItemCode.substring(0, len);
	}
	public void setAddressItemCode(String addressItemCode) {
		this.addressItemCode = cleanupName(addressItemCode);
	}
	public int getLineCount() {
		return line.length;
	}
	/*
	
	public void guessTehsilEtc(String toLookAt) {
		if (toLookAt == null)
			return;
		Pattern[] patterns = {
//							Pattern.compile("(\\b)(P.O.|P.O|P.S.|P.S)(\\s{0,}&\\s{0,})(P.S.|P.S|P.O.|P.O)(\\b)(\\W*)(\\w+)"),
//							Pattern.compile("(\\b)(VILL.|VILL.|VILLAGE|VILL|VIL)(\\s{0,}&\\s{0,})(P.O.|P.O)(\\b)(\\W*)(\\w+)"),
							
				 			Pattern.compile("(\\b)(DIST|DISTT|DISTRICT)(?!.*&.*)(\\b)(\\W*)(\\w+)"),
				 			Pattern.compile("(\\b)(VILL|VILLAGE|VIL)(?!.*&.*)(\\b)(\\W*)(\\w+)"),
							Pattern.compile("(\\b)(TEHSIL)(?!.*&.*)(\\W+)(\\b)(\\W*)(\\w+)"),
							Pattern.compile("(\\b)(PO|P.O|POST|POST OFFICE)(?!.*&.*)(\\b)(\\W*)(\\w+)"),
							Pattern.compile("(\\b)(PS|P.S|POLICE STATION|POLICE)(?!.*&.*)(\\b)(\\W*)(\\w+)")
		}
		;
		int multiCount = 0;
		for (int art=0,arts = patterns.length;art<arts;art++) {
			Pattern pattern = patterns[art];
			Matcher matcher = pattern.matcher(toLookAt);
			if (matcher.find()) {
				String s = matcher.group(matcher.groupCount());
				if (multiCount > 0 && art == 0) {
					this.setPoliceStation(s);
					this.setPostOffice(s);
					art++;
				}
				else if (multiCount > 0 && art == 1) {
					this.setVillage(s);
					this.setPostOffice(s);
					art++;
				}
				else if (art == multiCount+0)
					this.setDistrict(s);
				else if (art == multiCount+1)
					this.setVillage(s);
				else if (art == multiCount+2)
					this.setTehsil(s);
				else if (art == multiCount+3)
					this.setPostOffice(s);
				else if (art == multiCount+4)
					this.setPoliceStation(s);
				toLookAt = toLookAt.replaceFirst(pattern.toString(),"");
			}//found a match
		}
	}
	public void guessCityStateEtc(String toLookAt) {
		if (toLookAt == null)
			return;
		if (this.getCity() == null || this.getState() == null) {
			//TODO DEBUG13 generalize
			String [] parts = toLookAt.split(",");
			for (int i=parts == null ? 0 : parts.length-1;i>=0; i--) {
				String part = parts[i];
				if (part == null || "NULL".equalsIgnoreCase(part))
					continue;
				String stName = Utils.get2digitStateCode(part);
				if (stName != null) {
					if (getState() == null)
						this.setState(stName);
				}
				else {
					if (getCity() == null)
						this.setCity(part);
				}
			}
		}
		Pattern[] patterns = {
//							Pattern.compile("(\\b)(P.O.|P.O|P.S.|P.S)(\\s{0,}&\\s{0,})(P.S.|P.S|P.O.|P.O)(\\b)(\\W*)(\\w+)"),
//							Pattern.compile("(\\b)(VILL.|VILL.|VILLAGE|VILL|VIL)(\\s{0,}&\\s{0,})(P.O.|P.O)(\\b)(\\W*)(\\w+)"),
							
				 			Pattern.compile("(\\b)(DIST|DISTT|DISTRICT)(?!.*&.*)(\\b)(\\W*)(\\w+)"),
				 			Pattern.compile("(\\b)(VILL|VILLAGE|VIL)(?!.*&.*)(\\b)(\\W*)(\\w+)"),
							Pattern.compile("(\\b)(TEHSIL)(?!.*&.*)(\\W+)(\\b)(\\W*)(\\w+)"),
							Pattern.compile("(\\b)(PO|P.O|POST|POST OFFICE)(?!.*&.*)(\\b)(\\W*)(\\w+)"),
							Pattern.compile("(\\b)(PS|P.S|POLICE STATION|POLICE)(?!.*&.*)(\\b)(\\W*)(\\w+)")
		}
		;
		int multiCount = 0;
		for (int art=0,arts = patterns.length;art<arts;art++) {
			Pattern pattern = patterns[art];
			Matcher matcher = pattern.matcher(toLookAt);
			if (matcher.find()) {
				String s = matcher.group(matcher.groupCount());
				if (multiCount > 0 && art == 0) {
					this.setPoliceStation(s);
					this.setPostOffice(s);
					art++;
				}
				else if (multiCount > 0 && art == 1) {
					this.setVillage(s);
					this.setPostOffice(s);
					art++;
				}
				else if (art == multiCount+0)
					this.setDistrict(s);
				else if (art == multiCount+1)
					this.setVillage(s);
				else if (art == multiCount+2)
					this.setTehsil(s);
				else if (art == multiCount+3)
					this.setPostOffice(s);
				else if (art == multiCount+4)
					this.setPoliceStation(s);
				toLookAt = toLookAt.replaceFirst(pattern.toString(),"");
			}//found a match
		}
	}
*/
}
