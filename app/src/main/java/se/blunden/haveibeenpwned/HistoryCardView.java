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

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

/**
 * Custom ViewGroup that animates into view when added.
 */
public class HistoryCardView extends CardView {

	private TextView history1;
	private TextView history2;
	private TextView history3;
	
	public HistoryCardView(Context context) {
		super(context);
		initialize(context);
	}
	
	public HistoryCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}
	
	public HistoryCardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

	@Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.card_slide_up);
        this.startAnimation(anim);
    }
	
	public void setHistory1(String history) {
		if(history == null) {
			findViewById(R.id.card_history_layout_1).setVisibility(GONE);
		} else {
			history1.setText(history);
			findViewById(R.id.card_history_layout_1).setVisibility(VISIBLE);
		}
	}
	
	public void setHistory2(String history) {
		if(history == null) {
			findViewById(R.id.card_history_divider_1).setVisibility(GONE);
			findViewById(R.id.card_history_layout_2).setVisibility(GONE);
		} else {
			findViewById(R.id.card_history_divider_1).setVisibility(VISIBLE);
			history2.setText(history);
			findViewById(R.id.card_history_layout_2).setVisibility(VISIBLE);
		}
	}
	
	public void setHistory3(String history) {
		
		if(history == null) {
			findViewById(R.id.card_history_divider_2).setVisibility(GONE);
			findViewById(R.id.card_history_layout_3).setVisibility(GONE);
		} else {
			findViewById(R.id.card_history_divider_2).setVisibility(VISIBLE);
			history3.setText(history);
			findViewById(R.id.card_history_layout_3).setVisibility(VISIBLE);
		}
	}

	public TextView getHistory1() {
		return history1;
	}
	
	public TextView getHistory2() {
		return history2;
	}
	
	public TextView getHistory3() {
		return history3;
	}
	
	private void initialize(Context context) {
		// Set the content padding
		setContentPadding(18, 10, 20, 10);
		// Enable compat padding to use the same padding on Lollipop and older platforms
		setUseCompatPadding(true);
		// Inflate the card layout
		LayoutInflater  mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInflater.inflate(R.layout.now_history_card, this, true);
		
		// Save a reference to the different TextViews
		history1 = (TextView) findViewById(R.id.card_history_1);
		history2 = (TextView) findViewById(R.id.card_history_2);
		history3 = (TextView) findViewById(R.id.card_history_3);
	}
}
