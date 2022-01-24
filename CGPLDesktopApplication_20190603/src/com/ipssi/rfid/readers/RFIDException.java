package com.ipssi.rfid.readers;

public class RFIDException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message = null;

	public RFIDException() {
		super();
	}

	public RFIDException(String message) {
		super(message);
		this.message = message;
	}

	public RFIDException(Throwable cause) {
		super(cause);
	}

	@Override
	public String toString() {
		return message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
