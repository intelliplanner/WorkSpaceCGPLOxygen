package com.ipssi.rfid.readers;

public class CommandData {
	public byte length;
    public byte addr = 0x00;
    public byte cmd;
    public byte[] data;
}
