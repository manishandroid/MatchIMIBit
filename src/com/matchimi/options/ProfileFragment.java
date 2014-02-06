package com.matchimi.options;

import static com.matchimi.CommonUtilities.API_CREATE_AND_PART_TIMER_REGISTRATION;
import static com.matchimi.CommonUtilities.LOGIN;
import static com.matchimi.CommonUtilities.PARAM_PT_ID;
import static com.matchimi.CommonUtilities.PREFS_NAME;
import static com.matchimi.CommonUtilities.SERVERURL;
import static com.matchimi.CommonUtilities.TAG;
import static com.matchimi.CommonUtilities.USER_FULLNAME;
import static com.matchimi.CommonUtilities.USER_NRIC_NUMBER;
import static com.matchimi.CommonUtilities.USER_NRIC_TYPE;
import static com.matchimi.CommonUtilities.USER_NRIC_TYPE_ID;
import static com.matchimi.CommonUtilities.USER_PROFILE_PICTURE;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import khandroid.ext.apache.http.HttpEntity;
import khandroid.ext.apache.http.HttpResponse;
import khandroid.ext.apache.http.client.HttpClient;
import khandroid.ext.apache.http.client.methods.HttpGet;
import khandroid.ext.apache.http.client.methods.HttpPost;
import khandroid.ext.apache.http.entity.mime.HttpMultipartMode;
import khandroid.ext.apache.http.entity.mime.MultipartEntity;
import khandroid.ext.apache.http.entity.mime.content.ByteArrayBody;
import khandroid.ext.apache.http.entity.mime.content.StringBody;
import khandroid.ext.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.matchimi.CommonUtilities;
import com.matchimi.HomeActivity;
import com.matchimi.ProfileModel;
import com.matchimi.R;
import com.matchimi.profile.FeedbacksActivity;
import com.matchimi.profile.PreferredJobsActivity;
import com.matchimi.registration.EditProfile;
import com.matchimi.registration.LoginActivity;
import com.matchimi.registration.ProfileRegistrationActivity;
import com.matchimi.registration.Utilities;
import com.matchimi.utils.ApplicationUtils;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

public class ProfileFragment extends Fragment {

	public static final int RC_EDIT_PROFILE = 90;
	public static final String EXTRA_TITLE = "title";

	private Context context;

	private JSONParser jsonParser = null;
	private String jsonStr = null;

	private ScrollView scrollView;
	private ProgressBar progressBar;
	private ProgressDialog progress;
	private SharedPreferences settings;
	private View view;

	private String pt_id = null;

	private Button preferredJobsButton;
	private Button feedbackButton;
	private Button logoutProfileButton;
	private final int TAKE_IMG_PROFILE = 0;
	private final int TAKE_PHOTO_CODE = 4;

	private String profileImagePath;

	private int selectedIdx = -1;
	private int selectedPhotoId = -1;

	private final int SELECT_PHOTO = 5;
	private String uploadImage = null;
	private String uploadURL = null;
	private String responseString = null;
	private ImageView avatarView;
	private Boolean isUpload = false;

