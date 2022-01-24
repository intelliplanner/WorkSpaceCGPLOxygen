package a_example;

import static java.lang.System.*;

/*
 * @author IPSSI
 */
public class StaticImportExample {
	static void test() {
		out.print("Hello");
	}

	public static void main(String[] args) {
		test();
	}
}
