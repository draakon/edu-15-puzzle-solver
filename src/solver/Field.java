package solver;

import java.awt.Point;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * <code>Field</code> klass kujutab endast m�nguv�lja seisu<br>
 * ning v�imaldab sellega manipuleerida ja seda kontrollida.
 * 
 * @author Kristjan Kaitsa
 * @version 1.0
 * 
 */
public class Field implements Comparable<Field> {
	/** T�hja ruudu asukoht. */
	private Point emptyPoint;
	/** M�nguv�lja dimensioon (15-m�ngu ehk 4x4 puhul 4). */
	private int dimension;
	/** M�nguv�lja ruutude v��rtused kahedimensionaalses massiivis. */
	private int[][] values;
	/** Antud v�ljale eelnev v�li ehk v�li, millest �he liigutusega on
	 * v�imalik selle v�ljani j�uda. */
	private Field parent;
	/** K�ikude arv, mis vaja sooritada, et algseisust antud v�ljani j�uda. */
	private int moves;
	/** Massiiv kujutamaks oodatava lahendatud v�lja numbrite paigutust. */
	private int[][] solvedValues;
	/** Antud v�lja k�igi ruutude Manhattani kauguste summa nende n�utud kohast. */ 
	private int manhattanDistance;
	
	/**
	 * Koostab uue v�lja vastavalt ette antud seisule.<br>
	 * Kasutatakse algseisu p�hjal v�lja loomiseks -
	 * ei oma eelasi.
	 * @param values Kahedimensionaalne massiiv v�lja numbritega.
	 */
	public Field(int[][] values) {
		this.moves = 0;
		this.dimension = values.length;
		this.solvedValues = generateSolved();
		this.values = new int[dimension][dimension];
		for (int y = 0; y < dimension; y++) {
			for (int x = 0; x < dimension; x++) {
				this.values[y][x] = values[y][x];
				if (values[y][x] == 0)
					this.emptyPoint = new Point(x, y);
			}
		}
		this.manhattanDistance = this.calculateManhattanDistance();
	}
	
	/**
	 * Koostab uue v�lja vastavalt ette antud v�ljale ning
	 * t�hja ruudu liigutamise suunale.
	 * @param parent Antud v�lja eelane.
	 * @param emptyMove T�hja ruudu liigutamise suund.
	 * @throws IllegalMoveException Visatakse, kui antud suunas ei ole
	 * v�imalik t�hja ruutu liigutada.
	 */
	public Field(Field parent, Direction emptyMove) throws IllegalMoveException {
		this.parent = parent;
		this.moves = parent.getMoves() + 1;
		this.dimension = parent.getDimension();
		this.emptyPoint = parent.getEmptyPoint();
		
		Point destPoint = directionToPoint(this.emptyPoint, emptyMove);
		
		this.solvedValues = parent.getSolvedValues();
		this.values = new int[dimension][dimension];
		for (int i = 0; i < dimension; i++)
			System.arraycopy(parent.getValues()[i], 0, this.values[i], 0, dimension);
		
		swapElements(this.emptyPoint, destPoint);
		this.emptyPoint = destPoint;
		this.manhattanDistance = this.calculateManhattanDistance();
	}
	
	/**
	 * @return Tagastab m�nguv�lja ruutude v��rtused kahe-dimensionaalse massiivina.
	 */
	public int[][] getValues() {
		return this.values;
	}
	
	/**
	 * @return Tagastab m�nguv�lja dimensiooni (nt. 4x4 ehk 15-m�ngu puhul 4).
	 */
	public int getDimension() {
		return this.dimension;
	}
	
	/**
	 * @return Tagastab t�hja ruudu asukoha m�nguv�ljal.
	 */
	public Point getEmptyPoint() {
		return this.emptyPoint;
	}
	
	/**
	 * @return Tagastab antud v�ljani j�udmiseks vajaminevate k�ikude arvu algolekust.
	 */
	public int getMoves() {
		return this.moves;
	}
	
	/**
	 * @return Tagastab oodatava lahendatud v�lja v��rtused.
	 * Antakse edasi v�ljalt-v�ljale, et ei peaks alati uuesti arvutama.
	 */
	public int[][] getSolvedValues() {
		return this.solvedValues;
	}
	
	/**
	 * @return Tagastab antud v�lja vahetu eelase ehk v�lja, millest �he liigutusega on
	 * v�imalik selle v�ljani j�uda.
	 */
	public Field getParent() {
		return parent;
	}
	
