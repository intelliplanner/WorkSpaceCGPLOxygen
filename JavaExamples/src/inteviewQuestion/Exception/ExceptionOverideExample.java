package inteviewQuestion.Exception;

import java.io.IOException;

/*If the superclass method does not declare an exception, subclass overridden method cannot declare the checked exception but 
it can declare unchecked exception.

If the superclass method declares an exception, subclass overridden method can declare same, subclass exception or no 
exception but cannot declare parent exception.*/

class Parent {
	void sap() throws CustomException{
		System.out.println("test 1");
	}
	
	
//	void test2() {
//		System.out.println("test 2");
//	}
//	
//	
//	void test3() throws IOException{
//		System.out.println("test 3");
//	}
	
}

public class ExceptionOverideExample extends Parent {
//	void sap() {
//		try {
//			System.out.println("test 2");
//		}catch (CustomException e) {
//			// TODO: handle exception
//		}
//	}
//	void test2() throws IOException {
//		System.out.println("test 1");
//	}
	
	void test3() {
		System.out.println("test 3");
	}
	
	
	public static void main(String[] args) {
		ExceptionOverideExample obj = new  ExceptionOverideExample();
//		obj.sap();
		
		Parent obj2 = new  ExceptionOverideExample();
//		obj2.sap();
	}

}
