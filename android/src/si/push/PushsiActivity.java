package si.push;

import java.util.Date;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PushsiActivity extends ListActivity {
	
	protected static final String TAG = "Pushsi_PushsiActivity";
	
	protected SharedPreferences settings;
	protected String uuid;
	protected String registrationId;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Log.d(TAG, "onCreate()");

    	// Persistent settings
    	settings = PreferenceManager.getDefaultSharedPreferences(this);

    	// Grab the user's UUID and if not set, set it
    	uuid = settings.getString(Core.KEY_UUID, null);
    	if (uuid == null) {
    		try {
				uuid = Core.getUUID(this);
			} catch (CoreException e) {
				e.printStackTrace();
				Log.e(TAG, "CoreException: " + e);
				Toast.makeText(this, getString(R.string.error_uuid), Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
    		
    		try {
				Core.registerPhone(this, uuid);
			} catch (CoreException e) {
				e.printStackTrace();
				Log.e(TAG, "CoreException: " + e);
				Toast.makeText(this, getString(R.string.error_uuid), Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putString(Core.KEY_UUID, uuid);
            editor.commit();
    	}
    	
    	// Make sure the user is registered w/ C2DM, if not register them
    	registrationId = settings.getString(Core.KEY_REGISTRATION_ID, null);
    	if (registrationId == null) {
    		Log.d(TAG, "No registrationId set...");
    		Core.registerC2DM(this);
    	}
    	
    	long dbLastUpdated = settings.getLong(Core.KEY_DB_LAST_UPDATED, 0L);
    	if (dbLastUpdated == 0) {
    		Log.d(TAG, "dbLastUpdated=" + dbLastUpdated + " - calling onCreate()");
    		PushsiDBHelper mDBHelper = new PushsiDBHelper(this);
    		mDBHelper.onUpgrade(1, 1);
    		//mDBHelper.onCreate();
    		
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putLong(Core.KEY_DB_LAST_UPDATED, System.currentTimeMillis());
            editor.commit();
    	}
    	
        //setContentView(R.layout.main);
    	PushsiDBHelper mDBHelper = new PushsiDBHelper(this);
    	String[] fromColumns = {PushsiContract.Alert.COLUMN_NAME_CREATED};
		int[] toViews = {R.id.textview_alert_item};
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
    			R.layout.alert_item,
    			mDBHelper.getAlertCursor(),
    			fromColumns,
    			toViews
    			//TODO: FLAG_AUTO_REQUERY?
    	);
    	
    	adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
    	    @Override
    	    public boolean setViewValue(View view, Cursor cursor, int column) {
    	        if (column == 1){
    	            TextView tv = (TextView) view;
    	            long columnCreated = cursor.getLong(cursor.getColumnIndex(PushsiContract.Alert.COLUMN_NAME_CREATED));
    	            Date date = new Date(columnCreated);
    	            
    	            // TODO: format this using DateFormat
    	            // http://developer.android.com/reference/java/text/DateFormat.html
    	            tv.setText(date.toString());
    	            return true;
    	        }
    	        return false;
    	    }
    	});
    	
    	ListView listView = getListView();
    	listView.setAdapter(adapter);
        
        // Create a message handling object as an anonymous class.
        OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Log.d(TAG, "Clicked position=" + position + " id=" + id);
            	Intent intent = new Intent(PushsiActivity.this, PushsiAlertActivity.class);
            	intent.putExtra(Core.KEY_ALERT_ID, id);
            	startActivity(intent);
            }
        };
    	
        listView.setOnItemClickListener(mMessageClickedHandler); 
    }
    
    public void registerClick(View view) {
    	Log.d(TAG, "registerClick()");
    	
		Intent intent = new Intent(this, RegisterActivity.class);

		// Look for the registration id
		if (registrationId == null) {
			registrationId = settings.getString(Core.KEY_REGISTRATION_ID, null);
		}
    	
    	// If not found, fuck it
    	if (registrationId == null) {
    		Toast.makeText(this, getString(R.string.error_c2dm), Toast.LENGTH_SHORT).show();
    		finish();
    		return;
    	}
    	
    	// Otherwise, let's get this party started!
		startActivity(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_preferences:
	            Intent intent = new Intent(this, RegisterActivity.class);
	            startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}