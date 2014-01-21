package com.matchimi;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.joda.time.format.DateTimeFormat;

import android.content.Context;
import android.content.Intent;

import com.matchimi.utils.ApplicationUtils;

public final class CommonUtilities {	
	public static final String IS_FIRSTTIME = "isFirstTime";
	public static final String TAG = "matchimi";
	
//	public static final String SERVERURL = "http://54.254.221.30/";
//	public static final String SERVERURL_SECURE = "http://54.254.221.30/";
	
	public static final String BUGSENSE_KEY = "657a6840";
	
	public static final String SERVERURL = "http://api.staging.matchimi.com/";
	public static final String SERVERURL_SECURE = "https://api.staging.matchimi.com/";
	public static final String GCM_SERVER_URL = "http://api.staging.matchimi.com/";

//	public static final String SERVERURL = "http://api.matchimi.com/";
//	public static final String SERVERURL_SECURE = "http://api.matchimi.com/";	
//	public static final String GCM_SERVER_URL = "http://api.matchimi.com/";
	
//  public static final String GCM_SERVER_URL = "http://pongmob.com:8080/gcm-demo/";
	
//	public static final String SERVERURL = "http://192.168.43.214:8000/";
	
	public static final String SERVER_PROBLEM = "serverproblem";
	
	public static final Boolean USER_REGISTERED = false;
	public static final String PREFS_NAME = "MatchimiUserPrefs";
	public static final String USER_FULLNAME = "userfullname";
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
	
	public static final String API_VERSION = "api_version";
	public static final String API_CACHE_LOCATIONS = "cache_locations";
	public static final String API_CACHE_DAYS = "cache_days";
	public static final String API_CACHE_NRIC_TYPES = "cache_nric_types";
	public static final String API_CACHE_GENDERS = "cache_genders";
	public static final String API_CACHE_JOB_FUNCTIONS = "cache_job_functions";
	public static final String API_CACHE_SCHOOLS = "cache_schools";
	public static final String SHARED_FACEBOOK_FRIENDSDATE = "fb_friends_date";
	public static final String SHARED_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
	public static int SHARED_LIMIT_DAYS = 30;
	
	public static final String HOMEPAGE_OPTION = "homepageoption";
	public static final String PAGEPROFILE = "pageprofile";

	public static final String USER_FACEBOOK_ID = "userfacebookid";
	public static final String LOGGED = "logged";
	public static final String LOGGED_REGISTER = "loggedregister";

	public static final String REGISTERFORM_REQUIRED_MSG = "required";
	public static final String REGISTERFORM_INVALID_EMAIL = "Invalid email address";
	public static final String REGISTERFORM_INVALID_PASSWORD = "Your password is incorrect";
	public static final int CAMERA_REQUEST = 686868;

	public static final String API_ACCEPT_JOB_OFFER = "accept_job_offer";
	public static final String API_UPLOAD_PROFILE_PICTURE_BY_PT_ID = "upload_profile_pic_by_pt_id";
	
//	public static final String API_UPLOAD_PROFILE_PICTURE = "posts/upload/";	
	public static final String API_UPLOAD_FRONT_NRIC_PHOTOS = "upload_nric_front";
	public static final String API_UPLOAD_BACK_NRIC_PHOTOS = "upload_nric_back";
	
	public static final String API_UPLOAD_STUDENT_FRONT_PHOTOS = "upload_sid_front_pic_by_pt_id";
	public static final String API_UPLOAD_STUDENT_BACK_PHOTOS = "upload_sid_back_pic_by_pt_id";
	public static final String API_UPLOAD_BANK_STATEMENT_BY_PT_ID = "upload_bank_statement_by_pt_id";
	public static final String API_PART_TIMER_LOGGED_OUT = "part_timer_logged_out";
	
	public static final String API_CREATE_PARTTIMER_PROFILE = "create_part_timer_profile";
	public static final String API_CREATE_AND_PART_TIMER_FB_LOGIN = "create_and_part_timer_fb_login";
	
