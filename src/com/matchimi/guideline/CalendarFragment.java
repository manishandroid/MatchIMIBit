package com.matchimi.guideline;

import com.matchimi.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class CalendarFragment extends Fragment{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.guideline_calendar, container, false);
		return view;
	}
	
	public static CalendarFragment newInstance(){
		CalendarFragment fragment = new CalendarFragment();
		return fragment;
	}
}
