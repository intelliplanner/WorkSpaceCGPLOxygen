package inteviewQuestion.Wipro;

import java.util.Arrays;
import java.util.List;

/* Sort arraylist without any predefind function*/
public class WiproExample {
	public static void main(String r[]) {
		List<Integer> lst = Arrays.asList(10, 5, 20, 18, 21, 19);
		System.out.println(lst);
		for (int i = 0; i < lst.size(); i++) {
			for (int j = lst.size() - 1; j > i; j--) {
				if (lst.get(i) > lst.get(j)) {
					int temp = lst.get(j);
					int temp1 = lst.get(i);
					lst.set(i,temp);
					lst.set(j, temp1);
					System.out.println(lst);
				}
			}
		}
		
		System.out.println(lst);
	}
}
