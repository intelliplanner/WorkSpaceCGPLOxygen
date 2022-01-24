package inteviewQuestion;

public class Mainclass {
	public static void main(String[] args) {
		Employee e1=new Employee("virendra");
//		Employee e2=new Employee("virendra");
		Employee e2=new Employee("virendra");
		Employee e3 = e1;
		System.out.println(e1==e2);
		System.out.println(e1.equals(e2));
		
		
		
		System.out.println(e1==e3);
		System.out.println(e1.equals(e3));
//		
//		try{
//			int i = 100/0;
//		}finally {
//			System.out.println("finally");
//		}
//		
		
	}
}
