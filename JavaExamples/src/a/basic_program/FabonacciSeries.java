package a.basic_program;

public class FabonacciSeries {

	public static void main(String[] args) {
		int n = 20;
		fabonacciSeries(n);
		fabonacciSeriesByForLoop(n);
		System.out.println(nth_fabonacciNo(n));
	}

	public static void fabonacciSeriesByForLoop(int n) {
		int a = 0, b = 1;
		int c = 0;
		for (int i = 0; i < n; i++) {
			if (i == 0) {
				c = 0;
			} else {
				a = b;
				b = c;
				c = a + b;
			}
			System.out.println(c);
		}

	}

	private static void fabonacciSeries(int n) {
		int a = 0;
		int b = 1;
		int c = 0;
		int i = 0;
		while (i != n) {
			if (i > 0) {
				a = b;
				b = c; //
				c = a + b;
			}
			System.out.print(c + " ");
			i++;
		}
	}

	static int nth_fabonacciNo(int n) {
		if (n <= 1)
			return n;
		return nth_fabonacciNo(n - 1) + nth_fabonacciNo(n - 2);
	}

}
