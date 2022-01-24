package inteviewQuestion;

public class StringCompares {
	public static void main(String args1[])
	    {
		   System.out.println("===========Integer============");
		    Integer i1=new Integer(10);
	        Integer i2=new Integer(10);
	        
	        System.out.println(i1==i2);
	        System.out.println(i1.equals(i2));

	        System.out.println("==============String=============");
	        
	        String s1=new String("DELHI");
	        String s2="DELHI";
	        System.out.println(s1==s2);
	        System.out.println(s1.equals(s2));
	        System.out.println(s1.intern()==s2);
	        
	        System.out.println("==============Interview String=============");
	        String sa1 = new String("abc");
	        String sa5 = new String("abc");
	        String sa2= "abc";
	        String sa3="abc";
	        String sa4=sa3;
	        
	        System.out.println(sa1==sa2); //false
	        System.out.println(sa1.equals(sa2));// true
	        System.out.println(sa2==sa3);// true
	        System.out.println(sa3==sa4);// true
	        System.out.println(sa1==sa5); // false
	    }
}
