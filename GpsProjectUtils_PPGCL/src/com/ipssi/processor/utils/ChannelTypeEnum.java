package com.ipssi.processor.utils;

import java.io.Serializable;

public enum ChannelTypeEnum implements Serializable {
	CURRENT, DATA, BOTH, UNKNOWN;//, APPLICATION;

	public static ChannelTypeEnum getChannelType(String status) {

		if (ChannelTypeEnum.CURRENT.toString().equalsIgnoreCase(status))
			return CURRENT;
		else if (ChannelTypeEnum.DATA.toString().equalsIgnoreCase(status))
			return DATA;
		else if (ChannelTypeEnum.BOTH.toString().equalsIgnoreCase(status))
			return BOTH;
		else if (ChannelTypeEnum.UNKNOWN.toString().equalsIgnoreCase(status))
			return UNKNOWN;
		
		//else if (ChannelTypeEnum.APPLICATION.toString().equalsIgnoreCase(status))
		//	return APPLICATION;
		else
			return null;
	}
	
	public static ChannelTypeEnum getChannelType(int status) {
		if ( status == 0)
			return CURRENT;
		else if (status == 2)
			return BOTH;
		else if (status == 3)
			return UNKNOWN;
		else 
			return DATA;
	}
	public static boolean isDataChannel(ChannelTypeEnum status) {
		return status == DATA || status == BOTH;
	}
	
	public static boolean isCurrentChannel(ChannelTypeEnum status) {
		return status == CURRENT || status == BOTH;
	}
	
	public static boolean isUnknownChannel(ChannelTypeEnum status) {
		return status == UNKNOWN;
	}
}
