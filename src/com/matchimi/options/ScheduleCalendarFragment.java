package com.matchimi.options;

import static com.matchimi.CommonUtilities.*;

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

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.internal.d;
import com.matchimi.Api;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.options.ScheduleAdapter.ViewHolder;
import com.matchimi.swipecalendar.CalendarFragment;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class ScheduleCalendarFragment extends Fragment {

	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private ScheduleAdapter adapter;
	private ProgressBar progressBar;
	private RelativeLayout calendarLayout;

	private List<String> listAvailID = null;
	private List<String> listPrice = null;
	private List<String> listAddress = null;
	private List<String> listCompany = null;
	private List<String> listSchedule = null;
	private List<String> listHeader = null;
	private List<String> listDescription = null;
	private List<String> listRequirement = null;
	private List<String> listOptional = null;
	private List<String> listLocation = null;
	private List<Date> listAvailabilityDate = null;
	private List<String> listStartTime = null;
	private List<String> listEndTime = null;
	private View dynamicView;
	
	private List<String> listScAvailID = null;
	private List<String> listScStartTime = null;
	private List<String> listScEndTime = null;
	private List<String> listScRepeat = null;
	private List<String> listScLocation = null;
	private List<String> listScDate = null;
	private List<String> listScPrice = null;
	private List<Boolean> listScFreeze = null;
	private List<Date> listScAvailabilityDate = null;
	
	private String pt_id = null;

	private int totalHours = 0;
	private int totalEarning = 0;
	
	public static final String EXTRA_TITLE = "title";
	public static final int RC_SCHEDULE_DETAIL = 11;
	private CalendarFragment calendarFragment;
	private View view;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.schedule_menu_calendar, container, false);

		SharedPreferences settings = this.getActivity().getSharedPreferences(
				PREFS_NAME, Context.MODE_PRIVATE);
		pt_id = settings.getString(CommonUtilities.USER_PTID, null);
		
		progressBar = (ProgressBar) view.findViewById(R.id.progress);
		calendarLayout = (RelativeLayout) view.findViewById(R.id.calendar_schedule_availability);
		
		loadData();
//		getActivity().registerReceiver(scheduleReceiver, new IntentFilter("schedule.receiver"));
		
		return view;
	}
	
