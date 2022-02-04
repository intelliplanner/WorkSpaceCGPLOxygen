package InterviewQuestions.capgemini;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.poi.util.SystemOutLogger;

public class CapgeminiTest2 {

	public static void main(String[] args) {
//		test();
//		convertStringToInt("12345");
		convertStringToInt2("6789");
	}
	
	private static void convertStringToInt2(String str) {
		char[] ch = str.toCharArray();
		int n = 0;
		for (int i = 0; i < ch.length; i++) {
			n = n * 10 + (ch[i]-'0');
		}
		System.out.println(n);

		 int i = str.chars().reduce(0, (a, b) -> 10 * a + b - '0');
		 System.out.println(i);
	}

	private static void test() {
		List<Integer> arr = Arrays.asList(230,345,123,245,543);
		arr.stream().filter(s -> s.toString().charAt(0) - '0' == 2).collect(Collectors.toList()).forEach(System.out::println);
	}

	public static void convertStringToInt(String s) 
	{ 
	      
	    // Initialize a variable 
	    int num = 0; 
	    int n = s.length(); 
	  
	    // Iterate till length of the string 
	    for(int i = 0; i < n; i++) 
	  
	        // Subtract 48 from the current digit 
	        num = num * 10 + (s.charAt(i) - 48); 
	  
	    // Print the answer 
	    System.out.print(num); 
	} 
	  
}
