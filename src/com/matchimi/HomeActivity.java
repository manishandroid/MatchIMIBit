package com.matchimi;

import static com.matchimi.CommonUtilities.HOMEPAGE_OPTION;
import static com.matchimi.CommonUtilities.PAGEPROFILE;
import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.USER_FIRSTNAME;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.matchimi.options.JobsFragment;
import com.matchimi.options.ProfileFragment;
import com.matchimi.options.ScheduleFragment;
import com.matchimi.registration.LoginActivity;

public class HomeActivity extends TabSwipeActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Check if user not logged
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

		// If user not logged, redirect to Login/Register page
		// FIXME: change default value of login to false to go to login page
		if (settings == null
				|| !settings.getBoolean(CommonUtilities.LOGIN, false)) {
			Intent loginPage = new Intent(this, LoginActivity.class);
			loginPage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(loginPage);
			finish();
		}

		// inflating Tab
		addTab(getResources().getString(R.string.menu_jobs),
				JobsFragment.class, JobsFragment.createBundle("Fragment 1"));
		addTab(getResources().getString(R.string.menu_schedule),
				ScheduleFragment.class,
				ScheduleFragment.createBundle("Fragment 2"));
		addTab(getResources().getString(R.string.menu_profile),
				ProfileFragment.class,
				ProfileFragment.createBundle("Fragment 3"));

		moveTab(0);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Log.d(TAG, "Incoming intent");

			String page = extras.getString(HOMEPAGE_OPTION);
			if (page == PAGEPROFILE) {
				Log.d(TAG, "USER FIRSTNAME " + extras.getString(USER_FIRSTNAME));
				moveTab(2);
			}
		}
	}

}
