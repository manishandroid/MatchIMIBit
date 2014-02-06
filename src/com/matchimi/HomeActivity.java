package com.matchimi;

import static com.matchimi.CommonUtilities.TAG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.matchimi.availability.LocationPreferenceRegionAdapter;
import com.matchimi.guideline.GuidelineMain;
import com.matchimi.ongoingjobs.OngoingJobsFragment;
import com.matchimi.options.JobsFragment;
import com.matchimi.options.ProfileFragment;
import com.matchimi.registration.EditProfile;
import com.matchimi.registration.LoginActivity;
import com.matchimi.schedule.CalendarScheduleFragment;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class HomeActivity extends TabSwipeActivity {
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;

	private String responseString;
	private JSONParser jsonParser = null;
	private String jsonStr = null;
	private ProgressDialog progress;
	private Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Check whether user not logged or logged
		settings = getSharedPreferences(CommonUtilities.PREFS_NAME,
				Context.MODE_PRIVATE);
		context = this;
		
		// If user not logged, redirect to Login/Register page
		// FIXME: change default value of login to false to go to login page
		if (settings == null
				|| !settings.getBoolean(CommonUtilities.IS_FIRSTTIME, false)) {
			Intent intentGuideline = new Intent(this, GuidelineMain.class);
			startActivity(intentGuideline);
			finish();

		} else if (!settings.getBoolean(CommonUtilities.LOGIN, false)) {

			Intent loginPage = new Intent(this, LoginActivity.class);
			loginPage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(loginPage);
			finish();

		} else {
			
			String facebookID = settings.getString(
					CommonUtilities.USER_FACEBOOK_ID, null);
			if (facebookID != null && facebookID.length() > 0) {
				Log.d(CommonUtilities.TAG,
						"Activate facebook friends periodic task");
				setAlarmManager();
			}

			// Intent i = new Intent(this, RepeatAvailabilityActivity.class);
			// startActivity(i);
			// finish();

			int status = GooglePlayServicesUtil
					.isGooglePlayServicesAvailable(getApplicationContext());

			if (status == ConnectionResult.SUCCESS) {
				// inflating Tab
				addTab(getResources().getString(R.string.menu_jobs),
						JobsFragment.class,
						JobsFragment.createBundle("Fragment 1"));
				// addTab(getResources().getString(R.string.menu_schedule),
				// ScheduleFragment.class,
				// ScheduleFragment.createBundle("Fragment 2"));
				addTab(getResources().getString(R.string.menu_urgent_jobs),
						OngoingJobsFragment.class,
						OngoingJobsFragment.createBundle("Fragment 2"));
				addTab(getResources().getString(R.string.menu_schedule),
						CalendarScheduleFragment.class,
						CalendarScheduleFragment.createBundle("Fragment 3"));
				addTab(getResources().getString(R.string.menu_profile),
						ProfileFragment.class,
						ProfileFragment.createBundle("Fragment 4"));

				// Move to first tab by default
				moveTab(0);

				Intent intent = getIntent();
				Bundle extras = intent.getExtras();
				if (extras != null) {
					if (extras.getString(CommonUtilities.HOMEPAGE_OPTION) != null) {
						String page = extras
								.getString(CommonUtilities.HOMEPAGE_OPTION);
						if (page == CommonUtilities.PAGEPROFILE) {
							moveTab(2);
						}
					} else if (extras.getBoolean("is_expired", false)) {

						CharSequence text = context
								.getString(R.string.expired_jobs);
						int duration = Toast.LENGTH_LONG;

						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
					}
				}
			} else {
				int requestCode = 10;
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status,
						this, requestCode);
				dialog.show();
			}

			loadStaticData();
		}
	}

	
	/**
	 * Loading locations API data
	 */
	private void loadStaticData() {
		Log.d(TAG, "Loading static data ...");

		final String url = CommonUtilities.SERVERURL
				+ CommonUtilities.API_GET_STATIC_DATA;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				responseString = null;

				if (jsonStr != null) {
					saveGender(jsonStr);

				} else {
					Toast.makeText(context.getApplicationContext(),
							getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context,
				getString(R.string.preparing_data),
				getString(R.string.preparing_data), true, false);

		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}

			}
		}.start();
	}

	/**
	 * Saving Gender static data
	 * 
	 * @param jsonStr
	 */
	private void saveGender(String jsonStr) {
		try {
			JSONObject dataObj = new JSONObject(jsonStr);
			JSONArray jsonArray = dataObj
					.getJSONArray(CommonUtilities.PARAM_STATIC_GENDERS);

			editor = settings.edit();
			editor.putString(CommonUtilities.API_CACHE_GENDERS,
					jsonArray.toString());
			editor.commit();

			saveNRICType(jsonStr);

		} catch (JSONException e) {
			NetworkUtils.connectionHandler(HomeActivity.this, jsonStr,
					e.getMessage());
			Log.e(TAG, "Error while get genders >>> " + e.getMessage());
		}

	}

	private void saveNRICType(String jsonStr) {

		try {
			JSONObject dataObj = new JSONObject(jsonStr);
			JSONArray jsonArray = dataObj
					.getJSONArray(CommonUtilities.PARAM_STATIC_NRIC_TYPES);

			editor = settings.edit();
			editor.putString(CommonUtilities.API_CACHE_NRIC_TYPES,
					jsonArray.toString());
			editor.commit();

			saveSchool(jsonStr);

		} catch (JSONException e) {
			NetworkUtils.connectionHandler(HomeActivity.this, jsonStr,
					e.getMessage());
			Log.e(TAG, "Error get ic types >>> " + e.getMessage());
		}

	}

	private void saveSchool(String jsonStr) {
		try {
			JSONObject dataObj = new JSONObject(jsonStr);
			JSONArray jsonArray = dataObj
					.getJSONArray(CommonUtilities.PARAM_STATIC_SCHOOLS);

			editor = settings.edit();
			editor.putString(CommonUtilities.API_CACHE_SCHOOLS,
					jsonArray.toString());
			editor.commit();

			saveLocation(jsonStr);

		} catch (Exception e) {
			NetworkUtils.connectionHandler(HomeActivity.this, jsonStr,
					e.getMessage());
			Log.e(TAG, "Error school >>> " + e.getMessage());
		}
	}

	private void saveLocation(String jsonStr) {
		try {
			JSONObject dataObj = new JSONObject(jsonStr);
			JSONArray jsonArray = dataObj
					.getJSONArray(CommonUtilities.PARAM_STATIC_LOCATIONS);

			editor = settings.edit();
			editor.putString(CommonUtilities.API_CACHE_LOCATIONS,
					jsonArray.toString());
			editor.commit();

			saveDays(jsonStr);

		} catch (Exception e) {
			NetworkUtils.connectionHandler(HomeActivity.this, jsonStr,
					e.getMessage());
			Log.e(TAG, "Error school >>> " + e.getMessage());
		}
	}

	private void saveDays(String jsonStr) {
		try {
			JSONObject dataObj = new JSONObject(jsonStr);
			JSONArray jsonArray = dataObj
					.getJSONArray(CommonUtilities.PARAM_STATIC_DAYS);

			editor = settings.edit();
			editor.putString(CommonUtilities.API_CACHE_DAYS,
					jsonArray.toString());
			editor.commit();

			saveJobFunctions(jsonStr);

		} catch (Exception e) {
			NetworkUtils.connectionHandler(HomeActivity.this, jsonStr,
					e.getMessage());
			Log.e(TAG, "Error school >>> " + e.getMessage());
		}
	}

	private void saveJobFunctions(String jsonStr) {
		try {
			JSONObject dataObj = new JSONObject(jsonStr);
			JSONArray jsonArray = dataObj
					.getJSONArray(CommonUtilities.PARAM_STATIC_JOB_FUNCTIONS);

			editor = settings.edit();
			editor.putString(CommonUtilities.API_CACHE_JOB_FUNCTIONS,
					jsonArray.toString());
			editor.commit();

		} catch (Exception e) {
			NetworkUtils.connectionHandler(HomeActivity.this, jsonStr,
					e.getMessage());
			Log.e(TAG, "Error school >>> " + e.getMessage());
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
		am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				1000 * 60 * 60, pi);

		// am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
		// SystemClock.elapsedRealtime() +
		// AlarmManager.INTERVAL_FIFTEEN_MINUTES,
		// AlarmManager.INTERVAL_DAY, pi);
	}

}
