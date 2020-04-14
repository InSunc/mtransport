package utm.ptm.mtransport.data;

import android.provider.BaseColumns;

public class DatabaseContract {
    private DatabaseContract() {};

    public interface Route extends BaseColumns {
        public static final String TABLE_NAME = "routes";
        public static final String _ID = "id";
        public static final String _RID = "rid";
        public static final String _NAME = "name";
        public static final String _PRICE = "price";


        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + _RID + " TEXT NOT NULL UNIQUE, "
                + _NAME + " TEXT, "
                + _PRICE + " REAL "
                + ")";
    }

    public interface Stop extends BaseColumns {
        public static final String TABLE_NAME = "routes";
        public static final String _ID = "id";
        public static final String _NAME = "name";
        public static final String _LAT = "lat";
        public static final String _LON = "lon";


        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + _ID + " INTEGER PRIMARY KEY ,"
                + _NAME + " TEXT, "
                + _LAT + " REAL "
                + _LON + " REAL "
                + ")";
    }

}
