import java.io.BufferedReader;

import java.io.FileReader;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

public class Landkarte {

	public static String CSV_FILE_NAME = "verbindungen.csv";

	Map<Integer, Ort> ortMenge = new HashMap<Integer, Ort>();

	public static Landkarte initGraph() throws Exception {

		Landkarte l = new Landkarte();

		FileReader fr = new FileReader(CSV_FILE_NAME);

		String line = null;

		BufferedReader br = new BufferedReader(fr);

		while (null != (line = br.readLine())) {

			String[] args = line.split(",");

			int ortA = Integer.valueOf(args[0]);

			int ortB = Integer.valueOf(args[1]);

			int abst = Integer.parseInt(args[2].trim());

			ensureOrt(l, ortA);

			ensureOrt(l, ortB);

			l.ortMenge.get(ortA).addWeg(ortB, abst);

			l.ortMenge.get(ortB).addWeg(ortA, abst);

		}

		br.close();

		return l;

	}

	private static void ensureOrt(Landkarte l, int ortA) {

		if (l.ortMenge.containsKey(ortA)) {

			return;

		}

		Ort o = new Ort(ortA);

		o.vorgaenger = 0;

		o.wege = null;

		o.weglaenge = Integer.MAX_VALUE;

		l.ortMenge.put(ortA, o);

	}

	public Ort getOrt(int vorgaenger) {

		return this.ortMenge.get(vorgaenger);

	}

	public boolean hatUnbesuchteOrte() {

		for (Ort o : ortMenge.values()) {

			if (!o.besucht) {

				return true;

			}

		}

		return false;

	}

	public Ort unbesuchterOrtMitMinimalabstand() {

		Ort resultat = null;

		int minAbst = Integer.MAX_VALUE;

		for (Ort o : ortMenge.values()) {

			if (!o.besucht) {

				if (null == resultat) {

					resultat = o;

					minAbst = o.weglaenge;

				} else {

					if (minAbst > o.weglaenge) {

						resultat = o;

						minAbst = o.weglaenge;

					}

				}

			}

		}

		return resultat;

	}

	public List<Ort> angrenzendeAn(Ort minOrt) {

		ArrayList<Ort> angrenzende = new ArrayList<Ort>();

		for (Ort o : ortMenge.values()) {

			if (minOrt != o) {

				for (Weg w : o.wege) {

					if (w.ortA == minOrt.name || w.ortB == minOrt.name) {

						angrenzende.add(o);

					}

				}

			}

		}

		return angrenzende;

	}

	public int liesWegLaenge(int zielOrt) {

		return ortMenge.get(zielOrt).weglaenge;

	}

}