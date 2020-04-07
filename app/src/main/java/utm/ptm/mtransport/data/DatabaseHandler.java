package utm.ptm.mtransport.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import utm.ptm.mtransport.data.models.Route;

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
        db.execSQL(DatabaseContract.Route.CREATE_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "update");
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Route.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public void insert(Route[] routes) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (Route route : routes) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseContract.Route._RID, route.getId());
            contentValues.put(DatabaseContract.Route._NAME, route.getName());
            db.insert(DatabaseContract.Route.TABLE_NAME, null, contentValues);
        }
    }


    public long insert(Route route) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.Route._RID, route.getId());
        contentValues.put(DatabaseContract.Route._NAME, route.getName());

        return db.insert(DatabaseContract.Route.TABLE_NAME, null, contentValues);
    }

    public List<Route> getRoutes() {
        List<Route> routes = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DatabaseContract.Route.TABLE_NAME,
                new String[]{DatabaseContract.Route._RID, DatabaseContract.Route._NAME},
                null, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                Route route = new Route();
                route.setName(cursor.getString(cursor.getColumnIndex(DatabaseContract.Route._NAME)));
                route.setId(cursor.getString(cursor.getColumnIndex(DatabaseContract.Route._RID)));
                routes.add(route);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return routes;
    }

    public List<String> getRouteIds() {
        List<String> routeIds = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DatabaseContract.Route.TABLE_NAME,
                new String[]{DatabaseContract.Route._RID},
                null, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                routeIds.add(cursor.getString(cursor.getColumnIndex(DatabaseContract.Route._RID)));
                cursor.moveToNext();
            }
            cursor.close();
        }

        return routeIds;
    }


    public Route getRoute(String routeId) {
        Route route = null;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DatabaseContract.Route.TABLE_NAME,
                new String[]{DatabaseContract.Route._RID, DatabaseContract.Route._NAME},
                DatabaseContract.Route._RID + "=?",
                new String[]{ routeId }, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();

            route = new Route();
            route.setName(cursor.getString(cursor.getColumnIndex(DatabaseContract.Route._NAME)));
            route.setId(cursor.getString(cursor.getColumnIndex(DatabaseContract.Route._RID)));

            cursor.close();
        }

        return route;
    }
}
