package InterviewQuestions;

public class MRU {
 public static void main(String args[]) {
	 int[] arr2 = {3,5,2,4,1};
	 int k=3;
	 
	 
	 pringMRU(k,arr2);
	 int[] arr = {5, 7, 2, 3, 4, 1, 6};
	 int j = 10;
	 pringMRU(j,arr);
 }
 public static void pringMRU(int k,int[] arr2) {
	 
	 for(int i :arr2) {
		 System.out.print(" "+i);
	 }
	 System.out.println();
	 
	 int[] aar = new int[arr2.length];
	 if(arr2.length == 0) 
		 return;
	 if(k%arr2.length !=0)
		 k = k%arr2.length ; 
	 
	 for(int i=0;i<arr2.length;i++) {
		 aar[0] = arr2[k];
		 if(i<k) 
			 aar[i+1] = arr2[i];
		 else if(i>k)
			 aar[i] = arr2[i] ;
		
	 }
	 
//	  aar.forEach(System.out::println); 
	 
	 for(int i :aar) {
		 System.out.print(" "+i);
	 }
	 System.out.println();
	 
 }
}
