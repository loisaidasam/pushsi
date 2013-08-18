package si.push;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;


public class Core {

	public static String KEY_REGISTRATION_ID = "registration_id";
	public static String KEY_UUID = "uuid";
	
	private static final String TAG = "Pushsi_Core";
	
	// Random method
	private static UUID _getUUIDRandom() {
		return UUID.randomUUID();
	}
	
	// Device details method
	// requires: <uses-permission android:name="android.permission.READ_PHONE_STATE" />
	// from: http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
	private static UUID _getUUIDDevice(Context ctx) {
		final TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
	
	    final String tmDevice, tmSerial, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(ctx.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	
	    return new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	}

	private static JSONObject _makeServerCall(String url, String method, HashMap<String, String> data) 
		throws CoreException 
	{
		try {
			HttpResponse response = RestClient.request(url, method, data);
			String responseString = RestClient.getResponseAsString(response);
			JSONObject responseJSON = RestClient.stringToJSON(responseString);
			
			// See if we can use the status code
			StatusLine statusLine = response.getStatusLine();
			if (statusLine != null) {
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 400 || statusCode == 500) {
					throw new CoreException("CoreException: " + responseJSON.getString("error"));
				}
			}
			
			return responseJSON;
		
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new CoreException("ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new CoreException("IOException: " + e.getMessage());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new CoreException("JSONException: " + e.getMessage());
		}
	}
	
	public static String getUUID(Context ctx) throws CoreException {
		Log.d(TAG, "getUUID()");
		
		String uuid_type = ctx.getString(R.string.uuid_type);
		
		UUID uuid = null;
		
		if (uuid_type.compareTo("random") == 0) {
			uuid = _getUUIDRandom();
		} else if (uuid_type.compareTo("device") == 0) {
			uuid = _getUUIDDevice(ctx);
		} else {
			throw new CoreException("Unrecognized uuid_type: " + uuid_type);
		}
		
	    return uuid.toString();
	}
	
	public static void registerC2DM(Context ctx) {
		Log.d(TAG, "registerC2DM()");
    	Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
    	registrationIntent.putExtra("app", PendingIntent.getBroadcast(ctx, 0, new Intent(), 0)); // boilerplate
    	registrationIntent.putExtra("sender", ctx.getString(R.string.c2dm_sender_email));
    	ctx.startService(registrationIntent);
	}
	
	public static boolean registerPhone(Context ctx, String uuid) throws CoreException {
		Log.d(TAG, "Calling registerPhone() with uuid=" + uuid + "...");
		
		final String API_ENDPOINT = ctx.getString(R.string.api_endpoint);
		String url = API_ENDPOINT + "/phone/register";
		
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("phone_uuid", uuid);
		data.put("phone_type", "Android");
		
		JSONObject json = _makeServerCall(url, "POST", data);
		boolean status;
		try {
			status = json.getBoolean("status");
		} catch (JSONException e) {
			e.printStackTrace();
			throw new CoreException("JSONException: " + e.getMessage());
		}
		
		Log.i(TAG, "registerPhone() result : " + status);
		return status;
	}
	
	public static boolean updateC2DM(Context ctx, String uuid, String c2dmToken) throws CoreException {
		Log.d(TAG, "Calling updateC2DM() with uuid=" + uuid + " c2dmToken=" + c2dmToken + "...");
		
		final String API_ENDPOINT = ctx.getString(R.string.api_endpoint);
		String url = API_ENDPOINT + "/phone/c2dm";
		
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("phone_uuid", uuid);
		data.put("c2dm_token", c2dmToken);
		
		JSONObject json = _makeServerCall(url, "POST", data);
		boolean status;
		try {
			status = json.getBoolean("status");
		} catch (JSONException e) {
			e.printStackTrace();
			throw new CoreException("JSONException: " + e.getMessage());
		}
		
		Log.i(TAG, "updateC2DM() result : " + status);
		return status;
	}
	
	/**
	 * Send UUID and C2DM id to server
	 * 
	 * @param ctx Context 
	 * @param uuid String 
	 * @return String 4 digit pin code
	 * @throws CoreException 
	 */
	public static String getRegistrationPin(Context ctx, String uuid) throws CoreException {
		Log.d(TAG, "Calling getRegistrationPin() with uuid=" + uuid + " ...");
		
		final String API_ENDPOINT = ctx.getString(R.string.api_endpoint);
		String url = API_ENDPOINT + "/pin/request";
		
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("phone_uuid", uuid);
		
		JSONObject json = _makeServerCall(url, "POST", data);
		String pin = null;
		try {
			pin = json.getString("pin_code");
		} catch (JSONException e) {
			e.printStackTrace();
			throw new CoreException("JSONException: " + e.getMessage());
		}
		
		Log.i(TAG, "getRegistrationPin() result : " + pin);
		
		return pin;
	}
	
	/**
	 * Check whether the phone has been registered on a computer
	 * 
	 * @param ctx
	 * @param uuid
	 * @param pin_code
	 * @return
	 * @throws CoreException 
	 */
	public static boolean checkRegistrationStatus(Context ctx, String uuid, String pin_code) throws CoreException {
		Log.d(TAG, "Calling checkRegistrationStatus() with uuid=" + uuid + " pin_code=" + pin_code + " ...");
		
		final String API_ENDPOINT = ctx.getString(R.string.api_endpoint);
		String url = API_ENDPOINT + "/pin/status";
		
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("phone_uuid", uuid);
		data.put("pin_code", pin_code);
		
		JSONObject json = _makeServerCall(url, "GET", data);
		boolean resolved;
		try {
			resolved = json.getBoolean("resolved");
		} catch (JSONException e) {
			e.printStackTrace();
			throw new CoreException("JSONException: " + e.getMessage());
		}
		
		Log.i(TAG, "checkRegistrationStatus() result : " + resolved);
		
		return resolved;
	}
}