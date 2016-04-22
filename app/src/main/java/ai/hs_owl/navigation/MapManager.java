package ai.hs_owl.navigation;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * Created by mberg on 22.04.2016.
 */
public class MapManager {
    Context context;
    SubsamplingScaleImageView imageView;

    public MapManager(Context context, View v)
    {
        imageView = (SubsamplingScaleImageView) v;
        imageView.setMinimumScaleType(imageView.SCALE_TYPE_CENTER_CROP);
        this.context = context;
    }
    public void loadExampleImage()
    {
        imageView.setImage(ImageSource.resource(R.mipmap.example_plan));
    }
}
