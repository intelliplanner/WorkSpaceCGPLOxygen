package immutable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PersonTest {  

	  public static void main(String args[]) {
	     Calendar cal = Calendar.getInstance();
	     cal.set(1982, 4, 21);
	     Date birthDate = cal.getTime();
	     
	     List hobbies = new ArrayList<>();     
	     hobbies.add("Painting");
	     hobbies.add("Travelling");
	     hobbies.add("Fitness");
	    
	     Person robin = new Person("Robin", birthDate, hobbies);
	     System.out.println("Before");
	     System.out.println(robin);
	     
	     // if it's immutable you can't change the object
	     Date birthday = robin.getBirthday();
	     birthday.setTime(System.currentTimeMillis());
//	     
//	     List originalHobbies = robin.getHobbies();
//	     originalHobbies.add("newFitness");
	  
	     System.out.println("After");
	     System.out.println(robin);
	     
	  }

	}
