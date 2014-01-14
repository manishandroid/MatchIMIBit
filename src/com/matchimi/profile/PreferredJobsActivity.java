package com.matchimi.profile;

import static com.matchimi.CommonUtilities.API_CREATE_AND_AVAILABILITY;
import static com.matchimi.CommonUtilities.API_CREATE_REPEATED_AVAILABILITY;
import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SERVERURL;
import static com.matchimi.CommonUtilities.SETTING_THEME;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.THEME_LIGHT;
import static com.matchimi.CommonUtilities.USER_PTID;

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
	private String repeat_days = null;
	private SimpleDateFormat availabilityDateTime;
	
	private boolean update = false;
	private boolean locationLoaded = false;

	private List<String> selectedJobs = new ArrayList<String>();
	private List<Integer> selectedJobsInteger = new ArrayList<Integer>();
	private Date selectedDate;
	private String pt_id;
	
	private Map<Integer, String> mapPreferredJob = new HashMap<Integer, String>();
	private String responseString;
	private JSONParser jsonParser = null;
	private String jsonStr = null;
	private ProgressDialog progress;
	private Integer jobsID = 0;
	private String[] preferedJobsList;
	
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

		setContentView(R.layout.preferred_jobs);
		
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
		}

		context = this;

		Button buttonSet = (Button) findViewById(R.id.preferredSetButton);
		buttonSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
					submitPreferredJob();				
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
		
		preferedJobsList = getResources().getStringArray(R.array.preferred_jobs);
		
		loadData();
		
	}
	
	private void loadPreferredJobs() {
		
		String hotelLabel = getResources().getString(R.string.preferred_hotel);
		String salesLabel = getResources().getString(R.string.preferred_sales);
		String fanLabel = getResources().getString(R.string.preferred_fandb);
		String factoryLabel = getResources().getString(R.string.preferred_factory);
		
		for(int i=0; i<preferedJobsList.length;i++) {
			mapPreferredJob.put(i, preferedJobsList[i]);
		}
		
		LinearLayout listRepeatView = (LinearLayout) findViewById(R.id.preferedJobsList);
		
		// Add dynamic linear layoout from xml template
		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
		for (Map.Entry<Integer, String> entry : mapPreferredJob.entrySet()) {
			if(entry.getKey() == 0) {
				setLabel(listRepeatView, hotelLabel);
			} else if(entry.getKey() == 5) {
				setLabel(listRepeatView, salesLabel);
			} else if(entry.getKey() == 8) {
				setLabel(listRepeatView, fanLabel);
			} else if(entry.getKey() == 10) {
				setLabel(listRepeatView, factoryLabel);
			}
			
			View dynamicView = vi.inflate(R.layout.preferred_job_list, null);
			TextView jobText = (TextView) dynamicView.findViewById(R.id.preferredJobsName);
			jobText.setText(entry.getValue());

			CheckBox checkboxJob = (CheckBox) dynamicView.findViewById(R.id.preferredJobsCheckbox);			
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
			
			if(selectedJobsInteger.size() == 0) {
				checkboxJob.setChecked(true);
			} else if(selectedJobsInteger.size() > 0) {
				for(int k = 0; k < selectedJobsInteger.size(); k++) {
					if(entry.getKey() == selectedJobsInteger.get(k)) {
						checkboxJob.setChecked(true);
					}
				}
			}
			
		}
	}
	
	private void setLabel(LinearLayout listRepeatView,  String title) {
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
				CheckBox dayCheckbox = (CheckBox) v.findViewById(R.id.preferredJobsCheckbox);
				Log.d(CommonUtilities.TAG, "Clicked position " + position + " with status " + dayCheckbox.isChecked());
				
				dayCheckbox.toggle();
				
				if(dayCheckbox.isChecked()) {
					selectedJobsInteger.add(new Integer(position));
				} else {
					selectedJobsInteger.remove(new Integer(position));
					Log.d(CommonUtilities.TAG, "Remove position " + position);
				}
								
				// TODO
				Log.d(TAG, "SelectedJobs now " + selectedJobsInteger.toString());
			}
		};
	}
	
	protected void submitPreferredJob() {
		final String url = SERVERURL + CommonUtilities.API_EDIT_AND_PREFERRED_JOB_FUNCTION;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Intent returnIntent = new Intent();
						setResult(RESULT_OK, returnIntent);
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
				getString(R.string.preferred_jobs),
				getString(R.string.preferred_jobs_loading), true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();

				try {
					JSONObject parentData = new JSONObject();
					JSONObject childData = new JSONObject();
					
					childData.put("pt_id", pt_id);
					childData.put("job_functions", selectedJobsInteger.toString());

					parentData.put("part_timer", childData);

					String[] params = { "data" };
					String[] values = { parentData.toString() };
					jsonStr = jsonParser.getHttpResultUrlPut(url, params,
							values);
					Log.e(TAG, "Put data to " + url + " with data >>>\n"
							+ parentData.toString());
					Log.e(TAG, "Submit preferred jobs result >>> " + jsonStr);

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
	 * Loading days
	 */
	private void loadData() {
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_PREFERRED_JOB_FUNCTIONS
				+ "?" + CommonUtilities.PARAM_PT_ID + "=" + pt_id;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				responseString = null;

				if (jsonStr != null) {
					Log.d(CommonUtilities.TAG, "RESULT >>> " + jsonStr);
					
					try {
						JSONArray items = new JSONArray(jsonStr);
						Log.d(TAG, "Item length is " + items.length());

						if (items != null && items.length() > 0) {						
							for (int i = 0; i < items.length(); i++) {
								/* get all json items, and put it on list */
								try {
									JSONObject objs = items.getJSONObject(i);
									objs = objs.getJSONObject(CommonUtilities.PARAM_PREFERRED_JOBS);
									
									if (objs != null) {
										String jobFunctionName = jsonParser.getString(objs,
												CommonUtilities.PARAM_PREFERRED_JOBS_NAME);
										int jobFunctionID = Integer.parseInt(jsonParser.getString(objs,
												CommonUtilities.PARAM_PREFERRED_JOBS_FUNCTION));
										Log.d(TAG, "Job name " + jobFunctionName + " with ID " + jobFunctionID);
										selectedJobsInteger.add(jobFunctionID);
									}
								} catch (JSONException e) {
									Log.e("Parse Json Object",
											">> " + e.getMessage());
								}
							}
							
							Log.d(CommonUtilities.TAG, "Result " + selectedJobsInteger.toString());
							
						} else if(items != null && items.length() == 0) {
							for(int i=0; i<preferedJobsList.length;i++) {
								selectedJobsInteger.add(i);
							}
						} else {
							Log.e("Parse Json Object", ">> Array is null");
						}
						
						loadPreferredJobs();
						
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
				getString(R.string.loading_preferred_jobs),
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
