package inteviewQuestion.GlobalLogic;

public class FindValuesofSumEquals {
	public static void main(String[] args) {
		int total = 10;
		FindValuesofSumEqualss(total);
	}

	private static void FindValuesofSumEqualss(int total) {
		int[] arr = { 9, 3, 7, 6, 3, 1, 4 };
		int l = arr.length - 1;
		int i = 0;
		while (i < l) {
			if (arr[i] + arr[l] == total) {
				System.out.println(arr[i] + " " + arr[l] + " : " + (arr[i] + arr[l]));
				i++;
				l--;
			} else if (arr[i] + arr[l] < total) {
				i++;
			} else {
				l--;
			}
		}

	}
}
