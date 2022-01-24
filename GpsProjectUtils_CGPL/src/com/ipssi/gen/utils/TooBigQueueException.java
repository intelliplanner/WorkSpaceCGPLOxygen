package com.ipssi.gen.utils;

public class TooBigQueueException extends Exception {
	public int maxQSize = 0;
    public TooBigQueueException(){super();}
    public TooBigQueueException(int size) {super(); maxQSize=size;}
}
