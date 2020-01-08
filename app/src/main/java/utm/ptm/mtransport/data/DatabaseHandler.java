package utm.ptm.mtransport.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.google.android.gms.common.data.DataBuffer;
import com.google.android.gms.maps.model.LatLng;

import utm.ptm.mtransport.data.models.Stop;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static DatabaseHandler instance;

    public static final String TAG = DatabaseHandler.class.getSimpleName();

    public static final String DATABSE_NAME = "mtransport_db";
    public static final int DATABASE_VERSION = 1;

    public static synchronized DatabaseHandler getInstance(Context context) {
        if (instance == null) {
            if (context != null) {
                instance = new DatabaseHandler(context);
            }
        }

        return instance;
    }


    private DatabaseHandler(Context context) {
        super(context, DATABSE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "create");
        db.execSQL(DatabaseContract.Stop.CREATE_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "update");
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Stop.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }


    public long insert(Stop stop) {
        long StopId = -1;

        int lat = (int) (stop.getLat() * DatabaseContract.RESOLUTION);
        int lng = (int) (stop.getLng() * DatabaseContract.RESOLUTION);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.Stop._NAME, stop.getName());
        contentValues.put(DatabaseContract.Stop._LAT, lat);
        contentValues.put(DatabaseContract.Stop._LNG, lng);

        StopId = db.insert(DatabaseContract.Stop.TABLE_NAME, null, contentValues);

        return StopId;
    }

    public Stop getStop(Long id) {
        Stop stop = null;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DatabaseContract.Stop.TABLE_NAME,
                new String[]{DatabaseContract.Stop._NAME, DatabaseContract.Stop._LAT, DatabaseContract.Stop._LNG},
                DatabaseContract.Stop._ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();

            stop = new Stop();
            stop.setName(cursor.getString(cursor.getColumnIndex(DatabaseContract.Stop._NAME)));
            double lat = (double) (cursor.getInt(cursor.getColumnIndex(DatabaseContract.Stop._LAT)));
            double lng = (double) (cursor.getInt(cursor.getColumnIndex(DatabaseContract.Stop._LNG)));
            stop.setLat(lat);
            stop.setLng(lng);
            stop.setId(id);

            cursor.close();
        }

        return stop;
    }

    public Stop getStop(LatLng coords) {
        Stop stop = null;

        int lat = (int) (coords.latitude * DatabaseContract.RESOLUTION);
        int lng = (int) (coords.longitude * DatabaseContract.RESOLUTION);

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(DatabaseContract.Stop.TABLE_NAME,
                null,
                DatabaseContract.Stop._LAT + "=? AND " + DatabaseContract.Stop._LNG + "=?",
                new String[]{String.valueOf(lat), String.valueOf(lng)},
                null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();

            stop = new Stop();
            stop.setId(cursor.getInt(cursor.getColumnIndex(DatabaseContract.Stop._ID)));
            stop.setName(cursor.getString(cursor.getColumnIndex(DatabaseContract.Stop._NAME)));
            stop.setLat(coords.latitude);
            stop.setLng(coords.longitude);

            cursor.close();
        }

        return stop;
    }
}
