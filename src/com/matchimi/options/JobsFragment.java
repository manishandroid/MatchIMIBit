package com.matchimi.options;

import static com.matchimi.CommonUtilities.PREFS_NAME;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class JobsFragment extends Fragment {

	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private JobAdapter adapter;
	private ListView listview;
	private ProgressBar progressBar;

	private List<String> listAddress = null;
	private List<String> listAvailID = null;
	private List<String> listPrice = null;
	private List<String> listCompany = null;
	private List<String> listSchedule = null;
	private List<String> listTimeLeft = null;
	private List<String> listDescription = null;
	private List<String> listRequirement = null;
	private List<String> listOptional = null;
	private List<String> listLocation = null;
	
	private String pt_id = null;

	public static final String EXTRA_TITLE = "title";
	public static final int RC_JOB_DETAIL = 10;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.jobs_menu, container, false);

		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		pt_id = settings.getString(CommonUtilities.USER_PTID, null);
		
		adapter = new JobAdapter(getActivity());
		listview = (ListView) view.findViewById(R.id.joblistview);
		listview.setAdapter(adapter);
		progressBar = (ProgressBar) view.findViewById(R.id.progress);

		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i = new Intent(getActivity(), JobDetails.class);
				i.putExtra("price", listPrice.get(arg2));
				i.putExtra("date", listSchedule.get(arg2));
				i.putExtra("place",
						listCompany.get(arg2) + "\n" + listAddress.get(arg2));
				i.putExtra("expire", listTimeLeft.get(arg2));
				i.putExtra("description", listDescription.get(arg2));
				i.putExtra("requirement", listRequirement.get(arg2));
				i.putExtra("optional", listOptional.get(arg2));
				i.putExtra("id", listAvailID.get(arg2));
				i.putExtra("type", "offer");
				i.putExtra("location", listLocation.get(arg2));				
				startActivityForResult(i, RC_JOB_DETAIL);
			}
		});

		loadData();
		getActivity().registerReceiver(jobsReceiver, new IntentFilter("jobs.receiver"));

		return view;
	}

	BroadcastReceiver jobsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			loadData();
		}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		getActivity();
		Log.e("JobsFragment", "onActivityResult()");
		if (resultCode == FragmentActivity.RESULT_OK) {
			if (requestCode == RC_JOB_DETAIL) {
				loadData();
			}
		}
	}

	private void loadData() {
		final String url = CommonUtilities.SERVERURL + 
				CommonUtilities.API_GET_CURRENT_JOB_OFFERS + "?" +
				CommonUtilities.PARAM_PT_ID + "=" + pt_id;
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				listAvailID = new ArrayList<String>();
				listPrice = new ArrayList<String>();
				listAddress = new ArrayList<String>();
				listCompany = new ArrayList<String>();
				listSchedule = new ArrayList<String>();
				listTimeLeft = new ArrayList<String>();
				listDescription = new ArrayList<String>();
				listRequirement = new ArrayList<String>();
				listOptional = new ArrayList<String>();
				listLocation = new ArrayList<String>();

				if (jsonStr != null) {
					try {
						JSONArray items = new JSONArray(jsonStr);
						
						if (items != null && items.length() > 0) {
							Calendar calToday = new GregorianCalendar();
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
										listLocation.add(jsonParser.getString(
												objs, "location"));
										
										String startDate = jsonParser
												.getString(objs,
														"start_date_time");
										String endDate = jsonParser.getString(
												objs, "end_date_time");
										Calendar calStart = generateCalendar(startDate);
										Calendar calEnd = generateCalendar(endDate);
										listSchedule
												.add(formatterDate
														.format(calStart
																.getTime())
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
										int diffMnt = (int) ((calStart
												.getTimeInMillis() - calToday
												.getTimeInMillis()) / (1000 * 60));
										String timeLeft = "";
										if (diffMnt / (60 * 24) > 0) {
											timeLeft = (diffMnt / (60 * 24))
													+ " day ";
											diffMnt = diffMnt % (60 * 24);
										}
										listTimeLeft
												.add(timeLeft + (diffMnt / 60)
														+ " hrs "
														+ (diffMnt % 60)
														+ " mins left");
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
						NetworkUtils.connectionHandler(getActivity(), jsonStr,
								e1.getMessage());
						
						Log.e(CommonUtilities.TAG, "Error result " +
								jsonStr + " >> " + e1.getMessage());
					}
				} else {
					Toast.makeText(getActivity().getApplicationContext(),
							getString(R.string.server_error), Toast.LENGTH_SHORT).show();
				}

				adapter.updateList(listPrice, listAddress, listCompany,
						listSchedule, listTimeLeft);

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

	public static Bundle createBundle(String title) {
		Bundle bundle = new Bundle();
		bundle.putString(EXTRA_TITLE, title);
		return bundle;
	}
}
