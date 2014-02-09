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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A Parcelable representation of a breach as returned from the 'Have I been pwned?' API
 * 
 * Only the values currently used by the app are included.
 */
public class Breach implements Parcelable {
	private String name;
	private String title;
	private String description;
	private String account;
	
	public Breach(String name, String title, String description, String account) {
		this.name = name;
		this.title = title;
		this.description = description;
		this.account = account;
	}

	public String getName() {
		return name;
	}
	
	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}
	
	public String getAccount() {
		return account;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
		out.writeString(title);
		out.writeString(description);
		out.writeString(account);
	}
	
	public static final Parcelable.Creator<Breach> CREATOR = new Parcelable.Creator<Breach>() {
		public Breach createFromParcel(Parcel in) {
			return new Breach(in);
		}

		public Breach[] newArray(int size) {
			return new Breach[size];
		}
	};

	/**
	 * For use exclusively by the parcelable creator
	 */
	private Breach(Parcel in) {
        this.name = in.readString();
        this.title = in.readString();
		this.description = in.readString();
		this.account = in.readString();
    }
}
