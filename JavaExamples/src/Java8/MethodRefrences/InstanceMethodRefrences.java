package Java8.MethodRefrences;

interface Sayables {
	void say();
}


interface Sayabils {
	default void say() {
		System.out.println("test");
	}
}
public class InstanceMethodRefrences implements Sayabils {

	public void saySomething() {
		System.out.println("Hello, this is insctance method.");
	}

	public static void main(String[] args) {
		// Referring static method
		InstanceMethodRefrences s = new InstanceMethodRefrences();
		Sayables sayable = s::saySomething;
		sayable.say();
		Sayabils se =  new InstanceMethodRefrences();
		se.say();
	}
}
