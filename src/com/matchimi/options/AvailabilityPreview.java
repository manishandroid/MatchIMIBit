package com.matchimi.options;

import static com.matchimi.CommonUtilities.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;


public class AvailabilityPreview extends SherlockFragmentActivity {

	private Context context;

	private ProgressDialog progress;

	private JSONParser jsonParser = null;
	private String jsonStr = null;
	private TextView buttonFreeze;
	private String avail_id;
	private Boolean is_frozen = false;
	
	public static final int RC_PREV_AVAILABILITY_EDIT = 40;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		if (settings.getInt(SETTING_THEME, THEME_LIGHT) == THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}
		setContentView(R.layout.availability_preview);

		context = this;

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		Bundle b = getIntent().getExtras();
		final String pt_id = b.getString("pt_id");
		avail_id = b.getString("avail_id");
		final String start = b.getString("start");
		final String end = b.getString("end");
		final int repeat = b.getInt("repeat");
		final String location = b.getString("location");
		final String price = b.getString("price");
		is_frozen = b.getBoolean("is_frozen");

		SimpleDateFormat formatterDate = new SimpleDateFormat("EE d, MMM",
				Locale.getDefault());
		SimpleDateFormat formatterTime = new SimpleDateFormat("hh a",
				Locale.getDefault());

		Calendar calStart = generateCalendar(start);
		Calendar calEnd = generateCalendar(end);

		TextView textStart = (TextView) findViewById(R.id.textStart);
		textStart.setText(formatterDate.format(calStart.getTime())
				+ ", "
				+ formatterTime.format(calStart.getTime()).toLowerCase(
						Locale.getDefault()));
		TextView textEnd = (TextView) findViewById(R.id.textEnd);
		textEnd.setText(formatterTime.format(calEnd.getTime()).toLowerCase(
				Locale.getDefault()));

		String[] repeatString = context.getResources().getStringArray(
				R.array.repeat_value);
		TextView textRepeat = (TextView) findViewById(R.id.textRepeat);
		textRepeat.setText(repeatString[repeat]);

