package data_structure.sorting;

public class MergeSort {
	public static void main(String args[]) {
		int[] arr = {6,3,9,5,2,8 };
//		printArr(arr);
		// int[] arr = { 1, 2, 4, 6 ,3, 7, 8, 9 };
		int si = 0;// start index
		int n = arr.length; // end index
		int ei = n - 1;
		devide(arr, si, ei);

	}

	private static void devide(int[] arr, int si, int ei) {
		if (si >= ei)
			return;
		int mid = si + (ei - si) / 2; // (si+ei)/2
		devide(arr, si, mid);
		devide(arr, mid + 1, ei);
		conquer(arr, si, ei, mid);
	}

	private static void conquer(int[] arr, int si, int ei, int mid) {
		int idx1 = si;
		int idx2 = mid + 1;
		int k = 0;
		int[] arrNew = new int[ei - si + 1];
		while (idx1 <= mid && idx2 <= ei) {
			if (arr[idx1] <= arr[idx2]) {
				arrNew[k++] = arr[idx1++];
			} else {
				arrNew[k++] = arr[idx2++];
			}
		}

		while (idx1 <= mid) {
			arrNew[k++] = arr[idx1++];
		}
		while (idx2 <= ei) {
			arrNew[k++] = arr[idx2++];
		}

		printArr(arr);

	}

	private static void printArr(int[] a) {
		for (int m : a) {
			System.out.print(m + " ");
		}
		System.out.println();
	}
}
