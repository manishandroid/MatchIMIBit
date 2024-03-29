package com.matchimi.availability;

import static com.matchimi.CommonUtilities.AVAILABILITY_SELECTED;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
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
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.options.CreateAvailability;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.MapUtils;
import com.matchimi.utils.NetworkUtils;
import com.matchimi.utils.ProcessDataUtils;

public class DailyAvailabilityPreview extends SherlockFragmentActivity {

	private Context context;

	private ProgressDialog progress;

	private JSONParser jsonParser = null;
	private String jsonStr = null;
	private TextView buttonFreeze;
	private String avail_id;
	private String ravail_id;
	private Boolean is_frozen = false;
	private String status;
	private String expired_at;
	
	private Map<String, String> mapLocations = new HashMap<String, String>();
	
	private Boolean is_repeat;

	public static final int RC_PREV_AVAILABILITY_EDIT = 40;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences settings = getSharedPreferences(
				CommonUtilities.PREFS_NAME, Context.MODE_PRIVATE);
		if (settings.getInt(CommonUtilities.SETTING_THEME,
				CommonUtilities.THEME_LIGHT) == CommonUtilities.THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}

		setContentView(R.layout.availability_daily_preview);
		context = this;

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		Bundle b = getIntent().getExtras();
		final String pt_id = b.getString("pt_id");
		avail_id = b.getString("avail_id");
		ravail_id = b.getString("ravail_id");
		if(ravail_id != null && ravail_id.equals("null")) {
			ravail_id = null;
		}
		
		final String start = b.getString("start");
		final String end = b.getString("end");
		final String repeat = b.getString("repeat");
		final String repeat_text = b.getString("repeat_text");
		final String location = b.getString("location");
		final String price = b.getString("price");
		final String location_array = b.getString("location_array");
		if(location_array != null && location_array.length() > 0) {
			mapLocations = MapUtils.stringToMap(location_array);
		}
		
		status = b.getString("status");
		expired_at = b.getString("expired_at");

		is_frozen = b.getBoolean("is_frozen");
		is_repeat = b.getBoolean("is_repeat");

		final Calendar calStart = generateCalendar(start);
		Calendar calEnd = generateCalendar(end);

		TextView dateStart = (TextView) findViewById(R.id.textDailyDateStart);
		dateStart.setText(
		// CommonUtilities.AVAILABILITY_DATE.format(calStart.getTime())
		// + ", " +
				CommonUtilities.AVAILABILITY_DATE_TEXT.format(calStart.getTime()));
		// .toLowerCase(
		// Locale.getDefault()));
		
		TextView dateEnd = (TextView) findViewById(R.id.textDailyDateEnd);
		// textEnd.setText(CommonUtilities.AVAILABILITY_TIME.format(calEnd.getTime()).toLowerCase(
		// Locale.getDefault()));
		dateEnd.setText(CommonUtilities.AVAILABILITY_DATE_TEXT.format(calEnd
				.getTime()));
		
		TextView timeStart = (TextView) findViewById(R.id.textDailyTimeStart);
		timeStart.setText(
				CommonUtilities.AVAILABILITY_TIME.format(calStart.getTime()));
		
		TextView timeEnd = (TextView) findViewById(R.id.textDailyTimeEnd);
		timeEnd.setText(CommonUtilities.AVAILABILITY_TIME.format(calEnd
				.getTime()));

		TextView textRepeat = (TextView) findViewById(R.id.textRepeat);
		textRepeat.setText(" "+repeat_text);

		if(ProcessDataUtils.sameDay(calStart, calEnd) && ravail_id == null) {
			RelativeLayout repeatLayout = (RelativeLayout) findViewById(R.id.layoutDailyRepeat);
			repeatLayout.setVisibility(View.GONE);
		}

		TextView locationView = (TextView) findViewById(R.id.labelLocation);

		String locationRegion = "";
		List<String> items = ProcessDataUtils.convertStringToListWithSpace(location);

		for (String item : items) {
			for (Map.Entry<String, String> entry : mapLocations.entrySet()) {
				if (item.length() > 0) {
					if(item.equals(entry.getKey())) {
						locationRegion += entry.getValue() + ", ";
					}
				}
			}
		}

		// Remove last comma if any
		if (locationRegion.length() > 0) {
			locationRegion = locationRegion.substring(0,
					locationRegion.length() - 2);
		}
		
