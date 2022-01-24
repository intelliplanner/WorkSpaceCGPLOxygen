package InterviewQuestions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DateFinder {
	static SimpleDateFormat dateFormatter = new SimpleDateFormat("DD-MM-YYYY");

	public static void main(String args[]) {
		String paragraph = "On Document this, 2-12-2019 and 12-11-2018  and  02-10-2019.";
		int cunt = 0;
		// 1st way
		cunt = getDateCount(paragraph);
		System.out.println(cunt);

		// 2nd way
		cunt = getDateCountByJava8(paragraph);
		System.out.println(cunt);
	}

	private static int getDateCountByJava8(String paragraph) {
		paragraph = paragraph.substring(0, paragraph.length() - 1);
		List<String> li = Arrays.asList(paragraph.split(" "));
		List<String> list = li.stream().filter(s -> isValid(s)).collect(Collectors.toList());
		list.forEach(System.out::println);
		return list.size();
	}

	public static int getDateCount(String paragraph) {
		int count = 0;

		paragraph = paragraph.substring(0, paragraph.length() - 1);
		// char[] strArray = paragraph.toCharArray();
		String[] strArray = paragraph.split(" ");
		for (String s : strArray) {
			if (isValid(s)) {
				count++;
			}
		}
		return count;

	}

	public static boolean isValid(String dateStr) {
		boolean isTrue = false;
		try {
			dateFormatter.parse(dateStr);
			isTrue = true;
		} catch (ParseException e) {
			isTrue = false;
		}
		return isTrue;
	}
}
