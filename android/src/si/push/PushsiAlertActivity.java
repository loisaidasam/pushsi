
package si.push;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class PushsiAlertActivity extends Activity {
	
	protected static final String TAG = "Pushsi_PushsiAlertActivity";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Log.d(TAG, "onCreate()");

//    	if (savedInstanceState == null) {
//    		Log.e(TAG, "savedInstanceState is null! Sending user back to PushsiActivity");
//    		Intent intent = new Intent(this, PushsiActivity.class);
//    		startActivity(intent);
//    		return;
//    	}
    	
    	Bundle extras = getIntent().getExtras();
    	
    	long alertId = extras.getLong(Core.KEY_ALERT_ID, 0);
    	if (alertId == 0) {
    		Log.e(TAG, "alertId=0 - sending user back to PushsiActivity");
    		Intent intent = new Intent(this, PushsiActivity.class);
    		startActivity(intent);
    		return;
    	}

    	setContentView(R.layout.alert);
    	
    	PushsiDBHelper mDBHelper = new PushsiDBHelper(this);
    	List<KeyValuePair> keyValuePairs = mDBHelper.getAlertData(alertId);
    	
    	String tvData = "Alert Data:\n\n";
		for (KeyValuePair keyValuePair : keyValuePairs) {
			tvData += keyValuePair.key + ": " + keyValuePair.value + "\n";
		}
 
    	TextView tv = (TextView) findViewById(R.id.textview_alert);
    	tv.setText(tvData);
    }
}