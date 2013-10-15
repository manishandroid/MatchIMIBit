package com.matchimi.registration;

import static com.matchimi.CommonUtilities.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import khandroid.ext.apache.http.HttpResponse;
import khandroid.ext.apache.http.client.HttpClient;
import khandroid.ext.apache.http.client.methods.HttpPost;
import khandroid.ext.apache.http.entity.mime.HttpMultipartMode;
import khandroid.ext.apache.http.entity.mime.MultipartEntity;
import khandroid.ext.apache.http.entity.mime.content.ByteArrayBody;
import khandroid.ext.apache.http.entity.mime.content.StringBody;
import khandroid.ext.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.matchimi.CommonUtilities;
import com.matchimi.HomeActivity;
import com.matchimi.R;
import com.matchimi.ValidationUtilities;
import com.matchimi.options.JobDetails;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class ProfileRegistrationActivity extends Activity {

	private Context context;

	private List<String> listGender;
	private List<String> listGenderId;
	private List<String> listNRICType;
	private List<String> listNRICTypeId;
	private List<String> listSkill;
	private List<String> listSkillId;
	private List<String> listSkillDesc;
	private List<Integer> listSelectedItems;

	private ProgressDialog progress;
	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private Bundle bundleExtras;
	private TextView birthView;
	private TextView skillView;
	private TextView nricTypeView;

	private EditText firstName;
	private EditText lastName;
	
//	private EditText phoneNumber;
	private EditText editExperience;

	private int nricSelected = -1;

	private String pt_id;
	private String genderSelected = null;

	private Button photoFrontNRIC;
	private Button photoBackNRIC;
	private Button profileSubmit;

	private boolean takeFrontNRIC = true;

	int bytesRead, bytesAvailable, bufferSize;
	byte[] buffer;
	int maxBufferSize = 1024 * 1024 * 1;

	// Photos configuration
	private String JPEG_FILE_PREFIX = R.string.album_name + "_";
	private String JPEG_FILE_SUFFIX = ".jpg";
	String currentPhotoPath = "";
	
	private final String dobFormat = "dd/MM/yyyy";
	private final SimpleDateFormat facebookDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	private final SimpleDateFormat dobDateFormat = new SimpleDateFormat(dobFormat, Locale.US);	
	private long birthdayRestriction = 0;
	private Calendar defaultBirthday;
	
	private List<String> listSchool;
	private List<String> listSchoolId;
	private int selectedIdx = -1;
	private TextView school;
	private RelativeLayout schoolLayout;
	private boolean isSchoolRequired = false;

	private TextView workExperienceView;
	private List<CharSequence> listWorkExperience;
	private List<String> listWorkExpID;
	private int selectedWorkIdx = -1;
	private Bitmap facebookAvatar = null;
	private boolean isFacebookAvatar = false;
	private String facebookID;
	private String profilePicture = "";
	
	public AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	private SharedPreferences settings;
	private RadioGroup genderView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profileregistration_page);

		context = this;

		firstName = (EditText) findViewById(R.id.regprofile_family_name);
		lastName = (EditText) findViewById(R.id.regprofile_given_name);
		
//		phoneNumber = (EditText) findViewById(R.id.editPhoneNumber);
		genderView = (RadioGroup) findViewById(R.id.gender_group);

		// Set date birth
		birthView = (TextView) findViewById(R.id.regprofile_date_of_birth);
		birthView.setOnClickListener(birthListener);

		// Set date birth
		nricTypeView = (TextView) findViewById(R.id.regprofile_nric_type);
		nricTypeView.setOnClickListener(nricTypeListener);

		// Set skills textview and listener
//		skillView = (TextView) findViewById(R.id.regprofile_skills);
//		skillView.setOnClickListener(skillsListener);

		// Set experience textview and listener
//		editExperience = (EditText) findViewById(R.id.editExperience);
		
		workExperienceView = (TextView) findViewById(R.id.regprofile_work_experience);
		workExperienceView.setOnClickListener(workExperienceListener);

		CharSequence[] workExpArray = getResources().getStringArray(R.array.workexperience_array);
		listWorkExperience = Arrays.asList(workExpArray);
		
		String[] workExpIDArray = getResources().getStringArray(R.array.workexperience_id_array);
		listWorkExpID = Arrays.asList(workExpIDArray);

		// Set take front NRIC button
//		photoFrontNRIC = (Button) findViewById(R.id.profile_reg_addfront_button);
//		photoFrontNRIC.setOnClickListener(frontNRICListener);

		// Set take back NRIC button
