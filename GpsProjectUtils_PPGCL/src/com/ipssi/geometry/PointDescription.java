/**
 * 
 */
package com.ipssi.geometry;

/**
 * @author jai
 * 
 */
public class PointDescription extends Point {
	private String name;

	public PointDescription(double x, double y, String name) {
		this.name = name;
		this.setX(x);
		this.setY(y);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
