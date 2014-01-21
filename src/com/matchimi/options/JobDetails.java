package com.matchimi.options;

import static com.matchimi.CommonUtilities.API_CREATE_AND_AVAILABILITY;
import static com.matchimi.CommonUtilities.API_CREATE_REPEATED_AVAILABILITY;
import static com.matchimi.CommonUtilities.API_WITHDRAW_AVAILABILITY;
import static com.matchimi.CommonUtilities.PARAM_PT_ID;
import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SERVERURL;
import static com.matchimi.CommonUtilities.SETTING_THEME;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.THEME_LIGHT;
import static com.matchimi.CommonUtilities.USER_PROFILE_PICTURE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.matchimi.CommonUtilities;
import com.matchimi.HomeActivity;
import com.matchimi.R;
import com.matchimi.ValidationUtilities;
import com.matchimi.Variables;
import com.matchimi.registration.Utilities;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;
import com.matchimi.utils.ProcessDataUtils;
import com.matchimi.utils.TextProgressBar;

public class JobDetails extends SherlockFragmentActivity {

	private Context context;

	private ProgressDialog progress;
	private GoogleMap map;

	private JSONParser jsonParser = null;

	private String jsonStr = null;
	private String avail_id = null;
	private String reasons = null;
	
	public static final int RC_CANCEL = 21;
	public static final int RC_REJECT = 22;
	public static final int RC_ACCEPT = 23;
	
	private String isVerified = "false";
	private boolean isProfileComplete;
	private boolean isNotification = false;
	private String pt_id = "";

	private List<MessageModel> listMessage = null;
	private List<InboxModel> listInbox = null;
	private List<Float> listRating = null;
	private List<Bitmap> listFriend = null;
	private boolean newestMode = true;

	private SharedPreferences settings;
	private String optional = "";
	private String requirement = "";
	private String location;
	private String rawOptional = "";
	private String rawRequirement = "";
	private String place = "";
	private String price;		
	private String expire;
	private String description;		
	private String date;
	
	private String friendsFacebookId = null;
	private String friendsFacebookFirstName = null;
	private String friendsFacebookLastName = null;
	private String friendsFacebookProfilePicture = null;
	private String friendsFacebookPtID = null;
	
	private List<String> listScheduleFriendsFacebookID = new ArrayList<String>();
	private List<String> listScheduleFriendsFirstName = new ArrayList<String>();	
	private List<String> listScheduleFriendsLastName = new ArrayList<String>();	
	private List<String> listScheduleFriendsProfilePicture = new ArrayList<String>();
	private List<String> listScheduleFriendsPtID = new ArrayList<String>();
	
	private List<String> listRequirement = new ArrayList<String>();
	private List<String> listOptional = new ArrayList<String>();
	
	private int progressbar;
	private boolean colorstatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		
		if (settings.getInt(SETTING_THEME, THEME_LIGHT) == THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}
		
		isVerified = settings.getString(CommonUtilities.USER_IS_VERIFIED, "false");
		isProfileComplete = settings.getBoolean(CommonUtilities.USER_PROFILE_COMPLETE, false);
		
		pt_id = settings.getString(CommonUtilities.USER_PTID, "");
		setContentView(R.layout.job_detail);

		context = this;

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		
		Bundle b = getIntent().getExtras();
		
		// Check if request coming from GCM Notification or usual flow
		if(b.containsKey("is_notification")) {
			isNotification = true;
			
			// Reload job fragment
			Intent i = new Intent(CommonUtilities.BROADCAST_SCHEDULE_RECEIVER);
			sendBroadcast(i);
			
			showJobOfferFromNofiticationFlow(b);		
		} else {
			showJobOfferFromNormalFlow(b);
		}

	}
	
	private void showJobOfferFromNofiticationFlow(Bundle b) {
		String data = b.getString("data");
		parseData(data);
		
		if(friendsFacebookId != null) {
			listScheduleFriendsFacebookID = ProcessDataUtils.convertStringToList(friendsFacebookId);
		}
		
		if(friendsFacebookFirstName != null) {
			listScheduleFriendsFacebookID = ProcessDataUtils.convertStringToList(friendsFacebookFirstName);
		}
				
		if(friendsFacebookLastName != null) {
			listScheduleFriendsLastName = ProcessDataUtils.convertStringToList(friendsFacebookLastName);
		}
		
		if(friendsFacebookProfilePicture != null) {
			listScheduleFriendsProfilePicture = ProcessDataUtils.convertStringToList(friendsFacebookProfilePicture);
		}
		
		if(friendsFacebookPtID != null) {
			listScheduleFriendsPtID = ProcessDataUtils.convertStringToList(friendsFacebookPtID);
		}
		
		// Set value
		TextView textPrice = (TextView) findViewById(R.id.textPrice);
		textPrice.setText(Html.fromHtml(price));
		TextView textDate = (TextView) findViewById(R.id.textDate);
		textDate.setText(date);
		TextView textPlace = (TextView) findViewById(R.id.textPlace);
		textPlace.setText(place);
		TextProgressBar textProgressBar = (TextProgressBar) findViewById(R.id.progressBarWithText);
		
		// Check if Job Offer or My Schedule is coming here
		textProgressBar.setText(expire + " before this offer expires");			
		
		// The gesture threshold expressed in dip
		float GESTURE_THRESHOLD_DIP = 13.0f;

		// Convert the dips to pixels
		float scale = context.getResources().getDisplayMetrics().density;
		float mGestureThreshold = (int) (GESTURE_THRESHOLD_DIP * scale + 0.5f);		
	    textProgressBar.setProgress(progressbar);
	    textProgressBar.setTextSize(mGestureThreshold);
	    
	    if(colorstatus == true) {
	    	textProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.job_percentage_detail_red));
	    } else {
	    	textProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.job_percentage_detail_green));	    	
	    }
	    
		TextView textDescription = (TextView) findViewById(R.id.textDescription);
		textDescription.setText(description);
		
		// Set value for requirements
		TextView textRequirement = (TextView) findViewById(R.id.textRequirement);
		
		if(rawRequirement != null) {
			listRequirement = ProcessDataUtils.convertStringToListWithSpace(rawRequirement);
			
			String requirementText = "";
			
			if(listRequirement.size() > 1) {
				for(int i=0; i<listRequirement.size();i++) {
					requirementText += "" + listRequirement.get(i) + "\n";
				}				
			} else {
				requirementText += "" + listRequirement.get(0);				
			}
			
			textRequirement.setText(requirementText);
		}
		
		// Set value optional requirement
		TextView textOptional = (TextView) findViewById(R.id.textOptional);
		
		if(rawOptional != null) {
			listOptional = ProcessDataUtils.convertStringToListWithSpace(rawOptional);

			String optionalText = "";
			
			if(listRequirement.size() > 1) {
				for(int i=0; i<listOptional.size();i++) {
					optionalText += "" + listOptional.get(i) + "\n";
				}				
			} else {
				optionalText += "" + listOptional.get(0);				
			}
			
			textOptional.setText(optionalText);
		}

		TextView buttonAccept = (TextView) findViewById(R.id.buttonAccept);
		buttonAccept.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
