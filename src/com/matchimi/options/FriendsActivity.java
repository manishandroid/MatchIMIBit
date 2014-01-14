package com.matchimi.options;

import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SETTING_THEME;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.THEME_LIGHT;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;

public class FriendsActivity extends SherlockActivity {

	private JSONParser jsonParser = null;
	private String jsonStr = null;
	
	private Context context;
	private ProgressDialog progressDialog;
	
	private FriendsAdapter adapter;
	private List<FriendsModel> listFriends;
	
	private String pt_id = null;
	private boolean allFriends = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences settings = getSharedPreferences(
				CommonUtilities.PREFS_NAME, Context.MODE_PRIVATE);
		if (settings.getInt(CommonUtilities.SETTING_THEME,
				CommonUtilities.THEME_LIGHT) == CommonUtilities.THEME_LIGHT) {
			setTheme(ApplicationUtils.getTheme(true));
		} else {
			setTheme(ApplicationUtils.getTheme(false));
		}
		setContentView(R.layout.friends_layout);

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		
		context = this;
		
		pt_id = settings.getString(CommonUtilities.USER_PTID, null);

		adapter = new FriendsAdapter(context, null);
		ListView listView = (ListView)findViewById(R.id.listview);
		listView.setAdapter(adapter);

		TextView buttonAll = (TextView)findViewById(R.id.buttonAll);
		buttonAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!allFriends) {
					allFriends = true;
					showList();
				}
			}
		});

		TextView buttonFriends = (TextView)findViewById(R.id.buttonFriends);
		buttonFriends.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (allFriends) {
					allFriends = false;
					showList();
				}
			}
		});
		
		ImageView buttonClose = (ImageView)findViewById(R.id.buttonClose);
		buttonClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
