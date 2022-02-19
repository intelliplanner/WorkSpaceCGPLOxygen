package collection.map;

import java.lang.reflect.Field;
import java.util.HashMap;

public class HashMapTest {
	public static void main(String s[]) {
		// HashMap<Integer,String> map = new HashMap<>();
		// map.put(1, "1");
		// map.put(2, "2");
		// map.put(3, "1");
		// System.out.println(HashMap);
		// for(Map.Entry<Integer,String> m : map.entrySet()) {
		// map.put(m.getValue(), m.getKey());
		// }

		// int a = 35 / 0 ;
		checkHashMapCapcity();
	}

	private static void checkHashMapCapcity() {
		try {
			HashMap m = new HashMap();
			Field tableField;
			tableField = HashMap.class.getDeclaredField("table");
			tableField.setAccessible(true);
			Object[] table = (Object[]) tableField.get(m);
			System.out.println(table == null ? 0 : table.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
