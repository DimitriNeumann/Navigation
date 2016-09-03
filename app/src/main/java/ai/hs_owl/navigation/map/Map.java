package ai.hs_owl.navigation.map;

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

import ai.hs_owl.navigation.R;
import ai.hs_owl.navigation.Routenberechnung.Ort;
import ai.hs_owl.navigation.connection.Synchronize;
import ai.hs_owl.navigation.database.Queries;

/**
 * Created by mberg on 22.04.2016.
 */
public class Map extends SubsamplingScaleImageView {
    public static boolean run = true;
    public int strokeWidth;
    Bitmap icon;
    Paint paint;

    //Navigation
    PointF points[];
    boolean navigationRunning = false;
    private static int radiusReached = 30;
    //Winkel
    float angle=-1;
    static float lastAngle=0;
    public Map(Context context) {
        super(context);
    }

    public Map(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void initialize() {
        Log.i("Init", true+"");
        setWillNotDraw(false);

        float density = getResources().getDisplayMetrics().densityDpi;
        strokeWidth = (int) (density / 60f);
        setMinimumScaleType(SCALE_TYPE_CENTER_CROP);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        //icon = BitmapFactory.decodeResource(this.getContext().getResources(), R.mipmap.loca, options);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);

        paint.setTextSize(40);

        RefreshMap rm = new RefreshMap(this);
        rm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (!isReady())
            return;


        PointF sLeft = Location.getPositionOnMap();
        sLeft.x -= 15;
        sLeft.y -= 15;
        PointF vCenter = sourceToViewCoord(sLeft);

        //c.drawCircle(vCenter.x, vCenter.y, 30, paint);

        Log.i("Angle:", angle+"");
        paint.setColor(Color.BLACK);
        double x=vCenter.x;
        double y=vCenter.y;
        c.drawCircle(Float.valueOf(x+""),Float.valueOf(y+""),40, paint);
        paint.setColor(Color.RED);

        double endX   = x - 20 * Math.sin(Math.toRadians(angle));
        double endY   = y - 20 * Math.cos(Math.toRadians(angle));
        c.drawLine(Float.valueOf(x+""),Float.valueOf(y+""),Float.valueOf(endX+""),Float.valueOf(endY+""), paint);


        points = didReachPoint();
        if (points==null || points.length == 0)
            exitNavigation();
        if(navigationRunning)
        drawRoute(c, points);


    }
    public void startNavigation(ArrayList<Ort> ids)
    {
        points = new PointF[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            points[i] = Queries.getInstance(getContext()).searchNode(""+ids.get(i).getID());
        }
        navigationRunning=true;
    }

    public void setBackground(ImageSource s)
    {
        this.setImage(s);
    }
    private void exitNavigation()
    {
        navigationRunning=false;
    }
    public void setAngle(float angle)
    {
        this.lastAngle=this.angle;
        this.angle=angle;
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

    private class RefreshMap extends AsyncTask<String, Integer, String> {
        Map map;
        int layer = -1;

        public RefreshMap(Map m) {
            map = m;
        }

        @Override
        protected String doInBackground(String... params) {
            while (Map.run) {
             if (layer != Location.getLayer() || Synchronize.updated) {
                    Synchronize.updated = false;
                    layer = Location.getLayer();
                    publishProgress(1);
                }
                publishProgress(0);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        public void onProgressUpdate(Integer... progress) {
            if (progress[0] == 1) {
                Log.i("Location:", Location.getLayer() + "");
                ImageSource image = LayerManager.getImageSource(Location.getLayer());
                if (image != null)
                    map.setImage(image);
                else
                    Toast.makeText(map.getContext(), "Keine Daten vorhanden!", Toast.LENGTH_LONG).show();

                map.invalidate();
            } else if (progress[0] == 0)
                map.invalidate();
        }
    }

}
