package inteviewQuestion;

import java.util.ArrayList;

public class Solution {
	public int[] solution(int[] A, int F, int M) {
		int val = 0;
		int sum = 0;
		int mean = 0;
		int total = (A.length + F) * M;
		System.out.println("total:" + total);
		for (int i = 0; i < A.length; i++) {
			sum = sum + A[i];
		}
		System.out.println("sum:" + sum);
		val = total - sum;
		System.out.println("value =" + val);

		int n = val / F;
		
		ArrayList<Integer> i = new ArrayList<>();
		for (int k = 1; k <= 6; k++) {
			for (int j = 1; j <= 6; j++) {
				if (k + j == val) {
					i.add(k);
					i.add(j);
					break;
				}
			}
		}

		int[] m = null;// (int[]) i.toArray();
		return m;
	}

	public static void main(String a[]) {
		int[] A = { 3, 2, 4, 3 };
		int F = 2;
		int M = 4;
		Solution o = new Solution();
		o.solution(A, F, M);

	}

}
