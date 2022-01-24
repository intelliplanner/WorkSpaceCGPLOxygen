package Java8.Java8Example;

import java.util.Optional;

public class StringOptional {

	public static void main(String[] args) {
		String[] str = new String[5];
		str[0]="";
		str[1]="";
		str[2]="st";
		str[3]="ss";
		str[4]="sa";
		Optional<String> checkNull = Optional.ofNullable(str[3]);
		
		
		if(checkNull.isPresent()) {
			System.out.println("Not Nullable");
		}else {
			System.out.println("Nullable");
		}
		
		 checkNull.ifPresent(System.out::println);   // printing value by using method reference  
	     System.out.println(checkNull.get());    // printing value by using get method  
	}

}
