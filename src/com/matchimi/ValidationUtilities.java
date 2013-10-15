package com.matchimi;

import static com.matchimi.CommonUtilities.API_RESEND_VERIFICATION_EMAIL;
import static com.matchimi.CommonUtilities.SERVERURL;
import static com.matchimi.CommonUtilities.TAG;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.matchimi.utils.JSONParser;

public final class ValidationUtilities {
	public static ProgressDialog progress;
	public static JSONParser jsonParser = null;
	public static String jsonStr = null;
	
	/**
	 * Showing Dialog for resending email with link validation
	 * In case user not receive email or asking for new email validation
	 * 
	 * @param context
	 * @param pt_id
	 */
	public static final void resendLinkDialog(final Context context, final String pt_id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getResources().getString(R.string.app_name));
		builder.setMessage(context.getResources().getString(R.string.notify_user_verification))
				.setCancelable(false)
				.setPositiveButton(R.string.resend_link,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int id) {
								// resend link
								resendLink(context, pt_id);
							}
						})
				.setNegativeButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface arg0,
									int arg1) {
							}
						});

		// Create the AlertDialog object and return it
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public static final void resendLink(final Context context, final String pt_id) {
		final String url = SERVERURL + API_RESEND_VERIFICATION_EMAIL;
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						Toast.makeText(context, context.getResources().getString(R.string.resend_email_validation_success),
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(context,
								context.getResources().getString(R.string.resend_email_validation_failed),
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(context,
							context.getResources().getString(R.string.resend_email_validation_failed),
							Toast.LENGTH_LONG).show();
				}
			}
		};

		progress = ProgressDialog.show(context,
				context.getString(R.string.app_name), context.getResources().getString(R.string.please_wait),
				true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				try {
					String[] params = { "pt_id" };
					String[] values = { pt_id };
					jsonStr = jsonParser.getHttpResultUrlPost(url, params,
							values);

					Log.e(TAG, "Resend email link Result >>> " + jsonStr);
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
	
	public static final boolean checkProfileComplete(JSONObject obj) throws JSONException {		
		if(obj.getString(CommonUtilities.PARAM_PROFILE_DATE_OF_BIRTH).length() == 0 || 
				obj.getString(CommonUtilities.PARAM_PROFILE_DATE_OF_BIRTH) == "null") {
			return false;
		}
		
		if(obj.getString(CommonUtilities.PARAM_PROFILE_EMAIL).length() == 0 ||
				obj.getString(CommonUtilities.PARAM_PROFILE_EMAIL) == "null") {
			return false;
		}
		
		if(obj.getString(CommonUtilities.PARAM_PROFILE_WORK_EXPERIENCE).length() == 0 ||
				obj.getString(CommonUtilities.PARAM_PROFILE_WORK_EXPERIENCE) == "null") {
			return false;
		}
		
		if(obj.getString(CommonUtilities.PARAM_PROFILE_FIRSTNAME).length() == 0 ||
				obj.getString(CommonUtilities.PARAM_PROFILE_FIRSTNAME) == "null") {
			return false;
		}
		
		if(obj.getString(CommonUtilities.PARAM_PROFILE_LASTNAME).length() == 0 || 
				obj.getString(CommonUtilities.PARAM_PROFILE_LASTNAME) == "null") {
			return false;
		}
		
		if(obj.getString(CommonUtilities.PARAM_PROFILE_PHONE_NUMBER).length() == 0 ||
				obj.getString(CommonUtilities.PARAM_PROFILE_PHONE_NUMBER) == "null") {
			return false;
		}
		
		if(obj.getString(CommonUtilities.PARAM_PROFILE_GENDER).length() == 0 ||
				obj.getString(CommonUtilities.PARAM_PROFILE_GENDER) == "null") {
			return false;
		}
		
//		if(obj.getString(CommonUtilities.PARAM_PROFILE_PICTURE).length() == 0 ||
//					obj.getString(CommonUtilities.PARAM_PROFILE_PICTURE) == "null") {
//			return false;
//		}
		
		if(obj.getString(CommonUtilities.PARAM_PROFILE_IC_BACK_PICTURE).length() == 0 ||
				obj.getString(CommonUtilities.PARAM_PROFILE_IC_BACK_PICTURE) == "null") {
			return false;
		}
		
		if(obj.getString(CommonUtilities.PARAM_PROFILE_IC_FRONT_PICTURE).length() == 0 ||
				obj.getString(CommonUtilities.PARAM_PROFILE_IC_FRONT_PICTURE) == "null") {
			return false;
		}
		
		if(obj.getString(CommonUtilities.PARAM_PROFILE_IC_NUMBER).length() == 0 || 
				obj.getString(CommonUtilities.PARAM_PROFILE_IC_NUMBER) == "null") {
			return false;
		}
		
		if(obj.getString(CommonUtilities.PARAM_PROFILE_IC_TYPE).length() == 0 || 
				obj.getString(CommonUtilities.PARAM_PROFILE_IC_TYPE) == "null") {
			return false;
		}
		
		if(obj.getString(CommonUtilities.PARAM_PROFILE_IC_TYPE_ID).length() == 0 ||
				obj.getString(CommonUtilities.PARAM_PROFILE_IC_TYPE_ID) == "null") {
			return false;
		}
		
		String studentSchoolname = obj.optString(CommonUtilities.PARAM_PROFILE_SCHOOL_NAME);
		if(studentSchoolname != "") {
			if(obj.getString(CommonUtilities.PARAM_PROFILE_SCHOOL_NAME).length() == 0 ||
					obj.getString(CommonUtilities.PARAM_PROFILE_SCHOOL_NAME) == "null") {
				return false;
			}			
		}
		
		// Matric card only for ic student
		String studentMatricCard = obj.optString(CommonUtilities.PARAM_PROFILE_MATRIC_CARD_NO);
		if(studentMatricCard != "") {
			if(obj.getString(CommonUtilities.PARAM_PROFILE_MATRIC_CARD_NO).length() == 0 || 
					obj.getString(CommonUtilities.PARAM_PROFILE_MATRIC_CARD_NO) == "null") {
				return false;
			}					
		}		

		// Student ic expired
		String studentIcExpired = obj.optString(CommonUtilities.PARAM_PROFILE_STUDENT_IC_EXPIRY_DATE);
		if(studentIcExpired != "") {
			if(obj.getString(CommonUtilities.PARAM_PROFILE_STUDENT_IC_EXPIRY_DATE).length() == 0 ||
					obj.getString(CommonUtilities.PARAM_PROFILE_STUDENT_IC_EXPIRY_DATE) == "null") {
				return false;
			}			
		}
		
		if(obj.getString(CommonUtilities.PARAM_PROFILE_SKILLS).length() == 0 || 
				obj.getString(CommonUtilities.PARAM_PROFILE_SKILLS) == "null") {
			return false;
		}
		
//		if(obj.getString(CommonUtilities.PARAM_PROFILE_BANK_NAME).length() == 0 ||
//				obj.getString(CommonUtilities.PARAM_PROFILE_BANK_NAME) == "null") {
//			return false;
//		}
		

//		if(obj.getString(CommonUtilities.PARAM_PROFILE_BANK_ACCOUNT_NO).length() == 0 
//				|| obj.getString(CommonUtilities.PARAM_PROFILE_BANK_ACCOUNT_NO) == "null") {
//			return false;
//		}
		

//		if(obj.getString(CommonUtilities.PARAM_PROFILE_BANK_BRANCH_NAME).length() == 0 ||
//				obj.getString(CommonUtilities.PARAM_PROFILE_BANK_BRANCH_NAME) == "null") {
//			return false;
//		}
		

		if(obj.getString(CommonUtilities.PARAM_PROFILE_EC_FIRST_NAME).length() == 0 || 
				obj.getString(CommonUtilities.PARAM_PROFILE_EC_FIRST_NAME) == "null") {
			return false;
		}		

		if(obj.getString(CommonUtilities.PARAM_PROFILE_EC_PHONE_NO).length() == 0 ||
				obj.getString(CommonUtilities.PARAM_PROFILE_EC_PHONE_NO) == "null") {
			return false;
		}
		

		if(obj.getString(CommonUtilities.PARAM_PROFILE_EC_RELATIONSHIP).length() == 0 || 
				obj.getString(CommonUtilities.PARAM_PROFILE_EC_RELATIONSHIP) == "null") {
			return false;
		}
		
		// Visume
		String visume = obj.optString(Api.VISUME);
		if(visume != "") {
			if(obj.getString(Api.VISUME).length() == 0 ||
					obj.getString(Api.VISUME) == "null") {
				return false;
			}			
		}
		
		return true;
		
	}
}
