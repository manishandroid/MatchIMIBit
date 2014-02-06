package com.matchimi.ongoingjobs;

import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SETTING_THEME;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.THEME_LIGHT;
import static com.matchimi.CommonUtilities.USER_PTID;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonParser;
import com.matchimi.CommonUtilities;
import com.matchimi.HomeActivity;
import com.matchimi.R;
import com.matchimi.options.JobDetails;
import com.matchimi.options.MessageModel;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;
import com.matchimi.utils.ProcessDataUtils;
import com.matchimi.utils.TextProgressBar;

public class OngoingJobsLocationActivity extends SherlockFragmentActivity {
	private GoogleMap googleMap = null;
	
	private int selectedList = -1;
	private boolean modeList = true;
	private boolean newestMode = true;

	public static final int RC_REJECT = 22;
	public static final int RC_ACCEPT = 23;
	
	public static final String EXTRA_TITLE = "title";
	public static final int RC_JOB_DETAIL = 10;
	final int SWITCHER = 33;

	private List<String> listJobFunction = null;
	private List<String> listPriceRaw = null;

	private List<String> listAddress = null;
	private List<String> listAvailID = null;
	private List<String> listSubSlotID = null;
	private List<String> listPrice = null;
	private List<String> listCompany = null;
	private List<String> listCompanyFormat = null;
	private List<String> listSchedule = null;
	private List<String> listTimeLeft = null;
	private List<String> listTime = null;
	private List<Integer> listProgressBar = null;
	private List<Boolean> listColorStatus = null;
	private List<String> listDescription = null;
	private List<List<String>> listRequirement = null;
	private List<List<String>> listOptional = null;
	private List<String> listLocation = null;
	private List<MessageModel> listMessage = null;
	private List<Float> listRating = null;

	private List<List<String>> listScheduleFriendsFacebookID = null;
	private List<List<String>> listScheduleFriendsFirstName = null;
	private List<List<String>> listScheduleFriendsLastName = null;
	private List<List<String>> listScheduleFriendsProfilePicture = null;
	private List<List<String>> listScheduleFriendsPtID = null;
	private List<List<Bitmap>> listFriend = null;

	private Bundle bundle;
	private JSONParser jsonParser = null;
	private JSONObject objs = null;
	private JSONArray jsonArray = null;
	private String jsonStr = null;

	private Context context;
	private String pt_id;
	private String userLatitude;
	private String userLongitude;
	
	private Marker marker;
	private Hashtable<String, Integer> markers;

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

		pt_id = settings.getString(USER_PTID, null);
		setContentView(R.layout.ongoing_job_map);

		context = this;

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setTitle(getString(R.string.txt_ongoing_job));

		bundle = getIntent().getExtras();
		
		jsonStr = bundle.getString("jsonStr");
		userLatitude = bundle.getString("latitude");
		userLongitude = bundle.getString("longitude");
		
		markers = new Hashtable<String, Integer>();

