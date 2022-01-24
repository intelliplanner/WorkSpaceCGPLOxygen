package JavaAnnotations;

class TestAnnotation1{  
void eatSomething(){System.out.println("eating something");}

}  
  
class Dog extends TestAnnotation1{  
@Override  
void eatSomething(){System.out.println("eating foods");}//should be eatSomething  
}  
  
class Animals{  
public static void main(String args[]){  
	TestAnnotation1 a=new Dog();  
a.eatSomething();  
}}  