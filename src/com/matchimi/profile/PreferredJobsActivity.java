package com.matchimi.profile;

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

public class PreferredJobsActivity extends SherlockFragmentActivity {
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

	private List<String> selectedJobs = new ArrayList<String>();
	private List<Integer> selectedJobsInteger = new ArrayList<Integer>();
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

		setContentView(R.layout.preferred_jobs);		
		availabilityDateTime = CommonUtilities.AVAILABILTY_DATE;

		Bundle b = getIntent().getExtras();
		if (b != null) {
			repeat_days = b.getString(CommonUtilities.CREATEAVAILABILITY_REPEAT_DAYS);
			
			if(repeat_days != null && repeat_days.length() > 0) {
				List<String> items = Arrays.asList(location.split("\\s*,\\s*"));
				selectedJobs = new ArrayList<String>();
				
				for (String item : items) {
					selectedJobs.add(item);
				}
			}

			repeat_start_date = b.getString(CommonUtilities.CREATEAVAILABILITY_REPEAT_START_DATE);
			repeat_end_date = b.getString(CommonUtilities.CREATEAVAILABILITY_REPEAT_END_DATE);
		}

		context = this;

		Button buttonSet = (Button) findViewById(R.id.preferredSetButton);
		buttonSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
					Intent returnIntent = new Intent();
					setResult(RESULT_OK, returnIntent);
					finish();					
			}		
		});

		Button buttonCancel = (Button) findViewById(R.id.preferredCancelButton);
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED, returnIntent);
				finish();
			}
		});
		
		String[] hotelArray = getResources().getStringArray(R.array.preferred_hotel);
		loadPreferredJobs(hotelArray, getResources().getString(R.string.preferred_hotel));

		String[] salesArray = getResources().getStringArray(R.array.preferred_sales);
		loadPreferredJobs(salesArray, getResources().getString(R.string.preferred_sales));
		
		String[] fandbArray = getResources().getStringArray(R.array.preferred_fandb);
		loadPreferredJobs(fandbArray, getResources().getString(R.string.preferred_fandb));
		
		String[] factoryArray = getResources().getStringArray(R.array.preferred_factory);
		loadPreferredJobs(factoryArray, getResources().getString(R.string.preferred_factory));
	}
	
	private void loadPreferredJobs(String[] valueArray, String title) {
		LinearLayout listRepeatView = (LinearLayout) findViewById(R.id.preferedJobsList);
		
		android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
		
		TextView labelPreferredJob = new TextView(context);
		labelPreferredJob.setText(title);
		labelPreferredJob
				.setTextAppearance(
						context,
						android.R.style.TextAppearance_DeviceDefault_Small);
		labelPreferredJob.setTextColor(Color.WHITE);
		labelPreferredJob.setPadding(convertDpToPixel(5),
				convertDpToPixel(5), convertDpToPixel(5),
				convertDpToPixel(5));
		labelPreferredJob.setBackgroundColor(getResources()
				.getColor(R.color.blue_sky));
		labelPreferredJob.setLayoutParams(params);
		listRepeatView.addView(labelPreferredJob);
		
		for(int i=0; i<valueArray.length;i++) {
			mapDays.put(i, valueArray[i]);
		}
		
		// Add dynamic linear layoout from xml template
		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		selectedJobsInteger = new ArrayList<Integer>();
		
		for (Map.Entry<Integer, String> entry : mapDays.entrySet()) {
			View dynamicView = vi.inflate(R.layout.preferred_job_list, null);
			TextView jobText = (TextView) dynamicView.findViewById(R.id.preferredJobsName);
			jobText.setText(entry.getValue());
			
			CheckBox checkboxJob = (CheckBox) dynamicView.findViewById(R.id.preferredJobsCheckbox);
			checkboxJob.setChecked(true);
			
			dynamicView.setOnClickListener(repeatDayOnClick(dynamicView, entry.getKey()));
			listRepeatView.addView(dynamicView);
			
			// check if selected repeat match
			if(selectedJobs.size() > 0) {
				for(String dayRepeat : selectedJobs) {
					if(dayRepeat.equals(entry.getValue())) {
						selectedJobsInteger.add(entry.getKey());
						dynamicView.performClick();
					}
				}
			}
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
	
	private View.OnClickListener repeatDayOnClick(final View view, final int position) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckBox dayCheckbox = (CheckBox) v.findViewById(R.id.dayCheckbox);
				dayCheckbox.toggle();
				
				if(selectedJobsInteger.contains(position)) {
					selectedJobsInteger.remove(new Integer(position));
				} else {
					selectedJobsInteger.add(position);
				}
				
				// TODO
				Log.d(TAG, "Position " + position);
			}
		};
	}
	
	/**
	 * Loading days
	 */
	private void loadCategories() {
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
							selectedJobsInteger = new ArrayList<Integer>();
							
							for (Map.Entry<Integer, String> entry : mapDays.entrySet()) {
								View dynamicView = vi.inflate(R.layout.availability_repeat_list, null);
								TextView textDate = (TextView) dynamicView.findViewById(R.id.textDayName);
								textDate.setText(entry.getValue());
								
								dynamicView.setOnClickListener(repeatDayOnClick(dynamicView, entry.getKey()));
								listRepeatView.addView(dynamicView);
								
								// check if selected repeat match
								if(selectedJobs.size() > 0) {
									for(String dayRepeat : selectedJobs) {
										if(dayRepeat.equals(entry.getValue())) {
											selectedJobsInteger.add(entry.getKey());
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