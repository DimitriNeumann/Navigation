package ai.hs_owl.navigation.map;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;

import ai.hs_owl.navigation.Navigation;
import ai.hs_owl.navigation.R;
import ai.hs_owl.navigation.Routenberechnung.Ort;
import ai.hs_owl.navigation.connection.Synchronize;
import ai.hs_owl.navigation.database.Queries;

/**
 * Eine abgewandelte Form der Klasse SubsamplingScaleView.
 * Zeigt die Kartendaten an, zeichnet die Position mit Ausrichtung auf die Karte
 * Wird bei einer neuen Position automatisch aktualisiert
 */
public class Map extends SubsamplingScaleImageView  implements AltBeacon.LocationHandler{
    public int strokeWidth;
    Paint paint;

    // Manuelles Betrachten
    int manualLayer;
    //Navigation
    PointF points[];
    boolean navigationRunning = false;
    private static int radiusReached = 30;
    Activity activity;
    Navigation n;
    //Winkel
    float angle=-1;

    int lastLayer=0;

    public Map(Context context) {
        super(context);
    }

    public Map(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void initialize(Navigation f) {
        this.n = f;
        this.activity = f.getActivity();
        setWillNotDraw(false);

        float density = getResources().getDisplayMetrics().densityDpi;
        strokeWidth = (int) (density / 60f);
        setMinimumScaleType(SCALE_TYPE_CENTER_CROP);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);

        paint.setTextSize(40);



    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (!isReady())
            return;

        // Position wird nur angezeigt, wenn die Ebene die richtige ist bzw. nicht manuell betrachtet wird
        if(this.lastLayer==manualLayer){
            PointF sLeft = Location.getPositionOnMap();
            sLeft.x -= 15;
            sLeft.y -= 15;
            PointF vCenter = sourceToViewCoord(sLeft);
            // Zeichne die Position in die Mitte zentriert
            paint.setColor(Color.BLACK);
            double x = vCenter.x;
            double y = vCenter.y;
            c.drawCircle(Float.valueOf(x + ""), Float.valueOf(y + ""), 30, paint);
            paint.setColor(Color.RED);
            // und die Linie, welche in Richtung Norden zeigt
            double endX = x - 15 * Math.sin(Math.toRadians(angle));
            double endY  = y - 15 * Math.cos(Math.toRadians(angle));
            c.drawLine(Float.valueOf(x + ""), Float.valueOf(y + ""), Float.valueOf(endX + ""), Float.valueOf(endY + ""), paint);

        }

        // wurde ein Punkt erreicht, welcher gelöscht werden kann?
        points = didReachPoint();
        // wenn keine Punkte mehr vorhanden sind (--> Ziel erreicht oder Route nicht vorhanden) beenden der Route
        if (points==null || points.length == 0)
            exitNavigation();

        if(navigationRunning)
        drawRoute(c, points);


    }
    public void changeLayer(int s)
    {
        ImageSource imageSource;
        if(s>0) {
             imageSource= LayerManager.getImageSource(manualLayer = LayerManager.up(manualLayer));
        }
        else {
            imageSource = LayerManager.getImageSource(manualLayer = LayerManager.down(manualLayer));
        }
        if(imageSource!=null)
        this.setImage(imageSource);
        invalidate();
    }
    public void setAngle(float angle)
    {
        this.angle=angle;
        invalidate();

    }

    //Navigation
    public void startNavigation(ArrayList<Integer> ids)
    {

        points = new PointF[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            points[i] = Queries.getInstance(getContext()).searchNode(""+ids.get(i));
        }
        navigationRunning=true;
    }


    public void exitNavigation()
    {
        n.showNavigationButton(false);
        navigationRunning=false;
    }

    private PointF[] didReachPoint()
    {
        if(points==null ||points.length==0)
            return points;
        int cut = -1;
        for(int i=0; i<points.length; i++) {
            PointF pointF = points[i];
            if (Location.getPositionOnMap().x - radiusReached < pointF.x && Location.getPositionOnMap().x + radiusReached > pointF.x && Location.getPositionOnMap().y - radiusReached < pointF.y && Location.getPositionOnMap().y + radiusReached > pointF.y)
            {
                cut = i;
                break;
            }
        }
        if(cut==-1)
            return points;
        PointF[] newPoints = new PointF[cut];
        for(int i=0; i<newPoints.length; i++)
        {
            newPoints[i] = points[i];
        }
        return newPoints;
    }
    private void drawRoute(Canvas c, PointF[] points) {
        PointF points2[] = new PointF[points.length];
        for(int i=0;i<points.length;i++){
            points2[i] = sourceToViewCoord(points[i]);
        }
        int ptsLength = (2+((points.length-2)*2))*2;

        float[] pts = new float[ptsLength];
        int i=0,j=0;
        while (i<pts.length){
            if(i==0){
                pts[i] = points2[j].x;
                pts[i+1] = points2[j].y;
                j++;
            }
            if(i>=2 && i<ptsLength) {
                pts[i] = points2[j].x;
                pts[i+1] = points2[j].y;
                if(i%4 == 0) j++;
            }
            if(i==ptsLength-3){
                pts[i] = points2[j].x;
                pts[i+1] = points[j].y;
            }
            i+=2;
        }
        c.drawLines(pts, paint);
    }

    @Override
    public void newPositioncalculated() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(Location.getLayer()!=lastLayer || Location.getLayer()!=manualLayer)
                {
                    manualLayer = Location.getLayer();
                    lastLayer = Location.getLayer();
                    setImage(LayerManager.getImageSource(lastLayer));
                }
                invalidate();
            }
        });

    }


}
