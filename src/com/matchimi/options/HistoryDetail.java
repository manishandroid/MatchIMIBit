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

import android.app.ProgressDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;

public class HistoryDetail extends SherlockActivity {

	private Context context;
	private String pt_id;

	private ProgressDialog progress;

	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private List<String> listAvailID = null;
	private List<String> listPrice = null;
	private List<String> listAddress = null;
	private List<String> listCompany = null;
	private List<String> listSchedule = null;
	private List<String> listPoint = null;
	private List<String> listDescription = null;
	private List<String> listRequirement = null;
	private List<String> listOptional = null;

	private int totalHours = 0;
	private int totalEarning = 0;

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
		pt_id = authenticationPref.getString(CommonUtilities.USER_PTID, null);

		setContentView(R.layout.history_detail);

		context = this;

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		TextView textTotalHours = (TextView) findViewById(R.id.totalHours);
		TextView textTotalEarning = (TextView) findViewById(R.id.totalEarning);
		textTotalHours.setText("0 Hrs");
		textTotalEarning.setText("$0");

		final JobAdapter adapter = new JobAdapter(context);
		final ListView listview = (ListView) findViewById(R.id.joblistview);
		listview.setAdapter(adapter);

		final String url = "http://matchimi.buuukapps.com/get_past_accepted_job_offers?pt_id="
				+ pt_id;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				listAvailID = new ArrayList<String>();
				listPrice = new ArrayList<String>();
				listAddress = new ArrayList<String>();
				listCompany = new ArrayList<String>();
				listSchedule = new ArrayList<String>();
				listPoint = new ArrayList<String>();
				listDescription = new ArrayList<String>();
				listRequirement = new ArrayList<String>();
				listOptional = new ArrayList<String>();

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
									objs = objs.getJSONObject("sub_slots");
									if (objs != null) {
										listAvailID.add(jsonParser.getString(
												objs, "avail_id"));
										String price = ""
												+ jsonParser.getDouble(objs,
														"offered_salary");
										if (Integer
												.parseInt(price.substring(price
														.indexOf(".") + 1)) == 0) {
											price = price.substring(0,
													price.indexOf("."));
											totalEarning += Integer
													.parseInt(price);
										}
										listPrice
												.add("<font color='#A4A9AF'>$</font><big><big><big><big><font color='#276289'>"
														+ price
														+ "</font></big></big></big></big><font color='#A4A9AF'>/hr</font>");
										listAddress.add(jsonParser.getString(
												objs, "address"));
										listCompany.add(jsonParser.getString(
												objs, "company_name"));
										listRequirement
												.add(jsonParser.getString(objs,
														"requirements"));
										listDescription
												.add(jsonParser.getString(objs,
														"description"));
										listOptional.add(jsonParser.getString(
												objs, "optional"));
										listPoint.add(jsonParser.getString(
												objs, "points"));
										String startDate = jsonParser
												.getString(objs,
														"start_date_time");
										String endDate = jsonParser.getString(
												objs, "end_date_time");
										Calendar calStart = generateCalendar(startDate);
										Calendar calEnd = generateCalendar(endDate);
										listSchedule.add(formatterDate
												.format(calStart.getTime())
												+ "\n"
												+ formatterTime.format(calStart
														.getTime())
												+ " - "
												+ formatterTime.format(calEnd
														.getTime()));
										totalHours += (calEnd
												.get(Calendar.HOUR_OF_DAY) - calStart
												.get(Calendar.HOUR_OF_DAY));
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

				updateTotals();
				adapter.updateList(listPrice, listAddress, listCompany,
						listSchedule, null);
			}
		};

		progress = ProgressDialog.show(context, "History",
				"Loading job history...", true, false);
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

		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i = new Intent(context, JobDetails.class);
				i.putExtra("price", listPrice.get(arg2));
				i.putExtra("date", listSchedule.get(arg2));
				i.putExtra("place",
						listCompany.get(arg2) + "\n" + listAddress.get(arg2));
				i.putExtra("expire", "DONE");
				i.putExtra("description", listDescription.get(arg2));
				i.putExtra("requirement", listRequirement.get(arg2));
				i.putExtra("optional", listOptional.get(arg2));
				i.putExtra("id", listAvailID.get(arg2));
				i.putExtra("type", "past");
				startActivity(i);
			}
		});
	}

	protected void updateTotals() {
		TextView textTotalHours = (TextView) findViewById(R.id.totalHours);
		TextView textTotalEarning = (TextView) findViewById(R.id.totalEarning);
		textTotalHours.setText(totalHours + " Hrs");
		textTotalEarning.setText("$" + totalEarning);
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
