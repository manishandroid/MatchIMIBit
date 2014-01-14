package com.matchimi.utils;

import android.util.Log;

import com.matchimi.CommonUtilities;

public class UpdateContentEvent {
	public static String tabName;
	
	public UpdateContentEvent(String tabName) {
		Log.d(CommonUtilities.TAG, "Post tab name " + tabName);
		this.tabName = tabName;
	}
	
	public static String getTab() {
		return tabName;
	}
	
	
}
