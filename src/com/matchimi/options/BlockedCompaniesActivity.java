package com.matchimi.options;

import static com.matchimi.CommonUtilities.API_GET_BLOCKED_COMPANIES_BY_PT_ID;
import static com.matchimi.CommonUtilities.PARAM_PT_ID;
import static com.matchimi.CommonUtilities.SERVERURL;
import static com.matchimi.CommonUtilities.TAG;

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
import android.util.Log;
import android.widget.ListView;

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
import com.matchimi.utils.JSONHelper;

public class BlockedCompaniesActivity extends SherlockActivity {
	
	private Context context;
	private String pt_id;
	
	private ProgressDialog progress;
	
	private static RequestQueue volleyQueue;
	private List<Map> blockedCompanies = new ArrayList();
	private JSONObject companyData;
	private ListView listview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blocked_companies);

		context = this;
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		SharedPreferences authenticationPref = getSharedPreferences(
				CommonUtilities.APP_SETTING, Context.MODE_PRIVATE);
		pt_id = authenticationPref.getString(CommonUtilities.USER_PTID, null);

		listview = (ListView) findViewById(R.id.blocked_companies_listview);

		String[] values = new String[] { "Jumbo, Clark Quay",
				"Jumbo, Clark Quay", "Iguana, Clark Quay" };

		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < values.length; ++i) {
			list.add(values[i]);
		}

		volleyQueue = Volley.newRequestQueue(this);

		progress = ProgressDialog.show(context, "Profile",
				"Getting blocked companies...", true, false);
		
		String URL = SERVERURL + API_GET_BLOCKED_COMPANIES_BY_PT_ID + "?"
				+ PARAM_PT_ID + "=" + pt_id;

		volleyQueue.add(new JsonArrayRequest(URL, new Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray response) {

				for (int i = 0; i < response.length(); i++) {
					try {
						JSONObject json_data = response.getJSONObject(i);
						companyData = (JSONObject) json_data.get("companies");
					} catch (JSONException e) {
						e.printStackTrace();
					}

					try {
						Map<String, Object> dataCompany = JSONHelper
								.toMap(companyData);
						blockedCompanies.add(dataCompany);
						Log.d(TAG, "MOKO " + blockedCompanies.toString());
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
				if (progress != null && progress.isShowing()) {
					progress.dismiss();
				}

				addAdapter(blockedCompanies);

				// SUCCESS
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (progress != null && progress.isShowing()) {
					progress.dismiss();
				}
			}
		}));
	}

	private void addAdapter(List<Map> blockedCompanies) {
		final BlockedCompaniesAdapter adapter = new BlockedCompaniesAdapter(
				this, blockedCompanies);
		listview.setAdapter(adapter);
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
