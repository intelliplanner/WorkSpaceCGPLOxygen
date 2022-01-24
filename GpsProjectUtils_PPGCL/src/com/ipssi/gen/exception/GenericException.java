/**
 * 
 */
package com.ipssi.gen.exception;

/**
 * @author Kapil
 * 
 */
public class GenericException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6957085111071402487L;

	public GenericException() {
		super();
	}

	public GenericException(String message) {
		super(message);
	}

	public GenericException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public GenericException(Throwable throwable) {
		super(throwable);
	}
}
