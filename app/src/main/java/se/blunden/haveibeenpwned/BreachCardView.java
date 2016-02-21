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
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

/**
 * Custom ViewGroup that animates into view when added.
 */
public class BreachCardView extends CardView {

	private TextView siteHeaderView;
	private TextView siteAccountView;
	private TextView siteDescriptionView;
	
	// Store a Breach object with all relevant data about the breach
	private Breach breachData;
	
	public BreachCardView(Context context, Breach breach) {
		super(context);
		initialize(context, breach);
	}
	
	public BreachCardView(Context context, Breach breach, AttributeSet attrs) {
		super(context, attrs);
		initialize(context, breach);
	}
	
	public BreachCardView(Context context, Breach breach, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context, breach);
	}

	@Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.card_slide_up);
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
			siteDescriptionView.setText(Html.fromHtml(description));
			siteDescriptionView.setMovementMethod(LinkMovementMethod.getInstance());
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
	
	public Breach getBreach() {
		return breachData;
	}
	
	private void initialize(Context context, Breach breach) {
		// Set the content padding
		setContentPadding(18, 10, 10, 10);
		// Enable compat padding to use the same padding on Lollipop and older platforms
		setUseCompatPadding(true);
		// Inflate the card layout
		LayoutInflater  mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInflater.inflate(R.layout.now_card, this, true);
		
		// Save a reference to the different TextViews
		siteHeaderView = (TextView) findViewById(R.id.card_site_header);
		siteAccountView = (TextView) findViewById(R.id.card_site_account);
		siteDescriptionView = (TextView) findViewById(R.id.card_site_description);
		
		// Set the Roboto Light font on the header (not done in the layout due to pre-4.1 compatibility)
		Typeface robotoLight = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
		siteHeaderView.setTypeface(robotoLight);
		
		// Store the Breach object for later use, such as restoring state
		breachData = breach;
		
		// Fill in the TextViews from the breach data
		setSiteHeaderText(breach.getTitle());
		setSiteAccountText("Compromised: " + breach.getAccount());
		setSiteDescriptionText(breach.getDescription());
	}
}
