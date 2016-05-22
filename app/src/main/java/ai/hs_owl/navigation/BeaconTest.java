package ai.hs_owl.navigation;

/**
 * Created by dimitri on 13.05.2016.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import ai.hs_owl.navigation.protocol.IBeacon;
import ai.hs_owl.navigation.protocol.IBeaconListener;
import ai.hs_owl.navigation.protocol.IBeaconProtocol;
import ai.hs_owl.navigation.protocol.Utils;

import java.util.Arrays;
import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class BeaconTest implements IBeaconListener {

    private static final int REQUEST_BLUETOOTH_ENABLE = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private static HashMap<String, IBeacon> _beacons;
    private static IBeaconProtocol _ibp;
    private static FragmentActivity fragmentActivity;

    public BeaconTest(FragmentActivity fragActivity) {
        fragmentActivity = fragActivity;
    }

    public void onActivityCreated(BluetoothAdapter bluetoothAdapter) {
        mBluetoothAdapter = bluetoothAdapter;
        Log.d("easibeacon", "Searching Beacons");

        _ibp = IBeaconProtocol.getInstance(fragmentActivity);
        _ibp.setListener(this);

        if(_beacons == null)
            _beacons = new HashMap<String, IBeacon>();
    }

    public void onStart(){
       // TODO: ??
    }

    public void onStop() {
        _ibp.stopScan();
    }

    public void createBeaconHashMap(){
        for(String key : _beacons.keySet())
        {
            Log.i(Utils.LOG_TAG,"Key: " + key + " - ");
            Log.i(Utils.LOG_TAG,"Value: " + _beacons.get(key).getProximity() + "\n");
        }
    }

    public void scanBeacons(FragmentActivity fragmentActivity){
        Log.i(Utils.LOG_TAG,"Scanning");
        mBluetoothAdapter.startLeScan(mLeScanCallback);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                createBeaconHashMap();
            }
        }, 8000);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            int startByte = 2;
            boolean patternFound = false;
            while (startByte <= 5) {
                if (    ((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                        ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                    patternFound = true;
                    break;
                }
                startByte++;
            }

            if (patternFound) {
                //Convert to hex String
                byte[] uuidBytes = new byte[16];
                System.arraycopy(scanRecord, startByte+4, uuidBytes, 0, 16);
                String hexString = bytesToHex(uuidBytes);

                //Here is your UUID
                String uuid =  hexString.substring(0,8) + "-" +
                        hexString.substring(8,12) + "-" +
                        hexString.substring(12,16) + "-" +
                        hexString.substring(16,20) + "-" +
                        hexString.substring(20,32);

                //Here is your Major value
                int major = (scanRecord[startByte+20] & 0xff) * 0x100 + (scanRecord[startByte+21] & 0xff);

                //Here is your Minor value
                int minor = (scanRecord[startByte+22] & 0xff) * 0x100 + (scanRecord[startByte+23] & 0xff);

                //Here is your Distance
                byte powerValue = scanRecord[startByte+24];
                int newDistance = (int)calculateDistance((int)powerValue, rssi);

                Log.i(Utils.LOG_TAG,"Beacon UUID: " + uuid);
                Log.i(Utils.LOG_TAG,"Beacon major: " + major);
                Log.i(Utils.LOG_TAG,"Beacon minor: " + minor);
                Log.i(Utils.LOG_TAG,"Beacon distance: " + newDistance);

                IBeacon beacon =  new IBeacon();
                beacon.setUuid(uuid.getBytes());
                beacon.setMajor(major);
                beacon.setMinor(minor);
                beacon.setProximity(newDistance);

                Log.i(Utils.LOG_TAG,"BeaconList UUID: " + Arrays.toString(beacon.getUuid()));
                Log.i(Utils.LOG_TAG,"Beacon Device ID: " + device.getAddress());
                //Log.i(Utils.LOG_TAG,"BeaconList major: " + beacon.getMajor());
                //Log.i(Utils.LOG_TAG,"BeaconList minor: " + beacon.getMinor());
                //Log.i(Utils.LOG_TAG,"BeaconList distance: " + beacon.getProximity());

                _beacons.put(device.getAddress(), beacon);
            }
        }
    };

    /**
     * bytesToHex method
     * Found on the internet
     * http://stackoverflow.com/a/9855338
     */
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Roughly estimates the distance to the iBeacon
     * Calculation obtained from http://stackoverflow.com/questions/20416218/understanding-ibeacon-distancing
     *
     * @param txPower RSSI of the iBeacon at 1 meter
     * @param rssi measured RSSI by the user device
     * @return
     */
    private double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BLUETOOTH_ENABLE) {
            scanBeacons(fragmentActivity);
        }
    }

    @Override
    public void enterRegion(IBeacon ibeacon) {

    }

    @Override
    public void exitRegion(IBeacon ibeacon) {

    }

    @Override
    public void searchState(int state) {

    }

    @Override
    public void beaconFound(IBeacon ibeacon) {

    }

    @Override
    public void operationError(int status) {
        Log.i(Utils.LOG_TAG, "Bluetooth error: " + status);
    }

    private PointF calcXYPos (double x_1, double y_1, double x_2, double y_2, double x_3, double y_3, double d_1, double d_2, double d_3){
        // Berechnung der X und Y Koordinate
        double x=0,y=0;
        y = (   (x_2 - x_1)*(Math.pow(x_3,2) + Math.pow(y_3,2) - Math.pow(d_3,2))
                + (x_1 - x_3)*(Math.pow(x_2,2) + Math.pow(y_2,2) - Math.pow(d_2,2))
                + (x_3 - x_2)*(Math.pow(x_1,2) + Math.pow(y_1,2) - Math.pow(d_1,2))
        )       / (2*(y_3*(x_2 - x_1) + y_2*(x_1 - x_3) + y_1*(x_3 - x_2)));

        x = (   Math.pow(d_2,2) + Math.pow(x_1,2) + Math.pow(y_1,2)
                - Math.pow(d_1,2) - Math.pow(x_2,2) - Math.pow(y_2,2)
                - (2* (y_1 - y_2) * y)
        )       / (2*(x_1 - x_2));

        return new PointF((float)x,(float)y);
    }

}