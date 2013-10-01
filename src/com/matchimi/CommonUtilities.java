package com.matchimi;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.matchimi.utils.ApplicationUtils;

public final class CommonUtilities {
	public static final String TAG = "matchimi";
	public static final String SERVERURL = "http://matchimi.buuukapps.com/";
//	public static final String SERVERURL = "http://192.168.43.214:8000/";
	
	public static final Boolean USER_REGISTERED = false;
	public static final String PREFS_NAME = "MatchimiUserPrefs";
	public static final String USER_FIRSTNAME = "userfirstname";
	public static final String USER_LASTNAME = "userlastname";
	public static final String USER_EMAIL = "useremail";
	public static final String USER_GENDER = "user.gender";
	public static final String USER_PASSWORD = "userpassword";
	public static final String USER_NRIC_TYPE = "usernrictype";
	public static final String USER_NRIC_TYPE_ID = "usernrictypeid";
	public static final String USER_NRIC_NUMBER = "usernricnumber";
	public static final String USER_PTID = "user.ptid";
	public static final String USER_RATING = "userrating";
	public static final String USER_BIRTHDAY = "userbirthday";
	public static final String USER_NRIC_FRONT = "usernricfront";
	public static final String USER_NRIC_BACK = "usernricback";
	public static final String USER_PROFILE_PICTURE = "user.image";
	public static final String USER_CARD_IMG = "user.card.image";
	public static final String USER_IS_VERIFIED = "is_verified";
	public static final String USER_WORK_EXPERIENCE = "working_experience";
	public static final String USER_PHONE_NUMBER = "phone_no";	
	public static final String USER_PROFILE_COMPLETE = "user_profile_complete";
	public static final String LOGOUT = "logout";
	public static final String NOINTERNET = "nointernet";
	public static final String FILECORRUPT = "filecorrupt";
	public static final int AGE_LIMITATION = 14;
	
	public static final String HOMEPAGE_OPTION = "homepageoption";
	public static final String PAGEPROFILE = "pageprofile";

	public static final String USER_FACEBOOK_ID = "userfacebookid";
	public static final String LOGGED = "logged";
	public static final String LOGGED_REGISTER = "loggedregister";

	public static final String REGISTERFORM_REQUIRED_MSG = "required";
	public static final String REGISTERFORM_INVALID_EMAIL = "Invalid email address";
	public static final String REGISTERFORM_INVALID_PASSWORD = "Minimum 4 characters required";
	public static final int CAMERA_REQUEST = 686868;

	public static final String API_ACCEPT_JOB_OFFER = "accept_job_offer";
	public static final String API_UPLOAD_PROFILE_PICTURE = "upload_profile_pic";
//	public static final String API_UPLOAD_PROFILE_PICTURE = "posts/upload/";	
	public static final String API_UPLOAD_FRONT_NRIC_PHOTOS = "upload_nric_front";
	public static final String API_UPLOAD_BACK_NRIC_PHOTOS = "upload_nric_back";
	public static final String API_UPLOAD_MATRIC_PHOTOS = "upload_matric_card_picture";
	public static final String API_CREATE_PARTTIMER_PROFILE = "create_part_timer_profile";
	public static final String API_CREATE_AND_PART_TIMER_PROFILE = "create_and_part_timer_profile";	
	public static final String API_CREATE_PARTIMER_LOGIN = "create_part_timer_login";
	public static final String API_CREATE_AND_PARTIMER_LOGIN = "create_and_part_timer_login";
	public static final String API_CREATE_AND_AVAILABILITY = "create_and_availability";
	public static final String API_CHECK_PARTIMER_VERIFIED = "check_part_timer_verified";	
	public static final String API_DELETE_AVAILABILITY_BY_AVAIL_ID = "delete_availability_by_avail_id";	
	public static final String API_EDIT_AVAILABILITY = "edit_availability";
	public static final String API_EDIT_AND_PART_TIMER_PROFILE = "edit_and_part_timer_profile";
	public static final String API_EDIT_AND_AVAILABILITY = "edit_and_availability";
	public static final String API_FREEZE_AVAILABILITY_BY_AVAIL_ID = "freeze_availability";	
	public static final String API_FORGET_PART_TIMER_PASSWORD = "forget_part_timer_password";
	public static final String API_GET_AVAILABILITY_BY_AVAIL_ID = "get_availability_by_avail_id";
	public static final String API_GET_PART_TIMER_BY_PT_ID = "get_part_timer_by_pt_id";
	public static final String API_GET_BLOCKED_COMPANIES_BY_PT_ID = "get_blocked_companies_by_pt_id";
	public static final String API_GET_CURRENT_JOB_OFFERS = "get_current_job_offers";
	public static final String API_GET_CURRENT_ACCEPTED_JOB_OFFERS = "get_current_accepted_job_offers";
	public static final String API_GET_PART_TIMER_PROFILE_WITH_DEFAULT_DATA_BY_PT_ID = "get_part_timer_profile_with_default_data_by_pt_id";
	public static final String API_GET_SCHOOLS = "get_schools";
	public static final String API_GET_IC_TYPES = "get_ic_types";
	public static final String API_GET_SKILLS = "get_skills";
	public static final String API_GET_GENDERS = "get_genders";
	public static final String API_GET_FEEDBACKS_BY_PT_ID = "get_feedbacks_by_pt_id";
	public static final String API_GET_FREEZE_AVAILABILITY = "get_freeze_availabilities";	
	public static final String API_GET_PROFILE = "get_mobile_part_timer_profile_by_pt_id";
	public static final String API_GET_PAST_ACCEPTED_JOB_OFFERS = "get_past_accepted_job_offers";
	public static final String API_GET_BANK_INFO = "get_bank_account_by_pt_id";
	public static final String API_GET_EC_INFO = "get_emergency_contact_by_pt_id";	
	public static final String API_GET_PART_TIMER_BY_AVAILABLE_ID = "get_part_timer_by_avail_id";
	public static final String API_GET_AVAILABILITIES_BY_PT_ID = "get_availabilities_by_pt_id";
	public static final String API_LOGIN_PART_TIMER = "login_part_timer";
	public static final String API_RESEND_VERIFICATION_EMAIL = "resend_verification_email";	
	public static final String API_REJECT_JOB_OFFER = "reject_job_offer";
	public static final String API_UNFREEZE_AVAILABILITY_BY_AVAIL_ID  = "unfreeze_availability";	
	public static final String API_WITHDRAW_AVAILABILITY = "withdraw_availability";
	
