package com.matchimi.registration;

import android.content.SharedPreferences;

public final class SharedUtils {
	public static final void updateSettings(SharedPreferences settings,
			String key, String value) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
	}
}
