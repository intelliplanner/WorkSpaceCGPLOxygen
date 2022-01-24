package arrayExample;

import java.util.Arrays;
import java.util.List;

public class ArraySort {
	
	public static void main(String[] args) {
		int[] arr = {-8,-5,-3,-1,3,6,9};
		sortArray(arr);
	}

	private static void sortArray(int[] arr) {
		int secondArrLenth = arr.length/2;
		int firstArrLenth =  arr.length - secondArrLenth;
		int[] arr1=new int[firstArrLenth];
		int[] arr2=new int[secondArrLenth];
		
		int j=0,k=0;
		for (int i = 0; i < firstArrLenth-1; i++) {
			arr1[j] = arr[i];
			j++;
		}
		for (int i = firstArrLenth; i <  arr.length-1; i++) {
			arr2[k] = arr[i];
			k++;
		}
		int m=0,n=0;
		boolean isTrue = false;
		while(!isTrue) {
			if(arr1[m] < arr2[n]) {
				m++;
			}
		}
//		int j = 0;
		
				
//		List l= Arrays.asList(arr);
//		System.out.println(l);
//		while (i<arr.length) {
//			if(arr)
//		}
	}

}
