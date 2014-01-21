package com.matchimi.options;

import static com.matchimi.CommonUtilities.*;

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

import javax.xml.datatype.Duration;

import net.simonvt.numberpicker.NumberPicker;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.availability.HomeAvailabilityActivity;
import com.matchimi.availability.LocationPreferenceActivity;
import com.matchimi.availability.LocationPreferenceRegionAdapter;
import com.matchimi.availability.RepeatAvailabilityActivity;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.MapUtils;
import com.matchimi.utils.NetworkUtils;
import com.matchimi.utils.ProcessDataUtils;

public class CreateAvailability extends SherlockFragmentActivity {

	private Context context;
	private Date selectedDate;
	
	private ProgressDialog progress;
	private GoogleMap map;

	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private String pt_id = null;
	private String location = null;

	private String locationAPIData;
	private String repeatedAPIData;
	
	private Map<Integer, String> mapLocations = new HashMap<Integer, String>();
	private Map<Integer, String> mapDays = new HashMap<Integer, String>();
	private String price = null;
	private String avail_id = null;
	private String repeat_days;
	private String repeat_days_integer;
	private String repeat_start_date;
	private String repeat_end_date;
	private String repeat = "";
	private String repeat_text = "";
	private String ravail_id = null;

	private boolean update = false;
	private boolean locationLoaded = false;
	
	private List<Integer> selectedRepeat = null;

	public static final int RC_MAPS_ACTIVITY = 50;

	private TextView dateStart;
	private TextView dateEnd;
	private TextView timeStart;
	private TextView timeEnd;

	private TextView labelLocation;
	private TextView labelRepeat;
	
	private Date availabilityDateStart;
	private Date availabilityDateEnd;
	
	private DateTime availabilityTimeStart;
	private DateTime availabilityTimeEnd;
	private Date todayDate = new Date();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		if (settings.getInt(SETTING_THEME, THEME_LIGHT) == THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}

		pt_id = settings.getString(USER_PTID, null);
		setContentView(R.layout.edit_availability);

		context = this;

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		
		Bundle b = getIntent().getExtras();
		update = b.getBoolean("update");

		Intent intent = getIntent();
		if(intent.hasExtra(AVAILABILITY_SELECTED)) {
			try {
				selectedDate = CommonUtilities.AVAILABILTY_DATETIME.parse(intent.getStringExtra(AVAILABILITY_SELECTED));
				availabilityDateStart = selectedDate;
				availabilityDateEnd = selectedDate;
				Log.d(TAG, "Selected date " + selectedDate);
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (update) {
			ab.setTitle("Edit Availability");
			
			String startAvailability = b.getString("start");
			String endAvailability = b.getString("end");
			
			try {
				availabilityTimeStart = new DateTime(CommonUtilities.AVAILABILTY_DATETIME.parse(startAvailability));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				availabilityTimeEnd = new DateTime(CommonUtilities.AVAILABILTY_DATETIME.parse(endAvailability));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			availabilityDateStart = selectedDate;
			availabilityDateEnd = selectedDate;
			
			price = b.getString("price");
			avail_id = b.getString("avail_id");
			ravail_id = b.getString("ravail_id");
			
			if(ravail_id != null && ravail_id.equals("null")) {
				ravail_id = null;
			}
			
			repeat = b.getString("repeat");
			repeat_days = b.getString("repeat_text");
			repeat_days_integer = repeat;
			location = b.getString("location");
			
//			Log.d(CommonUtilities.TAG, "Repeat " + repeat + ", " + repeat_days + ", " + repeat_days_integer + ", " + ravail_id);
			
		} else {
			ab.setTitle(getString(R.string.create_availability));
		}
			
		labelRepeat = (TextView) findViewById(R.id.labelRepeat);
		labelRepeat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(context, RepeatAvailabilityActivity.class);
				i.putExtra("repeat_days", repeat_days);
				startActivityForResult(i, CommonUtilities.CREATEAVAILABILITY_RC_REPEAT);
			}
		});
		
		// Loading location default values from cache
		locationAPIData = settings.getString(CommonUtilities.API_CACHE_LOCATIONS, null);
		repeatedAPIData = settings.getString(CommonUtilities.API_CACHE_DAYS, null);
		
		generateLocationData();
		generateRepeatData();

