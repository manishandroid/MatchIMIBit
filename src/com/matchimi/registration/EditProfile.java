package com.matchimi.registration;

import static com.matchimi.CommonUtilities.*;
import com.bugsense.trace.BugSenseHandler;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import khandroid.ext.apache.http.HttpEntity;
import khandroid.ext.apache.http.HttpResponse;
import khandroid.ext.apache.http.client.HttpClient;
import khandroid.ext.apache.http.client.methods.HttpGet;
import khandroid.ext.apache.http.client.methods.HttpPost;
import khandroid.ext.apache.http.client.methods.HttpUriRequest;
import khandroid.ext.apache.http.entity.mime.HttpMultipartMode;
import khandroid.ext.apache.http.entity.mime.MultipartEntity;
import khandroid.ext.apache.http.entity.mime.content.ByteArrayBody;
import khandroid.ext.apache.http.entity.mime.content.StringBody;
import khandroid.ext.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.matchimi.Api;
import com.matchimi.CommonUtilities;
import com.matchimi.ProfileModel;
import com.matchimi.R;
import com.matchimi.ValidationUtilities;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class EditProfile extends SherlockActivity {

	private final int TAKE_IMG_PROFILE = 0;
	private final int TAKE_IMG_STUDENT_FRONT = 9;
	private final int TAKE_IMG_STUDENT_BACK = 10;	
	private final int TAKE_IMG_IC_BACK = 2;
	private final int TAKE_IMG_IC_FRONT = 3;
	private final int TAKE_PHOTO_CODE = 4;
	private final int SELECT_PHOTO = 5;
	private final int TAKE_IMG_BANK_STATEMENT = 6;
	private final int TAKE_VIDEO = 7;
	private final int SELECT_VIDEO = 8;
	
	public AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	
	private Context context;

	private ProfileModel profileInfo;

	private List<String> listGender;
	private List<String> listStudent;
	private List<String> listDependent;
	private List<String> listGenderId;
	private List<String> listNRICType;
	private List<String> listNRICTypeId;
	private List<String> listSkill;
	private List<String> listSkillId;
	private List<String> listSkillDesc;
	private List<String> listSchool;
	private List<String> listSchoolId;
	private List<CharSequence> listWorkExperience;
	private List<String> listWorkExpID;
	private List<Boolean> listSkillsSelected;
	
	private int selectedWorkIdx = -1;
	private List<CharSequence> listECRelation;
	private int selectedECRelationIdx = -1;

	private ProgressDialog progress;
	private JSONParser jsonParser = null;
	private String jsonStr = null;
	private String responseString = null;
	
	private int selectedIdx = -1;
	private int selectedPhotoId = -1;

	private SharedPreferences settings;

	private String pt_id = null;
	
	private EditText fullnameView;	
	private TextView schoolView;
	private TextView statusView;
	private TextView dobView;
	private EditText nricNumberView;
	private EditText phoneView;
	
	private TextView expiryDateView;
	private TextView skillView;	
	
	private EditText bankNameView;
	private EditText bankAccountNumberView;
	private EditText bankAccountNameView;
	private EditText bankBranchNameView;
	
	private EditText EcNameView;
	private EditText EcPhoneView;
	private TextView EcRelationView;	
	
//	private EditText bankAccTypeView;	
//	private EditText addressView;
//	private EditText postalCodeView;
//	private EditText EcAddressView;
//	private EditText EcPostalCodeView;
//	private EditText EcEmailView;	
	
	private TextView workExperienceView;
	private RadioGroup genderView;
	private RadioGroup studentView;
	
	private ImageView imageProfileView;
	private ImageView imageICBackView;
	private ImageView imageICFrontView;
	private ImageView imageStudentCardBackView;
	private ImageView imageStudentCardFrontView;	
	private ImageView imageBankStatementView;
	private ImageView visumeImageView;
	
	private Calendar defaultBirthday = Calendar.getInstance();
	private Calendar defaultExpire = Calendar.getInstance();
	private final String dobFormat = "dd/MM/yyyy";
	private final SimpleDateFormat dobDateFormat = new SimpleDateFormat(dobFormat, Locale.US);	
	private final SimpleDateFormat facebookDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	private RelativeLayout expiryDateLayout;
	private boolean isStudentInfoRequired = false;
	private boolean isUserCompleteInput;
	
	private String profileImagePath;
	private String studentFrontImagePath;
	private String studentBackImagePath;	
	private String icFrontImagePath;
	private String icBackImagePath;
	private String bankStatementPath;	
	private String visumePath;	
	
	private String uploadImage = null;
	private String uploadURL = null;
	private Uri mVideoUri = null;
	private VideoView mVideoView;
	
	private Boolean isOpeningPage = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BugSenseHandler.initAndStartSession(EditProfile.this, CommonUtilities.BUGSENSE_KEY);

		settings = getSharedPreferences(CommonUtilities.PREFS_NAME,
				Context.MODE_PRIVATE);
		if (settings.getInt(CommonUtilities.SETTING_THEME,
				CommonUtilities.THEME_LIGHT) == CommonUtilities.THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.edit_profile);
		context = this;

		pt_id = settings.getString(CommonUtilities.USER_PTID, null);

		Date date = new Date();
		defaultExpire.setTime(date);
		defaultExpire.add(Calendar.DAY_OF_MONTH, 1);
		
		fullnameView = (EditText) findViewById(R.id.field_edit_fullname);
//		fullnameView.setEnabled(false);

		phoneView = (EditText) findViewById(R.id.editPhoneNumber);
		EcNameView = (EditText) findViewById(R.id.editEcName);
		EcPhoneView = (EditText) findViewById(R.id.editEcPhone);		
		bankNameView = (EditText) findViewById(R.id.editBankName);
		bankAccountNumberView = (EditText) findViewById(R.id.editAccNumber);
		bankAccountNameView = (EditText) findViewById(R.id.editBankAccName);
		bankBranchNameView = (EditText) findViewById(R.id.editBranch);				
		
		genderView = (RadioGroup) findViewById(R.id.gender_group);
		for(int i=0; i< genderView.getChildCount(); i++) {
			((RadioButton) genderView.getChildAt(i)).setEnabled(false);
		}
		
		studentView = (RadioGroup) findViewById(R.id.student_group);
		listStudent = new ArrayList<String>();
		listStudent.add("Yes");
		listStudent.add("No");
		createStudentView(studentView);
		
//		EcEmailView = (EditText) findViewById(R.id.editEcEmail);
//		addressView = (EditText) findViewById(R.id.editAddress);
//		postalCodeView = (EditText) findViewById(R.id.editPostalCode);
//		EcAddressView = (EditText) findViewById(R.id.editEcAddress);
//		EcPostalCodeView = (EditText) findViewById(R.id.editEcPostCode);
//		bankAccTypeView = (EditText) findViewById(R.id.editBankAccType);

		EcRelationView = (TextView) findViewById(R.id.field_edit_ECRelation);
		EcRelationView.setOnClickListener(ECRelationListener);
		CharSequence[] ecRelationshipArray = getResources().getStringArray(R.array.ecrelationship_array);
		listECRelation = Arrays.asList(ecRelationshipArray);
		
		// Set date of birth fields and listener
		dobView = (TextView) findViewById(R.id.field_edit_date_of_birth);
		dobView.setOnClickListener(dobListener);
		dobView.setEnabled(false);

		// Set NRIC type fields and listener
		statusView = (TextView) findViewById(R.id.field_edit_nric_type);
		statusView.setOnClickListener(statusListener);
		statusView.setEnabled(false);
		
		// Set school fields and listener
		schoolView = (TextView) findViewById(R.id.textSchool);
		schoolView.setOnClickListener(schoolListener);
		
		RelativeLayout workExperienceLayout = (RelativeLayout) findViewById(R.id.profile_reg_workexperience_layout);
		workExperienceLayout.setVisibility(View.GONE);
		
		// Set profile upload picture
		imageProfileView = (ImageView) findViewById(R.id.buttonPicture);
		imageProfileView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				askImageFrom(TAKE_IMG_PROFILE);
			}
		});

		// Set NRIC back upload picture
		imageICBackView = (ImageView) findViewById(R.id.profile_reg_addback_button);
		imageICBackView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				askImageFrom(TAKE_IMG_IC_BACK);
			}
		});

		// Set NRIC front upload picture
		imageICFrontView = (ImageView) findViewById(R.id.profile_reg_addfront_button);
		imageICFrontView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				askImageFrom(TAKE_IMG_IC_FRONT);
			}
		});
		
		// Setup student card front
		imageStudentCardFrontView = (ImageView) findViewById(R.id.card_student_front_button);
		imageStudentCardFrontView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				askImageFrom(TAKE_IMG_STUDENT_FRONT);
			}
		});
		
		// Setup student card bank
		imageStudentCardBackView = (ImageView) findViewById(R.id.card_student_back_button);
		imageStudentCardBackView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				askImageFrom(TAKE_IMG_STUDENT_BACK);
			}
		});

		// Setup bank statement
		imageBankStatementView = (ImageView) findViewById(R.id.buttonBankStatement);
		imageBankStatementView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				askImageFrom(TAKE_IMG_BANK_STATEMENT);
			}
		});
		
		// Setup visume
		visumeImageView = (ImageView) findViewById(R.id.buttonVisume);
		visumeImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				askVideoFrom(SELECT_VIDEO);
			}
		});
		
		Button submit = (Button) findViewById(R.id.field_edit_submit_button);
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				checkInput();
			}
		});
		
		// Showing student ID or not
		showStudentInfo(false);

		if (savedInstanceState == null) {
			loadProfileWithDefaultData(false);
		} else {
			uploadImage = savedInstanceState.getString("uploadImage");	
			uploadURL = savedInstanceState.getString("uploadURL");
			selectedPhotoId = savedInstanceState.getInt("selectedPhotoId");				
			
			loadProfileWithDefaultData(true);
		}
		
		// WARNING: all code below are @deprecated
		
		workExperienceView = (TextView) findViewById(R.id.field_edit_work_experience);
		workExperienceView.setOnClickListener(workExperienceListener);
		
		CharSequence[] workExpArray = getResources().getStringArray(R.array.workexperience_array);
		listWorkExperience = Arrays.asList(workExpArray);
		
		String[] workExpIDArray = getResources().getStringArray(R.array.workexperience_id_array);
		listWorkExpID = Arrays.asList(workExpIDArray);
		
		// Set date of expire fields and listener
		expiryDateView = (TextView) findViewById(R.id.editExpireDate);
		expiryDateView.setOnClickListener(expiredListener);

		// Set date of skills fields and listener
		skillView = (TextView) findViewById(R.id.field_edit_skills);
		skillView.setOnClickListener(skillsListener);
		
		// Nric number
		nricNumberView = (EditText) findViewById(R.id.editNricNumber);
		
		// Set expiry layout
		expiryDateLayout = (RelativeLayout) findViewById(R.id.expiry_date_layout);				
	}
	
	/**
	 * Checking input before submitting into server
	 */
	protected void checkInput() {
		int idx = 0;
		isUserCompleteInput = true;

		String tmp = "";
		String errors = "";
		
		// Check Full Name
		tmp = fullnameView.getText().toString().trim();
		if (tmp.length() > 0) {
			profileInfo.setFullName(tmp);
		} else {
			errors += "" + getString(R.string.edit_profile_fullname) + "\n";
		}

		// Check if part-timer is student
		if(profileInfo.getIsStudent()) {			
			// Set schools as optional without any effects on isUserCompleteInput
			tmp = schoolView.getText().toString().trim();
			if (tmp.length() > 0) {
				profileInfo.setSchool(tmp);
			} else {
				errors += "* " + getString(R.string.registration_profile_school) + "\n";
				profileInfo.setSchool("");
			}
		}
		
		// Check phone number
		tmp = phoneView.getText().toString().trim();
		if (tmp.length() > 0) {
			profileInfo.setPhone_num(tmp);
		} else {
			profileInfo.setPhone_num("");				
			isUserCompleteInput = false;
			errors += "* " + getString(R.string.phone_number) + "\n";
		}

		// Emergency Contact Name
		tmp = EcNameView.getText().toString().trim();
		if (tmp.length() > 0) {
			profileInfo.setEc_name(tmp);
		} else {
			profileInfo.setEc_name("");
			isUserCompleteInput = false;
			errors += "* " + getString(R.string.edit_profile_label_emergency_contact) + " name\n";
		}
		
		// Check Emergency Contact Phone
		tmp = EcPhoneView.getText().toString().trim();
		if (tmp.length() > 0) {
			profileInfo.setEc_phone(tmp);
		}else {
			profileInfo.setEc_phone("");
			isUserCompleteInput = false;
			errors += "* " + getString(R.string.edit_profile_label_emergency_contact) + " phone\n";
		}
		
		// Check Emergency Relationship
		tmp = EcRelationView.getText().toString().trim();
		if (tmp.length() > 0) {
			profileInfo.setEc_relationship(tmp);
		} else {
			profileInfo.setEc_relationship("");
			isUserCompleteInput = false;
			errors += "* " + getString(R.string.edit_profile_label_emergency_contact) + " relationship\n";
		}
		
		Log.d(CommonUtilities.TAG, "Error " + errors + "");
		
		// If all fields completed, then create profile
		if (errors.length() == 0) {
			
			// Check Date of Birth
			tmp = dobView.getText().toString().trim();
			if (tmp.length() > 0) {
				profileInfo.setDob(tmp);
			} else {
				isUserCompleteInput = false;
			}
			
			// Check IC status working
			tmp = statusView.getText().toString().trim();
			if (tmp.length() > 0) {
				profileInfo.setIc_type(tmp);
			} else {
				isUserCompleteInput = false;
			}
			
			// Set bank data
			tmp = bankNameView.getText().toString().trim();
			if(tmp.length() > 0) {
				profileInfo.setBank_name(tmp);
			}
			
			// Set bank data
			tmp = bankAccountNumberView.getText().toString().trim();
			if(tmp.length() > 0) {
				profileInfo.setBank_account_no(tmp);
			}
			
			tmp = bankBranchNameView.getText().toString().trim();
			if(tmp.length() > 0) {
				profileInfo.setBank_branch_name(tmp);
			}
			
			tmp = bankAccountNameView.getText().toString().trim();
			if(tmp.length() > 0) {
				profileInfo.setBank_account_name(tmp);
			}
			
			doCreateProfile();
			
		} else {
			
			// Showing errors if fields not completed
			String showError = "Please complete :\n\n" + errors;
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
					EditProfile.this);

			// set title
			dialogBuilder.setTitle(getString(R.string.profile_header));
			
			// set dialog message
			dialogBuilder
					.setMessage(showError)
					.setPositiveButton(getString(R.string.ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// Nothing to do here
								}
							});

			// create alert dialog
			Dialog dialog = dialogBuilder.create();
			// show it
			dialog.show();
		}
	}
	
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
			
			loadNRICTypeJSON(jsonStr);
			
		} catch (JSONException e) {
			NetworkUtils.connectionHandler(EditProfile.this, jsonStr, e.getMessage());
			Log.e(TAG, "Error while get genders >>> " + e.getMessage());
		}
	}

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
//			loadSkillJson(jsonStr);
		} catch (JSONException e) {
			NetworkUtils.connectionHandler(EditProfile.this, jsonStr, e.getMessage());
			Log.e(TAG, "Error get ic types >>> " + e.getMessage());
		}
	}
	
	
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

			loadProfileJson(jsonStr);
			
		} catch (Exception e) {
			NetworkUtils.connectionHandler(EditProfile.this, jsonStr, e.getMessage());
			Log.e(TAG, "Error school >>> " + e.getMessage());
		}
	}
	
	private void loadProfileJson(String jsonStr) {
		profileInfo = new ProfileModel();
		
		try {
			JSONObject obj = new JSONObject(jsonStr);
			JSONObject wrapperObj = obj.getJSONObject(CommonUtilities.PARAM_PROFILE_DEFAUT_PART_TIMER);
			obj = wrapperObj.getJSONObject("part_timers");
			
//			Log.d(TAG, "User profile data\n" + obj.toString());
			
			// Convert date birth format
			Date dobDate = facebookDateFormat.parse(obj.getString("dob"));
			String dobConvert = dobDateFormat.format(dobDate);
			profileInfo.setDob(dobConvert);						
			
			profileInfo.setEmail(obj.getString("email"));
			profileInfo.setWork_exp(obj
					.getString("work_experience"));
			profileInfo.setFullName(obj.getString("full_name"));
			profileInfo.setPhone_num(obj.getString("phone_no"));
			profileInfo.setGender(obj.getString("gender"));
			profileInfo.setGenderID(obj.getString("gender_id"));
			
			String pictureTemp = obj.getString("profile_picture");
					
			if(pictureTemp != null && pictureTemp != "" &&
					pictureTemp != getResources().getString(R.string.image_not_found)){
				profileInfo.setProfile_pic(obj
						.getString("profile_picture"));					 
			}

			pictureTemp = obj.getString("ic_back_picture");
			if(pictureTemp != null && pictureTemp != "" &&
					pictureTemp != getResources().getString(R.string.image_not_found)){
				profileInfo.setIc_back_picture(obj
						.getString("ic_back_picture"));				
			}
			
			pictureTemp = obj.getString("ic_front_picture");
			if(pictureTemp != null && pictureTemp != "" &&
					pictureTemp != getResources().getString(R.string.image_not_found)){
				profileInfo.setIc_front_picture(obj
						.getString("ic_front_picture"));			
			}			
			
			profileInfo.setIc_no(obj.getString("ic_no"));
			profileInfo.setIc_type(obj.getString("ic_type"));
			profileInfo.setIc_type_id(obj.getString("ic_type_id"));
			profileInfo.setVisume(obj.getString("visume"));
			profileInfo.setVisumeFile(obj.getString("visume_file"));
			
			profileInfo.setStudent(obj.getString("is_student"));
			
			if(profileInfo.getIsStudent()) {
				JSONObject objStudent = obj.getJSONObject("student_details");
				profileInfo.setSchool(objStudent.getString("school_name"));

				String studentBackCardPicture = objStudent.optString("sid_back_pic");
				if(studentBackCardPicture != "" &&
						studentBackCardPicture != getResources().getString(R.string.image_not_found)) {
					profileInfo.setStudentBackCard_picture(objStudent.getString("sid_back_pic"));							
				}
				
				String studentFrontCardPicture = objStudent.optString("sid_front_pic");
				if(studentFrontCardPicture != "" &&
						studentFrontCardPicture != getResources().getString(R.string.image_not_found)) {
					profileInfo.setStudentFrontCard_picture(objStudent.getString("sid_front_pic"));							
				}
			}
			
			JSONObject objBankAccount = obj.getJSONObject("bank_account");
			
			// Loading Bank data
			profileInfo.setBank_name(objBankAccount.getString("bank_name"));
			profileInfo.setBank_account_no(objBankAccount
					.getString("bank_account_no"));
			profileInfo.setBank_branch_name(objBankAccount
					.getString("bank_branch_name"));
			profileInfo.setBank_account_name(objBankAccount
					.getString("bank_account_name"));
			profileInfo.setBank_statement(objBankAccount
					.getString("bank_statement"));
			
			JSONObject objECAccount = obj.getJSONObject("emergency_contact");
			
			// Loading Emergency Contact data
			profileInfo.setEc_name(objECAccount.getString("full_name"));
			profileInfo.setEc_phone(objECAccount.getString("phone_no"));
			profileInfo.setEc_relationship(objECAccount.getString("relationship"));

//			String s = obj.getString("skills");
//			if (s != null && !s.equalsIgnoreCase("null") && s.length() > 2) {
//				s = s.substring(1, s.length() - 1);
//				String[] arr = s.split(",");
//				List<Integer> l = new ArrayList<Integer>();
//				for (String tmp : arr) {
//					l.add(Integer.parseInt(tmp.trim()));
//				}
//				profileInfo.setSkill(l);
//			}
			
			// Update user is_verified status
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(CommonUtilities.USER_IS_VERIFIED, 
					obj.getString(CommonUtilities.PARAM_PROFILE_IS_VERIFIED));
			editor.commit();
			
			// Marks if user just open the page then don't do form validation
			isOpeningPage = true;
			checkUserComplete();
			
			updateLayout();			
			
		} catch (Exception e) {
			NetworkUtils.connectionHandler(EditProfile.this, jsonStr, e.getMessage());

			Log.e(TAG, "Get profile error >> " + e.getMessage());
			Toast.makeText(context,
					getString(R.string.edit_get_profile_error),
					Toast.LENGTH_SHORT).show();
		}
	}
	
	
	/**
	 * Update layout after get profileInfo
	 */
	protected void updateLayout() {
		if(profileInfo == null) {
			Log.d(TAG, "Profile null");
		}
		
		fullnameView.setText(profileInfo.getFullName());
		dobView.setText(profileInfo.getDob());
		phoneView.setText(profileInfo.getPhone_num());

		statusView.setText(profileInfo.getIc_type());
		schoolView.setText(profileInfo.getSchool());

		// Set nric number
		nricNumberView.setText(profileInfo.getIc_no());
		EcNameView.setText(profileInfo.getEc_name());
		EcPhoneView.setText(profileInfo.getEc_phone());
		EcRelationView.setText(profileInfo.getEc_relationship());			
		
		// Set bank data
		bankNameView.setText(profileInfo.getBank_name());
		bankAccountNumberView.setText(profileInfo.getBank_account_no());
		bankBranchNameView.setText(profileInfo.getBank_branch_name());
		bankAccountNameView.setText(profileInfo.getBank_account_name());
		
		createGenderView(genderView);
		createStudentView(studentView);
		
		if(profileInfo.getIsStudent()) {
			showStudentInfo(true);
		} else {
			showStudentInfo(false);			
		}
		
		updateImageLayout();
		
//		addressView.setText(profileInfo.getAddress());
//		postalCodeView.setText(profileInfo.getPost_code());		
//		if(profileInfo.getIc_expired() != "") {
//			expiryDateView.setText(profileInfo.getIc_expired());			
//		}
		
//		EcEmailView.setText(profileInfo.getEc_email());		
//		EcAddressView.setText(profileInfo.getEc_address());
//		EcPostalCodeView.setText(profileInfo.getEc_post_code());		

//		bankAccTypeView.setText(profileInfo.getBank_acc_type());
//		listSkillsSelected = new ArrayList<Boolean>();
//		
//		if (profileInfo.getSkill() != null) {
//			List<String> tmp = new ArrayList<String>();
//			Log.d(TAG, "Skills >>> " + profileInfo.getSkill().toString());
//			
//			for (int i = 0; i < profileInfo.getSkill().size(); i++) {
//				int x = listSkillId.indexOf("" + profileInfo.getSkill().get(i));
//				tmp.add(listSkill.get(x));
//			}
//			
//			for(String skillID : listSkillId) {
//				Boolean isSkillMatch = false;
//				
//				for(int profileSkillID : profileInfo.getSkill()) {
//					if(profileSkillID == Integer.parseInt(skillID)) {
//						isSkillMatch = true;
//					}
//				}
//				
//				listSkillsSelected.add(isSkillMatch);
//			}
//			
//			String s = tmp.toString();
//			Log.d(TAG, ">>> " + s);
//			String skillString = s.substring(1, s.length() - 1).replace(
//					", ", ", ");
//			skillView.setText(skillString);
//			
//		} else {
//			for(String skillID : listSkillId) {
//				listSkillsSelected.add(false);
//			}
//		}

//		String workExperienceText = "";
//		try {
//			int selectedWorkID = Integer.parseInt(profileInfo.getWork_exp());			
//			if(selectedWorkID > 3) {
//				selectedWorkID = 0;
//			} else {
//				selectedWorkID = listWorkExpID.indexOf(profileInfo.getWork_exp());						
//			}
//			workExperienceText = (String) listWorkExperience.get(selectedWorkID);
//		} catch(Exception e) {
//
//		}
//		workExperienceView.setText(workExperienceText);
	}
	
	/**
	 * Refreshing all images imageview in edit profile
	 */
	private void updateImageLayout() {
		Log.d(CommonUtilities.TAG, "Profile info " + profileInfo.getProfile_pic());
		
		// Update and show profile picture
		if(profileInfo.getProfile_pic() != null && profileInfo.getProfile_pic().length() > 0) {
			String url = SERVERURL + CommonUtilities.API_GET_PROFILE_PIC + "?"
					+ PARAM_PT_ID + "=" + pt_id;
			profileImagePath = checkAndDownload(profileInfo.getProfile_pic(), url);
			updateImageProfile(profileImagePath);		
		}

		// Update and show IC front	
//		Log.d(TAG, "Updating ic front" + profileInfo.getIc_front_picture());
		if(profileInfo.getIc_front_picture() != null && profileInfo.getIc_front_picture().length() > 0) {
			String url = SERVERURL + CommonUtilities.API_GET_IC_FRONT_PIC_BY_PT_ID + "?"
					+ PARAM_PT_ID + "=" + pt_id;
			icFrontImagePath = checkAndDownload(profileInfo.getIc_front_picture(), url);
			updateImageICFront(icFrontImagePath);		
		}
		
		// Update and show Student IC back	
//		Log.d(TAG, "Updating ic back" + profileInfo.getIc_back_picture());
		if(profileInfo.getIc_back_picture() != null && profileInfo.getIc_back_picture().length() > 0) {
			String url = SERVERURL + CommonUtilities.API_GET_IC_BACK_PIC_BY_PT_ID + "?"
					+ PARAM_PT_ID + "=" + pt_id;
			icBackImagePath = checkAndDownload(profileInfo.getIc_back_picture(), url);
			updateImageICBack(icBackImagePath);
		}
		
		// Update and show Student ID front
		if(profileInfo.getStudentBackCard_picture() != null && profileInfo.getStudentBackCard_picture().length() > 0) {			
			String url = SERVERURL + CommonUtilities.API_GET_STUDENT_BACK_PHOTOS + "?"
					+ PARAM_PT_ID + "=" + pt_id;
			studentBackImagePath = checkAndDownload(profileInfo.getStudentBackCard_picture(), url);
			updateImageStudentCardBackPicture(studentBackImagePath);	
		}
		
		// Update and show Student ID back		
		if(profileInfo.getStudentFrontCard_picture() != null && profileInfo.getStudentFrontCard_picture().length() > 0) {			
			String url = SERVERURL + CommonUtilities.API_GET_STUDENT_FRONT_PHOTOS + "?"
					+ PARAM_PT_ID + "=" + pt_id;
			studentFrontImagePath = checkAndDownload(profileInfo.getStudentFrontCard_picture(), url);
			updateImageStudentCardFrontPicture(studentFrontImagePath);	
		}
		
		// Update and show bank statement	
		if(profileInfo.getBank_statement() != null && profileInfo.getBank_statement().length() > 0) {			
			String url = SERVERURL + CommonUtilities.API_GET_BANK_STATEMENT_BY_PT_ID + "?"
					+ PARAM_PT_ID + "=" + pt_id;
			bankStatementPath = checkAndDownload(profileInfo.getBank_statement(), url);
			Log.d(TAG, "Updating bank statement" + profileInfo.getBank_statement() + " " + bankStatementPath);
			updateImageBankStatement(bankStatementPath);
		}
		
		if(profileInfo.getVisumeFile() != null && profileInfo.getVisumeFile().length() > 0) {		
			Log.d(CommonUtilities.TAG, "Video " + profileInfo.getVisumeFile());
			
			String url = SERVERURL + CommonUtilities.API_GET_VISUME_BY_PT_ID + "?"
					+ PARAM_PT_ID + "=" + pt_id;
			visumePath = checkAndDownload(profileInfo.getVisumeFile(), url);
			insertVideo(visumeImageView, visumePath);
		}
	}
	
	private class downloadWebPage extends AsyncTask<String, Void, String> {

	    @Override
		protected void onPostExecute(String imagePath) {
	    	if(imagePath.length() > 0) {
	    		if(imagePath.contains(CommonUtilities.FILE_IMAGE_PROFILE)) {
	    			updateImageProfile(imagePath);
	    		} else if(imagePath.contains(CommonUtilities.FILE_IC_BACK)) {
	    			updateImageICBack(imagePath);
	    		} else if(imagePath.contains(CommonUtilities.FILE_IC_FRONT)) {
	    			updateImageICFront(imagePath);
	    		} else if(imagePath.contains(CommonUtilities.FILE_STUDENT_FRONT)) {
	    			updateImageStudentCardFrontPicture(imagePath);
	    		} else if(imagePath.contains(CommonUtilities.FILE_STUDENT_BACK)) {
	    			updateImageStudentCardBackPicture(imagePath);
	    		}
	    	}
		}

		@Override
	    protected String doInBackground(String... params) {
			String picPath = "";
			
	        try {
	            HttpClient httpClient = new DefaultHttpClient();
	            HttpGet httpGet = new HttpGet(params[0]);
				
	            HttpResponse response;
	            response = httpClient.execute(httpGet);
	            HttpEntity entity = response.getEntity();
	            InputStream is = entity.getContent();
	            
				picPath = CommonUtilities.IMAGE_ROOT + params[1];
				File f = new File(picPath);
				FileOutputStream output = new FileOutputStream(f);

	            byte data[] = new byte[1024];
	            int count;
	            while ((count = is.read(data)) > 0) {
	                output.write(data, 0, count);
	            }
	            
	            output.flush();
	            output.close();
	            is.close();
	            
	        } catch (Exception e) {
	            // TODO Auto-generated catch block
				Log.e(TAG, "Error download >>> " + picPath  + " " + e.getMessage());
	            e.printStackTrace();
	        }
	        
	        return picPath;
	    }
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
		
		// Load Album Storage Factory based on Android Version
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}

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
	
	
	private String checkAndDownload(String imageStoragePath,
			String apiURL) {
//		Log.d(TAG, "Downloading url " + apiURL + " with file path" + imageStoragePath);
		
		File f = new File(imageStoragePath);
		String filename = f.getName();
		
		File imageFile = new File(CommonUtilities.IMAGE_ROOT, filename);
		if (!imageFile.exists()) {
			String[] params = {apiURL, filename};
			new downloadWebPage().execute(params);
		} else {
			Log.d(TAG, "File " + f.getName() + " is exists!");
		}
		
		return CommonUtilities.IMAGE_ROOT + filename;
	}
	
	private void updateImageProfile(String imagePath) {
		insertImage(imageProfileView, imagePath);
	}
	
	private void updateImageICBack(String imagePath) {
		insertImage(imageICBackView, imagePath);
	}
	
	private void updateImageICFront(String imagePath) {
		insertImage(imageICFrontView, imagePath);
	}
	
	private void updateImageStudentCardFrontPicture(String imagePath) {
		insertImage(imageStudentCardFrontView, imagePath);
	}
	
	private void updateImageStudentCardBackPicture(String imagePath) {
		insertImage(imageStudentCardBackView, imagePath);
	}
	
	private void updateImageBankStatement(String imagePath) {
		insertImage(imageBankStatementView, imagePath);
	}	
	
	private void showStudentInfo(boolean show) {
		LinearLayout layStudent = (LinearLayout) findViewById(R.id.layStudent);
		if (show) {
			layStudent.setVisibility(View.VISIBLE);
			isStudentInfoRequired = true;
		} else {
			layStudent.setVisibility(View.GONE);
			isStudentInfoRequired = false;
		}
	}

	private void insertImage(ImageView button, String imageFilePath) {
		File f = new File(imageFilePath);
		
		if (f.exists()) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 3;
			options.inScaled = false;
			Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
			button.setImageBitmap(bitmap);
		}
	}
	
	//FIXME insert video still empty
	private void insertVideo(ImageView button, String videoFilePath) {
		File f = new File(videoFilePath);
		Log.d(CommonUtilities.TAG, "File video path" + f.getAbsolutePath() + " is exists " + f.exists());

		if(f.exists()) {
			Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(f.getAbsolutePath(),
					MediaStore.Video.Thumbnails.MICRO_KIND);
			button.setImageBitmap(bitmap);			
		}
	}

	protected void createGenderView(RadioGroup genderView) {
		if (listGender.size() > 0) {
			genderView.removeAllViews();
			for (int i = 0; i < listGender.size(); i++) {
				final RadioButton rb = new RadioButton(context);
				rb.setText(listGender.get(i));
				rb.setEnabled(false);
				rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						if (arg1) {
							profileInfo.setGender(arg0.getText().toString());
						}
					}
				});
				genderView.addView(rb);
				
				if (profileInfo.getGender().equalsIgnoreCase(listGender.get(i))) {
					rb.setChecked(true);
				}
			}
		}
	}
	
	protected void createStudentView(RadioGroup studentView) {
		if (listStudent.size() > 0) {
			studentView.removeAllViews();

			for (int i = 0; i < listStudent.size(); i++) {
				final RadioButton rb = new RadioButton(context);
				rb.setText(listStudent.get(i));
				rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						if (arg1) {
							Boolean studentStatus = false;
							
							if(arg0.getText().toString().equals("Yes")) {
								studentStatus = true;
							}
							
							showStudentInfo(studentStatus);
							profileInfo.setStudent(studentStatus.toString());
						}
					}
				});
				
				studentView.addView(rb);
				
				// Update checkbox if student "yes" or "no"
				if (profileInfo != null && profileInfo.getIsStudent()) {
					if(i == 0) {
						rb.setChecked(true);
						showStudentInfo(true);
					} 
				} else if (profileInfo != null && !profileInfo.getIsStudent()) {
					if(i > 0) {
						rb.setChecked(true);	
						showStudentInfo(false);
					}
				}

			}
		}
	}
	
	/**
	 * Saving user profile and submit to EDIT PROFILE API 
	 */
	protected void doCreateProfile() {
		final String url = SERVERURL + CommonUtilities.API_EDIT_AND_PART_TIMER_PROFILE;
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						SharedPreferences.Editor editor = settings.edit();
						
						// Update settings
						editor.putString(USER_FULLNAME, profileInfo.getFullName());
						editor.putString(USER_NRIC_NUMBER, profileInfo.getIc_no());
						editor.putString(USER_PROFILE_PICTURE, profileInfo.getProfile_pic());
						editor.putString(USER_NRIC_TYPE, profileInfo.getIc_type());
						editor.putString(USER_NRIC_TYPE_ID, profileInfo.getIc_type_id());
						editor.commit();

						// Updating user profile is complete or not
						checkUserComplete();
						
						Toast.makeText(context,
								getString(R.string.edit_profile_success),
								Toast.LENGTH_LONG).show();
						
						setResult(RESULT_OK);
						finish();
					} else if (jsonStr.trim().equalsIgnoreCase("1")) {
						Toast.makeText(context,
								getString(R.string.edit_profile_error),
								Toast.LENGTH_LONG).show();
					} else if (jsonStr.trim().equalsIgnoreCase("2")) {
						ValidationUtilities.resendLinkDialog(EditProfile.this, pt_id);
					} else {
						NetworkUtils.connectionHandler(EditProfile.this, jsonStr, "");
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
					
					childData.put(CommonUtilities.PARAM_PT_ID, pt_id);
					childData.put(CommonUtilities.PARAM_PROFILE_DATE_OF_BIRTH, profileInfo.getDob());
					childData.put(CommonUtilities.PARAM_PROFILE_FULL_NAME, profileInfo.getFullName());
					childData.put(CommonUtilities.PARAM_PROFILE_GENDER, profileInfo.getGenderID());
					
					childData.put(CommonUtilities.PARAM_PROFILE_PHONE_NUMBER, profileInfo.getPhone_num());
					childData.put(CommonUtilities.PARAM_PROFILE_POST_IC_FRONT_PICTURE,
							profileInfo.getIc_front_picture());
					childData.put(CommonUtilities.PARAM_PROFILE_POST_IC_BACK_PICTURE,
							profileInfo.getIc_back_picture());

					int tmp = -1;
						
					// Check if part-timer selecting isStudent equal to Yes
					if(isStudentInfoRequired) {
						JSONObject studentData = new JSONObject();
						
						Log.e("School", ">>> " + profileInfo.getSchool());
						if (profileInfo.getSchool() != null && profileInfo.getSchool().length() > 0) {
							tmp = listSchool.indexOf(profileInfo.getSchool());
							Log.e("School", ">>> " + tmp);
							studentData.put("school_id", listSchoolId.get(tmp));
						} else {
							studentData.put("school_id", "");
						}
						
						studentData.put("sid_front_pic",
								profileInfo.getStudentFrontCard_picture());
						studentData.put("sid_back_pic",
								profileInfo.getStudentBackCard_picture());					
						childData.put("student_details", studentData);
						childData.put("is_student", "1");
					} else {
						childData.put("is_student", "0");
					}
					
					// Construct Emergency contact data
					JSONObject ecData = new JSONObject();
					if (profileInfo.getEc_name() != null) {
						ecData.put("ec_full_name", profileInfo.getEc_name().trim());						
					} 
					
					ecData.put("ec_email", profileInfo.getEc_email());
					ecData.put("ec_phone_no", profileInfo.getEc_phone());
					ecData.put("ec_relationship",
							profileInfo.getEc_relationship());
					childData.put("emergency_contact", ecData);
					
					// Building Bank Data
					JSONObject bankData = new JSONObject();
					bankData.put("bank_name", profileInfo.getBank_name());
					bankData.put("bank_branch_name", profileInfo.getBank_branch_name());
					bankData.put("bank_account_name", profileInfo.getBank_account_name());
					bankData.put("bank_account_no", profileInfo.getBank_account_no());
					childData.put("bank_account", bankData);

//					childData.put("first_name", profileInfo.getFirst_name());
//					childData.put("last_name", profileInfo.getLast_name());
//					childData.put("gender", profileInfo.getGender());
//					childData.put("dob", profileInfo.getDob());
//					childData.put("address", profileInfo.getAddress());
//					childData.put("postal_code", profileInfo.getPost_code());
//					childData.put("ic_no", profileInfo.getIc_no());
//					childData.put("ic_type", profileInfo.getIc_type());
//					childData.put("ic_type_id", profileInfo.getIc_type_id());
//					childData.put("profile_picture",
//							profileInfo.getProfile_pic());

//					childData
//					.put("ic_expiry_date", profileInfo.getIc_expired());
//					childData.put("work_experience", profileInfo.getWork_exp());
//					childData.put("profile_source", "system");
//					childData.put("matric_card_no",
//					profileInfo.getCard_number());
//					childData.put("ec_address", profileInfo.getEc_address());
//					childData.put("ec_postal_code",
//							profileInfo.getEc_post_code());
//					childData.put("skills", profileInfo.getSkill().toString());
					parentData.put("part_timer", childData);

					String[] params = { "data" };
					String[] values = { parentData.toString() };
					jsonStr = jsonParser.getHttpResultUrlPut(url, params,
							values);

					Log.e(TAG, "Post data" + childData.toString());
					Log.e(TAG, "Create profile to " + url + "\nResult >>>\n "
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
	
	/**
	 * Updating user complete profile
	 */
	private void checkUserComplete() {
		SharedPreferences settings = getSharedPreferences(
				CommonUtilities.PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		
		if(isOpeningPage) {
			isOpeningPage = false;
			
			if(profileInfo.checkComplete()) {
				editor.putBoolean(USER_PROFILE_COMPLETE, true);
				Log.d(TAG, "USER PROFILE COMPLETED!");
			} else {
				editor.putBoolean(USER_PROFILE_COMPLETE, false);
				Log.d(TAG, "USER PROFILE INCOMPLETED! ProfileInfo : " + profileInfo.checkComplete());
			}
		} else {
			Log.d(TAG, "USER PROFILE " + profileInfo.checkComplete() + " " + isUserCompleteInput);

			if(profileInfo.checkComplete() == true && isUserCompleteInput == true) {
				editor.putBoolean(USER_PROFILE_COMPLETE, true);
				Log.d(TAG, "USER PROFILE COMPLETED!");
			} else {
				editor.putBoolean(USER_PROFILE_COMPLETE, false);
				Log.d(TAG, "USER PROFILE INCOMPLETED! ProfileInfo : " + profileInfo.checkComplete() +
						"; Form Input "+ isUserCompleteInput);
			}
		}
		
		editor.commit();
		
	}
	
	/**
	 * Listener for IC Front, IC Back, Profile and Matric card listener
	 * @param idx
	 */
	protected void askImageFrom(final int idx) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		CharSequence[] choice = { "Gallery", "Camera" };
		builder.setTitle(R.string.select_image_from);
		builder.setItems(choice, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				if (arg1 == 0) {
					takeImageFromGallery(idx);
				} else {
					Log.d(TAG, "Take from camera with code: " + idx);
					takeImageFromCamera(idx);
				}
			}
		});

		Dialog dialog = builder.create();
		dialog.show();
	}

	protected void takeImageFromCamera(int idx) {
		uploadImage = "";
		selectedPhotoId = idx;
		uploadURL = CommonUtilities.SERVERURL;
		String selectedFileName = "";
		
		switch (idx) {
		
		case TAKE_IMG_PROFILE:
			selectedFileName = CommonUtilities.FILE_IMAGE_PROFILE + ".jpg";
			uploadImage = CommonUtilities.IMAGE_ROOT + selectedFileName;
			uploadURL += CommonUtilities.API_UPLOAD_PROFILE_PICTURE_BY_PT_ID;
			break;
			
		case TAKE_IMG_STUDENT_FRONT:
			selectedFileName = CommonUtilities.FILE_STUDENT_FRONT + ".jpg";
			uploadImage = CommonUtilities.IMAGE_ROOT + selectedFileName;
			uploadURL += CommonUtilities.API_UPLOAD_STUDENT_FRONT_PHOTOS;
			break;
			
		case TAKE_IMG_STUDENT_BACK:
			selectedFileName = CommonUtilities.FILE_STUDENT_BACK + ".jpg";
			uploadImage = CommonUtilities.IMAGE_ROOT + selectedFileName;
			uploadURL += CommonUtilities.API_UPLOAD_STUDENT_BACK_PHOTOS;
			break;
			
		case TAKE_IMG_IC_BACK:
			selectedFileName =  CommonUtilities.FILE_IC_BACK + ".jpg";
			uploadImage = CommonUtilities.IMAGE_ROOT + selectedFileName;
			uploadURL += CommonUtilities.API_UPLOAD_BACK_NRIC_PHOTOS;
			break;
			
		case TAKE_IMG_IC_FRONT:
			selectedFileName =  CommonUtilities.FILE_IC_FRONT + ".jpg";
			uploadImage = CommonUtilities.IMAGE_ROOT + selectedFileName;
			uploadURL += CommonUtilities.API_UPLOAD_FRONT_NRIC_PHOTOS;
			break;
			
		case TAKE_IMG_BANK_STATEMENT:
			selectedFileName =  Api.FILE_BANK_STATEMENT + ".jpg";
			uploadImage = Api.IMAGE_ROOT + selectedFileName;
			uploadURL += CommonUtilities.API_UPLOAD_BANK_STATEMENT_BY_PT_ID;
			break;
		}
		
        File newfile = new File(uploadImage);
        try {
            newfile.createNewFile();
        } catch (Exception e) {
        	Log.e("takeImageFromCamera", ">>> " + e.getMessage());
        }
        
        Uri outputFileUri = Uri.fromFile(newfile);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
	}

	protected void takeImageFromGallery(int idx) {
		// TODO Auto-generated method stub
		selectedPhotoId = idx;
		uploadImage = "";
		selectedPhotoId = idx;
		uploadURL = CommonUtilities.SERVERURL;
		String selectedFileName = "";
		
		switch (selectedPhotoId) {
		
		case TAKE_IMG_PROFILE:
			selectedFileName = CommonUtilities.FILE_IMAGE_PROFILE + ".jpg";
			uploadImage = CommonUtilities.IMAGE_ROOT + selectedFileName;
			uploadURL += CommonUtilities.API_UPLOAD_PROFILE_PICTURE_BY_PT_ID;
			break;
		
		case TAKE_IMG_STUDENT_FRONT:
			selectedFileName = CommonUtilities.FILE_STUDENT_FRONT + ".jpg";
			uploadImage = CommonUtilities.IMAGE_ROOT + selectedFileName;
			uploadURL += CommonUtilities.API_UPLOAD_STUDENT_FRONT_PHOTOS;
			break;
			
		case TAKE_IMG_STUDENT_BACK:
			selectedFileName = CommonUtilities.FILE_STUDENT_BACK + ".jpg";
			uploadImage = CommonUtilities.IMAGE_ROOT + selectedFileName;
			uploadURL += CommonUtilities.API_UPLOAD_STUDENT_BACK_PHOTOS;
			break;
			
		case TAKE_IMG_IC_BACK:
			selectedFileName =  CommonUtilities.FILE_IC_BACK + ".jpg";
			uploadImage = CommonUtilities.IMAGE_ROOT + selectedFileName;
			uploadURL += CommonUtilities.API_UPLOAD_BACK_NRIC_PHOTOS;
			break;
			
		case TAKE_IMG_IC_FRONT:
			selectedFileName =  CommonUtilities.FILE_IC_FRONT + ".jpg";
			uploadImage = CommonUtilities.IMAGE_ROOT + selectedFileName;
			uploadURL += CommonUtilities.API_UPLOAD_FRONT_NRIC_PHOTOS;
			break;

		case TAKE_IMG_BANK_STATEMENT:
			selectedFileName =  Api.FILE_BANK_STATEMENT + ".jpg";
			uploadImage = Api.IMAGE_ROOT + selectedFileName;
			uploadURL += CommonUtilities.API_UPLOAD_BANK_STATEMENT_BY_PT_ID;
			break;
		}
		
		Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), SELECT_PHOTO);
	}
	
	
	/**
	 * Listener for visume
	 * @param idx
	 */
	protected void askVideoFrom(final int idx) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.select_image_from);
		builder.setItems(R.array.mediaupload_array, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				uploadImage = "";		
				uploadURL = CommonUtilities.SERVERURL + Api.PARAM_VISUME_UPLOAD;
				
				String selectedFileName = "";	
				selectedFileName =  Api.FILE_VISUME + ".mp4";
				uploadImage = Api.IMAGE_ROOT + selectedFileName;

				if (arg1 == 0) {
					takeVideoFromGallery(idx);
				} else {
					dispatchVideoIntent();
				}
			}
		});

		Dialog dialog = builder.create();
		dialog.show();
	}

	protected void takeVideoFromGallery(int idx) {
		// TODO Auto-generated method stub
		selectedPhotoId = idx;
		
		Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Video"), SELECT_VIDEO);
	}
	
	
	private void dispatchVideoIntent() {		
		Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		takeVideoIntent.putExtra("android.intent.extra.durationLimit", Api.VIDEO_LIMIT);
		startActivityForResult(takeVideoIntent, TAKE_VIDEO);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);		
		switch (requestCode) {
			case TAKE_PHOTO_CODE: {
				if (resultCode == RESULT_OK) {
					uploadAction(uploadImage, uploadURL, selectedPhotoId);
				}
				break;
			} // ACTION_TAKE_PHOTO_B
	
			case SELECT_PHOTO: {
				if (resultCode == RESULT_OK) {
					Uri selectedImageUri = data.getData();
		            String selectedImagePath = getPath(selectedImageUri);
		            ApplicationUtils.copyFile(selectedImagePath, uploadImage);	            
	
					uploadAction(uploadImage, uploadURL, selectedPhotoId);
				}
				break;
			} // ACTION_TAKE_PHOTO_S
			
			case TAKE_VIDEO: {
				if(resultCode == RESULT_OK) {
					Uri selectedImageUri = data.getData();
		            String selectedImagePath = getPath(selectedImageUri);
		            ApplicationUtils.copyFile(selectedImagePath, uploadImage);	 
					
		            uploadVideoToServer(uploadImage, uploadURL, selectedPhotoId);
				}
				break;
			} // ACTION_TAKE_VIDEO
			
			case SELECT_VIDEO: {
				if (resultCode == RESULT_OK) {
					Uri selectedImageUri = data.getData();
		            String selectedImagePath = getPath(selectedImageUri);
		            
		            File videoUpload = new File(selectedImagePath);
		            
		            // Get length of file in bytes
		            long fileSizeInBytes = videoUpload.length();
		            // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
		            long fileSizeInKB = fileSizeInBytes / 1024;
		            // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
		            long fileSizeInMB = fileSizeInKB / 1024;
		            
		            if(fileSizeInMB > 4) {
		            	int duration = Toast.LENGTH_LONG;

		            	Toast toast = Toast.makeText(context, getResources().getString(R.string.video_upload_oversize), duration);
		            	toast.show();
		            	
		            } else {
			            ApplicationUtils.copyFile(selectedImagePath, uploadImage);	            
			            uploadVideoToServer(uploadImage, uploadURL, selectedPhotoId);		            	
		            }
				}
				break;
			} // ACTION_SELECT_VIDEO
		
		}
		// switch		
	}
	

	/**
	 * Execute upload to Server
	 */
	private void uploadAction(String uploadImage, String uploadURL, 
			int selectedPhotoId) {
		if(uploadImage != null && uploadURL != null) {
			uploadImageToServer(uploadImage, uploadURL, selectedPhotoId);
			uploadImage = null;
			uploadURL = null;
			selectedPhotoId = -1;					
		}
	}
	
	public String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
	
	public static Bitmap imageRotate(Bitmap bitmap, int degree) {
	    int w = bitmap.getWidth();
	    int h = bitmap.getHeight();

	    Matrix mtx = new Matrix();
	    mtx.postRotate(degree);

	    return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}
	
	private void renameFile(String oldPath, String imagePath) {
		String newFilename = new File(imagePath).getName();
		String newPath = CommonUtilities.IMAGE_ROOT + newFilename;
		boolean isRenamed = false;
		
		File f = null;
	    File f2 = null;
		
		try {
			f = new File(oldPath);						
			f2 = new File(newPath);					
			isRenamed = f.renameTo(f2);
		} catch(Exception e){
	         // if any error occurs
	         e.printStackTrace();
	    }		
	}
	
	private void uploadVideoToServer(final String filePath, final String url, final int outputPhotoId) {
		final int getSelectedPhoto;
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				uploadImage = null;
				uploadURL = null;
				selectedPhotoId = -1;	
				
				if(responseString != null) {
					Toast.makeText(context, responseString,
							Toast.LENGTH_SHORT).show();
				}
				
				// Update profile
				Intent iBroadcast = new Intent("profile.receiver");
				sendBroadcast(iBroadcast);
				
				updateImageLayout();
			}
		};

		progress = ProgressDialog.show(context, getString(R.string.profile_header),
				getString(R.string.uploading),
				true, false);
		new Thread() {
			public void run() {
				responseString = null;
				
				File file = new File(filePath);
				String filename = file.getName();
				
				Log.d(TAG, "Upload file " + filePath);			
				Log.d(TAG, "Upload video to " + url);			
				
				try {
					InputStream is = new FileInputStream(filePath);
					byte[] data = readBytes(is);
					
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
					reqEntity.addPart("pt_id", new StringBody(pt_id));
					
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
					
					Log.d(TAG, "Upload result: " + s.toString());
					renameFile(filePath, s.toString());
					
					profileInfo.setVisumeFile(s.toString());
					Log.d(TAG, "Set visume file path " + profileInfo.getVisumeFile());
					
					
				} catch (IOException e) {
					JSONObject json = new JSONObject(); 
					try {
						json.put("status", CommonUtilities.NOINTERNET);
						responseString = NetworkUtils.connectionHandlerString(context, jsonStr.toString(), e.getMessage());						
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
					Log.e(CommonUtilities.TAG, "Error uploading image " + " >> " + e.getMessage());
				
				} catch (Exception e) {
					Log.e(CommonUtilities.TAG, "Error uploading image" + e.getMessage());
				}

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}
	
	
	public byte[] readBytes(InputStream inputStream) throws IOException {
	    // This dynamically extends to take the bytes you read.
	    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

	    // This is storage overwritten on each iteration with bytes.
	    int bufferSize = 1024;
	    byte[] buffer = new byte[bufferSize];

	    // We need to know how may bytes were read to write them to the byteBuffer.
	    int len = 0;
	    while ((len = inputStream.read(buffer)) != -1) {
	        byteBuffer.write(buffer, 0, len);
	    }

	    // And then we can return your byte array.
	    return byteBuffer.toByteArray();
	}
	
	private void uploadImageToServer(final String filePath, final String url, final int outputPhotoId) {
		final int getSelectedPhoto;
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if(responseString != null) {
					Toast.makeText(context, responseString,
							Toast.LENGTH_SHORT).show();
				}
				
				// Update profile
				Intent iBroadcast = new Intent("profile.receiver");
				sendBroadcast(iBroadcast);
				
				updateImageLayout();
			}
		};

		progress = ProgressDialog.show(context, getString(R.string.profile_header),
				getString(R.string.uploading),
				true, false);
		
		new Thread() {
			public void run() {
				responseString = null;
				
				File file = new File(filePath);
				String filename = file.getName();
				
				Log.d(TAG, "Upload file " + filePath);			
				Log.d(TAG, "Upload photo to " + url);			
				
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;
				options.inScaled = false;
				Bitmap bm = BitmapFactory.decodeFile(filePath, options);
				
				// Fix S3 bug image orientation
				ExifInterface exif;
				try {
					exif = new ExifInterface(filePath);
					
					Log.d(TAG, "Exif value " + exif.getAttribute(ExifInterface.TAG_ORIENTATION));
					if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")){
						bm=imageRotate(bm, 90);
					} else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
						bm=imageRotate(bm, 270);
					} else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
						bm=imageRotate(bm, 180);
					}

				} catch (IOException e) {
					JSONObject json = new JSONObject(); 
					// TODO Auto-generated catch block
					try {
						json.put("status", CommonUtilities.FILECORRUPT);
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					responseString = NetworkUtils.connectionHandlerString(context, jsonStr.toString(), e.getMessage());						
					Log.e(CommonUtilities.TAG, "Error uploading image " + " >> " + e.getMessage());
				}
				
				Log.d(CommonUtilities.TAG, "Fine " + pt_id);
				
				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bm.compress(CompressFormat.JPEG, 75, bos);
					
					byte[] data = bos.toByteArray();
					
					Log.d(CommonUtilities.TAG, "Blum " + pt_id);
					
					// Replacing image in local storage
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(data);
					
					Log.d(CommonUtilities.TAG, "OK " + pt_id);

					HttpClient httpClient = new DefaultHttpClient();
					HttpPost postRequest = new HttpPost(url);

					ByteArrayBody bab = new ByteArrayBody(data, filename);
					MultipartEntity reqEntity = new MultipartEntity(
							HttpMultipartMode.BROWSER_COMPATIBLE);
					reqEntity.addPart("file", bab);
					reqEntity.addPart("filename", new StringBody(filename));
					reqEntity.addPart("pt_id", new StringBody(pt_id));
					
					Log.d(CommonUtilities.TAG, "Upload " + filename + " | " + pt_id);
					
					postRequest.setEntity(reqEntity);
					HttpResponse response = httpClient.execute(postRequest);
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(
									response.getEntity().getContent(), "UTF-8"));
					String sResponse;
					StringBuilder s = new StringBuilder();
					
					while ((sResponse = reader.readLine()) != null) {
						s = s.append(sResponse);
						Log.d(CommonUtilities.TAG, "Upload " + sResponse);
					}
					
					Log.d(TAG, "Upload result: " + s.toString());
					renameFile(filePath, s.toString());
					
					switch (outputPhotoId) {
		    		case TAKE_IMG_PROFILE:
		    			profileInfo.setProfile_pic(s.toString());
		    			
		    			// Update user profile picture
		    			SharedPreferences.Editor editor = settings.edit();
						editor.putString(CommonUtilities.USER_PROFILE_PICTURE, s.toString());
						editor.commit();
						
		    			break;
		    		case TAKE_IMG_STUDENT_FRONT:
		    			profileInfo.setStudentFrontCard_picture(s.toString());
		    			break;
		    		case TAKE_IMG_STUDENT_BACK:
		    			profileInfo.setStudentBackCard_picture(s.toString());
		    			break;
		    		case TAKE_IMG_IC_BACK:
		    			profileInfo.setIc_back_picture(s.toString());
		    			break;
		    		case TAKE_IMG_IC_FRONT:
		    			profileInfo.setIc_front_picture(s.toString());
		    			break;
		    		case TAKE_IMG_BANK_STATEMENT:
		    			profileInfo.setBank_statement(s.toString());
		    			break;
		    		}
					
				} catch (IOException e) {
					JSONObject json = new JSONObject(); 
					try {
						json.put("status", CommonUtilities.NOINTERNET);
						responseString = NetworkUtils.connectionHandlerString(context, jsonStr.toString(), e.getMessage());						
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
					Log.e(CommonUtilities.TAG, "Error uploading image " + " >> " + e.getMessage());
				
				} catch (Exception e) {
					Log.d(CommonUtilities.TAG, "Get profile " + profileInfo.getProfile_pic());
					
					Log.e(CommonUtilities.TAG, "Error uploading image : " + e.getMessage());
				}

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent result = new Intent();
			setResult(RESULT_CANCELED, result);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
    protected void onStart() {
       super.onStart();
		BugSenseHandler.startSession(EditProfile.this);
    }
	
	@Override
    protected void onStop() {
       super.onStart();
       BugSenseHandler.closeSession(EditProfile.this);
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
        outState.putString("uploadImage", uploadImage);
        outState.putString("uploadURL", uploadURL);
        outState.putInt("selectedPhotoId", selectedPhotoId);
        
		Log.d(TAG, "On Save Instance : " + uploadImage + " " + uploadURL + " " + selectedPhotoId);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
//		uploadImage = savedInstanceState.getString("uploadImage");
//		uploadURL = savedInstanceState.getString("uploadURL");
//		selectedPhotoId = savedInstanceState.getInt("selectedPhotoId");
		
		Log.d(TAG, "On Restore Instance : " + uploadImage + " " + uploadURL + " " + selectedPhotoId);
	}

	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
	
	/**
	 * Loading profile data
	 * @param isUpload
	 */
	private void loadProfileWithDefaultData(final boolean isUpload) {
//		final String url = SERVERURL_SECURE + CommonUtilities.API_GET_PART_TIMER_PROFILE_WITH_DEFAULT_DATA_BY_PT_ID +
//				"?" + CommonUtilities.PARAM_PT_ID + "=" + pt_id;		
		final String url = SERVERURL + CommonUtilities.API_GET_PART_TIMER_PROFILE_WITH_DEFAULT_DATA_BY_PT_ID +
				"?" + CommonUtilities.PARAM_PT_ID + "=" + pt_id;
		Log.d(TAG, "Load profile with default data " + url);
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null || jsonStr.length() > 0) {
//					Log.d(TAG, "Result " + jsonStr);
					loadGenderJson(jsonStr);
					
					if(isUpload) {
						uploadImageToServer(uploadImage, uploadURL, selectedPhotoId);
					}
					
				} else {
					Toast.makeText(context,
							getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context, "Profile",
				getString(R.string.loading_profile), true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);				
//				jsonStr = jsonParser.getHttpsResultUrlGet(context, url);
				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

	/**
	 * Emergency Contact Relationship listener
	 */
	private OnClickListener ECRelationListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			// Set the dialog title
			selectedECRelationIdx = -1;
			builder.setTitle("Select "
					+ getString(R.string.ec_relation));
			builder.setItems(R.array.ecrelationship_array,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							selectedECRelationIdx = which;
							if (selectedECRelationIdx != -1) {
								EcRelationView.setText(listECRelation
										.get(selectedECRelationIdx));
							}
						}
					});

			Dialog dialog = builder.create();
			dialog.show();
		}
	};
	
	
	/**
	 * Date of birth listener
	 */
	private OnClickListener dobListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String birthValue = dobView.getText().toString().trim();
			if(birthValue.length() == 10) {
				try {
					Date dobDate = dobDateFormat.parse(birthValue);
					defaultBirthday.setTime(dobDate);					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			
			DatePickerDialog dateDlg = new DatePickerDialog(context,
					new DatePickerDialog.OnDateSetListener() {
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							Time chosenDate = new Time();
							chosenDate.set(dayOfMonth, monthOfYear, year);
							long dtDob = chosenDate.toMillis(true);

							Date date = new Date();
							Calendar cal = Calendar.getInstance();
							cal.setTime(date);
							cal.add(Calendar.YEAR, -CommonUtilities.AGE_LIMITATION);
							long limitation = cal.getTimeInMillis();

							if (dtDob > limitation) {
								dobView.setText("");
								AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
										context);

								// set title
								dialogBuilder
										.setTitle(getString(R.string.registration_birthday_restriction_title));

								// set dialog message
								dialogBuilder
										.setMessage(
												getString(R.string.registration_birthday_restriction))
										.setPositiveButton(
												getString(R.string.ok),
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
														// Nothing to do
													}
												});

								// create alert dialog
								Dialog dialog = dialogBuilder
										.create();

								// show it
								dialog.show();

							} else {
								CharSequence strDate = DateFormat.format(
										dobFormat, dtDob);
								dobView.setText(strDate);
							}
						}
					}, defaultBirthday.get(Calendar.YEAR),
					defaultBirthday.get(Calendar.MONTH),
					defaultBirthday.get(Calendar.DAY_OF_MONTH));
			dateDlg.setMessage(getString(R.string.registration_profile_date_of_birth));
			dateDlg.show();
			
		}
	};
	
	/**
	 * School list listener for Student IC Type
	 */
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
		            		   schoolView.setText(listSchool.get(selectedIdx));		
		            		   profileInfo.setSchool(listSchool
										.get(selectedIdx));
		            	   }
		           }
		    });
	           
			Dialog dialog = builder.create();
			dialog.show();
		}
	};
	
	
	
	private boolean[] toPrimitiveArray(final List<Boolean> booleanList) {
	    final boolean[] primitives = new boolean[booleanList.size()];
	    int index = 0;
	    for (Boolean object : booleanList) {
	        primitives[index++] = object;
	    }
	    return primitives;
	}
	
	/**
	 * NRIC Type listener
	 */
	private OnClickListener statusListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			// Set the dialog title
			if (profileInfo.getIc_type_id() != null) {
				selectedIdx = listNRICTypeId.indexOf(profileInfo.getIc_type_id());
			} else {
				selectedIdx = -1;
			}
			
			// Set the dialog title
			builder.setTitle("Select " + getString(R.string.registration_profile_nrictype));
			builder.setItems(listNRICType.toArray(new CharSequence[listNRICType.size()]), new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	               // The 'which' argument contains the index position
	               // of the selected item
	            	   selectedIdx = which;
	            	   
	            	   if (selectedIdx != -1) {
							statusView.setText(listNRICType
									.get(selectedIdx));
							profileInfo.setIc_type_id(listNRICTypeId.get(selectedIdx));
							if (listNRICType.get(selectedIdx)
									.contains("Student")) {
								showStudentInfo(true);
							} else {
								showStudentInfo(false);
							}
						}	            	   
	               }
	        });
			
			Dialog dialog = builder.create();
			dialog.show();			
		}
	};
	
	/**
	 * Working experience listener
	 * @deprecated
	 */
	private OnClickListener workExperienceListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			// Set the dialog title
			selectedIdx = -1;
			builder.setTitle("Select "
					+ getString(R.string.registration_profile_work_experience));
			builder.setItems(R.array.workexperience_array,
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
	

	@Deprecated
	protected void loadBankInfo() {
		final String url = SERVERURL + CommonUtilities.API_GET_BANK_INFO + "?"
				+ PARAM_PT_ID + "=" + pt_id;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null || jsonStr.length() > 0) {
					Log.e("Bank", ">>> " + jsonStr);
					try {
						JSONObject obj = new JSONObject(jsonStr);
						obj = obj.getJSONObject("bank_accounts");

//						profileInfo.setBank_name(obj.getString("bank_name"));
//						profileInfo.setBank_account_no(obj
//								.getString("bank_account_no"));
//						profileInfo.setBank_acc_type(obj
//								.getString("bank_account_type"));
//						profileInfo.setBank_acc_branch(obj
//								.getString("bank_branch_name"));

						loadEcInfo();
					} catch (Exception e) {
						Log.e("Bank", ">>> " + e.getMessage());
						Toast.makeText(context,
								getString(R.string.failed_load_data),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(context,
							getString(R.string.something_wrong),
							Toast.LENGTH_SHORT).show();
				}
				
			}
		};

		progress = ProgressDialog.show(context, "Profile",
				"Getting bank info...", true, false);
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
	protected void loadEcInfo() {
		final String url = SERVERURL + CommonUtilities.API_GET_EC_INFO + "?"
				+ PARAM_PT_ID + "=" + pt_id;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null || jsonStr.length() > 0) {
					try {
						JSONObject obj = new JSONObject(jsonStr);
						obj = obj.getJSONObject("emergency_contacts");

						profileInfo.setEc_address(obj.getString("address"));
						profileInfo.setEc_email(obj.getString("email"));
						profileInfo.setEc_name(obj.getString("first_name")
								+ " " + obj.getString("last_name"));
						profileInfo.setEc_phone(obj.getString("phone_no"));
						profileInfo.setEc_post_code(obj
								.getString("postal_code"));
						profileInfo.setEc_relationship(obj
								.getString("relationship"));
						
						Log.e("Profile", profileInfo.toString());
						updateLayout();
						
					} catch (Exception e) {
						Log.e("EC Info", ">>> " + e.getMessage());
						Toast.makeText(
								context,
								"Getting emergency contact FAILED, Please try again !",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(
							context,
							getString(R.string.something_wrong),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		progress = ProgressDialog.show(context, "Profile",
				"Getting emergency contact info...", true, false);
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
	 * Skills list listener
	 * @deprecated
	 */
	private OnClickListener skillsListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			final List<Integer> listSelectedItems = new ArrayList<Integer>();
			final ArrayList<CharSequence> selectedSkills = new ArrayList<CharSequence>();
			final List<Integer> selectedIdx = new ArrayList<Integer>();
			
			for(int i = 0; i < listSkillsSelected.size(); i++) {
				if(listSkillsSelected.get(i) == true) {
					selectedIdx.add(i);
					listSelectedItems.add(Integer.parseInt(listSkillId.get(i)));
				}
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			
			// Set the dialog title
			builder.setTitle("Select " + getString(R.string.registration_profile_skills))
					.setMultiChoiceItems(
							listSkill.toArray(new CharSequence[listSkill
									.size()]), toPrimitiveArray(listSkillsSelected),
							new DialogInterface.OnMultiChoiceClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									
									if (isChecked) {										
										if(!selectedIdx.contains(which)) {
											listSelectedItems.add(Integer.parseInt(listSkillId
													.get(which)));											
									
											selectedIdx.add(which);								
										}
										
									} else {
										listSelectedItems.remove(Integer.valueOf(listSkillId.get(which)));
										
										// Avoid list integer bug
										selectedIdx.remove(new Integer(which));
									}
									
									listSkillsSelected = new ArrayList<Boolean>();
									
									for(String skillID : listSkillId) {
										Boolean isMatched = false;
										for(int selectItem : listSelectedItems) {
											if(selectItem == Integer.parseInt(skillID)) {
												isMatched = true;
											}
										}
										
										listSkillsSelected.add(isMatched);
									}
									
									Log.d(TAG, " " + listSkillsSelected.toString());
									
								}
							})
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									selectedSkills.clear();
									for (int i = 0; i < selectedIdx.size(); i++) {
										selectedSkills.add(listSkill
												.get(selectedIdx.get(i)));
									}
//									profileInfo.setSkill(listSelectedItems);

									// Convert ArrayList into String
									// comma-separated
									String selectedSkillsList = selectedSkills
											.toString();
									String selectedSkillsSet = selectedSkillsList
											.substring(
													1,
													selectedSkillsList
															.length() - 1)
											.replace(", ", ", ");
									skillView.setText(selectedSkillsSet);
								}
							});

			Dialog dialog = builder.create();
			dialog.show();
		}
	};

	/**
	 * Card expired for student listener
	 * @deprecated
	 */
	private OnClickListener expiredListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			// Set default value expire 
			String expireValue = expiryDateView.getText().toString().trim();
			if(expireValue.length() == 10) {
				try {
					Date expireDate = dobDateFormat.parse(expireValue);
					defaultExpire.setTime(expireDate);					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			
			DatePickerDialog dateDlg = new DatePickerDialog(context,
					new DatePickerDialog.OnDateSetListener() {
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							Time chosenDate = new Time();
							chosenDate.set(dayOfMonth, monthOfYear, year);
							long dtDob = chosenDate.toMillis(true);

							Calendar cal = Calendar.getInstance();
							long limitation = cal.getTimeInMillis();

							if (dtDob < limitation) {
								expiryDateView.setText("");
								AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
										context);

								// set title
								dialogBuilder
										.setTitle(getString(R.string.expired_date));

								// set dialog message
								dialogBuilder
										.setMessage(
												getString(R.string.expired_date_err))
										.setPositiveButton(
												getString(R.string.ok),
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
														// Nothing to do
													}
												});

								// create alert dialog
								Dialog dialog = dialogBuilder
										.create();

								// show it
								dialog.show();

							} else {
								CharSequence strDate = DateFormat.format(
										dobFormat, dtDob);
								expiryDateView.setText(strDate);
							}
						}
					}, defaultExpire.get(Calendar.YEAR),defaultExpire.get(Calendar.MONTH),
					defaultExpire.get(Calendar.DAY_OF_MONTH));
			dateDlg.setMessage("IC Expire Date");
			dateDlg.show();
			
		}
	};
	

