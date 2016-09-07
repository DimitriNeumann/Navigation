package ai.hs_owl.navigation;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;

import java.util.ArrayList;

import ai.hs_owl.navigation.Routenberechnung.Dijkstra;
import ai.hs_owl.navigation.Routenberechnung.Ort;
import ai.hs_owl.navigation.connection.Synchronize;
import ai.hs_owl.navigation.database.Queries;
import ai.hs_owl.navigation.datastructures.Knoten;
import ai.hs_owl.navigation.map.AltBeacon;
import ai.hs_owl.navigation.map.LayerManager;
import ai.hs_owl.navigation.map.Location;
import ai.hs_owl.navigation.map.Map;
import ai.hs_owl.navigation.map.Orientation;
/**
 * Stellt ein gefülltes Fragment dar, welches einfach in die Haupt Applikation eingesetzt werden kann.
 * Dabei wird hauptsächlich die Karte verwaltet, und die Suche ausgeführt.
 * */
public class Navigation extends Fragment {

    Map map;
    private static final int REQUEST_BLUETOOTH_ENABLE = 1;
    Knoten[] results;
    AltBeacon altBeacon;
    Orientation orientation;

    public Navigation() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // der View, welcher das komplette Fragment beinhaltet.
        final View root = inflater.inflate(R.layout.fragment_main, container, false);
        // die Karte
        map = (Map) root.findViewById(R.id.view);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Versteckt die Liste der Ergebnisse, wenn auf die Karte geklickt wird.
                root.findViewById(R.id.listView).setVisibility(View.GONE);
            }
        });
        map.initialize(this);
        map.changeLayer(+1);

        //Ortung
        altBeacon = new AltBeacon(Navigation.this.getContext(), map);

        // Suchfeld und Kontrollfelder
        initializeEditText(root);
        initializeControlls(root);
        // Blickrichtung
       orientation= new Orientation(Navigation.this.getContext());

        return root;
    }
    /**
     * Aktiviert die Ortung beim erneuten aufrufen
     * Überprüft, ob Synchronisiert werden muss
     * Startet die Orientierungsberechnung
     * */
    @Override
    public void onResume()
    {
        super.onResume();
        ((Switch) getView().findViewById(R.id.switch1)).setChecked(true);
        if(Synchronize.syncNeeded(this.getContext())) {
            Log.i("SyncNeeded", true+"");
            Synchronize.sync(this.getContext(), map);
        }

        orientation.getOrientation(new Orientation.DataHandler() {
            @Override
            public void receiveData(float o) {
                map.setAngle(o);
            }
        });
        if(checkBluetooth())
            altBeacon.start();

    }
    @Override
    public void onPause()
    {
        super.onPause();
        orientation.stop();
        altBeacon.stop();
    }
    /*
    * Wird aufgerufen, wenn die Bluetooth Anfrage gesetzt wurde
    * **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BLUETOOTH_ENABLE) {
            if (resultCode == Activity.RESULT_OK) {
                final Switch aSwitch = (Switch) getView().findViewById(R.id.switch1);
                aSwitch.setChecked(true);
            }
        }
    }

    /**
     * @return boolean true wenn Bluetooth eingeschaltet ist, sonst false
     * */
    public boolean checkBluetooth() {
        // Check Bluetooth every time
        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLE);
            return false;
        }
        return true;
    }
    /*
    * Initialisiert die Such Eingabe, öffnet die Liste der Favoriten beim Klicken
    * **/
    private void initializeEditText(View v)
    {
        final ListView listView = (ListView) v.findViewById(R.id.listView);
        final EditText search = (EditText)v.findViewById(R.id.editText);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    performSearch(search.getText().toString());
                    return true;
                }
                return false;

            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.setVisibility(View.VISIBLE);
                showFavorites();
            }
        });
    }
    /*
    * Initilialisiert die Buttons zum Wechseln der Ebene, zum Beenden der Navigation und den Switch zur Kontrolle der Ortung
    * **/
    private void initializeControlls(View root)
    {
        final Switch aSwitch = (Switch) root.findViewById(R.id.switch1);
        Button up = (Button) root.findViewById(R.id.button2);
        Button down = (Button) root.findViewById(R.id.button);
        Button exit = (Button) root.findViewById(R.id.button3);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    altBeacon.start();
                }
                else
                {
                    altBeacon.stop();
                }
            }
        });
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aSwitch.setChecked(false);
                altBeacon.stop();
                map.changeLayer(1);
                
            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aSwitch.setChecked(false);
                altBeacon.stop();
                map.changeLayer(-1);


            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map.exitNavigation();
            }
        });
    }
    /*
    * Öffnet die Favoriten und zeigt diese in der Liste an. Startet die Navigation zu diesen Orten beim Klicken
    * **/
    private void showFavorites()
    {
        results = Queries.getInstance(this.getContext()).getFavorites();
        String[] show = new String[results.length];
        for(int i=0; i<results.length; i++) {
            show[i] = results[i].getBeschreibung();
        }

        final ListView listView = (ListView) getView().findViewById(R.id.listView);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_list_item_1, show);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    int start = Queries.getInstance(Navigation.this.getContext()).getNearestKnot(Location.getPositionOnMap());

                    ArrayList<Integer> weg = Dijkstra.calculate (start, results[position].getId(), Navigation.this.getContext());
                    map.startNavigation(weg);
                    showNavigationButton(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(Navigation.this.getContext(), "Fehler bei der Berechnung", Toast.LENGTH_SHORT).show();
                }


                final ListView listView = (ListView) Navigation.this.getView().findViewById(R.id.listView);
                listView.setVisibility(View.GONE);
            }
        });
    }
    public void showNavigationButton(boolean show)
    {
        Button b = (Button) getView().findViewById(R.id.button3);
        if(show)
            b.setVisibility(View.VISIBLE);
        else
            b.setVisibility(View.GONE);
    }
    /*
    * Sucht die passenden Knoten aus der Datenbank raus und zeigt die Ergebnisse in der Liste an
    * Startet beim Klickena auf einen Knoten die Navigation an dieses Ort
    * **/
    private void performSearch(String text)
    {
        // hole Ergebnisse
        results=  Queries.getInstance(this.getContext()).searchKnots(text);
        String[] show = new String[results.length];
        for(int i=0; i<results.length; i++) {
            show[i] = results[i].getBeschreibung();
        }

        //initialisiere Liste mit Ergebnissen
        final ListView listView = (ListView) getView().findViewById(R.id.listView);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_list_item_1, show);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    ArrayList<Integer> weg = Dijkstra.calculate (Queries.getInstance(Navigation.this.getContext()).getNearestKnot(Location.getPositionOnMap()), results[position].getId(), Navigation.this.getContext());
                    map.startNavigation(weg);
                    showNavigationButton(true);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(Navigation.this.getContext(), "Fehler bei der Berechnung", Toast.LENGTH_SHORT).show();
                }


                final ListView listView = (ListView) Navigation.this.getView().findViewById(R.id.listView);
                listView.setVisibility(View.GONE);
            }
        });
    }
}
