package com.matchimi.options;

import static com.matchimi.CommonUtilities.PARAM_PT_ID;
import static com.matchimi.CommonUtilities.SERVERURL;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.USER_FIRSTNAME;
import static com.matchimi.CommonUtilities.USER_LASTNAME;
import static com.matchimi.CommonUtilities.USER_NRIC_NUMBER;
import static com.matchimi.CommonUtilities.USER_NRIC_TYPE;
import static com.matchimi.CommonUtilities.USER_NRIC_TYPE_ID;
import static com.matchimi.CommonUtilities.USER_PROFILE_PICTURE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.matchimi.CommonUtilities;
import com.matchimi.ProfileModel;
import com.matchimi.R;
import com.matchimi.registration.EditProfile;
import com.matchimi.registration.Utilities;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class ProfileFragment extends Fragment {

	public static final int RC_EDIT_PROFILE = 90;
	public static final String EXTRA_TITLE = "title";

	private Context context;

	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private ScrollView scrollView;
	private ProgressBar progressBar;

	private SharedPreferences settings;
	private LinearLayout layFeedback;
	private View view;
	
	private List<String> listFeedbackTitle;
	private List<String> listFeedbackComment;
	private List<String> listFeedbackRating;

	private String pt_id = null;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.user_profile, container, false);

		// Check if user not logged
		settings = getActivity().getSharedPreferences(CommonUtilities.PREFS_NAME, Context.MODE_PRIVATE);
		pt_id = settings.getString(CommonUtilities.USER_PTID, null);

		context = getActivity();

		progressBar = (ProgressBar) view.findViewById(R.id.progress);
		scrollView = (ScrollView)view.findViewById(R.id.scrollView);
		
		Button blockedCompanyButton = (Button) view
				.findViewById(R.id.profile_blocked_companies_button);
		blockedCompanyButton.setOnClickListener(blockedCompanyListener);
		layFeedback = (LinearLayout) view.findViewById(R.id.layFeedback);

		Button editProfile = (Button)view.findViewById(R.id.editProfile);
		editProfile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getActivity(), EditProfile.class);
				startActivityForResult(i, RC_EDIT_PROFILE);
			}
		});
		
		loadProfile();
		getActivity().registerReceiver(profileReceiver, new IntentFilter("profile.receiver"));

		return view;
	}

	private void loadProfile() {
		final String url = SERVERURL + CommonUtilities.API_GET_PROFILE + "?"
				+ PARAM_PT_ID + "=" + pt_id;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null || jsonStr.length() > 0) {
					try {
						JSONObject obj = new JSONObject(jsonStr);
						obj = obj.getJSONObject("part_timers");

						SharedPreferences.Editor editor = settings.edit();
						
						// Update settings
						editor.putString(USER_FIRSTNAME, obj.getString("first_name"));
						editor.putString(USER_LASTNAME, obj.getString("last_name"));
						editor.putString(USER_PROFILE_PICTURE, obj.getString("profile_picture"));
						String studentMatricCard = obj.optString("matric_card_no");
						if (studentMatricCard != null) {
							editor.putString(USER_NRIC_NUMBER, studentMatricCard);
						}
						editor.putString(CommonUtilities.USER_EMAIL, obj.getString("email"));
						editor.putInt(CommonUtilities.USER_RATING, obj.getInt("pt_grade_id"));
						editor.commit();
						
					} catch (Exception e) {
						Log.e(TAG, "Get profile error >> " + e.getMessage());
						Toast.makeText(context,
								getString(R.string.edit_get_profile_error),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(context,
							getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
				
				String picName = settings.getString(USER_PROFILE_PICTURE, null);
				Log.d(TAG, "picture: " + picName);
				
				if(picName != null && picName != getResources().getString(R.string.image_not_found)) {
					String url = SERVERURL + CommonUtilities.API_GET_PROFILE_PIC + "?"
							+ PARAM_PT_ID + "=" + pt_id;
					checkAndDownloadPic(picName, url);
				}
				
				loadData();
			}
		};

		progressBar.setVisibility(View.VISIBLE);
		scrollView.setVisibility(View.GONE);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);
				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}
	
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
		
		return CommonUtilities.IMAGE_ROOT + filename;
	}

	private class downloadWebPage extends AsyncTask<String, Void, Void> {

	    @Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Log.d(TAG, "downloading profile picture");
			loadData();
		}

		@Override
	    protected Void doInBackground(String... params) {
	        try {
	            HttpClient httpClient = new DefaultHttpClient();
	            HttpGet httpGet = new HttpGet(params[0]);
	            HttpResponse response;
	            response = httpClient.execute(httpGet);
	            HttpEntity entity = response.getEntity();
	            InputStream is = entity.getContent();
	            
				String picName = settings.getString(USER_PROFILE_PICTURE, null);
				int idx = picName.lastIndexOf("/");
				picName = picName.substring(idx + 1);
				File f = new File(CommonUtilities.IMAGE_ROOT, picName);

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
	        return null;
	    }

	}
		
	protected void loadPicture() {
		final String link = SERVERURL + CommonUtilities.API_GET_PROFILE_PIC + "?"
				+ PARAM_PT_ID + "=" + pt_id;
		downloadWebPage download = new downloadWebPage();
		download.execute(link);
	}

	BroadcastReceiver profileReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			loadProfile();
		}
	};
	
	private void loadFeedback() {
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_FEEDBACKS_BY_PT_ID 
				+ "?" + CommonUtilities.PARAM_PT_ID + "=" + pt_id;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				listFeedbackTitle = new ArrayList<String>();
				listFeedbackComment = new ArrayList<String>();
				listFeedbackRating = new ArrayList<String>();

				if (jsonStr != null) {
					try {
						Log.e(CommonUtilities.TAG, "Feedback result >>> " + jsonStr);
						JSONArray items = new JSONArray(jsonStr);
						if (items != null && items.length() > 0) {
							for (int i = 0; i < items.length(); i++) {
								JSONObject objs = items.getJSONObject(i);
								objs = objs.getJSONObject("availabilities");
								listFeedbackTitle.add(jsonParser.getString(
										objs, "branch_name")
										+ ", "
										+ jsonParser.getString(objs,
												"company_name"));
								listFeedbackComment.add(jsonParser.getString(
										objs, "feedback"));
								listFeedbackRating.add(jsonParser.getString(
										objs, "grade"));
							}
						} else {
							Log.e(TAG, ">> Array is null");
						}
					} catch (JSONException e1) {						
						NetworkUtils.connectionHandler(getActivity(), jsonStr, 
								e1.getMessage());
						
						Log.e(CommonUtilities.TAG, "Load feedback result " +
								jsonStr + " >> " + e1.getMessage());
					}
				} else {
					Toast.makeText(getActivity().getApplicationContext(),
							getString(R.string.server_error), Toast.LENGTH_SHORT).show();
				}
				createLayout();
				progressBar.setVisibility(View.GONE);
				scrollView.setVisibility(View.VISIBLE);
			}
		};

