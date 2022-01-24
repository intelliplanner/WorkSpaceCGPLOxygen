package MemoryLeak;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HeapExceptionOutOfMemory {

	/**
	 * @author Crunchify.com
	 * @throws Exception
	 * 
	 */
	public static void main(String[] args) {
		// HeapExceptionOutOfMemory memoryTest = new HeapExceptionOutOfMemory();
		// memoryTest.generateOOM();

		Map<Integer, String> m = new HashMap<>();
		// m = System.getProperties();
		// Random = new Random();
		int i = 0;
		try {
			while (true) {
				m.put(i++, "randomValue");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void generateOOM() throws Exception {
		int iteratorValue = 20;
		System.out.println("\n=================> OOM test started..\n");
		for (int outerIterator = 1; outerIterator < 20; outerIterator++) {
			System.out.println("Iteration " + outerIterator + " Free Mem: " + Runtime.getRuntime().freeMemory());
			int loop1 = 2;
			int[] memoryFillIntVar = new int[iteratorValue];
			// feel memoryFillIntVar array in loop..
			do {
				memoryFillIntVar[loop1] = 0;
				loop1--;
			} while (loop1 > 0);
			iteratorValue = iteratorValue * 5;
			System.out.println("\nRequired Memory for next loop: " + iteratorValue);
			Thread.sleep(1000);
		}
	}

}