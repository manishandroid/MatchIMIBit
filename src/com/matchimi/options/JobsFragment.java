package com.matchimi.options;

import static com.matchimi.CommonUtilities.PARAM_PT_ID;
import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SERVERURL;
import static com.matchimi.CommonUtilities.TAG;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
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
import com.matchimi.ValidationUtilities;
import com.matchimi.Variables;
import com.matchimi.ongoingjobs.OngoingJobsActivity;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.ProcessDataUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class JobsFragment extends Fragment {

	private View view = null;
	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private GoogleMap map = null;

	private JobAdapter adapter;
	private ListView listview;
	private ProgressBar progressBar;
	private TextView tabList;
	private TextView tabLocation;
	private ScrollView mapScrollView;
	private RelativeLayout mapJobDetail;
	private ProgressDialog progress;
	
	private List<String> listAddress = null;
	private List<String> listAvailID = null;
	private List<String> listPrice = null;
	private List<String> listCompany = null;
	private List<String> listSchedule = null;
	private List<String> listTimeLeft = null;
	private List<Integer> listProgressBar = null;
	private List<Boolean> listColorStatus = null;	
	private List<String> listDescription = null;
	private List<List<String>> listRequirement = null;
	private List<List<String>> listOptional = null;
	private List<String> listLocation = null;
	private List<String> listJobFunctionName = null;
	private List<MessageModel> listMessage = null;
	private List<Float> listRating = null;
	
	private List<List<String>> listScheduleFriendsFacebookID = null;
	private List<List<String>> listScheduleFriendsFirstName = null;	
	private List<List<String>> listScheduleFriendsLastName = null;	
	private List<List<String>> listScheduleFriendsProfilePicture = null;
	private List<List<String>> listScheduleFriendsPtID = null;
	private List<List<Bitmap>> listFriend = null;

	private String pt_id = null;
	private int selectedList = -1;
	private boolean modeList = true;
	private boolean newestMode = true;
	private Context context;
	private static final int ONGOING_MENU = 1;

	public static final int RC_REJECT = 22;
	public static final int RC_ACCEPT = 23;
	
	public static final String EXTRA_TITLE = "title";
	public static final int RC_JOB_DETAIL = 10;
	
	private Button jobAvailaibilityButton;
	private Button onGoingJobsButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if (view != null) {
	        ViewGroup parent = (ViewGroup) view.getParent();
	        if (parent != null)
	            parent.removeView(view);
	    }

	    try {
		    view = inflater.inflate(R.layout.jobs_menu, container, false);
	    } catch (InflateException e) {
	        /* map is already there, just return view as it is */
	    }
	    
		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		pt_id = settings.getString(CommonUtilities.USER_PTID, null);
		
		adapter = new JobAdapter(getActivity());
		
//		onGoingJobsButton = (Button) view.findViewById(R.id.onGoingButtonJobOffer);
//		onGoingJobsButton.setOnClickListener(onGoingListener);
		
		listview = (ListView) view.findViewById(R.id.joblistview);
		listview.setAdapter(adapter);
		
		progressBar = (ProgressBar) view.findViewById(R.id.progress);
		
		tabList = (TextView)view.findViewById(R.id.buttonList);
		tabList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!modeList) {
					modeList = true;
					selectedList = -1;
					manageTab();
				}
			}
		});
		
		tabLocation = (TextView)view.findViewById(R.id.buttonLocation);
		tabLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (modeList) {
					modeList = false;
					selectedList = -1;
					manageTab();
				}
			}
		});
		
		mapScrollView = (ScrollView)view.findViewById(R.id.mapScrollView);
		mapJobDetail = (RelativeLayout)view.findViewById(R.id.mapJobDetail);

		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {				
				
				Intent i = new Intent(getActivity(), JobDetails.class);
				i.putExtra("price", listPrice.get(arg2));
				i.putExtra("date", listSchedule.get(arg2));
				i.putExtra("place",
						listCompany.get(arg2) + "\n" + listAddress.get(arg2));
				i.putExtra("expire", listTimeLeft.get(arg2));
				i.putExtra("description", listDescription.get(arg2));

				i.putExtra("mandatory_requirements", listRequirement.get(arg2).toString());				
				i.putExtra("optional_requirements", listOptional.get(arg2).toString());
				i.putExtra("avail_id", listAvailID.get(arg2));
				i.putExtra("type", "offer");
				i.putExtra("rating", listRating.get(arg2));
				i.putExtra("location", listLocation.get(arg2));
				i.putExtra("progressbar", listProgressBar.get(arg2));
				i.putExtra("colorstatus", listColorStatus.get(arg2));
				i.putExtra("job_function_name", listJobFunctionName.get(arg2));
				
				i.putExtra(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_ID, listScheduleFriendsFacebookID.get(arg2).toString());
				i.putExtra(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_FIRST_NAME, listScheduleFriendsFirstName.get(arg2).toString());	
				i.putExtra(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_LAST_NAME, listScheduleFriendsLastName.get(arg2).toString());	
				i.putExtra(CommonUtilities.JSON_KEY_FRIEND_FACEBOOK_PROFILE_PICTURE, listScheduleFriendsProfilePicture.get(arg2).toString());
				i.putExtra(CommonUtilities.PARAM_PT_ID, listScheduleFriendsPtID.get(arg2).toString());
				
				startActivityForResult(i, RC_JOB_DETAIL);
			}
		});
		
		if (map == null) {
			map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		}
		
		context = getActivity().getApplicationContext();
		
		jobAvailaibilityButton = (Button) view.findViewById(R.id.buttonJobAvailability);
		jobAvailaibilityButton.setOnClickListener(jobAvailabilityListener);
		
		loadData();
		
		return view;
	}
	
	private void loadData() {
		selectedList = -1;
		modeList = true;
		newestMode = true;
		
		map.clear();
		manageTab();
		
		final String url = CommonUtilities.SERVERURL + 
				CommonUtilities.API_GET_CURRENT_JOB_OFFERS + "?" +
				CommonUtilities.PARAM_PT_ID + "=" + pt_id;
		Log.d(CommonUtilities.TAG, "Job offer URL " + url);
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				listAvailID = new ArrayList<String>();
				listPrice = new ArrayList<String>();
				listAddress = new ArrayList<String>();
				listCompany = new ArrayList<String>();
				listSchedule = new ArrayList<String>();
				listTimeLeft = new ArrayList<String>();
				listDescription = new ArrayList<String>();
				listLocation = new ArrayList<String>();
				listJobFunctionName = new ArrayList<String>();
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
				
				// Dummy job offers
//				jsonStr = "[{\"sub_slots\":{\"address\":\"No 1 CPF Tampines Building Tampines Central 5 #01-04\",\"avail_id\":403,\"branch_id\":9,\"branch_name\":\"Tampines CPF\",\"company_id\":2,\"company_name\":\"ABC F&B\",\"description\":\"\",\"end_date_time\":\"2013-11-26T15:30:00+08:00\",\"friends\":[{\"part_timer_friends\":{\"facebook_id\":\"715383314\",\"first_name\":\"Jack\",\"last_name\":\"Tan\",\"profile_picture\":\"/matchimi/matchimi_api/data/images/profile_pic/1383738044_image.jpeg\",\"pt_id\":24}},{\"part_timer_friends\":{\"facebook_id\":\"100006545287128\",\"first_name\":\"Polatic\",\"last_name\":\"Dev\",\"profile_picture\":\"/matchimi/matchimi_api/data/images/profile_pic/1383630071_man3.jpeg\",\"pt_id\":27}}],\"grade\":\"3\",\"job_function_id\":1,\"job_function_name\":\"Waiter\",\"latitude\":\"1.352954\",\"location\":\"1.352954,103.943696\",\"longitude\":\"103.943696\",\"main_slot_id\":58,\"mandatory_requirements\":[\"Must wear black shoe.\"],\"offered_salary\":6.0,\"optional_requirements\":[],\"postal_code\":\"529508\",\"start_date_time\":\"2013-11-26T10:00:00+08:00\",\"expired_at\":\"2013-11-26T10:00:00+08:00\", \"status\":\"PA\",\"sub_slot_id\":455,\"ts_end_date_time\":1385479800,\"ts_start_date_time\":1385460000}}]";
//				jsonStr = "[{\"sub_slots\":{\"address\":\"\",\"avail_id\":852,\"branch_id\":13,\"branch_name\":\"Funan Digital Mall \",\"company_id\":1,\"company_name\":\"Pizza Hut\",\"description\":\"\",\"end_date_time\":\"2014-01-19T15:00:00+08:00\",\"expired_at\":\"2014-01-02T21:59:59+08:00\",\"friends\":[{\"part_timer_friends\":{\"facebook_id\":\"100006545287128\",\"first_name\":\"Polatic\",\"last_name\":\"Dev\",\"profile_picture\":\"/matchimi/matchimi_api/data/images/profile_pic/1384769367_profile.jpg\",\"pt_id\":27}}],\"grade\":\"0\",\"job_function_id\":6,\"job_function_name\":\"Waiter/Waitress\",\"latitude\":\"1.396515\",\"location\":\"1.396515,103.819991\",\"longitude\":\"103.819991\",\"main_slot_id\":302,\"mandatory_requirements\":[\"Must wear black shoes and black pants\"],\"offered_salary\":7.0,\"optional_requirements\":[],\"postal_code\":\"\",\"start_date_time\":\"2014-01-19T12:00:00+08:00\",\"status\":\"MA\",\"sub_slot_id\":1604,\"ts_end_date_time\":1390114800,\"ts_expired_at\":1388671199,\"ts_start_date_time\":1390104000}}]";
				
				if (jsonStr != null) {
					try {
						JSONArray items = new JSONArray(jsonStr);
						
						if (items != null && items.length() > 0) {
							Calendar calToday = new GregorianCalendar();
							SimpleDateFormat formatterDate = new SimpleDateFormat(
									"EE d, MMM", Locale.getDefault());
							SimpleDateFormat formatterTime = new SimpleDateFormat(
									"hh:mm a", Locale.getDefault());
							for (int i = 0; i < items.length(); i++) {
								/* get all json items, and put it on list */
								try {
									JSONObject objs = items.getJSONObject(i);
									objs = objs.getJSONObject("sub_slots");
									if (objs != null) {
										listAvailID.add(jsonParser.getString(objs, "avail_id"));
										String price = "" + jsonParser.getDouble(objs,
														"offered_salary");
										if (Integer
												.parseInt(price.substring(price
														.indexOf(".") + 1)) == 0) {
											price = price.substring(0,
													price.indexOf("."));
										}
										
										listPrice
												.add("<font color='#A4A9AF'>$</font><big><big><big><big><font color='#276289'>"
														+ price
														+ "</font></big></big></big></big><font color='#A4A9AF'>/hr</font>");
										
										listAddress.add(jsonParser.getString(
												objs, "address"));
										String companyName = jsonParser.getString(objs, "company_name");
										String branchName = jsonParser.getString(objs, "branch_name");
										
										listCompany.add(branchName + ", " +companyName);
										
										listDescription.add(jsonParser.getString(objs, "description"));
										
										listLocation.add(jsonParser.getString(objs, "location"));
										listJobFunctionName.add(jsonParser.getString(objs, "job_function_name"));
										listRating.add(Float.parseFloat(jsonParser.getString(objs, "grade")));
										
										String startDate = jsonParser.getString(objs, "start_date_time");
										String endDate = jsonParser.getString(objs, "end_date_time");
										Calendar calStart = ProcessDataUtils.generateCalendar(startDate);
										Calendar calEnd = ProcessDataUtils.generateCalendar(endDate);
										
										listSchedule
												.add(formatterDate
														.format(calStart
																.getTime())
														+ "\n"
														+ formatterTime
																.format(calStart
																		.getTime())
																.toLowerCase(
																		Locale.getDefault())
														+ " - "
														+ formatterTime
																.format(calEnd
																		.getTime())
																.toLowerCase(
																		Locale.getDefault()));
										
										String expiredAt = jsonParser.getString(objs,
														"expired_at");
										Calendar calExpiredAt = ProcessDataUtils.generateCalendar(expiredAt);
										
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
											
											listTimeLeft.add(timeLeft);

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
											listTimeLeft.add(timeLeft);	
										}
										
//										Log.d(CommonUtilities.TAG, "Percentage " + progressPercentage);
										
										listProgressBar.add((int) progressPercentage);
										
										// Calculate colors										
										Boolean redStatus = false;
										if(progressPercentage > 50) {
											redStatus = true;
										}
										
										listColorStatus.add(redStatus);
										
										// Load requirement list
										JSONArray listReqArray = new JSONArray(jsonParser
												.getString(objs, "mandatory_requirements"));	
										
										List<String> listRequirementItem = new ArrayList<String>();
										if (listReqArray != null && listReqArray.length() > 0) {
											for (int h = 0; h < listReqArray.length(); h++) {
												listRequirementItem.add(listReqArray.get(h).toString());	
											}
										}										
										listRequirement.add(listRequirementItem);
										
										// Load requirement list
										JSONArray listOptionalArray = new JSONArray(jsonParser
												.getString(objs, "optional_requirements"));	

										List<String> listOptionalItem = new ArrayList<String>();
										if (listOptionalArray != null && listOptionalArray.length() > 0) {
											for (int m = 0; m < listOptionalArray.length(); m++) {
												listOptionalItem.add(listOptionalArray.get(m).toString());	
											}
										}
										listOptional.add(listOptionalItem);
										
										// Load friend list
										JSONArray friends = new JSONArray(jsonParser
												.getString(objs, "friends"));										

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
										
										listScheduleFriendsFacebookID.add(listScheduleFriendsFacebookIDItem);
										listScheduleFriendsFirstName.add(listScheduleFriendsFirstNameItem);
										listScheduleFriendsLastName.add(listScheduleFriendsLastNameItem);	
										listScheduleFriendsProfilePicture.add(listScheduleFriendsProfilePictureItem);
										listScheduleFriendsPtID.add(listScheduleFriendsPtIDItem);
									}
									
								} catch (JSONException e) {
									Log.e("Parse Json Object",
											">> " + e.getMessage());
								}
							}
							
						} else {
							Log.e("Parse Json Object", ">> Array is null");
						}
					} catch (JSONException e1) {						
						NetworkUtils.connectionHandler(getActivity(), jsonStr,
								e1.getMessage());
						
						Log.e(CommonUtilities.TAG, "Error result " +
								jsonStr + " >> " + e1.getMessage());
					}
				} else {
					Toast.makeText(getActivity().getApplicationContext(),
							getString(R.string.server_error), Toast.LENGTH_SHORT).show();
				}

				adapter.updateList(listPrice, listAddress, listCompany,
						listSchedule, listTimeLeft, listProgressBar, listColorStatus);				

				loadFriends();				
			}
		};

		listview.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpsResultUrlGet(context, url);
				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		getActivity();
		
		Log.e(CommonUtilities.TAG, "Jobs Fragment onActivityResult()");
		if (resultCode == FragmentActivity.RESULT_OK) {
			if (requestCode == RC_JOB_DETAIL) {
				loadData();
			}
			if (requestCode == RC_ACCEPT) {
				Log.e(TAG, "Accept");
				doAcceptOffer();
			}
			if (requestCode == RC_REJECT) {
				Log.e(TAG, "Reject");
				boolean isBlocked = data.getBooleanExtra(
						CommonUtilities.INTENT_REJECT_IS_BLOCKED, false);
				doRejectOffer(isBlocked);
			}
		}
	}

	private void doRejectOffer(final boolean isBlocked) {
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_REJECT_JOB_OFFER;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(getActivity(), getString(R.string.job_reject_offer_success),
								Toast.LENGTH_SHORT).show();
						loadData();
					} else if(jsonStr.trim().equalsIgnoreCase("1")){
						Toast.makeText(
								getActivity(),
								getString(R.string.job_reject_offer_failed),
								Toast.LENGTH_LONG).show();
					} else {
						NetworkUtils.connectionHandler(getActivity(), jsonStr, "");
					}
				} else {
					Toast.makeText(getActivity(), getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(getActivity(), getString(R.string.job_menu), 
				getString(R.string.job_reject_progress),
				true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				try {
					JSONObject parentData = new JSONObject();
					JSONObject childData = new JSONObject();				

					childData.put("avail_id", listAvailID.get(selectedList));
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

	private void doAcceptOffer() {
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_ACCEPT_JOB_OFFER;
		Log.d(CommonUtilities.TAG, "Jobs Fragment Accept offers" + url + " >>>\n" + jsonStr);

		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					Log.d(CommonUtilities.TAG, "Result from " + url + " >>>\n" + jsonStr);
					
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						// Reload schedule
						Intent i = new Intent(CommonUtilities.BROADCAST_SCHEDULE_RECEIVER);
						getActivity().sendBroadcast(i);
						
						Toast.makeText(getActivity(), getString(R.string.job_accept_offer_success),
								Toast.LENGTH_SHORT).show();
						loadData();
					} else if(jsonStr.trim().equalsIgnoreCase("1")) {
						Toast.makeText(
								getActivity(),
								getString(R.string.job_accept_offer_failed),
								Toast.LENGTH_LONG).show();
					} else {
						NetworkUtils.connectionHandler(getActivity(), jsonStr, "");
					}
				} else {
					Toast.makeText(getActivity(), getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(getActivity(), getString(R.string.job_menu),
				getString(R.string.job_accept_offer_progress),
				true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				String[] params = { "avail_id" };
				String[] values = { listAvailID.get(selectedList) };
				jsonStr = jsonParser.getHttpResultUrlPut(url, params, values);

				Log.d(CommonUtilities.TAG, "Values : " + values.toString());
				
				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

	protected void loadMap() {
		map.clear();
		if (listLocation != null && listLocation.size() > 0) {
			for (int i = 0; i < listLocation.size(); i++) {
				String latitude = listLocation.get(i).substring(0, listLocation.get(i).indexOf(","));
				String longtitude = listLocation.get(i).substring(listLocation.get(i).indexOf(",") + 1);
				LatLng pos = new LatLng(Double.parseDouble(latitude),
						Double.parseDouble(longtitude));

				map.addMarker(new MarkerOptions().position(pos).title(listCompany.get(i))
						.snippet(i + ". " + listDescription.get(i))
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
				
				// Move the camera instantly to hamburg with a zoom of 15.
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));
				// Zoom in, animating the camera.
				map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
			}
			
			map.setOnMarkerClickListener(new OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker arg0) {
					String selected = arg0.getSnippet();
					int idx = selected.indexOf(".");
					selectedList = Integer.parseInt(selected.substring(0, idx));
					mapJobDetail.setVisibility(View.VISIBLE);
					loadMapJobDetail();
					
					return false;
				}
			});
		}
	}
	
	/**
	 * Load data in Location Map Tab
	 */
	protected void loadMapJobDetail() {
		TextView textPrice = (TextView) view.findViewById(R.id.textJobPrice);
		textPrice.setText(Html.fromHtml(listPrice.get(selectedList)));
		TextView textDate = (TextView) view.findViewById(R.id.textJobDate);
		textDate.setText(listSchedule.get(selectedList));
		TextView textPlace = (TextView) view.findViewById(R.id.textJobPlace);
		textPlace.setText(listCompany.get(selectedList) + "\n" + listAddress.get(selectedList));
		TextView textExpire = (TextView) view.findViewById(R.id.textExpire);
		textExpire.setText(listTimeLeft.get(selectedList) + " before this offer expires");
		TextView textDescription = (TextView) view.findViewById(R.id.textDescription);
		textDescription.setText(listDescription.get(selectedList));
		
		// Set requirements details
		TextView textRequirement = (TextView) view.findViewById(R.id.textRequirement);
		if (listRequirement.get(selectedList) == null || listRequirement.get(selectedList).size() == 0) {
			textRequirement.setText("");
		} else {
			for(int i=0; i<listRequirement.get(selectedList).size(); i++) {
				String requirementDetail = listRequirement.get(selectedList).get(i);
				textRequirement.setText(requirementDetail);				
			}
		}
		
		// Set optional details		
		TextView textOptional = (TextView) view.findViewById(R.id.textOptional);
		if (listOptional.get(selectedList) == null || listOptional.get(selectedList).size() == 0) {
			textOptional.setText("");
		} else {
			for(int j=0; j<listOptional.get(selectedList).size(); j++) {
				String optionalDetail = listOptional.get(selectedList).get(j);
				textOptional.setText(optionalDetail);
			}
		}		
		
		TextView buttonCancel = (TextView) view.findViewById(R.id.buttonCancel);
		buttonCancel.setVisibility(View.GONE);
		
		showFriends();
	}


	private void showFriends() {
		LinearLayout layFriend = (LinearLayout)view.findViewById(R.id.layFriend);
		
		if(layFriend != null) {
			layFriend.removeAllViews();
			
			if (listFriend != null && listFriend.size() > 0 && selectedList > 0 && listFriend.get(selectedList).size() > 0) {
				LinearLayout layRow = new LinearLayout(getActivity());
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				layRow.setLayoutParams(params);
				layRow.setGravity(Gravity.CENTER_HORIZONTAL);
				layRow.setOrientation(LinearLayout.HORIZONTAL);
				int rowNum = ApplicationUtils.getColumnNumber(getActivity(), 64);

				for (int i = 0; i < listFriend.get(selectedList).size(); i++) {
					layRow.addView(createImageView(listFriend.get(selectedList).get(i)));
					if ((i + 1) % rowNum == 0) {
						layFriend.addView(layRow);
						layRow = new LinearLayout(getActivity());
						layRow.setLayoutParams(params);
						layRow.setGravity(Gravity.CENTER_HORIZONTAL);
						layRow.setOrientation(LinearLayout.HORIZONTAL);
					} else if (i == listFriend.size() - 1) {
						layFriend.addView(layRow);
					}
				}
			} else {
				Log.d(CommonUtilities.TAG, "No selected friends ");
				TextView labelFriends = (TextView) view.findViewById(R.id.labelFriend);
				labelFriends.setVisibility(View.GONE);
			}
		}
	}

	private ImageView createImageView(Bitmap b) {
		ImageView res = new ImageView(getActivity());
		LayoutParams params = new LayoutParams(70, 70);
		res.setLayoutParams(params);
		res.setPadding(3, 3, 3, 3);
		res.setScaleType(ScaleType.CENTER_INSIDE);
		res.setImageBitmap(b);
		
		return res;
	}

	private void loadFriends() {
		// TODO Auto-generated method stub
		listFriend = new ArrayList<List<Bitmap>>();
		
		if(selectedList > 0) {
			if(listScheduleFriendsPtID.get(selectedList) != null && listScheduleFriendsPtID.get(selectedList).size() > 0) {			
				List<Bitmap> imageFriends = new ArrayList<Bitmap>();
				
				for(int i = 0; i < listScheduleFriendsPtID.size(); i++) {
					String friendImage = new File(listScheduleFriendsProfilePicture.get(selectedList).get(i)).getName();				
					String url = SERVERURL + CommonUtilities.API_GET_PROFILE_PIC + "?"
							+ PARAM_PT_ID + "=" + listScheduleFriendsPtID.get(selectedList).get(i);

					String imagePath = checkAndDownloadPic(friendImage, url, selectedList, i);
					
					if(imagePath.length() > 0) {
						File f = new File(CommonUtilities.IMAGE_ROOT, imagePath);
						
						Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());
						imageFriends.add(b);
						
					} else {
						Bitmap b = null;
						imageFriends.add(b);
					}
				}
				
				listFriend.add(imageFriends);
			}
		}
		
		manageTab();
		showFriends();
	}
	
	/**
	 * Check and download pictures if not exists
	 * @param imageStoragePath
	 * @param apiURL
	 * @return
	 */
	private String checkAndDownloadPic(String imageStoragePath,
			String apiURL, int selectedList, int order) {
		
		File f = new File(imageStoragePath);
		String filename = f.getName();
		Log.d(TAG, "Looking " + apiURL);
		
		File imageFile = new File(CommonUtilities.IMAGE_ROOT, filename);
		
		if (!imageFile.exists()) {
			String[] params = {apiURL, filename, ""+selectedList, ""+ order};
			new downloadWebPage().execute(params);
			filename = "";
			
		} else {
			Log.d(TAG, "Image exists");
		}
		
		return filename;
	}

	private class downloadWebPage extends AsyncTask<String, Void, String[]> {

	    @Override
		protected void onPostExecute(String[] result) {
			Log.d(TAG, "Finished download friends profile picture");
			
			File f = new File(CommonUtilities.IMAGE_ROOT, result[1]);
			Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());

			// Updating bitmap
			List<Bitmap> listBitmap = listFriend.get(Integer.parseInt(result[2]));
			listBitmap.set(Integer.parseInt(result[3]), b);
			listFriend.set(Integer.parseInt(result[2]), listBitmap);
			
			showFriends();
		}

		@Override
	    protected String[] doInBackground(String... params) {
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

	        return params;
	    }

	}
	
	private OnClickListener onGoingListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent i = new Intent(getActivity(), OngoingJobsActivity.class);
			startActivity(i);
		}
	};

	protected void manageTab() {
		if (modeList) {
//			Log.d(TAG, "TABBED ");
			tabList.setBackgroundResource(R.drawable.bg_button_tab);
			tabList.setTextColor(Color.WHITE);
			tabLocation.setBackgroundResource(R.drawable.bg_button_tab_def);
			tabLocation.setTextColor(Color.BLACK);
			listview.setVisibility(View.VISIBLE);
			mapScrollView.setVisibility(View.GONE);
			
		} else {
			
			tabLocation.setBackgroundResource(R.drawable.bg_button_tab);
			tabLocation.setTextColor(Color.WHITE);
			tabList.setBackgroundResource(R.drawable.bg_button_tab_def);
			tabList.setTextColor(Color.BLACK);
			listview.setVisibility(View.GONE);
			mapScrollView.setVisibility(View.VISIBLE);
			
			if (selectedList == -1) {
				mapJobDetail.setVisibility(View.GONE);
			} else {
				mapJobDetail.setVisibility(View.VISIBLE);
			}
			loadMap();
		}
		
		progressBar.setVisibility(View.GONE);
		
		TextView buttonReject = (TextView)view.findViewById(R.id.buttonReject);
		buttonReject.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getActivity(), RejectPreview.class);
				i.putExtra("company", listCompany.get(selectedList));
				startActivityForResult(i, RC_REJECT);
			}
		});
		
		TextView buttonAccept = (TextView)view.findViewById(R.id.buttonAccept);
		buttonAccept.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SharedPreferences settings = getActivity().getSharedPreferences(
						PREFS_NAME, Context.MODE_PRIVATE);
				String isVerified = settings.getString(CommonUtilities.USER_IS_VERIFIED, "true");
				boolean isProfileComplete = settings.getBoolean(CommonUtilities.USER_PROFILE_COMPLETE, false);
				
