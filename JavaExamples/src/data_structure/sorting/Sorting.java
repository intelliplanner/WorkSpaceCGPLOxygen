package data_structure.sorting;

public class Sorting {
	public static void main(String[] args) {
		int[] a = { 2, 7, 3, 1, 5, 4 };
		System.out.print("{");
		for (int m : a) {
			System.out.print(m + " ");
		}
		System.out.println("}");
		// SelectionSorting(a, a.length);
		// Bubblesort(a);
		mergeSort(a);
	}

	private static void mergeSort(int[] a) {

		System.out.println("-----------------------Merge Sort---------------------");
		// int n = a.length;
		int[] l = { 1, 2, 4, 6 };
		int[] r = { 3, 7, 8, 9 };

		System.out.print("{");
		for (int m : l) {
			System.out.print(m + " ");
		}
		System.out.println("}");

		System.out.print("{");
		for (int m : r) {
			System.out.print(m + " ");
		}
		System.out.println("}");
		int[] newArr = new int[8];
		int i = 0, j = 0, k = 0;
		while (i < l.length && j < r.length) {
			if (l[i] <= r[j]) {
				newArr[k] = l[i];
				i++;
			} else {
				newArr[k] = r[j];
				j++;
			}k++;
		}
		while (i < l.length) {
			newArr[k] = l[i];
			i++;  k++;
		}
		while (j < r.length) {
			newArr[k] = r[j];
			j++;k++;
		}
		System.out.print("{");
		for (int m : newArr) {
			System.out.print(m + " ");
		}
		System.out.println("}");
	}

	public static void SelectionSorting(int[] a, int n) {
		System.out.println("-----------------------Selection Sort---------------------");
		int imin = -1;
		for (int i = 0; i < n - 1; i++) {
			imin = i;
			for (int j = i + 1; j < n; j++) {
				if (a[i] > a[j]) {
					imin = j;
					int temp = a[i];
					a[i] = a[j];
					a[j] = temp;
				}
			}
			System.out.print("{");
			for (int m : a) {
				System.out.print(m + " ");
			}
			System.out.println("}");
		}
		System.out.print("{");
		for (int m : a) {
			System.out.print(m + " ");
		}
		System.out.println("}");
	}

	static void bubblesort(int arr[]) {

		System.out.println("-----------------------Bubble Sort---------------------");
		int n = arr.length;

		// One by one move boundary of unsorted subarray
		for (int i = 1; i < n - 1; i++) {
			for (int j = 0; j < n - i; j++) {
				if (arr[j] > arr[j + 1]) {
					int temp = arr[j];
					arr[j] = arr[j + 1];
					arr[j + 1] = temp;
					System.out.print("{");
					for (int m : arr) {
						System.out.print(m + " ");
					}
					System.out.println("}");
				}
			}

		}

	}
}
