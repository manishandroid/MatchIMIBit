package com.matchimi.registration;
import static com.matchimi.CommonUtilities.COMMON_FACEBOOK_ID;
import static com.matchimi.CommonUtilities.COMMON_PASSWORD;
import static com.matchimi.CommonUtilities.LOGIN;
import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SERVERURL;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.USER_FIRSTNAME;
import static com.matchimi.CommonUtilities.USER_LASTNAME;
import static com.matchimi.CommonUtilities.USER_NRIC_NUMBER;
import static com.matchimi.CommonUtilities.USER_NRIC_TYPE;
import static com.matchimi.CommonUtilities.USER_NRIC_TYPE_ID;
import static com.matchimi.CommonUtilities.USER_PROFILE_PICTURE;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
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
import com.matchimi.CommonUtilities;
import com.matchimi.HomeActivity;
import com.matchimi.R;
import com.matchimi.ValidationUtilities;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class LoginActivity extends Activity {

	private Context context;

	private ProgressDialog progress;
	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private Bundle extraBundle;

	private LoginButton fbLoginButton;
	private Button submitButton;
	private Button registerButton;
	private String userIsVerified;
	private String userPhoneNumber;
	private String userWorkExperience;
	private String userGender;
	private String userDob;
	private String userNRICType;
	private String userNRICTypeID;
	private boolean userBasicComplete = false;
	private String pt_id;
	private String facebookFriends = "";
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			postLogin();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login_page);

		context = this;

		fbLoginButton = (LoginButton) findViewById(R.id.login_button);
		fbLoginButton.setReadPermissions(Arrays.asList("email",
				"user_about_me", "user_birthday", "user_education_history",
				"user_location", "user_work_history", "friends_birthday"));
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
//					closeKeyboard();

					extraBundle = new Bundle();
					extraBundle.putString(CommonUtilities.USER_BIRTHDAY, "");
					extraBundle.putString(CommonUtilities.USER_FIRSTNAME, "");
					extraBundle.putString(CommonUtilities.USER_LASTNAME, "");					
					extraBundle.putString(CommonUtilities.USER_FACEBOOK_ID, "");
					extraBundle.putString(CommonUtilities.USER_GENDER, "");
					extraBundle.putString(CommonUtilities.USER_IS_VERIFIED, "false");

					loginPartTimer(emailText.getText().toString(), passwordText
							.getText().toString(), false);
				}
			}
		});

		registerButton = (Button) findViewById(R.id.register_button);
		registerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(context, RegistrationActivity.class);
				startActivity(i);
				finish();
			}
		});

		Button forgetButton = (Button) findViewById(R.id.forgot_button);
		forgetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
