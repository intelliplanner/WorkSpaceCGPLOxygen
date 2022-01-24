package a_example;
import java.util.*;
import java.io.*;

public class Madlibs {

	/**
	 * Displays Intro
	 * */
	private static void intro() {
		System.out.println("Welcome to the game of Mad Libs.");
		System.out.println("I will ask you to provide several words");
		System.out.println("and phrases to fill in a mad lib story.");
		System.out.println("The result will be written to an output file.");
	}

	public static void main(String[] args) throws FileNotFoundException {
		Scanner input = new Scanner(System.in);
		String choice = "";
		intro();

		while (true) {
			System.out.print("(C)reate mad-lib, (V)iew mad-lib, (Q)uit? ");
			choice = input.nextLine();

			if (choice.equals("c")) {
				choice = choice.toLowerCase();
				createMadLib(input);
				System.out.println("Your MadLib story has been created.");

			}

			else if (choice.equals("v")) {
				File file = getFile(input);
				viewMadLib(file, input);
			}

			else if (choice.equals("q")) {
				System.exit(0);
			}
		}

	}

	/***
	 * 
	 * @param input
	 * @throws FileNotFoundException
	 */
	public static void createMadLib(Scanner input) throws FileNotFoundException {
		System.out.println();
		File inputFile = getFile(input);
		System.out.print("Output file name: ");
		String outputFile = input.nextLine();
		System.out.println();
		PrintStream output = new PrintStream(new File(outputFile));
		createMadLibararyFile(inputFile, output, input);

	}

	/**
	 * returns the file
	 */
	public static File getFile(Scanner input) {
		System.out.print("Input file name: ");
		String fileName = input.nextLine();
		File f = new File(fileName);
		while (!f.exists()) {
			System.out.print("File not found. Try again: ");
			fileName = input.nextLine();
			f = new File(fileName);
		}
		return f;
	}
	
	/**
	 * Shows the Mad Lib file
	 * */
	public static void viewMadLib(File outputFile, Scanner input)
			throws FileNotFoundException {
		System.out.println();
		input = new Scanner(outputFile);
		while (input.hasNextLine()) {
			String str = input.nextLine();
			System.out.println(str);
		}
		System.out.println();

	}

	/**
	 * returns if it is vowel
	 * */
	public static boolean isVowel(String value) {
		if (value.startsWith("a") || value.startsWith("A")
				|| value.startsWith("e") || value.startsWith("E")
				|| value.startsWith("i") || value.startsWith("I")
				|| value.startsWith("o") || value.startsWith("O")
				|| value.startsWith("u") || value.startsWith("U")) {
			return true;
		} else
			return false;
	}

	/**
	 * returns length
	 * */
	public static int getLength(String word) {
		int length = word.length();
		if (word.substring(length - 1).equals(">")) {
			return (length);
		} else
			return 1;

	}

	/***
	 * creates MadLib file
	 */
	public static void createMadLibararyFile(File inputFile,
			PrintStream output, Scanner input) throws FileNotFoundException {

		input = new Scanner(inputFile);
		while (input.hasNextLine()) {
			String line = input.nextLine();
			Scanner token = new Scanner(line);
			while (token.hasNext()) {
				int length = 0;
				String word = token.next();
				if (word.substring(0, 1).equals("<")) {
					length = getLength(word);

					if (length > 2) {
						String placeHolder = word.substring(1,
								length - 1);
						String placeHolder2 = placeHolder.toLowerCase()
								.replaceAll("-", " ");
						System.out.print("Please type ");
						boolean vowel = isVowel(placeHolder);
						if (vowel) {
							System.out.print("an " + placeHolder2 + ": ");
						} else {
							System.out.print("a " + placeHolder2 + ": ");
						}
						String newPlaceHolder = input.nextLine();
						output.print(newPlaceHolder + " ");
					} else
						output.print(word + " ");
				} else
					output.print(word + " ");
			}
			output.println("");
		}

	}

}
