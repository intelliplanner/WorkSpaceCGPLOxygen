package stringExample;

public class StringPattern {

	public static void main(String[] args) {
		int n = 10;
		int k=0;
		for (int i = 1; i <= n; i++) {
			for (int j = 0; j < k; j++) {
				System.out.print(i);
				i = i + j;
			}
			k++;
		}
		System.out.println("");
	}

}
