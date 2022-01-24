package InterviewQuestions.nagarrowInterviewQuestions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Subset {
	public static void main(String agrs[]) {
		int[] set = { 1, 2, 3, 4, 2 };
		subsetTest(set);
	}

	private static void subsetTest(int[] set) {
		int[] subset1 = new int[5];
		int[] subset2 = new int[5];
		// List<int[]> list = Arrays.asList(set);
		List<Integer> list = Arrays.stream(set) // IntStream
				.boxed() // Stream<Integer>
				.collect(Collectors.toList());

		Integer sum = list.stream().collect(Collectors.summingInt(s -> s));

		int subArrTotal = sum / 2;

		int subArr_1_total = 0;
		int n1 = 0;
		int subArr_2_total = 0;
		int n2 = 0;
		int i = 0;
		while (i < set.length) {

			subArr_1_total += set[i];

			subArr_2_total += set[i];

			if (subArr_1_total <= subArrTotal) {
				subset1[n1] = set[i];
				n1++;
				subArr_2_total -= set[i];
			} else {
				subset2[n2] = set[i];
				n2++;
				subArr_1_total -= set[i];
			}
			i++;
		}
		for (int m : subset1) {
			System.out.print(m != 0 ? m + " " : "");
		}
		System.out.println(" ");
		for (int m : subset2) {
			System.out.print(m != 0 ? m + " " : "");
		}

	}
}
