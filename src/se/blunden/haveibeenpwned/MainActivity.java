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
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	
	private DBAdapter db;
	private static ArrayList<String> searchHistory = null;
	
	private HistoryCardView historyCard;
	private EditText searchInputField;
	private ImageButton searchButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Explicitly specify the preference file to load instead of the default to actually make it read it properly
		mPreferences = getApplicationContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
		
		db = new DBAdapter(this);
		
		if(isFirstLaunch()) {
			displayHelpCard();			
			storeFirstLaunch();
		}
		
		prepareAboutDialog();
		
		// Check if this is the first start and if so, fetch the history from the db
		if(savedInstanceState == null) {
			searchHistory = new ArrayList<String>();
			// Fetch the search history from the database and display the history card
			new FetchHistoryTask().execute();
		} else {
			// Since we are restoring from a configuration change (probably a rotation), searchHistory is populated already
			if(!searchHistory.isEmpty()) {
				displayHistoryCard();
			}
		}
		
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
		if(!isConnected()) {
			Log.e(TAG, "No internet connection detected");
			Toast.makeText(getBaseContext(), getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
			return;
		}
		
		String account = searchInputField.getText().toString().trim();
    	
    	// Add to search history unless it matches the most recently searched account
    	if(!account.equals("") && account != null && !matchesLatestSearch(account)) {
    		searchHistory.add(account);
    		Log.d(TAG, "performSearch searchHistory: " + searchHistory);
    		db.setOutdated(true);
    	}
    	Log.d(TAG, "Searching for account: " + account);
    	
    	// Clear the search field
    	searchInputField.setText("");
    	
    	showSpinner();
    	
    	// Perform the search using the AsyncTask
    	new PerformSearchTask().execute(account);
	}

	private void displayOutput(Breach breach) {
		// Get a reference to the layout where the card will be displayed
		final LinearLayout layout = (LinearLayout) findViewById(R.id.now_layout);
		
		// Create the View for the card and pass along the breach data used to populate it
		final BreachCardView card = new BreachCardView(this, breach);
		
		// Specify layout parameters to be applied
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 30, 0, 0);
		
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
		lp.setMargins(0, 30, 0, 0);
		
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
	
	private void displayHistoryCard() {
		// Get a reference to the layout where the card will be displayed
		final LinearLayout layout = (LinearLayout) findViewById(R.id.now_layout);
		
		// Create the View for the card and save the reference
		if(historyCard == null) {
			historyCard = new HistoryCardView(this);
		}
		
		// Specify layout parameters to be applied
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 30, 0, 0);
		
		updateHistoryCard(historyCard);
		historyCard.setLayoutParams(lp);
		
        // Create the swipe-to-dismiss touch listener.
		historyCard.setOnTouchListener(new SwipeDismissTouchListener(
			historyCard,
            null,
            new SwipeDismissTouchListener.DismissCallbacks() {
                @Override
                public boolean canDismiss(Object token) {
                    return true;
                }

                @Override
                public void onDismiss(View view, Object token) {
                	layout.removeView(historyCard);
                }
            }));

        layout.addView(historyCard);
        
        // Register listeners for the buttons (must be done after adding the view)
		ImageButton historyButton1 = (ImageButton) findViewById(R.id.button_history_search_1);
		historyButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	searchInputField.setText(historyCard.getHistory1().getText());
            	performSearch();
            }
        });
		
		final TextView historyText1 = (TextView) findViewById(R.id.card_history_1);
		historyText1.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
            	confirmDelete(historyCard.getHistory1().getText().toString(), 1);
            	return true;
            }
        });
		
		ImageButton historyButton2 = (ImageButton) findViewById(R.id.button_history_search_2);
		historyButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	searchInputField.setText(historyCard.getHistory2().getText());
            	performSearch();
            }
        });
		
		final TextView historyText2 = (TextView) findViewById(R.id.card_history_2);
		historyText2.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
            	confirmDelete(historyCard.getHistory2().getText().toString(), 2);
            	return true;
            }
        });
		
		ImageButton historyButton3 = (ImageButton) findViewById(R.id.button_history_search_3);
		historyButton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	searchInputField.setText(historyCard.getHistory3().getText());
            	performSearch();
            }
        });
		
		final TextView historyText3 = (TextView) findViewById(R.id.card_history_3);
		historyText3.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
            	confirmDelete(historyCard.getHistory3().getText().toString(), 3);
            	return true;
            }
        });
	}
	
	private void updateHistoryCard(HistoryCardView card) {
		if(searchHistory != null) {
			int last = searchHistory.size() - 1;
			// TODO: Consider using a switch statement instead
			if(last < 2) {
				if(last < 1) {
					card.setHistory1(searchHistory.get(last));
					card.setHistory2(null);
					card.setHistory3(null);
				} else {
					card.setHistory1(searchHistory.get(last));
					card.setHistory2(searchHistory.get(last - 1));
					card.setHistory3(null);
				}
			} else {
				card.setHistory1(searchHistory.get(last));
				card.setHistory2(searchHistory.get(last - 1));
				card.setHistory3(searchHistory.get(last - 2));
			}
		}
	}
	
	private void confirmDelete(String account, int id) {
		AlertDialog confirmDialog = new AlertDialog.Builder(this)
		.setTitle(account)
		.setMessage(R.string.confirm_delete)
		.setPositiveButton(R.string.yes, new DeleteOnClickListener(id))
		.setNegativeButton(R.string.no, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create();
		confirmDialog.show();
	}
	
	private final class DeleteOnClickListener implements DialogInterface.OnClickListener {
    	private int id;
    	
    	public DeleteOnClickListener(int id) {
    		this.id = id;
    	}
		
		public void onClick(DialogInterface dialog, int which) {
			searchHistory.remove(searchHistory.size() - id);
			updateHistoryCard(historyCard);
    	}
    }
	
	private void showSpinner() {
		searchInputField.setVisibility(View.INVISIBLE);
		
		View spinner = findViewById(R.id.search_spinner);
		spinner.setVisibility(View.VISIBLE);
	}
	
	private void hideSpinner() {
		View spinner = findViewById(R.id.search_spinner);
		spinner.setVisibility(View.GONE);
		
		searchInputField.setVisibility(View.VISIBLE);
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
		        if (view instanceof BreachCardView || view instanceof HelpCardView) {
		        	group.removeView(view);
		        	break;
		        }
		    }
			if(i == count) {
				finished = true;
			}
		}
	}
	
	public boolean isConnected() {
    	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    	
    	return (activeNetwork != null && activeNetwork.isConnected());
    }
	
	private boolean isFirstLaunch() {
    	return mPreferences.getBoolean("firstLaunch", true);
    }
	
	private void storeFirstLaunch(){
    	SharedPreferences.Editor editor = mPreferences.edit();
    	
    	editor.putBoolean("firstLaunch", false);
    	editor.apply();
    }
	
	private boolean matchesLatestSearch(String account) {
		if(!searchHistory.isEmpty()) {
			String latestSearch = searchHistory.get(searchHistory.size() - 1);
			if(latestSearch != null) {
				return latestSearch.equals(account);
			}
		}
		return false;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    // Store all breaches to be able to restore on configuration change
	    ArrayList<Breach> savedBreaches = new ArrayList<Breach>();
	    boolean firstLaunch = false;
	    
	    ViewGroup group = (ViewGroup) findViewById(R.id.now_layout);
	    for (int i = 0, count = group.getChildCount(); i < count; ++i) {
	        View view = group.getChildAt(i);
	        if (view instanceof BreachCardView) {
	        	savedBreaches.add(((BreachCardView) view).getBreach());
	        }
	        if (view instanceof HelpCardView) {
	        	firstLaunch = true;
	        }
	    }

	    outState.putParcelableArrayList("savedBreaches", savedBreaches);
	    outState.putString("savedSearchInput", searchInputField.getText().toString());
	    outState.putBoolean("firstLaunch", firstLaunch);
	    outState.putStringArrayList("searchHistory", searchHistory);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// Retrieve saved breaches
		ArrayList<Breach> savedBreaches = savedInstanceState.getParcelableArrayList("savedBreaches");
		searchHistory = savedInstanceState.getStringArrayList("searchHistory");
		
		boolean firstLaunch = savedInstanceState.getBoolean("firstLaunch");
		
		// Restore saved user search field input
		searchInputField.setText(savedInstanceState.getString("savedSearchInput"));
		
		// Add the help card back
		if(firstLaunch) {
			displayHelpCard();
		}
		
		// Add the cards back
		if(savedBreaches != null) {
			for(Breach breach : savedBreaches) {
				displayOutput(breach);
			}
		}
		
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ABOUT_ID, 0, R.string.menu_about)
			.setIcon(R.drawable.ic_actionbar_info).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(0, CLEAR_ALL_ID, 0, R.string.menu_clear_all)
			.setIcon(R.drawable.ic_actionbar_clear_all).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
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
	
	@Override
	public void onStop() {
		super.onStop();
		if(db.isOutdated()) {
			db.updateHistoryDB(searchHistory);
		}
		db.close();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause searchHistory: " + searchHistory);
		if(db.isOutdated()) {
			db.updateHistoryDB(searchHistory);
		}
		db.close();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(db.isOutdated()) {
			db.updateHistoryDB(searchHistory);
		}
		db.close();
	}
	
	private class PerformSearchTask extends AsyncTask<String, Void, ArrayList<Breach>> {
    	protected ArrayList<Breach> doInBackground(String... accounts) {
    		HaveIBeenPwnedAPI api = new HaveIBeenPwnedAPI();
    		ArrayList<Breach> result = new ArrayList<Breach>();
    		try {
				result = api.query(accounts[0]);
			} catch (URISyntaxException e) {
				Toast.makeText(getBaseContext(), getString(R.string.error_invalid_uri_syntax), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(getBaseContext(), getString(R.string.error_invalid_response), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
    		return result;
        }

        protected void onPostExecute(ArrayList<Breach> result) {
        	hideSpinner();
        	
        	// Create the history card if not already created by FetchHistoryTask
    		if(historyCard == null) {
    			displayHistoryCard();
    		} else {
    			updateHistoryCard(historyCard);
    		}
    		
        	if(result == null) {
        		Toast.makeText(getBaseContext(), getString(R.string.error_result_null), Toast.LENGTH_SHORT).show();
        		return;
        	} else if(!result.isEmpty()) {
        		for(Breach breach : result) {
        			displayOutput(breach);
        		}
        	}
        }
    }
	
	private class FetchHistoryTask extends AsyncTask<Void, Void, ArrayList<String>> {
    	protected ArrayList<String> doInBackground(Void... params) {
			return db.getHistory();
        }

        protected void onPostExecute(ArrayList<String> result) {
        	if(result == null) {
        		Log.d(TAG, "onPostExecute: Empty history returned!");
        		return;
        	} else if(!result.isEmpty()) {
        		searchHistory = result;
        		db.setOutdated(false);
        		displayHistoryCard();
        	}
        }
    }
}
