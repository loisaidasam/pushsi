package si.push;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class C2DMReceiver extends BroadcastReceiver {
	
	protected static final String TAG = "Pushsi_C2DMReceiver";
	private static final int HELLO_ID = 1;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive()");
		
		if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
	        handleRegistration(context, intent);
	    } else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
	        handleMessage(context, intent);
	    }
	}

	private void handleRegistration(Context context, Intent intent) {
		Log.d(TAG, "handleRegistration()");
		
	    if (intent.getStringExtra("error") != null) {
	        // Registration failed, we should try again later.
		    String error = intent.getStringExtra("error");
	    	Log.e(TAG, "Registration failed! Error: " + error);
	    	Toast.makeText(context, context.getString(R.string.registration_failure_general), Toast.LENGTH_SHORT).show();
	    	Activity activity = (Activity) context;
	    	activity.finish();
	    	return;
	    }
	    
	    if (intent.getStringExtra("unregistered") != null) {
	        // Unregistration done, new messages from the authorized sender will be rejected
	    	Log.i(TAG, "Unregistered");
	    	return;
	    }

	    String registrationId = intent.getStringExtra("registration_id");
	    if (registrationId != null) {
	    	Log.i(TAG, "Registered! registration_id=" + registrationId);
	    	
	    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	    	String uuid = settings.getString(Core.KEY_UUID, null);
	    	try {
				Core.updateC2DM(context, uuid, registrationId);
			} catch (CoreException e) {
				e.printStackTrace();
		    	Log.e(TAG, "Registration failed! CoreException: " + e);
		    	Toast.makeText(context, context.getString(R.string.registration_failure_general), Toast.LENGTH_SHORT).show();
		    	Activity activity = (Activity) context;
		    	activity.finish();
		    	return;
			}

			SharedPreferences.Editor editor = settings.edit();
            editor.putString(Core.KEY_REGISTRATION_ID, registrationId);
    		editor.commit();
	    }
	}

	private void handleMessage(Context context, Intent intent)
	{
		Log.d(TAG, "handleMessage()");
		
		Bundle b = intent.getExtras();
		String message = b.getString("message");
		Log.i(TAG, "received message:" + message);

		// Decode the JSON Object
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(message);
		} catch (JSONException e) {
			Log.e(TAG, "Error decoding JSON from received message");
			e.printStackTrace();
			return;
		}
		List<KeyValuePair> keyValuePairs = new ArrayList<KeyValuePair>();
		Iterator<?> iter = jsonObject.keys();
	    while (iter.hasNext()) {
	        String key = (String) iter.next();
	        try {
	            String value = jsonObject.getString(key);
	            keyValuePairs.add(new KeyValuePair(key, value));
	        } catch (JSONException e) {
				Log.e(TAG, "Error decoding JSON from received message");
				e.printStackTrace();
				return;
	        }
	    }
	    
	    // And save the data
		PushsiDBHelper mDBHelper = new PushsiDBHelper(context);
	    long alertId = mDBHelper.createAlert();
	    mDBHelper.addAlertData(alertId, keyValuePairs);
		
		// Do whatever you want with the message
		//Toast.makeText(context, "RECEIVED A MESSAGE!", Toast.LENGTH_SHORT).show();

		// TODO: obviously expand upon this...
		
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);

		int icon = R.drawable.ic_launcher;        // icon from resources
		CharSequence tickerText = "Push.si";              // ticker-text
		long when = System.currentTimeMillis();         // notification time
		
		Notification notification = new Notification(icon, tickerText, when);
		
		CharSequence contentTitle = "Push.si";  // message title
		CharSequence contentText = message;      // message text
		
		Intent notificationIntent = new Intent(context, PushsiAlertActivity.class);
		notificationIntent.putExtra(Core.KEY_ALERT_ID, alertId);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		// Add sound!
		notification.defaults |= Notification.DEFAULT_SOUND;
		
		// Vibration!
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		
		// Flashing lights!
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		mNotificationManager.notify(HELLO_ID, notification);
	}
	
}