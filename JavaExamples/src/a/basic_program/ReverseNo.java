package a.basic_program;

public class ReverseNo {

	public static void main(String[] args) {
		int n=233;
		int reverse=0;
		int last_digit=0;
		while(n>0) {
			last_digit = last_digit*10 + n%10;
			n = n/10;
		}
		System.out.println(last_digit);
	}

}