	public static final String API_CREATE_AND_PART_TIMER_PROFILE = "create_and_part_timer_profile";
	public static final String API_CREATE_AND_PART_TIMER_REGISTRATION = "create_and_part_timer_registration_v2";
	
	public static final String API_CREATE_PARTIMER_LOGIN = "create_part_timer_login";
	public static final String API_CREATE_AND_PARTIMER_LOGIN = "create_and_part_timer_login";
	public static final String API_CREATE_AND_AVAILABILITY = "create_and_availability";
	public static final String API_CHECK_PARTIMER_VERIFIED = "check_part_timer_verified";	
	public static final String API_DELETE_AVAILABILITY_BY_AVAIL_ID = "delete_availability_by_avail_id";	
	public static final String API_DELETE_REPEATEAD_AVAILABILITY = "delete_repeated_availability";	
	public static final String API_EDIT_AVAILABILITY = "edit_availability";
	public static final String API_EDIT_REPEATED_AVAILABILITY = "edit_repeated_availability";
	public static final String API_EDIT_AND_PART_TIMER_PROFILE = "edit_and_part_timer_complete_profile_v2";
	public static final String API_EDIT_AND_AVAILABILITY = "edit_and_availability";
	public static final String API_FREEZE_AVAILABILITY_BY_AVAIL_ID = "freeze_availability";	
	public static final String API_FORGET_PART_TIMER_PASSWORD = "forget_part_timer_password";
	public static final String API_GET_AVAILABILITY_BY_AVAIL_ID = "get_availability_by_avail_id";
	public static final String API_GET_PART_TIMER_BY_PT_ID = "get_part_timer_by_pt_id";
	public static final String API_GET_BLOCKED_COMPANIES_BY_PT_ID = "get_blocked_branches_by_pt_id";
	public static final String API_CREATE_REPEATED_AVAILABILITY = "create_and_repeated_availability";
	public static final String API_GET_CURRENT_JOB_OFFERS = "get_current_job_offers";

	public static final String API_GRADE_EMPLOYER = "grade_employer";
	public static final String API_GET_CURRENT_ACCEPTED_JOB_OFFERS = "get_current_accepted_job_offers";
	
	public static final String API_GET_PART_TIMER_PROFILE_WITH_DEFAULT_DATA_BY_PT_ID = "get_part_timer_profile_with_default_data_v2";
	public static final String API_GET_PROFILE_DEFAULT_DATA = "get_profile_default_data";
	public static final String API_GET_SCHOOLS = "get_schools";
	public static final String API_GET_IC_TYPES = "get_ic_types";
	public static final String API_GET_SKILLS = "get_skills";
	public static final String API_GET_GENDERS = "get_genders";
	public static final String API_GET_FEEDBACKS_BY_PT_ID = "get_feedbacks_by_pt_id";
	public static final String API_GET_FREEZE_AVAILABILITY = "get_freeze_availabilities";	
	public static final String API_GET_PROFILE = "get_part_timer_profile_v2";
	
	public static final String API_GET_PAST_ACCEPTED_JOB_OFFERS = "get_past_accepted_job_offers";
	
	public static final String API_GET_BANK_INFO = "get_bank_account_by_pt_id";
	public static final String API_GET_EC_INFO = "get_emergency_contact_by_pt_id";	
	public static final String API_GET_PART_TIMER_BY_AVAILABLE_ID = "get_part_timer_by_avail_id";
	public static final String API_GET_AVAILABILITIES_BY_PT_ID = "get_availabilities_by_pt_id";
	
	public static final String API_LOGIN_PART_TIMER = "login_part_timer_v2";
	public static final String API_LOGIN_FB_PART_TIMER = "login_fb_part_timer_v2";
	