		locationView.setText(getResources().getString(
				R.string.location_preference)
				+ ": " + locationRegion);

		final TextView buttonEdit = (TextView) findViewById(R.id.buttonEdit);
		buttonEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (status != null) {

					Calendar expiredAt = Calendar.getInstance();
					Date todayDate = new Date();

					if (expired_at != null && !expired_at.equals("null")) {
						expiredAt = generateCalendar(expired_at);
					}

					// If MA and expired greater than today
					if ((status.equals(CommonUtilities.AVAILABILITY_STATUS_MA)
							&& expired_at != null && todayDate
							.compareTo(expiredAt.getTime()) < 0)
							|| status
									.equals(CommonUtilities.AVAILABILITY_STATUS_PA)) {

						// Use the Builder class for convenient dialog
						// construction
						AlertDialog.Builder builder = new AlertDialog.Builder(
								context);
						builder.setMessage(R.string.job_offer_gone_warning)
								.setPositiveButton(R.string.ok,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												Intent i = new Intent(
														context,
														CreateAvailability.class);
												i.putExtra("pt_id", pt_id);
												i.putExtra("avail_id", avail_id);
												i.putExtra("start", start);
												i.putExtra("end", end);
												i.putExtra("repeat", repeat);
												i.putExtra("ravail_id", ravail_id);
												i.putExtra("location", ProcessDataUtils.convertListStringToComma(location));
												i.putExtra("repeat", ProcessDataUtils.convertListStringToComma(repeat));
												i.putExtra("repeat_text", ProcessDataUtils.convertListStringToComma(repeat_text));
												i.putExtra("is_repeat", is_repeat);
												i.putExtra("price", price);
												i.putExtra("update", true);
												i.putExtra("location_array", MapUtils.mapToString(mapLocations));
												i.putExtra(
														CommonUtilities.AVAILABILITY_SELECTED,
														CommonUtilities.AVAILABILTY_DATETIME
																.format(calStart
																		.getTime()));
												
												startActivityForResult(i,
														RC_PREV_AVAILABILITY_EDIT);
											}
										});
						// Create the AlertDialog object and return it
						Dialog dialog = builder.create();
						dialog.show();

					} else {
						Intent i = new Intent(context, CreateAvailability.class);
						i.putExtra("pt_id", pt_id);
						i.putExtra("avail_id", avail_id);
						i.putExtra("start", start);
						i.putExtra("end", end);
						i.putExtra("repeat",  ProcessDataUtils.convertListStringToComma(repeat));
						i.putExtra("repeat_text", repeat_text);
						i.putExtra("location",  ProcessDataUtils.convertListStringToComma(location));
						i.putExtra("price", price);
						i.putExtra("ravail_id", ravail_id);
						i.putExtra("location_array", MapUtils.mapToString(mapLocations));
						i.putExtra("update", true);
						i.putExtra(CommonUtilities.AVAILABILITY_SELECTED,
								CommonUtilities.AVAILABILTY_DATETIME.format(calStart
										.getTime()));
						startActivityForResult(i, RC_PREV_AVAILABILITY_EDIT);
					}
				}

			}
		});

		final TextView buttonDelete = (TextView) findViewById(R.id.buttonDelete);
		buttonDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(R.string.menu_availability);
				builder.setMessage(R.string.delete_availability_question);

				builder.setPositiveButton(R.string.delete,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// We decide to use single availability for edit and edit 
								doDeleteAvailability(true);									

//								if(ravail_id == null || ravail_id.equals("null")) {
//									Log.d(CommonUtilities.TAG, "Avail ID " + avail_id + " ");
//
//									doDeleteAvailability(true);									
//								} else {
//									Log.d(CommonUtilities.TAG, "Ravail ID " + ravail_id + " ");
//									
//									doDeleteAvailability(false);									
//								}
							}
						});

				builder.setNegativeButton(R.string.cancel,
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
		if (is_frozen) {
			buttonFreeze.setText(getString(R.string.unfreeze_availability));
		}

		buttonFreeze.setOnClickListener(freezeListener);

		// loadMap(location);
	}

	private OnClickListener freezeListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			// If status is_frozen true, then unfreze them
			if (is_frozen) {
				doUnfreezeAvailability(avail_id);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(R.string.menu_availability);
				builder.setMessage(R.string.freeze_availability_question);
				builder.setPositiveButton(R.string.freeze_title,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								doFreezeAvailability(avail_id);
							}
						});

				builder.setNegativeButton(R.string.cancel,
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

	protected void doDeleteAvailability(final Boolean isSingleAvailability) {
		final String url;
		if(isSingleAvailability) {
			url = CommonUtilities.SERVERURL
				+ CommonUtilities.API_DELETE_AVAILABILITY_BY_AVAIL_ID + "?"
				+ CommonUtilities.PARAM_AVAIL_ID + "=" + avail_id;
		} else {
			url = CommonUtilities.SERVERURL
					+ CommonUtilities.API_DELETE_REPEATEAD_AVAILABILITY + "?"
					+ CommonUtilities.PARAM_RAVAIL_ID + "=" + ravail_id;
		}

		Log.d(CommonUtilities.TAG, "Delete " + url);
		
		RequestQueue queue = Volley.newRequestQueue(this);
		StringRequest dr = new StringRequest(Request.Method.DELETE, url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						if (response.equalsIgnoreCase("0")) {
							Toast.makeText(
									context,
									getString(R.string.delete_availability_success),
									Toast.LENGTH_SHORT).show();

							Intent i = new Intent(
									CommonUtilities.BROADCAST_SCHEDULE_RECEIVER);
							sendBroadcast(i);

							Intent result = new Intent();
							setResult(RESULT_OK, result);
							finish();

						} else if (response.equalsIgnoreCase("1")) {
							Toast.makeText(
									context,
									getString(R.string.delete_availability_failed),
									Toast.LENGTH_LONG).show();
							Log.d(CommonUtilities.TAG,
									"Delete failed with result code "
											+ response);
						} else {
							NetworkUtils.connectionHandler(context, response, "");
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.d(CommonUtilities.TAG, "Error " + error.toString());
						// TODO Auto-generated method stub
						Toast.makeText(context,
								getString(R.string.something_wrong),
								Toast.LENGTH_LONG).show();
					}
				});
		queue.add(dr);

	}

	/**
	 * Freeze user availability
	 * 
	 * @param avail_id
	 */
	protected void doFreezeAvailability(final String avail_id) {
		final String url = CommonUtilities.SERVERURL
				+ CommonUtilities.API_FREEZE_AVAILABILITY_BY_AVAIL_ID;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(
								context,
								getString(R.string.freeze_availability_success),
								Toast.LENGTH_SHORT).show();

						Intent i = new Intent(
								CommonUtilities.BROADCAST_SCHEDULE_RECEIVER);
						sendBroadcast(i);

						Intent result = new Intent();
						setResult(RESULT_OK, result);
						finish();
					} else if (jsonStr.trim().equalsIgnoreCase("1")) {
						Toast.makeText(context,
								getString(R.string.freeze_availability_failed),
								Toast.LENGTH_LONG).show();
					} else {
						NetworkUtils.connectionHandler(context, jsonStr, "");
					}
				} else {
					Toast.makeText(context,
							getString(R.string.something_wrong),
							Toast.LENGTH_LONG).show();
				}
			}
		};

		progress = ProgressDialog.show(context,
				getString(R.string.menu_availability),
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
	 * 
	 * @param avail_id
	 */
	protected void doUnfreezeAvailability(final String avail_id) {
		final String url = CommonUtilities.SERVERURL
				+ CommonUtilities.API_UNFREEZE_AVAILABILITY_BY_AVAIL_ID;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(
								context,
								getString(R.string.unfreeze_availability_success),
								Toast.LENGTH_SHORT).show();

						Intent i = new Intent(
								CommonUtilities.BROADCAST_SCHEDULE_RECEIVER);
						sendBroadcast(i);

						Intent result = new Intent();
						setResult(RESULT_OK, result);
						finish();
					} else if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(
								context,
								getString(R.string.unfreeze_availability_failed),
								Toast.LENGTH_LONG).show();
					} else {
						NetworkUtils.connectionHandler(context, jsonStr, "");
					}
				} else {
					Toast.makeText(context,
							getString(R.string.something_wrong),
							Toast.LENGTH_LONG).show();
				}
			}
		};

		progress = ProgressDialog
				.show(context, getString(R.string.menu_availability),
						getString(R.string.unfreeze_availability_progress),
						true, false);
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
			Intent result = new Intent(getApplicationContext(),
					HomeAvailabilityActivity.class);
			setResult(RESULT_CANCELED, result);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