	/**
	 * Vahetab antud v�lja esivanema v�lja.<br>
	 * Vajalik A* algoritmis toimuva tippudevaheliste viitade �mberorienteerimiseks.
	 * @param parent Antud v�ljale uus eelane.
	 */
	public void setParent(Field parent) {
		this.parent = parent;
		this.moves = parent.getMoves() + 1;
	}
	
	public int getManhattanDistance() {
		return this.manhattanDistance;
	}
	
	/**
	 * Hinnangufunktsioon: f(n)= <b>g(n)</b> + 3 * <b>h*(n)</b>.<br>
	 * <ul>
	 * <li><b>g(n)</b> - siia olekusse j�udmiseks l�bitud tippude arv.</li>
	 * <li><b>h*(n)</b> - minimaalne k�ikude arv, mis siit l�pptippu j�udmiseks
	 * teha oleks vaja.</li>
	 * </ul>
	 * Kiiremini lahenduse leidmiseks ohverdatakse algoritmi optimaalsus ning
	 * <b>h*(n)</b> korrutatakse kolmega.
	 * @return Tagastab antud v�lja hinnangu.
	 */
	public int getHeuristicValue() {
		return this.moves + 3 * this.getManhattanDistance();
	}
	
	/**
	 * Kontrollib, kas antud v�lja n�ol on tegemist lahendatud v�ljaga.
	 * @return T�ev��rtuse.
	 */
	public boolean isSolved() {
		if (Arrays.deepEquals(this.values, this.solvedValues))
			return true;
		else
			return false;
	}
	
	/**
	 * Genereerib antud v�ljale k�ik legaalsed j�rglased va.
	 * tagasik�igu (ehk antud v�lja eelase).
	 * @return Lingitud listi antud v�lja j�rglastega.
	 */
	public List<Field> getChildren() {
		List<Field> children = new LinkedList<Field>();
		for (Direction direction : Direction.values()) {
			try {
				Field child = new Field(this, direction);
				if ((parent == null) || 
						((parent != null) && (!parent.getEmptyPoint().equals(child.getEmptyPoint()))))
					children.add(child);				
			} catch (IllegalMoveException e) {
				continue;
			}
		}
		return children;
	}
	
	/**
	 * Genereerib vastavalt m�ngu dimensioonile oodatava lahenduse
	 * massiivi. Eeldatakse, et lahenduses peavad numbrid
	 * olema j�rjest �levalt vasakult kuni alla paremale ning t�hi
	 * ruut (ehk 0) on k�ige l�pus.
	 * @return Tagastab lahendatud v�lja massiivi.
	 */
	public int[][] generateSolved() {
		int number = 1;
		int[][] solved = new int[dimension][dimension];
		for (int y = 0; y < dimension; y++) {
			for (int x = 0; x < dimension; x++) {
				if ((y == dimension - 1) && (x == dimension - 1))
					solved[y][x] = 0;
				else
					solved[y][x] = number;
				number++;
			}
		}
		return solved;
	}
	
	/**
	 * Arvutab koordinaadid, kuhu j�utakse <code>basePoint</code>'i liigutades
	 * m��ratud suunas (<code>direction</code>).
	 * @param basePoint Ruut, mida soovitakse liigutada.
	 * @param direction Suund, kuhu <code>basePoint</code> liigutada.
	 * @return Tagastab ruudu koordinaadid, kuhu liigutuse sooritamisel j�utakse.
	 * @throws IllegalMoveException Visatakse, kui k�ik ei ole 15-m�ngu
	 * reeglite j�rgi legaalne.
	 */
	private Point directionToPoint(Point basePoint, Direction direction) throws IllegalMoveException {
		if ((basePoint.y < 0) || (basePoint.y >= dimension))
			throw new IllegalMoveException();
		if ((basePoint.x < 0) || (basePoint.x >= dimension))
			throw new IllegalMoveException();
		
		Point destPoint = new Point();
		
		switch (direction) {
		case NORTH:
			if (emptyPoint.y == 0)
				throw new IllegalMoveException();
			destPoint.setLocation(emptyPoint.x, emptyPoint.y - 1);
			break;
		case EAST:
			if (emptyPoint.x >= dimension - 1)
				throw new IllegalMoveException();
			destPoint.setLocation(emptyPoint.x + 1, emptyPoint.y);
			break;
		case SOUTH:
			if (emptyPoint.y >= dimension - 1)
				throw new IllegalMoveException();
			destPoint.setLocation(emptyPoint.x, emptyPoint.y + 1);
			break;
		case WEST:
			if (emptyPoint.x == 0)
				throw new IllegalMoveException();
			destPoint.setLocation(emptyPoint.x - 1, emptyPoint.y);
			break;
		}
		return destPoint;
	}
	
