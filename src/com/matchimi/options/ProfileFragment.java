package com.matchimi.options;


import static com.matchimi.CommonUtilities.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.matchimi.R;
import com.matchimi.registration.Utilities;
import com.matchimi.utils.JSONHelper;

public class ProfileFragment extends Fragment {
	private static final String EXTRA_TITLE = "title";
	private TextView emailView;
	private TextView usernameView;
	private TextView nricView;
	private ImageView avatarView;
	private RatingBar ratingBar;
	private JSONObject partTimer;
	private Button blockedCompanyButton;
	private static RequestQueue volleyQueue;
	private ListView listview;
	private List feedbackData;
	
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
		
		listview = (ListView) view.findViewById(R.id.feedback_listview);

		volleyQueue = Volley.newRequestQueue(getActivity());		
		String URL = SERVERURL + API_GET_FEEDBACKS_BY_PT_ID + "?" + PARAM_PT_ID + "=" + API_PT_ID;
		
		volleyQueue.add(new JsonArrayRequest(URL, new Listener<JSONArray>() {
		    @Override
		    public void onResponse(JSONArray response) {
		    	
				try {
					feedbackData = JSONHelper.toList(response);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			addAdapter(feedbackData);
		    	
		        //SUCCESS
		    }}, new ErrorListener() {

			    @Override
			    public void onErrorResponse(VolleyError error) {
			        //ERROR
		    }}));		
	
		
		return view;

	}
	
	private void addAdapter(List feedbackData) {
		// TODO Auto-generated method stub
		final FeedbackAdapter adapter = new FeedbackAdapter(getActivity(), feedbackData);
		listview.setAdapter(adapter);
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