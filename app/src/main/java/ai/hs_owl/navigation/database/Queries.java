package ai.hs_owl.navigation.database;

        import android.content.Context;
        import android.database.Cursor;
        import android.graphics.PointF;
        import android.util.Log;

        import java.sql.PreparedStatement;

        import ai.hs_owl.navigation.datastructures.Knoten;
        import ai.hs_owl.navigation.datastructures.Verbindung;

/**
 * Dies ist eine Hilfsklasse, welche Methoden für den Datenbankzugriff stellt
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
    /**
     * @return float[] Der Punkt, an dem sich der Beacon befindet
     * @param id Die ID des gesuchten Beacons
     * Überprüft in der Datenbank, wo sich der Beacon auf dem Koordinaten System befindet.
     * */
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
    /**
     * @return boolean true wenn Beacons gespeichert sind, sonst false
     * Überprüft in der Datenbank, ob Beacons hinterlegt sind
     * */
    public boolean hasBeacons()
    {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM " + Database.BEACONS_TABLE_NAME, null);
        return (c.getCount()>0);

    }
    /**
     * @return int Die ID des Knotens, welcher am nächsten ist
     * @param loca Der Ausgangspunkt, von dem aus gesucht wird
     * Sucht den Knoten aus der Datenbank, welcher den geringsten Abstand zum Punkt hat
     * */
    public int getNearestKnot(PointF loca)
    {
        int id_smallest=-1;
        double distance=Double.MAX_VALUE;
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM " + Database.KNOTEN_TABLE_NAME, null);
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
    /**
     * @return boolean true wenn der Beacons gespeichert ist, sonst false
     * @param id Die ID des gesuchten Beacons
     * Überprüft, ob der Beacon in der Datenbank hinterlegt ist
     * */
    public boolean hasBeacon(String id) {
        return (db.getReadableDatabase().rawQuery("SELECT * FROM " + Database.BEACONS_TABLE_NAME + " WHERE " + Database.BEACONS_COLUMN_ID + "='" + id + "'", null).getCount() > 0);
    }
    /**
     * @return Knoten[] die Favoriten, welche in der Datenbank als solche hinterlegt sind
     * Sucht die Favoriten aus der Datenbank raus und gibt diese wieder
     * */
    public Knoten[] getFavorites()
    {
        Cursor cursor = db.getReadableDatabase().rawQuery("SELECT * FROM " + Database.KNOTEN_TABLE_NAME + " WHERE " + Database.KNOTEN_COLUMN_FAV +"=1", null);
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
    /**
     * @return Knoten[] die Knoten, welche auf den Suchbegriff passen
     * @param text Der Suchbegriff
     * Sucht die Knoten aus der Datenbank aus, welche auf den Suchbegriff passen
     * */
    public Knoten[] searchKnots(String text) {
        Cursor cursor = db.getReadableDatabase().rawQuery("SELECT * FROM " + Database.KNOTEN_TABLE_NAME + " WHERE " + Database.KNOTEN_COLUMN_BESCHREIBUNG + " LIKE '%"+text+"%' AND "+ Database.KNOTEN_COLUMN_BESCHREIBUNG + "!='' OR " + Database.KNOTEN_COLUMN_FAV +"=1 ORDER BY "+ Database.KNOTEN_COLUMN_FAV +" ASC", null);
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
    /**
     * @return Verbindung[] die Verbindungen
     * Gibt alle Verbindunden aus der Datenbank wieder
     * */
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
    /**
     * @return PointF die Position des Knotens
     * @param id Die ID des Knotens
     * Sucht einen Knoten anhand seiner ID raus und gibt dessen Position zurück.
     * */
    public PointF searchNode(String id) {
        Cursor cursor = db.getReadableDatabase().rawQuery("SELECT * FROM " + Database.KNOTEN_TABLE_NAME + " WHERE " + Database.KNOTEN_COLUMN_ID + "='" + id + "'", null);
        if(cursor.moveToFirst()) {
            return new PointF(cursor.getFloat(cursor.getColumnIndex(Database.KNOTEN_COLUMN_X)), cursor.getFloat(cursor.getColumnIndex(Database.KNOTEN_COLUMN_Y)));
        }
        return new PointF(0.0f,0.0f);
    }
    //Schreiben
    /**
     * @param id Die ID des Beacons
     * @param x Die X Koordinate des Beacons
     * @param y Die Y Koordinate des Beacons
     * @param ebene Die Ebene des Beacons
     * Speichert einen Beacon mit den gewählten Parametern ab.
     * */
    public void insertNewBeacon(String id, float x, float y, int ebene) {
        db.getWritableDatabase().execSQL("INSERT INTO " + Database.BEACONS_TABLE_NAME + "(" + Database.BEACONS_COLUMN_ID + ", " + Database.BEACONS_COLUMN_X + ", " + Database.BEACONS_COLUMN_Y + ", " + Database.BEACONS_COLUMN_EBENE + ") VALUES('" + id + "', " + x + ", " + y + ", " + ebene + ")");
    }
    /**
     * @param id Die ID des Knotens
     * @param x Die X Koordinate des Knotens
     * @param y Die Y Koordinate des Knotens
     * @param ebene Die Ebene des Knotens
     * @param fav 1 Wenn der Knoten direkt in der Suchleiste angezeigt werden soll, sonst 0
     * @param name Der Name des Knotens, dieser ist Optional und nur für suchbare Knoten von Bedeutung
     * Speichert einen Knoten mit den gewählten Parametern ab.
     * */
    public void insertNewKnot(String id, float x, float y, int ebene,int fav, String name) {
        db.getWritableDatabase().execSQL("INSERT INTO " + Database.KNOTEN_TABLE_NAME + "(" + Database.KNOTEN_COLUMN_ID + ", " + Database.KNOTEN_COLUMN_X + ", " + Database.KNOTEN_COLUMN_Y + ", " + Database.KNOTEN_COLUMN_EBENE + ", "+ Database.KNOTEN_COLUMN_BESCHREIBUNG +", "+Database.KNOTEN_COLUMN_FAV+") VALUES('" + id + "', " + x + ", " + y + ", " + ebene + ",'"+name+"', "+fav+")");
    }
    /**
     * @param idA Die ID des ersten Knotens
     * @param idB Die ID des zweiten Knotens
     * @param gewicht Das Gewicht des Weges
     * Speichert eine Verbindung mit den gewählten Parametern ab.
     * */
    public void insertNewConnection(int idA, int idB, double gewicht) {
        gewicht *=10;
        db.getWritableDatabase().execSQL("INSERT INTO " + Database.VERBINDUNGEN_TABLE_NAME + "(" + Database.VERBINDUNGEN_COLUMN_IDA + ", " + Database.VERBINDUNGEN_COLUMN_IDB + ", " + Database.VERBINDUNGEN_COLUMN_GEWICHT + ", " + Database.VERBINDUNGEN_COLUMN_OUTDOOR + ") VALUES(" + idA + ", " + idB + ", " + (int) gewicht+ ", 0)");
    }

    /**
     * @param beaconsTableName Die Tabelle, welche geleert werden soll
     * Löscht eine Tabelle und erstellt diese danach neu
     * */
    public void clearTable(String beaconsTableName) {
        db.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + beaconsTableName);
        if(Database.BEACONS_TABLE_CREATE.contains(beaconsTableName))
            db.getWritableDatabase().execSQL(Database.BEACONS_TABLE_CREATE);
        if(Database.KNOTEN_TABLE_CREATE.contains(beaconsTableName))
            db.getWritableDatabase().execSQL(Database.KNOTEN_TABLE_CREATE);
        if(Database.VERBINDUNGEN_TABLE_CREATE.contains(beaconsTableName))
            db.getWritableDatabase().execSQL(Database.VERBINDUNGEN_TABLE_CREATE);
    }


}
