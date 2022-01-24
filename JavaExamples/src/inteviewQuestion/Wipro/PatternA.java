package inteviewQuestion.Wipro;

// TODO Auto-generated method stub
// 1
// 2 3
// 4 5 6
// 7 8 9 10
public class PatternA {

	public static void main(String[] args) {

		int n = 10;
		int k = 1;
		for (int i = 1; i <= n;) {
			for (int j = 1; j <= k; j++) {
				System.out.print(" " + i);
				i++;
			}
			k++;
			System.out.println("");
		}
	}

}
