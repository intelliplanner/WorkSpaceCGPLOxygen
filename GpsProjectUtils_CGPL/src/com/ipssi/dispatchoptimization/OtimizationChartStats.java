package com.ipssi.dispatchoptimization;

import java.util.ArrayList;
import java.util.Map;

public interface OtimizationChartStats {
	/**
	 * Hourly Shift Stats of a single shovel. Shift Will be 1/2/3
	 * 
	 * @param shovelId
	 * @return Map<String(1/2/3), ShovelStatsDTO>
	 */
	public Map<String, ShovelStatsDTO> getShiftShovelStats(ArrayList<Integer> shovelIds);

	/**
	 * Current hour Shovel Stats
	 * 
	 * @param shovelId
	 * @return
	 */
	public ShovelStatsDTO getCurrentShovelStats(ArrayList<Integer> shovelIds);

	/**
	 * Hourly Shift Stats of a Pit.
	 * 
	 * @param pitId
	 * @return
	 */
	public Map<String, PitStatsDTO> getShiftPitStats(ArrayList<Integer> pitIds);

	/**
	 * Current hour Pit Stats
	 * 
	 * @param pitId
	 * @return
	 */
	public PitStatsDTO getCurrentPitStats(ArrayList<Integer> pitIds);
}