//	private void loadSkillJson(String jsonStr) {
//		try {
//			JSONObject dataObj = new JSONObject(jsonStr);
//			JSONArray jsonArray = dataObj.getJSONArray(CommonUtilities.PARAM_PROFILE_DEFAUT_PART_TIMER_SKILL);
//			
//			listSkill = new ArrayList<String>();
//			listSkillId = new ArrayList<String>();
//			listSkillDesc = new ArrayList<String>();
//
//			for (int i = 0; i < jsonArray.length(); i++) {
//				JSONObject obj = jsonArray.getJSONObject(i);
//				obj = obj.getJSONObject("skills");
//				listSkillDesc.add(obj.getString("skill_desc"));
//				listSkillId.add(obj.getString("skill_id"));
//				listSkill.add(obj.getString("skill_name"));
//			}
//
//			loadSchoolJson(jsonStr);
//			
//		} catch (JSONException e) {
//			NetworkUtils.connectionHandler(EditProfile.this, jsonStr, e.getMessage());
//			Log.e(TAG, "Error skills >>> " + e.getMessage());
//		}
//	}
	
//	/**
//	 * Loading gender
//	 * @param isUpload
//	 * @deprecated
//	 */
//	private void loadGender(final boolean isUpload) {
//		final String url = SERVERURL + API_GET_GENDERS;
//		final Handler mHandlerFeed = new Handler();
//		final Runnable mUpdateResultsFeed = new Runnable() {
//			public void run() {
//				if (jsonStr != null) {
//					try {
//						listGender = new ArrayList<String>();
//						listGenderId = new ArrayList<String>();
//
//						JSONArray jsonArray = new JSONArray(jsonStr);
//						for (int i = 0; i < jsonArray.length(); i++) {
//							JSONObject obj = jsonArray.getJSONObject(i);
//							obj = obj.getJSONObject("genders");
//							listGender.add(obj.getString("gender"));
//							listGenderId.add(obj.getString("gender_id"));
//						}
//
//						loadNRICType(isUpload);
//						
//					} catch (JSONException e) {
//						NetworkUtils.connectionHandler(EditProfile.this, jsonStr, e.getMessage());
//						Log.e(TAG, "Error get genders >>> " + e.getMessage());
//					}
//				} else {
//					Toast.makeText(context,
//							getString(R.string.server_error),
//							Toast.LENGTH_SHORT).show();
//				}
//			}
//		};
//
//		progress = ProgressDialog.show(context, "Profile", "Loading gender...",
//				true, false);
//		new Thread() {
//			public void run() {
//				jsonParser = new JSONParser();
//				jsonStr = jsonParser.getHttpResultUrlGet(url);
//
//				if (progress != null && progress.isShowing()) {
//					progress.dismiss();
//					mHandlerFeed.post(mUpdateResultsFeed);
//				}
//			}
//		}.start();
//	}
//
//	@Deprecated
//	protected void loadNRICType(final boolean isUpload) {
//		final String url = SERVERURL + API_GET_IC_TYPES;
//		final Handler mHandlerFeed = new Handler();
//		final Runnable mUpdateResultsFeed = new Runnable() {
//			public void run() {
//				if (jsonStr != null) {
//					try {
//						listNRICType = new ArrayList<String>();
//						listNRICTypeId = new ArrayList<String>();
//
//						JSONArray jsonArray = new JSONArray(jsonStr);
//						for (int i = 0; i < jsonArray.length(); i++) {
//							JSONObject obj = jsonArray.getJSONObject(i);
//							obj = obj.getJSONObject("nric_types");
//							listNRICType.add(obj.getString("nric_type"));
//							listNRICTypeId.add(obj.getString("nric_type_id"));
//						}
//
//						loadSkill(isUpload);
//					} catch (JSONException e) {
//						NetworkUtils.connectionHandler(EditProfile.this, jsonStr, e.getMessage());
//						Log.e(TAG, "Error get ic types >>> " + e.getMessage());
//					}
//				} else {
//					Toast.makeText(context,
//							getString(R.string.server_error),
//							Toast.LENGTH_SHORT).show();
//				}
//			}
//		};
//
//		progress = ProgressDialog.show(context, "Profile",
//				"Loading NRIC type...", true, false);
//		new Thread() {
//			public void run() {
//				jsonParser = new JSONParser();
//				jsonStr = jsonParser.getHttpResultUrlGet(url);
//
//				if (progress != null && progress.isShowing()) {
//					progress.dismiss();
//					mHandlerFeed.post(mUpdateResultsFeed);
//				}
//			}
//		}.start();
//	}
//
//	@Deprecated
//	protected void loadSkill(final boolean isUpload) {
//		final String url = SERVERURL + API_GET_SKILLS;
//		final Handler mHandlerFeed = new Handler();
//		final Runnable mUpdateResultsFeed = new Runnable() {
//			public void run() {
//				if (jsonStr != null) {
//					try {
//						listSkill = new ArrayList<String>();
//						listSkillId = new ArrayList<String>();
//						listSkillDesc = new ArrayList<String>();
//
//						JSONArray jsonArray = new JSONArray(jsonStr);
//						for (int i = 0; i < jsonArray.length(); i++) {
//							JSONObject obj = jsonArray.getJSONObject(i);
//							obj = obj.getJSONObject("skills");
//							listSkillDesc.add(obj.getString("skill_desc"));
//							listSkillId.add(obj.getString("skill_id"));
//							listSkill.add(obj.getString("skill_name"));
//						}
//
//						loadSchool(isUpload);
//						
//					} catch (JSONException e) {
//						NetworkUtils.connectionHandler(EditProfile.this, jsonStr, e.getMessage());
//						Log.e(TAG, "Error skills >>> " + e.getMessage());
//					}
//				} else {
//					Toast.makeText(context,
//							getString(R.string.server_error),
//							Toast.LENGTH_SHORT).show();
//				}
//			}
//		};
//
//		progress = ProgressDialog.show(context, "Profile", "Loading skill...",
//				true, false);
//		new Thread() {
//			public void run() {
//				jsonParser = new JSONParser();
//				jsonStr = jsonParser.getHttpResultUrlGet(url);
//
//				if (progress != null && progress.isShowing()) {
//					progress.dismiss();
//					mHandlerFeed.post(mUpdateResultsFeed);
//				}
//			}
//		}.start();
//	}
//
//	@Deprecated
//	protected void loadSchool(final boolean isUpload) {
//		final String url = SERVERURL + CommonUtilities.API_GET_SCHOOLS;
//		final Handler mHandlerFeed = new Handler();
//		final Runnable mUpdateResultsFeed = new Runnable() {
//			public void run() {
//				if (jsonStr != null) {
//					try {
//						listSchool = new ArrayList<String>();
//						listSchoolId = new ArrayList<String>();
//
//						JSONArray jsonArray = new JSONArray(jsonStr);
//						for (int i = 0; i < jsonArray.length(); i++) {
//							JSONObject obj = jsonArray.getJSONObject(i);
//							obj = obj.getJSONObject("schools");
//							listSchool.add(obj.getString("school_name"));
//							listSchoolId.add(obj.getString("school_id"));
//						}
//
//						loadProfile(isUpload);
//						
//					} catch (Exception e) {
//						NetworkUtils.connectionHandler(EditProfile.this, jsonStr, e.getMessage());
//						Log.e(TAG, "Error skills >>> " + e.getMessage());
//					}
//				} else {
//					Toast.makeText(context,
//							getString(R.string.server_error),
//							Toast.LENGTH_SHORT).show();
//				}
//			}
//		};
//
//		progress = ProgressDialog.show(context, "Profile", "Loading school...",
//				true, false);
//		new Thread() {
//			public void run() {
//				jsonParser = new JSONParser();
//				jsonStr = jsonParser.getHttpResultUrlGet(url);
//
//				if (progress != null && progress.isShowing()) {
//					progress.dismiss();
//					mHandlerFeed.post(mUpdateResultsFeed);
//				}
//			}
//		}.start();
//	}
	
