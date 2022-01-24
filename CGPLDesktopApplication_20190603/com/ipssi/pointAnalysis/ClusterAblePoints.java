package com.ipssi.pointAnalysis;

import org.apache.commons.math3.ml.clustering.Clusterable;

import com.ipssi.gen.utils.Misc;

public class ClusterAblePoints implements Clusterable {
	public double[] pt = null;
	public int id = Misc.getUndefInt();
	public ClusterAblePoints(double lon, double lat, int id) {
		pt = new double[2];
		pt[0] = lon;
		pt[1] = lat;
		this.id = id;
	}
	
	@Override
	public double[] getPoint() {
		// TODO Auto-generated method stub
		return null;
	}

}
