package Sudoku;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "sudoku", mixinStandardHelpOptions = true, version = "checksum 4.0", description = "validates Sudoku numbers")
public class Sudoku implements Runnable {

	@Option(names = { "-f", "--file" }, required = true, description = "Filename")
	File file;

	@Override
	public void run() {
		// File file = new File("src/main/java/input.txt");
		Scanner scan = null;
		List<String> is_valid = new ArrayList<String>();
		List<String> validate = new ArrayList<String>();
		for (int i = 1; i < 10; i++) {
			is_valid.add("VALID");
		}

		try {
			scan = new Scanner(file);
		} catch (FileNotFoundException fnfe) {
			System.out.println(fnfe);
		}

		List<Integer> numList = new ArrayList<Integer>();
		for (int i = 1; i < 10; i++) {
			numList.add(i);
		}

		String fileContents = "";
		List<String> fC = new ArrayList<String>();

		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			fC.add(line);
		}

		for (int i = 0; i < 9; i++) {
			String row = fC.get(i);
			String[] splitRow = row.split("[,]");

			List<Integer> rowList = new ArrayList<Integer>();
			for (String j : splitRow) {
				Integer k = Integer.parseInt(j);
				rowList.add(k);
			}
			rowList.sort(Comparator.naturalOrder());

			if (rowList.equals(numList)) {
				validate.add("VALID");
			} else {
				validate.add("INVALID");
			}
		}

		if (validate.equals(is_valid)) {
			System.out.println("VALID");
		} else {
			System.out.println("INVALID");
		}
	}

	public static void main(String[] args) {
		CommandLine.run(new Sudoku(), System.err, args);
	}

}
