package arrayExample;

public class ArrayRotation2 {
	
	
	static void rotateLeft(int[] arr,int key) {
		int arrSize=arr.length;
		if(arr[arrSize-1] == key) {
			System.out.println("Rotated Array: "+ arr);
			return;
		}
		int m[]= {};
		for (int i = 0; i < arr.length; i++) {
			 m[i+1] = arr[i];  
		}
		arr = m;
		rotateLeft(arr, key);
	}
	static void rotateRight(int[] arr,int key) {
		
	}
	
	public static void main(String ars[]) {
		int arr[]= {1,2,3,4,5,6};
		rotateLeft(arr, 2);
	}
}
