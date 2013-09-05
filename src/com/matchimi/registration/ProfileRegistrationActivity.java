package com.matchimi.registration;

import static com.matchimi.CommonUtilities.*;
import static com.matchimi.registration.SharedUtils.updateSettings;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import khandroid.ext.apache.http.HttpResponse;
import khandroid.ext.apache.http.client.HttpClient;
import khandroid.ext.apache.http.client.methods.HttpPost;
import khandroid.ext.apache.http.entity.mime.HttpMultipartMode;
import khandroid.ext.apache.http.entity.mime.MultipartEntity;
import khandroid.ext.apache.http.entity.mime.content.ByteArrayBody;
import khandroid.ext.apache.http.entity.mime.content.StringBody;
import khandroid.ext.apache.http.impl.client.DefaultHttpClient;
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
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.matchimi.HomeActivity;
import com.matchimi.R;
import com.matchimi.api.MyVolley;

public class ProfileRegistrationActivity extends Activity {
	private TextView birthView;
	private TextView skillView;
	private TextView experienceView;
	private TextView nricTypeView;
	private int nricSelected = -1;

	private Button photoFrontNRIC;
	private Button photoBackNRIC;
	private Button profileSubmit;

	private int orientation = 0;
	private boolean takeFrontNRIC = true;

	private DataOutputStream outputStream = null;

	private String lineEnd = "\r\n";
	private String twoHyphens = "--";
	private String boundary = "SwA" + Long.toString(System.currentTimeMillis())
			+ "SwA";

	int bytesRead, bytesAvailable, bufferSize;
	byte[] buffer;
	int maxBufferSize = 1024 * 1024 * 1;

	// Photos configuration
	private String JPEG_FILE_PREFIX = R.string.album_name + "_";
	private String JPEG_FILE_SUFFIX = ".jpg";
	String currentPhotoPath = "";
	public static final SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd", Locale.US);

	public AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	private SharedPreferences settings;
	private EditText fullName;
	private RadioGroup genderView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Remove title bar
		// this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.profileregistration_page);

		fullName = (EditText) findViewById(R.id.regprofile_fullname);
		genderView = (RadioGroup) findViewById(R.id.gender_group);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		settings = getSharedPreferences(PREFS_NAME, 0);

		if (extras != null) {
			Log.d(TAG, "Incoming intent to profile");

			String loggedStatus = extras.getString(LOGGED);
			String userEmail = extras.getString(USER_EMAIL);
			String userPassword = extras.getString(USER_PASSWORD);
			String userFirstName = extras.getString(USER_FIRSTNAME);
			String userLastName = extras.getString(USER_LASTNAME);

			// Set default value for user fullname
			if (userFirstName != null) {
				fullName.setText(userFirstName + " " + userLastName);
			}

			// Check if user register using manual email
			if (loggedStatus == LOGGED_REGISTER) {
				Log.d(TAG, "USER Email " + extras.getString(userEmail));
			}
		}

		// Set date birth
		birthView = (TextView) findViewById(R.id.regprofile_date_of_birth);
		birthView.setOnClickListener(birthListener);

		// Set date birth
		nricTypeView = (TextView) findViewById(R.id.regprofile_nric_type);
		nricTypeView.setOnClickListener(nricTypeListener);

		// Set skills textview and listener
		skillView = (TextView) findViewById(R.id.regprofile_skills);
		skillView.setOnClickListener(skillsListener);

		// Set experience textview and listener
		experienceView = (TextView) findViewById(R.id.regprofile_work_experience);
		experienceView.setOnClickListener(workingExperienceListener);

		// Set take front NRIC button
		photoFrontNRIC = (Button) findViewById(R.id.profile_reg_addfront_button);
		photoFrontNRIC.setOnClickListener(frontNRICListener);

		// Set take back NRIC button
		photoBackNRIC = (Button) findViewById(R.id.profile_reg_addback_button);
		photoBackNRIC.setOnClickListener(backNRICListener);

		profileSubmit = (Button) findViewById(R.id.regprofile_submit_button);
		profileSubmit.setOnClickListener(profileSubmitListener);

