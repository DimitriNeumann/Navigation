package ai.hs_owl.navigation;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;

import ai.hs_owl.navigation.connection.Synchronize;
import ai.hs_owl.navigation.database.LayerManager;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    Map map;
    Runnable run = new Runnable()
    {

        int layer=-1;
        @Override
        public void run() {
            while(true)
            {
                if((layer==-1 && Location.getLayer()!=-1)|| layer!=Location.getLayer()) { // wurde erstmals gesetzt
                    layer = Location.getLayer();
                    String uri = LayerManager.getPathToLayer(layer);
                    Log.i("uri", uri);
                    if(uri!=null) {
                        Log.i("uri", "not null");
                        map.setImage(ImageSource.uri(uri));
                    }
                }
               // map.invalidate();
            }
        }
    };
    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Toast.makeText(this.getContext(), R.string.hello_fragment, Toast.LENGTH_SHORT).show();

        View root = inflater.inflate(R.layout.fragment_main, container, false); // der View, welcher das komplette Fragment beinhaltet.
        map = (Map) root.findViewById(R.id.view);
        map.setImage(ImageSource.uri(LayerManager.getPathToLayer(1)));

        root.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Synchronize.sync(MainActivityFragment.this.getContext());
            }
        });
        //this.getActivity().runOnUiThread(run);
        return root;
    }
}
