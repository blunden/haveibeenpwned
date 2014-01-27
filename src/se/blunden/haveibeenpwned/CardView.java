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

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Custom ViewGroup that animates into view when added.
 */
public class CardView extends LinearLayout {

	private TextView siteHeaderView;
	private TextView siteAccountView;
	private TextView siteDescriptionView;
	private String siteState;
	
	public CardView(Context context) {
		super(context);
		initialize(context);
	}
	
	public CardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}
	
	public CardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

	@Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up_left);
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500);
        anim.setFillAfter(true);
        this.startAnimation(anim);
    }
	
	public void setSiteHeaderText(String siteheader) {
		if(siteheader.equals("")) {
			siteHeaderView.setVisibility(GONE);
		} else {
			siteHeaderView.setText(siteheader);
		}
	}
	
	public void setSiteAccountText(String account) {
		if(account.equals("")) {
			siteAccountView.setVisibility(GONE);
		} else {
			siteAccountView.setText(account);
		}
	}
	
	public void setSiteDescriptionText(String description) {
		
		if(description.equals("")) {
			siteHeaderView.setVisibility(GONE);
			siteDescriptionView.setVisibility(GONE);
		} else {
			siteDescriptionView.setText(description);
		}
	}

	public TextView getSiteHeaderView() {
		return siteHeaderView;
	}
	
	public TextView getSiteAccountView() {
		return siteAccountView;
	}
	
	public TextView getSiteDescriptionView() {
		return siteDescriptionView;
	}
	
	public String getSite() {
		return siteState;
	}
	
	public void setSite(String site) {
		siteState = site;
	}
	
	private void initialize(Context context) {
		// Inflate the card layout
		LayoutInflater  mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInflater.inflate(R.layout.now_card, this, true);
		
		// Save a reference to the different TextViews
		siteHeaderView = (TextView) findViewById(R.id.card_site_header);
		siteAccountView = (TextView) findViewById(R.id.card_site_account);
		siteDescriptionView = (TextView) findViewById(R.id.card_site_description);
	}
}
