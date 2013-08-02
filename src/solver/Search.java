package solver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import solver.Field.IllegalMoveException;

/**
 * Lõime klass sooritamaks A* otsingut (antud heuristiku puhul <b>kaalutud</b>
 * - st. ei ole enam lubav) olekute ruumis leidmaks käike lahenduseni.
 * 
 * @author Kristjan Kaitsa
 * @version 1.0
 * 
 */
public class Search implements Runnable {
	/** Prioritiseeritud järjekord. */ 
	private Queue<Field> queue = new PriorityQueue<Field>(50000);
	/** Avatud nimekiri. */
	private Map<Field, Field> open = new HashMap<Field, Field>(50000);
	/** Suletud nimekiri. */
	private Set<Field> closed = new HashSet<Field>(50000);
	/** Käigud, mis lahenduseni jõudmiseks teha vaja.<br> 
	 *  Täidetakse pärast lõppolekusse jõudmist. */
	private String movesToSolution;
	/** Olekute arv, mis on läbivaadatud.<br>
	 * Vajalik puhtalt statistikaks. */
	private long states = 0;
	/** Aeg millisekundites, mis kulus lahenduse leidmiseks.<br>
	 * Vajalik puhtalt statistikaks. */
	private long time = 0;
	
	/**
	 * Konstruktor uue otsingu sooritamiseks.
	 * @param initField Algseis.
	 * @throws UnsolvableException Visatakse, kui antud väli ei ole lahendatav.
	 * Kontrollitakse matemaatiliselt (vt. {@link Field#isSolvable()}).
	 */
	public Search(Field initField) throws UnsolvableException {
		if (!initField.isSolvable())
			throw new UnsolvableException();
		open.put(initField, initField);
		queue.add(initField);
	}
	
	/**
	 * Alustab mänguvälja lahendamist.<br>
	 * Töötab kuni lahendus leitakse.
	 */
	@Override
	public void run() {
		long start = System.currentTimeMillis();
		while (!queue.isEmpty()) {
			this.states++;
			Field curField = queue.poll();
			
			if (curField.isSolved()) {
				try {
					movesToSolution = curField.backtrack();
					time = System.currentTimeMillis() - start;
				} catch (IllegalMoveException e) {
					System.err.println("Oh, backtrackimine ebaõnnestus!");
					System.exit(-1);
				}
				return;
			}
			
			open.remove(curField);
			closed.add(curField);
			for (Field child : curField.getChildren()) {
				if (closed.contains(child)) continue;
				if (open.containsKey(child)) {
					Field oldField = open.get(child);
					if (child.compareTo(oldField) < 0) {
						queue.remove(oldField);
						oldField.setParent(child.getParent());
						queue.add(oldField);
					}
					continue;
				}
				open.put(child, child);
				queue.add(child);
			}
		}
	}
	
	/**
	 * Vt. {@link Field#backtrack()}.<br>
	 * @return Tagastab sõnena lahenduseni jõudmiseks vajalikud käigud.
	 */
	public String getMovesToSolution() {
		return movesToSolution;
	}
	
	/**
	 * @return Tagastab lahenduse leidmiseks kulunud aja millisekundites.
	 */
	public long getTime() {
		return time;
	}
	
	/**
	 * @return Tagastab lahenduse leidmiseks läbivaadatud seisundite (väljade) arvu.
	 */
	public long getStates() {
		return states;
	}
	
	public static class UnsolvableException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
}
