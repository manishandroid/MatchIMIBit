package com.matchimi.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.matchimi.CommonUtilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Display;

public class ApplicationUtils {

	public static String getAppRootDir() {
		File rootDir = Environment.getExternalStorageDirectory();
		File rootFile = new File(rootDir, CommonUtilities.ROOT_DIR);
		if (!rootFile.exists() || !rootFile.isDirectory()) {
			rootFile.mkdir();
		}
		return rootFile.getAbsolutePath();
	}

	public static void restartApp(Context context) {
		Intent i = context.getPackageManager().getLaunchIntentForPackage(
				context.getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(i);
	}

	public static int getThemeDialog(boolean light) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return light ? android.R.style.Theme_DeviceDefault_Light_Dialog
					: android.R.style.Theme_DeviceDefault_Dialog;
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return light ? android.R.style.Theme_Holo_Light_Dialog
					: android.R.style.Theme_Holo_Dialog;
		} else {
			return android.R.style.Theme_Dialog;
		}
	}

	public static int getTheme(boolean light) {
		return light ? com.actionbarsherlock.R.style.Theme_Sherlock_Light
				: com.actionbarsherlock.R.style.Theme_Sherlock;
	}

	public static void copyFile(String src, String dst) {
		try {
			FileInputStream fis = new FileInputStream(src);
			FileOutputStream fos = new FileOutputStream(dst);

			byte[] buff = new byte[1024];
			int read;
			while ((read = fis.read(buff)) != -1) {
				fos.write(buff, 0, read);
			}

			fos.flush();
			fos.close();
			fis.close();
		} catch (Exception e) {
			Log.e("CopyFile", ">>> " + e.getMessage());
		}
	}
	
	public static Bitmap resizeBitmap(String filename, int height) {
		Bitmap originalBitmap = BitmapFactory.decodeFile(filename);
		Bitmap res = null;
		
		if (originalBitmap != null) {
			int heightofBitMap = originalBitmap.getHeight();
			int widthofBitMap = originalBitmap.getWidth();

			heightofBitMap = height;
			widthofBitMap = height * widthofBitMap / heightofBitMap;

			// Scaling the bitmap according to new height and width
			res = Bitmap.createScaledBitmap(originalBitmap, widthofBitMap, heightofBitMap, true);
		}

		return res;
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static int getColumnNumber(Activity a, int w) {
		int numOfColumn = 0;
		/* prepare max screen width */
		Display display = a.getWindowManager().getDefaultDisplay();
		int availableWidth = 0;
		try {
			Point size = new Point();
			display.getSize(size);
			availableWidth = size.x;
		} catch (NoSuchMethodError e) {
			availableWidth = display.getWidth();
		}

		numOfColumn = availableWidth / (w + 6);
		
		return numOfColumn;
	}
}
