package com.matchimi.availability;

import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SETTING_THEME;
import static com.matchimi.CommonUtilities.THEME_LIGHT;
import static com.matchimi.CommonUtilities.USER_PTID;

import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;

public class BulkAvailabilityActivity extends SherlockFragmentActivity {
	private Context context;
	private TextView repeatWeeksView;
	private TextView submitView;
	private TextView cancelView;
	private ProgressBar progressBar;
	
	private List<CharSequence> listRepeatWeeks;
	
	private String pt_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		if (settings.getInt(SETTING_THEME, THEME_LIGHT) == THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}
		
		pt_id = settings.getString(USER_PTID, null);
		setContentView(R.layout.availability_add_bulk);
		context = this;		
		
		progressBar = (ProgressBar) findViewById(R.id.progress);
		
		repeatWeeksView = (TextView) findViewById(R.id.repeatField);
		repeatWeeksView.setOnClickListener(weeksListener);
		
		CharSequence[] repeatArray = getResources().getStringArray(R.array.repeat_week_array);
		listRepeatWeeks = Arrays.asList(repeatArray);
		
		submitView = (TextView) findViewById(R.id.buttonSubmitBulkAvailability);
		submitView.setOnClickListener(submitListener);
		
		cancelView = (TextView) findViewById(R.id.buttonCancelBulkAvailability);
		cancelView.setOnClickListener(cancelListener);		
	}
	
	private OnClickListener weeksListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
		    builder.setTitle(R.string.availability_repeat)
		           .setItems(R.array.repeat_week_array, new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int which) {
		            	   repeatWeeksView.setText(listRepeatWeeks.get(which));
		           }
		    });
		    
		    Dialog dialog = builder.create();
			dialog.show();
		}
	};
	
	/**
	 * Submiting availability data
	 */
	private OnClickListener submitListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
		}
	};
	
	/**
	 * Cancel availability data and back to home
	 */
	private OnClickListener cancelListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent cancelIntent = new Intent(context, HomeAvailabilityActivity.class);
			startActivity(cancelIntent);
			finish();
		}
	};
	
	
}
