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
		if(obj.getString("dob").length() == 0 || obj.getString("dob") == "null") {
			return false;
		}
		
		if(obj.getString("email").length() == 0 || obj.getString("email") == "null") {
			return false;
		}
		
		if(obj.getString("work_experience").length() == 0 || obj.getString("work_experience") == "null") {
			return false;
		}
		
		if(obj.getString("first_name").length() == 0 || obj.getString("first_name") == "null") {
			return false;
		}
		
		if(obj.getString("last_name").length() == 0 || obj.getString("last_name") == "null") {
			return false;
		}
		
		if(obj.getString("phone_no").length() == 0 || obj.getString("phone_no") == "null") {
			return false;
		}
		
		if(obj.getString("gender").length() == 0 || obj.getString("gender") == "null") {
			return false;
		}
		
		if(obj.getString("profile_picture").length() == 0 || obj.getString("profile_picture") == "null") {
			return false;
		}
		
		if(obj.getString("ic_back_picture").length() == 0 || obj.getString("ic_back_picture") == "null") {
			return false;
		}
		
		if(obj.getString("ic_front_picture").length() == 0 || obj.getString("ic_front_picture") == "null") {
			return false;
		}
		
		if(obj.getString("ic_no").length() == 0 || obj.getString("ic_no") == "null") {
			return false;
		}
		
		if(obj.getString("ic_type").length() == 0 || obj.getString("ic_type") == "null") {
			return false;
		}
		
		if(obj.getString("ic_type_id").length() == 0 || obj.getString("ic_type_id") == "null") {
			return false;
		}
		
		String studentSchoolname = obj.optString("school_name");
		if(studentSchoolname != "") {
			if(obj.getString("school_name").length() == 0 || obj.getString("school_name") == "null") {
				return false;
			}			
		}
		
		// Matric card only for ic student
		String studentMatricCard = obj.optString("matric_card_no");
		if(studentMatricCard != "") {
			if(obj.getString("matric_card_no").length() == 0 || obj.getString("matric_card_no") == "null") {
				return false;
			}					
		}		

		// Student ic expired
		String studentIcExpired = obj.optString("ic_expiry_date");
		if(studentIcExpired != "") {
			if(obj.getString("ic_expiry_date").length() == 0 || obj.getString("ic_expiry_date") == "null") {
				return false;
			}			
		}
		
		if(obj.getString("skills").length() == 0 || obj.getString("skills") == "null") {
			return false;
		}
		
		if(obj.getString("bank_name").length() == 0 || obj.getString("bank_name") == "null") {
			return false;
		}
		

		if(obj.getString("bank_account_no").length() == 0 || obj.getString("bank_account_no") == "null") {
			return false;
		}
		

		if(obj.getString("bank_branch_name").length() == 0 || obj.getString("bank_branch_name") == "null") {
			return false;
		}
		

		if(obj.getString("ec_first_name").length() == 0 || obj.getString("ec_first_name") == "null") {
			return false;
		}		

		if(obj.getString("ec_phone_no").length() == 0 || obj.getString("ec_phone_no") == "null") {
			return false;
		}
		

		if(obj.getString("ec_relationship").length() == 0 || obj.getString("ec_relationship") == "null") {
			return false;
		}
		
		
		return true;
		
	}
}
