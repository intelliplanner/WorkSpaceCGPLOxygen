package com.ipssi.dispatchoptimization.vo;

import java.util.ArrayList;

import com.ipssi.RegionTest.RegionTest;

public class SiteVO {
	// private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	// private NewMU ownerMU = null;

	private int id;
	private String name;
	private int status;
	private double difficulty;
	private double lon;
	private double lat;
	private double lowRadius;
	private RegionTest.RegionTestHelper region;
	private double minQty;
	private double maxQty;
	private int materialId;
	private int pitId;
	private long startFrom;
	private long endTill;
	private double avgCycleTime;
	private double positioningTime;
	private double clearingTime;
	private double clearingCycleTime;
	private ArrayList<Integer> notAllowedDumperTypes = new ArrayList<Integer>();
	private ArrayList<Integer> notAllowedLoaderTypes = new ArrayList<Integer>();
	private int totTripsInShift;
	private double totTonnesInShift;
	private long latestProcessedAt = -1;

	public boolean equals(SiteVO s) {
		return s != null && s.id == this.id;
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public double getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(double difficulty) {
		this.difficulty = difficulty;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLowRadius() {
		return lowRadius;
	}

	public void setLowRadius(double lowRadius) {
		this.lowRadius = lowRadius;
	}

	public RegionTest.RegionTestHelper getRegion() {
		return region;
	}

	public void setRegion(RegionTest.RegionTestHelper region) {
		this.region = region;
	}

	public double getMinQty() {
		return minQty;
	}

	public void setMinQty(double minQty) {
		this.minQty = minQty;
	}

	public double getMaxQty() {
		return maxQty;
	}

	public void setMaxQty(double maxQty) {
		this.maxQty = maxQty;
	}

	public int getMaterialId() {
		return materialId;
	}

	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}

	public int getPitId() {
		return pitId;
	}

	public void setPitId(int pitId) {
		this.pitId = pitId;
	}

	public long getStartFrom() {
		return startFrom;
	}

	public void setStartFrom(long startFrom) {
		this.startFrom = startFrom;
	}

	public long getEndTill() {
		return endTill;
	}

	public void setEndTill(long endTill) {
		this.endTill = endTill;
	}

	public ArrayList<Integer> getNotAllowedDumperTypes() {
		return notAllowedDumperTypes;
	}

	public void setNotAllowedDumperTypes(
			ArrayList<Integer> notAllowedDumperTypes) {
		this.notAllowedDumperTypes = notAllowedDumperTypes;
	}

	public ArrayList<Integer> getNotAllowedLoaderTypes() {
		return notAllowedLoaderTypes;
	}

	public void setNotAllowedLoaderTypes(
			ArrayList<Integer> notAllowedLoaderTypes) {
		this.notAllowedLoaderTypes = notAllowedLoaderTypes;
	}

	public int getTotTripsInShift() {
		return totTripsInShift;
	}

	public void setTotTripsInShift(int totTripsInShift) {
		this.totTripsInShift = totTripsInShift;
	}

	public double getTotTonnesInShift() {
		return totTonnesInShift;
	}

	public void setTotTonnesInShift(double totTonnesInShift) {
		this.totTonnesInShift = totTonnesInShift;
	}

	public long getLatestProcessedAt() {
		return latestProcessedAt;
	}

	public void setLatestProcessedAt(long latestProcessedAt) {
		this.latestProcessedAt = latestProcessedAt;
	}

	public double getAvgCycleTime() {
		return avgCycleTime;
	}

	public void setAvgCycleTime(double avgCycleTime) {
		this.avgCycleTime = avgCycleTime;
	}

	public double getPositioningTime() {
		return positioningTime;
	}

	public void setPositioningTime(double positioningTime) {
		this.positioningTime = positioningTime;
	}

	public double getClearingTime() {
		return clearingTime;
	}

	public void setClearingTime(double clearingTime) {
		this.clearingTime = clearingTime;
	}

	public double getClearingCycleTime() {
		return clearingCycleTime;
	}

	public void setClearingCycleTime(double clearingCycleTime) {
		this.clearingCycleTime = clearingCycleTime;
	}

}