//	BroadcastReceiver scheduleReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context arg0, Intent arg1) {
//			Log.d(TAG, "Receive broadcast from jobs");
//			loadData();
//		}
//	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
//		try {
//			getActivity().unregisterReceiver(scheduleReceiver);			
//		} catch (IllegalArgumentException e) {			
//		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		getActivity();
		Log.e(TAG, "Schedule Fragment : onActivityResult()");
		if (resultCode == FragmentActivity.RESULT_OK) {
			if (requestCode == RC_SCHEDULE_DETAIL) {
				loadData();
			}
		}
	}

	private void loadHistory() {
		final String url = CommonUtilities.SERVERURL + Api.PARAM_GET_JOB_HISTORY + "?" + 
				Api.PT_ID + "=" + pt_id;
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {

				totalEarning = 0;
				totalHours = 0;
				
				if (jsonStr != null) {
					try {
						JSONObject mainObj = new JSONObject(jsonStr);
						
						JSONArray items = new JSONArray(mainObj.get(Api.SUB_SLOTS).toString());
						
						if (items != null && items.length() > 0) {
							for (int i = 0; i < items.length(); i++) {
								/* get all json items, and put it on list */
								try {
									JSONObject objs = items.getJSONObject(i);
									objs = objs.getJSONObject("sub_slots");
									
									if (objs != null) {
										String price = ""
												+ jsonParser.getDouble(objs,
														"offered_salary");
										if (Integer
												.parseInt(price.substring(price
														.indexOf(".") + 1)) == 0) {
											price = price.substring(0,
													price.indexOf("."));
										}
										
										String startDate = jsonParser
												.getString(objs,
														"start_date_time");
										String endDate = jsonParser.getString(
												objs, "end_date_time");
										Calendar calStart = generateCalendar(startDate);
										Calendar calEnd = generateCalendar(endDate);
										int tmp = calEnd.get(Calendar.HOUR) - calStart.get(Calendar.HOUR);
										totalHours += tmp;
										totalEarning += (tmp * (Integer.parseInt(price)));
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
						Log.e("BBBBB", ">> Ex " + e1.getMessage());
						NetworkUtils.connectionHandler(getActivity(), jsonStr, e1.getMessage());
					}
				} else {
					Log.e("BBBBB", ">> JSON is null");
					Toast.makeText(getActivity(), getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
				
				loadSchedule();

			}
		};

		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);
				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}
	
	private void loadSchedule() {
		final String url = CommonUtilities.SERVERURL
				+ CommonUtilities.API_GET_AVAILABILITIES_BY_PT_ID + "?"
				+ CommonUtilities.PARAM_PT_ID + "=" + pt_id;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				listScAvailID = new ArrayList<String>();
				listScStartTime = new ArrayList<String>();
				listScEndTime = new ArrayList<String>();
				listScRepeat = new ArrayList<String>();
				listScLocation = new ArrayList<String>();
				listScDate = new ArrayList<String>();
				listScPrice = new ArrayList<String>();
				listScFreeze = new ArrayList<Boolean>();
				listScAvailabilityDate = new ArrayList<Date>();

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
										listScAvailID.add(jsonParser.getString(
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

										listScAvailabilityDate.add(calStart
												.getTime());

										listScStartTime.add(convertStartDate);
										listScEndTime.add(convertEndDate);

										listScRepeat.add(jsonParser.getString(
												objs, "repeat"));
										listScLocation.add(jsonParser.getString(
												objs, "location"));
										listScPrice.add(jsonParser.getString(
												objs, "asked_salary"));
										listScFreeze.add(jsonParser.getBoolean(
												objs, "is_frozen"));

										listScDate.add(CommonUtilities.AVAILABILITY_DATE
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
						NetworkUtils.connectionHandler(getActivity(), jsonStr,
								e1.getMessage());
					}
				} else {
					Toast.makeText(getActivity(), getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}

				progressBar.setVisibility(View.GONE);
				buildAvailabilityCalendar();
				calendarLayout.setVisibility(View.VISIBLE);				
			}
		};

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
	
	private void loadData() {
		Log.d(TAG, "Schedule load data called");
		
		final String url = SERVERURL + API_GET_CURRENT_ACCEPTED_JOB_OFFERS + "?" + PARAM_PT_ID + "=" + pt_id;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				listAvailID = new ArrayList<String>();
				listPrice = new ArrayList<String>();
				listAddress = new ArrayList<String>();
				listCompany = new ArrayList<String>();
				listSchedule = new ArrayList<String>();
				listHeader = new ArrayList<String>();
				listDescription = new ArrayList<String>();
				listRequirement = new ArrayList<String>();
				listOptional = new ArrayList<String>();
				listLocation = new ArrayList<String>();
				listAvailabilityDate = new ArrayList<Date>();
				listStartTime = new ArrayList<String>();
				listEndTime = new ArrayList<String>();

				if (jsonStr != null) {
					try {
						Log.d(TAG, "Schedule results from " + url +
								">>>\n" + jsonStr.toString());
						
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
												"mandatory_requirements"));
										listDescription
												.add(jsonParser.getString(objs,
														"description"));
										listOptional.add(jsonParser.getString(
												objs, "optional_requirements"));
										listLocation.add(jsonParser.getString(
												objs, "location"));
										
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
										
										listStartTime.add(convertStartDate);
										listEndTime.add(convertEndDate);
										listAvailabilityDate.add(calStart.getTime());
										
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
										if (formatterDate
												.format(calStart.getTime())
												.equalsIgnoreCase(
														formatterDate
																.format(calToday
																		.getTime()))) {
											listHeader.add("TODAY");
										} else {
											listHeader
													.add(formatterDate
															.format(calStart
																	.getTime()));
										}
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
						NetworkUtils.connectionHandler(getActivity(), jsonStr, e1.getMessage());
						
						Log.e(CommonUtilities.TAG, "Load schedule " +
								jsonStr + " >> " + e1.getMessage());
					}
				} else {
					Toast.makeText(getActivity().getApplicationContext(),
							getString(R.string.server_error), Toast.LENGTH_SHORT).show();
				}

				loadHistory();
			}
		};

		progressBar.setVisibility(View.VISIBLE);
		calendarLayout.setVisibility(View.GONE);
		
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);
				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}
	
	private void buildAvailabilityCalendar() {
		List<String> occupiedList = new ArrayList();
		SimpleDateFormat occupiedDate = new SimpleDateFormat(getResources().getString(R.string.datetime_simple_format));
		
		for(Date availabilityDate : listAvailabilityDate) {
			occupiedList.add(occupiedDate.format(availabilityDate));
		}

		if (listScStartTime != null && listScStartTime.size()> 0) {
			for(String s: listScStartTime) {
				occupiedList.add(s);
			}
		}

		Bundle bundle= new Bundle();
		bundle.putBoolean(CommonUtilities.IS_AVAILABILITY_CALENDAR, false);
		bundle.putString(CommonUtilities.PARAM_PT_ID, pt_id);
		
		bundle.putStringArray(CommonUtilities.OCCUPIED_DATES, occupiedList.toArray(new String[occupiedList.size()]));
		bundle.putStringArray(CommonUtilities.SCHEDULE_LIST_AVAIL_ID, listAvailID.toArray(new String[listAvailID.size()]));
		bundle.putStringArray(CommonUtilities.SCHEDULE_START_TIME, listStartTime.toArray(new String[listStartTime.size()]));
		bundle.putStringArray(CommonUtilities.SCHEDULE_END_TIME, listEndTime.toArray(new String[listEndTime.size()]));
		bundle.putStringArray(CommonUtilities.SCHEDULE_DATA_LOCATION, listLocation.toArray(new String[listLocation.size()]));
		bundle.putStringArray(CommonUtilities.SCHEDULE_ADDRESS, listAddress.toArray(new String[listAddress.size()]));
		bundle.putStringArray(CommonUtilities.SCHEDULE_COMPANY, listCompany.toArray(new String[listCompany.size()]));
		bundle.putStringArray(CommonUtilities.SCHEDULE_TIMEWORK, listSchedule.toArray(new String[listSchedule.size()]));
		bundle.putStringArray(CommonUtilities.SCHEDULE_PRICE, listPrice.toArray(new String[listPrice.size()]));
		bundle.putStringArray(CommonUtilities.SCHEDULE_HEADER, listHeader.toArray(new String[listHeader.size()]));
		bundle.putStringArray(CommonUtilities.SCHEDULE_DESCRIPTION, listDescription.toArray(new String[listDescription.size()]));
		bundle.putStringArray(CommonUtilities.SCHEDULE_LIST_REQUIREMENT, listRequirement.toArray(new String[listRequirement.size()]));
		bundle.putStringArray(CommonUtilities.SCHEDULE_LIST_OPTIONAL, listOptional.toArray(new String[listOptional.size()]));
		
		List<String> scFreezeList = new ArrayList();
		for (boolean b : listScFreeze) {
			scFreezeList.add(Boolean.toString(b));
		}
		
		bundle.putStringArray(CommonUtilities.AVAIL_ID, listScAvailID.toArray(new String[listScAvailID.size()]));
		bundle.putStringArray(CommonUtilities.AVAIL_START_TIME, listScStartTime.toArray(new String[listScStartTime.size()]));
		bundle.putStringArray(CommonUtilities.AVAIL_END_TIME, listScEndTime.toArray(new String[listScEndTime.size()]));
		bundle.putStringArray(CommonUtilities.AVAIL_REPEAT, listScRepeat.toArray(new String[listScRepeat.size()]));
		bundle.putStringArray(CommonUtilities.AVAIL_LOCATION, listScLocation.toArray(new String[listScLocation.size()]));
		bundle.putStringArray(CommonUtilities.AVAIL_PRICE, listScPrice.toArray(new String[listScPrice.size()]));
		bundle.putStringArray(CommonUtilities.AVAIL_FREEZE, scFreezeList.toArray(new String[scFreezeList.size()]));
		bundle.putStringArray(CommonUtilities.AVAIL_DATE, listScDate.toArray(new String[listScDate.size()]));
		bundle.putInt(CommonUtilities.TOTAL_EARNING, totalEarning);
		bundle.putInt(CommonUtilities.TOTAL_HOURS, totalHours);
		
		calendarFragment = new CalendarFragment();
		calendarFragment.setArguments(bundle);
		
		FragmentTransaction t = getChildFragmentManager().beginTransaction();
		t.replace(R.id.calendar_schedule_wrapper, calendarFragment);
		t.commitAllowingStateLoss();

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
