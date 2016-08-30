import static eu.gressly.io.utility.Input.inputChar;

import java.util.ArrayList;

import eu.gressly.io.utility.Input;

public class Dijkstra {
	ArrayList besuchteOrte;

	public Dijkstra (int start, int ziel) {

		try {

			top(start,ziel);

		} catch (Exception e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		}

	}

	void top(int start, int ziel) throws Exception {

		Landkarte landkarte = Landkarte.initGraph();

		//int startOrt = Input.inputInt("Startort (A-Z):");
		int startOrt = start;
		
		landkarte.getOrt(startOrt).setzeVorgaenger('\u0000');

		landkarte.getOrt(startOrt).setzeWeglaenge(0);

		while (landkarte.hatUnbesuchteOrte()) {

			Ort minOrt = landkarte.unbesuchterOrtMitMinimalabstand();

			minOrt.besucht = true;

			for (Ort angrenzend : landkarte.angrenzendeAn(minOrt)) {

				behandleNachbar(landkarte, minOrt, angrenzend);

			}

			// System.out.println("Debug in top(): minOrt besucht: " + minOrt);

		}

		// ende: Ausgabe

//		int zielOrt = Input.inputInt("Zielort (A-Z): ");
		int zielOrt = ziel;
		
		int weglaenge = landkarte.liesWegLaenge(zielOrt);

		ausgabeAlleWege(landkarte, startOrt, zielOrt, weglaenge);

	}

	private void ausgabeAlleWege(Landkarte landkarte, int startOrt, int zielOrt, int weglaenge) {

		System.out.println("Die Weglänge von " + startOrt + " zu " + zielOrt + " beträgt " + weglaenge + ".");

		Ort o = landkarte.getOrt(zielOrt);

		//System.out.println("Unterwegs wurden besucht: ");

		besuchteOrte= new ArrayList<>();
		
		while (o.name != startOrt) {

			System.out.println(o.name);
			besuchteOrte.add(o.name);
			o = landkarte.getOrt(o.vorgaenger);

		}

		System.out.println(startOrt);
		besuchteOrte.add(startOrt);
		System.out.println(besuchteOrte);

	}

	private void behandleNachbar(Landkarte landkarte, Ort minOrt, Ort angrenzend) {

		int ortAbstand = ortAbstand(minOrt, angrenzend);

		if (Math.abs(minOrt.weglaenge - angrenzend.weglaenge) > ortAbstand) {

			updateWeiterenVonBeiden(minOrt, angrenzend, ortAbstand);

		}

	}

	private void updateWeiterenVonBeiden(Ort minOrt, Ort angrenzend,

			int ortAbstand) {

		Ort naeher, weiter;

		if (minOrt.weglaenge <= angrenzend.weglaenge) {

			naeher = minOrt;

			weiter = angrenzend;

		} else {

			naeher = angrenzend;

			weiter = minOrt;

		}

		weiter.weglaenge = naeher.weglaenge + ortAbstand;

		weiter.vorgaenger = naeher.name;

	}

	private int ortAbstand(Ort minOrt, Ort angrenzend) {

		for (Weg w : minOrt.wege) {

			if (w.ortA == angrenzend.name || w.ortB == angrenzend.name) {

				return w.weglaenge;

			}

		}

		return Integer.MAX_VALUE;

	}

}