//				if(isVerified == "false") {
//					ValidationUtilities.resendLinkDialog(JobDetails.this, pt_id);
//				} else if (isProfileComplete == false) {
				if (isProfileComplete == false) {
					profileNotCompleteDialog();
				} else {
					allowAcceptJob();				
				}
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
		
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();

		loadMap(location);
		
		RelativeLayout additionalLayout = (RelativeLayout)findViewById(R.id.layAdditional);
		LinearLayout friendsLayout = (LinearLayout)findViewById(R.id.friendsLayout);
		RelativeLayout inboxLayout = (RelativeLayout)findViewById(R.id.inboxLayout);
		
		loadFriends();
		
		buttonAccept.setVisibility(View.VISIBLE);
		buttonReject.setVisibility(View.VISIBLE);
		buttonCancel.setVisibility(View.GONE);
		additionalLayout.setVisibility(View.GONE);
		friendsLayout.setVisibility(View.VISIBLE);
		inboxLayout.setVisibility(View.GONE);
	}
	
	private void parseData(String data) {
		jsonParser = new JSONParser();
		
		try {
			JSONObject objs = new JSONObject(data);
			objs = objs.getJSONObject("sub_slots");
					
			if (objs != null) {
				avail_id = jsonParser.getString(objs, "avail_id");
				String getPrice = ""
						+ jsonParser.getDouble(objs,
								"offered_salary");
				if (Integer
						.parseInt(getPrice.substring(getPrice
								.indexOf(".") + 1)) == 0) {
					getPrice = getPrice.substring(0,
							getPrice.indexOf("."));
				}
								
				price = "<font color='#A4A9AF'>$</font><big><big><big><big><font color='#276289'>"
								+ getPrice
								+ "</font></big></big></big></big><font color='#A4A9AF'>/hr</font>";
				
				description = jsonParser.getString(objs, "description");
				location = jsonParser.getString(objs, "location");
				
				place = jsonParser.getString(objs, "company_name") + "\n" + jsonParser.getString(objs, "address");
				
				String startDate = jsonParser
						.getString(objs,
								"start_date_time");
				String endDate = jsonParser.getString(
						objs, "end_date_time");
				Calendar calStart = ProcessDataUtils.generateCalendar(startDate);
				Calendar calEnd = ProcessDataUtils.generateCalendar(endDate);
				
				String expiredAt = jsonParser.getString(objs,
								"expired_at");
				Calendar calExpiredAt = ProcessDataUtils.generateCalendar(expiredAt);
				
				Calendar calToday = Calendar.getInstance();
				
				int diffMnt = (int) ((calExpiredAt
						.getTimeInMillis() - calToday
						.getTimeInMillis()) / (1000 * 60));
				
				float progressPercentage =0;
				
				String timeLeft = "";										
				Integer timeLeftDay = (diffMnt / (60 * 24));
				
				if (timeLeftDay > 0) {					
					timeLeft = (timeLeftDay) + "";

					if(timeLeftDay >= 2) {
						timeLeft += " days ";
						progressPercentage = 25;
					} else {
						timeLeft += " day ";
						progressPercentage = 50;												
					}
					
					diffMnt = diffMnt % (60 * 24);											
					Integer hourLeft = diffMnt / 60;
					
					if(hourLeft > 0) {
						if(hourLeft > 1) {
							timeLeft += (hourLeft) + " hours left";
						} else if(hourLeft == 1) {
							timeLeft += (hourLeft) + " hour left";
						} else {
							timeLeft += " left";
						}
					} else {
						timeLeft += " left";
					}
					
					expire = timeLeft;

				} else{
					
					int hourLeft =  (diffMnt / 60);
					
					if(hourLeft < 2 && hourLeft > 0) {
						timeLeft = String.valueOf(hourLeft) + " hour ";
					} else if (hourLeft >= 2) {
						timeLeft = String.valueOf(hourLeft) + " hours ";												
					}
					
					float additionalTimes =  0.5f * ((24f - (float) hourLeft) / 24f);
					progressPercentage = (0.5f + additionalTimes) * 100;
					
					timeLeft += (diffMnt % 60) + " minutes left";
					expire = timeLeft;	
				}
				
//				Log.d(CommonUtilities.TAG, "Percentage " + progressPercentage);
				
				progressbar = (int) progressPercentage;
				
				// Calculate colors										
				Boolean redStatus = false;
				if(progressPercentage > 50) {
					redStatus = true;
				}
				
				colorstatus = redStatus;
				
				// Load requirement list
				JSONArray listReqArray = new JSONArray(jsonParser
						.getString(objs, "mandatory_requirements"));	
				
				List<String> listRequirementItem = new ArrayList<String>();
				if (listReqArray != null && listReqArray.length() > 0) {
					for (int h = 0; h < listReqArray.length(); h++) {
						listRequirementItem.add(listReqArray.get(h).toString());	
					}
				}		
				
				rawRequirement = listRequirementItem.toString();
				
				Log.d(CommonUtilities.TAG, "Raw req " + rawRequirement);
				
				// Load requirement list
				JSONArray listOptionalArray = new JSONArray(jsonParser
						.getString(objs, "optional_requirements"));	

				List<String> listOptionalItem = new ArrayList<String>();
				if (listOptionalArray != null && listOptionalArray.length() > 0) {
					for (int m = 0; m < listOptionalArray.length(); m++) {
						listOptionalItem.add(listOptionalArray.get(m).toString());	
					}
				}
				rawOptional = listOptionalItem.toString();
				
				// Load friend list
				JSONArray friends = new JSONArray(jsonParser.getString(objs, "friends"));										

				List<String> listScheduleFriendsFacebookIDItem = new ArrayList<String>();
				List<String> listScheduleFriendsFirstNameItem = new ArrayList<String>();
				List<String> listScheduleFriendsLastNameItem = new ArrayList<String>();
				List<String> listScheduleFriendsProfilePictureItem = new ArrayList<String>();
				List<String> listScheduleFriendsPtIDItem = new ArrayList<String>();										
				
				if (friends != null && friends.length() > 0) {
					for (int k = 0; k < friends.length(); k++) {
						JSONObject friendsObjs = friends.getJSONObject(k);
						friendsObjs = friendsObjs.getJSONObject(CommonUtilities.JSON_KEY_PART_TIMER_FRIEND);
																		
						listScheduleFriendsFacebookIDItem.add(jsonParser
								.getString(friendsObjs, CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_ID));

						listScheduleFriendsFirstNameItem.add(jsonParser
								.getString(friendsObjs, CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_FIRST_NAME));

						listScheduleFriendsLastNameItem.add(jsonParser
								.getString(friendsObjs, CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_LAST_NAME));

						listScheduleFriendsProfilePictureItem.add(jsonParser
								.getString(friendsObjs, CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_PROFILE_PICTURE));

						listScheduleFriendsPtIDItem.add(jsonParser
								.getString(friendsObjs, CommonUtilities.PARAM_PT_ID));
					}
				}
				
				friendsFacebookId = listScheduleFriendsFacebookIDItem.toString();
				friendsFacebookFirstName = listScheduleFriendsFirstNameItem.toString();
				friendsFacebookLastName= listScheduleFriendsLastNameItem.toString();	
				friendsFacebookProfilePicture = listScheduleFriendsProfilePictureItem.toString();
				friendsFacebookPtID = listScheduleFriendsPtIDItem.toString();
			}
			
		} catch (JSONException e) {
			Log.e("Parse Json Object",
					">> " + e.getMessage());
		}
		
	}
	
	
	private void showJobOfferFromNormalFlow(Bundle b) {
		avail_id = b.getString("avail_id");
		price = b.getString("price");
		date = b.getString("date");
		place = b.getString("place");
		expire = b.getString("expire");
		description = b.getString("description");
		
		friendsFacebookId = b.getString(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_ID);
//		Log.d(CommonUtilities.TAG, "Received " + friendsFacebookId);
		
		if(friendsFacebookId != null) {
			listScheduleFriendsFacebookID = ProcessDataUtils.convertStringToList(friendsFacebookId);
		}
		
		friendsFacebookFirstName = b.getString(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_FIRST_NAME);
		if(friendsFacebookFirstName != null) {
			listScheduleFriendsFacebookID = ProcessDataUtils.convertStringToList(friendsFacebookFirstName);
		}
				
		friendsFacebookLastName = b.getString(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_LAST_NAME);
		if(friendsFacebookLastName != null) {
			listScheduleFriendsLastName = ProcessDataUtils.convertStringToList(friendsFacebookLastName);
		}
		
		friendsFacebookProfilePicture = b.getString(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_PROFILE_PICTURE);
		if(friendsFacebookProfilePicture != null) {
			listScheduleFriendsProfilePicture = ProcessDataUtils.convertStringToList(friendsFacebookProfilePicture);
		}
		
		friendsFacebookPtID =  b.getString(CommonUtilities.PARAM_PT_ID);
		if(friendsFacebookPtID != null) {
			listScheduleFriendsPtID = ProcessDataUtils.convertStringToList(friendsFacebookPtID);
		}
		
		location = b.getString("location");
		progressbar = b.getInt("progressbar");
		colorstatus = b.getBoolean("colorstatus");
		
		// Set value
		TextView textPrice = (TextView) findViewById(R.id.textPrice);
		textPrice.setText(Html.fromHtml(price));
		TextView textDate = (TextView) findViewById(R.id.textDate);
		textDate.setText(date);
		TextView textPlace = (TextView) findViewById(R.id.textPlace);
		textPlace.setText(place);
		TextProgressBar textProgressBar = (TextProgressBar) findViewById(R.id.progressBarWithText);
		
		// Check if Job Offer or My Schedule is coming here
		String jobType = b.getString("type");
		if (jobType.equalsIgnoreCase("offer")) {
			textProgressBar.setText(expire + " before this offer expires");			
		} else if (jobType.equalsIgnoreCase("accepted")) {
			textProgressBar.setText("Job for " + expire);
		} else {
			textProgressBar.setText(expire);
		}
		
		// The gesture threshold expressed in dip
		float GESTURE_THRESHOLD_DIP = 13.0f;

		// Convert the dips to pixels
		float scale = context.getResources().getDisplayMetrics().density;
		float mGestureThreshold = (int) (GESTURE_THRESHOLD_DIP * scale + 0.5f);		
	    textProgressBar.setProgress(progressbar);
	    textProgressBar.setTextSize(mGestureThreshold);
	    
	    if(colorstatus == true) {
	    	textProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.job_percentage_detail_red));
	    } else {
	    	textProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.job_percentage_detail_green));	    	
	    }
	    
		TextView textDescription = (TextView) findViewById(R.id.textDescription);
		textDescription.setText(description);
		
		// Set value for requirements
		TextView textRequirement = (TextView) findViewById(R.id.textRequirement);
		rawRequirement = b.getString("mandatory_requirements");

		if(rawRequirement != null) {
			listRequirement = ProcessDataUtils.convertStringToListWithSpace(rawRequirement);
			
			String requirementText = "";
			
			if(listRequirement.size() > 1) {
				for(int i=0; i<listRequirement.size();i++) {
					requirementText += "" + listRequirement.get(i) + "\n";
				}				
			} else {
				requirementText += "" + listRequirement.get(0);				
			}
			
			textRequirement.setText(requirementText);
		}
		
		// Set value optional requirement
		TextView textOptional = (TextView) findViewById(R.id.textOptional);
		rawOptional = b.getString("mandatory_optionals");
		
		if(rawOptional != null) {
			listOptional = ProcessDataUtils.convertStringToListWithSpace(rawOptional);

			String optionalText = "";
			
			if(listRequirement.size() > 1) {
				for(int i=0; i<listOptional.size();i++) {
					optionalText += "" + listOptional.get(i) + "\n";
				}				
			} else {
				optionalText += "" + listOptional.get(0);				
			}
			
			textOptional.setText(optionalText);
		}

		TextView buttonAccept = (TextView) findViewById(R.id.buttonAccept);
		buttonAccept.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
