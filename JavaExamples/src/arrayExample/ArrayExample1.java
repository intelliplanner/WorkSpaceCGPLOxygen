package arrayExample;

//Given an array of integers, update the index with multiplication of previous and next integers,
//e.g. Input: 2 , 3, 4, 5, 6
//     Output: 2*3, 2*4, 3*5, 4*6, 5*6
public class ArrayExample1 {
	
	public static void main(String []args) {
		int arr[] = {2,3,4,5,6};
		int arr2[] = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			if(i==0 && arr.length > 1) { //i  0 
				arr2[i] = arr[i] * arr[i+1];
			}else if(i == arr.length-1) { // i  4    
				arr2[i] = arr[i-1] * arr[i];
			}else { //i 1  2  3
				arr2[i] = arr[i-1] * arr[i+1];
			}
		}
		for(int i:arr2) {
			System.out.print(i +" ");
		}
	}
}
