package ai.hs_owl.navigation.connection;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;

import ai.hs_owl.navigation.database.CSVReader;
import ai.hs_owl.navigation.database.Database;
import ai.hs_owl.navigation.database.Queries;

/**
 * Created by mberg on 12.05.2016.
 */
public class Synchronize {
    public static String rootpath = Environment.getExternalStorageDirectory().getPath().toString() + "/hs_owl_navigation";
    private static String csvpath = rootpath + "/beacons.csv";
    private static String zippath = rootpath + "/ebenen.zip";

    public static boolean updated = false;


    public interface DownloadHandler {
        void data_received();
    }

    public static void sync(final Context c) {
        deleteFolders(new File(rootpath));
        createFolders();

        Download.startDownload(c, new DownloadHandler() {
            @Override
            public void data_received() {
                Queries.getInstance(c).clearTable(Database.BEACONS_TABLE_NAME);
                String[][] table = CSVReader.read(csvpath);
                for (String[] line : table) {
                    Queries.getInstance(c).insertNewBeacon(line[0], Float.parseFloat(line[1]), Float.parseFloat(line[2]), Integer.parseInt(line[3]));
                }

                try {
                    ZipFile zip = new ZipFile(zippath);
                    zip.extractAll(rootpath);
                    updated = true;
                } catch (ZipException e) {
                    e.printStackTrace();
                }
            }
        }, new String[]{"http://192.168.1.150/navi/ebenen.zip", "http://192.168.1.150/navi/beacons.csv"});

    }
    public static boolean syncNeeded(Context c)
    {
        if(Queries.getInstance(c).hasBeacons())
        {
            File f = new File(rootpath);
            if(f.exists() && f.listFiles().length>2)
                return false;
        }
        return true;
    }
    private static void deleteFolders(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolders(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    private static void createFolders() {
        File f = new File(rootpath);
        if (!f.exists())
            f.mkdirs();
    }

}
