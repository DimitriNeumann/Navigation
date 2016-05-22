package ai.hs_owl.navigation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import ai.hs_owl.navigation.connection.Synchronize;
import ai.hs_owl.navigation.database.LayerManager;
import ai.hs_owl.navigation.protocol.IBeacon;
import ai.hs_owl.navigation.protocol.IBeaconListener;
import ai.hs_owl.navigation.protocol.IBeaconProtocol;
import ai.hs_owl.navigation.protocol.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    Map map;
    private static IBeaconProtocol _ibp;
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_BLUETOOTH_ENABLE = 1;
    private static HashMap<String, IBeacon> _beacons;
    BeaconTest bt;
    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false); // der View, welcher das komplette Fragment beinhaltet.
        map = (Map) root.findViewById(R.id.view);
        //map.setImage(ImageSource.uri(LayerManager.getPathToLayer(1)));
        initializeButton(root);

        bt = new BeaconTest(getActivity());
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        checkBluetooth();
        bt.onActivityCreated(mBluetoothAdapter);
    }

    @Override
    public void onStart(){
        super.onStart();
        bt.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        bt.onStop();
    }

    public void checkBluetooth(){
        // Check Bluetooth every time
        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Filter based on default easiBeacon UUID, remove if not required
        //_ibp.setScanUUID(UUID here);

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLE );
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        checkBluetooth();
        bt.onActivityResult(requestCode,resultCode,data);
    }
}
