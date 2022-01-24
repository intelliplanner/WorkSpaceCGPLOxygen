package com.ipssi.common.ds.trip;

import java.util.ArrayList;

import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;

public class OpMapping {
	private ArrayList<Triple<Integer, ArrayList<Integer>, ArrayList<Integer>>> mappings = new ArrayList<Triple<Integer, ArrayList<Integer>, ArrayList<Integer>>>();
	//first = type, second list of fixed opstations, third = list of moving opstations
	public void calculate(ArrayList<Integer> opProfileIds) {
		ArrayList<Triple<Integer, ArrayList<Integer>, ArrayList<Integer>>> mappings = new ArrayList<Triple<Integer, ArrayList<Integer>, ArrayList<Integer>>>();
		for (Integer profileId : opProfileIds) {
			ArrayList<Pair<Integer, Integer>> mapping = NewProfileCache.getRawMappingsForProfileId(profileId);
			if (mapping != null) {
				for (Pair<Integer, Integer> pr : mapping) {
					add(mappings, pr.second, pr.first);
				}
			}
		}
		this.mappings = mappings;
	}
	public int getTypeFor(int opstationId) {
		for (Triple<Integer, ArrayList<Integer>, ArrayList<Integer>> entry : mappings) {
			for (int art=0;art<2;art++) {
				ArrayList<Integer> beanList = art == 0 ? entry.second : entry.third;
				for (int j=0, js = beanList == null ? 0 : beanList.size();j<js;j++) {
					if (beanList.get(j) == opstationId)
						return entry.first;
				}
			}
		}
		return -1;
	}
	public boolean isMovingOpExists(int opstationId) {
		for (Triple<Integer, ArrayList<Integer>, ArrayList<Integer>> entry : mappings) {
			if (entry.third != null) {
				for (Integer op : entry.third)
					if (op.intValue() == opstationId)
						return true;
			}
		}
		return false;
	}
	
	public ArrayList<Integer> getOpListForType(int type, int fixedOrMovingType) {
		ArrayList<Integer> addToThis = null;
		boolean toCopyAndCreate = false;
		for (Triple<Integer, ArrayList<Integer>, ArrayList<Integer>> entry : mappings) {
			if (entry.first == type) {
				if (fixedOrMovingType != 1) {
					addToThis = entry.second;
				}
				if (fixedOrMovingType != 0 && entry.third != null) {
					if (addToThis != null && addToThis.size() != 0) {						
						ArrayList<Integer> temp = new ArrayList<Integer>();
						for (Integer op : addToThis)
							temp.add(op);
						for (Integer op : entry.third)
							temp.add(op);
						addToThis = temp;
					}
					else {
						addToThis = entry.third;
					}
				}
				break;
			}
		}
		return addToThis;
	}
	
	public void add(ArrayList<Triple<Integer, ArrayList<Integer>, ArrayList<Integer>>> mappings, int type, int opId) {//will add in sorted manner
		OpStationBean bean = TripInfoCacheHelper.getOpStation(opId);
		if (bean != null)
			add(mappings, type, bean);
	}
	
	public void add(ArrayList<Triple<Integer, ArrayList<Integer>, ArrayList<Integer>>> mappings, int type, OpStationBean bean) {//will add in sorted manner
		Triple<Integer, ArrayList<Integer>, ArrayList<Integer>> addtoThis = null;
		boolean fixed = bean.getLinkedVehicleId() < 0;
		for (Triple<Integer, ArrayList<Integer>, ArrayList<Integer>> entry : mappings) {
			if (entry.first == type) {
				addtoThis = entry;
				break;
			}
		}
		if (addtoThis == null) {
			addtoThis = new Triple<Integer, ArrayList<Integer>, ArrayList<Integer>> (type, null, null);
			mappings.add(addtoThis);
		}
		ArrayList<Integer> arrayToAdd = fixed ? addtoThis.second : addtoThis.third;
		if (arrayToAdd == null) {
			arrayToAdd = new ArrayList<Integer>();
			if (fixed)
				addtoThis.second = arrayToAdd;
			else
				addtoThis.third = arrayToAdd;
		}
		arrayToAdd.add(bean.getOpStationId());
	}
}
