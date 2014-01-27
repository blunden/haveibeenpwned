/*
* Copyright (C) 2014 Björn Lundén
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
	private static final String TAG = "HaveIBeenPwnedAPI";
	
	public HaveIBeenPwnedAPI() {
		
	}
	
	public ArrayList<String> query(String account) throws URISyntaxException, IOException, JSONException {
		String apiUrl = "https://haveibeenpwned.com/api/breachedaccount/";
		ArrayList<String> response = new ArrayList<String>();
		URL requestURL = new URL(apiUrl + account);
		
		// Split and reassemble the URL to properly encode the relevant parts 
		URI uri = new URI(requestURL.getProtocol(), requestURL.getHost(),
				requestURL.getPath(), requestURL.getRef());
		requestURL = uri.toURL();
		final HttpsURLConnection connection = (HttpsURLConnection) requestURL.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent","se.blunden.HaveIBeenPwned/1.0");
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
			Log.d(TAG, "Response: " + connection.getResponseCode() + connection.getResponseMessage());
			return null;
		}
	}
}
