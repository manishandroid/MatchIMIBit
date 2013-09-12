package com.matchimi.options;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import static com.matchimi.CommonUtilities.*;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;

public class JobDetails extends SherlockActivity {

	private Context context;

	private ProgressDialog progress;
	private GoogleMap map;

	private JSONParser jsonParser = null;

	private String jsonStr = null;
	private String jobID = null;

	public static final int RC_CANCEL = 21;
	public static final int RC_REJECT = 22;
	public static final int RC_ACCEPT = 23;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences authenticationPref = getSharedPreferences(
				APP_SETTING, Context.MODE_PRIVATE);
		if (authenticationPref.getInt(SETTING_THEME, THEME_LIGHT) == THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}
		setContentView(R.layout.job_detail);

		context = this;

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		Bundle b = getIntent().getExtras();
		jobID = b.getString("id");
		String price = b.getString("price");
		String date = b.getString("date");
		final String place = b.getString("place");
		String expire = b.getString("expire");
		String description = b.getString("description");
		String reqs = b.getString("requirement");
		if (reqs.contains("|")) {
			String[] tmp = reqs.split("\\|");
			reqs = "";
			for (int i = 0; i < tmp.length; i++) {
				if (i + 1 == tmp.length) {
					reqs += (i + 1) + tmp[i];
				} else {
					reqs += (i + 1) + tmp[i] + "\n";
				}
			}
		}
		final String requirement = reqs;
		final String optional = b.getString("optional");
		String jobType = b.getString("type");

		TextView textPrice = (TextView) findViewById(R.id.textPrice);
		textPrice.setText(Html.fromHtml(price));
		TextView textDate = (TextView) findViewById(R.id.textDate);
		textDate.setText(date);
		TextView textPlace = (TextView) findViewById(R.id.textPlace);
		textPlace.setText(place);
		TextView textExpire = (TextView) findViewById(R.id.textExpire);
		if (jobType.equalsIgnoreCase("offer")) {
			textExpire.setText(expire + " before this offer expires");
		} else if (jobType.equalsIgnoreCase("accepted")) {
			textExpire.setText("Job for " + expire);
		} else {
			textExpire.setText(expire);
		}
		TextView textDescription = (TextView) findViewById(R.id.textDescription);
		textDescription.setText(description);
		TextView textRequirement = (TextView) findViewById(R.id.textRequirement);
		textRequirement.setText(requirement);
		TextView textOptional = (TextView) findViewById(R.id.textOptional);
		if (optional == null || optional.length() == 0) {
			textOptional.setText("none");
		} else {
			textOptional.setText(optional);
		}

		TextView buttonAccept = (TextView) findViewById(R.id.buttonAccept);
		buttonAccept.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(context, RequirementsDetail.class);
				i.putExtra("optional", optional);
				if (requirement != null
						&& !requirement.equalsIgnoreCase("null")
						&& requirement.trim().length() > 1) {
					i.putExtra("requirement", requirement.split("\n"));
				}
				startActivityForResult(i, RC_ACCEPT);
			}
		});

		TextView buttonReject = (TextView) findViewById(R.id.buttonReject);
		buttonReject.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(context, RejectPreview.class);
				i.putExtra("company", place.substring(0, place.indexOf("\n")));
				startActivityForResult(i, RC_REJECT);
			}
		});

		TextView buttonCancel = (TextView) findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(context, CancelPreview.class);
				startActivityForResult(i, RC_CANCEL);
			}
		});

		if (jobType.equalsIgnoreCase("offer")) {
			buttonAccept.setVisibility(View.VISIBLE);
			buttonReject.setVisibility(View.VISIBLE);
			buttonCancel.setVisibility(View.GONE);
		} else if (jobType.equalsIgnoreCase("accepted")) {
			buttonAccept.setVisibility(View.GONE);
			buttonReject.setVisibility(View.GONE);
			buttonCancel.setVisibility(View.VISIBLE);
		} else {
			buttonAccept.setVisibility(View.GONE);
			buttonReject.setVisibility(View.GONE);
			buttonCancel.setVisibility(View.GONE);
		}

		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		loadLocation();
	}

	private void loadLocation() {
		final String url = "http://matchimi.buuukapps.com/get_availability_by_avail_id?avail_id=";
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					try {
						JSONObject obj = new JSONObject(jsonStr);
						obj = obj.getJSONObject("availabilities");
						String location = jsonParser.getString(obj, "location");
						loadMap(location);
					} catch (Exception e) {
						Log.e("loadLocation", ">>> " + e.getMessage());
					}
				} else {
					Toast.makeText(context, "Failed to load location !",
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url + jobID);

				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}

	protected void loadMap(String location) {
		if (location != null && !location.equalsIgnoreCase("null")
				&& location.length() > 1) {
			String latitude = location.substring(0, location.indexOf(","));
			String longtitude = location.substring(location.indexOf(",") + 1);
			LatLng pos = new LatLng(Double.parseDouble(latitude),
					Double.parseDouble(longtitude));

			map.addMarker(new MarkerOptions().position(pos).title("Matchimi")
					.snippet("Job area")
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
			// Move the camera instantly to hamburg with a zoom of 15.
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));
			// Zoom in, animating the camera.
			map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
		}
	}

	protected void doCancelOffer(String reason) {
		Log.e("Cancel", ">>> Reason: " + reason);
		final String url = SERVERURL+ API_WITHDRAW_AVAILABILITY;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					Log.e("Result", ">>> " + jsonStr + ".");
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(context, "Job canceled succesfully.",
								Toast.LENGTH_SHORT).show();
						setResult(RESULT_OK);
						finish();
					} else {
						Toast.makeText(
								context,
								"Failed when rejecting offer, please try again.",
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(context, "jsonStr is Null",
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context, "Job", "Canceling job...",
				true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				String[] params = { "avail_id" };
				String[] values = { jobID };
				jsonStr = jsonParser.getHttpResultUrlPut(url, params, values);

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

	protected void doRejectOffer() {
		final String url = "http://matchimi.buuukapps.com/reject_job_offer";
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(context, "Rejecting offer Done.",
								Toast.LENGTH_SHORT).show();
						setResult(RESULT_OK);
						finish();
					} else {
						Toast.makeText(
								context,
								"Failed when rejecting offer, try again latter !",
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(context, "jsonStr is Null",
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context, "Job", "Rejecting job...",
				true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				String[] params = { "avail_id" };
				String[] values = { jobID };
				jsonStr = jsonParser.getHttpResultUrlPut(url, params, values);

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

	protected void doAcceptOffer() {
		final String url = "http://matchimi.buuukapps.com/accept_job_offer";
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(context, "Accepting offer Done.",
								Toast.LENGTH_SHORT).show();
						setResult(RESULT_OK);
						finish();
					} else {
						Toast.makeText(
								context,
								"Failed when accepting offer, try again latter !",
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(context, "jsonStr is Null",
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context, "Job", "Accepting job...",
				true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				String[] params = { "avail_id" };
				String[] values = { jobID };
				jsonStr = jsonParser.getHttpResultUrlPut(url, params, values);

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == RC_ACCEPT) {
				doAcceptOffer();
			}
			if (requestCode == RC_CANCEL) {
				doCancelOffer(data.getExtras().getString("reason"));
			}
			if (requestCode == RC_REJECT) {
				doRejectOffer();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