//	@Deprecated
//	private void loadProfile(final boolean isUpload) {
//		Log.d(TAG, "Load profile called");
//		
//		final String url = SERVERURL + CommonUtilities.API_GET_PROFILE + "?"
//				+ PARAM_PT_ID + "=" + pt_id;
//		final Handler mHandlerFeed = new Handler();
//		final Runnable mUpdateResultsFeed = new Runnable() {
//			public void run() {
//				if (jsonStr != null || jsonStr.length() > 0) {
//					profileInfo = new ProfileModel();
//					try {
//						JSONObject obj = new JSONObject(jsonStr);
//						obj = obj.getJSONObject("part_timers");
//
//						JSONObject bank = obj.getJSONObject("bank_account");
//						JSONObject ec = obj.getJSONObject("emergency_contact");
//						
////						profileInfo.setAddress(obj.getString("address"));
////						profileInfo.setPost_code(obj.getString("postal_code"));
//						
//						// Convert date birth format
//						Date dobDate = facebookDateFormat.parse(obj.getString("dob"));
//						String dobConvert = dobDateFormat.format(dobDate);
//						profileInfo.setDob(dobConvert);						
//						
//						profileInfo.setEmail(obj.getString("email"));
//						profileInfo.setWork_exp(obj
//								.getString("work_experience"));
////						profileInfo.setFirst_name(obj.getString("first_name"));
////						profileInfo.setLast_name(obj.getString("last_name"));
//						profileInfo.setPhone_num(obj.getString("phone_no"));
//						profileInfo.setGender(obj.getString("gender"));
//						profileInfo.setProfile_pic(obj
//								.getString("profile_picture"));
//						profileInfo.setIc_back_picture(obj
//								.getString("ic_back_picture"));
//						profileInfo.setIc_front_picture(obj
//								.getString("ic_front_picture"));
//						profileInfo.setIc_no(obj.getString("ic_no"));
//						profileInfo.setIc_type(obj.getString("ic_type"));
//						profileInfo.setIc_type_id(obj.getString("ic_type_id"));
//						
//						// School only for ic student
//						String studentSchoolname = obj.optString("school_name");
//						if(studentSchoolname != "") {
//							profileInfo.setSchool(obj.getString("school_name"));							
//						}
//
//						// Matric card only for ic student
//						String studentMatricCard = obj.optString("matric_card_no");
//						if(studentMatricCard != "") {
//							profileInfo.setCard_number(obj.getString("matric_card_no"));							
//						}						
//						
//						// Student ic expired
//						String studentIcExpired = obj.optString("ic_expiry_date");
//						if(studentIcExpired != "") {
//							String expiryDate = obj.getString("ic_expiry_date");
//							if(expiryDate != "null") {
//								// Convert date birth format
//								Date expireDate = facebookDateFormat.parse(obj.getString("ic_expiry_date"));
//								String expireConvert = dobDateFormat.format(expireDate);
//								profileInfo.setIc_expired(expireConvert);								
//							}
//							expiryDateLayout.setVisibility(View.VISIBLE);
//						} else {
//							expiryDateLayout.setVisibility(View.GONE);
//						}
//
////						String s = obj.getString("skills");
////						if (s != null && !s.equalsIgnoreCase("null") && s.length() > 2) {
////							s = s.substring(1, s.length() - 1);
////							String[] arr = s.split(",");
////							List<Integer> l = new ArrayList<Integer>();
////							for (String tmp : arr) {
////								l.add(Integer.parseInt(tmp.trim()));
////							}
////							profileInfo.setSkill(l);
////						}
//						Log.d(TAG, "Load profile from " + url + " with result >>>\n" + jsonStr.toString());
//						
//						// Loading Bank data
//						profileInfo.setBank_ID(bank.getString("bank_id"));
//						profileInfo.setBank_name(bank.getString("bank_name"));
//						profileInfo.setBank_acc_number(bank
//								.getString("bank_account_no"));
////						profileInfo.setBank_acc_type(obj
////								.getString("bank_account_type"));
//						profileInfo.setBank_acc_branch(bank
//								.getString("bank_branch_name"));
//						profileInfo.setBank_statement(bank
//								.getString("bank_statement"));
//						
//						// Loading EC data
////						profileInfo.setEc_address(obj.getString("ec_address"));
////						profileInfo.setEc_email(obj.getString("ec_email"));
//						profileInfo.setEc_name(ec.getString("ec_full_name"));
//						profileInfo.setEc_phone(obj.getString("ec_phone_no"));
////						profileInfo.setEc_post_code(obj
////								.getString("ec_postal_code"));
//						profileInfo.setEc_relationship(obj
//								.getString("ec_relationship"));
//						
//						updateLayout();
//						
//						if(isUpload) {
//							uploadImageToServer(uploadImage, uploadURL, selectedPhotoId);
//						}
//						
////						loadBankInfo();
//					} catch (Exception e) {
//						NetworkUtils.connectionHandler(EditProfile.this, jsonStr, e.getMessage());
//
//						Log.e(TAG, "Get profile error >> " + e.getMessage());
//						Toast.makeText(context,
//								getString(R.string.edit_get_profile_error),
//								Toast.LENGTH_SHORT).show();
//					}
//				} else {
//					Toast.makeText(context,
//							getString(R.string.server_error),
//							Toast.LENGTH_SHORT).show();
//				}
//			}
//		};
//
//		progress = ProgressDialog.show(context, "Profile",
//				"Getting profile info...", true, false);
//		new Thread() {
//			public void run() {
//				jsonParser = new JSONParser();
//				jsonStr = jsonParser.getHttpResultUrlGet(url);
//				if (progress != null && progress.isShowing()) {
//					progress.dismiss();
//					mHandlerFeed.post(mUpdateResultsFeed);
//				}
//			}
//		}.start();
//	}
	
}
