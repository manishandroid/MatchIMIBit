package com.matchimi;

import static com.matchimi.CommonUtilities.LOGIN;
import static com.matchimi.CommonUtilities.SERVERURL;
import static com.matchimi.CommonUtilities.TAG;

import java.util.List;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.matchimi.utils.JSONParser;

public class MatchimiAlarmReceiver extends BroadcastReceiver {
	private String facebookFriends = "";
	private String pt_id;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private JSONParser jsonParser = null;
	private String jsonStr = null;
	private Context context;

	@Override
	public void onReceive(Context c, Intent intent) {
//		context = c;
//		settings = context.getSharedPreferences(CommonUtilities.PREFS_NAME,
//				Context.MODE_PRIVATE);
//		pt_id = settings.getString(CommonUtilities.USER_PTID, null);

		// TODO Auto-generated method stub
//		Log.d(CommonUtilities.TAG, "Matchimi Alarm Called");
//		Session.setActiveSession(appState.getState());
//		Session s = appState.getState();
//		
//		if (s != null && s.isOpened()) {
//			Request.executeMeRequestAsync(s, new Request.GraphUserCallback() {
//				@Override
//				public void onCompleted(GraphUser user, Response response) {
//					editor = settings.edit();
//					editor.putString(CommonUtilities.USER_FACEBOOK_ID,
//							user.getId());
//					editor.commit();
//
//					Log.e(CommonUtilities.TAG, "Facebook session indeed");
//					getFriends(user, settings, editor);
//				}
//			});
//		} else {
//			Log.e(CommonUtilities.TAG, "No Facebook Session");
//		}
	}

	private void getFriends(final GraphUser user,
			final SharedPreferences settings,
			final SharedPreferences.Editor editor) {
		Session activeSession = Session.getActiveSession();
		if (activeSession.getState().isOpened()) {
			Request friendRequest = Request.newMyFriendsRequest(activeSession,
					new Request.GraphUserListCallback() {
						@Override
						public void onCompleted(List<GraphUser> users,
								Response response) {

							for (GraphUser user : users) {
								// TODO Auto-generated method stub
								facebookFriends += user.getId() + ",";
							}

							// Remove last comma if any
							if (facebookFriends.length() > 0) {
								facebookFriends = facebookFriends.substring(0,
										facebookFriends.length() - 1);
							}

							Log.d(TAG, "Alarm Facebook friends "
									+ facebookFriends);
							submitFriendFacebook();
						}
					});

			Bundle params = new Bundle();
			params.putString("fields", "id, name, picture");
			friendRequest.setParameters(params);
			friendRequest.executeAsync();
		}
	}

	protected void submitFriendFacebook() {
		final String url = SERVERURL
				+ CommonUtilities.API_CREATE_PART_TIMER_FRIENDS_BY_PT_ID;

		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					// Running periodic task
					CharSequence text = "Friends already submitted!\n"
							+ jsonStr;
					int duration = Toast.LENGTH_LONG;
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				} else {

				}
			}
		};

		new Thread() {
			public void run() {
				jsonParser = new JSONParser();

				try {
					JSONObject parentData = new JSONObject();
					JSONObject childData = new JSONObject();

					childData.put("pt_id", pt_id);
					childData.put("friends", "[" + facebookFriends + "]");
					parentData.put("part_timer", childData);

					String[] params = { "data" };
					String[] values = { parentData.toString() };
					jsonStr = jsonParser.getHttpResultUrlPost(url, params,
							values);

					Log.e(TAG, "Post data" + childData.toString());
					Log.e(TAG, "Create friend list to " + url + "Result >>>\n "
							+ jsonStr);
				} catch (Exception e) {
					jsonStr = null;
					Log.e(TAG, ">> " + e.getMessage());
				}
				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}

}