//		photoBackNRIC = (Button) findViewById(R.id.profile_reg_addback_button);
//		photoBackNRIC.setOnClickListener(backNRICListener);

		profileSubmit = (Button) findViewById(R.id.regprofile_submit_button);
		profileSubmit.setOnClickListener(profileSubmitListener);

		bundleExtras = getIntent().getExtras();
		pt_id = bundleExtras.getString(CommonUtilities.USER_PTID);

		settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		
		// Set date now and birthday limitation
		Date date = new Date();
		defaultBirthday = Calendar.getInstance();
		defaultBirthday.setTime(date);
		defaultBirthday.add(Calendar.YEAR, -AGE_LIMITATION);
		birthdayRestriction = defaultBirthday.getTimeInMillis();		

		schoolLayout = (RelativeLayout) findViewById(R.id.schoolLayout);
		
		if (bundleExtras != null) {
			Log.d(TAG, "Incoming intent to profile");
			
			String userFirstName = bundleExtras
					.getString(CommonUtilities.USER_FIRSTNAME);
			String userLastName = bundleExtras
					.getString(CommonUtilities.USER_LASTNAME);

			// Set default value for user fullname
			if (userFirstName != null) {
				firstName.setText(userFirstName);
			}
			
			if(userLastName != null) {
				lastName.setText(userLastName);
			}

			String bday = bundleExtras.getString(CommonUtilities.USER_BIRTHDAY);
			if (bday.length() == 10) {
				try {
					Date birthdayFromFacebook = facebookDateFormat.parse(bday);

					if(birthdayFromFacebook.getTime() > birthdayRestriction) {
						birthdayRestrictionDialog();
					} else {
						defaultBirthday.setTime(birthdayFromFacebook);
						Log.e(TAG, "Facebook birthday " + birthdayFromFacebook.toString());
						
						facebookDateFormat.applyPattern(dobFormat);
						birthView.setText(facebookDateFormat.format(birthdayFromFacebook));
					}					

					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
			
			facebookID = bundleExtras.getString(CommonUtilities.USER_FACEBOOK_ID);
			if(facebookID.length() > 0) {
				isFacebookAvatar = true;
			}
			
		}
		
		school = (TextView) findViewById(R.id.registrationTextSchool);
		school.setOnClickListener(schoolListener);

		// Load Album Storage Factory based on Android Version
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}

		loadDefaultProfileData();
	}
	
	protected void loadDefaultProfileData() {
		final String url = CommonUtilities.SERVERURL + 
				CommonUtilities.API_GET_PROFILE_DEFAULT_DATA;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					loadGenderJson(jsonStr);
					
					// If user register using Facebook, download the avatar
					if(isFacebookAvatar) {
						loadFacebookAvatar(facebookID);
					}

				} else {
					Toast.makeText(context,
							getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context,
				getResources().getString(R.string.registration_profile_title),
				getString(R.string.loading_data), true, false);
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
	
	/**
	 * Loading gender default data from JSON
	 * 
	 * @param jsonStr
	 */
	private void loadGenderJson(String jsonStr) {
		try {
			JSONObject dataObj = new JSONObject(jsonStr);
			JSONArray jsonArray = dataObj.getJSONArray(CommonUtilities.PARAM_PROFILE_DEFAUT_PART_TIMER_GENDER);
			
			listGender = new ArrayList<String>();
			listGenderId = new ArrayList<String>();
			
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				
				obj = obj.getJSONObject("genders");
				listGender.add(obj.getString("gender"));
				listGenderId.add(obj.getString("gender_id"));
			}
			
			createGenderView();
			loadNRICTypeJSON(jsonStr);
			
		} catch (JSONException e) {
			NetworkUtils.connectionHandler(ProfileRegistrationActivity.this, jsonStr, e.getMessage());
			Log.e(TAG, "Error while get genders >>> " + e.getMessage());
		}
	}

	/**
	 * Loading NRICType from JSON
	 * 
	 * @param jsonStr
	 */
	private void loadNRICTypeJSON(String jsonStr) {		
		
		try {
			JSONObject dataObj = new JSONObject(jsonStr);
			JSONArray jsonArray = dataObj.getJSONArray(CommonUtilities.PARAM_PROFILE_DEFAUT_PART_TIMER_IC_TYPE);
			
			listNRICType = new ArrayList<String>();
			listNRICTypeId = new ArrayList<String>();


			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				obj = obj.getJSONObject("nric_types");
				listNRICType.add(obj.getString("nric_type"));
				listNRICTypeId.add(obj.getString("nric_type_id"));
			}

			loadSchoolJson(jsonStr);
		} catch (JSONException e) {
			NetworkUtils.connectionHandler(ProfileRegistrationActivity.this, jsonStr, e.getMessage());
			Log.e(TAG, "Error get ic types >>> " + e.getMessage());
		}
	}
	
	/**
	 * Loading schools default data from JSON
	 * 
	 * @param jsonStr
	 */
	private void loadSchoolJson(String jsonStr) {
		try {
			listSchool = new ArrayList<String>();
			listSchoolId = new ArrayList<String>();

			JSONObject dataObj = new JSONObject(jsonStr);
			JSONArray jsonArray = dataObj.getJSONArray(CommonUtilities.PARAM_PROFILE_DEFAUT_PART_TIMER_SCHOOL);
			
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				obj = obj.getJSONObject("schools");
				listSchool.add(obj.getString("school_name"));
				listSchoolId.add(obj.getString("school_id"));
			}
			
		} catch (Exception e) {
			NetworkUtils.connectionHandler(ProfileRegistrationActivity.this, jsonStr, e.getMessage());
			Log.e(TAG, "Error skills >>> " + e.getMessage());
		}
	}
	
	protected void createGenderView() {
		if (listGender.size() > 0) {
			for (int i = 0; i < listGender.size(); i++) {
				final RadioButton rb = new RadioButton(context);
				rb.setText(listGender.get(i));
				rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						if (arg1) {
							genderSelected = arg0.getText().toString();
						}
					}
				});
				genderView.addView(rb);
				Bundle b = getIntent().getExtras();
				if (getIntent().hasExtra(CommonUtilities.USER_GENDER)
						&& b.getString(CommonUtilities.USER_GENDER)
								.equalsIgnoreCase(listGender.get(i))) {
					genderSelected = listGender.get(i);
					rb.setChecked(true);
				}
			}
		}
	}

	protected void loadFacebookAvatar(final String user_id) {
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				// Nothing needed
			}
		};

		progress = ProgressDialog.show(context,
				getResources().getString(R.string.app_name),
				getString(R.string.download_avatar), true, false);
		new Thread() {
			public void run() {				
				try {
					URL fbAvatarUrl = new URL("http://graph.facebook.com/"
							+ user_id + "/picture?type=large");
					Log.d(TAG, "Image from " + fbAvatarUrl.toString());

					facebookAvatar = BitmapFactory.decodeStream(fbAvatarUrl
							.openConnection().getInputStream());
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

	/**
	 * Date birth selection dialog listener
	 */
	private OnClickListener birthListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			String birthValue = birthView.getText().toString().trim();
			if(birthValue.length() == 10) {
				try {
					Date dobDate = dobDateFormat.parse(birthValue);
					defaultBirthday.setTime(dobDate);					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			
			DatePickerDialog dateDlg = new DatePickerDialog(
					ProfileRegistrationActivity.this,
					new DatePickerDialog.OnDateSetListener() {
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							Time chosenDate = new Time();
							chosenDate.set(dayOfMonth, monthOfYear, year);
							long dtDob = chosenDate.toMillis(true);

							Date date = new Date();
							Calendar cal = Calendar.getInstance();
							cal.setTime(date);
							cal.add(Calendar.YEAR, -AGE_LIMITATION);
							long limitation = cal.getTimeInMillis();

							if (dtDob > limitation) {
								birthView.setText("");
								birthdayRestrictionDialog();

							} else {
								CharSequence strDate = DateFormat.format(
										"dd/MM/yyyy", dtDob);
								birthView.setText(strDate);
							}
						}
					}, defaultBirthday.get(Calendar.YEAR),
					defaultBirthday.get(Calendar.MONTH),
					defaultBirthday.get(Calendar.DAY_OF_MONTH));
			
			dateDlg.setMessage("Your Birthday");
			dateDlg.show();
		}
	};
	
	private void birthdayRestrictionDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				ProfileRegistrationActivity.this);

		// set title
		builder.setTitle(getString(R.string.registration_birthday_restriction_title));

		// set dialog message
		builder.setMessage(getString(R.string.registration_birthday_restriction))
				.setPositiveButton(
						"OK",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int id) {
								// Nothing to do
							}
						});

		Dialog dialog = builder.create();
		dialog.show();
	}

	private OnClickListener workExperienceListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			// Set the dialog title
			selectedIdx = -1;
			builder.setTitle("Select " + getString(R.string.registration_profile_work_experience));
			builder.setItems(listWorkExperience.toArray(new CharSequence[listWorkExperience.size()]), 
					new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	            	   selectedWorkIdx = which;
	            	   if (selectedWorkIdx != -1) {
							workExperienceView.setText(listWorkExperience
									.get(selectedWorkIdx));
						}
	               }
            });

			Dialog dialog = builder.create();
			dialog.show();
		}
	};
	
	
	private OnClickListener schoolListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			// Set the dialog title
			selectedIdx = -1;
			builder.setTitle("Select " + getString(R.string.registration_profile_school));
			builder.setItems(listSchool.toArray(new CharSequence[listSchool.size()]), new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int which) {
		            	   selectedIdx = which;
		            	   if(selectedIdx != -1) {
			            	   school.setText(listSchool.get(selectedIdx));		            		   
		            	   }
		           }
		    });
	           
			Dialog dialog = builder.create();
			dialog.show();
		}
	};
	
	/**
	 * NRIC type selection list-checkbox listener
	 */
	private OnClickListener nricTypeListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					ProfileRegistrationActivity.this);

			// Set the dialog title
			builder.setTitle("Select " + getString(R.string.registration_profile_nrictype));
			builder.setItems(listNRICType.toArray(new CharSequence[listNRICType.size()]), new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	               // The 'which' argument contains the index position
	               // of the selected item
	            	   nricSelected = which;
	            	   if (nricSelected != -1) {
							nricTypeView.setText(listNRICType
									.get(nricSelected));
							
							// If student selected, enable school
							if(nricSelected == 2) {
								schoolLayout.setVisibility(View.VISIBLE);
								isSchoolRequired = true;
							} else {
								schoolLayout.setVisibility(View.GONE);											
								school.setText("");
								isSchoolRequired = false;
							}
						}
	               }
	        });
			
			Dialog dialog = builder.create();
			dialog.show();
		}
	};
	
	private OnClickListener profileSubmitListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			String userNRICBack = settings.getString(USER_NRIC_BACK, "");
			String userNRICFront = settings.getString(USER_NRIC_FRONT, "");
			String errors = "";

			if (firstName.getText().toString().trim().length() == 0) {
				errors += "* " + getString(R.string.registration_profile_family_name) + "\n";
			}

			if (lastName.getText().toString().trim().length() == 0) {
				errors += "* " + getString(R.string.registration_profile_given_name) + "\n";
			}

			if (genderSelected == null) {
				errors += "* " + getString(R.string.registration_profile_gender) + "\n";
			}

			if (birthView.getText().length() == 0) {
				errors += "* " + getString(R.string.registration_profile_date_of_birth) + "\n";
			}

			if (nricTypeView.getText().length() == 0) {
				errors += "* " + getString(R.string.registration_profile_nrictype) + "\n";
			}
			
			if(isSchoolRequired == true && school.getText().length() == 0) {
				errors += "* " + getString(R.string.registration_profile_school) + "\n";				
			}
			
			if (workExperienceView.getText().length() == 0) {
				errors += "* " + getString(R.string.registration_profile_work_experience) + "\n";
			}
			
			// if (userNRICFront == "") {
			// errors += "* NRIC front photo\n";
			// }
			//
			// if (userNRICBack == "") {
			// errors += "* NRIC back photo\n";
			// }

