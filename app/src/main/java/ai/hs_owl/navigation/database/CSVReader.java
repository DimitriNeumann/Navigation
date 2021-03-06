package ai.hs_owl.navigation.database;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Eine einfache Klasse, welche CSV Dateien einliest und anhand des semikolons trennt.
 * Unterstützt keine Escape Zeichen oder ähnliches.
 */
public class CSVReader {
    /**
     * @return String[][] die Ergebniss Array
     * @param path Der Pfad zur CSV Datei
     * @param max Die Maximale Anzahl an Spalten pro Zeile, eine geringere Anzahl wird mit null gefüllt
     * Gibt die Tabelle in Form einer zweidimmensionalen Array wieder
     * */
    public static String[][] read(String path, int max) {
        try {
            Scanner scan = new Scanner(new FileReader(path));
            ArrayList<String> lines = new ArrayList<String>();

            while (scan.hasNextLine())
                lines.add(scan.nextLine());

            String[][] data = new String[lines.size()][max];
            for (int i = 0; i < data.length; i++) {
                data[i] = lines.get(i).split(";");
            }
            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
