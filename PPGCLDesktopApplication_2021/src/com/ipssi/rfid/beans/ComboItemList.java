/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.beans;

/**
 *
 * @author Vi$ky
 */
public class ComboItemList {

	int value;
	String label;
	String address;

	public ComboItemList(int value, String label, String address) {
		this.value = value;
		this.label = label;

		this.address = address;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return label;
	}

}