	private TextView usernameView;
	private TextView emailView;
	private TextView nricView;
	private RatingBar ratingBar;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.user_profile, container, false);

		// Check if user not logged
		settings = getActivity().getSharedPreferences(
				CommonUtilities.PREFS_NAME, Context.MODE_PRIVATE);
		pt_id = settings.getString(CommonUtilities.USER_PTID, null);

		context = getActivity();

		progressBar = (ProgressBar) view.findViewById(R.id.progress);

		Button logoutProfileButton = (Button) view
				.findViewById(R.id.buttonLogoutProfileMenu);
		logoutProfileButton.setOnClickListener(logoutListener);

		scrollView = (ScrollView) view.findViewById(R.id.scrollView);

		// Set user profile
		avatarView = (ImageView) view.findViewById(R.id.profile_avatar);
		// Set user name
		usernameView = (TextView) view
				.findViewById(R.id.profile_username);

		// Set email name
		emailView = (TextView) view.findViewById(R.id.profile_email);

		// Set nric
		nricView = (TextView) view.findViewById(R.id.profile_nric);


		ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
		TextView editProfile = (TextView) view.findViewById(R.id.editProfile);
		editProfile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getActivity(), EditProfile.class);
				startActivityForResult(i, RC_EDIT_PROFILE);
			}
		});

		Button blockedCompanyButton = (Button) view
				.findViewById(R.id.profile_blocked_companies_button);
		blockedCompanyButton.setOnClickListener(blockedCompanyListener);

		preferredJobsButton = (Button) view
				.findViewById(R.id.preferred_jobs_button);
		preferredJobsButton.setOnClickListener(preferredJobsListener);

		feedbackButton = (Button) view.findViewById(R.id.feedback_button);
		feedbackButton.setOnClickListener(feedbackListener);

		if (savedInstanceState != null) {
			uploadImage = savedInstanceState.getString("uploadImage");
			uploadURL = savedInstanceState.getString("uploadURL");
			selectedPhotoId = savedInstanceState.getInt("selectedPhotoId");
			isUpload = true;
		}

		loadProfile();

		getActivity().registerReceiver(profileReceiver,
				new IntentFilter("profile.receiver"));

		return view;
	}

	private OnClickListener blockedCompanyListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(),
					BlockedCompaniesActivity.class);
			startActivity(intent);
		}
	};

	private OnClickListener logoutListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(CommonUtilities.TAG, "Logout clicked!");
			sendLogoutToAPI();
		}
	};

	private OnClickListener feedbackListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(), FeedbacksActivity.class);
			startActivity(intent);
		}
	};

	private OnClickListener preferredJobsListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(),
					PreferredJobsActivity.class);
			startActivity(intent);
		}
	};

	private void loadProfile() {
		progressBar.setVisibility(View.VISIBLE);
		scrollView.setVisibility(View.GONE);

		String picName = settings.getString(USER_PROFILE_PICTURE, null);
		Log.d(TAG, "picture: " + picName);

		if (getResources() != null) {
			if (picName != null
					&& picName != getResources().getString(
							R.string.image_not_found)) {
				String url = SERVERURL + CommonUtilities.API_GET_PROFILE_PIC
						+ "?" + PARAM_PT_ID + "=" + pt_id;
				checkAndDownloadPic(picName, url);
			}
		}

		// If returning from cameras
		if (isUpload) {
			isUpload = false;
			uploadImageToServer(uploadImage, uploadURL, selectedPhotoId);
		} else {
			loadData();
		}

		progressBar.setVisibility(View.GONE);
		scrollView.setVisibility(View.VISIBLE);
	}

	protected void sendLogoutToAPI() {
		final String url = CommonUtilities.SERVERURL + CommonUtilities.API_PART_TIMER_LOGGED_OUT;		
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				
				if (jsonStr != null) {
					Log.d(TAG, "Result from " + url + " " + jsonStr.toString());
					
					// FIXME: please check on server response
					if (jsonStr.trim().equalsIgnoreCase("0")) {
						SharedPreferences settings = getActivity().getSharedPreferences(
								PREFS_NAME, Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = settings.edit();
						editor.clear().commit();
						
						// Always create new editor instance after committed in previous step
						editor = settings.edit();
						editor.putBoolean(LOGIN, false);
						editor.putBoolean(CommonUtilities.IS_FIRSTTIME, true);
						editor.commit();

						Intent i = new Intent(getActivity().getApplicationContext(),
								LoginActivity.class);
						startActivity(i);
						getActivity().finish();

					} else if (jsonStr.trim().equalsIgnoreCase("1")) {
						Toast.makeText(context,
								getString(R.string.logged_error),
								Toast.LENGTH_LONG).show();
					} else {
						NetworkUtils.connectionHandler(getActivity(), jsonStr, "");
					}
					
				} else {
					Toast.makeText(context,
							getString(R.string.logged_error),
							Toast.LENGTH_LONG).show();
				}
			}
		};

		progress = ProgressDialog.show(context, getString(R.string.menu_logout),
				getString(R.string.logout_loading), true, false);
		new Thread() {
			public void run() {
				jsonParser = new JSONParser();
				
				try {
					String[] params = { "pt_id" };
					String[] values = { pt_id };
					jsonStr = jsonParser.getHttpResultUrlPost(url, params, values);

					Log.e(TAG, "Send logout to server with pt_id " + pt_id);
					
				} catch (Exception e) {
					jsonStr = null;
					Log.d(TAG, "Issue in generating Json profile");
				}

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}	
	
	// private void loadProfile() {
	// final String url = SERVERURL + CommonUtilities.API_GET_PROFILE + "?"
	// + PARAM_PT_ID + "=" + pt_id;
	//
	// final Handler mHandlerFeed = new Handler();
	// final Runnable mUpdateResultsFeed = new Runnable() {
	// public void run() {
	// if (jsonStr != null || jsonStr.length() > 0) {
	// try {
	// JSONObject obj = new JSONObject(jsonStr);
	// obj = obj.getJSONObject("part_timers");
	//
	// SharedPreferences.Editor editor = settings.edit();
	//
	// // Update settings
	// editor.putString(CommonUtilities.USER_FULLNAME,
	// obj.getString("full_name"));
	// editor.putString(USER_PROFILE_PICTURE, obj.getString("profile_picture"));
	// String studentMatricCard = obj.optString("matric_card_no");
	// if (studentMatricCard != null) {
	// editor.putString(USER_NRIC_NUMBER, studentMatricCard);
	// }
	// editor.putString(CommonUtilities.USER_EMAIL, obj.getString("email"));
	// editor.putInt(CommonUtilities.USER_RATING, obj.getInt("pt_grade_id"));
	// editor.commit();
	//
	// } catch (Exception e) {
	// Log.e(TAG, "Get profile error >> " + e.getMessage());
	// Toast.makeText(context,
	// getString(R.string.edit_get_profile_error),
	// Toast.LENGTH_SHORT).show();
	// }
	// } else {
	// Toast.makeText(context,
	// getString(R.string.server_error),
	// Toast.LENGTH_SHORT).show();
	// }
	//
	// String picName = settings.getString(USER_PROFILE_PICTURE, null);
	// Log.d(TAG, "picture: " + picName);
	//
	// if(getResources() != null) {
	// if(picName != null && picName !=
	// getResources().getString(R.string.image_not_found)) {
	// String url = SERVERURL + CommonUtilities.API_GET_PROFILE_PIC + "?"
	// + PARAM_PT_ID + "=" + pt_id;
	// checkAndDownloadPic(picName, url);
	// }
	// }
	//
	// // If returning from cameras
	// if(isUpload) {
	// isUpload = false;
	// uploadImageToServer(uploadImage, uploadURL, selectedPhotoId);
	// } else {
	// loadData();
	// }
	//
	//
	// progressBar.setVisibility(View.GONE);
	// scrollView.setVisibility(View.VISIBLE);
	// }
	// };
	//
	// progressBar.setVisibility(View.VISIBLE);
	// scrollView.setVisibility(View.GONE);
	//
	// new Thread() {
	// public void run() {
	// jsonParser = new JSONParser();
	// jsonStr = jsonParser.getHttpResultUrlGet(url);
	// mHandlerFeed.post(mUpdateResultsFeed);
	// }
	// }.start();
	// }

	private String checkAndDownloadPic(String imageStoragePath, String apiURL) {
		File f = new File(imageStoragePath);
		String filename = f.getName();

		File imageFile = new File(CommonUtilities.IMAGE_ROOT, filename);

		if (!imageFile.exists()) {
			String[] params = { apiURL, filename };
			new downloadWebPage().execute(params);
		} else {
			// Log.d(TAG, "Image exists");
		}

		return CommonUtilities.IMAGE_ROOT + filename;
	}

	private class downloadWebPage extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Log.d(TAG, "downloading profile picture");
			loadData();
		}

		@Override
		protected Void doInBackground(String... params) {
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(params[0]);
				HttpResponse response;
				response = httpClient.execute(httpGet);
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();

				String picName = settings.getString(USER_PROFILE_PICTURE, null);
				int idx = picName.lastIndexOf("/");
				picName = picName.substring(idx + 1);
				File f = new File(CommonUtilities.IMAGE_ROOT, picName);

				FileOutputStream output = new FileOutputStream(f);
				byte data[] = new byte[1024];
				int count;
				while ((count = is.read(data)) > 0) {
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				is.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, ">>> " + e.getMessage());
				e.printStackTrace();
			}
			return null;
		}

	}

	BroadcastReceiver profileReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			loadProfile();
		}
	};

	public static Bundle createBundle(String title) {
		Bundle bundle = new Bundle();
		bundle.putString(EXTRA_TITLE, title);
		return bundle;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		getActivity();

		if (resultCode == FragmentActivity.RESULT_OK) {
			if (requestCode == RC_EDIT_PROFILE) {
				loadData();
			}

			switch (requestCode) {
			case TAKE_PHOTO_CODE: {
				if (resultCode == FragmentActivity.RESULT_OK) {
					uploadAction(uploadImage, uploadURL, selectedPhotoId);
				}
				break;
			} // ACTION_TAKE_PHOTO_B

			case SELECT_PHOTO: {
				if (resultCode == FragmentActivity.RESULT_OK) {
					Uri selectedImageUri = data.getData();
					String selectedImagePath = getPath(selectedImageUri);
					
					ApplicationUtils.copyFile(selectedImagePath, uploadImage);
					uploadAction(uploadImage, uploadURL, selectedPhotoId);
				}
				break;
			} // ACTION_TAKE_PHOTO_S

			}
			// switch
		}
	}

	/**
	 * Execute upload to Server
	 */
	private void uploadAction(String uploadImage, String uploadURL,
			int selectedPhotoId) {
		if (uploadImage != null && uploadURL != null) {
			uploadImageToServer(uploadImage, uploadURL, selectedPhotoId);
			uploadImage = null;
			uploadURL = null;
			selectedPhotoId = -1;
		}
	}

	private void uploadImageToServer(final String filePath, final String url,
			final int outputPhotoId) {
		final Handler mHandlerFeed = new Handler();
		final Runnable mUpdateResultsFeed = new Runnable() {
			public void run() {
				if (responseString != null) {
					Toast.makeText(context, responseString, Toast.LENGTH_SHORT)
							.show();
				}

				// Update profile
				Intent iBroadcast = new Intent("profile.receiver");
				getActivity().sendBroadcast(iBroadcast);

				loadAvatar();
			}
		};

		progress = ProgressDialog.show(context,
				getString(R.string.profile_header),
				getString(R.string.uploading), true, false);

		new Thread() {
			public void run() {
				responseString = null;

				File file = new File(filePath);
				String filename = file.getName();

				Log.d(TAG, "Upload file " + filePath);
				Log.d(TAG, "Upload photo to " + url);

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;
				options.inScaled = false;
				Bitmap bm = BitmapFactory.decodeFile(filePath, options);

				// Fix S3 bug image orientation
				ExifInterface exif;
				try {
					exif = new ExifInterface(filePath);

					Log.d(TAG,
							"Exif value "
									+ exif.getAttribute(ExifInterface.TAG_ORIENTATION));
					if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
							.equalsIgnoreCase("6")) {
						bm = imageRotate(bm, 90);
					} else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
							.equalsIgnoreCase("8")) {
						bm = imageRotate(bm, 270);
					} else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
							.equalsIgnoreCase("3")) {
						bm = imageRotate(bm, 180);
					}

				} catch (IOException e) {
					JSONObject json = new JSONObject();
					// TODO Auto-generated catch block
					try {
						json.put("status", CommonUtilities.FILECORRUPT);
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					responseString = NetworkUtils.connectionHandlerString(
							context, jsonStr.toString(), e.getMessage());
					Log.e(CommonUtilities.TAG, "Error uploading image "
							+ " >> " + e.getMessage());
				}

				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bm.compress(CompressFormat.JPEG, 75, bos);

					byte[] data = bos.toByteArray();

					// Replacing image in local storage
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(data);

					HttpClient httpClient = new DefaultHttpClient();
					HttpPost postRequest = new HttpPost(url);

					ByteArrayBody bab = new ByteArrayBody(data, filename);
					MultipartEntity reqEntity = new MultipartEntity(
							HttpMultipartMode.BROWSER_COMPATIBLE);
					reqEntity.addPart("file", bab);
					reqEntity.addPart("filename", new StringBody(filename));
					reqEntity.addPart("pt_id", new StringBody(pt_id));

					postRequest.setEntity(reqEntity);
					HttpResponse response = httpClient.execute(postRequest);
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent(), "UTF-8"));
					String sResponse;
					StringBuilder s = new StringBuilder();

					while ((sResponse = reader.readLine()) != null) {
						s = s.append(sResponse);
					}
				
					renameFile(filePath, s.toString());

					switch (outputPhotoId) {
						case TAKE_IMG_PROFILE:
							// Update user profile picture
							SharedPreferences.Editor editor = settings.edit();
							editor.putString(CommonUtilities.USER_PROFILE_PICTURE,
									s.toString());
							editor.commit();
	
							break;
					}

				} catch (IOException e) {
					JSONObject json = new JSONObject();
					try {
						json.put("status", CommonUtilities.NOINTERNET);
						responseString = NetworkUtils.connectionHandlerString(
								context, jsonStr.toString(), e.getMessage());
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					Log.e(CommonUtilities.TAG, "Error uploading image "
							+ " >> " + e.getMessage());

				} catch (Exception e) {
					Log.e(CommonUtilities.TAG,
							"Error uploading image" + e.getMessage());
				}

				if (progress != null && progress.isShowing()) {
					progress.dismiss();
					mHandlerFeed.post(mUpdateResultsFeed);
				}
			}
		}.start();
	}

	public static Bitmap imageRotate(Bitmap bitmap, int degree) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		Matrix mtx = new Matrix();
		mtx.postRotate(degree);

		return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}

	private void renameFile(String oldPath, String imagePath) {
		String newFilename = new File(imagePath).getName();
		String newPath = CommonUtilities.IMAGE_ROOT + newFilename;
		boolean isRenamed = false;

		File f = null;
		File f2 = null;

		try {
			f = new File(oldPath);
			f2 = new File(newPath);
			isRenamed = f.renameTo(f2);
		} catch (Exception e) {
			// if any error occurs
			e.printStackTrace();
		}
	}

	private void loadData() {
		// Set user name
		String userName = settings.getString(CommonUtilities.USER_FULLNAME, "");
		usernameView.setText(userName);

		// Set email name
		String userEmail = settings.getString(CommonUtilities.USER_EMAIL, "");
		emailView.setText(userEmail);

		// Set nric
		String userNRIC = settings.getString(CommonUtilities.USER_NRIC_NUMBER,
				"");

		if (userNRIC == "") {
			nricView.setVisibility(View.GONE);
		} else {
			nricView.setText(userNRIC);
			nricView.setVisibility(View.VISIBLE);
		}

		nricView.setText(userNRIC);

		// Set user profile onclick
//		avatarView.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				askImageFrom(TAKE_IMG_PROFILE);
//			}
//		});

		ratingBar.setRating(settings.getInt(CommonUtilities.USER_RATING, 0));

		loadAvatar();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(profileReceiver);
	}

	private void loadAvatar() {
		String picName = settings.getString(USER_PROFILE_PICTURE, null);
		if (picName != null) {

			int idx = picName.lastIndexOf("/");
			picName = picName.substring(idx + 1);
			File f = new File(CommonUtilities.IMAGE_ROOT, picName);

			String facebookID = settings.getString(
					CommonUtilities.USER_FACEBOOK_ID, "");
			String profilePic = settings.getString(
					CommonUtilities.USER_PROFILE_PICTURE, "");

			if (profilePic.length() > 0) {
				if (f.exists()) {
					Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());
					avatarView.setImageBitmap(b);
				}
			} else {
				if (facebookID.length() > 0) {
					Utilities util = new Utilities();
					util.downloadAvatar(settings.getString(
							CommonUtilities.USER_FACEBOOK_ID, ""), avatarView);
				}
			}
		}
	}

	/**
	 * Listener for IC Front, IC Back, Profile and Matric card listener
	 * 
	 * @param idx
	 */
	protected void askImageFrom(final int idx) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		CharSequence[] choice = { "Gallery", "Camera" };
		builder.setTitle(R.string.select_image_from);
		builder.setItems(choice, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				if (arg1 == 0) {
					takeImageFromGallery(idx);
				} else {
					Log.d(TAG, "Take from camera with code: " + idx);
					takeImageFromCamera(idx);
				}
			}
		});

		Dialog dialog = builder.create();
		dialog.show();
	}

	protected void takeImageFromCamera(int idx) {
		uploadImage = "";
		selectedPhotoId = idx;
		uploadURL = CommonUtilities.SERVERURL;
		String selectedFileName = "";

		switch (idx) {

		case TAKE_IMG_PROFILE:
			selectedFileName = CommonUtilities.FILE_IMAGE_PROFILE + ".jpg";
			uploadImage = CommonUtilities.IMAGE_ROOT + selectedFileName;
			uploadURL += CommonUtilities.API_UPLOAD_PROFILE_PICTURE_BY_PT_ID;
			break;

		}

		File newfile = new File(uploadImage);
		try {
			newfile.createNewFile();
		} catch (Exception e) {
			Log.e("takeImageFromCamera", ">>> " + e.getMessage());
		}

		Uri outputFileUri = Uri.fromFile(newfile);
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
	}

	protected void takeImageFromGallery(int idx) {
		// TODO Auto-generated method stub
		selectedPhotoId = idx;
		uploadImage = "";
		selectedPhotoId = idx;
		uploadURL = CommonUtilities.SERVERURL;
		String selectedFileName = "";

		switch (selectedPhotoId) {

		case TAKE_IMG_PROFILE:
			selectedFileName = CommonUtilities.FILE_IMAGE_PROFILE + ".jpg";
			uploadImage = CommonUtilities.IMAGE_ROOT + selectedFileName;
			uploadURL += CommonUtilities.API_UPLOAD_PROFILE_PICTURE_BY_PT_ID;
			break;

		}

		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"),
				SELECT_PHOTO);
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getActivity().managedQuery(uri, projection, null, null,
				null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		// Log.e(TAG, "onPause !!!");
		super.onPause();

		Bundle outState = new Bundle();
		outState.putString("uploadImage", uploadImage);
		outState.putString("uploadURL", uploadURL);
		outState.putInt("selectedPhotoId", selectedPhotoId);

		onSaveInstanceState(outState);
	}

}
