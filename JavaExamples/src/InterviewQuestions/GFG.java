package InterviewQuestions;

//Java program to find the smallest 
//positive missing number 
import java.util.*; 

class GFG 
{ 

//Function to find the smallest 
//positive missing number 
static int findMissingPositive(int arr[], int n) 
{ 
 int m = 1;  // Default smallest Positive Integer  // Store values in set which are  // greater than variable m 
 HashSet<Integer> x = new HashSet<Integer>(); 
 for (int i = 0; i < n; i++) 
 { 
      if (m < arr[i]) //[2, 3, -7, 6, 8, 1, -10, 15]     // Store value when m is less than          // current index of given array 
     { 
         x.add(arr[i]); 
     } 
     else if (m == arr[i]) 
     { 
         m = m + 1;          // Increment m when it is equal          // to current element 

         while (x.contains(m))  
         { 
             x.remove(m);              // Increment m when it is one of the              // element of the set 
             m = m + 1; 
         } 
     } 
 } 

 // Return the required answer 
 return m; 
} 

//Driver code 
public static void main(String[] args)  
{ 
 int arr[] = { 2, 3, -7, 6, 8, 1, -10, 15 }; 
 int n = arr.length; 
 // Function call 
 System.out.println(findMissingPositive(arr, n)); 
} 
} 
