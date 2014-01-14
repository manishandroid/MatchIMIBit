	package com.matchimi;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gcm.GCMRegistrar;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;


public class NotificationActivity extends SherlockFragmentActivity {

    TextView displayMessage;

    private Context context;
    private String pt_id;
    
    AsyncTask<Void, Void, Void> registerTask;
    Button registerNotifications;
    Button pingButton;
    private DatabaseStorage db;
    private String registrationID;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(
				CommonUtilities.PREFS_NAME, Context.MODE_PRIVATE);
		if (settings.getInt(CommonUtilities.SETTING_THEME,
				CommonUtilities.THEME_LIGHT) == CommonUtilities.THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}
		setContentView(R.layout.notifications);

		context = this;
		pt_id = settings.getString(CommonUtilities.USER_PTID, null);
		
        // Checking server configuration
        checkNotNull(CommonUtilities.GCM_SERVER_URL, "SERVER_URL");
        checkNotNull(CommonUtilities.SENDER_ID, "SENDER_ID");
        
        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(this);

        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(this);
        
        db = new DatabaseStorage();
        registerReceiver(mHandleStatusReceiver,
                new IntentFilter(CommonUtilities.DISPLAY_STATUS_ACTION));
        registerReceiver(mHandleMessageReceiver,
                new IntentFilter(CommonUtilities.DISPLAY_MESSAGE_ACTION));
        
        registerNotifications = (Button)findViewById(R.id.register_notifications);
        registerNotifications.setOnClickListener(registerListener);
        
        pingButton = (Button)findViewById(R.id.register_notifications_ping);
        pingButton.setOnClickListener(pingListener);

        registrationID = GCMRegistrar.getRegistrationId(context);
        Log.d(CommonUtilities.TAG, "Registration ID " + registrationID);
    }
    
    public OnClickListener registerListener = new OnClickListener() {
		
    	@Override
		public void onClick(View v) {
    		Log.d(CommonUtilities.TAG, "Registration ID " + registrationID);
    		
    		if (registrationID.equals("")) {
        		Log.d(CommonUtilities.TAG, "Auto Registration " + registrationID);

                // Automatically registers application on startup.
                GCMRegistrar.register(context, CommonUtilities.SENDER_ID);

            } else {
                // Device is already registered on GCM, check server.
                if (GCMRegistrar.isRegisteredOnServer(context)) {
                    popUp(getString(R.string.already_registered));
                    
                } else {
                	Log.d(CommonUtilities.TAG, "Device not registered yet");
                	
                    // Try to register again, but not in the UI thread.
                    // It's also necessary to cancel the thread onDestroy(),
                    // hence the use of AsyncTask instead of a raw thread.
                    registerTask = new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... params) {
                            boolean registered =
                                    ServerUtilities.registerPartimer(context, registrationID, pt_id);
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
            }
		}
	};
	
	public OnClickListener pingListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				registerTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {       
                    	Log.d(CommonUtilities.TAG, "Send message " + pt_id);
                    	
                        boolean message =
                                ServerUtilities.messaging(context, pt_id, "Ping");

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        registerTask = null;
                    }

                };
                registerTask.execute(null, null, null);                
			}
	};
	

    @Override
    protected void onDestroy() {
        if (registerTask != null) {
            registerTask.cancel(true);
        }
        unregisterReceiver(mHandleStatusReceiver);
        GCMRegistrar.onDestroy(this);
        super.onDestroy();
    }

    private void checkNotNull(Object reference, String name) {
        if (reference == null) {
            throw new NullPointerException(
                    getString(R.string.error_config, name));
        }
    }

    private final BroadcastReceiver mHandleMessageReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(CommonUtilities.EXTRA_MESSAGE);
        }
    };
    
    private final BroadcastReceiver mHandleStatusReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(CommonUtilities.EXTRA_MESSAGE);
            popUp(newMessage);
        }
    };
    
    public void popUp(String message) {
    	// Skips registration.
        int duration = Toast.LENGTH_SHORT;
        
        Toast toast = Toast.makeText(this, message, duration);
        toast.show();
    }

}
