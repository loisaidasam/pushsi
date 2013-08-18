package si.push;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class RestClient {
	
	private static final String TAG = "Pushsi_RestClient";

	private static String _convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static HttpResponse request(String url, String method, HashMap<String, String> data) 
		throws ClientProtocolException, IOException
	{
		HttpClient httpclient = new DefaultHttpClient();

		// Prepare the params
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        if (data != null) {
	        for (String key : data.keySet()) {
	        	params.add(new BasicNameValuePair(key, data.get(key)));
	        }
        }

		HttpResponse response = null;

		// Prepare a request object and execute
		if (method.toLowerCase().equals("post")) {
			Log.d(TAG, "POST REQUEST");
			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			response = httpclient.execute(httpPost);
		} else {
			Log.d(TAG, "GET REQUEST");
	        String paramString = URLEncodedUtils.format(params, "utf-8");
	        url += "?" + paramString;
			HttpGet httpGet = new HttpGet(url);
			response = httpclient.execute(httpGet);
		}
		
		// Examine the response status
		Log.d(TAG, response.getStatusLine().toString());
		
		return response;
	}
	
	public static String getResponseAsString(HttpResponse response) 
		throws IllegalStateException, IOException 
	{
		// Get hold of the response entity
		HttpEntity entity = response.getEntity();
		// If the response does not enclose an entity, there is no need
		// to worry about connection release

		if (entity == null) {
			return null;
		}

		// Grab the response and convert it to a String
		InputStream instream = entity.getContent();
		String result = _convertStreamToString(instream);

		// Closing the input stream will trigger connection release
		instream.close();
		
		return result;
	}
	
	public static JSONObject stringToJSON(String input) 
		throws JSONException 
	{
		return new JSONObject(input);
	}
}