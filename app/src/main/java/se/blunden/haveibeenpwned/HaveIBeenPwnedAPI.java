/*
* Copyright (C) 2014 blunden
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

public class HaveIBeenPwnedAPI {
	private static final String TAG = "HaveIBeenPwnedAPI";
	
	public HaveIBeenPwnedAPI() {
		
	}
	
	public ArrayList<Breach> query(String account) throws URISyntaxException, IOException {
		String apiUrl = "https://haveibeenpwned.com/api/v2/breachedaccount/";
		
		// No need to generate api requests for empty searches
		if(account.equals("")) {
			return null;
		}
		URL requestURL = new URL(apiUrl + account);
		
		// Split and reassemble the URL to properly encode the relevant parts 
		URI uri = new URI(requestURL.getProtocol(), requestURL.getHost(),
				requestURL.getPath(), requestURL.getRef());
		requestURL = uri.toURL();
		final HttpsURLConnection connection = (HttpsURLConnection) requestURL.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent","se.blunden.HaveIBeenPwned");
		connection.connect();
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return readJsonStream(connection.getInputStream(), account);
		} else {
			Log.d(TAG, "Response: " + connection.getResponseCode() + connection.getResponseMessage());
			return null;
		}
	}
	
	private ArrayList<Breach> readJsonStream(InputStream in, String compromisedAccount) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
	    try {
	    	return readResponseArray(reader, compromisedAccount);
	    } finally {
	    	reader.close();
	    }
	}
	
	private ArrayList<Breach> readResponseArray(JsonReader reader, String compromisedAccount) throws IOException {
		ArrayList<Breach> breaches = new ArrayList<Breach>();
		
		reader.beginArray();
		while(reader.hasNext()) {
			breaches.add(readBreach(reader, compromisedAccount));
		}
		reader.endArray();
		return breaches;
	}
	
	private Breach readBreach(JsonReader reader, String compromisedAccount) throws IOException {
		String name = null;
		String title = null;
		String description = null;
		String account = compromisedAccount;
		
		reader.beginObject();
		while(reader.hasNext()) {
			String tokenName = reader.nextName();
			if(tokenName.equals("Name")) {
				name = reader.nextString();
			} else if(tokenName.equals("Title")) {
				title = reader.nextString();
			} else if(tokenName.equals("Description")) {
				description = reader.nextString();
				// Needed because the API currently returns improperly formatted/escaped data breaking URL parsing
				String[] invalid = {"” ", " “", "”","“"};
				String[] correct = {"\"", "\"", "\"", "\""};
				description = TextUtils.replace(description, invalid, correct).toString();
			} else {
				reader.skipValue();
			}			
		}
		reader.endObject();
		return new Breach(name, title, description, account);
	}
}
