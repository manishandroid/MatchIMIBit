package com.matchimi.availability;

import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SETTING_THEME;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.THEME_LIGHT;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.matchimi.Api;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.options.CreateAvailability;
import com.matchimi.options.CreateAvailabilityBackup;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class RepeatAvailabilityActivity extends SherlockFragmentActivity {
	private Context context;
	private String location;
	
	private String pt_id = null;
	private String price = null;
	private String avail_id = null;
	private int repeat = 0;
	private String repeat_days = null;
	private String repeat_days_integer = null;
	private String repeat_start_date = null;
	private String repeat_end_date = null;
	
	private SimpleDateFormat availabilityDateTime;
	
	private boolean update = false;
	private boolean locationLoaded = false;

	private List<String> selectedRepeat = new ArrayList<String>();
	private List<Integer> selectedRepeatInteger = new ArrayList<Integer>();
	private Date selectedDate;
	
	private Map<Integer, String> mapDays = new HashMap<Integer, String>();
	private String responseString;
	private JSONParser jsonParser = null;
	private String jsonStr = null;
	private ProgressDialog progress;
	
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

		setContentView(R.layout.availability_repeat);
		
		availabilityDateTime = CommonUtilities.AVAILABILTY_DATE;

		Bundle b = getIntent().getExtras();
		if (b != null) {
			repeat_days = b.getString(CommonUtilities.CREATEAVAILABILITY_REPEAT_DAYS);
			
			if(repeat_days != null && repeat_days.length() > 0) {
				List<String> items = Arrays.asList(location.split("\\s*,\\s*"));
				selectedRepeat = new ArrayList<String>();
				
				for (String item : items) {
					selectedRepeat.add(item);
				}
			}

			repeat_start_date = b.getString(CommonUtilities.CREATEAVAILABILITY_REPEAT_START_DATE);
			repeat_end_date = b.getString(CommonUtilities.CREATEAVAILABILITY_REPEAT_END_DATE);
		}

		context = this;

		Button buttonSet = (Button) findViewById(R.id.buttonSet);
		buttonSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				repeat_days = "";
				repeat_days_integer = "";
				
				if(repeat_start_date == null ||
				   repeat_end_date == null ||
				   repeat_start_date.length() < 1 ||
				   repeat_end_date.length() < 1) {
					
					Toast.makeText(
							context,
							getString(R.string.repeat_start_end_date_incomplete),
							Toast.LENGTH_SHORT).show();

				} else {
					
					repeat_days_integer = "";
					repeat_days = "";
					
					// Remove last comma if any
					if (selectedRepeatInteger.size() > 0) {
						for(Integer repeatInteger : selectedRepeatInteger) {
							for (Map.Entry<Integer, String> entry : mapDays.entrySet()) {
								if(entry.getKey() == repeatInteger) {
									repeat_days += entry.getValue() + ",";
								}
								
							}
							repeat_days_integer += repeatInteger + ",";
						}
					}
					
					// Remove last comma if any
					if (repeat_days_integer.length() > 0) {
						repeat_days_integer = repeat_days_integer.substring(0,
								repeat_days_integer.length() - 1);
					}

					Intent i = new Intent(context, CreateAvailability.class);
					i.putExtra(CommonUtilities.CREATEAVAILABILITY_REPEAT_DAYS, repeat_days);
					i.putExtra(CommonUtilities.CREATEAVAILABILITY_REPEAT_DAYS_INTEGER, repeat_days_integer);
					i.putExtra(CommonUtilities.CREATEAVAILABILITY_REPEAT_START_DATE, repeat_start_date);
					i.putExtra(CommonUtilities.CREATEAVAILABILITY_REPEAT_END_DATE, repeat_end_date);
					setResult(RESULT_OK, i);
					finish();
					
				}				
			}
		});

		Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED, returnIntent);
				finish();
			}
		});
		

		TextView labelStart = (TextView) findViewById(R.id.labelRepeatStart);
		labelStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDateDialog(true);
			}
		});

		TextView labelEnd = (TextView) findViewById(R.id.labelRepeatEnd);
		labelEnd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (repeat_start_date != null) {
					showDateDialog(false);
				} else {
					Toast.makeText(
							context,
							getString(R.string.availability_select_date_validation),
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		loadDays();
	}
	
	protected void showDateDialog(boolean withDate) {
		final Calendar cal;

		if (withDate) {
			if (repeat_start_date != null) {
				cal = generateCalendar(repeat_start_date);
			} else if (selectedDate != null){
				cal = Calendar.getInstance();
				cal.setTime(selectedDate); 
			} else {
				cal = new GregorianCalendar(Locale.getDefault());
			}			
		
			DatePickerDialog dialogDate = new DatePickerDialog(context,
					new OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker arg0, int arg1,
								int arg2, int arg3) {
							cal.set(Calendar.YEAR, arg1);
							cal.set(Calendar.MONTH, arg2);
							cal.set(Calendar.DAY_OF_MONTH, arg3);
							selectedDate = null;

							// Restrict start date not older
							// than now
							Date today = new Date();
							if (today.after(cal.getTime())) {
								// Use the Builder class for
								// convenient dialog
								// construction
								AlertDialog.Builder builder = new AlertDialog.Builder(
										RepeatAvailabilityActivity.this);
								builder.setMessage(
										R.string.availability_start_error)
										.setPositiveButton(
												R.string.ok,
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
														repeat_start_date = null;
														repeat_end_date = null;
														reloadView();
													}
												});
								// Create the AlertDialog object
								// and return it
								Dialog dialog = builder.create();
								dialog.show();
							} else {
								if(repeat_end_date != null) {
									Date endAvailability = null;
									try {
										endAvailability = availabilityDateTime.parse(repeat_end_date);
										
										// If end date is before start
										if (endAvailability != null && endAvailability.getTime() <= cal.getTimeInMillis()) {
											repeat_end_date = null;
										}
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}

								selectedDate = cal.getTime();
								repeat_start_date = availabilityDateTime.format(cal.getTime());
								reloadView();
							}

						}
					}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DAY_OF_MONTH));
			dialogDate.show();
			
		} else {

			if (repeat_end_date != null) {
				cal = generateCalendar(repeat_end_date);
			} else if (selectedDate != null){
				cal = Calendar.getInstance();
				Date startAvailability = null;
				
				try {
					cal.setTime(availabilityDateTime.parse(repeat_start_date));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				cal = new GregorianCalendar(Locale.getDefault());
			}			
		
			DatePickerDialog dialogDate = new DatePickerDialog(context,
					new OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker arg0, int arg1,
								int arg2, int arg3) {
							cal.set(Calendar.YEAR, arg1);
							cal.set(Calendar.MONTH, arg2);
							cal.set(Calendar.DAY_OF_MONTH, arg3);
							selectedDate = null;
							
							// Restrict start date not older
							// than now
							Date today = new Date();
							if (today.after(cal.getTime())) {
								// Use the Builder class for
								// convenient dialog
								// construction
								AlertDialog.Builder builder = new AlertDialog.Builder(
										RepeatAvailabilityActivity.this);
								builder.setMessage(
										R.string.availability_start_error)
										.setPositiveButton(
												R.string.ok,
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
														repeat_start_date = null;
														repeat_end_date = null;
														reloadView();
													}
												});
								// Create the AlertDialog object
								// and return it
								Dialog dialog = builder
										.create();
								dialog.show();

							} else {

								Date startAvailability = null;
								try {
									startAvailability = availabilityDateTime.parse(repeat_start_date);
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								// If end date is before start
								if (startAvailability != null && startAvailability.getTime() >= cal.getTimeInMillis()) {
									repeat_end_date = null;
									
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
															reloadView();
														}
													});
									// Create the AlertDialog object and return
									// it
									Dialog dialog = builder.create();
									dialog.show();

								} else if(startAvailability != null) {
									repeat_end_date = availabilityDateTime.format(cal.getTime());
									selectedDate = cal.getTime();
									reloadView();									
								}
							}
						}
					}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DAY_OF_MONTH));
			dialogDate.show();
		}
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
				Integer.parseInt(str.substring(8, 10)), 11, 59, 59);

		return calRes;
	}

	private void reloadView() {
		TextView labelStart = (TextView) findViewById(R.id.labelRepeatStart);
		TextView labelEnd = (TextView) findViewById(R.id.labelRepeatEnd);

		labelStart.setText(getResources().getString(R.string.availability_start_repeat_date) + " :");
		labelEnd.setText(getResources().getString(R.string.availability_end_repeat_date) + " :");

		if (repeat_start_date != null) {
			Calendar calStart = generateCalendar(repeat_start_date);
			labelStart.setText(getResources().getString(R.string.availability_start_repeat_date) + " : "
					+ CommonUtilities.AVAILABILTY_DATETIME_CALENDAR.format(
							calStart.getTime()));
		}

		if (repeat_end_date != null) {
			Calendar calEnd = generateCalendar(repeat_end_date);
			labelEnd.setText(getResources().getString(R.string.availability_end_repeat_date) + " : "
					+ CommonUtilities.AVAILABILTY_DATETIME_CALENDAR
							.format(calEnd.getTime()));
		}
	}
	
	private View.OnClickListener repeatDayOnClick(final View view, final int position) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckBox dayCheckbox = (CheckBox) v.findViewById(R.id.dayCheckbox);
				dayCheckbox.toggle();
				
				if(selectedRepeatInteger.contains(position)) {
					selectedRepeatInteger.remove(new Integer(position));
				} else {
					selectedRepeatInteger.add(position);
				}
				
				// TODO
				Log.d(TAG, "Position " + position);
			}
		};
	}
	
	/**
	 * Loading days
	 */
	private void loadDays() {
		Log.d(TAG, "Loading days ...");

		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_DAYS;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				responseString = null;

				if (jsonStr != null) {
					try {
						JSONArray items = new JSONArray(jsonStr);
						Log.d(TAG, "Item length is " + items.length());

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
										Log.d(TAG, "Days is " + dayName);
										mapDays.put(dayID, dayName);
									}
								} catch (JSONException e) {
									Log.e("Parse Json Object",
											">> " + e.getMessage());
								}
							}
														
							// Add dynamic linear layoout from xml template
							LinearLayout listRepeatView = (LinearLayout) findViewById(R.id.dayRepeatList);
							LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							selectedRepeatInteger = new ArrayList<Integer>();
							
							for (Map.Entry<Integer, String> entry : mapDays.entrySet()) {
								View dynamicView = vi.inflate(R.layout.availability_repeat_list, null);
								TextView textDate = (TextView) dynamicView.findViewById(R.id.textDayName);
								textDate.setText(entry.getValue());
								
								dynamicView.setOnClickListener(repeatDayOnClick(dynamicView, entry.getKey()));
								listRepeatView.addView(dynamicView);
								
								// check if selected repeat match
								if(selectedRepeat.size() > 0) {
									for(String dayRepeat : selectedRepeat) {
										if(dayRepeat.equals(entry.getValue())) {
											selectedRepeatInteger.add(entry.getKey());
											dynamicView.performClick();
										}
									}
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
				} else {
					Toast.makeText(context.getApplicationContext(),
							getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context, getString(R.string.loading_data), 
				getString(R.string.loading_repeat_days),
				true, false);
		
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
	}
	
}
