package com.matchimi.schedule;

import static com.matchimi.CommonUtilities.API_GET_CURRENT_ACCEPTED_JOB_OFFERS;
import static com.matchimi.CommonUtilities.AVAILABILITY_SELECTED;
import static com.matchimi.CommonUtilities.PARAM_PT_ID;
import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.RC_SCHEDULE_DETAIL;
import static com.matchimi.CommonUtilities.SERVERURL;
import static com.matchimi.CommonUtilities.TAG;
import com.matchimi.CommonUtilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caldroid.CaldroidFragment;
import com.caldroid.CaldroidListener;
import com.matchimi.Api;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.availability.DailyAvailabilityPreview;
import com.matchimi.options.CreateAvailability;
import com.matchimi.options.JobDetails;
import com.matchimi.options.ScheduleAdapter;
import com.matchimi.swipecalendar.CalendarFragment;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class CalendarScheduleFragment extends Fragment {

	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private ScheduleAdapter adapter;
	private ProgressBar progressBar;
	private RelativeLayout calendarLayout;

	private List<String> listScheduleAvailID = null;
	private List<String> listSchedulePrice = null;
	private List<String> listScheduleAddress = null;
	private List<String> listScheduleCompany = null;
	private List<String> listScheduleHeader = null;
	private List<String> listScheduleDescription = null;
	private List<String> listScheduleRequirement = null;
	private List<String> listScheduleOptional = null;
	private List<String> listScheduleLocation = null;
	private List<String> listStartTime = null;
	private List<String> listEndTime = null;
	private List<String> listScheduleStartDateRange = null;
	private List<Date> listScheduleDate = null;
	
	private List<String> listAvailabilityID = null;
	private List<String> listAvailabilityStartTime = null;
	private List<String> listAvailabilityEndTime = null;
	private List<String> listAvailabilityRepeat = null;
	private List<String> listAvailabilityLocation = null;
	private List<String> listAvailabilityPrice = null;
	private List<String> listAvailabilityStartDateRange = null;
	private List<Boolean> listAvailabilityFreeze = null;
	private List<Date> listAvailabilityDate = null;
	
	private LinearLayout jobLayout;
	private View dynamicView;

	public static final int RC_EDIT_AVAILABILITY = 30;
	
	private String pt_id = null;

	private int totalHours = 0;
	private int totalEarning = 0;

	public static final String EXTRA_TITLE = "title";
	public static final int RC_SCHEDULE_DETAIL = 11;
	private CalendarFragment calendarFragment;
	private View view;
	private View historyJobView;
	private CaldroidFragment caldroidFragment;
	private CaldroidFragment dialogCaldroidFragment;
	
	private final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.caldroid_schedule, container, false);
		historyJobView = inflater.inflate(R.layout.history_jobs, container, false);
		
		jobLayout = (LinearLayout) view.findViewById(R.id.calendar_jobs_selected_wrapper);

		SharedPreferences settings = this.getActivity().getSharedPreferences(
				PREFS_NAME, Context.MODE_PRIVATE);
		pt_id = settings.getString(CommonUtilities.USER_PTID, null);

		// Setup caldroid fragment
