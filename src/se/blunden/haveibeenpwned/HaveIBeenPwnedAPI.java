package se.blunden.haveibeenpwned;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;

public class HaveIBeenPwnedAPI {
	public HaveIBeenPwnedAPI() {
		
	}
	
	public ArrayList<String> query(String account) {
		String apiUrl = "https://haveibeenpwned.com/api/breachedaccount/";
		ArrayList<String> response = new ArrayList<String>(); // 9 dumps are currently searched by service
		URL requestURL = null;
		URI uri = null;
		//TODO: Consider handling exceptions in MainActivity instead
		try {
			uri = new URI(apiUrl + account);
			requestURL = uri.toURL();
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
				return null;
			}
			
		} catch (URISyntaxException e) {
			// Do something better here
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// Do something better here
			e.printStackTrace();
		} catch (IOException e) {
			// Do something better here
			e.printStackTrace();
		} catch (JSONException e) {
			// Do something better here
			e.printStackTrace();
		}
		return null;
	}
}
