package ai.hs_owl.navigation;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by mberg on 10.05.2016.
 */
public class Location {
    static PointF position;
    public static PointF getPositionOnMap()
    {
        if(position==null)
            position= new PointF(300,300);
        return position;
    }
    public static PointF getPosition()
    {
        if(position==null)
            position= new PointF(300,300);
        return position;
    }
    public static void setPosition(PointF position)
    {
     Location.position = position;
    }
}
