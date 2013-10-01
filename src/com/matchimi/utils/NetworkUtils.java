package com.matchimi.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import com.matchimi.CommonUtilities;
import com.matchimi.R;

public final class NetworkUtils {
	public static final void connectionHandler(Context context, String jsonStr, String error) {
		try {
			JSONObject items = new JSONObject(jsonStr);
			String status = items.optString("status", "");
			
			if(status.contains(CommonUtilities.NOINTERNET)) {
				Toast.makeText(context,
						context.getApplicationContext().getString(R.string.no_internet),
						Toast.LENGTH_SHORT).show();
				Log.e(CommonUtilities.TAG, "No internet connection");
				
			} else if(status.contains(CommonUtilities.FILECORRUPT)) { 
				Toast.makeText(context,
						context.getApplicationContext().getString(R.string.file_corrupt),
						Toast.LENGTH_SHORT).show();
				Log.e(CommonUtilities.TAG, "File has been corrupted");
				
			} else {
				Toast.makeText(context,
						error,
						Toast.LENGTH_SHORT).show();
				Log.e(CommonUtilities.TAG, "Invalid response value" + jsonStr);
			}			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d(CommonUtilities.TAG, "Network error message " + e.toString());
		}
	}
	
}
