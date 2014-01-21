package com.matchimi;


import static com.matchimi.CommonUtilities.COMMON_FACEBOOK_ID;
import static com.matchimi.CommonUtilities.COMMON_PASSWORD;
import static com.matchimi.CommonUtilities.PARAM_PROFILE_EMAIL;
import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.USER_EMAIL;
import static com.matchimi.CommonUtilities.USER_PTID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.matchimi.CommonUtilities;
import com.matchimi.HomeActivity;
import com.matchimi.R;
import com.matchimi.guideline.JobFragment;
import com.matchimi.options.JobDetails;
import com.matchimi.registration.ProfileRegistrationActivity;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;


/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
	private String message;
	private String avail_id;
	private String data;
	private Integer typeData;
	
    @SuppressWarnings("hiding")
    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(CommonUtilities.SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "on Registered " + registrationId);
        CommonUtilities.displayMessage(context, getString(R.string.gcm_registered));
//        ServerUtilities.register(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        CommonUtilities.displayMessage(context, getString(R.string.gcm_unregistered));
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
    	String receivedMessage = intent.getStringExtra("message");
        Log.i(TAG, "Received message" + receivedMessage);
//        receivedMessage = "{\"message\":\"You have received a new job offer\",\"avail_id\":\"2539\",\"sub_slot_id\":1561,\"type\":1,\"data\":{\"sub_slots\":{\"address\":\"68 Orchard Road, #B2-07\",\"avail_id\":2539,\"branch_id\":26,\"branch_name\":\"Bishan\",\"company_id\":1,\"company_name\":\"Pizza Hut\",\"description\":\"\",\"end_date_time\":\"2014-02-16T14:00:00+08:00\",\"expired_at\":\"2014-01-21T21:59:59+08:00\",\"friends\":[],\"grade\":\"0\",\"job_function_id\":1,\"job_function_name\":\"Banquet Staff\",\"latitude\":\"1.301016\",\"location\":\"1.301016,103.845411\",\"longitude\":\"103.845411\",\"main_slot_id\":182,\"mandatory_requirements\":[\"Must wear black pants and black shoes.\",\"Interview Required.\"],\"offered_salary\":7.5,\"optional_requirements\":[],\"postal_code\":\"238839\",\"pt_id\":136,\"start_date_time\":\"2014-02-16T09:00:00+08:00\",\"status\":\"MA\",\"sub_slot_id\":1561,\"ts_end_date_time\":1392530400,\"ts_expired_at\":1390312799,\"ts_start_date_time\":1392512400}}}";
        parseMessage(context, receivedMessage);
        
        CommonUtilities.displayMessage(context, message);

        if(typeData == 1) {
        	fowardJobDetails(context, message, avail_id, data);
        }
        
    }
    
    private void parseMessage(Context context, String jsonStr) {
    	JSONParser jsonParser = new JSONParser();
    	
    	try {
    		JSONObject obj = new JSONObject(jsonStr);
			
    		message = obj.getString("message");
    		data = obj.getString("data");
    		avail_id = obj.getString("avail_id");
    		typeData = Integer.parseInt(obj.getString("type"));
    		
		} catch (JSONException e1) {
			NetworkUtils.connectionHandler(context, jsonStr,
					e1.getMessage());
		}
    	
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        CommonUtilities.displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        CommonUtilities.displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        CommonUtilities.displayMessage(context, getString(R.string.gcm_recoverable_error,
                errorId));
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_stat_gcm;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, NotificationActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }
    
    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void fowardJobDetails(Context context, String message, String avail_id,
    		String data) {
    	Log.d(CommonUtilities.TAG, "Received notification with :" + avail_id + ", " + ", " + message);
//    	
        int icon = R.drawable.ic_stat_gcm;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        
        String title = context.getString(R.string.app_name);
//        String data = "{\"address\":\"5 Tampines Street 32 #01-07/16 Tampines Mart\",\"avail_id\":2315,\"branch_id\":10,\"branch_name\":\"Tampines Mart\",\"company_id\":2,\"company_name\":\"ABC F&B\",\"description\":\"\",\"end_date_time\":\"2014-03-11T04:00:00+08:00\",\"expired_at\":\"2014-01-21T21:59:59+08:00\",\"friends\":[],\"grade\":\"0\",\"job_function_id\":1,\"job_function_name\":\"Banquet Staff\",\"latitude\":\"1.354349\",\"location\":\"1.354349,103.9602589\",\"longitude\":\"103.9602589\",\"main_slot_id\":205,\"mandatory_requirements\":[\"Must wear black shoes\",\"Must wear back pants\"],\"offered_salary\":7.5,\"optional_requirements\":[],\"postal_code\":\"529284\",\"start_date_time\":\"2014-03-11T23:00:00+08:00\",\"status\":\"MA\",\"sub_slot_id\":1653,\"ts_end_date_time\":1394481600,\"ts_expired_at\":1390312799,\"ts_start_date_time\":1394550000}";
        		
        // When user click notification message, redirect them to Job Page
        Intent notificationIntent = new Intent(context, JobDetails.class);
        notificationIntent.putExtra("avail_id", avail_id);
        notificationIntent.putExtra("is_notification", true);        
        notificationIntent.putExtra("data", data);
        
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }


}
