package a_example;

public class SequenceTest {
	static {
		// System.out.print("A ");
		System.out.println("Inside Static Block.");
		System.exit(1);
	}

	{
		System.out.print("C ");
	}

	SequenceTest() {
		System.out.print("D ");
	}

	public static void main(String[] args) {
		System.out.print("B ");
		new SequenceTest().go();
	}

	private void go() {
		System.out.print("E ");
	}

}
