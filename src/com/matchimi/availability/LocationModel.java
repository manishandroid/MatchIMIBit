package com.matchimi.availability;

import com.matchimi.CommonUtilities;

import android.util.Log;

public class LocationModel {

	private String name;
	private Integer id;
	private boolean selected;

	public LocationModel(String name, Integer id) {
		this.name = name;
		this.id = id;
		selected = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
//		Log.d(CommonUtilities.TAG, "This is seleted " + selected);
		this.selected = selected;
	}

}