	public static final String API_RESEND_VERIFICATION_EMAIL = "resend_verification_email";	
	public static final String API_REJECT_JOB_OFFER = "reject_job_offer";
	
	public static final String API_GET_DAYS = "get_days";
	public static final String API_GET_DAY_TIMES = "get_day_times";
	public static final String API_GET_LOCATIONS = "get_locations";
	public static final String API_GET_STATIC_DATA = "get_static_data";

	public static final String API_UNFREEZE_AVAILABILITY_BY_AVAIL_ID  = "unfreeze_availability";	
	public static final String API_WITHDRAW_AVAILABILITY = "withdraw_availability";
	public static final String API_GET_PROFILE_PIC = "get_profile_pic_by_pt_id";
	public static final String API_GET_IC_BACK_PIC_BY_PT_ID = "get_ic_back_pic_by_pt_id";
	public static final String API_GET_IC_FRONT_PIC_BY_PT_ID = "get_ic_front_pic_by_pt_id";
	public static final String API_GET_BANK_STATEMENT_BY_PT_ID = "get_bank_statement_by_pt_id";
	
	public static final String API_GET_STUDENT_FRONT_PHOTOS = "get_sid_front_pic_by_pt_id";
	public static final String API_GET_STUDENT_BACK_PHOTOS = "get_sid_back_pic_by_pt_id";
	
	public static final String API_GET_VISUME_BY_PT_ID = "get_visume_by_pt_id";	
	public static final String API_GET_PREFERRED_JOB_FUNCTIONS = "get_preferred_job_functions";
	
	public static final String API_CREATE_PART_TIMER_FRIENDS_BY_PT_ID = "create_part_timer_friends_by_pt_id";
	public static final String API_UPDATE_PART_TIMER_FRIENDS_BY_PT_ID = "update_part_timer_friends_by_pt_id";
	
	public static final String API_EDIT_AND_PREFERRED_JOB_FUNCTION = "edit_and_preferred_job_functions_v2";
	
	public static final String API_SET_MESSAGE_READ = "mark_pt_message_as_read";
	public static final String API_GET_MESSAGE = "get_messages_by_avail_id";
	public static final String API_REPLY_MESSAGE = "create_avail_message";
	
	public static final String API_SET_RATING = "set_rating";
	public static final String API_GET_RATING = "get_rating";
	public static final String API_GET_FRIEND_BY_PT_ID = "get_friend_by_pt_id";
	public static final String API_GET_ALL_FRIEND = "get_all_friends";
	public static final String API_REQUEST_FRIENDSHIP = "request_friendship";
	
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
	public static final String COMMON_FACEBOOK_ID = "facebook_id";
	
	public static final int RATING_SCHEDULE = 4;
	
	// public static final String API_UPLOAD_FRONT_NRIC_PHOTOS =
	// "posts/upload/";
	public static final String PARAM_DATA = "data";	
	public static final String PARAM_PART_TIMER = "part_timer";
	public static final String PARAM_PT_ID = "pt_id";
	public static final String PARAM_AVAIL_ID = "avail_id";
	public static final String PARAM_RAVAIL_ID = "ravail_id";
	
	public static final String PARAM_PROFILE_PARTIMER = "part_timers";
	public static final String PARAM_PROFILE_ADDRESS = "address";
	public static final String PARAM_PROFILE_DATE_OF_BIRTH = "dob";
	public static final String PARAM_PROFILE_EMAIL = "email";
	public static final String PARAM_PROFILE_FULL_NAME= "full_name";
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
	public static final String PARAM_YEAR = "year";
	public static final String PARAM_MONTH = "month";
	public static final String PARAM_TOTAL_MONEY = "total_money";
	public static final String PARAM_TOTAL_TIME = "total_time";
	
