package com.matchimi.options;

import static com.matchimi.CommonUtilities.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

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
import android.widget.NumberPicker;
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
import com.matchimi.availability.RepeatAvailabilityActivity;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class CreateAvailability extends SherlockFragmentActivity {

	private Context context;
	private Date selectedDate;
	
	private ProgressDialog progress;
	private GoogleMap map;

	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private String pt_id = null;
	private String start = null;
	private String end = null;
	private String location = null;
	private String price = null;
	private String avail_id = null;
	private String repeat_days;
	private String repeat_days_integer;
	private String repeat_start_date;
	private String repeat_end_date;
	
	private boolean update = false;
	private boolean locationLoaded = false;

	private String[] repeatString = null;
	private List<CharSequence> listRepeatWeeksShort;	
	private String[] regionAvailabilityArray;
	
	private List<Integer> selectedRepeat = null;

	public static final int RC_MAPS_ACTIVITY = 50;
	private SimpleDateFormat sdf;

	private TextView labelLocation;
	private TextView labelRepeat;
	
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
		
		Intent intent = getIntent();
		if(intent.hasExtra(AVAILABILITY_SELECTED)) {
			try {
				selectedDate = CommonUtilities.AVAILABILTY_DATETIME.parse(intent.getStringExtra(AVAILABILITY_SELECTED));
				Log.d(TAG, "Selected date " + selectedDate);
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		repeatString = context.getResources().getStringArray(R.array.repeat_week);
		regionAvailabilityArray = getResources().getStringArray(
				R.array.region_availability);
		
		CharSequence[] repeatShortArray = getResources().getStringArray(R.array.repeat_week_short);
		listRepeatWeeksShort = Arrays.asList(repeatShortArray);

		TextView labelStart = (TextView) findViewById(R.id.labelStart);
		labelStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDateDialog(true);
			}
		});

		TextView labelEnd = (TextView) findViewById(R.id.labelEnd);
		labelEnd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (start != null) {
					showDateDialog(false);
				} else {
					Toast.makeText(
							context,
							getString(R.string.availability_select_date_validation),
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		labelRepeat = (TextView) findViewById(R.id.labelRepeat);
		labelRepeat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(context, RepeatAvailabilityActivity.class);
				startActivityForResult(i, CommonUtilities.CREATEAVAILABILITY_RC_REPEAT);
				
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
		
		Bundle b = getIntent().getExtras();
		update = b.getBoolean("update");
		Log.d(TAG, "Update data " + " Create availability");

		if (update) {
			ab.setTitle("Edit Availability");
			
			start = b.getString("start");
			end = b.getString("end");
			location = b.getString("location");
			price = b.getString("price");
			avail_id = b.getString("avail_id");

			Log.d(TAG, "Update data " + start);
			reloadView();
		} else {
			ab.setTitle(getString(R.string.create_availability));
		}

		map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

//		if (location != null && !location.equalsIgnoreCase("null")
//				&& location.length() > 1) {
////			loadMap(location);
//		} else {
////			loadLocation();
//		}
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

	private void reloadView() {
		TextView labelStart = (TextView) findViewById(R.id.labelStart);
		TextView labelEnd = (TextView) findViewById(R.id.labelEnd);
		TextView labelSalary = (TextView) findViewById(R.id.labelSalary);

		labelStart.setText("Start at :");
		labelEnd.setText("End at :");

		if (start != null) {
			Calendar calStart = generateCalendar(start);
			labelStart.setText("Start at : "
//					+ CommonUtilities.AVAILABILITY_DATE.format(calStart
//							.getTime())
//					+ ", "
					+ CommonUtilities.AVAILABILITY_TIME.format(
							calStart.getTime()));
//							.toLowerCase(Locale.getDefault()));
		}

		if (end != null) {
			Calendar calEnd = generateCalendar(end);
			labelEnd.setText("End at : "
//					+ CommonUtilities.AVAILABILITY_DATE.format(calEnd.getTime())
//					+ ", "
					+ CommonUtilities.AVAILABILITY_TIME
							.format(calEnd.getTime()));
//							.toLowerCase(Locale.getDefault()));
		}

		if(repeat_days != null) {
			// Remove last comma if any
			if(repeat_days.length() > 0) {
				repeat_days = repeat_days.substring(0, repeat_days.length()-1);
			}
			
			// Remove last comma if any
			if(repeat_days_integer.length() > 0) {
				repeat_days_integer = repeat_days_integer.substring(0, 
						repeat_days_integer.length()-1);
			}

			labelRepeat.setText("Repeat : " + repeat_days);			
		}
		
		if(location != null && location.length() > 0) {
			Log.d(TAG, "Location values are " + location);
			
			String locationRegion = "";
			List<String> items = Arrays.asList(location.split("\\s*,\\s*"));

			for (String item : items) {
				for (int i = 0; i < regionAvailabilityArray.length; i++) {
					try {
						int itemInt = Integer.parseInt(item);
						
						if(itemInt == i) {
							locationRegion += regionAvailabilityArray[Integer.parseInt(item)] + ",";					
						}

					} catch (Exception e) {
						
					}
					
				}
			}
			
			// Remove last comma if any
			if(locationRegion.length() > 0) {
				locationRegion = locationRegion.substring(0, locationRegion.length()-1);
			}
			
			labelLocation.setText(getResources().getString(R.string.location_preference) + ": " + locationRegion);			
		}
		
//		if (price != null) {
//			labelSalary.setText("Ask Salary : $" + price);
//		}
	}

	protected void showDateDialog(boolean withDate) {
		sdf = CommonUtilities.AVAILABILTY_DATETIME;
		final Calendar cal;

		if (withDate) {
			if (start != null) {
				cal = generateCalendar(start);
			} else if (selectedDate != null){
				cal = Calendar.getInstance();
				cal.setTime(selectedDate); 
			} else {
				cal = new GregorianCalendar(Locale.getDefault());
			}
			
			if(selectedDate == null) {
				Log.d(TAG, "Selected date " + selectedDate);
				
				DatePickerDialog dialogDate = new DatePickerDialog(context,
						new OnDateSetListener() {
							@Override
							public void onDateSet(DatePicker arg0, int arg1,
									int arg2, int arg3) {
								cal.set(Calendar.YEAR, arg1);
								cal.set(Calendar.MONTH, arg2);
								cal.set(Calendar.DAY_OF_MONTH, arg3);
								startDateDialog(cal);								
							}
						}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
						cal.get(Calendar.DAY_OF_MONTH));
				dialogDate.show();
				
			} else {
				startDateDialog(cal);
			}
			
		} else {

			cal = generateCalendar(start);
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			final View view = LayoutInflater.from(context).inflate(
					R.layout.custome_time, null);
			builder.setView(view);

			builder.setTitle(getString(R.string.availability_set_time));

			final NumberPicker np1 = (NumberPicker) view
					.findViewById(R.id.numberPicker1);
			final String[] nums1 = generateHour(0);
			np1.setMaxValue(23);
			np1.setMinValue(0);
			np1.setWrapSelectorWheel(false);
			np1.setDisplayedValues(nums1);
			
			final NumberPicker np2 = (NumberPicker) view
					.findViewById(R.id.numberPicker2);
			final String[] nums2 = { "00", "15", "30", "45" };
			np2.setMaxValue(3);
			np2.setMinValue(0);
			np2.setWrapSelectorWheel(false);
			np2.setDisplayedValues(nums2);
			
			try {
				Date getStart = sdf.parse(start);
				Calendar currentStart = Calendar.getInstance();
				currentStart.setTime(getStart);
				int currentHour = currentStart.get(Calendar.HOUR_OF_DAY);
				int currentMinute = currentStart.get(Calendar.MINUTE);
				if (currentMinute > 30) {
					np1.setValue(currentHour + 1);
				} else {
					np1.setValue(currentHour);
					int increment = currentMinute / 15;
					np2.setValue(increment + 1);
				}
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			if(update && end != null) {
				Calendar calEnd = generateCalendar(end);
				np1.setValue(calEnd.get(Calendar.HOUR_OF_DAY));
				np2.setValue(calEnd.get(Calendar.MINUTE));
			}


			builder.setPositiveButton(R.string.set,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							cal.set(Calendar.HOUR_OF_DAY,
									Integer.parseInt(nums1[np1.getValue()]));
							cal.set(Calendar.MINUTE,
									Integer.parseInt(nums2[np2.getValue()]));

							try {
								Date startAvailability = sdf.parse(start);
								Log.d(TAG, "Start comparing ..");

								// If end date is before start
								if (startAvailability.getTime() >= cal.getTimeInMillis()) {
									end = null;
									
									AlertDialog.Builder builder = new AlertDialog.Builder(
											context);
									String message = "";
									
									if (startAvailability.getTime() == cal.getTimeInMillis()) {
										message = getString(R.string.availability_end_equal);
									} else {
										message = getString(R.string.availability_end_overlap);
									}
									
									builder.setMessage(message)
											.setPositiveButton(
													R.string.ok,
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int id) {

														}
													});
									// Create the AlertDialog object and return
									// it
									Dialog dialog = builder.create();
									dialog.show();

								// Check if the difference of start and end more than 12 hours
//								} else if(((cal.getTimeInMillis()-startAvailability.getTime())/(1000*60*60)) > 12)  {
//									Log.d(TAG, "Diff " + ((cal.getTimeInMillis()-startAvailability.getTime())/(1000*60*60)));
//									
//									AlertDialog.Builder builder = new AlertDialog.Builder(
//											context);
//									String message = getString(R.string.availability_end_12_hours);
//									
//									builder.setMessage(message)
//											.setPositiveButton(
//													R.string.ok,
//													new DialogInterface.OnClickListener() {
//														public void onClick(
//																DialogInterface dialog,
//																int id) {
//
//														}
//													});
//									// Create the AlertDialog object and return
//									// it
//									Dialog dialog = builder.create();
//									dialog.show();

								} else {
									end = sdf.format(cal.getTime());
								}
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							reloadView();
						}
					});

			builder.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
						}
					});
			
			AlertDialog dialogTime = builder.create();
			// TimePickerDialog dialogTime = new TimePickerDialog(context,
			// new OnTimeSetListener() {
			// @Override
			// public void onTimeSet(TimePicker arg0, int arg1,
			// int arg2) {
			// cal.set(Calendar.HOUR_OF_DAY, arg1);
			// cal.set(Calendar.MINUTE, arg2);
			// end = sdf.format(cal.getTime());
			// reloadView();
			// }
			// }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
			// true);
			dialogTime.show();
		}
	}

	
	private void startDateDialog(final Calendar cal) {
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
		np1.setWrapSelectorWheel(false);
		np1.setDisplayedValues(nums1);

		final NumberPicker np2 = (NumberPicker) view
				.findViewById(R.id.numberPicker2);
		final String[] nums2 = { "00", "15", "30", "45" };
		np2.setMaxValue(3);
		np2.setMinValue(0);
		np2.setWrapSelectorWheel(false);
		np2.setDisplayedValues(nums2);
		
		if(update && start != null) {
			Calendar calStart = generateCalendar(start);
			np1.setValue(calStart.get(Calendar.HOUR_OF_DAY));
			np2.setValue(calStart.get(Calendar.MINUTE));
		}

		builder.setPositiveButton(R.string.set,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(
							DialogInterface arg0, int arg1) {
						cal.set(Calendar.HOUR_OF_DAY,
								Integer.parseInt(nums1[np1
										.getValue()]));
						cal.set(Calendar.MINUTE, Integer
								.parseInt(nums2[np2
										.getValue()]));
						start = sdf.format(cal.getTime());

						// Restrict start date not older
						// than now
						Date today = new Date();
						if (today.after(cal.getTime())) {
							// Use the Builder class for
							// convenient dialog
							// construction
							AlertDialog.Builder builder = new AlertDialog.Builder(
									CreateAvailability.this);
							builder.setMessage(
									R.string.availability_start_error)
									.setPositiveButton(
											R.string.ok,
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													start = null;
													end = null;
													reloadView();
												}
											});
							// Create the AlertDialog object
							// and return it
							Dialog dialog = builder
									.create();
							dialog.show();

						} else {
							if (end != null) {
								try {
									Date endAvailability = sdf
											.parse(end);
									if (endAvailability
											.getTime() <= cal
											.getTimeInMillis()) {
										end = null;
									}
								} catch (ParseException e) {
									// TODO Auto-generated
									// catch block
									e.printStackTrace();
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

	protected void doEditAvailability() {
		final String url = SERVERURL + API_EDIT_AND_AVAILABILITY;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
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
					childData.put("asked_salary", 0);
					childData.put("start_date_time", start);
					childData.put("end_date_time", end);
					
					childData.put("avail_id", avail_id);
					if (location == null || location.equalsIgnoreCase("null")
							|| location.length() == 0) {
						location = "";
					}
					childData.put("location", "[" + location + "]");
					childData.put("from_date", repeat_start_date);
					childData.put("to_date", repeat_end_date);
					childData.put("days", "[" + repeat_days + "]");
					
					parentData.put("availability", childData);

					String[] params = { "data" };
					String[] values = { parentData.toString() };
					jsonStr = jsonParser.getHttpResultUrlPut(url, params,
							values);

					Log.e(TAG, "EDIT Post >>> " + childData.toString());
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

	private String checkInput() {
		if (start != null && end != null) {
			return "";
		} else {
			String errors = "";

			if (start == null) {
				errors += "* " + getString(R.string.start_time) + "\n";
			}

			if (end == null) {
				errors += "* " + getString(R.string.end_time) + "\n";
			}

			return errors;

		}
	}

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
						
						if (selectedDate != null) {
							Intent i = new Intent(CommonUtilities.BROADCAST_SCHEDULE_RECEIVER);
							sendBroadcast(i);
						}
					    
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

					Calendar startTime = generateCalendar(start);
					Calendar endTime = generateCalendar(end);
					
					SimpleDateFormat simpleTime = CommonUtilities.AVAILABILITY_TIME_FULL;
					
					String startTimeVal = simpleTime.format(startTime.getTime());
					String endTimeVal = simpleTime.format(endTime.getTime());
					
					childData.put("pt_id", pt_id);
					// childData.put("asked_salary",
					// price.substring(0, price.indexOf(".")));
					childData.put("asked_salary", 1);
					
					if (location == null || location.equalsIgnoreCase("null")
							|| location.length() == 0) {
						location = "";
					}
					
					if(repeat_days_integer == null) {
						childData.put("repeat", "0");
						childData.put("location", "1.280891,103.839769");
						childData.put("start_date_time", start);
						childData.put("end_date_time", end);
						parentData.put("availability", childData);
					} else {
						childData.put("start_time", startTimeVal + ":00");
						childData.put("end_time", endTimeVal + ":00");						
						childData.put("locations", "[" + location + "]");
						childData.put("days", "[" + repeat_days_integer + "]");
						
						childData.put("from_date", repeat_start_date);
						childData.put("to_date", repeat_end_date);
						parentData.put("repeated_availability", childData);						
					}
					
					String[] params = { "data" };

					String[] values = { parentData.toString() };
					jsonStr = jsonParser.getHttpResultUrlPost(url, params,
							values);
					Log.e(TAG, "Post data to " + url + " with data >>>\n"
							+ parentData.toString());
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

