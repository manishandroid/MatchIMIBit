package com.matchimi.options;

import static com.matchimi.CommonUtilities.API_WITHDRAW_AVAILABILITY;
import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SERVERURL;
import static com.matchimi.CommonUtilities.SETTING_THEME;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.THEME_LIGHT;

import java.util.ArrayList;
import java.util.List;

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
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
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
import com.matchimi.R;
import com.matchimi.ValidationUtilities;
import com.matchimi.Variables;
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
	private String jobID = null;
	private String reasons = null;
	
	public static final int RC_CANCEL = 21;
	public static final int RC_REJECT = 22;
	public static final int RC_ACCEPT = 23;
	
	private String isVerified = "false";
	private boolean isProfileComplete;
	private String pt_id = "";

	private List<MessageModel> listMessage = null;
	private List<Float> listRating = null;
	private List<Bitmap> listFriend = null;
	private boolean newestMode = true;

	private SharedPreferences settings;
	private String optional = "";
	private String requirement = "";
	private String location;
	private String rawOptional = "";
	private String rawRequirement = "";
	private int progressbar;
	private boolean colorstatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = getSharedPreferences(
				PREFS_NAME, Context.MODE_PRIVATE);
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
		jobID = b.getString("id");
		String price = b.getString("price");
		String date = b.getString("date");
		final String place = b.getString("place");
		String expire = b.getString("expire");
		String description = b.getString("description");
		rawRequirement = b.getString("requirement");		
		rawOptional = b.getString("optional");
		location = b.getString("location");
		progressbar = b.getInt("progressbar");
		colorstatus = b.getBoolean("colorstatus");
		
		// Parsing requirements mandatory and optional
		requirement = ProcessDataUtils.parseRequirement(rawRequirement);		
		optional = ProcessDataUtils.parseRequirement(rawOptional);
				
		String jobType = b.getString("type");

		TextView textPrice = (TextView) findViewById(R.id.textPrice);
		textPrice.setText(Html.fromHtml(price));
		TextView textDate = (TextView) findViewById(R.id.textDate);
		textDate.setText(date);
		TextView textPlace = (TextView) findViewById(R.id.textPlace);
		textPlace.setText(place);
		
