package a_example;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

public class DateTest {
	private static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;

	public static void main(String[] args) {
		Date today = new Date();

		System.out.println("Today     :: " + today(today));
		System.out.println("Next date :: " + findNextDay(today));
		System.out.println("Prev date :: " + findPrevDay(today));

		LocalDate todayDate = LocalDate.now();

		System.out.println("Today     :: " + todayDate);
		System.out.println("Next date :: " + findNextDay(todayDate));
		System.out.println("Prev date :: " + findPrevDay(todayDate));

		LocalDate date = todayDate.with(TemporalAdjusters.firstDayOfMonth());
		System.out.println("First day of month = " + date);
		date = todayDate.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
		System.out.println("Next Tuesday date = " + date);
	}

	private static Date today(Date today) {
		return today;
	}

	private static Date findNextDay(Date date) {
		return new Date(date.getTime() + MILLIS_IN_A_DAY);
	}

	private static Date findPrevDay(Date date) {
		return new Date(date.getTime() - MILLIS_IN_A_DAY);
	}

	private static LocalDate findNextDay(LocalDate localdate) {
		return localdate.plusDays(1);
	}

	private static LocalDate findPrevDay(LocalDate localdate) {
		return localdate.minusDays(1);
	}
}
