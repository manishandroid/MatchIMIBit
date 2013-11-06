package com.matchimi.availability;

import static com.matchimi.CommonUtilities.TAG;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.Variables;
import com.matchimi.options.CreateAvailability;
import com.matchimi.swipecalendar.CalendarFragment;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class HomeAvailabilityActivity extends SherlockFragmentActivity {

	private Context context;
	private String pt_id;
	private boolean availabilityChange = false;
	private ProgressBar progressBar;

	private TextView buttonAddBulk;
	private TextView buttonAddDaily;
	private RelativeLayout availabilityCalendarLayout;

	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private List<String> listAvailID = null;
	private List<String> listStartTime = null;
	private List<String> listEndTime = null;
	private List<String> listDate = null;
	private List<String> listRepeat = null;
	private List<String> listLocation = null;
	private List<String> listPrice = null;
	private List<Boolean> listFreeze = null;
	private List<Date> listAvailabilityDate = null;

	public static final int RC_EDIT_AVAILABILITY = 30;
	public static final int RC_ADD_AVAILABILITY = 31;

	private CalendarFragment calendarFragment;

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
		setContentView(R.layout.availability_home);

		context = this;

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		pt_id = settings.getString(CommonUtilities.USER_PTID, null);

		progressBar = (ProgressBar) findViewById(R.id.progress);
		progressBar.setVisibility(View.GONE);

		buttonAddBulk = (TextView) findViewById(R.id.buttonAddBulkAvailability);
		buttonAddBulk.setOnClickListener(addBulkAvailability);

		buttonAddDaily = (TextView) findViewById(R.id.buttonAddDailyAvailability);
		buttonAddDaily.setOnClickListener(addDailyAvailability);

		availabilityCalendarLayout = (RelativeLayout) findViewById(R.id.layout_calendar_wrapper);

		loadDate();
		
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMessageReceiver, new IntentFilter(CommonUtilities.CREATE_AVAILABILITY_BROADCAST));
	}

	// Our handler for received Intents. This will be called whenever an Intent
	// with an action named "custom-event-name" is broadcasted.
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get extra data included in the Intent
			String message = intent.getStringExtra(CommonUtilities.CREATE_AVAILABILITY_BROADCAST);
			Log.d(TAG, "Got message: " + message);
			
			if (message != null && message == "true") {
				loadDate();
			}
		}
	};

	/**
	 * Loading all unmatched availabilities
	 */
	private void loadDate() {
		final String url = CommonUtilities.SERVERURL
				+ CommonUtilities.API_GET_AVAILABILITIES_BY_PT_ID + "?"
				+ CommonUtilities.PARAM_PT_ID + "=" + pt_id;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				listAvailID = new ArrayList<String>();
				listStartTime = new ArrayList<String>();
				listEndTime = new ArrayList<String>();
				listRepeat = new ArrayList<String>();
				listLocation = new ArrayList<String>();
				listDate = new ArrayList<String>();
				listPrice = new ArrayList<String>();
				listFreeze = new ArrayList<Boolean>();
				listAvailabilityDate = new ArrayList<Date>();

				if (jsonStr != null) {
					try {
						JSONArray items = new JSONArray(jsonStr);
						if (items != null && items.length() > 0) {
							for (int i = 0; i < items.length(); i++) {
								/* get all json items, and put it on list */
								try {
									JSONObject objs = items.getJSONObject(i);
									objs = objs.getJSONObject("availabilities");
									if (objs != null) {
										listAvailID.add(jsonParser.getString(
												objs, "avail_id"));
										String startDate = jsonParser
												.getString(objs,
														"start_date_time");
										String endDate = jsonParser.getString(
												objs, "end_date_time");

										Calendar calStart = generateCalendar(startDate);
										Calendar calEnd = generateCalendar(endDate);

										String convertStartDate = CommonUtilities.AVAILABILTY_DATETIME
												.format(calStart.getTime());
										String convertEndDate = CommonUtilities.AVAILABILTY_DATETIME
												.format(calEnd.getTime());

										listAvailabilityDate.add(calStart
												.getTime());

										listStartTime.add(convertStartDate);
										listEndTime.add(convertEndDate);

										listRepeat.add(jsonParser.getString(
												objs, "repeat"));
										listLocation.add(jsonParser.getString(
												objs, "location"));
										listPrice.add(jsonParser.getString(
												objs, "asked_salary"));
										listFreeze.add(jsonParser.getBoolean(
												objs, "is_frozen"));

										listDate.add(CommonUtilities.AVAILABILITY_DATE
												.format(calStart.getTime())
												+ "\n"
												+ CommonUtilities.AVAILABILITY_TIME
														.format(calStart
																.getTime())
														.toLowerCase(
																Locale.getDefault())
												+ " - "
												+ CommonUtilities.AVAILABILITY_TIME
														.format(calEnd
																.getTime())
														.toLowerCase(
																Locale.getDefault()));
									}
								} catch (JSONException e) {
									Log.e(CommonUtilities.TAG,
											"Error Array >> " + e.getMessage());
								}
								// Log.d(CommonUtilities.TAG,
								// "Availability Results >>>\n "
								// + jsonStr.toString());
							}
						} else {
							Log.e(CommonUtilities.TAG, "Array is null");
						}

					} catch (JSONException e1) {
						NetworkUtils.connectionHandler(context, jsonStr,
								e1.getMessage());
					}
				} else {
					Toast.makeText(context, getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}

				buildAvailabilityCalendar();

				availabilityCalendarLayout.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
			}
		};

		availabilityCalendarLayout.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);

		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);
				Log.d(CommonUtilities.TAG, "Result from " + url + " >>>\n"
						+ jsonStr);
				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}

	private void buildAvailabilityCalendar() {
		List<String> occupiedList = new ArrayList();
		SimpleDateFormat occupiedDate = new SimpleDateFormat(getResources()
				.getString(R.string.datetime_simple_format));

		for (Date availabilityDate : listAvailabilityDate) {
			occupiedList.add(occupiedDate.format(availabilityDate));
		}

		Bundle bundle = new Bundle();
		bundle.putBoolean(CommonUtilities.IS_AVAILABILITY_CALENDAR, true);
		bundle.putStringArray(CommonUtilities.OCCUPIED_DATES,
				occupiedList.toArray(new String[occupiedList.size()]));
		bundle.putString(CommonUtilities.PARAM_PT_ID, pt_id);
		bundle.putStringArray(CommonUtilities.AVAILABILITY_LIST_AVAIL_ID,
				listAvailID.toArray(new String[listAvailID.size()]));
		bundle.putStringArray(CommonUtilities.AVAILABILITY_LIST_START_TIME,
				listStartTime.toArray(new String[listStartTime.size()]));
		bundle.putStringArray(CommonUtilities.AVAILABILITY_LIST_END_TIME,
				listEndTime.toArray(new String[listEndTime.size()]));
		bundle.putStringArray(CommonUtilities.AVAILABILITY_LIST_REPEAT,
				listRepeat.toArray(new String[listRepeat.size()]));
		bundle.putStringArray(CommonUtilities.AVAILABILITY_LIST_LOCATION,
				listLocation.toArray(new String[listLocation.size()]));

		calendarFragment = new CalendarFragment();
		calendarFragment.setArguments(bundle);

		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		calendarFragment.setAvailabilityData();
		t.replace(R.id.calendar_wrapper, calendarFragment);
		t.commit();
	}

	/**
	 * Convert start / end datetime String into calendar object
	 * 
	 * @param str
	 * @return
	 */
	private Calendar generateCalendar(String str) {
		Calendar calRes = new GregorianCalendar(Integer.parseInt(str.substring(
				0, 4)), Integer.parseInt(str.substring(5, 7)) - 1,
				Integer.parseInt(str.substring(8, 10)), Integer.parseInt(str
						.substring(11, 13)), Integer.parseInt(str.substring(14,
						16)), Integer.parseInt(str.substring(17, 19)));

		return calRes;
	}

	/**
	 * Daily availability listener
	 */
	private OnClickListener addDailyAvailability = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent createAvailabilityIntent = new Intent(context,
					CreateAvailability.class);
			createAvailabilityIntent.putExtra("id", pt_id);
			createAvailabilityIntent.putExtra("update", false);
			startActivityForResult(createAvailabilityIntent,
					Variables.RC_ADD_AVAILABILITY);
			finish();
		}
	};

	/**
	 * Bulk availability listener
	 */
	private OnClickListener addBulkAvailability = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent createAvailabilityIntent = new Intent(context,
					BulkAvailabilityActivity.class);
			startActivity(createAvailabilityIntent);
			finish();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.ab_home_availability, menu);

		MenuItem reload = menu.findItem(R.id.menu_reload);
		SharedPreferences settings = getSharedPreferences(
				CommonUtilities.PREFS_NAME, Context.MODE_PRIVATE);

		if (settings.getInt(CommonUtilities.SETTING_THEME,
				CommonUtilities.THEME_LIGHT) == CommonUtilities.THEME_LIGHT) {
			reload.setIcon(R.drawable.navigation_refresh);
		} else {
			reload.setIcon(R.drawable.navigation_refresh_dark);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {

		case android.R.id.home:
			i = new Intent();
			if (availabilityChange) {
				setResult(RESULT_OK, i);
			} else {
				setResult(RESULT_CANCELED, i);
			}
			finish();
			break;
		case R.id.menu_reload:
			loadDate();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		try {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(
					mMessageReceiver);
		} catch (IllegalArgumentException e) {
			// Nothing
		}

		super.onDestroy();
	}

}