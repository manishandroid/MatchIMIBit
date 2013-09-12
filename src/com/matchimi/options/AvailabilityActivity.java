package com.matchimi.options;

import static com.matchimi.CommonUtilities.API_GET_AVAILABILITIES_BY_PT_ID;
import static com.matchimi.CommonUtilities.PARAM_PT_ID;
import static com.matchimi.CommonUtilities.SERVERURL;

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
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;

public class AvailabilityActivity extends SherlockActivity {

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

	public static final int RC_EDIT_AVAILABILITY = 30;
	public static final int RC_ADD_AVAILABILITY = 31;

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
		setContentView(R.layout.availability_menu);

		context = this;

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		pt_id = authenticationPref.getString(CommonUtilities.USER_PTID, null);

		adapter = new AvailabilityAdapter(context);
		listview = (ListView) findViewById(R.id.listview);
		listview.setAdapter(adapter);

		progressBar = (ProgressBar) findViewById(R.id.progress);

		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i = new Intent(context, AvailabilityPreview.class);
				i.putExtra("id", pt_id);
				i.putExtra("avail_id", listAvailID.get(arg2));
				i.putExtra("start", listStartTime.get(arg2));
				i.putExtra("end", listEndTime.get(arg2));
				i.putExtra("repeat", listRepeat.get(arg2));
				i.putExtra("location", listLocation.get(arg2));
				i.putExtra("price", listPrice.get(arg2));
				startActivityForResult(i, RC_EDIT_AVAILABILITY);
			}
		});

		loadDate();
	}

	private void loadDate() {
		final String url = SERVERURL + API_GET_AVAILABILITIES_BY_PT_ID + "?" +
						PARAM_PT_ID + "=" + pt_id;
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

				if (jsonStr != null) {
					try {
						JSONArray items = new JSONArray(jsonStr);
						if (items != null && items.length() > 0) {
							SimpleDateFormat formatterDate = new SimpleDateFormat(
									"EE d, MMM", Locale.getDefault());
							SimpleDateFormat formatterTime = new SimpleDateFormat(
									"hh a", Locale.getDefault());
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
										listStartTime.add(startDate);
										listEndTime.add(endDate);
										listRepeat.add(jsonParser.getInt(objs,
												"repeat"));
										listLocation.add(jsonParser.getString(
												objs, "location"));
										listPrice.add(jsonParser.getString(
												objs, "asked_salary"));

										Calendar calStart = generateCalendar(startDate);
										Calendar calEnd = generateCalendar(endDate);
										listDate.add(formatterDate
												.format(calStart.getTime())
												+ "\n"
												+ formatterTime
														.format(calStart
																.getTime())
														.toLowerCase(
																Locale.getDefault())
												+ " - "
												+ formatterTime
														.format(calEnd
																.getTime())
														.toLowerCase(
																Locale.getDefault()));
									}
								} catch (JSONException e) {
									Log.e("Parse Json Object",
											">> " + e.getMessage());
								}
							}
						} else {
							Log.e("Parse Json Object", ">> Array is null");
						}
					} catch (JSONException e1) {
						Log.e("updateUIFromJSON", ">> " + e1.getMessage());
					}
				} else {
					Toast.makeText(context, "jsonObj is Null",
							Toast.LENGTH_SHORT).show();
				}

				adapter.updateList(listDate, listRepeat);

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
				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}

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
		}
		return super.onOptionsItemSelected(item);
	}
}
