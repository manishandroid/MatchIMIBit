package com.matchimi.options;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.ProcessDataUtils;
import static com.matchimi.CommonUtilities.TAG;

public class RequirementsDetail extends SherlockActivity {

	private Context context;
	private String requirement = null;
	private String[] requirementArray = null;
	private boolean[] selected = null;

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
		setContentView(R.layout.requirements_detail);

		context = this;

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		
		Bundle b = getIntent().getExtras();
		
		if (b.containsKey("requirement")) {
			Log.d(TAG, "requirement " + requirement);

			requirement = ProcessDataUtils.parseRequirement(b.getString("requirement"));
			Log.d(TAG, "requirement " + requirement);
			
			if(requirement.length() > 0) {
				requirementArray = requirement.split("\n");
			}
		}
		
		String optional = ProcessDataUtils.parseRequirement(b.getString("optional"));

		TextView textOptional = (TextView) findViewById(R.id.textOptional);
		if (optional == null || optional.length() == 0) {
			textOptional.setText("");
		} else {
			textOptional.setText(optional);
		}

		final TextView buttonAcceptSubmit = (TextView) findViewById(R.id.buttonAcceptSubmit);
		buttonAcceptSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent result = new Intent();
				setResult(RESULT_OK, result);
				finish();
			}
		});

		LinearLayout layoutMandatory = (LinearLayout) findViewById(R.id.layoutMandatory);
		if (requirementArray != null && requirementArray.length > 0) {
			
			buttonAcceptSubmit.setEnabled(false);
			buttonAcceptSubmit.setBackgroundResource(R.drawable.button_reject_offer);

			selected = new boolean[requirementArray.length];
			for (int i = 0; i < requirementArray.length; i++) {
				selected[i] = false;
				layoutMandatory.addView(generateView(i, buttonAcceptSubmit));
			}
		} else {
			TextView textView = new TextView(context);
			textView.setText("");
			layoutMandatory.addView(textView);
		}
	}

	private View generateView(final int position,
			final TextView buttonAcceptSubmit) {
		RelativeLayout layView = new RelativeLayout(context);
		layView.setPadding(2, 6, 2, 6);

		TextView textView = new TextView(context);
		textView.setText(requirementArray[position]);
		
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		textView.setLayoutParams(params);

		final CheckBox checkBox = new CheckBox(context);
		params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		checkBox.setLayoutParams(params);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				selected[position] = checkBox.isChecked();
				for (int i = 0; i < selected.length; i++) {
					if (selected[i]) {
						buttonAcceptSubmit.setEnabled(true);
						buttonAcceptSubmit
								.setBackgroundResource(R.drawable.button_accept_offer);
					} else {
						buttonAcceptSubmit.setEnabled(false);
						buttonAcceptSubmit
								.setBackgroundResource(R.drawable.button_reject_offer);
						break;
					}
				}
			}
		});

		layView.addView(textView);
		layView.addView(checkBox);
		layView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				checkBox.setChecked(!checkBox.isChecked());
			}
		});

		return layView;
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
