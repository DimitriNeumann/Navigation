package ai.hs_owl.navigation.database;

import android.content.Context;
import android.database.Cursor;
import android.graphics.PointF;
import android.util.Log;

/**
 * Created by mberg on 10.05.2016.
 */
public class Queries {
    //Singleton Implementierung
    static Queries q;
    public static Queries getInstance(Context c)
    {
        if(q==null)
            q = new Queries(c);
        return q;
    }

    //Abfragen an die DB bzw. Schreiboperationen
    Context context;
    Database db;
    private Queries(Context c)
    {
        this.context = c;
        db = new Database(c);
    }
    //Lesen
    public float[] getPositionOfBeacon(String id)
    {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM "+db.BEACONS_TABLE_NAME+" WHERE "+db.BEACONS_COLUMN_ID+"='"+id+"'", null);
        c.moveToFirst();
        if(c.getCount()>0)
        {
            float[] returnValue = new float[]{(Float.parseFloat(c.getInt(c.getColumnIndex(db.BEACONS_COLUMN_X))+"")),Float.parseFloat(c.getInt(c.getColumnIndex(db.BEACONS_COLUMN_X))+""), Float.parseFloat(c.getInt(c.getColumnIndex(db.BEACONS_COLUMN_X))+"")};
            return returnValue;
        }
        return new float[]{0,0,0};
    }
    //Schreiben
    public void insertNewBeacon(String id, float x, float y, int ebene)
    {
        Log.i("Query", "INSERT INTO " + db.BEACONS_TABLE_NAME + "("+db.BEACONS_COLUMN_ID+", "+db.BEACONS_COLUMN_X+", "+db.BEACONS_COLUMN_Y+", "+db.BEACONS_COLUMN_EBENE+") VALUES('"+id+"', "+x+", "+y+", "+ebene+")");
        db.getWritableDatabase().execSQL("INSERT INTO " + db.BEACONS_TABLE_NAME + "("+db.BEACONS_COLUMN_ID+", "+db.BEACONS_COLUMN_X+", "+db.BEACONS_COLUMN_Y+", "+db.BEACONS_COLUMN_EBENE+") VALUES('"+id+"', "+x+", "+y+", "+ebene+")");
    }

    public void clearTable(String beaconsTableName) {
        db.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + beaconsTableName);
        db.getWritableDatabase().execSQL(Database.BEACONS_TABLE_CREATE);
    }
}
