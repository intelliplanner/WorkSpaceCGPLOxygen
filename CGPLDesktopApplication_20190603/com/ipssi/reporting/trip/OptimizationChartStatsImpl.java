package com.ipssi.reporting.trip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ipssi.dispatchoptimization.OtimizationChartStats;
import com.ipssi.dispatchoptimization.PitStatsDTO;
import com.ipssi.dispatchoptimization.ShovelStatsDTO;

public class OptimizationChartStatsImpl implements OtimizationChartStats {
	/**
	 * Hourly Shift Stats of a single shovel. Shift Will be 1/2/3
	 * 
	 * @param shovelId
	 * @return Map<String(1/2/3), ShovelStatsDTO>
	 */
	public Map<String, ShovelStatsDTO> getShiftShovelStats(ArrayList<Integer> shovelIds){
		return new HashMap<String, ShovelStatsDTO>();
	}

	/**
	 * Current hour Shovel Stats
	 * 
	 * @param shovelId
	 * @return
	 */
	public ShovelStatsDTO getCurrentShovelStats(ArrayList<Integer> shovelIds){
		ShovelStatsDTO s=new ShovelStatsDTO();
		s.setTonnagePerHour((double)(Math.random()*1000));
//		s.setNumOfCycle((double)(Math.random()*1000));
//		s.setAvgCycleDuration((double)(Math.random()*1000));
//		s.setIdleTime((double)(Math.random()*1000));
//		s.setDumperWaitTime((double)(Math.random()*1000));
		return s;
	}

	/**
	 * Hourly Shift Stats of a Pit.
	 * 
	 * @param pitId
	 * @return
	 */
	public Map<String, PitStatsDTO> getShiftPitStats(ArrayList<Integer> pitIds){
		return new HashMap<String, PitStatsDTO>();
	}

	/**
	 * Current hour Pit Stats
	 * 
	 * @param pitId
	 * @return
	 */
	public PitStatsDTO getCurrentPitStats(ArrayList<Integer> pitIds){
		PitStatsDTO s=new PitStatsDTO();
//		s.setListOfShovels((int)(Math.random()*1000));
//		s.setListOfDumpers((int)(Math.random()*1000));
//		s.setNumOfShovels((double)(Math.random()*10));
//		s.setNumOfDumpers((double)(Math.random()*100));
//		s.setNumOfTonnagePerHour((double)(Math.random()*1000));
//		s.setNumOfTonnagePerKM((double)(Math.random()*1000));
//		s.setNumOfTrips((double)(Math.random()*100));
		return s; 
	}

	
}