		// Load Album Storage Factory based on Android Version
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
	}

	/**
	 * Date birth selection dialog listener
	 */
	private OnClickListener birthListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
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
						    cal.add(Calendar.YEAR, -16);
						    long limitation = cal.getTimeInMillis();
						    
						    if(dtDob > limitation) {
						    	birthView.setText("");
						    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
										ProfileRegistrationActivity.this);
						 
									// set title
									alertDialogBuilder.setTitle("Date Birth Restriction");
						 
									// set dialog message
									alertDialogBuilder
										.setMessage("You must more than 16 years old")
										.setCancelable(false)
										.setPositiveButton("OK",new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,int id) {
												
											}
										  });
						 
										// create alert dialog
										AlertDialog alertDialog = alertDialogBuilder.create();
						 
										// show it
										alertDialog.show();
							
						    } else {
								CharSequence strDate = DateFormat.format(
										"MMMM dd, yyyy", dtDob);
								birthView.setText(strDate);						    	
						    }
						}
					}, 2011, 0, 1);
			dateDlg.setMessage("Your Birthday");
			dateDlg.show();

		}
	};

	private OnClickListener profileSubmitListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			String userNRICBack = settings.getString(USER_NRIC_BACK, "");
			String userNRICFront = settings.getString(USER_NRIC_FRONT, "");
			String errors = "";

			final String front = "/home/matchimi/data/images/nric_front/1377514650_2131034191_20130826-175753_-1142009496.jpg";
			final String back = "/home/matchimi/data/images/nric_front/1377514712_2131034191_20130826-175848_-1295733410.jpg";

