package com.matchimi;

import static com.matchimi.CommonUtilities.*;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.matchimi.utils.ApplicationUtils;

public class SettingsActivity extends Activity {
	private CheckBox freezeCheckbox;
	private Spinner themeSpinner;
	private int currentTheme;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		SharedPreferences settings = getSharedPreferences(CommonUtilities.PREFS_NAME,
				Context.MODE_PRIVATE);
		if (settings.getInt(CommonUtilities.SETTING_THEME,
				CommonUtilities.THEME_LIGHT) == CommonUtilities.THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}
		
		setContentView(R.layout.settings_menu);
//		freezeCheckbox = (CheckBox) findViewById(R.id.settings_freeze_checkbox);
//		freezeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//			   @Override
//			   public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
//				   if(isChecked) {
//					   
//				   } else {
//					   
//				   }
//			   }
//			});
//		
		themeSpinner = (Spinner) findViewById(R.id.settings_theme_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.theme_value, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		themeSpinner.setAdapter(adapter);
		
		currentTheme = settings.getInt(SETTING_THEME, THEME_LIGHT);
		themeSpinner.setSelection(currentTheme);
		
		themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int post, long id) {
					Log.d(TAG, "Settings selection post, id " + post + " " + id);
					
					if (post != currentTheme) {
						SharedPreferences settings = getSharedPreferences(PREFS_NAME, 
								Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = settings.edit();
						editor.putInt(CommonUtilities.SETTING_THEME, (int) id);
						editor.commit();
						
						// restart apps
						ApplicationUtils.restartApp(getApplicationContext());
						finish();
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}

	    }); // (optional)

	}
	
	
	
	private void toogleFreezeAvailability() { 
		RequestQueue queue = Volley.newRequestQueue(this);
		String url = "";

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {

			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub

			}
		});

		queue.add(jsObjRequest);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

}
