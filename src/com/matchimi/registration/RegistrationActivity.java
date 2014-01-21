package com.matchimi.registration;

import java.io.IOException;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import static com.matchimi.CommonUtilities.*;

import com.matchimi.CommonUtilities;
import com.matchimi.DatabaseStorage;
import com.matchimi.R;
import com.matchimi.ServerUtilities;
import com.matchimi.ValidationUtilities;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class RegistrationActivity extends Activity {
	private Context context;

	private ProgressDialog progress;
	private JSONParser jsonParser = null;
	private String jsonStr = null;
	private Bundle extraBundle;
	private String registrationID;
	private DatabaseStorage db;
	AsyncTask<Void, Void, Void> registerTask;
	private String pt_id;
	GoogleCloudMessaging gcm;

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

		// Register user devices with GCM server
		// Checking server configuration
		checkNotNull(CommonUtilities.GCM_SERVER_URL, "SERVER_URL");
		checkNotNull(CommonUtilities.SENDER_ID, "SENDER_ID");

		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);

		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(this);

		db = new DatabaseStorage();
		registrationID = GCMRegistrar.getRegistrationId(context);
		gcm = GoogleCloudMessaging.getInstance(this);

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
					extraBundle.putString(USER_FACEBOOK_ID, "");
					extraBundle.putString(USER_FULLNAME, "");
					extraBundle.putString(USER_GENDER, "");

					createLogin(emailText.getText().toString(), passwordText
							.getText().toString(), false);
				}
			}
		});
	}

	private void closeKeyboard() {
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		inputManager.hideSoftInputFromWindow(
				getCurrentFocus().getWindowToken(),
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
					extraBundle.putString(USER_BIRTHDAY, user.getBirthday());
					extraBundle.putString(USER_FULLNAME, user.getFirstName()
							+ " " + user.getLastName());
					extraBundle.putString(USER_FACEBOOK_ID, user.getId());
					extraBundle.putString(USER_GENDER,
							user.getProperty("gender").toString());

					Log.d(CommonUtilities.TAG,
							"Facebook DOB " + user.getBirthday()
									+ " and Fullname " + user.getFirstName()
									+ " " + user.getLastName());

					createLogin(user.getProperty("email").toString(),
							user.getId(), true);
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

	protected void createLogin(final String email, final String password,
			final boolean isFacebook) {

		final String url;

		if (isFacebook) {
			url = SERVERURL + API_CREATE_AND_PART_TIMER_FB_LOGIN;
		} else {
			url = SERVERURL + API_CREATE_AND_PARTIMER_LOGIN;
		}

		final Handler mHandlerFeed = new Handler();

		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					Log.d(CommonUtilities.TAG, "Result " + jsonStr);

					// FIXME: please check on server response
					if (jsonStr.trim().equalsIgnoreCase("1")) {
						closeSession();
						Toast.makeText(context,
								getString(R.string.registration_failed),
								Toast.LENGTH_LONG).show();
					} else if (jsonStr.trim().equalsIgnoreCase("2")) {
						closeSession();
						Toast.makeText(context,
								getString(R.string.registration_already_exist),
								Toast.LENGTH_LONG).show();
					} else if (jsonStr.trim().length() > 0) {

						try {
							JSONObject obj = new JSONObject(jsonStr);
							JSONObject partTimer = obj
									.getJSONObject("part_timers");
							pt_id = partTimer.getString("pt_id");

							SharedPreferences settings = getSharedPreferences(
									PREFS_NAME, Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = settings.edit();
							editor.putString(USER_PTID, pt_id);
							editor.putString(USER_EMAIL,
									partTimer.getString(PARAM_PROFILE_EMAIL));
							editor.commit();
							extraBundle.putString(USER_PTID, pt_id);

							Log.d(CommonUtilities.TAG, "User with PT ID "
									+ pt_id + " has been created");

							if (registrationID.length() == 0) {
								Log.d(CommonUtilities.TAG, "Register background executed");
					        } else {
					        	Log.d(CommonUtilities.TAG, "User devices already registered " + registrationID);
					        }
							
							registerBackground();
							
							Intent i = new Intent(context,
									ProfileRegistrationActivity.class);
							i.putExtras(extraBundle);
							startActivity(i);
							finish();

						} catch (JSONException e1) {
							NetworkUtils.connectionHandler(context, jsonStr,
									e1.getMessage());
						}
					} else {
						closeSession();
						Toast.makeText(context,
								getString(R.string.server_error),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					closeSession();
					Toast.makeText(context,
							getString(R.string.something_wrong),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context, getString(R.string.register),
				getString(R.string.register_progress), true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				try {
					JSONObject parentData = new JSONObject();
					JSONObject childData = new JSONObject();

					childData.put("email", email);
					if (isFacebook) {
						childData.put(COMMON_FACEBOOK_ID, password);
					} else {
						childData.put(COMMON_PASSWORD, password);
					}

					parentData.put("part_timer", childData);

					String[] params = { "data" };
					String[] values = { parentData.toString() };
					jsonStr = jsonParser.getHttpResultUrlPost(url, params,
							values);

					Log.e(TAG, "Result >>> " + jsonStr);

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

	/**
    * Registers the application with GCM servers asynchronously.
    * <p>
    * Stores the registration id, app versionCode, and expiration time in the application's
    * shared preferences.
    */
   private void registerBackground() {
       new AsyncTask<Void, Void, String>() {
           @Override
           protected String doInBackground(Void... params) {
               String msg = "";
               try {
                   if (gcm == null) {
                       gcm = GoogleCloudMessaging.getInstance(context);
                   }
                   
                   Log.d(CommonUtilities.TAG, "Register sender ID in Registration Activity");
                   
                   registrationID = gcm.register(SENDER_ID);
                   msg = "";

                   // You should send the registration ID to your server over HTTP, so it
                   // can use GCM/HTTP or CCS to send messages to your app.

                   // For this demo: we don't need to send it because the device will send
                   // upstream messages to a server that echo back the message using the
                   // 'from' address in the message.
                   registerDevices();

                   // Save the regid - no need to register again.
               } catch (IOException ex) {
                   msg = "Error register Backround : " + ex.getMessage();
               }
               return msg;
           }

           @Override
           protected void onPostExecute(String msg) {
              Log.d(CommonUtilities.TAG, msg + "\n");
           }
       }.execute(null, null, null);
   }
    
	/**
	 * Register user android devices key to server
	 */
	private void registerDevices() {
		Log.d(CommonUtilities.TAG, "Registration ID " + registrationID + " and PT_ID : " + pt_id + " to API SERVER");

		// Try to register again, but not in the UI thread.
		// It's also necessary to cancel the thread onDestroy(),
		// hence the use of AsyncTask instead of a raw thread.
		registerTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				boolean registered = ServerUtilities.registerPartimer(
						context, registrationID, pt_id);
				// At this point all attempts to register with the app
				// server failed, so we need to unregister the device
				// from GCM - the app will try to register again when
				// it is restarted. Note that GCM will send an
				// unregistered callback upon completion, but
				// GCMIntentService.onUnregistered() will ignore it.
				if (!registered) {
					GCMRegistrar.unregister(context);
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				registerTask = null;
			}
		};

		registerTask.execute(null, null, null);
	}

	private void checkNotNull(Object reference, String name) {
		if (reference == null) {
			throw new NullPointerException(getString(R.string.error_config,
					name));
		}
	}

	public void popUp(String message) {
		// Skips registration.
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(this, message, duration);
		toast.show();
	}

}
