package ai.hs_owl.navigation.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mberg on 10.05.2016.
 */
public class Database extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION =6;
    public static final String DATABASE_NAME = "navigation.db";

    //Beacons
    public static String BEACONS_TABLE_NAME ="beacons";
    public static String BEACONS_COLUMN_ID ="_id";
    public static String BEACONS_COLUMN_X ="X";
    public static String BEACONS_COLUMN_Y ="Y";
    public static String BEACONS_COLUMN_EBENE="ebene";
    public static String BEACONS_TABLE_CREATE ="create table "+BEACONS_TABLE_NAME+" ("+BEACONS_COLUMN_ID+" text not null, "+BEACONS_COLUMN_X+" REAL not null, "+BEACONS_COLUMN_Y+" REAL not null," + BEACONS_COLUMN_EBENE+" integer not null)";


    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(BEACONS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Löschen der alten Tabellen
        db.execSQL("DROP TABLE IF EXISTS " + BEACONS_TABLE_NAME);
        // Die Neuen erstellen
        onCreate(db);
    }
}
