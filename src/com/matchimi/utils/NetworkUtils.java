package com.matchimi.utils;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.Toast;

import com.matchimi.CommonUtilities;
import com.matchimi.R;

public final class NetworkUtils {
	public static final void connectionHandler(Context context, String jsonStr) {
		try {
			JSONObject items = new JSONObject(jsonStr);
			String status = items.optString("status", "");
			
			if(status.contains(CommonUtilities.NOINTERNET)) {
				Toast.makeText(context,
						context.getApplicationContext().getString(R.string.no_internet),
						Toast.LENGTH_SHORT).show();
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
