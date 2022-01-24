package arrayExample;


public class ArraySorting {
	static int arr1[] = { 1, 0, 0, 0, 1, 1,1 };
	static int arr2[] = new int[7];

	public static void main(String args[]) {
		int j=0;
		for(int i=0; i<arr1.length; i++) {
			if(arr1[i]==0) {
				j++;
			} 
		}
		
		for(int i=0; i<arr1.length; i++) {
			if(i < j) {
				System.out.print("0");
			}else {
				System.out.print("1");
			}
		}
		
		for(int i=0; i<arr1.length; i++) {
			if(arr1[i]==0) 
				arr2[i]=arr1[i];
			else
				arr2[i]=arr1[i];
		}
		
		for(int i=0; i<arr2.length; i++) {
				System.out.println("0");
		}
		
	}
}
