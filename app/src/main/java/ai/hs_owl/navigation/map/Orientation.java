package ai.hs_owl.navigation.map;

/**
 * Created by marvinberger on 01.09.16.
 */

        import android.content.Context;
        import android.database.DataSetObservable;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.animation.RotateAnimation;
        import android.widget.Toast;

        import java.util.Calendar;
        import java.util.Date;


/**
 * Liest die Sensor und GPS Daten aus und gibt diese im DataHandler wieder
 */
public class Orientation {

    //Orientation
    DataHandler orientationHandler;


    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;

    Context c;

    public Orientation(Context c)
    {
        this.c = c;
    }

    /**
     * Stellt ein Interface zur Verfügung, damit die Daten zeitlich abgestimmt übergeben werden können
     * */
    public interface DataHandler
    {
        void receiveData(float o);
    }

    //Neigungsdaten
    /**
     * @param handler Der Handler, an welchen die Daten übergeben werden
     * Startet die Sensoren und registriert den Listener, damit dieser bei Werteänderungen aufgerufen wird.
     * */
    public void getOrientation(DataHandler handler)
    {
        orientationHandler = handler;

        mSensorManager = (SensorManager)c.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(sensorEventListener, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);


        }
    public void stop()
    {
        mSensorManager.unregisterListener(sensorEventListener);
    }
    /**
     * Der SensorEventListener, welche bei neuen Sensordaten für den Beschleunigungssensor und den Magnetfeld Sensor aufgerufen wird.
     * Die Daten werden in einer Matrix verschoben und anschließend wird der Kompasswert und die Neigung an den Handler übergeben
     * */
    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor == mAccelerometer) {
                System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
                mLastAccelerometerSet = true;
            } else if (event.sensor == mMagnetometer) {
                System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                mLastMagnetometerSet = true;
            }
            if (mLastAccelerometerSet && mLastMagnetometerSet) {
                SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
                SensorManager.getOrientation(mR, mOrientation);
                float azimuthInRadians = mOrientation[0];
                float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;


                mCurrentDegree = -azimuthInDegress;
                orientationHandler.receiveData(azimuthInDegress);
            }
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

}
