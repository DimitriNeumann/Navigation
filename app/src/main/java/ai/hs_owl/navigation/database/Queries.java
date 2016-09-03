package ai.hs_owl.navigation.database;

        import android.content.Context;
        import android.database.Cursor;
        import android.graphics.PointF;
        import android.util.Log;

        import java.sql.PreparedStatement;

        import ai.hs_owl.navigation.datastructures.Knoten;
        import ai.hs_owl.navigation.datastructures.Verbindung;

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
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM " + Database.BEACONS_TABLE_NAME + " WHERE " + Database.BEACONS_COLUMN_ID + "='" + id + "'", null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            float[] returnValue = new float[]{(Float.parseFloat(c.getInt(c.getColumnIndex(Database.BEACONS_COLUMN_X)) + "")),
                    Float.parseFloat(c.getInt(c.getColumnIndex(Database.BEACONS_COLUMN_Y)) + ""),
                    Float.parseFloat(c.getInt(c.getColumnIndex(Database.BEACONS_COLUMN_EBENE)) + "")};
            return returnValue;
        }
        return new float[]{0, 0, 0};
    }
    public boolean hasBeacons()
    {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM " + Database.BEACONS_TABLE_NAME, null);
        return (c.getCount()>0);

    }
    public int getNearestKnot(PointF loca)
    {
        int id_smallest=-1;
        double distance=Double.MAX_VALUE;
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM " + Database.BEACONS_TABLE_NAME, null);
        c.moveToFirst();
        while(!c.isAfterLast())
        {
            if((Math.abs(loca.x-c.getInt(c.getColumnIndex(Database.KNOTEN_COLUMN_X)))+Math.abs(loca.x-c.getInt(c.getColumnIndex(Database.KNOTEN_COLUMN_X))))<distance)
            {
                id_smallest = c.getInt(c.getColumnIndex(Database.KNOTEN_COLUMN_ID));
                distance = (Math.abs(loca.x-c.getInt(c.getColumnIndex(Database.KNOTEN_COLUMN_X)))+Math.abs(loca.x-c.getInt(c.getColumnIndex(Database.KNOTEN_COLUMN_X))));
            }
            c.moveToNext();
        }
        return id_smallest;
    }

    //Schreiben
    public void insertNewBeacon(String id, float x, float y, int ebene) {
        Log.i("Query", "INSERT INTO " + Database.BEACONS_TABLE_NAME + "(" + Database.BEACONS_COLUMN_ID + ", " + Database.BEACONS_COLUMN_X + ", " + Database.BEACONS_COLUMN_Y + ", " + Database.BEACONS_COLUMN_EBENE + ") VALUES('" + id + "', " + x + ", " + y + ", " + ebene + ")");
        db.getWritableDatabase().execSQL("INSERT INTO " + Database.BEACONS_TABLE_NAME + "(" + Database.BEACONS_COLUMN_ID + ", " + Database.BEACONS_COLUMN_X + ", " + Database.BEACONS_COLUMN_Y + ", " + Database.BEACONS_COLUMN_EBENE + ") VALUES('" + id + "', " + x + ", " + y + ", " + ebene + ")");
    }
    public void insertNewKnot(String id, float x, float y, int ebene,int fav, String name) {
        db.getWritableDatabase().execSQL("INSERT INTO " + Database.KNOTEN_TABLE_NAME + "(" + Database.KNOTEN_COLUMN_ID + ", " + Database.KNOTEN_COLUMN_X + ", " + Database.KNOTEN_COLUMN_Y + ", " + Database.KNOTEN_COLUMN_EBENE + ", "+ Database.KNOTEN_COLUMN_BESCHREIBUNG +", "+Database.KNOTEN_COLUMN_FAV+") VALUES('" + id + "', " + x + ", " + y + ", " + ebene + ",'"+name+"', "+fav+")");
    }
    public void insertNewConnection(int idA, int idB, double gewicht) {
        gewicht *=10;
        db.getWritableDatabase().execSQL("INSERT INTO " + Database.VERBINDUNGEN_TABLE_NAME + "(" + Database.VERBINDUNGEN_COLUMN_IDA + ", " + Database.VERBINDUNGEN_COLUMN_IDB + ", " + Database.VERBINDUNGEN_COLUMN_GEWICHT + ", " + Database.VERBINDUNGEN_COLUMN_OUTDOOR + ") VALUES(" + idA + ", " + idB + ", " + (int) gewicht+ ", 0)");
    }


    public void clearTable(String beaconsTableName) {
        db.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + beaconsTableName);
        if(Database.BEACONS_TABLE_CREATE.contains(beaconsTableName))
            db.getWritableDatabase().execSQL(Database.BEACONS_TABLE_CREATE);
        if(Database.KNOTEN_TABLE_CREATE.contains(beaconsTableName))
            db.getWritableDatabase().execSQL(Database.KNOTEN_TABLE_CREATE);
        if(Database.VERBINDUNGEN_TABLE_CREATE.contains(beaconsTableName))
            db.getWritableDatabase().execSQL(Database.VERBINDUNGEN_TABLE_CREATE);
    }

    public boolean hasBeacon(String id) {
        return (db.getReadableDatabase().rawQuery("SELECT * FROM " + Database.BEACONS_TABLE_NAME + " WHERE " + Database.BEACONS_COLUMN_ID + "='" + id + "'", null).getCount() > 0);
    }

    public Knoten[] searchKnots(String text) {
        Cursor cursor = db.getReadableDatabase().rawQuery("SELECT * FROM " + Database.KNOTEN_TABLE_NAME + " WHERE " + Database.KNOTEN_COLUMN_BESCHREIBUNG + " LIKE '%"+text+"%' AND "+ Database.KNOTEN_COLUMN_BESCHREIBUNG + "!=''", null);
        cursor.moveToFirst();
        if(cursor.getCount()==0)
            return new Knoten[0];
        Knoten[] knoten = new Knoten[cursor.getCount()];
        int i=0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            knoten[i] = new Knoten(cursor.getInt(cursor.getColumnIndex(Database.KNOTEN_COLUMN_ID)),cursor.getInt(cursor.getColumnIndex(Database.KNOTEN_COLUMN_X)),cursor.getInt(cursor.getColumnIndex(Database.KNOTEN_COLUMN_Y)),cursor.getInt(cursor.getColumnIndex(Database.KNOTEN_COLUMN_EBENE)), cursor.getString(cursor.getColumnIndex(Database.KNOTEN_COLUMN_BESCHREIBUNG)) );
            i++;
            cursor.moveToNext();
        }
        return knoten;
    }
    public Verbindung[] getVerbindungen()
    {
        Cursor cursor = db.getReadableDatabase().rawQuery("SELECT * FROM " + Database.VERBINDUNGEN_TABLE_NAME, null);
        cursor.moveToFirst();
        if(cursor.getCount()==0)
            return new Verbindung[0];
        Verbindung[] verbindung = new Verbindung[cursor.getCount()];
        int i=0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            verbindung[i] = new Verbindung(cursor.getInt(cursor.getColumnIndex(Database.VERBINDUNGEN_COLUMN_ID)),cursor.getInt(cursor.getColumnIndex(Database.VERBINDUNGEN_COLUMN_IDA)),cursor.getInt(cursor.getColumnIndex(Database.VERBINDUNGEN_COLUMN_IDB)),cursor.getInt(cursor.getColumnIndex(Database.VERBINDUNGEN_COLUMN_GEWICHT)) );
            i++;
            cursor.moveToNext();
        }
        return verbindung;
    }
    public PointF searchNode(String id) {
        Cursor cursor = db.getReadableDatabase().rawQuery("SELECT * FROM " + Database.KNOTEN_TABLE_NAME + " WHERE " + Database.KNOTEN_COLUMN_ID + "='" + id + "'", null);
        if(cursor.moveToFirst()) {
            return new PointF(cursor.getFloat(cursor.getColumnIndex(Database.KNOTEN_COLUMN_X)), cursor.getFloat(cursor.getColumnIndex(Database.KNOTEN_COLUMN_Y)));
        }
        return new PointF(0.0f,0.0f);
    }
}
