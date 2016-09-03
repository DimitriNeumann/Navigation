package ai.hs_owl.navigation.map;

import android.graphics.PointF;
import android.util.Log;

/**
 * Created by mberg on 10.05.2016.
 */
public class Location {
    static PointF position;
    static int layer = -1;

    public static void setLayer(int layer) {
        Location.layer = layer;
    }

    public static int getLayer() {
        return layer;
    }

    public static PointF getPositionOnMap() {
        if (position == null) {
            Log.i("Position", "is null");
            position = new PointF(512, 512);
        }
        return new PointF(position.x/2F, position.y/2F);
    }

    public static void setPosition(PointF position) {
        Location.position = position;
    }

}
