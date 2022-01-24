package functionInterFace;

public class FunctionalInterfaceExample2 {
	public static void main(String s[]) {
		Controller c = (a,b) -> System.out.println(a+b);
		c.ShowMsg(10, 20);
		
		System.out.println("============================================");
		
		
		Controller c1 = (a,b) -> {
				System.out.println(a-b);
		};
		
		c1.ShowMsg(20, 10);
		
		System.out.println("============================================");
		
		
		FunInter f1 = name ->{  
            return "Hello, "+name;  
        };  
		
        f1.method1("Vicky");
    	
		Controller f2 = (a,b) ->  System.out.println(a+b);
			
	}
}
