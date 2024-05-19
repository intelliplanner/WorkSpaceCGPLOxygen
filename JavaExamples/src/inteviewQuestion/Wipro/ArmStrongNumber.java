package inteviewQuestion.Wipro;

public class ArmStrongNumber {
	public static void main(String s[]) {
		int no = 153;
		System.out.println(checkArmStrong(no));
	}
	public static boolean checkArmStrong(int no) {
		boolean isTrue = false;
		int num = 0;
		String val = Integer.toString(no);
		int armStrongVal = 0;
		for (int i = 0; i < val.length(); i++) {
			String j = val.charAt(i) + "";
			num = Integer.valueOf(j) * Integer.valueOf(j) * Integer.valueOf(j);
			armStrongVal += num;
		}
		if (armStrongVal == no) {
			isTrue = true;
		}
		return isTrue;
	}
}
