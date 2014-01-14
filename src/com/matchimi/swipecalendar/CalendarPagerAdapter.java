package com.matchimi.swipecalendar;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * PagerAdapter contains Array CalendarSingleFragments This pageradapter will
 * used for InifinitePager
 * 
 * @author yodi
 * 
 */
public class CalendarPagerAdapter extends FragmentStatePagerAdapter {
	private CalendarSingleFragment calendarSingleFragment;

	public CalendarPagerAdapter(FragmentManager fm) {
		super(fm);
		notifyDataSetChanged();
	}

	private ArrayList<CalendarSingleFragment> fragments;

	// Lazily create the fragments
	public ArrayList<CalendarSingleFragment> getFragments() {
		if (fragments == null) {
			fragments = new ArrayList<CalendarSingleFragment>();
			for (int i = 0; i < getCount(); i++) {
				fragments.add(new CalendarSingleFragment());
			}
		}
		return fragments;
	}

	public void setFragments(ArrayList<CalendarSingleFragment> fragments) {
		this.fragments = fragments;
		notifyDataSetChanged();
	}

	@Override
	public Fragment getItem(int position) {
		CalendarSingleFragment fragment = getFragments().get(position);

		return fragment;
	}
	
	public int getMonthRow(int position) {
		return calendarSingleFragment.getMonthRow();
	}

	@Override
	public int getCount() {
		// We need 4 gridviews for previous month, current month and next month,
		// and 1 extra fragment for fragment recycle
		return CalendarFragment.NUMBER_OF_PAGES;
	}

}