//				if(isVerified == "false") {
//					ValidationUtilities.resendLinkDialog(JobDetails.this, pt_id);
//				} else if (isProfileComplete == false) {
				if (isProfileComplete == false) {
					profileNotCompleteDialog();
				} else {
					allowAcceptJob();				
				}
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
		
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();

		loadMap(location);
		
//		if (getIntent().hasExtra("rating")) {
//			showRating(getIntent().getExtras().getFloat("rating"));
//		} else {
//			showRating(0f);
//		}
//		
//		loadMessage();
		
		
		RelativeLayout additionalLayout = (RelativeLayout)findViewById(R.id.layAdditional);
		LinearLayout friendsLayout = (LinearLayout)findViewById(R.id.friendsLayout);
		RelativeLayout inboxLayout = (RelativeLayout)findViewById(R.id.inboxLayout);
		
		if (jobType.equalsIgnoreCase("offer")) {
			loadFriends();
//			loadInbox();
			
			buttonAccept.setVisibility(View.VISIBLE);
			buttonReject.setVisibility(View.VISIBLE);
			buttonCancel.setVisibility(View.GONE);
			additionalLayout.setVisibility(View.GONE);
			friendsLayout.setVisibility(View.VISIBLE);
			inboxLayout.setVisibility(View.GONE);
			
		} else if (jobType.equalsIgnoreCase("accepted")) {
			loadInbox();
			
			buttonAccept.setVisibility(View.GONE);
			buttonReject.setVisibility(View.GONE);
			buttonCancel.setVisibility(View.VISIBLE);
			additionalLayout.setVisibility(View.GONE);
			friendsLayout.setVisibility(View.GONE);
			inboxLayout.setVisibility(View.VISIBLE);

		} else {
			buttonAccept.setVisibility(View.GONE);
			buttonReject.setVisibility(View.GONE);
			buttonCancel.setVisibility(View.GONE);
			additionalLayout.setVisibility(View.GONE);
			friendsLayout.setVisibility(View.GONE);
			inboxLayout.setVisibility(View.GONE);
		}
		
