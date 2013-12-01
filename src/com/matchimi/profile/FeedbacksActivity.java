package com.matchimi.profile;

import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SETTING_THEME;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.THEME_LIGHT;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class FeedbacksActivity extends SherlockFragmentActivity {
	private Context context;
	private LinearLayout layFeedback;
	
	private JSONParser jsonParser = null;
	private String jsonStr = null;
	private ProgressDialog progress;
	private String pt_id = null;
	
	private List<String> listFeedbackTitle;
	private List<String> listFeedbackComment;
	private List<String> listFeedbackRating;
	
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
		
		pt_id = settings.getString(CommonUtilities.USER_PTID, null);
		context = this;
		
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		
		setContentView(R.layout.feedback_users);
		
		layFeedback = (LinearLayout) findViewById(R.id.layFeedback);
		loadFeedback();
	}
	
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
						NetworkUtils.connectionHandler(context, jsonStr, 
								e1.getMessage());
						
						Log.e(CommonUtilities.TAG, "Load feedback result " +
								jsonStr + " >> " + e1.getMessage());
					}
				} else {
					Toast.makeText(context.getApplicationContext(),
							getString(R.string.server_error), Toast.LENGTH_SHORT).show();
				}
				createLayout();
			}
		};

		progress = ProgressDialog.show(context, context.getString(R.string.app_name),
				context.getString(R.string.feedbacks_loading), true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);
//				mHandlerFeed.post(mUpdateResultsFeed);
				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
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
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
