package com.ipssi.jrm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;

public class JRMList {
	private HashMap<String, JRMPoint> jrmListHashMap;
	private ArrayList<JRMPoint> jrmList;
	public static class JRMPoint {
		private int id;
		private String jrmId;
		private String jrmName;
		private String type;
		private String riskLevel;
		private Pair<String, String> startPos;
		private Pair<String, String> endPos;
		private Pair<String, String> startTime;
		private Pair<String, String> endTime;
		private com.vividsolutions.jts.geom.Point[] points;
		
		public JRMPoint(String jrmId, String jrmName, String type, String riskLevel, 
				String startLong, String startLat, String endLong, String endLat, 
				String startHr, String startMin, String endHr, String endMin, com.vividsolutions.jts.geom.Point[] points) {
			this.setId(Misc.getParamAsInt(jrmId));
			this.setJrmId(jrmId);
			this.setJrmName(jrmName);
			this.setType(type);
			this.setRiskLevel(riskLevel);
			this.setStartPos(new Pair<String, String>(startLong, startLat));
			this.setEndPos(new Pair<String, String>(endLong, endLat));
			this.setStartTime(new Pair<String, String> (startHr, startMin));
			this.setEndTime(new Pair<String, String> (endHr, endMin));
			this.setPoints(points);
		}
		
		public JRMPoint(int id, String jrmId, String type, String riskLevel, 
				String startLong, String startLat, String endLong, String endLat, 
				String startHr, String startMin, String endHr, String endMin, com.vividsolutions.jts.geom.Point[] points) {
			this.setId(id);
			this.setJrmId(jrmId);
			this.setType(type);
			this.setRiskLevel(riskLevel);
			this.setStartPos(new Pair<String, String>(startLong, startLat));
			this.setEndPos(new Pair<String, String>(endLong, endLat));
			this.setStartTime(new Pair<String, String> (startHr, startMin));
			this.setEndTime(new Pair<String, String> (endHr, endMin));
			this.setPoints(points);
		}

		public String toString () {
			String s = new String ("No Data");
			return s;
		}
		public int hashCode() {
			return id;
		}
		public boolean equals(Object o) {
			if (o instanceof JRMPoint && ((JRMPoint)o).getJrmId().equals(this.getJrmId()))
			{
				return true;
			}
			return false;
		}
		
		public void setId(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	
		public void setJrmId(String jrmId2) {
			this.jrmId = jrmId2;
		}

		public String getJrmId() {
			return jrmId;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}

		public void setRiskLevel(String riskLevel) {
			this.riskLevel = riskLevel;
		}

		public String getRiskLevel() {
			return riskLevel;
		}

		public void setStartPos(Pair<String, String> pair) {
			this.startPos = pair;
		}

		public Pair<String, String> getStartPos() {
			return startPos;
		}

		public void setEndPos(Pair<String, String> pair) {
			this.endPos = pair;
		}

		public Pair<String, String> getEndPos() {
			return endPos;
		}

		public void setStartTime(Pair<String, String> pair) {
			this.startTime = pair;
		}

		public Pair<String, String> getStartTime() {
			return startTime;
		}

		public void setEndTime(Pair<String, String> pair) {
			this.endTime = pair;
		}

		public Pair<String, String> getEndTime() {
			return endTime;
		}

		public void setPoints(com.vividsolutions.jts.geom.Point[] points) {
			this.points = points;
		}

		public com.vividsolutions.jts.geom.Point[] getPoints() {
			return points;
		}

		public void setJrmName(String jrmName) {
			this.jrmName = jrmName;
		}

		public String getJrmName() {
			return jrmName;
		}
	}
	
	public JRMList () {
		jrmList = new ArrayList<JRMPoint>();
		this.jrmListHashMap = new HashMap<String, JRMPoint> ();
	}
	
	public boolean contains(JRMPoint pt) {
		return jrmListHashMap.containsKey(pt.getJrmId());
	}
	
