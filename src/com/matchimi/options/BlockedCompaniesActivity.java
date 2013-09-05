package com.matchimi.options;

import static com.matchimi.CommonUtilities.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import com.matchimi.R;
import com.matchimi.utils.JSONHelper;

public class BlockedCompaniesActivity extends Activity {
	private static RequestQueue volleyQueue;
	private List<Map> blockedCompanies = new ArrayList();
	private JSONObject companyData;
	private ListView listview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blocked_companies);
        
        listview = (ListView) findViewById(R.id.blocked_companies_listview);

		String[] values = new String[] { "Jumbo, Clark Quay",
				"Jumbo, Clark Quay", "Iguana, Clark Quay" };

		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < values.length; ++i) {
			list.add(values[i]);
		}
		
		volleyQueue = Volley.newRequestQueue(this);
		
		String URL = SERVERURL + API_GET_BLOCKED_COMPANIES_BY_PT_ID + "?" + PARAM_PT_ID +"=" + API_PT_ID;
		
		volleyQueue.add(new JsonArrayRequest(URL, new Listener<JSONArray>() {
		    @Override
		    public void onResponse(JSONArray response) {
		    	
		    	
		    	for(int i=0;i<response.length();i++){
		    		try {
						JSONObject json_data = response.getJSONObject(i);
						companyData = (JSONObject) json_data.get("companies");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    		
		    		try {
		    			Map<String, Object> dataCompany = JSONHelper.toMap(companyData);
		    			blockedCompanies.add(dataCompany);
		    			Log.d(TAG, "MOKO " + blockedCompanies.toString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
    			addAdapter(blockedCompanies);
		    	
		        //SUCCESS
		    }}, new ErrorListener() {

		    @Override
		    public void onErrorResponse(VolleyError error) {
		        //ERROR
		    }}));		
	}
	
	private void addAdapter(List<Map> blockedCompanies) {
		// TODO Auto-generated method stub
		final BlockedCompaniesAdapter adapter = new BlockedCompaniesAdapter(this, blockedCompanies);
		listview.setAdapter(adapter);
	}
	
	
}
