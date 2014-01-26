package se.blunden.haveibeenpwned;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "HaveIBeenPwned";
	
	private static final int ABOUT_ID = Menu.FIRST;
	private static final int CLEAR_ALL_ID = Menu.FIRST + 1;
	
	private static String aboutMessage = null;
	private AlertDialog mAboutDialog;
	private static HashMap<String, String> siteDescriptions = null;
	
	private EditText searchInputField;
	private ImageButton searchButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		prepareAboutDialog();
		populateDescriptionMap();
		
        searchInputField = (EditText) findViewById(R.id.input_search);

        searchButton = (ImageButton) findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                	String account = searchInputField.getText().toString();
                	
                	Log.d(TAG, "Searching for account: " + account);
                	
                	// Clear the search field
                	searchInputField.setText("");
                	
                	Toast.makeText(getBaseContext(), getString(R.string.toast_search), Toast.LENGTH_SHORT).show();
                	
                	// Perform the search using the AsyncTask
                	new PerformSearchTask().execute(account);
                }
            });
	}
	
	private void displayOutput(String site) {
		// Get a reference to the layout where the card will be displayed
		final LinearLayout layout = (LinearLayout) findViewById(R.id.now_layout);
		
		// Create the View for the card 
		final CardView card = new CardView(this);
		
		// Specify layout parameters to be applied
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 20, 0, 0);
		
		Log.d(TAG, "displayOutput site: " + site);
		
		card.setSiteHeaderText(site);
		card.setSiteDescriptionText(siteDescriptions.get(site));
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
	
	private void populateDescriptionMap() {
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
		ViewGroup group = (ViewGroup) findViewById(R.id.now_layout);
		for (int i = 0, count = group.getChildCount(); i < count; i++) {
	        View view = group.getChildAt(i);
	        if (view instanceof CardView) {
	        	Log.d(TAG, "clearAllCards i: " + i);
	        	group.removeView(view);
	        }
	    }
		// The loop above does not remove the first card if 3 or more have been added
		// For now, remove the card at index 1 (index 0 is the input field) if it is indeed a card
		View view = group.getChildAt(1);
		if(view instanceof CardView) {
			Log.d(TAG, "clearAllCards removing remaining card at index 1");
        	group.removeView(view);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    // Store all formatted card strings to be able to restore on configuration change
	    ArrayList<String> savedHeaderStrings = new ArrayList<String>();
	    ViewGroup group = (ViewGroup) findViewById(R.id.now_layout);
	    for (int i = 0, count = group.getChildCount(); i < count; ++i) {
	        View view = group.getChildAt(i);
	        if (view instanceof CardView) {
	        	savedHeaderStrings.add(((CardView)view).getSiteHeaderView().getText().toString());
	        }
	    }
	    outState.putStringArrayList("savedHeaderText", savedHeaderStrings);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// Retrieve saved strings
		ArrayList<String> savedHeaderStrings = savedInstanceState.getStringArrayList("savedHeaderText");
		Log.d(TAG, "restored savedHeaderText size: " + savedHeaderStrings.size());
		
		// Add the cards back
		if(savedHeaderStrings != null) {
	    	for(String site : savedHeaderStrings) {
	    		displayOutput(site);
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
			clearAllCards();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}
	
	private class PerformSearchTask extends AsyncTask<String, Void, ArrayList<String>> {
    	protected ArrayList<String> doInBackground(String... accounts) {
    		Log.d(TAG, "doInBackground account: " + accounts[0]);
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
        		Log.d(TAG, "onPostExecute: result is null");
        		Toast.makeText(getBaseContext(), getString(R.string.error_result_null), Toast.LENGTH_SHORT).show();
        		return;
        	} else if(!result.isEmpty()) {
        		for(String site : result) {
        			displayOutput(site);
        		}
        	}
        }
     }
}
