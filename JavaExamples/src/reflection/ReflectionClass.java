/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reflection;


/**
 *
 * @author Vicky
 */
public class ReflectionClass implements Cloneable{
	int id;
	String name;
	public ReflectionClass(int id,	String name){
		this.id=id;
		this.name=name;
	}
	
	public Object getClone() throws CloneNotSupportedException {
		return super.clone();
	}
	
  public static void main(String []args) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
       try{  
            Class c = Class.forName("Test");
//            Test t = c.getClass();
//            System.out.println(c.getName());
//            System.out.println(c.isAnnotation());
//            System.out.println(c.isArray());
//            System.out.println(c.isInterface());
//            System.out.println(c.isLocalClass());
//            System.out.println(c.isEnum());
//            System.out.println(c.isPrimitive());
//            System.out.println(c.getModifiers());
//            System.out.println(c.getConstructors());
//            System.out.println(c.isLocalClass());
//            Test s=(Test)c.newInstance();  
//            System.out.println(s.printVal());  
            ReflectionClass obj= new ReflectionClass(10,"virendra");
            ReflectionClass obj2= (ReflectionClass)obj.clone();
            System.out.print(obj2.name);
  
  }catch(Exception e){System.out.println(e);}  
  
 }  
    
}
