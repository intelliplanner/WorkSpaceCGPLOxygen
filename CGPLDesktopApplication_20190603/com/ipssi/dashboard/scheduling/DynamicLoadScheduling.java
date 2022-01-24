package com.ipssi.dashboard.scheduling;

import java.util.ArrayList;

import com.ipssi.gen.utils.Pair;

public class DynamicLoadScheduling {
	public volatile ArrayList<Pair<Integer,Integer>> shovelOperatingQueueLength = new ArrayList<Pair<Integer,Integer>>(); 
}
