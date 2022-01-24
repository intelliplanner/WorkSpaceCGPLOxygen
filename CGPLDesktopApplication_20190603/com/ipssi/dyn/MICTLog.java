package com.ipssi.dyn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ipssi.gen.utils.DBConnectionPool;

public class MICTLog {
	/*
	private static ArrayList<String> getPatternMatching(String line, String[] patternList, boolean stripFirst, boolean stripLast, boolean stripOfTruckPattern) {
		ArrayList<String> retval = new ArrayList<String>();
		for (int i=0,is=patternList.length; i<is; i++) {
			Pattern pattern = Pattern.compile(patternList[i]);
			Matcher matcher = pattern.matcher(line);
			while (matcher.find()) {
				String g = matcher.group();
				String contName =g.substring(stripFirst ? 1 : 0, g.length()-(stripLast ? 1 : 0));
				if (stripOfTruckPattern)
					contName = stripOffForTruckPattern(contName);
				if (!retval.contains(contName))
					retval.add(contName);
			}	
		}
		return retval;
	}

	private static String g_containerPattern[] = {
			"[^A-Z0-9][A-Z]{4}[0-9]{7}[^A-Z0-9]"
	};
	private static String g_vehiclePattern[] = {
			"[^A-Z0-9](H|L|M|S|Y)[0-9]{1,3}[^A-Z0-9]" //L1?? H??, S??
			,"[^A-Z0-9][L][D][0-9]{1,3}[^A-Z0-9]" //LD
			,"[^A-Z0-9][S][P][0-9]{1,3}[^A-Z0-9]" //SP
	};
	//2E39F2
	private static String g_locationPattern[] = {
			"[^A-Z0-9][0-9][A-Z][0-9]{2}[A-Z][0-9][^A-Z0-9]" //2E39F2
			,"[^A-Z0-9][Q][C][0-9]{1,2}[^A-Z0-9]" //QC??
	};
	private static String g_truckPattern[] = {
	//		"[^A-Z0-9]truck\\sID=\\K[0-9]+[^A-Z0-9]" //truck ID=number
	//		,"[^A-Z0-9]Trk\\.Lic\\.=\\K[A-Z0-9]+[^A-Z0-9]"
	//		,"[^A-Z0-9]BAT=\\K[A-Z0-9]+[^A-Z0-9]"
		//it seems look behind \K doesnt work
			    "[^A-Z0-9]truck\\sID=\\w*[0-9]+[^A-Z0-9]" //truck ID=number
				,"[^A-Z0-9]Trk\\.Lic\\.=\\w*[A-Z0-9]+[^A-Z0-9]"
				,"[^A-Z0-9]BAT=\\w*[A-Z0-9]+[^A-Z0-9]"

	};
	private static String g_chePatternFrom[] = {
				    "[^A-Z0-9]fromCHE=[A-Z]+[0-9]+[^A-Z0-9]" //truck ID=number
		};
	private static String g_chePatternTo[] = {
				    "[^A-Z0-9]toCHE=[A-Z]+[0-9]+[^A-Z0-9]" //truck ID=number
		};
	private static String g_chePatternDsptch[] = {
				    "[^A-Z0-9]CHE-Dsptch=[A-Z]+[0-9]+[^A-Z0-9]" //truck ID=number
		};
	private static String g_poWork[] = {
	    "[^A-Z0-9]pow=[A-Z]+[0-9]+[^A-Z0-9]" //truck ID=number
	};

	private static void stripOffForTruckPattern(ArrayList<String> res) {
		for (int i=0,is=res.size(); i<is ;i++) {
			res.set(i, stripOffForTruckPattern(res.get(i)));
		}
	}

	private static String stripOffForTruckPattern(String s) {
			int posEq = s.indexOf('=');
			s = s.substring(posEq+1);
			s = s.trim();
			return s;
	}

	public static void main(String[] args) {
		Connection conn = null;
		boolean destroyIt = false;
//		ArrayList<String> temp = possibleContainers("2012: 9: 5");
//		temp = possibleContainers("20120905_122007 <   > Cannot deck MEDU6105892: no allocation group");
//		if (true)
//			return;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			String fileNames[] = {"d:\\temp\\Logs\\Log.20120905_122007.txt","d:\\temp\\Logs\\Log.20120905_130804.txt","d:\\temp\\Logs\\Log.20120905_132627.txt","d:\\temp\\Logs\\Log.20120905_141332.txt","d:\\temp\\Logs\\Log.20120905_150825.txt","d:\\temp\\Logs\\Log.20120905_155052.txt","d:\\temp\\Logs\\Log.20120905_170321.txt"};
			PreparedStatement delPS = conn.prepareStatement("truncate mict_log");
			delPS.execute();
			delPS.close();
			PreparedStatement ps= conn.prepareStatement("insert into mict_log(filename, line, row, container1, container2, container3, location1, location2, location3, veh1, veh2, veh3, lic1, lic2, lic3, fromCHE, toCHE, CHE_DISPTCH,powork) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			int row = 0;
			for (int i=0,is=fileNames.length;i<is;i++) {
				String fileName = fileNames[i];
				File f = new File(fileName);
				FileReader in = null;
				BufferedReader br = null;
				try {
					in = new FileReader(f);
					
					br = new BufferedReader(in);
					String s = null;
					
					ps.setString(1, fileName);
					while ((s = br.readLine()) != null) {
						ArrayList<String> containers = getPatternMatching(s, g_containerPattern, true, true, false);
						ArrayList<String> vehicles = getPatternMatching(s, g_vehiclePattern, true, true, false);
						ArrayList<String> locations = getPatternMatching(s, g_locationPattern, true, true, false);
						ArrayList<String> trucks = getPatternMatching(s, g_truckPattern, true, true, true);
						ArrayList<String> fromCHE = getPatternMatching(s, MICTLog.g_chePatternFrom, true, true, true);
						ArrayList<String> toCHE = getPatternMatching(s, MICTLog.g_chePatternTo, true, true, true);
						ArrayList<String> CHE_dsptch = getPatternMatching(s, MICTLog.g_chePatternDsptch, true, true, true);
						ArrayList<String> pow = getPatternMatching(s, MICTLog.g_poWork, true, true, true);
						
						int colIndex = 2;
						ps.setString(colIndex++, s);
						ps.setInt(colIndex++, row++);
						for (int art=0;art<4;art++) {
							ArrayList<String> items = art == 0 ? containers : art == 1 ? locations : art == 2 ? vehicles : trucks;
							
							for (int j=0;j<3;j++) {
								ps.setString(colIndex++, j < items.size() ? items.get(j) : null);
							}
						}
						for (int art=0;art<4;art++) {
							ArrayList<String> items = art == 0 ? fromCHE : art == 1 ? toCHE : art == 2 ? CHE_dsptch : pow;
							
							for (int j=0;j<1;j++) {
								ps.setString(colIndex++, j < items.size() ? items.get(j) : null);
							}
						}
						ps.addBatch();
						if (row % 10000 == 0) {
							ps.executeBatch();
							conn.commit();
						}
					}
					ps.executeBatch();
					conn.commit();
					
					br.close();
					br = null;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					if (br != null)
						br.close();
				}
				
			}
			ps.close();
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
				catch (Exception e2) {
					
				}
			}
		}
	}
	*/
}
