package ai.hs_owl.navigation.database;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by mberg on 12.05.2016.
 */
public class CSVReader {
    public static String[][] read(String path) {
        try {
            Scanner scan = new Scanner(new FileReader(path));
            ArrayList<String> lines = new ArrayList<String>();

            while (scan.hasNext())
                lines.add(scan.next());

            String[][] data = new String[lines.size()][4];
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
