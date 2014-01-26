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
	private TextView siteDescriptionView;
	
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
	
	public void setSiteDescriptionText(String description) {
		
		if(description.equals("")) {
			siteHeaderView.setVisibility(GONE);
			siteDescriptionView.setVisibility(GONE);
		} else {
			siteDescriptionView.setText(description);
		}
	}

	public TextView getSiteDescriptionView() {
		return siteDescriptionView;
	}
	
	public TextView getSiteHeaderView() {
		return siteHeaderView;
	}
	
	private void initialize(Context context) {
		// Inflate the card layout
		LayoutInflater  mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInflater.inflate(R.layout.now_card, this, true);
		
		// Save a reference to the different TextViews
		siteHeaderView = (TextView)findViewById(R.id.card_site_header);
		siteDescriptionView = (TextView)findViewById(R.id.card_site_description);
	}
}
