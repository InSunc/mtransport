package utm.ptm.mtransport.data;

import android.provider.BaseColumns;

public class DatabaseContract {
    private DatabaseContract() {};

    // Used to convert coordinate values from physical to logical
    public static final int RESOLUTION = (int) 1e7;

    public interface Stop extends BaseColumns {
        public static final String TABLE_NAME = "stops";
        public static final String _NAME = "name";
        public static final String _ROUTE_NODE = "routeNode";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + _ID + " INTEGER PRIMARY KEY "
                + _NAME + " TEXT, "
                + _ROUTE_NODE + " INTEGER "
                + "FOREIGN KEY (" + _ROUTE_NODE + ") REFERENCES " + Node.TABLE_NAME
                    + "(" + Node._ID + ")"
                + "FOREIGN KEY (" + _ID + ") REFERENCES " + Node.TABLE_NAME
                    + "(" + Node._ID + ")"
                + ")";

    }

    public interface Node extends BaseColumns {
        public static final String TABLE_NAME = "nodes";
        public static final String _LAT = "lat";
        public static final String _LNG = "lng";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + _LAT + " INTEGER, "
                + _LNG + " INTEGER "
                + ")";
    }

}
