package com.matchimi.options;

import java.util.ArrayList;
import java.util.List;

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
	private boolean[] selected = null;
	
	private List<String> listRequirement = new ArrayList<String>();
	private List<String> listOptional = new ArrayList<String>();

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
		
		// Loading mandantory requirement 
		if (b.containsKey("mandatory_requirements")) {
			String rawRequirement = b.getString("mandatory_requirements");

			if(rawRequirement != null) {
				listRequirement = ProcessDataUtils.convertStringToListWithSpace(rawRequirement);
			}
		}
		
		// Loading optional requirements
		if (b.containsKey("optional_requirements")) {
			String rawOptional = b.getString("optional_requirements");

			if(rawOptional != null) {
				listOptional = ProcessDataUtils.convertStringToListWithSpace(rawOptional);
			}
		}

		TextView textOptional = (TextView) findViewById(R.id.textOptional);
		if (listOptional == null || listOptional.size() == 0) {
			textOptional.setText("");
		} else {
			for(String optional : listOptional) {
				textOptional.setText(optional);
			}
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
		if (listRequirement != null && listRequirement.size() > 0) {
			
			buttonAcceptSubmit.setEnabled(false);
			buttonAcceptSubmit.setBackgroundResource(R.drawable.button_reject_offer);

			selected = new boolean[listRequirement.size()];
			for (int i = 0; i < listRequirement.size(); i++) {
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
		textView.setText(listRequirement.get(position));
		
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