//		progress = ProgressDialog.show(context, context.getString(R.string.app_name),
//				"Loading...", true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);
				mHandlerFeed.post(mUpdateResultsFeed);
//				if (progress != null && progress.isShowing()) {
//					progress.dismiss();
//					mHandlerFeed.post(mUpdateResultsFeed);
//				}
			}
		}.start();
	}

	protected void createLayout() {
		layFeedback.removeAllViews();
		if (listFeedbackTitle != null && listFeedbackTitle.size() > 0) {
			for (int i = 0; i < listFeedbackTitle.size(); i++) {
				LayoutInflater li = LayoutInflater.from(context);
				View v = li.inflate(R.layout.feedback_list, null);
				TextView textTitle = (TextView) v
						.findViewById(R.id.feedback_company_name);
				textTitle.setText(listFeedbackTitle.get(i));
				TextView textCommand = (TextView) v
						.findViewById(R.id.feedback_comment);
				textCommand.setText(listFeedbackComment.get(i));
				RatingBar rateGrade = (RatingBar) v
						.findViewById(R.id.feedback_grade);
				rateGrade
						.setRating(Float.parseFloat(listFeedbackRating.get(i)));

				layFeedback.addView(v);
			}
		}
	}

	private OnClickListener blockedCompanyListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(),
					BlockedCompaniesActivity.class);
			startActivity(intent);
		}
	};

	public static Bundle createBundle(String title) {
		Bundle bundle = new Bundle();
		bundle.putString(EXTRA_TITLE, title);
		return bundle;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		getActivity();
		if (resultCode == FragmentActivity.RESULT_OK) {
			if (requestCode == RC_EDIT_PROFILE) {
				loadData();
			}
		}
	}

	private void loadData() {
		// Set user name
		TextView usernameView = (TextView) view
				.findViewById(R.id.profile_username);
		String userName = settings.getString(CommonUtilities.USER_FIRSTNAME, "")
				+ " " + settings.getString(CommonUtilities.USER_LASTNAME, "");
		usernameView.setText(userName);

		// Set email name
		TextView emailView = (TextView) view.findViewById(R.id.profile_email);
		String userEmail = settings.getString(CommonUtilities.USER_EMAIL, "");
		emailView.setText(userEmail);

		// Set nric
		TextView nricView = (TextView) view.findViewById(R.id.profile_nric);
		String userNRIC = settings.getString(CommonUtilities.USER_NRIC_NUMBER, "");
		if(userNRIC == "") {
			nricView.setVisibility(View.GONE);
		} else {
			nricView.setText(userNRIC);
			nricView.setVisibility(View.VISIBLE);
		}

		// Set user profile
		ImageView avatarView = (ImageView) view
				.findViewById(R.id.profile_avatar);

		String picName = settings.getString(USER_PROFILE_PICTURE, null);
		int idx = picName.lastIndexOf("/");
		picName = picName.substring(idx + 1);
		File f = new File(CommonUtilities.IMAGE_ROOT, picName);
		
		String facebookID = settings.getString(CommonUtilities.USER_FACEBOOK_ID, "");
		String profilePic = settings.getString(CommonUtilities.USER_PROFILE_PICTURE, "");
		
		if(profilePic.length() > 0) {
			if (f.exists()) {
				Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());
				avatarView.setImageBitmap(b);
			}	
		} else {
			if(facebookID.length() > 0) {
				Utilities util = new Utilities();
				util.downloadAvatar(settings.getString(CommonUtilities.USER_FACEBOOK_ID, ""),
						avatarView);				
			}
		}

		RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
		ratingBar.setRating(settings.getInt(CommonUtilities.USER_RATING, 0));
		
		loadFeedback();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(profileReceiver);
	}

}
