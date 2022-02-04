package a.immutable_class;

import java.util.List;
import java.util.Map;
import java.util.Set;


//java 9
public class ImmutableCollection {

	public static void main(String[] args) {
		ImmutableCollectionList();
		ImmutableCollectionSet();
		ImmutableCollectionMap();
	}

	private static void ImmutableCollectionMap() {
//		Map<String, String> names = Map.ofEntries(
//                Map.entry("1", "Lokesh"),
//                Map.entry("2", "Amit"),
//                Map.entry("3", "Brian"));
//         
//        System.out.println(names);
         
        //UnsupportedOperationException
        //names.put("2", "Ravi");
	}

	private static void ImmutableCollectionSet() {

//        Set<String> names = Set.of("Lokesh", "Amit", "John");
//         
//        //Elements order not fixed
//        System.out.println(names);
         
        //names.add("Brian"); //UnsupportedOperationException occured
         
        //java.lang.NullPointerException
        //Set<String> names2 = Set.of("Lokesh", "Amit", "John", null); 
         
        //java.lang.IllegalArgumentException
        //Set<String> names3 = Set.of("Lokesh", "Amit", "John", "Amit");
	}

	private static void ImmutableCollectionList() {
//		List<String> names = List.of("Lokesh", "Amit", "John");
//        
//        //Preserve the elements order
//        System.out.println(names);
         
        //names.add("Brian"); //UnsupportedOperationException occured
         
        //java.lang.NullPointerException
        //List<String> names2 = List.of("Lokesh", "Amit", "John", null); 
	}

}
