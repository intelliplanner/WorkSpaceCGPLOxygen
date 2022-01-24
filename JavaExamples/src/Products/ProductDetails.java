package Products;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;

public class ProductDetails {

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void main(String[] args) {
		Connection conn = null;
		boolean destroyIt = false;
		int count = 1;
		PreparedStatement ps = null;
		int colPos = 1;
		int i = 1;
		int status = 1;
		String query = "INSERT INTO product_details (product_name,price,total_products,status,sales_date) values (?,?,?,?,?)";
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			while (count <= 500) {
				colPos = 1;
				if (count % 5 == 0) {
					i = 1;
				}
				String productName = "product_" + i;
				int price = 100 * i;
				int total_products = i;
				String created_on = createRandomDate();
				ps = conn.prepareStatement(query);
				
				ps.setString(colPos++, productName);
				Misc.setParamInt(ps, price, colPos++);
				Misc.setParamInt(ps, total_products, colPos++);
				Misc.setParamInt(ps, status, colPos++);
				ps.setString(colPos++, created_on);
				ps.executeUpdate();

				i++;
				count++;
			}
			System.out.println("[Row Inserted:" + (count-1) + "]");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (GenericException e) {
				e.printStackTrace();
			}
		}
	}

	public static String createRandomDate() {
		{
			Random gen = new Random();
			int range =  30; // 1 month
			int no = gen.nextInt(range);
			Date currentDate = new Date();
//			System.out.println(dateFormat.format(currentDate));
			Calendar c = Calendar.getInstance();
			c.setTime(currentDate);
			c.add(Calendar.DATE, -no);
			c.add(Calendar.MINUTE, -no);
			Date currentDatePlusOne = c.getTime();
			return dateFormat.format(currentDatePlusOne);
		}
	}

}
