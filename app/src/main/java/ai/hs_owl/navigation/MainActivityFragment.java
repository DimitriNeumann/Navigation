package ai.hs_owl.navigation;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Toast.makeText(this.getContext(), R.string.hello_fragment, Toast.LENGTH_SHORT).show();

        View root = inflater.inflate(R.layout.fragment_main, container, false); // der View, welcher das komplette Fragment beinhaltet.
        Map map = (Map) root.findViewById(R.id.view);
        map.setImage(ImageSource.resource(R.mipmap.sechsdrei));

        return root;
    }
}
