package solver;

import java.awt.Point;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * <code>Field</code> klass kujutab endast mänguvälja seisu<br>
 * ning võimaldab sellega manipuleerida ja seda kontrollida.
 * 
 * @author Kristjan Kaitsa
 * @version 1.0
 * 
 */
public class Field implements Comparable<Field> {
	/** Tühja ruudu asukoht. */
	private Point emptyPoint;
	/** Mänguvälja dimensioon (15-mängu ehk 4x4 puhul 4). */
	private int dimension;
	/** Mänguvälja ruutude väärtused kahedimensionaalses massiivis. */
	private int[][] values;
	/** Antud väljale eelnev väli ehk väli, millest ühe liigutusega on
	 * võimalik selle väljani jõuda. */
	private Field parent;
	/** Käikude arv, mis vaja sooritada, et algseisust antud väljani jõuda. */
	private int moves;
	/** Massiiv kujutamaks oodatava lahendatud välja numbrite paigutust. */
	private int[][] solvedValues;
	/** Antud välja kõigi ruutude Manhattani kauguste summa nende nõutud kohast. */ 
	private int manhattanDistance;
	
	/**
	 * Koostab uue välja vastavalt ette antud seisule.<br>
	 * Kasutatakse algseisu põhjal välja loomiseks -
	 * ei oma eelasi.
	 * @param values Kahedimensionaalne massiiv välja numbritega.
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
	 * Koostab uue välja vastavalt ette antud väljale ning
	 * tühja ruudu liigutamise suunale.
	 * @param parent Antud välja eelane.
	 * @param emptyMove Tühja ruudu liigutamise suund.
	 * @throws IllegalMoveException Visatakse, kui antud suunas ei ole
	 * võimalik tühja ruutu liigutada.
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
	 * @return Tagastab mänguvälja ruutude väärtused kahe-dimensionaalse massiivina.
	 */
	public int[][] getValues() {
		return this.values;
	}
	
	/**
	 * @return Tagastab mänguvälja dimensiooni (nt. 4x4 ehk 15-mängu puhul 4).
	 */
	public int getDimension() {
		return this.dimension;
	}
	
	/**
	 * @return Tagastab tühja ruudu asukoha mänguväljal.
	 */
	public Point getEmptyPoint() {
		return this.emptyPoint;
	}
	
	/**
	 * @return Tagastab antud väljani jõudmiseks vajaminevate käikude arvu algolekust.
	 */
	public int getMoves() {
		return this.moves;
	}
	
	/**
	 * @return Tagastab oodatava lahendatud välja väärtused.
	 * Antakse edasi väljalt-väljale, et ei peaks alati uuesti arvutama.
	 */
	public int[][] getSolvedValues() {
		return this.solvedValues;
	}
	
	/**
	 * @return Tagastab antud välja vahetu eelase ehk välja, millest ühe liigutusega on
	 * võimalik selle väljani jõuda.
	 */
	public Field getParent() {
		return parent;
	}
	
	/**
	 * Vahetab antud välja esivanema välja.<br>
	 * Vajalik A* algoritmis toimuva tippudevaheliste viitade ümberorienteerimiseks.
	 * @param parent Antud väljale uus eelane.
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
	 * <li><b>g(n)</b> - siia olekusse jõudmiseks läbitud tippude arv.</li>
	 * <li><b>h*(n)</b> - minimaalne käikude arv, mis siit lõpptippu jõudmiseks
	 * teha oleks vaja.</li>
	 * </ul>
	 * Kiiremini lahenduse leidmiseks ohverdatakse algoritmi optimaalsus ning
	 * <b>h*(n)</b> korrutatakse kolmega.
	 * @return Tagastab antud välja hinnangu.
	 */
	public int getHeuristicValue() {
		return this.moves + 3 * this.getManhattanDistance();
	}
	
	/**
	 * Kontrollib, kas antud välja näol on tegemist lahendatud väljaga.
	 * @return Tõeväärtuse.
	 */
	public boolean isSolved() {
		if (Arrays.deepEquals(this.values, this.solvedValues))
			return true;
		else
			return false;
	}
	
	/**
	 * Genereerib antud väljale kõik legaalsed järglased va.
	 * tagasikäigu (ehk antud välja eelase).
	 * @return Lingitud listi antud välja järglastega.
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
	 * Genereerib vastavalt mängu dimensioonile oodatava lahenduse
	 * massiivi. Eeldatakse, et lahenduses peavad numbrid
	 * olema järjest ülevalt vasakult kuni alla paremale ning tühi
	 * ruut (ehk 0) on kõige lõpus.
	 * @return Tagastab lahendatud välja massiivi.
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
	 * Arvutab koordinaadid, kuhu jõutakse <code>basePoint</code>'i liigutades
	 * määratud suunas (<code>direction</code>).
	 * @param basePoint Ruut, mida soovitakse liigutada.
	 * @param direction Suund, kuhu <code>basePoint</code> liigutada.
	 * @return Tagastab ruudu koordinaadid, kuhu liigutuse sooritamisel jõutakse.
	 * @throws IllegalMoveException Visatakse, kui käik ei ole 15-mängu
	 * reeglite järgi legaalne.
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
	 * @return Tagastab liikumisuuna, kuhu <code>fromPoint</code> vaja lükata.
	 * @throws IllegalMoveException Visatakse, kui antud käiku ei ole 15-mängu
	 * reeglite kohaselt võimalik teha.
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
	 * Vahetab märgitud koordinaatidel olevad arvud.
	 * @param a Esimene koordinaat.
	 * @param b Teine koordinaat.
	 */
	private void swapElements(Point a, Point b) {
		int temp = values[a.y][a.x];
		values[a.y][a.x] = values[b.y][b.x];
		values[b.y][b.x] = temp;
	}
	
