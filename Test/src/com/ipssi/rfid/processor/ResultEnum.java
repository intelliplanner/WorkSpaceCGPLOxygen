package com.ipssi.rfid.processor;

public enum ResultEnum {
	GREEN(0), YELLOW(1), RED(2);

	private int ordinal;

	ResultEnum(int position) {
		this.ordinal = position;
	}

	public int getOrdinal() {
		return this.ordinal;
	}

}
