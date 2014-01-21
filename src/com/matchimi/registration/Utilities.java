package com.matchimi.registration;

import static com.matchimi.CommonUtilities.API_RESEND_VERIFICATION_EMAIL;
import static com.matchimi.CommonUtilities.PARAM_PROFILE_DATE_OF_BIRTH;
import static com.matchimi.CommonUtilities.PARAM_PROFILE_GENDER;
import static com.matchimi.CommonUtilities.PARAM_PROFILE_IC_TYPE;
import static com.matchimi.CommonUtilities.PARAM_PROFILE_IC_TYPE_ID;
import static com.matchimi.CommonUtilities.PARAM_PROFILE_IS_VERIFIED;
import static com.matchimi.CommonUtilities.PARAM_PROFILE_PHONE_NUMBER;
import static com.matchimi.CommonUtilities.PARAM_PROFILE_WORK_EXPERIENCE;
import static com.matchimi.CommonUtilities.SERVERURL;
import static com.matchimi.CommonUtilities.TAG;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import khandroid.ext.apache.http.HttpResponse;
import khandroid.ext.apache.http.client.HttpClient;
import khandroid.ext.apache.http.client.methods.HttpPost;
import khandroid.ext.apache.http.entity.mime.HttpMultipartMode;
import khandroid.ext.apache.http.entity.mime.MultipartEntity;
import khandroid.ext.apache.http.entity.mime.content.ByteArrayBody;
import khandroid.ext.apache.http.entity.mime.content.StringBody;
import khandroid.ext.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.JSONParser;
import com.matchimi.utils.NetworkUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class Utilities {
	
	boolean isEmailValid(CharSequence email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}

	public synchronized void downloadAvatar(final String user_id,
			final ImageView iv) {
		AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {
				URL fbAvatarUrl = null;
				Bitmap fbAvatarBitmap = null;
				try {
					fbAvatarUrl = new URL("http://graph.facebook.com/"
							+ user_id + "/picture?type=large");
					Log.d(TAG, "Image from " + fbAvatarUrl.toString());

					fbAvatarBitmap = BitmapFactory.decodeStream(fbAvatarUrl
							.openConnection().getInputStream());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return fbAvatarBitmap;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				Log.d(TAG, "Facebook image from " + result.toString());
				iv.setImageBitmap(result);
			}

		};
		task.execute();
	}
	
	
	public synchronized void facebookProfilePicture(final String user_id, final String pt_id) {
		AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {
				URL fbAvatarUrl = null;
				Bitmap fbAvatarBitmap = null;
				try {
					fbAvatarUrl = new URL("http://graph.facebook.com/"
							+ user_id + "/picture?type=large");
					Log.d(TAG, "Image from " + fbAvatarUrl.toString());

					fbAvatarBitmap = BitmapFactory.decodeStream(fbAvatarUrl
							.openConnection().getInputStream());
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				return fbAvatarBitmap;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				Object[] params = { result, pt_id };
				new uploadProfilePic().execute(params);
			}

		};
		task.execute();
	}
	
	/**
	 * Upload bitmap into profile picture to server
	 *
	 */
	private class uploadProfilePic extends AsyncTask<Object, Void, String> {
		
		@Override
		public String doInBackground(Object... params) {
			Bitmap bm = (Bitmap) params[0];
			String pt_id = (String) params[1];
			String result = "";
			
			boolean isRenamed = false;
			
			try {
				String url = CommonUtilities.SERVERURL + CommonUtilities.API_UPLOAD_PROFILE_PICTURE_BY_PT_ID;
				String selectedFileName = CommonUtilities.FILE_IMAGE_PROFILE + ".jpg";
				String filePath = CommonUtilities.IMAGE_ROOT + selectedFileName;
				
				File file = new File(filePath);
				String filename = file.getName();
				
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
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));
				String sResponse;
				StringBuilder s = new StringBuilder();

				while ((sResponse = reader.readLine()) != null) {
					s = s.append(sResponse);
				}
				
				String imagePath = s.toString();
				Log.d(TAG, "Upload result: " + imagePath);
				
				String newFilename = new File(imagePath).getName();
				String newPath = CommonUtilities.IMAGE_ROOT + newFilename;
				
				File f = null;
			    File f2 = null;
				
				try {
//					f = new File(filePath);
					String profileFilename = CommonUtilities.FILE_IMAGE_PROFILE + pt_id + ".jpg";
					f = new File(CommonUtilities.IMAGE_ROOT, profileFilename);
					
					f2 = new File(newPath);					
					isRenamed = f.renameTo(f2);
				
				} catch(Exception e){
			         // if any error occurs
			         e.printStackTrace();
			    }
				
				if(isRenamed) {
					result = imagePath;
				}
				
			} catch (IOException e) {
				Log.e(CommonUtilities.TAG, "Network error  " + " >> " + e.getMessage());
			
			} catch (Exception e) {
				Log.e(CommonUtilities.TAG, "Error uploading image" + e.getMessage());
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "API profile result :" + result);
		}
	}

}
