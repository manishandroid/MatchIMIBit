package com.matchimi.options;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.matchimi.R;

public class CancelPreview extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cancel_offer_preview);

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		final EditText inputReason = (EditText) findViewById(R.id.inputReason);
		final TextView buttonCancel = (TextView) findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent result = new Intent();
				result.putExtra("reason", inputReason.getText().toString()
						.trim());
				setResult(RESULT_OK, result);
				finish();
			}
		});

		final TextView buttonKepp = (TextView) findViewById(R.id.buttonKepp);
		buttonKepp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent result = new Intent();
				setResult(RESULT_CANCELED, result);
				finish();
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