//				closeKeyboard();
				showForgetDialog();
			}
		});
	}	
	
	private void closeKeyboard() {
		InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE); 

		inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
		                   InputMethodManager.HIDE_NOT_ALWAYS);
	}

	protected void showForgetDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(getString(R.string.login_forgot_password_title));
		builder.setMessage(getString(R.string.login_forgot_password_message));

		final EditText input = new EditText(context);
		input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		input.setHint(getString(R.string.hint_email_address));
		builder.setView(input);
		builder.setPositiveButton(getString(R.string.reset), null);
		builder.setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {

					}
				});

		final AlertDialog dialog = builder.create();
		dialog.show();
		Button positiveButton = dialog
				.getButton(DialogInterface.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View onClick) {
				if (Validation.isValidEmail(input)) {
					sendForgetRequest(input.getText().toString().trim());
					dialog.dismiss();
				} else {
					input.setError(getString(R.string.login_email_not_valid));
				}
			}
		});
	}

	protected void sendForgetRequest(final String email) {
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_FORGET_PART_TIMER_PASSWORD;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					closeKeyboard();

					if (jsonStr.trim().equalsIgnoreCase("1")) {
						Toast.makeText(context,
								getString(R.string.reset_password_failed),
								Toast.LENGTH_LONG).show();
					} else if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(
								context,
								getString(R.string.reset_password_success),
								Toast.LENGTH_LONG).show();
					} else if (jsonStr.trim().length() > 0 && !jsonStr.trim().equalsIgnoreCase("0")) {
						NetworkUtils.connectionHandler(context, jsonStr, "");
					}
				} else {
					Toast.makeText(context,
							getString(R.string.something_wrong),
							Toast.LENGTH_LONG).show();
				}
			}
		};

		progress = ProgressDialog.show(context,
				getString(R.string.app_name),
				getString(R.string.reset_password_progress), true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				try {
					String[] params = { "email" };
					String[] values = { email };
					jsonStr = jsonParser.getHttpResultUrlPost(url, params,
							values);

					Log.e(CommonUtilities.TAG, "Reset password " + url + " >>> " + jsonStr);
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

	protected void postLogin() {
		Session s = Session.getActiveSession();
		
		if (s != null && s.isOpened()) {
			Request.executeMeRequestAsync(s, new Request.GraphUserCallback() {
				@Override
				public void onCompleted(GraphUser user, Response response) {
					extraBundle = new Bundle();
					extraBundle.putString(CommonUtilities.USER_BIRTHDAY, user.getBirthday());
					extraBundle.putString(CommonUtilities.USER_FIRSTNAME, user.getFirstName());
					extraBundle.putString(CommonUtilities.USER_LASTNAME, user.getLastName());					
					extraBundle.putString(CommonUtilities.USER_FACEBOOK_ID, user.getId());
					extraBundle.putString(CommonUtilities.USER_GENDER, user.getProperty("gender").toString());

					Log.d(CommonUtilities.TAG, "User birthday from post login " + user.getBirthday());
					
					settings = getSharedPreferences(
							CommonUtilities.PREFS_NAME, Context.MODE_PRIVATE);
					editor = settings.edit();
					editor.putString(CommonUtilities.USER_FACEBOOK_ID, user.getId());
					editor.commit();
					
					getFriends(user, settings, editor);
					
					
				}
			});
		} else {
			Log.e(CommonUtilities.TAG, "updateUI NULL");
		}
	}
	
	private void getFriends(final GraphUser user, final SharedPreferences settings,
			final SharedPreferences.Editor editor) {
	    Session activeSession = Session.getActiveSession();
	    if(activeSession.getState().isOpened()){
	        Request friendRequest = Request.newMyFriendsRequest(activeSession, 
	            new Request.GraphUserListCallback(){
	                @Override
	                public void onCompleted(List<GraphUser> users,
	                        Response response) {
	                   
	                	for(GraphUser user : users) {
							// TODO Auto-generated method stub
							facebookFriends += user.getId() + ",";
						}
	                	
	                	// Remove last comma if any
	            		if (facebookFriends.length() > 0) {
	            			facebookFriends = facebookFriends.substring(0,
	            					facebookFriends.length() - 1);
	            		}
	            		
	            		Log.d(TAG, "Facebook friends " + facebookFriends);
	            		
	            		if(user.getProperty("email") == null) {						
							// Clear stored data
							settings.edit().clear().commit();
							editor.putBoolean(LOGIN, false);				
							editor.commit();
							
							Toast toast = Toast.makeText(context,
									getResources().getString(R.string.faceboook_email_failed), 
									Toast.LENGTH_LONG);
							toast.show();
							
							logout();
							
						} else {			
							loginPartTimer(user.getProperty("email").toString(),
									user.getId(), true);						
						}
	            		
	                }
	        });
	        
	        Bundle params = new Bundle();
	        params.putString("fields", "id, name, picture");
	        friendRequest.setParameters(params);
	        friendRequest.executeAsync();
	    }
	}
	
	protected void submitFriendFacebook() {
		final String url = SERVERURL + CommonUtilities.API_CREATE_PART_TIMER_FRIENDS_BY_PT_ID;
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						if (settings.getBoolean(CommonUtilities.REGISTERED, false)) {
							editor.putBoolean(CommonUtilities.LOGIN, true);
							editor.commit();
							
							// If user not verified, remind them
							if(userIsVerified == "false") {
								notifyUserVerification(extraBundle, editor, true);
							} else {
								goHome(extraBundle);
							}
							
						} else {
							// Check if user already completed basic profile
							if(userBasicComplete) {
								editor.putBoolean(CommonUtilities.LOGIN, true);
								editor.commit();
								
								// If user not verified, remind them
								if(userIsVerified == "false") {
									notifyUserVerification(extraBundle, editor, true);
								} else {
									goHome(extraBundle);
								}
							} else {
								editor.commit();
								if(userIsVerified == "false") {
									notifyUserVerification(extraBundle, editor, false);
								} else {
									goRegistrationProfile(extraBundle);								
								}
							}
						}	
					} else if (jsonStr.trim().equalsIgnoreCase("1")) {
						Toast.makeText(context,
								getString(R.string.edit_profile_error),
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(context,
							getString(R.string.something_wrong),
							Toast.LENGTH_LONG).show();
				}
			}
		};

		progress = ProgressDialog.show(context,
				getString(R.string.profile_header),
				getString(R.string.updating_profile), true, false);
		
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

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

	private void logout(){
	    // find the active session which can only be facebook in my app
	    Session session = Session.getActiveSession();
	    // run the closeAndClearTokenInformation which does the following
	    // DOCS : Closes the local in-memory Session object and clears any persistent 
	    // cache related to the Session.
	    session.closeAndClearTokenInformation();
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

	protected void loginPartTimer(final String email, final String password, final boolean isFacebook) {
		final String url;		
		
		if(isFacebook) {
			url = CommonUtilities.SERVERURL + CommonUtilities.API_LOGIN_FB_PART_TIMER;
		} else {
			url = CommonUtilities.SERVERURL + CommonUtilities.API_LOGIN_PART_TIMER;
		}

		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					Log.d(CommonUtilities.TAG, "Result from " + url + " >>>\n" + jsonStr.toString());
					
					// FIXME: please check on server response
					if (jsonStr.trim().equalsIgnoreCase("1")) {
						closeSession();
						Toast.makeText(context,
								getString(R.string.login_failed),
								Toast.LENGTH_LONG).show();
					} else if (jsonStr.trim().equalsIgnoreCase("2")) {
						closeSession();
						Toast.makeText(context,
								getString(R.string.something_wrong),
								Toast.LENGTH_LONG).show();
					} else if (jsonStr.trim().length() > 0){
						settings = getSharedPreferences(
								CommonUtilities.PREFS_NAME, Context.MODE_PRIVATE);
						editor = settings.edit();

						pt_id = "";
						
						try {
							JSONObject obj = new JSONObject(jsonStr);
							JSONObject partTimer = obj
									.getJSONObject("part_timers");
							pt_id = partTimer.getString(CommonUtilities.PARAM_PT_ID);
							
							userIsVerified = partTimer.getString(CommonUtilities.PARAM_PROFILE_IS_VERIFIED);
							userDob = partTimer.getString(CommonUtilities.PARAM_PROFILE_DATE_OF_BIRTH);
							userWorkExperience = partTimer.getString(CommonUtilities.PARAM_PROFILE_WORK_EXPERIENCE);
							userPhoneNumber = partTimer.getString(CommonUtilities.PARAM_PROFILE_PHONE_NUMBER);
							userGender = partTimer.getString(CommonUtilities.PARAM_PROFILE_GENDER);
							userNRICType = partTimer.getString(CommonUtilities.PARAM_PROFILE_IC_TYPE);
							userNRICTypeID = partTimer.getString(CommonUtilities.PARAM_PROFILE_IC_TYPE_ID);
							
							// Update settings
							String nricNumber = partTimer.getString(CommonUtilities.PARAM_PROFILE_IC_NUMBER);
							if(nricNumber == "null") {
								nricNumber = "";
							}
							
							editor.putString(CommonUtilities.USER_PTID, pt_id);
							editor.putString(CommonUtilities.USER_FIRSTNAME, 
									partTimer.getString(CommonUtilities.PARAM_PROFILE_FIRSTNAME));
							editor.putString(CommonUtilities.USER_LASTNAME, 
									partTimer.getString(CommonUtilities.PARAM_PROFILE_LASTNAME));
							editor.putString(CommonUtilities.USER_EMAIL, 
									partTimer.getString(CommonUtilities.PARAM_PROFILE_EMAIL));
							editor.putString(CommonUtilities.USER_NRIC_NUMBER, nricNumber);
							editor.putString(CommonUtilities.USER_PROFILE_PICTURE, 
									partTimer.getString(CommonUtilities.PARAM_PROFILE_PICTURE));
							editor.putString(CommonUtilities.USER_NRIC_TYPE, userNRICType);
							editor.putString(CommonUtilities.USER_NRIC_TYPE_ID, userNRICTypeID);
							editor.putString(CommonUtilities.USER_IS_VERIFIED, userIsVerified);
							editor.putInt(CommonUtilities.USER_RATING, 
									(int) partTimer.getInt(CommonUtilities.PARAM_PROFILE_GRADE_ID));
							
							// Check all fields in user 
							editor.putBoolean(CommonUtilities.USER_PROFILE_COMPLETE, 
									ValidationUtilities.checkProfileComplete(partTimer));							

							extraBundle.putString(CommonUtilities.USER_PTID, pt_id);
							extraBundle.putString(CommonUtilities.USER_EMAIL, email);
							extraBundle.putString(CommonUtilities.USER_IS_VERIFIED, userIsVerified);
							extraBundle.putString(CommonUtilities.USER_BIRTHDAY, userDob);
							extraBundle.putString(CommonUtilities.USER_GENDER, userGender);
							extraBundle.putString(CommonUtilities.USER_WORK_EXPERIENCE, userWorkExperience);
							extraBundle.putString(CommonUtilities.USER_PHONE_NUMBER, userPhoneNumber);
							extraBundle.putString(CommonUtilities.USER_NRIC_TYPE, userNRICType);
							extraBundle.putString(CommonUtilities.USER_NRIC_TYPE_ID, userNRICTypeID);	
							
							// Check if basic profile user already completed
							if(userGender != "null" && userDob != "null"
									&& userPhoneNumber != "null"
//									&& userWorkExperience != "null"
									&& userNRICType != "null") {
								userBasicComplete = true;		
								Log.d(CommonUtilities.TAG, " complete");
								
							} else {
								Log.d(CommonUtilities.TAG, "Not complete " + userNRICType + 
										" " + userGender + " " + userDob + " " + userPhoneNumber + " ");
							}
							
							submitFriendFacebook();
							
						} catch (JSONException e1) {			
							NetworkUtils.connectionHandler(context, jsonStr, e1.getMessage());
						}
					}
				} else {
					closeSession();
					Toast.makeText(context,
							getString(R.string.server_error), Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context, getString(R.string.login),
				getString(R.string.please_wait), true,
				false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				
				try {
					JSONObject parentData = new JSONObject();
					JSONObject childData = new JSONObject();
					
					childData.put("email", email);
					if(isFacebook) {
						childData.put(COMMON_FACEBOOK_ID, password);						
					} else {
						childData.put(COMMON_PASSWORD, password);						
					}
					
					parentData.put(CommonUtilities.COMMON_PART_TIMER, childData);

					String[] params = { CommonUtilities.COMMON_DATA };
					String[] values = { parentData.toString() };
					jsonStr = jsonParser.getHttpResultUrlPost(url, params,
							values);

					Log.e(CommonUtilities.TAG, "HTTPPOST to " + url + " with data\n " + childData.toString());
					
				} catch (Exception e) {
					Log.d(CommonUtilities.TAG, "Error HTTP POST " + e.toString());
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
	 * Showing email validation dialog if user not verify their account
	 * 
	 * @param extraBundle
	 * @param editor
	 * @param isCompleted
	 */
	private void notifyUserVerification(final Bundle extraBundle, 
			final Editor editor, final boolean isCompleted) {
		// Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.notify_user_verification)
        		.setCancelable(false)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   if(isCompleted) {
                    	   goHome(extraBundle);                		   
                	   } else {
                		   goRegistrationProfile(extraBundle);
                	   }
                   }
               });
        
        // Create the AlertDialog object and return it
        AlertDialog alert = builder.create();
        alert.show();
	}
	
	private void goHome(Bundle extraBundle) {
		Intent i = new Intent(context, HomeActivity.class);
		i.putExtras(extraBundle);
		startActivity(i);
		finish();
	}
	
	private void goRegistrationProfile(Bundle extraBundle) {
		Intent i = new Intent(context,
				ProfileRegistrationActivity.class);		
		i.putExtras(extraBundle);
		startActivity(i);
		finish();
	}

}
