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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caldroid.CaldroidFragment;
import com.caldroid.CaldroidListener;
import com.matchimi.Api;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.availability.DailyAvailabilityPreview;
import com.matchimi.availability.LocationPreferenceRegionAdapter;
import com.matchimi.options.CreateAvailability;
import com.matchimi.options.JobDetails;
import com.matchimi.options.ScheduleAdapter;
import com.matchimi.swipecalendar.CalendarFragment;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.MapUtils;
import com.matchimi.utils.NetworkUtils;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This class handle for my schedule tab contents
 * Calendar using Caldroid : https://github.com/roomorama/Caldroid
 * 
 * @author yodi
 *
 */
public class CalendarScheduleFragment extends Fragment {

	private JSONParser jsonParser = null;
	private String jsonStr = null;
	private String jsonScheduleStr = null;
	
	private ProgressDialog progress;

	private ScheduleAdapter adapter;
	private ProgressBar progressBar;
	private RelativeLayout calendarLayout;

	private String responseString = null;

	private Dialog rateDialog;
	
	private List<String> listScheduleAvailID = null;
	private List<String> listSchedulePrice = null;
	private List<String> listScheduleAddress = null;
	private List<String> listScheduleCompany = null;
	private List<String> listScheduleGrade = null;
	private List<String> listScheduleHeader = null;
	private List<String> listScheduleDescription = null;
	private List<List<String>> listScheduleRequirement = null;
	private List<List<String>> listScheduleOptional = null;
	private List<String> listScheduleLocation = null;
	private List<String> listScheduleStartDateRange = null;
	private List<String> listScheduleStartTimeRange = null;
	private List<String> listScheduleJobName = null;
	
	private List<List<String>> listScheduleFriendsFacebookID = null;
	private List<List<String>> listScheduleFriendsFirstName = null;	
	private List<List<String>> listScheduleFriendsLastName = null;	
	private List<List<String>> listScheduleFriendsProfilePicture = null;
	private List<List<String>> listScheduleFriendsPtID = null;
	
	private List<String> listScheduleWorkTime = null;
	private List<String> listScheduleWorkMoney = null;
	private List<Date> listScheduleDate = null;
	
	private List<String> listPastScheduleAvailID = null;
	private List<String> listPastScheduleBranchID = null;
	private List<String> listPastScheduleBranchName = null;
	private List<String> listPastSchedulePrice = null;
	private List<String> listPastScheduleAddress = null;
	private List<String> listPastScheduleCompany = null;
	private List<String> listPastScheduleGrade = null;
	private List<String> listPastScheduleHeader = null;
	private List<String> listPastScheduleDescription = null;
	private List<String> listPastScheduleRequirement = null;
	private List<String> listPastScheduleOptional = null;
	private List<String> listPastScheduleLocation = null;
	private List<String> listPastScheduleStartDateRange = null;
	private List<String> listPastScheduleStartTimeRange = null;
	private List<String> listPastScheduleJobName = null;
	private List<String> listPastScheduleWorkTime = null;
	private List<String> listPastScheduleWorkMoney = null;
	private List<Integer> listPastSchedulePTGrade = null;
	private List<Date> listPastScheduleDate = null;

	private List<String> listAvailabilityID = null;
	private List<String> listRAvailabilityID = null;
	private List<String> listAvailabilityStartTime = null;
	private List<String> listAvailabilityEndTime = null;
	private List<List<String>> listAvailabilityRepeat = null;
	private List<String> listAvailabilityEditRepeat = null;
	private List<String> listAvailabilityLocation = null;
	private List<String> listAvailabilityPrice = null;
	private List<String> listAvailabilityStartDateRange = null;
	private List<String> listAvailabilityStatus = null;
	private List<String> listAvailabilityExpiredAt = null;
	private List<Boolean> listAvailabilityIsRepeat= null;
	
 	private List<Boolean> listAvailabilityFreeze = null;
	private List<Date> listAvailabilityDate = null;
	private Map<Integer, String> mapDays = new HashMap<Integer, String>();
	
	private LinearLayout jobLayout;
	private View dynamicView;
	private Context context;

	public static final int RC_EDIT_AVAILABILITY = 30;

	private String pt_id = null;

	private String totalHours = "0";
	private String totalEarning = "0";

	public static final String EXTRA_TITLE = "title";
	public static final int RC_SCHEDULE_DETAIL = 11;
	private View view;
	private View historyJobView;
	private CaldroidFragment caldroidFragment;
	private Date todayDate = new Date();

	private int selectedMonth;
	private int selectedYear;
	private Boolean isSwipe = false;
	private Boolean isLoadData = false;
	private Boolean isRefreshed = false;
	private Date availabilityRedirect = null;
	
	private final SimpleDateFormat formatter = new SimpleDateFormat(
			"dd MMM yyyy");

	private String[] ratingArray;
	private Map<String, String> mapLocations = new HashMap<String, String>();
	
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;	
	private String locationAPIData = null;
	private String dayAPIData = null;

	private Runnable scheduleRunnable;
	private Handler scheduleHandler;
	private Runnable pastScheduleRunnable;
	private Handler pastScheduleHandler;
	private Runnable availabilityRunnable;
	private Handler availabilityHandler;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
//		if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_SMALL) 
//				== Configuration.SCREENLAYOUT_SIZE_SMALL){
//			view = inflater.inflate(R.layout.caldroid_schedule_ldpi, container, false);
//			
//		}else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_NORMAL) 
//				== Configuration.SCREENLAYOUT_SIZE_NORMAL){
//			Display displayparm= getActivity().getWindowManager().getDefaultDisplay();
////			int width= displayparm.getWidth();
//			int Height= displayparm.getHeight();
//			Log.d(CommonUtilities.TAG, "Screen height is " + Height);
//			
//			if(Height > 500) {
//				view = inflater.inflate(R.layout.caldroid_schedule, container, false);				
//			} else {
//				view = inflater.inflate(R.layout.caldroid_schedule_ldpi, container, false);
//			}
//			
//		}else{
//			view = inflater.inflate(R.layout.caldroid_schedule, container, false);
//		}
		
		view = inflater.inflate(R.layout.caldroid_schedule, container, false);
		
		historyJobView = inflater.inflate(R.layout.history_jobs, container,
				false);

		context = getActivity();

		jobLayout = (LinearLayout) view
				.findViewById(R.id.calendar_jobs_selected_wrapper);

		settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		pt_id = settings.getString(CommonUtilities.USER_PTID, null);

		// Setup caldroid fragment
		// caldroidFragment = new CaldroidFragment();
		
		// Setup broadcast receiver for refreshing calendar everytime it's get called
		context.registerReceiver(scheduleReceiver, new IntentFilter(
				CommonUtilities.BROADCAST_SCHEDULE_RECEIVER));

		LocalBroadcastManager.getInstance(context).registerReceiver(
				mBackPressedReceiver,
				new IntentFilter(CommonUtilities.LOCALBROADCAST_SCHEDULE_BACKPRESSED_RECEIVER));


		LocalBroadcastManager.getInstance(context).registerReceiver(
				historyReceiver,
				new IntentFilter(CommonUtilities.BROADCAST_LOAD_HISTORY));

		
		progressBar = (ProgressBar) view.findViewById(R.id.progress);
		calendarLayout = (RelativeLayout) view
				.findViewById(R.id.calendar_schedule_availability);

		Calendar todayCal = Calendar.getInstance();
		todayCal.setTime(todayDate);
		
		selectedMonth = todayCal.get(Calendar.MONTH);
		selectedYear = todayCal.get(Calendar.YEAR);
		
		ratingArray = getResources().getStringArray(R.array.rating_employer_label);
		
		loadData();

