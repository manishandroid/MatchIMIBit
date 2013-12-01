package com.matchimi;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import static com.matchimi.CommonUtilities.TAG;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.matchimi.availability.HomeAvailabilityActivity;
import com.matchimi.availability.LocationPreferenceActivity;
import com.matchimi.availability.RepeatAvailabilityActivity;
import com.matchimi.options.JobsFragment;
import com.matchimi.options.ProfileFragment;
import com.matchimi.options.ScheduleCalendarFragment;
import com.matchimi.options.ScheduleFragment;
import com.matchimi.registration.LoginActivity;
import com.matchimi.schedule.CalendarScheduleFragment;

public class HomeActivity extends TabSwipeActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Check whether user not logged or logged
		SharedPreferences settings = getSharedPreferences(
				CommonUtilities.PREFS_NAME, Context.MODE_PRIVATE);

		// If user not logged, redirect to Login/Register page
		// FIXME: change default value of login to false to go to login page
		if (settings == null
				|| !settings.getBoolean(CommonUtilities.LOGIN, false)) {
			Intent loginPage = new Intent(this, LoginActivity.class);
			loginPage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(loginPage);
			finish();
		}
		
		setAlarmManager();

		// Intent i = new Intent(this, RepeatAvailabilityActivity.class);
		// startActivity(i);
		// finish();

		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());
		if (status == ConnectionResult.SUCCESS) {

			// inflating Tab
			addTab(getResources().getString(R.string.menu_jobs),
					JobsFragment.class, JobsFragment.createBundle("Fragment 1"));
			// addTab(getResources().getString(R.string.menu_schedule),
			// ScheduleFragment.class,
			// ScheduleFragment.createBundle("Fragment 2"));
			addTab(getResources().getString(R.string.menu_schedule),
					CalendarScheduleFragment.class,
					CalendarScheduleFragment.createBundle("Fragment 2"));
			addTab(getResources().getString(R.string.menu_profile),
					ProfileFragment.class,
					ProfileFragment.createBundle("Fragment 3"));

			// Move to first tab by default
			moveTab(0);

			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
				String page = extras.getString(CommonUtilities.HOMEPAGE_OPTION);
				if (page == CommonUtilities.PAGEPROFILE) {
					moveTab(2);
				}
			}
		} else {
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
					requestCode);
			dialog.show();
		}
	}

	/**
	 * Set alarm manager which running periodic task
	 */
	private void setAlarmManager() {
		Log.d(CommonUtilities.TAG, "Set alarm manager");
		
		Intent i = new Intent(this, MatchimiAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0); 
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.MINUTE, 1);

		
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.cancel(pi); // cancel any existing alarms
		am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60, pi);
		
//		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
//				SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
//				AlarmManager.INTERVAL_DAY, pi);
	}

}
