package inteviewQuestion;

import java.util.TreeSet;

public class TreeSetClass {
	public static void main(String str[]) {

		TreeSet<String> currentJobs = new TreeSet<>();
		currentJobs.add("Test1");
		currentJobs.add("Test2");
//		currentJobs.add("Test3");
//		currentJobs.add("Test4");
		currentJobs.add("Test5");
//		currentJobs.add("Test6");
		System.out.println(currentJobs);
		System.out.println(currentJobs.pollFirst());
//		System.out.println(currentJobs.pollLast());
//		System.out.println(currentJobs.contains("Test2"));
		System.out.println(currentJobs.ceiling("Test3"));
	}
}
