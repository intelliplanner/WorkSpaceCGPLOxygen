package Java8.MethodRefrences;

import java.util.function.BiFunction;

public class BiFunctionExam {
	
	static int add(int a,int b) {
		return a+b;
	}
	static float add(float a,int b) {
		return a+b;
	}
	static float add(float a,float b) {
		return a+b;
	}
	
	public static void main(String[] args) 
	{
		BiFunction<Integer,Integer, Integer>add1 = BiFunctionExam :: add;
		BiFunction<Float,Integer, Float>add2 = BiFunctionExam :: add;
		BiFunction<Float,Float, Float>add3 = BiFunctionExam :: add;
		System.out.println(add1.apply(1,2));
		System.out.println(add2.apply(10.9f, 20));
		System.out.println(add3.apply(20.9f,10.1f));
	}
}
