package stringExample;

public class FindSubString {
	public static void main(String args[]) {
		int i = subString("aabcbc","abc"); 
		System.out.println(i);
	}
	
	public static int subString(String s1,String s2) {
		int count = 0;
		
		int m1 = s1.length();
		int m2 = s2.length();
	// abc		aabcbc   		
		String ch;
		int subcount=0;
		int[] indexes = new int[20];
		int i1;
		for(i1=0;i1<m1;i1++) {
			int i2=0;
			while(i2 != m2) {	
				if(s1.charAt(i1) == s2.charAt(i2)) { 
					break;
				}
				i2++;
				indexes[indexes.length-1] = i1-1;
				if(count == m2); 
					subcount++;
				
			}
		}
		return count;
	} 
}
