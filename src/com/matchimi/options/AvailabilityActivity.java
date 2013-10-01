package com.matchimi.options;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class AvailabilityActivity extends SherlockFragmentActivity {

	private Context context;
	private String pt_id;

	private JSONParser jsonParser = null;
	private String jsonStr = null;
	private boolean availabilityChange = false;

	private AvailabilityAdapter adapter;
	private ListView listview;
	private ProgressBar progressBar;

	private List<String> listAvailID = null;
	private List<String> listStartTime = null;
	private List<String> listEndTime = null;
	private List<String> listDate = null;
	private List<Integer> listRepeat = null;
	private List<String> listLocation = null;
	private List<String> listPrice = null;
	private List<Boolean> listFreeze = null;
	
	public static final int RC_EDIT_AVAILABILITY = 30;
	public static final int RC_ADD_AVAILABILITY = 31;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences settings = getSharedPreferences(CommonUtilities.PREFS_NAME, 
				Context.MODE_PRIVATE);
		if (settings.getInt(CommonUtilities.SETTING_THEME, CommonUtilities.THEME_LIGHT) == CommonUtilities.THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}
		setContentView(R.layout.availability_menu);

		context = this;

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		pt_id = settings.getString(CommonUtilities.USER_PTID, null);
		adapter = new AvailabilityAdapter(context);
		
		// Build list of availibilities
		listview = (ListView) findViewById(R.id.listview);
		listview.setAdapter(adapter);

		progressBar = (ProgressBar) findViewById(R.id.progress);

		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i = new Intent(context, AvailabilityPreview.class);
				i.putExtra("pt_id", pt_id);
				i.putExtra("avail_id", listAvailID.get(arg2));
				i.putExtra("start", listStartTime.get(arg2));
				i.putExtra("end", listEndTime.get(arg2));
				i.putExtra("repeat", listRepeat.get(arg2));
				i.putExtra("location", listLocation.get(arg2));
				i.putExtra("price", listPrice.get(arg2));
				i.putExtra("is_frozen", listFreeze.get(arg2));
				
				startActivityForResult(i, RC_EDIT_AVAILABILITY);
			}
		});

		loadDate();
	}

	/**
	 * Loading all unmatched availabilities
	 */
	private void loadDate() {
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_AVAILABILITIES_BY_PT_ID + "?" +
				CommonUtilities.PARAM_PT_ID + "=" + pt_id;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				listAvailID = new ArrayList<String>();
				listStartTime = new ArrayList<String>();
				listEndTime = new ArrayList<String>();
				listRepeat = new ArrayList<Integer>();
				listLocation = new ArrayList<String>();
				listDate = new ArrayList<String>();
				listPrice = new ArrayList<String>();
				listFreeze = new ArrayList<Boolean>();
				
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

										String convertStartDate = CommonUtilities.AVAILABILTY_DATETIME.format(calStart.getTime());
										String convertEndDate = CommonUtilities.AVAILABILTY_DATETIME.format(calEnd.getTime());
										
										listStartTime.add(convertStartDate);
										listEndTime.add(convertEndDate);
										
										listRepeat.add(jsonParser.getInt(objs,
												"repeat"));
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
								Log.d(CommonUtilities.TAG, "Availability Results >>>\n " + jsonStr.toString());
							}
						} else {
							Log.e(CommonUtilities.TAG, "Array is null");
						}
					} catch (JSONException e1) {
						NetworkUtils.connectionHandler(context, jsonStr, e1.getMessage());
					}
				} else {
					Toast.makeText(context, getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}

				adapter.updateList(listDate, listRepeat, listFreeze);

				listview.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
			}
		};

		listview.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);
				Log.d(CommonUtilities.TAG, "Result from " + url + " >>>\n" + jsonStr);
				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}

	/**
	 * Convert start / end datetime String into calendar object
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.ab_availability, menu);

		MenuItem reload = menu.findItem(R.id.menu_reload);
		MenuItem add = menu.findItem(R.id.menu_add_availability);
		SharedPreferences settings = getSharedPreferences(CommonUtilities.PREFS_NAME, 
				Context.MODE_PRIVATE);
		if (settings.getInt(CommonUtilities.SETTING_THEME, CommonUtilities.THEME_LIGHT) == CommonUtilities.THEME_LIGHT) {
			reload.setIcon(R.drawable.navigation_refresh);
			add.setIcon(R.drawable.add);
		} else {
			reload.setIcon(R.drawable.navigation_refresh_dark);
			add.setIcon(R.drawable.add_dark);
		}
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			availabilityChange = true;
			loadDate();
		}
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
		case R.id.menu_add_availability:
			i = new Intent(context, CreateAvailability.class);
			i.putExtra("id", pt_id);
			i.putExtra("update", false);
			startActivityForResult(i, RC_ADD_AVAILABILITY);
			break;
		case R.id.menu_reload:
			loadDate();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
