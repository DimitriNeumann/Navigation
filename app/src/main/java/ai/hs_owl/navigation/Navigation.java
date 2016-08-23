package ai.hs_owl.navigation;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ai.hs_owl.navigation.connection.Synchronize;

public class Navigation extends Fragment {
    Map map;
    private static final int REQUEST_BLUETOOTH_ENABLE = 1;

    AltBeacon altBeacon;

    public Navigation() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // der View, welcher das komplette Fragment beinhaltet.
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        // die Karte
        map = (Map) root.findViewById(R.id.view);
        map.initialise();
        // Buttons mit Funktionen belegen
      //  initializeButton(root);
        // Beacons Scanner initialisieren
        altBeacon = new AltBeacon(Navigation.this.getContext());
        checkBluetooth();

        return root;
    }

    /*private void initializeButton(View v) {
        v.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Synchronize.sync(Navigation.this.getContext());

            }
        });
        final Button b = (Button) v.findViewById(R.id.scan);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (altBeacon.isScanning()) {
                    b.setText("Scan");
                    altBeacon.stop();
                    Map.run = false;
                } else {
                    b.setText("Stop");
                    altBeacon.start();
                    Map.run = true;
                }


            }
        });

    }*/
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BLUETOOTH_ENABLE) {
            if (resultCode == Activity.RESULT_OK) {
            }
        }
    }
}
