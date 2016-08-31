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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ai.hs_owl.navigation.Routenberechnung.Dijkstra;
import ai.hs_owl.navigation.Routenberechnung.Ort;
import ai.hs_owl.navigation.connection.Synchronize;
import ai.hs_owl.navigation.database.Queries;
import ai.hs_owl.navigation.datastructures.Knoten;
import ai.hs_owl.navigation.map.AltBeacon;
import ai.hs_owl.navigation.map.Location;
import ai.hs_owl.navigation.map.Map;

public class Navigation extends Fragment {
    Map map;
    private static final int REQUEST_BLUETOOTH_ENABLE = 1;
    Knoten[] results;
    AltBeacon altBeacon;

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
                root.findViewById(R.id.listView).setVisibility(View.GONE);
            }
        });
        map.initialise();
        altBeacon = new AltBeacon(Navigation.this.getContext());
        checkBluetooth();
        initializeEditText(root);
        return root;
    }
    @Override
    public void onResume()
    {
        super.onResume();
        altBeacon.start();
        if(Synchronize.syncNeeded(this.getContext())) {
            Log.i("SyncNeeded", true+"");
            Synchronize.sync(this.getContext());
        }
        else
            Log.i("SyncNeeded", false+"");

    }
    @Override
    public void onPause()
    {
        super.onPause();
        altBeacon.stop();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BLUETOOTH_ENABLE) {
            if (resultCode == Activity.RESULT_OK) {
            }
        }
    }


    public boolean checkBluetooth() {
        // Check Bluetooth every time
        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        // Filter based on default easiBeacon UUID, remove if not required
        //_ibp.setScanUUID(UUID here);

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLE);
            return false;
        }
        return true;
    }
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
    private void showFavorites()
    {
        String[] show = new String[3];
        show[0] = "Bibliothek";
        show[1] = "Mensa";
        show[2] = "Prüfungsamt";
        final ListView listView = (ListView) getView().findViewById(R.id.listView);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_list_item_1, show);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(Navigation.this.getContext(), (String)parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                final ListView listView = (ListView) Navigation.this.getView().findViewById(R.id.listView);
                listView.setVisibility(View.GONE);
            }
        });
    }
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
                    ArrayList<Ort> weg = Dijkstra.calculate (Queries.getInstance(Navigation.this.getContext()).getNearestKnot(Location.getPositionOnMap()), results[position].getId(), Navigation.this.getContext());
                    //map.startNavigation(weg);
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