	public static final String COMMON_EMAIL = "email";
	public static final String COMMON_PART_TIMER = "part_timer";
	public static final String COMMON_PASSWORD = "password";
	public static final String COMMON_DATA = "data";
	public static final String COMMON_END = "end";
	public static final String COMMON_START = "start";
	public static final String COMMON_REPEAT = "repeat";
	public static final String COMMON_LOCATION = "location";
	public static final String COMMON_PRICE = "price";	
	public static final String COMMON_UPDATE = "update";	

	
	// public static final String API_UPLOAD_FRONT_NRIC_PHOTOS =
	// "posts/upload/";
	public static final String PARAM_DATA = "data";	
	public static final String PARAM_PART_TIMER = "part_timer";
	public static final String PARAM_PT_ID = "pt_id";
	public static final String PARAM_AVAIL_ID = "avail_id";
	public static final String PARAM_PROFILE_PARTIMER = "part_timers";
	public static final String PARAM_PROFILE_ADDRESS = "address";
	public static final String PARAM_PROFILE_DATE_OF_BIRTH = "dob";
	public static final String PARAM_PROFILE_EMAIL = "email";
	public static final String PARAM_PROFILE_FIRSTNAME = "first_name";
	public static final String PARAM_PROFILE_LASTNAME = "last_name";
	public static final String PARAM_PROFILE_GENDER = "gender";
	public static final String PARAM_PROFILE_ACCEPT_SCORE = "acpt_score";
	public static final String PARAM_PROFILE_USER_PASSWORD = "password";
	public static final String PARAM_PROFILE_POSTAL_CODE = "postal_code";
	public static final String PARAM_PROFILE_WORK_EXPERIENCE = "work_experience";
	public static final String PARAM_PROFILE_PERF_SCORE = "perf_score";
	public static final String PARAM_PROFILE_DEFAUT_PART_TIMER = "part_timer";
	public static final String PARAM_PROFILE_DEFAUT_PART_TIMER_GENDER = "genders";
	public static final String PARAM_PROFILE_DEFAUT_PART_TIMER_IC_TYPE = "ic_types";
	public static final String PARAM_PROFILE_DEFAUT_PART_TIMER_SKILL = "skills";
	public static final String PARAM_PROFILE_DEFAUT_PART_TIMER_SCHOOL = "schools";
	
	public static final String PARAM_PROFILE_IC_BACK_PICTURE = "ic_back_picture";
	public static final String PARAM_PROFILE_IC_FRONT_PICTURE = "ic_front_picture";
	public static final String PARAM_PROFILE_IC_TYPE_ID = "ic_type_id";
	public static final String PARAM_PROFILE_IC_TYPE = "ic_type";
	public static final String PARAM_PROFILE_IC_NUMBER = "ic_no";
	
	public static final String PARAM_PROFILE_IS_IDLE = "is_idle";
	public static final String PARAM_PROFILE_IS_BLACKLISTED = "is_blacklisted";
	public static final String PARAM_PROFILE_IS_VERIFIED = "is_verified";
	public static final String PARAM_PROFILE_IS_DEACTIVATED = "is_deactivated";
	public static final String PARAM_PROFILE_PICTURE = "profile_picture";
	public static final String PARAM_PROFILE_PROFILE_SOURCE = "profile_source";
	public static final String PARAM_PROFILE_GRADE_ID = "pt_grade_id";

