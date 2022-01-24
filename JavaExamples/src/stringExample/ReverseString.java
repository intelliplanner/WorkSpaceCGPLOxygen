package stringExample;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ReverseString {

	public static void main(String[] args) {
		int[] arr = { 9, 3, 5, 7, 8 };
		// reverseArray(arr);
		// reverseString("vickyTest");
//		reverseStringNew("test");
		String str = "test";
		char[] ch = str.toCharArray();
		IntStream.range(0,ch.length).mapToObj(i-> ch[(ch.length-1)-i]).forEach(s->System.out.println(s));
	}

	private static void reverseStringNew(String string) {
		// 1st way
		System.out.println("1st way:");
		String str = "test";
		StringBuilder newStr = new StringBuilder();
		for (int i = str.length() - 1; i >= 0; i--) {
			newStr.append(str.charAt(i));
		}

		System.out.println(newStr.toString());

		// 2nd way
		System.out.println("2nd way:");
		StringBuilder newStr1 = new StringBuilder(str).reverse();
		System.out.println(newStr1.toString());

		// 3rd way
		System.out.println("3rd way:");
		String revStr = Stream.of(str).map(w -> new StringBuilder(w).reverse()).collect(Collectors.joining(" "));

		System.out.println(revStr);

		// 4th way

		System.out.println("4th way:");
//		char[] charArray = "Hello Vicky".toCharArray();
		
		String value = "I am hungry";
		char[] ch = value.toCharArray();
		IntStream.range(0,ch.length).mapToObj(i-> ch[(ch.length-1) - i]).forEach(System.out:: print);
		
		System.out.println("5th way:");
		String reversed = str.chars()
			    .mapToObj(c -> (char)c)
			    .reduce("", (s,c) -> c+s, (s1,s2) -> s2+s1);
		System.out.println(reversed);
		
	}

	private static void reverseString(String str) {
		System.out.println();
		char[] arr = str.toCharArray();
		for (int j = 0; j < arr.length; j++) {
			System.out.print(" " + arr[j]);
		}
		System.out.println();
		int l = arr.length;
		int r = l / 2;
		int i = 0;
		char temp;
		while (i < r) {
			temp = arr[i];
			arr[i] = arr[l - 1];
			arr[l - 1] = temp;
			i++;
			l--;
		}
		for (int j = 0; j < arr.length; j++) {
			System.out.print(" " + arr[j]);
		}
	}

	private static void reverseArray(int[] arr) {
		for (int j = 0; j < arr.length; j++) {
			System.out.print(" " + arr[j]);
		}
		System.out.println();
		int l = arr.length;
		int r = l / 2;
		int i = 0;
		int temp;
		while (i < r) {
			temp = arr[i];
			arr[i] = arr[l - 1];
			arr[l - 1] = temp;
			i++;
			l--;
		}
		for (int j = 0; j < arr.length; j++) {
			System.out.print(" " + arr[j]);
		}

	}

}
