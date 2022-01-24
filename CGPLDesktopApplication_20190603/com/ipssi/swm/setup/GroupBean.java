package com.ipssi.swm.setup;

import java.util.ArrayList;

public class GroupBean {
	public static class Regions {
		private int id;
		private String name;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Regions(int id, String name) {
			super();
			this.id = id;
			this.name = name;
		}
	}
	public static class RoadSegments {
		private int id;
		private String name;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public RoadSegments(int id, String name) {
			super();
			this.id = id;
			this.name = name;
		}
		
	}
    public static class Station {
    	private int id;
    	private String name;
    	private int seq;
    	private int loadMin;
    	private int timeMinFromPrev;
    	private int timeMinToDump;
    	public Station(int id, String name, int seq) {
    		this.id = id;
    		this.name = name;
    		this.seq = seq;
    	}
    	public static String getStationListAsString(ArrayList<Station> list) {
            	StringBuilder retval = new StringBuilder();
            	for (Station station:list) {
            		if (retval.length() != 0)
            			retval.append(",");
            		retval.append(station.getName());
            	}
            	return retval.toString();
        }
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getSeq() {
			return seq;
		}
		public void setSeq(int seq) {
			this.seq = seq;
		}
		public void setLoadMin(int loadMin) {
			this.loadMin = loadMin;
		}
		public int getLoadMin() {
			return loadMin;
		}
		public int getTimeMinFromPrev() {
			return timeMinFromPrev;
		}
		public void setTimeMinFromPrev(int timeMinFromPrev) {
			this.timeMinFromPrev = timeMinFromPrev;
		}
		public int getTimeMinToDump() {
			return timeMinToDump;
		}
		public void setTimeMinToDump(int timeMinToDump) {
			this.timeMinToDump = timeMinToDump;
		}
    }
    private int id;
    private String name;
    private int portNodeId;
    private int status;
    private String description;
    private int recoVehicleCount;
    private ArrayList<Station> loadStations = new ArrayList<Station>();
    private ArrayList<Station> unloadStations = new ArrayList<Station>();
    private ArrayList<RoadSegments> roadSegments = new ArrayList<RoadSegments>();
    private ArrayList<Regions> regions = new ArrayList<Regions>();
    public GroupBean(int id, String name, int status, String description,  int portNodeId, int recoVehicleCount) {
    	this.id = id;
    	this.name = name;
    	this.portNodeId = portNodeId;
    	this.status = status;
    	this.description = description;
    	this.recoVehicleCount = recoVehicleCount;
    }
    public void addLoadStation(Station station) {
    	loadStations.add(station);
    }
    public void addUnloadStation(Station station) {
    	unloadStations.add(station);
    }
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getRecoVehicleCount() {
		return recoVehicleCount;
	}
	public void setRecoVehicleCount(int recoVehicleCount) {
		this.recoVehicleCount = recoVehicleCount;
	}
	public ArrayList<Station> getLoadStations() {
		return loadStations;
	}
	
	public ArrayList<Station> getUnloadStations() {
		return unloadStations;
	}
	public ArrayList<RoadSegments> getRoadSegments() {
		return roadSegments;
	}
	public void setRoadSegments(ArrayList<RoadSegments> roadSegments) {
		this.roadSegments = roadSegments;
	}
	public ArrayList<Regions> getRegions() {
		return regions;
	}
	public void setRegions(ArrayList<Regions> regions) {
		this.regions = regions;
	}
	public void setLoadStations(ArrayList<Station> loadStations) {
		this.loadStations = loadStations;
	}
	public void setUnloadStations(ArrayList<Station> unloadStations) {
		this.unloadStations = unloadStations;
	}
	
    
}