//				if(isVerified == "false") {
//					ValidationUtilities.resendLinkDialog(getActivity(), pt_id);
//				} else 
				if (isProfileComplete == false) {						
					// Use the Builder class for convenient dialog construction
			        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			        builder.setTitle(R.string.profile_header);
			        builder.setMessage(R.string.profile_complete_question)
			               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			                   public void onClick(DialogInterface dialog, int id) {
			                      // Nothing here
			                   }
			               });
			        Dialog dialog = builder.create();
					dialog.show();
				} else {
					Intent i = new Intent(getActivity(), RequirementsDetail.class);
					i.putExtra("optional", listOptional.get(selectedList).toString());
					if (listRequirement != null && listRequirement.get(selectedList) != null
							&& !listRequirement.get(selectedList).toString().equalsIgnoreCase("null")
							&& listRequirement.get(selectedList).toString().trim().length() > 1) {
						i.putExtra("requirement", listRequirement.get(selectedList).toString());
					}
					
					startActivityForResult(i, RC_ACCEPT);					
				}
			}
		});
	}

	public static Bundle createBundle(String title) {
		Bundle bundle = new Bundle();
		bundle.putString(EXTRA_TITLE, title);
		return bundle;
	}
	

	private android.view.View.OnClickListener jobAvailabilityListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(getActivity(), CreateAvailability.class);
			intent.putExtra("id", pt_id);
			intent.putExtra("update", false);
			startActivity(intent);
		}
	};

	BroadcastReceiver jobsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			loadData();
		}
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
//		Log.e(TAG, "onActivityCreated !!!");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
//		Log.e(TAG, "onAttach !!!");
		super.onAttach(activity);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
//		Log.e(TAG, "onContextItemSelected !!!");
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
//		Log.e(TAG, "onCreate !!!");
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
//		Log.e(TAG, "onDestroyView !!!");
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
//		Log.e(TAG, "onDetach !!!");
		super.onDetach();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
//		Log.e(TAG, "onHiddenChanged !!!");
		super.onHiddenChanged(hidden);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
//		Log.e(TAG, "onPause !!!");
		super.onPause();
		
		try {
			getActivity().unregisterReceiver(jobsReceiver);
		} catch(Exception e) {
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onResume !!!");
		try {
			getActivity().registerReceiver(jobsReceiver, new IntentFilter(CommonUtilities.BROADCAST_JOBS_RECEIVER));
		} catch (IllegalArgumentException e) {
			
		}
		
		super.onResume();
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onStart !!!");
		super.onStart();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onViewCreated !!!");
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		try {
			getActivity().unregisterReceiver(jobsReceiver);
		} catch(Exception e) {
		}
	}
}
