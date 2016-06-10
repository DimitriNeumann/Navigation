package ai.hs_owl.navigation;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import ai.hs_owl.navigation.connection.Synchronize;
import ai.hs_owl.navigation.protocol.IBeacon;
import ai.hs_owl.navigation.protocol.IBeaconProtocol;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    Map map;
    private static final int REQUEST_BLUETOOTH_ENABLE = 1;
    BeaconLocating beaconLocating;
    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false); // der View, welcher das komplette Fragment beinhaltet.
        map = (Map) root.findViewById(R.id.view);
        map.initialise();
        initializeButton(root);
        beaconLocating =  new BeaconLocating(MainActivityFragment.this.getContext());
        checkBluetooth();
        return root;
    }
    private void initializeButton(View v)
    {
        v.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Synchronize.sync(MainActivityFragment.this.getContext());

        }
    });
        v.findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                beaconLocating.start();
            }
        });

        }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public boolean checkBluetooth(){
        // Check Bluetooth every time
        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        // Filter based on default easiBeacon UUID, remove if not required
        //_ibp.setScanUUID(UUID here);

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLE );
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == REQUEST_BLUETOOTH_ENABLE){
            if(resultCode == Activity.RESULT_OK){
            }
        }
    }
}
