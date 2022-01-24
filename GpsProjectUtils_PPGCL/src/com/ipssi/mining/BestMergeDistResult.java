package com.ipssi.mining;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;

public class BestMergeDistResult {
	public int bestIndex = Misc.getUndefInt(); //internally index ... but when returning art opstation id
	public int ptIndexBefore = Misc.getUndefInt();
	public int support = 0;
	public int deltaLonToNearest = Misc.getUndefInt();
	public int deltaLatToNearest = Misc.getUndefInt();
	public long tsGap = Misc.getUndefInt();
	public boolean isWithingRange = false;
	public boolean refPtIsAfter = false;
	public ArrayList<Integer> movingOpStationIds = null;
	public void reinit() {
		bestIndex = Misc.getUndefInt();
		this.support = 0;
		this.deltaLatToNearest = Misc.getUndefInt();
		this.deltaLonToNearest = Misc.getUndefInt();
		this.tsGap = Misc.getUndefInt();
		this.isWithingRange = false;
		this.refPtIsAfter = false;
		this.ptIndexBefore = Misc.getUndefInt();
		this.movingOpStationIds = null;
	}
	public void copy(BestMergeDistResult rhs) {
		this.bestIndex = rhs.bestIndex;
		this.support = rhs.support;
		this.deltaLatToNearest = rhs.deltaLatToNearest;
		this.deltaLonToNearest = rhs.deltaLonToNearest;
		this.tsGap = rhs.tsGap;
		this.isWithingRange = rhs.isWithingRange;
		this.refPtIsAfter = rhs.refPtIsAfter;
		this.ptIndexBefore = rhs.ptIndexBefore;
		this.movingOpStationIds = rhs.movingOpStationIds;
	}
}