//		caldroidFragment = new CaldroidFragment();
		caldroidFragment = new CaldroidSampleCustomFragment();
		
		Bundle args = new Bundle();
		Calendar cal = Calendar.getInstance();
		args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
		args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
		args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
		args.putBoolean(CaldroidFragment.FIT_ALL_MONTHS, false);

		// Uncomment this to customize startDayOfWeek
		// args.putInt("startDayOfWeek", 6); // Saturday
		caldroidFragment.setArguments(args);
		
		// Attach to the activity
		FragmentTransaction t = getChildFragmentManager().beginTransaction();
		t.replace(R.id.calendar1, caldroidFragment);
		t.commit();

		// Setup listener
		final CaldroidListener listener = calendarListener;

		// Setup Caldroid
		caldroidFragment.setCaldroidListener(listener);

		getActivity().registerReceiver(scheduleReceiver,
				new IntentFilter("schedule.receiver"));
		
		progressBar = (ProgressBar) view.findViewById(R.id.progress);
		calendarLayout = (RelativeLayout) view.findViewById(R.id.calendar_schedule_availability);

		loadData();
		
		return view;
	}
	
	private CaldroidListener calendarListener = new CaldroidListener() {

		@Override
		public void onSelectDate(Date selectedDate, View view) {
			final Calendar selectedCal = Calendar.getInstance();
			selectedCal.setTime(selectedDate);

			Log.d(TAG, "Get clicked date " + selectedDate);
			
			// Clear all views
			jobLayout.removeAllViews();

			boolean isEmptyCalendar = true;
			
			if(listAvailabilityDate.size() > 0) {
				for (int i=0; i < listAvailabilityDate.size(); i++) {
					if(sameDay(listAvailabilityDate.get(i),selectedDate)) {
						isEmptyCalendar = false;	
					}
				}
			}
			
			if(listScheduleDate.size() > 0) {
				for (int i=0; i < listScheduleDate.size(); i++) {
					if(sameDay(listScheduleDate.get(i),selectedDate)) {
						isEmptyCalendar = false;									
					}
				}
			}
			
			// If no availability and schedule on this date, showing add availability button and indicate text
			if (isEmptyCalendar) {
				if(getActivity() != null) {
					// Add selected schedule
					TextView labelIndicate = new TextView(getActivity());
					android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
							android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 
							android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
					
					labelIndicate.setText(getResources().getString(R.string.schedule_indicate));
					labelIndicate.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Small);
					labelIndicate.setTextColor(Color.WHITE);
					labelIndicate.setPadding(convertDpToPixel(5), convertDpToPixel(5), convertDpToPixel(5), convertDpToPixel(5));
					
					labelIndicate.setBackgroundColor(getResources().getColor(R.color.blue_sky));
					labelIndicate.setLayoutParams(params);
					jobLayout.addView(labelIndicate);
					
					Button addAvailButton = addingButtonAddAvailability(selectedCal);
					jobLayout.addView(addAvailButton);
				}
				
			} else {
				if(getActivity() != null) {
					
					// Add selected schedule
					android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
					TextView labelMySchedule = new TextView(getActivity());
					labelMySchedule.setText(getResources().getString(R.string.schedule_tab_my_availability));
					labelMySchedule.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Small);
					labelMySchedule.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
					labelMySchedule.setTextColor(Color.WHITE);
					labelMySchedule.setPadding(convertDpToPixel(5), convertDpToPixel(5), convertDpToPixel(5), convertDpToPixel(5));
					labelMySchedule.setBackgroundColor(getResources().getColor(R.color.blue_sky));
					labelMySchedule.setLayoutParams(params);
					jobLayout.addView(labelMySchedule);
	
					if(listAvailabilityDate.size() > 0) {
						for (int i=0; i < listAvailabilityDate.size(); i++) {
							Log.d(TAG, "Availability clicked " + selectedDate + " " + listAvailabilityDate.get(i));
							
							if(sameDay(listAvailabilityDate.get(i), selectedDate)) {
								Log.d(TAG, "matched!");
								addAvailability(jobLayout, i);								
							}
						}
					}
					
					Button addAvailButton = addingButtonAddAvailability(selectedCal);
					jobLayout.addView(addAvailButton);
					
					if (listScheduleDate.size() > 0) {
						boolean isScheduleEmpty = true;
						
						for (int i=0; i < listScheduleDate.size(); i++) {
							if(sameDay(listScheduleDate.get(i), selectedDate)) {
								isScheduleEmpty = false;								
							}
						}
						
						if(!isScheduleEmpty) {
							TextView labelMyJob = new TextView(getActivity());
							params.topMargin = 6;
							labelMyJob.setText(getResources().getString(R.string.schedule_tab_my_schedule));
							labelMyJob.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Small);
							labelMyJob.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
							labelMyJob.setTextColor(Color.WHITE);
							labelMyJob.setPadding(convertDpToPixel(5), convertDpToPixel(5), convertDpToPixel(5), convertDpToPixel(5));
							labelMyJob.setBackgroundColor(getResources().getColor(R.color.blue_sky));
							labelMyJob.setLayoutParams(params);
							jobLayout.addView(labelMyJob);
							
							if(listScheduleDate.size() > 0) {
								for (int i=0; i < listScheduleDate.size(); i++) {
									if(sameDay(listScheduleDate.get(i), selectedDate)) {
										addScheduleJobs(jobLayout, i);									
									}
								}
							}							
						}
						
					}
				}
			}
		}

		@Override
		public void onChangeMonth(int month, int year) {
			// Nothing to do here
		}

	};
	
	BroadcastReceiver scheduleReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// Update adapter with refresh data
			Log.d(TAG, "Receive broadcast from jobs");
			loadData();
		}
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		try {
			getActivity().unregisterReceiver(scheduleReceiver);			
		} catch (IllegalArgumentException e) {			
		}
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
	
	private static boolean sameDay(Date first, Date second) {
		Calendar firstCal = Calendar.getInstance();
		firstCal.setTime(first);
		Calendar secondCal = Calendar.getInstance();
		secondCal.setTime(second);
		
		return firstCal.get(Calendar.YEAR) == secondCal.get(Calendar.YEAR)
				&& firstCal.get(Calendar.DAY_OF_YEAR) == secondCal
						.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * Loading history
	 */
	private void loadHistory() {
		Log.d(TAG, "Loading history ...");
		
		final String url = Api.SERVERURL + Api.PARAM_GET_JOB_HISTORY + "?" + 
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
						Log.e(CommonUtilities.TAG, ">> Ex " + e1.getMessage());
						NetworkUtils.connectionHandler(getActivity(), jsonStr, e1.getMessage());
					}
				} else {
					Log.e(CommonUtilities.TAG, ">> JSON is null");
					Toast.makeText(getActivity(), getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
				
				Log.d(TAG, "Set earning and hours " + totalHours + " " + totalEarning);

				// Updating text view
				TextView totalHoursView = (TextView) view.findViewById(R.id.scheduleTotalHours);
				TextView totalEarningView = (TextView) view.findViewById(R.id.scheduleTotalEarning);
				
				totalHoursView.setText(getResources().getString(R.string.schedule_total_hours) + " " + totalHours + " hour");
				totalEarningView.setText(getResources().getString(R.string.schedule_total_income) + " $" + totalEarning);										

				buildScheduleCalendar();
				progressBar.setVisibility(View.GONE);
				calendarLayout.setVisibility(View.VISIBLE);
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
	
	/**
	 * Loading schedule part-timer
	 */
	private void loadAvailability() {
		final String url = CommonUtilities.SERVERURL
				+ CommonUtilities.API_GET_AVAILABILITIES_BY_PT_ID + "?"
				+ CommonUtilities.PARAM_PT_ID + "=" + pt_id;
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				listAvailabilityID = new ArrayList<String>();
				listAvailabilityStartTime = new ArrayList<String>();
				listAvailabilityStartDateRange = new ArrayList<String>();
				listAvailabilityEndTime = new ArrayList<String>();
				listAvailabilityRepeat = new ArrayList<String>();
				listAvailabilityLocation = new ArrayList<String>();
				listAvailabilityPrice = new ArrayList<String>();
				listAvailabilityFreeze = new ArrayList<Boolean>();
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
										listAvailabilityID.add(jsonParser.getString(
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

										listAvailabilityStartTime.add(convertStartDate);
										listAvailabilityEndTime.add(convertEndDate);

										listAvailabilityRepeat.add(jsonParser.getString(
												objs, "repeat"));
										listAvailabilityLocation.add(jsonParser.getString(
												objs, "location"));
										listAvailabilityPrice.add(jsonParser.getString(
												objs, "asked_salary"));
										listAvailabilityFreeze.add(jsonParser.getBoolean(
												objs, "is_frozen"));

										listAvailabilityStartDateRange.add(CommonUtilities.AVAILABILITY_DATE
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
		
				loadHistory();
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
	
	/**
	 * Load availabilities, history and schedule
	 */
	private void loadData() {
		progressBar.setVisibility(View.VISIBLE);
		calendarLayout.setVisibility(View.GONE);
		
		LinearLayout jobsLayoutWrapper = (LinearLayout) view.findViewById(R.id.calendar_jobs_selected_wrapper);
		jobsLayoutWrapper.removeAllViews();
		jobsLayoutWrapper.addView(historyJobView);
		
		// Add selected schedule
		TextView labelIndicate = new TextView(getActivity());
		android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
		
		labelIndicate.setText(getResources().getString(R.string.schedule_indicate));
		labelIndicate.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Small);
		labelIndicate.setTextColor(Color.WHITE);
		labelIndicate.setPadding(convertDpToPixel(5), convertDpToPixel(5), convertDpToPixel(5), convertDpToPixel(5));
		
		labelIndicate.setBackgroundColor(getResources().getColor(R.color.blue_sky));
		labelIndicate.setLayoutParams(params);
		jobsLayoutWrapper.addView(labelIndicate);
		
		Button addAvailButton = addingGeneralButtonAddAvailability();
		jobsLayoutWrapper.addView(addAvailButton);
		
		
		final String url = SERVERURL + CommonUtilities.API_GET_CURRENT_ACCEPTED_JOB_OFFERS 
				+ "?" + PARAM_PT_ID + "=" + pt_id;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				listScheduleAvailID = new ArrayList<String>();
				listSchedulePrice = new ArrayList<String>();
				listScheduleAddress = new ArrayList<String>();
				listScheduleCompany = new ArrayList<String>();
				listScheduleStartDateRange = new ArrayList<String>();
				listScheduleHeader = new ArrayList<String>();
				listScheduleDescription = new ArrayList<String>();
				listScheduleRequirement = new ArrayList<String>();
				listScheduleOptional = new ArrayList<String>();
				listScheduleLocation = new ArrayList<String>();
				listScheduleDate = new ArrayList<Date>();

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
										listScheduleAvailID.add(jsonParser.getString(
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
										listSchedulePrice
												.add("<font color='#A4A9AF'>$</font><big><big><big><big><font color='#276289'>"
														+ price
														+ "</font></big></big></big></big><font color='#A4A9AF'>/hr</font>");
										listScheduleAddress.add(jsonParser.getString(
												objs, "address"));
										listScheduleCompany.add(jsonParser.getString(
												objs, "company_name"));
										listScheduleRequirement
										.add(jsonParser.getString(objs,
												"mandatory_requirements"));
										listScheduleDescription
												.add(jsonParser.getString(objs,
														"description"));
										listScheduleOptional.add(jsonParser.getString(
												objs, "optional_requirements"));
										listScheduleLocation.add(jsonParser.getString(
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
										
										listScheduleDate.add(calStart.getTime());
										
										listScheduleStartDateRange
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
											listScheduleHeader.add("TODAY");
										} else {
											listScheduleHeader
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

				loadAvailability();
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
	
	private void buildScheduleCalendar() {
		if (caldroidFragment != null) {
			int dotIteration = 0;
			
			if (listAvailabilityDate != null && listAvailabilityDate.size() > 0) {
				for(int i = 0; i< listAvailabilityDate.size(); i++) {
					caldroidFragment.setBackgroundResourceForDate(R.color.green_lighter, listAvailabilityDate.get(i));
					caldroidFragment.setTextColorForDate(R.color.darktitle, listAvailabilityDate.get(i));
					
//					Calendar cal = Calendar.getInstance();
//					cal.setTime(listAvailabilityDate.get(i));
//					cal.add(Calendar.SECOND, i);
//					
//					caldroidFragment.setPointColorForDateTime(R.drawable.dot_green, new DateTime(cal.getTime()));
//					dotIteration += 1;
				}
			}

			if (listScheduleDate != null && listScheduleDate.size() > 0) {
				for(int i = 0; i < listScheduleDate.size(); i++) {
					caldroidFragment.setBackgroundResourceForDate(R.color.calendar_selected_day_bg,
							listScheduleDate.get(i));
					caldroidFragment.setTextColorForDate(R.color.darktitle, listScheduleDate.get(i));
					
					Calendar cal = Calendar.getInstance();
					cal.setTime(listScheduleDate.get(i));
					cal.add(Calendar.SECOND, dotIteration + i);
					
					caldroidFragment.setPointColorForDateTime(R.drawable.dot_green, new DateTime(cal.getTime()));
				}
			}
		}

		// Update adapter with refresh data
		caldroidFragment.refreshView();

		Date todayDate = new Date();
		caldroidFragment.moveToDate(todayDate);
	}
	
	/**
	 * Convert dp to pixels
	 * @param dps
	 * @return
	 */
	private int convertDpToPixel(int dps) {
		final float scale = getActivity().getResources().getDisplayMetrics().density;
		int pixels = (int) (dps * scale + 0.5f);
		
		return pixels;
	}
	
	private void addScheduleJobs(LinearLayout jobLayout, int position) {
		if(getActivity() != null) {
			Log.d(TAG, "Adding schedule " + position);
			LayoutInflater vi = (LayoutInflater) getActivity()
					.getApplicationContext().getSystemService(
							Context.LAYOUT_INFLATER_SERVICE);
			View dynamicView = vi.inflate(R.layout.schedule_item_list_calendar, null);

			TextView textDate = (TextView) dynamicView.findViewById(R.id.textDate);
			TextView textPlace = (TextView) dynamicView
					.findViewById(R.id.textPlace);
			TextView textPrice = (TextView) dynamicView
					.findViewById(R.id.textPrice);
//			TextView textHeader = (TextView) dynamicView
//					.findViewById(R.id.textHeader);

			textPrice.setText(Html.fromHtml(listSchedulePrice.get(position)));
			textDate.setText(listScheduleStartDateRange.get(position));
			textPlace.setText(listScheduleCompany.get(position) + "\n" + listScheduleAddress.get(position));
//			textHeader.setText(listScheduleHeader.get(position));
			
			dynamicView.setOnClickListener(scheduleOnClick(dynamicView, position));
			jobLayout.addView(dynamicView);			
		}
	}
	
	private void addAvailability(LinearLayout jobLayout, int position) {
		if(getActivity() != null) {
			LayoutInflater vi = (LayoutInflater) getActivity()
					.getApplicationContext().getSystemService(
							Context.LAYOUT_INFLATER_SERVICE);
			View dynamicView = vi.inflate(R.layout.availability_item_list, null);

			TextView textDate = (TextView) dynamicView.findViewById(R.id.textDate);
			textDate.setText(listAvailabilityStartDateRange.get(position));

			dynamicView.setOnClickListener(ActivityOnClick(dynamicView, position));
			jobLayout.addView(dynamicView);			
		}
	}
	
	private View.OnClickListener ActivityOnClick(final View view, final int position) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO
				Intent i = new Intent(getActivity(), DailyAvailabilityPreview.class);
				i.putExtra("pt_id", pt_id);
				i.putExtra("avail_id", listAvailabilityID.get(position));
				i.putExtra("start", listAvailabilityStartTime.get(position));
				i.putExtra("end", listAvailabilityEndTime.get(position));
				i.putExtra("repeat", listAvailabilityRepeat.get(position));
				i.putExtra("location", listAvailabilityLocation.get(position));
				i.putExtra("price", listAvailabilityPrice.get(position));
				i.putExtra("is_frozen", listAvailabilityFreeze.get(position));

				startActivityForResult(i, RC_EDIT_AVAILABILITY);
			}
		};
	}
	
	private View.OnClickListener scheduleOnClick(final View view, final int position) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "This is clicked" + listScheduleDescription.get(position));
				
				Intent i = new Intent(getActivity(), JobDetails.class);
				i.putExtra("price", listSchedulePrice.get(position));
				i.putExtra("place",
						listScheduleCompany.get(position) + "\n" + listScheduleAddress.get(position));
				i.putExtra("expire", listScheduleHeader.get(position));
				i.putExtra("description", listScheduleDescription.get(position));
				i.putExtra("requirement", listScheduleRequirement.get(position));
				i.putExtra("optional", listScheduleOptional.get(position));
				i.putExtra("location", listScheduleLocation.get(position));				
				i.putExtra("id", listScheduleAvailID.get(position));
				i.putExtra("type", "accepted");
				startActivityForResult(i, RC_SCHEDULE_DETAIL);
			}
		};
	}
	
	private Button addingButtonAddAvailability(final Calendar selectedCal) {
		Button addAvail = new Button(getActivity());
		addAvail.setText(getResources().getString(R.string.schedule_add_availability));
		addAvail.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Small);
		addAvail.setBackgroundResource(R.color.lightcolor);
		addAvail.setPadding(convertDpToPixel(5), convertDpToPixel(5), convertDpToPixel(5), convertDpToPixel(5));
		addAvail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				goToAvailabilityForm(selectedCal);
			}
		});
		
		return addAvail;
	}
	
	private Button addingGeneralButtonAddAvailability() {
		Button addAvail = new Button(getActivity());
		addAvail.setText(getResources().getString(R.string.schedule_add_availability));
		addAvail.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Small);
		addAvail.setBackgroundResource(R.color.lightcolor);
		addAvail.setPadding(convertDpToPixel(5), convertDpToPixel(5), convertDpToPixel(5), convertDpToPixel(5));
		addAvail.setOnClickListener(jobAvailabilityListener);
		
		return addAvail;
	}

	private void goToAvailabilityForm(Calendar selectedCal) {
		Intent dailyAvailability = new Intent(getActivity(), CreateAvailability.class);
		dailyAvailability.putExtra(AVAILABILITY_SELECTED, CommonUtilities.AVAILABILTY_DATETIME
				.format(selectedCal.getTime()));
		getActivity().startActivity(dailyAvailability);						
	}
	
	private static boolean sameDay(Calendar cal, Calendar selectedDate) {
		return cal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
				&& cal.get(Calendar.DAY_OF_YEAR) == selectedDate
						.get(Calendar.DAY_OF_YEAR);
	}

	private android.view.View.OnClickListener jobAvailabilityListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(getActivity(), CreateAvailability.class);
			intent.putExtra("id", pt_id);
			intent.putExtra("update", false);
			startActivity(intent);
		}
	};

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
