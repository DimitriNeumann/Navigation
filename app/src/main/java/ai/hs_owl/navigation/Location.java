package ai.hs_owl.navigation;

import android.graphics.PointF;

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
        if (position == null)
            position = new PointF(0, 0);
        return position;
    }

    public static void setPosition(PointF position) {
        Location.position = position;
    }

}