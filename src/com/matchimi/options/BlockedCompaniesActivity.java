package com.matchimi.options;

import static com.matchimi.CommonUtilities.API_GET_BLOCKED_COMPANIES_BY_PT_ID;
import static com.matchimi.CommonUtilities.PARAM_PT_ID;
import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SERVERURL;
import static com.matchimi.CommonUtilities.SETTING_THEME;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.THEME_LIGHT;
import static com.matchimi.CommonUtilities.USER_PTID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONHelper;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class BlockedCompaniesActivity extends SherlockActivity {
	
	private Context context;
	private String pt_id;
	
	private ProgressDialog progress;
	
	private List<String> listAddress;
	private List<String> listGrade;
	private List<String> listName;
	private List<String> listPostalCode;
	private LinearLayout blockedCompaniesLayout;
	
	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private List<Map> blockedCompanies = new ArrayList();
	private JSONObject companyData;
	private ListView listview;

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
		setContentView(R.layout.blocked_companies);

		context = this;
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		
		blockedCompaniesLayout = (LinearLayout) findViewById(R.id.blockedCompaniesLayout);

		pt_id = settings.getString(USER_PTID, null);
		loadBlockedCompanies();
		
	}

	private void loadBlockedCompanies() {
		final String url = SERVERURL + API_GET_BLOCKED_COMPANIES_BY_PT_ID + "?"
				+ PARAM_PT_ID + "=" + pt_id;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				listAddress = new ArrayList<String>();
				listGrade = new ArrayList<String>();
				listName= new ArrayList<String>();
				listPostalCode = new ArrayList<String>();
				
				if (jsonStr != null) {
					try {
						Log.e(TAG, "Blocked companies result >>> " + jsonStr);
						
						JSONArray items = new JSONArray(jsonStr);
						if (items != null && items.length() > 0) {
							for (int i = 0; i < items.length(); i++) {
								JSONObject objs = items.getJSONObject(i);
								objs = objs.getJSONObject(CommonUtilities.PARAM_BLOCKED_COMPANIES);
								listAddress.add(jsonParser.getString(objs,
										CommonUtilities.PARAM_BLOCKED_COMPANIES_ADDRESS));
								listGrade.add(jsonParser.getString(objs,
										CommonUtilities.PARAM_BLOCKED_COMPANIES_GRADE_ID));
								listName.add(jsonParser.getString(objs,
										CommonUtilities.PARAM_BLOCKED_COMPANIES_NAME));
								
								String information = "";
								String postal_code = jsonParser.getString(objs,
										CommonUtilities.PARAM_BLOCKED_COMPANIES_POSTAL_CODE);
								if(postal_code.length() > 0) {
									information += "Postal code: " + postal_code + "\n";
								}
								listPostalCode.add(information);
							}
						} else {
							Log.e(TAG, "Result array is null");
						}
					} catch (JSONException e1) {						
						NetworkUtils.connectionHandler(BlockedCompaniesActivity.this, jsonStr,
								e1.getMessage());						
					}
				} else {
					Toast.makeText(context,
							getString(R.string.server_error), Toast.LENGTH_SHORT).show();
				}
				createLayout();
			}
		};

		progress = ProgressDialog.show(context, context.getString(R.string.app_name),
				getString(R.string.blocked_companies_loading), true, false);
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
	
	protected void createLayout() {
		if (listName != null && listName.size() > 0) {
			for (int i = 0; i < listName.size(); i++) {
				LayoutInflater li = LayoutInflater.from(context);
				View v = li.inflate(R.layout.blocked_companies_list, null);
				TextView nameView = (TextView) v
						.findViewById(R.id.blockedcompanies_company_name);
				nameView.setText(listName.get(i));
				
				RatingBar rateGrade = (RatingBar) v
						.findViewById(R.id.blockedcompanies_ratingBar);
				rateGrade.setRating(Float.parseFloat(listGrade.get(i)));

				TextView addressView = (TextView) v
						.findViewById(R.id.blockedcompanies_address);
				addressView.setText(listAddress.get(i));
				
				TextView informationView = (TextView) v
						.findViewById(R.id.blockedcompanies_information);
				informationView.setText(listPostalCode.get(i));
				
				blockedCompaniesLayout.addView(v);
			}
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