		dateStart = (TextView) findViewById(R.id.labelDateStart);
		if(!update) {
			dateStart.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// If user not select particular date on calendar, give them 
					showDateDialog(true);					
				}
			});			
		} else {
			dateStart.setTextColor(getResources().getColor(R.color.gray));
		}

		dateEnd = (TextView) findViewById(R.id.labelDateEnd);
		if(!update) {
			dateEnd.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					
						// If user already select start date, allow to edit end date
						if (availabilityDateStart != null) {
							showDateDialog(false);
						} else {
							Toast.makeText(
									context,
									getString(R.string.availability_select_date_validation),
									Toast.LENGTH_SHORT).show();
						}					
				}
			});			
		} else {
			dateEnd.setTextColor(getResources().getColor(R.color.gray));
		}
		
		timeStart = (TextView) findViewById(R.id.labelTimeStart);
		timeStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(availabilityDateEnd == null) {
					Toast.makeText(
							context,
							getString(R.string.availability_select_time_start_validation),
							Toast.LENGTH_SHORT).show();
				} else {
					showTimeDialog(true);					
				}

			}
		});

		timeEnd = (TextView) findViewById(R.id.labelTimeEnd);
		timeEnd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(availabilityTimeStart == null) {
					Toast.makeText(
							context,
							getString(R.string.availability_select_time_end_validation),
							Toast.LENGTH_SHORT).show();
				} else {
					showTimeDialog(false);					
				}
			}
		});

		
		labelLocation = (TextView) findViewById(R.id.labelLocation);
		labelLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(context, LocationPreferenceActivity.class);
				i.putExtra("location", location);
				startActivityForResult(i, CREATEAVAILABILITY_RC_MAPS_ACTIVITY);
			}
		});

		if(intent.hasExtra(AVAILABILITY_DATA_LOCATION)) {
			labelLocation.setText(getResources().getString(R.string.location_preference) 
					+ ": " + intent.getStringExtra(AVAILABILITY_DATA_LOCATION));
		}
		
		reloadView();
		
//		map = ((SupportMapFragment) getSupportFragmentManager()
//				.findFragmentById(R.id.map)).getMap();

