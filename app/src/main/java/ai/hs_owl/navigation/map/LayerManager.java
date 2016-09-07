package ai.hs_owl.navigation.map;

import android.util.Log;

import com.davemorrissey.labs.subscaleview.ImageSource;

import java.io.File;

import ai.hs_owl.navigation.connection.Synchronize;

/**
 * Verwaltet die verschiedenen Kartenebenen (--> Bilddateien)
 */
public class LayerManager {
    /**
     * @return int die neue Ebene
     * Erhöht die Ebene um eins, wenn dies möglich ist
     * */
    public static int up(int manuallayer)
    {
        if(manuallayer<6)
            manuallayer++;
        else return -1;
        return manuallayer;
        }
    /**
     * @return int die neue Ebene
     * Vermindert die Ebene um eins, wenn dies möglich ist
     * */
    public static int down(int manuallayer)
    {
        if(manuallayer>1)
            manuallayer--;
        else return -1;
        return manuallayer;
    }
    /**
     * @return ImageSource die Bildquelle für die Ebene
     * @param layer Die Ebene, für die die Quelle benötigt wird
     * Überprüft, ob für die Ebene eine Bilddatei vorhanden ist und gibt diese zurück
     * */
    public static ImageSource getImageSource(int layer) {
        File f = new File(Synchronize.rootpath + "/" + layer + ".png");
        if (!f.exists()) {
            return null;
        }
        return ImageSource.uri(f.getAbsolutePath().toString());
    }
}
