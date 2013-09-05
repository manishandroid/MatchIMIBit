package com.matchimi;

public final class CommonUtilities {
	public static final String TAG = "matchimi";
	public static final String SERVERURL = "http://matchimi.buuukapps.com/";
	// public static final String SERVERURL = "http://192.168.43.214:8000/";
	public static final Boolean USER_REGISTERED = false;
	public static final String PREFS_NAME = "MatchimiUserPrefs";
	public static final String USER_FIRSTNAME = "userfirstname";
	public static final String USER_LASTNAME = "userlastname";
	public static final String USER_EMAIL = "useremail";
	public static final String USER_PASSWORD = "userpassword";
	public static final String USER_NRIC = "usernric";
	public static final String USER_RATING = "userrating";
	public static final String USER_BIRTHDAY = "userbirthday";
	public static final String USER_NRIC_FRONT = "usernricfront";
	public static final String USER_NRIC_BACK = "usernricback";
	public static final String LOGOUT = "logout";
	
	public static final String HOMEPAGE_OPTION = "homepageoption";
	public static final String PAGEPROFILE = "pageprofile";

	public static final String USER_FACEBOOK_ID = "userfacebookid";
	public static final String LOGGED = "logged";
	public static final String LOGGED_REGISTER = "loggedregister";

	public static final String REGISTERFORM_REQUIRED_MSG = "required";
	public static final String REGISTERFORM_INVALID_EMAIL = "Invalid email address";
	public static final String REGISTERFORM_INVALID_PASSWORD = "Minimum 4 characters required";
	public static final int CAMERA_REQUEST = 686868;

	public static final String API_PT_ID = "37";
	public static final String API_UPLOAD_FRONT_NRIC_PHOTOS = "upload_nric_front";
	public static final String API_CREATE_PARTTIMER_PROFILE = "create_part_timer_profile";
	public static final String API_CREATE_PARTIMER_LOGIN = "create_part_timer_login";
	public static final String API_CREATE_AVAILABILITY = "create_availability";
	public static final String API_GET_PART_TIMER_BY_PT_ID = "get_part_timer_by_pt_id";
	public static final String API_GET_BLOCKED_COMPANIES_BY_PT_ID = "get_blocked_companies_by_pt_id";
	public static final String API_GET_CURRENT_JOB_OFFERS = "get_current_job_offers";
	public static final String API_GET_SCHOOLS = "get_schools";
	public static final String API_GET_IC_TYPES = "get_ic_types";
	public static final String API_GET_GENDERS = "get_genders";
	public static final String API_GET_PART_TIMER_BY_AVAILABLE_ID = "get_part_timer_by_avail_id";
	public static final String API_GET_FEEDBACKS_BY_PT_ID = "get_feedbacks_by_pt_id";
	
//	public static final String API_UPLOAD_FRONT_NRIC_PHOTOS = "posts/upload/";	
	public static final String PARAM_DATA= "data";
	public static final String PARAM_PART_TIMER = "part_timer";
	public static final String PARAM_PT_ID = "pt_id";
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
	
	public static final String PARAM_PROFILE_CREATED = "created_at";	
	public static final String PARAM_PROFILE_JOINED_DATE = "joined_date";
	public static final String PARAM_PROFILE_UPDATED = "updated_at";
	public static final String PARAM_PROFILE_LAST_SEEN_DATE= "last_seen_date";
	
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

	public static final String APP_SETTING = "matchimi.setting";
	public static final String SETTING_THEME = "setting.theme";
	public static final int THEME_LIGHT = 0;
	public static final int THEME_DARK = 1;
	
	public static final String LOGIN = "login";
	public static final String REGISTERED = "registered";

}
