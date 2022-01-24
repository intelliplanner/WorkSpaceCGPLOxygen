package com.ipssi.rfid.readers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RFIDEncoding {

	public enum TYPE {

		INTEGER, STRING, DATE, DOUBLE, DELEMETER
	}

	TYPE type();

	String start();

	String max();

	String end();

	public @interface EPC {
	}

	public @interface delemeter {
	}
}
