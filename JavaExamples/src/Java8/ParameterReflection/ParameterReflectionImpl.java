package Java8.ParameterReflection;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ParameterReflectionImpl {

	public static void main(String str[]) {

		try {
			Calculate c = new Calculate();
		    Class cls = c.getClass();
		    Method[] mthd = cls.getDeclaredMethods();
		for (Method m:mthd) {
			System.out.println("m:" +m.getName());
		//	Parameter[] p = m.getParameters();
			for(Parameter pm : m.getParameters()) {
					System.out.println(pm.getName()+", type"+pm.getParameterizedType());
			}
		}
		}catch(Exception e) {
			System.out.println(e);
		}
	}
}
