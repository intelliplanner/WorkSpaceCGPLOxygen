package a.basic_program;

public class InstanceBlockTest {
	int speed;
	{
		speed = 10;
		System.out.println("Instance Block call");
	}

	InstanceBlockTest() {
		System.out.println("contructor Block call, speed:" + speed);
	}

	public static void main(String[] args) {
		System.out.println("test");
		new InstanceBlockTest();
	}

}
