package inteviewQuestion.Exception;

import java.sql.SQLException;

public class CustomTest {
	public static void main(String[] args) {
		try {
			validate(16);
		} catch (SQLException e) {

		}

	}

	static void validate(int age) throws SQLException {
		if (age < 18) {
			throw new SQLException("age is not valid to vote");
		} else {
			System.out.println("welcome to vote");
		}
	}

	// void testCase1() throws IOException{

	// try{
	// int x = 10/0;
	// }catch(IOException e) { // same Exception as throws
	// System.out.println(e);
	//// throw new Exception();
	// }finally {
	// System.out.println("Finny Called After Throw");
	// }
	// }

}
