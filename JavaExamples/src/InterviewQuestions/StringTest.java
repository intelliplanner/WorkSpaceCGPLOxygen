package InterviewQuestions;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class StringTest {
	
	public static void main(String argrs[]) {
		String s1="Hello";
		String s2= new String("Hello");
		String a1="Vicky";
		s1 = s1 + " "+ a1;
		System.out.println("Test: "+s1);
		
		System.out.println(s1==s2);//compare the object reference so result comes false
		System.out.println(s1.equals(s2));//compare the  hashcode, content so result comes true
	    System.out.println(s1 == s2.intern());  
		System.out.println("s1= "+s1.hashCode() +", s2= "+ s2.hashCode());
		
		
		String s3 = "Hello 24-02-1989, 12-02-1990.";
		s3 = s3.replace(",", " ");
		s3 = s3.replace(".", " ");
		String[] s4 = s3.split(" ");
		ArrayList<String> arr = new ArrayList<>();
		for(String s:s4) {
			if(Pattern.matches("\\d{2}-\\d{2}-\\d{4}",s)) {
				System.out.println(s);
			}
		}
		String [] s6= s3.split("\\d{2}-\\d{2}-\\d{4}",s4.length);
		System.out.println();
		
		
	}
}


//(\\d{2}-\\d{2}-\\d{4},s)