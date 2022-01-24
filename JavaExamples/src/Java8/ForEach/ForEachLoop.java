package Java8.ForEach;

import java.util.ArrayList;
import java.util.List;

public class ForEachLoop {

	public static void main(String[] args) {
		 List<String> gamesList = new ArrayList<String>();  
	        gamesList.add("Football");  
	        gamesList.add("Cricket");  
	        gamesList.add("Chess");  
	        gamesList.add("Hocky");
	        
	        System.out.println("------------1. Iterating by Lamda Expressin---------------");  
	        gamesList.forEach(games -> System.out.println(games));
	        
	        System.out.println("------------2. Iterating by passing method reference---------------");
	        gamesList.forEach(System.out::println);
	        
	        System.out.println("------------3. Iterating by passing method reference---------------");
	        gamesList.stream().forEach(System.out::println);
	        
	        System.out.println("------------4. Iterating by Lamda Expressin---------------");  
	        gamesList.stream().forEachOrdered(game->System.out.println(game));
	        
	        
	        System.out.println("------------5. Iterating by passing method reference---------------");
	        gamesList.stream().forEachOrdered(System.out::println);
	        
	        
	        System.out.println("------------6. Iterating by  Lamda Expressin---------------");
	        gamesList.forEach(
	        		n->{System.out.println(n);}
	        		
	        		);
	        
	        
	        
	        
	        
	}
}