	/**
	 * Arvutab iga välja kauguse nende asukohast lahendatud mänguväljal ning
	 * liidab kaugused kokku. Kaugus arvutatakse
	 * <a href='http://en.wikipedia.org/wiki/Taxicab_geometry'>Manhattan Distance</a> 
	 * põhimõttel. Kasutatakse heuristilises funktsioonis h*(n)-ina. 
	 * Nulli ei arvestata, kuna soovime, et algoritm oleks lubav (ennem
	 * läbikaalumist vähemalt ;) )
	 * @return
	 */
	private int calculateManhattanDistance() {
		/* Arvutamised eeldatakse, et lõppväljal peaks
		 * arvud paiknema järjest ülevalt vasakult nurgast
		 * suunaga paremasse alumisse nurka ning null kõige
		 * lõpus. */
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
	 * Arvutab mänguvälja inversioonid, et kontrollida lahendatavust ({@link #isSolvable}).<br>
	 * Põhineb järgneval algoritmil:<br>
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
	 * Kontrollib matemaatiliselt, kas antud väli on lahendatav.<br>
	 * Kasutatud järgnevat algoritmi:<br>
	 * <a href='http://www.cs.bham.ac.uk/~mdr/teaching/modules04/java2/TilesSolvability.html'>
	 * Mark Ryan - Solvability of the Tiles Game</a>
	 * @return Tagastab tõeväärtuse, kas välja on võimalik lahedada.
	 */
	public boolean isSolvable() {
		int inversionsCount = inversions();
        int emptySlotLineFromBotton = dimension - emptyPoint.y;
        if (((dimension % 2 != 0) && (inversionsCount % 2 == 0)) 
        		|| ((dimension % 2 == 0) && (emptySlotLineFromBotton % 2 != inversionsCount % 2))) return true;
        else return false;
	}
	
	/**
	 * Koostab sõne, mis näitab lahenduseni jõudmiseks vajalikke käike.<br>
	 * Liigutatakse tühja ruutu tähega märgitud suunas!<br>
	 * <ul>
	 * <li>Ü - ülesse</li>
	 * <li>P - paremale</li>
	 * <li>A - alla</li>
	 * <li>V - vasakule</li>
	 * </ul>
	 * @return Tagastab liigutusi kirjeldava sõne. Lugeda vasakult paremale.
	 * @throws IllegalMoveException Visatakse, kui kuskil esineb vigane käik.
	 * Normaalse töö puhul ei tohiks juhtuda!
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
				curMove = 'Ü';
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
	 * Põhimõtteliselt Eclipse poolt genereeritud räsifunktsioon:
	 * võimalik, et parema mõeldes saaks etema jõudluse.<br>
	 * Vajalik paisktabelisse paigutamisel/leidmisel.
	 * @return Antud objekti räsiväärtuse.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode((values));
		return result;
	}

	/**
	 * Kontrollib kahe välja samaväärsust. Vaadatakse kõigi numbrite paigutust - 
	 * eelased jms. tähtsust ei oma.
	 * @param obj Teine objekt, millega võrrelda.
	 * @return Tõeväärtus.
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
	 * Võrreldakse kahte välja: ehk täpsemalt nende heuristilisi väärtusi.
	 * Vajalik järjekorda paigutamisel.
	 * @param o Teine objekt, millega võrrelda.
	 * @return Kui antud objekti (<code>this</code>) heuristiline väärtus on parem,
	 * siis tagastatakse negatiivne arv.
	 */
	@Override
	public int compareTo(Field o) {
		return this.getHeuristicValue() - o.getHeuristicValue();
	}
	
	/**
	 * Kirjeldab suunda.
	 * Kasutatakse tühja ruudu liigutamisel.
	 * 
	 * @author Kristjan Kaitsa
	 * @version 1.0
	 * 
	 */
	public static enum Direction {
		/** Põhi ehk ülesse. */
		NORTH,
		/** Ida ehk paremale. */
		EAST,
		/** Lõuna ehk alla. */
		SOUTH,
		/** Lääs ehk vasakule. */
		WEST
	}
	
	/**
	 * Erind, mis visatakse, kui proovitud käik ei ole legaalne
	 * ehk ei vasta 15-mängu reeglitele.
	 * 
	 * @author Kristjan Kaitsa
	 * @version 1.0
	 *
	 */
	public static class IllegalMoveException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	/**
	 * Väljastab mänguvälja tabulaatorite ja reavahetustega
	 * vormistatuna konsooli.
	 * 
	 * @return Mänguvälja kujutav sõne.
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
