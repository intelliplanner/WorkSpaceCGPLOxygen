package Java8.MethodRefrences;
interface Sayable{  
    void say();  
}  
public class StaticMethodRefrences {
	
	    public static void saySomething(){  
	        System.out.println("Hello, this is static method.");  
	    }  
	    public static void main(String[] args) {  
	        // Referring static method  
	        Sayable sayable = StaticMethodRefrences::saySomething;  
	        // Calling interface method  
	        sayable.say();  
	    }  
	
}
