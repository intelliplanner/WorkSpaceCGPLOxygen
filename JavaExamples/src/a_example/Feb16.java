package a_example;

public class Feb16 {

	public static void main(String[] args) {
		 findSum();
	}

	private static void findSum() {
		int [] arr = {2,7,11,15};
		int m = arr.length-1;
		int n = 0;
		int total = 9;
		while(true) {
			int val = arr[n]+arr[m] ; // n=0 m=3  2 15
			if(arr[n]+arr[m] == total) {
				System.out.println(arr[n] +", "+arr[m]);
				break;
			}else if(val < total) {  // n 0,1,2,3
				n++;
			}else {		// m 3,2,1,0
				m--;
			}
		}
	}
}
