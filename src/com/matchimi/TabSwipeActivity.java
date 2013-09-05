package com.matchimi;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.matchimi.options.AvailabilityActivity;
import com.matchimi.options.HistoryDetail;
import com.matchimi.options.JobsFragment;
import com.matchimi.registration.RegistrationActivity;

import static com.matchimi.CommonUtilities.*;
import com.matchimi.utils.ApplicationUtils;

public abstract class TabSwipeActivity extends SherlockFragmentActivity {

	private ViewPager mViewPager;
	private TabsAdapter adapter;

	private SharedPreferences authenticationPref;
	private SharedPreferences.Editor prefEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		/* load setting from pref */
		authenticationPref = getSharedPreferences(CommonUtilities.APP_SETTING,
				MODE_PRIVATE);
		prefEditor = authenticationPref.edit();

		/* load setting from pref */
		authenticationPref = getSharedPreferences(CommonUtilities.APP_SETTING,
				Context.MODE_PRIVATE);
		if (authenticationPref.getInt(CommonUtilities.SETTING_THEME,
				CommonUtilities.THEME_LIGHT) == CommonUtilities.THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}

		/*
		 * Create the ViewPager and our custom adapter
		 */
		mViewPager = new ViewPager(this);
		adapter = new TabsAdapter(this, mViewPager);
		mViewPager.setAdapter(adapter);
		mViewPager.setOnPageChangeListener(adapter);

		/*
		 * We need to provide an ID for the ViewPager, otherwise we will get an
		 * exception like:
		 * 
		 * java.lang.IllegalArgumentException: No view found for id 0xffffffff
		 * for fragment TestFragment{40de5b90 #0 id=0xffffffff
		 * android:switcher:-1:0} at
		 * android.support.v4.app.FragmentManagerImpl.moveToState
		 * (FragmentManager.java:864)
		 * 
		 * The ID 0x7F04FFF0 is large enough to probably never be used for
		 * anything else
		 */
		mViewPager.setId(0x7F04FFF0);

		super.onCreate(savedInstanceState);