//			if (skillView.getText().length() == 0) {
//				errors += "* Skills\n";
//			}

//			if (editExperience.getText().length() == 0) {
//				errors += "* " + getString(R.string.registration_profile_work_experience) +"\n";
//			}

//			if (phoneNumber.getText().length() == 0) {
//				errors += "* " + getString(R.string.registration_profile_phone_number) + "\n";
//			}

			// If all fields completed, then create profile
			if (errors.length() == 0) {
				if(isFacebookAvatar) {
					uploadFacebookAvatar(pt_id);
				} else {
					doCreateProfile();					
				}
				
			} else {
				// Showing errors if fields not completed
				String showError = "Please complete :\n\n" + errors;
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						ProfileRegistrationActivity.this);

				// set title
				alertDialogBuilder.setTitle("Registration");
				// set dialog message
				alertDialogBuilder
						.setMessage(showError)
						.setCancelable(false)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// Nothing to do here
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
				// show it
				alertDialog.show();
			}
		}
	};
	
	/**
	 * Upload facebook avatar
	 * 
	 * @param pt_id
	 */
	protected void uploadFacebookAvatar(final String pt_id) {
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				doCreateProfile();
			}
		};

		progress = ProgressDialog.show(context,
				getResources().getString(R.string.app_name),
				"Uploading avatar...", true, false);
		new Thread() {
			public void run() {
				String result = "";
				Bitmap bm = facebookAvatar;
				
				boolean isRenamed = false;
				
				try {
					String url = CommonUtilities.SERVERURL + CommonUtilities.API_UPLOAD_PROFILE_PICTURE;
					String selectedFileName = CommonUtilities.FILE_IMAGE_PROFILE + ".jpg";
					String filePath = CommonUtilities.IMAGE_ROOT + selectedFileName;
					
					File file = new File(filePath);
					String filename = file.getName();
					
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bm.compress(CompressFormat.JPEG, 75, bos);
					
					byte[] data = bos.toByteArray();
					
					// Replacing image in local storage
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(data);
					
					HttpClient httpClient = new DefaultHttpClient();
					HttpPost postRequest = new HttpPost(url);

					ByteArrayBody bab = new ByteArrayBody(data, filename);
					MultipartEntity reqEntity = new MultipartEntity(
							HttpMultipartMode.BROWSER_COMPATIBLE);
					reqEntity.addPart("file", bab);
					reqEntity.addPart("filename", new StringBody(filename));
					postRequest.setEntity(reqEntity);
					HttpResponse response = httpClient.execute(postRequest);
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(
									response.getEntity().getContent(), "UTF-8"));
					String sResponse;
					StringBuilder s = new StringBuilder();

					while ((sResponse = reader.readLine()) != null) {
						s = s.append(sResponse);
					}
					
					String imagePath = s.toString();
					Log.d(TAG, "Upload result: " + imagePath);
					
					String newFilename = new File(imagePath).getName();
					String newPath = CommonUtilities.IMAGE_ROOT + newFilename;
					
					File f = null;
				    File f2 = null;
					
					try {
						f = new File(filePath);						
						f2 = new File(newPath);					
						isRenamed = f.renameTo(f2);
					} catch(Exception e){
				         // if any error occurs
				         e.printStackTrace();
				    }
					
					if(isRenamed) {
						profilePicture = imagePath;
					}					

				} catch(Exception e){
			         // if any error occurs
			         e.printStackTrace();
			    }

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

	protected void doCreateProfile() {
		Log.d(TAG, "Create profile executed ");
		
		final String url = SERVERURL + API_CREATE_AND_PART_TIMER_PROFILE;		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				Log.d(TAG, "Create profile running ... ");
				
				if (jsonStr != null) {
					Log.d(TAG, "Result from " + url + " " + jsonStr.toString());
					
					// FIXME: please check on server response
					if (jsonStr.trim().equalsIgnoreCase("0") || 
						jsonStr.trim().equalsIgnoreCase("2")) {
						SharedPreferences.Editor editor = settings.edit();
						editor.putBoolean(CommonUtilities.LOGIN, true);
						editor.putBoolean(CommonUtilities.REGISTERED, true);
						
//						String name = fullName.getText().toString().trim();
//						String fname = "";
//						String lname = "";
//						if (name.contains(" ")) {
//							fname = name.substring(0, name.lastIndexOf(" "));
//							lname = name.substring(name.lastIndexOf(" ") + 1);
//						} else {
//							fname = name;
//						}
						
						editor.putString(USER_FIRSTNAME, firstName.getText().toString().trim());
						editor.putString(USER_LASTNAME, lastName.getText().toString().trim());
						editor.putString(USER_NRIC_TYPE, listNRICType.get(nricSelected));
						editor.putString(USER_NRIC_TYPE_ID, listNRICTypeId.get(nricSelected));						
						editor.commit();
						
						Intent intent = new Intent(context, HomeActivity.class);
						startActivity(intent);
						finish();
						
//					} else if (jsonStr.trim().equalsIgnoreCase("2")) {
//						ValidationUtilities.resendLinkDialog(ProfileRegistrationActivity.this, pt_id);
					} else if (jsonStr.trim().equalsIgnoreCase("1")) {
						Toast.makeText(context,
								getString(R.string.registration_profile_failed),
								Toast.LENGTH_LONG).show();
					} else {
						NetworkUtils.connectionHandler(ProfileRegistrationActivity.this, jsonStr, "");
					}
					
				} else {
					Toast.makeText(context,
							getString(R.string.registration_profile_failed),
							Toast.LENGTH_LONG).show();
				}
			}
		};

		progress = ProgressDialog.show(context, getString(R.string.registration_profile_title),
				getString(R.string.registration_profile_progress), true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				Log.d(TAG, "Dialog process profile running ... ");
				
				try {
					JSONObject parentData = new JSONObject();
					JSONObject childData = new JSONObject();
					childData.put("pt_id", pt_id);
					
//					String name = fullName.getText().toString().trim();
//					String fname = "";
//					String lname = "";
//					if (name.contains(" ")) {
//						fname = name.substring(0, name.lastIndexOf(" "));
//						lname = name.substring(name.lastIndexOf(" ") + 1);
//					} else {
//						fname = name;
//					}
					
					childData.put("first_name", firstName.getText().toString().trim());
					childData.put("last_name", lastName.getText().toString().trim());
					
					childData.put("gender",
							genderSelected.toLowerCase(Locale.getDefault()));
					childData.put("dob", birthView.getText().toString().trim());
					childData.put("profile_picture", profilePicture);
					
//					childData.put("phone_no", phoneNumber.getText().toString()
//							.trim());
					childData.put("phone_no", "");
					childData.put("address", "");
					childData.put("postal_code", "");
					childData.put("ic_type", listNRICType.get(nricSelected));
					childData.put("ic_type_id",
							listNRICTypeId.get(nricSelected));
					
					if(isSchoolRequired) {
						int tmp = listSchool.indexOf(school.getText().toString().trim());
						childData.put("school_id", listSchoolId.get(tmp));
					} else {
						childData.put("school_id", "");						
					}
					childData.put("ic_expiry_date", "");
					childData.put("ic_front_picture", settings.getString(
							CommonUtilities.USER_NRIC_FRONT, ""));
					childData.put("ic_back_picture", settings.getString(
							CommonUtilities.USER_NRIC_BACK, ""));
					
					int tmp = listWorkExperience.indexOf(workExperienceView.getText().toString().trim());
					childData.put("work_experience", listWorkExpID.get(tmp));
					childData.put("profile_source", "");
					childData.put("matric_card_picture", "");
					childData.put("matric_card_no", "");
					childData.put("ec_first_name", "");
					childData.put("ec_last_name", "");
					childData.put("ec_email", "");
					childData.put("ec_phone_no", "");
					childData.put("ec_address", "");
					childData.put("ec_postal_code", "");
					childData.put("ec_relationship", "");
					childData.put("bank_name", "");
					childData.put("bank_account_type", "");
					childData.put("bank_account_no", "");
					childData.put("bank_branch_name", "");
					childData.put("skills", "[]");
					parentData.put("part_timer", childData);

					String[] params = { "data" };
					String[] values = { parentData.toString() };
					jsonStr = jsonParser.getHttpResultUrlPost(url, params, values);

					Log.e(TAG, "Create profile to " + url + "with data  >>>\n" + childData.toString());
					
				} catch (Exception e) {
					jsonStr = null;
					Log.d(TAG, "Issue in generating Json profile");
				}

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}	

	/**
	 * Skills selection list-checkbox listener
	 */
