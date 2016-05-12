package ai.hs_owl.navigation.database;

import android.util.Log;

import java.io.File;

import ai.hs_owl.navigation.connection.Synchronize;

/**
 * Created by mberg on 12.05.2016.
 */
public class LayerManager {
    public static String getPathToLayer(int layer)
    {
        File f = new File(Synchronize.rootpath + "/" +layer+ ".png");
        if(!f.exists()) {
            Log.i("File", "no such file" + f.getAbsolutePath());
            return null;
        }
        return f.getAbsolutePath();
    }
}