		final TextView buttonEdit = (TextView) findViewById(R.id.buttonEdit);
		buttonEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(context, CreateAvailability.class);
				i.putExtra("pt_id", pt_id);
				i.putExtra("avail_id", avail_id);
				i.putExtra("start", start);
				i.putExtra("end", end);
				i.putExtra("repeat", repeat);
				i.putExtra("location", location);
				i.putExtra("price", price);
				i.putExtra("update", true);
				startActivityForResult(i, RC_PREV_AVAILABILITY_EDIT);
			}
		});

		final TextView buttonDelete = (TextView) findViewById(R.id.buttonDelete);
		buttonDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(getString(R.string.menu_availability));
				builder.setMessage(getString(R.string.delete_availability_question));

				builder.setPositiveButton("Delete",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								doDeleteAvailability(avail_id);
							}
						});

				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
							}
						});

				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});

		
		buttonFreeze = (TextView) findViewById(R.id.buttonFreeze);
		// If frozen, change into unfreeze
		if(is_frozen) {
			buttonFreeze.setText(getString(R.string.unfreeze_availability));
		}		
		buttonFreeze.setOnClickListener(freezeListener);
		buttonFreeze.setVisibility(View.GONE);
		
		loadMap(location);
	}

	private OnClickListener freezeListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			// If status is_frozen true, then unfreze them
			if(is_frozen) {
				doUnfreezeAvailability(avail_id);	
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(getString(R.string.menu_availability));
				builder.setMessage(getString(R.string.freeze_availability_question));
				builder.setPositiveButton(getString(R.string.freeze_title),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								doFreezeAvailability(avail_id);									
							}
						});

				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								// Nothing to do
							}
						});

				AlertDialog dialog = builder.create();
				dialog.show();	
			}
		}
	};
	
	protected void loadMap(String location) {
		if (location != null && !location.equalsIgnoreCase("null")
				&& location.length() > 1) {
			String latitude = location.substring(0, location.indexOf(","));
			String longtitude = location.substring(location.indexOf(",") + 1);
			LatLng pos = new LatLng(Double.parseDouble(latitude),
					Double.parseDouble(longtitude));

			GoogleMap map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			map.addMarker(new MarkerOptions().position(pos).title(getString(R.string.app_name))
					.snippet("Your job area availability")
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
			// Move the camera instantly to hamburg with a zoom of 15.
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));
			// Zoom in, animating the camera.
			map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
		}
	}

	protected void doDeleteAvailability(final String avail_id) {
		final String url = SERVERURL + API_DELETE_AVAILABILITY_BY_AVAIL_ID + "?" +
							PARAM_AVAIL_ID + "=" + avail_id;
		
		RequestQueue queue = Volley.newRequestQueue(this); 
		StringRequest dr = new StringRequest(Request.Method.DELETE, url, 
		    new Response.Listener<String>() 
		    {
		        @Override
		        public void onResponse(String response) {
		        	if (response.equalsIgnoreCase("0")) {
						Toast.makeText(context, getString(R.string.delete_availability_success),
								Toast.LENGTH_SHORT).show();
						Intent result = new Intent();
						setResult(RESULT_OK, result);
						finish();
					} else {
						Toast.makeText(
								context,
								getString(R.string.delete_availability_failed),
								Toast.LENGTH_LONG).show();
						Log.d(TAG, "Delete failed with result code " + response);
					}
		        }
		    }, 
		    new Response.ErrorListener() 
		    {
				@Override
				public void onErrorResponse(VolleyError error) {
					Log.d(TAG, "error " + error.toString());
					// TODO Auto-generated method stub
		        	 Toast.makeText(
								context,
								getString(R.string.something_wrong),
								Toast.LENGTH_LONG).show();
				}
		    }
		);
		queue.add(dr);
//		
//		final Handler mHandlerFeed = new Handler();
//		final Runnable mUpdateResultsFeed = new Runnable() {
//			public void run() {
//				if (jsonStr != null) {
//					if (jsonStr.trim().equalsIgnoreCase("0")) {
//						Toast.makeText(context, getString(R.string.delete_availability_success),
//								Toast.LENGTH_SHORT).show();
//						Intent result = new Intent();
//						setResult(RESULT_OK, result);
//						finish();
//					} else {
//						Toast.makeText(
//								context,
//								getString(R.string.delete_availability_failed),
//								Toast.LENGTH_LONG).show();
//					}
//				} else {
//					Toast.makeText(
//							context,
//							getString(R.string.something_wrong),
//							Toast.LENGTH_LONG).show();
//				}
//			}
//		};
//
//		progress = ProgressDialog.show(context, getString(R.string.menu_availability),
//				getString(R.string.delete_availability_progress), true, false);
//		new Thread() {
//			public void run() {
//				jsonParser = new JSONParser();
//				try {
//					String[] params = { "avail_id" };
//					String[] values = { avail_id };
//					jsonStr = jsonParser.getHttpResultUrlDelete(url, params,
//							values);
//					Log.e(TAG, "Deleting availability: " + url + " with avail_id " + avail_id + ". Result >>>\n" + jsonStr);
//				} catch (Exception e) {
//					jsonStr = null;
//				}
//
//				if (progress != null && progress.isShowing()) {
//					progress.dismiss();
//					mHandlerFeed.post(mUpdateResultsFeed);
//				}
//			}
//		}.start();
	}

	/**
	 * Freeze user availability
	 * @param avail_id
	 */
	protected void doFreezeAvailability(final String avail_id) {
		final String url = SERVERURL + API_FREEZE_AVAILABILITY_BY_AVAIL_ID;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(context, getString(R.string.freeze_availability_success),
								Toast.LENGTH_SHORT).show();
						Intent result = new Intent();
						setResult(RESULT_OK, result);
						finish();
					} else {
						Toast.makeText(
								context,
								getString(R.string.freeze_availability_failed),
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(
							context,
							getString(R.string.something_wrong),
							Toast.LENGTH_LONG).show();
				}
			}
		};

		progress = ProgressDialog.show(context, getString(R.string.menu_availability),
				getString(R.string.freeze_availability_progress), true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				String[] params = { "avail_id" };
				String[] values = { avail_id };
				jsonStr = jsonParser.getHttpResultUrlPut(url, params, values);

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}				
			}
		}.start();
	}
	
	/**
	 * Unfreeze availability
	 * @param avail_id
	 */
	protected void doUnfreezeAvailability(final String avail_id) {
		final String url = SERVERURL + API_UNFREEZE_AVAILABILITY_BY_AVAIL_ID;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(context, getString(R.string.unfreeze_availability_success),
								Toast.LENGTH_SHORT).show();
						Intent result = new Intent();
						setResult(RESULT_OK, result);
						finish();
					} else {
						Toast.makeText(
								context,
								getString(R.string.unfreeze_availability_failed),
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(
							context,
							getString(R.string.something_wrong),
							Toast.LENGTH_LONG).show();
				}
			}
		};

		progress = ProgressDialog.show(context, getString(R.string.menu_availability),
				getString(R.string.unfreeze_availability_progress), true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				String[] params = { "avail_id" };
				String[] values = { avail_id };
				jsonStr = jsonParser.getHttpResultUrlPut(url, params, values);

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}				
			}
		}.start();
	}
	
	private Calendar generateCalendar(String str) {
		Calendar calRes = new GregorianCalendar(Integer.parseInt(str.substring(
				0, 4)), Integer.parseInt(str.substring(5, 7)) - 1,
				Integer.parseInt(str.substring(8, 10)), Integer.parseInt(str
						.substring(11, 13)), Integer.parseInt(str.substring(14,
						16)), Integer.parseInt(str.substring(17, 19)));

		return calRes;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == RC_PREV_AVAILABILITY_EDIT) {
				Intent result = new Intent();
				setResult(RESULT_OK, result);
				finish();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent result = new Intent();
			setResult(RESULT_CANCELED, result);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