//		TextView textExpire = (TextView) findViewById(R.id.textExpire);
//		if (jobType.equalsIgnoreCase("offer")) {
//			textExpire.setText(expire + " before this offer expires");
//		} else if (jobType.equalsIgnoreCase("accepted")) {
//			textExpire.setText("Job for " + expire);
//		} else {
//			textExpire.setText(expire);
//		}
	}

	/**
	 * Loading messages from API
	 */
	private void loadInbox() {
		TextView labelFriends = (TextView) findViewById(R.id.labelInbox);
		labelFriends.setVisibility(View.GONE);
		
		// TODO Auto-generated method stub
		listInbox = new ArrayList<InboxModel>();
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					try {
						JSONArray items = new JSONArray(jsonStr);
						if (items != null && items.length() > 0) {
							for (int i = 0; i < items.length(); i++) {
								/* get all json items, and put it on list */
								try {
									JSONObject objs = items.getJSONObject(i);
									objs = objs.getJSONObject("sub_slot_avail_notifications");
									if (objs != null) {
										
										InboxModel msg = new InboxModel();
										msg.setAvailId(jsonParser.getString(objs, "avail_id"));
										msg.setBody(jsonParser.getString(objs, "body"));
										msg.setCreatedAt(jsonParser.getString(objs, "created_at"));
										msg.setIsFromPartTimer(Boolean.parseBoolean(jsonParser.getString(objs, "direction")));
										msg.setIsHasRead(Boolean.parseBoolean(jsonParser.getString(objs, "has_read")));
										msg.setReadAt(jsonParser.getString(objs, "read_at"));
										msg.setSentAt(jsonParser.getString(objs, "sent_at"));
										msg.setSubSlot(jsonParser.getString(objs, "sub_slot_id"));
										msg.setId(jsonParser.getString(objs, "san_id"));
										
										listInbox.add(msg);
									}
								} catch (Exception e) {
								}
							}
						}
					} catch (Exception e) {
						Log.e(CommonUtilities.TAG, ">>> " + e.getMessage());
					}
					
					showInbox();
				}				
			}
		};

		new Thread() {
			public void run() {
				String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_MESSAGE 
						+"?" + CommonUtilities.PARAM_AVAIL_ID + "=" + avail_id;

				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);
				Log.d(CommonUtilities.TAG, "Loading message from " + url + " with result : " + jsonStr);

				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}

	/**
	 * Showing message inbox
	 */
	private void showInbox() {
		LinearLayout layMessage = (LinearLayout) findViewById(R.id.listInboxMessages);
		layMessage.setVisibility(View.VISIBLE);
		layMessage.removeAllViews();
		
		LinearLayout layMessageContent = (LinearLayout)findViewById(R.id.inboxDetailLayout);
		layMessageContent.setVisibility(View.GONE);
		

		if (listInbox != null && listInbox.size() > 0) {
			TextView labelFriends = (TextView) findViewById(R.id.labelInbox);
			labelFriends.setVisibility(View.VISIBLE);
			
			for (int i = 0; i < listInbox.size(); i++) {
				LinearLayout message = new LinearLayout(context);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				message.setLayoutParams(params);
				message.setPadding(0, 5, 0, 0);
				message.setOrientation(LinearLayout.HORIZONTAL);
				
				final int selectedMessage = i;
				message.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						showInboxContent(selectedMessage);
					}
				});
				
				TextView textFrom = new TextView(context);
				params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
				params.weight = 1;
				textFrom.setLayoutParams(params);
				textFrom.setPadding(10, 20, 0, 20);
				
				TextView textTime = new TextView(context);
				params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
				params.weight = (float)0.5;
				textTime.setLayoutParams(params);
				textTime.setPadding(0, 20, 10, 10);
			
				textFrom.setText(listInbox.get(i).getBody());
				
				Calendar calSentAt = ProcessDataUtils.generateCalendar(listInbox.get(i).getSentAt());
				CharSequence sentAtText = DateUtils.getRelativeTimeSpanString(calSentAt.getTimeInMillis(), DateTime.now().getMillis(), DateUtils.SECOND_IN_MILLIS);
				textTime.setText(sentAtText);
				
				if(!listInbox.get(i).getIsFromPartTimer()) {
					if (listInbox.get(i).getIsHasRead()) {
						textFrom.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
						textTime.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
					} else {
						textFrom.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
						textTime.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
					}
				} else {
					textFrom.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
					textTime.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
				}
				
				// Inflate message into inbox layout
				message.addView(textFrom);
				message.addView(textTime);
				
				if(listInbox.get(i).getIsFromPartTimer()) {
					message.setBackgroundColor(getResources().getColor(R.color.light_dust));
				} else {
					message.setBackgroundColor(getResources().getColor(R.color.blue_sky));					
					textFrom.setTextColor(Color.WHITE);
					textTime.setTextColor(Color.WHITE);
				}
				
				layMessage.addView(message);

				// Adding separator line

				if(i < listInbox.size()-1) {
					View separator = new View(context);
					params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					params.height = 1;
					separator.setLayoutParams(params);
					separator.setBackgroundColor(Color.LTGRAY);
					layMessage.addView(separator);
				}
				
			}
		}
		
		// Showing messages
		LinearLayout listInbox = (LinearLayout) findViewById(R.id.listInboxLayout);
		listInbox.setVisibility(View.VISIBLE);

	}
	
	/**
	 * Showing details inbox message with close button
	 * @param selectedMessage
	 */
	protected void showInboxContent(final int selectedMessage) {
		LinearLayout layMessageContent = (LinearLayout) findViewById(R.id.inboxDetailLayout);
		layMessageContent.setVisibility(View.VISIBLE);
		
		TextView textMessageContent = (TextView) findViewById(R.id.inboxContentBody);
		
		String detailMessage = "from : ";
		if(listInbox.get(selectedMessage).getIsFromPartTimer()) {
			detailMessage += "<strong>You</strong><br/><br/>";
		} else {
			detailMessage += "<strong>"+ place.substring(0, place.indexOf("\n")) + "</strong><br/><br/>";
		}
		textMessageContent.setText(Html.fromHtml(detailMessage + listInbox.get(selectedMessage).getBody()));
		
		LinearLayout layMessage = (LinearLayout) findViewById(R.id.listInboxMessages);		
		layMessage.setVisibility(View.GONE);
		
		Button buttonClose = (Button)findViewById(R.id.inboxButtonClose);
		buttonClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showInbox();
			}
		});

		Button buttonReply = (Button)findViewById(R.id.inboxButtonReply);

		if(!listInbox.get(selectedMessage).getIsFromPartTimer()) {
			buttonReply.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					replyInbox("");
				}
			});			
			buttonReply.setVisibility(View.VISIBLE);
			markInboxAsRead(selectedMessage);

		} else {
			buttonReply.setVisibility(View.GONE);			
		}

	}
	
	/**
	 * Reply message inbox Dialog
	 */
	protected void replyInbox(String initialText) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(getResources().getString(R.string.message));