	public ArrayList<JRMPoint> getJrmList() {
		return jrmList;
	}
	
	public void addJRMPoint (JRMPoint jPoint) {
		if (!contains(jPoint)) {
			jrmListHashMap.put(jPoint.getJrmId(), jPoint);
			jrmList.add(jPoint);
		}
	}
	
	public void addJRMPoint (int id, String jrmId, String type, String riskLevel, 
			String startLat, String startLong, String endLat, String endLong, 
			String startHr, String startMin, String endHr, String endMin, com.vividsolutions.jts.geom.Point[] points) {
		JRMPoint jPoint = new JRMPoint(id, jrmId, type, riskLevel, startLat, startLong, 
										endLat, endLong, startHr, startMin, endHr, endMin, points);
		addJRMPoint(jPoint);
	}
	
	public void addJRMPoint (String jrmId, String name, String type, String riskLevel, 
			String startLat, String startLong, String endLat, String endLong, 
			String startHr, String startMin, String endHr, String endMin, com.vividsolutions.jts.geom.Point[] points) {
		JRMPoint jPoint = new JRMPoint(jrmId, name, type, riskLevel, startLat, startLong, 
										endLat, endLong, startHr, startMin, endHr, endMin, points);
		addJRMPoint(jPoint);
	}
	
	public int getListCount () {
		return jrmList.size();
	}
    
	public static String toString(ArrayList<JRMPoint> ptList, boolean onlyId, boolean ignUseLess) {
		StringBuilder sb = new StringBuilder ();
		Iterator<JRMPoint> it = ptList.iterator();
		while (it.hasNext()) {
			JRMPoint itPoint = it.next();
			if (!ignUseLess) {
				sb.append("0000");
				sb.append(itPoint.getId());
			}
			
			sb.append(",");
			sb.append(itPoint.getJrmId());
			if (!onlyId) {
				sb.append(",");
				if (!ignUseLess)
					sb.append(itPoint.getJrmName());
				sb.append(",");
				sb.append(itPoint.getStartPos().first);
				sb.append(",");
				sb.append(itPoint.getStartPos().second);
				sb.append(",");
				sb.append(itPoint.getEndPos().first);
				sb.append(",");
				sb.append(itPoint.getEndPos().second);
				sb.append(",");
				sb.append(itPoint.getStartTime().first);
				sb.append(":");
				sb.append(itPoint.getStartTime().second);
				sb.append(",");
				sb.append(itPoint.getEndTime().first);
				sb.append(":");
				sb.append(itPoint.getEndTime().second);
				sb.append(",");
				sb.append(itPoint.getType());
				sb.append(",");
				sb.append(itPoint.getRiskLevel());
			}
			sb.append("\r\n");
		}
		return sb.toString();
	}
	public String toString() {
		return toString(jrmList, false, false);		
	}
	
	public void clear() {
		jrmList.clear();
		jrmListHashMap.clear();
	}
	public boolean isEmpty() {
		return jrmList.isEmpty();
	}
	public int size() {
		return jrmList.size();
	}
	public Pair<Integer, ArrayList<JRMPoint>> getMeMinusRHS (JRMList rhsList, int lastSentIncl, int maxToGet) {
		ArrayList<JRMPoint> deltaList = new ArrayList<JRMPoint> ();
		for (int i=lastSentIncl+1,is=this.jrmList.size();i<is;i++) {
			JRMPoint itPoint = jrmList.get(i);
			if (rhsList == null || !rhsList.contains(itPoint)) {
				deltaList.add(itPoint);
				if (deltaList.size() >= maxToGet) {
					return new Pair<Integer, ArrayList<JRMPoint>> (i, deltaList);
				}
			} 
		}
		return new Pair<Integer, ArrayList<JRMPoint>> (this.jrmList.size()-1, deltaList);
	}
	
}
