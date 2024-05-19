package Junit_Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JunitTestCases {

	@Test
	void test() {
		int actual = AssertTest.add(2, 2);
		int expected = 3;
		assertNotEquals(actual,expected,()->"Equals");
	}
	@Test
	void test1() {
		int actual = AssertTest.add(2, 2);
		int expected = 3;
		assertNotEquals(actual,expected,()->"Equals");
	}
}
