package collection.list;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ArrayListDictionaryOrder { // lexographically
	public static void main(String[] args) {
		// Initializing String array.
//		dictionarySortedString();

//		dictionarySortedInteger();
		
//		String stringArray[] = { "1", "2", "10", "1", "50", "20","0" };
		
//		Stream<String> s = Stream.of("1", "2", "10", "1", "50", "20","0");
//		s.sorted(Comparator.reverseOrder()).forEach(i-> System.out.print(i+" "));
//		System.out.println();
		Stream<String> sa = Stream.of("1", "2", "10", "1", "50", "20","0");
//		
//		sa.sorted(Comparator.naturalOrder()).forEach(i-> System.out.print(i+" "));
		sa.sorted().forEach(i-> System.out.print(i+" "));
		System.out.println();
		List<Integer> li = Arrays.asList(1,2,3,10,4,5);
		li.stream().sorted().forEach(i-> System.out.print(i+" "));
//		li.stream().sorted(Comparator.naturalOrder()).forEach(i-> System.out.print(i+" "));
	}

//	private static void dictionarySortedInteger() {
//		int arr[] = { 1, 2, 10, 20, 30, 111, 222 };
//		for (int i = 0; i < arr.length; i++) {
//			for (int j = i+1; j < arr.length; j++) {
//				if(swap(arr[i],arr[j])) {
//					int temp1 = arr[i];
//					int temp2 = arr[j];
//					arr[i]=temp1;
//					arr[j]=temp2;
//				}
//			}
//		}
//
//	}
//	
//	private static void swap(int i,int j) {
//		int len = Integer.toString(i).length() >  Integer.toString(j).length() ? Integer.toString(j).length() : Integer.toString(i).length() ;
//		for (int m= 0;m < len; m++) {
//			char  c1 = Integer.toString(i).charAt(m);
//			char  c2 = Integer.toString(j).charAt(m);
//			if( > Integer.parseInt(Integer.toString(i).charAt(m)) ) {
//				
//			}
//		}
//		
//	}

	public static void dictionarySortedString() {
		String stringArray[] = { "1", "2", "10", "1", "50", "20","0" };
//		String stringArray[] = { "Gritav", "Harit", "Lovenish", "Nikhil", "Harman", "Girish", };
		// sorting String array lexicographically.
		sortLexicographically(stringArray);

		printArray(stringArray);
	}

	public static void sortLexicographically(String strArr[]) {
		for (int i = 0; i < strArr.length; i++) {
			for (int j = i + 1; j < strArr.length; j++) {
				if (strArr[i].compareToIgnoreCase(strArr[j]) > 0) {
					String temp = strArr[i];
					strArr[i] = strArr[j];
					strArr[j] = temp;
				}
			}
		}
	}

	// this function prints the array passed as argument
	public static void printArray(String strArr[]) {
		for (String string : strArr)
			System.out.print(string + " ");
		System.out.println();
	}

}
