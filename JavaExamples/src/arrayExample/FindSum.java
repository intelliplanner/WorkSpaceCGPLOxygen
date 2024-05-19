package arrayExample;

import java.util.Arrays;

public class FindSum {
	public static void main(String[] args) {
		int[] arr = {9,3,5,7,8};
		
		FindSumEqualToGivenInput(arr);
//		FindSumEqualToGivenInputTest(arr);
	}

	private static void FindSumEqualToGivenInputTest(int[] arr) {
		int total = 14;
		int l = arr.length;
		int j=l-1;
		while(l<arr.length) {
			for (int i = 1; i < arr.length-1; i++) {
				if(arr[j]+arr[i] == total) {
					System.out.println(arr[j]+", "+arr[i]);
				}else {
					j--;
				}
			}
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	private static void FindSumEqualToGivenInput(int[] arr) {
		Arrays.sort(arr);
		int l = arr.length-1;
		int i = 0;
		int total = 14;
		while(i<l) {
			if(arr[i]+arr[l] == total) {
				System.out.println(arr[i]+","+arr[l]);
				break;
			}
			else if(arr[i]+arr[l] > total) {
				l--;
			}else {
				i++;
			}
		}
		
		
	}
}
