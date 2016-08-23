package ai.hs_owl.navigation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import ai.hs_owl.navigation.database.Queries;
import ai.hs_owl.navigation.protocol.IBeacon;
import ai.hs_owl.navigation.protocol.IBeaconListener;
import ai.hs_owl.navigation.protocol.IBeaconProtocol;
import ai.hs_owl.navigation.protocol.Utils;

// This activity implements IBeaconListener to receive events about iBeacon discovery
public class BeaconLocating implements IBeaconListener {


    private static ArrayList<IBeacon> beacons;
    private static IBeaconProtocol _ibp;
    Context c;

    public BeaconLocating(Context c)
    {
        this.c = c;
        if(beacons == null)
            beacons = new ArrayList<IBeacon>();


        _ibp = IBeaconProtocol.getInstance(c);
        IBeaconProtocol.configureBluetoothAdapter(c);
        _ibp.setListener(this);
    }

    private void scanBeacons(){
        beacons.clear();leras
        if(_ibp.isScanning())
                _ibp.stopScan();
            _ibp.reset();
            _ibp.startScan();
        Log.i("Status", _ibp.isScanning()+"");

    }
    // The following methods implement the IBeaconListener interface

    @Override
    public void beaconFound(IBeacon ibeacon) {
        beacons.add(ibeacon);
        Log.w("Beacon found", ibeacon.getUuidHexString());
        checkBeaconsLength();
    }

    @Override
    public void enterRegion(IBeacon ibeacon) {
        // TODO Auto-generated method stub

    }

    @Override
    public void exitRegion(IBeacon ibeacon) {
        // TODO Auto-generated method stub

    }

    @Override
    public void operationError(int status) {
        Log.i(Utils.LOG_TAG, "Bluetooth error: " + status);

    }

    @Override
    public void searchState(int state) {
        if(state == IBeaconProtocol.SEARCH_STARTED){
        }else if (state == IBeaconProtocol.SEARCH_END_EMPTY || state == IBeaconProtocol.SEARCH_END_SUCCESS){
            Toast.makeText(c, "Suche beendet", Toast.LENGTH_SHORT).show();
        }

    }
    private void checkBeaconsLength()
    {

        if(beacons.size()>=3)
        {
            IBeacon[] beacons_array =Arrays.copyOf(beacons.toArray(), beacons.size(), IBeacon[].class);
            Arrays.sort(beacons_array, new Comparator<IBeacon>() {
                @Override
                public int compare(IBeacon lhs, IBeacon rhs) {
                    if(lhs.getProximity()<rhs.getProximity())
                        return -1;
                    if(lhs.getProximity()==rhs.getProximity())
                        return 0;
                    return 1;
                }
            });
            float[][] beacon_positions = new float[3][2];
            for(int i=0; i<3; i++)
            {
                if(Queries.getInstance(c).hasBeacon(beacons.get(i).getUuidHexString()))
                    beacon_positions[i] = Queries.getInstance(c).getPositionOfBeacon(beacons.get(i).getUuidHexString());
                else
                {
                    beacons.remove(i);
                    if(beacons.size()<3)
                        return;
                    i-=1;
                }
            }
            PointF position = calcXYPos(beacon_positions[0][0], beacon_positions[0][1],beacon_positions[1][0], beacon_positions[1][1],beacon_positions[2][0], beacon_positions[2][1],beacons.get(0).getProximity(),beacons.get(1).getProximity(),beacons.get(2).getProximity());
            Toast.makeText(c, position.x + "  "+ position.y, Toast.LENGTH_LONG).show();
            Location.setPosition(calcXYPos(beacon_positions[0][0], beacon_positions[0][1],beacon_positions[1][0], beacon_positions[1][1],beacon_positions[2][0], beacon_positions[2][1],beacons.get(0).getProximity(),beacons.get(1).getProximity(),beacons.get(2).getProximity() ));
            this.stop();
        }
    }
    public void start()
    {
        scanBeacons();
    }
    public void stop()
    {
        _ibp.reset();
        _ibp.stopScan();
    }
    private PointF calcXYPos (double x_1, double y_1, double x_2, double y_2, double x_3, double y_3, double d_1, double d_2, double d_3){
        // Berechnung der X und Y Koordinate
        double x=0,y=0;
        //y = (   (x_2 - x_1)*(Math.pow(x_3,2) + Math.pow(y_3,2) - Math.pow(d_3,2))
        //        + (x_1 - x_3)*(Math.pow(x_2,2) + Math.pow(y_2,2) - Math.pow(d_2,2))
        //        + (x_3 - x_2)*(Math.pow(x_1,2) + Math.pow(y_1,2) - Math.pow(d_1,2))
        //)       / (2*(y_3*(x_2 - x_1) + y_2*(x_1 - x_3) + y_1*(x_3 - x_2)));

        //x = (   Math.pow(d_2,2) + Math.pow(x_1,2) + Math.pow(y_1,2)
        //        - Math.pow(d_1,2) - Math.pow(x_2,2) - Math.pow(y_2,2)
        //        - (2* (y_1 - y_2) * y)
        //)       / (2*(x_1 - x_2));

        double S = (Math.pow(x_3, 2) - Math.pow(x_2, 2) + Math.pow(y_3, 2) - Math.pow(y_2, 2) + Math.pow(d_2, 2) - Math.pow(d_3, 2.)) / 2.0;
        double T = (Math.pow(x_1, 2) - Math.pow(x_2, 2) + Math.pow(y_1, 2) - Math.pow(y_2, 2) + Math.pow(d_2, 2) - Math.pow(d_1, 2.)) / 2.0;
        y = ((T * (x_2 - x_3)) - (S * (x_2 - x_1))) / (((y_1 - y_2) * (x_2 - x_3)) - ((y_3 - y_2) * (x_2 - x_1)));
        x = ((y * (y_1 - y_2)) - T) / (x_2 - x_1);

        return new PointF((float)x,(float)y);
    }
}
