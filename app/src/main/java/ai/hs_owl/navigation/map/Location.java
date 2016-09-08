package ai.hs_owl.navigation.map;

import android.graphics.PointF;
import android.util.Log;

/**
 * Diese statische Klasse speichert die Position auf der Karte und die Ebene der Karte.
 * Die Werte werden von AltBeacon gesetzt und von Map gelesen.
 */
public class Location {
    static PointF position;
    static int layer = -1;

    public static void setLayer(int layer) {
        Location.layer = layer+1;
    }

    public static int getLayer() {
        return layer;
    }
    /**
     * @return PointF die Position auf der Karte
     * Gibt die Position auf der Karte zurück, korrigiert die Werte
     * */
    public static PointF getPositionOnMap() {
        if (position == null) {
            position = new PointF(0,0);
        }
        return new PointF(position.x, position.y);
    }

    public static void setPosition(PointF position) {
        Location.position = position;
    }

}
