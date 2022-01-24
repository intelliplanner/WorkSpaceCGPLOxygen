package com.ipssi.miningOpt;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;

public class Pits {
	private int id;
	private String name;
	private ArrayList<Integer> loadSites = new ArrayList<Integer>();
	private ArrayList<Integer> unloadSites = new ArrayList<Integer>();
	private NewMU ownerMU = null;
	
	public Pits(int id, String name, NewMU ownerMU) {
		this.id = id;
		this.name = name;
		this.ownerMU = ownerMU;
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
	public ArrayList<Integer> getLoadSites() {
		return loadSites;
	}
	public void setLoadSites(ArrayList<Integer> loadSites) {
		this.loadSites = loadSites;
	}
	public ArrayList<Integer> getUnloadSites() {
		return unloadSites;
	}
	public void setUnloadSites(ArrayList<Integer> unloadSites) {
		this.unloadSites = unloadSites;
	}
	public NewMU getOwnerMU() {
		return ownerMU;
	}
	public void setOwnerMU(NewMU ownerMU) {
		this.ownerMU = ownerMU;
	}
	
}
