package com.matchimi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import com.matchimi.CommonUtilities;

import android.util.Log;

public class DatabaseStorage {
	private String userName;
	private String filePath = "userconfig.properties";
	
	public DatabaseStorage() {
		Properties prop = new Properties();

		try {
			File in = new File(filePath);
			if (!in.exists()) {
				in.createNewFile();
			}

			// load a properties file
			prop.load(new FileInputStream(filePath));
			String users = prop.getProperty("username");

			if (users == null) {
				this.userName = "";
			} else {
				// get the property value and print it out
				this.userName = prop.getProperty("username");
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	// Get User Devices
	public String getUsername() {
		return this.userName;
	}

	public void register(String userName) {
		this.userName = userName;		
		updateUser();
	}


	/**
	 * Create new register ID
	 * 
	 * @param regId
	 */
	public void updateUser() {
		try {
			Properties prop = new Properties();
			prop.setProperty("username", this.userName);

			// save properties to project root folder
			prop.store(new FileOutputStream(filePath), null);
			Log.v(CommonUtilities.TAG, "UDPATE USERNAME  " + this.userName);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