	/**
	 * Arvutab liikumissuuna, et <code>fromPoint</code> liigutada koordinaatidele
	 * <code>toPoint</code>. 
	 * @param fromPoint Algse asukoha koordinaadid.
	 * @param toPoint Sihtpunkti koordinaadid.
	 * @return Tagastab liikumisuuna, kuhu <code>fromPoint</code> vaja l�kata.
	 * @throws IllegalMoveException Visatakse, kui antud k�iku ei ole 15-m�ngu
	 * reeglite kohaselt v�imalik teha.
	 */
	public static Direction pointsToDirection(Point fromPoint, Point toPoint) throws IllegalMoveException {
		if ((fromPoint.x == toPoint.x) && (fromPoint.y - 1 == toPoint.y))
			return Direction.NORTH;
		else if ((fromPoint.x == toPoint.x) && (fromPoint.y + 1 == toPoint.y))
			return Direction.SOUTH;
		else if ((fromPoint.x + 1 == toPoint.x) && (fromPoint.y == toPoint.y))
			return Direction.EAST;
		else if ((fromPoint.x - 1 == toPoint.x) && (fromPoint.y == toPoint.y))
			return Direction.WEST;
		else throw new IllegalMoveException();
	}
	
	/**
	 * Vahetab m�rgitud koordinaatidel olevad arvud.
	 * @param a Esimene koordinaat.
	 * @param b Teine koordinaat.
	 */
	private void swapElements(Point a, Point b) {
		int temp = values[a.y][a.x];
		values[a.y][a.x] = values[b.y][b.x];
		values[b.y][b.x] = temp;
	}
	
	/**
	 * Arvutab iga v�lja kauguse nende asukohast lahendatud m�nguv�ljal ning
	 * liidab kaugused kokku. Kaugus arvutatakse
	 * <a href='http://en.wikipedia.org/wiki/Taxicab_geometry'>Manhattan Distance</a> 
	 * p�him�ttel. Kasutatakse heuristilises funktsioonis h*(n)-ina. 
	 * Nulli ei arvestata, kuna soovime, et algoritm oleks lubav (ennem
	 * l�bikaalumist v�hemalt ;) )
	 * @return
	 */
	private int calculateManhattanDistance() {
		/* Arvutamised eeldatakse, et l�ppv�ljal peaks
		 * arvud paiknema j�rjest �levalt vasakult nurgast
		 * suunaga paremasse alumisse nurka ning null k�ige
		 * l�pus. */
		int distance = 0;
		for (int y = 0; y < dimension; y++) {
			for (int x = 0; x < dimension; x++) {
				int number = values[y][x];
				if (number == 0) continue;
				int dest_x = (number - 1) % dimension;
				int dest_y = (number - 1) / dimension;
				distance += Math.abs(x - dest_x) + Math.abs(y - dest_y);
			}
		}
		return distance;
	}
	
	/**
	 * Arvutab m�nguv�lja inversioonid, et kontrollida lahendatavust ({@link #isSolvable}).<br>
	 * P�hineb j�rgneval algoritmil:<br>
	 * <a href='http://www.cs.bham.ac.uk/~mdr/teaching/modules04/java2/TilesSolvability.html'>
	 * Mark Ryan - Solvability of the Tiles Game</a>
	 * @return Tagastab inversioonide summa.
	 */
	private int inversions() {
		int inversionsCount = 0; 
		for (int y1 = 0; y1 < dimension; y1++) {
			for (int x1 = 0; x1 < dimension; x1++) {
				if ((y1 == dimension - 1) && (x1 == dimension -1))
					return inversionsCount;
				if (values[y1][x1] == 0) continue;
				for (int y2 = (x1 == dimension - 1) ? y1 + 1: y1; y2 < dimension; y2++) {
					for (int x2 = (y2 == y1) && (x1 != dimension - 1) ? x1 + 1 : 0; x2 < dimension; x2++) {
						if ((values[y2][x2] != 0) && (values[y2][x2] < values[y1][x1])) inversionsCount++;
					}
				}
			}
		}
		return inversionsCount;
	}
	
