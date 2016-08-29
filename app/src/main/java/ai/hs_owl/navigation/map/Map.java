package ai.hs_owl.navigation.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import ai.hs_owl.navigation.R;
import ai.hs_owl.navigation.connection.Synchronize;

/**
 * Created by mberg on 22.04.2016.
 */
public class Map extends SubsamplingScaleImageView {
    public static boolean run = true;
    public int strokeWidth;
    Bitmap icon;
    Paint paint;


    public Map(Context context) {
        super(context);
    }

    public Map(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initialise();
    }

    public void initialise() {
        setWillNotDraw(false);

        float density = getResources().getDisplayMetrics().densityDpi;
        strokeWidth = (int) (density / 60f);
        setMinimumScaleType(this.SCALE_TYPE_CENTER_CROP);
        icon = BitmapFactory.decodeResource(this.getContext().getResources(), R.mipmap.location_icon);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(strokeWidth * 2);
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
        PointF vCenter = sourceToViewCoord(sLeft);
        vCenter.x = vCenter.x - icon.getWidth() / 2;
        vCenter.y = vCenter.y - icon.getHeight() / 2;


        c.drawBitmap(icon, vCenter.x, vCenter.y, paint);


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
                    Thread.sleep(1500);
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
