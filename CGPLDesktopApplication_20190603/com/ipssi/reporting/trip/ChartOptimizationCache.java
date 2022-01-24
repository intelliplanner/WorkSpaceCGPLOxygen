package com.ipssi.reporting.trip;

public class ChartOptimizationCache {
	private static FixedSizeQueue shovelStats = new FixedSizeQueue(20);
	private static FixedSizeQueue pitStats = new FixedSizeQueue(20);
	private static FixedSizeQueue dumpersStats = new FixedSizeQueue(20);

	public static FixedSizeQueue getShovelStats(boolean isFirstTime) {
		if (isFirstTime) {
			// getFrom cached value
			return null;
		} else {
			return shovelStats;
		}
	}

	public static FixedSizeQueue getPitStats(boolean isFirstTime) {
		if (isFirstTime) {
			// getFrom cached value
			return null;
		} else {
			return pitStats;
		}
	}

	public static FixedSizeQueue getDumpersStats(boolean isFirstTime) {
		if (isFirstTime) {
			// getFrom cached value
			return null;
		} else {
			return dumpersStats;
		}
	}
	
	public static void clearAllStats() {
		dumpersStats.clear();
		pitStats.clear();
		shovelStats.clear();
	}
}
