package InnerClass;

public class MemberInner {
		 private int data=30;
		 static int d=10;
		 class Inner{
		 static final int i = 2;	 
		  void msg(){System.out.println("data is "+data + d);}  
		 }  
		 public static void main(String args[]){  
			 MemberInner obj=new MemberInner();  
			 MemberInner.Inner in=obj.new Inner();  
			 in.msg();
			 System.out.println(MemberInner.Inner.i);
		 }  
}