		return view;
	}
	
	/**
	 * CLICK DATE ON CALENDAR CELL IS IN HERE
	 */
	private CaldroidListener calendarListener = new CaldroidListener() {

		@Override
		public void onSelectDate(Date selectedDate, View view) {
			final Calendar selectedCal = Calendar.getInstance();
			selectedCal.setTime(selectedDate);
			
//			view.setBackgroundResource(R.color.caldroid_sky_blue);			

			// Clear all views
			jobLayout.removeAllViews();

			boolean isEmptyCalendar = true;

			if (listAvailabilityDate.size() > 0) {
				for (int i = 0; i < listAvailabilityDate.size(); i++) {
					if (sameDay(listAvailabilityDate.get(i), selectedDate)) {
						isEmptyCalendar = false;
					}
				}
			}

			if (listScheduleDate.size() > 0) {
				for (int i = 0; i < listScheduleDate.size(); i++) {
					if (sameDay(listScheduleDate.get(i), selectedDate)) {
						isEmptyCalendar = false;
					}
				}
			}
						
			// If no availability and schedule on this date, showing add
			// availability button and indicate text
			if ((isEmptyCalendar && sameDay(selectedDate, todayDate))
					|| (isEmptyCalendar && selectedDate.compareTo(todayDate) >= 0)) {
				if (context != null) {
					// Add selected schedule
					TextView labelIndicate = new TextView(context);
					android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
							android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
							android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);

					labelIndicate.setText(getResources().getString(
							R.string.schedule_tab_my_availability));
					labelIndicate.setTextAppearance(context,
							android.R.style.TextAppearance_DeviceDefault_Small);
					// labelIndicate.setTypeface(Typeface.DEFAULT,
					// Typeface.BOLD);
					labelIndicate.setPadding(convertDpToPixel(5),
							convertDpToPixel(5), convertDpToPixel(5),
							convertDpToPixel(5));
					labelIndicate.setTextColor(Color.WHITE);
					labelIndicate.setBackgroundColor(getResources().getColor(
							R.color.blue_sky));
					labelIndicate.setLayoutParams(params);
					jobLayout.addView(labelIndicate);

					Button addAvailButton = addingButtonAddAvailability(selectedCal);
					jobLayout.addView(addAvailButton);
				}

			} else {
				if (context != null) {
					// Add selected schedule
					android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
							android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
							android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);

					if (listScheduleDate.size() > 0) {
						boolean isScheduleEmpty = true;

						for (int i = 0; i < listScheduleDate.size(); i++) {
							if (sameDay(listScheduleDate.get(i), selectedDate)) {
								isScheduleEmpty = false;
							}
						}

						if (!isScheduleEmpty) {
							TextView labelMyJob = new TextView(context);
							// params.topMargin = 6;
							labelMyJob.setText(getResources().getString(
									R.string.schedule_tab_my_schedule));
							labelMyJob
									.setTextAppearance(
											context,
											android.R.style.TextAppearance_DeviceDefault_Small);
							// labelMyJob.setTypeface(Typeface.DEFAULT,
							// Typeface.BOLD);
							labelMyJob.setTextColor(Color.WHITE);
							labelMyJob.setPadding(convertDpToPixel(5),
									convertDpToPixel(5), convertDpToPixel(5),
									convertDpToPixel(5));
							labelMyJob.setBackgroundColor(getResources()
									.getColor(R.color.blue_sky));
							labelMyJob.setLayoutParams(params);
							jobLayout.addView(labelMyJob);

							// Adding clicker for schedule accepted jobs
							if (listScheduleDate.size() > 0) {
								for (int i = 0; i < listScheduleDate.size(); i++) {
									if (sameDay(listScheduleDate.get(i),
											selectedDate)) {

										// If selected day lesser than today,
										// showing schedule jobs
										if (selectedDate.compareTo(todayDate) >= 0
												|| sameDay(selectedDate,
														todayDate)) {
											addScheduleJobs(jobLayout, i);
										}
									}
								}
							}
						}
					}
					
					// Adding clicker for past accepted jobs
					if(listPastScheduleDate.size() > 0) {
						boolean isPastScheduleEmpty = true;

						for (int j = 0; j < listPastScheduleDate.size(); j++) {
							if (sameDay(listPastScheduleDate.get(j), selectedDate)) {
								isPastScheduleEmpty = false;
							}
						}

						if (!isPastScheduleEmpty) {
							TextView labelMyJob = new TextView(context);
							// params.topMargin = 6;
							labelMyJob.setText(getResources().getString(
									R.string.schedule_tab_my_schedule));
							labelMyJob
									.setTextAppearance(
											context,
											android.R.style.TextAppearance_DeviceDefault_Small);
							// labelMyJob.setTypeface(Typeface.DEFAULT,
							// Typeface.BOLD);
							labelMyJob.setTextColor(Color.WHITE);
							labelMyJob.setPadding(convertDpToPixel(5),
									convertDpToPixel(5), convertDpToPixel(5),
									convertDpToPixel(5));
							labelMyJob.setBackgroundColor(getResources()
									.getColor(R.color.blue_sky));
							labelMyJob.setLayoutParams(params);
							jobLayout.addView(labelMyJob);
							
							// Adding clicker for schedule accepted jobs
							if (listPastScheduleDate.size() > 0) {
								for (int k = 0; k < listPastScheduleDate.size(); k++) {
									if (sameDay(listPastScheduleDate.get(k),
											selectedDate)) {
										Log.d(CommonUtilities.TAG, "List past schedule " + listPastScheduleDate.size());

										// If selected day lesser than today,
										// showing schedule jobs
										if (selectedDate.compareTo(todayDate) <= 0) {
											addPastAcceptedJobs(jobLayout, k);
										}
									}
								}
							}
						}
						
					}
					
					// Don't showing availabilities if clicked date if lesser
					// than today
					if (selectedDate.compareTo(todayDate) >= 0
							|| sameDay(selectedDate, todayDate)) {
						TextView labelMySchedule = new TextView(context);
						labelMySchedule.setText(getResources().getString(
								R.string.schedule_tab_my_availability));
						labelMySchedule
								.setTextAppearance(
										context,
										android.R.style.TextAppearance_DeviceDefault_Small);
						// labelMySchedule.setTypeface(Typeface.DEFAULT,
						// Typeface.BOLD);
						labelMySchedule.setTextColor(Color.WHITE);
						labelMySchedule.setPadding(convertDpToPixel(5),
								convertDpToPixel(5), convertDpToPixel(5),
								convertDpToPixel(5));
						labelMySchedule.setBackgroundColor(getResources()
								.getColor(R.color.blue_sky));
						labelMySchedule.setLayoutParams(params);
						jobLayout.addView(labelMySchedule);

						boolean noAvailability = true;

						if (listAvailabilityDate.size() > 0) {
							for (int i = 0; i < listAvailabilityDate.size(); i++) {
								// Log.d(TAG, "Availability clicked " +
								// selectedDate
								// + " " + listAvailabilityDate.get(i));

								if (sameDay(listAvailabilityDate.get(i),
										selectedDate)) {
									addAvailability(jobLayout, i);
									noAvailability = false;
								}
							}
						}

						Button addAvailButton = addingButtonAddAvailability(selectedCal);
						if (noAvailability) {
							LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
									LinearLayout.LayoutParams.MATCH_PARENT,
									LinearLayout.LayoutParams.WRAP_CONTENT);
							linearParams.setMargins(0, convertDpToPixel(0), 0,
									convertDpToPixel(5));
							addAvailButton.setLayoutParams(linearParams);

							LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
									LinearLayout.LayoutParams.MATCH_PARENT,
									convertDpToPixel(3));
							View borderView = new View(context);
							borderView
									.setBackgroundResource(R.color.lightcolor);
							borderView.setLayoutParams(viewParams);
							jobLayout.addView(borderView);

						}

						jobLayout.addView(addAvailButton);
					}
				}
			}
		}

		@Override
		public void onChangeMonth(int month, int year) {
			Log.d(CommonUtilities.TAG, "onChangeMonth called for calendar " + month + " " + year);

			// If calendar more than today month, don't call history
			Calendar selectedCal = Calendar.getInstance();
			selectedCal.setTime(todayDate);
			
			int todayMonth = selectedCal.get(Calendar.MONTH) + 1;
			int todayYear = selectedCal.get(Calendar.YEAR);
			
			selectedMonth = month;
			selectedYear = year;
			
			if(todayYear > year || (todayMonth >= month && todayYear == year)) {
				if(isLoadData) {
					isSwipe = false;
					isLoadData = false;
					
				} else {
					isSwipe = true;				
				}

				if(isSwipe) {
					pastScheduleHandler.removeCallbacks(pastScheduleRunnable);
					loadHistory(selectedMonth, selectedYear);
				} else {
					loadMonthHistoryInfo();
				}
			}
		}
	};
	
	/**
	 * Loading history
	 */
	private void loadHistory(final int month, final int year) {
		Log.d(CommonUtilities.TAG, "Load history " + month + " year : " + year);
		
		listPastScheduleAvailID = new ArrayList<String>();
		listPastScheduleBranchID = new ArrayList<String>();
		listPastScheduleBranchName = new ArrayList<String>();
		listPastSchedulePrice = new ArrayList<String>();
		listPastScheduleAddress = new ArrayList<String>();
		listPastScheduleCompany = new ArrayList<String>();
		listPastScheduleGrade = new ArrayList<String>();
		listPastScheduleHeader = new ArrayList<String>();
		listPastScheduleDescription = new ArrayList<String>();
		listPastScheduleRequirement = new ArrayList<String>();
		listPastScheduleOptional = new ArrayList<String>();
		listPastScheduleLocation = new ArrayList<String>();
		listPastScheduleStartDateRange = new ArrayList<String>();
		listPastScheduleStartTimeRange = new ArrayList<String>();
		listPastScheduleJobName = new ArrayList<String>();
		listPastScheduleWorkTime = new ArrayList<String>();
		listPastScheduleWorkMoney = new ArrayList<String>();
		listPastSchedulePTGrade = new ArrayList<Integer>();
		listPastScheduleDate = new ArrayList<Date>();
		
		Log.d(TAG, "Loading history " + month + " " + year);

		final String pastUrl = CommonUtilities.SERVERURL
				+ Api.PARAM_GET_PAST_MONTHLY_ACCEPTED_JOBS + "?" 
				+ CommonUtilities.PARAM_PT_ID + "=" + pt_id 
				+ "&" + CommonUtilities.PARAM_YEAR + "=" + year
				+  "&" + CommonUtilities.PARAM_MONTH + "=" + month;
		
		Log.d(CommonUtilities.TAG, "Accessing " + pastUrl + "");

		pastScheduleHandler = new Handler();
		pastScheduleRunnable = new Runnable() {
			public void run() {
				responseString = null;

				totalEarning = "0";
				totalHours = "0";

				if (jsonStr != null) {			
					try {
						JSONObject mainObj = new JSONObject(jsonStr);
						JSONArray testItems = mainObj.getJSONArray(Api.SUB_SLOTS);														
						Log.d(CommonUtilities.TAG, "Sub slots past month : " + testItems.toString());
						
						JSONArray items = null;
						try {
							items = mainObj.getJSONArray(Api.SUB_SLOTS);							
						} catch (Exception e) {
						}
						
//						JSONArray items = new JSONArray(jsonStr);
//						JSONObject historyObj = mainObj
//								.getJSONObject(Api.HISTORY);
//						historyObj = historyObj.getJSONObject(Api.PART_TIMERS);
//
						totalHours = jsonParser.getString(mainObj,
								CommonUtilities.PARAM_TOTAL_TIME);
						totalEarning = jsonParser.getString(mainObj,
								CommonUtilities.PARAM_TOTAL_MONEY);
						
						if (totalHours.equals("null")) {
							totalHours = "0";
						} else {
							if (Integer.parseInt(totalHours.substring(totalHours
											.indexOf(".") + 1)) == 0) {
								totalHours = totalHours.substring(0,
										totalHours.indexOf("."));
							}
						}

						if (totalEarning.equals("null")) {
							totalEarning = "0";
						} else {
							if (Integer.parseInt(totalEarning.substring(totalEarning
											.indexOf(".") + 1)) == 0) {
								totalEarning = totalEarning.substring(0,
										totalEarning.indexOf("."));
							}
						}
						
						if (items != null && items.length() > 0) {
							Calendar calToday = new GregorianCalendar();
							SimpleDateFormat formatterDate = new SimpleDateFormat(
									"EE d, MMM", Locale.getDefault());
							SimpleDateFormat formatterTime = new SimpleDateFormat(
									"hh:mm a", Locale.getDefault());
							for (int i = 0; i < items.length(); i++) {
								/* get all json items, and put it on list */
								try {
									JSONObject objs = items.getJSONObject(i);
									
									if (objs != null) {
										listPastScheduleAvailID.add(jsonParser.getString(objs, "avail_id"));
										listPastScheduleBranchID.add(jsonParser.getString(objs, "branch_id"));
										listPastScheduleBranchName.add(jsonParser.getString(objs, "branch_name"));
										
										String price = "" + jsonParser.getDouble(objs,"offered_salary");
										if (Integer.parseInt(price.substring(price
														.indexOf(".") + 1)) == 0) {
											price = price.substring(0,
													price.indexOf("."));
										}
										
										listPastSchedulePrice
												.add("<font color='#A4A9AF'>$</font><big><big><big><big><font color='#276289'>"
														+ price
														+ "</font></big></big></big></big><font color='#A4A9AF'>/hr</font>");
										
										listPastScheduleAddress.add(jsonParser.getString(objs, "address"));
										listPastScheduleCompany
												.add(jsonParser.getString(objs, "company_name"));
										listPastScheduleRequirement
												.add(jsonParser.getString(objs, "mandatory_requirements"));
										listPastScheduleDescription
												.add(jsonParser.getString(objs, "description"));
										listPastScheduleOptional
												.add(jsonParser.getString(objs, "optional_requirements"));

										listPastScheduleLocation.add(jsonParser.getString(objs, "location"));
										listPastScheduleJobName.add(jsonParser.getString(objs, "job_function_name"));
										listPastScheduleGrade.add(jsonParser.getString(objs, "grade"));

										String workMoney = ""
												+ jsonParser.getString(objs, 
														CommonUtilities.JSON_KEY_NET_WORK_MONEY);
//										if (Integer
//												.parseInt(workMoney.substring(price
//														.indexOf(".") + 1)) == 0) {
//											workMoney = workMoney.substring(0,
//													workMoney.indexOf("."));
//										}
										
										if(workMoney.equals("null")) {
											listPastScheduleWorkMoney.add("");
										} else {
											listPastScheduleWorkMoney.add("<font color='#A4A9AF'>$</font><big><big><big><big><font color='#276289'>"
													+ workMoney
													+ "</font></big></big></big></big>");											
										}
										
										String workTime = jsonParser.getString(objs, 
												CommonUtilities.JSON_KEY_NET_WORK_TIME);
										int workInteger = 0;
										
										try {
											workInteger = Integer.parseInt(workTime);
										} catch(Exception e) {
											
										}
										
										listPastScheduleWorkTime.add(String.valueOf(workInteger));

										String grade = jsonParser.getString(objs, "pt_grade");
										
										if(grade == null) {
											grade = "0";
										}
										
										listPastSchedulePTGrade.add(
												Integer.parseInt(grade));

										String startPastDate = jsonParser
												.getString(objs,
														"start_date_time");
										String endPastDate = jsonParser.getString(
												objs, "end_date_time");
										
										Log.d(CommonUtilities.TAG, "start and end " + startPastDate 
												+ " " + endPastDate);

										Calendar calPastStart = generateCalendar(startPastDate);
										Calendar calPastEnd = generateCalendar(endPastDate);

										String convertStartDate = CommonUtilities.AVAILABILTY_DATETIME
												.format(calPastStart.getTime());
										String convertEndDate = CommonUtilities.AVAILABILTY_DATETIME
												.format(calPastEnd.getTime());

										listPastScheduleDate
												.add(calPastStart.getTime());

										listPastScheduleStartDateRange
												.add(formatterDate
														.format(calPastStart
																.getTime())
														+ "\n");

										listPastScheduleStartTimeRange
												.add(formatterTime
														.format(calPastStart
																.getTime())
														.toLowerCase(
																Locale.getDefault())
														+ " - "
														+ formatterTime
																.format(calPastEnd
																		.getTime())
																.toLowerCase(
																		Locale.getDefault()));

										if (formatterDate
												.format(calPastStart.getTime())
												.equalsIgnoreCase(
														formatterDate
																.format(calToday
																		.getTime()))) {
											listPastScheduleHeader.add("TODAY");

										} else {
											listPastScheduleHeader
													.add(formatterDate
															.format(calPastStart
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
						responseString = NetworkUtils.connectionHandlerString(context, jsonStr,
								e1.getMessage());
						
						if(responseString != null && responseString.length() < 30) {
							Toast.makeText(context.getApplicationContext(), responseString,
									Toast.LENGTH_SHORT).show();
							
							Log.e(CommonUtilities.TAG, "Load past schedule " + jsonStr
									+ " >> " + e1.getMessage());
						}
						
					}
				} else {
					Toast.makeText(context.getApplicationContext(),
							getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
				
				loadMonthHistoryInfo();	
				
				// Updating text view
				TextView totalHoursView = (TextView) view
						.findViewById(R.id.scheduleTotalHours);
				TextView totalEarningView = (TextView) view
						.findViewById(R.id.scheduleTotalEarning);

				try {
					if (totalHoursView != null) {
						totalHoursView.setText(getResources().getString(
								R.string.schedule_total_hours)
								+ " " + totalHours + " hours");
					}

					if (totalEarningView != null) {
						totalEarningView.setText(getResources().getString(
								R.string.schedule_total_income)
								+ " $" + totalEarning);
						
					}
					
				} catch(Exception e) {
					Log.e(CommonUtilities.TAG, "Fragment detached!");
				}
				
				Calendar selectCal = Calendar.getInstance();
				selectCal.set(Calendar.MONTH, month-1);
				selectCal.set(Calendar.YEAR, year);
				selectCal.set(Calendar.DAY_OF_MONTH, 1);
				
				Log.d(TAG, "Selected " + selectCal.get(Calendar.MONTH));
				
				buildScheduleCalendar(selectCal.getTime());				
			}
		};

		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(pastUrl);
				pastScheduleHandler.post(pastScheduleRunnable);
			}
		}.start();
	}

	/**
	 * Loading schedule part-timer
	 */
	private void loadAvailability() {
		progressBar.setVisibility(View.VISIBLE);
		calendarLayout.setVisibility(View.GONE);
		
		final String url = CommonUtilities.SERVERURL
				+ CommonUtilities.API_GET_AVAILABILITIES_BY_PT_ID + "?"
				+ CommonUtilities.PARAM_PT_ID + "=" + pt_id;

		availabilityHandler = new Handler();
		availabilityRunnable = new Runnable() {
			public void run() {
				listAvailabilityID = new ArrayList<String>();
				listRAvailabilityID = new ArrayList<String>();
				listAvailabilityStartTime = new ArrayList<String>();
				listAvailabilityStartDateRange = new ArrayList<String>();
				listAvailabilityStatus = new ArrayList<String>();
				listAvailabilityExpiredAt = new ArrayList<String>();
				listAvailabilityIsRepeat= new ArrayList<Boolean>();
				listAvailabilityEndTime = new ArrayList<String>();
				listAvailabilityRepeat = new ArrayList<List<String>>();
				listAvailabilityEditRepeat = new ArrayList<String>();
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
										listAvailabilityID.add(jsonParser
												.getString(objs, "avail_id"));
										listRAvailabilityID.add(jsonParser
												.getString(objs, "ravail_id"));
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

										listAvailabilityStartTime
												.add(convertStartDate);
										listAvailabilityEndTime
												.add(convertEndDate);

										listAvailabilityLocation.add(jsonParser
												.getString(objs, "location"));
										listAvailabilityPrice
												.add(jsonParser.getString(objs,
														"asked_salary"));
										listAvailabilityFreeze.add(jsonParser
												.getBoolean(objs, "is_frozen"));
										listAvailabilityStatus.add(jsonParser
												.getString(objs, "status"));
										listAvailabilityExpiredAt.add(jsonParser
												.getString(objs, "expired_at"));

										String isRepeated = jsonParser.getString(objs, "ravail_id");
										if(isRepeated.length() > 0 && !isRepeated.equals("null")) {
											listAvailabilityIsRepeat.add(true);											
										} else {
											listAvailabilityIsRepeat.add(false);																						
										}

										List<String> repeatDaysVal = new ArrayList<String>();
										if(isRepeated.length() > 0 && !isRepeated.equals("null")) {
											String repeatDays = "";
											int listIteration = 0;
											
											try {
												JSONArray repeatedDay = objs.getJSONArray("days");
												
												if(repeatedDay.length() > 0) {
													for(int j=0; j < repeatedDay.length(); j++) {
														repeatDaysVal.add(repeatedDay.get(j).toString());													
													}
												}
												
												listAvailabilityRepeat.add(repeatDaysVal);
		
												for(int k = 0; k < repeatDaysVal.size(); k++) {
													for (Map.Entry<Integer, String> entry : mapDays.entrySet()) {
														String repeatDayVal = repeatDaysVal.get(k);
														
														if(entry.getKey() == Integer.parseInt(repeatDayVal)) {
															if(repeatDaysVal.size() > 1) {
																
																if(listIteration == repeatDaysVal.size()-1) {
																	repeatDays += entry.getValue();
																} else {
																	repeatDays += entry.getValue() + ", ";
																}
																
															} else {
																repeatDays += entry.getValue();
															}
														}
													}
													listIteration += 1;
												}
												
												listAvailabilityEditRepeat.add(repeatDays);
												
											} catch (JSONException e) {
												listAvailabilityRepeat.add(repeatDaysVal);
												listAvailabilityEditRepeat.add(repeatDays);
											}
										} else {
											listAvailabilityRepeat.add(repeatDaysVal);
											listAvailabilityEditRepeat.add("");
										}
										
										
										listAvailabilityStartDateRange
												.add(
												// CommonUtilities.AVAILABILITY_DATE
												// .format(calStart.getTime())
												// + "\n" +
												CommonUtilities.AVAILABILITY_TIME
														.format(calStart
																.getTime())
														.toLowerCase(
																Locale.getDefault())
														+ " - "
														+ CommonUtilities.AVAILABILITY_TIME
																.format(calEnd
																		.getTime())
																.toLowerCase(
																		Locale.getDefault())
//														+ "\n"
//														+ "Repeat every - "
														);
									}
								} catch (JSONException e) {
									Log.e(CommonUtilities.TAG,
											"Error Array >> " + e.getMessage());
								}

							}
						} else {
							Log.e(CommonUtilities.TAG, "Array is null");
						}
						
						Log.d(CommonUtilities.TAG, "Show " + listAvailabilityDate.size() + " And " + items.length());
						

					} catch (JSONException e1) {
						NetworkUtils.connectionHandlerString(context, jsonStr,
								e1.getMessage());
						if(responseString != null && responseString.length() < 30) {
							Toast.makeText(context, responseString,
									Toast.LENGTH_SHORT).show();
						}
								
						Log.d(CommonUtilities.TAG, "Availability " + responseString);
					}
				} else {
					Toast.makeText(context, getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}

				// Load past accepted jobs and build calendar refresh
				isRefreshed = true;
				loadHistory(selectedMonth, selectedYear);
			}
		};

		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);
//				Log.d(CommonUtilities.TAG, "Result from " + url + " >>>\n"
//						+ jsonStr);
				availabilityHandler.post(availabilityRunnable);
			}
		}.start();
	}

	/**
	 * Loading default month menu below calendar
	 * Eg: indicate your availability so we can schedule a job for you
	 */
	private void loadMonthHistoryInfo() {
		jobLayout.removeAllViews();
		jobLayout.addView(historyJobView);

		// Add selected schedule
		TextView labelIndicate = new TextView(context);

		try {
			if (labelIndicate != null && getActivity() != null && getActivity().getResources() != null) {
				android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
						android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
						android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);

				labelIndicate.setText(getResources().getString(
						R.string.schedule_indicate));
				labelIndicate.setTextAppearance(context,
						android.R.style.TextAppearance_DeviceDefault_Small);
				labelIndicate.setTextColor(Color.WHITE);
				labelIndicate.setPadding(convertDpToPixel(5), convertDpToPixel(5),
						convertDpToPixel(5), convertDpToPixel(5));

				labelIndicate.setBackgroundColor(getResources().getColor(
						R.color.blue_sky));
				labelIndicate.setLayoutParams(params);
				jobLayout.addView(labelIndicate);

				Button addAvailButton = addingGeneralButtonAddAvailability();
				jobLayout.addView(addAvailButton);
			}			
		} catch (Exception e) {
			
		}
	}

	/**
	 * Load availabilities, history and schedule
	 * It will loading all data in my schedule calendar
	 */
	private void loadData() {
		// Show loading ...
		progressBar.setVisibility(View.VISIBLE);
		calendarLayout.setVisibility(View.GONE);
		
		// This is loading no swipe
		isLoadData = true;
		
		view.invalidate();

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
		t.commitAllowingStateLoss();

		// Setup listener
		final CaldroidListener listener = calendarListener;

		// Setup Caldroid
		caldroidFragment.setCaldroidListener(listener);

		// Loading default menu below calendar to show add availability, indicate, etc..
		loadMonthHistoryInfo();

		// Loading current accepted jobs from today till future
		final String scheduleUrl = SERVERURL
				+ CommonUtilities.API_GET_CURRENT_ACCEPTED_JOB_OFFERS + "?"
				+ PARAM_PT_ID + "=" + pt_id;
		
		scheduleHandler = new Handler();
		scheduleRunnable = new Runnable() {
			public void run() {
				
				listScheduleAvailID = new ArrayList<String>();
				listSchedulePrice = new ArrayList<String>();
				listScheduleAddress = new ArrayList<String>();
				listScheduleCompany = new ArrayList<String>();
				listScheduleStartDateRange = new ArrayList<String>();
				listScheduleStartTimeRange = new ArrayList<String>();
				listScheduleJobName = new ArrayList<String>();
				listScheduleGrade = new ArrayList<String>();
				listScheduleWorkMoney = new ArrayList<String>();
				listScheduleWorkTime = new ArrayList<String>();
				listScheduleHeader = new ArrayList<String>();
				listScheduleDescription = new ArrayList<String>();
				listScheduleRequirement = new ArrayList<List<String>>();
				listScheduleOptional = new ArrayList<List<String>>();
				listScheduleLocation = new ArrayList<String>();
				listScheduleDate = new ArrayList<Date>();
				
				listScheduleFriendsFacebookID = new ArrayList<List<String>>();
				listScheduleFriendsFirstName = new ArrayList<List<String>>();
				listScheduleFriendsLastName = new ArrayList<List<String>>();
				listScheduleFriendsProfilePicture = new ArrayList<List<String>>();
				listScheduleFriendsPtID = new ArrayList<List<String>>();

				if (jsonScheduleStr != null) {
					try {
						Log.d(TAG, "Schedule results from " + scheduleUrl);

						JSONArray items = new JSONArray(jsonScheduleStr);
						
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
										listScheduleAvailID.add(jsonParser
												.getString(objs, "avail_id"));
										
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
										listScheduleAddress.add(jsonParser
												.getString(objs, "address"));
										listScheduleCompany
												.add(jsonParser.getString(objs, "company_name"));
										
										listScheduleDescription
												.add(jsonParser.getString(objs, "description"));

										listScheduleWorkMoney.add("1");
										listScheduleWorkTime.add("1");
										
										listScheduleLocation.add(jsonParser
												.getString(objs, "location"));
										listScheduleJobName.add(jsonParser
												.getString(objs,
														"job_function_name"));
																				
										listScheduleGrade.add(jsonParser
												.getString(objs, "grade"));
										
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

										listScheduleDate
												.add(calStart.getTime());

										listScheduleStartDateRange
												.add(formatterDate
														.format(calStart
																.getTime())
														+ "\n");

										listScheduleStartTimeRange
												.add(formatterTime
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
										
										// Load requirement list
										JSONArray listReqArray = new JSONArray(jsonParser
												.getString(objs, "mandatory_requirements"));	
										
										List<String> listRequirementItem = new ArrayList<String>();
										if (listReqArray != null && listReqArray.length() > 0) {
											for (int h = 0; h < listReqArray.length(); h++) {
												listRequirementItem.add(listReqArray.get(h).toString());	
											}
										}										
										listScheduleRequirement.add(listRequirementItem);
										
										// Load requirement list
										JSONArray listOptionalArray = new JSONArray(jsonParser
												.getString(objs, "optional_requirements"));	

										List<String> listOptionalItem = new ArrayList<String>();
										if (listOptionalArray != null && listOptionalArray.length() > 0) {
											for (int m = 0; m < listOptionalArray.length(); m++) {
												listOptionalItem.add(listOptionalArray.get(m).toString());	
											}
										}
										listScheduleOptional.add(listOptionalItem);
										
										// Load friend list
										JSONArray friends = new JSONArray(jsonParser
												.getString(objs, "friends"));										

										List<String> listScheduleFriendsFacebookIDItem = new ArrayList<String>();
										List<String> listScheduleFriendsFirstNameItem = new ArrayList<String>();
										List<String> listScheduleFriendsLastNameItem = new ArrayList<String>();
										List<String> listScheduleFriendsProfilePictureItem = new ArrayList<String>();
										List<String> listScheduleFriendsPtIDItem = new ArrayList<String>();										
										
										if (friends != null && friends.length() > 0) {
											for (int k = 0; k < friends.length(); k++) {
												JSONObject friendsObjs = friends.getJSONObject(k);
												friendsObjs = friendsObjs.getJSONObject(CommonUtilities.JSON_KEY_PART_TIMER_FRIEND);
																								
												listScheduleFriendsFacebookIDItem.add(jsonParser
														.getString(friendsObjs, CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_ID));

												listScheduleFriendsFirstNameItem.add(jsonParser
														.getString(friendsObjs, CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_FIRST_NAME));

												listScheduleFriendsLastNameItem.add(jsonParser
														.getString(friendsObjs, CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_LAST_NAME));

												listScheduleFriendsProfilePictureItem.add(jsonParser
														.getString(friendsObjs, CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_PROFILE_PICTURE));

												listScheduleFriendsPtIDItem.add(jsonParser
														.getString(friendsObjs, CommonUtilities.PARAM_PT_ID));
											}
										}
										
										listScheduleFriendsFacebookID.add(listScheduleFriendsFacebookIDItem);
										listScheduleFriendsFirstName.add(listScheduleFriendsFirstNameItem);
										listScheduleFriendsLastName.add(listScheduleFriendsLastNameItem);	
										listScheduleFriendsProfilePicture.add(listScheduleFriendsProfilePictureItem);
										listScheduleFriendsPtID.add(listScheduleFriendsPtIDItem);
										
									}
									
								} catch (JSONException e) {
									Log.e("Parse Json Object",
											">> " + e.getMessage());
								}
							}

						} else {
							Log.e("Parse Json Object", ">> Array is null");
						}

					} catch (JSONException e) {
						if(jsonStr != null) {
							responseString = NetworkUtils.connectionHandlerString(context, jsonStr.toString(), e.getMessage());	
							if(responseString != null && responseString.length() < 30) {
								Toast.makeText(context, responseString,
										Toast.LENGTH_SHORT).show();
							}
													
							Log.e(CommonUtilities.TAG, "Error schedule "
									+ " >> " + e.getMessage() + " " + jsonStr);							
						}
					}
				} else {
					Toast.makeText(context.getApplicationContext(),
							getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}

				locationAPIData = settings.getString(CommonUtilities.API_CACHE_LOCATIONS, null);
				
				if(locationAPIData == null) {
					loadLocations();
				} else {
					generateViewLocation();
				}
				
			}
		};

		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonScheduleStr = jsonParser.getHttpResultUrlGet(scheduleUrl);
