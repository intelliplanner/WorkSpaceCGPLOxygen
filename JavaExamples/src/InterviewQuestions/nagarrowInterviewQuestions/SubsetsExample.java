package InterviewQuestions.nagarrowInterviewQuestions;

//Question:Wap to find subsets that contains equal sum in an array:
//for eg{1,2,3,4,2}->{1,2,3}&&{4,2}
//{1,1,3,3,2,8}->{1,3,3,2}&&{1,8}
//{1,3,4,7}->no subset 

public class SubsetsExample {
	static int[] subset1 = new int[5];
	static int[] subset2 = new int[5];

	public static void main(String[] args) {
		int[] set = { 1, 2, 3, 4, 2 };
		getSubsets(set);
	}

	private static void getSubsets(int[] set) {
		int arrTotalSize = 0;
		for (int i = 0; i < set.length; i++) {
			arrTotalSize += set[i];
		}
		int subArrTotalSize = arrTotalSize / 2;
		if ((arrTotalSize - subArrTotalSize) == arrTotalSize) {
			System.out.println("No Subset");
			return;
		}
		int subset1_total = 0, n1 = 0;
		int subset2_total = 0, n2 = 0;
		int i = 0;
		while (i < set.length) {
			subset1_total += set[i];
			subset2_total += set[i];

			if (subset1_total <= subArrTotalSize) {
				subset1[n1] = set[i];
				n1++;
				subset2_total -= set[i];
			} else {
				subset2[n2] = set[i];
				n2++;
				subset1_total -= set[i];
			}

			i++;
		}
		for (int m : subset1) {
			System.out.print(m != 0 ? m + " " : "");
		}
		System.out.println(" ");
		for (int m : subset2) {
			System.out.print(m != 0 ? m + " " : "");
		}
	}
}