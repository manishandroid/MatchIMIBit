package com.matchimi.options;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import static com.matchimi.CommonUtilities.*;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;

public class MapsActivity extends SherlockFragmentActivity {

	private GoogleMap map;
	private boolean locationLoaded = false;
	private String location = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences settings = getSharedPreferences(
				PREFS_NAME, Context.MODE_PRIVATE);
		if (settings.getInt(SETTING_THEME,
				THEME_LIGHT) == THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}
		setContentView(R.layout.maps_layout);

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

		Button buttonSet = (Button) findViewById(R.id.buttonSet);
		buttonSet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent();
				i.putExtra("location", location);
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

		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();
		location = getIntent().getExtras().getString("location");
		if (location != null && !location.equalsIgnoreCase("null")
				&& location.length() > 1) {
			loadMap(location);
		} else {
			loadLocation();
		}

		map.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng point) {
				location = point.latitude + "," + point.longitude;
				Log.d(TAG, "Location selected " + location);
				
				map.clear();
				map.addMarker(new MarkerOptions()
						.position(point)
						.title("Matchimi")
						.snippet("Your job area availability")
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.pin)));
			}
		});
	}

	private void loadLocation() {
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

	protected void loadMap(String location) {
		if (location != null && !location.equalsIgnoreCase("null")
				&& location.length() > 1) {
			String latitude = location.substring(0, location.indexOf(","));
			String longtitude = location.substring(location.indexOf(",") + 1);
			LatLng pos = new LatLng(Double.parseDouble(latitude),
					Double.parseDouble(longtitude));

			map.clear();
			map.addMarker(new MarkerOptions().position(pos).title("Matchimi")
					.snippet("Your job area availability")
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
			// Move the camera instantly to hamburg with a zoom of 15.
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));
			// Zoom in, animating the camera.
			map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
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
}