		try {
			if (googleMap == null) {

				googleMap = ((SupportMapFragment) getSupportFragmentManager()
						.findFragmentById(R.id.ongoing_map)).getMap();

				if (googleMap == null) {
					Toast.makeText(getApplicationContext(),
							"Sorry! unable to create maps", Toast.LENGTH_SHORT)
							.show();
				} else {
					// Setting a custom info window adapter for the google map
					googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
					googleMap.setOnInfoWindowClickListener(infoWindowListener);
					jsonParser = new JSONParser();
					parseData();
				}
			}

		} catch (Exception e) {
			Log.e(CommonUtilities.TAG, ">> can't load map");
		}
	}

	private OnInfoWindowClickListener infoWindowListener = new OnInfoWindowClickListener() {
		
		@Override
		public void onInfoWindowClick(Marker marker) {
			// TODO Auto-generated method stub

			Integer position = null;

			if (marker.getId() != null && markers != null && markers.size() > 0) {
				if (markers.get(marker.getId()) != null
						&& markers.get(marker.getId()) != null) {
					position = markers.get(marker.getId());
				}
			}
			
			Intent i = new Intent(getApplicationContext(), JobDetails.class);
			i.putExtra("job_function", listJobFunction.get(position));
			i.putExtra("price", listPrice.get(position));
			i.putExtra("priceFormat", listPriceRaw.get(position));
			i.putExtra("date", listSchedule.get(position));
			i.putExtra("company", listCompany.get(position));
			i.putExtra("place", listCompany.get(position) + "\n"
							+ listAddress.get(position));
			i.putExtra("company_format", listCompanyFormat.get(position));
			i.putExtra("expire", listTimeLeft.get(position));
			i.putExtra("description", listDescription.get(position));
			i.putExtra("mandatory_requirements", listRequirement.get(position)
					.toString());
			i.putExtra("optional_requirements", listOptional.get(position)
					.toString());
			i.putExtra("avail_id", listAvailID.get(position));
			i.putExtra("sub_slot_id", listSubSlotID.get(position));
			i.putExtra("type", "ongoing");
			i.putExtra("rating", listRating.get(position));
			i.putExtra("location", listLocation.get(position));
			i.putExtra("progressbar", listProgressBar.get(position));
			i.putExtra("colorstatus", listColorStatus.get(position));
			Log.d(CommonUtilities.TAG, "This is cliecked");

			i.putExtra(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_ID,
					listScheduleFriendsFacebookID.get(position).toString());
			i.putExtra(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_FIRST_NAME,
					listScheduleFriendsFirstName.get(position).toString());
			i.putExtra(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_LAST_NAME,
					listScheduleFriendsLastName.get(position).toString());
			i.putExtra(
					CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_PROFILE_PICTURE,
					listScheduleFriendsProfilePicture.get(position).toString());
			i.putExtra(CommonUtilities.PARAM_PT_ID, listScheduleFriendsPtID
					.get(position).toString());
			i.putExtra("type", "ongoing");

			startActivityForResult(i, RC_JOB_DETAIL);		
		}
	};

	private class CustomInfoWindowAdapter implements InfoWindowAdapter {

		private View view;

		public CustomInfoWindowAdapter() {
			view = getLayoutInflater().inflate(R.layout.customer_marker_layout, null);
		}

		@Override
		public View getInfoContents(Marker marker) {

			if (OngoingJobsLocationActivity.this.marker != null
					&& OngoingJobsLocationActivity.this.marker
							.isInfoWindowShown()) {
				OngoingJobsLocationActivity.this.marker.hideInfoWindow();
				OngoingJobsLocationActivity.this.marker.showInfoWindow();
			}
			
			return null;
		}

		@Override
		public View getInfoWindow(final Marker marker) {
			OngoingJobsLocationActivity.this.marker = marker;

			Integer i = null;

			if (marker.getId() != null && markers != null && markers.size() > 0) {
				if (markers.get(marker.getId()) != null
						&& markers.get(marker.getId()) != null) {
					i = markers.get(marker.getId());
				}
			}
			
			TextView priceJobView = (TextView) view.findViewById(R.id.mapJobPrice);
			priceJobView.setText(Html
							.fromHtml("<font color='#FFFFFF'>$</font><big><big><big><font color='#FFFFFF'>"
									+ listPriceRaw.get(i)+ "&nbsp;"
									+ "</font></big></big></big><br/><font color='#FFFFFF'>per hr</font>"));

			TextView joblocationView = (TextView) view.findViewById(R.id.mapJobLocation);
			joblocationView.setText(listCompanyFormat.get(i));

			TextView jobTask = (TextView) view.findViewById(R.id.mapJobTask);
			jobTask.setText(listJobFunction.get(i));

			
			return view;
		}
	}

	protected void loadMap() {
		if (listLocation != null && listLocation.size() > 0) {
			for (int i = 0; i < listLocation.size(); i++) {
				if(listLocation.get(i).toLowerCase().contains(",")) {
					String latitude = listLocation.get(i).substring(0,
							listLocation.get(i).indexOf(","));
					String longtitude = listLocation.get(i).substring(
							listLocation.get(i).indexOf(",") + 1);
					LatLng pos = new LatLng(Double.parseDouble(latitude),
							Double.parseDouble(longtitude));
					
					Marker markerJob = googleMap.addMarker(new MarkerOptions()
							.position(pos)
							.title(listCompany.get(i))
							.snippet(i + ". " + listDescription.get(i))
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.pin)));
					markers.put(markerJob.getId(), i);

					
				}
			}
		}

		Log.d(CommonUtilities.TAG, "Get user lat long " + userLatitude + " " + userLongitude);
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(userLatitude),
				Double.parseDouble(userLongitude)), 14.0f));
		
		// Zoom in, animating the camera.
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000,
				null);
		
		googleMap.setMyLocationEnabled(true);

	}

	private void parseData() {
		listJobFunction = new ArrayList<String>();
		listPriceRaw = new ArrayList<String>();

		listAvailID = new ArrayList<String>();
		listSubSlotID = new ArrayList<String>();
		listPrice = new ArrayList<String>();
		listAddress = new ArrayList<String>();
		listCompany = new ArrayList<String>();
		listCompanyFormat = new ArrayList<String>();
		listSchedule = new ArrayList<String>();
		listTimeLeft = new ArrayList<String>();
		listTime = new ArrayList<String>();
		listDescription = new ArrayList<String>();
		listLocation = new ArrayList<String>();
		listProgressBar = new ArrayList<Integer>();
		listColorStatus = new ArrayList<Boolean>();
		listRating = new ArrayList<Float>();

		listRequirement = new ArrayList<List<String>>();
		listOptional = new ArrayList<List<String>>();

		listScheduleFriendsFacebookID = new ArrayList<List<String>>();
		listScheduleFriendsFirstName = new ArrayList<List<String>>();
		listScheduleFriendsLastName = new ArrayList<List<String>>();
		listScheduleFriendsProfilePicture = new ArrayList<List<String>>();
		listScheduleFriendsPtID = new ArrayList<List<String>>();

		Calendar calToday = new GregorianCalendar();
		SimpleDateFormat formatterDate = new SimpleDateFormat("EE d, MMM",
				Locale.getDefault());
		SimpleDateFormat formatterTime = new SimpleDateFormat("hh:mm a",
				Locale.getDefault());

		try {
			jsonArray = new JSONArray(jsonStr);

			if (jsonArray.length() > 0) {
				for (int i = 0; i < jsonArray.length(); i++) {
					objs = jsonArray.getJSONObject(i);
					objs = objs.getJSONObject("sub_slots");

					listAvailID.add(jsonParser.getString(objs, "avail_id"));
					listSubSlotID .add(jsonParser.getString(objs, "sub_slot_id"));
					String price = ""
							+ jsonParser.getDouble(objs, "offered_salary");
					if (Integer
							.parseInt(price.substring(price.indexOf(".") + 1)) == 0) {
						price = price.substring(0, price.indexOf("."));
					}

					listPrice
							.add("<font color='#A4A9AF'>$</font><big><big><big><big><font color='#276289'>"
									+ price
									+ "</font></big></big></big></big><font color='#A4A9AF'>/hr</font>");
					listPriceRaw.add(price);
					listAddress.add(jsonParser.getString(objs, "address"));

					String companyName = jsonParser.getString(objs,
							"company_name");
					String branchName = jsonParser.getString(objs,
							"branch_name");

					listCompany.add(branchName + ", " + companyName);
					listCompanyFormat.add(branchName + "\n" + companyName);

					listDescription.add(jsonParser.getString(objs,
							"description"));

					listLocation.add(jsonParser.getString(objs, "location"));

					listRating.add(Float.parseFloat(jsonParser.getString(objs,
							"grade")));

					String startDate = jsonParser.getString(objs,
							"start_date_time");
					String endDate = jsonParser
							.getString(objs, "end_date_time");
					Calendar calStart = ProcessDataUtils
							.generateCalendar(startDate);
					Calendar calEnd = ProcessDataUtils
							.generateCalendar(endDate);

					listTime.add(formatterTime.format(calStart.getTime())
							.toLowerCase(Locale.getDefault())
							+ " - "
							+ formatterTime.format(calEnd.getTime())
									.toLowerCase(Locale.getDefault()));

					listSchedule.add(formatterDate.format(calStart.getTime())
							+ "\n"
							+ formatterTime.format(calStart.getTime())
									.toLowerCase(Locale.getDefault())
							+ " - "
							+ formatterTime.format(calEnd.getTime())
									.toLowerCase(Locale.getDefault()));

					String expiredAt = jsonParser.getString(objs, "expired_at");
					Calendar calExpiredAt = ProcessDataUtils
							.generateCalendar(expiredAt);

					int diffMnt = (int) ((calExpiredAt.getTimeInMillis() - calToday
							.getTimeInMillis()) / (1000 * 60));

					float progressPercentage = 0;

					String timeLeft = "";
					Integer timeLeftDay = (diffMnt / (60 * 24));

					if (timeLeftDay > 0) {
						timeLeft = (timeLeftDay) + "";

						if (timeLeftDay >= 2) {
							timeLeft += " days ";
							progressPercentage = 25;
						} else {
							timeLeft += " day ";
							progressPercentage = 50;
						}

						diffMnt = diffMnt % (60 * 24);
						Integer hourLeft = diffMnt / 60;

						if (hourLeft > 0) {
							if (hourLeft > 1) {
								timeLeft += (hourLeft) + " hours left";
							} else if (hourLeft == 1) {
								timeLeft += (hourLeft) + " hour left";
							} else {
								timeLeft += " left";
							}
						} else {
							timeLeft += " left";
						}

						listTimeLeft.add(timeLeft);

					} else {

						int hourLeft = (diffMnt / 60);

						if (hourLeft < 2 && hourLeft > 0) {
							timeLeft = String.valueOf(hourLeft) + " hour ";
						} else if (hourLeft >= 2) {
							timeLeft = String.valueOf(hourLeft) + " hours ";
						}

						float additionalTimes = 0.5f * ((24f - (float) hourLeft) / 24f);
						progressPercentage = (0.5f + additionalTimes) * 100;

						timeLeft += (diffMnt % 60) + " minutes left";
						listTimeLeft.add(timeLeft);
					}

					listProgressBar.add((int) progressPercentage);

					// Calculate colors
					Boolean redStatus = false;
					if (progressPercentage > 50) {
						redStatus = true;
					}

					listColorStatus.add(redStatus);
					listJobFunction.add(jsonParser.getString(objs,
							"job_function_name"));

					// Load requirement list
					 JSONArray listReqArray = new JSONArray(
							 jsonParser.getString(objs, "mandatory_requirements"));
					
					List<String> listRequirementItem = new ArrayList<String>();
					
					if (listReqArray != null && listReqArray.length() > 0) {
						for (int h = 0; h < listReqArray.length(); h++) {
							listRequirementItem.add(listReqArray.get(h)
									.toString());
						}
					}

					listRequirement.add(listRequirementItem);

					// Load requirement list
					JSONArray listOptionalArray = new JSONArray(jsonParser
							.getString(objs, "optional_requirements"));

					List<String> listOptionalItem = new ArrayList<String>();
					if (listOptionalArray != null
							&& listOptionalArray.length() > 0) {
						for (int m = 0; m < listOptionalArray.length(); m++) {
							listOptionalItem.add(listOptionalArray.get(m)
									.toString());
						}
					}

					listOptional.add(listOptionalItem);

					List<String> listScheduleFriendsFacebookIDItem = new ArrayList<String>();
					List<String> listScheduleFriendsFirstNameItem = new ArrayList<String>();
					List<String> listScheduleFriendsLastNameItem = new ArrayList<String>();
					List<String> listScheduleFriendsProfilePictureItem = new ArrayList<String>();
					List<String> listScheduleFriendsPtIDItem = new ArrayList<String>();

					// Load friend list
					JSONArray friends = new JSONArray(jsonParser.getString(objs, "friends"));
					if (friends != null && friends.length() > 0) {
						for (int k = 0; k < friends.length(); k++) {
							JSONObject friendsObjs = friends.getJSONObject(k);
							friendsObjs = friendsObjs
									.getJSONObject(CommonUtilities.JSON_KEY_PART_TIMER_FRIEND);

							listScheduleFriendsFacebookIDItem
									.add(jsonParser
											.getString(
													friendsObjs,
													CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_ID));

							listScheduleFriendsFirstNameItem
									.add(jsonParser
											.getString(
													friendsObjs,
													CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_FIRST_NAME));

							listScheduleFriendsLastNameItem
									.add(jsonParser
											.getString(
													friendsObjs,
													CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_LAST_NAME));

							listScheduleFriendsProfilePictureItem
									.add(jsonParser
											.getString(
													friendsObjs,
													CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_PROFILE_PICTURE));

							listScheduleFriendsPtIDItem.add(jsonParser
									.getString(friendsObjs,
											CommonUtilities.PARAM_PT_ID));
						}
					}

					listScheduleFriendsFacebookID
							.add(listScheduleFriendsFacebookIDItem);
					listScheduleFriendsFirstName
							.add(listScheduleFriendsFirstNameItem);
					listScheduleFriendsLastName
							.add(listScheduleFriendsLastNameItem);
					listScheduleFriendsProfilePicture
							.add(listScheduleFriendsProfilePictureItem);
					listScheduleFriendsPtID.add(listScheduleFriendsPtIDItem);
				}

			} else {
				Log.e(CommonUtilities.TAG, ">> Array parse json is null ");
			}
		} catch (JSONException e) {
			Log.e(CommonUtilities.TAG,
					"Error result " + jsonStr + " >> " + e.getMessage());
		}

		loadMap();
	}

	// Convert a view to bitmap
	public static Bitmap createDrawableFromView(Context context, View view) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
		view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.FILL_PARENT));
		view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
		view.layout(0, 0, displayMetrics.widthPixels,
				displayMetrics.heightPixels);
		view.buildDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
				view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);

		return bitmap;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		case SWITCHER:
			Intent ongoing = new Intent(this, OngoingJobsActivity.class);
			ongoing.putExtra("jsonStr", jsonStr);
			startActivity(ongoing);
			finish();
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	private void redirectToList() {
		Intent ongoing = new Intent(this, OngoingJobsActivity.class);
		ongoing.putExtras(bundle);
		startActivity(ongoing);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SWITCHER, 0, "List").setIcon(R.drawable.ic_list)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Log.e(CommonUtilities.TAG, "OnGoingJobLists Fragment onActivityResult()");
		if (resultCode == FragmentActivity.RESULT_OK) {
			if (requestCode == RC_JOB_DETAIL) {
				redirectToList();
			}
		}
	}
}
