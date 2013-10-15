package com.matchimi.options;

import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.TAG;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.matchimi.utils.ApplicationUtils;
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
	private ScrollView scrollView;
	private RelativeLayout jobDetail;;
	private ProgressDialog progress;
	
	private List<String> listAddress = null;
	private List<String> listAvailID = null;
	private List<String> listPrice = null;
	private List<String> listCompany = null;
	private List<String> listSchedule = null;
	private List<String> listTimeLeft = null;
	private List<String> listProgressBar = null;
	private List<String> listDescription = null;
	private List<String> listRequirement = null;
	private List<String> listOptional = null;
	private List<String> listLocation = null;
	private List<MessageModel> listMessage = null;
	private List<Float> listRating = null;
	private List<Bitmap> listFriend = null;

	private String pt_id = null;
	private int selectedList = -1;
	private boolean modeList = true;
	private boolean newestMode = true;

	public static final int RC_REJECT = 22;
	public static final int RC_ACCEPT = 23;
	
	public static final String EXTRA_TITLE = "title";
	public static final int RC_JOB_DETAIL = 10;

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
		scrollView = (ScrollView)view.findViewById(R.id.scrollView);
		jobDetail = (RelativeLayout)view.findViewById(R.id.jobDetail);

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
				i.putExtra(Variables.job_requirement, listRequirement.get(arg2));
				i.putExtra("optional", listOptional.get(arg2));
				i.putExtra("id", listAvailID.get(arg2));
				i.putExtra("type", "offer");
				i.putExtra("rating", listRating.get(arg2));
				i.putExtra("location", listLocation.get(arg2));				
				startActivityForResult(i, RC_JOB_DETAIL);
			}
		});
		
		if (map == null) {
			map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		}
		
		loadData();
		getActivity().registerReceiver(jobsReceiver, new IntentFilter("jobs.receiver"));

		return view;
	}

	BroadcastReceiver jobsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			loadData();
		}
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onActivityCreated !!!");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onAttach !!!");
		super.onAttach(activity);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onContextItemSelected !!!");
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onCreate !!!");
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onDestroyView !!!");
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onDetach !!!");
		super.onDetach();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onHiddenChanged !!!");
		super.onHiddenChanged(hidden);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onPause !!!");
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onResume !!!");
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
		getActivity().unregisterReceiver(jobsReceiver);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		getActivity();
		Log.e("JobsFragment", "onActivityResult()");
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
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						// Reload schedule
						Intent i = new Intent("schedule.receiver");
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

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
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
				listRequirement = new ArrayList<String>();
				listOptional = new ArrayList<String>();
				listLocation = new ArrayList<String>();
				listProgressBar = new ArrayList<String>();

				if (jsonStr != null) {
					try {
						JSONArray items = new JSONArray(jsonStr);
						
						if (items != null && items.length() > 0) {
							Calendar calToday = new GregorianCalendar();
							SimpleDateFormat formatterDate = new SimpleDateFormat(
									"EE d, MMM", Locale.getDefault());
							SimpleDateFormat formatterTime = new SimpleDateFormat(
									"hh a", Locale.getDefault());
							for (int i = 0; i < items.length(); i++) {
								/* get all json items, and put it on list */
								try {
									JSONObject objs = items.getJSONObject(i);
									objs = objs.getJSONObject("sub_slots");
									if (objs != null) {
										listAvailID.add(jsonParser.getString(
												objs, "avail_id"));
										String price = ""
												+ jsonParser.getDouble(objs,
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
										listCompany.add(jsonParser.getString(
												objs, "company_name"));
										listRequirement
												.add(jsonParser.getString(objs,
														"requirements"));
										listDescription
												.add(jsonParser.getString(objs,
														"description"));
										listOptional.add(jsonParser.getString(
												objs, "optional"));
										listLocation.add(jsonParser.getString(
												objs, "location"));
										
										String startDate = jsonParser
												.getString(objs,
														"start_date_time");
										String endDate = jsonParser.getString(
												objs, "end_date_time");
										Calendar calStart = generateCalendar(startDate);
										Calendar calEnd = generateCalendar(endDate);
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
										
										String expiredAt = jsonParser
												.getString(objs,
														"expired_at");
										Calendar calExpiredAt = generateCalendar(expiredAt);
										
										int diffMnt = (int) ((calExpiredAt
												.getTimeInMillis() - calToday
												.getTimeInMillis()) / (1000 * 60));
										String timeLeft = "";
										if (diffMnt / (60 * 24) > 0) {
											timeLeft = (diffMnt / (60 * 24))
													+ " day ";
											diffMnt = diffMnt % (60 * 24);
										}
										
										listTimeLeft
												.add(timeLeft + (diffMnt / 60)
														+ " hrs "
														+ (diffMnt % 60)
														+ " mins left");
										
										listProgressBar.add("50");
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
						listSchedule, listTimeLeft, listProgressBar);				

				loadFriends();				
			}
		};

		listview.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);
				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}

	@Deprecated
	/**
	 * Only use for retreieve availability location
	 */
	private void loadLocation() {
		listLocation = new ArrayList<String>();
		selectedList = -1;
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				loadMessage();
			}
		};

		new Thread() {
			public void run() {
				for (String id : listAvailID) {
					String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_AVAILABILITY_BY_AVAIL_ID 
							+"?" + CommonUtilities.PARAM_AVAIL_ID + "=" + id;

					jsonParser = new JSONParser();
					jsonStr = jsonParser.getHttpResultUrlGet(url);
					if (jsonStr != null) {
						try {
							JSONObject obj = new JSONObject(jsonStr);
							obj = obj.getJSONObject("availabilities");
							String location = jsonParser.getString(obj, "location");
							listLocation.add(location);
						} catch (Exception e) {
							Log.e(CommonUtilities.TAG, ">>> " + e.getMessage());
						}
					}
				}

				mHandlerFeed.post(mUpdateResultsFeed);
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
					jobDetail.setVisibility(View.VISIBLE);
					loadJobDetail();
					return false;
				}
			});
		}
	}
	
	protected void loadJobDetail() {
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
		TextView textRequirement = (TextView) view.findViewById(R.id.textRequirement);
		textRequirement.setText(listRequirement.get(selectedList));
		TextView textOptional = (TextView) view.findViewById(R.id.textOptional);
		if (listOptional.get(selectedList) == null || listOptional.get(selectedList).length() == 0) {
			textOptional.setText("none");
		} else {
			textOptional.setText(listOptional.get(selectedList));
		}
		TextView buttonCancel = (TextView) view.findViewById(R.id.buttonCancel);
		buttonCancel.setVisibility(View.GONE);
		
		showMessage();
		showRating();
		showFriends();
	}

	private void showMessage() {
		TextView buttonNewest = (TextView)view.findViewById(R.id.buttonNewest);
		buttonNewest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!newestMode) {
					newestMode = true;
					showMessage();
				}
			}
		});
		TextView buttonUnread = (TextView)view.findViewById(R.id.buttonUnread);
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
		LinearLayout layMessage = (LinearLayout)view.findViewById(R.id.listMessage);
		layMessage.setVisibility(View.VISIBLE);
		layMessage.removeAllViews();
		
		LinearLayout layMessageContent = (LinearLayout)view.findViewById(R.id.layMessageContent);
		layMessageContent.setVisibility(View.GONE);
		
		if (listMessage != null && listMessage.size() > 0) {
			for (int i = 0; i < listMessage.size(); i++) {
				LinearLayout message = new LinearLayout(getActivity());
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
				
				TextView textFrom = new TextView(getActivity());
				params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
				params.weight = 1;
				textFrom.setLayoutParams(params);
				textFrom.setPadding(14, 4, 0, 4);
				
				TextView textDate = new TextView(getActivity());
				params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
				params.weight = (float)0.5;
				textDate.setLayoutParams(params);
				textDate.setPadding(0, 4, 0, 4);
				
				TextView textTime = new TextView(getActivity());
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
		LinearLayout layMessageContent = (LinearLayout)view.findViewById(R.id.layMessageContent);
		layMessageContent.setVisibility(View.VISIBLE);
		TextView textMessageContent = (TextView)view.findViewById(R.id.textMessageContent);
		textMessageContent.setText(listMessage.get(selectedMessage).getMessage());
		LinearLayout layMessage = (LinearLayout)view.findViewById(R.id.listMessage);
		layMessage.setVisibility(View.GONE);
		Button buttonOK = (Button)view.findViewById(R.id.buttonOk);
		buttonOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				markMessageAsRead(selectedMessage);
				showMessage();
			}
		});
		
		
		TextView textFrom = (TextView)view.findViewById(R.id.selectedTittle);
		TextView textDate = (TextView)view.findViewById(R.id.selectedDate);
		TextView textTime = (TextView)view.findViewById(R.id.selectedTime);
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

	private void showRating() {
		final RatingBar rate = (RatingBar)view.findViewById(R.id.rateJob);
		if (listRating != null && listRating.size() > selectedList) {
			rate.setRating(listRating.get(selectedList));
		} else {
			rate.setRating(0);
		}
		rate.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {
				if (listRating != null && listRating.size() > selectedList) {
					rate.setRating(listRating.get(selectedList));
				} else {
					rate.setRating(0);
				}
			}
		});
		rate.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (arg1.getAction() == MotionEvent.ACTION_UP) {
					if (listRating != null && listRating.size() > selectedList) {
						rate.setRating(listRating.get(selectedList));
					} else {
						rate.setRating(0);
					}
					showRatingDialog();
				}
				return false;
			}
		});
	}

	private void showRatingDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());

		// set title
		builder.setTitle(getString(R.string.app_name));
		// set dialog message
		builder.setMessage(getString(R.string.rate_this_job));
		
		final RatingBar rating = new RatingBar(getActivity());
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rating.setNumStars(5);
		rating.setStepSize(0.1f);
		rating.setLayoutParams(params);
		if (listRating != null && listRating.size() > selectedList) {
			rating.setRating(listRating.get(selectedList));
		} else {
			rating.setRating(0);
		}
		
		LinearLayout parent = new LinearLayout(getActivity());
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
		if (listRating != null && listRating.size() > selectedList) {
			listRating.set(selectedList, f);
			showRating();
			
			// TODO submit rating here !
			final String url = CommonUtilities.SERVERURL + CommonUtilities.API_SET_RATING;
			new Thread() {
				public void run() {
					jsonParser = new JSONParser();
					String[] params = { "ptid", "avail_id", "rating" };
					String[] values = { pt_id, listAvailID.get(selectedList),  Float.toString(f)};
					jsonStr = jsonParser.getHttpResultUrlPut(url, params, values);
				}
			}.start();
		}
	}

	private void showFriends() {
		LinearLayout layFriend = (LinearLayout)view.findViewById(R.id.layFriend);
		layFriend.removeAllViews();
		if (listFriend != null && listFriend.size() > 0) {
			LinearLayout layRow = new LinearLayout(getActivity());
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layRow.setLayoutParams(params);
			layRow.setGravity(Gravity.CENTER_HORIZONTAL);
			layRow.setOrientation(LinearLayout.HORIZONTAL);
			int rowNum = ApplicationUtils.getColumnNumber(getActivity(), 64);
			for (int i = 0; i < listFriend.size(); i++) {
				layRow.addView(createImageView(listFriend.get(i)));
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
									objs = objs.getJSONObject("friends");
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
				
				loadMessage();
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

	private void loadRating() {
		// TODO Auto-generated method stub
		listRating = new ArrayList<Float>();
		listRating.add(3f);
		listRating.add(2f);
		listRating.add(4f);
		listRating.add(5f);
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				manageTab();
			}
		};

		new Thread() {
			public void run() {
				for (String id : listAvailID) {
					String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_RATING 
							+"?" + CommonUtilities.PARAM_AVAIL_ID + "=" + id;

					jsonParser = new JSONParser();
					jsonStr = jsonParser.getHttpResultUrlGet(url);
					if (jsonStr != null) {
						try {
							JSONObject obj = new JSONObject(jsonStr);
							String rating = jsonParser.getString(obj, "rating");
							listRating.add(Float.parseFloat(rating));
						} catch (Exception e) {
							Log.e(CommonUtilities.TAG, ">>> " + e.getMessage());
						}
					}
				}

				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
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
				
				loadRating();
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

	protected void manageTab() {
		if (modeList) {
			tabList.setBackgroundResource(R.drawable.bg_button_tab);
			tabList.setTextColor(Color.WHITE);
			tabLocation.setBackgroundResource(R.drawable.bg_button_tab_def);
			tabLocation.setTextColor(Color.BLACK);
			listview.setVisibility(View.VISIBLE);
			scrollView.setVisibility(View.GONE);
		} else {
			tabLocation.setBackgroundResource(R.drawable.bg_button_tab);
			tabLocation.setTextColor(Color.WHITE);
			tabList.setBackgroundResource(R.drawable.bg_button_tab_def);
			tabList.setTextColor(Color.BLACK);
			listview.setVisibility(View.GONE);
			scrollView.setVisibility(View.VISIBLE);
			if (selectedList == -1) {
				jobDetail.setVisibility(View.GONE);
			} else {
				jobDetail.setVisibility(View.VISIBLE);
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
				
				if(isVerified == "false") {
					ValidationUtilities.resendLinkDialog(getActivity(), pt_id);
				} else if (isProfileComplete == false) {						
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
					i.putExtra("optional", listOptional.get(selectedList));
					if (listRequirement != null && listRequirement.get(selectedList) != null
							&& !listRequirement.get(selectedList).equalsIgnoreCase("null")
							&& listRequirement.get(selectedList).trim().length() > 1) {
						i.putExtra("requirement", listRequirement.get(selectedList).split("\n"));
					}
					startActivityForResult(i, RC_ACCEPT);					
				}
			}
		});
	}

	private Calendar generateCalendar(String str) {
		Calendar calRes = new GregorianCalendar(Integer.parseInt(str.substring(
				0, 4)), Integer.parseInt(str.substring(5, 7)) - 1,
				Integer.parseInt(str.substring(8, 10)), Integer.parseInt(str
						.substring(11, 13)), Integer.parseInt(str.substring(14,
						16)), Integer.parseInt(str.substring(17, 19)));

		return calRes;
	}

	public static Bundle createBundle(String title) {
		Bundle bundle = new Bundle();
		bundle.putString(EXTRA_TITLE, title);
		return bundle;
	}
}
