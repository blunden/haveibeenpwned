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

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {    
	private static final String TAG = "HaveIBeenPwned";
    
    private static final String DATABASE_NAME = "search_history";
    private static final int DATABASE_VERSION = 1;
    
	private static final String HISTORY_TABLE = "history";
    
	private static final String CREATE_TABLE_HISTORY = "create table history (history_id integer primary key, "
			+ "account text not null);";
	
	private final Context context; 
    
	private boolean outdated = true;

	private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
        
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_HISTORY);
        }

        /**
         * The database is upgraded by destroying the existing data.
         * 
         * Modify to upgrade in-place if ever needed
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion 
                    + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS history");
            onCreate(db);
        }
    }    
    
    // Open the database as writable
    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    // Close the database
    public void close() {
        DBHelper.close();
    }
    
    public boolean insertHistoryItem(String account) {
		open();
    	ContentValues values = new ContentValues();
		values.put("account", account);
		Log.d(TAG, "insertHistoryItem ContentValues: " + values);
		return (db.insert(HISTORY_TABLE, null, values) > 0);
	}
    
    public boolean updateHistoryDB(ArrayList<String> searchHistory) {
    	open();
		// Clear the database
		db.delete(HISTORY_TABLE, null, null);
		
		int linesInserted = 0;
		int startIndex = 0;
		int size = searchHistory.size();
		if(size > 50) {
			startIndex = size - 50;
		}
    	try {
    		db.beginTransaction();
    		ContentValues values = new ContentValues();
    		String account;
    		for(int i = startIndex; i < size; i++) {
    			account = searchHistory.get(i);
    			if(account != null) {
    				values.put("account", account);
    				if(db.insert(HISTORY_TABLE, null, values) != -1) {
    					linesInserted++;
    				}
    			}
    		}
    		db.setTransactionSuccessful();
    		outdated = false;
    	} catch (SQLException e) {
    		Log.e(TAG, e.toString());
    	} finally {
    		db.endTransaction();
    	}
    	Log.d(TAG, "DB lines inserted: " + linesInserted);
    	return (linesInserted > 0);
	}

	public boolean deleteHistoryItem(Long historyId) {
		open();
		return (db.delete(HISTORY_TABLE, "history_id=" + historyId.toString(), null) > 0);
	}
	
	public ArrayList<String> getHistory() {
		open();
		ArrayList<String> searchHistory = new ArrayList<String>();
		try {
			Cursor mCursor = db.query(HISTORY_TABLE, new String[] { "history_id", "account"}, null, null, null, null, null);

			int numRows = mCursor.getCount();
			mCursor.moveToFirst();
			for(int i = 0; i < numRows; i++) {
				String account = mCursor.getString(1);
				searchHistory.add(account);
				mCursor.moveToNext();
			}
		} catch (SQLException e) {
			Log.e(TAG, e.toString());
		}
		Log.d(TAG, "searchHistory db: " + searchHistory);
		return searchHistory;
	}
	
	public boolean isOutdated() {
		return outdated;
	}

	public void setOutdated(boolean outdated) {
		this.outdated = outdated;
	}
}
