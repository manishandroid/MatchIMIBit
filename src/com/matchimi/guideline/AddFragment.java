package com.matchimi.guideline;

import com.matchimi.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AddFragment extends Fragment{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.guideline_add, container, false);
		return view;
	}
	
	public static AddFragment newInstance(){
		AddFragment fragment = new AddFragment();
		return fragment;
	}
}