//				Log.e(CommonUtilities.TAG, "Load schedule " + jsonScheduleStr);
				
				scheduleHandler.post(scheduleRunnable);
			}
		}.start();
	}

	private void buildScheduleCalendar(Date selectedDate) {
		
		if (caldroidFragment != null) {
			int dotIteration = 0;

			if(isRefreshed) {
				if (listAvailabilityDate != null && listAvailabilityDate.size() > 0) {
					for (int i = 0; i < listAvailabilityDate.size(); i++) {
						// Log.d(TAG, "The gray date " +
						// listAvailabilityDate.get(i));
						
						if(listAvailabilityDate.get(i).after(todayDate)) {
							caldroidFragment.setBackgroundResourceForDate(
									R.color.green_lighter, listAvailabilityDate.get(i));
							caldroidFragment.setTextColorForDate(R.color.darktitle,
									listAvailabilityDate.get(i));
						}

						// Calendar cal = Calendar.getInstance();
						// cal.setTime(listAvailabilityDate.get(i));
						// cal.add(Calendar.SECOND, i);

						// caldroidFragment.setPointColorForDateTime(R.drawable.dot_green,
						// new DateTime(cal.getTime()));
						// dotIteration += 1;
					}
				}

				if (listScheduleDate != null && listScheduleDate.size() > 0) {
					for (int i = 0; i < listScheduleDate.size(); i++) {
						caldroidFragment.setBackgroundResourceForDate(
								R.color.calendar_selected_day_bg,
								listScheduleDate.get(i));
						caldroidFragment.setTextColorForDate(R.color.darktitle,
								listScheduleDate.get(i));

						Calendar cal = Calendar.getInstance();
						cal.setTime(listScheduleDate.get(i));
						cal.add(Calendar.SECOND, dotIteration + i);

						caldroidFragment.setPointColorForDateTime(
								R.drawable.dot_green, new DateTime(cal.getTime()));
					}
				}
			}
			
			if (listPastScheduleDate != null && listPastScheduleDate.size() > 0) {
				for (int i = 0; i < listPastScheduleDate.size(); i++) {
					caldroidFragment.setBackgroundResourceForDate(
							R.color.calendar_selected_day_bg,
							listPastScheduleDate.get(i));
					caldroidFragment.setTextColorForDate(R.color.darktitle,
							listPastScheduleDate.get(i));

					Calendar cal = Calendar.getInstance();
					cal.setTime(listPastScheduleDate.get(i));
					cal.add(Calendar.SECOND, dotIteration + i);

					caldroidFragment.setPointColorForDateTime(
							R.drawable.dot_green, new DateTime(cal.getTime()));
				}
			}
		}

		// Update adapter with refresh data
		caldroidFragment.refreshView();
		
		// Check if calendar just refreshed, it should be move to today
		if(isRefreshed) {
			Log.d(CommonUtilities.TAG, "Is refreshed calendar called");
			caldroidFragment.moveToDate(todayDate);
			isRefreshed = false;
			
		} else {
			if(isSwipe) {
				Log.d(CommonUtilities.TAG, "Is swipe called "  + selectedDate.toString());
				caldroidFragment.moveToDate(selectedDate);
				isSwipe = false;
			}
		}
		
		Log.d(CommonUtilities.TAG, "This is being called !");
		progressBar.setVisibility(View.GONE);
		calendarLayout.setVisibility(View.VISIBLE);
		
		if(availabilityRedirect != null) {
			Log.d(CommonUtilities.TAG, "Redirect availability " + availabilityRedirect);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(availabilityRedirect);
			caldroidFragment.moveToDate(calendar.getTime());
			availabilityRedirect = null;
		}
	}

	/**
	 * Convert dp to pixels
	 * 
	 * @param dps
	 * @return
	 */
	private int convertDpToPixel(int dps) {
		final float scale = context.getResources().getDisplayMetrics().density;
		int pixels = (int) (dps * scale + 0.5f);

		return pixels;
	}

	/**
	 * Add details of accepted schedules when user click on calendar
	 * @param jobLayout
	 * @param position
	 */
	private void addScheduleJobs(LinearLayout jobLayout, int position) {
		if (context != null) {
			Log.d(TAG, "Adding schedule " + position);

			LayoutInflater vi = (LayoutInflater) context
					.getApplicationContext().getSystemService(
							Context.LAYOUT_INFLATER_SERVICE);
			View dynamicView = vi.inflate(R.layout.schedule_item_list_calendar,
					null);

			TextView textDate = (TextView) dynamicView
					.findViewById(R.id.textDate);
			TextView textPlace = (TextView) dynamicView
					.findViewById(R.id.textPlace);
			TextView textPrice = (TextView) dynamicView
					.findViewById(R.id.textPrice);
			// TextView textHeader = (TextView) dynamicView
			// .findViewById(R.id.textHeader);

			textPrice.setText(Html.fromHtml(listSchedulePrice.get(position)));
			textDate.setText(listScheduleStartDateRange.get(position)
					+ listScheduleStartTimeRange.get(position));

			textPlace.setText(listScheduleCompany.get(position) + "\n"
					+ listScheduleAddress.get(position));
			// textHeader.setText(listScheduleHeader.get(position));

			dynamicView.setOnClickListener(scheduleOnClick(dynamicView,
					position));
			jobLayout.addView(dynamicView);
		}
	}

	/**
	 * Adding past accepted jobs when user click on Calendar 
	 * @param jobLayout
	 * @param position
	 */
	private void addPastAcceptedJobs(LinearLayout jobLayout, int position) {
		if (context != null) {
			LayoutInflater vi = (LayoutInflater) context
					.getApplicationContext().getSystemService(
							Context.LAYOUT_INFLATER_SERVICE);
			View dynamicView = vi.inflate(R.layout.past_job_item_list_calendar,
					null);

			TextView textDate = (TextView) dynamicView
					.findViewById(R.id.textDate);
			TextView textPlace = (TextView) dynamicView
					.findViewById(R.id.textPlace);
			TextView textDatePrice = (TextView) dynamicView
					.findViewById(R.id.textDatePrice);
			TextView textPrice = (TextView) dynamicView
					.findViewById(R.id.textPrice);

			// TextView textHeader = (TextView) dynamicView
			// .findViewById(R.id.textHeader);
			textDatePrice.setText(Html.fromHtml(listPastScheduleStartTimeRange.get(position)));
			textDatePrice.setTextColor(Color.BLACK);
			
			textPrice.setText(Html.fromHtml(listPastScheduleWorkMoney.get(position)));
			
			textDate.setText(listPastScheduleJobName.get(position));
			textPlace.setText(listPastScheduleCompany.get(position));
			// textHeader.setText(listScheduleHeader.get(position));

			final RatingBar rate = (RatingBar) dynamicView
					.findViewById(R.id.gradeRateJob);
			rate.setNumStars(CommonUtilities.RATING_SCHEDULE);
			rate.setRating((float) Integer.parseInt(listPastScheduleGrade
					.get(position)));
			
			TextView buttonRate = (TextView) dynamicView.findViewById(R.id.rateEmployer);
			if(listPastSchedulePTGrade.get(position) > 0) {
				String rated = ratingArray[4-listPastSchedulePTGrade.get(position)];
				buttonRate.setText(getString(R.string.you_are_rated) + ": " + rated);				
				
			} else {
				buttonRate.setOnClickListener(ratingOnClick(buttonRate, position));
			}

//			dynamicView.setOnClickListener(scheduleOnClick(dynamicView,
//					position));
			jobLayout.addView(dynamicView);
		}
	}

	/**
	 * Add availability details when user click on Calendar
	 * @param jobLayout
	 * @param position
	 */
	// Add availability
	private void addAvailability(LinearLayout jobLayout, int position) {
		if (context != null) {
			LayoutInflater vi = (LayoutInflater) context
					.getApplicationContext().getSystemService(
							Context.LAYOUT_INFLATER_SERVICE);
			View dynamicView = vi.inflate(R.layout.caldroid_availability_list,
					null);

			TextView textDate = (TextView) dynamicView
					.findViewById(R.id.textDate);
			textDate.setText(listAvailabilityStartDateRange.get(position));
			
			String repeatDays = "";
			
			if(listAvailabilityIsRepeat.get(position)) {
				Log.d(TAG, "Selected calendar contains repeated days\n" + 
							listAvailabilityRepeat.get(position).toString() +
							" " + " with mapdays " + mapDays.toString());
				
				int listIteration = 0;				
				for(int k = 0; k < listAvailabilityRepeat.get(position).size(); k++) {
					for (Map.Entry<Integer, String> entry : mapDays.entrySet()) {
						String repeatDayVal = listAvailabilityRepeat.get(position).get(k);
						
						if(entry.getKey() == Integer.parseInt(repeatDayVal)) {
							if(listAvailabilityRepeat.get(position).size() > 1) {
								
								if(listIteration == listAvailabilityRepeat.get(position).size()-1) {
									repeatDays += " and ";
									repeatDays += entry.getValue();
									
								} else {
									repeatDays += entry.getValue() + ", ";
								}
								
							} else {
								repeatDays += entry.getValue();
							}

						}
					}
					listIteration += 1;
				}
				
				if(repeatDays.length() > 0) {
					TextView repeatedTextView = (TextView) dynamicView
							.findViewById(R.id.scheduleRepeated);
//					repeatedTextView.setText("Repeats every " + repeatDays);
				}
				
			}
			
			dynamicView.setOnClickListener(AvailabilityOnClick(dynamicView,
					position));
			jobLayout.addView(dynamicView);
		}
	}

	// Listener for rate your employer
	private View.OnClickListener ratingOnClick(final View view,
			final int position) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
