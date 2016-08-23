package ai.hs_owl.navigation.database;

import android.util.Log;

import com.davemorrissey.labs.subscaleview.ImageSource;

import java.io.File;

import ai.hs_owl.navigation.connection.Synchronize;

/**
 * Created by mberg on 12.05.2016.
 */
public class LayerManager {
    public static ImageSource getImageSource(int layer) {
        Log.i("Layer:", layer + "");
        File f = new File(Synchronize.rootpath + "/" + layer + ".png");
        if (!f.exists()) {
            Log.i("File", "no such file" + f.getAbsolutePath());
            return null;
        }
        return ImageSource.uri(f.getAbsolutePath().toString());
    }
}
