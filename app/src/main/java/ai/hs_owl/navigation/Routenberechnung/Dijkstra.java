package ai.hs_owl.navigation.Routenberechnung;


import android.content.Context;

import java.lang.reflect.Array;
import java.util.ArrayList;
/* Diese Klasse berechnet die kürzeste Route.
* Zurückgegeben wird eine ArrayList mit den besuchten Orten.
* */

public class Dijkstra {
	static ArrayList<Integer>  besuchteOrte;

//Start und Ziel werden übergeben.
	public static ArrayList calculate(int start, int ziel, Context c) throws Exception {

		Landkarte landkarte = Landkarte.initGraph(c);

		int startOrt = start;
		
		landkarte.getOrt(startOrt).setzeVorgaenger('\u0000');

		landkarte.getOrt(startOrt).setzeWeglaenge(0);

		while (landkarte.hatUnbesuchteOrte()) {

			Ort minOrt = landkarte.unbesuchterOrtMitMinimalabstand();

			minOrt.besucht = true;

			for (Ort angrenzend : landkarte.angrenzendeAn(minOrt)) {

				behandleNachbar(landkarte, minOrt, angrenzend);

			}


		}


		int zielOrt = ziel;
		
		int weglaenge = landkarte.liesWegLaenge(zielOrt);

		ausgabeAlleWege(landkarte, startOrt, zielOrt, weglaenge);
		return besuchteOrte;

	}

	// In dieser Methode werden alle besuchten Orte in einer ArrayList gespeichert.
	private static void ausgabeAlleWege(Landkarte landkarte, int startOrt, int zielOrt, int weglaenge) {


		Ort o = landkarte.getOrt(zielOrt);


		besuchteOrte= new ArrayList<>();
		
		while (o.name != startOrt) {

			System.out.println(o.name);
			besuchteOrte.add(o.name);
			o = landkarte.getOrt(o.vorgaenger);

		}

		besuchteOrte.add(startOrt);

	}

	private static void behandleNachbar(Landkarte landkarte, Ort minOrt, Ort angrenzend) {

		int ortAbstand = ortAbstand(minOrt, angrenzend);

		if (Math.abs(minOrt.weglaenge - angrenzend.weglaenge) > ortAbstand) {

			updateWeiterenVonBeiden(minOrt, angrenzend, ortAbstand);

		}

	}

	private static void updateWeiterenVonBeiden(Ort minOrt, Ort angrenzend,

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

	private static int ortAbstand(Ort minOrt, Ort angrenzend) {

		for (Weg w : minOrt.wege) {

			if (w.ortA == angrenzend.name || w.ortB == angrenzend.name) {

				return w.weglaenge;

			}

		}

		return Integer.MAX_VALUE;

	}

	public static ArrayList getBesuchteOrte(){
		return besuchteOrte;
	}

}