//				// Create custom dialog object
//                rateDialog = new Dialog(context);
//                // Include dialog.xml file
//                rateDialog.setContentView(R.layout.rating_dialog);
//                // Set dialog title
//                rateDialog.setTitle(R.string.rate_your_employer_dialog);
// 
//                // set values for custom dialog components - text, image and button
//                TextView rateText4 = (TextView) rateDialog.findViewById(R.id.rateTextContent1);
//                rateText4.setText(Html.fromHtml("<b>Excelent</b> (eager to work again)"));
//                
//                TextView rateText3 = (TextView) rateDialog.findViewById(R.id.rateTextContent2);
//                rateText3.setText(Html.fromHtml("<b>Good</b> (willing to work again)"));
//                
//                TextView rateText2 = (TextView) rateDialog.findViewById(R.id.rateTextContent3);
//                rateText2.setText(Html.fromHtml("<b>Average</b> (do not mind working again)"));
//                
//                TextView rateText1 = (TextView) rateDialog.findViewById(R.id.rateTextContent4);
//                rateText1.setText(Html.fromHtml("<b>Bad</b> (do not want to work again)"));
// 
//                rateDialog.show();
//
//                TextView submitRate = (TextView) rateDialog.findViewById(R.id.buttonSubmitRate);
//                submitRate.setOnClickListener(submitRatingListener);
//                
//                TextView cancelRate = (TextView) rateDialog.findViewById(R.id.buttonCancelRate);
//                cancelRate.setOnClickListener(cancelRatingListener);
                
