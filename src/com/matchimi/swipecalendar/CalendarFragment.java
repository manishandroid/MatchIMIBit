package com.matchimi.swipecalendar;

import static com.matchimi.CommonUtilities.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.antonyt.infiniteviewpager.InfinitePagerAdapter;
import com.antonyt.infiniteviewpager.InfiniteViewPager;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.availability.DailyAvailabilityPreview;
import com.matchimi.calendar.CalendarPickerView;
import com.matchimi.options.CreateAvailability;
import com.matchimi.options.JobDetails;
import com.matchimi.options.ScheduleDetailsActivity;

/**
 * Fragment showing calendar list that can swipe and infinite
 * 
 * @author yodi
 * 
 */
public class CalendarFragment extends Fragment {
	public static final String MONTH = "month";
	public static final String YEAR = "year";
	public final static int NUMBER_OF_PAGES = 4;

	public static final int RC_EDIT_AVAILABILITY = 30;
	
	private Button leftArrowButton;
	private Button rightArrowButton;
	private int year;
	private int month;

	private CalendarChangeListener calendarChangeListener;
	private DateTime currentDateTime;

	private ArrayList<CalendarSingleFragment> fragments;
	private ArrayList<Bundle> bundleCalendar = new ArrayList<Bundle>();
	private InfiniteViewPager calendarViewPager;
	private CalendarPickerView calendar;
	private CalendarPagerAdapter pagerAdapter;

	private String[] occupiedDates;
	private String pt_id;
	private String[] listAvailID;
	private String[] listStartTime;
	private String[] listEndTime;
	private String[] listRepeat;
	private String[] listLocation;
	private String[] listAddress;
	private String[] listCompany;
	private String[] listPrice;
	private String[] listJobID;
	private String[] listHeader;
	private String[] listSchedule;
	private String[] listDescription;
	private String[] listRequirement;
	private String[] listOptional;	
	
	private String[] listScAvailID = null;
	private String[] listScStartTime = null;
	private String[] listScEndTime = null;
	private String[] listScRepeat = null;
	private String[] listScLocation = null;
	private String[] listScDate = null;
	private String[] listScPrice = null;
	private String[] listScFreeze = null;
	
	private int totalHours = 0;
	private int totalEarning = 0;
	
	private int monthRow = 5;
	private LinearLayout linearLayout;

	private Button jobAvailaibilityButton;

	private List<Integer> listPosition;
	private List<Integer> listPositionAvail;

	private LinearLayout jobLayout;
	private FrameLayout frameLayout;
	
	private boolean isAvailability = false;
	private int FRAMELAYOUT_HEIGHT_6 = 350;
	private int FRAMELAYOUT_HEIGHT_5 = 305;

	public Button getLeftArrowButton() {
		return leftArrowButton;
	}

	public Button getRightArrowButton() {
		return rightArrowButton;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.calendar_pager, container, false);
		jobLayout = (LinearLayout) view
				.findViewById(R.id.calendar_jobs_selected);

		Bundle bundle = getArguments();
		isAvailability = bundle.getBoolean(IS_AVAILABILITY_CALENDAR);

		Log.d(TAG, "Is availabilty " + isAvailability);