	public static final String PARAM_PROFILE_IC_BACK_PICTURE = "ic_back_picture";
	public static final String PARAM_PROFILE_IC_FRONT_PICTURE = "ic_front_picture";
	public static final String PARAM_PROFILE_IC_TYPE_ID = "ic_type_id";
	public static final String PARAM_PROFILE_IC_TYPE = "ic_type";
	public static final String PARAM_PROFILE_IC_NUMBER = "ic_no";
	
	public static final String PARAM_PROFILE_POST_IC_BACK_PICTURE = "ic_back_pic";
	public static final String PARAM_PROFILE_POST_IC_FRONT_PICTURE = "ic_front_pic";
	
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
	public static final String PARAM_PROFILE_SCHOOL_ID = "school_id";
	public static final String PARAM_PROFILE_MATRIC_CARD_NO = "matric_card_no";
	public static final String PARAM_PROFILE_STUDENT_IC_EXPIRY_DATE = "ic_expiry_date";
	public static final String PARAM_PROFILE_IS_STUDENT = "is_student";
	public static final String PARAM_PROFILE_STUDENT_DETAILS = "student_details";
	public static final String PARAM_PROFILE_EMERGENCY_CONTACT = "emergency_contact";
	
	public static final String PARAM_STATIC_JOB_FUNCTIONS = "job_functions";
	public static final String PARAM_STATIC_DAY_TIMES = "day_times";
	public static final String PARAM_STATIC_DAYS = "days";
	public static final String PARAM_STATIC_LOCATIONS = "locations";
	public static final String PARAM_STATIC_NRIC_TYPES = "nric_types";
	public static final String PARAM_STATIC_SCHOOLS = "schools";
	public static final String PARAM_STATIC_GENDERS = "genders";
	public static final String PARAM_STATIC_CITIZENSHIP = "citizenships";
	
	public static final String PARAM_PROFILE_IC_STUDENT_FRONT = "sid_front_pic";
	public static final String PARAM_PROFILE_IC_STUDENT_BACK = "sid_back_pic";
	
	public static final String PARAM_PROFILE_SKILLS= "skills";

	public static final String PARAM_PROFILE_CREATED = "created_at";
	public static final String PARAM_PROFILE_JOINED_DATE = "joined_date";
	public static final String PARAM_PROFILE_UPDATED = "updated_at";
	public static final String PARAM_PROFILE_LAST_SEEN_DATE = "last_seen_date";
	
	public static final String PARAM_PROFILE_BANK_NAME = "bank_name";
	public static final String PARAM_PROFILE_BANK_BRANCH_NAME = "bank_branch_name";
	public static final String PARAM_PROFILE_BANK_ACCOUNT_NO = "bank_account_no";
	
	public static final String PARAM_PROFILE_EC_RELATIONSHIP = "relationship";
	public static final String PARAM_PROFILE_EC_PHONE_NO = "phone_no";
	public static final String PARAM_PROFILE_EC_FULLNAME = "full_name";

	public static final String PARAM_BLOCKED_COMPANIES= "branches";
	public static final String PARAM_BLOCKED_COMPANIES_ADDRESS = "address";
	public static final String PARAM_BLOCKED_COMPANIES_GRADE_ID = "company_grade_id";
	public static final String PARAM_BLOCKED_COMPANIES_ID = "company_id";
	public static final String PARAM_BLOCKED_BRANCH_NAME = "branch_name";
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
	
	public static final String PARAM_PREFERRED_JOBS = "pt_preferred_job_functions";
	public static final String PARAM_PREFERRED_JOBS_FUNCTION = "job_function_id";
	public static final String PARAM_PREFERRED_JOBS_NAME = "job_function_name";
	
	public static final String INTENT_REJECT_IS_BLOCKED = "reject_is_blocked";
	
	public static final String FILE_IMAGE_PROFILE = "profile";
	public static final String FILE_IC_FRONT = "ic_front";
	public static final String FILE_IC_BACK = "ic_back";
	public static final String FILE_STUDENT_FRONT = "student_front";
	public static final String FILE_STUDENT_BACK = "student_back";
	public static final String IMAGE_ROOT = ApplicationUtils.getAppRootDir() + "/";
	public static final String ROOT_DIR = ".matchimi";
	
