package com.matchimi.schedule;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caldroid.CaldroidFragment;
import com.caldroid.CaldroidGridAdapter;
import com.matchimi.CommonUtilities;
import com.matchimi.R;

public class CaldroidSampleCustomAdapter extends CaldroidGridAdapter {

	public CaldroidSampleCustomAdapter(Context context, int month, int year,
			HashMap<String, Object> caldroidData,
			HashMap<String, Object> extraData) {
		super(context, month, year, caldroidData, extraData);
	}
	
	@SuppressWarnings("unchecked")
	protected void setCustomResources(View convertView, DateTime dateTime, TextView textView,
			LinearLayout dotLayout) {
		// Set custom background resource
		HashMap<DateTime, Integer> backgroundForDateTimeMap = (HashMap<DateTime, Integer>) caldroidData
				.get(CaldroidFragment._BACKGROUND_FOR_DATETIME_MAP);
		if (backgroundForDateTimeMap != null) {
			// Get background resource for the dateTime
			Integer backgroundResource = backgroundForDateTimeMap.get(dateTime);

			// Set it
			if (backgroundResource != null) {				
				if (dateTime.compareTo(getToday()) < 0) {
					if (sameDay(dateTime, getToday())) {
						if (sameDay(dateTime, getToday())) {
							convertView.setBackgroundResource(R.drawable.red_border);
						}
						textView.setBackgroundResource(R.drawable.circle_past);						
					} else {
						textView.setBackgroundResource(R.drawable.circle_past);						
					}

				} else {
					if (sameDay(dateTime, getToday())) {
						convertView.setBackgroundResource(R.drawable.red_border);
					}
					textView.setBackgroundResource(R.drawable.circle_schedule_availability);
				}
				
			} else if (backgroundResource == null && sameDay(dateTime, getToday())) {
				convertView.setBackgroundResource(R.drawable.red_border);
			} else {
//				textView.setBackgroundResource(R.drawable.cell_bg);
			}
		}

		// Set custom text color
		HashMap<DateTime, Integer> textColorForDateTimeMap = (HashMap<DateTime, Integer>) caldroidData
				.get(CaldroidFragment._TEXT_COLOR_FOR_DATETIME_MAP);
		if (textColorForDateTimeMap != null) {
			// Get textColor for the dateTime
			Integer textColorResource = textColorForDateTimeMap.get(dateTime);

			// Set it
			if (textColorResource != null) {
				textView.setTextColor(resources.getColor(textColorResource.intValue()));
			} else if (sameDay(dateTime, getToday())) {
				textView.setTextColor(Color.GRAY);
			} else {
				textView.setTextColor(Color.GRAY);
			}			
		}
		
		// Set custom text color
		HashMap<DateTime, Integer> pointColorForDateTimeMap = (HashMap<DateTime, Integer>) caldroidData
				.get(CaldroidFragment._POINT_COLOR_FOR_DATETIME_MAP);
		
		if (pointColorForDateTimeMap != null) {
			Iterator iter = pointColorForDateTimeMap.keySet().iterator();
			boolean isMoreThanOne = false;
			
			while(iter.hasNext()) { 
				DateTime key = (DateTime) iter.next();
				
				if(sameDay(key, dateTime)) {
					Integer pointColorResource = (Integer) pointColorForDateTimeMap.get(key);
					
					// Set it
					if (pointColorResource != null) {
						final float scale = convertView.getResources().getDisplayMetrics().density;
						int pixels = (int) (5 * scale + 0.5f);
						
						View labelIndicate = new View(convertView.getContext());
						LayoutParams params = new LayoutParams(pixels, pixels);

						if(isMoreThanOne) {
							params.setMargins((int) (5 * scale + 0.5f), 0, 0, 0);													
						}

						labelIndicate.setBackgroundResource(pointColorResource.intValue());	
						labelIndicate.setLayoutParams(params);
						
						if (dateTime.compareTo(getToday()) < 0) {
							dotLayout.removeAllViews();
						} else {
							dotLayout.addView(labelIndicate);
							isMoreThanOne = true;
						}
					}
				}
			}
		}
	}
	
	private static boolean sameDay(DateTime cal, DateTime selectedDate) {
		return cal.getYear() == selectedDate.getYear()
				&& cal.getDayOfYear() == selectedDate.getDayOfYear();
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// For reuse
		if (convertView == null) {
			convertView = (LinearLayout) inflater.inflate(R.layout.custom_cell, null);
		}

		TextView tv1 = (TextView) convertView.findViewById(R.id.caldroidDateText);
		LinearLayout dotLayout = (LinearLayout) convertView.findViewById(R.id.caldroidDots);
		dotLayout.removeAllViews();

		tv1.setTextColor(Color.GRAY);

		// Get dateTime of this cell
		DateTime dateTime = this.datetimeList.get(position);

		// Set color of the dates in previous / next month
		if (dateTime.getMonthOfYear() != month) {
			tv1.setTextColor(resources
					.getColor(R.color.caldroid_darker_gray));
		}

		boolean shouldResetDiabledView = false;
		boolean shouldResetSelectedView = false;

		// Customize for disabled dates and date outside min/max dates
		if ((minDateTime != null && dateTime.isBefore(minDateTime))
				|| (maxDateTime != null && dateTime.isAfter(maxDateTime))
				|| (disableDates != null && disableDatesMap
						.containsKey(dateTime))) {

			tv1.setTextColor(CaldroidFragment.disabledTextColor);
			if (CaldroidFragment.disabledBackgroundDrawable == -1) {
				tv1.setBackgroundResource(R.drawable.disable_cell);
			} else {
				tv1.setBackgroundResource(CaldroidFragment.disabledBackgroundDrawable);
			}

			if (sameDay(dateTime, getToday())) {
//				tv1.setBackgroundResource(R.drawable.circle_today);
				tv1.setTextColor(Color.GRAY);
			}
		} else {
			shouldResetDiabledView = true;
		}

		// Customize for selected dates
		if (selectedDates != null && selectedDatesMap.containsKey(dateTime)) {
			if (CaldroidFragment.selectedBackgroundDrawable != -1) {
				tv1.setBackgroundResource(CaldroidFragment.selectedBackgroundDrawable);
			} else {
				tv1.setBackgroundColor(resources
						.getColor(R.color.caldroid_sky_blue));
			}

			tv1.setTextColor(CaldroidFragment.selectedTextColor);
			
		} else {
			shouldResetSelectedView = true;
		}

		if (shouldResetDiabledView && shouldResetSelectedView) {
			// Customize for today
			if (sameDay(dateTime, getToday())) {
//				tv1.setBackgroundResource(R.drawable.circle_today);
				tv1.setTextColor(Color.GRAY);
			} else {
				tv1.setTextColor(Color.WHITE);
//				tv1.setBackgroundResource(R.drawable.cell_bg);
			}

		}

		tv1.setText("" + dateTime.getDayOfMonth());

		// Set custom color if required
		setCustomResources(convertView, dateTime, tv1, dotLayout);

		return convertView;
	}


}

