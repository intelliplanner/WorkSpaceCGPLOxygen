package stringExample;

public class PatternA {
	public static void main(String str[]) {
		int k = 1;
		for (int i = 1; i <= 10;) {
			for (int j = 1; j <= k; j++) {
				System.out.print(i+" ");
				i++;
			}
			k++;
			System.out.println();
		}
		
	}
}
