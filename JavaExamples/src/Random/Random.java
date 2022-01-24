package Random;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Random {
	 static String getRandomString() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
	}
	
	 public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			System.out.println(getRandomString());
		}
		
	}
}