	public static final String PARAM_PROFILE_NUMBER_OFFERS = "no_of_offers";
	public static final String PARAM_PROFILE_NUMBER_ACCEPTS = "no_of_accepts";
	public static final String PARAM_PROFILE_NUMBER_OF_REJECTS = "no_of_rejects";
	public static final String PARAM_PROFILE_PHONE_NUMBER = "phone_no";

	public static final String PARAM_PROFILE_SCHOOL_NAME = "school_name";
	public static final String PARAM_PROFILE_MATRIC_CARD_NO = "matric_card_no";
	public static final String PARAM_PROFILE_STUDENT_IC_EXPIRY_DATE = "ic_expiry_date";

	public static final String PARAM_PROFILE_SKILLS= "skills";

	public static final String PARAM_PROFILE_CREATED = "created_at";
	public static final String PARAM_PROFILE_JOINED_DATE = "joined_date";
	public static final String PARAM_PROFILE_UPDATED = "updated_at";
	public static final String PARAM_PROFILE_LAST_SEEN_DATE = "last_seen_date";
	
	public static final String PARAM_PROFILE_BANK_NAME = "bank_name";
	public static final String PARAM_PROFILE_BANK_BRANCH_NAME = "bank_branch_name";
	public static final String PARAM_PROFILE_BANK_ACCOUNT_NO = "bank_account_no";
	
	public static final String PARAM_PROFILE_EC_RELATIONSHIP = "ec_relationship";
	public static final String PARAM_PROFILE_EC_PHONE_NO = "ec_phone_no";
	public static final String PARAM_PROFILE_EC_FIRST_NAME = "ec_first_name";
	

	public static final String PARAM_BLOCKED_COMPANIES= "companies";
	public static final String PARAM_BLOCKED_COMPANIES_ADDRESS = "address";
	public static final String PARAM_BLOCKED_COMPANIES_GRADE_ID = "company_grade_id";
	public static final String PARAM_BLOCKED_COMPANIES_ID = "company_id";
	public static final String PARAM_BLOCKED_COMPANIES_NAME = "company_name";
	public static final String PARAM_BLOCKED_COMPANIES_TYPE_ID = "company_type_id";
	public static final String PARAM_BLOCKED_COMPANIES_CREATED = "created_at";
	public static final String PARAM_BLOCKED_COMPANIES_EMAIL_1 = "email_1";
	public static final String PARAM_BLOCKED_COMPANIES_EMAIL_2 = "email_2";
	public static final String PARAM_BLOCKED_COMPANIES_FAX_NO_1 = "fax_no_1";
	public static final String PARAM_BLOCKED_COMPANIES_FAX_NO_2 = "fax_no_2";
	public static final String PARAM_BLOCKED_COMPANIES_IS_BLACKLISTED = "is_blacklisted";
	public static final String PARAM_BLOCKED_COMPANIES_IS_VERIFIED = "is_verified";
	public static final String PARAM_BLOCKED_COMPANIES_PERF_SCORE = "perf_score";
	public static final String PARAM_BLOCKED_COMPANIES_PHONE_1 = "phone_no_1";
	public static final String PARAM_BLOCKED_COMPANIES_PHONE_2 = "phone_no_2";
	public static final String PARAM_BLOCKED_COMPANIES_POSTAL_CODE = "postal_code";
	public static final String PARAM_BLOCKED_COMPANIES_REGISTERED_DATE = "registered_date";
	public static final String PARAM_BLOCKED_COMPANIES_REGISTRATION_NO = "registration_no";
	public static final String PARAM_BLOCKED_COMPANIES_UPDATE = "updated_at";
	
	public static final String PARAM_FEEDBACK_AVAILABILITIES = "availabilities";
	public static final String PARAM_FEEDBACK_COMPANY_NAME = "company_name";
	public static final String PARAM_FEEDBACK_COMPANY_BRANCH = "branch_name";
	public static final String PARAM_FEEDBACK_COMPANY_COMMENT = "feedback";
	public static final String PARAM_FEEDBACK_COMPANY_GRADE = "grade";
	
	public static final String INTENT_REJECT_IS_BLOCKED = "reject_is_blocked";
	
	public static final String FILE_IMAGE_PROFILE = "profile_";
	public static final String FILE_IC_FRONT = "ic_front";
	public static final String FILE_IC_BACK = "ic_back";
	public static final String FILE_CARD = "card_";
	public static final String IMAGE_ROOT = ApplicationUtils.getAppRootDir() + "/";
	
	public static final String SETTING_THEME = "setting.theme";
	public static final int THEME_LIGHT = 0;
	public static final int THEME_DARK = 1;

	public static final String LOGIN = "login";
	public static final String REGISTERED = "registered";
	
	public static final SimpleDateFormat AVAILABILITY_DATE = new SimpleDateFormat("EE d, MMM",
			Locale.getDefault());
	public static final SimpleDateFormat AVAILABILITY_TIME = new SimpleDateFormat("hh:mm a",
			Locale.getDefault());
	public static final SimpleDateFormat AVAILABILTY_DATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
}
