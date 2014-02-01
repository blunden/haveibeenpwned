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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Custom ViewGroup that animates into view when added.
 */
public class HelpCardView extends LinearLayout {

	private TextView headerView;
	private TextView descriptionView;
	private TextView dismissTextView;
	
	public HelpCardView(Context context) {
		super(context);
		initialize(context);
	}
	
	public HelpCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}
	
	public HelpCardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

	@Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.card_slide_up);
        this.startAnimation(anim);
    }
	
	public void setHeaderText(String header) {
		if(header.equals("")) {
			headerView.setVisibility(GONE);
		} else {
			headerView.setText(header);
		}
	}
	
	public void setDescriptionText(String description) {
		
		if(description.equals("")) {
			headerView.setVisibility(GONE);
			descriptionView.setVisibility(GONE);
		} else {
			descriptionView.setText(description);
		}
	}
	
	public void setDismissText(String text) {
		if(text.equals("")) {
			dismissTextView.setVisibility(GONE);
		} else {
			dismissTextView.setText(text);
		}
	}

	public TextView getSiteHeaderView() {
		return headerView;
	}
	
	public TextView getSiteDescriptionView() {
		return descriptionView;
	}
	
	private void initialize(Context context) {
		// Inflate the card layout
		LayoutInflater  mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInflater.inflate(R.layout.now_help_card, this, true);
		
		// Save a reference to the different TextViews
		headerView = (TextView) findViewById(R.id.card_header);
		descriptionView = (TextView) findViewById(R.id.card_description);
		dismissTextView = (TextView) findViewById(R.id.card_dismiss);
	}
}
