package com.matchimi.ongoingjobs;

import static com.matchimi.CommonUtilities.PREFS_NAME;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.matchimi.CommonUtilities;
import com.matchimi.HomeActivity;
import com.matchimi.R;
import com.matchimi.availability.HomeAvailabilityActivity;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.ProcessDataUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class OngoingJobsActivity extends SherlockFragmentActivity{

	private List<String> listPrice = null;
	private List<String> listTime = null;
	private List<String> listPosition = null;
	private List<String> listCompany = null;
	private List<String> listLocation = null;
	
	private OngoingJobsAdapter adapter;
	private ListView listview;
	private String pt_id = null;
	
	private String jsonStr = null;
	private JSONParser jsonParser = null;
	private JSONObject jsonObject = null;
	private JSONArray jsonArray = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ongoing_job_listview);
		
		SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, 0);
		
		adapter = new OngoingJobsAdapter(this);
		pt_id = settings.getString(CommonUtilities.USER_PTID, null);
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setTitle(getString(R.string.txt_ongoing_job));
		
		loadData();
		
		listview = (ListView) findViewById(R.id.ongoing_job_listview);
		listview.setAdapter(adapter);	
		
		listview.setOnItemClickListener(listener);
	}
	
	private void loadData(){
		final String url = CommonUtilities.SERVERURL +
				CommonUtilities.API_GET_CURRENT_JOB_OFFERS +"?"+
				CommonUtilities.PARAM_PT_ID +"="+pt_id;
		final Handler handler = new Handler();
		
		new Thread(){
			public void run(){
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpsResultUrlGet(OngoingJobsActivity.this, url);
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						listPrice = new ArrayList<String>();
						listTime = new ArrayList<String>();
						listPosition = new ArrayList<String>();
						listCompany = new ArrayList<String>();
						listLocation = new ArrayList<String>();
						
						try {
							jsonArray = new JSONArray(jsonStr);	
							SimpleDateFormat formatterTime = new SimpleDateFormat(
									"hh a", Locale.getDefault());
							
							if (jsonArray.length() > 0) {
								for (int i = 0; i < jsonArray.length(); i++) {
									jsonObject = jsonArray.getJSONObject(i);
									jsonObject = jsonObject.getJSONObject("sub_slots");
										
									String startDate = jsonParser
											.getString(jsonObject,
													"start_date_time");
									String endDate = jsonParser.getString(
											jsonObject, "end_date_time");
									Calendar calStart = ProcessDataUtils.generateCalendar(startDate);
									Calendar calEnd = ProcessDataUtils.generateCalendar(endDate);
									
									listPrice.add(
											"<font color='#A4A9AF'>$</font><big><big><big><big><font color='#376e68'>"
													+ jsonParser.getDouble(jsonObject,"offered_salary")
													+ "</font></big></big></big></big><font color='#A4A9AF'>/hr</font>");
									listTime.add(formatterTime.format(calStart.getTime()).toLowerCase(Locale.getDefault())
											+" - "+formatterTime.format(calEnd.getTime()).toLowerCase(Locale.getDefault()));
									listPosition.add(""+jsonParser.getString(jsonObject,"job_function_name"));
									listCompany.add(""+jsonParser.getString(jsonObject,"company_name"));
									listLocation.add(jsonParser.getString(jsonObject, "location"));
								}
							}else{
								Log.e(CommonUtilities.TAG, ">> Array parse json is null ");
							}							
						} catch (JSONException e) {
							Log.e(CommonUtilities.TAG, "Error result " +
									jsonStr + " >> " + e.getMessage());
						}
						
						adapter.updateList(listPrice, listTime, listPosition, listCompany);
					}
				});
				
			}
		}.start();
	}

	private OnItemClickListener listener = new OnItemClickListener() {
		public void onItemClick(android.widget.AdapterView<?> adapterView, View view, int position, long id) {
			Intent i = new Intent(getApplicationContext(), OngoingJobsLocationActivity.class);
			i.putExtra("location", listLocation.get(position));	
			i.putExtra("company", listCompany.get(position));
			i.putExtra("position", listPosition.get(position));
			startActivity(i);
			finish();
		};
	};		

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent result = new Intent(getApplicationContext(), HomeActivity.class);
			setResult(RESULT_CANCELED, result);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}