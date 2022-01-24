package Java8.MethodRefrences;

import java.util.function.BiFunction;

interface Arithmetic{
	 void add(int a,int b);
}

interface operation{
	 int multi(int a,int b);
}

interface operationP{
	 int multi(int a,int b);
	 static int test() {
		return 0;
	}
}


public class StaticMethodExample1 {
	public static void addRefrence(int a,int b) {
		System.out.println(a+b);
	}
	
	public static int multiRef(int a,int b) {
		return a*b;
	}
	
	public static void main(String[] args) {
		Arithmetic a = StaticMethodExample1::addRefrence;
		a.add(10, 20);
		
		BiFunction<Integer, Integer, Integer> b =  StaticMethodExample1::multiRef;
		System.out.println(b.apply(20, 30));
	}
	
	
}
