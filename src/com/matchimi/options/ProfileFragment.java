package com.matchimi.options;


import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.USER_EMAIL;
import static com.matchimi.CommonUtilities.USER_FACEBOOK_ID;
import static com.matchimi.CommonUtilities.USER_FIRSTNAME;
import static com.matchimi.CommonUtilities.USER_LASTNAME;
import static com.matchimi.CommonUtilities.USER_NRIC;
import static com.matchimi.CommonUtilities.USER_RATING;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.matchimi.R;
import com.matchimi.registration.Utilities;

public class ProfileFragment extends Fragment {
	private static final String EXTRA_TITLE = "title";
	private TextView emailView;
	private TextView usernameView;
	private TextView nricView;
	private ImageView avatarView;
	private RatingBar ratingBar;
	private JSONObject partTimer;
	private Button blockedCompanyButton;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.user_profile, container, false);

		// Check if user not logged
		SharedPreferences settings = this.getActivity().getSharedPreferences(
				PREFS_NAME, 0);

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

		blockedCompanyButton = (Button) view.findViewById(R.id.profile_blocked_companies_button);
		blockedCompanyButton.setOnClickListener(blockedCompanyListener);
		
		final ListView listview = (ListView) view
				.findViewById(R.id.feedback_listview);

		String[] values = new String[] { "Jumbo, Clark Quay",
				"Jumbo, Clark Quay", "Iguana, Clark Quay" };

		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < values.length; ++i) {
			list.add(values[i]);
		}

		final FeedbackAdapter adapter = new FeedbackAdapter(getActivity(), list);
		listview.setAdapter(adapter);

		return view;

	}
	
	private OnClickListener blockedCompanyListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

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