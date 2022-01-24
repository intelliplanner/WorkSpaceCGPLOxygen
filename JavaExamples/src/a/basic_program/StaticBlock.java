package a.basic_program;

public class StaticBlock {
	static {
		System.out.println("static block is invoked");
		// System.exit(0);
		test();
	}

	public static void test() {
		System.out.println("test()");
	}

	public static void main(String[] args) {

		new StaticBlock();
		try {
			System.out.println("try block call");
			return;
		} finally {
			System.out.println("finally");
		}

	}

}
