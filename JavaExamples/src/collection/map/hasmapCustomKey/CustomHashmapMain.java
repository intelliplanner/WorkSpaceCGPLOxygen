package collection.map.hasmapCustomKey;

import java.util.HashMap;

public class CustomHashmapMain {
	public static void main(String args[]) {
		HashMap<CustomHashmapKey, String> obj = new HashMap<CustomHashmapKey, String>();
		CustomHashmapKey obj1 = new CustomHashmapKey(2, "Vicky");
		CustomHashmapKey obj2 = new CustomHashmapKey(1, "Virendra");
		CustomHashmapKey obj3 = new CustomHashmapKey(3, "Veeru");
		CustomHashmapKey obj4 = new CustomHashmapKey(3, "Veeru");
		obj.put(obj3, "virendra");
		obj.put(obj2, "virendra");
		obj.put(obj1, "Veeru");
		obj.put(obj4, "visa");
		System.out.println(obj);
	}
}