	/**
	 * Kontrollib matemaatiliselt, kas antud v�li on lahendatav.<br>
	 * Kasutatud j�rgnevat algoritmi:<br>
	 * <a href='http://www.cs.bham.ac.uk/~mdr/teaching/modules04/java2/TilesSolvability.html'>
	 * Mark Ryan - Solvability of the Tiles Game</a>
	 * @return Tagastab t�ev��rtuse, kas v�lja on v�imalik lahedada.
	 */
	public boolean isSolvable() {
		int inversionsCount = inversions();
        int emptySlotLineFromBotton = dimension - emptyPoint.y;
        if (((dimension % 2 != 0) && (inversionsCount % 2 == 0)) 
        		|| ((dimension % 2 == 0) && (emptySlotLineFromBotton % 2 != inversionsCount % 2))) return true;
        else return false;
	}
	
	/**
	 * Koostab s�ne, mis n�itab lahenduseni j�udmiseks vajalikke k�ike.<br>
	 * Liigutatakse t�hja ruutu t�hega m�rgitud suunas!<br>
	 * <ul>
	 * <li>� - �lesse</li>
	 * <li>P - paremale</li>
	 * <li>A - alla</li>
	 * <li>V - vasakule</li>
	 * </ul>
	 * @return Tagastab liigutusi kirjeldava s�ne. Lugeda vasakult paremale.
	 * @throws IllegalMoveException Visatakse, kui kuskil esineb vigane k�ik.
	 * Normaalse t�� puhul ei tohiks juhtuda!
	 */
	public String backtrack() throws IllegalMoveException {
		StringBuffer moves = new StringBuffer(100);
		Field curField = this;
		Field curParent = this.getParent();
		
		while (curParent != null) {
			Point curEmpty = curField.getEmptyPoint();
			Point prevEmpty = curParent.getEmptyPoint();
			char curMove;
			switch (Field.pointsToDirection(prevEmpty, curEmpty)) {
			case NORTH:
				curMove = '�';
				break;
			case SOUTH:
				curMove = 'A';
				break;
			case WEST:
				curMove = 'V';
				break;
			case EAST:
				curMove = 'P';
				break;
			default:
				throw new IllegalMoveException();
			}
			moves.insert(0, curMove);
			curField = curParent;
			curParent = curField.getParent();
		}

		return moves.toString();
	}

	/**
	 * P�him�tteliselt Eclipse poolt genereeritud r�sifunktsioon:
	 * v�imalik, et parema m�eldes saaks etema j�udluse.<br>
	 * Vajalik paisktabelisse paigutamisel/leidmisel.
	 * @return Antud objekti r�siv��rtuse.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode((values));
		return result;
	}

	/**
	 * Kontrollib kahe v�lja samav��rsust. Vaadatakse k�igi numbrite paigutust - 
	 * eelased jms. t�htsust ei oma.
	 * @param obj Teine objekt, millega v�rrelda.
	 * @return T�ev��rtus.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Field cObj = (Field) obj;
		if (!Arrays.deepEquals(this.getValues(), cObj.getValues()))
			return false;
		return true;
	}

	/**
	 * V�rreldakse kahte v�lja: ehk t�psemalt nende heuristilisi v��rtusi.
	 * Vajalik j�rjekorda paigutamisel.
	 * @param o Teine objekt, millega v�rrelda.
	 * @return Kui antud objekti (<code>this</code>) heuristiline v��rtus on parem,
	 * siis tagastatakse negatiivne arv.
	 */
	@Override
	public int compareTo(Field o) {
		return this.getHeuristicValue() - o.getHeuristicValue();
	}
	
	/**
	 * Kirjeldab suunda.
	 * Kasutatakse t�hja ruudu liigutamisel.
	 * 
	 * @author Kristjan Kaitsa
	 * @version 1.0
	 * 
	 */
	public static enum Direction {
		/** P�hi ehk �lesse. */
		NORTH,
		/** Ida ehk paremale. */
		EAST,
		/** L�una ehk alla. */
		SOUTH,
		/** L��s ehk vasakule. */
		WEST
	}
	
	/**
	 * Erind, mis visatakse, kui proovitud k�ik ei ole legaalne
	 * ehk ei vasta 15-m�ngu reeglitele.
	 * 
	 * @author Kristjan Kaitsa
	 * @version 1.0
	 *
	 */
	public static class IllegalMoveException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	/**
	 * V�ljastab m�nguv�lja tabulaatorite ja reavahetustega
	 * vormistatuna konsooli.
	 * 
	 * @return M�nguv�lja kujutav s�ne.
	 */
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		for (int y = 0; y < dimension; y++) {
			for (int x = 0; x < dimension; x++) {
				output.append(values[y][x]);
				output.append('\t');
			}
			if (y != dimension - 1)	output.append("\n");
		}
		return output.toString();
	}
}