		// If schedule mode
		if (isAvailability == false) {
			occupiedDates = bundle.getStringArray(OCCUPIED_DATES);
			listAvailID = bundle.getStringArray(SCHEDULE_LIST_AVAIL_ID);
			listStartTime = bundle.getStringArray(SCHEDULE_START_TIME);
			listEndTime = bundle.getStringArray(SCHEDULE_END_TIME);
			listAddress = bundle.getStringArray(SCHEDULE_ADDRESS);
			listLocation = bundle.getStringArray(SCHEDULE_DATA_LOCATION);
			listCompany = bundle.getStringArray(SCHEDULE_COMPANY);
			listPrice = bundle.getStringArray(SCHEDULE_PRICE);
			listJobID = bundle.getStringArray(SCHEDULE_LIST_JOB_ID);
			listHeader = bundle.getStringArray(SCHEDULE_HEADER);
			listSchedule = bundle.getStringArray(SCHEDULE_TIMEWORK);
			listDescription= bundle.getStringArray(SCHEDULE_DESCRIPTION);
			listRequirement = bundle.getStringArray(SCHEDULE_LIST_REQUIREMENT);
			listOptional = bundle.getStringArray(SCHEDULE_LIST_OPTIONAL);

			listScAvailID = bundle.getStringArray(AVAIL_ID);
			listScStartTime = bundle.getStringArray(AVAIL_START_TIME);
			listScEndTime = bundle.getStringArray(AVAIL_END_TIME);
			listScRepeat = bundle.getStringArray(AVAIL_REPEAT);
			listScLocation = bundle.getStringArray(AVAIL_LOCATION);
			listScDate = bundle.getStringArray(AVAIL_DATE);
			listScPrice = bundle.getStringArray(AVAIL_PRICE);
			listScFreeze = bundle.getStringArray(AVAIL_FREEZE);
			totalEarning = bundle.getInt(TOTAL_EARNING);
			totalHours = bundle.getInt(TOTAL_HOURS);
			
		} else {
			occupiedDates = bundle.getStringArray(OCCUPIED_DATES);

			listAvailID = bundle.getStringArray(AVAILABILITY_LIST_AVAIL_ID);
			listStartTime = bundle.getStringArray(AVAILABILITY_LIST_START_TIME);
			listEndTime = bundle.getStringArray(AVAILABILITY_LIST_END_TIME);
			listLocation = bundle.getStringArray(AVAILABILITY_LIST_LOCATION);
			listRepeat = bundle.getStringArray(AVAILABILITY_LIST_REPEAT);
			
		}

		pt_id = bundle.getString(PARAM_PART_TIMER);

		// Configure calendar
		Calendar cal = Calendar.getInstance();
		month = cal.get(Calendar.MONTH) + 1;
		year = cal.get(Calendar.YEAR);

		currentDateTime = new DateTime(year, month, 1, 0, 0, 0);

		calendarChangeListener = new CalendarChangeListener();
		calendarChangeListener.setCurrentDateTime(currentDateTime);

		List<String> calTime = new ArrayList<String>();
		if (listStartTime != null && listStartTime.length > 0) {
			for (String s : listStartTime) {
				calTime.add(s);
			}
		}
		if (listScStartTime != null && listScStartTime.length > 0) {
			for (String s : listScStartTime) {
				calTime.add(s);
			}
		}
		
		// Adding 4 month, previous, current and next 2
		Bundle currentCal = new Bundle();
		currentCal
				.putInt(CALENDAR_DATE_MONTH, currentDateTime.getMonthOfYear());
		currentCal.putInt(CALENDAR_DATE_YEAR, currentDateTime.getYear());
		currentCal.putBoolean(IS_AVAILABILITY_CALENDAR, isAvailability);
		currentCal.putStringArray(OCCUPIED_DATES, occupiedDates);

		currentCal.putString(PARAM_PART_TIMER, pt_id);
		currentCal.putStringArray(AVAILABILITY_LIST_AVAIL_ID, listAvailID);
		currentCal.putStringArray(AVAILABILITY_LIST_START_TIME, listScStartTime);
		currentCal.putStringArray(AVAILABILITY_LIST_END_TIME, listEndTime);
		currentCal.putStringArray(AVAILABILITY_LIST_REPEAT, listRepeat);
		currentCal.putStringArray(AVAILABILITY_LIST_LOCATION, listLocation);

		// Next month
		DateTime nextMonth = currentDateTime.plusMonths(1);
		Bundle nextCal = new Bundle();
		nextCal.putInt(CALENDAR_DATE_MONTH, nextMonth.getMonthOfYear());
		nextCal.putInt(CALENDAR_DATE_YEAR, nextMonth.getYear());
		nextCal.putBoolean(IS_AVAILABILITY_CALENDAR, isAvailability);
		nextCal.putStringArray(OCCUPIED_DATES, occupiedDates);

		nextCal.putString(PARAM_PART_TIMER, pt_id);
		nextCal.putStringArray(AVAILABILITY_LIST_AVAIL_ID, listAvailID);
		nextCal.putStringArray(AVAILABILITY_LIST_START_TIME, listScStartTime);
		nextCal.putStringArray(AVAILABILITY_LIST_END_TIME, listEndTime);
		nextCal.putStringArray(AVAILABILITY_LIST_REPEAT, listRepeat);
		nextCal.putStringArray(AVAILABILITY_LIST_LOCATION, listLocation);

