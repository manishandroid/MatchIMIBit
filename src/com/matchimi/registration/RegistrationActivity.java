package com.matchimi.registration;

import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import static com.matchimi.CommonUtilities.*;

import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class RegistrationActivity extends Activity {
	private Context context;

	private ProgressDialog progress;
	private JSONParser jsonParser = null;
	private String jsonStr = null;
	private Bundle extraBundle;

	private LoginButton fbLoginButton;
	private Button submitButton;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			getUserInfo();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.registration_page);

		context = this;

		fbLoginButton = (LoginButton) findViewById(R.id.login_button);
		fbLoginButton.setReadPermissions(Arrays.asList("email",
				"user_about_me", "user_birthday", "user_education_history",
				"user_location", "user_work_history"));
		fbLoginButton.setSessionStatusCallback(callback);

		final EditText emailText = (EditText) findViewById(R.id.registration_email);
		final EditText passwordText = (EditText) findViewById(R.id.registration_password);

		submitButton = (Button) findViewById(R.id.submit_button);
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Email validation
				boolean validEmail = Validation.isValidEmail(emailText);
				// Password validation
				boolean validPassword = Validation
						.isValidPassword(passwordText);
				if (validEmail && validPassword) {
					closeKeyboard();
					
					extraBundle = new Bundle();
					extraBundle.putString(USER_BIRTHDAY, "");
					extraBundle.putString(USER_FIRSTNAME, "");
					extraBundle.putString(USER_FACEBOOK_ID, "");
					extraBundle.putString(USER_LASTNAME, "");
					extraBundle.putString(USER_FIRSTNAME, "");
					extraBundle.putString(USER_GENDER, "");

					createLogin(emailText.getText().toString(), passwordText
							.getText().toString());
				}
			}
		});
	}
	
	private void closeKeyboard() {
		InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE); 

		inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
		                   InputMethodManager.HIDE_NOT_ALWAYS);
	}

	protected void getUserInfo() {
		// TODO Auto-generated method stub
		Session s = Session.getActiveSession();
		if (s != null && s.isOpened()) {
			Request.executeMeRequestAsync(s, new Request.GraphUserCallback() {
				@Override
				public void onCompleted(GraphUser user, Response response) {
					extraBundle = new Bundle();
					extraBundle.putString(USER_BIRTHDAY,
							user.getBirthday());
					extraBundle.putString(USER_FIRSTNAME,
							user.getFirstName());
					extraBundle.putString(USER_FACEBOOK_ID,
							user.getId());
					extraBundle.putString(USER_LASTNAME,
							user.getLastName());
					extraBundle.putString(USER_FIRSTNAME,
							user.getName());
					extraBundle.putString(USER_GENDER, user
							.getProperty("gender").toString());

					createLogin(user.getProperty("email").toString(),
							user.getId());
				}
			});
		} else {
			Log.e(TAG, "updateUI NULL");
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeSession();
	}

	private void closeSession() {
		Session s = Session.getActiveSession();
		if (s != null) {
			s.closeAndClearTokenInformation();
		}
	}
	
	protected void createLogin(final String email, final String password) {
		final String url = SERVERURL + API_CREATE_AND_PARTIMER_LOGIN;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					// FIXME: please check on server response
					if (jsonStr.trim().equalsIgnoreCase("1")) {
						closeSession();
						Toast.makeText(context,
								getString(R.string.registration_failed),
								Toast.LENGTH_LONG).show();
					} else if (jsonStr.trim().length() > 0) {
						String ptid = "";

						try {
							JSONObject obj = new JSONObject(jsonStr);
							JSONObject partTimer = obj
									.getJSONObject("part_timers");
							ptid = partTimer.getString("pt_id");

							extraBundle.putString(USER_PTID, ptid);
							Intent i = new Intent(context,
									ProfileRegistrationActivity.class);
							i.putExtras(extraBundle);
							startActivity(i);
							finish();

						} catch (JSONException e1) {						
							NetworkUtils.connectionHandler(context, jsonStr);
							
							Log.e(CommonUtilities.TAG, "Load register result " +
									jsonStr + " >> " + e1.getMessage());
						}
					}
				} else {
					closeSession();
					Toast.makeText(context,
							getString(R.string.server_error), Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context, "Register", "Registering...",
				true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				try {
					JSONObject parentData = new JSONObject();
					JSONObject childData = new JSONObject();
					childData.put("email", email);
					childData.put("password", password);
					parentData.put("part_timer", childData);

					String[] params = { "data" };
					String[] values = { parentData.toString() };
					jsonStr = jsonParser.getHttpResultUrlPost(url, params,
							values);

					Log.e(TAG, "Register to " + url + "\n\n Result >>> " + jsonStr);

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

}
