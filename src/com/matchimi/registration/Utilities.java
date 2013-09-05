package com.matchimi.registration;

import static com.matchimi.CommonUtilities.TAG;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

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
