package com.matchimi.options;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.matchimi.R;
import com.matchimi.utils.JSONParser;

public class CreateAvailability extends SherlockActivity {

	private Context context;

	private ProgressDialog progress;
	private GoogleMap map;

	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private String id = null;
	private String start = null;
	private String end = null;
	private String location = null;
	private String price = null;
	private String avail_id = null;
	private int repeat = 0;

	private boolean update = false;
	private boolean locationLoaded = false;

	private String[] repeatString = null;

	public static final int RC_MAPS_ACTIVITY = 50;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_availability);

		context = this;

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		repeatString = context.getResources().getStringArray(
				R.array.repeat_value);

		TextView labelStart = (TextView) findViewById(R.id.labelStart);
		labelStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDateDialog(true);
			}
		});

		TextView labelEnd = (TextView) findViewById(R.id.labelEnd);
		labelEnd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (start != null) {
					showDateDialog(false);
				} else {
					Toast.makeText(context,
							"You should choose start time first !",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		TextView labelRepeat = (TextView) findViewById(R.id.labelRepeat);
		labelRepeat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Repeat");
				builder.setItems(R.array.repeat_value,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								repeat = arg1;
								reloadView();
							}
						});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});

		TextView labelSalary = (TextView) findViewById(R.id.labelSalary);
		labelSalary.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Ask Salary");
				builder.setMessage("How much salary you want to ask for this time of work ?");

				final EditText input = new EditText(context);
				input.setInputType(InputType.TYPE_CLASS_NUMBER);
				input.setHint("$");
				if (price != null) {
					input.setText(price.substring(0, price.indexOf(".")));
				}
				builder.setView(input);

				builder.setPositiveButton("Set",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								price = input.getText().toString().trim()
										+ ".0";
								reloadView();
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

		TextView labelLocation = (TextView) findViewById(R.id.labelLocation);
		labelLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(context, MapsActivity.class);
				i.putExtra("location", location);
				startActivityForResult(i, RC_MAPS_ACTIVITY);
			}
		});

		Bundle b = getIntent().getExtras();
		id = b.getString("id");
		update = b.getBoolean("update");
		if (update) {
			ab.setTitle("Edit Availability");
			start = b.getString("start");
			end = b.getString("end");
			repeat = b.getInt("repeat");
			location = b.getString("location");
			price = b.getString("price");
			avail_id = b.getString("avail_id");

			reloadView();
		} else {
			ab.setTitle("Create Availability");
		}

		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		if (location != null && !location.equalsIgnoreCase("null")
				&& location.length() > 1) {
			loadMap(location);
		} else {
			loadLocation();
		}
	}

	@SuppressLint("ValidFragment")
	public class EditNameDialog extends DialogFragment {

		public EditNameDialog() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.maps_layout, container);

			getDialog().setTitle("Location");

			return view;
		}
	}

	private void reloadView() {
		SimpleDateFormat formatterDate = new SimpleDateFormat("EE d MMM",
				Locale.getDefault());
		SimpleDateFormat formatterTime = new SimpleDateFormat("hh a",
				Locale.getDefault());

		TextView labelStart = (TextView) findViewById(R.id.labelStart);
		TextView labelEnd = (TextView) findViewById(R.id.labelEnd);
		TextView labelRepeat = (TextView) findViewById(R.id.labelRepeat);
		TextView labelSalary = (TextView) findViewById(R.id.labelSalary);

		if (start != null) {
			Calendar calStart = generateCalendar(start);
			labelStart.setText("Start at : "
					+ formatterDate.format(calStart.getTime())
					+ ", "
					+ formatterTime.format(calStart.getTime()).toLowerCase(
							Locale.getDefault()));
		}

		if (end != null) {
			Calendar calEnd = generateCalendar(end);
			labelEnd.setText("End at : "
					+ formatterDate.format(calEnd.getTime())
					+ ", "
					+ formatterTime.format(calEnd.getTime()).toLowerCase(
							Locale.getDefault()));
		}

		labelRepeat.setText("Repeat : " + repeatString[repeat]);

		if (price != null) {
			labelSalary.setText("Ask Salary : $" + price);
		}
	}

	private void loadLocation() {
		Log.e("XXXX", "loadLocation()");
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean isGPSEnabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// getting network status
		boolean isNetworkEnabled = locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		// Check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to
		// go to the settings
		if (!isGPSEnabled && !isNetworkEnabled) {
			Log.e("Main", "NO NETWORK PROVIDER IS ENABLED !");
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		} else {
			// if GPS Enabled get lat/long using GPS Services
			if (isGPSEnabled) {
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 6000, 10,
						locationListener);
			} else if (isNetworkEnabled) {
				// First get location from Network Provider
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 6000, 10,
						locationListener);
			}
		}
	}

	LocationListener locationListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.e("LocationListener", "onStatusChanged()");
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.e("LocationListener", "onProviderEnabled()");
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.e("LocationListener", "onProviderDisabled()");
		}

		@Override
		public void onLocationChanged(Location location) {
			Log.e("LocationListener", "onLocationChanged()");
			if (!locationLoaded) {
				loadMap(location.getLatitude() + "," + location.getLongitude());
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

			map.addMarker(new MarkerOptions().position(pos).title("Matchimi")
					.snippet("Your job area availability")
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
			// Move the camera instantly to hamburg with a zoom of 15.
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));
			// Zoom in, animating the camera.
			map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
		}
	}

	protected void showDateDialog(boolean withDate) {
		final SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault());
		final Calendar cal;
		if (withDate) {
			if (start != null) {
				cal = generateCalendar(start);
			} else {
				cal = new GregorianCalendar(Locale.getDefault());
			}
			DatePickerDialog dialogDate = new DatePickerDialog(
					context,
					new OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker arg0, int arg1,
								int arg2, int arg3) {
							cal.set(Calendar.YEAR, arg1);
							cal.set(Calendar.MONTH, arg2);
							cal.set(Calendar.DAY_OF_MONTH, arg3);
							TimePickerDialog dialogTime = new TimePickerDialog(
									context, new OnTimeSetListener() {
										@Override
										public void onTimeSet(TimePicker arg0,
												int arg1, int arg2) {
											cal.set(Calendar.HOUR_OF_DAY, arg1);
											cal.set(Calendar.MINUTE, arg2);
											start = sdf.format(cal.getTime());
											reloadView();
										}
									}, cal.get(Calendar.HOUR_OF_DAY), cal
											.get(Calendar.MINUTE), true);
							dialogTime.show();
						}
					}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DAY_OF_MONTH));
			dialogDate.show();
		} else {
			cal = generateCalendar(start);
			TimePickerDialog dialogTime = new TimePickerDialog(context,
					new OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker arg0, int arg1,
								int arg2) {
							cal.set(Calendar.HOUR_OF_DAY, arg1);
							cal.set(Calendar.MINUTE, arg2);
							end = sdf.format(cal.getTime());
							reloadView();
						}
					}, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
					true);
			dialogTime.show();
		}
	}

	private Calendar generateCalendar(String str) {
		Calendar calRes = new GregorianCalendar(Integer.parseInt(str.substring(
				0, 4)), Integer.parseInt(str.substring(5, 7)) - 1,
				Integer.parseInt(str.substring(8, 10)), Integer.parseInt(str
						.substring(11, 13)), Integer.parseInt(str.substring(14,
						16)), Integer.parseInt(str.substring(17, 19)));

		return calRes;
	}

	protected void doEditAvailability() {
		final String url = "http://matchimi.buuukapps.com/edit_availability";
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					// FIXME: please check on server response
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(context, "Edit availability Done.",
								Toast.LENGTH_SHORT).show();
						Intent result = new Intent();
						setResult(RESULT_OK, result);
						finish();
					} else {
						Toast.makeText(
								context,
								"Update availability is FAILED. Please try again !",
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(
							context,
							"Update availability is FAILED. Please try again !",
							Toast.LENGTH_LONG).show();
				}
			}
		};

		progress = ProgressDialog.show(context, "Availability",
				"Updating availability...", true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				try {
					JSONObject parentData = new JSONObject();
					JSONObject childData = new JSONObject();
					childData.put("pt_id", id);
					childData.put("asked_salary",
							price.substring(0, price.indexOf(".")));
					childData.put("start_date_time", start);
					childData.put("end_date_time", end);
					childData.put("repeat", repeat);
					parentData.put("availability", childData);

					String[] params = { "data" };
					String[] values = { parentData.toString() };
					jsonStr = jsonParser.getHttpResultUrlPut(url, params,
							values);

					Log.e("doEditAvailability", "Result >>> " + jsonStr);
					Log.e("doEditAvailability",
							"Post >>> " + childData.toString());
				} catch (Exception e) {
					jsonStr = null;
				}

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

	protected void doAddAvailability() {
		final String url = "http://matchimi.buuukapps.com/create_availability";
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					// FIXME: please check on server response
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(context, "Add availability Done.",
								Toast.LENGTH_SHORT).show();
						Intent result = new Intent();
						setResult(RESULT_OK, result);
						finish();
					} else {
						Toast.makeText(
								context,
								"Create availability is FAILED. Please try again !",
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(
							context,
							"Create availability is FAILED. Please try again !",
							Toast.LENGTH_LONG).show();
				}
			}
		};

		progress = ProgressDialog.show(context, "Availability",
				"Creating availability...", true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				try {
					JSONObject parentData = new JSONObject();
					JSONObject childData = new JSONObject();
					childData.put("pt_id", id);
					childData.put("asked_salary",
							price.substring(0, price.indexOf(".")));
					childData.put("start_date_time", start);
					childData.put("end_date_time", end);
					childData.put("repeat", repeat);
					parentData.put("availability", childData);

					String[] params = { "data" };
					String[] values = { parentData.toString() };
					jsonStr = jsonParser.getHttpResultUrlPost(url, params,
							values);

					Log.e("doAddAvailability", "Result >>> " + jsonStr);
					Log.e("doAddAvailability",
							"Post >>> " + childData.toString());
				} catch (Exception e) {
					jsonStr = null;
				}

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.ab_edit_availability, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent result = new Intent();
			setResult(RESULT_CANCELED, result);
			finish();
			break;
		case R.id.menu_submit:
			if (update) {
				doEditAvailability();
			} else {
				doAddAvailability();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == RC_MAPS_ACTIVITY) {
				location = data.getExtras().getString("location");
				loadMap(location);
			}
		}
	}
}