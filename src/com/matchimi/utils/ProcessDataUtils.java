package com.matchimi.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;
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
	

	public static final List<String> convertStringToList(String listString) {
		listString = listString.replaceAll("\\s+","");
		List<String> listObject = new ArrayList<String>();
		listString = listString.replace("[", "").replace("]", "");
		String[] split = listString.split(",");
		listObject = Arrays.asList(split);
		
		return listObject;
	}
	
	public static final List<String> convertStringToListWithSpace(String listString) {
		listString = listString.replaceAll("\\s+"," ");
		List<String> listObject = new ArrayList<String>();
		listString = listString.replace("[", "").replace("]", "");
		String[] split = listString.split(",");
		listObject = Arrays.asList(split);
		
		return listObject;
	}
	
	public static final String convertListStringToComma(String input) {
		return input.toString().replace("[", "").replace("]", "").replace(", ", ",");
	}
	

	public static final Calendar generateCalendar(String str) {
		Calendar calRes = new GregorianCalendar(Integer.parseInt(str.substring(
				0, 4)), Integer.parseInt(str.substring(5, 7)) - 1,
				Integer.parseInt(str.substring(8, 10)), Integer.parseInt(str
						.substring(11, 13)), Integer.parseInt(str.substring(14,
						16)), Integer.parseInt(str.substring(17, 19)));

		return calRes;
	}
	
	public static boolean sameDay(Calendar calendar, Calendar selectedCal) {
		return calendar.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR)
				&& calendar.get(Calendar.DAY_OF_YEAR) == selectedCal
						.get(Calendar.DAY_OF_YEAR);
	}
	
	public static boolean compareSameDayDate(Date start, Date end) {
		// Check if selected start - end date is a range
		Calendar availabilityStartCal = Calendar.getInstance();
		availabilityStartCal.setTime(start);
		
		Calendar availabilityEndCal = Calendar.getInstance();
		availabilityEndCal.setTime(end);

		if(!ProcessDataUtils.sameDay(availabilityStartCal, availabilityEndCal)) {
			return false;
		} else {
			return true;
		}
	}

}