	public static final String SETTING_THEME = "setting.theme";
	public static final int THEME_LIGHT = 0;
	public static final int THEME_DARK = 1;

	public static final String LOGIN = "login";
	public static final String REGISTERED = "registered";
	
	public static final SimpleDateFormat AVAILABILITY_DATE_TEXT = new SimpleDateFormat("EEEE d, MMMMM yyyy", Locale.getDefault());
	public static final SimpleDateFormat AVAILABILITY_TIME = new SimpleDateFormat("hh:mm a",
			Locale.getDefault());
	public static final SimpleDateFormat AVAILABILITY_TIME_FULL = new SimpleDateFormat("HH:mm",
			Locale.getDefault());
	public static final SimpleDateFormat AVAILABILTY_DATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
	public static final SimpleDateFormat AVAILABILITY_DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	
	public static final SimpleDateFormat AVAILABILTY_DATETIME_CALENDAR = new SimpleDateFormat("dd MMMMM yyyy");
	
	public static final String OCCUPIED_DATES = "occupied_dates";
	
	public static final String HOMEAVAILABILITY_BUNDLE = "homeavailability_bundle";
	public static final String IS_AVAILABILITY_CALENDAR  = "is_availability_calendar";
	public static final String AVAILABILITY_SELECTED = "availability_selected";
	public static final int CALENDAR_DAILY_DETAIL = 199;

	public static final String AVAILABILITY_LIST_AVAIL_ID = "availability_list_avail_id";
	public static final String AVAILABILITY_LIST_START_TIME = "availability_start_time";
	public static final String AVAILABILITY_LIST_END_TIME = "availability_end_time";
	public static final String AVAILABILITY_LIST_REPEAT = "availability_list_repeat";
	public static final String AVAILABILITY_LIST_LOCATION = "availability_list_location";
	public static final String AVAILABILITY_SELECTED_DATE = "availability_selected_date";
	public static final String AVAILABILITY_DATA_LOCATION = "location";

	public static final String LOCAL_BROADCAST_AVAILABILITY = "local_broadcast_availability";
	public static final String CREATE_AVAILABILITY_BROADCAST = "create_availability_broadcast";
	public static final String CALENDAR_DATE_YEAR = "calendar_date_year";
	public static final String CALENDAR_DATE_MONTH = "calendar_date_month";
	public static final String CALENDAR_MENU_AVAILABILITY = "calendar_availability";
	
	public static final String SCHEDULE_LIST_JOB_ID = "schedule_list_job_id";
	public static final String SCHEDULE_LIST_AVAIL_ID = "schedule_list_avail_id";
	public static final String SCHEDULE_START_TIME = "schedule_start_time";
	public static final String SCHEDULE_END_TIME = "schedule_end_time";
	public static final String SCHEDULE_PRICE = "schedule_price";
	public static final String SCHEDULE_COMPANY = "schedule_company";
	public static final String SCHEDULE_ADDRESS = "schedule_address";
	public static final String SCHEDULE_DATA_LOCATION = "schedule_location";
	public static final String SCHEDULE_HEADER = "schedule_header";
	public static final String SCHEDULE_TIMEWORK = "schedule_timework";
	public static final String SCHEDULE_LIST_OPTIONAL = "schedule_optional";
	public static final String SCHEDULE_LIST_REQUIREMENT = "schedule_requirement";
	public static final String SCHEDULE_DESCRIPTION = "schedule_description";
	
	public static final String SCHEDULE_LOCAL_BROADCAST = "schedule_local_broadcast";
	public static final Integer RC_SCHEDULE_DETAIL = 11;
	
