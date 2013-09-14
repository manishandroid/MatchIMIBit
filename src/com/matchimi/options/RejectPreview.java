package com.matchimi.options;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;

public class RejectPreview extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences settings = getSharedPreferences(
				CommonUtilities.PREFS_NAME, Context.MODE_PRIVATE);
		if (settings.getInt(CommonUtilities.SETTING_THEME,
				CommonUtilities.THEME_LIGHT) == CommonUtilities.THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}
		setContentView(R.layout.reject_offer_preview);

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		Bundle b = getIntent().getExtras();
		String company = b.getString("company");

		final TextView buttonConfirmSubmit = (TextView) findViewById(R.id.buttonConfirmSubmit);
		buttonConfirmSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent result = new Intent();
				setResult(RESULT_OK, result);
				finish();
			}
		});

		buttonConfirmSubmit.setEnabled(false);
		buttonConfirmSubmit
				.setBackgroundResource(R.drawable.button_reject_offer);

		final CheckBox checkOffer = (CheckBox) findViewById(R.id.checkOffer);
		checkOffer.setText(getResources().getString(R.string.dont_show_offer)
				+ " \"" + company + "\" anymore");
		checkOffer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (checkOffer.isChecked()) {
					buttonConfirmSubmit.setEnabled(true);
					buttonConfirmSubmit
							.setBackgroundResource(R.drawable.button_accept_offer);
				} else {
					buttonConfirmSubmit.setEnabled(false);
					buttonConfirmSubmit
							.setBackgroundResource(R.drawable.button_reject_offer);
				}
			}
		});

		LinearLayout layButton = (LinearLayout) findViewById(R.id.layButton);
		layButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				checkOffer.setChecked(!checkOffer.isChecked());
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent result = new Intent();
			setResult(RESULT_CANCELED, result);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
