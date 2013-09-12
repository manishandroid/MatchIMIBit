package com.matchimi.options;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;

public class CancelPreview extends SherlockActivity {
	private List<String> listReason;
	private boolean receiveOffer = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences authenticationPref = getSharedPreferences(
				CommonUtilities.APP_SETTING, Context.MODE_PRIVATE);
		if (authenticationPref.getInt(CommonUtilities.SETTING_THEME,
				CommonUtilities.THEME_LIGHT) == CommonUtilities.THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}
		setContentView(R.layout.cancel_offer_preview);

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

//		final EditText inputReason = (EditText) findViewById(R.id.inputReason);
		final TextView buttonCancel = (TextView) findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				final ArrayList<Integer> mSelectedItems = new ArrayList<Integer>();
				final ArrayList<CharSequence> selectedReason = new ArrayList<CharSequence>();

				AlertDialog.Builder builder = new AlertDialog.Builder(
						CancelPreview.this);

				listReason = new ArrayList<String>();
				listReason.add("Don’t like the employer");				
				listReason.add("Don’t like the job function");
				listReason.add("Don’t like the location");
				
				// Set the dialog title
				builder.setTitle("Your Reason")
						.setMultiChoiceItems(
								listReason
										.toArray(new CharSequence[listReason.size()]),
								null,
								new DialogInterface.OnMultiChoiceClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which, boolean isChecked) {
										if (isChecked) {
											mSelectedItems.add(which);
										} else if (mSelectedItems.contains(which)) {
											mSelectedItems.remove(Integer
													.valueOf(which));
										}
									}
								})
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										selectedReason.clear();
										for (int i = 0; i < mSelectedItems.size(); i++) {
											selectedReason.add(listReason
													.get(mSelectedItems.get(i)));
										}

										// Convert ArrayList into String
										// comma-separated
										String selectedReasonList = selectedReason
												.toString();
										String selectedReasonSet = selectedReasonList
												.substring(
														1,
														selectedReasonList.length() - 1)
												.replace(", ", ", ");
										
										dialogReceiveOffer(selectedReasonSet);
									}
								});

				Dialog dialog = builder.create();
				dialog.show();

				
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

	private void dialogReceiveOffer(final String selectedReasonSet) {
		 // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);        
        
        builder.setMessage(R.string.cancel_offer_receive)        
               .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   receiveOffer = true;
                       submitCancelJob(selectedReasonSet, receiveOffer);                	   
                   }
               })
               .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // ReceiveOffer default value is false
                       submitCancelJob(selectedReasonSet, receiveOffer);                	   
                   }
               });
        builder.show();

	}
	
	private void submitCancelJob(String selectedReasonSet, boolean receiveOffer) {
		Intent result = new Intent();
		result.putExtra("reason", selectedReasonSet);
		setResult(RESULT_OK, result);
		finish();
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
