package com.ipssi.gen.deviceMessaging;

public enum MessageStatus {
	CREATED(0), SENT(1), ACKNOWLEDGED(2), UNACKNOWLEDGED(3);
	private int value;
	private MessageStatus(int value) {
		this.value = value;
	}
	
	public int value() {
		return this.value;
	}
	
	public static MessageStatus toMessageStatus(int v) {
		return v == 0 ? CREATED : v == 1 ? SENT : v == 2 ? ACKNOWLEDGED : UNACKNOWLEDGED;
	}
}
