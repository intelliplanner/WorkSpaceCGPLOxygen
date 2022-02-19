package collection.map.hasmapCustomKey;

import java.util.HashMap;

public class CustomHashmapMain {
	public static void main(String args[]) {
		HashMap<CustomHashmapKey, String> obj = new HashMap<CustomHashmapKey, String>();
		CustomHashmapKey obj1 = new CustomHashmapKey(1, "Virendra");
		CustomHashmapKey obj2 = new CustomHashmapKey(2, "Vicky");
		CustomHashmapKey obj3 = new CustomHashmapKey(3, "Veeru");
		CustomHashmapKey obj4 = new CustomHashmapKey(4, "Veeru");
		
		obj.put(obj1, "a");
		obj.put(obj2, "b");
		obj.put(obj3, "c");
		obj.put(obj4, "d");
		System.out.println(obj);
	}
}
