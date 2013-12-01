package com.matchimi.availability;

import com.matchimi.CommonUtilities;

import android.util.Log;

public class LocationModel {

	private String name;
	private boolean selected;

	public LocationModel(String name) {
		this.name = name;
		selected = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
//		Log.d(CommonUtilities.TAG, "This is seleted " + selected);
		this.selected = selected;
	}

}