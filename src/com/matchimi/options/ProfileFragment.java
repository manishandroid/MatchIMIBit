package com.matchimi.options;

import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.USER_EMAIL;
import static com.matchimi.CommonUtilities.USER_FACEBOOK_ID;
import static com.matchimi.CommonUtilities.USER_FIRSTNAME;
import static com.matchimi.CommonUtilities.USER_LASTNAME;
import static com.matchimi.CommonUtilities.USER_NRIC;
import static com.matchimi.CommonUtilities.USER_RATING;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.registration.Utilities;
import com.matchimi.utils.JSONParser;

public class ProfileFragment extends Fragment {

	public static final String EXTRA_TITLE = "title";

	private Context context;

	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private ProgressDialog progress;

	private LinearLayout layFeedback;
	private List<String> listFeedbackTitle;
	private List<String> listFeedbackComment;
	private List<String> listFeedbackRating;

	private String pt_id = null;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.user_profile, container, false);

		// Check if user not logged
		SharedPreferences settings = this.getActivity().getSharedPreferences(
				PREFS_NAME, 0);
		pt_id = settings.getString(CommonUtilities.USER_PTID, null);

		context = getActivity();

		// Set user name
		TextView usernameView = (TextView) view
				.findViewById(R.id.profile_username);
		String userName = settings.getString(USER_FIRSTNAME, "")
				+ settings.getString(USER_LASTNAME, "");
		usernameView.setText(userName);

		// Set email name
		TextView emailView = (TextView) view.findViewById(R.id.profile_email);
		String userEmail = settings.getString(USER_EMAIL, "");
		emailView.setText(userEmail);

		// Set nric
		TextView nricView = (TextView) view.findViewById(R.id.profile_nric);
		String userNRIC = settings.getString(USER_NRIC, "");
		nricView.setText(userNRIC);

		// Set user profile
		ImageView avatarView = (ImageView) view
				.findViewById(R.id.profile_avatar);

		Utilities util = new Utilities();
		util.downloadAvatar(settings.getString(USER_FACEBOOK_ID, ""),
				avatarView);

		RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
		ratingBar.setRating(settings.getInt(USER_RATING, 4));

		Button blockedCompanyButton = (Button) view
				.findViewById(R.id.profile_blocked_companies_button);
		blockedCompanyButton.setOnClickListener(blockedCompanyListener);
		layFeedback = (LinearLayout) view.findViewById(R.id.layFeedback);

		loadFeedback();

		return view;

	}

	private void loadFeedback() {
		final String url = "http://matchimi.buuukapps.com/get_feedbacks_by_pt_id?pt_id="
				+ pt_id;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				listFeedbackTitle = new ArrayList<String>();
				listFeedbackComment = new ArrayList<String>();
				listFeedbackRating = new ArrayList<String>();

				if (jsonStr != null) {
					try {
						Log.e("Feedback", ">>> " + jsonStr);
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
							Log.e("Parse Json Object", ">> Array is null");
						}
					} catch (JSONException e1) {
						Log.e("loadFeedback", ">> " + e1.getMessage());
					}
				} else {
					Toast.makeText(context, "jsonObj is Null",
							Toast.LENGTH_SHORT).show();
				}
				createLayout();
			}
		};

		progress = ProgressDialog.show(context, "Profile",
				"Loading feedback...", true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);
				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

	protected void createLayout() {
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

}
