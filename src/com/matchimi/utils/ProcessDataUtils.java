package com.matchimi.utils;

import org.json.JSONArray;
import org.json.JSONException;

public final class ProcessDataUtils {
	
	/**
	 * Parsing array string requirements
	 * @param requirements
	 * @return
	 */
	public static final String parseRequirement(String requirements) {
		String requirementDetail = "";
		
		try {
			JSONArray requirementItems = new JSONArray(requirements);
			if (requirementItems != null && requirementItems.length() > 0) {
				for (int i = 0; i < requirementItems.length(); i++) {		
					if(requirementItems.getString(i).length() > 0) {
						requirementDetail += (i + 1) + ". " + requirementItems.getString(i);
						requirementDetail += "\n";
					}
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return requirementDetail;
	}
}