//				AutoCompleteTextView autoComplete = (AutoCompleteTextView)findViewById(R.id.autoComplete);
//				autoComplete.setText("");
				EditText editText = (EditText)findViewById(R.id.editText);
				editText.setText("");
				showList();
			}
		});
		
		registerReceiver(myReceiver, new IntentFilter("friends.receiver"));
		
		showList();
		loadFriends();
	}

	
	protected void showList() {
		ProgressBar progress = (ProgressBar)findViewById(R.id.progress);
		progress.setVisibility(View.GONE);
		ListView listView = (ListView)findViewById(R.id.listview);
		listView.setVisibility(View.VISIBLE);
		
		TextView buttonAll = (TextView)findViewById(R.id.buttonAll);
		TextView buttonFriends = (TextView)findViewById(R.id.buttonFriends);
		if (allFriends) {
			buttonAll.setBackgroundResource(R.drawable.bg_button_tab);
			buttonAll.setTextColor(Color.WHITE);
			buttonFriends.setBackgroundResource(R.drawable.bg_button_tab_def);
			buttonFriends.setTextColor(Color.BLACK);
			
			adapter.updateView(listFriends);
			setupAutoComplete(listFriends);
		} else {
			List<FriendsModel> friends = new ArrayList<FriendsModel>();
			if (listFriends != null && listFriends.size() > 0) {
				for (FriendsModel f : listFriends) {
					if (f.isFriend()) {
						friends.add(f);
					}
				}
			}
			
			buttonAll.setBackgroundResource(R.drawable.bg_button_tab_def);
			buttonAll.setTextColor(Color.BLACK);
			buttonFriends.setBackgroundResource(R.drawable.bg_button_tab);
			buttonFriends.setTextColor(Color.WHITE);
			
			adapter.updateView(friends);
			setupAutoComplete(friends);
		}
	}

	private void setupAutoComplete(final List<FriendsModel> friends) {
		List<String> fl = new ArrayList<String>();
		if (friends != null && friends.size() > 0) {
			for (FriendsModel fm : friends ) {
				fl.add(fm.getName());
			}
		}
		EditText editText = (EditText)findViewById(R.id.editText);
		editText.setText("");
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				adapter.getFilter().filter(arg0);
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		
//		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, fl);
//		AutoCompleteTextView autoComplete = (AutoCompleteTextView)findViewById(R.id.autoComplete);
//        autoComplete.setAdapter(arrayAdapter);
//        autoComplete.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//					long arg3) {
//				List<FriendsModel> newM = new ArrayList<FriendsModel>();
//				newM.add(friends.get(arg2));
//				adapter.updateView(newM);
//			}
//		});
	}


	private void loadFriends() {
		// TODO Auto-generated method stub
		ListView listView = (ListView)findViewById(R.id.listview);
		listView.setVisibility(View.GONE);
		ProgressBar progress = (ProgressBar)findViewById(R.id.progress);
		progress.setVisibility(View.VISIBLE);
		
		listFriends = new ArrayList<FriendsModel>();
		
		Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		FriendsModel model = new FriendsModel();
		model.setFriend(true);
		model.setId("1");
		model.setName("nama 1");
		model.setImg(icon);
		listFriends.add(model);
		
		model = new FriendsModel();
		model.setFriend(false);
		model.setId("2");
		model.setName("nama 2");
		model.setImg(icon);
		listFriends.add(model);
		
		model = new FriendsModel();
		model.setFriend(false);
		model.setId("3");
		model.setName("nama 3");
		model.setImg(icon);
		listFriends.add(model);
		
		model = new FriendsModel();
		model.setFriend(false);
		model.setId("4");
		model.setName("nama 4");
		model.setImg(icon);
		listFriends.add(model);
		
		model = new FriendsModel();
		model.setFriend(true);
		model.setId("5");
		model.setName("nama 5");
		model.setImg(icon);
		listFriends.add(model);
		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					try {
						JSONArray items = new JSONArray(jsonStr);
						if (items != null && items.length() > 0) {
							for (int i = 0; i < items.length(); i++) {
								/* get all json items, and put it on list */
								try {
									JSONObject objs = items.getJSONObject(i);
									objs = objs.getJSONObject("friend");
									if (objs != null) {
										FriendsModel model = new FriendsModel();
										byte[] imageAsBytes = Base64.decode(jsonParser.getString(
												objs, "image").getBytes(), 0);
										model.setImg(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
										model.setId(jsonParser.getString(objs, "id"));
										model.setName(jsonParser.getString(objs, "name"));
										model.setFriend(Boolean.parseBoolean(jsonParser.getString(objs, "friend")));
										listFriends.add(model);
									}
								} catch (Exception e) {
								}
							}
						}
					} catch (Exception e) {
						Log.e(CommonUtilities.TAG, ">>> " + e.getMessage());
					}
				}
				showList();
			}
		};

		new Thread() {
			public void run() {
				String url = CommonUtilities.SERVERURL + CommonUtilities.API_GET_ALL_FRIEND;

				jsonParser = new JSONParser();
				jsonStr = jsonParser.getHttpResultUrlGet(url);

				mHandlerFeed.post(mUpdateResultsFeed);
			}
		}.start();
	}

	BroadcastReceiver myReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			onButtonClicked(arg1.getExtras().getString("id"), arg1.getExtras().getBoolean("add"), arg1.getExtras().getInt("position"));
		}
	};

	protected void onButtonClicked(final String id, final boolean isAdd, final int position) {
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (jsonStr != null) {
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						adapter.setFriendStatus(position, isAdd);
					} else {
						Toast.makeText(context, getString(R.string.failed_try_again),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(context, getString(R.string.server_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		String msg = getString(R.string.request_friend);
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_REQUEST_FRIENDSHIP;
		if (!isAdd) {
			msg = getString(R.string.request_unfriends);
		}
		progressDialog = ProgressDialog.show(context, getString(R.string.app_name), 
				msg, true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				try {
					String[] params = { "ptid", "friend_id", "request" };
					String[] values = { pt_id, id, isAdd ? "friend" : "unfriend" };
					
					jsonStr = jsonParser.getHttpResultUrlPut(url, params, values);
					Log.d(TAG, "Result >>> " + jsonStr);
					
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myReceiver);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.ab_history, menu);

		MenuItem reload = menu.findItem(R.id.menu_reload);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 
				Context.MODE_PRIVATE);
		if (settings.getInt(SETTING_THEME, THEME_LIGHT) == THEME_LIGHT) {
			reload.setIcon(R.drawable.navigation_refresh);
		} else {
			reload.setIcon(R.drawable.navigation_refresh_dark);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.menu_reload:
			loadFriends();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
