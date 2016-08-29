package ai.hs_owl.navigation.database;

import android.content.Context;
import android.database.Cursor;
import android.graphics.PointF;
import android.util.Log;

import java.sql.PreparedStatement;

import ai.hs_owl.navigation.datastructures.Knoten;

/**
 * Created by mberg on 10.05.2016.
 */
public class Queries {
    //Singleton Implementierung
    static Queries q;

    public static Queries getInstance(Context c) {
        if (q == null)
            q = new Queries(c);
        return q;
    }

    //Abfragen an die DB bzw. Schreiboperationen
    Context context;
    Database db;

    private Queries(Context c) {
        this.context = c;
        db = new Database(c);
    }

    //Lesen
    public float[] getPositionOfBeacon(String id) {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM " + db.BEACONS_TABLE_NAME + " WHERE " + db.BEACONS_COLUMN_ID + "='" + id + "'", null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            float[] returnValue = new float[]{(Float.parseFloat(c.getInt(c.getColumnIndex(db.BEACONS_COLUMN_X)) + "")),
                    Float.parseFloat(c.getInt(c.getColumnIndex(db.BEACONS_COLUMN_Y)) + ""),
                    Float.parseFloat(c.getInt(c.getColumnIndex(db.BEACONS_COLUMN_EBENE)) + "")};
            return returnValue;
        }
        return new float[]{0, 0, 0};
    }
    public boolean hasBeacons()
    {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM " + db.BEACONS_TABLE_NAME, null);
        return (c.getCount()>0);

    }
    public int getNearestBeacon(PointF loca)
    {
        int id_smallest=-1;
        double distance=Double.MAX_VALUE;
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM " + db.BEACONS_TABLE_NAME, null);
        c.moveToFirst();
        while(!c.isAfterLast())
        {
           if((Math.abs(loca.x-c.getInt(c.getColumnIndex(db.KNOTEN_COLUMN_X)))+Math.abs(loca.x-c.getInt(c.getColumnIndex(db.KNOTEN_COLUMN_X))))<distance)
           {
               id_smallest = c.getInt(c.getColumnIndex(db.KNOTEN_COLUMN_ID));
               distance = (Math.abs(loca.x-c.getInt(c.getColumnIndex(db.KNOTEN_COLUMN_X)))+Math.abs(loca.x-c.getInt(c.getColumnIndex(db.KNOTEN_COLUMN_X))));
           }
            c.moveToNext();
        }
        return id_smallest;
    }

    //Schreiben
    public void insertNewBeacon(String id, float x, float y, int ebene) {
        Log.i("Query", "INSERT INTO " + db.BEACONS_TABLE_NAME + "(" + db.BEACONS_COLUMN_ID + ", " + db.BEACONS_COLUMN_X + ", " + db.BEACONS_COLUMN_Y + ", " + db.BEACONS_COLUMN_EBENE + ") VALUES('" + id + "', " + x + ", " + y + ", " + ebene + ")");
        db.getWritableDatabase().execSQL("INSERT INTO " + db.BEACONS_TABLE_NAME + "(" + db.BEACONS_COLUMN_ID + ", " + db.BEACONS_COLUMN_X + ", " + db.BEACONS_COLUMN_Y + ", " + db.BEACONS_COLUMN_EBENE + ") VALUES('" + id + "', " + x + ", " + y + ", " + ebene + ")");
    }
    public void insertNewKnot(String id, float x, float y, int ebene, String name) {
        db.getWritableDatabase().execSQL("INSERT INTO " + db.KNOTEN_TABLE_NAME + "(" + db.KNOTEN_COLUMN_ID + ", " + db.KNOTEN_COLUMN_X + ", " + db.KNOTEN_COLUMN_Y+ ", " + db.KNOTEN_COLUMN_EBENE+ ", "+db.KNOTEN_COLUMN_BESCHREIBUNG+") VALUES('" + id + "', " + x + ", " + y + ", " + ebene + ", '"+name+"')");
    }
    public void insertNewConnection(int idA, int idB, double gewicht) {
        gewicht *=10;
        db.getWritableDatabase().execSQL("INSERT INTO " + db.VERBINDUNGEN_TABLE_NAME + "(" + db.VERBINDUNGEN_COLUMN_IDA+ ", " + db.VERBINDUNGEN_COLUMN_IDB+ ", " + db.VERBINDUNGEN_COLUMN_GEWICHT+ ", " + db.VERBINDUNGEN_COLUMN_OUTDOOR+ ") VALUES(" + idA + ", " + idB + ", " + (int) gewicht+ ", 0)");
    }


    public void clearTable(String beaconsTableName) {
        db.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + beaconsTableName);
        if(db.BEACONS_TABLE_CREATE.contains(beaconsTableName))
            db.getWritableDatabase().execSQL(db.BEACONS_TABLE_CREATE);
        if(db.KNOTEN_TABLE_CREATE.contains(beaconsTableName))
            db.getWritableDatabase().execSQL(db.KNOTEN_TABLE_CREATE);
        if(db.VERBINDUNGEN_TABLE_CREATE.contains(beaconsTableName))
            db.getWritableDatabase().execSQL(db.VERBINDUNGEN_TABLE_CREATE);
    }

    public boolean hasBeacon(String id) {
        return (db.getReadableDatabase().rawQuery("SELECT * FROM " + db.BEACONS_TABLE_NAME + " WHERE " + db.BEACONS_COLUMN_ID + "='" + id + "'", null).getCount() > 0);
    }

    public Knoten[] searchKnots(String text) {
        Cursor cursor = db.getReadableDatabase().rawQuery("SELECT * FROM " + db.KNOTEN_TABLE_NAME + " WHERE " + db.KNOTEN_COLUMN_BESCHREIBUNG + " LIKE '%"+text+"%' AND "+db.KNOTEN_COLUMN_BESCHREIBUNG + "!=''", null);
        cursor.moveToFirst();
        if(cursor.getCount()==0)
            return new Knoten[0];
        Knoten[] knoten = new Knoten[cursor.getCount()];
        int i=0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            knoten[i] = new Knoten(cursor.getInt(cursor.getColumnIndex(db.KNOTEN_COLUMN_ID)),cursor.getInt(cursor.getColumnIndex(db.KNOTEN_COLUMN_X)),cursor.getInt(cursor.getColumnIndex(db.KNOTEN_COLUMN_Y)),cursor.getInt(cursor.getColumnIndex(db.KNOTEN_COLUMN_EBENE)), cursor.getString(cursor.getColumnIndex(db.KNOTEN_COLUMN_BESCHREIBUNG)) );
            i++;
            cursor.moveToNext();
        }
      return knoten;
    }
}
