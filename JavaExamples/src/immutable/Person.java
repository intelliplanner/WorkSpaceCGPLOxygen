package immutable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Java Program to demonstrate that making all
 * fields final doesn't make a class Immutable.
 * You can still break immutability if you return
 * reference to mutable objects to client.
 * 
 * @author WINDOWS 8
 *
 */

class Person{
  private final String name;
  private final Date birthday;
  private final List hobbies;
  
  

public Person(String name, Date birthday, List hobbies){
    this.name = name;
    this.birthday = birthday;
    this.hobbies = hobbies;
  }
  public String getName() {
    return name;
  }
  public Date getBirthday() {
    return (Date)birthday.clone();
  }
  public List getHobbies() {
    return Collections.unmodifiableList(hobbies);
  }
  @Override
  public String toString() {
    return "Person [name=" + name + ", birthday=" + birthday + ", hobbies="
        + hobbies + "]";
  }
  
  
  
}