//		alert.setMessage(getResources().getString(R.string.reply_message));

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setText(initialText);
		alert.setView(input);

		alert.setPositiveButton(getResources().getString(R.string.reply), new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			  String message = input.getText().toString();
			  sendReplyInbox(message);
		  }
		});

		alert.show();
	}

	/**
	 * Send reply message to API server
	 */
	protected void sendReplyInbox(final String replyMessage) {
		final String url = SERVERURL + CommonUtilities.API_REPLY_MESSAGE;
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(
								context,
								getString(R.string.sending_message_success),
								Toast.LENGTH_SHORT).show();
						
					} else if (jsonStr.trim().equalsIgnoreCase("1")) {
						Toast.makeText(context,
								getString(R.string.sending_message_failed),
								Toast.LENGTH_SHORT).show();
						replyInbox(replyMessage);
						
					} else {
						NetworkUtils.connectionHandler(context, jsonStr, "");
						replyInbox(replyMessage);
					}
				} else {
					Toast.makeText(context, getString(R.string.server_error),
							Toast.LENGTH_LONG).show();
				}
			}
		};

		progress = ProgressDialog.show(context,
				getString(R.string.message),
				getString(R.string.sending_message), true, false);
		
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();

				try {
					JSONObject parentData = new JSONObject();
					JSONObject childData = new JSONObject();

					childData.put("body", replyMessage);
					childData.put("title", "");
					childData.put("avail_id", avail_id);
					parentData.put("message", childData);						
					
					String[] params = { "data" };

					String[] values = { parentData.toString() };
					jsonStr = jsonParser.getHttpResultUrlPost(url, params,
							values);
					Log.e(TAG, "Post data to " + url + " with data >>>\n"
							+ childData.toString());
					Log.e(TAG, "Reply message result >>> " + jsonStr);

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
	
	/**
	 * Mark message as read
	 * @param selectedMessage
	 */
	protected void markInboxAsRead(final int selectedMessage) {
		InboxModel msg = listInbox.get(selectedMessage);
		if(!msg.getIsHasRead()) {
			msg.setIsHasRead(true);
			listInbox.set(selectedMessage, msg);
			Log.d(CommonUtilities.TAG, "Set " + selectedMessage + "with read true");
			
			// TODO submit message as read
			final String url = CommonUtilities.SERVERURL
					+ CommonUtilities.API_SET_MESSAGE_READ;
			new Thread() {
				public void run() {
					jsonParser = new JSONParser();
					try {
						String[] params = { "avail_id" };
						String[] values = { avail_id };
						
						jsonStr = jsonParser.getHttpResultUrlPost(url, params, values);
						Log.d(CommonUtilities.TAG, "Mark message POST to " + url + " >>>\n" + jsonStr);
						
					} catch (Exception e) {
						jsonStr = null;
					}				
				}
			}.start();
		} else {
			// Do nothing
		}
	}
	
	protected void doCancelOffer(String inputReason) {
		Log.e(TAG, ">>> Reason: " + inputReason);
		reasons = inputReason;
		final String url = SERVERURL+ API_WITHDRAW_AVAILABILITY;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					Log.e(TAG, "Result >>> " + jsonStr);
					
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(context, getString(R.string.job_cancel_success),
								Toast.LENGTH_SHORT).show();
						setResult(RESULT_OK);
						finish();
					} else if(jsonStr.trim().equalsIgnoreCase("1")){
						Toast.makeText(
								context,
								getString(R.string.job_cancel_failed),
								Toast.LENGTH_LONG).show();
					} else {
						NetworkUtils.connectionHandler(context, jsonStr, "");
					}
				} else {
					Toast.makeText(context, getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context, getString(R.string.job_menu), 
				getString(R.string.job_cancel_progress),
				true, false);
		
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				String[] params = { "avail_id"};
				String[] values = { avail_id
						};
//				String[] params = { "avail_id", "reasons"};
//				String[] values = { avail_id, reasons };
				jsonStr = jsonParser.getHttpResultUrlPut(url, params, values);

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

	/**
	 * Reject offer action
	 */
	protected void doRejectOffer(final boolean isBlocked) {		
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_REJECT_JOB_OFFER;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Intent jobBroadcast = new Intent(CommonUtilities.BROADCAST_JOBS_RECEIVER);
						sendBroadcast(jobBroadcast);

						Toast.makeText(context, getString(R.string.job_reject_offer_success),
								Toast.LENGTH_SHORT).show();
						
						if(isNotification) {
							Intent setIntent = new Intent(context, HomeActivity.class);
							startActivity(setIntent);
							finish();
						} else {
							setResult(RESULT_OK);
							finish();
						}

					} else if(jsonStr.trim().equalsIgnoreCase("1")){
						Toast.makeText(
								context,
								getString(R.string.job_reject_offer_failed),
								Toast.LENGTH_LONG).show();
					} else {
						NetworkUtils.connectionHandler(context, jsonStr, "");
					}
				} else {
					Toast.makeText(context, getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context, getString(R.string.job_menu), 
				getString(R.string.job_reject_progress),
				true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				try {
					JSONObject parentData = new JSONObject();
					JSONObject childData = new JSONObject();				

					childData.put("avail_id", avail_id);
					if(isBlocked) {
						childData.put("blocked", 1);					
					} else {
						childData.put("blocked", 0);
					}
					parentData.put("job_offer", childData);
					String[] params = { "data" };
					String[] values = { parentData.toString() };
					
					jsonStr = jsonParser.getHttpResultUrlPut(url, params, values);
					Log.d(TAG, "Put into " + url + " with data " + parentData.toString());
					Log.d(TAG, "Result >>> " + jsonStr);
					
				} catch (JSONException e) {
					e.printStackTrace();
				}

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

	protected void doAcceptOffer() {
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_ACCEPT_JOB_OFFER;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					Log.d(CommonUtilities.TAG, "Result from " + url + " >>>\n" + jsonStr);
					
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						// Reload schedule
						Intent i = new Intent(CommonUtilities.BROADCAST_SCHEDULE_RECEIVER);
						sendBroadcast(i);
						
						Intent jobBroadcast = new Intent(CommonUtilities.BROADCAST_JOBS_RECEIVER);
						sendBroadcast(jobBroadcast);

						Toast.makeText(context, getString(R.string.job_accept_offer_success),
								Toast.LENGTH_SHORT).show();

						if(isNotification) {
							Intent setIntent = new Intent(context, HomeActivity.class);
							startActivity(setIntent);
							finish();
						} else {
							setResult(RESULT_OK);
							finish();
						}

					} else if(jsonStr.trim().equalsIgnoreCase("1")) {
						Toast.makeText(
								context,
								getString(R.string.job_accept_offer_failed),
								Toast.LENGTH_LONG).show();
					} else {
						NetworkUtils.connectionHandler(context, jsonStr, "");
					}
				} else {
					Toast.makeText(context, getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context, getString(R.string.job_menu),
				getString(R.string.job_accept_offer_progress),
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

	private void loadFriends() {
		// TODO Auto-generated method stub
		listFriend = new ArrayList<Bitmap>();
		
		if(listScheduleFriendsPtID != null && listScheduleFriendsPtID.size() > 0) {			
			for(int i = 0; i < listScheduleFriendsPtID.size(); i++) {
				String friendImage = new File(listScheduleFriendsProfilePicture.get(i)).getName();				
				String url = SERVERURL + CommonUtilities.API_GET_PROFILE_PIC + "?"
						+ PARAM_PT_ID + "=" + listScheduleFriendsPtID.get(i);

				String imagePath = checkAndDownloadPic(friendImage, url);				
				File f = new File(CommonUtilities.IMAGE_ROOT, imagePath);
				
				if(imagePath.length() > 0) {
					if (f.exists()) {
						Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());
						listFriend.add(b);
					}	
				}
			}
		}
		
		showFriends();
		
//		final Handler mHandlerFeed = new Handler();
//		final Runnable mUpdateResultsFeed = new Runnable() {
//			public void run() {
//				if (jsonStr != null) {
//					try {
//						JSONArray items = new JSONArray(jsonStr);
//						if (items != null && items.length() > 0) {
//							for (int i = 0; i < items.length(); i++) {
//								/* get all json items, and put it on list */
//								try {
//									JSONObject objs = items.getJSONObject(i);
//									objs = objs.getJSONObject("friend");
//									if (objs != null) {
//										byte[] imageAsBytes = Base64.decode(jsonParser.getString(
//												objs, "image").getBytes(), 0);
//										listFriend.add(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
//									}
//								} catch (Exception e) {
//								}
//							}
//						}
//					} catch (Exception e) {
//						Log.e(CommonUtilities.TAG, ">>> " + e.getMessage());
//					}
//				}
//				
//				showFriends();
//			}
//		};
//
//		new Thread() {
//			public void run() {
//				String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_FRIEND_BY_PT_ID 
//						+"?" + CommonUtilities.PARAM_PT_ID + "=" + pt_id;
//
//				jsonParser = new JSONParser();
//				jsonStr = jsonParser.getHttpResultUrlGet(url);
//
//				mHandlerFeed.post(mUpdateResultsFeed);
//			}
//		}.start();
	}
	
	/**
	 * Check and download pictures if not exists
	 * @param imageStoragePath
	 * @param apiURL
	 * @return
	 */
	private String checkAndDownloadPic(String imageStoragePath,
			String apiURL) {
		File f = new File(imageStoragePath);
		String filename = f.getName();
		Log.d(TAG, "Looking " + apiURL);
		
		File imageFile = new File(CommonUtilities.IMAGE_ROOT, filename);
		
		if (!imageFile.exists()) {
			String[] params = {apiURL, filename};
			new downloadWebPage().execute(params);
		} else {
			Log.d(TAG, "Image exists");
		}
		
		return filename;
	}

	private class downloadWebPage extends AsyncTask<String, Void, String> {

	    @Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d(TAG, "Finished download friends profile picture");
			File f = new File(CommonUtilities.IMAGE_ROOT, result);
			Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());
			listFriend.add(b);
			showFriends();
		}

		@Override
	    protected String doInBackground(String... params) {
	        try {
	            HttpClient httpClient = new DefaultHttpClient();
	            HttpGet httpGet = new HttpGet(params[0]);
	            HttpResponse response;
	            response = httpClient.execute(httpGet);
	            HttpEntity entity = response.getEntity();
	            InputStream is = entity.getContent();
	            				
				File f = new File(CommonUtilities.IMAGE_ROOT, params[1]);

				FileOutputStream output = new FileOutputStream(f);
	            byte data[] = new byte[1024];
	            int count;
	            while ((count = is.read(data)) > 0) {
	                output.write(data, 0, count);
	            }
	            
	            output.flush();
	            output.close();
	            is.close();
	            
	        } catch (Exception e) {
	            // TODO Auto-generated catch block
				Log.e(TAG, ">>> " + e.getMessage());
	            e.printStackTrace();
	        }

	        return params[1];
	    }
	}
		
	private void showFriends() {
		LinearLayout layFriend = (LinearLayout)findViewById(R.id.layFriend);
		layFriend.removeAllViews();

		if (listFriend == null || listFriend.size() == 0) {
			Log.d(CommonUtilities.TAG, "No selected friends ");
			TextView labelFriends = (TextView) findViewById(R.id.labelFriend);
			labelFriends.setVisibility(View.GONE);
		}
		
		if (listFriend != null && listFriend.size() > 0) {
			LinearLayout layRow = new LinearLayout(context);
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layRow.setLayoutParams(params);
			layRow.setGravity(Gravity.CENTER_HORIZONTAL);
			layRow.setOrientation(LinearLayout.HORIZONTAL);
			int rowNum = ApplicationUtils.getColumnNumber((Activity) context, 64);
			for (int i = 0; i < listFriend.size(); i++) {
				layRow.addView(createImageView(listFriend.get(i)));
				if ((i + 1) % rowNum == 0) {
					layFriend.addView(layRow);
					layRow = new LinearLayout(context);
					layRow.setLayoutParams(params);
					layRow.setGravity(Gravity.CENTER_HORIZONTAL);
					layRow.setOrientation(LinearLayout.HORIZONTAL);
				} else if (i == listFriend.size() - 1) {
					layFriend.addView(layRow);
				}
			}
		}
	}

	private ImageView createImageView(Bitmap b) {
		ImageView res = new ImageView(context);
		LayoutParams params = new LayoutParams(140, 140);
		res.setLayoutParams(params);
		res.setPadding(3, 3, 3, 3);
		res.setScaleType(ScaleType.FIT_XY);		
		res.setImageBitmap(b);
		
		return res;
	}
	
	/**
	 * Verify user 
	 */
	protected void verifyUser() {
		final String url = CommonUtilities.SERVERURL +
				CommonUtilities.API_CHECK_PARTIMER_VERIFIED + "?" +
				CommonUtilities.PARAM_PT_ID + "=" + pt_id;
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					Log.e(TAG, "Result >>> " + jsonStr + ".");
					
					if (jsonStr.trim().equalsIgnoreCase("true")) {
						
						// Update user is_verified status
						SharedPreferences.Editor editor = settings.edit();
						editor.putString(CommonUtilities.USER_IS_VERIFIED, jsonStr.trim());
						editor.commit();
						
						if(isProfileComplete == false) {
							profileNotCompleteDialog();							
						} else {
							allowAcceptJob();
						}

					} else if(jsonStr.trim().equalsIgnoreCase("false")){
						ValidationUtilities.resendLinkDialog(JobDetails.this, pt_id);
					} else if(jsonStr.trim().equalsIgnoreCase("1")){
						Toast.makeText(context, getString(R.string.server_error),
								Toast.LENGTH_SHORT).show();
					} else {
						NetworkUtils.connectionHandler(context, jsonStr, "");
					}
				} else {
					Toast.makeText(context, getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context, getString(R.string.job_menu), 
				getString(R.string.verifying),
				true, false);
		
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);
				Log.d(CommonUtilities.TAG, "Result from " + url + " >>>\n" + jsonStr);
				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}
	
	private void allowAcceptJob() {
		Intent i = new Intent(context, RequirementsDetail.class);
		i.putExtra("optional", rawOptional);
		if(rawRequirement.length() > 0) {
			i.putExtra("mandatory_requirements", rawRequirement);
			i.putExtra("optional_requirements", rawOptional);
		}
		
		startActivityForResult(i, RC_ACCEPT);
	}
	
	private void profileNotCompleteDialog() {
		// Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(JobDetails.this);
        builder.setTitle(R.string.profile_header);
        builder.setMessage(R.string.profile_complete_question)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                      // Nothing here
                   }
               });
        Dialog dialog = builder.create();
		dialog.show();
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
				boolean isBlocked = data.getBooleanExtra(
						CommonUtilities.INTENT_REJECT_IS_BLOCKED, false);
				doRejectOffer(isBlocked);
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
	
	private void loadLocation() {
		loadMap(location);
//		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_AVAILABILITY_BY_AVAIL_ID 
//				+"?" + CommonUtilities.PARAM_AVAIL_ID + "=";
//
//		final Handler mHandlerFeed = new Handler();
//		final Runnable mUpdateResultsFeed = new Runnable() {
//			public void run() {
//				if (jsonStr != null) {
//					try {
//						JSONObject obj = new JSONObject(jsonStr);
//						obj = obj.getJSONObject("availabilities");
//						String location = jsonParser.getString(obj, "location");
//						Log.d(TAG, "Load location " + location);
//						
//						loadMap(location);
//					} catch (JSONException e1) {						
//						NetworkUtils.connectionHandler(JobDetails.this, jsonStr, e1.getMessage());
//					} catch (Exception e) {
//						Log.e(CommonUtilities.TAG, ">>> " + e.getMessage());
//					}
//				} else {
//					Toast.makeText(JobDetails.this,
//							getString(R.string.server_error), Toast.LENGTH_SHORT).show();
//				}
//			}
//		};
//
//		new Thread() {
//			public void run() {
//				jsonParser = new JSONParser();
//				jsonStr = jsonParser.getHttpResultUrlGet(url + avail_id);
//
//				mHandlerFeed.post(mUpdateResultsFeed);
//			}
//		}.start();
	}
	
	private void loadMessage() {
		// TODO Auto-generated method stub
		listMessage = new ArrayList<MessageModel>();
		MessageModel msg = new MessageModel();
		msg.setDate("1111");
		msg.setTime("0001");
		msg.setFrom("FROM 1");
		msg.setMessage("This is my message\n\nRegards,\nMe!");
		msg.setRead(true);
		listMessage.add(msg);
		msg = new MessageModel();
		msg.setDate("2222");
		msg.setTime("0002");
		msg.setFrom("FROM 2");
		msg.setMessage("This is my message\n\nRegards,\nMe!");
		msg.setRead(true);
		listMessage.add(msg);
		msg = new MessageModel();
		msg.setDate("3333");
		msg.setTime("0003");
		msg.setFrom("FROM 3");
		msg.setMessage("This is my message\n\nRegards,\nMe!");
		msg.setRead(false);
		listMessage.add(msg);
		msg = new MessageModel();
		msg.setDate("4444");
		msg.setTime("0004");
		msg.setFrom("FROM 4");
		msg.setMessage("This is my message\n\nRegards,\nMe!");
		msg.setRead(false);
		listMessage.add(msg);
		msg = new MessageModel();
		msg.setDate("5555");
		msg.setTime("0005");
		msg.setFrom("FROM 5");
		msg.setMessage("This is my message\n\nRegards,\nMe!");
		msg.setRead(true);
		listMessage.add(msg);
		msg = new MessageModel();
		msg.setDate("6666");
		msg.setTime("0006");
		msg.setFrom("FROM 6");
		msg.setMessage("This is my message\n\nRegards,\nMe!");
		msg.setRead(false);
		listMessage.add(msg);
		
		showMessage();
//		
//		final Handler mHandlerFeed = new Handler();
//		final Runnable mUpdateResultsFeed = new Runnable() {
//			public void run() {
//				if (jsonStr != null) {
//					try {
//						JSONArray items = new JSONArray(jsonStr);
//						if (items != null && items.length() > 0) {
//							for (int i = 0; i < items.length(); i++) {
//								/* get all json items, and put it on list */
//								try {
//									JSONObject objs = items.getJSONObject(i);
//									objs = objs.getJSONObject("message");
//									if (objs != null) {
//										MessageModel msg = new MessageModel();
//										msg.setDate(jsonParser.getString(objs, "date"));
//										msg.setFrom(jsonParser.getString(objs, "from"));
//										msg.setId(jsonParser.getString(objs, "id"));
//										msg.setMessage(jsonParser.getString(objs, "message"));
//										msg.setRead(Boolean.parseBoolean(jsonParser.getString(objs, "read")));
//										msg.setTime(jsonParser.getString(objs, "time"));
//										listMessage.add(msg);
//									}
//								} catch (Exception e) {
//								}
//							}
//						}
//					} catch (Exception e) {
//						Log.e(CommonUtilities.TAG, ">>> " + e.getMessage());
//					}
//				}
//				
//				showMessage();
//			}
//		};
//
//		new Thread() {
//			public void run() {
//				String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_MESSAGE 
//						+"?" + CommonUtilities.PARAM_PT_ID + "=" + pt_id;
//
//				jsonParser = new JSONParser();
//				jsonStr = jsonParser.getHttpResultUrlGet(url);
//
//				mHandlerFeed.post(mUpdateResultsFeed);
//			}
//		}.start();
	}

	private void showMessage() {
		TextView buttonNewest = (TextView)findViewById(R.id.buttonNewest);
		buttonNewest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!newestMode) {
					newestMode = true;
					showMessage();
				}
			}
		});
		TextView buttonUnread = (TextView)findViewById(R.id.buttonUnread);
		buttonUnread.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (newestMode) {
					newestMode = false;
					showMessage();
				}
			}
		});
		if (newestMode) {
			buttonNewest.setBackgroundResource(R.drawable.bg_button_tab);
			buttonNewest.setTextColor(Color.WHITE);
			buttonUnread.setBackgroundResource(R.drawable.bg_button_tab_def);
			buttonUnread.setTextColor(Color.BLACK);
		} else {
			buttonNewest.setBackgroundResource(R.drawable.bg_button_tab_def);
			buttonNewest.setTextColor(Color.BLACK);
			buttonUnread.setBackgroundResource(R.drawable.bg_button_tab);
			buttonUnread.setTextColor(Color.WHITE);
		}
		
		LinearLayout layMessage = (LinearLayout)findViewById(R.id.listMessage);
		layMessage.setVisibility(View.VISIBLE);
		layMessage.removeAllViews();
		
		LinearLayout layMessageContent = (LinearLayout)findViewById(R.id.layMessageContent);
		layMessageContent.setVisibility(View.GONE);
		
		if (listMessage != null && listMessage.size() > 0) {
			for (int i = 0; i < listMessage.size(); i++) {
				LinearLayout message = new LinearLayout(context);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				message.setLayoutParams(params);
				message.setPadding(0, 5, 0, 0);
				message.setOrientation(LinearLayout.HORIZONTAL);
				final int selectedMessage = i;
				message.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						showMessageContent(selectedMessage);
					}
				});
				
				TextView textFrom = new TextView(context);
				params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
				params.weight = 1;
				textFrom.setLayoutParams(params);
				textFrom.setPadding(14, 4, 0, 4);
				
				TextView textDate = new TextView(context);
				params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
				params.weight = (float)0.5;
				textDate.setLayoutParams(params);
				textDate.setPadding(0, 4, 0, 4);
				
				TextView textTime = new TextView(context);
				params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
				params.weight = (float)0.5;
				textTime.setLayoutParams(params);
				textTime.setPadding(0, 4, 14, 4);
				
				if (newestMode) {
					textFrom.setText(listMessage.get(i).getFrom());
					textDate.setText(listMessage.get(i).getDate());
					textTime.setText(listMessage.get(i).getTime());
					
					if (listMessage.get(i).isRead()) {
						textFrom.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
						textDate.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
						textTime.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
					} else {
						textFrom.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
						textDate.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
						textTime.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
					}
					message.addView(textFrom);
					message.addView(textDate);
					message.addView(textTime);
					layMessage.addView(message);
				}
				if (!listMessage.get(i).isRead() && !newestMode) {
					textFrom.setText(listMessage.get(i).getFrom());
					textDate.setText(listMessage.get(i).getDate());
					textTime.setText(listMessage.get(i).getTime());
					
					textFrom.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
					textDate.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
					textTime.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
					
					message.addView(textFrom);
					message.addView(textDate);
					message.addView(textTime);
					layMessage.addView(message);
				}
			}
		}
	}

	protected void showMessageContent(final int selectedMessage) {
		LinearLayout layMessageContent = (LinearLayout)findViewById(R.id.layMessageContent);
		layMessageContent.setVisibility(View.VISIBLE);
		TextView textMessageContent = (TextView)findViewById(R.id.textMessageContent);
		textMessageContent.setText(listMessage.get(selectedMessage).getMessage());
		LinearLayout layMessage = (LinearLayout)findViewById(R.id.listMessage);
		layMessage.setVisibility(View.GONE);
		Button buttonOK = (Button)findViewById(R.id.buttonOk);
		buttonOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				markMessageAsRead(selectedMessage);
				showMessage();
			}
		});
		
		
		TextView textFrom = (TextView)findViewById(R.id.selectedTittle);
		TextView textDate = (TextView)findViewById(R.id.selectedDate);
		TextView textTime = (TextView)findViewById(R.id.selectedTime);
		textFrom.setText(listMessage.get(selectedMessage).getFrom());
		textDate.setText(listMessage.get(selectedMessage).getDate());
		textTime.setText(listMessage.get(selectedMessage).getTime());
		
		if (listMessage.get(selectedMessage).isRead()) {
			textFrom.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
			textDate.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
			textTime.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
		} else {
			textFrom.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
			textDate.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
			textTime.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		}
	}

	protected void markMessageAsRead(final int selectedMessage) {
		MessageModel msg = listMessage.get(selectedMessage);
		msg.setRead(true);
		listMessage.set(selectedMessage, msg);
		
		// TODO submit message as read
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_SET_MESSAGE_READ;
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				String[] params = { "ptid", "msg_id" };
				String[] values = { pt_id, listMessage.get(selectedMessage).getId() };
				jsonStr = jsonParser.getHttpResultUrlPut(url, params, values);
			}
		}.start();
	}
	
	@Override
	public void onBackPressed() {
		// If page opened through notification, override back pressed
		if(isNotification) {
			Intent setIntent = new Intent(this, HomeActivity.class);
			startActivity(setIntent);
			finish();
		}
		
		super.onBackPressed();
	}

	@Deprecated
	private void showRating(final float currentRating) {
		final RatingBar rate = (RatingBar)findViewById(R.id.rateJob);
		rate.setRating(currentRating);
		rate.setRating(currentRating);
		
		rate.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {
				rate.setRating(currentRating);
			}
		});
		rate.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (arg1.getAction() == MotionEvent.ACTION_UP) {
					rate.setRating(currentRating);
					showRatingDialog(currentRating);
				}
				return true;
			}
		});
	}

	@Deprecated
	private void showRatingDialog(float currentRating) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		// set title
		builder.setTitle(getString(R.string.app_name));
		// set dialog message
		builder.setMessage(getString(R.string.rate_this_job));
		
		final RatingBar rating = new RatingBar(context);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rating.setNumStars(5);
		rating.setStepSize(0.1f);
		rating.setLayoutParams(params);
		rating.setRating(currentRating);
		
		LinearLayout parent = new LinearLayout(context);
        parent.setGravity(Gravity.CENTER);
        parent.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        parent.addView(rating);
        
		builder.setView(parent);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				submitRating(rating.getRating());
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});

		Dialog dialog = builder.create();
		dialog.show();
	}

	@Deprecated
	protected void submitRating(final float f) {
		showRating(f);
		
		// TODO submit rating here !
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_SET_RATING;
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				String[] params = { "ptid", "avail_id", "rating" };
				String[] values = { pt_id, avail_id,  Float.toString(f)};
				jsonStr = jsonParser.getHttpResultUrlPut(url, params, values);
			}
		}.start();
	}
}