//	private OnClickListener skillsListener = new OnClickListener() {
//
//		@Override
//		public void onClick(View view) {
//			listSelectedItems = new ArrayList<Integer>();
//			final ArrayList<CharSequence> selectedSkills = new ArrayList<CharSequence>();
//			final List<Integer> selectedIdx = new ArrayList<Integer>();
//
//			AlertDialog.Builder builder = new AlertDialog.Builder(
//					ProfileRegistrationActivity.this);
//
//			// Set the dialog title
//			builder.setTitle("Select Skills")
//					.setMultiChoiceItems(
//							listSkill
//									.toArray(new CharSequence[listSkill.size()]),
//							null,
//							new DialogInterface.OnMultiChoiceClickListener() {
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which, boolean isChecked) {
//									if (isChecked) {
//										listSelectedItems.add(Integer
//												.parseInt(listSkillId
//														.get(which)));
//										selectedIdx.add(which);
//									} else if (listSelectedItems
//											.contains(which)) {
//										listSelectedItems.remove(Integer
//												.valueOf(which));
//										selectedIdx.remove(Integer
//												.valueOf(which));
//									}
//								}
//							})
//					.setPositiveButton(R.string.ok,
//							new DialogInterface.OnClickListener() {
//								@Override
//								public void onClick(DialogInterface dialog,
//										int id) {
//									selectedSkills.clear();
//									for (int i = 0; i < selectedIdx.size(); i++) {
//										selectedSkills.add(listSkill
//												.get(selectedIdx.get(i)));
//									}
//
//									// Convert ArrayList into String
//									// comma-separated
//									String selectedSkillsList = selectedSkills
//											.toString();
//									String selectedSkillsSet = selectedSkillsList
//											.substring(
//													1,
//													selectedSkillsList.length() - 1)
//											.replace(", ", ", ");
//									skillView.setText(selectedSkillsSet);
//								}
//							});
//
//			Dialog dialog = builder.create();
//			dialog.show();
//		}
//	};

	/**
	 * Take front NRIC photos listener
	 */
	private OnClickListener frontNRICListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			// Set value of taking front NRIC
			takeFrontNRIC = true;

			// Execute camera
			dispatchTakePhotoIntent(CAMERA_REQUEST);
		}
	};

	/**
	 * Take back NRIC photos listener
	 */
	private OnClickListener backNRICListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			// Set value of taking back NRIC
			takeFrontNRIC = false;

			// Execute camera
			dispatchTakePhotoIntent(CAMERA_REQUEST);
		}
	};

	/**
	 * Handle Photo Results and send into Server
	 */
	private void handleCameraPhoto(Intent intent) {
		String[] params = { currentPhotoPath };

		new postData().execute(params);
	}

	/**
	 * Set the albumName for storing photos
	 * 
	 * @return String
	 */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}

	/**
	 * Create and get the album storage directory
	 * 
	 * @return File
	 */
	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			storageDir = mAlbumStorageDirFactory
					.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.d(TAG, "Can't create camera directory");
						return null;
					}
				}
			}
		} else {
			Log.v(TAG, "External storage is not mounted READ/WRITE");
		}

		return storageDir;
	}

	/**
	 * Create Photo file from Camera
	 * 
	 * @return File
	 * @throws IOException
	 */
	private File createPhotoFile() throws IOException {
		String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss",
				Locale.getDefault()).format(new Date());
		String imageFile = JPEG_FILE_PREFIX + timestamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFile, JPEG_FILE_SUFFIX, albumF);

		return imageF;
	}

	/**
	 * Setup Photo file, generate and set the path of the file
	 * 
	 * @return File
	 * @throws IOException
	 */
	private File setupPhotoFile() throws IOException {
		File photo = createPhotoFile();
		currentPhotoPath = photo.getAbsolutePath();
		Log.v(TAG, "Set photo file path" + currentPhotoPath);

		return photo;
	}

	/**
	 * Intent to handle capture image from camera and save into a file
	 * 
	 * @param actionCode
	 */
	private void dispatchTakePhotoIntent(int actionCode) {
		Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		switch (actionCode) {
		case CAMERA_REQUEST:
			File photo = null;

			try {
				photo = setupPhotoFile();
				takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photo));

				setResult(Activity.RESULT_OK, takePhotoIntent);
				takePhotoIntent.putExtra("return-data", true);

			} catch (IOException e) {
				e.printStackTrace();
				photo = null;
				currentPhotoPath = null;
			}
			break;

		default:
			break;
		}

		startActivityForResult(takePhotoIntent, actionCode);
	}

	/**
	 * Process photos that already saved by the users after taken from Camera
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CAMERA_REQUEST:
			if (resultCode == Activity.RESULT_OK) {
				handleCameraPhoto(data);
			}
			break;
		}
	}

	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 * 
	 * @param context
	 *            The application's environment.
	 * @param action
	 *            The Intent action to check for availability.
	 * 
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	/**
	 * Check whether app can handle our intent
	 * 
	 * @param context
	 * @param action
	 * @return
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
	

	/**
	 * Upload image to server and retrieve the path
	 * 
	 * @author Polatic
	 * 
	 */
	private class postData extends AsyncTask<String, Void, String> {
		ProgressDialog dialog = new ProgressDialog(
				ProfileRegistrationActivity.this);
		Bitmap bm;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.setMessage("Please wait...");
			dialog.setIndeterminate(true);
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected String doInBackground(String... params) {

			String filePath = params[0];
			File file = new File(filePath);
			String filename = file.getName();

			bm = BitmapFactory.decodeFile(filePath);
			
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bm.compress(CompressFormat.JPEG, 75, bos);

				byte[] data = bos.toByteArray();
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost postRequest = new HttpPost(CommonUtilities.SERVERURL
						+ CommonUtilities.API_UPLOAD_FRONT_NRIC_PHOTOS);

				ByteArrayBody bab = new ByteArrayBody(data, filename);
				MultipartEntity reqEntity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart("file", bab);
				reqEntity.addPart("filename", new StringBody(filename));
				postRequest.setEntity(reqEntity);
				HttpResponse response = httpClient.execute(postRequest);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));
				String sResponse;
				StringBuilder s = new StringBuilder();

				while ((sResponse = reader.readLine()) != null) {
					s = s.append(sResponse);
				}

				Log.d(TAG, "Server response : " + s);

				// Saving user NRIC path
				if (takeFrontNRIC) {
					ApplicationUtils.copyFile(filePath,
							ApplicationUtils.getAppRootDir() + "/front");
					SharedUtils.updateSettings(settings,
							CommonUtilities.USER_NRIC_FRONT, s.toString());
				} else {
					ApplicationUtils.copyFile(filePath,
							ApplicationUtils.getAppRootDir() + "/back");
					SharedUtils.updateSettings(settings,
							CommonUtilities.USER_NRIC_BACK, s.toString());
				}
			} catch (Exception e) {
				// handle exception here
				Log.e(e.getClass().getName(), e.getMessage());
			}

			return filePath;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			dialog.dismiss();

			Bitmap original = BitmapFactory.decodeFile(result);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			original.compress(Bitmap.CompressFormat.JPEG, 75, out);
			Bitmap decoded = BitmapFactory
					.decodeStream(new ByteArrayInputStream(out.toByteArray()));

			Drawable d = new BitmapDrawable(getResources(), decoded);
			if (takeFrontNRIC) {
				photoFrontNRIC.setBackgroundDrawable(d);
			} else {
				photoBackNRIC.setBackgroundDrawable(d);
			}
		}
	}


	@Deprecated
	private void loadGender() {
		final String url = SERVERURL + API_GET_GENDERS;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					try {
						listGender = new ArrayList<String>();
						listGenderId = new ArrayList<String>();

						JSONArray jsonArray = new JSONArray(jsonStr);
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject obj = jsonArray.getJSONObject(i);
							obj = obj.getJSONObject("genders");
							listGender.add(obj.getString("gender"));
							listGenderId.add(obj.getString("gender_id"));
						}

						createGenderView();
						loadNRICType();
					}  catch (JSONException e) {
						NetworkUtils.connectionHandler(ProfileRegistrationActivity.this, jsonStr, e.getMessage());
					}
				} else {
					Toast.makeText(context,
							getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context,
				getResources().getString(R.string.registration_profile_title),
				"Loading gender...", true, false);
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

	
	@Deprecated
	protected void loadSkill() {
		final String url = SERVERURL + API_GET_SKILLS;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					try {
						listSkill = new ArrayList<String>();
						listSkillId = new ArrayList<String>();
						listSkillDesc = new ArrayList<String>();

						JSONArray jsonArray = new JSONArray(jsonStr);
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject obj = jsonArray.getJSONObject(i);
							obj = obj.getJSONObject("skills");
							listSkillDesc.add(obj.getString("skill_desc"));
							listSkillId.add(obj.getString("skill_id"));
							listSkill.add(obj.getString("skill_name"));
						}
					} catch (JSONException e) {
						NetworkUtils.connectionHandler(ProfileRegistrationActivity.this, jsonStr, e.getMessage());
					}
				} else {
					Toast.makeText(context,
							getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context,
				getResources().getString(R.string.app_name),
				"Loading skill...", true, false);
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
	
	@Deprecated
	protected void loadNRICType() {
		final String url = SERVERURL + API_GET_IC_TYPES;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					try {
						listNRICType = new ArrayList<String>();
						listNRICTypeId = new ArrayList<String>();

						JSONArray jsonArray = new JSONArray(jsonStr);
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject obj = jsonArray.getJSONObject(i);
							obj = obj.getJSONObject("nric_types");
							listNRICType.add(obj.getString("nric_type"));
							listNRICTypeId.add(obj.getString("nric_type_id"));
						}
						loadSchool();

					} catch (JSONException e) {
							NetworkUtils.connectionHandler(ProfileRegistrationActivity.this, jsonStr, e.getMessage());
					}
				} else {
					Toast.makeText(context,
							getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context,
				getResources().getString(R.string.registration_profile_title),
				"Loading NRIC type...", true, false);
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

	@Deprecated
	protected void loadSchool() {
		final String url = SERVERURL + API_GET_SCHOOLS;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					try {
						listSchool = new ArrayList<String>();
						listSchoolId = new ArrayList<String>();

						JSONArray jsonArray = new JSONArray(jsonStr);
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject obj = jsonArray.getJSONObject(i);
							obj = obj.getJSONObject("schools");
							listSchool.add(obj.getString("school_name"));
							listSchoolId.add(obj.getString("school_id"));
						}
					} catch (JSONException e) {
						NetworkUtils.connectionHandler(ProfileRegistrationActivity.this, jsonStr, e.getMessage());
					}
					
					
					
				} else {
					Toast.makeText(context,
							getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context, "Registration", "Loading school...",
				true, false);
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
}
