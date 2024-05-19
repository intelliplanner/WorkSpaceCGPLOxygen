package a_example;

import java.util.Scanner;

public class ParkingCalculater {

	public static void main(String str[]) {

		System.out.println("============= Welcome to Parking System ==============");

		String command = "c"; // c for continue and e for exit
		while (!command.equalsIgnoreCase("e")) {
			double perHourCharge = 20; // 2o rs per hours
			System.out.println("Enter hours parked for car: ");
			Scanner hrs = new Scanner(System.in);
			double hrss = hrs.nextDouble();
			int finalCharge = 0;
			double rem = hrss % 2;
			for (int i = 1; i < hrss;) {
				finalCharge += perHourCharge;
				i += 2;
			}
			if (rem != 0) {
				finalCharge += perHourCharge;
			}
			System.out.println("final Parking Charge :" + finalCharge + " Rs.");

			System.out.println("Press c for continue and press e for exit: ");

			Scanner cmd = new Scanner(System.in);
			command = cmd.next();
		}

		System.out.println("============= Thanks to Visit ==============");

	}

}
