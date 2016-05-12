package ai.hs_owl.navigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import ai.hs_owl.navigation.database.LayerManager;

/**
 * Created by mberg on 22.04.2016.
 */
public class Map extends SubsamplingScaleImageView {
    public int strokeWidth;
    Bitmap icon;

    public Map(Context context)
    {
        super(context);
    }
    public Map(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        initialise();
    }

    private void initialise() {
        setWillNotDraw(false);
        float density = getResources().getDisplayMetrics().densityDpi;
        strokeWidth = (int)(density/60f);
        setMinimumScaleType(this.SCALE_TYPE_CENTER_CROP);
        icon = BitmapFactory.decodeResource(this.getContext().getResources(), R.mipmap.location_icon);
        /*Thread th = new Thread(run);
        th.start();*/
    }

    @Override
   public void onDraw(Canvas c)
   {
        super.onDraw(c);
       if(!isReady())
           return;

       Paint paint = new Paint();
       paint.setAntiAlias(true);
       paint.setColor(Color.RED);
       paint.setStrokeWidth(strokeWidth*2);
       paint.setTextSize(40);


       PointF sLeft = Location.getPositionOnMap();
       PointF vCenter = sourceToViewCoord(sLeft);
       vCenter.x = vCenter.x-icon.getWidth()/2;
       vCenter.y = vCenter.y-icon.getHeight()/2;

       //if(vCenter.x <0 || vCenter.y <0)
           c.drawBitmap(icon,vCenter.x, vCenter.y,  paint);

   }

}
