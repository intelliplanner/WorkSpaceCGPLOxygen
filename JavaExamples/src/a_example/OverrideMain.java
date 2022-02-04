package a_example;

public class OverrideMain {
	public static void main(String[] args) {
		System.out.println("parent class main method");
	}
}

class Parent extends OverrideMain {
	public static void main(String[] args) {
		System.out.println("child class main method");
	}
}
