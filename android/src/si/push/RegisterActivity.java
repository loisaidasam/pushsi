package si.push;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	
	protected static final String TAG = "Pushsi_RegisterActivity";
	
	protected PinChecker pc;

	final Handler handler = new Handler();

    final Runnable updateResultsRunnable = new Runnable() { 
        public void run() {
        	// Remove any other callbacks from the handler
        	handler.removeCallbacks(this);
        	
        	// Kill the thread if it's not already dead
        	pc.shouldContinue = false;
        	
        	// Update the UI and finish the activity
        	registrationExpiredUI(pc.resolved);
        }
    };
	
	class PinChecker extends Thread {
		public boolean shouldContinue;
		public boolean resolved;
		protected Context context;
		protected String uuid;
		protected String pinCode;
		
		public PinChecker(Context context, String uuid, String pinCode) {
			shouldContinue = true;
			resolved = false;
			this.context = context;
			this.uuid = uuid;
			this.pinCode = pinCode;
		}
		
		public void run() {
			while (shouldContinue) {
				// Continually check registration status
				
				try {
					resolved = Core.checkRegistrationStatus(context, uuid, pinCode);
				} catch (CoreException e) {
					Log.e(TAG, "CoreException calling Core.checkRegistrationStatus(): " + e.getMessage());
					e.printStackTrace();
					break;
				}
				if (resolved) {
					break;
				}
			}
			handler.post(updateResultsRunnable);
		}
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Log.d(TAG, "onCreate()");
    	setContentView(R.layout.register);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	Log.d(TAG, "onResume()");

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String uuid = settings.getString(Core.KEY_UUID, null);
		if (uuid == null) {
	    	Log.e(TAG, "No uuid set");
			Toast.makeText(this, getString(R.string.registration_failure_general), Toast.LENGTH_SHORT).show();
	    	finish();
	    	return;
		}
    	
    	String pinCode;
		try {
			pinCode = Core.getRegistrationPin(this, uuid);
		} catch (CoreException e) {
	    	Log.e(TAG, "CoreException calling Core.getRegistrationPin(): " + e.getMessage());
			e.printStackTrace();
			Toast.makeText(this, getString(R.string.registration_failure_general), Toast.LENGTH_SHORT).show();
	    	finish();
	    	return;
		}
    	
    	TextView tv = (TextView) findViewById(R.id.textview_pin);
    	tv.setText(pinCode);
    	
    	pc = new PinChecker(this, uuid, pinCode);
    	pc.start();
    	
    	int pinValiditySeconds = Integer.parseInt(getString(R.string.pin_validity_seconds));
    	handler.postDelayed(updateResultsRunnable, pinValiditySeconds * 1000); 
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	Log.d(TAG, "onPause()");
    	
    	if (pc != null) {
	    	pc.shouldContinue = false;
	    	handler.removeCallbacks(updateResultsRunnable);
    	}
    }
    
    protected void registrationExpiredUI(boolean resolved) {
    	Log.d(TAG, "registrationExpiredUI()");
    	String msg = resolved ? getString(R.string.registration_success) : getString(R.string.registration_failure_expired);
    	Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    	finish();
    }
}