package si.push;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class PushsiActivity extends Activity {
	
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
    	
        setContentView(R.layout.main);
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
}