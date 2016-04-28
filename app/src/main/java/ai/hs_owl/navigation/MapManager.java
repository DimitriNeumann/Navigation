package ai.hs_owl.navigation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * Created by mberg on 22.04.2016.
 */
public class MapManager extends SubsamplingScaleImageView {
    public int strokeWidth;

    public MapManager(Context context)
    {
        super(context);
    }
    public MapManager(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        initialise();
    }

    private void initialise() {
        float density = getResources().getDisplayMetrics().densityDpi;
        strokeWidth = (int)(density/60f);
        setMinimumScaleType(this.SCALE_TYPE_CENTER_CROP);
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

       PointF sCenter = new PointF(getSWidth()/2, getSHeight()/2);
       PointF vCenter = sourceToViewCoord(sCenter);

       float radius = (getScale() * getSWidth()) * 0.1f;

       PointF sLeft = new PointF(radius/2, radius/2);
       vCenter = sourceToViewCoord(sLeft);

       c.drawCircle(vCenter.x, vCenter.y, radius, paint);
       paint.setColor(Color.BLACK);
       c.drawText("DRAW", vCenter.x-20, vCenter.y+20, paint);
   }
}
