import java.util.Set;

import java.util.TreeSet;

public class Ort {

	int name;

	int vorgaenger;

	int weglaenge;

	Set<Weg> wege;

	boolean besucht;

	@Override

	public String toString() {

		return "Ort " + name + " (Vorg�nger: " + (0 == vorgaenger ? "[]" : vorgaenger)
				+ ") [wegl�nge total von start aus: " + weglaenge + "] <besucht: " + besucht + ">";

	}

	public Ort(int ortA) {

		this.name = ortA;

		this.vorgaenger = 0;

		this.weglaenge = Integer.MAX_VALUE;

		this.wege = new TreeSet<Weg>();

		besucht = false;

	}

	public void addWeg(int ortB, int abst) {

		if (null == wege) {

			wege = new TreeSet<Weg>();

		}

		Weg w = new Weg();

		w.ortA = this.name;

		w.ortB = ortB;

		w.weglaenge = abst;

		wege.add(w);

	}

	public void setzeVorgaenger(int vorgaenger) {

		this.vorgaenger = vorgaenger;

	}

	public void setzeWeglaenge(int weglaenge) {

		this.weglaenge = weglaenge;

	}

}