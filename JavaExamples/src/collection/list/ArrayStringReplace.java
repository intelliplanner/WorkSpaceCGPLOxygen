package collection.list;

public class ArrayStringReplace {
	private static void replaceString(String paragraph, String replaceStr) {
		char[] ch = paragraph.toCharArray();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < ch.length) {
			if (ch[i] == 'i' && ch[i + 1] == 's') {
				sb.append(replaceStr);
				i++;
			} else {
				sb.append(ch[i]);
			}
			i++;
		}
		System.out.println(sb.toString());
	}

	public static void main(String[] args) {
		String paragraph = "This is";
		String replaceWith = "is";
		String replaceStr = "Vicky";
		replaceString(paragraph, replaceStr);
	}
}
