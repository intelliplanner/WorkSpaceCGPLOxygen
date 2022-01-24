/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

/**
 *
 * @author IPSSI
 */
import com.ipssi.gen.utils.DBConnectionPool;
import java.sql.*;

class DBTest {

    public static void main(String args[]) {
        try {
//Class.forName("oracle.jdbc.driver.OracleDriver");  
//  
//Connection con=DriverManager.getConnection(  
//"jdbc:oracle:thin:@localhost:1521:xe","system","oracle");  
            Connection conn = null;

            Class.forName("com.mysql.jdbc.Driver");
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            DatabaseMetaData dbmd = conn.getMetaData();
            String table[] = {""};
            ResultSet rs = dbmd.getTables(null, null, null, table);
            ResultSet resultSet = dbmd.getIndexInfo(null, "ipssi_cgpl", "tp_record", true, false);
            while (rs.next()) {
                System.out.println(rs.getString(3));
            }

            conn.close();

        } catch (Exception e) {
            System.out.println(e);
        }

    }
}
