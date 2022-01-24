/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RandomNo;

import com.ipssi.gen.utils.DBConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

/**
 *
 * @author Vi$ky
 */
public class RandomNo {

    public static void main(String a[]) {
        int no1 = getRandomNumber(9);
        System.out.println(" Number1: " + no1);
        int no2 = getRandomNumber(122);
        System.out.println(" Number2: " + no2);
        String ch1 = getRandomCharactor(no2);
        int no3 = getRandomNumber(9);
        System.out.println(" Number3: " + no3);
        int no4 = getRandomNumber(122);
        String ch2 = getRandomCharactor(no4);
        System.out.println(" Number4: " + no4);
        int no5 = getRandomNumber(9);
        System.out.println(" Number5: " + no5);
        String generateRandomNo = no1 + ch1 + no3 + ch2 + no5;
        System.out.println("Random Number: " + generateRandomNo);
        insertRandomNumber(generateRandomNo);
    }

    public static int getRandomNumber(int val) {
        Random t = new Random();
        return t.nextInt(val);
    }

    public static String getRandomCharactor(int Ascii_Val) {
        if (Ascii_Val < 65) {
            int i = 65 - Ascii_Val;
            Ascii_Val += i;
        } else if (Ascii_Val == 91 || Ascii_Val == 96 || Ascii_Val < 96) {
            int i = 97 - Ascii_Val;
            Ascii_Val += i;
        }


        String aChar = new Character((char) Ascii_Val).toString();// 65-90(A-Z) to 97-122(a-z) range of Charactors
        return aChar;
    }

    public static void insertRandomNumber(String generateRandomNo) {
        Connection conn = null;
        boolean destroyIt = false;
        PreparedStatement ps = null;
        int parameterIndex = 1;
        String query = "insert into test_random (random) values (?)";
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            ps = conn.prepareStatement(query);
            ps.setString(parameterIndex++, generateRandomNo);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
            } catch (Exception ex) {
                destroyIt = true;
                ex.printStackTrace();
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                System.out.println("Exception: " + ex);
            }
        }
    }
    
     public static void verifyRandomNumber(String generateRandomNo) {
        Connection conn = null;
        boolean destroyIt = false;
        PreparedStatement ps = null;
        int parameterIndex = 1;
        String query = "insert into test_random (random) values (?)";
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            ps = conn.prepareStatement(query);
            ps.setString(parameterIndex++, generateRandomNo);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
            } catch (Exception ex) {
                destroyIt = true;
                ex.printStackTrace();
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                System.out.println("Exception: " + ex);
            }
        }
    }
}
