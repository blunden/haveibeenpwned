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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "HaveIBeenPwned";
	
	private static final int ABOUT_ID = Menu.FIRST;
	private static final int CLEAR_ALL_ID = Menu.FIRST + 1;
	
	private static String aboutMessage = null;
	private AlertDialog mAboutDialog;
	private SharedPreferences mPreferences;
	
	private static HashMap<String, String> siteNames = null;
	private static HashMap<String, String> siteDescriptions = null;
	
	private static ArrayDeque<String> searchHistory = null;
	
	private EditText searchInputField;
	private ImageButton searchButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Explicitly specify the preference file to load instead of the default to actually make it read it properly
		mPreferences = getApplicationContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
		
		if(isFirstLaunch()) {
			displayHelpCard();			
			storeFirstLaunch();
		}
		
		prepareAboutDialog();
		populateSiteData();
		
		searchHistory = new ArrayDeque<String>(4);
		
        searchInputField = (EditText) findViewById(R.id.input_search);
        
        searchInputField.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	performSearch();
                    return true;
                }
                return false;
            }
        });

        searchButton = (ImageButton) findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	performSearch();
            }
        });
	}
	
	private void performSearch() {
		String account = searchInputField.getText().toString().trim();
    	
    	// Add to search history
    	if(!account.equals("") || account == null) {
    		searchHistory.add(account);
    	}
    	Log.d(TAG, "Searching for account: " + account);
    	
    	// Clear the search field
    	searchInputField.setText("");
    	
    	Toast.makeText(getBaseContext(), getString(R.string.toast_search), Toast.LENGTH_SHORT).show();
    	
    	// Perform the search using the AsyncTask
    	new PerformSearchTask().execute(account);
	}
	
	private void displayOutput(String site, String restoredAccount) {
		// Get a reference to the layout where the card will be displayed
		final LinearLayout layout = (LinearLayout) findViewById(R.id.now_layout);
		
		// Create the View for the card 
		final CardView card = new CardView(this);
		
		// Specify layout parameters to be applied
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 20, 0, 0);
		
		// Set the internal state of the card
		card.setSite(site);
		
		// Show the prettier string if available
		if(siteNames.containsKey(site)) {
			card.setSiteHeaderText(siteNames.get(site));
		} else {
			card.setSiteHeaderText(site);
		}
		
		// Check if account is specified or pick the most recent from the search history if not
		if(restoredAccount == null) {
			if(!searchHistory.isEmpty()) {
				card.setSiteAccountText("Compromised: " + searchHistory.peekLast());
			}
		} else {
			card.setSiteAccountText(restoredAccount);
		}
		
		if(siteDescriptions.containsKey(site)) {
			card.setSiteDescriptionText(siteDescriptions.get(site));
		} else {
			card.setSiteDescriptionText(getString(R.string.card_description_unavailable));
		}
		card.setLayoutParams(lp);
		
        // Create the swipe-to-dismiss touch listener.
        card.setOnTouchListener(new SwipeDismissTouchListener(
            card,
            null,
            new SwipeDismissTouchListener.DismissCallbacks() {
                @Override
                public boolean canDismiss(Object token) {
                    return true;
                }

                @Override
                public void onDismiss(View view, Object token) {
                	layout.removeView(card);
                }
            }));
        
        layout.addView(card);
	}
	
	private void displayHelpCard() {
		// Get a reference to the layout where the card will be displayed
		final LinearLayout layout = (LinearLayout) findViewById(R.id.now_layout);
		
		// Create the View for the card 
		final HelpCardView card = new HelpCardView(this);
		
		// Specify layout parameters to be applied
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 20, 0, 0);
		
		card.setHeaderText(getString(R.string.card_title_help));
		card.setDescriptionText(getString(R.string.card_description_help));
		card.setDismissText(getString(R.string.card_swipe_dismiss));
		card.setLayoutParams(lp);
		
        // Create the swipe-to-dismiss touch listener.
        card.setOnTouchListener(new SwipeDismissTouchListener(
            card,
            null,
            new SwipeDismissTouchListener.DismissCallbacks() {
                @Override
                public boolean canDismiss(Object token) {
                    return true;
                }

                @Override
                public void onDismiss(View view, Object token) {
                	layout.removeView(card);
                }
            }));
        
        layout.addView(card);
	}
	
	private void populateSiteData() {
		// Increase initial capacity when new sites are added to the service
		siteNames = new HashMap<String, String>(9);
		
		siteNames.put("Adobe", getString(R.string.card_title_adobe));
		siteNames.put("BattlefieldHeroes", getString(R.string.card_title_battlefield_heroes));
		siteNames.put("Gawker", getString(R.string.card_title_gawker));
		siteNames.put("PixelFederation", getString(R.string.card_title_pixel_federation));
		siteNames.put("Snapchat", getString(R.string.card_title_snapchat));
		siteNames.put("Sony", getString(R.string.card_title_sony));
		siteNames.put("Stratfor", getString(R.string.card_title_stratfor));
		siteNames.put("Vodafone", getString(R.string.card_title_vodafone));
		siteNames.put("Yahoo", getString(R.string.card_title_yahoo));
		
		// Increase initial capacity when new sites are added to the service
		siteDescriptions = new HashMap<String, String>(9);
		
		siteDescriptions.put("Adobe", getString(R.string.card_description_adobe));
		siteDescriptions.put("BattlefieldHeroes", getString(R.string.card_description_battlefield_heroes));
		siteDescriptions.put("Gawker", getString(R.string.card_description_gawker));
		siteDescriptions.put("PixelFederation", getString(R.string.card_description_pixel_federation));
		siteDescriptions.put("Snapchat", getString(R.string.card_description_snapchat));
		siteDescriptions.put("Sony", getString(R.string.card_description_sony));
		siteDescriptions.put("Stratfor", getString(R.string.card_description_stratfor));
		siteDescriptions.put("Vodafone", getString(R.string.card_description_vodafone));
		siteDescriptions.put("Yahoo", getString(R.string.card_description_yahoo));
	}
	
	private void prepareAboutDialog() {
		if (aboutMessage == null) {
			aboutMessage = getString(R.string.about_message);
		}
		
		mAboutDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.menu_about)
		.setMessage(aboutMessage)
		.setNeutralButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create();
	}
	
	private void clearAllCards() {
		boolean finished = false;
		while(!finished) {
			ViewGroup group = (ViewGroup) findViewById(R.id.now_layout);
			int count = group.getChildCount();
			int i;
			for (i = 0; i < count; i++) {
				View view = group.getChildAt(i);
		        if (view instanceof CardView || view instanceof HelpCardView) {
		        	group.removeView(view);
		        	break;
		        }
		    }
			if(i == count) {
				finished = true;
			}
		}
	}
	
	private boolean isFirstLaunch() {
    	return mPreferences.getBoolean("firstLaunch", true);
    }
	
	private void storeFirstLaunch(){
    	SharedPreferences.Editor editor = mPreferences.edit();
    	
    	editor.putBoolean("firstLaunch", false);
    	editor.apply();
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    // Store all formatted card strings to be able to restore on configuration change
	    ArrayList<String> savedSiteStrings = new ArrayList<String>();
	    ArrayList<String> savedAccountStrings = new ArrayList<String>();
	    boolean firstLaunch = false;
	    ViewGroup group = (ViewGroup) findViewById(R.id.now_layout);
	    for (int i = 0, count = group.getChildCount(); i < count; ++i) {
	        View view = group.getChildAt(i);
	        if (view instanceof CardView) {
	        	savedSiteStrings.add(((CardView)view).getSite());
	        	savedAccountStrings.add(((CardView)view).getSiteAccountView().getText().toString());
	        }
	        if (view instanceof HelpCardView) {
	        	firstLaunch = true;
	        }
	    }
	    outState.putStringArrayList("savedSiteText", savedSiteStrings);
	    outState.putStringArrayList("savedAccountText", savedAccountStrings);
	    outState.putString("savedSearchInput", searchInputField.getText().toString());
	    outState.putBoolean("firstLaunch", firstLaunch);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// Retrieve saved strings
		ArrayList<String> savedSiteStrings = savedInstanceState.getStringArrayList("savedSiteText");
		ArrayList<String> savedAccountStrings = savedInstanceState.getStringArrayList("savedAccountText");
		
		boolean firstLaunch = savedInstanceState.getBoolean("firstLaunch");
		
		// Restore saved user search field input
		searchInputField.setText(savedInstanceState.getString("savedSearchInput"));
		
		// Add the help card back
		if(firstLaunch) {
			displayHelpCard();
		}
		
		// Add the cards back
		if(savedSiteStrings != null && savedAccountStrings != null) {
	    	for(int i = 0; i < Math.max(savedSiteStrings.size(), savedAccountStrings.size()); i++) {
	    		displayOutput(savedSiteStrings.get(i), savedAccountStrings.get(i));
	    	}
	    }
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ABOUT_ID, 0, R.string.menu_about)
			.setIcon(android.R.drawable.ic_menu_info_details).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(0, CLEAR_ALL_ID, 0, R.string.menu_clear_all)
			.setIcon(android.R.drawable.ic_menu_close_clear_cancel).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case ABOUT_ID:
			mAboutDialog.show();
			return true;
		
		case CLEAR_ALL_ID:
			// Clear the search field
        	searchInputField.setText("");
			// Remove all the cards
        	clearAllCards();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}
	
	private class PerformSearchTask extends AsyncTask<String, Void, ArrayList<String>> {
    	protected ArrayList<String> doInBackground(String... accounts) {
    		//Log.d(TAG, "doInBackground account: " + accounts[0]);
    		HaveIBeenPwnedAPI api = new HaveIBeenPwnedAPI();
    		ArrayList<String> result = new ArrayList<String>(9);
    		try {
				result = api.query(accounts[0]);
			} catch (URISyntaxException e) {
				Toast.makeText(getBaseContext(), getString(R.string.error_invalid_uri_syntax), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(getBaseContext(), getString(R.string.error_invalid_response), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (JSONException e) {
				Toast.makeText(getBaseContext(), getString(R.string.error_json_parsing), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
    		return result;
        }

        protected void onPostExecute(ArrayList<String> result) {
        	if(result == null) {
        		Toast.makeText(getBaseContext(), getString(R.string.error_result_null), Toast.LENGTH_SHORT).show();
        		return;
        	} else if(!result.isEmpty()) {
        		for(String site : result) {
        			displayOutput(site, null);
        		}
        	}
        }
     }
}
