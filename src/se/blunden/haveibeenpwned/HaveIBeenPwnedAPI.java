package se.blunden.haveibeenpwned;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

public class HaveIBeenPwnedAPI {
	private static final String TAG = "HaveIBeenPwned";
	
	public HaveIBeenPwnedAPI() {
		
	}
	
	public ArrayList<String> query(String account) throws URISyntaxException, IOException, JSONException {
		String apiUrl = "https://haveibeenpwned.com/api/breachedaccount/";
		ArrayList<String> response = new ArrayList<String>();
		URI uri = new URI(apiUrl + account);
		URL requestURL = uri.toURL();
		final HttpsURLConnection connection = (HttpsURLConnection) requestURL.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
			String responseMessage = reader.readLine();
            reader.close();
			JSONArray jsonArray = new JSONArray(responseMessage);
			for(int i = 0; i < jsonArray.length(); i++) {
				response.add(jsonArray.getString(i));
			}
			return response;
		} else {
			Log.d(TAG, connection.getResponseMessage());
			return null;
		}
	}
}
