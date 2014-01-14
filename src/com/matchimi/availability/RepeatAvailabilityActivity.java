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
import com.matchimi.utils.ProcessDataUtils;

public class RepeatAvailabilityActivity extends SherlockFragmentActivity {
	private Context context;
	
	private String repeat_days = null;
	private String repeat_days_integer = "";
	
	private List<String> selectedRepeat = new ArrayList<String>();
	private List<Integer> selectedRepeatInteger = new ArrayList<Integer>();
	
	private Map<Integer, String> mapDays = new HashMap<Integer, String>();
	private String responseString;
	private JSONParser jsonParser = null;
	private String jsonStr = null;
	private ProgressDialog progress;
	
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;	
	private String repeatedAPIData = null;

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
		
		Bundle b = getIntent().getExtras();
		if (b != null) {
			repeat_days = b.getString(CommonUtilities.CREATEAVAILABILITY_REPEAT_DAYS);
			
			if(repeat_days != null && repeat_days.length() > 0) {
				List<String> items = Arrays.asList(repeat_days.split("\\s*,\\s*"));
				selectedRepeat = new ArrayList<String>();
				
				for (String item : items) {
					selectedRepeat.add(item);
				}
			}		
		}

		context = this;

		Button buttonSet = (Button) findViewById(R.id.buttonSet);
		buttonSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				repeat_days = "";
				repeat_days_integer = "";
				
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
				
				Log.d(CommonUtilities.TAG, "Repeat " + repeat_days + ", " + repeat_days_integer );
				setResult(RESULT_OK, i);
				finish();
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
		
		repeatedAPIData = settings.getString(CommonUtilities.API_CACHE_DAYS, null);
		
		if(repeatedAPIData == null) {
			loadDays();
		} else {
			generateView();
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
	
	private View.OnClickListener repeatDayOnClick(final View view, final int position) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Position " + position);
				
				CheckBox dayCheckbox = (CheckBox) view.findViewById(R.id.dayCheckbox);
				dayCheckbox.toggle();
				
				if(!dayCheckbox.isChecked()) {
					if(selectedRepeatInteger.contains(position)) {
						selectedRepeatInteger.remove(new Integer(position));
					}
				} else if(dayCheckbox.isChecked()) {
					if(!selectedRepeatInteger.contains(position)) {
						selectedRepeatInteger.add(position);
					}
				}
				
				// TODO
				Log.d(TAG, "Position " + position + " with checkbox: " + dayCheckbox.isEnabled() 
						+ " selectedRepeat " + selectedRepeatInteger.toString());
			}
		};
	}
	
	private void generateView() {
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
						
						// Store data into cache
						editor = settings.edit();
						editor.putString(CommonUtilities.API_CACHE_DAYS, items.toString());
						editor.commit();

						repeatedAPIData = items.toString();
						generateView();
						
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
				Log.e(CommonUtilities.TAG, "Load repeat days " + jsonStr);
				
				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
				
			}
		}.start();
	}
	
}