	public static final String AVAIL_ID = "param_avail_id";
	public static final String AVAIL_START_TIME = "param_avail_start";
	public static final String AVAIL_END_TIME = "param_avail_end";
	public static final String AVAIL_REPEAT = "param_avail_repeat";
	public static final String AVAIL_LOCATION = "param_avail_location";
	public static final String AVAIL_PRICE = "param_avail_price";
	public static final String AVAIL_FREEZE = "param_avail_freeze";
	public static final String AVAIL_DATE = "param_avail_date";
	public static final String TOTAL_EARNING = "param_total_earning";
	public static final String TOTAL_HOURS = "param_total_hours";
	
	public static final String BROADCAST_LOAD_HISTORY = "schedule.history";
	public static final String BROADCAST_SCHEDULE_RECEIVER = "schedule.receiver";
	public static final String BROADCAST_JOBS_RECEIVER = "jobs.receiver";
	public static final String LOCALBROADCAST_SCHEDULE_BACKPRESSED_RECEIVER = "localschedule.backpressed";
	
	public static final int CREATEAVAILABILITY_RC_MAPS_ACTIVITY = 51;
	public static final int CREATEAVAILABILITY_RC_REPEAT = 52;	
	public static final int RC_EDIT_AVAILABILITY = 30;
	
	public static final String CREATEAVAILABILITY_MAP_REGION = "map_region";
	public static final String CREATEAVAILABILITY_MAP_REGION_ID = "map_region_id";
	public static final String CREATEAVAILABILITY_REPEAT_DAYS = "repeat_days";
	public static final String CREATEAVAILABILITY_REPEAT_DAYS_INTEGER = "repeat_days_integer";
	public static final String CREATEAVAILABILITY_REPEAT_START_DATE = "repeat_start";
	public static final String CREATEAVAILABILITY_REPEAT_END_DATE = "repeat_end";
	
	public static final String AVAILABILITY_STATUS_AV = "AV";
	public static final String AVAILABILITY_STATUS_MA = "MA";
	public static final String AVAILABILITY_STATUS_PA = "PA";
	
	public static final String JSON_KEY_REPEAT_DAY = "day";
	public static final String JSON_KEY_REPEAT_DAY_ID = "day_id";
	public static final String JSON_KEY_REPEAT_DAYS = "days";
	public static final String JSON_KEY_LOCATION_ID = "location_id";
	public static final String JSON_KEY_LOCATION_NAME = "location";
	public static final String JSON_KEY_LOCATIONS = "locations";
	public static final String JSON_KEY_NET_WORK_MONEY = "net_work_money";
	public static final String JSON_KEY_NET_WORK_TIME ="net_work_time";
	
	public static final String JSON_KEY_FRIEND_FACEBOOK_ID = "facebook_id";
	public static final String JSON_KEY_FRIEND_FACEBOOK_FIRST_NAME = "first_name";
	public static final String JSON_KEY_FRIEND_FACEBOOK_LAST_NAME = "last_name";
	public static final String JSON_KEY_FRIEND_FACEBOOK_PROFILE_PICTURE = "profile_picture";	
	public static final String JSON_KEY_PART_TIMER_FRIEND = "part_timer_friends";
	
    /**
     * Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
     */
    

    /**
     * Google API project id registered to use GCM.
     */
    public static final String SENDER_ID = "893865425217";

    /**
     * Intent used to display a status message in the screen.
     */
    public static final String DISPLAY_STATUS_ACTION =
            "com.machimi.notifications.DISPLAY_STATUS";

    /**
     * Intent used to display a chat message in the screen.
     */
    public static final String DISPLAY_MESSAGE_ACTION =
            "com.machimi.notifications.DISPLAY_MESSAGE";

    /**
     * Intent's extra that contains the message to be displayed.
     */
    public static final String EXTRA_MESSAGE = "message";

    /**
     * Notifies UI to display a status message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    public static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_STATUS_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
    
    /**
     * Notifies UI to display a chat message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    public static void displayChat(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }

}
