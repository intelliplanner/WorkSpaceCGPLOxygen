package a_example;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Test {

	/**
	 * @param args
	 */
	private static Date getMeYesterday(){
	     return new Date(System.currentTimeMillis()-24*60*60*1000);
		
	}
	
	private Date yesterday() {
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, -1);
	    return cal.getTime();
	}
	
	private static String getYesterdayDateString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		String currentDate = dateFormat.format(new Date());
        return currentDate;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	//	String str = "image_blink.png";
		
	
//		System.out.print(System.currentTimeMillis());
		//System.out.println(getYesterdayDateString());
	//	printDateIntoLong();
		
		int i=7;
		System.out.println(i/2);
//		String  
	}

	public static void printDateIntoLong() {
		String dt = "2019-09-14";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Long l;
		try {
			l = dateFormat.parse(dt).getTime();
			System.out.println(l);
			Long m = new Long(l);
			System.out.println(dateFormat.format(new Date(m)));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
