package com.matchimi.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ApplicationUtils {

	public static void restartApp(Context context) {
		Intent i = context.getPackageManager().getLaunchIntentForPackage(
				context.getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(i);
	}

	public static int getThemeDialog(boolean light) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return light ? android.R.style.Theme_DeviceDefault_Light_Dialog
					: android.R.style.Theme_DeviceDefault_Dialog;
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return light ? android.R.style.Theme_Holo_Light_Dialog
					: android.R.style.Theme_Holo_Dialog;
		} else {
			return android.R.style.Theme_Dialog;
		}
	}

	public static int getTheme(boolean light) {
		return light ? com.actionbarsherlock.R.style.Theme_Sherlock_Light
				: com.actionbarsherlock.R.style.Theme_Sherlock;
	}
}
