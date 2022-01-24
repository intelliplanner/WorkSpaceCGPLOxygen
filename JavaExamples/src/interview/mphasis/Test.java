package interview.mphasis;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {
	public static void main(String[] arg) {
		List<Integer> l1= Arrays.asList(1,2,3,4,5);
		List<Integer> l2 = Arrays.asList(11,12,13,14,15);
		System.out.println(l1);
		System.out.println(l2);
		List<Integer> l3 = Stream.concat(l1.stream(), l2.stream()).collect(Collectors.toList());
		Collections.sort(l3,Collections.reverseOrder());
		System.out.println(l3);
	}

	private static void test1(int a, int b) {
		
	}
}
