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

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false); // der View, welcher das komplette Fragment beinhaltet.
        map = (Map) root.findViewById(R.id.view);
        //map.setImage(ImageSource.uri(LayerManager.getPathToLayer(1)));

        initializeButton(root);
        return root;
    }
    private void initializeButton(View v)
    {
        v.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Synchronize.sync(MainActivityFragment.this.getContext());
        }
    });}
}
