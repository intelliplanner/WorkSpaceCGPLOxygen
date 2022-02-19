package Sudoku;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SudokuValidate {
	static int N = 9;

	// Function to check if all elements
	// of the board[][] array store
	// value in the range[1, 9]
	static boolean isinRange(int[][] board) {

		// Traverse board[][] array
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {// Check if board[i][j] , lies in the range
				if (board[i][j] <= 0 || board[i][j] > 9) {
					System.out.println("Input Value not in range 1 to 9 at row " + (++i) + " and column " + ++j);
					return false;
				}
			}
		}
		return true;
	}

	// Function to check if the solution
	// of sudoku puzzle is valid or not
	static boolean isValidSudoku(int board[][]) { // Check if all elements of board[][] , stores value in the range[1,
													// 9]
		if (isinRange(board) == false) {
			return false;
		}
		// Stores unique value from 1 to N
		boolean[] unique = new boolean[N + 1];

		for (int i = 0; i < N; i++) { // Traverse each row of the given array

			Arrays.fill(unique, false);// Initialize unique[] , array to false

			for (int j = 0; j < N; j++) { // Traverse each column of current row
				// Stores the value
				// of board[i][j]
				int Z = board[i][j];
				// Check if current row
				// stores duplicate value
				if (unique[Z]) {
					System.out.println("stores duplicate value " + Z + " at row " + (i + 1));
					return false;
				}
				unique[Z] = true;
			}
		}

		// Traverse each column of
		// the given array
		for (int i = 0; i < N; i++) {

			// Initialize unique[]
			// array to false
			Arrays.fill(unique, false);

			// Traverse each row
			// of current column
			for (int j = 0; j < N; j++) {
				// Stores the value
				// of board[j][i]
				int Z = board[j][i];
				// Check if current column
				// stores duplicate value
				if (unique[Z]) {
					System.out.println("stores duplicate value " + Z + " at column " + (i + 1));
					return false;
				}
				unique[Z] = true;
			}
		}

		// Traverse each block of
		// size 3 * 3 in board[][] array
		for (int i = 0; i < N - 2; i += 3) {
			// j stores first column of each 3 * 3 block
			for (int j = 0; j < N - 2; j += 3) {
				// Initialize unique[] // array to false
				Arrays.fill(unique, false);

				// Traverse current block
				for (int k = 0; k < 3; k++) {
					for (int l = 0; l < 3; l++) {

						// Stores row number
						// of current block
						int X = i + k;

						// Stores column number
						// of current block
						int Y = j + l;

						// Stores the value
						// of board[X][Y]
						int Z = board[X][Y];
						// Check if current block
						// stores duplicate value
						if (unique[Z]) {
							System.out.println("stores duplicate value " + Z + " in block ");
							return false;
						}
						unique[Z] = true;
					}
				}
			}
		}

		return true;		// If all conditions satisfied
	}

	static int[][] parseArrayFromList(ArrayList<ArrayList<Integer>> mainList) {
		int arr[][] = new int[mainList.size()][mainList.get(0).size()];
		for (int i = 0; i < mainList.size(); i++) {
			for (int j = 0; j < mainList.get(i).size(); j++) {
				arr[i][j] = mainList.get(i).get(j);
			}
		}

		return arr;
	}

	public static void main(String[] args) {

		File file = new File(
				"E:\\IntelliPlanner Workspaces\\WorkSpaceCGPLOxygen\\JavaExamples\\src\\Sudoku\\input.txt");
		Scanner sc;
		try {
			sc = new Scanner(file);

			List<String> fC = new ArrayList<String>();
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				System.out.println(line);
				fC.add(line);
			}

			ArrayList<ArrayList<Integer>> mainList = new ArrayList<ArrayList<Integer>>();
			for (int i = 0; i < fC.size(); i++) {
				String row = fC.get(i);
				String[] splitRow = row.split("[,]");
				ArrayList<Integer> rowList = new ArrayList<Integer>();
				for (String j : splitRow) {
					Integer k = Integer.parseInt(j.trim());
					rowList.add(k);
				}
				mainList.add(rowList);
			}

			int[][] board = parseArrayFromList(mainList);

			if (isValidSudoku(board)) {
				System.out.println("\nValid");
				
			} else {
				System.out.println("\nNot Valid");
			}
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found");
			e.printStackTrace();
		}

	}

}
