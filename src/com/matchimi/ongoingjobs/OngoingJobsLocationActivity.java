package com.matchimi.ongoingjobs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.internal.ev;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.options.JobDetails;

public class OngoingJobsLocationActivity extends SherlockFragmentActivity{

	private GoogleMap googleMap = null;
	private String location;
	private String company;
	private String job;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ongoing_job_map);
		
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setTitle(getString(R.string.txt_ongoing_job));
		
		Bundle b = getIntent().getExtras();
		location = b.getString("location");
		company = b.getString("company");
		job = b.getString("position");
		
		try {
			if (googleMap == null) {
				
				googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.ongoing_map)).getMap();
	            
	            if (googleMap == null) {
	                Toast.makeText(getApplicationContext(),
	                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
	                        .show();
	            }
	        }
			loadMap(location);
			
		} catch (Exception e) {
			Log.e(CommonUtilities.TAG, ">> can't load map");
		}
	}
	
	protected void loadMap(String location) {
		if (location != null && !location.equalsIgnoreCase("null")
				&& location.length() > 1) {
			String latitude = location.substring(0, location.indexOf(","));
			String longtitude = location.substring(location.indexOf(",") + 1);
			LatLng pos = new LatLng(Double.parseDouble(latitude),
					Double.parseDouble(longtitude));

			MarkerOptions marker = new MarkerOptions().position(pos)
					.title(company);
			googleMap.addMarker(marker);
			
			// Move the camera instantly to hamburg with a zoom of 15.
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));
			// Zoom in, animating the camera.
			googleMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
						
			OnMarkerClickListener markerListener = new OnMarkerClickListener() {
				
				@Override
				public boolean onMarkerClick(Marker marker) {
					startActivity(new Intent(OngoingJobsLocationActivity.this, JobDetails.class));
					finish();
					return true;
				}
			};
			
			googleMap.setOnMarkerClickListener(markerListener);
		}
	}
	


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent result = new Intent(this, OngoingJobsActivity.class);
			setResult(RESULT_CANCELED, result);
			startActivity(result);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
