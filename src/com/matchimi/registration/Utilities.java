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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.matchimi.CommonUtilities;
import com.matchimi.R;
import com.matchimi.utils.JSONParser;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
				Log.d(TAG, "Image from " + result.toString());
				iv.setImageBitmap(result);
			}

		};
		task.execute();
	}
	

	
}