		/*
		 * Set the ViewPager as the content view
		 */
		setContentView(mViewPager);
	}

	/**
	 * Add a tab with a backing Fragment to the action bar
	 * 
	 * @param titleRes
	 *            A string resource pointing to the title for the tab
	 * @param fragmentClass
	 *            The class of the Fragment to instantiate for this tab
	 * @param args
	 *            An optional Bundle to pass along to the Fragment (may be null)
	 */
	protected void addTab(int titleRes,
			Class<? extends Fragment> fragmentClass, Bundle args) {
		adapter.addTab(getString(titleRes), fragmentClass, args);
	}

	/**
	 * Add a tab with a backing Fragment to the action bar
	 * 
	 * @param titleRes
	 *            A string resource pointing to the title for the tab
	 * @param fragmentClass
	 *            The class of the Fragment to instantiate for this tab
	 * @param args
	 *            An optional Bundle to pass along to the Fragment (may be null)
	 */
	protected void moveTab(int position) {
		mViewPager.setCurrentItem(position);
	}

	/**
	 * Add a tab with a backing Fragment to the action bar
	 * 
	 * @param titleRes
	 *            A string to be used as the title for the tab
	 * @param fragmentClass
	 *            The class of the Fragment to instantiate for this tab
	 * @param args
	 *            An optional Bundle to pass along to the Fragment (may be null)
	 */
	protected void addTab(CharSequence title,
			Class<? extends Fragment> fragmentClass, Bundle args) {
		adapter.addTab(title, fragmentClass, args);
	}

	private static class TabsAdapter extends FragmentPagerAdapter implements
			TabListener, ViewPager.OnPageChangeListener {

		private final SherlockFragmentActivity mActivity;
		private final ActionBar mActionBar;
		private final ViewPager mPager;

		/**
		 * @param fm
		 * @param fragments
		 */
		public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			this.mActivity = activity;
			this.mActionBar = activity.getSupportActionBar();
			this.mPager = pager;

			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		}

		private static class TabInfo {
			public final Class<? extends Fragment> fragmentClass;
			public final Bundle args;

			public TabInfo(Class<? extends Fragment> fragmentClass, Bundle args) {
				this.fragmentClass = fragmentClass;
				this.args = args;
			}
		}

		private List<TabInfo> mTabs = new ArrayList<TabInfo>();

		public void addTab(CharSequence title,
				Class<? extends Fragment> fragmentClass, Bundle args) {
			final TabInfo tabInfo = new TabInfo(fragmentClass, args);

			Tab tab = mActionBar.newTab();
			tab.setText(title);
			tab.setTabListener(this);
			tab.setTag(tabInfo);

			mTabs.add(tabInfo);

			mActionBar.addTab(tab);
			notifyDataSetChanged();
		}

		@Override
		public Fragment getItem(int position) {
			final TabInfo tabInfo = mTabs.get(position);
			return (Fragment) Fragment.instantiate(mActivity,
					tabInfo.fragmentClass.getName(), tabInfo.args);
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		public void onPageScrollStateChanged(int arg0) {
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		public void onPageSelected(int position) {
			/*
			 * Select tab when user swiped
			 */
			Log.e("Page Selected", "" + position);
			mActionBar.setSelectedNavigationItem(position);
		}

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			/*
			 * Slide to selected fragment when user selected tab
			 */
			TabInfo tabInfo = (TabInfo) tab.getTag();
			for (int i = 0; i < mTabs.size(); i++) {
				if (mTabs.get(i) == tabInfo) {
					Log.e("Page Selected", "" + i);
					mPager.setCurrentItem(i);
				}
			}
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);

		// Set file with share history to the provider and set the share intent.
		MenuItem actionItem = menu.findItem(R.id.menu_share);
		ShareActionProvider actionProvider = (ShareActionProvider) actionItem
				.getActionProvider();
		actionProvider
				.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		actionProvider.setShareIntent(createAllShareIntent());

		MenuItem actionNav = menu.findItem(R.id.menu_more);
		if (authenticationPref.getInt(CommonUtilities.SETTING_THEME,
				CommonUtilities.THEME_LIGHT) == CommonUtilities.THEME_LIGHT) {
			actionNav
					.setIcon(R.drawable.abs__ic_menu_moreoverflow_normal_holo_light);
		} else {
			actionNav
					.setIcon(R.drawable.abs__ic_menu_moreoverflow_normal_holo_dark);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == android.R.id.home) {
			finish();
		} else {
			Intent i;
			switch (item.getItemId()) {
			case R.id.menu_more:
				prepareNav(item.getSubMenu());
				break;
			case R.id.menu_help:
				break;
			case R.id.menu_history:
				i = new Intent(getApplicationContext(), HistoryDetail.class);
				i.putExtra("pt_id", "37");
				startActivity(i);
				break;
			case R.id.menu_availability:
				i = new Intent(getApplicationContext(),
						AvailabilityActivity.class);
				i.putExtra("pt_id", "37");
				startActivityForResult(i, JobsFragment.RC_JOB_DETAIL);
				break;
			case R.id.menu_logout:
				i = new Intent(getApplicationContext(),
						RegistrationActivity.class);
				i.putExtra(LOGOUT, "true");
				startActivityForResult(i, JobsFragment.RC_JOB_DETAIL);
				break;				
			case R.id.menu_setting:
				showSettingMenu();
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void showSettingMenu() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select application theme");
		builder.setItems(R.array.theme_value,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						prefEditor.putInt(CommonUtilities.SETTING_THEME, arg1);
						prefEditor.commit();
						// restart apps
						ApplicationUtils.restartApp(getApplicationContext());
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void prepareNav(SubMenu subMenu) {
		// TODO Auto-generated method stub

	}

	/**
	 * Creates a sharing {@link Intent}.
	 * 
	 * @return The sharing intent.
	 */
	private Intent createAllShareIntent() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Matchimi");
		shareIntent
				.putExtra(
						Intent.EXTRA_TEXT,
						"Check �Matchimi� out! An app that matches you to the part-time job you want! Download at www.Matchimi.com");

		return shareIntent;
	}
}