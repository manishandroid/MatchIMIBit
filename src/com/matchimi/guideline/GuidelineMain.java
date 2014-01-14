package com.matchimi.guideline;

import org.acra.jraf.android.util.activitylifecyclecallbackscompat.MainLifecycleDispatcher;

import com.matchimi.CommonUtilities;
import com.matchimi.HomeActivity;
import com.matchimi.R;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GuidelineMain extends FragmentActivity implements OnClickListener{

	private PageIndicator mIndicator;
    private ViewPager mPager;
    private SharedPreferences preferencesGuideline;
    private SharedPreferences.Editor editor;
    private Button btnPrev;
    private Button btnNext;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guideline_main);
		
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
		
        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        		
        btnPrev = (Button) findViewById(R.id.guide_btn_prev);
        btnNext = (Button) findViewById(R.id.guide_btn_next);

		btnPrev.setVisibility(View.GONE);
		btnPrev.setText(R.string.guide_txt_btn_prev);
		btnNext.setText(R.string.guide_txt_btn_next);
		
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            	switch(position){
            	case 0:
            		btnPrev.setVisibility(View.GONE);
            		btnNext.setText(R.string.guide_txt_btn_next);
            		break;
            	case 1:
            	case 2:
            		btnPrev.setVisibility(View.VISIBLE);
            		btnPrev.setText(R.string.guide_txt_btn_prev);
            		btnNext.setText(R.string.guide_txt_btn_next);
            		break;
            	case 3:
            		btnPrev.setVisibility(View.VISIBLE);
            		btnPrev.setText(R.string.guide_txt_btn_prev);
            		btnNext.setText(R.string.guide_txt_btn_start);
            		break;
            	default:
            		btnPrev.setText(R.string.guide_txt_btn_prev);
            		btnNext.setText(R.string.guide_txt_btn_next);
            		break;
            	}
            	
//            	LayoutInflater inflater = getLayoutInflater();
//            	View layout = inflater.inflate(R.layout.guideline_toast, 
//            			(ViewGroup)findViewById(R.id.guideline_toast));
//            	((TextView)layout.findViewById(R.id.txt_toast)).setText("Page "+(position+1));
            	
//            	Toast toast = new Toast(getBaseContext());
//            	toast.setView(layout);
//            	toast.setDuration(Toast.LENGTH_SHORT);
//            	toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
//            	toast.show();
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        
        preferencesGuideline = getSharedPreferences(
        		CommonUtilities.PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferencesGuideline.edit();
        editor.putBoolean(CommonUtilities.IS_FIRSTTIME, true);
        editor.commit();
        
        btnNext.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
	}
    
	private class MyPagerAdapter extends FragmentPagerAdapter{
		public MyPagerAdapter(FragmentManager fm){
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			switch(arg0){
				case 0:
					return CalendarFragment.newInstance();
				case 1:
					return AddFragment.newInstance();
				case 2:
					return JobFragment.newInstance();
				case 3:
					return ProfileFragment.newInstance();
				default:
					return CalendarFragment.newInstance();
			}
		}

		@Override
		public int getCount() {
			return 4;
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.guide_btn_prev:
			int current1 = mPager.getCurrentItem();
			mPager.setCurrentItem(current1-1, true);
			break;
		case R.id.guide_btn_next:
			if (btnNext.getText().toString().equals("next")) {
				int current2 = mPager.getCurrentItem();
				mPager.setCurrentItem(current2+1, true);				
			} else if (btnNext.getText().toString().equals("start")) {
				startActivity(new Intent(this, HomeActivity.class));
				finish();
			}
			break;
		}
	}

}