		// Next 2 month
		DateTime next2Month = currentDateTime.plusMonths(2);
		Bundle next2Cal = new Bundle();
		next2Cal.putInt(CALENDAR_DATE_MONTH, next2Month.getMonthOfYear());
		next2Cal.putInt(CALENDAR_DATE_YEAR, next2Month.getYear());
		next2Cal.putBoolean(IS_AVAILABILITY_CALENDAR, isAvailability);
		next2Cal.putStringArray(OCCUPIED_DATES, occupiedDates);

		next2Cal.putString(PARAM_PART_TIMER, pt_id);
		next2Cal.putStringArray(AVAILABILITY_LIST_AVAIL_ID, listAvailID);
		next2Cal.putStringArray(AVAILABILITY_LIST_START_TIME, listScStartTime);
		next2Cal.putStringArray(AVAILABILITY_LIST_END_TIME, listEndTime);
		next2Cal.putStringArray(AVAILABILITY_LIST_REPEAT, listRepeat);
		next2Cal.putStringArray(AVAILABILITY_LIST_LOCATION, listLocation);

		// Previous month
		DateTime prevMonth = currentDateTime.minusMonths(1);
		Bundle prevCal = new Bundle();
		prevCal.putInt(CALENDAR_DATE_MONTH, prevMonth.getMonthOfYear());
		prevCal.putInt(CALENDAR_DATE_YEAR, prevMonth.getYear());
		prevCal.putBoolean(IS_AVAILABILITY_CALENDAR, isAvailability);
		prevCal.putStringArray(OCCUPIED_DATES, occupiedDates);

		prevCal.putString(PARAM_PART_TIMER, pt_id);
		prevCal.putStringArray(AVAILABILITY_LIST_AVAIL_ID, listAvailID);
		prevCal.putStringArray(AVAILABILITY_LIST_START_TIME, listScStartTime);
		prevCal.putStringArray(AVAILABILITY_LIST_END_TIME, listEndTime);
		prevCal.putStringArray(AVAILABILITY_LIST_REPEAT, listRepeat);
		prevCal.putStringArray(AVAILABILITY_LIST_LOCATION, listLocation);

		bundleCalendar.add(currentCal);
		bundleCalendar.add(nextCal);
		bundleCalendar.add(next2Cal);
		bundleCalendar.add(prevCal);

		// Set bundleCalendar to calendarSwipeListener, so it can refresh the
		// bundleUpdater when page change
		calendarChangeListener.setBundleCalendar(bundleCalendar);

		// Setup InfiniteViewPager and InfinitePagerAdapter. The
		// InfinitePagerAdapter is responsible
		// for reuse the fragments
		calendarViewPager = (InfiniteViewPager) view
				.findViewById(R.id.months_infinite_pager);
		
		// MonthPagerAdapter actually provides 4 real fragments. The
		// InfinitePagerAdapter only recycles fragment provided by this
		// MonthPagerAdapter
		pagerAdapter = new CalendarPagerAdapter(getChildFragmentManager());

		// Provide initial data to the fragments, before they are attached to
		// view.
		fragments = pagerAdapter.getFragments();
		for (int i = 0; i < NUMBER_OF_PAGES; i++) {
			CalendarSingleFragment calendarSingleFragment = fragments.get(i);
			Bundle fragmentUpdater = bundleCalendar.get(i);
			calendarSingleFragment.setArguments(fragmentUpdater);
		}

		// Setup InfinitePagerAdapter to wrap around MonthPagerAdapter
		InfinitePagerAdapter infinitePagerAdapter = new InfinitePagerAdapter(
				pagerAdapter);

		// Use the infinitePagerAdapter to provide data for dateViewPager
		calendarViewPager.setAdapter(infinitePagerAdapter);

		// Setup calendarChangeListener for infiniteViewPager
		calendarViewPager.setOnPageChangeListener(calendarChangeListener);
		
//		Log.d(TAG, String.format("Set current page %d and current page %d",
//				calendarViewPager.getCurrentItem(),
//				calendarChangeListener.getCurrentPage()));