//		if (location != null && !location.equalsIgnoreCase("null")
//				&& location.length() > 1) {
////			loadMap(location);
//		} else {
////			loadLocation();
//		}
	}
	
	/**
	 * Generate locations data
	 */
	private void generateLocationData() {
		jsonParser = new JSONParser();
		
		try {
			JSONArray items = new JSONArray(locationAPIData);

			if (items != null && items.length() > 0) {							
				for (int i = 0; i < items.length(); i++) {
					/* get all json items, and put it on list */
					try {
						JSONObject objs = items.getJSONObject(i);
//									objs = objs.getJSONObject(CommonUtilities.JSON_KEY_LOCATIONS);
						
						if (objs != null) {
							Log.d(CommonUtilities.TAG, "Location data " + objs.toString());
							
							String locationName = jsonParser.getString(objs,
									CommonUtilities.JSON_KEY_LOCATION_NAME);
							
							int locationID = Integer.parseInt(jsonParser.getString(objs,
									CommonUtilities.JSON_KEY_LOCATION_ID));
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

			Log.e(CommonUtilities.TAG, "Load location " + jsonStr
					+ " >> " + e1.getMessage());
		}
	}
	
	/**
	 * Generate locations data
	 */
	private void generateRepeatData() {
		jsonParser = new JSONParser();
		
		try {
			JSONArray items = new JSONArray(repeatedAPIData);
			Log.d(TAG, "Item length is " + items.length());

			if (items != null && items.length() > 0) {							
				for (int i = 0; i < items.length(); i++) {
					/* get all json items, and put it on list */
					try {
						JSONObject objs = items.getJSONObject(i);
						
						if (objs != null) {
							String dayName = jsonParser.getString(objs,
									CommonUtilities.JSON_KEY_REPEAT_DAY);
							int dayID = Integer.parseInt(jsonParser.getString(objs,
									CommonUtilities.JSON_KEY_REPEAT_DAY_ID));
							Log.d(TAG, "Days is " + dayName);
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

			Log.e(CommonUtilities.TAG, "Load schedule " + jsonStr
					+ " >> " + e1.getMessage());
		}
	}

	@SuppressLint("ValidFragment")
	public class EditNameDialog extends DialogFragment {

		public EditNameDialog() {

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.maps_layout, container);
			getDialog().setTitle("Location");

			return view;
		}
	}

	/**
	 * Reloading all fields with selected data
	 */
	private void reloadView() {
		// Load Initial value
		dateStart.setText("Start Date :");
		dateEnd.setText("End Date :");
		timeStart.setText("Start Time :");
		timeEnd.setText("End Time :");

		if (availabilityDateStart != null) {
			dateStart.setText("Start Date : "
					+ CommonUtilities.AVAILABILITY_DATE_TEXT.format(
							availabilityDateStart));
		}

		if (availabilityDateEnd != null) {
			dateEnd.setText("End Date : "
					+ CommonUtilities.AVAILABILITY_DATE_TEXT
							.format(availabilityDateEnd));
		}

		if(availabilityTimeStart != null) {
			timeStart.setText("Start Time : "
					+ CommonUtilities.AVAILABILITY_TIME.format(
							availabilityTimeStart.toDate()));			
		}
		
		if(availabilityTimeEnd != null) {
			timeEnd.setText("End Time : "
					+ CommonUtilities.AVAILABILITY_TIME
							.format(availabilityTimeEnd.toDate()));
		}
		
		// Check if user change start date without update time, auto update datetime
		if(availabilityTimeStart != null && availabilityDateStart != null) {
			Log.d(CommonUtilities.TAG, "Comparing " + availabilityTimeStart.toDate() + " " + availabilityDateStart);
			
			if(!ProcessDataUtils.compareSameDayDate(availabilityTimeStart.toDate(), availabilityDateStart)) {
				availabilityTimeStart = updateAvailabilityValue(availabilityDateStart, availabilityTimeStart);
			}
		}
		
		// Check if user change end date without update end time, auto update datetime
		if(availabilityTimeEnd != null && availabilityDateEnd != null) {
			Log.d(CommonUtilities.TAG, "Comparing " + availabilityTimeEnd.toDate() + " " + availabilityDateEnd);
			
			if(!ProcessDataUtils.compareSameDayDate(availabilityTimeEnd.toDate(), availabilityDateEnd)) {
				availabilityTimeEnd = updateAvailabilityValue(availabilityDateEnd, availabilityTimeEnd);
			}
		}

		if(repeat_days != null) {
			// Remove last comma if any
			if(repeat_days.length() > 0) {
				if(repeat_days.substring(repeat_days.length() - 1).equals(",")) {
					repeat_days = repeat_days.substring(0, repeat_days.length()-1);
				}
			}
			
			// Remove last comma if any
			if(repeat_days_integer != null && repeat_days_integer.length() > 0) {
				if(repeat_days_integer.substring(repeat_days_integer.length() - 1).equals(",")) {
					repeat_days_integer = repeat_days_integer.substring(0, 
							repeat_days_integer.length()-1);
				}
			}

			labelRepeat.setText("Repeat : " + repeat_days);			
		}
		
		if(availabilityDateStart != null && availabilityDateEnd != null) {
			// Don't show repeat for edit and view availability
			labelRepeat.setVisibility(View.GONE);
			
			if(!update && !checkAvailabilitySameDay()) {
				labelRepeat.setVisibility(View.VISIBLE);
			}
			
//			if(!checkAvailabilitySameDay()) {
//				Log.d(CommonUtilities.TAG, "Compare two date : " + availabilityDateStart.toString() + " " + availabilityDateEnd.toString());
//				labelRepeat.setVisibility(View.VISIBLE);
//			} else if(checkAvailabilitySameDay() && ravail_id == null) {
//				labelRepeat.setVisibility(View.GONE);
//				repeat_days_integer = null;
//			} else if(checkAvailabilitySameDay() && ravail_id != null) {
//				Log.d(CommonUtilities.TAG, "KOKO " + availabilityDateStart.toString() + " " + availabilityDateEnd.toString());
//				
//				labelRepeat.setVisibility(View.VISIBLE);
//			}
		}
		
		if(location != null && location.length() > 0) {
			Log.d(TAG, "Location values are " + location);
			
			String locationRegion = "";
			List<String> items = Arrays.asList(location.split("\\s*,\\s*"));

			for (String item : items) {
				for (Map.Entry<Integer, String> entry : mapLocations.entrySet()) {
					if (item.length() > 0) {
						Integer itemNumber = Integer.parseInt(item);
						if(itemNumber == entry.getKey()) {
							locationRegion += entry.getValue() + ", ";
						}
					}
				}
			}
			
			// Remove last comma if any
			if(locationRegion.length() > 0) {
				locationRegion = locationRegion.substring(0, locationRegion.length()-2);
			}
			
			labelLocation.setText(getResources().getString(R.string.location_preference) + ": " + locationRegion);			
		}
		
//		if (price != null) {
//			labelSalary.setText("Ask Salary : $" + price);
//		}
	}
	
	/**
	 * Updating availability datetime
	 * @param availDate
	 * @param availTime
	 * @return
	 */
	private DateTime updateAvailabilityValue(Date availDate, DateTime availTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(availDate);
		cal.set(Calendar.HOUR_OF_DAY, availTime.getHourOfDay());
		cal.set(Calendar.MINUTE, availTime.getMinuteOfHour());
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);

		return new DateTime(cal.getTime());
	}
	
	private boolean checkAvailabilitySameDay() {
		// Check if selected start - end date is a range
		Calendar availabilityStartCal = Calendar.getInstance();
		availabilityStartCal.setTime(availabilityDateStart);
		
		Calendar availabilityEndCal = Calendar.getInstance();
		availabilityEndCal.setTime(availabilityDateEnd);

		if(ProcessDataUtils.sameDay(availabilityStartCal, availabilityEndCal)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Showing availability date selection dialog
	 * @param withDate
	 */
	protected void showDateDialog(final boolean availabilityMode) {
		final Calendar cal;

		// Start mode true meaning start date availability
		if (availabilityMode) {
			if (availabilityDateStart != null) {
				cal = Calendar.getInstance();
				cal.setTime(availabilityDateStart);
			} else {
				cal = new GregorianCalendar(Locale.getDefault());
			}
			
		} else {
			cal = Calendar.getInstance();
			cal.setTime(availabilityDateStart);
		}
		
		// Dialog showing start and end selection
		DatePickerDialog dialogDate = new DatePickerDialog(context,
				new OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker arg0, int arg1,
							int arg2, int arg3) {
						cal.set(Calendar.YEAR, arg1);
						cal.set(Calendar.MONTH, arg2);
						cal.set(Calendar.DAY_OF_MONTH, arg3);

						if(availabilityMode) {
							if(cal.getTime().before(todayDate)) {
								alertDateTimeOverlap(true, true);
								
							} else {
								
								// Set availability start date
								availabilityDateStart = cal.getTime();
								Log.d(CommonUtilities.TAG, "Update date start " + availabilityDateStart.toString());
								
								if(availabilityDateEnd != null && availabilityDateEnd.before(availabilityDateStart)) {
									availabilityDateEnd = null;
								}
							}
							
						} else {
							if(availabilityDateStart.after(cal.getTime())) {
								alertDateTimeOverlap(true, false);
							} else {
								availabilityDateEnd = cal.getTime();								
								Log.d(CommonUtilities.TAG, "Update date start " + availabilityDateStart.toString());
								
							}
						}
						
						reloadView();

					}
				}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH));
		dialogDate.show();
	}
	
	/**
	 * Alert box if end availability is overlap
	 */
	private void alertDateTimeOverlap(Boolean isDate, Boolean isPassed) {
		String errorMessage = "";
		if(isPassed) {
			errorMessage = getString(R.string.availability_error_passed);
		} else if(isDate && !isPassed) {
			errorMessage = getString(R.string.availability_end_date_error);
		} else {
			errorMessage = getString(R.string.availability_end_time_error);
		}
		
		// Use the Builder class for
		// convenient dialog
		// construction
		AlertDialog.Builder builder = new AlertDialog.Builder(
				CreateAvailability.this);
		builder.setMessage(errorMessage)
				.setPositiveButton(
						R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int id) {
								reloadView();
							}
						});
		// Create the AlertDialog object
		// and return it
		Dialog dialog = builder
				.create();
		dialog.show();
	}
	
	/**
	 * Showing availability time selection dialog
	 * @param withDate
	 */
	private void showTimeDialog(final Boolean availabilityMode) {		
		AlertDialog.Builder builder = new AlertDialog.Builder(
				context);
		final View view = LayoutInflater.from(context)
				.inflate(R.layout.custome_time, null);

		builder.setView(view);
		builder.setTitle(getString(R.string.availability_set_time));

		final NumberPicker np1 = (NumberPicker) view
				.findViewById(R.id.numberPicker1);
		final String[] nums1 = generateHour(0);
		np1.setMaxValue(23);
		np1.setMinValue(0);
		np1.setWrapSelectorWheel(true);
		np1.setDisplayedValues(nums1);
		
		final NumberPicker np2 = (NumberPicker) view
				.findViewById(R.id.numberPicker2);
		final String[] nums2 = { "00", "15", "30", "45" };
		np2.setMaxValue(3);
		np2.setMinValue(0);
		np2.setWrapSelectorWheel(true);
		np2.setDisplayedValues(nums2);
		
		if(availabilityMode) {
			if(availabilityTimeStart != null) {
				String getMinutes = String.valueOf(availabilityTimeStart.getMinuteOfHour());
				if(getMinutes.equals("0")) {
					getMinutes = "00";
				}
				
				np1.setValue(availabilityTimeStart.getHourOfDay());
				np2.setValue(Arrays.asList(nums2).indexOf(getMinutes));
			}
		} else {
			
			if(availabilityTimeEnd != null) {
				np1.setValue(availabilityTimeEnd.getHourOfDay());
				
				String getMinutes = String.valueOf(availabilityTimeEnd.getMinuteOfHour());
				if(getMinutes.equals("0")) {
					getMinutes = "00";
				}
				
				np2.setValue(Arrays.asList(nums2).indexOf(getMinutes));
				
				
			} else {
				
				int currentHour = availabilityTimeStart.getHourOfDay();
				int currentMinute = availabilityTimeStart.getMinuteOfHour();
				
				if (currentMinute > 30) {
					np1.setValue(currentHour + 1);
				} else {
					np1.setValue(currentHour);
					int increment = currentMinute / 15;
					np2.setValue(increment + 1);
				}
			}
		}
		
		builder.setPositiveButton(R.string.set,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(
							DialogInterface arg0, int arg1) {
						int selectHour = Integer.parseInt(nums1[np1.getValue()]);
						int selectMinute = Integer.parseInt(nums2[np2.getValue()]);
						
						// If user select start time availability
						if(availabilityMode) {		
							Calendar dateCal = Calendar.getInstance();
							dateCal.setTime(availabilityDateStart);
							
							dateCal.set(Calendar.HOUR_OF_DAY, selectHour);
							dateCal.set(Calendar.MINUTE, selectMinute);
							dateCal.set(Calendar.SECOND, 0);
							
							availabilityTimeStart = new DateTime(dateCal.getTime());
							
							// Reset end time if start time in advance
							if(availabilityTimeEnd != null && availabilityTimeEnd.isBefore(availabilityTimeStart)) {
								availabilityTimeEnd = null;
							} else if(availabilityTimeEnd != null) {
								Integer startTimeTemp = availabilityTimeStart.getHourOfDay() * 60 + availabilityTimeStart.getMinuteOfHour();
								Integer endTimeTemp = availabilityTimeEnd.getHourOfDay() * 60 + availabilityTimeEnd.getMinuteOfHour();
								
								if(startTimeTemp > endTimeTemp) {
									availabilityTimeEnd = null;
								}
							}
							
						} else {
							
							Calendar dateCal = Calendar.getInstance();
							dateCal.setTime(availabilityDateEnd);
							
							dateCal.set(Calendar.HOUR_OF_DAY, selectHour);
							dateCal.set(Calendar.MINUTE, selectMinute);
							dateCal.set(Calendar.SECOND, 0);

							DateTime temporaryEndTime = new DateTime(dateCal.getTime());
							if((temporaryEndTime.getHourOfDay() == availabilityTimeStart.getHourOfDay()) &&
								(temporaryEndTime.getMinuteOfHour() == availabilityTimeStart.getMinuteOfHour())) {
								alertDateTimeOverlap(false, false);	
							} else if(availabilityTimeStart.isAfter(temporaryEndTime)) {
								alertDateTimeOverlap(false, false);
							} else {
								Integer startTimeTemp = availabilityTimeStart.getHourOfDay() * 60 + availabilityTimeStart.getMinuteOfHour();
								Integer endTimeTemp = temporaryEndTime.getHourOfDay() * 60 + temporaryEndTime.getMinuteOfHour();
								
								if(startTimeTemp > endTimeTemp) {
									alertDateTimeOverlap(false, false);	
								} else {
									availabilityTimeEnd = temporaryEndTime;
								}
							}
								
						}
						
						reloadView();
					}
				});

		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(
							DialogInterface arg0, int arg1) {
					}
				});

		AlertDialog dialogTime = builder.create();
		dialogTime.show();
	}
	
	NumberPicker.OnValueChangeListener onValueChanged = new NumberPicker.OnValueChangeListener() {
		
		@Override
		public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
			// TODO Auto-generated method stub
			
		}
	};

	
	/**
	 * Generate hour
	 * 
	 * @param startHour
	 * @return
	 */
	private String[] generateHour(int startHour) {
		List<String> hourResult = new ArrayList<String>();

		int endHour = 24;
		for (int i = startHour; i < endHour; i++) {
			if (i < 10) {
				hourResult.add("0" + String.valueOf(i));
			} else {
				hourResult.add(String.valueOf(i));
			}
		}

		return hourResult.toArray(new String[hourResult.size()]);
	}

	private Calendar generateCalendar(String str) {
		Calendar calRes = new GregorianCalendar(Integer.parseInt(str.substring(
				0, 4)), Integer.parseInt(str.substring(5, 7)) - 1,
				Integer.parseInt(str.substring(8, 10)), Integer.parseInt(str
						.substring(11, 13)), Integer.parseInt(str.substring(14,
						16)), Integer.parseInt(str.substring(17, 19)));

		return calRes;
	}

	/**
	 * Submit data into create availability API
	 */
	protected void doAddAvailability() {
		final String url;					
		
		if(repeat_days_integer != null) {
			url = SERVERURL + API_CREATE_REPEATED_AVAILABILITY;			
		} else {
			url = SERVERURL + API_CREATE_AND_AVAILABILITY;
		}
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(
								context,
								getString(R.string.availability_adding_succesfully),
								Toast.LENGTH_SHORT).show();
						
//						Intent intent = new Intent(
//								CommonUtilities.CREATE_AVAILABILITY_BROADCAST);
//						// You can also include some extra data.
//						intent.putExtra(CommonUtilities.CREATE_AVAILABILITY_BROADCAST, "true");
//						LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
						
						Intent i = new Intent(CommonUtilities.BROADCAST_SCHEDULE_RECEIVER);
						SimpleDateFormat availabilityDateTime = CommonUtilities.AVAILABILTY_DATETIME;
						String startDateTimeVal = availabilityDateTime.format(availabilityTimeStart.toDate());
						i.putExtra("month", startDateTimeVal);						
						sendBroadcast(i);
						
						Intent result = new Intent();
					    setResult(RESULT_OK, result);
						finish();
						
					} else if (jsonStr.trim().equalsIgnoreCase("1")) {
						Toast.makeText(context,
								getString(R.string.availability_adding_failed),
								Toast.LENGTH_SHORT).show();
					} else if (jsonStr.trim().equalsIgnoreCase("2")) {
						Toast.makeText(
								context,
								getString(R.string.availability_adding_overlap),
								Toast.LENGTH_SHORT).show();
					} else {
						NetworkUtils.connectionHandler(context, jsonStr, "");
					}
				} else {
					Toast.makeText(context, getString(R.string.server_error),
							Toast.LENGTH_LONG).show();
				}
			}
		};

		progress = ProgressDialog.show(context,
				getString(R.string.menu_availability),
				getString(R.string.add_availability_progress), true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();

				try {
					JSONObject parentData = new JSONObject();
					JSONObject childData = new JSONObject();
					
					childData.put("pt_id", pt_id);
					// childData.put("asked_salary",
					// price.substring(0, price.indexOf(".")));
					childData.put("asked_salary", 1);
					
					if (location == null || location.equalsIgnoreCase("null")
							|| location.length() == 0) {
						location = "";
					}
					
					if(repeat_days_integer == null) {
						SimpleDateFormat availabilityDateTime = CommonUtilities.AVAILABILTY_DATETIME;
						String startDateTimeVal = availabilityDateTime.format(availabilityTimeStart.toDate());
						String endDateTimeVal = availabilityDateTime.format(availabilityTimeEnd.toDate());
						
						childData.put("locations", "[" + location + "]");
						childData.put("start_date_time", startDateTimeVal);
						childData.put("end_date_time", endDateTimeVal);
						childData.put("asked_salary", "1");
						
						parentData.put("availability", childData);
						
					} else {
						
						SimpleDateFormat simpleDate= CommonUtilities.AVAILABILITY_DATE;
						String startDateVal = simpleDate.format(availabilityTimeStart.toDate());
						String endDateVal = simpleDate.format(availabilityTimeEnd.toDate());
						
						SimpleDateFormat simpleTime = CommonUtilities.AVAILABILITY_TIME_FULL;
						String startTimeVal = simpleTime.format(availabilityTimeStart.toDate());
						String endTimeVal = simpleTime.format(availabilityTimeEnd.toDate());
						
						childData.put("start_time", startTimeVal + ":00");
						childData.put("end_time", endTimeVal + ":00");						
						childData.put("locations", "[" + location + "]");
						childData.put("days", "[" + repeat_days_integer + "]");
						
						childData.put("from_date", startDateVal);
						childData.put("to_date", endDateVal);
						parentData.put("repeated_availability", childData);						
					}
					
					String[] params = { "data" };
					String[] values = { parentData.toString() };
					
					Log.e(TAG, "Post data to " + url + " with data >>>\n"
							+ childData.toString());
					
					jsonStr = jsonParser.getHttpResultUrlPost(url, params,
							values);
					
					Log.e(TAG, "Create Availability result >>> " + jsonStr);

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
	

	/**
	 * Submit data into API Edit Availability
	 */
	protected void doEditAvailability() {
		final String url;
		
		// We only use single availability API
		repeat_days_integer = null;
		ravail_id = null;
		
		if(repeat_days_integer != null || ravail_id != null) {
			url = SERVERURL + CommonUtilities.API_EDIT_REPEATED_AVAILABILITY;
		} else {
			url = SERVERURL + API_EDIT_AND_AVAILABILITY;
		}

		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						// Reload calendar
						Intent i = new Intent(CommonUtilities.BROADCAST_SCHEDULE_RECEIVER);
						SimpleDateFormat availabilityDateTime = CommonUtilities.AVAILABILTY_DATETIME;
						String startDateTimeVal = availabilityDateTime.format(availabilityTimeStart.toDate());
						i.putExtra("month", startDateTimeVal);				
						sendBroadcast(i);
					    
						Toast.makeText(context,
								getString(R.string.edit_availability_success),
								Toast.LENGTH_SHORT).show();
						Intent result = new Intent();
						setResult(RESULT_OK, result);
						finish();
					} else if (jsonStr.trim().equalsIgnoreCase("1")) {
						Toast.makeText(context,
								getString(R.string.edit_availability_failed),
								Toast.LENGTH_LONG).show();
					} else if (jsonStr.trim().equalsIgnoreCase("2")) {
						Toast.makeText(
								context,
								getString(R.string.availability_adding_overlap),
								Toast.LENGTH_LONG).show();
					} else {
						NetworkUtils.connectionHandler(context, jsonStr, "");
					}
				} else {
					Toast.makeText(context, getString(R.string.server_error),
							Toast.LENGTH_LONG).show();
				}
			}
		};

		progress = ProgressDialog.show(context,
				getString(R.string.menu_availability),
				getString(R.string.edit_availability_progress), true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				try {
					JSONObject parentData = new JSONObject();
					JSONObject childData = new JSONObject();
										
					childData.put("pt_id", pt_id);
					// childData.put("asked_salary",
					// price.substring(0, price.indexOf(".")));
					childData.put("asked_salary", 1);
					
					childData.put("pt_id", pt_id);
					// childData.put("asked_salary",
					// price.substring(0, price.indexOf(".")));
					childData.put("asked_salary", 1);
					
					if (location == null || location.equalsIgnoreCase("null")
							|| location.length() == 0) {
						location = "";
					}
					
					if(repeat_days_integer == null) {
						SimpleDateFormat availabilityDateTime = CommonUtilities.AVAILABILTY_DATETIME;
						String startDateTimeVal = availabilityDateTime.format(availabilityTimeStart.toDate());
						String endDateTimeVal = availabilityDateTime.format(availabilityTimeEnd.toDate());

						childData.put("start_date_time", startDateTimeVal);
						childData.put("end_date_time", endDateTimeVal);
						childData.put("asked_salary", "1");
						childData.put("avail_id", avail_id);
						childData.put("locations", "[" + location + "]");
						
						parentData.put("availability", childData);
						
					} else {
						
						SimpleDateFormat simpleDate= CommonUtilities.AVAILABILITY_DATE;
						String startDateVal = simpleDate.format(availabilityTimeStart.toDate());
						String endDateVal = simpleDate.format(availabilityTimeEnd.toDate());
						
						SimpleDateFormat simpleTime = CommonUtilities.AVAILABILITY_TIME_FULL;
						String startTimeVal = simpleTime.format(availabilityTimeStart.toDate());
						String endTimeVal = simpleTime.format(availabilityTimeEnd.toDate());
						
						if(ravail_id == null) {
							ravail_id = "";
						}
						
						childData.put("ravail_id", ravail_id);
						childData.put("start_time", startTimeVal);
						childData.put("end_time", endTimeVal);						
						childData.put("locations", "[" + location + "]");
						childData.put("days", "[" + repeat_days_integer + "]");
						childData.put("from_date", startDateVal);
						childData.put("to_date", endDateVal);
						parentData.put("repeated_availability", childData);						
					}

					String[] params = { "data" };
					String[] values = { parentData.toString() };

					Log.e(TAG, "EDIT Post to " + url + " >>>\n" + childData.toString());
					
					if(url.equals(SERVERURL + CommonUtilities.API_EDIT_REPEATED_AVAILABILITY)) {
						jsonStr = jsonParser.getHttpResultUrlPost(url, params,
								values);
					} else {
						jsonStr = jsonParser.getHttpResultUrlPut(url, params,
								values);
					}

					Log.e(TAG, "HttpPut to " + url + "Result >>> " + jsonStr);
					
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

	/**
	 * Checking and validate inputs
	 * @return
	 */
	private String checkInput() {
		String errors = "";
		if (availabilityTimeEnd != null && availabilityTimeStart != null &&
				location != null) {
			return "";
		} else {
			if(availabilityTimeEnd == null && availabilityTimeStart == null) {
				errors = getString(R.string.availability_datetime_validation) + "\n";
			}
			
			if(location == null) {
				errors = getString(R.string.location_preference);				
			}
			
			return errors;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.ab_edit_availability, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent result = new Intent(getApplicationContext(), HomeAvailabilityActivity.class);
			setResult(RESULT_CANCELED, result);
			finish();
			break;
		case R.id.menu_submit:
			String errors = checkInput();

			if (errors.length() > 0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(getString(R.string.menu_availability));
				builder.setMessage("Please complete :\n" + errors);
				builder.setPositiveButton(getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								// Nothing
							}
						});
				AlertDialog dialog = builder.create();
				dialog.show();
				
			} else {
				
				// Check if selected start - end date is a range
				Calendar availabilityStartCal = Calendar.getInstance();
				availabilityStartCal.setTime(availabilityDateStart);
				
				Calendar availabilityEndCal = Calendar.getInstance();
				availabilityEndCal.setTime(availabilityDateEnd);

				if(!ProcessDataUtils.sameDay(availabilityStartCal, availabilityEndCal)) {
					// If user select start-end date range and not select repeat, make it all days
					if(repeat_days_integer == null || repeat_days_integer.length() == 0) {
						repeat_days_integer = "0,1,2,3,4,5,6";
					}
				} else {
					repeat_days_integer = null;
				}
				
				
				if (update) {
					doEditAvailability();
				} else {
					doAddAvailability();
				}
			}

			break;
		}
		return super.onOptionsItemSelected(item);
	}
	

	LocationListener locationListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.e(TAG, "onStatusChanged()");
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.e(TAG, "onProviderEnabled()");
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.e(TAG, "onProviderDisabled()");
		}

		@Override
		public void onLocationChanged(Location location) {
			Log.e(TAG, "onLocationChanged()");
			if (!locationLoaded) {
//				loadMap(location.getLatitude() + "," + location.getLongitude());
			}
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == CommonUtilities.CREATEAVAILABILITY_RC_REPEAT) {
				repeat_days = data.getExtras().getString(CommonUtilities.CREATEAVAILABILITY_REPEAT_DAYS);
				repeat_days_integer = data.getExtras().getString(CommonUtilities.CREATEAVAILABILITY_REPEAT_DAYS_INTEGER);				
				repeat_start_date = data.getExtras().getString(CommonUtilities.CREATEAVAILABILITY_REPEAT_START_DATE);
				repeat_end_date = data.getExtras().getString(CommonUtilities.CREATEAVAILABILITY_REPEAT_END_DATE);				

				Log.d(TAG, "Repeat " + repeat_days + " " + repeat_days_integer);

				// Remove last comma if any
				if(repeat_days.length() > 0) {
					repeat_days = repeat_days.substring(0, repeat_days.length()-1);
				}
				
				labelRepeat.setText("Repeat : " + repeat_days);				
				Log.d(TAG, "Repeat : " + repeat_days + " " + repeat_start_date + " " + repeat_end_date);

//				loadMap(location);
			} else if (requestCode == CommonUtilities.CREATEAVAILABILITY_RC_MAPS_ACTIVITY) {
				String listLocation = data.getExtras().getString(CommonUtilities.CREATEAVAILABILITY_MAP_REGION);
				location = data.getExtras().getString(CommonUtilities.CREATEAVAILABILITY_MAP_REGION_ID);				
				labelLocation.setText(getResources().getString(R.string.location_preference) + ": " + listLocation);
			}
		}
	}
	
	@Deprecated
	protected void loadMap(String location) {
		if (location != null && !location.equalsIgnoreCase("null")
				&& location.length() > 1) {
			String latitude = location.substring(0, location.indexOf(","));
			String longtitude = location.substring(location.indexOf(",") + 1);
			LatLng pos = new LatLng(Double.parseDouble(latitude),
					Double.parseDouble(longtitude));

			map.clear();
			map.addMarker(new MarkerOptions().position(pos)
					.title(getString(R.string.app_name))
					.snippet(getString(R.string.map_availability_area))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
			// Move the camera instantly to hamburg with a zoom of 15.
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));
			// Zoom in, animating the camera.
			map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
		}
	}

	@Deprecated
	private void loadLocation() {
		Log.e(TAG, "loadLocation()");

		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean isGPSEnabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// getting network status
		boolean isNetworkEnabled = locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		// Check if enabled and if not send user to the GPS settings
		// Better solution would be to display a dialog and suggesting to
		// go to the settings
		if (!isGPSEnabled && !isNetworkEnabled) {
			Log.e(TAG, "NO NETWORK PROVIDER IS ENABLED !");
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		} else {
			// if GPS Enabled get lat/long using GPS Services
			if (isGPSEnabled) {
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 6000, 10,
						locationListener);
			} else if (isNetworkEnabled) {
				// First get location from Network Provider
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 6000, 10,
						locationListener);
			}
		}
	}


	
}

