package com.matchimi.swipecalendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.matchimi.CommonUtilities.*;
import com.matchimi.R;
import com.matchimi.calendar.CalendarPickerView.SelectionMode;
import static com.matchimi.swipecalendar.CalendarSwipeView.SelectionMode.MULTIPLE;
import static com.matchimi.swipecalendar.CalendarSwipeView.SelectionMode.SINGLE;

public class CalendarSingleFragment extends Fragment {
	private CalendarSwipeView calendarSwipeView;
	private Bundle bundle;
	private int year;
	private int month;
	private Calendar currentCal;
	private boolean isAvailabilityCalendar;
	private int monthRowNext = 0;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.calendar_area, container, false);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		bundle = getArguments();

		month = bundle.getInt(CALENDAR_DATE_MONTH);
		year = bundle.getInt(CALENDAR_DATE_YEAR);
		isAvailabilityCalendar = bundle.getBoolean(IS_AVAILABILITY_CALENDAR);
		
		calendarSwipeView = (CalendarSwipeView) getView().findViewById(
				R.id.calendar_swipeview);
		currentCal = Calendar.getInstance();
		currentCal.set(year, month, 1);
		
		Date today = new Date();
		
		String pt_id = bundle.getString(PARAM_PART_TIMER);		
		String[] occupiedDates = bundle.getStringArray(OCCUPIED_DATES);		
		
		if(isAvailabilityCalendar == true) {
			String[] listAvailID = bundle.getStringArray(AVAILABILITY_LIST_AVAIL_ID);
			String[] listStartTime = bundle.getStringArray(AVAILABILITY_LIST_START_TIME);
			String[] listEndTime = bundle.getStringArray(AVAILABILITY_LIST_END_TIME);
			String[] listLocation = bundle.getStringArray(AVAILABILITY_LIST_LOCATION);
			String[] listRepeat = bundle.getStringArray(AVAILABILITY_LIST_REPEAT);

			if (listStartTime != null) {
				Log.e("GGGG", ">>> " + listStartTime.length);
			} else {
				Log.e("GGGG", ">>> NULL");
			}
			
			if(occupiedDates != null) {
				ArrayList<Date> occupiedList = new ArrayList<Date>();			
				for(String dates : occupiedDates) {
					Date occupiedDate;
					
					try {
						occupiedDate = new SimpleDateFormat(getResources().getString(R.string.datetime_simple_format)).parse(dates);
						occupiedList.add(occupiedDate);					
					} catch (NotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}	
				
				calendarSwipeView.init(today, currentCal.getTime(), isAvailabilityCalendar
						).inMode(MULTIPLE).withSelectedDates(occupiedList).withListAvailability(pt_id, listAvailID, listStartTime, listEndTime, listLocation, listRepeat);				
			} else {
				calendarSwipeView.init(today, currentCal.getTime(), isAvailabilityCalendar
						).inMode(MULTIPLE);				
			}
			
		} else {
			
			String[] listAvailID = bundle.getStringArray(SCHEDULE_LIST_AVAIL_ID);
			String[] listAddress = bundle.getStringArray(SCHEDULE_ADDRESS);			
			String[] listCompany = bundle.getStringArray(SCHEDULE_COMPANY);
			String[] listLocation = bundle.getStringArray(SCHEDULE_DATA_LOCATION);
			String[] listEndTime = bundle.getStringArray(SCHEDULE_END_TIME);
			String[] listStartTime = bundle.getStringArray(SCHEDULE_START_TIME);
			String[] listPrice = bundle.getStringArray(SCHEDULE_PRICE);
			String[] listJobID = bundle.getStringArray(SCHEDULE_LIST_JOB_ID);
			
			if (listStartTime != null) {
				Log.e("HHHHH", ">>> " + listStartTime.length);
			} else {
				Log.e("HHHHH", ">>> NULL");
			}
			
			if(occupiedDates != null) {
				ArrayList<Date> occupiedList = new ArrayList<Date>();			
				for(String dates : occupiedDates) {
					Date occupiedDate;
					
					try {
						occupiedDate = new SimpleDateFormat(getResources().getString(R.string.datetime_simple_format)).parse(dates);
						occupiedList.add(occupiedDate);					
					} catch (NotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}	
				
				calendarSwipeView.init(today, currentCal.getTime(), isAvailabilityCalendar
						).inMode(MULTIPLE).withSelectedDates(occupiedList).withListSchedule(pt_id,
								listAvailID, listStartTime, listEndTime, listLocation, listAddress, listCompany,
								listPrice, listJobID);				

			} else {
				calendarSwipeView.init(today, currentCal.getTime(), isAvailabilityCalendar
						).inMode(MULTIPLE);		
			}
			
		}		
	}

	public int getMonthRow() {
		return calendarSwipeView.weekRow;
	}
}
