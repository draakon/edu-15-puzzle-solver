package ui;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Set;

import solver.*;
import solver.Field.Direction;
import solver.Field.IllegalMoveException;
import solver.Search.UnsolvableException;

/**
 * 15-m‰ngu (vıi 3x3 vıi 5x5) lahendaja.<br>
 * T‰psemalt algoritmist klassides {@link Field} ja {@link Search}.<br>
 * Antud klass sisaldab lihtsat tekstipıhist liides lahendatavate 
 * m‰nguv‰ljade sisestamiseks.
 * 
 * @author Kristjan Kaitsa
 * @version 1.0
 * 
 */
public class M‰ng15 {
	private static Scanner input = new Scanner(System.in);
	
	/**
	 * <code>main</code>-meetod, mis asub kasutajalt lahendatava m‰nguv‰lja
	 * p‰rimist ning seej‰rel v‰ljastab lahenduse.
	 * @param args Sisendparameetreid ei kasutata.
	 */
	public static void main(String[] args) {
		//int[][] test = { {15, 13, 12, 4}, {2, 0, 11, 10}, {14, 8, 6, 5}, {9, 3, 7, 1} };
		//int[][] test = { {3, 8, 2}, {1, 5, 4}, {0, 6, 7}};
		//int[][] test = { {1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 0, 14, 15} };
		//int[][] test = { {5, 9, 6, 3}, {11, 14, 7, 2}, {12, 10, 4, 8}, {15, 0, 1, 13} };
		
		// P‰is
		System.out.println("15-m2ngu lahendaja");
		System.out.println("Autor: Kristjan Kaitsa");
		System.out.println("------------------\n");
		
		// M‰nguv‰lja mııde
		System.out.print("Sisestage m2nguv2lja m66de [3,4,5]: ");
		int n = 0;
		try {
			n = input.nextInt();
		} catch (InputMismatchException e) {
			System.err.println("Vigane sisend, sisestage ainult numbreid.");
			System.exit(-1);
		}
		if (n < 3 || n > 5) {
			System.err.println("Sobimatu m66de: lubatud 3, 4 vıi 5.");
			System.exit(-1);
		}
		
		input.nextLine(); // Puhastame buffri
		
		// M‰nguv‰lja kasutajalt "kogumine"
		System.out.println("\nSisestage m2nguv2li rida-rea kaupa.");
		System.out.println("Iga veeru j2rel vajutage tyhikut ning rea j2rel reavahetust.");
		System.out.println("Tyhja ruudu kohale sisestage 0.");
		System.out.println("T2psemad juhised kasutusjuhendis.\n");
		
		int[][] fieldToSolve = getInputField(n);
		
		// Otsing
		Search pathFinder = null;
		try {
			pathFinder = new Search(new Field(fieldToSolve));
		} catch (UnsolvableException e1) {
			System.err.println("Antud v2li ei ole lahendatav!");
			System.exit(-1);
		}
						
		(new Thread(pathFinder)).start();
		
		System.out.print("\nPalun oodake, lahendan: ");
		while (pathFinder.getMovesToSolution() == null) {
			System.out.print('*');
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) { }
		}
		
		// Tulemuse v‰ljastamine
		System.out.println();
		System.out.println("\nTeha liigutused*: " + pathFinder.getMovesToSolution().replace('‹', 'Y'));
		System.out.println("K2ike: " + pathFinder.getMovesToSolution().length());
		System.out.println("Aega kulus lahendamiseks: " + pathFinder.getTime() + " ms");
		System.out.println("Vaadati l2bi olekuid: " + pathFinder.getStates());
		System.out.println("\n* liigutatakse tyhja ruutu m2rgitud suunas: ");
		System.out.println("Y - ylesse");
		System.out.println("P - paremale");
		System.out.println("A - alla");
		System.out.println("V - vasakule");
		
		outputToFile(new Field(fieldToSolve), pathFinder.getMovesToSolution(), "lahendus.txt" );
		
		
	}
	
	/**
	 * Vıtab kasutajalt parameetris m‰‰ratud dimensiooniga
	 * m‰nguv‰lja ning valideerib selle esmast korrektsust
	 * jooksvalt.<br>
	 * Vigade avastamiseks v‰ljutakse.
	 * @param n M‰nguv‰lja dimensioon (15-m‰ngu ehk 4x4 puhul 4)
	 * @return Tagastab kahemııtmelise massiivi m‰nguv‰lja numbritega.
	 */
	private static int[][] getInputField(int n) {
		Set<Integer> used = new HashSet<Integer>();
		int[][] inputData = new int[n][n];
		for (int i = 0; i < n; i++) {
			String line = input.nextLine();
			String[] numbers = line.split(" ");
			if (numbers.length != n) {
				System.err.println("Real ebakorrektne arv numbreid!");
				System.exit(-1);
			}
			int j = 0;
			for (String number : numbers) {
				int intNumber = 0;
				try {
					intNumber = Integer.parseInt(number);
				} catch (NumberFormatException e) {
					System.err.println("Vigane sisend, sisestage ainult numbreid.");
					System.exit(-1);
				}
				if (intNumber < 0 || intNumber >= n*n || used.contains(intNumber)) {
					System.err.println("Rida sisaldab illegaalset vıi korduvat arvu.");
					System.exit(-1);
				}
				inputData[i][j++] = intNumber;
				used.add(intNumber);
			}
		}
		return inputData;
	}
	
	private static void outputToFile(Field initField, String moves, String filename) {
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("Lahendus k‰ikhaaval:");
			out.newLine();
			out.write("Algseis:");
			out.newLine();
			out.write(initField.toString().replace("\n", "\r\n"));
			out.newLine();
			Field field = initField;
			for (int i = 0; i < moves.length(); i++) {
				try {
					switch (moves.charAt(i)) {
					case '‹':
						out.write("‹lesse");
						field = new Field(field, Direction.NORTH);
						break;
					case 'P':
						out.write("Paremale");
						field = new Field(field, Direction.EAST);
						break;
					case 'A':
						out.write("Alla");
						field = new Field(field, Direction.SOUTH);
						break;
					case 'V':
						out.write("Vasakule");
						field = new Field(field, Direction.WEST);
						break;
					}
					out.newLine();
					out.write(field.toString().replace("\n", "\r\n"));
					out.newLine();
				} catch (IllegalMoveException e) {
					System.err.println("Faili v2ljastamine eba6nnestus!");
					System.exit(-1);
				}
			}
			out.close();
			fstream.close();
		} catch (IOException e1) {
			System.err.println("Faili kirjutamine eba6nnestus!");
			System.exit(-1);
		}
	}

}
