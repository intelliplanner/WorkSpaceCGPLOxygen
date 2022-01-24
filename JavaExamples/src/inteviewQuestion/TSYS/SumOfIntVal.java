package inteviewQuestion.TSYS;


// Sum of all digit value of 5 digit no. in single digit
public class SumOfIntVal {
	
	public static void main(String[] args) {
		int n = 5432;
		while(Integer.toString(n).length() > 1) {
			n = calcSum(n); 
		}
		System.out.println();
		System.out.println("Final: "+n);
	}

	private static int calcSum(int n) {
		int sum = 0;
			while (n > 0  ) {
				int rem = n % 10;
				sum += rem;
				n = n / 10;
			}
		System.out.print(sum +  " ");
		return sum;
	}
}
