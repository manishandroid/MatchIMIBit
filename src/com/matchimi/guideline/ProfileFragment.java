package com.matchimi.guideline;

import com.matchimi.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ProfileFragment extends Fragment{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.guideline_profile, container, false);
		return view;
	}
	
	public static ProfileFragment newInstance(){
		ProfileFragment fragment = new ProfileFragment();
		return fragment;
	}
}
