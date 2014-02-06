package com.matchimi.registration;
import static com.matchimi.CommonUtilities.COMMON_FACEBOOK_ID;
import static com.matchimi.CommonUtilities.COMMON_PASSWORD;
import static com.matchimi.CommonUtilities.LOGIN;
import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SENDER_ID;
import static com.matchimi.CommonUtilities.SERVERURL;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.USER_NRIC_NUMBER;
import static com.matchimi.CommonUtilities.USER_NRIC_TYPE;
import static com.matchimi.CommonUtilities.USER_NRIC_TYPE_ID;
import static com.matchimi.CommonUtilities.USER_PROFILE_PICTURE;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.matchimi.CommonUtilities;
import com.matchimi.DatabaseStorage;
import com.matchimi.HomeActivity;
import com.matchimi.R;
import com.matchimi.ServerUtilities;
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
	
	private String registrationID;
	private DatabaseStorage db;
	AsyncTask<Void, Void, Void> registerTask;
	GoogleCloudMessaging gcm;
	
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
					extraBundle.putString(CommonUtilities.USER_BIRTHDAY, "");
					extraBundle.putString(CommonUtilities.USER_FULLNAME, "");				
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
		
		settings = getSharedPreferences(
				CommonUtilities.PREFS_NAME, Context.MODE_PRIVATE);
		
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
					extraBundle.putString(CommonUtilities.USER_FULLNAME, user.getFirstName() + " " + user.getLastName());
					extraBundle.putString(CommonUtilities.USER_FACEBOOK_ID, user.getId());
					extraBundle.putString(CommonUtilities.USER_GENDER, user.getProperty("gender").toString());

					Log.d(CommonUtilities.TAG, "User birthday from post login " + user.getBirthday());
					
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(CommonUtilities.USER_FACEBOOK_ID, user.getId());
					editor.commit();
					
					getFriends(user);
					
				}
			});
		} else {
			Log.e(CommonUtilities.TAG, "updateUI NULL");
		}
	}
	
	private void getFriends(final GraphUser user) {
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
	            		
//	            		Log.d(TAG, "Facebook friends " + facebookFriends);
	            		
	            		if(user.getProperty("email") == null) {		
	            			
							// Clear stored data
							settings.edit().clear().commit();
							SharedPreferences.Editor editor = settings.edit();
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
	
	protected void submitFriendFacebook(final Boolean isCreated) {
		final String url;

		if(isCreated) {
			url = SERVERURL + CommonUtilities.API_CREATE_PART_TIMER_FRIENDS_BY_PT_ID;
		} else {
			url = SERVERURL + CommonUtilities.API_UPDATE_PART_TIMER_FRIENDS_BY_PT_ID;
		}
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						
						DateTimeFormatter format = DateTimeFormat.forPattern(CommonUtilities.SHARED_DATETIME_FORMAT);
						DateTime today = new DateTime();
						String submittedDate = today.toString(format);
						
						SharedPreferences.Editor editor = settings.edit();
						editor.putString(CommonUtilities.SHARED_FACEBOOK_FRIENDSDATE, submittedDate);	
						editor.commit();						
						
						// Always create new editor
						editor = settings.edit();						
						if (settings.getBoolean(CommonUtilities.REGISTERED, false)) {
							editor.putBoolean(CommonUtilities.LOGIN, true);
							editor.commit();
							
							// If user not verified, remind them
//							if(userIsVerified == "false") {
//								notifyUserVerification(extraBundle, editor, true);
//							} else {
//								goHome(extraBundle);
//							}
							goHome(extraBundle);
							
						} else {
							// Check if user already completed basic profile
							if(userBasicComplete) {
								editor.putBoolean(CommonUtilities.LOGIN, true);
								editor.commit();
								
								// If user not verified, remind them
//								if(userIsVerified == "false") {
//									notifyUserVerification(extraBundle, editor, true);
//								} else {
//									goHome(extraBundle);
//								}
								
								goHome(extraBundle);
								
							} else {
								editor.commit();
//								if(userIsVerified == "false") {
//									notifyUserVerification(extraBundle, editor, false);
//								} else {
//									goRegistrationProfile(extraBundle);								
//								}
								
								goRegistrationProfile(extraBundle);	
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
				getString(R.string.updating_friends), true, false);
		
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
					
					if(isCreated) {
						jsonStr = jsonParser.getHttpResultUrlPost(url, params,
								values);
					} else {
						jsonStr = jsonParser.getHttpResultUrlPut(url, params,
								values);
					}

//					Log.e(TAG, "Post data" + childData.toString());
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
					} if (jsonStr.trim().equalsIgnoreCase("3")) {
						closeSession();
						Toast.makeText(context,
								getString(R.string.account_not_existed),
								Toast.LENGTH_LONG).show();
					} else if (jsonStr.trim().length() > 0){
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
							
							SharedPreferences.Editor editor = settings.edit();
							editor.putString(CommonUtilities.USER_PTID, pt_id);
							editor.putString(CommonUtilities.USER_FULLNAME, 
									partTimer.getString(CommonUtilities.PARAM_PROFILE_FULL_NAME));
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
							Boolean checkUserComplete = ValidationUtilities.checkProfileComplete(partTimer);
							
							Log.d(CommonUtilities.TAG, "Check user profile is copmlete status : " + checkUserComplete);
							
							editor.putBoolean(CommonUtilities.USER_PROFILE_COMPLETE, checkUserComplete);			
							editor.commit();
							
							Log.d(CommonUtilities.TAG, "Check user complete status : " + checkUserComplete);
							
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
									&& userWorkExperience != "null"
									&& userNRICType != "null") {
								userBasicComplete = true;		
								Log.d(CommonUtilities.TAG, " complete");
								
							} else {
								Log.d(CommonUtilities.TAG, "Not complete : IC TYPE = " + userNRICType + 
										"; GENDER = " + userGender + "; DOB = " + userDob + ";");
							}

							String friendsDate = settings.getString(CommonUtilities.SHARED_FACEBOOK_FRIENDSDATE, null);
							Log.d(CommonUtilities.TAG, "GET Update FRIEDNS " + friendsDate);
							
							Boolean isSubmitFriends = false;
							
							if(friendsDate != null) {
								DateTimeFormatter formatter = DateTimeFormat.forPattern(CommonUtilities.SHARED_DATETIME_FORMAT);
								DateTime dt = formatter.parseDateTime(friendsDate);
								
								DateTime now = DateTime.now();
								Days days = Days.daysBetween(now, dt);
								
								if(days.getDays() > CommonUtilities.SHARED_LIMIT_DAYS) {
									isSubmitFriends = true;
								}
							} else {
								isSubmitFriends = true;
							}
							
							if (registrationID.length() == 0) {
								Log.d(CommonUtilities.TAG, "Register background executed");
					        } else {
					        	Log.d(CommonUtilities.TAG, "User devices already registered " + registrationID);
					        }
							
							registerBackground();

							File rootDir = Environment.getExternalStorageDirectory();
							File rootFile = new File(rootDir, CommonUtilities.ROOT_DIR);
							
							if (!rootFile.exists() || !rootFile.isDirectory()) {
								rootFile.mkdir();
							}
							
							if(isFacebook && isSubmitFriends) {
								if(friendsDate != null) {
									submitFriendFacebook(false);																
								} else {
									submitFriendFacebook(true);																
								}
							} else {

								if (settings.getBoolean(CommonUtilities.REGISTERED, false)) {
									editor = settings.edit();
									editor.putBoolean(CommonUtilities.LOGIN, true);
									editor.commit();
									
									goHome(extraBundle);
									
								} else {
									// Check if user already completed basic profile
									if(userBasicComplete) {
										editor = settings.edit();
										editor.putBoolean(CommonUtilities.LOGIN, true);
										editor.commit();
										goHome(extraBundle);
										
									} else {
										goRegistrationProfile(extraBundle);	
									}
								}
							}
							
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
