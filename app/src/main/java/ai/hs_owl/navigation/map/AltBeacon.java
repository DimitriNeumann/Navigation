package ai.hs_owl.navigation.map;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import ai.hs_owl.navigation.database.Queries;

/**
 * Diese Klassen benutzt die AltBeacon API und scannt, wenn gestartet, nach neuen Beacons und berechnet direkt die Position des Smartphones.
 */
public class AltBeacon implements BeaconConsumer {
    private BeaconManager beaconManager;
    private Context c;
    private LocationHandler handler;

    public static boolean scanning = false;

    public interface LocationHandler
    {
        void newPositioncalculated();
    }

    public AltBeacon(Context c, LocationHandler handler) {
        this.handler = handler;
        this.c = c;
        beaconManager = BeaconManager.getInstanceForApplication(c); // wird benötigt, wenn der BeaconConsumer außerhalb einer Activity aufgebaut wird

        // möglichst viele Beacons einfangen
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));  // iBeacons
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));      // Estimotes
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=a7ae2eb7,i:4-19,i:20-21,i:22-23,p:24-24")); // Easibeacons

        //die Scan Periode, hier dran kann zum optimieren noch geschraubt werden
        beaconManager.setForegroundScanPeriod(3000);


    }

    /*
    * Startet den Scan
    * **/
    public void start() {
        // Flag zum bestimmen, ob zu diesem Zeitpunkt gescannt wird
        scanning = true;

        if (!beaconManager.isAnyConsumerBound())
            beaconManager.bind(this);
        else
            try {
                beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            } catch (RemoteException e) {
                e.printStackTrace();
            }

    }

    /**
     * Stoppt den Scan
     */
    public void stop() {
        if (isScanning())
            try {
                scanning = false;
                beaconManager.stopRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
    }

    /*
    * @return boolean Beim Scannen true, sonst false
    * Gibt an, ob zu diesem Zeitpunkt gescannt und berechnet wird
    * **/
    public boolean isScanning() {
        return scanning;
    }

    /**
     * Wird ausgeführt, wenn sich zum BeaconManager verbunden wurde
     * Fügt den gesamten Prozess zum berechnen der Position und des Layers zusammen
     */
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {

            /*
            * Wenn mehr als 3 Beacons gefunden wurden, werden diese nach der Entfernung sortiert, aussortiert, falls diese nicht in der Tabelle enthalten sind,
            * die Mehrzahl an Layern der Beacons ermittelt und gesetzt, die Position berechnet und übergeben.
            * **/
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {




                if (beacons.size() >= 3) {
                    Log.i("Beacon Found ", beacons.size()+"");
                    // Beacons sortieren
                    Beacon[] beacons_array = Arrays.copyOf(beacons.toArray(), beacons.size(), Beacon[].class);
                    Arrays.sort(beacons_array, new Comparator<Beacon>() {
                        @Override
                        public int compare(Beacon lhs, Beacon rhs) {
                            if (lhs.getDistance() < rhs.getDistance())
                                return -1;
                            if (lhs.getDistance() == rhs.getDistance())
                                return 0;
                            return 1;
                        }
                    });
                    ArrayList<Beacon> beaconList = new ArrayList<Beacon>(Arrays.asList(beacons_array));

                    // Beacons aussortieren
                    float[][] beacon_positions = new float[3][3];
                    for (int i = 0; i < 3; i++) {

                        if (Queries.getInstance(c).hasBeacon(beaconList.get(i).getId1().toUuid().toString().toUpperCase().replaceAll("-", "")))
                        {
                            beacon_positions[i] = Queries.getInstance(c).getPositionOfBeacon(beaconList.get(i).getId1().toUuid().toString().toUpperCase().replaceAll("-", ""));
                        }
                        else {
                            beaconList.remove(i);
                            if (beaconList.size() < 3)
                                return;
                            i -= 1;
                        }
                    }
                    // Layer bestimmen
                    Location.setLayer(getLayerFromBeacons(beacon_positions));
                    // Position berechnen und übergeben
                    Location.setPosition(calcXYPos(beacon_positions[0][0], beacon_positions[0][1], beacon_positions[1][0], beacon_positions[1][1], beacon_positions[2][0], beacon_positions[2][1], beaconList.get(0).getDistance(), beaconList.get(1).getDistance(), beaconList.get(2).getDistance()));

                     handler.newPositioncalculated();



                }
            }

        });

        // starten des Scannens
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Context getApplicationContext() {
        return c.getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        scanning = false;
        c.unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        scanning = true;
        return c.bindService(intent, serviceConnection, i);
    }

    /*
    * Nimmt drei Koordinaten paarweise entgegen, anschließend die jeweiligen Entfernungen.
    * @param x_1 X-Wert der ersten Koordinate
    * @param x_2 X-Wert der zweiten Koordinate
    * @param x_3 X-Wert der dritten Koordinate
    * @param y_1 Y-Wert der ersten Koordinate
    * @param y_2 Y-Wert der zweiten Koordinate
    * @param y_3 Y-Wert der dritten Koordinate
    * @param d_1 Entfernung der ersten Koordinate
    * @param d_2 Entfernung der zweiten Koordinate
    * @param d_3 Entfernung der dritten Koordinate
     * @return PointF die berechnete Position
    * **/
    private PointF calcXYPos(double x_1, double y_1, double x_2, double y_2, double x_3, double y_3, double d_1, double d_2, double d_3) {
        double x, y;

        double S = (Math.pow(x_3, 2) - Math.pow(x_2, 2) + Math.pow(y_3, 2) - Math.pow(y_2, 2) + Math.pow(d_2, 2) - Math.pow(d_3, 2.)) / 2.0;
        double T = (Math.pow(x_1, 2) - Math.pow(x_2, 2) + Math.pow(y_1, 2) - Math.pow(y_2, 2) + Math.pow(d_2, 2) - Math.pow(d_1, 2.)) / 2.0;
        y = ((T * (x_2 - x_3)) - (S * (x_2 - x_1))) / (((y_1 - y_2) * (x_2 - x_3)) - ((y_3 - y_2) * (x_2 - x_1)));
        x = ((y * (y_1 - y_2)) - T) / (x_2 - x_1);
        Log.i("Berechnete Position: ", x + "  " + y);
        return new PointF((float) x, (float) y);
    }

    /*
    * Ermittelt die Mehrzahl an Layern innerhalb der benutzen Beacons und gibt den passenden Layer zurück
    * @return int der Layer
    * @param beacons die gewählten Beacons in der Form float[][x,y,layer]
    * **/
    private int getLayerFromBeacons(float[][] beacons) {
        HashMap<Integer, Integer> layer_counts = new HashMap<>();
        for (float[] b : beacons) {
            if (layer_counts.containsKey(b[2]))
                layer_counts.put((int) b[2], layer_counts.get((b[2]) + 1));
            else
                layer_counts.put((int) b[2], 1);
        }
        int max = -1;
        int max_layer = 0;
        for (Map.Entry entry : layer_counts.entrySet())
            if ((int) entry.getValue() > max) {
                max = (int) entry.getValue();
                max_layer = (int) entry.getKey();
            }
        return max_layer;
    }
}