		// Configure left and right button
		leftArrowButton = (Button) view.findViewById(R.id.calendar_left_arrow);
		leftArrowButton.bringToFront();
		leftArrowButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				prevMonth();
			}
		});

		rightArrowButton = (Button) view
				.findViewById(R.id.calendar_right_arrow);
		rightArrowButton.bringToFront();
		rightArrowButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				nextMonth();
			}
		});
		
//		jobAvailaibilityButton = (Button) view
//				.findViewById(R.id.scheduleButtonAddAvailability);
//		jobAvailaibilityButton.setOnClickListener(jobAvailabilityListener);

		LinearLayout historyJobLayout = (LinearLayout) view.findViewById(R.id.history_past_job);
		frameLayout = (FrameLayout) view.findViewById(R.id.calendar_title_view);		
		
		if(isAvailability) {
			historyJobLayout.setVisibility(View.GONE);
		} else {
			TextView totalHoursView = (TextView) view.findViewById(R.id.scheduleTotalHours);
			TextView totalEarningView = (TextView) view.findViewById(R.id.scheduleTotalEarning);
			
			totalHoursView.setText(getResources().getString(R.string.schedule_total_hours) + " " + totalHours + " hour");
			totalEarningView.setText(getResources().getString(R.string.schedule_total_income) + " $" + totalEarning);
			
		}
		
		// RECEIVER FROM SCHEDULE CALENDAR CLICKED
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				mMessageReceiver,
				new IntentFilter(CommonUtilities.SCHEDULE_LOCAL_BROADCAST));

		return view;
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

			textPrice.setText(Html.fromHtml(listPrice[position]));
			textDate.setText(listSchedule[position]);
			textPlace.setText(listCompany[position] + "\n" + listAddress[position]);
