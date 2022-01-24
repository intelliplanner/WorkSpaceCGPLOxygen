package TypeInferenceJava9;

public class BoxDemo {
	/** This class is a user-defined class that contains one methods cube.*/  
	public static <U> void addBox(U u, java.util.List<Box<U>> boxes) {
		Box<U> box = new Box<>();
		box.set(u);
		boxes.add(box);
	}

	public static <U> void outputBoxes(java.util.List<Box<U>> boxes) {
		int counter = 0;
		for (Box<U> box : boxes) {
			U boxContents = box.get();
			System.out.println("Box #" + (counter+1) + " contains [" + boxContents.toString() + "]");
			counter++;
		}
	}

	public static void main(String[] args) {
		java.util.ArrayList<Box<Integer>> listOfIntegerBoxes = new java.util.ArrayList<>();
		BoxDemo.<Integer>addBox(Integer.valueOf(10), listOfIntegerBoxes);
		BoxDemo.addBox(Integer.valueOf(20), listOfIntegerBoxes);
		BoxDemo.addBox(Integer.valueOf(30), listOfIntegerBoxes);
		BoxDemo.outputBoxes(listOfIntegerBoxes);
		
		java.util.ArrayList<Box<String>> listOfStringBoxes = new java.util.ArrayList<>();
		BoxDemo.<String>addBox("test1", listOfStringBoxes);
		BoxDemo.addBox("test2", listOfStringBoxes);
		BoxDemo.addBox("test3", listOfStringBoxes);
		BoxDemo.outputBoxes(listOfStringBoxes);
	}
}
