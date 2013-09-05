package com.matchimi.registration;

import static com.matchimi.CommonUtilities.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.matchimi.R;

public class RegistrationActivity extends Activity {
	private LoginButton fbLoginButton;
	private GraphUser user;
	private UiLifecycleHelper uiHelper;
	private Button submitButton;
	private String userEmail;
	private static RequestQueue volleyQueue;
	private SharedPreferences settings;
	
	private static final List<String> PERMISSIONS = Arrays
			.asList("publish_actions");
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Remove title bar
		// this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		setContentView(R.layout.registration_page);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		settings = getSharedPreferences(PREFS_NAME, 0);

		if (extras != null) {
			String logout = extras.getString(LOGOUT);
			if(logout == "true") {
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(LOGGED, "");
				editor.commit();
			}
			
		}
		
		volleyQueue = Volley.newRequestQueue(this);
		
		String URL = SERVERURL + API_CREATE_PARTIMER_LOGIN;		
		
		Map<String, Map> dataParams = new HashMap<String, Map>();
		Map<String, Map> partTimerWrap = new HashMap<String, Map>();
		Map<String, String> partTimerParams = new HashMap<String, String>();
		
		partTimerParams.put(PARAM_PROFILE_EMAIL, "123@matchimi.com");
		partTimerParams.put(PARAM_PROFILE_IC_NUMBER, "G7444132N");
		partTimerParams.put(PARAM_PROFILE_USER_PASSWORD, "123");
		
		partTimerWrap.put(PARAM_PART_TIMER, partTimerParams);
		dataParams.put(PARAM_DATA, partTimerWrap);
		
		JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(dataParams),
		       new com.android.volley.Response.Listener<JSONObject>() {
		           @Override
		           public void onResponse(JSONObject response) {
		               Log.d(TAG, response.toString());
		           }
		       }, new com.android.volley.Response.ErrorListener() {
		           @Override
		           public void onErrorResponse(VolleyError error) {
		        	   Log.d(TAG, error.toString());
		           }
		       });

		volleyQueue.add(req);
		
		
		// Intent intent = new Intent(RegistrationActivity.this,
		// ProfileRegistrationActivity.class);
		// intent.putExtra(LOGGED, LOGGED_REGISTER);
		// intent.putExtra(USER_EMAIL, "yodi@gmail.com");
		// intent.putExtra(USER_PASSWORD, "helloworld");
		// intent.putExtra(USER_FIRSTNAME, "Polatic");
		// intent.putExtra(USER_LASTNAME, "Developer");
		//
		// startActivity(intent);
		// finish();

		fbLoginButton = (LoginButton) findViewById(R.id.login_button);
		fbLoginButton.setReadPermissions(Arrays.asList("email",
				"user_about_me", "user_birthday", "user_education_history",
				"user_location", "user_work_history"));

		fbLoginButton
				.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
					@Override
					public void onUserInfoFetched(GraphUser user) {
						RegistrationActivity.this.user = user;
						harvestingUser(user);
						updateUI();
					}
				});

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
					
					Intent intent = new Intent(RegistrationActivity.this,
							ProfileRegistrationActivity.class);
					intent.putExtra(LOGGED, LOGGED_REGISTER);
					intent.putExtra(USER_EMAIL, emailText.getText().toString());
					intent.putExtra(USER_PASSWORD, passwordText.getText()
							.toString());

					startActivity(intent);
					finish();
				}
			}
		});
	}
	
	private Listener<JSONObject> createMyReqSuccessListener() {
		return new com.android.volley.Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				Log.d(TAG, "Response : " + response.toString());
			}
		};
	}


	private com.android.volley.Response.ErrorListener createMyReqErrorListener() {
		return new com.android.volley.Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, "ERROR MESSAGE" + error.toString());
			}
		};
	}

	public void harvestingUser(GraphUser user) {
		Session session = Session.getActiveSession();
		boolean enableButtons = (session != null && session.isOpened());

		if (enableButtons) {
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(USER_FIRSTNAME, user.getFirstName());
			editor.putString(USER_LASTNAME, user.getLastName());
			editor.putString(USER_FACEBOOK_ID, user.getId());
			editor.commit();

			Intent intent = new Intent(RegistrationActivity.this,
					ProfileRegistrationActivity.class);
			intent.putExtra(LOGGED, LOGGED_REGISTER);
			intent.putExtra(USER_BIRTHDAY, user.getBirthday());
			intent.putExtra(USER_FIRSTNAME, user.getFirstName());
			intent.putExtra(USER_LASTNAME, user.getLastName());
			intent.putExtra(USER_PASSWORD, user.getId());

			startActivity(intent);
			finish();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		updateUI();
	}

	private void updateUI() {
		Session session = Session.getActiveSession();
		boolean enableButtons = (session != null && session.isOpened());

		if (enableButtons && user != null) {
			Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

				@Override
				public void onCompleted(GraphUser user, Response response) {
					String firstName = user.getFirstName();
                    String lastName = user.getLastName();
                    String id = user.getId();
                    String email = user.getProperty("email").toString();

                    Log.e(TAG, id);
                    Log.e(TAG, firstName);
                    Log.e(TAG, lastName);
                    Log.e(TAG, email);	
				}
            });
		} else {

		}
	}

	private boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null
				&& session.getPermissions().contains("publish_actions");
	}

	private void showAlert(String title, String message) {
		new AlertDialog.Builder(this).setTitle(title).setMessage(message)
				.setPositiveButton(R.string.ok, null).show();
	}

	private void performPublish() {
		Session session = Session.getActiveSession();
		if (session != null) {
			// We need to get new permissions, then complete the action when
			// we get called back.
			session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
					this, PERMISSIONS));
		}
	}
}
