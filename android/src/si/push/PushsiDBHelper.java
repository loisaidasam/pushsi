
package si.push;

import java.util.ArrayList;
import java.util.List;

import si.push.PushsiContract.Alert;
import si.push.PushsiContract.AlertData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PushsiDBHelper extends SQLiteOpenHelper {
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";

	private static final String SQL_CREATE_TABLE_ALERT =
	    "CREATE TABLE " + Alert.TABLE_NAME + " (" +
	    Alert._ID + " INTEGER PRIMARY KEY," +
		Alert.COLUMN_NAME_CREATED +  INTEGER_TYPE +
	    " )";
	private static final String SQL_CREATE_TABLE_ALERT_DATA =
	    "CREATE TABLE " + AlertData.TABLE_NAME + " (" +
	    AlertData._ID + " INTEGER PRIMARY KEY," +
		AlertData.COLUMN_NAME_ALERT_ID + INTEGER_TYPE + COMMA_SEP +
		AlertData.COLUMN_NAME_KEY + TEXT_TYPE + COMMA_SEP +
		AlertData.COLUMN_NAME_VALUE + TEXT_TYPE +
	    " )";

	private static final String SQL_DROP_TABLE_ALERT =
	    "DROP TABLE IF EXISTS " + Alert.TABLE_NAME;
	private static final String SQL_DROP_TABLE_ALERT_DATA =
		    "DROP TABLE IF EXISTS " + AlertData.TABLE_NAME;
	
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Pushsi.db";

    public PushsiDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public void onCreate() {
    	SQLiteDatabase db = getWritableDatabase();
    	onCreate(db);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_ALERT);
        db.execSQL(SQL_CREATE_TABLE_ALERT_DATA);
    }
    
    public void onUpgrade(int oldVersion, int newVersion) {
    	SQLiteDatabase db = getWritableDatabase();
    	onUpgrade(db, oldVersion, newVersion);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DROP_TABLE_ALERT);
        db.execSQL(SQL_DROP_TABLE_ALERT_DATA);
        onCreate(db);
    }
    
//	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        onUpgrade(db, oldVersion, newVersion);
//    }
    
    public long createAlert() {
    	// Gets the data repository in write mode
    	SQLiteDatabase db = getWritableDatabase();

    	// Create a new map of values, where column names are the keys
    	ContentValues values = new ContentValues();
    	values.put(Alert.COLUMN_NAME_CREATED, System.currentTimeMillis());

    	// Insert the new row, returning the primary key value of the new row
    	long newRowId;
    	newRowId = db.insert(Alert.TABLE_NAME, null, values);
    	return newRowId;
    }
    
    public void addAlertData(long alertId, List<KeyValuePair> keyValuePairs) {
    	SQLiteDatabase db = getWritableDatabase();
    	for (KeyValuePair keyValuePair : keyValuePairs) {
    		ContentValues values = new ContentValues();
    		values.put(AlertData.COLUMN_NAME_ALERT_ID, alertId);
    		values.put(AlertData.COLUMN_NAME_KEY, keyValuePair.key);
    		values.put(AlertData.COLUMN_NAME_VALUE, keyValuePair.value);
    		db.insert(AlertData.TABLE_NAME, null, values);
    	}
    }
    
    public Cursor getAlertCursor() {
    	SQLiteDatabase db = getReadableDatabase();

    	// Define a projection that specifies which columns from the database
    	// you will actually use after this query.
    	String[] projection = {
    	    Alert._ID,
			Alert.COLUMN_NAME_CREATED,
	    };

    	// How you want the results sorted in the resulting Cursor
    	String sortOrder = Alert.COLUMN_NAME_CREATED + " DESC";

    	Cursor cursor = db.query(
    	    Alert.TABLE_NAME,	  					  // The table to query
    	    projection,                               // The columns to return
    	    null,           	                      // The columns for the WHERE clause
    	    null, 			                          // The values for the WHERE clause
    	    null,                                     // don't group the rows
    	    null,                                     // don't filter by row groups
    	    sortOrder                                 // The sort order
	    );
    	
    	return cursor;
    }
    
//    public long getAlertIdBasedOnOffset(int offset) {
//    	SQLiteDatabase db = getReadableDatabase();
//    	String query = "SELECT " + Alert._ID + " FROM " + Alert.TABLE_NAME + " ORDER BY " + Alert.COLUMN_NAME_CREATED + " DESC LIMIT 0, ?";
//    	String[] args = {Integer.toString(offset)};
//    	Cursor cursor = db.rawQuery(query, args);
//    	
//    	if (! cursor.moveToFirst()) {
//    		return 0;
//    	}
//    	
//    	return cursor.getLong(cursor.getColumnIndex(Alert._ID));
//    }
    
    public List<KeyValuePair> getAlertData(long alertId) {
    	SQLiteDatabase db = getReadableDatabase();

    	// Define a projection that specifies which columns from the database
    	// you will actually use after this query.
    	String[] projection = {
    	    AlertData._ID,
			AlertData.COLUMN_NAME_KEY,
			AlertData.COLUMN_NAME_VALUE,
	    };
    	
    	String selection = AlertData.COLUMN_NAME_ALERT_ID + " = ?";
    	String[] selectionArgs = {
    		Long.toString(alertId),
    	};

    	// How you want the results sorted in the resulting Cursor
    	String sortOrder = AlertData.COLUMN_NAME_KEY + " ASC";

    	Cursor cursor = db.query(
    	    AlertData.TABLE_NAME,  					  // The table to query
    	    projection,                               // The columns to return
    	    selection,                                // The columns for the WHERE clause
    	    selectionArgs,                            // The values for the WHERE clause
    	    null,                                     // don't group the rows
    	    null,                                     // don't filter by row groups
    	    sortOrder                                 // The sort order
	    );
    	
    	List<KeyValuePair> keyValuePairs = new ArrayList<KeyValuePair>();
    	
    	if (cursor.moveToFirst()) {
    		do {
    			String key = cursor.getString(cursor.getColumnIndex(AlertData.COLUMN_NAME_KEY));
    			String value = cursor.getString(cursor.getColumnIndex(AlertData.COLUMN_NAME_VALUE));
    			keyValuePairs.add(new KeyValuePair(key, value));
			} while(cursor.moveToNext());
		}
		cursor.close();
		
		return keyValuePairs;
    }
}