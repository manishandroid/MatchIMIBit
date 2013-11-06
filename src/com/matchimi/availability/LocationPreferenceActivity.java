package com.matchimi.availability;

import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SETTING_THEME;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.THEME_LIGHT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.matchimi.R;
import com.matchimi.options.CreateAvailability;
import com.matchimi.utils.ApplicationUtils;

public class LocationPreferenceActivity extends SherlockFragmentActivity {
	private Context context;
	private String location;
	
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

		setContentView(R.layout.availability_location_preference);
		
		Bundle b = getIntent().getExtras();
		if(b != null) {
			location = b.getString("location");
		}
		
		final ListView listView = (ListView) findViewById(R.id.regionList);	
		final ImageView mapImageView = (ImageView) findViewById(R.id.singaporeMap);

		context = this;
		
		ViewTreeObserver vto = mapImageView.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
		    @Override
		    public void onGlobalLayout() {
		        LayerDrawable ld = (LayerDrawable) mapImageView.getBackground();
		        int height = mapImageView.getHeight()/2;
				LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.setMargins(0, height, 0, 0);
				listView.setLayoutParams(params);
		    }
		});
		
		final List<LocationModel> listModels = new ArrayList<LocationModel>();
		String[] regionAvailabilityArray  = getResources().getStringArray(R.array.region_availability);
		
		for(String region : regionAvailabilityArray) {
			listModels.add(get(region));			
		}
		
		if(location != null) {
			List<String> items = Arrays.asList(location.split("\\s*,\\s*"));

			for(String item : items) {
				for(int i = 0; i < listModels.size(); i++) {
					Log.d(TAG, "This value " + listModels.get(i).getName() + " is mached " + item);
					if(listModels.get(i).getName().equals(item)) {
						Log.d(TAG, "This value " + listModels.get(i).getName() + " is mached");
						listModels.get(i).setSelected(true);
					}
				}
			}
		}
		
		for(LocationModel region : listModels) {
			Log.d(TAG, "Set " + region.isSelected());
		}
		
		// create an array of Strings, that will be put to our ListActivity
		LocationPreferenceRegionAdapter adapter = new LocationPreferenceRegionAdapter(
				this, listModels);
		listView.setAdapter(adapter);

		Button buttonSet = (Button) findViewById(R.id.buttonSet);
		buttonSet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String selectionLocation = "";

				for(LocationModel listModel : listModels) {
					Log.d(TAG, "Selection " + listModel.isSelected() + " " + listModel.getName());
					
					if(listModel.isSelected() == true) {
						selectionLocation += listModel.getName() + ",";
					}
				}

				Log.d(TAG, "Selection " + selectionLocation);
				
				// Remove last comma if any
				if(selectionLocation.length() > 0) {
					selectionLocation = selectionLocation.substring(0, selectionLocation.length()-1);
				}
				
				Intent i = new Intent(context, CreateAvailability.class);
				i.putExtra("location", selectionLocation);
				setResult(RESULT_OK, i);
				finish();
			}
		});

		Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}

	private LocationModel get(String s) {
		return new LocationModel(s);
	}
}
