package InterviewQuestions.nagarrowInterviewQuestions;

import java.util.ArrayList;
import java.util.List;

//Check whether two strings are anagram of each other. Write a function to check whether
//two given strings are anagram of each other or not. An anagram of a string is another string
//that contains same characters, only the order of characters can be different. 
//For example, “abcd” and “dabc” are anagram of each other.
public class AnaGram {

	public static void main(String[] args) {
		isss("TRIANGLE","INTEGRAL");
	}

	private static void isss(String string1, String string2) {
		char[] ch1=string1.toCharArray();
		char[] ch2=string2.toCharArray();
		List<Integer> index=new ArrayList<>();
		boolean isTrue = false;
		if(ch1.length!=ch2.length)
			System.out.println("Not ");
		for (int i = 0; i < string1.length(); i++) {
			for (int j = 0; j < string2.length(); j++) {// LISTEN  SILENT
				System.out.println(i +"="+ ch1[i]  + " , "+ j +"="+ ch2[j] );
				if(ch1[i]==ch2[j] && !index.contains(j)){
					index.add(j);
					break;
				}else {
					continue;
				}
			}
		}
		if(string1.length() == index.size() ) {
			isTrue=true;
		}
		System.out.println(isTrue);
	}

}
