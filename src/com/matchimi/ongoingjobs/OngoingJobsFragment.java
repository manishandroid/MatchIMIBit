package com.matchimi.ongoingjobs;

import static com.matchimi.CommonUtilities.PREFS_NAME;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.SupportMapFragment;
import com.matchimi.CommonUtilities;
import com.matchimi.HomeActivity;
import com.matchimi.R;
import com.matchimi.options.JobAdapter;
import com.matchimi.options.JobDetails;
import com.matchimi.options.MessageModel;
import com.matchimi.utils.GPSTracker;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.ProcessDataUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class OngoingJobsFragment extends Fragment {
	private String pt_id = null;
	private View view;
	private List<String> listJobFunctionName = null;
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

	public static final String EXTRA_TITLE = "title";
	public static final int RC_JOB_DETAIL = 10;
	public static final int RC_SETTINGS = 20;
	final int SWITCHER = 33;

	private OngoingJobsAdapter adapter;
	private ListView listview;

	private String jsonStr = null;
	private JSONParser jsonParser = null;
	private JSONObject objs = null;
	private JSONArray jsonArray = null;
	private Context context;
	private ProgressBar progressBar;
	private int items = 0;
	private Bundle bundle;
	// GPSTracker class
	private GPSTracker location;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}

		try {
			view = inflater.inflate(R.layout.ongoing_job_listview, container,
					false);
		} catch (InflateException e) {
			/* map is already there, just return view as it is */
		}

		SharedPreferences settings = getActivity().getSharedPreferences(
				PREFS_NAME, 0);
		pt_id = settings.getString(CommonUtilities.USER_PTID, null);
		context = getActivity();

		adapter = new OngoingJobsAdapter(getActivity());
		listview = (ListView) view.findViewById(R.id.ongoing_job_listview);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(listener);

		progressBar = (ProgressBar) view.findViewById(R.id.ongoingProgress);
		bundle = getActivity().getIntent().getExtras();

		// create class object
		location = new GPSTracker(getActivity());

		// check if GPS enabled
		if (location.canGetLocation()) {
			loadData();
		} else {
			// can't get location
			// GPS or Network is not enabled
			// Ask user to enable GPS/network in settings
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

			// Setting Dialog Title
			alertDialog.setTitle("GPS is settings");

			// Setting Dialog Message
			alertDialog
					.setMessage("GPS is not enabled. Do you want to go to settings menu?");

			// On pressing Settings button
			alertDialog.setPositiveButton("Settings",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivityForResult(intent, RC_SETTINGS);
						}
					});

			// on pressing cancel button
			alertDialog.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});

			// Showing Alert Message
			alertDialog.show();
		}

//		if (bundle != null && bundle.getString("jsonStr").length() > 0) {
//			jsonStr = bundle.getString("jsonStr");
//			parseData();
//		}

		return view;
	}

	public static Bundle createBundle(String title) {
		Bundle bundle = new Bundle();
		bundle.putString(EXTRA_TITLE, title);
		return bundle;
	}

	/**
	 * Function to show settings alert dialog
	 * */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		// Setting Dialog Title
		alertDialog.setTitle(context.getString(R.string.location_service));

		// Setting Dialog Message
		alertDialog.setMessage(context
				.getString(R.string.location_service_description));

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						context.startActivity(intent);
					}
				});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		// Showing Alert Message
		alertDialog.show();
	}

	private void parseData() {
		listJobFunctionName = new ArrayList<String>();
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

		Log.d(CommonUtilities.TAG, "Parse data");

		try {
			jsonArray = new JSONArray(jsonStr);
			jsonParser = new JSONParser();

			if (jsonArray.length() > 0) {
				for (int i = 0; i < jsonArray.length(); i++) {
					objs = jsonArray.getJSONObject(i);
					objs = objs.getJSONObject("sub_slots");

					listSubSlotID
							.add(jsonParser.getString(objs, "sub_slot_id"));
					listAvailID.add(jsonParser.getString(objs, "avail_id"));
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
					listJobFunctionName.add(jsonParser.getString(objs,
							"job_function_name"));

					// Load requirement list
					JSONArray listReqArray = new JSONArray(
							jsonParser
									.getString(objs, "mandatory_requirements"));

					List<String> listRequirementItem = new ArrayList<String>();
					if (listReqArray != null && listReqArray.length() > 0) {
						for (int h = 0; h < listReqArray.length(); h++) {
							listRequirementItem.add(listReqArray.get(h)
									.toString());
						}
					}

					listRequirement.add(listRequirementItem);

					// Load requirement list
					JSONArray listOptionalArray = new JSONArray(
							jsonParser.getString(objs, "optional_requirements"));

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
					JSONArray friends = new JSONArray(jsonParser.getString(
							objs, "friends"));

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

		adapter.updateList(listPrice, listSchedule, listJobFunctionName,
				listCompany);

		progressBar.setVisibility(View.GONE);
		listview.setVisibility(View.VISIBLE);
		items = 1;
	}

	private void loadData() {
		listview.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		
		final String url = CommonUtilities.SERVERURL
				+ CommonUtilities.API_GET_AND_ONGOING_JOBS + "?"
				+ CommonUtilities.PARAM_PT_ID + "=" + pt_id + "&lat="
				+ location.getLatitude() + "&long=" + location.getLongitude();
		Log.d(CommonUtilities.TAG, "Loading data : " + url);

		final Handler handler = new Handler();

		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				parseData();
			}
		};

		items = 0;

		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpsResultUrlGet(context, url);
				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}

	private OnItemClickListener listener = new OnItemClickListener() {
		public void onItemClick(android.widget.AdapterView<?> adapterView,
				View view, int position, long id) {

			Intent i = new Intent(getActivity().getApplicationContext(),
					JobDetails.class);
			i.putExtra("job_function_name", listJobFunctionName.get(position));
			i.putExtra("price", listPrice.get(position));
			i.putExtra("priceFormat", listPriceRaw.get(position));
			i.putExtra("date", listSchedule.get(position));
			i.putExtra("company", listCompany.get(position));
			i.putExtra(
					"place",
					listCompany.get(position) + "\n"
							+ listAddress.get(position));
			i.putExtra("company_format", listCompanyFormat.get(position));

			i.putExtra("expire", listTimeLeft.get(position));
			i.putExtra("description", listDescription.get(position));

			i.putExtra("mandatory_requirements", listRequirement.get(position)
					.toString());
			i.putExtra("optional_requirements", listOptional.get(position)
					.toString());
			i.putExtra("avail_id", listAvailID.get(position));
			i.putExtra("type", "ongoing");
			i.putExtra("rating", listRating.get(position));
			i.putExtra("location", listLocation.get(position));
			i.putExtra("progressbar", listProgressBar.get(position));
			i.putExtra("colorstatus", listColorStatus.get(position));
			i.putExtra("sub_slot_id", listSubSlotID.get(position));
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

		};
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		loadData();
	}

}