//			textHeader.setText(listHeader[position]);
			
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
			textDate.setText(listScDate[position]);

			dynamicView.setOnClickListener(ActivityOnClick(dynamicView, position));
			jobLayout.addView(dynamicView);			
		}
	}
	
	private View.OnClickListener scheduler = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.d(TAG, "This is clicked");
		}
	};
	
	private View.OnClickListener detailScheduleListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent i = new Intent(getActivity(), ScheduleDetailsActivity.class);
			startActivity(i);
		}
	};

	private View.OnClickListener ActivityOnClick(final View view, final int position) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO
				Intent i = new Intent(getActivity(), DailyAvailabilityPreview.class);
				i.putExtra("pt_id", pt_id);
				i.putExtra("avail_id", listScAvailID[position]);
				i.putExtra("start", listScStartTime[position]);
				i.putExtra("end", listScEndTime[position]);
				i.putExtra("repeat", listScRepeat[position]);
				i.putExtra("location", listScLocation[position]);
				i.putExtra("price", listScPrice[position]);
				i.putExtra("is_frozen", Boolean.parseBoolean(listScFreeze[position]));

				startActivityForResult(i, RC_EDIT_AVAILABILITY);
			}
		};
	}
	
	private View.OnClickListener scheduleOnClick(final View view, final int position) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "This is clicked" + listDescription[position]);
				
				Intent i = new Intent(getActivity(), JobDetails.class);
				i.putExtra("price", listPrice[position]);
				i.putExtra("date", listSchedule[position]);
				i.putExtra("place",
						listCompany[position] + "\n" + listAddress[position]);
				i.putExtra("expire", listHeader[position]);
				i.putExtra("description", listDescription[position]);
				i.putExtra("requirement", listRequirement[position]);
				i.putExtra("optional", listOptional[position]);
				i.putExtra("location", listLocation[position]);				
				i.putExtra("id", listAvailID[position]);
				i.putExtra("type", "accepted");
				startActivityForResult(i, RC_SCHEDULE_DETAIL);
			}
		};
	}
	
	/**
	 * RECEIVER FROM CALENDAR SCHEDULER
	 */
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String currentSpeed = intent
					.getStringExtra(CommonUtilities.SCHEDULE_LOCAL_BROADCAST);

			Date selectedDate = new Date();
			try {
				selectedDate = CommonUtilities.AVAILABILTY_DATETIME
						.parse(currentSpeed);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			final Calendar selectedCal = Calendar.getInstance();
			selectedCal.setTime(selectedDate);

			Log.d(TAG, "Get clicked date " + currentSpeed);
			listPosition = new ArrayList<Integer>();

			for (int i = 0; i < listStartTime.length; i++) {
				Date convertStartDate;
				try {
					convertStartDate = CommonUtilities.AVAILABILTY_DATETIME
							.parse(listStartTime[i]);
					Calendar convertCal = Calendar.getInstance();
					convertCal.setTime(convertStartDate);

					if (sameDay(selectedCal, convertCal)) {
						listPosition.add(i);
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			listPositionAvail = new ArrayList<Integer>();
			if (listScStartTime != null && listScStartTime.length > 0) {
				for (int i = 0; i < listScStartTime.length; i++) {
					Date convertStartDate;
					try {
						convertStartDate = CommonUtilities.AVAILABILTY_DATETIME
								.parse(listScStartTime[i]);
						Calendar convertCal = Calendar.getInstance();
						convertCal.setTime(convertStartDate);

						if (sameDay(selectedCal, convertCal)) {
							listPositionAvail.add(i);
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			// Clear all views
			jobLayout.removeAllViews();

			if (listPosition.size() == 0 && listPositionAvail.size() == 0) {
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
	
					for (Integer position : listPositionAvail) {
						Log.e(TAG, "Iterate position Avail " + position);
						addAvailability(jobLayout, position);
					}
					
					Button addAvailButton = addingButtonAddAvailability(selectedCal);
					jobLayout.addView(addAvailButton);
					
					if (listPosition.size() > 0) {
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
						for (Integer position : listPosition) {
							Log.d(TAG, "Iterate position " + position);
		
							addScheduleJobs(jobLayout, position);
						}
					}
				}
			}
		}
	};
	
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

	/**
	 * Set availability data
	 */
	public void setAvailabilityData() {
		Log.d(TAG, "Set availability data");
	}

	/**
	 * Set calendar to previous month
	 */
	public void prevMonth() {
		Log.d(TAG,
				String.format("Previous month %d",
						calendarChangeListener.getCurrentPage()));
		int position = calendarChangeListener.getCurrentPage() - 1;
		Integer[] positionArray = { position };
		new SwitchCalendar().execute(positionArray);
	}

	/**
	 * Set calendar to next month
	 */
	public void nextMonth() {
		Log.d(TAG,
				String.format("Next month %d",
						calendarChangeListener.getCurrentPage()));
		int position = calendarChangeListener.getCurrentPage() + 1;
		Integer[] positionArray = { position };
		new SwitchCalendar().execute(positionArray);
	}

	public void setCalendardateTime(DateTime dateTime) {
		month = dateTime.getMonthOfYear();
		year = dateTime.getYear();
	}

	public class CalendarChangeListener implements OnPageChangeListener {
		private int currentPage = InfiniteViewPager.OFFSET;
		private DateTime currentDateTime;
		private DateTime previousDateTime;
		private DateTime nextDateTime;

		private ArrayList<Bundle> bundleCalendar;

		/**
		 * Return currentpage of calendar view pager
		 * 
		 * @return
		 */
		public int getCurrentPage() {
			return currentPage;
		}

		/**
		 * Set currentpage of calendar view pager
		 * 
		 * @param currentPage
		 */
		public void setCurrentPage(int currentPage) {
			this.currentPage = currentPage;
		}

		/**
		 * Return current Datetime of selected page
		 * 
		 * @return
		 */
		public DateTime getCurrentDateTime() {
			return currentDateTime;
		}

		/**
		 * Set current Datetime of selected page
		 */
		public void setCurrentDateTime(DateTime dateTime) {
			this.currentDateTime = dateTime;
		}

		/**
		 * Return 4 Bundle calendars
		 */
		public ArrayList<Bundle> getBundleCalendar() {
			return bundleCalendar;
		}

		/**
		 * Set Bundle calendar
		 */
		public void setBundleCalendar(ArrayList<Bundle> bundleCalendar) {
			this.bundleCalendar = bundleCalendar;
		}

		/**
		 * Return current Datetime of selected page
		 * 
		 * @param position
		 * @return
		 */
		public int getCurrent(int position) {
			return position % CalendarFragment.NUMBER_OF_PAGES;
		}

		public int getNext(int position) {
			return (position + 1) % CalendarFragment.NUMBER_OF_PAGES;
		}

		public int getPrevious(int position) {
			return (position - 1) % CalendarFragment.NUMBER_OF_PAGES;
		}

		@Override
		public void onPageScrollStateChanged(int position) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrolled(int position, float arg1, int arg2) {
			// TODO Auto-generated method stub
		}

		public void refreshCalendarPosition(int position) {
			// Log.d(TAG, String.format(
			// "Update position %d and currentPage %d, previous %d",
			// position, getCurrent(position), getPrevious(position)));

			// Clear all views
//			jobLayout.removeAllViews();
			
			// Adding 4 month, previous, current and next 2
			Bundle currentCal = bundleCalendar.get(getCurrent(position));
			Bundle previousCal = bundleCalendar.get(getPrevious(position));
			Bundle nextCal = bundleCalendar.get(getNext(position));

			List<String> calTime = new ArrayList<String>();
			if (listStartTime != null && listStartTime.length > 0) {
				for (String s : listStartTime) {
					calTime.add(s);
				}
			}
			if (listScStartTime != null && listScStartTime.length > 0) {
				for (String s : listScStartTime) {
					calTime.add(s);
				}
			}
			
			if (position == currentPage) {
				currentCal.putInt(CALENDAR_DATE_MONTH,
						currentDateTime.getMonthOfYear());
				currentCal
						.putInt(CALENDAR_DATE_YEAR, currentDateTime.getYear());
				currentCal.putBoolean(IS_AVAILABILITY_CALENDAR, isAvailability);
				currentCal.putStringArray(OCCUPIED_DATES, occupiedDates);

				currentCal.putString(PARAM_PART_TIMER, pt_id);
				currentCal.putStringArray(AVAILABILITY_LIST_AVAIL_ID,
						listAvailID);
				currentCal.putStringArray(AVAILABILITY_LIST_START_TIME,
						listScStartTime);
				currentCal.putStringArray(AVAILABILITY_LIST_END_TIME,
						listEndTime);
				currentCal.putStringArray(AVAILABILITY_LIST_REPEAT, listRepeat);
				currentCal.putStringArray(AVAILABILITY_LIST_LOCATION,
						listLocation);
//
//				Log.d(TAG, String.format("Set current month %d and year %d",
//						currentDateTime.getMonthOfYear(),
//						currentDateTime.getYear()));

				previousDateTime = currentDateTime.minusMonths(1);
				previousCal.putInt(CALENDAR_DATE_MONTH,
						previousDateTime.getMonthOfYear());
				previousCal.putInt(CALENDAR_DATE_YEAR,
						previousDateTime.getYear());
				previousCal
						.putBoolean(IS_AVAILABILITY_CALENDAR, isAvailability);
				previousCal.putStringArray(OCCUPIED_DATES, occupiedDates);

				previousCal.putString(PARAM_PART_TIMER, pt_id);
				previousCal.putStringArray(AVAILABILITY_LIST_AVAIL_ID,
						listAvailID);
				previousCal.putStringArray(AVAILABILITY_LIST_START_TIME,
						listScStartTime);
				previousCal.putStringArray(AVAILABILITY_LIST_END_TIME,
						listEndTime);
				previousCal
						.putStringArray(AVAILABILITY_LIST_REPEAT, listRepeat);
				previousCal.putStringArray(AVAILABILITY_LIST_LOCATION,
						listLocation);

				nextDateTime = currentDateTime.plusMonths(1);
				nextCal.putInt(CALENDAR_DATE_MONTH,
						nextDateTime.getMonthOfYear());
				nextCal.putInt(CALENDAR_DATE_YEAR, nextDateTime.getYear());
				nextCal.putString("comingfrom", "CURRENTPAGE");
				nextCal.putBoolean(IS_AVAILABILITY_CALENDAR, isAvailability);
				nextCal.putStringArray(OCCUPIED_DATES, occupiedDates);

				nextCal.putString(PARAM_PART_TIMER, pt_id);
				nextCal.putStringArray(AVAILABILITY_LIST_AVAIL_ID, listAvailID);
				nextCal.putStringArray(AVAILABILITY_LIST_START_TIME,
						listScStartTime);
				nextCal.putStringArray(AVAILABILITY_LIST_END_TIME, listEndTime);
				nextCal.putStringArray(AVAILABILITY_LIST_REPEAT, listRepeat);
				nextCal.putStringArray(AVAILABILITY_LIST_LOCATION, listLocation);

			}
			// Swipe right to see next month
			else if (position > currentPage) {
				currentDateTime = currentDateTime.plusMonths(1);

				nextDateTime = currentDateTime.plusMonths(1);
				nextCal.putInt(CALENDAR_DATE_MONTH,
						nextDateTime.getMonthOfYear());
				nextCal.putInt(CALENDAR_DATE_YEAR, nextDateTime.getYear());
				nextCal.putString("comingfrom", "NEXTPAGE");
				nextCal.putBoolean(IS_AVAILABILITY_CALENDAR, isAvailability);
				nextCal.putStringArray(OCCUPIED_DATES, occupiedDates);

				nextCal.putString(PARAM_PART_TIMER, pt_id);
				nextCal.putStringArray(AVAILABILITY_LIST_AVAIL_ID, listAvailID);
				nextCal.putStringArray(AVAILABILITY_LIST_START_TIME,
						listScStartTime);
				nextCal.putStringArray(AVAILABILITY_LIST_END_TIME, listEndTime);
				nextCal.putStringArray(AVAILABILITY_LIST_REPEAT, listRepeat);
				nextCal.putStringArray(AVAILABILITY_LIST_LOCATION, listLocation);

				Log.d(TAG, String.format("Set next month %d and year %d",
						nextDateTime.getMonthOfYear(), nextDateTime.getYear()));
			} else {
				currentDateTime = currentDateTime.minusMonths(1);
				previousDateTime = currentDateTime.minusMonths(1);
				previousCal.putInt(CALENDAR_DATE_MONTH,
						previousDateTime.getMonthOfYear());
				previousCal.putInt(CALENDAR_DATE_YEAR,
						previousDateTime.getYear());
				previousCal.putString("comingfrom", "PREVIOUS");
				previousCal
						.putBoolean(IS_AVAILABILITY_CALENDAR, isAvailability);
				previousCal.putStringArray(OCCUPIED_DATES, occupiedDates);

				previousCal.putString(PARAM_PART_TIMER, pt_id);
				previousCal.putStringArray(AVAILABILITY_LIST_AVAIL_ID,
						listAvailID);
				previousCal.putStringArray(AVAILABILITY_LIST_START_TIME,
						listScStartTime);
				previousCal.putStringArray(AVAILABILITY_LIST_END_TIME,
						listEndTime);
				previousCal
						.putStringArray(AVAILABILITY_LIST_REPEAT, listRepeat);
				previousCal.putStringArray(AVAILABILITY_LIST_LOCATION,
						listLocation);
			}

			currentPage = position;
		}

		@Override
		public void onPageSelected(int position) {
			refreshCalendarPosition(position);
			calendarViewPager.setCurrentItem(position);
			
			CalendarSingleFragment calendarSingleFragment = fragments.get(getCurrent(position));
			Log.d(TAG, " moth " + calendarSingleFragment.getMonthRow());
		
//			calendarViewPager.setMonthRow(calendarSingleFragment.getMonthRow());
//			monthRow = calendarSingleFragment.getMonthRow();
//			
//			int heightCal = convertDpToPixel(FRAMELAYOUT_HEIGHT_5);
//			if(calendarViewPager.getMonthRow() == 6) {
//				heightCal = convertDpToPixel(FRAMELAYOUT_HEIGHT_6);
//			}
//			
//			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
//					heightCal);
//			frameLayout.setLayoutParams(lp);
			
		}
	}

	private class SwitchCalendar extends AsyncTask<Integer, Void, Integer> {

		@Override
		protected Integer doInBackground(Integer... params) {
			return params[0];
		}

		@Override
		protected void onPostExecute(Integer result) {
			calendarChangeListener.onPageSelected(result);
		}

	}

}
