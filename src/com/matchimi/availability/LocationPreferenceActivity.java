package com.matchimi.availability;

import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SETTING_THEME;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.THEME_LIGHT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.options.CreateAvailability;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class LocationPreferenceActivity extends SherlockFragmentActivity {
	private Context context;
	private String location;
	private Map<Integer, String> mapLocations = new HashMap<Integer, String>();
	private String responseString;
	private JSONParser jsonParser = null;
	private String jsonStr = null;
	private ProgressDialog progress;
	private List<LocationModel> listModels = new ArrayList<LocationModel>();
	private ListView listView;
	private LocationPreferenceRegionAdapter adapter;
	
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
		
		if (b != null) {
			location = b.getString("location");
			Log.d(TAG, "Location are " + location);
		}

		listView = (ListView) findViewById(R.id.regionList);
		final ImageView mapImageView = (ImageView) findViewById(R.id.singaporeMap);

		context = this;

		ViewTreeObserver vto = mapImageView.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				LayerDrawable ld = (LayerDrawable) mapImageView.getBackground();
				int height = mapImageView.getHeight() / 2;
				LayoutParams params = new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.setMargins(0, height, 0, 0);
				listView.setLayoutParams(params);
			}
		});

		Button buttonSet = (Button) findViewById(R.id.buttonSet);
		buttonSet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String selectionLocation = "";
				String selectionLocationID = "";

				for (LocationModel listModel : listModels) {
					if (listModel.isSelected() == true) {
						selectionLocation += listModel.getName() + ",";
						
						for (Map.Entry<Integer, String> entry : mapLocations.entrySet()) {
							if(entry.getValue().equals(listModel.getName())) {
								selectionLocationID += entry.getKey() + ",";
							}
						}
					}
				}

				Log.d(TAG, "Selection " + selectionLocation + " " + selectionLocationID);

				// Remove last comma if any
				if (selectionLocation.length() > 0) {
					selectionLocation = selectionLocation.substring(0,
							selectionLocation.length() - 1);
				}
				
				// Remove last comma if any
				if(selectionLocationID.length() > 0) {
					selectionLocationID = selectionLocationID.substring(0, 
							selectionLocationID.length()-1);
				}

				Intent i = new Intent(context, CreateAvailability.class);
				i.putExtra(CommonUtilities.CREATEAVAILABILITY_MAP_REGION,
						selectionLocation);
				i.putExtra(CommonUtilities.CREATEAVAILABILITY_MAP_REGION_ID,
						selectionLocationID);
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
		
		loadLocations();

	}

	private LocationModel get(String s) {
		return new LocationModel(s);
	}
	
	/**
	 * Loading locations
	 */
	private void loadLocations() {
		Log.d(TAG, "Loading locations ...");

		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_LOCATIONS;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				responseString = null;

				if (jsonStr != null) {
					try {
						JSONArray items = new JSONArray(jsonStr);

						if (items != null && items.length() > 0) {							
							for (int i = 0; i < items.length(); i++) {
								/* get all json items, and put it on list */
								try {
									JSONObject objs = items.getJSONObject(i);
									objs = objs.getJSONObject(CommonUtilities.JSON_KEY_LOCATIONS);
									
									if (objs != null) {
										String locationName = jsonParser.getString(objs,
												CommonUtilities.JSON_KEY_LOCATION_NAME);
										
										int locationID = Integer.parseInt(jsonParser.getString(objs,
												CommonUtilities.JSON_KEY_LOCATION_ID));
										mapLocations.put(locationID, locationName);
									}
								} catch (JSONException e) {
									Log.e("Parse Json Object",
											">> " + e.getMessage());
								}
							}
														
							for (Map.Entry<Integer, String> entry : mapLocations.entrySet()) {
								listModels.add(get(entry.getValue()));
							}

							// create an array of Strings, that will be put to our ListActivity
							adapter = new LocationPreferenceRegionAdapter(context, 
									listModels);
							listView.setAdapter(adapter);
							
							if (location != null) {
								List<String> locationItems = Arrays.asList(location.split("\\s*,\\s*"));

								for (String item : locationItems) {
									for (int i = 0; i < listModels.size(); i++) {
										Log.d(TAG, "This value " + listModels.get(i).getName()
												+ " is mached " + item);

										if (listModels.get(i).getName().equals(item)) {
											Log.d(TAG, "This value " + listModels.get(i).getName()
													+ " is mached");
											listModels.get(i).setSelected(true);
										}
									}
								}
							} else {
								for (int i = 0; i < listModels.size(); i++) {
									listModels.get(i).setSelected(true);
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
				getString(R.string.loading_location),
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
