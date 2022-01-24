package inteviewQuestion.Wipro;

//check String is pandrome/
public class TestDec292021 {
	public static void main(String args[]) {
		System.out.println(checkPalindromeString("NAMAN"));
	}

	private static boolean checkPalindromeString(String str) {
		StringBuffer s = new StringBuffer(str);
//		s.reverse();
		String sNew = s.reverse().toString();
		if(sNew.equals(str)) {
			return true;
		}
		return false;
	}
}
