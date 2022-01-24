package com.ipssi.dispatchoptimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class OptimizationChartStatsImpl implements OtimizationChartStats {
	/**
	 * Hourly Shift Stats of a single shovel. Shift Will be 1/2/3
	 * 
	 * @param shovelId
	 * @return Map<String(1/2/3), ShovelStatsDTO>
	 */
	public Map<String, ShovelStatsDTO> getShiftShovelStats(ArrayList<Integer> shovelIds){
		Map<String, ShovelStatsDTO> map=new HashMap<String, ShovelStatsDTO>();
		ShovelStatsDTO s=new ShovelStatsDTO();
		s.setTonnagePerHour((double)(Math.random()*1000));
		s.setAvgNumOfCyclePerTrip((double)(Math.random()*100));
		s.setAvgCycleTime((double)(Math.random()*100));
		s.setAvgShovelIdlePercentage((double)(Math.random()*100));
		s.setAvgDumperWaitTime((double)(Math.random()*100));
		s.setAvgCleaningPercentage((double)(Math.random()*100));
		map.put("0", s);
		ShovelStatsDTO s1=new ShovelStatsDTO();
		s1.setTonnagePerHour((double)(Math.random()*1000));
		s1.setAvgNumOfCyclePerTrip((double)(Math.random()*100));
		s1.setAvgCycleTime((double)(Math.random()*100));
		s1.setAvgShovelIdlePercentage((double)(Math.random()*100));
		s1.setAvgDumperWaitTime((double)(Math.random()*100));
		s1.setAvgCleaningPercentage((double)(Math.random()*100));
		map.put("1", s1);
		ShovelStatsDTO s2=new ShovelStatsDTO();
		s2.setTonnagePerHour((double)(Math.random()*1000));
		s2.setAvgNumOfCyclePerTrip((double)(Math.random()*100));
		s2.setAvgCycleTime((double)(Math.random()*100));
		s2.setAvgShovelIdlePercentage((double)(Math.random()*100));
		s2.setAvgDumperWaitTime((double)(Math.random()*100));
		s2.setAvgCleaningPercentage((double)(Math.random()*100));
		map.put("2", s2);
		
		return map;
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
		s.setAvgNumOfCyclePerTrip((double)(Math.random()*100));
		s.setAvgCycleTime((double)(Math.random()*100));
		s.setAvgShovelIdlePercentage((double)(Math.random()*100));
		s.setAvgDumperWaitTime((double)(Math.random()*100));
		s.setAvgCleaningPercentage((double)(Math.random()*100));
		return s;
	}

	/**
	 * Hourly Shift Stats of a Pit.
	 * 
	 * @param pitId
	 * @return
	 */
	public Map<String, PitStatsDTO> getShiftPitStats(ArrayList<Integer> pitIds){
		Map<String, PitStatsDTO> map=new HashMap<String, PitStatsDTO>();
		PitStatsDTO s=new PitStatsDTO();
		s.setNumOfShovels((int)(Math.random()*10));
		s.setNumOfDumpers((int)(Math.random()*50));
		s.setAvgTonnageDispatched((double)(Math.random()*2000));
		s.setAvgWaitTimeForDumpers((double)(Math.random()*100));
		s.setAvgIdleTimeOfShovel((double)(Math.random()*100));
		s.setAvgCycleTime((double)(Math.random()*100));
		s.setAvgCyclePerTrip((double)(Math.random()*100));
		s.setAvgLead((double)(Math.random()*100));
		map.put("0", s);
		PitStatsDTO s1=new PitStatsDTO();
		s1.setNumOfShovels((int)(Math.random()*10));
		s1.setNumOfDumpers((int)(Math.random()*50));
		s1.setAvgTonnageDispatched((double)(Math.random()*2000));
		s1.setAvgWaitTimeForDumpers((double)(Math.random()*100));
		s1.setAvgIdleTimeOfShovel((double)(Math.random()*100));
		s1.setAvgCycleTime((double)(Math.random()*100));
		s1.setAvgCyclePerTrip((double)(Math.random()*100));
		s1.setAvgLead((double)(Math.random()*100));
		map.put("0", s1);
		PitStatsDTO s2=new PitStatsDTO();
		s2.setNumOfShovels((int)(Math.random()*10));
		s2.setNumOfDumpers((int)(Math.random()*50));
		s2.setAvgTonnageDispatched((double)(Math.random()*2000));
		s2.setAvgWaitTimeForDumpers((double)(Math.random()*100));
		s2.setAvgIdleTimeOfShovel((double)(Math.random()*100));
		s2.setAvgCycleTime((double)(Math.random()*100));
		s2.setAvgCyclePerTrip((double)(Math.random()*100));
		s2.setAvgLead((double)(Math.random()*100));
		map.put("0", s2);
		return map; 
	}

	/**
	 * Current hour Pit Stats
	 * 
	 * @param pitId
	 * @return
	 */
	public PitStatsDTO getCurrentPitStats(ArrayList<Integer> pitIds){
		PitStatsDTO s=new PitStatsDTO();
		s.setNumOfShovels((int)(Math.random()*10));
		s.setNumOfDumpers((int)(Math.random()*50));
		s.setAvgTonnageDispatched((double)(Math.random()*2000));
		s.setAvgWaitTimeForDumpers((double)(Math.random()*100));
		s.setAvgIdleTimeOfShovel((double)(Math.random()*100));
		s.setAvgCycleTime((double)(Math.random()*100));
		s.setAvgCyclePerTrip((double)(Math.random()*100));
		s.setAvgLead((double)(Math.random()*100));
		return s; 
	}


}
