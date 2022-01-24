package com.ipssi.processor.utils;

import java.io.IOException;
import java.io.Serializable;

import com.ipssi.gen.utils.Misc;

public class DimensionInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4484182423071562932L;
	private Dimension dimension;
	private double value = Misc.getUndefDouble();

	public DimensionInfo() {
	}
	
	public DimensionInfo(int dimId, double value) {
		dimension = Dimension.getDimInfo(dimId);
		this.value = value;		
	}
	public DimensionInfo(Dimension dimension, double value) {
		this.dimension = dimension;
		this.value = value;		
	}

	public DimensionInfo(DimensionInfo dimensionInfo) {
		this.dimension = dimensionInfo.dimension;
		this.value = dimensionInfo.value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj instanceof DimensionInfo) {
			return ((DimensionInfo) obj).dimension.equals(this.dimension);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.dimension.getId();
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * @return the dimension
	 */
	public Dimension getDimension() {
		return dimension;
	}

	/**
	 * @param dimension
	 *            the dimension to set
	 */
	public void setDimension(Dimension dimension) {
		this.dimension = dimension;
	}

	@Override
	public String toString() {
		return this.dimension.toString() + " value = " + this.value;
	}
	
}
