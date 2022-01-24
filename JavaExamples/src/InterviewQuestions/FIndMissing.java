package InterviewQuestions;

public class FIndMissing {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int arr[] = { 5, 6, 8, 9, 11 };
		findMissing(arr);
	}

	private static void findMissing(int[] arr) {
		int i = 0;
		int lastDiff = 0;
		int currDiff = 0;
		while (i < arr.length - 1) {
			currDiff = arr[i + 1] - arr[i];
			if (lastDiff < currDiff && lastDiff > 0) {
				int val = arr[i] + lastDiff;
				System.out.println(val);
			} else {
				lastDiff = currDiff;
			}
			i++;
		}
	}

}
