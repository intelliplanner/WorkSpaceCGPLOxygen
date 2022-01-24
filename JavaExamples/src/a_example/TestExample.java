package a_example;

public class TestExample {
	String berry = "blue";

	public static void main(String args[]) {
		divisionTest();
		new TestExample().juicy("straw");
	}

	private static void divisionTest() {
		// boolean s = Math.isD== 0;
		int int0 = 0;
		boolean s = int0 % 5 == 0;
		boolean s1 = int0 % 5 != 0;
		// boolean s2 = int0 / 5 ? true : false;

		System.out.println(s + ", " + s1);

	}

	void juicy(String berry) {
		this.berry = "rasp";
		System.out.println(berry + "berry");
	}
}