//				AlertDialog.Builder builder = new AlertDialog.Builder(context);
//
//				// set title
//				builder.setTitle(getString(R.string.app_name));
//				// set dialog message
//				builder.setMessage(getString(R.string.rate_your_employer));
//				
//				final RatingBar rating = new RatingBar(context);
//				LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//				rating.setNumStars(CommonUtilities.RATING_SCHEDULE);
//				rating.setStepSize(1f);
//				rating.setLayoutParams(params);
//				
//				LinearLayout parent = new LinearLayout(context);
//		        parent.setGravity(Gravity.CENTER);
//		        parent.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
//		                LayoutParams.MATCH_PARENT));
//		        parent.addView(rating);
//		        
//				builder.setView(parent);
//				builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int id) {
//						// SUBMIT RATING HERE
//					}
//				});
//				builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						// Nothing to do here
//					}
//				});
//
//				Dialog dialog = builder.create();
//				dialog.show();
				
//				AlertDialog.Builder builder = new AlertDialog.Builder(context);
//				    builder.setTitle(R.string.rate_your_employer)
//				           .setItems(R.array.rating_employer, new DialogInterface.OnClickListener() {
//				               public void onClick(DialogInterface dialog, int which) {
//				            	   // The 'which' argument contains the index position
//				            	   // of the selected item
//				               }
//				});
//				Dialog dialogSho =  builder.create();
//				dialogSho.show();

				String[] description = getResources().getStringArray(R.array.rating_employer);
				final String[] label = getResources().getStringArray(R.array.rating_employer_label);
				RatingListAdapter adapter = new RatingListAdapter(context, description);

                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.rating_dialog);
                dialog.setCancelable(true);
                dialog.setTitle(R.string.rate_your_employer);
                dialog.show();
                
                ListView lv = (ListView ) dialog.findViewById(R.id.listRatingDialog);
                lv.setAdapter(adapter);
                
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						TextView buttonRateView = (TextView) view.findViewById(R.id.rateEmployer);
						submitRating(position, (int) arg3, dialog, buttonRateView, label[(int)arg3]);
					}
                });
			}
		};	
	}
	
	protected void submitRating(final int position, final int rating,
			final Dialog dialog, final TextView buttonRate, final String rated) {
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_GRADE_EMPLOYER;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {

					if (jsonStr.trim().equalsIgnoreCase("1")) {
						// TODO Auto-generated method stub
						buttonRate.setText(getString(R.string.you_are_rated) + ": " + rated);

					} else if (jsonStr.trim().equalsIgnoreCase("0")) {
						// TODO Auto-generated method stub
						buttonRate.setText(getString(R.string.you_are_rated) + ": " + rated);

					} else if (jsonStr.trim().length() > 0 && !jsonStr.trim().equalsIgnoreCase("0")) {
						NetworkUtils.connectionHandler(context, jsonStr, "");
					}
				} else {
					Toast.makeText(context,
							getString(R.string.something_wrong),
							Toast.LENGTH_LONG).show();
				}
				dialog.dismiss();
			}
		};

		progress = ProgressDialog.show(context,
				getString(R.string.rate_this_job),
				getString(R.string.rate_loading), true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				try {
					JSONObject parentData = new JSONObject();
					JSONObject childData = new JSONObject();
					
					childData.put("branch_id", listPastScheduleBranchID.get(position));
					childData.put("avail_id", listPastScheduleAvailID.get(position));
					childData.put("grade_id", rating);

					parentData.put("grade", childData);

					String[] params = { "data" };
					String[] values = { parentData.toString() };
					jsonStr = jsonParser.getHttpResultUrlPost(url, params,
							values);
					
					Log.e(CommonUtilities.TAG, "Submit data " + childData.toString());
					
					Log.e(CommonUtilities.TAG, "Submit rating " + url + " >>> " + jsonStr);
				} catch (Exception e) {
					jsonStr = null;
				}

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}
	
	
	private OnClickListener submitRatingListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			Log.d(CommonUtilities.TAG, "This is clicked");
			rateDialog.dismiss();
		}
	};
	
	private OnClickListener cancelRatingListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			rateDialog.dismiss();
		}
	};

	/**
	 * Availability details click handler
	 * 
	 * @param view
	 * @param position
	 * @return
	 */
	private View.OnClickListener AvailabilityOnClick(final View view,
			final int position) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO
				Intent i = new Intent(context, DailyAvailabilityPreview.class);
				i.putExtra("pt_id", pt_id);
				i.putExtra("avail_id", listAvailabilityID.get(position));
				i.putExtra("ravail_id", listRAvailabilityID.get(position));
				i.putExtra("start", listAvailabilityStartTime.get(position));
				i.putExtra("end", listAvailabilityEndTime.get(position));
				i.putExtra("repeat", listAvailabilityRepeat.get(position).toString());				
				i.putExtra("repeat_text", listAvailabilityEditRepeat.get(position));
				i.putExtra("location", listAvailabilityLocation.get(position));
				i.putExtra("price", listAvailabilityPrice.get(position));
				i.putExtra("status", listAvailabilityStatus.get(position));
				i.putExtra("expired_at", listAvailabilityExpiredAt.get(position));				
				i.putExtra("is_frozen", listAvailabilityFreeze.get(position));
				i.putExtra("is_repeat", listAvailabilityIsRepeat.get(position));
				i.putExtra("location_array", MapUtils.mapToString(mapLocations));

				startActivityForResult(i, RC_EDIT_AVAILABILITY);
			}
		};
	}

	private View.OnClickListener scheduleOnClick(final View view,
			final int position) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "This is clicked" + 
							listScheduleDescription.get(position));

				Intent jobDetailsIntent = new Intent(context, JobDetails.class);
				jobDetailsIntent.putExtra("price", listSchedulePrice.get(position));
				jobDetailsIntent.putExtra("place", listScheduleCompany.get(position) + "\n"
						+ listScheduleAddress.get(position));
				jobDetailsIntent.putExtra("expire", listScheduleHeader.get(position));
				jobDetailsIntent.putExtra("description", listScheduleDescription.get(position));
				jobDetailsIntent.putExtra("mandatory_requirements", listScheduleRequirement.get(position).toString());
				jobDetailsIntent.putExtra("optional_requirements", listScheduleOptional.get(position).toString());
				jobDetailsIntent.putExtra("location", listScheduleLocation.get(position));
				jobDetailsIntent.putExtra("avail_id", listScheduleAvailID.get(position));
				
				jobDetailsIntent.putExtra(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_ID, listScheduleFriendsFacebookID.get(position).toString());
				jobDetailsIntent.putExtra(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_FIRST_NAME, listScheduleFriendsFirstName.get(position).toString());	
				jobDetailsIntent.putExtra(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_LAST_NAME, listScheduleFriendsLastName.get(position).toString());	
				jobDetailsIntent.putExtra(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_PROFILE_PICTURE, listScheduleFriendsProfilePicture.get(position).toString());
				jobDetailsIntent.putExtra(CommonUtilities.PARAM_PT_ID, listScheduleFriendsPtID.get(position).toString());
				
				jobDetailsIntent.putExtra("type", "accepted");
				startActivityForResult(jobDetailsIntent, RC_SCHEDULE_DETAIL);
			}
		};
	}

	/**
	 * Adding Availability Button
	 * @param selectedCal
	 * @return
	 */
	private Button addingButtonAddAvailability(final Calendar selectedCal) {
		Button addAvail = new Button(context);

		// LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		// LinearLayout.LayoutParams.MATCH_PARENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT);
		// params.setMargins(0, convertDpToPixel(5), 0, convertDpToPixel(5));

		addAvail.setText(getResources().getString(
				R.string.schedule_add_availability));
		addAvail.setTextAppearance(context,
				android.R.style.TextAppearance_DeviceDefault_Small);
		addAvail.setBackgroundResource(R.color.lightcolor);
		addAvail.setPadding(convertDpToPixel(5), convertDpToPixel(5),
				convertDpToPixel(5), convertDpToPixel(5));
		// addAvail.setLayoutParams(params);

		addAvail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				goToAvailabilityForm(selectedCal);
			}
		});

		return addAvail;
	}

	BroadcastReceiver scheduleReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			// Update adapter with refresh data
			SimpleDateFormat availabilityDateTime = CommonUtilities.AVAILABILTY_DATETIME;
			
			String availMonth = intent.getStringExtra("month");
			if(availMonth != null) {
				try {
					availabilityRedirect = availabilityDateTime.parse(availMonth);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			loadData();
		}
	};

	// Reset history menu below calendar
	private BroadcastReceiver historyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get extra data included in the Intent
			loadMonthHistoryInfo();
		}
	};
	

	// Our handler for received backpressed
	private BroadcastReceiver mBackPressedReceiver = new BroadcastReceiver() {
	  @Override
	  public void onReceive(Context context, Intent intent) {
		  DateTime todayDatetime = new DateTime();
		  Log.d(CommonUtilities.TAG, 
				  " " + selectedMonth + " " + selectedYear +" " + todayDatetime.getMonthOfYear());
		  
		  if((todayDatetime.getMonthOfYear() == selectedMonth) && 
			(todayDatetime.getYear() == selectedYear)) {
			  if(getActivity() != null) {
				  getActivity().finish();				  
			  }
		  } else {
			  loadData();
		  }
	  }
	};

	/**
	 * Loading days
	 */
	private void loadDays() {
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_DAYS;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				responseString = null;

				if (jsonStr != null) {
					try {
						JSONArray items = new JSONArray(jsonStr);
						
						// Store data into cache
						editor = settings.edit();
						editor.putString(CommonUtilities.API_CACHE_LOCATIONS, items.toString());
						editor.commit();

						dayAPIData = items.toString();
						generateViewDays();
						
					} catch (JSONException e1) {
						NetworkUtils.connectionHandler(context, jsonStr,
								e1.getMessage());

						Log.e(CommonUtilities.TAG, "Load schedule " + jsonStr
								+ " >> " + e1.getMessage());
					}
					
					
					
				} else {
					Toast.makeText(context.getApplicationContext(),
							getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
				
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
	
	private void generateViewDays() {
		jsonParser = new JSONParser();
		
		try {
			JSONArray items = new JSONArray(dayAPIData);

			if (items != null && items.length() > 0) {							
				for (int i = 0; i < items.length(); i++) {
					/* get all json items, and put it on list */
					try {
						JSONObject objs = items.getJSONObject(i);
						objs = objs.getJSONObject(CommonUtilities.JSON_KEY_REPEAT_DAYS);
						
						if (objs != null) {
							String dayName = jsonParser.getString(objs,
									CommonUtilities.JSON_KEY_REPEAT_DAY);
							int dayID = Integer.parseInt(jsonParser.getString(objs,
									CommonUtilities.JSON_KEY_REPEAT_DAY_ID));
							
							mapDays.put(dayID, dayName);
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
			NetworkUtils.connectionHandler(context, jsonStr,
					e1.getMessage());
		}
		

		loadAvailability();
	}
	
	/**
	 * Loading locations
	 */
	private void loadLocations() {
		Log.d(TAG, "Loading locations ...");

		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_LOCATIONS;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				responseString = null;

				if (jsonStr != null) {
					try {
						JSONArray items = new JSONArray(jsonStr);

						// Store data into cache
						editor = settings.edit();
						editor.putString(CommonUtilities.API_CACHE_LOCATIONS, items.toString());
						editor.commit();

						locationAPIData = items.toString();

						generateViewLocation();
						
					} catch (JSONException e1) {
						NetworkUtils.connectionHandler(context, jsonStr,
								e1.getMessage());

					}
				} else {
					Toast.makeText(context.getApplicationContext(),
							getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
				
				loadDays();
				
			}
		};
		
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);
				Log.e(CommonUtilities.TAG, "Load locations " + jsonStr);
				mHandlerFeed.post(mUpdateResultsFeed);
				
			}
		}.start();
	}
	

	private void generateViewLocation() {
		jsonParser = new JSONParser();
		
		try {
			JSONArray items = new JSONArray(locationAPIData);

			if (items != null && items.length() > 0) {							
				for (int i = 0; i < items.length(); i++) {
					/* get all json items, and put it on list */
					try {
						JSONObject objs = items.getJSONObject(i);
//						objs = objs.getJSONObject(CommonUtilities.JSON_KEY_LOCATIONS);
						
						if (objs != null) {
							Log.d(CommonUtilities.TAG, "Location data " + objs.toString());
							
							String locationName = jsonParser.getString(objs,
									CommonUtilities.JSON_KEY_LOCATION_NAME);
							
							String locationID = jsonParser.getString(objs,
									CommonUtilities.JSON_KEY_LOCATION_ID);
							mapLocations.put(locationID, locationName);
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
			NetworkUtils.connectionHandler(context, jsonStr,
					e1.getMessage());
		}
		
		dayAPIData = settings.getString(CommonUtilities.API_CACHE_DAYS, null);
		
		if(dayAPIData == null) {
			loadDays();
		} else {
			generateViewDays();
		}
	}
	
	
	@Override
	public void onDestroy() {
		Log.e(TAG, "onDestroy !!!");
		scheduleHandler.removeCallbacks(scheduleRunnable);
		availabilityHandler.removeCallbacks(availabilityRunnable);
		pastScheduleHandler.removeCallbacks(pastScheduleRunnable);
		
		super.onDestroy();

		try {
			context.unregisterReceiver(scheduleReceiver);
		} catch (IllegalArgumentException e) {

		}

		try {
			context.unregisterReceiver(historyReceiver);
		} catch (IllegalArgumentException e) {

		}

	}
	
	@Override
	public void onStop() {
		Log.e(TAG, "onStop !!!");
		if(scheduleHandler != null) {
			scheduleHandler.removeCallbacks(scheduleRunnable);
		}
		
		if(availabilityHandler != null) {
			availabilityHandler.removeCallbacks(availabilityRunnable);
		}
		
		if(pastScheduleHandler != null) {
			pastScheduleHandler.removeCallbacks(pastScheduleRunnable);
		}
		
		super.onStop();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(CommonUtilities.TAG, "Schedule Fragment Is called : onActivityResult()");
		
		super.onActivityResult(requestCode, resultCode, data);

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


	private Button addingGeneralButtonAddAvailability() {
		Button addAvail = new Button(context);
		addAvail.setText(getResources().getString(
				R.string.schedule_add_availability));
		addAvail.setTextAppearance(context,
				android.R.style.TextAppearance_DeviceDefault_Small);
		addAvail.setBackgroundResource(R.color.lightcolor);
		addAvail.setPadding(convertDpToPixel(5), convertDpToPixel(5),
				convertDpToPixel(5), convertDpToPixel(5));
		addAvail.setOnClickListener(jobAvailabilityListener);

		return addAvail;
	}

	private void goToAvailabilityForm(Calendar selectedCal) {
		Intent createAvailability = new Intent(context, CreateAvailability.class);
		createAvailability.putExtra(AVAILABILITY_SELECTED,
				CommonUtilities.AVAILABILTY_DATETIME.format(selectedCal
						.getTime()));
		context.startActivity(createAvailability);
	}

	private static boolean sameDay(Calendar cal, Calendar selectedDate) {
		return cal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
				&& cal.get(Calendar.DAY_OF_YEAR) == selectedDate
						.get(Calendar.DAY_OF_YEAR);
	}

	private android.view.View.OnClickListener jobAvailabilityListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(context, CreateAvailability.class);
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
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onPause !!!");
		if(scheduleHandler != null) {
			scheduleHandler.removeCallbacks(scheduleRunnable);			
		}

		if(availabilityHandler != null) {
			availabilityHandler.removeCallbacks(availabilityRunnable);			
		}

		if(pastScheduleHandler != null) {
			pastScheduleHandler.removeCallbacks(pastScheduleRunnable);			
		}
		
		super.onPause();
	}
	
}