//			RequestQueue queue = MyVolley.getRequestQueue();
//			StringRequest myReq = new StringRequest(Method.POST,
//					SERVERURL + API_CREATE_PARTTIMER_PROFILE,
//					createMyReqSuccessListener(),
//					createMyReqErrorListener()) {
//
//				protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
//					Map<String, String> params = new HashMap<String, String>();
//
//					Map<String, String> dataParams = new HashMap<String, String>();
//					Map<String, String> partTimerParams = new HashMap<String, String>();
//					Map<String, String> partTimerWrap = new HashMap<String, String>();
//
//					partTimerParams.put("pt_id", "27");
//					partTimerParams.put("first_name", "Luis");
//					partTimerParams.put("last_name", "Vulton");
//					partTimerParams.put("gender", "male");
//					partTimerParams.put("dob", "01/01/1977");
//					partTimerParams.put("profile_picture", "data/images/profile_pic/profile_pic.jpeg");
//					partTimerParams.put("phone_no", "90009999");
//					partTimerParams.put("address", "Singapore");
//					partTimerParams.put("postal_code", "102312");
//					partTimerParams.put("ic_type", "Student’s Pass");
//					partTimerParams.put("ic_type_id", "3");
//					partTimerParams.put("school_id", "1");
//					partTimerParams.put("ic_expiry_date", "2015-05-01");
//					partTimerParams.put("ic_front_picture", front);
//					partTimerParams.put("ic_back_picture", back);
//					partTimerParams.put("work_experience", "50");
//					partTimerParams.put("profile_source", "system");
//					partTimerParams.put("matric_card_picture", "data/images/matric_card/matric_card.jpeg");
//					partTimerParams.put("matric_card_no", "G1341332G");
//					partTimerParams.put("ec_first_name", "Alex");
//					partTimerParams.put("ec_last_name", "Ferguson");
//					partTimerParams.put("ec_email", "alex@man.com");
//					partTimerParams.put("ec_phone_no", "98776554");
//					partTimerParams.put("ec_address", "singapore");
//					partTimerParams.put("ec_postal_code", "123412");
//					partTimerParams.put("ec_relationship", "father");
//					partTimerParams.put("bank_name", "POSB");
//					partTimerParams.put("bank_account_type", "POSB Savings Plus");
//					partTimerParams.put("bank_account_no", "12-098-134");
//					partTimerParams.put("bank_branch_name", "Clementi Branch");
//
//					partTimerWrap.put("part_timer", partTimerParams.toString());
//					dataParams.put("data", partTimerWrap.toString());   
//
//					return params;
//				};
//			};
//			queue.add(myReq);

			if (fullName.getText().toString() == "") {
				errors += "* Full name\n";
			}

			if (genderView.getCheckedRadioButtonId() < 0) {
				errors += "* Gender\n";
			}

			if (birthView.getText().toString() == "") {
				errors += "* Birthday\n";

			}

			if (nricTypeView.getText().toString() == "") {
				errors += "* NRIC Type\n";
			}

			if (userNRICFront == "") {
				errors += "* NRIC front photo\n";
			}

			if (userNRICBack == "") {
				errors += "* NRIC back photo\n";
			}

			if (skillView.getText().toString() == "") {
				errors += "* Skills\n";
			}

			if (experienceView.getText().toString() == "") {
				errors += "* Working Experience\n";
			}


			if (errors == "") {
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(LOGGED, "true");
				editor.commit();

				Intent intent = new Intent(ProfileRegistrationActivity.this,
						HomeActivity.class);
				startActivity(intent);
				finish();
			} else {
				String showError = "Please complete :\n\n" + errors;

				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						ProfileRegistrationActivity.this);
		 
					// set title
					alertDialogBuilder.setTitle("Invalid Data correct");
		 
					// set dialog message
					alertDialogBuilder
						.setMessage(showError)
						.setCancelable(false)
						.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								
							}
						  });
		 
						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();
		 
						// show it
						alertDialog.show();
			}

		}
	};

	protected ErrorListener createMyReqErrorListener() {
		// TODO Auto-generated method stub
		return null;
	}

	protected Listener<String> createMyReqSuccessListener() {
		// TODO Auto-generated method stub
		return null;
	}

	

	/**
	 * NRIC type selection list-checkbox listener
	 */
	private OnClickListener nricTypeListener = new OnClickListener() {

		@Override
		public void onClick(View view) {

			final CharSequence[] choiceList = { "Citizen", "Resident" };
			final CharSequence selectedText = "";

			AlertDialog.Builder builder = new AlertDialog.Builder(
					ProfileRegistrationActivity.this);

			// Set the dialog title
			builder.setTitle("Select NRIC Type");

			builder.setSingleChoiceItems(choiceList, nricSelected,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							nricSelected = which;
						}
					})
					.setCancelable(false)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									nricTypeView
											.setText(choiceList[nricSelected]);
								}
							});

			Dialog dialog = builder.create();
			dialog.show();

		}
	};

	/**
	 * Skills selection list-checkbox listener
	 */
	private OnClickListener skillsListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			final ArrayList<Integer> mSelectedItems = new ArrayList(); // Where
			// we
			// track
			// the
			// selected
			// items
			final ArrayList<CharSequence> selectedSkills = new ArrayList<CharSequence>();
			final List<CharSequence> items = new ArrayList<CharSequence>();

			items.add("Accounting");
			items.add("Ushering");
			items.add("Waiting Tables");
			items.add("Administration");
			items.add("Cleaner");
			items.add("HouseKeeper");
			items.add("Writer");
			items.add("IT Support");
			items.add("Curator");

			AlertDialog.Builder builder = new AlertDialog.Builder(
					ProfileRegistrationActivity.this);

			// Set the dialog title
			builder.setTitle("Select Skills")
					// Specify the list array, the items to be selected by
					// default (null for none),
					// and the listener through which to receive callbacks when
					// items are selected
					.setMultiChoiceItems(
							items.toArray(new CharSequence[items.size()]),
							null,
							new DialogInterface.OnMultiChoiceClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									if (isChecked) {
										// If the user checked the item, add it
										// to the selected items
										mSelectedItems.add(which);
									} else if (mSelectedItems.contains(which)) {
										// Else, if the item is already in the
										// array, remove it
										mSelectedItems.remove(Integer
												.valueOf(which));
									}
								}
							})
					// Set the action buttons
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									// User clicked OK, so save the
									// mSelectedItems results somewhere
									// or return them to the component that
									// opened the dialog
									selectedSkills.clear();

									for (int i = 0; i < mSelectedItems.size(); i++) {
										selectedSkills.add(items
												.get(mSelectedItems.get(i)));
									}

									// Convert ArrayList into String
									// comma-separated
									String selectedSkillsList = selectedSkills
											.toString();
									String selectedSkillsSet = selectedSkillsList
											.substring(
													1,
													selectedSkillsList.length() - 1)
											.replace(", ", ", ");
									skillView.setText(selectedSkillsSet);
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									// Nothing happened
								}
							});

			Dialog dialog = builder.create();
			dialog.show();
		}
	};

	/**
	 * Working experience list-checkbox listener
	 */
	private OnClickListener workingExperienceListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			final ArrayList<Integer> mSelectedItems = new ArrayList(); // Where
			// we
			// track
			// the
			// selected
			// items
			final ArrayList<CharSequence> selectedExperiences = new ArrayList<CharSequence>();
			final List<CharSequence> items = new ArrayList<CharSequence>();
			items.add("1 Year");
			items.add("2 Years");
			items.add("3 Years");
			items.add("3 Years+");

			AlertDialog.Builder builder = new AlertDialog.Builder(
					ProfileRegistrationActivity.this);

			// Set the dialog title
			builder.setTitle("Select Working Experience")
					// Specify the list array, the items to be selected by
					// default (null for none),
					// and the listener through which to receive callbacks when
					// items are selected
					.setMultiChoiceItems(
							items.toArray(new CharSequence[items.size()]),
							null,
							new DialogInterface.OnMultiChoiceClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									if (isChecked) {
										// If the user checked the item, add it
										// to the selected items
										mSelectedItems.add(which);
									} else if (mSelectedItems.contains(which)) {
										// Else, if the item is already in the
										// array, remove it
										mSelectedItems.remove(Integer
												.valueOf(which));
									}
								}
							})
					// Set the action buttons
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									// User clicked OK, so save the
									// mSelectedItems results somewhere
									// or return them to the component that
									// opened the dialog
									selectedExperiences.clear();

									for (int i = 0; i < mSelectedItems.size(); i++) {
										selectedExperiences.add(items
												.get(mSelectedItems.get(i)));
									}

									// Convert ArrayList into String
									// comma-separated
									String selectedExperiencesList = selectedExperiences
											.toString();
									String selectedExperienceSet = selectedExperiencesList
											.substring(
													1,
													selectedExperiencesList
															.length() - 1)
											.replace(", ", ", ");
									experienceView
											.setText(selectedExperienceSet);
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {

								}
							});

			Dialog dialog = builder.create();
			dialog.show();

		}
	};

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
		// File file = new File(currentPhotoPath);
		// new backgroundUploadMultipart(SERVERURL +
		// API_UPLOAD_FRONT_NRIC_PHOTOS, file).execute();

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
		String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss")
				.format(new Date());
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
				ExifInterface exif = new ExifInterface(currentPhotoPath);
				orientation = Integer.parseInt(exif
						.getAttribute(ExifInterface.TAG_ORIENTATION));

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
		case CAMERA_REQUEST: {
			if (resultCode == Activity.RESULT_OK) {
				handleCameraPhoto(data);
			}
			break;
		}
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
	private class postData extends AsyncTask<String, Void, Void> {
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
		protected Void doInBackground(String... params) {

			String filePath = params[0];
			File file = new File(filePath);
			String filename = file.getName();

			bm = BitmapFactory.decodeFile(filePath);

			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bm.compress(CompressFormat.JPEG, 95, bos);

				byte[] data = bos.toByteArray();
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost postRequest = new HttpPost(SERVERURL
						+ API_UPLOAD_FRONT_NRIC_PHOTOS);

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
					updateSettings(settings, USER_NRIC_FRONT, s.toString());
				} else {
					updateSettings(settings, USER_NRIC_BACK, s.toString());
				}

			} catch (Exception e) {
				// handle exception here
				Log.e(e.getClass().getName(), e.getMessage());
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dialog.dismiss();
		}
	}

	/**
	 * Add params POST to HTTP Multipart file
	 * 
	 * @param outputStream
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	private void addParams(DataOutputStream outputStream, String key,
			String value) throws IOException {
		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
		outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key
				+ "\"" + lineEnd);
		outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
		outputStream.writeBytes(lineEnd + value + lineEnd);
		outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	}

	private class backgroundUploadMultipart extends
			AsyncTask<Void, Integer, Void> implements
			DialogInterface.OnCancelListener {
		private ProgressDialog progressDialog;
		private String url;
		private File file;

		public backgroundUploadMultipart(String url, File file) {
			this.url = url;
			this.file = file;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(
					ProfileRegistrationActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("Uploading...");
			progressDialog.setCancelable(false);
			progressDialog.setMax((int) file.length());
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... v) {
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection connection = null;
			String fileName = file.getName();
			try {
				connection = (HttpURLConnection) new URL(url).openConnection();

				// Allow Inputs & Outputs
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setUseCaches(false);

				// Enable POST method
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);

				outputStream = new DataOutputStream(
						connection.getOutputStream());
				String filename = new File(currentPhotoPath).getName();
				addParams(outputStream, "filename", filename);

				outputStream
						.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
								+ fileName + "\"" + lineEnd);
				outputStream.writeBytes(lineEnd);
				outputStream.flush();

				int progress = 0;
				int bytesRead = 0;
				byte buf[] = new byte[1024];
				BufferedInputStream bufInput = new BufferedInputStream(
						new FileInputStream(file));
				while ((bytesRead = bufInput.read(buf)) != -1) {
					// write output
					outputStream.write(buf, 0, bytesRead);
					outputStream.flush();
					progress += bytesRead;
					// update progress bar
					publishProgress(progress);
				}
				outputStream.writeBytes(lineEnd);
				outputStream.writeBytes(twoHyphens + boundary + twoHyphens
						+ lineEnd);

				outputStream.flush();
				outputStream.close();

				// Get server response
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				String line = "";
				StringBuilder builder = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				Log.d(TAG, builder.toString());

			} catch (Exception e) {
				// Exception
			} finally {
				if (connection != null)
					connection.disconnect();
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressDialog.setProgress((int) (progress[0]));
		}

		@Override
		protected void onPostExecute(Void v) {
			progressDialog.dismiss();
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			cancel(true);
			dialog.dismiss();
		}
	}

}
