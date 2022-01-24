package InnerClass;

public class LocalInnerClassTest {
	 private int data=30;//instance variable  
	 void display(){  
		 int i=30;
	   class Local{  
	   void msg(){System.out.println(data);}  
	  }  
	  Local l=new Local();  
	  l.msg();  
	 }  
	 public static void main(String args[]){  
		 LocalInnerClassTest obj=new LocalInnerClassTest();  
		 obj.display();  
	 }  
}