//		TextView textExpire = (TextView) findViewById(R.id.textExpire);
//		if (jobType.equalsIgnoreCase("offer")) {
//			textExpire.setText(expire + " before this offer expires");
//		} else if (jobType.equalsIgnoreCase("accepted")) {
//			textExpire.setText("Job for " + expire);
//		} else {
//			textExpire.setText(expire);
//		}

		TextProgressBar textProgressBar = (TextProgressBar) findViewById(R.id.progressBarWithText);
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
		TextView textRequirement = (TextView) findViewById(R.id.textRequirement);
		textRequirement.setText(requirement);
		TextView textOptional = (TextView) findViewById(R.id.textOptional);
		if (optional == null || optional.length() == 0) {
			textOptional.setText("");
		} else {
			textOptional.setText(optional);
		}

		TextView buttonAccept = (TextView) findViewById(R.id.buttonAccept);
		buttonAccept.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isVerified == "false") {
					ValidationUtilities.resendLinkDialog(JobDetails.this, pt_id);
				} else if (isProfileComplete == false) {						
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

		RelativeLayout additionalLayout = (RelativeLayout)findViewById(R.id.layAdditional);
		if (jobType.equalsIgnoreCase("offer")) {
			buttonAccept.setVisibility(View.VISIBLE);
			buttonReject.setVisibility(View.VISIBLE);
			buttonCancel.setVisibility(View.GONE);
			additionalLayout.setVisibility(View.VISIBLE);
		} else if (jobType.equalsIgnoreCase("accepted")) {
			buttonAccept.setVisibility(View.GONE);
			buttonReject.setVisibility(View.GONE);
			buttonCancel.setVisibility(View.VISIBLE);
			additionalLayout.setVisibility(View.GONE);
		} else {
			buttonAccept.setVisibility(View.GONE);
			buttonReject.setVisibility(View.GONE);
			buttonCancel.setVisibility(View.GONE);
			additionalLayout.setVisibility(View.GONE);
		}

		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();

		loadLocation();
		
		if (getIntent().hasExtra("rating")) {
			showRating(getIntent().getExtras().getFloat("rating"));
		} else {
			showRating(0f);
		}
		
		showMessage();
		showFriends();
		
		loadMessage();
		loadFriends();
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
			i.putExtra("requirement", rawRequirement);			
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
//				jsonStr = jsonParser.getHttpResultUrlGet(url + jobID);
//
//				mHandlerFeed.post(mUpdateResultsFeed);
//			}
//		}.start();
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

	protected void doCancelOffer(String inputReason) {
		Log.e(TAG, ">>> Reason: " + inputReason);
		reasons = inputReason;
		final String url = SERVERURL+ API_WITHDRAW_AVAILABILITY;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					Log.e(TAG, "Result >>> " + jsonStr + ".");
					
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
				String[] params = { "avail_id", "reasons"};
				String[] values = { jobID, reasons };
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
						Toast.makeText(context, getString(R.string.job_reject_offer_success),
								Toast.LENGTH_SHORT).show();
						setResult(RESULT_OK);
						finish();
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

					childData.put("avail_id", jobID);
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
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						// Reload schedule
						Intent i = new Intent("schedule.receiver");
						sendBroadcast(i);
						
						Toast.makeText(context, getString(R.string.job_accept_offer_success),
								Toast.LENGTH_SHORT).show();
						setResult(RESULT_OK);
						finish();
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
				String[] values = { jobID };
				jsonStr = jsonParser.getHttpResultUrlPut(url, params, values);

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

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

	protected void submitRating(final float f) {
		showRating(f);
		
		// TODO submit rating here !
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_SET_RATING;
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				String[] params = { "ptid", "avail_id", "rating" };
				String[] values = { pt_id, jobID,  Float.toString(f)};
				jsonStr = jsonParser.getHttpResultUrlPut(url, params, values);
			}
		}.start();
	}

	private void loadFriends() {
		// TODO Auto-generated method stub
		listFriend = new ArrayList<Bitmap>();
		Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		listFriend.add(icon);
		listFriend.add(icon);
		listFriend.add(icon);
		listFriend.add(icon);
		listFriend.add(icon);
		listFriend.add(icon);
		
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
									objs = objs.getJSONObject("friend");
									if (objs != null) {
										byte[] imageAsBytes = Base64.decode(jsonParser.getString(
												objs, "image").getBytes(), 0);
										listFriend.add(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
									}
								} catch (Exception e) {
								}
							}
						}
					} catch (Exception e) {
						Log.e(CommonUtilities.TAG, ">>> " + e.getMessage());
					}
				}
				
				showFriends();
			}
		};

		new Thread() {
			public void run() {
				String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_FRIEND_BY_PT_ID 
						+"?" + CommonUtilities.PARAM_PT_ID + "=" + pt_id;

				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);

				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}

	private void showFriends() {
		LinearLayout layFriend = (LinearLayout)findViewById(R.id.layFriend);
		layFriend.removeAllViews();
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
		LayoutParams params = new LayoutParams(70, 70);
		res.setLayoutParams(params);
		res.setPadding(3, 3, 3, 3);
		res.setScaleType(ScaleType.CENTER_INSIDE);
		res.setImageBitmap(b);
		
		return res;
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
									objs = objs.getJSONObject("message");
									if (objs != null) {
										MessageModel msg = new MessageModel();
										msg.setDate(jsonParser.getString(objs, "date"));
										msg.setFrom(jsonParser.getString(objs, "from"));
										msg.setId(jsonParser.getString(objs, "id"));
										msg.setMessage(jsonParser.getString(objs, "message"));
										msg.setRead(Boolean.parseBoolean(jsonParser.getString(objs, "read")));
										msg.setTime(jsonParser.getString(objs, "time"));
										listMessage.add(msg);
									}
								} catch (Exception e) {
								}
							}
						}
					} catch (Exception e) {
						Log.e(CommonUtilities.TAG, ">>> " + e.getMessage());
					}
				}
				
				showMessage();
			}
		};

		new Thread() {
			public void run() {
				String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_MESSAGE 
						+"?" + CommonUtilities.PARAM_PT_ID + "=" + pt_id;

				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);

				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
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
}
