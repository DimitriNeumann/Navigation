package ai.hs_owl.navigation.map;

import android.util.Log;

import com.davemorrissey.labs.subscaleview.ImageSource;

import java.io.File;

import ai.hs_owl.navigation.connection.Synchronize;

/**
 * Verwaltet die verschiedenen Kartenebenen (--> Bilddateien)
 */
public class LayerManager {
    public static int up(int manuallayer)
    {
        if(manuallayer<5)
            manuallayer++;
        else return -1;
        return manuallayer;
        }
    public static int down(int manuallayer)
    {
        if(manuallayer>1)
            manuallayer--;
        else return -1;
        return manuallayer;
    }

    public static ImageSource getImageSource(int layer) {
        File f = new File(Synchronize.rootpath + "/" + layer + ".png");
        if (!f.exists()) {
            return null;
        }
        return ImageSource.uri(f.getAbsolutePath().toString());
    }
}
