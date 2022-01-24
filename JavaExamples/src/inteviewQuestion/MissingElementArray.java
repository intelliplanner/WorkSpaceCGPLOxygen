package inteviewQuestion;

import java.util.ArrayList;
import java.util.Arrays;
public class MissingElementArray {

	public static void main(String arhgs[]) {
		int arr[]= {1,2,3,5,6,7};
		
		printMissingElement(arr);
		
		int arr1[]= {6,7,9,10};
		printMissingElement(arr1);
		
	}

	public static void printMissingElement(int arr[]) {
		if (arr == null )
			return;
		
		  Arrays.sort(arr);
		 
		ArrayList<Integer> value = new ArrayList<>();
		int lastDiff = 0;
		for (int i = 0; i < arr.length-1; i++) {
//			if ((i + 1) >= arr.length) {
//				break;
//			}
			int CurrDiff = arr[i + 1] - arr[i];
			if (lastDiff < CurrDiff  && lastDiff!=0) {
				int a = arr[i + 1] - lastDiff;
				value.add(a);
			} else if (lastDiff > CurrDiff) {
				int a = arr[i + 1] + lastDiff;
				value.add(a);
			} else {
				lastDiff = CurrDiff;
			}
		}
		
		System.out.println(value);
	}

}
