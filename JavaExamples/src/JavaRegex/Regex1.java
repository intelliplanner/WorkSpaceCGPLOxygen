package JavaRegex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import stringExample.pattern;

public class Regex1 {
	
	public static void main(String args[]) {
		System.out.println(Pattern.matches(".s", "mst")); // 2 char and 2nd must be s
		System.out.println(Pattern.matches(".st", "msg")); // 3 char and 2nd must be s and last must be t

		// =======================

		System.out.println(Pattern.matches("[abc]", "a"));
		System.out.println(Pattern.matches("[abc]?", "abc"));
		System.out.println(Pattern.matches("[abc]+", "a"));
		System.out.println(Pattern.matches("[abc]*", "aabb"));

		System.out.println(Pattern.matches("[[0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9]]{10}", "04-02-1989"));
		System.out.println(Pattern.matches("\\d{2}-\\d{2}-\\d{4}", "4-02-1989"));

		// System.out.println(Pattern.matches("[a-z]{2}", "abd"));

		Pattern pattern = Pattern.compile("ab", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher("ABcabdAb");
		// using Matcher find(), group(), start() and end() methods
		while (matcher.find()) {
			System.out.println("Found the text \"" + matcher.group() + "\" starting at " + matcher.start()
					+ " index and ending at index " + matcher.end());
		}

		// using Pattern split() method
		pattern = Pattern.compile("\\W");
		String[] words = pattern.split("one@two#three:four$five");
		for (String s : words) {
			System.out.println("Split using Pattern.split(): " + s);
		}

		// using Matcher.replaceFirst() and replaceAll() methods
		pattern = Pattern.compile("1*2");
		matcher = pattern.matcher("11234512678");
		System.out.println("Using replaceAll: " + matcher.replaceAll("_"));
		System.out.println("Using replaceFirst: " + matcher.replaceFirst("_"));

		Pattern pattern1 = Pattern.compile("\\d{2}-\\d{2}-\\d{4}", Pattern.CASE_INSENSITIVE);
		Matcher matcher1 = pattern1.matcher("This is 24-02-1989, 12. .");
		// using Matcher find(), group(), start() and end() methods
		while (matcher.find()) {
			
		}

	}
}
