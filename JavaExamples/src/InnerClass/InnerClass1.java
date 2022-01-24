package InnerClass;

public class InnerClass1{// Local Inner Class Example
 private int data=30;//instance variable
 void display(){
  class Local{
   void msg(){System.out.println(data);}
  }
  Local l=new Local();
  l.msg();
 }
 public static void main(String args[]){
	 InnerClass1 obj=new InnerClass1();
     obj.display();
 }
}
