package com.matchimi.registration;


import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;

public class EditProfile extends SherlockActivity {

	private Context context;

	private List<String> listGender;
	private List<String> listGenderId;
	private List<String> listNRICType;
	private List<String> listNRICTypeId;
	private List<String> listSkill;
	private List<String> listSkillId;
	private List<String> listSkillDesc;
	private List<Integer> listSelectedItems;

	private ProgressDialog progress;
	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private Bundle bundleExtras;
	
	private SharedPreferences settings;
	private RadioGroup genderView;

	private String pt_id = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		settings = getSharedPreferences(CommonUtilities.PREFS_NAME, Context.MODE_PRIVATE);
		if (settings.getInt(CommonUtilities.SETTING_THEME,
				CommonUtilities.THEME_LIGHT) == CommonUtilities.THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		
		setContentView(R.layout.edit_profile);

		context = this;

		bundleExtras = getIntent().getExtras();
		pt_id = settings.getString(CommonUtilities.USER_PTID, null);

		if (bundleExtras != null) {
			// TODO
		}

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent result = new Intent();
			setResult(RESULT_CANCELED, result);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
